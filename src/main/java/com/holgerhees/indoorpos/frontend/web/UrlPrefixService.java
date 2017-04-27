package com.holgerhees.indoorpos.frontend.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.holgerhees.indoorpos.application.ApplicationConfig;
import com.holgerhees.indoorpos.frontend.FrontendConfig;
import com.holgerhees.indoorpos.frontend.web.model.Protocol;

@Service("urlPrefixService")
public class UrlPrefixService {

	protected final static String COLON = ":";
	protected final static String DOT = ".";
	protected final static String SLASH = "/";
	protected final static String SLASHSLASH = SLASH + SLASH;

	@Autowired
	private ApplicationConfig applicationConfig;
	@Autowired
	private FrontendConfig frontendConfig;

	public String getUrlPrefix(Protocol protocol)
	{
		StringBuilder sb = new StringBuilder();

		if (protocol != null && protocol != Protocol.NONE)
		{
			sb.append(protocol.getUrlName());
			sb.append(COLON);
		}
		sb.append(SLASHSLASH);

		sb.append(frontendConfig.getWebDomain());

		if (!applicationConfig.isProduction())
		{
			sb.append(COLON);
			sb.append( protocol == Protocol.HTTPS ? frontendConfig.getWebHttpsPort() : frontendConfig.getWebHttpPort() );
		}

		return sb.toString();
	}

	public String getCssUrlPrefix(Protocol protocol)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(getUrlPrefix(protocol));
		sb.append(SLASH);
		sb.append(frontendConfig.getStaticFolderCss());

		if (applicationConfig.isProduction())
		{
			sb.append(SLASH);
			sb.append(frontendConfig.getStaticVersionCss());
		}

		return sb.toString();
	}
	
	public String getJavaScriptUrlPrefix(Protocol protocol)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(getUrlPrefix(protocol));
		sb.append(SLASH);
		sb.append(frontendConfig.getStaticFolderJs());

		if (applicationConfig.isProduction())
		{
			sb.append(SLASH);
			sb.append(frontendConfig.getStaticVersionJs());
		}

		return sb.toString();
	}

	public String getImageUrlPrefix(Protocol protocol)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(getUrlPrefix(protocol));
		sb.append(SLASH);
		sb.append(frontendConfig.getStaticFolderImg());

		return sb.toString();
	}
}
