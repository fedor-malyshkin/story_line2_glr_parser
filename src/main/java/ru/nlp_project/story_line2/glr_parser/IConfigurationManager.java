package ru.nlp_project.story_line2.glr_parser;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import ru.nlp_project.story_line2.config.ConfigurationException;

public interface IConfigurationManager {
	// To ignore any unknown properties in JSON input without exception:
	@JsonIgnoreProperties(ignoreUnknown = true)
	class MasterConfiguration {

		@JsonProperty("glr_parser.debug")
		public boolean debug;
		@JsonProperty("glr_parser.dictionary_file")
		public String dictionaryFile;
		@JsonProperty("glr_parser.sentence_data")
		public String sentenceData;
		@JsonProperty("glr_parser.morph_zip_db")
		public String morphZipDB;
		@JsonProperty("glr_parser.tagger_json_db")
		public String taggerJsonDB;
		@JsonProperty("glr_parser.articles")
		public List<String> articles;
		@JsonProperty("glr_parser.fact_file")
		public String factFile;
		@JsonProperty("glr_parser.hierarchy_file")
		public String hierarchyFile;

		@Override
		public String toString() {
			return "MasterConfiguration [debug=" + debug + ", dictionaryFile=" + dictionaryFile
					+ ", sentenceData=" + sentenceData + ", morphZipDB=" + morphZipDB
					+ ", taggerZipDB=" + taggerJsonDB + ", articles=" + articles + ", factFile="
					+ factFile + ", hierarchyFile=" + hierarchyFile + "]";
		}
	}

	// To ignore any unknown properties in JSON input without exception:
	@JsonIgnoreProperties(ignoreUnknown = true)
	class DictionaryConfiguration {
		@JsonProperty("dictionaries")
		public List<DictionaryConfigurationEntry> dictionaryEntries = new ArrayList<>();
	}

	class DictionaryConfigurationEntry {
		@JsonProperty("name")
		String name;
		@JsonProperty("type")
		String type;
		@JsonProperty("options")
		String options;
		@JsonProperty("grammar_file")
		String grammarFile;
		@JsonProperty("keywords_file")
		String keywordsFile;
		@JsonProperty("keywords")
		List<String> keywords;

		@Override
		public String toString() {
			return "DictionaryConfigurationEntry [name=" + name + ", type=" + type
					+ ", additionalParameter=" + options + ", grammarFile=" + grammarFile
					+ ", keywordsFile=" + keywordsFile + ", keywords=" + keywords + "]";
		}

	}
	class HierarchyConfiguration {
		@JsonProperty("hierarchies")
		Map<String, List<String>> hierarchies = new HashMap<>();
	}

	class FactConfiguration {
		@JsonProperty("facts")
		public List<FactConfigurationEntry> factEntries = new ArrayList<>();
	}

	class FactConfigurationEntry {
		@JsonProperty("name")
		String name;
		@JsonProperty("fields")
		List<String> fields;

		@Override
		public String toString() {
			return "FactConfigurationEntry [name=" + name + ", fields=" + fields + "]";
		}


	}

	MasterConfiguration getMasterConfiguration();

	DictionaryConfiguration getDictionaryConfiguration();

	HierarchyConfiguration getHierarchyConfiguration();

	FactConfiguration getFactConfiguration();

	void initialize();

	InputStream getSiblingInputStream(String relativePath) throws ConfigurationException;

}
