# Mule Migration Assistant
[![Build Status](https://munit.ci.cloudbees.com/job/MMT-1.x/badge/icon)](https://munit.ci.cloudbees.com/job/MMT-1.x)

This tool goal is to assist in the migration of projects in general.

Currently, it is being developed towards supporting the Mule 3.x &#8594; Mule 4.x migration.

## Overview

In order to understand the tool, let's start with a couple of informal definitions:

- Task &#8594; a set of steps;
- Step &#8594; an operation that change/remove/update a resource or content that can be present in a project.

In simple terms, the tool executes a sequence of tasks over a project and outputs a migrated project and a report about the migration.

## Getting Started

These are the instructions to have the project up and running.

### Minimum requirements

- Java 8
- Maven 3.3.9

### Check out

```
git clone --recursive git@github.com:mulesoft/mule-migration-tool.git 
```

### Build

Go to the project root and type

```
$ mvn clean package
```

### Run

Go to the runner module target directory:

```
$ cd runner/target
```

Run the tool:

```
$ java -jar mule-migration-assistant-runner-*CURRENT VERSION*.jar [options]
```

// TODO list options

### Contributing

There are two ways of contributing

- [Contributing directly to the migration tool engine](#contributing-to-the-engine);
- [Creating new migration tasks](#contributing-with-tasks) that are going to be run by the engine.

#### Contributing to the engine

We welcome pull requests related to the engine. :smile:

However, please be aware that migration task contributions are going to be added to this project just as dependencies :grimacing:

#### Contributing with tasks

The migration assistant locates the tasks using SPI. In order to contribute with your own task, follow the steps below:

- Create your new task contribution project:

```
mvn archetype:generate \
  -DarchetypeGroupId=com.mulesoft.tools \
  -DarchetypeArtifactId=migration-contribution-archetype \
  -DarchetypeVersion=<CURRENT VERSION> \
  -DartifactId=<YOUR MIGRATION ARTIFACT ID> \
  -DmainTaskClassName=<TASK CLASS NAME>
```

* The generate project should be composed of:
    - A pom file;
    - Some steps to start working over;
    - A task class that declares the steps above.

 :exclamation: The generated POM file declares a dependency to the mule-migration-tool-api. This is the only dependency from the migration tool that should be required to create your contribution.


* Create/modify the steps that are going to compose the task. A step must be:
    - An _AbstractApplicationModelMigrationStep_: works at the configuration file level;
    - A _PomContribution_: works over the project pom;
    - A _ProjectStructureContribution_: works over the project resources.

 * When your contribution is ready to be added to the main engine, please deploy the generated jar to https://repository.mulesoft.org/nexus/content/repositories/releases/
 * Go to the mule-migration-tool-contribution module and add your task class canonical name to META-INF/services/com.mulesoft.tools.migration.task.AbstractMigrationTask and your project dependency to the POM file.
 * Create a pull request.

 That's it. Thanks!