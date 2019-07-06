package de.axxepta.filters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import javax.servlet.FilterChain;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.axxepta.properties.BuildResourceBinderReader;
import de.axxepta.properties.ResourceBundleReader;
import ro.sync.auth.CsrfFilter;

@Singleton
@WebFilter(filterName = "FilterJersey", urlPatterns = { "/*" })
public class SecurityFilter extends CsrfFilter {

	private static final Logger LOG = LoggerFactory.getLogger(SecurityFilter.class);

	private Map<String, List<String>> mapServicesAcceptedIP;
	
	@PostConstruct
	public void loadRules() {

		LOG.info("load rules for security filter");
		
		mapServicesAcceptedIP = new HashMap<>();

		BuildResourceBinderReader resourceBundleReader = new BuildResourceBinderReader("ArgonServerConfig-OxygenWebAuthor");

		ResourceBundleReader bundleReader = resourceBundleReader.getBundlerReader();

		for (String key : bundleReader.getKeys()) {
			if (key.contains("service")) {
				String propertyValue = (String) bundleReader.getValueAsString(key);
				if (!propertyValue.isEmpty()) {
					String[] values = ((String) propertyValue).split(",");
					List<String> valuesList = Arrays.stream(values).collect(Collectors.toList());
					mapServicesAcceptedIP.put(key, valuesList);
				} else {
					mapServicesAcceptedIP.put(key, new ArrayList<>());
				}
			}
		}

		LOG.info("Rules loaded " + mapServicesAcceptedIP.toString());
		LOG.info("Number of rules loaded " + mapServicesAcceptedIP.size());

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		String pathInfo = ((HttpServletRequest) request).getPathInfo();
		String ip = request.getRemoteAddr();
		LOG.info("Path request " + pathInfo + " from IP " + ip);

		if (pathInfo != null) {
			for (Map.Entry<String, List<String>> entry : mapServicesAcceptedIP.entrySet()) {
				String pathNameService = entry.getKey().trim();
				if (pathInfo.contains(pathNameService)) {
					List<String> restrictedIPList = entry.getValue();
					if (restrictedIPList.isEmpty()) {
						RequestDispatcher dispatcher = request.getRequestDispatcher("/services" + pathInfo);
						dispatcher.forward(request, response);
						return;
					} else {
						for (String ipElem : restrictedIPList) {
							if (ip.equals(ipElem)) {
								RequestDispatcher dispatcher = request.getRequestDispatcher("/services" + pathInfo);
								dispatcher.forward(request, response);
								return;
							}
						}
					}
				}
			}
		}

		super.doFilter(request, response, chain);

	}
	
}
