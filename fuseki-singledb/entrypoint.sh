#!/bin/bash

set -e

java -cp $FUSEKI_BASE/extra/custom-filters.jar:./fuseki-server.jar jena.textindexer --desc=$DB_CONFIG

$FUSEKI_HOME/fuseki-server --conf=$DB_CONFIG