
# Thank you! 

Really, thank you. Thank you for taking some of your precious time helping the Mule Migration Assistant (MMA) project move forward.

This guide will help you get started with Mule Migration Assistant's development environment. You'll also find the set of rules you're expected to follow in order to submit improvements/fixes to the migration assistant or to contribute with new migration tasks.

In this guide you will find:

- [Before you begin](#before-you-begin) 
    - [Getting to know better Mule Migration Assistant](#getting-to-know-better-mule-migration-assistant)     
    - [Understanding the different types of contributions](#understanding-the-migration-task-locator-mechanisms)
- [Setting up the development environment](#setting-up-the-development-environment) 
    - [Installing Prerequisites](#installing-prerequisites) 
    - [Getting the Source Code](#getting-the-source-code) 
- [Configuring the IDE](#configuring-the-ide)
    - [Working with Eclipse](#working-with-eclipse)
    - [Working with IntelliJ IDEA](#working-with-intellij-idea) 
- [Developing your contribution](#developing-your-contribution)    
    - [Creating your topic branch](#creating-your-topic-branch)
    - [Updating Your topic Branch](#updating-your-topic-branch)
    - [Submitting a Pull Request](#submitting-a-pull-request)
- [Contribute a new component migration to MMA](#contribute-a-new-component-migration-to-mma)
    - [Creating your migration task repository](#creating-your-migration-task-repository)    
    - [Develop your component migration](#develop-your-component-migration)
    - [Document new component support](#document-new-component-support)
    - [Publish your migration to Maven repository](#publish-your-migration-to-maven-repository)
    - [Include your migration into MMA](#include-your-migration-into-mma)   
- [Summary](#summary)

-----------------------------------------------------------------------------

 
# Before you begin 
The migration assistan is an easy project to contribute with. Before contributing to the Mule Migration Assistant source code, it's important to understand the domain of the use case from the user point of view and the different mechanisms to extend the assistant.

## Getting to know better Mule Migration Assistant
Here you can find useful sources to understand better the Mule Migration Assistant and its forthcoming features:

- MuleSoft's [Docs Site](https://beta.docs.stgx.mulesoft.com/beta-mule-migration-tool/mule-runtime/4.1/migration-tool). The fastest way of knowing about new features and missing scenarios. 
- The Mule Migration Assistant Github [repository](https://github.com/mulesoft/mule-migration-tool/blob/master/README.md). You can see detailed information about the Mule Migration Assistant architecture.

## Understanding the migration task locator mechanisms (TBD)
Explain different types of contribution: fix on the assistant or new component migration.

## Visiting the community meeting points

If you are here reading this document, you probably have already in mind a new feature or a bug fix to work on. This is great, however there could be other members of the community with the same idea.  

Before you begin, please take a few minutes to review community meeting points to make sure someone else hasn't already taken on your challenge:

1. Review [existing JIRAs](http://www.mulesoft.org/jira/browse/MMT) to see if a bug has already been logged.
2. Follow the [Mule forum](http://forum.mulesoft.org/mulesoft) chatter to see if anyone else has begun to resolve the problem or initiate the improvement. 
If, in the above-listed resources, no-one else has initiated your improvement or fix, log the issue by creating a [MMA JIRA](http://www.mulesoft.org/jira/browse/MMT).  JIRA issues a identifier for your issue; keep this handy as you will use it to create a branch later if you decide to fix it yourself.
 
# Setting up the development environment

While getting ready to contribute to any piece of software we will need to install number of prerequisites, we will need also to obtain the preexisting source code there could be. In this section we will follow some installation steps for the prerequisites and also we will download the source code.

## Installing Prerequisites

Before you get started, you need to set yourself up with an environment in which to develop Mule.  Your dev environment needs three things: a Java SDK, a recent version of Maven, an integration development environment (IDE), and new branch of code to work on.

### JDK

1. If you are working with **Windows** or **Linux**, install Java SE 8u152 or a later update on your local drive. It can be downloaded from [Java Development Kits](http://www.oracle.com/technetwork/java/javase/downloads/index.html). If you are working on a **Mac**, simply confirm that the JDK shipped with your Mac OS X is *Java SE Development Kit 8 (also known as Java SE 8u152)* or a later update using the command `java -version`, then you can skip this section: 
2. Create an environment variable called `JAVA_HOME`, setting it to the directory in which you installed the JDK. 
3. Update the PATH environment variable so that it includes the path to JDK binaries. Add the following line to the PATH variable:
    - Windows: `%JAVA_HOME%/bin`
    - Linux or Mac OS X: `$JAVA_HOME/bin`

### Maven

1. Download the Maven distribution from the [Maven web site](http://maven.apache.org/download.cgi), then unpack it to a convenient folder on your local drive. Mule requires Maven version >= 3.3.9. 
2. Create an environment variable called `M2_HOME`, then set it to the folder into which you unpacked Maven. 
3. Update the `PATH` environment variable to include the path to Maven binaries. 
    - Windows: add the following line to the `PATH` variable: `%M2_HOME%/bin` 
    - Mac or Linux: add the following line to the `PATH` variable: `$M2_HOME/bin`          

## Getting the Source Code

MMA source code lives on Github. Complete the following procedure to locate the code and get it onto your local drive.

	
If you're new to Git, consider reading [Pro Git](http://git-scm.com/book) to absorb the basics.
 Just want a Read-Only version of MMA source code?

1. [Create](https://help.github.com/articles/signing-up-for-a-new-github-account) or log in to your github account. 
2. If you haven't already done so, [set up git](https://help.github.com/articles/set-up-git) on your local drive. 
3. Navigate to MMA's github page at: [https://github.com/mulesoft/mule-migration-tool.git](https://github.com/mulesoft/mule-migration-tool.git) 
4. Click the Fork button at the top right corner of the page, then select your own git repository into which github inserts a copy of the repository.
5. Prepare to clone your forked Mule Migration Assistant repository from your github account to your local drive via a secure file transfer connection. As per git's recommendation, we advise using HTTPS to transfer the source code files to your local drive. However, if you prefer to establish a secure connection for transferring the files via SSH, follow git's procedure to [generate SSH keys](https://help.github.com/articles/generating-ssh-keys).
6. In the command line, create or navigate to an existing folder on your local drive into which you wish to store your forked clone of Mule source code.
7. From the command line, execute one of the following:
    - For **HTTPS**:  `git clone https://github.com/<yourreponame>/mule`
    - For **SSH**:  `git clone git@github.com:<username>/<repo-name>.git`
8. Add the upstream repository so that you can pull changes and stay updated with changes to the mule-3.x (i.e. master) branch. From the command line, execute one of the following:
    - For **HTTPS**: `git remote add upstream https://github.com/mulesoft/mule-migration-tool.git`
    - For **SSH**: `git remote add upstream git@github.com:mulesoft/mule-migration-tool.git`

## Understanding the build
This is an excellent moment to read the guide to [build Mule](BUILD.md). A correct understanding of how the Mule project is organized and build is key for a productive development.

We are ready to develop our improvements. However, instead of doing it manually we may want to configure an IDE for better productivity. We will do it in the next section.

# Configuring the IDE

This section offers tips for importing and working on Mule source code in  **Eclipse** or **IntelliJ IDEA**. There are no restrictions on the type of integration development environment you use to develop Mule, we simply chose to discuss the above-listed three as common IDEs.

### Working with Eclipse

An open-source integration development platform, use Eclipse to modify or add to your cloned version of Mule source code.

#### Importing

1. Download and install [Eclipse](http://www.eclipse.org/downloads/) on your local drive.
2. From the command line, in the directory into which you downloaded the Mule source code, enter the following command to generate the classpath and project files for each sub-project:  `mvn eclipse:eclipse`.
3. Before launching Eclipse, make the Maven repository known in Eclipse using the following command: `mvn -Declipse.workspace=/path/to/eclipse/workspace eclipse:configure-workspace`.
4. Launch Eclipse, selecting the workspace you just "mavenized".
5. Select **File > Import**.
6. In the **Import** wizard, click to expand the **General** folder, then select **Existing Projects into Workspace**, then click **Next**.
7. In the **Select root directory** field, use the **Browse** button to navigate to the directory into which you downloaded the cloned fork of Mule source code from your Github account.
8. Ensure all **Projects** are checked, then click **Finish**. Eclipse imports the mule source code. 
9. Open source code files as you need to edit or add content.
10. Click the **Save** icon to save locally.

#### Debugging

You can debug following these steps. There is also a more in-depth guide available in the [Mule documentation site](https://developer.mulesoft.com/docs/display/current/Debugging).

1. In Eclipse, select **Window >  Open Perspective > Other...**, then select **Java** to open the Java Perspective.
2. Select **File > New > Java Project**. You are creating a new project just for launching Mule.
3. In the **New Java Project wizard**, select a **Name** for your project, such as Mule Launcher, then click **Next**.
4. In the **Java Settings** panel of the wizard, select the **Projects** tab, then click **Add**.
5. Click **Select All**, then click **OK**, then **Finish**.
6. In the Package Explorer, right click your launcher project's name, then select **Debug As > Debug Configurations...**
7. In the **Debug Configurations** wizard, double-click **Java Application**.
10. Click **Apply**, then click **Debug**.
11. Eclipse requests permission to switch to the **Debug Perspective**; click **Yes** to accept and open. 

#### Debugging Remotely

1. From the command line, edit the `JPDA_OPTS` variable in the Mule startup script and specify the debugger port.
2. Start the Mule server with the `-debug` switch. The server waits until a debugger attaches.
3. In the Package Explorer in studio, right-click your Mule source code project's name, then select ***Debug > Debug Configurations...***
4. Double-click ***Remote Java Application***.
5. Under ***Connection Properties***, enter a value for ***Host*** and ***Port***, then click ***Apply***.
6. Click Debug. Eclipse requests permission to switch to the ***Debug Perspective***; click ***Yes*** to accept and open.

#### Testing

Use Maven to run unit test on your project using the following command: `mvn test`.

In addition to the unit tests for each sub-project, the Mule parent project has a separate sub-project containing integration tests. These tests verify "macroscopic" functionality that could not be tested by any single sub-project alone.

#### Setting Eclipse Startup Parameters

The table below lists a number of command-line parameters you can use to alter Eclipse's startup behavior, if you wish. 

| Parameter         | Action                                             |  
|:------------------|:---------------------------------------------------|  
| `-clean`          | enables clean registration of plug-in (some plug-ins do not always register themselves properly after a restart) |
| `-nosplash`       | does not show Eclipse or plug-in splash screens    |  
| `-showLocation`   | allows you to explicitly set which JDK to use      |  
| `-vm`             | examples that come with the full Mule distribution |
| `-vmargs`         | allows you to pass in standard VM arguments        |
 

### Working with IntelliJ IDEA

Use IntelliJ's IDEA integration platform to modify or add to your cloned Mule source code.

#### Importing

1. [Download](https://www.jetbrains.com/idea/download/) and install IntelliJ IDEA.
2. Open IDEA, then select ***File > Open...***
3. Browse to the directory into which you downloaded the Mule source code, then select the `pom.xml` file. 
4. Click ***OK***. IDEA takes awhile to process all the pom.xml files.

#### Debugging Remotely

You can debug following these steps. There is also a more in-depth guide available in the [Mule documentation site](https://developer.mulesoft.com/docs/display/current/Debugging).

1. Start the Mule server with the `-debug` switch. The server waits until a debugger attaches.
2. In IDEA, select **Run > Edit Configurations...** to open the **Run/Debug Configurations** window.
3. Click **Add New Configuration** (plus sign), then select **Remote**.
4. Enter a **name** for the configuration, then update the **host** and **port** values if required (You can use the default values, localhost:5005, for debugging a local mule instance).
5. Click **OK** to start the debugging session.

#### Testing

Use Maven to run unit tests on your project using the following command: `mvn test`.

In addition to the unit tests for each sub-project, the Mule parent project has a separate sub-project containing integration tests. These tests verify "macroscopic" functionality that could not be tested by any single sub-project alone.

We finally have everything ready to start writing code. Lets start with the code and also learn how to commit it in the next section.

#  Developing your contribution

Working directly on the master version of Mule source code would likely result in merge conflicts with the original master. Instead, as a best practice for contributing to source code, work on your project in a feature branch.

## Creating your feature branch

In order to create our feature branch we should follow these steps:

1. From your local drive, create a new branch in which you can work on your bug fix or improvement using the following command:
`git branch yourJIRAissuenumber`.
2. Switch to the new branch using the following command: 
`git checkout yourJIRAissuenumber`.

Now we should be able to make our very first compilation of the Mule Runtime source code. We just need to instruct Maven to download all the dependent libraries and compile the project, you can do so execution the following command within the directory into which you cloned the Mule source code: `mvn -DskipTests install`.

Note that if this is your first time using Maven, the download make take several minutes to complete.

> ** Windows and the local Maven repository **  
> In Windows, Maven stores the libraries in the .m2 repository in your home directory.   For example, `C:\Documents and Settings\<username>\.m2\repository`.  Because Java RMI tests fail where a directory name includes spaces, you must move the Maven local repository to a directory with a name that does not include spaces, such as `%M2_HOME%/conf` or `%USERPROFILE%/.m2`

Now that you're all set with a local development environment and your own branch of Mule source code, you're ready get kicking! The following steps briefly outline the development lifecycle to follow to develop and commit your changes in preparation for submission.

1. If you are using an IDE, make sure you read the previous section about [IDE configuration](#configuring-the-ide).
2. Review the [Mule Coding Style](STYLE.md) documentation to ensure you adhere to source code standards, thus increasing the likelihood that your changes will be merged with the `mule-4.x` (i.e. master) source code.
3. Import the Mule source code project into your IDE (if you are using one), then work on your changes, fixes or improvements. 
4. Debug and test your  local version, resolving any issues that arise. 
5. Save your changes locally.
6. Push your branch on your github repository. Refer to [Git's documentation](http://git-scm.com/book/en/v2/Git-Basics-Recording-Changes-to-the-Repository) for details on how to commit your changes.
7. Regularly update your branch with any changes or fixes applied to the mule-4.x branch. Refer to details below.

## Updating Your feature Branch

To ensure that your cloned version of MMA source code remains up-to-date with any changes to the ``master`` branch, regularly update your branch to rebase off the latest version.  

1. Pull the latest changes from the "upstream" master branch using the following commands:

```shell
git fetch upstream
git fetch upstream --tags 
```
2. Ensure you are working with the master branch using the following command:

```shell
git checkout master
```
3. Merge the latest changes and updates from the master branch to your feature branch using the following command:

```shell
git merge upstream/master
```
4. Push any changes to the master of your forked clone using the following commands:

```shell
git push origin master
git push origin --tags
```
5. Access your feature branch once again (to continue coding) using the following command:

```shell
git checkout dev/yourreponame/bug/yourJIRAissuenumber
```
6. Rebase your branch from the latest version of the master branch using the following command:

```shell
git rebase master
```
7. Resolve any conflicts on your feature branch that may appear as a result of the changes to master.
8. Push the newly-rebased branch back to your fork on your git repository using the following command:

```shell
git push origin dev/yourreponame/bug/yourJIRAissuenumber -f
```

##  Submitting a Pull Request (TBD)

Ready to submit your patch for review and merging? Initiate a pull request in github!

1. Review the [MuleSoft Contributor's Agreement](http://www.mulesoft.org/legal/contributor-agreement.html). Before any contribution is accepted, we need you to run the following notebook [script ](https://api-notebook.anypoint.mulesoft.com/notebooks/#380297ed0e474010ff43). This script will ask you to login to github and accept our Contributor's Agreement. That process creates an issue in our contributors project with your name.
2. From the repo of your branch, click the Pull Request button.
3. In the Pull Request Preview dialog, enter a title and optional description of your changes, review the commits that form part of your pull request, then click Send Pull Request (Refer to github's [detailed instructions](https://help.github.com/articles/using-pull-requests) for submitting a pull request).
4. MMA's core dev team reviews the pull request and may initiate discussion or ask questions about your changes in a Pull Request Discussion. The team can then merge your commits with the master where appropriate. We will validate acceptance of the agreement at this step. 
5. If you have made changes or corrections to your commit after having submitted the pull request, please add them into the existing pull request. Don't create a new one. 

## Contribute a new component migration to MMA
The component migration consists of two parts: the actual application migration, and a report indicating errors, suggestions or links to docs.

There are several types of component migrations:

1. Application structure: This migration will modify the Mule Application XML. You can add or remove or edit the XML elements and its children.
2. Pom structure: in this type of migration you can modify the whole pom structure, add or remove dependencies, repositories, profiles, etc.
3. Project structure: TBD


## Creating your migration task repository
To get started with your migration task you have to create a new class and make it inherit from [AbstractMigrationTask](./mule-migration-tool-api/src/main/java/com/mulesoft/tools/migration/task/AbstractMigrationTask.java). In order to be consistent with the rest of the migrators, this class must be placed in the library module under the folder `src/main/java/com/mulesoft/tools/migration/library/mule/tasks`.

These are the methods that you must implement:

`getDescription` a simple description of what this migrator is intended to do.

`getFrom` the Mule version which the migration will be supported from.

`getTo` the Mule version code that your migration will generate.

`getSteps` This is the most important method, this is the collection of steps that the migrator will execute.

Now it's time to create each step of your migration task. Create a new class under `src/main/java/com/mulesoft/tools/migration/library/mule/steps` and implement the corresponding interface depending the type of migration you want to make:

- [ApplicationModelContribution](./mule-migration-tool-api/src/main/java/com/mulesoft/tools/migration/step/category/ApplicationModelContribution.java) if you want to migrate the application structure. Note that there's also an abstract class named [AbstractApplicationModelMigrationStep](./mule-migration-tool-api/src/main/java/com/mulesoft/tools/migration/step/AbstractApplicationModelMigrationStep.java) with some XPath-related functionality that you can extend.

- [PomContribution](./mule-migration-tool-api/src/main/java/com/mulesoft/tools/migration/step/category/PomContribution.java) if you want to migrate the application POM structure.

- [ProjectStructureContribution](./mule-migration-tool-api/src/main/java/com/mulesoft/tools/migration/step/category/ProjectStructureContribution.java) if you want to migrate TBD

Then, if you are doing an application structure migration you have to declare the XPath selector of the mule processor that is going to be migrated. To do it you have call the `setAppliedTo` method –which is inherited– and pass the selector as parameter.
For example:

```java
private static final String XPATH_SELECTOR = XmlDslUtils.getCoreXPathSelector("set-payload");

public MyMigrationTaskStep() {
  this.setAppliedTo(XPATH_SELECTOR);
}
```

**Note:** You can write your own XPath selector or use the methods provided by the XmlDslUtils class.

Finally, you have to implement the `execute` method, which receives two arguments:
1. The element that is being processed depending the interface you are implementing: 
    - If it's [ApplicationModelContribution](./mule-migration-tool-api/src/main/java/com/mulesoft/tools/migration/step/category/ApplicationModelContribution.java) this argument is going to be the JDom Element. 
    - If it's [PomContribution](./mule-migration-tool-api/src/main/java/com/mulesoft/tools/migration/step/category/PomContribution.java) this argument is going to be the application POM model.
    - If it's [ProjectStructureContribution](./mule-migration-tool-api/src/main/java/com/mulesoft/tools/migration/step/category/ProjectStructureContribution.java) this argument is going to be the Project Path. 

2) The current migration report model.

```java
@Override
public void execute(Element element, MigrationReport report) throws RuntimeException {
  element.setName("new-element-name");
}
``` 

If you need to report something, for example a deprecated behavior, you must create a new entry in the [report.yaml](./mule-migration-tool-library/src/main/resources/report.yaml) file indicating the severity of the report (error, warning or info), a message, and then call the `report` method of the [MigrationReport](./mule-migration-tool-api/src/main/java/com/mulesoft/tools/migration/step/category/MigrationReport.java) model
```java
@Override
public void execute(Element element, MigrationReport report) throws RuntimeException {
  if (element.getAttribute("deprecatedMule3Value") != null) {
    report.report("myNewReportSection.reportEntry", element, element);
  }
}
```

## Include your migration into MMA
Your new migrator is ready to be used! Now you have to make the Mule Migration Assitant to be aware of this new feature.
Doing it is as simple as going to the engine module and including your new migrator in the `getCoreMigrationTask` method of the [MigrationTaskLocator](./mule-migration-tool-engine/src/main/java/com/mulesoft/tools/migration/engine/MigrationTaskLocator.java) class.

```java
public List<AbstractMigrationTask> getCoreMigrationTasks() {
    List<AbstractMigrationTask> coreMigrationTasks = new ArrayList<>();
    ...
    coreMigrationTasks.add(new MyNewMigrationTask());
    ...
    return coreMigrationTasks;
}
```

## Document new component support
You have already created a new migrator, created its migration tasks, and added it to the Mule Migration Assistant. Now it's time of the last but not least step: document the new feature!

In order to make other developers aware of this new migrator, you have to document it! Send us a PR to TBD describing the new feature.

# Summary

This guide started with pointing to different [sources of information](#getting-to-know-better-mule-migration-assistant). These were useful to understand were MMA is moving to and to have contact mechanisms with the rest of the community for help or discussion.  
  
In order to set up our [development environment](#setting-up-the-development-environment) we got to [install some prerequisites](#installing-prerequisites). Once we had them ready, we  downloaded the [source code](#getting-the-source-code).

At that point we were almost ready to develop improvements, we just needed to [configured our favourite IDE](#configuring-the-ide) to develop or debug Mule Migration Assistant code.

Then we were finally ready to [develop our contribution](#developing-your-contribution). We created our very own [feature branch](#creating-your-topic-branch) were we'll develop our improvement, then we learnt how to [keep it updated](#updating-your-topic-branch) in order to be able to submit a [pull request](#submitting-a-pull-request) to the main Mule Migration Assistant repository

We also covered the scenario where you need to [develop a migration to a component](#contribute-a-new-component-migration-to-mma) that is not currently supported by MMA. Then we learnt how to [publish it](publish-your-migration-to-maven-repository) and include it into the [engine](#include-your-migration-into-mma). 

Thank you one more time for taking some time understanding how to contribute to Mule Migration Assistant.

