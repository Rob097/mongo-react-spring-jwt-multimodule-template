# React & Spring Boot: Multi-models Project

Hi! This is a simple template to begin programming a React & Spring boot Web application.

### How to create the project structure
To create a project structure like this one, so a multi-module structure, with Eclipse IDE you have to:
  - File > New > Project > Spring Starter Project
  - Give to your project a Name, a Group and an artifact id
  - The Dependencies that you may need are: Spring Web, Spring Data Mongo DB, Spring Boot DevTools and Lombok
  - Finish
    
Now you have your project but you need to set it up, so:
  - Delete the src/ Source Folders 
  - Remove from Build Path the JRE System Library and the Maven Dependencies
  - The pom.xml have to be like the one in the folder "Defaul Files" of this project
  - Attention! Be sure that the packaging in the pom.xml is "pom"
    
Well Done! Now you just need to create your modules:
  - Right click on the project > New > Other > Maven Module.
  - Check On "Create a simple project" and give it a name > Finish
  - On each module's pom.xml add "<packaging>jar</packaging>"
  - If you now check the pom file of the parent project you may see that now you'd have something like this:

```sh
<modules>
	<module>runnable</module>
	<module>clients</module>
	<module>users</module>
</modules>
```

## Mongo Database
The database is developed using the NoSql technology by MongoDB.

## React frontend
The frontend is developed in React JS and it is fully commented to explain everything.

## Java Backend
The backend is developed in java using the spring boot framework.
We have developed a multimodule project because is simpler to understand, is more solid and it's also quicker when we have to build or deploy just one module not to build everything inside the project. Everything in the project it's carrefoully commented to understand all better. Now let's see the main modules in this template project.

### RUNNABLE MODULE
I reccomend you to create always a different module to manage the Main class and also to build and copying in the jar package the frontend. In fact this is what it is the runnable module. It contains the Main Class (Application.java) and in resources we have the the build folder of frontend to use when we are in production. It also contain a folder named "frontend" that is the actual REACT prject. The module need this folder to create a build folder and coping it inside the jar package when we build the project (It does that thanks to two plugin inside the pom.xml of this module).
This module also contains in the resources folder the application.properties file. This file contains the parameters to connect the project with the Database and some constant used somewhere inside the project.
You could put all these files directly inside the users module but, expecially if the application is going to be big or complex, is better to separate all these aspects in a different module.

### USERS MODULE
The Users module is extremely important because is the module that manage the users that can log in or sign up in the application. The authentication is managed by JWT standard. (https://jwt.io/).

### CLIENTS MODULE
The clients module is just a simple module to understand better how to link the different modules and of what is composed a module. Indeed it cointains just the basict so:
  - Modules (Entity)
  - Repository (Connection to Database)
  - Services (Methods)
  - Controllers (Operation linked to a particular web path and http method)

If a module need to be integrate in another you have to include it as a dependency. In order to do that you have to add in the pom.xml file of the module A, the one that need something to the module B, the following code:

```sh
<dependencies>
	<dependency>
		<groupId>com.authentication.jwt</groupId>
		<artifactId>clients</artifactId>
		<version>0.0.1-SNAPSHOT</version>
	</dependency>
</dependencies>
```
Into the runnable module, beause it has tha Main class that run the entire project, you have to add the above code for every module and also the dependency for the spring-boot-starter-web.





# Dillinger

[![N|Solid](data:image/png;base64,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)](https://nodesource.com/products/nsolid) [![N|Solid](data:image/png;base64,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)](https://nodesource.com/products/nsolid)

[![Build Status](https://travis-ci.org/joemccann/dillinger.svg?branch=master)](https://travis-ci.org/joemccann/dillinger)