# Building Mule Migration Assistant (MMA)

[Maven](http://maven.apache.org/) is the build automation tool used in Mule Migration Assistant. To modify or add to MMA source code, it's specially relevant to understand the hierarchy of maven projects that represent the source code.  

To be able to build MMA you will need Maven among other things. Refer to the [CONTRIBUTING.md](CONTRIBUTING.md) file to find the installation instructions and prerequisites necessary to build it.

In the following sections you will learn how MMA is organized, how to build it and how to troubleshoot problems we might find.

### Mule Migration Assistant Source Code Sub-projects

The MMA source code project is neatly organized into a hierarchy of sub-projects. (Sub-projects are sometimes referred to as "modules", but to avoid confusion, we use "sub-projects") Each sub-project generally produces one Maven artifact, usually a JAR file. The Mule build is structured using intermediate project object models (POMs) to group common sub-projects together. These intermediate sub-projects are sub-folders of the source code's mule folder, and are described in the table below.

| Sub-project       | Description  |  
|-------------------|--------------|  
| `migration-contribution-archetype`      | Maven archetype to generate a contribution project |
| `mule-migration-tool-api`            | MMA's API and functionality to be consumed by any migration contribution       |  
| `mule-migration-tool-contribution`          | The external distributions    |  
| `mule-migration-tool-expression`        | MEL to DW expressions migrator |
| `mule-migration-tool-library`         | Default migrations that are shipped within MMA
| `mule-migration-tool-engine`           | MMA's execution engine |
| `runner`           | Console implementation of the migration assistant |
| `mule-migration-tool-tests`      | Integration Tests |
 

### Building Mule Migration Assistant

The following table lists common goals to execute for building Mule. These goals are standard Maven goals, refer to [Maven's documentation](http://maven.apache.org/guides/getting-started/maven-in-five-minutes.html#Running_Maven_Tools) for more info on them.

|Command | Description |
|:----------|:-------------|
| `mvn clean`	 | purges any built artifacts or intermediate files (such as .class) from the target directory |
| `mvn install` | installs all modules but the distributions to your local repository, will run all tests. |
| `mvn test`    | runs any unit tests for this sub-project |
| `mvn -DskipTests install` |	By default, Maven runs all unit tests for each project for each build which, depending on the project, can take a long time to complete. If you wish, you can skip the tests using this command.|

#### Build properties

In addition to the standard properties, the following ones can change the behaviour of the Mule build:

| Property                  | Default      | Description  |
|:--------------------------|:-------------|:-------------|
| `skipIntegrationTests`	   | `false`      | Skip flag for integration tests |
| `skipVerifications`       | `false`      | Skip flag for the license check, version enforce, style checks, etc.|
| `formatterGoal=format`       | `check`      | Enforce the coding standards defined on [formatter.xml](https://github.com/mulesoft/mule-migration-tool/blob/master/formatter.xml) into your changes.|

To set these properties, it's necessary to pass them in the maven command line as `-DPropertyName=value` where value is optional for `true`. Therefore, to skip the integration tests we can `-DskipIntegrationTests`.

It is important to remember that the rest of the maven plugins flags are still applicable, for instance to disable tests in the surefire plugin we could do: `-DskipTests`.

### Troubleshooting Maven

This section describes some problems you might experience using Maven and how to resolve or work around them.

| Problem                             | Description  |  Solution    |
|:------------------------------------|:-------------|:------------| 
| Files could not be retrieved	       | You are behind a firewall and get an error stating that repository metadata for org.mule.tools could not be retrieved from the central repository.|Check the proxy settings in your Maven settings.xml file to confirm that they are configured correctly.|
|OutOfMemory Error                    | You encounter OutOfMemoryError exceptions when attempting a full build of Mule.| Increasing the max heap and the PermGen space sizes. To do so, either export a MAVEN_OPTS variable in your shell, or add the variable to the original mvn script. Use the following: `MAVEN_OPTS=-Xmx512m -XX:MaxPermSize=256m` |
|Slow build                           | 	-	 |If you know your downloads are up-to-date, you can use the offline option using the following command: `mvn -o` |
| Conflicting transitive dependencies | Transitive dependencies in m2 are both powerful and problematic at times. For example, you may have conflicting library versions or when unwanted libraries are in your classpath.|	Use the debug option to display the effective classpath in a tree format, making it easy to see where each library is coming from: `mvn -x` |
| Running a goal for a specific project| By default, Maven execute a goal for a project and all sub-projects in its hierarchy. |	If you want to run the goal for a specific project only (and not its children), you can use the non-recursive option: `mvn -N` |
| Debugging test failures | 	Surefire, the default Maven test runner, outputs all reports as a set of XML and text files. Any test failure details and stack traces are written to those files instead of the console, so it can be time consuming to open files to find problems. | You can redirect the output to the console temporarily by adding the following option: `mvn -Dsurefire.useFile=false`. This option skips creation ofthe text report, but still makes the XML report available for transformation by tools. | 
