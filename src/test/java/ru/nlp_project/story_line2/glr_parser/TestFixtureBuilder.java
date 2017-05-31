package ru.nlp_project.story_line2.glr_parser;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Ignore;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import ru.nlp_project.story_line2.glr_parser.GrammarManagerImpl.GrammarDirectiveTypes;
import ru.nlp_project.story_line2.glr_parser.InterpreterImpl.Fact;
import ru.nlp_project.story_line2.glr_parser.Token.TokenTypes;
import ru.nlp_project.story_line2.glr_parser.eval.Grammar;
import ru.nlp_project.story_line2.glr_parser.eval.Projection;
import ru.nlp_project.story_line2.glr_parser.eval.RNGLRAnalyser;
import ru.nlp_project.story_line2.glr_parser.eval.RNGLRAnalyser.SPPFNode;
import ru.nlp_project.story_line2.glr_parser.eval.Symbol;
import ru.nlp_project.story_line2.glr_parser.eval.Symbol.SymbolTypes;

@Ignore
public class TestFixtureBuilder {
  private static FileOutputStream fos;

  /**
   * Создать проекцию. 
   * 
   * Просматривается строка и для каждой буквы формируется терминал (для прописных) или нетерминал (для заглавных).
   * С помощью этого метода нельзя сформировать проекции с символами более одного символа.
   *  
   * 
   * @param head
   * @param body
   * @return
   */
  public static Projection createProjection(String head, String body) {
    List<Symbol> bodyList = new ArrayList<Symbol>();
    for (char c : body.toCharArray()) {
      if (Character.isWhitespace(c))
        continue;
      if (Character.toUpperCase(c) == c && Character.isLetter(c))
        bodyList.add(new Symbol("" + c, SymbolTypes.NonTerminal));
      else
        bodyList.add(new Symbol("" + c, SymbolTypes.Terminal));
    }
    return new Projection(new Symbol(head, SymbolTypes.NonTerminal), bodyList);
  }

  public static List<Symbol> createSymbols(String input) {
    List<Symbol> symbolList = new ArrayList<Symbol>();
    for (char c : input.toCharArray()) {
      if (Character.toUpperCase(c) == c && Character.isLetter(c))
        symbolList.add(new Symbol("" + c, SymbolTypes.NonTerminal));
      else
        symbolList.add(new Symbol("" + c, SymbolTypes.Terminal));
    }
    symbolList.add(Symbol.EOI);
    return symbolList;
  }

  public static List<Token> createOneLetterTokens(String text) {
    List<Token> result = new ArrayList<Token>();
    for (int i = 0; i < text.length(); i++)
      result.add(new Token(i, 1, text.substring(i, i + 1), TokenTypes.WORD));
    return result;
  }

  public static List<Token> createWhitespaceSeparatedTokens(String text) {
    String[] split = StringUtils.splitByWholeSeparator(text, " ");
    List<Token> result = new ArrayList<Token>();
    for (int i = 0; i < split.length; i++)
      result.add(new Token(i, 1, split[i], TokenTypes.WORD));
    return result;
  }

  /**
   * Распаковать zip-файл во временную директорию и вернуть путь к ней.
   * 
   * @param cpZipFile
   * @return
   * @throws IOException
   */
  @SuppressWarnings("unchecked")
  public static String unzipToTempDir(String cpZipFile) throws IOException {
    InputStream resourceAsStream = Thread.currentThread()
        .getContextClassLoader().getResourceAsStream(cpZipFile);
    File glrZipFile = File.createTempFile("glr-parser-config", ".zip");
    FileUtils.forceDeleteOnExit(glrZipFile);
    fos = new FileOutputStream(glrZipFile);
    IOUtils.copy(resourceAsStream, fos);

    int BUFFER = 2048;

    ZipFile zip = new ZipFile(glrZipFile);
    String newPath = glrZipFile.getAbsolutePath().substring(0,
        glrZipFile.getAbsolutePath().length() - 4);

    new File(newPath).mkdir();
    Enumeration<ZipEntry> zipFileEntries =
        (Enumeration<ZipEntry>) zip.entries();

    // Process each entry
    while (zipFileEntries.hasMoreElements()) {
      // grab a zip file entry
      ZipEntry entry = zipFileEntries.nextElement();
      String currentEntry = entry.getName();
      File destFile = new File(newPath, currentEntry);
      // destFile = new File(newPath, destFile.getName());
      File destinationParent = destFile.getParentFile();

      // create the parent directory structure if needed
      destinationParent.mkdirs();

      if (!entry.isDirectory()) {
        BufferedInputStream is =
            new BufferedInputStream(zip.getInputStream(entry));
        int currentByte;
        // establish buffer for writing file
        byte data[] = new byte[BUFFER];

        // write the current file to disk
        FileOutputStream fos = new FileOutputStream(destFile);
        BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER);

        // read and write until last byte is encountered
        while ((currentByte = is.read(data, 0, BUFFER)) != -1) {
          dest.write(data, 0, currentByte);
        }
        dest.flush();
        dest.close();
        is.close();
      }
    }
    IOUtils.closeQuietly(zip);
    FileUtils.forceDeleteOnExit(new File(newPath));
    return newPath;
  }

  public static Grammar parseGrammar(String grammarText, String rootSymbol) {
    Map<GrammarDirectiveTypes, Object> grmDrct =
        new HashMap<GrammarDirectiveTypes, Object>();
    grmDrct.put(GrammarDirectiveTypes.ROOT, rootSymbol);

    NGLRGrammarProcessor grammarProcessor = new NGLRGrammarProcessor();
    // shallow processing
    grammarProcessor.parseGrammar(grammarText.toString());
    Grammar grammar = grammarProcessor.getGrammar();

    // expand grammar
    grammarProcessor.expandGrammar(grammar, grmDrct);
    return grammarProcessor.getGrammar();
  }

  public static SentenceProcessingContext
      createDummySentenceProcessingContext() {
    SentenceProcessingContext result = SentenceProcessingContext
        .create("processingArticle", null, new IFactListener() {
          @Override
          public void factExtracted(SentenceProcessingContext context,
              Fact fact) {
          }
        }, new IGLRLogger() {
        });
    return result;
  }

  public static ParseTreeNode createParseTree(String grammarText,
      List<Token> tokens, GrammarManagerImpl grammarManager) {
    Grammar grammar = TestFixtureBuilder.parseGrammar(grammarText, "Root");
    RNGLRAnalyser analyser = grammarManager.createAnalyser(grammar);
    assertTrue(analyser.processTokens(tokens));
    SPPFNode sppfNode = analyser.getRootNode();

    List<ParseTreeNode> trees =
        grammarManager.createParseTrees(sppfNode, grammar);
    assertTrue(trees.size() > 0);
    ParseTreeNode parseTreeNode = trees.get(0);

    grammarManager.calculateParseTreeNodeCoverages(tokens, parseTreeNode);

    ParseTreeNode userRootSymbolNode =
        grammarManager.extractGrammarUserRootSymbolNode(parseTreeNode,
            grammar.getUserRootSymbol());

    assertNotNull(userRootSymbolNode);
    return userRootSymbolNode;
  }

  public static ParseTreeNode createAndValidateParseTree(String grammarText,
      List<Token> tokens, GrammarManagerImpl grammarManager) throws Exception {
    ParseTreeNode root = createParseTree(grammarText, tokens, grammarManager);
    ParseTreeValidator validator =
        new ParseTreeValidator(new HierarchyManagerImpl());
    validator.validateTree(createDummySentenceProcessingContext(), root);
    return root;
  }
}
