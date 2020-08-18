#!/bin/bash

set -e

$FUSEKI_HOME/fuseki-server --file=$DB_FILE "/$DB_NAME"