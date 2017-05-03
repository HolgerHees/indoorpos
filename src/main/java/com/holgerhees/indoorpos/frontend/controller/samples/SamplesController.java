package com.holgerhees.indoorpos.frontend.controller.samples;

import com.holgerhees.indoorpos.frontend.controller.Controller;
import com.holgerhees.shared.web.model.Request;
import com.holgerhees.shared.web.service.PageDtoInitService;
import com.holgerhees.shared.web.view.JspView;
import com.holgerhees.shared.web.view.View;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component( "samplesController" )
public class SamplesController implements Controller
{
    @Autowired
    PageDtoInitService pageDtoInitService;

    @Override
    public View handle( Request request )
    {
        SamplesPageDTO ctx = pageDtoInitService.getPageDto( new SamplesPageDTO(), request );

        return new JspView( "/WEB-INF/jsp/samples.jsp", request );
    }

}
