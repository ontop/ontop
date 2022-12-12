package it.unibz.inf.ontop.spec.mapping.bootstrap.engines.impl;

/*
 * #%L
 * ontop-obdalib-owlapi
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import it.unibz.inf.ontop.dbschema.NamedRelationDefinition;
import it.unibz.inf.ontop.dbschema.impl.JDBCMetadataProviderFactory;
import it.unibz.inf.ontop.exception.MappingBootstrappingException;
import it.unibz.inf.ontop.injection.OntopSQLCredentialSettings;
import it.unibz.inf.ontop.injection.SQLPPMappingFactory;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import org.apache.commons.rdf.api.RDF;
import it.unibz.inf.ontop.spec.mapping.*;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.BnodeStringTemplateFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbolFactory;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.spec.mapping.bootstrap.util.DirectMappingAxiomProducer;
import it.unibz.inf.ontop.spec.mapping.bootstrap.util.mpbootstrapper.BootConf;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.spec.mapping.pp.impl.OntopNativeSQLPPTriplesMap;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.semanticweb.owlapi.model.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;


/***
 * A class that provides manipulation for Direct Mapping
 *
 * @author Victor
 *
 */
public class DirectMappingEngine extends AbstractBootstrappingEngine {

	@Inject
	private DirectMappingEngine(OntopSQLCredentialSettings settings,
								SpecificationFactory specificationFactory,
								SQLPPMappingFactory ppMappingFactory, TypeFactory typeFactory, TermFactory termFactory,
								RDF rdfFactory, TargetAtomFactory targetAtomFactory,
								DBFunctionSymbolFactory dbFunctionSymbolFactory,
								SQLPPSourceQueryFactory sourceQueryFactory,
								JDBCMetadataProviderFactory metadataProviderFactory) {
		super(settings, specificationFactory, ppMappingFactory,
				typeFactory, termFactory, rdfFactory,
				targetAtomFactory, dbFunctionSymbolFactory, sourceQueryFactory, metadataProviderFactory);
	}

	/***
	 * extract mappings from given datasource, and insert them into the pre-processed mapping
	 *
	 * Duplicate Exception may happen,
	 * since mapping id is generated randomly and same id may occur
	 */
	@Override
	public ImmutableList<SQLPPTriplesMap> bootstrapMappings(String baseIRI, ImmutableList<NamedRelationDefinition> tables, SQLPPMapping mapping, BootConf bootConf) {
		Map<NamedRelationDefinition, BnodeStringTemplateFunctionSymbol> bnodeTemplateMap = new HashMap<>();
		AtomicInteger currentMappingIndex = new AtomicInteger(mapping.getTripleMaps().size() + 1);

		ImmutableList<SQLPPTriplesMap> mappings = Stream.concat(
						mapping.getTripleMaps().stream(),
						tables.stream().flatMap(td -> getMapping(td, baseIRI, bnodeTemplateMap, currentMappingIndex).stream()))
				.collect(ImmutableCollectors.toList());

		return mappings;
	}

	@Override
	public OWLOntology bootstrapOntology(String baseIRI, Optional<OWLOntology> inputOntology, SQLPPMapping newPPMapping, BootConf bootConf) throws MappingBootstrappingException {

		OWLOntology ontology = null;
		try {
			ontology = inputOntology.isPresent() ? inputOntology.get() : createEmptyOntology(baseIRI);
		} catch (OWLOntologyCreationException e) {
			throw new MappingBootstrappingException(e);
		}

		OWLOntologyManager manager = ontology.getOWLOntologyManager();

		Set<OWLDeclarationAxiom> declarationAxioms = targetAtoms2OWLDeclarations(manager, newPPMapping);
		manager.addAxioms(ontology, declarationAxioms);

		return ontology;
	}

