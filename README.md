# Fedora 3 Federation Connector

This fedoration connector allows exposure of fedora 3 content in a running fedora 3 repository
to appear within a fedora 4 repository.  To use this code, you'd need to have access to a fedora 3 repository.

[Design Documentation](https://wiki.duraspace.org/display/FF/Design+-+Fedora+3+to+4+Upgrade)

## Fetching the source code

```bash
$ git clone https://github.com/futures/fcrepo4.git
$ cd fcrepo4
$ git clone https://github.com/mikedurbin/fcrepo-fedora3-federation-connector
```
### Update build configuration to include new module
In fcrepo4/pom.xml add

	<module>fcrepo-fedora3-federation-connector</module>

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
```bash
$ mvn clean install
$ cd fcrepo-webapp
$ mvn jetty:run
```

You can see the federation over your fedora 3 content at [http://localhost:8080/rest/f3](http://localhost:8080/rest/f3)

## Caveats

* right now, the number of objects in the repository that are exposed is reduced to 21 to simplify testing
* datastream content doesn't yet behave properly
* versions are not presented
* a great deal of fedora 3 attributes and metadata aren't yet made available
* no integration tests

