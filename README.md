VCAP_SERVICES parser
====================
Usecase: connect to databases provisioned by cloundfoundry in a java application deployed on cloudfoundry
Auto-detect a cloudfoundry environment and fallback to a standard database setup transparently.
API design: minimize changes to existing configs.

Usage
=====
(ZkTodo2)[https://github.com/simbo1905/ZkToDo2] database connection URL in a
spring context file:
```
<!-- 
DATABASE_URL should be set with either "-DDATABASE_URL=postgres://user:password@hostname/dbname"
else with an environment variable. 
-->
<bean class="java.net.URI" id="dbUrl">
	<constructor-arg value="${DATABASE_URL}" />
</bean>
```
In order to use a postgresql database provided by cloudfoundry:
```
<bean id="vcapservices" class="org.intalio.cloudfoundry.vcapservices.impl.VCapServices"/>
<bean id="dbUrl" factory-bean="vcapservices" factory-method="getConnectionAsURI">
	<constructor-arg value="DATABASE_URL" />
	<constructor-arg value="postgresql" />
	<constructor-arg value="/^postgres.*/" />
</bean>
```
If the app is not executed in cloudfoundry, it will continue to behave like before.

Setup / Dependency
==================
With maven
```
<repositories>
	<repository>
	    <id>intalio.org</id>
	    <url>http://www.intalio.org/public/maven2</url>
	</repository>
</repositories>
<dependencies>
	<dependency>
		<groupId>org.intalio.cloudfoundry</groupId>
		<artifactId>vcapservices</artifactId>
		<version>1.0.0.001</version>
		<scope>compile</scope>
	</dependency>
</dependencies>
```

Build / Download
================
The library depends on json.org's parser.
Build and tests with maven.
Download here: http://www.intalio.org/public/maven2/org/intalio/cloudfoundry/vcapservices

API - VCAP_SERVICES Intro
=========================
```
<bean id="dbUrl" factory-bean="vcapservices" factory-method="getConnectionAsURI">
    
    <!-- default URL of name of the sys property/env variable that contains
    the URL used when not in cloudfoundry -->
	<constructor-arg value="DATABASE_URL" />
	
	<!-- The scheme of the generated URI -->
	<constructor-arg value="postgresql" />
	
	<!-- The regular expression for the type of the data-service -->
	<constructor-arg value="/^postgres.*/" />
	
	<!-- Optional regular expression for the name of the data-service -->
	<constructor-arg value="/^storedb$/" />
</bean>
```

Example: multiple postgresql databases exposed in VCAP_SERVICES
```
<bean id="dbOneUrl" factory-bean="vcapservices" factory-method="getConnectionAsURI">
	<constructor-arg value="DATABASE_ONE_URL" />
	<constructor-arg value="postgresql" />
	<constructor-arg value="/^postgres.*/" />
    <!-- The first data-service that service type match the regular expression '^postgres.*'
    and that name match the regular expression '^one.*' -->
	<constructor-arg value="/^one.*/" />
</bean>
<bean id="dbNotOneUrl" factory-bean="vcapservices" factory-method="getConnectionAsURI">
	<constructor-arg value="DATABASE_NOT_ONE_URL" />
	<constructor-arg value="postgresql" />
	<constructor-arg value="/^postgres.*/" />
	<!-- Negated: The first data-service that name does not match the regular expression -->
	<constructor-arg value="!/^one.*/" />
</bean>
```


This code is provided as is.
License: MIT.

Alternatives: vcap's runtime java library.
But: it has a lot of dependencies and it does not support transparently 
switching from a cloudfoundry deployment to a non-cloudfoundry deployment
