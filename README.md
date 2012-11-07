# javafx-maven-plugin

This plugin is deployed to maven central.

    <groupId>org.codeartisans.javafx</groupId>
    <artifactId>javafx-maven-plugin</artifactId>
    <version>1.1</version>

## Usage

### JavaFX artifacts installation and deployment

JavaFX artifacts are not available in any public repository so special steps are needed to overcome this.

You must at least install JavaFX artifacts to your local repository prior to using this plugin as itself depends on them. Kind of chicken and egg issue yeah.

Prior to this you must have a JavaFX runtime installed. Then, not in a maven project directory, run:

    mvn org.codeartisans.javafx:javafx-deployer-maven-plugin:1.1:install

This will install the JavaFX from your installation to your local maven repository so your projects can refer to them. The dependency snippet will be shown:

    [INFO] --- javafx-deployer-maven-plugin:1.1:install (default-cli) @ javafx-deployer-maven-plugin ---
    [INFO] Will install JavaFX 2.2.1 artifacts to the local repository.
    [INFO] Installing /path/to/jdk1.7.0_07.jdk/jre/lib/jfxrt.jar to /home/you/.m2/repository/com/sun/javafx/jfxrt/2.2.1/jfxrt-2.2.1.jar
    [INFO] Installing /path/to/generated-jfxrt-2.2.1.pom to /home/you/.m2/repository/com/sun/javafx/jfxrt/2.2.1/jfxrt-2.2.1.pom
    [INFO] Installing /path/to/jdk1.7.0_07.jdk/lib/ant-javafx.jar to /home/you/.m2/repository/com/sun/javafx/ant-javafx/2.2.1/ant-javafx-2.2.1.jar
    [INFO] Installing /path/to/generated-ant-javafx-2.2.1.pom to /home/you/.m2/repository/com/sun/javafx/ant-javafx/2.2.1/ant-javafx-2.2.1.pom
    [INFO] You can now use the following dependency in jour JavaFX projects:
        <dependency>
            <groupId>com.sun.javafx</groupId>
            <artifactId>jfxrt</artifactId>
            <version>2.2.1</version>
            <scope>provided</scope>
        </dependency>

You can directly use the dependency snippet shown above in your projects but can also use version ranges. Here is an example using JavaFX 2.0 or greater:

    <dependency>
        <groupId>com.sun.javafx</groupId>
        <artifactId>jfxrt</artifactId>
        <version>[2.0,)</version>
        <scope>provided</scope>
    </dependency>

### Use maven-exec-plugin to run your JavaFX Application

    <plugin>
        <groupId>org.codehaus.mojo</groupId>
        <artifactId>exec-maven-plugin</artifactId>
        <version>1.2.1</version>
        <executions>
            <execution>
                <goals>
                    <goal>java</goal>
                </goals>
            </execution>
        </executions>
        <configuration>
            <mainClass>${main.class}</mainClass>
            <classpathScope>runtime</classpathScope>
            <includePluginDependencies>true</includePluginDependencies>
        </configuration>
        <dependencies>
            <dependency>
                <groupId>com.sun.javafx</groupId>
                <artifactId>jfxrt</artifactId>
                <version>[2.0,)</version>
            </dependency>
        </dependencies>
    </plugin>


### Enhance your JAR for JavaFX

    <plugin>
        <groupId>org.codeartisans.javafx</groupId>
        <artifactId>javafx-maven-plugin</artifactId>
        <version>1.1</version>
        <executions>
            <execution>
                <phase>package</phase>
                <goals>
                    <goal>package</goal>
                </goals>
            </execution>
        </executions>
        <configuration>
            <mainClass>${main.class}</mainClass>
        </configuration>
    </plugin>


### Package JavaFX distributions and installers

    <plugin>
        <groupId>org.codeartisans.javafx</groupId>
        <artifactId>javafx-maven-plugin</artifactId>
        <version>1.1</version>
        <executions>
            <execution>
                <phase>package</phase>
                <goals>
                    <goal>package</goal>
                </goals>
            </execution>
        </executions>
        <configuration>
            <mainClass>${main.class}</mainClass>
            <!-- This will build and attach native images and installers -->
            <!-- Can be all, installer, image or none, case insensitive. -->
            <bundles>all</bundles>
        </configuration>
    </plugin>


