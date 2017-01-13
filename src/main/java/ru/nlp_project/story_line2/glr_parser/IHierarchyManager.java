package ru.nlp_project.story_line2.glr_parser;

import java.util.Collection;


public interface IHierarchyManager {
	void initialize();
	boolean isParent(String parent, String child);

	boolean isAnyParent(Collection<String> parentSet, String child);

}
