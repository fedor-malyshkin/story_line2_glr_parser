package ru.nlp_project.story_line2.glr_parser;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import org.apache.commons.collections4.Factory;
import org.apache.commons.collections4.map.LazyMap;

import ru.nlp_project.story_line2.glr_parser.IConfigurationManager.FactConfiguration;
import ru.nlp_project.story_line2.glr_parser.IConfigurationManager.FactConfigurationEntry;

public class InterpreterImpl implements IInterpreter {

	public class Fact {
		private Map<String, FactField> fieldsMap = new TreeMap<String, InterpreterImpl.FactField>();

		private String name;

		public Fact(String name) {
			super();
			this.name = name;
		}

		public void addField(FactField field) {
			fieldsMap.put(field.name, field);
		}

		public Fact cloneEmpty() {
			Fact result = new Fact(this.name);
			for (FactField field : fieldsMap.values())
				result.addField(field.cloneEmpty());
			return result;
		}

		public FactField getField(String field) {
			return fieldsMap.get(field);
		}

		public Map<String, FactField> getFieldsMap() {
			return fieldsMap;
		}

		public String getFieldValue(String field) {
			FactField factField = fieldsMap.get(field);
			if (null == factField)
				throw new IllegalStateException("not existing field name:" + field);
			return factField.value;
		}

		public String getName() {
			return name;
		}

		public void setFieldValue(String field, String value, int from, int length) {
			FactField factField = fieldsMap.get(field);
			if (null == factField)
				throw new IllegalStateException("not existing field name:" + field);
			factField.value = value;
			factField.from = from;
			factField.length = length;
		}

		@Override
		public String toString() {
			return "Fact [name=" + name + ", fieldsMap=" + fieldsMap + "]";
		}

	}

	public class FactField {
		/**
		 * Начало действия (ед. измерения в токенах)
		 */
		int from;
		/**
		 * Длинна (ед. измерения в токенах)
		 */
		int length;

		String name;

		String value;

		public FactField cloneEmpty() {
			FactField result = new FactField();
			result.from = this.from;
			result.length = this.length;
			result.name = this.name;
			return result;
		}

		public int getFrom() {
			return from;
		}

		public int getLength() {
			return length;
		}

		public String getName() {
			return name;
		}


		public String getValue() {
			return value;
		}

		@Override
		public String toString() {
			return "[" + name + "=" + value + "]";
		}

	}


	public class FactListFacade extends Fact {

		private List<Fact> facts;

		public FactListFacade(List<Fact> intersectedFacts) {
			super(null);
			this.facts = intersectedFacts;
		}

		public FactListFacade(String name) {
			super(name);
		}

		@Override
		public void setFieldValue(String field, String value, int from, int length) {
			for (Fact f : facts)
				f.setFieldValue(field, value, from, length);
		}

	}

	public class FactManager {
		private Factory<List<Fact>> lazyFactMapFactory = new Factory<List<Fact>>() {
			public List<Fact> create() {
				return new LinkedList<Fact>();
			}
		};

		private Map<String, List<Fact>> innerFactsMap =
				LazyMap.lazyMap(new HashMap<String, List<Fact>>(), lazyFactMapFactory);

		/**
		 * Fact occurence counter.
		 */
		Map<String, AtomicInteger> occCounter = new HashMap<String, AtomicInteger>();

		private Map<String, String> previousFactFieldsMap;
		private List<Fact> previousFacts;
		private List<TreeInterpData> treeInterpDatas;

		public FactManager(List<TreeInterpData> treeInterpDatas,
				InterpretationResult interpretationResult) {
			this.treeInterpDatas = treeInterpDatas;
			if (interpretationResult != null)
				this.previousFacts = interpretationResult.getFacts();
		}

		private void checkNotExistingFactAndFields() {
			for (TreeInterpData treeInterpData : treeInterpDatas) {
				Fact fact = factsMap.get(treeInterpData.fact);
				if (null == fact)
					throw new IllegalStateException("Incorrect fact '" + treeInterpData.fact + "'");
				FactField field = fact.getField(treeInterpData.field);
				if (null == field)
					throw new IllegalStateException(
							"Incorrect field '" + treeInterpData.field + "'");
			}

		}

		private void countFactAndFields() {
			for (TreeInterpData treeInterpData : treeInterpDatas) {
				AtomicInteger atomicInteger = occCounter.get(treeInterpData.fullName);
				if (atomicInteger == null) {
					atomicInteger = new AtomicInteger(0);
					occCounter.put(treeInterpData.fullName, atomicInteger);
				}
				atomicInteger.incrementAndGet();
			}
		}

