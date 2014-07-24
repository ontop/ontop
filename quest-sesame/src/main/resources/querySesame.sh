#!/bin/sh
<<<<<<< HEAD
java -Dlogback.configurationFile=log/logback.xml -Djava.ext.dirs=lib/:jdbc/ org.semanticweb.ontop.sesame.QuestSesameCMD $@
=======
java -Dlogback.configurationFile=log/logback.xml -Djava.ext.dirs=lib/:jdbc/ sesameWrapper.QuestSesameCMD $@
>>>>>>> develop
