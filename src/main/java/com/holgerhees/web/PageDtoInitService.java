package com.holgerhees.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.holgerhees.web.model.PageDTO;
import com.holgerhees.web.model.Request;

@Service( "pageDtoInitService" )
public class PageDtoInitService
{

	@Autowired
	private UrlPrefixService urlPrefixService;

	public <T extends PageDTO> T getPageDto(T ctx, Request request)
	{
		ctx.setCssPrefix(urlPrefixService.getCssUrlPrefix(request.getProtocol()));
		ctx.setJsPrefix(urlPrefixService.getJavaScriptUrlPrefix(request.getProtocol()));
		ctx.setImgPrefix(urlPrefixService.getImageUrlPrefix(request.getProtocol()));

		request.setPageDTO(ctx);

		return ctx;
	}
}
