#!/bin/sh
java -Dlogback.configurationFile=log/logback.xml -Djava.ext.dirs=lib/:jdbc/ \
org.semanticweb.ontop.cli.QuestSesameMaterializerCMD $@