		public void generateFacts() {
			for (TreeInterpData treeInterpData : treeInterpDatas) {
				Fact f = getFactWithEmptyField(treeInterpData.fact, treeInterpData.field,
						treeInterpData.from, treeInterpData.length);

				if (treeInterpData.interpData.getFromFactName() != null)
					// { ZZZZ.ZZZZZ from YYY.YYY }
					f.setFieldValue(treeInterpData.field,
							previousFactFieldsMap.get(treeInterpData.interpData.getFromFactName()
									+ "." + treeInterpData.interpData.getFromFieldName()),
							treeInterpData.from, treeInterpData.length);
				else if (treeInterpData.interpData.getValue() != null)
					// { YYY.YYYY = 'value'}
					f.setFieldValue(treeInterpData.field, treeInterpData.interpData.getValue(),
							treeInterpData.from, treeInterpData.length);
				else
					// { YYYY.YYYY }
					// use #setFieldValue in mandatory case - reimplemented in
					// FactListFacade
					f.setFieldValue(treeInterpData.field, generateFactValue(treeInterpData),
							treeInterpData.from, treeInterpData.length);
			}

			// check for 1 time occurences - copy in all fact entries
			for (Entry<String, AtomicInteger> entry : occCounter.entrySet()) {
				if (entry.getValue().get() == 1) {
					String fact = SymbolInterpData.extracFactName(entry.getKey());
					String field = SymbolInterpData.extracFieldName(entry.getKey());
					FactField ff = innerFactsMap.get(fact).get(0).getField(field);
					for (Fact f : innerFactsMap.get(fact))
						f.setFieldValue(field, ff.value, ff.from, ff.length);
				}
			}
		}

		public List<Fact> getFacts() {
			List<Fact> result = new LinkedList<InterpreterImpl.Fact>();
			for (Entry<String, List<Fact>> entry : innerFactsMap.entrySet())
				result.addAll(entry.getValue());
			return result;
		}

		/**
		 * Get fact with empty field name from {@link #innerFactsMap}, append to list in neccessary
		 * case.
		 * 
		 * In addition to it check all previous _intersectioned_ facts and get them to value
		 * replacement (can intersect only fact wich was added previusly with the same field).
		 * 
		 * @param fact
		 * @param field
		 * @param from
		 * @param length
		 * @return
		 */
		private Fact getFactWithEmptyField(String fact, String field, int from, int length) {
			Fact result = null;
			List<Fact> list = innerFactsMap.get(fact);
			List<Fact> intersectedFacts = new LinkedList<Fact>();
			for (Fact f : list) {
				if (intersect(f.getField(field), from, length)) {
					intersectedFacts.add(f);
				}

				if (f.getFieldValue(field) == null) {
					result = f;
					break;
				}
			}
			// if there is intersected fact return them for value replacements
			if (!intersectedFacts.isEmpty())
				return new FactListFacade(intersectedFacts);

			if (result == null) {
				// if not - append to list and return new fact
				Fact fact2 = factsMap.get(fact).cloneEmpty();
				list.add(fact2);
				return fact2;
			} else
				return result;
		}

		public void initialize() {
			storeToMapPreviousFacts();
			checkNotExistingFactAndFields();
			countFactAndFields();
		}

		private boolean intersect(FactField field1, int from, int length) {
			int start = Math.min(field1.from, from);
			int end = Math.max(field1.from + field1.length, from + length);
			return end - start <= field1.length + length;
		}

		/**
		 * Store prevoius facts in map for latter access.
		 */
		private void storeToMapPreviousFacts() {
			if (previousFacts == null)
				return;
			previousFactFieldsMap = new HashMap<String, String>();
			for (int i = previousFacts.size() - 1; i >= 0; i--) {
				Fact fact = previousFacts.get(i);
				for (FactField factField : fact.fieldsMap.values())
					previousFactFieldsMap.put(fact.name + "." + factField.name, factField.value);
			}
		}

	}

	public class InterpretationResult {

		private List<Fact> facts;

		public InterpretationResult(List<Fact> facts) {
			this.facts = facts;
		}

		public List<Fact> getFacts() {
			return facts;
		}

		@Override
		public String toString() {
			return "InterpretationResult [" + facts + "]";
		}

	}

	class TreeInterpData {
		String fact;
		String field;
		/**
		 * Начало действия (ед. измерения в токенах)
		 */
		int from;
		String fullName;
		SymbolInterpData interpData;
		/**
		 * Длинна (ед. измерения в токенах)
		 */
		int length;
		private ParseTreeNode node;

		public TreeInterpData(int from, int length, String fact, String field,
				SymbolInterpData interpData, ParseTreeNode node) {
			super();
			this.from = from;
			this.length = length;
			this.fullName = fact + "." + field;
			this.fact = fact;
			this.field = field;
			this.interpData = interpData;
			this.node = node;
		}

		public TreeInterpData(int from, int length, SymbolInterpData interpData,
				ParseTreeNode node) {
			super();
			this.from = from;
			this.length = length;
			this.interpData = interpData;
			this.node = node;
			this.fact = this.interpData.getFactName();
			this.field = this.interpData.getFieldName();
			this.fullName = this.fact + "." + this.field;
		}

		@Override
		public String toString() {
			return String.format("<%d,%d %s>", from, length, fullName);
		}

	}

