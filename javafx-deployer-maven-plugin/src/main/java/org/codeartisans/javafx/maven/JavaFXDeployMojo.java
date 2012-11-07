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
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import static org.codeartisans.javafx.maven.InstallDeployUtils.*;

/**
 * @goal deploy
 * @requiresProject false
 */
public class JavaFXDeployMojo
    extends AbstractMojo
{

    /**
     * @parameter property="java.home"
     */
    private String javaHome;
    /**
     * @parameter property="project"
     * @required
     * @readonly
     */
    protected MavenProject project;

    @Override
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        // Find JavaFX files
        File javaHomeDir = new File( javaHome );
        File jfxPropertiesFile = findJfxFile( javaHomeDir, "javafx.properties" );
        File jfxRuntime = findJfxFile( javaHomeDir, "jfxrt.jar" );
        File jfxAnt = findJfxFile( javaHomeDir, "ant-javafx.jar" );

        // Gather Runtime version
        String version = loadJavaFxRuntimeVersion( jfxPropertiesFile );
        getLog().info( "Will deploy JavaFX " + version + " artifacts." );

        throw new UnsupportedOperationException( "Not supported yet. Contribution wanted! :-)" );
    }

}
