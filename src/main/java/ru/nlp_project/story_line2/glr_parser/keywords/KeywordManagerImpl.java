package ru.nlp_project.story_line2.glr_parser.keywords;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrTokenizer;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.JCommander;

import ru.nlp_project.story_line2.glr_parser.IConfigurationManager;
import ru.nlp_project.story_line2.glr_parser.SymbolExtData.SymbolExtDataTypes;
import ru.nlp_project.story_line2.glr_parser.Token;
import ru.nlp_project.story_line2.glr_parser.TokenManagerImpl;
import ru.nlp_project.story_line2.morph.GrammemeEnum;
import ru.nlp_project.story_line2.morph.GrammemeUtils;
import ru.nlp_project.story_line2.morph.Grammemes;

/**
 * Основной класс для работы с ключевыми словами (чтение слов из файлов, составление внутренних
 * структур, поиск в предложениях, составление оптимального распределения по предложению (с условием
 * максимального покрытия)).
 * 
 * Общий алгоритм работы такой:
 * <p>
 * Упрощенно: для каждого набора ключей (keyword set) создается дерево (trie) потом по ним
 * осуществляется поиск слов на предмет вхождения.
 * </p>
 * Упрощенное описание не описывает следующие сложные моменты:
 * <ul>
 * <li>порядок работ с многословными ключами;</li>
 * <li>порядок работы с омонимичными тэгами;</li>
 * <li>ситуации вхождения слова в нескольких позициях в предложении;</li>
 * <li>ситуации перекрывающихся вхождений слов;</li>
 * <li>порядок поиска слов в точной форме.</li>
 * </ul>
 * <p>
 * Фактически для разрешения этого каждое слово набора ключей (как и сам набор) нумеруется, а в
 * случае многословного ключа (multiword key) нумеруется и слово в ключе, указанная информация
 * (среди прочего) вносится для каждого слова/слова ключа в соответствующий объект, прикрепленный к
 * внешним узлам дерева. Указанные данные используются для определения вхождения или границ
 * вхождения. Кроме этого для определения всех вхождений, включая перекрывающиеся, поиск идет по
 * всем направлениям для всех слов, что реализуется с использованием специального объекта
 * ({@link CoverageVariant}), сохраняющего в себе статус поиска, который в случае успеха создает
 * записи о вхождении (многословных) ключей в текст.
 * </p>
 * Порядок построения такой:
 * <ul>
 * <li>Для однословных ключей в дерево просто вносится слово с соответствующим объектом данных;</li>
 * <li>Для многословных ключей в дерево вносится каждое слово с соответствующим объектом данных (с
 * указанием на вхождение в набор ключей и номер слова);</li>
 * </ul>
 * 
 * Порядок анализа такой:
 * <ul>
 * <li>Поиск выполняется для каждого омонима слова, для леммы и исходной формы</li>
 * <li>Для каждого нового слова дается новый шанс на начало вхождения (многословного) ключа</li>
 * <li>Для каждого омонима, леммы и исходной формы слова копируется статус текущего поиска в свой
 * отдельный поток поиска с целью проработки всех возможных вариантов</li>
 * </ul>
 * 
 * Порядок использования такой:
 * <ul>
 * <li>Добавляются необходимые ключевые слова
 * ({@link #addKeywordSet(String, List, Map, String)})</li>
 * <li>Ищются все возможные вхождения ({@link #detectKeywordEntrances(String, List)})</li>
 * <li>Вычисляется оптимальное по покрытию непересекающееся множество ключевых
 * слов({@link #calculateOptimalKeywordsCoverage(List, int)})</li>
 * </ul>
 * 
 * <br/>
 * MULTITHREAD_SAFE: TRUE (все имеющиеся члены класса меняются лишь при инициализации, а помле
 * осуществляется доступ лишь на чтение)
 * 
 * @author fedor
 *
 */
public class KeywordManagerImpl implements IKeywordManager {
	/**
	 * Вариант покрытия.
	 * 
	 * @author fedor
	 *
	 */
	private class CoverageVariant {
		/**
		 * Из каких элементов состоит вариант покрытия.
		 */
		List<? extends IKeywordEntrance> contains;
		/**
		 * Суммарная длинна (площадь) покрытия (в кол-ве токенов).
		 */
		int coverageLength;
		/**
		 * Номер первого токена, с которого начинается покрытие для данного варианта.
		 */
		int from;

