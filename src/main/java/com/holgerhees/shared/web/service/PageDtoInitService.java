package com.holgerhees.shared.web.service;

import com.holgerhees.shared.web.model.PageDTO;
import com.holgerhees.shared.web.model.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
