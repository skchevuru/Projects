package com.yaml.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@PropertySource(ignoreResourceNotFound=true, value="classpath:mapping.properties")
@Component
public class ResourceProperties {
	@Autowired Environment environment;
	Logger logger = LoggerFactory.getLogger(ResourceProperties.class);
	public String getPropertyValue(String key) {
		String value = environment.getProperty(key);
		logger.info("Propert Key = "+key+" Value = "+value);
		return value;
	}
}
