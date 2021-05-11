[![Maven Central](https://img.shields.io/maven-central/v/it.unibz.inf.ontop/ontop.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22it.unibz.inf.ontop%22)
[![GitHub license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0)
[![SourceForge](https://img.shields.io/sourceforge/dm/ontop4obda.svg)](http://sourceforge.net/projects/ontop4obda/files/)
[![Twitter](https://img.shields.io/twitter/follow/ontop4obda.svg?style=social)](https://twitter.com/ontop4obda)

| Branch    | build status  |
|-----------|---------------|
| [master](https://github.com/ontop/ontop/tree/master)  |[![Build Status](https://travis-ci.org/ontop/ontop.svg?branch=master)](https://travis-ci.org/ontop/ontop)|
| [version4](https://github.com/ontop/ontop/tree/version4) |[![Build Status](https://travis-ci.org/ontop/ontop.svg?branch=version4)](https://travis-ci.org/ontop/ontop)|


Ontop
=====

Ontop is a Virtual Knowledge Graph system.
It exposes the content of arbitrary relational databases as knowledge graphs. These graphs are virtual, which means that data remains in the data sources instead of being moved to another database.

Ontop translates [SPARQL queries](https://www.w3.org/TR/sparql11-query/) expressed over the knowledge graphs into SQL queries executed by the relational data sources. It relies on [R2RML mappings](https://www.w3.org/TR/r2rml/) and can take advantage of lightweight ontologies.

OntopSpark extension
--------------------

The OntopSpark extension developed by [__Chimera__](https://chimera-suite.github.io/chimera/), enables Ontop to perform OBDA on relational data using Apache Spark as distributed query processing engine. This opens new possible scenarios, where OBDA can be applied to Data Lakes.

The extension work consists into the implementation of 7 Java classes that manages the interaction with the databases. The classes have been inserted into the _ontop-rdb_ package, and are the following ones:
- `SparkSQLDBMetadataProvider.java`: is the class in charge of reading the database metadata by interacting with the Apache Spark ThriftServer using JDBC calls. In this case, has been necessary to manually retrieve the default schema and the matedata because the default calls are not supported by the JDBC (OntopSpark is developed to interact with [HiveJDBC](https://repo1.maven.org/maven2/org/apache/hive/hive-jdbc/)). We have also implemented `SparkSQLQuotedIDFactory.java`.
- `SparkSQLDBFunctionSymbolFactory.java`: is the class that manages the translation  of SPARQL functions into SQL functions and it's the implementation of AbstractSQLDBFunctionSymbolFactory. In this implementation, the main issue is related to the timestamp translation from SparkSQL to SPARQL and vice-versa. We have decided to translate the SparkSQL timestamp datatypes in the standard format “yyyy-MM-ddTHH:mm:ss.SSSxxx” and the denormalization in `SparkSQLTimestampDenormFunctionSymbol.java`. Another issue was related to `\` characters in the VKG formulations, which are interpreted by SparkSQL as escape characters and have been fixed in `SparkSQLEncodeURLorIRIFunctionSymbolImpl.java`.
- `SparkSQLDBTypeFactory.java`: defines the datatypes that can be used by Ontop when performing queries to build the internal VKG representation. We have to adapt the class to the SparkSQL datatypes
- `SparkSQLSelectFromWhereSerializer.java` implements some minor features to manage the SPARQL 'OFFSET' clause that is not supported by SparkSQL, while the `AlwaysProjectOrderByTermsNormalizer.java` has not required any adjustment.

Compiling, packing, testing, etc.
--------------------

The project is a [Maven](http://maven.apache.org/) project. Compiling,
running the unit tests, building the release binaries all can be done
using maven.  Currently, we use Maven 3 and Java 8 to build the
project.


Links
--------------------

- [Official Website and Documentation](https://ontop-vkg.org)
- [SourceForge Download](http://sourceforge.net/projects/ontop4obda/files/)
- [Docker Hub](https://hub.docker.com/r/ontop/ontop-endpoint)
- [GitHub](https://github.com/ontop/ontop/)
- [GitHub Issues](https://github.com/ontop/ontop/issues)
- [Google Group](https://groups.google.com/forum/#!forum/ontop4obda)
- [Facebook](https://www.facebook.com/obdaontop/)
- [Twitter](https://twitter.com/ontop4obda)
- [Travis CI](https://travis-ci.org/ontop/ontop)

License
-------

The Ontop framework is available under the Apache License, Version 2.0

```
  Copyright (C) 2009 - 2021 Free University of Bozen-Bolzano

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
```

All documentation is licensed under the
[Creative Commons](http://creativecommons.org/licenses/by/4.0/)
(attribute)  license.
