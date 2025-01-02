[![Maven Central](https://img.shields.io/maven-central/v/it.unibz.inf.ontop/ontop.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22it.unibz.inf.ontop%22)
[![GitHub license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0)
[![SourceForge](https://img.shields.io/sourceforge/dm/ontop4obda.svg)](http://sourceforge.net/projects/ontop4obda/files/)
[![Docker](https://img.shields.io/docker/image-size/ontop/ontop?label=ontop%2Fontop)](https://hub.docker.com/r/ontop/ontop)
[![Twitter](https://img.shields.io/twitter/follow/ontop4obda.svg?style=social)](https://twitter.com/ontop4obda)


| Branch    | build status  |
|-----------|---------------|
| [master](https://github.com/ontop/ontop/tree/master)  |[![Build Status](https://github.com/ontop/ontop/actions/workflows/.github/workflows/main.yml/badge.svg?branch=master)](https://github.com/ontop/ontop/actions/)|
| [version5](https://github.com/ontop/ontop/tree/version5) |[![Build Status](https://github.com/ontop/ontop/actions/workflows/.github/workflows/main.yml/badge.svg?branch=version5)](https://github.com/ontop/ontop/actions/)|


Ontop
=====

Ontop is a Virtual Knowledge Graph system.
It exposes the content of arbitrary relational databases as knowledge graphs. These graphs are virtual, which means that data remains in the data sources instead of being moved to another database.

Ontop translates [SPARQL queries](https://www.w3.org/TR/sparql11-query/) expressed over the knowledge graphs into SQL queries executed by the relational data sources. It relies on [R2RML mappings](https://www.w3.org/TR/r2rml/) and can take advantage of lightweight ontologies.

Compiling, packing, testing, etc.
--------------------

The project is a [Maven](http://maven.apache.org/) project. Compiling,
running the unit tests, building the release binaries all can be done
using Maven.  Currently, we use Maven 3.6+ and Java 11 to build the
project.


Links
--------------------

- [Official Website and Documentation](https://ontop-vkg.org)
- [SourceForge Download](http://sourceforge.net/projects/ontop4obda/files/)
- [Docker Hub](https://hub.docker.com/r/ontop/ontop)
- [GitHub](https://github.com/ontop/ontop/)
- [GitHub Issues](https://github.com/ontop/ontop/issues)
- [Google Group](https://groups.google.com/forum/#!forum/ontop4obda)
- [Facebook](https://www.facebook.com/obdaontop/)
- [Twitter](https://twitter.com/ontop4obda)
- [Mastodon](https://fosstodon.org/@ontop)

License
-------

The Ontop framework is available under the Apache License, Version 2.0

```
  Copyright (C) 2009 - 2024 Free University of Bozen-Bolzano

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

Organizations
-------------
Ontop is backed by the [Free University of Bozen-Bolzano](https://www.inf.unibz.it/krdb/) and [Ontopic s.r.l.](https://ontopic.ai). 
It also receives regular important contributions from [Birkbeck, University of London](http://www.bbk.ac.uk/) and the [University of Bergen](https://www.uib.no).