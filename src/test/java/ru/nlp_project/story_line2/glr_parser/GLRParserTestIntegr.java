package ru.nlp_project.story_line2.glr_parser;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

import ru.nlp_project.story_line2.glr_parser.InterpreterImpl.Fact;
import ru.nlp_project.story_line2.glr_parser.InterpreterImpl.FactField;
import ru.nlp_project.story_line2.glr_parser.ParseTreeValidator.ParseTreeValidationException;

public class GLRParserTestIntegr {

	static class FactSerializer implements IFactListener {
		List<String> factsSerialized = Collections.synchronizedList(new LinkedList<String>());

		public void clear() {
			factsSerialized.clear();
		}

		@Override
		public void factExtracted(SentenceProcessingContext context, Fact fact) {
			factsSerialized.add(serializeFact(context, fact));
		}

		private String serializeFact(SentenceProcessingContext executionContext, Fact fact) {
			Comparator<Entry<String, FactField>> strartComp =
					new Comparator<Entry<String, FactField>>() {
						@Override
						public int compare(Entry<String, FactField> f1,
								Entry<String, FactField> f2) {
							FactField ff1 = f1.getValue();
							FactField ff2 = f2.getValue();
							return ff1.from - ff2.from;
						}

					};
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			pw.print("[artc: " + executionContext.getArticle() + "]");
			pw.print("[snt-st: " + executionContext.getSentence().start + "]");
			pw.print("[fact: " + fact.getName() + "<");
			pw.print(fact.getFieldsMap().entrySet().stream().sorted(strartComp)
					.map(e -> String.format("%s=\'%s\'(%d,%d)", e.getValue().name,
							e.getValue().value, e.getValue().from, e.getValue().length))
					.collect(Collectors.joining("; ")));
			pw.print(">]");
			pw.flush();
			sw.flush();
			return sw.toString();
		}
	}

	static class GrammarSerializationLogger implements IGLRLogger {

		List<String> detectedParseTreeSerialized =
				Collections.synchronizedList(new LinkedList<String>());

		public void clear() {
			detectedParseTreeSerialized.clear();
		}

		@Override
		public void detectedParseTree(SentenceProcessingContext context, ParseTreeNode tree,
				ParseTreeNode userTree, boolean validated, ParseTreeValidationException exception) {
			detectedParseTreeSerialized
					.add(serializeParseTree(context, tree, userTree, validated, exception));

		}

