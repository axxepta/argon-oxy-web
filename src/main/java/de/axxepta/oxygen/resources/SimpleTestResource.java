package de.axxepta.oxygen.resources;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.codahale.metrics.Meter;

@Path("testing")
public class SimpleTestResource {

	private static final Logger LOG = Logger.getLogger(SimpleTestResource.class);
	
	private Meter metricRegistry;

	@Context
	private HttpServletRequest request;
	
	@Inject
	public void setMetricRegistry(Meter metricRegistry) {
		this.metricRegistry = metricRegistry;
	}
	
	@GET
	@Path("rest-test-services")
	@Produces(MediaType.TEXT_PLAIN)
	public Response test() {
		
		metricRegistry.mark();
		
		LOG.info("Simple test in oxygen web author module from " + request.getPathInfo());
		return Response.ok("Do a simple test from oxygen web module").build();
	}
}
