package de.axxepta.oxygen.configuration;

import java.io.IOException;

import javax.ws.rs.core.Context;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.jaxrs.xml.JacksonJaxbXMLProvider;

import de.axxepta.configuration.InitDiscoveryService;
import de.axxepta.configuration.InitResourceConfig;
import eu.infomas.annotation.AnnotationDetector;
import net.sagebits.HK2Utilities.AnnotatedClasses;
import net.sagebits.HK2Utilities.AnnotationReporter;

public class OxygenRestResourceConfiguration extends ResourceConfig {

	private static final Logger LOG = LoggerFactory.getLogger(OxygenRestResourceConfiguration.class);

	public OxygenRestResourceConfiguration(@Context ServiceLocator serviceLocator) {

		setApplicationName("Oxygen Web Author REST services application");

		packages(true, "de.axxepta.oxygen");

		String[] packages = new String[] { "de.axxepta.oxygen.services.interfaces",
				"de.axxepta.oxygen.services.implementations" };
		AnnotatedClasses ac = new AnnotatedClasses();
		@SuppressWarnings("unchecked")
		AnnotationDetector cf = new AnnotationDetector(new AnnotationReporter(ac, new Class[] { Service.class }));
		try {
			cf.detect(packages);
		} catch (IOException e) {
			LOG.error("IO EXception " + e.getMessage());
		}

		/*try {
			Class<?>[] classes = ac.getAnnotatedClasses();
			for (Class<?> c : classes) {
				LOG.info("Annotated class " + c.getName());
			}
		} catch (ClassNotFoundException e) {

		}*/

		try {
			for (ActiveDescriptor<?> ad : ServiceLocatorUtilities.addClasses(serviceLocator, ac.getAnnotatedClasses())) {
				LOG.info("Added " + ad.toString());
			}
		} catch (ClassNotFoundException e) {
			
		}
		
		InitDiscoveryService.initDiscoveryService(serviceLocator, Thread.currentThread().getContextClassLoader());

		LOG.info(this.getApplicationName());

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
