package de.axxepta.oxygen.configuration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import de.axxepta.configuration.ArgonSwaggerBootstrap;

public class OxygenWebSwaggerConfigurationBoostrap extends ArgonSwaggerBootstrap {

	private static final long serialVersionUID = -614665388830100752L;

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		super.setPackageName("de.axxepta.oxygen.resources");
		super.setTitle("Oxygen Web Author REST application");
		super.init();
	}
}
