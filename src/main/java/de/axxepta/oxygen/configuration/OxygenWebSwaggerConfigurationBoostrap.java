package de.axxepta.oxygen.configuration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import de.axxepta.configuration.ArgonSwaggerBootstrap;

public class OxygenWebSwaggerConfigurationBoostrap extends HttpServlet{

	private static final long serialVersionUID = -614665388830100752L;

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		ArgonSwaggerBootstrap argonSwaggerBooststrap = new ArgonSwaggerBootstrap();
		argonSwaggerBooststrap.setPackageName("de.axxepta.oxygen.providers");
		argonSwaggerBooststrap.init();
	}
}
