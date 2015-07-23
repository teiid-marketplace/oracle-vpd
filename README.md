# oracle-vpd

This is Proxy JDBC driver for Oracle that enables a way to inject a V$Session OSUSER from JBoss EAP environment.
It should be noted that, this MUST be used along "CallerIdentity" login module to set the OSUSER, as well as
to keep the connection pools based $Subject separate in the JEE engine.

### Steps to compile
- Make sure you have JDK, and maven installed on your machine.
- Then clone this project 

```
git clone https://github.com/teiid-marketplace/oracle-vpd.git
cd oracle-vpd
```
- Since Oracle JDBC driver is not available in any public maven repositories, you need to create one in your local repository. Your local Maven repo is typically in "~/.m2/repository" directory. 

- Copy the "maven-repo" directory into it

- Copy "ojdbc6.jar" into "~/.m2/repository/com/oracle/1.0" and rename it to "ojdbc6-1.0.jar"  
  
- Now setting up of the local maven repository with Oracle JDBC driver is complete.

- Build this project by issuing

```
mvn clean install
```

- Unzip  "./target/oracle-vpd-1.0.0-jboss-as7-dist.zip" file into "<jboss-eap/modules" directory such that they overlap and merge.

### Configuration changes to JBoss EAP Environemnt

Edit "standalone.xml" or "standalone-teiid.xml" file and add following

- Under "security" subsystem add a new security domain

```
<security-domain name="caller-identity">
    <authentication>
        <login-module code="org.picketbox.datasource.security.CallerIdentityLoginModule" flag="required">
            <module-option name="password-stacking" value="useFirstPass"/>
        </login-module>
    </authentication>
</security-domain>
```

- Under "datasources" subsystem, create a driver definition for oracle vpd
```
<drivers>
    <driver name="oracle-vpd" module="com.oracle">
        <driver-class>org.jboss.teiid.oracle.OracleVPDDriver</driver-class>
    </driver>
</drivers>
```

- Now using the above driver definition, create a data source

```
<datasource jndi-name="java:/oracleDS" pool-name="OracleDS" enabled="true" use-java-context="true">
    <driver>oracle-vpd</driver>
    <connection-url>{CONNECTION-URL}</connection-url>
    <connection-property name="global-user">
        {STATIC-USER}
    </connection-property>
    <connection-property name="global-password">
        {STATIC-USER-PASSWORD}
    </connection-property>    
    <security>
        <security-domain>caller-identity</security-domain>
    </security>
</datasource>
```

Two things to note from above. 
 
1. Usage of of "caller-identity" security module, this will enforce that, that current user logged into the JEE application be used as the VPD user. This will also force the application server to define user segmented connection pools (very important). 
2. The "global" properties are used as static user name and password to log into oracle.

Since this was usecase tobe using with Teiid (http://teiid.org), I created the following Dyanmic VDB and deployed into a Teiid server

```
<vdb name="oracle" version="1">
    <model visible="true" name="Parts">
        <source name="Parts" translator-name="oracle" connection-jndi-name="java:/oracleDS"/>    
        <metadata type = "DDL"><![CDATA[        
            CREATE FOREIGN TABLE DUAL (e1 integer PRIMARY KEY, e2 varchar(25), e3 double);
            CREATE FOREIGN FUNCTION sys_context(x string, y string) returns string;
        ]]>
       </metadata>         
    </model>
</vdb>
```

Then using the SQuirreL JDBC client, connected to the above VDB in the Teiid, issued

```
select sys_context( 'userenv', 'os_user' ) from dual
``` 

That returned the user I connected with to the Teiid, that is "teiidUser"