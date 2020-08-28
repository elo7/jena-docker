#!/bin/bash

set -e

echo "Preloading database..."
java -cp ./fuseki-server.jar tdb.tdbloader --tdb=$DB_CONFIG $DB_FILE

echo "Indexing triples..."
java -cp ./fuseki-server.jar jena.textindexer --desc=$DB_CONFIG

$FUSEKI_HOME/fuseki-server --conf=$DB_CONFIG