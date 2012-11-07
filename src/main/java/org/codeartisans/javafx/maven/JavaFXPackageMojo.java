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

import com.sun.javafx.tools.packager.CreateJarParams;
import com.sun.javafx.tools.packager.DeployParams;
import com.sun.javafx.tools.packager.Log;
import com.sun.javafx.tools.packager.PackagerException;
import com.sun.javafx.tools.packager.PackagerLib;
import com.sun.javafx.tools.packager.bundlers.Bundler.BundleType;
import de.schlichtherle.truezip.file.TFile;
import de.schlichtherle.truezip.file.TVFS;
import java.io.File;
import java.io.IOException;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Build;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.util.FileUtils;

/**
 * @goal package
 * @requiresDependencyResolution
 */
public class JavaFXPackageMojo
    extends AbstractMojo
{

    /**
     * @parameter property="mainClass"
     * @required
     */
    private String mainClass;
    /**
     * @parameter property="bundleType" default-value="none"
     * @required
     */
    private String bundles;
    /**
     * @parameter property="verbose" default-value="true"
     */
    private Boolean verbose;
    /**
     * @parameter property="project"
     * @required
     * @readonly
     */
    private MavenProject project;
    /**
     * @component
     * @required
     */
    private MavenProjectHelper projectHelper;

    @Override
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        Build build = project.getBuild();
        File buildDir = new File( build.getDirectory() );

        // Unpack main artifact JAR, necessary to keep maven metadata in JavaFX JAR
        File unpackedJarDir = new File( buildDir, "javafx-unpacked-project-artifact" );
        try
        {
            FileUtils.mkdir( unpackedJarDir.getAbsolutePath() );
            new TFile( project.getArtifact().getFile() ).cp_rp( unpackedJarDir );
            TVFS.umount();
        }
        catch( IOException ex )
        {
            throw new MojoExecutionException( "Unable to unpack project JAR", ex );
        }

        // Create JavaFX JAR
        Log.setLogger( new Log.Logger( verbose ) );

        String javaFxJarName = build.getFinalName() + "-javafx.jar";
        File javaFxCreateJarDir = new File( buildDir, "javafx-create-jar" );

        CreateJarParams createJar = new CreateJarParams();
        createJar.setVerbose( verbose );
        createJar.setApplicationClass( mainClass );
        createJar.setOutdir( javaFxCreateJarDir );
        createJar.setOutfile( javaFxJarName );
        createJar.addResource( unpackedJarDir, "" );

        try
        {
            getLog().info( "Packaging JavaFX JAR" );

            PackagerLib packager = new PackagerLib();
            packager.packageAsJar( createJar );

            // Replace main artifact with JavaFX JAR
            File mainArtifactFile = project.getArtifact().getFile();
            FileUtils.forceDelete( mainArtifactFile );
            mainArtifactFile.delete();
            FileUtils.copyFile( new File( javaFxCreateJarDir, javaFxJarName ), mainArtifactFile );
        }
        catch( PackagerException ex )
        {
            throw new MojoExecutionException( "Unable to create JavaFX JAR", ex );
        }
        catch( IOException ex )
        {
            throw new MojoExecutionException( "Unable to attach JavaFX JAR", ex );
        }

        // Create JavaFX native packaging
        BundleType bundleType = BundleType.valueOf( bundles.toUpperCase() );
        File dependenciesDir = new File( buildDir, "dependencies" );
        String dependenciesPath = dependenciesDir.getAbsolutePath();

        getLog().info( "Copying project dependencies to '" + dependenciesPath + "' for packaging." );
        try
        {
            FileUtils.mkdir( dependenciesPath );
            for( Artifact artifact : project.getArtifacts() )
            {
                String fileName = artifact.getGroupId() + "-" + artifact.getFile().getName();
                FileUtils.copyFile( artifact.getFile(), new File( dependenciesDir, fileName ) );
            }
        }
        catch( IOException ex )
        {
            throw new MojoExecutionException( "Unable to copy dependencies to '" + dependenciesPath + "' for packaging.", ex );
        }

        File javaFxNativeDir = new File( buildDir, "javafx-native" );

        DeployParams deployParams = new DeployParams();
        deployParams.setVerbose( verbose );
        deployParams.setId( build.getFinalName() );
        deployParams.setAppId( build.getFinalName() );
        deployParams.setAppName( build.getFinalName() );
        deployParams.setVersion( project.getVersion() );
        deployParams.setTitle( build.getFinalName() );
        deployParams.setDescription( build.getFinalName() );
        deployParams.setApplicationClass( mainClass );
        deployParams.setOutdir( javaFxNativeDir );
        deployParams.setOutfile( build.getFinalName() );
        deployParams.setBundleType( bundleType );

        deployParams.addResource( javaFxCreateJarDir, javaFxJarName );
        if( dependenciesDir.exists() )
        {
            deployParams.addResource( dependenciesDir, "" );
        }

        try
        {
            getLog().info( "Packaging JavaFX Application" );

            PackagerLib packager = new PackagerLib();
            packager.generateDeploymentPackages( deployParams );

            // Create and attach JNLP Artifact
            File javaFxJnlpDir = new File( buildDir, "javafx-jnlp" );
            FileUtils.mkdir( javaFxJnlpDir.getAbsolutePath() );
            for( File file : javaFxNativeDir.listFiles() )
            {
                if( file.isFile() )
                {
                    file.renameTo( new File( javaFxJnlpDir, file.getName() ) );
                }
            }
            File jnlpZipFile = new File( buildDir, build.getFinalName() + "-jnlp.zip" );
            new TFile( javaFxJnlpDir ).cp_rp( new TFile( jnlpZipFile ) );
            TVFS.umount();
            projectHelper.attachArtifact( project, "zip", "jnlp", jnlpZipFile );

            // Attach native bundles
            File bundlesDir = new File( javaFxNativeDir, "bundles" );
            for( File bundle : bundlesDir.listFiles() )
            {
                if( bundle.isFile() )
                {
                    // Simple file bundle (exe, msi, rpm, dmg)
                    String ext = FileUtils.extension( bundle.getName() );
                    projectHelper.attachArtifact( project, ext, ext, bundle );
                }
                else if( bundle.isDirectory() )
                {
                    // Directory bundle
                    if( bundle.getName().endsWith( ".app" ) )
                    {
                        // MacOSX Application bundle, will zip it before attach
                        File macosxAppZip = new File( buildDir, build.getFinalName() + "-macosx.zip" );
                        new TFile( bundle ).cp_rp( new TFile( macosxAppZip, bundle.getName() ) );
                        TVFS.umount();
                        projectHelper.attachArtifact( project, "zip", "macosx", macosxAppZip );
                    }
                    else
                    {
                        // TODO Windows/Linux ARCHIVE support
                        getLog().warn( "Unknown bundle type: '" + bundle.getAbsolutePath() + "', doing nothing." );
                    }
                }
            }

        }
        catch( PackagerException ex )
        {
            throw new MojoExecutionException( "Unable to package JavaFX Native Bundle(s)", ex );
        }
        catch( IOException ex )
        {
            throw new MojoExecutionException( "Unable to attach JavaFX Native Bundle(s)", ex );
        }
    }

}
