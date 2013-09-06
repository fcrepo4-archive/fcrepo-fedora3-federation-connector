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
$ git clone https://github.com/mikedurbin/fcrepo-fedora3-federation-connector
```
### Update build configuration to include new module

In fcrepo4/fcrepo4-webapp/pom.xml add

	<dependency>
	  <groupId>org.fcrepo</groupId>
	  <artifactId>fcrepo-fedora3-federation-connector</artifactId>
	  <version>${project.version}</version>
	</dependency>

In fcrepo4/fcrepo-kernel/src/main/resources/fedora-node-types.cnd add

	/*
	 * A federated fedora 3 repository
	 */
	[fedora:repository]

In fcrepo4/fcrepo-jcr/src/main/resources/config/single/repository.json (or whichever you're using) add

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
For each of the components modified above, as well as this project:

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
* datastream content doesn't yet behave properly
* versions are not presented
* a great deal of fedora 3 attributes and metadata aren't yet made available
* no integration tests

