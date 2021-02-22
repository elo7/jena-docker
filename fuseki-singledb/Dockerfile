#   Licensed to the Apache Software Foundation (ASF) under one or more
#   contributor license agreements.  See the NOTICE file distributed with
#   this work for additional information regarding copyright ownership.
#   The ASF licenses this file to You under the Apache License, Version 2.0
#   (the "License"); you may not use this file except in compliance with
#   the License.  You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.

ARG FUSEKI_VERSION=3.17.0

FROM adoptopenjdk:11-jdk-openj9

LABEL maintainer="Elo7 Engineering Team <engenharia@elo7.com.br>"

ENV LANG C.UTF-8
RUN set -eux; \
    apt-get update; \
    apt-get install -y --no-install-recommends \
       bash curl ca-certificates findutils coreutils pwgen wget procps \
    ; \
    rm -rf /var/lib/apt/lists/*

# Update below according to https://jena.apache.org/download/ 
# and checksum for apache-jena-fuseki-3.x.x.tar.gz.sha512
ENV FUSEKI_SHA512 2b92f3304743da335f648c1be7b5d7c3a94725ed0a9b5123362c89a50986690114dcef0813e328126b14240f321f740b608cc353417e485c9235476f059bd380
# No need for https due to sha512 checksums below
ENV ASF_MIRROR http://www.apache.org/dyn/mirrors/mirrors.cgi?action=download&filename=
ENV ASF_ARCHIVE http://archive.apache.org/dist/

LABEL org.opencontainers.image.url https://github.com/stain/jena-docker/tree/master/jena-fuseki
LABEL org.opencontainers.image.source https://github.com/stain/jena-docker/
LABEL org.opencontainers.image.documentation https://jena.apache.org/documentation/fuseki2/
LABEL org.opencontainers.image.title "Apache Jena Fuseki"
LABEL org.opencontainers.image.description "Fuseki is a SPARQL 1.1 server with a web interface, backed by the Apache Jena TDB RDF triple store."
LABEL org.opencontainers.image.version ${FUSEKI_VERSION}
LABEL org.opencontainers.image.licenses "(Apache-2.0 AND (GPL-2.0 WITH Classpath-exception-2.0) AND GPL-3.0)"
LABEL org.opencontainers.image.authors "Apache Jena Fuseki by https://jena.apache.org/; this image by https://orcid.org/0000-0001-9842-9718"

# Config and data
RUN mkdir /fuseki/
ENV FUSEKI_BASE /fuseki

ARG FUSEKI_VERSION

# Installation folder
ENV FUSEKI_HOME /opt/fuseki

WORKDIR /tmp
# published sha512 checksum
RUN echo "$FUSEKI_SHA512  fuseki.tar.gz" > fuseki.tar.gz.sha512
# Download/check/unpack/move in one go (to reduce image size)
RUN     (curl --location --silent --show-error --fail --retry-connrefused --retry 3 --output fuseki.tar.gz ${ASF_MIRROR}jena/binaries/apache-jena-fuseki-$FUSEKI_VERSION.tar.gz || \
         curl --fail --silent --show-error --retry-connrefused --retry 3 --output fuseki.tar.gz $ASF_ARCHIVE/jena/binaries/apache-jena-fuseki-$FUSEKI_VERSION.tar.gz) && \
        sha512sum -c fuseki.tar.gz.sha512 && \
        tar zxf fuseki.tar.gz && \
        mv apache-jena-fuseki* $FUSEKI_HOME && \
        rm fuseki.tar.gz* && \
        cd $FUSEKI_HOME && rm -rf fuseki.war && chmod 755 fuseki-server

RUN mkdir $FUSEKI_BASE/extra/ && \
    wget -O $FUSEKI_BASE/extra/logstash-gelf-1.14.0.jar https://repo1.maven.org/maven2/biz/paluch/logging/logstash-gelf/1.14.0/logstash-gelf-1.14.0.jar && \
    wget -O $FUSEKI_BASE/extra/kafka-clients-2.1.1.jar https://repo1.maven.org/maven2/org/apache/kafka/kafka-clients/2.1.1/kafka-clients-2.1.1.jar && \
    wget -O $FUSEKI_BASE/extra/disruptor-3.4.2.jar https://repo1.maven.org/maven2/com/lmax/disruptor/3.4.2/disruptor-3.4.2.jar

# Test the install by testing it's ping resource. 20s sleep because Docker Hub.
RUN  $FUSEKI_HOME/fuseki-server & \
     sleep 20 && \
     curl -sS --fail 'http://localhost:3030/$/ping' 

# As "localhost" is often inaccessible within Docker container,
# we'll enable basic-auth with a random admin password
# (which we'll generate on start-up)
COPY entrypoint.sh /opt/fuseki/
RUN chmod 755 /opt/fuseki/entrypoint.sh

COPY log4j2.xml $FUSEKI_HOME/resources/log4j2.xml

# Where we start our server from
WORKDIR /opt/fuseki
EXPOSE 3030
ENTRYPOINT ["./entrypoint.sh"]

