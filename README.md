VCAP_SERVICES java parser
=========================
h3. Usecase:
* connect to databases provisioned by cloundfoundry in a java application deployed on cloudfoundry
* Auto-detect a cloudfoundry environment and fallback to a standard database setup transparently.

h3. API 'design':
* Minimize changes to existing configs.
* Ignore databases APIs; parse JSON and produces URIs.
* Minimize dependencies

h2. Usage
[ZkTodo2](https://github.com/simbo1905/ZkToDo2) database connection URL in a
spring context file:
```xml
<!-- 
DATABASE_URL should be set with either "-DDATABASE_URL=postgres://user:password@hostname/dbname"
else with an environment variable. 
-->
<bean class="java.net.URI" id="dbUrl">
	<constructor-arg value="${DATABASE_URL}" />
</bean>
```
In order to use a postgresql database provided by cloudfoundry:
```xml
<bean id="vcapservices" class="org.intalio.cloudfoundry.vcapservices.impl.VCapServices"/>
<bean id="dbUrl" factory-bean="vcapservices" factory-method="getConnectionAsURI">
	<constructor-arg value="DATABASE_URL" />
	<constructor-arg value="postgresql" />
	<constructor-arg value="/^postgres.*/" />
</bean>
```
If the app is not executed in cloudfoundry, it will continue to behave like before.

h2. Setup / Dependency
With maven
```xml
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

h2. Build / Download
The library depends on json.org's parser.
Build and tests with maven.
Download here: http://www.intalio.org/public/maven2/org/intalio/cloudfoundry/vcapservices

h2. API - Other Examples
h3. Simple example
```xml
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

h3. Multiple postgresql databases exposed in VCAP_SERVICES
```xml
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

h2. License: MIT.
This code is provided as is.

h2. Alternative
[vcap's runtime java library](https://github.com/cloudfoundry/vcap-java/tree/master/cloudfoundry-runtime).
* Very elegant syntax in spring.
* Database specific APIs awareness.
But:
* More dependencies
* Specific config that will only work on cloudfoundry
