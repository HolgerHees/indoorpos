package com.holgerhees.indoorpos.frontend.controller.files;

import com.holgerhees.indoorpos.frontend.FrontendConfig;
import com.holgerhees.indoorpos.frontend.controller.Controller;
import com.holgerhees.shared.web.model.Request;
import com.holgerhees.shared.web.view.ImageView;
import com.holgerhees.shared.web.view.TextView;
import com.holgerhees.shared.web.view.View;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

@Component( "pictureController" )
public class PictureController implements Controller
{
    private static Log LOGGER = LogFactory.getLog( PictureController.class );

    @Autowired
    FrontendConfig frondendConfig;

    @Override
    public View handle( Request req )
    {
        String[] parts = req.getServletPath().split( "/" );

        File file = new File( frondendConfig.getStaticFolderUpload() + parts[parts.length - 1] );
        try
        {
            InputStream input = new FileInputStream( file );
            return new ImageView( req, file, input, "image/jpeg" );
        } catch( FileNotFoundException e )
        {
            LOGGER.error( "Imagefile '" + req.getServletPath() + "' not found" );
            return new TextView( req, "Image not found", HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
        }
    }
}