		public CoverageVariant(int coverageLength, int from,
				List<? extends IKeywordEntrance> contains) {
			super();
			this.coverageLength = coverageLength;
			this.from = from;
			this.contains = contains;
		}

		/**
		 * Создать вариант на основании списка вхождений (отсортированы от конечных вхождений к
		 * основным)
		 * 
		 * @param list
		 * @return
		 */
		CoverageVariant newInstanceFromList(List<? extends IKeywordEntrance> list) {
			int newCoverageLength = list.stream().mapToInt(e -> e.getLength()).sum();
			int newFrom = list.stream().mapToInt(e -> e.getFrom()).min().orElse(0);
			return new CoverageVariant(newCoverageLength, newFrom, list);
		}

		@Override
		public String toString() {
			return "{" + from + "|" + coverageLength + "|" + contains + "}";
		}

	}

	public static class OptionAgrConverter implements IStringConverter<SymbolExtDataTypes> {

		@Override
		public SymbolExtDataTypes convert(String value) {
			switch (value) {
				case "c_agr":
					return SymbolExtDataTypes.c_agr;
				case "gnc_agr":
					return SymbolExtDataTypes.gnc_agr;
			}
			throw new IllegalStateException(value);
		}

	}


	public static class OptionGrammConverter implements IStringConverter<Set<GrammemeEnum>> {

		@Override
		public Set<GrammemeEnum> convert(String value) {
			// remove '"' at beginning
			value = StringUtils.removeStart(value, "\"");
			// remove '"' at end
			value = StringUtils.removeEnd(value, "\"");

			Grammemes result = new Grammemes();
			GrammemeUtils.fillGrammemesByCSVMyTags(value, result, true);
			return EnumSet.copyOf(result.getGrammemes());
		}

	}

	public static final String KWS_NAME_FIO = "fio";

	@Inject
	public IConfigurationManager configurationManager;

	private List<String> keywordSets = new ArrayList<String>();
	private Map<String, PlainKeywordTrie> keywordSetToTrieMap =
			new HashMap<String, PlainKeywordTrie>();

	@Inject
	public KeywordManagerImpl() {
		super();
	}

