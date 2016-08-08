package ru.nlp_project.story_line2.glr_parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Класс чтения конфигурации. 
 * 
 * Предназначен для разрешения имен и выдачи InputStream'ов для файлов на которые имеются ссылки.
 * Самостоятельно читает лишь основной файл конфигурации.
 *
 * MULTITHREAD_SAFE: YES
 * 
 * @author fedor
 *
 */
public class ConfigurationReader {

  public class ConfigurationMain {
    public boolean debug;
    public String dictionaryFile;
    public String sentenceData;
    public String morphZipDB;
    public List<String> articles;
    public String factFile;
    public String hierarchyFile;
  }

  private String configFile;
  private ConfigurationMain configurationMain;
  private ObjectMapper objectMapper;
  private File parentFile;

  private ConfigurationReader(String configFile) {
    this.configFile = configFile;
  }

  public static ConfigurationReader newInstance(String configFile) throws IOException {
    ConfigurationReader result = new ConfigurationReader(configFile);
    result.read();
    return result;
  }

  public InputStream getInputStream(String fileName)
      throws FileNotFoundException {
    if (null == fileName)
      throw new FileNotFoundException(
          "Incorrect configuration file: " + fileName + "");
    File file = new File(parentFile, fileName);
    if (!file.isFile() || !file.exists())
      throw new FileNotFoundException(
          "Incorrect configuration file: " + fileName + "");
    return new FileInputStream(file);
  }

  private void read() throws IOException {
    File file = new File(this.configFile);

    if (!file.isFile() || !file.exists())
      throw new FileNotFoundException(
          "Incorrect configuration file: " + configFile + "");
    parentFile = file.getParentFile();

    JsonFactory jsonFactory = new JsonFactory();
    jsonFactory.configure(Feature.ALLOW_COMMENTS, true);
    objectMapper = new ObjectMapper(jsonFactory);

    readMainConfig(file);
  }

  public ConfigurationMain getConfigurationMain() {
    return configurationMain;
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  private void readMainConfig(File file) throws IOException {
    HashMap map = objectMapper.readValue(file, HashMap.class);
    configurationMain = new ConfigurationMain();
    configurationMain.debug = map.get("debug") == Boolean.TRUE ? true : false;
    configurationMain.dictionaryFile = (String) map.get("dictionary_file");
    configurationMain.factFile = (String) map.get("fact_file");
    configurationMain.sentenceData = (String) map.get("sentence_data");
    configurationMain.morphZipDB = (String) map.get("morph_zip_db");
    configurationMain.articles = (List<String>) map.get("articles");
    configurationMain.hierarchyFile = ( String) map.get("hierarchy_file");
  }

  public String getAbsolutePath(String fileName) throws FileNotFoundException {
    if (null == fileName)
      throw new FileNotFoundException(
          "Incorrect configuration file: " + fileName + "");
    File file = new File(parentFile, fileName);
    if (!file.isFile() || !file.exists())
      throw new FileNotFoundException("Incorrect configuration file: " + "("
          + file.getAbsolutePath() + ") " + fileName + "");
    return file.getAbsolutePath();
  }

}
