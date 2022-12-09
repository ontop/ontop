package it.unibz.inf.ontop.spec.mapping.bootstrap.engines.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.dbschema.ImmutableMetadata;
import it.unibz.inf.ontop.dbschema.MetadataProvider;
import it.unibz.inf.ontop.dbschema.NamedRelationDefinition;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.dbschema.impl.JDBCMetadataProviderFactory;
import it.unibz.inf.ontop.exception.MappingBootstrappingException;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.injection.OntopSQLCredentialSettings;
import it.unibz.inf.ontop.injection.SQLPPMappingFactory;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbolFactory;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.model.vocabulary.RDF;
import it.unibz.inf.ontop.spec.mapping.SQLPPSourceQueryFactory;
import it.unibz.inf.ontop.spec.mapping.TargetAtomFactory;
import it.unibz.inf.ontop.spec.mapping.bootstrap.Bootstrapper;
import it.unibz.inf.ontop.spec.mapping.bootstrap.engines.BootstrappingEngine;
import it.unibz.inf.ontop.spec.mapping.bootstrap.util.BootConf;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.spec.mapping.util.DirectMappingOntologyUtils;
import it.unibz.inf.ontop.utils.LocalJDBCConnectionUtils;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class AbstractBootstrappingEngine implements BootstrappingEngine {

    public static class DefaultBootstrappingResults implements Bootstrapper.BootstrappingResults {
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

    protected final SQLPPSourceQueryFactory sourceQueryFactory;
    protected final JDBCMetadataProviderFactory metadataProviderFactory;
    protected final SpecificationFactory specificationFactory;
    protected final SQLPPMappingFactory ppMappingFactory;
    protected final TypeFactory typeFactory;
    protected final TermFactory termFactory;
    protected final RDF rdfFactory;
    protected final OntopSQLCredentialSettings settings;
    protected final TargetAtomFactory targetAtomFactory;
    protected final DBFunctionSymbolFactory dbFunctionSymbolFactory;
    protected ImmutableList<NamedRelationDefinition> tables; // Fetching the DB metadata is expensive, so I store it once and for all.
    protected MetadataProvider metadataProvider; // Fetching the DB metadata is expensive, so I store it once and for all.

    protected AbstractBootstrappingEngine(OntopSQLCredentialSettings settings,
                                          SpecificationFactory specificationFactory,
                                          SQLPPMappingFactory ppMappingFactory, TypeFactory typeFactory, TermFactory termFactory,
                                          RDF rdfFactory, TargetAtomFactory targetAtomFactory,
                                          DBFunctionSymbolFactory dbFunctionSymbolFactory,
                                          SQLPPSourceQueryFactory sourceQueryFactory,
                                          JDBCMetadataProviderFactory metadataProviderFactory){
        this.specificationFactory = specificationFactory;
        this.ppMappingFactory = ppMappingFactory;
        this.settings = settings;
        this.typeFactory = typeFactory;
        this.termFactory = termFactory;
        this.rdfFactory = rdfFactory;
        this.targetAtomFactory = targetAtomFactory;
        this.dbFunctionSymbolFactory = dbFunctionSymbolFactory;
        this.sourceQueryFactory = sourceQueryFactory;
        this.metadataProviderFactory = metadataProviderFactory;
        this.tables = null;
        this.metadataProvider = null;
    }

    /**
     * NOT THREAD-SAFE (not reentrant)
     * @param bootConf Some bootstrapper engines might ignore this parameter (e.g., DirectMappingEngine)
     */
    @Override
    public Bootstrapper.BootstrappingResults bootstrapMappingAndOntology(String baseIRI,
                                                                         Optional<SQLPPMapping> inputPPMapping,
                                                                         Optional<OWLOntology> inputOntology,
                                                                         BootConf bootConf
    ) throws MappingBootstrappingException {
        try {
            // inputMapping might be not empty.
            SQLPPMapping newPPMapping = extractPPMapping(inputPPMapping, fixBaseURI(baseIRI), bootConf);
            OWLOntology ontology = bootstrapOntology(baseIRI, inputOntology, newPPMapping, bootConf);

            return new DefaultBootstrappingResults(newPPMapping, ontology);
        } catch (SQLException | MetadataExtractionException e) {
            throw new MappingBootstrappingException(e);
        }
    }

    /***
     * extract all the mappings from a datasource
     *
     * @return a new OBDA Model containing all the extracted mappings
     */
    protected final SQLPPMapping extractPPMapping(Optional<SQLPPMapping> optionalMapping, String baseIRI0, BootConf bootConf) throws SQLException, MetadataExtractionException {

        SQLPPMapping mapping = initInputMapping(optionalMapping);
        tables = bootConf.getSchema().equals("") ? getDBMetadata() : getDBMetadata(bootConf.getSchema());
        ImmutableList<SQLPPTriplesMap> mappings = bootstrapMappings(initBaseIRI(baseIRI0, mapping), tables, mapping, bootConf);

        return ppMappingFactory.createSQLPreProcessedMapping(mappings, mapping.getPrefixManager());
    }

    protected final SQLPPMapping initInputMapping(Optional<SQLPPMapping> optionalMapping){
        return optionalMapping
                .orElse(ppMappingFactory.createSQLPreProcessedMapping(ImmutableList.of(),
                        specificationFactory.createPrefixManager(ImmutableMap.of())));
    }

    /**
     * Take all targetAtoms and transforms them into OWL declaratons. E.g., A(f(x)) -> OWL assertion "Declaration(Class(A))"
     **/
    protected final Set<OWLDeclarationAxiom> targetAtoms2OWLDeclarations(OWLOntologyManager manager, SQLPPMapping newPPMapping) {
        return DirectMappingOntologyUtils.extractDeclarationAxioms(
                manager,
                newPPMapping.getTripleMaps().stream()
                        .flatMap(ax -> ax.getTargetAtoms().stream()),
                typeFactory,
                true
        );
    }

    protected final ImmutableList<NamedRelationDefinition> getDBMetadata() throws SQLException, MetadataExtractionException{
        if( this.tables == null ) {
            try (Connection conn = LocalJDBCConnectionUtils.createConnection(settings)) {
                // this operation is EXPENSIVE
                this.metadataProvider = metadataProviderFactory.getMetadataProvider(conn);
                this.tables = ImmutableMetadata.extractImmutableMetadata(metadataProvider).getAllRelations();
            }
        }
        return this.tables;
    }

    protected final ImmutableList<NamedRelationDefinition> getDBMetadata(String schema) throws SQLException, MetadataExtractionException{
        if( this.tables == null ) {
            try (Connection conn = LocalJDBCConnectionUtils.createConnection(settings)) {
                // this operation is EXPENSIVE
                this.metadataProvider = metadataProviderFactory.getMetadataProvider(conn);
                this.tables = ImmutableList.copyOf(ImmutableMetadata.extractImmutableMetadata(metadataProvider).getAllRelations().stream()
                        .filter(table -> table.getID().getComponents().get(RelationID.SCHEMA_INDEX).getName().equals(schema))
                        .collect(Collectors.toList()));
            }
        }
        return this.tables;
    }

    protected final String initBaseIRI(String baseIRI0, SQLPPMapping mapping){
        return baseIRI0.isEmpty()
                ? mapping.getPrefixManager().getDefaultIriPrefix()
                : baseIRI0;
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
}
