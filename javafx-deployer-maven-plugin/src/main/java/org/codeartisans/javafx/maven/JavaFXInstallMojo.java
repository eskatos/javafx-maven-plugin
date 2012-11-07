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
import java.io.IOException;
import java.io.Writer;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.installer.ArtifactInstallationException;
import org.apache.maven.artifact.installer.ArtifactInstaller;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.artifact.ProjectArtifactMetadata;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.WriterFactory;

import static org.codeartisans.javafx.maven.InstallDeployUtils.*;

/**
 * @goal install
 * @requiresProject false
 */
public class JavaFXInstallMojo
    extends AbstractMojo
{

    /**
     * @parameter property="java.home"
     */
    private String javaHome;
    /**
     * @parameter property="localRepository"
     */
    private ArtifactRepository localRepository;
    /**
     * @component
     * @required
     */
    private ArtifactFactory artifactFactory;
    /**
     * @component
     * @required
     */
    private ArtifactInstaller installer;

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
        getLog().info( "Will install JavaFX " + version + " artifacts to the local repository." );

        // Install artifacts
        install( RUNTIME_ARTIFACTID, version, jfxRuntime );
        install( ANT_ARTIFACTID, version, jfxAnt );

        // Display GAV of installed artifacts
        getLog().info( "You can now use the following dependency in jour JavaFX projects:\n"
                       + "    <dependency>\n"
                       + "        <groupId>" + GROUPID + "</groupId>\n"
                       + "        <artifactId>" + RUNTIME_ARTIFACTID + "</artifactId>\n"
                       + "        <version>" + version + "</version>\n"
                       + "        <scope>provided</scope>\n"
                       + "    </dependency>" );
    }

    private void install( String artifactId, String version, File file )
        throws MojoExecutionException
    {
        Artifact artifact = artifactFactory.createArtifactWithClassifier( GROUPID, artifactId, version, "jar", null );
        File pomFile = generatePomFile( GROUPID, artifactId, version, "jar" );
        artifact.addMetadata( new ProjectArtifactMetadata( artifact, pomFile ) );
        artifact.setRelease( true );

        try
        {
            installer.install( file, artifact, localRepository );
        }
        catch( ArtifactInstallationException ex )
        {
            throw new MojoExecutionException( "Unable to install " + artifactId, ex );
        }
        finally
        {
            pomFile.delete();
        }

    }

    private File generatePomFile( String groupId, String artifactId, String version, String packaging )
        throws MojoExecutionException
    {
        Model model = new Model();
        model.setModelVersion( "4.0.0" );
        model.setGroupId( groupId );
        model.setArtifactId( artifactId );
        model.setVersion( version );
        model.setPackaging( packaging );
        model.setDescription( "POM was created from javafx-maven-plugin" );
        Writer writer = null;
        try
        {
            File pomFile = File.createTempFile( "mvninstall", ".pom" );
            writer = WriterFactory.newXmlWriter( pomFile );
            new MavenXpp3Writer().write( writer, model );
            return pomFile;
        }
        catch( IOException e )
        {
            throw new MojoExecutionException( "Error writing temporary POM file: " + e.getMessage(), e );
        }
        finally
        {
            IOUtil.close( writer );
        }
    }

}