		private String serializeParseTree(SentenceProcessingContext context, ParseTreeNode tree,
				ParseTreeNode userTree, boolean validated, ParseTreeValidationException exception) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			if (validated)
				pw.print("<validated - " + userTree.toString() + ">");
			else
				pw.print("<invalidated - " + userTree.toString() + "(" + exception.getMessage()
						+ ")>");
			pw.flush();
			sw.flush();
			return sw.toString();
		}
	}

	private static GLRParser testable;
	private static FactSerializer factSerializer;

	private static GrammarSerializationLogger grammarSerializationLogger;

	private static String parserConfigDir;

	@BeforeClass
	public static void setUpClass() throws IOException {
		factSerializer = new FactSerializer();
		grammarSerializationLogger = new GrammarSerializationLogger();

		parserConfigDir = TestFixtureBuilder
				.unzipToTempDir("ru/nlp_project/story_line2/glr_parser/GLRParserTestIntegr.zip");
		System.setProperty(IConfigurationManager.CONFIGURATION_SYSTEM_KEY,
				new File(parserConfigDir + "/glr-config.yaml").toURI().toString());
		testable = GLRParser.newInstance(grammarSerializationLogger, factSerializer, true, false);
	}

	@Test
	public void testTest0() throws IOException {
		factSerializer.clear();
		grammarSerializationLogger.clear();
		String text = "Он одел пальто, сапоги, шапку и пошёл...";
		testable.processText(Arrays.asList("testTest02"), text);

		String result =
				factSerializer.factsSerialized.stream().sorted().collect(Collectors.joining("\n"));
		assertEquals(
				"[artc: testTest02][snt-st: 0][fact: PaintedMan<Color='null'(0,0); Who='пальто'(2,1)>]",
				result);
	}

	@Test
	public void testTest00_0() throws IOException {
		factSerializer.clear();
		grammarSerializationLogger.clear();

		String text = "Синяя Юлия синей Юлии синей Юлия.";
		testable.processText(Arrays.asList("testTest00"), text);
		assertEquals(
				"[[artc: testTest00][snt-st: 0][fact: PaintedMan<Color='синяя'(0,1); Who='синяя юлия'(0,2)>], "
						+ "[artc: testTest00][snt-st: 0][fact: PaintedMan<Color='синяя'(2,1); Who='синяя юлия'(2,2)>], "
						+ "[artc: testTest00][snt-st: 0][fact: PaintedMan<Color='синяя'(4,1); Who='синяя юлия'(4,2)>]]",
				factSerializer.factsSerialized.toString());
	}

	@Test
	public void testTest00_1() throws IOException {

		factSerializer.clear();
		grammarSerializationLogger.clear();
		String text =
				"В последние несколько месяцев Саудовская Аравия и Россия провели целую серию консультаций, "
						+ "но не достигли пока каких-то прорывных решений, о чем сообщают американские и саудовские официальные "
						+ "представители. Пока неясно, насколько тесно во время этих переговоров саудовские руководители увязывали"
						+ " цены на нефть и сирийский вопрос. Однако официальные лица Саудовской Аравии заявляют (и они проинформировали об этом США), "
						+ "что, на их взгляд, они способны оказать определенное влияние на Путина, поскольку имеют возможность сократить поставки нефти, "
						+ "что может вызвать повышение цен.";
		testable.processText(Arrays.asList("testTest00"), text);

		String result =
				factSerializer.factsSerialized.stream().sorted().collect(Collectors.joining("\n"));
		assertEquals(
				"[artc: testTest00][snt-st: 0][fact: PaintedMan<Color='саудовская'(4,1); Who='саудовская аравия'(4,2)>]\n"
						+ "[artc: testTest00][snt-st: 0][fact: PaintedMan<Color='целая'(9,1); Who='целая серия'(9,2)>]\n"
						+ "[artc: testTest00][snt-st: 330][fact: PaintedMan<Color='саудовская'(3,1); Who='саудовская аравия'(3,2)>]",
				result);

		result = grammarSerializationLogger.detectedParseTreeSerialized.stream().sorted()
				.collect(Collectors.joining("\n"));
		assertEquals(
				"<validated - <S('Аравии'), 3,2 (24,17)>->[<Painter{PaintedMan.Who}('Аравии'), 3,2 (24,17)>->[<adj{PaintedMan.Color}('Саудовской'), 3,1 (24,10)>, <noun<rt, gram=\"femn\">('Аравии'), 4,1 (35,6)>]]>\n"
						+ "<validated - <S('Аравия'), 4,2 (30,17)>->[<Painter{PaintedMan.Who}('Аравия'), 4,2 (30,17)>->[<adj{PaintedMan.Color}('Саудовская'), 4,1 (30,10)>, <noun<rt, gram=\"femn\">('Аравия'), 5,1 (41,6)>]]>\n"
						+ "<validated - <S('серию'), 9,2 (65,11)>->[<Painter{PaintedMan.Who}('серию'), 9,2 (65,11)>->[<adj{PaintedMan.Color}('целую'), 9,1 (65,5)>, <noun<rt, gram=\"femn\">('серию'), 10,1 (71,5)>]]>",
				result);
	}

	@Test
	public void testTest01() throws IOException {
		factSerializer.clear();
		grammarSerializationLogger.clear();
		String text =
				"В последние несколько месяцев Саудовская Аравия и Россия провели целую серию консультаций, "
						+ "но не достигли пока каких-то прорывных решений, о чем сообщают американские и саудовские официальные "
						+ "представители. Пока неясно, насколько тесно во время этих переговоров саудовские руководители увязывали"
						+ " цены на нефть и сирийский вопрос. Однако официальные лица Саудовской Аравии заявляют (и они проинформировали об этом США), "
						+ "что, на их взгляд, они способны оказать определенное влияние на Путина, поскольку имеют возможность сократить поставки нефти, "
						+ "что может вызвать повышение цен.";
		testable.processText(Arrays.asList("testTest01"), text);

		String result = grammarSerializationLogger.detectedParseTreeSerialized.stream().sorted()
				.collect(Collectors.joining("\n"));
		assertEquals(
				"<validated - <S('Саудовская_Аравия'), 4,3 (30,26)>->[<GeoUnion{Geo.Union}('Саудовская_Аравия'), 4,3 (30,26)>->[<word<kwtype=\"geo-name\">{Geo.Part}('Саудовская_Аравия'), 4,1 (30,17)>, <'и'('и'), 5,1 (48,1)>, <word<kwset=[\"geo-name\"]>{Geo.Part}('Россия'), 6,1 (50,6)>]]>",
				result);

		result = factSerializer.factsSerialized.stream().sorted().collect(Collectors.joining("\n"));
		assertEquals(
				"[artc: testTest01][snt-st: 0][fact: Geo<Part='саудовская аравия'(4,1); Union='саудовская аравия и россия'(4,3)>]\n"
						+ "[artc: testTest01][snt-st: 0][fact: Geo<Union='саудовская аравия и россия'(4,3); Part='россия'(6,1)>]",
				result);

	}

	@Test
	@Ignore
	public void testTest01_10_000Iterations() throws IOException {
		for (int i = 0; i < 10_000; i++) {
			factSerializer.clear();
			grammarSerializationLogger.clear();
			String text =
					"В последние несколько месяцев Саудовская Аравия и Россия провели целую серию консультаций, "
							+ "но не достигли пока каких-то прорывных решений, о чем сообщают американские и саудовские официальные "
							+ "представители. Пока неясно, насколько тесно во время этих переговоров саудовские руководители увязывали"
							+ " цены на нефть и сирийский вопрос. Однако официальные лица Саудовской Аравии заявляют (и они проинформировали об этом США), "
							+ "что, на их взгляд, они способны оказать определенное влияние на Путина, поскольку имеют возможность сократить поставки нефти, "
							+ "что может вызвать повышение цен."
							+ "В последние несколько месяцев Саудовская Аравия и Россия провели целую серию консультаций, "
							+ "но не достигли пока каких-то прорывных решений, о чем сообщают американские и саудовские официальные "
							+ "представители. Пока неясно, насколько тесно во время этих переговоров саудовские руководители увязывали"
							+ " цены на нефть и сирийский вопрос. Однако официальные лица Саудовской Аравии заявляют (и они проинформировали об этом США), "
							+ "что, на их взгляд, они способны оказать определенное влияние на Путина, поскольку имеют возможность сократить поставки нефти, "
							+ "что может вызвать повышение цен."
							+ "В последние несколько месяцев Саудовская Аравия и Россия провели целую серию консультаций, "
							+ "но не достигли пока каких-то прорывных решений, о чем сообщают американские и саудовские официальные "
							+ "представители. Пока неясно, насколько тесно во время этих переговоров саудовские руководители увязывали"
							+ " цены на нефть и сирийский вопрос. Однако официальные лица Саудовской Аравии заявляют (и они проинформировали об этом США), "
							+ "что, на их взгляд, они способны оказать определенное влияние на Путина, поскольку имеют возможность сократить поставки нефти, "
							+ "что может вызвать повышение цен."
							+ "В последние несколько месяцев Саудовская Аравия и Россия провели целую серию консультаций, "
							+ "но не достигли пока каких-то прорывных решений, о чем сообщают американские и саудовские официальные "
							+ "представители. Пока неясно, насколько тесно во время этих переговоров саудовские руководители увязывали"
							+ " цены на нефть и сирийский вопрос. Однако официальные лица Саудовской Аравии заявляют (и они проинформировали об этом США), "
							+ "что, на их взгляд, они способны оказать определенное влияние на Путина, поскольку имеют возможность сократить поставки нефти, "
							+ "что может вызвать повышение цен.";
			testable.processText(Arrays.asList("testTest00"), text);

			String result = factSerializer.factsSerialized.stream().sorted()
					.collect(Collectors.joining("\n"));
			assertEquals(
					"[artc: testTest00][snt-st: 0][fact: PaintedMan<Color='саудовская'(4,1); Who='саудовская аравия'(4,2)>]\n"
							+ "[artc: testTest00][snt-st: 0][fact: PaintedMan<Color='целая'(9,1); Who='целая серия'(9,2)>]\n"
							+ "[artc: testTest00][snt-st: 1484][fact: PaintedMan<Color='саудовская'(3,1); Who='саудовская аравия'(3,2)>]\n"
							+ "[artc: testTest00][snt-st: 1484][fact: PaintedMan<Color='саудовская'(46,1); Who='саудовская аравия'(46,2)>]\n"
							+ "[artc: testTest00][snt-st: 1484][fact: PaintedMan<Color='целая'(51,1); Who='целая серия'(51,2)>]\n"
							+ "[artc: testTest00][snt-st: 2061][fact: PaintedMan<Color='саудовская'(3,1); Who='саудовская аравия'(3,2)>]\n"
							+ "[artc: testTest00][snt-st: 330][fact: PaintedMan<Color='саудовская'(3,1); Who='саудовская аравия'(3,2)>]\n"
							+ "[artc: testTest00][snt-st: 330][fact: PaintedMan<Color='саудовская'(46,1); Who='саудовская аравия'(46,2)>]\n"
							+ "[artc: testTest00][snt-st: 330][fact: PaintedMan<Color='целая'(51,1); Who='целая серия'(51,2)>]\n"
							+ "[artc: testTest00][snt-st: 907][fact: PaintedMan<Color='саудовская'(3,1); Who='саудовская аравия'(3,2)>]\n"
							+ "[artc: testTest00][snt-st: 907][fact: PaintedMan<Color='саудовская'(46,1); Who='саудовская аравия'(46,2)>]\n"
							+ "[artc: testTest00][snt-st: 907][fact: PaintedMan<Color='целая'(51,1); Who='целая серия'(51,2)>]",
					result);
			/*
			 * result = grammarSerializationLogger.detectedParseTreeSerialized.stream()
			 * .sorted().collect(Collectors.joining("\n")); assertEquals(
			 * "<validated - <S('Аравии'), 3,2 (24,17)>->[<Painter{PaintedMan.Who}('Аравии'), 3,2 (24,17)>->[<adj{PaintedMan.Color}('Саудовской'), 3,1 (24,10)>, <noun<rt, gram=\"femn\">('Аравии'), 4,1 (35,6)>]]>\n"
			 * +
			 * "<validated - <S('Аравия'), 4,2 (30,17)>->[<Painter{PaintedMan.Who}('Аравия'), 4,2 (30,17)>->[<adj{PaintedMan.Color}('Саудовская'), 4,1 (30,10)>, <noun<rt, gram=\"femn\">('Аравия'), 5,1 (41,6)>]]>\n"
			 * +
			 * "<validated - <S('серию'), 9,2 (65,11)>->[<Painter{PaintedMan.Who}('серию'), 9,2 (65,11)>->[<adj{PaintedMan.Color}('целую'), 9,1 (65,5)>, <noun<rt, gram=\"femn\">('серию'), 10,1 (71,5)>]]>"
			 * , result);
			 */
		}
	}

	@Test
	public void testTest03() throws IOException {
		factSerializer.clear();
		grammarSerializationLogger.clear();
		String text =
				"Не помню как назвали научный центр: то ли 'имЯни Москвина', то ли 'имени Пирогова'. "
						+ "Кроме этого я посетил ещё пару научных центров и исследовательских комплексов.";
		testable.processText(Arrays.asList("testTest03"), text);

		String result =
				factSerializer.factsSerialized.stream().sorted().collect(Collectors.joining("\n"));
		assertEquals(
				"[artc: testTest03][snt-st: 0][fact: Org<Name='имя пирогова'(16,2)>]\n"
						+ "[artc: testTest03][snt-st: 84][fact: Org<Name='научные центры'(6,1)>]",
				result);

	}

	@Test
	public void testTest04() throws IOException {
		factSerializer.clear();
		grammarSerializationLogger.clear();
		String text =
				"Дом плавал. Синий слон плавал. Рыжий Тигр летал. Рыжий тигр. Сильный МосОблБанк летал.";
		testable.processText(Arrays.asList("testTest04"), text);

		String result =
				factSerializer.factsSerialized.stream().sorted().collect(Collectors.joining("\n"));
		assertEquals(
				"[artc: testTest04][snt-st: 0][fact: Org<Name='дом плавал'(0,2)>]\n"
						+ "[artc: testTest04][snt-st: 31][fact: Org<Name='тигр летал'(1,2)>]\n"
						+ "[artc: testTest04][snt-st: 61][fact: Org<Name='МосОблБанк летал'(1,2)>]",
				result);

	}

	@Test
	public void testTest05() throws IOException {
		factSerializer.clear();
		grammarSerializationLogger.clear();
		String text =
				"Лектор Иван Петрович Смольников будет нам читать рассказ \"Мой друг Иван Лапшин\".";
		testable.processText(Arrays.asList("testTest05"), text);

		String result =
				factSerializer.factsSerialized.stream().sorted().collect(Collectors.joining("\n"));
		assertEquals(
				"[artc: testTest05][snt-st: 0][fact: FIO<FIO='иван петрович'(1,1)>]\n"
						+ "[artc: testTest05][snt-st: 0][fact: FIO<FIO='лапшин иван'(10,1)>]",
				result);

	}

	@Test
	public void testExtractDates() throws IOException {
		factSerializer.clear();
		grammarSerializationLogger.clear();
		testable.processText(Arrays.asList("absolute_dates"),
				"В четверг 9 июля схожую позицию озвучил помощник Медведева Аркадий Дворкович.");

		String result =
				factSerializer.factsSerialized.stream().sorted().collect(Collectors.joining("\n"));
		assertEquals(
				"[artc: absolute_dates][snt-st: 0][fact: TEMPORAL<day_of_week='null'(0,0); absolute_date='четверг 9 июля'(1,3)>]",
				result);
	}

	/**
	 * Проверка отсуствия бага с генерацией новых не нужных токенов при извлечении GrammarKW с FIO.
	 * Вторая статья "geos" необходима лищь для выявления бага (будет NPE при работе с лексемами
	 * нового странного токена).
	 * 
	 * @throws IOException
	 */
	@Test
	public void testExtractingFios() throws IOException {
		factSerializer.clear();
		grammarSerializationLogger.clear();

		testable.processText(Arrays.asList("fios", "geos"), "Фото Дмитрия Торопова");

		String result =
				factSerializer.factsSerialized.stream().sorted().collect(Collectors.joining("\n"));
		assertEquals("[artc: fios][snt-st: 0][fact: FIO<FIO='торопова дмитрий'(1,1)>]", result);
	}

}
