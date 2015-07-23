[![Build Status](https://travis-ci.org/ontop/ontop.png?branch=develop)](https://travis-ci.org/ontop/ontop)
[![Bitdeli Badge](https://d2weczhvl823v0.cloudfront.net/ontop/ontop/trend.png)](https://bitdeli.com/free "Bitdeli Badge")
Ontop-Spatial
==================

Ontop-spatial is an extension of Ontop framework with geospatial 
support. Ontop is a framework for ontology based data access (OBDA). 
It supports SPARQL over
virtual RDF graphs defined through mappings to RDBMS. Ontop-spatial
extends ontop with the following geospatial capabilities: 
* Geospatial virtual RDF graphs can be created on top of geospatial databases
(i.e., PostgreSQL with PostGIS extension enabled). 
* Geometry columns in geospatial databases can be mapped to  GeoSPARQL
WKT literals using R2RML or OBDA mappings 
* Geospatial topology functions as defined in GeoSPARQL can be used 
in the filter clause of (Geo)SPARQL queries.  

Check also issues.txt for known issues and todo.txt for future 
enchancements

Licensing terms 
--------------------
The -ontop- framework is available under the Apache License, Version 2.0

All documentation is licensed under the 
[Creative Commons](http://creativecommons.org/licenses/by/4.0/)
(attribute)  license.


Compiling, packing, testing, etc.
--------------------
The project is a [Maven](http://maven.apache.org/) project. Compiling, running the unit tests, building the release binaries all can be done using maven. To make it more practical we created several .sh scripts that you can run on any unix environment that has maven installed. The scripts are located in the folder 'scripts', look at that folder for more information.

Currently we use Maven 3 and Java 7 to build the project.

Code organization
--------------------
The code is organized in several submodules as follows:

// TODO - extend this section of the readme


Links
--------------------

official website and documentations: http://ontop.inf.unibz.it/

Google Group: https://groups.google.com/forum/#!forum/ontop4obda

Blog: http://ontop-obda.blogspot.it/

Source Code: https://github.com/ontop/ontop

Issue Tracker: https://github.com/ontop/ontop/issues

Wiki: https://github.com/ontop/ontop/wiki




