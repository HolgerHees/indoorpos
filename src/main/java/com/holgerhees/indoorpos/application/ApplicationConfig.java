package com.holgerhees.indoorpos.application;

import org.springframework.stereotype.Component;

@Component("applicationConfig")
public class ApplicationConfig
{
	private Boolean production;

	/**
	 * Called from DefaultConfigContext.xml
	 */
	public void init()
	{
	}
	
	public Boolean isProduction() {
		return production;
	}

	public void setProduction(Boolean production) {
		this.production = production;
	}
}
