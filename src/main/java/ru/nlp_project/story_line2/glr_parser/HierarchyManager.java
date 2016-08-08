package ru.nlp_project.story_line2.glr_parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class HierarchyManager {

  public static HierarchyManager
      newInstance(ConfigurationReader configurationReader) throws IOException {
    HierarchyManager result = new HierarchyManager();
    result.configurationReader = configurationReader;
    result.initialize();
    return result;
  }

  private ConfigurationReader configurationReader;

  private Map<String, Set<String>> hierarchiesMap = new TreeMap<>();

  protected HierarchyManager() {
  }

  public Map<String, Set<String>> getHierarchiesMap() {
    return hierarchiesMap;
  }

  protected void initialize() throws IOException {
    if (configurationReader == null
        || configurationReader.getConfigurationMain().hierarchyFile == null)
      return;
    InputStream inputStream = configurationReader.getInputStream(
        configurationReader.getConfigurationMain().hierarchyFile);
    readConfigurationFile(inputStream);
    IOUtils.closeQuietly(inputStream);
  }

  protected void addConfigurationEntry(String key, List<String> values) {
    hierarchiesMap.put(key.toLowerCase(), new HashSet<String>(
        values.stream().map(String::toLowerCase).collect(Collectors.toList())));
    // rebuil pair key-values
    boolean wereChanges = true;
    do {
      wereChanges = false;
      Set<String> keys = hierarchiesMap.keySet();
      Iterator<String> keyIter = keys.iterator();
      // проходим по ключам и при нахождении его в другой паре
      // в виде значения - расширяем значения значениями ключа (удаляя его
      // самого)
      // и так до тех пор пока не будут находиться вхождения
      // Рекурсия проверяется так:
      while (keyIter.hasNext()) {
        String k = keyIter.next();
        Set<String> v = hierarchiesMap.get(k);
        Iterator<String> keyIterInn = keys.iterator();
        while (keyIterInn.hasNext()) {
          String kInn = keyIterInn.next();
          Set<String> vInn = hierarchiesMap.get(kInn);
          // check recursion
          if (vInn.contains(k) && v.contains(kInn))
            throw new IllegalStateException(
                "Есть рекурсия между ключами '" + kInn + "' и '" + k + "'.");

          // expand
          if (vInn.contains(k)) {
            wereChanges = true;
            vInn.remove(k);
            vInn.addAll(v);
            hierarchiesMap.put(kInn, vInn);
          }
        }
      }
    } while (wereChanges);
  }

  @SuppressWarnings("unchecked")
  protected void readConfigurationFile(InputStream inputStream)
      throws IOException {
    JsonFactory jsonFactory = new JsonFactory();
    jsonFactory.configure(Feature.ALLOW_COMMENTS, true);
    ObjectMapper objectMapper = new ObjectMapper(jsonFactory);
    Map<String, Object> entries =
        objectMapper.readValue(inputStream, HashMap.class);
    for (Map.Entry<String, Object> entry : entries.entrySet()) {
      String name = entry.getKey();
      List<String> values = (List<String>) entry.getValue();
      addConfigurationEntry(name, values);
    }
  }

  public boolean isParent(String parent, String child) {
    // проверка на нижний регистр не осуществляется - т.к. всё
    // приводилось ранее к нижнему регистру
    // проверка на null не выполняется, т.к. код предусматривает подобное
    if (parent.equals(child))
      return true;
    Set<String> set = hierarchiesMap.get(parent);
    if (set == null)
      return false;
    return set.contains(child);
  }

  public boolean isAnyParent(Collection<String> parentSet, String child) {
    if (parentSet.contains(child))
      return true;
    Iterator<String> iterator = parentSet.iterator();
    while (iterator.hasNext()) {
      Set<String> set = hierarchiesMap.get(iterator.next());
      if (set == null)
        continue;
      if (set.contains(child))
        return true;
    }
    return false;
  }
}