	public static final String PARAM_NO_NORM = "no-norm";


	@Inject
	public ITokenManager tokenManager;
	@Inject
	public IConfigurationManager configurationManager;

	@Inject
	public InterpreterImpl() {}

	private Map<String, Fact> factsMap;
	private ParseTreeSerializer serializer;

	public String generateFactValue(TreeInterpData treeInterpData) {
		return serializer.serialize(treeInterpData.node,
				!PARAM_NO_NORM.equalsIgnoreCase(treeInterpData.interpData.getParam()));
	}

	public Map<String, Fact> getFactsMap() {
		return factsMap;
	}

	/**
	 * Testing purposes only (not for production!!!).
	 * 
	 * @param dictionaryManager
	 */
	@Deprecated
	public ParseTreeSerializer getParseTreeSerializer() {
		return serializer;
	}

	public void initialize() {
		serializer = ParseTreeSerializer.newInstance(tokenManager);
		readFact();
	}

	TreeInterpData newTreeInterpData(int from, int length, String fact, String field,
			SymbolInterpData interpData, ParseTreeNode parseTreeNode) {
		return new TreeInterpData(from, length, fact, field, interpData, parseTreeNode);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ru.nlp_project.story_line2.glr_parser.IInterpreter#processEntrances(ru.nlp_project.
	 * story_line2.glr_parser.SentenceProcessingContext, java.util.List, java.util.List)
	 */
	@Override
	public void processEntrances(SentenceProcessingContext context,
			List<GrammarKeywordEntrance> grammarEntrances, List<Token> tokens) {
		context.getLogger().startFactProcessing(context);
		// Process each entrance (and associated parse tree)
		for (GrammarKeywordEntrance grammarKeywordEntrance : grammarEntrances) {
			// extract interpretation instructions
			List<TreeInterpData> treeInterpDatas = null;
			try {
				treeInterpDatas = readEntranceTree(grammarKeywordEntrance.getParseTreeNode());
			} catch (Exception e) {
				context.getLogger().error(e.getMessage(), e);
			}
			// process instructions (check some pre-conditions) and generate facts
			List<Fact> facts = processTreeInterpDatas(treeInterpDatas,
					grammarKeywordEntrance.getInterpretationResult());
			grammarKeywordEntrance.setInterpretationResult(new InterpretationResult(facts));

			IFactListener factListener = context.getFactListener();
			for (Fact fact : facts)
				factListener.factExtracted(context, fact);

		}
		context.getLogger().endFactProcessing(context);
	}

	protected List<Fact> processTreeInterpDatas(List<TreeInterpData> treeInterpDatas) {
		return processTreeInterpDatas(treeInterpDatas, null);
	}

	/**
	 * Выполнить обработку данных, полученных при анализе деревьев.
	 * 
	 * При этом учитываются моменты, отраженные в описании tomita-parser:
	 * https://tech.yandex.ru/tomita/doc/dg/concept/interpretation-docpage/
	 * 
	 * @param treeInterpDatas
	 * @return
	 */
	protected List<Fact> processTreeInterpDatas(List<TreeInterpData> treeInterpDatas,
			InterpretationResult interpretationResult) {
		FactManager factManager = new FactManager(treeInterpDatas, interpretationResult);
		factManager.initialize();
		factManager.generateFacts();
		return factManager.getFacts();
	}

	List<TreeInterpData> readEntranceTree(ParseTreeNode parseTreeNode) throws Exception {
		List<TreeInterpData> result = new LinkedList<InterpreterImpl.TreeInterpData>();
		parseTreeNode.walkPostOrderLeafFirst(new ParseTreeNode.IPostOrderWalkLeafFirstProcessor() {
			@Override
			public void processNode(ParseTreeNode node) {
				if (node.symbol != null
						&& SymbolExt.class.isAssignableFrom(node.symbol.getClass())) {
					SymbolExt s = (SymbolExt) node.symbol;
					if (s.getInterpDatas() != null)
						for (SymbolInterpData id : s.getInterpDatas())
							result.add(
									new TreeInterpData(node.tokenFrom, node.tokenLength, id, node));
				}
			}
		});
		return result;
	}

	protected void readFact() {
		factsMap = new HashMap<String, Fact>();
		FactConfiguration configuration = configurationManager.getFactConfiguration();


		for (FactConfigurationEntry entry : configuration.factEntries) {
			String name = entry.name;
			if (name == null)
				throw new IllegalStateException("Fact with no name: " + entry.toString());
			Fact fact = new Fact(name);
			for (String fieldEntry : entry.fields) {
				FactField field = new FactField();
				field.name = fieldEntry;
				fact.addField(field);
			}
			factsMap.put(fact.name, fact);
		}
	}

	/**
	 * Testing purposes only (not for production!!!).
	 * 
	 * @param dictionaryManager
	 */
	@Deprecated
	public void setParseTreeSerializer(ParseTreeSerializer serializer) {
		this.serializer = serializer;
	}

}
