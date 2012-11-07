package org.codeartisans.javafx.maven;

import java.io.File;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import static org.codeartisans.javafx.maven.InstallDeployUtils.*;
import static org.twdata.maven.mojoexecutor.MojoExecutor.*;

/**
 * @goal install
 */
public class JavaFXInstallMojo
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
    /**
     * @parameter property="session"
     * @required
     * @readonly
     */
    protected MavenSession session;
    /**
     * @component
     * @required
     */
    protected BuildPluginManager pluginManager;

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
        executeMojo( plugin( groupId( "org.apache.maven.plugins" ),
                             artifactId( "maven-install-plugin" ),
                             version( "2.4" ) ),
                     goal( "install-file#javafx-install" ),
                     configuration( element( "packaging", "jar" ),
                                    element( "generatePom", "true" ),
                                    element( "groupId", GROUPID ),
                                    element( "artifactId", artifactId ),
                                    element( "version", version ),
                                    element( "file", file.getAbsolutePath() ) ),
                     executionEnvironment( project,
                                           session,
                                           pluginManager ) );
    }

}
