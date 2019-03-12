package de.axxepta.oxygen.configuration;

import javax.ws.rs.core.Context;

import org.apache.log4j.Logger;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;

import com.fasterxml.jackson.jaxrs.xml.JacksonJaxbXMLProvider;

import de.axxepta.configuration.InitDiscoveryService;
import de.axxepta.configuration.InitResourceConfig;

public class OxygenRestResourceConfiguration extends ResourceConfig {

	private static final Logger LOG = Logger.getLogger(OxygenRestResourceConfiguration.class);

	public OxygenRestResourceConfiguration(@Context ServiceLocator locator) {

		setApplicationName("Oxygen Web Author rest services application");

		packages(true, "de.axxepta.oxygen");

		LOG.info(this.getApplicationName());
		
		InitDiscoveryService.initDiscoveryService(locator, getClass().getClassLoader());
		
		InitResourceConfig.initResourceConfig(this);

		InitResourceConfig.initRegisterMeterBinder(this);

		InitResourceConfig.initUtilitiesXML(this);

		register(MultiPartFeature.class);

		register(JacksonJaxbXMLProvider.class);

		InitResourceConfig.initSwaggerProvider(this);

		InitResourceConfig.initEncoding(this);

		InitResourceConfig.initLogger();
	}
	
}
