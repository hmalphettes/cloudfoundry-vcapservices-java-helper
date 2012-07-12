VCAP_SERVICES parser
====================
Usecase: connect to databases provisioned by cloundfoundry in a java application deployed on cloudfoundry
Auto-detect a cloudfoundry environment and default to a standard database transparently.

Example: ZkTodo2.

Dependency: json.org: http://repo1.maven.org/maven2/org/json/json/20090211/json-20090211.jar

Example use in a spring context file:
```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
   xmlns:osgi="http://www.springframework.org/schema/osgi"
   xmlns:p="http://www.springframework.org/schema/p"
   xmlns:util="http://www.springframework.org/schema/util"                                     
   xmlns:mongo="http://www.springframework.org/schema/data/mongo"
   xsi:schemaLocation="http://www.springframework.org/schema/beans   
     http://www.springframework.org/schema/beans/spring-beans.xsd
     http://www.springframework.org/schema/osgi
     http://www.springframework.org/schema/osgi/spring-osgi.xsd
     http://www.springframework.org/schema/data/mongo
     http://www.springframework.org/schema/data/mongo/spring-mongo-1.0.xsd
     http://www.springframework.org/schema/util
     http://www.springframework.org/schema/util/spring-util-2.0.xsd">

     <bean id="vcapservices" class="org.intalio.cloudfoundry.vcapservices.impl.VCapServices">
        <constructor-arg index="0">
        <!-- The string below is the default connection parameters formatted as a VCAP_SERVICES.
        If the environment variable VCAP_SERVICES is defined, we use it.
        If it is not defined, we use the hardcoded string here.
        
        The hardcoded string supports inserting system properties or environment constants and
        default values with the synthax ${sysKey,defValue}
        
        For mongdb on cloudfoundry the keys username and password for the credentials are defined.
        but for development, they are null.
        -->
<value>{
  "postgres-9.1":[
    {
      "name":"acme-prodrdb",
      "label":"postgres-9.1-prodacme",
      "plan":"free",
      "tags":[
        "postgres",
        "postgres-9.1",
        "relational"
      ],
      "credentials":{
        "name":"${acme.prodrdb.dbname,acme}",
        "hostname":"${acme.prodrdb.hostname,localhost}",
        "port":"${acme.prodrdb.port,5432}",
        "user":"${acme.prodrdb.user,acme}",
        "password":"${acme.prodrdb.password,acme}"
      }
   },
   {
     "name":"acme-definition-persistence-prodrdb",
     "label":"postgres-9.1-definition-persistence-prodacme",
     "plan":"free",
     "tags":[
       "postgres",
       "postgres-9.1",
       "relational"
     ],

     "credentials":{
       "name":"${acme.metadata.dbname,metadata}",
       "hostname":"${acme.prodrdb.hostname,localhost}",
       "port":"${acme.prodrdb.port,5432}",
       "user":"${acme.prodrdb.user,acme}",
       "password":"${acme.prodrdb.password,acme}"
     }
   }
  ],
  "mongodb-1.8":[
    {
      "name":"mongodb-acme",
      "label":"mongodb-1.8",
      "plan":"free",
      "tags":[
        "mongodb",
        "mongodb-1.8",
        "nosql"
      ],
      "credentials":{
        "hostname":"localhost",
        "port":27017,
        "name":"acme",
        "db":"acme"
      }
    }
  ]
}</value></constructor-arg>
     </bean>


     <bean id="postgresCredentials" class="org.intalio.cloudfoundry.vcapservices.impl.VCapServiceCredentials"
               factory-method="getCredentialsOfService">
       <constructor-arg index="0" ref="vcapservices"/>
       <constructor-arg index="1" value="/^postgres.*/"/>
       <constructor-arg index="2" value="!/.*${TOKEN_OF_DEF_PERSISTENCE_DATA_SERVICE,definition}.*/"/>
     </bean>
     
     <bean id="postgresDefinitionPersitenceCredentials" class="org.intalio.cloudfoundry.vcapservices.impl.VCapServiceCredentials"
                factory-method="getCredentialsOfService">
        <constructor-arg index="0" ref="vcapservices"/>                
        <constructor-arg index="1" value="/^postgres.*/"/>
        <constructor-arg index="2" value="/.*${TOKEN_OF_DEF_PERSISTENCE_DATA_SERVICE,definition}.*/"/>
     </bean>
        
     <bean id="datasource" class="com.mchange.v2.c3p0.ComboPooledDataSource" destroy-method="close">
         <!-- Connection properties -->
         <property name="driverClass" value="org.postgresql.Driver"/>
         <property name="jdbcUrl" value="#{'jdbc:postgresql://' + @postgresCredentials.hostname + '/' + @postgresCredentials.name}"/>
         <property name="properties">
             <props>
                 <prop key="c3p0.acquire_increment">5</prop>
                 <prop key="c3p0.idle_test_period">100</prop>
                 <prop key="c3p0.max_size">90</prop>
                 <prop key="c3p0.max_statements">0</prop>
                 <prop key="c3p0.min_size">10</prop>
                 <prop key="user">#{@postgresCredentials.user}</prop>
                 <prop key="password">#{@postgresCredentials.password}</prop>
             </props>
         </property>
     </bean>

        <bean id="definition.persistence.datasource" class="com.mchange.v2.c3p0.ComboPooledDataSource" destroy-method="close">
            <!-- Connection properties -->
            <property name="driverClass" value="org.postgresql.Driver"/>
            <property name="jdbcUrl" value="#{'jdbc:postgresql://' + @postgresDefinitionPersitenceCredentials.hostname + '/' + @postgresDefinitionPersitenceCredentials.name}"/>
            <property name="properties">
                <props>
                    <prop key="c3p0.acquire_increment">2</prop>
                    <prop key="c3p0.idle_test_period">100</prop>
                    <prop key="c3p0.max_size">10</prop>
                    <prop key="c3p0.max_statements">0</prop>
                    <prop key="c3p0.min_size">4</prop>
                    <prop key="user">#{@postgresDefinitionPersitenceCredentials.user}</prop>
                    <prop key="password">#{@postgresDefinitionPersitenceCredentials.password}</prop>
                </props>
            </property>
        </bean>

     <bean id="mongodbCredentials" class="org.intalio.cloudfoundry.vcapservices.impl.VCapServiceCredentials"
               factory-method="getCredentialsOfFirstService">
       <constructor-arg index="0" ref="vcapservices"/>
       <constructor-arg index="1" value="/^mongo.*/"/>
     </bean>
     

<mongo:mongo host="#{@mongodbCredentials.hostname}" port="#{@mongodbCredentials.port}"/>

<bean id="mongoTemplate" class="org.springframework.data.document.mongodb.MongoTemplate">
    <constructor-arg>
       <mongo:db-factory
            host="#{@mongodbCredentials.hostname}"
            port="#{@mongodbCredentials.port}"
            dbname="#{@mongodbCredentials.db}"
            username="#{@mongodbCredentials.user}"
            password="#{@mongodbCredentials.password}"/>
            <!-- @mongodbCredentials.user should be written @mongodbCredentials.username
                 but it has the advantage of working with the older version of
                 the vcap_services parser and the new one too. -->
    </constructor-arg>
</bean>

</beans>


```

This code is provided as is, tests cases are lacking, it works in OSGi.
License: public domain.

Alternatives: vcap's runtime java library.


