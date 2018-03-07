# Mule Migration Tool
This tool's goal is to migrate projects. 

Initially is intended to help with the Mule 3.x - Mule 4.x migration effort.

## Approach
The tool works at xml level, making DOM operations/manipulation based on a set of configurable tasks and steps described in json files.

## Build and Package
Execute: `mvn clean package`

## Run
`java -jar migration-tool-1.0.0-SNAPSHOT.jar [options]`

Option | Description | Mandatory | Default
------------ | ------------ | ------------ | ------------
-files | List of mule xml's file paths separated by ';' | Yes if not using -filesDir | -
-filesDir | Root directory to search mule xml's files to migrate | Yes, if not using -files | -
-migrationConfigFile | Migration config file (json) containing all the rules and steps | Yes if not using -migrationConfigDir | -
-migrationConfigDir | Root directory to search the tasks and steps json files | Yes if not using -migrationConfigFile | -
-report | Reporting strategy, options: console | No | console
-backup | Flag to determine if you want a backup of your original files, options: true, false | No | true

## Example
`java -jar migration-tool-1.0.0-SNAPSHOT.jar -filesDir myApps/usecase01 -migrationConfigDir rules/mule/tasks -report console -backup true`

![Output Example](/img/output_example.png)

## List of Tasks
The tasks are located in `src/test/resources/mule/tasks` and `src/test/resources/munit/tasks`

NOTE: The order of the tasks matters!

Id | Component | Description
------------ | ------------ | ------------
1 | HTTP | Includes all the changes for HTTP Listener, HTTP Request and HTTP Static Handler
2 | Web Service Consumer | Includes all the changes for the WSC Consumer and WSS
3 | Dataweave | Includes all the changes for the Dataweave Transformer + the migration of dw 1.0 scripts to 2.0
4 | Database | Includes all the changes for the Database Module: Derby, MySQL, Oracle and Generic Configs
5 | Message Structure | Mule Message Structure: payload, attributes, variables

## Test Files
Individual component-related tests located in: `src/test/resources/mule/examples`

Use Cases / Apps tests located in: `src/test/resources/mule/apps`

## Limitations
Id | Key | Description | Doable
------------ | ------------ | ------------ | ------------
1 | Folder Structure | The new mule4 folder structure migration is not included | yes
2 | pom.xml | Dependencies, plugins, and all the pom.xml content migration is not included | yes
3 | external log | External log strategy not included | yes, add a new strategy
4 | Transports | Migration of transports not included: file, ftp, tcp, smtp, etc | no, use compatibility module
5 | Domains | Migration of xml files depending on a domain configuration or in a separate file configuration is not supported | yes
6 | APIKit | APIKit migration not included | yes, just add more rules
7 | Connectors | Connectors (Object Store, SFDC, Workday, etc) not included | yes, just add more rules
8 | Error handling | Error handling new structure not included | yes, just add more rules

## Final Note
Enjoy and provide feedback / contribute :)
