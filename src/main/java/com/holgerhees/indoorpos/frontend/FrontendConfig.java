package com.holgerhees.indoorpos.frontend;

import org.springframework.stereotype.Component;

@Component("frontendConfig")
public class FrontendConfig
{
	private String webDomain;
	private String webHttpPort;
	private String webHttpsPort;
	private boolean webHttpsApi;

	private String staticFolderUpload;
	private String staticFolderTemp;

	private String staticFolderCss;
	private String staticFolderJs;
	private String staticFolderImg;
	private Integer staticVersionCss;
	private Integer staticVersionJs;

	private Integer uploadMaxMemory;
	private Integer uploadMaxFileSize;

	public String getWebDomain()
	{
		return webDomain;
	}

	public void setWebDomain(String webDomain)
	{
		this.webDomain = webDomain;
	}

	public String getWebHttpPort()
	{
		return webHttpPort;
	}

	public void setWebHttpPort(String webHttpPort)
	{
		this.webHttpPort = webHttpPort;
	}

	public String getWebHttpsPort()
	{
		return webHttpsPort;
	}

	public void setWebHttpsPort(String webHttpsPort)
	{
		this.webHttpsPort = webHttpsPort;
	}

	public boolean getWebHttpsApi()
	{
		return webHttpsApi;
	}

	public void setWebHttpsApi(boolean webHttpsApi)
	{
		this.webHttpsApi = webHttpsApi;
	}

	public Integer getStaticVersionCss()
	{
		return staticVersionCss;
	}

	public void setStaticVersionCss(Integer staticVersionCss)
	{
		this.staticVersionCss = staticVersionCss;
	}

	public Integer getStaticVersionJs()
	{
		return staticVersionJs;
	}

	public void setStaticVersionJs(Integer staticVersionJs)
	{
		this.staticVersionJs = staticVersionJs;
	}

	public String getStaticFolderCss()
	{
		return staticFolderCss;
	}

	public void setStaticFolderCss(String staticFolderCss)
	{
		this.staticFolderCss = staticFolderCss;
	}

	public String getStaticFolderJs()
	{
		return staticFolderJs;
	}

	public void setStaticFolderJs(String staticFolderJs)
	{
		this.staticFolderJs = staticFolderJs;
	}

	public String getStaticFolderImg()
	{
		return staticFolderImg;
	}

	public void setStaticFolderImg(String staticFolderImg)
	{
		this.staticFolderImg = staticFolderImg;
	}

	public String getStaticFolderUpload()
	{
		return staticFolderUpload;
	}

	public void setStaticFolderUpload(String staticFolderUpload)
	{
		this.staticFolderUpload = staticFolderUpload;
	}

	public String getStaticFolderTemp()
	{
		return staticFolderTemp;
	}

	public void setStaticFolderTemp(String staticFolderTemp)
	{
		this.staticFolderTemp = staticFolderTemp;
	}

	public Integer getUploadMaxMemory()
	{
		return uploadMaxMemory;
	}

	public void setUploadMaxMemory(Integer uploadMaxMemory)
	{
		this.uploadMaxMemory = uploadMaxMemory;
	}

	public Integer getUploadMaxFileSize()
	{
		return uploadMaxFileSize;
	}

	public void setUploadMaxFileSize(Integer uploadMaxFileSize)
	{
		this.uploadMaxFileSize = uploadMaxFileSize;
	}
}
