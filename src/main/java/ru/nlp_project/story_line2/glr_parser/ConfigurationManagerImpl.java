package ru.nlp_project.story_line2.glr_parser;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import javax.inject.Inject;

import ru.nlp_project.story_line2.config.ConfigurationException;
import ru.nlp_project.story_line2.config.ConfigurationManager;
import ru.nlp_project.story_line2.config.IConfigurationSupplier;

public class ConfigurationManagerImpl implements IConfigurationManager {

	private MasterConfiguration masterConfiguration;
	private String parentPath;
	private String configurationPath;
	private IConfigurationSupplier configurationSupplier;

	@Inject
	public ConfigurationManagerImpl(String configurationPath) {
		this.configurationPath = configurationPath;

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
		configurationSupplier = ConfigurationManager.getConfigurationSupplier(configurationPath);
		masterConfiguration = configurationSupplier
				.getConfigurationObjectFromPath(configurationPath, MasterConfiguration.class, null);
		parentPath = configurationSupplier.getParentPath(configurationPath);
	}

	public MasterConfiguration getMasterConfiguration() {
		return masterConfiguration;
	}

	public DictionaryConfiguration getDictionaryConfiguration() {
		String dictionaryFile = masterConfiguration.dictionaryFile;
		try {
			return configurationSupplier.getConfigurationObjectFromPath(parentPath, dictionaryFile,
					DictionaryConfiguration.class, null);
		} catch (ConfigurationException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public InputStream getSiblingInputStream(String relativePath) throws ConfigurationException {
		return configurationSupplier.getInputStreamFromPath(parentPath, relativePath);
	}

	@Override
	public HierarchyConfiguration getHierarchyConfiguration() {
		String hierarchyFile = masterConfiguration.hierarchyFile;
		try {
			return configurationSupplier.getConfigurationObjectFromPath(parentPath, hierarchyFile,
					HierarchyConfiguration.class, null);
		} catch (ConfigurationException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public FactConfiguration getFactConfiguration() {
		String factFile = masterConfiguration.factFile;
		try {
			return configurationSupplier.getConfigurationObjectFromPath(parentPath, factFile,
					FactConfiguration.class, null);
		} catch (ConfigurationException e) {
			throw new IllegalStateException(e);
		}
	}


}
