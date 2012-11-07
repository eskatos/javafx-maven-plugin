/*
 * Copyright 2012 Paul Merlin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codeartisans.javafx.maven;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/* package */ class InstallDeployUtils
{

    /* package */ static final String GROUPID = "com.sun.javafx";
    /* package */ static final String RUNTIME_ARTIFACTID = "jfxrt";
    /* package */ static final String ANT_ARTIFACTID = "ant-javafx";

    /* package */ static File findJfxFile( File javaHomeDir, String filename )
        throws MojoExecutionException
    {
        File file = new File( javaHomeDir, "lib/" + filename );
        if( file.exists() )
        {
            // JAVA_HOME points to a JRE
            return file;
        }
        file = new File( javaHomeDir, "jre/lib/" + filename );
        if( file.exists() )
        {
            // JAVA_HOME points to a JDK
            return file;
        }
        file = new File( javaHomeDir, "../lib/" + filename );
        if( file.exists() )
        {
            // JAVA_HOME points to a JRE inside a JDK
            return file;
        }
        throw new MojoExecutionException( "Unable to find JavaFX '" + filename + "' at '" + javaHomeDir
                                          + "'. Is your JAVA_HOME set to a JDK with JavaFX installed "
                                          + "(must be at least Java 7u9)?" );
    }

    /* package */ static String loadJavaFxRuntimeVersion( File jfxPropertiesFile )
        throws MojoFailureException
    {
        Properties jfxProperties = loadJavaFxProperties( jfxPropertiesFile );
        return (String) jfxProperties.get( "javafx.runtime.version" );
    }

    private static Properties loadJavaFxProperties( File jfxPropertiesFile )
        throws MojoFailureException
    {
        FileInputStream input = null;
        try
        {
            Properties jfxProperties = new Properties();
            input = new FileInputStream( jfxPropertiesFile );
            jfxProperties.load( input );
            return jfxProperties;
        }
        catch( IOException ex )
        {
            throw new MojoFailureException( "Unable to load JavaFX Properties", ex );
        }
        finally
        {
            try
            {
                input.close();
            }
            catch( IOException ignored )
            {
            }
        }
    }

    private InstallDeployUtils()
    {
    }

}
