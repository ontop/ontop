package it.unibz.inf.ontop.spec.mapping.bootstrap.impl;

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
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import it.unibz.inf.ontop.dbschema.DatabaseRelationDefinition;
import it.unibz.inf.ontop.dbschema.RDBMetadataExtractionTools;
import it.unibz.inf.ontop.exception.MappingBootstrappingException;
import it.unibz.inf.ontop.exception.MappingException;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.injection.OntopMappingSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.injection.OntopSQLCredentialSettings;
import it.unibz.inf.ontop.injection.SQLPPMappingFactory;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.spec.mapping.*;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.BnodeStringTemplateFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbolFactory;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.spec.mapping.SQLPPSourceQuery;
import it.unibz.inf.ontop.spec.mapping.bootstrap.DirectMappingBootstrapper.BootstrappingResults;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.spec.mapping.pp.impl.OntopNativeSQLPPTriplesMap;
import it.unibz.inf.ontop.spec.mapping.util.MappingOntologyUtils;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.LocalJDBCConnectionUtils;
import org.apache.commons.rdf.api.RDF;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;


/***
 *
 * A class that provides manipulation for Direct Mapping
 *
 * @author Victor
 *
 */
public class DirectMappingEngine {

	private final SQLPPSourceQueryFactory sourceQueryFactory;
	private final SpecificationFactory specificationFactory;
	private final SQLPPMappingFactory ppMappingFactory;
	private final TypeFactory typeFactory;
	private final TermFactory termFactory;
	private final RDF rdfFactory;
	private final OntopSQLCredentialSettings settings;
	private final TargetAtomFactory targetAtomFactory;
	private final DBFunctionSymbolFactory dbFunctionSymbolFactory;

	/**
	 * Entry point.
	 *
	 */
	static BootstrappingResults bootstrap(OntopMappingSQLOWLAPIConfiguration configuration, String baseIRI)
			throws MappingException, OWLOntologyCreationException, MappingBootstrappingException {
		DirectMappingEngine engine = configuration.getInjector().getInstance(DirectMappingEngine.class);
		return engine.bootstrapMappingAndOntology(baseIRI, configuration.loadPPMapping(),
				configuration.loadInputOntology());
	}

	@Inject
	private DirectMappingEngine(OntopSQLCredentialSettings settings,
								SpecificationFactory specificationFactory,
								SQLPPMappingFactory ppMappingFactory, TypeFactory typeFactory, TermFactory termFactory,
								RDF rdfFactory, TargetAtomFactory targetAtomFactory,
								DBFunctionSymbolFactory dbFunctionSymbolFactory,
								SQLPPSourceQueryFactory sourceQueryFactory) {
		this.specificationFactory = specificationFactory;
		this.ppMappingFactory = ppMappingFactory;
		this.settings = settings;
		this.typeFactory = typeFactory;
		this.termFactory = termFactory;
		this.rdfFactory = rdfFactory;
		this.targetAtomFactory = targetAtomFactory;
		this.dbFunctionSymbolFactory = dbFunctionSymbolFactory;
		this.sourceQueryFactory = sourceQueryFactory;
	}

	/**
	 * NOT THREAD-SAFE (not reentrant)
	 */
	private BootstrappingResults bootstrapMappingAndOntology(String baseIRI, Optional<SQLPPMapping> inputPPMapping,
															 Optional<OWLOntology> inputOntology)
			throws MappingBootstrappingException {

		try {
			SQLPPMapping newPPMapping = extractPPMapping(inputPPMapping, fixBaseURI(baseIRI));

			// TODO: fixURI for the ontology too?
			OWLOntology ontology = inputOntology
					.orElse(OWLManager.createOWLOntologyManager().createOntology(IRI.create(baseIRI)));

            // update ontology
            OWLOntologyManager manager = ontology.getOWLOntologyManager();
            Set<OWLDeclarationAxiom> declarationAxioms = MappingOntologyUtils.extractDeclarationAxioms(
            		manager,
                    newPPMapping.getTripleMaps().stream()
                            .flatMap(ax -> ax.getTargetAtoms().stream()),
					typeFactory,
					true
			);
            manager.addAxioms(ontology, declarationAxioms);

            return new 	BootstrappingResults() {
				@Override
				public SQLPPMapping getPPMapping() { return newPPMapping; }
				@Override
				public OWLOntology getOntology() { return ontology; }
			};
		}
		catch (SQLException | OWLOntologyCreationException | MetadataExtractionException e) {
			throw new MappingBootstrappingException(e);
		}
	}

