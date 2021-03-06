package ru.nlp_project.story_line2.glr_parser;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.inject.Inject;

import ru.nlp_project.story_line2.glr_parser.IConfigurationManager.HierarchyConfiguration;

public class HierarchyManagerImpl implements IHierarchyManager {

	@Inject
	public HierarchyManagerImpl() {
		super();
	}

	@Inject
	public IConfigurationManager configurationManager;

	private Map<String, Set<String>> hierarchiesMap = new TreeMap<>();

	public Map<String, Set<String>> getHierarchiesMap() {
		return hierarchiesMap;
	}

	public void initialize() {
		if (configurationManager == null
				|| configurationManager.getMasterConfiguration().hierarchyFile == null)
			return;
		readConfiguration();
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

	protected void readConfiguration() {
		HierarchyConfiguration hierarchyConfiguration =
				configurationManager.getHierarchyConfiguration();
		for (Entry<String, List<String>> entry : hierarchyConfiguration.hierarchies.entrySet()) {
			String name = entry.getKey();
			List<String> values = entry.getValue();
			addConfigurationEntry(name, values);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ru.nlp_project.story_line2.glr_parser.IHierarchyManager#isParent(java.lang.String,
	 * java.lang.String)
	 */
	@Override
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ru.nlp_project.story_line2.glr_parser.IHierarchyManager#isAnyParent(java.util.Collection,
	 * java.lang.String)
	 */
	@Override
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
