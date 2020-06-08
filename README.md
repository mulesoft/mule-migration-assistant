# Mule Migration Assistant
[![Build Status](https://munit.ci.cloudbees.com/job/MMT-1.x/badge/icon)](https://munit.ci.cloudbees.com/job/MMT-1.x)

This tool goal is to assist in the migration of projects in general.

Currently, it is being developed towards supporting the Mule 3.x &#8594; Mule 4.x migration.

## Overview

In order to understand the tool, let's start with a couple of informal definitions:

- Task &#8594; a set of steps;
- Step &#8594; an operation that change/remove/update a resource or content that can be present in a project.

In simple terms, the tool executes a sequence of tasks over a project and outputs a migrated project and a report about the migration.

## Architecture

The Mule Migration Assistant workflow is designed as follows:

<!-- NOTE: this image is defined in './architecture.dot' and created by running './architecture.sh' -->
![Mule Migration Assistant Architecture](./architecture.svg
"A visual representation of the relation between different components that take part of a recording")

## Modules

* [Migration Archetype](./migration-contribution-archetype): Maven archetype generator for a new Migration Task contribution. 
* [Migration API](./mule-migration-tool-api): Set of API's to use when developing a new Migration task.
* [Contributions](./mule-migration-tool-contribution): Where all external contributions are contributed to the tools. The set of migration tasks defined on this module will be loaded to the Migration Assistant trough SPI.
* [Expression Migrator](./mule-migration-tool-expression): MEL to DW migrator. It contains all the rules to migrate Mule 3 expressions defined on MEL to the new DW transformation language on Mule 4.
* [Migration Library](./mule-migration-tool-library): Set of default migrations that are shipped with the Mule Migration Assistant. It contains all the migrations of Mule Core components, MUnit, etc.
* [Engine](./mule-migration-tool-engine): The execution engine that identifies the project type, locate all the migration tasks available and performs the migration.
* [Runner](./runner): Console implementation to execute the Mule Migration Assistant.
* [Integration Tests](./mule-migration-tool-tests): Suite of tests to validate all the component that will be supported by this tool. 

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
$ java -jar mule-migration-assistant-runner-*CURRENT VERSION*.jar [parameters]
```

| Parameter                  |      Description                                 |  Required |
|----------------------------|:------------------------------------------------:|----------:|
| projectBasePath            | Path of the application to be migrated           | Yes       |
| destinationProjectBasePath | Path where to generate the migrated application  | Yes       |
| muleVersion                | Mule 4 version to define on the application      | Yes       |
| parentDomainBasePath       | Path of the domain to be migrated                | No        |  
| help                       | Show all the parameters to define on MMA         | No        |


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