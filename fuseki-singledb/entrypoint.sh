#!/bin/bash

set -e

echo "Indexing triples..."
java -cp ./fuseki-server.jar jena.textindexer --desc=$DB_CONFIG

$FUSEKI_HOME/fuseki-server --conf=$DB_CONFIG