[![GitHub license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0)
[![Twitter](https://img.shields.io/twitter/follow/ontop4obda.svg?style=social)](https://twitter.com/ontop4obda)
[![Build Status](https://github.com/ontop/ontop/actions/workflows/.github/workflows/main.yml/badge.svg?branch=feature/obdf)](https://github.com/ontop/ontop/actions/)|


# Ontop version for OBDF experiments

This branch contains an experimental version of Ontop 5.1.2 including all the modifications related to the OBDF framework. **This version is not meant for production use: for that, please use an official release from [Ontop website](https://ontop-vkg.org)**

Ontop is a Virtual Knowledge Graph system.
It exposes the content of arbitrary relational databases as knowledge graphs. These graphs are virtual, which means that data remains in the data sources instead of being moved to another database.

Ontop translates [SPARQL queries](https://www.w3.org/TR/sparql11-query/) expressed over the knowledge graphs into SQL queries executed by the relational data sources. It relies on [R2RML mappings](https://www.w3.org/TR/r2rml/) and can take advantage of lightweight ontologies.

More information at https://ontop-vkg.org


## Compiling, packing, testing, etc.

The project is a [Maven](http://maven.apache.org/) project. Compiling, running the unit tests, building the release binaries all can be done using Maven. Currently, we use Maven 3.6+ and Java 11 to build the project.

This experimental branch introduces two additional OBDF-related *Maven profiles* that enable running OBDF unit tests and simplify running Ontop within the IDE against one of the considered federation and baseline systems:
* `obdf-tests` - activable with `-Pobdf-tests`, it enables OBDF unit tests and includes publicly available JDBC drivers of federation systems as additional dependencies, with the exception of the driver for Denodo;
* `obdf-tests-denodo` - activable with `-Pobdf-tests -Pobdf-tests-denodo`, it builds on `obdf-tests` and also includes an additional dependency for Denodo driver available on a Maven repository internal to UNIBZ.

None of these profiles is needed to build Ontop with OBDF extensions. They are only used for testing the OBDF code.


## List of changes

We list here all the changes to Ontop 5.1.2 introduced in relation to implement and test the OBDF framework. The marker '+' denotes files/directories that were newly added, instead of modification to existing ones.

### OBDF implementation

Query answering:

* [`FederationOptimizer.java`](core/optimization/src/main/java/it/unibz/inf/ontop/iq/optimizer/FederationOptimizer.java)+,
  [`FederationOptimizerImpl.java`](core/optimization/src/main/java/it/unibz/inf/ontop/iq/optimizer/impl/FederationOptimizerImpl.java)+:
  interface and implementation class for OBDF implementations
* [`OntopOptimizationModule.java`](core/optimization/src/main/java/it/unibz/inf/ontop/injection/impl/OntopOptimizationModule.java),
  [`optimization-default.properties`](core/optimization/src/main/resources/it/unibz/inf/ontop/injection/optimization-default.properties):
  modified Guice code and configuration, adding binding for `FederationOptimizer` interface defaulting to `FederationOptimizerImpl`
* [`QuestQueryProcessor.java`](engine/reformulation/core/src/main/java/it/unibz/inf/ontop/answering/reformulation/impl/QuestQueryProcessor.java),
  [`ToFullNativeQueryReformulator.java`](engine/reformulation/core/src/main/java/it/unibz/inf/ontop/answering/reformulation/impl/ToFullNativeQueryReformulator.java):
  modified query reformulation main classes in order to call `FederationOptimizer`
* [`OptimizationTestingTools.java`](core/optimization/src/test/java/it/unibz/inf/ontop/OptimizationTestingTools.java):
  added `FEDERATION_OPTIMIZER` for use in tests (not used yet)

Hint precomputation:

* [`FederationHintPrecomputation.java`](binding/rdf4j/src/test/java/federationOptimization/precomputation/FederationHintPrecomputation.java)+:
  entry point class implementing the hint precomputation algorithm (running for now as a JUnit test)
* [`SourceLab.java`](binding/rdf4j/src/test/java/federationOptimization/precomputation/SourceLab.java)+:
  enumeration for possible labels (e.g., `inefficient`, `dynamic`) used to characterize sources
* [`SourceHints.java`](binding/rdf4j/src/test/java/federationOptimization/precomputation/SourceHints.java)+,
  [`EmptyFederatedJoin.java`](binding/rdf4j/src/test/java/federationOptimization/precomputation/EmptyFederatedJoin.java)+,
  [`MaterializedView.java`](binding/rdf4j/src/test/java/federationOptimization/precomputation/MaterializedView.java)+,
  [`Redundancy.java`](binding/rdf4j/src/test/java/federationOptimization/precomputation/Redundancy.java)+,
  [`RedundancyRelation.java`](binding/rdf4j/src/test/java/federationOptimization/precomputation/RedundancyRelation.java)+:
  support classes modeling obtained hints, with `SourceHints` main class holding a set of hints, and `RedundancyRelation` an enumeration.


### Ontop enhancements

Support for external addition of not-null constraints in Ontop DB metadata:

* [`NamedRelationDefinition.java`](core/model/src/main/java/it/unibz/inf/ontop/dbschema/NamedRelationDefinition.java):
  modified interface with addition of method `addNotNullConstraint`
* [`AbstractNamedRelationDefinition.java`](core/model/src/main/java/it/unibz/inf/ontop/dbschema/impl/AbstractNamedRelationDefinition.java),
  [`AbstractRelationDefinition.java`](core/model/src/main/java/it/unibz/inf/ontop/dbschema/impl/AbstractRelationDefinition.java):
  modified classes with implementation of method `addNotNullConstraint`

Extended syntax for constraints files, supporting specification of `NOT NULL` and `UNIQUE`/`PRIMARY KEY` constraints:

* [`ImplicitDBConstraintsProviderFactoryImpl.java`](mapping/core/src/main/java/it/unibz/inf/ontop/spec/dbschema/impl/ImplicitDBConstraintsProviderFactoryImpl.java),
  [`ImplicitDBConstraintsProvider.java`](mapping/core/src/main/java/it/unibz/inf/ontop/spec/dbschema/impl/ImplicitDBConstraintsProvider.java):
  modified classes to parse extended syntax (`ImplicitDBConstraintsProviderFactoryImpl`) and inject parsed constraints in Ontop data structures for DB metadata
* [`ImplicitDBConstraintsTest.java`](mapping/core/src/test/java/it/unibz/inf/ontop/spec/impl/ImplicitDBConstraintsTest.java),
  [`all_constraints_extended_syntax.lst`](mapping/core/src/test/resources/userconstraints/all_constraints_extended_syntax.lst)+:
  modified `ImplicitDBConstraintsTest` unit test and new supporting file `all_constraints_extended_syntax.lst`, to test handling of extended syntax

Bug fixes in existing Ontop code:

* [`DenodoExtraNormalizer.java`](db/rdb/src/main/java/it/unibz/inf/ontop/generation/normalization/impl/DenodoExtraNormalizer.java),
  [`SubQueryFromComplexLeftJoinExtraNormalizer.java`](db/rdb/src/main/java/it/unibz/inf/ontop/generation/normalization/impl/SubQueryFromComplexLeftJoinExtraNormalizer.java)+:
  modified `DenodoExtraNormalizer` to call a newly added `SubQueryFromComplexLeftJoinExtraNormalizer` transformer that inserts a `ConstructionNode` above operands of a *complex* `LeftJoin` whose operands are joins themselves, so to force the generation of SQL subquery/ies that Denodo handles correctly, rather than performing a cartesian join if such transformation is not performed
* [`DremioDBMetadataProvider.java`](db/rdb/src/main/java/it/unibz/inf/ontop/dbschema/impl/DremioDBMetadataProvider.java):
  modified existing class to ignore (system) tables for which metadata extraction would fail in Dremio
* [`TeiidExtraNormalizer.java`](db/rdb/src/main/java/it/unibz/inf/ontop/generation/normalization/impl/TeiidExtraNormalizer.java)+,
  [`sql-default.properties`](db/rdb/src/main/resources/it/unibz/inf/ontop/injection/sql-default.properties):
  added new `DialectExtraNormalizer` to implement a workaround for a bug in Teiid 16.x, whereby `LIMIT` in submitted SQL query is incorrectly ignored by Teiid


### Unit tests

Unit test code:

* [`FederationOptimizerTest.java`](binding/rdf4j/src/test/java/federationOptimization/FederationOptimizerTest.java)+,
  [`FederationOptimizerMultipleTest.java`](binding/rdf4j/src/test/java/federationOptimization/FederationOptimizerMultipleTest.java)+:
  main test code to evaluate SPARQL-to-SQL query reformulation with/without OBDF optimizations and in different settings, either for an individual or multiple tests

* [`Analyzer.java`](binding/rdf4j/src/test/java/federationOptimization/Analyzer.java)+,
  [`Tester.java`](binding/rdf4j/src/test/java/federationOptimization/Tester.java)+,
  [`federation/bsbm-queries/`](binding/rdf4j/src/test/resources/federation/bsbm-queries/)+:
  class `Analyzer` for structural analysis of queries under `federation/bsbm-queries/`, running Ontop with OBDF optimizer via `Tester` general-purpose utility class

* [`TeiidQueryTest.java`](binding/rdf4j/src/test/java/federationOptimization/TeiidQueryTest.java)+:
  test code used to investigate Teiid bug related to ignoring LIMIT clause

Unit test data ([`federation`](binding/rdf4j/src/test/resources/federation) package), related to BSBM setting:

* [`ontology.owl`](binding/rdf4j/src/test/resources/federation/ontology.owl)+:
  ontology

* [`mappings.orig.obda`](binding/rdf4j/src/test/resources/federation/mappings.orig.obda)+,
  [`mappings.fed.obda`](binding/rdf4j/src/test/resources/federation/mappings.fed.obda)+,
  [`mappings.fed.teiid.obda`](binding/rdf4j/src/test/resources/federation/mappings.fed.teiid.obda)+:
  mappings in different variants: non-federated baseline (`orig`), federated setting common to most systems (`fed`), federated setting for Teiid dialect (`fed.teiid`)

* [`system-sc1.properties`](binding/rdf4j/src/test/resources/federation/system-sc1.properties)+,
  [`system-sc2.properties`](binding/rdf4j/src/test/resources/federation/system-sc2.properties)+,
  [`system-denodo-het.properties`](binding/rdf4j/src/test/resources/federation/system-denodo-het.properties)+,
  [`system-denodo-hom.properties`](binding/rdf4j/src/test/resources/federation/system-denodo-hom.properties)+,
  [`system-dremio-het.properties`](binding/rdf4j/src/test/resources/federation/system-dremio-het.properties)+,
  [`system-dremio-hom.properties`](binding/rdf4j/src/test/resources/federation/system-dremio-hom.properties)+,
  [`system-teiid-het.properties`](binding/rdf4j/src/test/resources/federation/system-teiid-het.properties)+,
  [`system-teiid-hom.properties`](binding/rdf4j/src/test/resources/federation/system-teiid-hom.properties)+:
  configuration properties for different systems and settings; JDBC URLs are used to select the correct driver but can also allow evaluating queries if pointing to correct host:port where `obdf serve` has been launched (not done in unit tests; change `obdalin.inf.unibz.it` and port prefix `400xx` accordingly, in case)

* [`system-sc1.metadata.json`](binding/rdf4j/src/test/resources/federation/system-sc1.metadata.json)+,
  [`system-sc2.metadata.json`](binding/rdf4j/src/test/resources/federation/system-sc2.metadata.json)+,
  [`system-denodo-het.metadata.json`](binding/rdf4j/src/test/resources/federation/system-denodo-het.metadata.json)+,
  [`system-denodo-hom.metadata.json`](binding/rdf4j/src/test/resources/federation/system-denodo-hom.metadata.json)+,
  [`system-dremio-het.metadata.json`](binding/rdf4j/src/test/resources/federation/system-dremio-het.metadata.json)+,
  [`system-dremio-hom.metadata.json`](binding/rdf4j/src/test/resources/federation/system-dremio-hom.metadata.json)+,
  [`system-teiid-het.metadata.json`](binding/rdf4j/src/test/resources/federation/system-teiid-het.metadata.json)+,
  [`system-teiid-hom.metadata.json`](binding/rdf4j/src/test/resources/federation/system-teiid-hom.metadata.json)+:
  DB metadata in Ontop JSON format for different systems and settings, used to avoid the need for Ontop to connect to the system to obtain required metadata when running unit tests

* [`source_relations.sc1.txt`](binding/rdf4j/src/test/resources/federation/source_relations.sc1.txt)+,
  [`source_relations.sc2.txt`](binding/rdf4j/src/test/resources/federation/source_relations.sc2.txt)+,
  [`source_relations.het.txt`](binding/rdf4j/src/test/resources/federation/source_relations.het.txt)+,
  [`source_relations.hom.txt`](binding/rdf4j/src/test/resources/federation/source_relations.hom.txt)+:
  mapping of relations to sources

* [`source_efficiency_labels.sc1.txt`](binding/rdf4j/src/test/resources/federation/source_efficiency_labels.sc1.txt)+,
  [`source_efficiency_labels.sc2.txt`](binding/rdf4j/src/test/resources/federation/source_efficiency_labels.sc2.txt)+,
  [`source_efficiency_labels.het.txt`](binding/rdf4j/src/test/resources/federation/source_efficiency_labels.het.txt)+,
  [`source_efficiency_labels.hom.txt`](binding/rdf4j/src/test/resources/federation/source_efficiency_labels.hom.txt)+:
  labeling of sources as efficient or inefficient

* [`source_dynamicity_labels.het.txt`](binding/rdf4j/src/test/resources/federation/source_dynamicity_labels.het.txt)+,
  [`source_dynamicity_labels.hom.txt`](binding/rdf4j/src/test/resources/federation/source_dynamicity_labels.hom.txt)+:
  labeling of sources as static or dynamic

* [`hints.denodo-opt.txt`](binding/rdf4j/src/test/resources/federation/hints.denodo-opt.txt)+,
  [`hints.denodo-optmatv.het.txt`](binding/rdf4j/src/test/resources/federation/hints.denodo-optmatv.het.txt)+,
  [`hints.denodo-optmatv.hom.txt`](binding/rdf4j/src/test/resources/federation/hints.denodo-optmatv.hom.txt)+,
  [`hints.dremio-opt.txt`](binding/rdf4j/src/test/resources/federation/hints.dremio-opt.txt)+,
  [`hints.dremio-optmatv.het.txt`](binding/rdf4j/src/test/resources/federation/hints.dremio-optmatv.het.txt)+,
  [`hints.dremio-optmatv.hom.txt`](binding/rdf4j/src/test/resources/federation/hints.dremio-optmatv.hom.txt)+,
  [`hints.teiid-opt.txt`](binding/rdf4j/src/test/resources/federation/hints.teiid-opt.txt)+,
  [`hints.teiid-optmatv.het.txt`](binding/rdf4j/src/test/resources/federation/hints.teiid-optmatv.het.txt)+,
  [`hints.teiid-optmatv.hom.txt`](binding/rdf4j/src/test/resources/federation/hints.teiid-optmatv.hom.txt)+:
  OBDF hints computed with/without considering materialized views, for homogeneous and heterogeneous settings and for different federation systems (due to SQL dialect differences, hints are the same across them)

* [`teiid-test.sparql`](binding/rdf4j/src/test/resources/federation/teiid-test.sparql)+,
  [`teiid-test.sql`](binding/rdf4j/src/test/resources/federation/teiid-test.sql)+:
  supporting SPARQL and SQL queries used to investigate Teiid bug related to ignoring LIMIT clause

* [`logback-test.xml`](binding/rdf4j/src/test/resources/federation/logback-test.xml):
  revised logging configuration

Unit tests supporting code and project configuration:

* [`ObdfTest.java`](binding/rdf4j/src/test/java/federationOptimization/ObdfTest.java)+:
  new annotation for OBDF-related tests, to selectively enable/disable them in Ontop CI pipeline

* [`ontop-client-cli pom.xml`](client/cli/pom.xml),
  [`ontop-binding-rdf4j pom.xml`](binding/rdf4j/pom.xml),
  [`ontop-docker-tests pom.xml`](test/docker-tests/pom.xml):
  revised `pom.xml` files to control selective running of `@ObdfTest` test cases and to add `obdf-tests` and `obdf-tests-denodo` profiles with extra dependencies for federator JDBC drivers, allowing running Ontop against such federators from the IDE (not needed by unit tests); note: `ontop-tests-denodo` profile references a private Maven repository internal to UNIBZ storing the non-redistributable Denodo driver, and therefore is not available outside UNIBZ intranet

* [`.gitignore`](.gitignore):
  exclusion of generated OBDF files


## License

The Ontop framework is available under the Apache License, Version 2.0

```
  Copyright (C) 2009 - 2025 Free University of Bozen-Bolzano

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

All documentation is licensed under the [Creative Commons](http://creativecommons.org/licenses/by/4.0/) (attribute)  license.
