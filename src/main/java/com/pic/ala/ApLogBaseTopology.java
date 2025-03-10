package com.pic.ala;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;

public abstract class ApLogBaseTopology {

	private static final Logger LOG = Logger.getLogger(ApLogBaseTopology.class);
	protected static Properties topologyConfig;

	public ApLogBaseTopology(String configFileLocation) throws Exception {

		topologyConfig = new Properties();

		try {
			topologyConfig.load(ClassLoader.getSystemResourceAsStream(configFileLocation));
		} catch (FileNotFoundException e) {
			LOG.error("Encountered error while reading configuration properties: "
					+ e.getMessage());
			throw e;
		} catch (IOException e) {
			LOG.error("Encountered error while reading configuration properties: "
					+ e.getMessage());
			throw e;
		}
	}
}
