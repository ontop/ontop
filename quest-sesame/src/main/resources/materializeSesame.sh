#!/bin/sh
<<<<<<< HEAD
java -Dlogback.configurationFile=log/logback.xml -Djava.ext.dirs=lib/:jdbc/ org.semanticweb.ontop.sesame.QuestSesameMaterializerCMD $@
=======
java -Dlogback.configurationFile=log/logback.xml -Djava.ext.dirs=lib/:jdbc/ sesameWrapper.QuestSesameMaterializerCMD $@
>>>>>>> develop
