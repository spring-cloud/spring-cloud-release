package org.springframework.cloud.starter.parent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.boot.autoconfigure.AutoConfiguration;

@AutoConfiguration
public class DeprecatedModuleAutoConfiguration {

	private static final Log log = LogFactory.getLog(DeprecatedModuleAutoConfiguration.class);

	private static final String BORDER = "\n\n**********************************************************\n\n";
	/* for testing */ static final String MESSAGE = BORDER + "The artifact spring-cloud-starter-parent is deprecated. "
			+ "It will be removed in the next major release.\n"
			+ "Please use the org.springframework.cloud:spring-cloud-dependencies BOM instead." + BORDER;

	public DeprecatedModuleAutoConfiguration() {
		log.warn(MESSAGE);
	}
}
