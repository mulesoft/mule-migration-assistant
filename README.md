# Mule Migration Assistant

Mule Migration Assistant is a command-line tool that helps migrate Mule 3.x applications to Mule 4.x. This tool works on a "best-effort" basis to translate every component in the base application. This means that if it does not complete 100% of the migration, manual adjustments are required. A migration report generated when the assistant is ran can help with those adjustments.

## License

Mule Migration Assistant is distributed under the terms of the [3-Clause BSD License](https://github.com/mulesoft/mule-migration-assistant/blob/master/LICENSE.txt)

Important: 
Mule Migration Assistant (MMA) is subject to the terms and conditions described for [Community](https://www.mulesoft.com/legal/versioning-back-support-policy#community) connectors.

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

### Build and Run

See the following commands in order to clone, build and run this project

```
$ git clone --recursive git@github.com:mulesoft/mule-migration-tool.git
$ cd mule-migration-tool/
$ mvn clean package
$ cd runner/target
$ java -jar mule-migration-assistant-runner-*CURRENT VERSION*.jar [parameters]
```
#### Mule Migration Assistant accepted parameters

| Parameter                  |      Description                                 |  Required |
|----------------------------|:------------------------------------------------:|----------:|
| projectBasePath            | Path of the application to be migrated           | Yes       |
| destinationProjectBasePath | Path where to generate the migrated application  | Yes       |
| muleVersion                | Mule 4 version to define on the application      | Yes       |
| parentDomainBasePath       | Path of the domain to be migrated                | No        |  
| help                       | Show all the parameters to define on MMA         | No        |


### Contributing

This project is Open Source and therefor welcome contributions, in case you wish to contribute please check our [contribution guide](https://github.com/mulesoft/mule-migration-assistant/blob/master/CONTRIBUTING.md)
