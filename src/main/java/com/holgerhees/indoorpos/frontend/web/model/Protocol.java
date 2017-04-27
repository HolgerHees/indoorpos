package com.holgerhees.indoorpos.frontend.web.model;

public enum Protocol
{
	HTTP("http"),
	HTTPS("https"),
	NONE ("");
	
	private String urlName;

	private Protocol(String urlName)
	{
		this.urlName = urlName;
	}

	public String getUrlName()
	{
		return urlName;
	}
	
	public static Protocol fromUrlName(String urlName)
	{
		if (urlName != null && urlName.trim().isEmpty() == false)
		{
			for (Protocol protocol : values())
			{
				if (protocol.getUrlName().equals(urlName))
				{
					return protocol;
				}
			}
		}

		return NONE;
	}
}