	protected void addKeywordEntry(PlainKeywordTrieBuilder trieBuilder, String kwEntry,
			LookupOptions globalOptions, int entryPos) {
		// skip comments
		if (kwEntry.startsWith("#"))
			return;

		List<String> strings = new LinkedList<>();
		LookupOptions localOptions = parseEntryString(kwEntry, strings);
		combineLookupOptions(localOptions, globalOptions);

		// проверки
		if (localOptions.mainWord < 0)
			throw new IllegalStateException(
					String.format("Неверное значение 'mainWord' для '%s'", kwEntry));
		if (localOptions.mainWord >= strings.size())
			throw new IllegalStateException(
					String.format("Завышенное значение 'mainWord' для '%s'", kwEntry));


		if (strings.size() == 1)
			trieBuilder.addWord(strings.get(0),
					trieBuilder.createSinglewordKewordInfo(entryPos, localOptions));
		else {
			int counter = 0;
			for (String string : strings) {
				trieBuilder.addWord(string, trieBuilder.createMultiwordKewordInfo(entryPos,
						counter < strings.size() - 1, counter, localOptions));
				counter++;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ru.nlp_project.story_line2.glr_parser.keywords.IKeywordManager#addKeywordSet(java.lang.
	 * String, java.util.List, java.util.Map, java.lang.String)
	 */
	@Override
	public void addKeywordSet(String keywordSetName, List<String> keywords, String optionsRaw) {
		if (keywordSets.contains(keywordSetName))
			throw new IllegalStateException(
					"Keyword set '" + keywordSetName + "' already added to keyword base.");
		PlainKeywordTrieBuilder trieBuilder = createPlainKeywordTrieBuilder(keywordSetName);
		LookupOptions globalOptions = parseLookupOptions(optionsRaw);
		int counter = 0;
		for (String kwEntry : keywords)
			addKeywordEntry(trieBuilder, kwEntry, globalOptions, counter++);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ru.nlp_project.story_line2.glr_parser.keywords.IKeywordManager#
	 * calculateOptimalKeywordsCoverage(java.util.List, int)
	 */
	@Override
	public List<? extends IKeywordEntrance> calculateOptimalKeywordsCoverage(
			List<? extends IKeywordEntrance> kwes, int tokensLength) {
		if (kwes.size() == 0)
			return Collections.emptyList();

		// DEBUG OUTPUT
		// String collect = kwes.stream().map(k -> k.getFrom() + "-" +
		// k.getLength())
		// .collect(Collectors.joining("; "));

		Map<String, IKeywordEntrance> map = new HashMap<>();
		kwes.stream().forEach(k -> map.put(k.getFrom() + "-" + k.getLength(), k));
		ArrayList<IKeywordEntrance> list = new ArrayList<>(map.values());
		sortKeywordEntranceByStartReverse(list);
		ArrayList<CoverageVariant> coverageVariants =
				new ArrayList<KeywordManagerImpl.CoverageVariant>();
		// dumb one
		coverageVariants.add(
				new CoverageVariant(0, tokensLength, Collections.<IKeywordEntrance>emptyList()));
		for (int i = 0; i < list.size(); i++) {
			IKeywordEntrance kwe = list.get(i);
			// int currFrom = kwe.getFrom();
			// do {
			// kwe = kwes.get(i);
			createCoverageVariants(coverageVariants, kwe);
			// i++;
			// }
			// выполняем действия для всех элементов с одинаковыми from
			// while (i < kwes.size() - 1 && currFrom == kwes.get(i).getFrom());
		}
		// System.out.println(coverageVariants.size());
		List<? extends IKeywordEntrance> result = null;
		if (!coverageVariants.isEmpty()) {
			// наийти максимальное покрытие
			int maxCovers =
					coverageVariants.stream().mapToInt(c -> c.coverageLength).max().orElse(0);

			Comparator<CoverageVariant> minElComp = new Comparator<CoverageVariant>() {
				@Override
				public int compare(CoverageVariant o1, CoverageVariant o2) {
					return o1.contains.size() - o2.contains.size();
				}
			};
			// потом найти покрытие с указнным значением и минимальным кол-вом
			// элементов
			// т.е. с элементами покрывающими максимальное кол-во каждый
			Optional<CoverageVariant> optional = null;
			if (coverageVariants.size() > 1_000) {
				optional = coverageVariants.parallelStream()
						.filter(c -> c.coverageLength == maxCovers).sorted(minElComp).findFirst();
			} else
				optional = coverageVariants.stream().filter(c -> c.coverageLength == maxCovers)
						.sorted(minElComp).findFirst();

			// select variant with maximal coverage
			/*
			 * Optional<CoverageVariant> max = coverageVariants.stream().max(new
			 * Comparator<CoverageVariant>() {
			 * 
			 * @Override public int compare(CoverageVariant o1, CoverageVariant o2) { return
			 * o1.coverageLength - o2.coverageLength; } });
			 */
			if (optional.isPresent()) {
				result = optional.get().contains;
				sortKeywordEntranceByStart(result);
			}
		}
		return result;
	}

	private void combineExlamationSymbol(List<String> strs) {
		for (int i = 1; i < strs.size(); i++) {
			if (strs.get(i - 1).equals("!")) {
				strs.set(i, "!" + strs.get(i));
				strs.remove(i - 1);
				i--;
			}
		}
	}

	protected void combineLookupOptions(LookupOptions localOptions, LookupOptions globalOptions) {
		if (globalOptions.exactForm)
			localOptions.exactForm = globalOptions.exactForm;
		if (globalOptions.upperCase)
			localOptions.upperCase = globalOptions.upperCase;
		if (globalOptions.agr != null)
			localOptions.agr = globalOptions.agr;
		if (globalOptions.gramm != null)
			localOptions.gramm = globalOptions.gramm;
		if (globalOptions.grammTree != null)
			localOptions.grammTree = globalOptions.grammTree;
		if (globalOptions.mainWord != 0)
			localOptions.mainWord = globalOptions.mainWord;
	}

	/**
	 * Создать варианты покрытия.
	 * 
	 * Алгоритм примерно такой: Проходим по всем уже имеющимся и:
	 * <ul>
	 * <li>Создаем новый (на базе данных имеющегося), если окончание текущего вхождения меньше/равно
	 * начала самого раннего токена в покрытии.</li>
	 * <li>Создаем новый (на базе данных имеющегося), если окончание текущего вхождения больше
	 * начала самого раннего токена в покрытии (при этом из нового варианта удаляются вхождения,
	 * создающие подобное условие).</li>
	 * </ul>
	 * 
	 * @param coverageVariants
	 * 
	 * @param kwe
	 */
	private void createCoverageVariants(List<CoverageVariant> coverageVariants,
			IKeywordEntrance kwe) {
		int origSize = coverageVariants.size();
		for (int i = 0; i < origSize; i++) {
			CoverageVariant dOpt = coverageVariants.get(i);
			int kweEnd = kwe.getFrom() + kwe.getLength();
			if (kweEnd <= dOpt.from) {
				/*
				 * Создаем новый (на базе имеющегося), если окончание текущего вхождения
				 * меньше/равно начала самого раннего токена в покрытии.
				 */
				ArrayList<IKeywordEntrance> list = new ArrayList<IKeywordEntrance>(dOpt.contains);
				// добавляем наше вхождение
				list.add(kwe);
				coverageVariants.add(new CoverageVariant(dOpt.coverageLength + kwe.getLength(),
						kwe.getFrom(), list));
			} else {
				/*
				 * Создаем новый (на базе имеющегося), если окончание текущего вхождения больше
				 * начала самого раннего токена в покрытии (при этом из нового варианта удаляются
				 * вхождения, создающие подобное условие).
				 */
				LinkedList<IKeywordEntrance> list = new LinkedList<IKeywordEntrance>(dOpt.contains);
				while (list.size() > 0 && (kweEnd > list.getLast().getFrom()))
					list.removeLast();
				// добавляем наше вхождение
				list.add(kwe);
				coverageVariants.add(dOpt.newInstanceFromList(list));
			}
		}
	}

	private PlainKeywordTrieBuilder createPlainKeywordTrieBuilder(String keywordSetName) {
		PlainKeywordTrieBuilder trieBuilder =
				new PlainKeywordTrieBuilder(keywordSets.size(), keywordSetName);
		keywordSets.add(keywordSetName);
		keywordSetToTrieMap.put(keywordSetName, trieBuilder.getTrie());
		return trieBuilder;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ru.nlp_project.story_line2.glr_parser.keywords.IKeywordManager#detectPlainKeywordEntrances(
	 * java.util.Collection, java.util.List)
	 */
	@Override
	public List<PlainKeywordEntrance> detectPlainKeywordEntrances(
			Collection<String> usedKeywordSets, List<Token> tokens) {
		List<PlainKeywordEntrance> result = new ArrayList<PlainKeywordEntrance>();
		for (String kws : usedKeywordSets)
			result.addAll(detectPlainKeywordEntrances(kws, tokens));

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ru.nlp_project.story_line2.glr_parser.keywords.IKeywordManager#detectPlainKeywordEntrances(
	 * java.lang.String, java.util.List)
	 */
	@Override
	public List<PlainKeywordEntrance> detectPlainKeywordEntrances(String keywordSetName,
			List<Token> tokens) {
		if (keywordSetName.equalsIgnoreCase(KWS_NAME_FIO))
			return Collections.emptyList();
		PlainKeywordTrie keywordTrie = keywordSetToTrieMap.get(keywordSetName);
		if (keywordTrie == null)
			throw new IllegalStateException("Unknown keywordSet: " + keywordSetName + ". Have: "
					+ keywordSetToTrieMap.keySet());
		// main word
		PlainKeywordEntranceDetector detector =
				new PlainKeywordEntranceDetector(keywordTrie, tokens);
		return detector.detectPlainKeywordEntrances();
	}



	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ru.nlp_project.story_line2.glr_parser.keywords.IKeywordManager#getKeywordSetsNameByIndex(int)
	 */
	@Override
	public String getKeywordSetsNameByIndex(int index) {
		return keywordSets.get(index);
	}

	/**
	 * 
	 * Выполнить парсин строки ключевых словю
	 * 
	 * При анализе в первую очередь ищется отметка '|' отделяющая локальные опции от самого текста.
	 * Выделенные текст trim'ся, а опции (при наличии) - анлизируются штатным образом.
	 * 
	 * 
	 * @param kwEntry оригинальная строка
	 * @param strings пустой список для слов, который в дальнейшем заполняется словами
	 * @return опции в строке (при наличии), в обратном случае - null
	 */
	protected LookupOptions parseEntryString(String kwEntry, List<String> strings) {
		String toSpit = kwEntry;
		int sepPos = StringUtils.indexOf(kwEntry, "|");
		if (sepPos != -1)
			toSpit = kwEntry.substring(0, sepPos);
		List<String> strs = TokenManagerImpl.splitIntoStrings(toSpit.toLowerCase());
		combineExlamationSymbol(strs);
		for (int i = 0; i < strs.size(); i++)
			strings.add(strs.get(i));
		if (sepPos != -1)
			return parseLookupOptions(kwEntry.substring(sepPos + 1, kwEntry.length()));
		return new LookupOptions();
	}

	/**
	 * Выполнить парсинг опций.
	 * 
	 * Опции это значения в формате "-key=value или -key value". При наличии пробелов в значении -
	 * должно включаться в <b>двойные кавычки</b>.
	 * 
	 * 
	 * @param optionsRaw
	 * @return
	 */
	LookupOptions parseLookupOptions(String optionsRaw) {
		LookupOptions result = new LookupOptions();
		if (optionsRaw == null)
			return result;
		// replace '=' to ' '
		optionsRaw = StringUtils.replaceChars(optionsRaw, '=', ' ');

		optionsRaw = StringUtils.replacePattern(optionsRaw, ", *", ",");
		// return back '=' symbol for parameters of form '-gramm-3=value'
		optionsRaw = StringUtils.replacePattern(optionsRaw, "-gramm-([0-9]) *", "-gramm-$1=");

		// tokenize (delimer ' ', quote symbol '"')
		StrTokenizer tokenizer = new StrTokenizer(optionsRaw, ' ', '"');
		String[] args = tokenizer.getTokenArray();
		if (args.length == 0)
			return result;
		// parse
		new JCommander(result, args);
		if (!result.grammTreeRaw.isEmpty())
			result.convertGrammTree();
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ru.nlp_project.story_line2.glr_parser.keywords.IKeywordManager#simpleCoverageFiltering(java.
	 * util.List)
	 */
	@Override
	public void simpleCoverageFiltering(List<? extends IKeywordEntrance> entrances) {
		Collections.sort(entrances, new Comparator<IKeywordEntrance>() {
			@Override
			public int compare(IKeywordEntrance o1, IKeywordEntrance o2) {
				return -1 * (o1.getLength() - o2.getLength());
			}
		});
		for (int i = 0; i < entrances.size() - 1; i++) {
			IKeywordEntrance entrance = entrances.get(i);
			List<? extends IKeywordEntrance> subList = entrances.subList(i + 1, entrances.size());
			Iterator<? extends IKeywordEntrance> listIter = subList.iterator();
			while (listIter.hasNext()) {
				IKeywordEntrance kw = listIter.next();
				if (kw.getFrom() >= entrance.getFrom() && (kw.getLength()
						+ kw.getFrom()) <= (entrance.getFrom() + entrance.getLength()))
					listIter.remove();
			}
		}
	}

	private void sortKeywordEntranceByStart(List<? extends IKeywordEntrance> kwes) {
		Collections.sort(kwes, new Comparator<IKeywordEntrance>() {
			@Override
			public int compare(IKeywordEntrance o1, IKeywordEntrance o2) {
				return Integer.compare(o1.getFrom(), o2.getFrom());
			}
		});
	}


	private void sortKeywordEntranceByStartReverse(List<? extends IKeywordEntrance> kwes) {
		Collections.sort(kwes, new Comparator<IKeywordEntrance>() {
			@Override
			public int compare(IKeywordEntrance o1, IKeywordEntrance o2) {
				return -1 * Integer.compare(o1.getFrom(), o2.getFrom());
			}
		});
	}
}
