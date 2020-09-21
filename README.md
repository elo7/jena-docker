# Docker files for Jena 3.16

This repository hosts [Docker](https://www.docker.com/) recipes for distributing 
[Apache Jena](http://jena.apache.org/).

Two Docker images are available:

 - [fuseki-singledb](fuseki-singledb/)  - Based on [fuseki](fuseki/), it's better suited for Kubernetes deployments, since its configuration is supposed to be based on ttl config files instead of environment variables, and the support for starting with multiple databases has been droped. It also has a few additions, including support for GELF Kafka Logs and a custom implementation for the `ConcatenateGraphFilter` Lucene filter.
 - [jena](jena/) - `riot` command line and friends, for use on the command line
 - [fuseki](fuseki/) - the [Fuseki 2](http://jena.apache.org/documentation/fuseki2/) server with SPARQL endpoint and web interface
 
## Building

```shell
docker build -t jena jena
docker build -t jena-fuseki jena-fuseki
```
 
## Dockerfile overview

The `Dockerfile`s for both images use the official [openjdk:11-jre-slim-buster](https://hub.docker.com/r/_/openjdk/) base image, which is [based on](https://github.com/docker-library/openjdk/blob/master/11/jre/slim/Dockerfile) the `[debian](https://hub.docker.com/_/debian/):buster-slim` image; this clocks in at about [69 MB](https://microbadger.com/images/openjdk:11-jre-slim-buster)

The `ENV` variables like `JENA_VERSION` and `FUSEKI_VERSION` determines which version of Jena and Fuseki are downloaded. Updating the version also requires updating the `JENA_SHA512` and `FUSEKI_SHA512` variables, which values should match the official Jena download `.tar.gz.sha512` hashes, as approved in their release `[VOTE]` emails.

The `ASF_MIRROR` use <http://www.apache.org/dyn/mirrors/mirrors.cgi> that redirect to a local mirror, with a fallback to the `ASF_ARCHIVE` <http://archive.apache.org/dist/> for older versions. Note that due to subsequent sha512 checking these accessed with `http` rather than `https`.

To minimize layer size, there's a single `RUN` with `curl`, `sha512sum`, `tar zxf` and `mv` - thus the temporary files during download and extraction are not part of the final image.

Some files from the Apache Jena distributions are stripped, e.g. javadocs and the `fuseki.war` file.

The Fuseki image includes some [helper scripts](jena-fuseki/load.sh) to do [tdb loading](https://jena.apache.org/documentation/tdb/commands.html) using `fuseki-server.jar`.
In addition Fuseki has a `[docker-entrypoint.sh](https://github.com/stain/jena-docker/blob/master/jena-fuseki/docker-entrypoint.sh)` that populates `shiro.ini` with the password provided as `-e ADMIN_PASSWORD` to Docker, or with a new randomly generated password that is printed the first time.

