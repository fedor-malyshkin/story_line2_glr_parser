package ru.nlp_project.story_line2.glr_parser;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.inject.Inject;

import ru.nlp_project.story_line2.config.ConfigurationException;
import ru.nlp_project.story_line2.config.YAMLConfigurationReader;

public class ConfigurationManagerImpl implements IConfigurationManager {

	private MasterConfiguration masterConfiguration;
	protected File parentFile;

	@Inject
	public ConfigurationManagerImpl() {

	}

	@Override
	public void initialize() {
		try {
			readConfiguration();
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}

	}

	private void readConfiguration()
			throws ConfigurationException, MalformedURLException, URISyntaxException {
		masterConfiguration = YAMLConfigurationReader.readConfigurationFromEnvironment(
				CONFIGURATION_SYSTEM_KEY, MasterConfiguration.class);
		String path = YAMLConfigurationReader
				.getConfigurationPathFromEnvironment(CONFIGURATION_SYSTEM_KEY);
		parentFile = new File(new URI(path).getPath()).getParentFile();
	}

	public MasterConfiguration getMasterConfiguration() {
		return masterConfiguration;
	}

	public DictionaryConfiguration getDictionaryConfiguration() {
		String dictionaryFile = masterConfiguration.dictionaryFile;
		File file = new File(dictionaryFile);
		if (parentFile != null)
			file = new File(parentFile, dictionaryFile);
		try {
			return YAMLConfigurationReader.readConfigurationFromPath(file.toURI().toString(),
					DictionaryConfiguration.class);
		} catch (MalformedURLException | ConfigurationException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public String getAbsolutePath(String file) {
		File result = new File(file);
		if (parentFile != null)
			result = new File(parentFile, file);
		return result.getAbsolutePath();
	}

	@Override
	public HierarchyConfiguration getHierarchyConfiguration() {
		String hierarchyFile = masterConfiguration.hierarchyFile;
		File file = new File(hierarchyFile);
		if (parentFile != null)
			file = new File(parentFile, hierarchyFile);
		try {
			return YAMLConfigurationReader.readConfigurationFromPath(file.toURI().toString(),
					HierarchyConfiguration.class);
		} catch (MalformedURLException | ConfigurationException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public FactConfiguration getFactConfiguration() {
		String factFile = masterConfiguration.factFile;
		File file = new File(factFile);
		if (parentFile != null)
			file = new File(parentFile, factFile);
		try {
			return YAMLConfigurationReader.readConfigurationFromPath(file.toURI().toString(),
					FactConfiguration.class);
		} catch (MalformedURLException | ConfigurationException e) {
			throw new IllegalStateException(e);
		}
	}



}
