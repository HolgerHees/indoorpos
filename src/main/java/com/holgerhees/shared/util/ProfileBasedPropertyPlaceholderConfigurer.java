package com.holgerhees.shared.util;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;

public class ProfileBasedPropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer
{

    public static String getHostname()
    {
        try
        {
            return InetAddress.getLocalHost().getHostName();
        } catch( UnknownHostException e )
        {
            throw new RuntimeException("Unable to resolve hostname", e);
        }
    }

    private static boolean shouldBeReplaced(Resource location)
    {
        String name = location.getFilename();
        return name.endsWith("version.properties") || name.endsWith("shared.properties") ? false : true;
    }

    protected static Resource replaceLocation(Resource location)
    {
        ClassPathResource oldLocation = (ClassPathResource) location;

        String oldPath = oldLocation.getPath();
        String newPath = replaceName(oldPath);
        if( newPath == null )
        {
            return null;
        }
        return new ClassPathResource(newPath);
    }

    public static String replaceName(String name)
    {
        return name.replace(".properties", "." + getHostname() + ".properties");
    }

    @Override
    public void setLocation(Resource location)
    {
        setLocations(location);
    }

    @Override
    public void setLocations(Resource... locations)
    {
        List<Resource> locationsToSet = new LinkedList<>();
        for( Resource location : locations )
        {
            if( !shouldBeReplaced(location) )
            {
                locationsToSet.add(location);
                continue;
            }

            Resource profileBasedLocation = replaceLocation(location);
            if( profileBasedLocation == null )
            {
                throw new IllegalStateException(
                        "Unable to replace resource with profile based resource. Resource type was: [" + location.getClass().getName() + "]");
            }
            locationsToSet.add(profileBasedLocation);
        }

        super.setLocations(locationsToSet.toArray(new Resource[locationsToSet.size()]));
    }
}