	/***
	 * generate a mapping axiom from a table of a database
	 *
	 * @param table : the data definition from which mappings are extraced
	 * @param baseIRI : the base uri needed for direct mapping axiom
	 *
	 * @param bnodeTemplateMap
	 * @return a List of OBDAMappingAxiom-s
	 *
	 * TODO: Make private.
	 */
	public ImmutableList<SQLPPTriplesMap> getMapping(NamedRelationDefinition table,
													 String baseIRI,
													 Map<NamedRelationDefinition, BnodeStringTemplateFunctionSymbol> bnodeTemplateMap,
													 AtomicInteger mappingIndex) {

		DirectMappingAxiomProducer dmap = new DirectMappingAxiomProducer(baseIRI, termFactory, targetAtomFactory,
				rdfFactory, dbFunctionSymbolFactory, typeFactory);

		return Stream.concat(
						Stream.of(Maps.immutableEntry(dmap.getSQL(table), dmap.getCQ(table, bnodeTemplateMap))),
						table.getForeignKeys().stream()
								.map(fk -> Maps.immutableEntry(dmap.getRefSQL(fk), dmap.getRefCQ(fk, bnodeTemplateMap))))
				.map(e -> new OntopNativeSQLPPTriplesMap("MAPPING-ID" + mappingIndex.getAndIncrement(),
						sourceQueryFactory.createSourceQuery(e.getKey()), e.getValue()))
				.collect(ImmutableCollectors.toList());
	}
}
//	}
//
//	private final SQLPPSourceQueryFactory sourceQueryFactory;
//	private final JDBCMetadataProviderFactory metadataProviderFactory;
//	private final SpecificationFactory specificationFactory;
//	private final SQLPPMappingFactory ppMappingFactory;
//	private final TypeFactory typeFactory;
//	private final TermFactory termFactory;
//	private final RDF rdfFactory;
//	private final OntopSQLCredentialSettings settings;
//	private final TargetAtomFactory targetAtomFactory;
//	private final DBFunctionSymbolFactory dbFunctionSymbolFactory;
//
//	/**
//	 * Entry point.
//	 *
//	 */
//	public static BootstrappingResults bootstrapMappingAndOntology(OntopMappingSQLOWLAPIConfiguration configuration, String baseIRI)
//			throws MappingException, OWLOntologyCreationException, MappingBootstrappingException {
//		DirectMappingEngine engine = configuration.getInjector().getInstance(DirectMappingEngine.class);
//		return engine.bootstrapMappingAndOntology(baseIRI, configuration.loadPPMapping(),
//				configuration.loadInputOntology());
//	}
//
//	@Inject
//	private DirectMappingEngine(OntopSQLCredentialSettings settings,
//								SpecificationFactory specificationFactory,
//								SQLPPMappingFactory ppMappingFactory, TypeFactory typeFactory, TermFactory termFactory,
//								RDF rdfFactory, TargetAtomFactory targetAtomFactory,
//								DBFunctionSymbolFactory dbFunctionSymbolFactory,
//								SQLPPSourceQueryFactory sourceQueryFactory,
//								JDBCMetadataProviderFactory metadataProviderFactory){
//		this.specificationFactory = specificationFactory;
//		this.ppMappingFactory = ppMappingFactory;
//		this.settings = settings;
//		this.typeFactory = typeFactory;
//		this.termFactory = termFactory;
//		this.rdfFactory = rdfFactory;
//		this.targetAtomFactory = targetAtomFactory;
//		this.dbFunctionSymbolFactory = dbFunctionSymbolFactory;
//		this.sourceQueryFactory = sourceQueryFactory;
//		this.metadataProviderFactory = metadataProviderFactory;
//	}
//
//	/**
//	 * NOT THREAD-SAFE (not reentrant)
//	 */
//	private BootstrappingResults bootstrapMappingAndOntology(String baseIRI, Optional<SQLPPMapping> inputPPMapping,
//															 Optional<OWLOntology> inputOntology)
//			throws MappingBootstrappingException {
//
//		try {
//			SQLPPMapping newPPMapping = extractPPMapping(inputPPMapping, fixBaseURI(baseIRI));
//
//			// TODO: fixURI for the ontology too?
//			OWLOntology ontology = inputOntology
//					.orElse(OWLManager.createOWLOntologyManager().createOntology(IRI.create(baseIRI)));
//
//            // update ontology
//            MappingOntologyUtils.extractAndInsertDeclarationAxioms(
//					ontology,
//                    newPPMapping.getTripleMaps(),
//					typeFactory,
//					true);
//
//            return new 	BootstrappingResults() {
//				@Override
//				public SQLPPMapping getPPMapping() { return newPPMapping; }
//				@Override
//				public OWLOntology getOntology() { return ontology; }
//			};
//		}
//		catch (SQLException | OWLOntologyCreationException | MetadataExtractionException e) {
//			throw new MappingBootstrappingException(e);
//		}
//	}
//
//	/***
//	 * extract all the mappings from a datasource
//	 *
//	 * @return a new OBDA Model containing all the extracted mappings
//	 */
//	private SQLPPMapping extractPPMapping(Optional<SQLPPMapping> optionalMapping, String baseIRI0) throws SQLException, MetadataExtractionException {
//
//        SQLPPMapping mapping = optionalMapping
//				.orElse(ppMappingFactory.createSQLPreProcessedMapping(ImmutableList.of(),
//						specificationFactory.createPrefixManager(ImmutableMap.of())));
//
//		try (Connection conn = LocalJDBCConnectionUtils.createConnection(settings)) {
//			// this operation is EXPENSIVE
//			MetadataProvider metadataProvider = metadataProviderFactory.getMetadataProvider(conn);
//			ImmutableList<NamedRelationDefinition> tables = ImmutableMetadata.extractImmutableMetadata(metadataProvider).getAllRelations();
//			String baseIRI = baseIRI0.isEmpty()
//					? mapping.getPrefixManager().getDefaultIriPrefix()
//					: baseIRI0;
//
//			Map<NamedRelationDefinition, BnodeStringTemplateFunctionSymbol> bnodeTemplateMap = new HashMap<>();
//			AtomicInteger currentMappingIndex = new AtomicInteger(mapping.getTripleMaps().size() + 1);
//
//			ImmutableList<SQLPPTriplesMap> mappings = Stream.concat(
//					mapping.getTripleMaps().stream(),
//					tables.stream().flatMap(td -> getMapping(td, baseIRI, bnodeTemplateMap, currentMappingIndex).stream()))
//					.collect(ImmutableCollectors.toList());
//
//			return ppMappingFactory.createSQLPreProcessedMapping(mappings, mapping.getPrefixManager());
//		}
//	}
//
//	public static String fixBaseURI(String prefix) {
//		if (prefix.endsWith("#")) {
//			return prefix.replace("#", "/");
//		} else if (prefix.endsWith("/")) {
//			return prefix;
//		} else {
//			return prefix + "/";
//		}
//	}
//
//
//	/***
//	 * generate a mapping axiom from a table of a database
//	 *
//	 * @param table : the data definition from which mappings are extraced
//	 * @param baseIRI : the base uri needed for direct mapping axiom
//	 *
//	 *  @param bnodeTemplateMap
//	 * @return a List of OBDAMappingAxiom-s
//	 */
//	public ImmutableList<SQLPPTriplesMap> getMapping(NamedRelationDefinition table,
//                                                     String baseIRI,
//                                                     Map<NamedRelationDefinition, BnodeStringTemplateFunctionSymbol> bnodeTemplateMap,
//                                                     AtomicInteger mappingIndex) {
//
//		DirectMappingAxiomProducer dmap = new DirectMappingAxiomProducer(baseIRI, termFactory, targetAtomFactory,
//				rdfFactory, dbFunctionSymbolFactory, typeFactory);
//
//		return Stream.concat(
//				Stream.of(Maps.immutableEntry(dmap.getSQL(table), dmap.getCQ(table, bnodeTemplateMap))),
//				table.getForeignKeys().stream()
//						.map(fk -> Maps.immutableEntry(dmap.getRefSQL(fk), dmap.getRefCQ(fk, bnodeTemplateMap))))
//					.map(e -> new OntopNativeSQLPPTriplesMap("MAPPING-ID" + mappingIndex.getAndIncrement(),
//						sourceQueryFactory.createSourceQuery(e.getKey()), e.getValue()))
//				.collect(ImmutableCollectors.toList());
//	}
// }
