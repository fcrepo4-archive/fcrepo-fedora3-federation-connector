# Fedora 3 Federation Connector

This fedoration connector allows exposure of fedora 3 content in a running fedora 3 repository
to appear within a fedora 4 repository.  To use this code, you'd need to have access to a fedora 3 repository.

## Organization

This module has 2 sub-modules.

### local-legacy-fedora3

A sub module that builds  jar file of a build of fedora 3 for use in
cargo to support integration testing.

### fcrepo-fedora3-federation-connector

A federation connector with unit and integration tests rigging to expose
fedora 3 conent in a fedora 4 repository.


[Design Documentation](https://wiki.duraspace.org/display/FF/Design+-+Fedora+3+to+4+Upgrade)

## Fetching the source code

```bash
$ git clone https://github.com/futures/fcrepo-fedora3-federation-connector
```
### Update build configuration to include new module

The following steps reference a fcrepo4 source tree.  You can fetch this
from https://github.com/futures/fcrepo4.

In fcrepo4/fcrepo-webapp/pom.xml add

	<dependency>
	  <groupId>org.fcrepo</groupId>
	  <artifactId>fcrepo-fedora3-federation-connector</artifactId>
	  <version>${project.version}</version>
	    <exclusions>
	      <exclusion>
	        <groupId>com.hp.hpl.jena</groupId>
	        <artifactId>jena</artifactId>
	      </exclusion>
	    </exclusions>
	  </dependency>
	</dependency>

In the json file referenced in fcrepo4/fcrepo-webapp/src/main/resources/spring/repo.xml, 
(which at the time of this writing is fcrepo4/fcrepo-jcr/src/main/resources/config/rest-sessions/repository.json add

	"externalSources" : {
	  "fedora3" : {
	     "classname" : "org.fcrepo.connector.fedora3.Fedora3FederationConnector",
	     "projections" : [ "default:/f3 => /" ],
	     "fedoraUrl" : "http://localhost-or-wherever-your-fedora3-is/fedora",
	     "username" : "your-fedora-username",
	     "password" : "your-fedora-password",
             "pageSize" : 10
	  }
	}

Note: pageSize is optional and represents the size of pages of objects that are the child nodes of the
repository node.

### Compile and install the code
For this project, then each of the components modified above:

```bash
$ mvn clean install
```

Within fcrepo-webapp:

```bash
$ mvn jetty:run
```

You can see the federation over your fedora 3 content at [http://localhost:8080/rest/f3](http://localhost:8080/rest/f3)

## Caveats

* right now, the number of objects in the repository that are exposed is reduced to 21 to simplify testing
* versions are not presented