	/***
	 * extract all the mappings from a datasource
	 *
	 * @return a new OBDA Model containing all the extracted mappings
	 */
	private SQLPPMapping extractPPMapping(Optional<SQLPPMapping> optionalMapping, String baseIRI0) throws SQLException, MetadataExtractionException {

        SQLPPMapping mapping = optionalMapping
				.orElse(ppMappingFactory.createSQLPreProcessedMapping(ImmutableList.of(),
						specificationFactory.createPrefixManager(ImmutableMap.of())));

		try (Connection conn = LocalJDBCConnectionUtils.createConnection(settings)) {
			// this operation is EXPENSIVE
			Collection<DatabaseRelationDefinition> tables = RDBMetadataExtractionTools.loadAllRelations(conn, typeFactory.getDBTypeFactory());
			String baseIRI = baseIRI0.isEmpty()
					? mapping.getPrefixManager().getDefaultPrefix()
					: baseIRI0;

			Map<DatabaseRelationDefinition, BnodeStringTemplateFunctionSymbol> bnodeTemplateMap = new HashMap<>();
			AtomicInteger currentMappingIndex = new AtomicInteger(mapping.getTripleMaps().size() + 1);

			ImmutableList<SQLPPTriplesMap> mappings = Stream.concat(
					mapping.getTripleMaps().stream(),
					tables.stream().flatMap(td -> getMapping(td, baseIRI, bnodeTemplateMap, currentMappingIndex).stream()))
					.collect(ImmutableCollectors.toList());

			return ppMappingFactory.createSQLPreProcessedMapping(mappings, mapping.getPrefixManager());
		}
	}

	public static String fixBaseURI(String prefix) {
		if (prefix.endsWith("#")) {
			return prefix.replace("#", "/");
		} else if (prefix.endsWith("/")) {
			return prefix;
		} else {
			return prefix + "/";
		}
	}


	/***
	 * generate a mapping axiom from a table of a database
	 *
	 * @param table : the data definition from which mappings are extraced
	 * @param baseIRI : the base uri needed for direct mapping axiom
	 *
	 *  @param bnodeTemplateMap
	 * @return a List of OBDAMappingAxiom-s
	 */
	public List<SQLPPTriplesMap> getMapping(DatabaseRelationDefinition table,
											String baseIRI,
											Map<DatabaseRelationDefinition, BnodeStringTemplateFunctionSymbol> bnodeTemplateMap,
											AtomicInteger mappingIndex) {

		DirectMappingAxiomProducer dmap = new DirectMappingAxiomProducer(baseIRI, termFactory, targetAtomFactory,
				rdfFactory, dbFunctionSymbolFactory, typeFactory);

		List<SQLPPTriplesMap> axioms = new ArrayList<>();
		axioms.add(new OntopNativeSQLPPTriplesMap("MAPPING-ID" + mappingIndex.getAndIncrement(),
				sourceQueryFactory.createSourceQuery(dmap.getSQL(table)), dmap.getCQ(table, bnodeTemplateMap)));

		Map<String, ImmutableList<TargetAtom>> refAxioms = dmap.getRefAxioms(table, bnodeTemplateMap);
		for (Map.Entry<String, ImmutableList<TargetAtom>> e : refAxioms.entrySet()) {
            SQLPPSourceQuery sqlQuery = sourceQueryFactory.createSourceQuery(e.getKey());
			ImmutableList<TargetAtom> targetQuery = e.getValue();
            axioms.add(new OntopNativeSQLPPTriplesMap("MAPPING-ID" + mappingIndex.getAndIncrement(), sqlQuery, targetQuery));
		}

		return axioms;
	}
}
