#!/usr/bin/env bash
java -Dlogback.configurationFile=log/logback.xml -Djava.ext.dirs=lib/:jdbc/ it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLCMD $@