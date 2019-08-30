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
import it.unibz.inf.ontop.dbschema.RDBMetadata;
import it.unibz.inf.ontop.dbschema.RDBMetadataExtractionTools;
import it.unibz.inf.ontop.exception.DuplicateMappingException;
import it.unibz.inf.ontop.exception.MappingBootstrappingException;
import it.unibz.inf.ontop.exception.MappingException;
import it.unibz.inf.ontop.injection.OntopMappingSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.injection.OntopSQLCredentialSettings;
import it.unibz.inf.ontop.injection.SQLPPMappingFactory;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.model.atom.TargetAtom;
import it.unibz.inf.ontop.model.atom.TargetAtomFactory;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.BnodeStringTemplateFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbolFactory;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.spec.mapping.MappingMetadata;
import it.unibz.inf.ontop.spec.mapping.OBDASQLQuery;
import it.unibz.inf.ontop.spec.mapping.SQLMappingFactory;
import it.unibz.inf.ontop.spec.mapping.bootstrap.DirectMappingBootstrapper.BootstrappingResults;
import it.unibz.inf.ontop.spec.mapping.impl.SQLMappingFactoryImpl;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.spec.mapping.pp.impl.OntopNativeSQLPPTriplesMap;
import it.unibz.inf.ontop.spec.mapping.util.MappingOntologyUtils;
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


/***
 *
 * A class that provides manipulation for Direct Mapping
 *
 * @author Victor
 *
 */
public class DirectMappingEngine {

	public static class DefaultBootstrappingResults implements BootstrappingResults {
		private final SQLPPMapping ppMapping;
		private final OWLOntology ontology;

		public DefaultBootstrappingResults(SQLPPMapping ppMapping, OWLOntology ontology) {
			this.ppMapping = ppMapping;
			this.ontology = ontology;
		}

		@Override
		public SQLPPMapping getPPMapping() {
			return ppMapping;
		}

		@Override
		public OWLOntology getOntology() {
			return ontology;
		}
	}

	private static final SQLMappingFactory SQL_MAPPING_FACTORY = SQLMappingFactoryImpl.getInstance();
	private final SpecificationFactory specificationFactory;
	private final SQLPPMappingFactory ppMappingFactory;
	private final TypeFactory typeFactory;
	private final TermFactory termFactory;
	private final RDF rdfFactory;
	private final OntopSQLCredentialSettings settings;
	private final TargetAtomFactory targetAtomFactory;
	private final DBFunctionSymbolFactory dbFunctionSymbolFactory;

    private String baseIRI;
	private int currentMappingIndex = 1;

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
								DBFunctionSymbolFactory dbFunctionSymbolFactory) {
		this.specificationFactory = specificationFactory;
		this.ppMappingFactory = ppMappingFactory;
		this.settings = settings;
		this.typeFactory = typeFactory;
		this.termFactory = termFactory;
		this.rdfFactory = rdfFactory;
		this.targetAtomFactory = targetAtomFactory;
		this.dbFunctionSymbolFactory = dbFunctionSymbolFactory;
	}

	/**
	 * NOT THREAD-SAFE (not reentrant)
	 */
	private BootstrappingResults bootstrapMappingAndOntology(String baseIRI, Optional<SQLPPMapping> inputPPMapping,
															 Optional<OWLOntology> inputOntology)
			throws MappingBootstrappingException {

		this.baseIRI = fixBaseURI(baseIRI);

		try {
			SQLPPMapping newPPMapping = extractPPMapping(inputPPMapping);

			OWLOntology ontology = inputOntology.isPresent()
                    ? inputOntology.get()
                    : OWLManager.createOWLOntologyManager().createOntology(IRI.create(baseIRI));

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

            return new DefaultBootstrappingResults(newPPMapping, ontology);
		}
		catch (SQLException | MappingException | OWLOntologyCreationException e) {
			throw new MappingBootstrappingException(e);
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
	 * extract all the mappings from a datasource
	 *
	 * @return a new OBDA Model containing all the extracted mappings
	 */
	private SQLPPMapping extractPPMapping(Optional<SQLPPMapping> ppMapping) throws MappingException, SQLException {

        SQLPPMapping mapping;
	    if (!ppMapping.isPresent()) {
            it.unibz.inf.ontop.spec.mapping.PrefixManager prefixManager = specificationFactory.createPrefixManager(ImmutableMap.of());
            MappingMetadata mappingMetadata = specificationFactory.createMetadata(prefixManager);
            mapping = ppMappingFactory.createSQLPreProcessedMapping(ImmutableList.of(), mappingMetadata);
        }
        else
            mapping = ppMapping.get();

		currentMappingIndex = mapping.getTripleMaps().size() + 1;
		return bootstrapMappings(mapping);
	}

	/***
	 * extract mappings from given datasource, and insert them into the pre-processed mapping
	 *
	 * Duplicate Exception may happen,
	 * since mapping id is generated randomly and same id may occur
	 */
	private SQLPPMapping bootstrapMappings(SQLPPMapping ppMapping)
			throws SQLException, DuplicateMappingException {
		if (ppMapping == null) {
			throw new IllegalArgumentException("Model should not be null");
		}
		try (Connection conn = LocalJDBCConnectionUtils.createConnection(settings)) {
			RDBMetadata metadata = RDBMetadataExtractionTools.createMetadata(conn, typeFactory);
			// this operation is EXPENSIVE
			RDBMetadataExtractionTools.loadMetadata(metadata, conn, null);
			return bootstrapMappings(metadata, ppMapping);
		}
	}

	private SQLPPMapping bootstrapMappings(RDBMetadata metadata, SQLPPMapping ppMapping) throws DuplicateMappingException {
		if (baseIRI == null || baseIRI.isEmpty())
			this.baseIRI = ppMapping.getMetadata().getPrefixManager().getDefaultPrefix();
		Collection<DatabaseRelationDefinition> tables = metadata.getDatabaseRelations();
		List<SQLPPTriplesMap> mappingAxioms = new ArrayList<>();
		Map<DatabaseRelationDefinition, BnodeStringTemplateFunctionSymbol> bnodeTemplateMap = new HashMap<>();
		for (DatabaseRelationDefinition td : tables) {
			mappingAxioms.addAll(getMapping(td, baseIRI, bnodeTemplateMap));
		}

		List<SQLPPTriplesMap> mappings = new ArrayList<>();
		mappings.addAll(ppMapping.getTripleMaps());
		mappings.addAll(mappingAxioms);

		return ppMappingFactory.createSQLPreProcessedMapping(ImmutableList.copyOf(mappings), ppMapping.getMetadata());
	}


	/***
	 * generate a mapping axiom from a table of a database
	 *
	 * @param table : the data definition from which mappings are extraced
	 * @param baseUri : the base uri needed for direct mapping axiom
	 *
	 *  @param bnodeTemplateMap
	 * @return a List of OBDAMappingAxiom-s
	 */
	private List<SQLPPTriplesMap> getMapping(DatabaseRelationDefinition table, String baseUri,
											 Map<DatabaseRelationDefinition, BnodeStringTemplateFunctionSymbol> bnodeTemplateMap) {

		DirectMappingAxiomProducer dmap = new DirectMappingAxiomProducer(baseUri, termFactory, targetAtomFactory,
				rdfFactory, dbFunctionSymbolFactory, typeFactory);

		List<SQLPPTriplesMap> axioms = new ArrayList<>();
		axioms.add(new OntopNativeSQLPPTriplesMap("MAPPING-ID"+ currentMappingIndex,
				SQL_MAPPING_FACTORY.getSQLQuery(dmap.getSQL(table)), dmap.getCQ(table, bnodeTemplateMap)));
		currentMappingIndex++;

		Map<String, ImmutableList<TargetAtom>> refAxioms = dmap.getRefAxioms(table, bnodeTemplateMap);
		for (Map.Entry<String, ImmutableList<TargetAtom>> e : refAxioms.entrySet()) {
            OBDASQLQuery sqlQuery = SQL_MAPPING_FACTORY.getSQLQuery(e.getKey());
			ImmutableList<TargetAtom> targetQuery = e.getValue();
            axioms.add(new OntopNativeSQLPPTriplesMap("MAPPING-ID"+ currentMappingIndex, sqlQuery, targetQuery));
			currentMappingIndex++;
		}

		return axioms;
	}
}
