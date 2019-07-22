package it.unibz.inf.ontop.utils;

import com.google.common.collect.ImmutableList;
import com.google.inject.Injector;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.exception.DuplicateMappingException;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.model.atom.TargetAtom;
import it.unibz.inf.ontop.model.atom.TargetAtomFactory;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.BnodeStringTemplateFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbolFactory;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.spec.mapping.OBDASQLQuery;
import it.unibz.inf.ontop.spec.mapping.SQLMappingFactory;
import it.unibz.inf.ontop.spec.mapping.bootstrap.impl.DirectMappingEngine;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.spec.mapping.pp.impl.OntopNativeSQLPPTriplesMap;
import it.unibz.inf.ontop.spec.mapping.impl.SQLMappingFactoryImpl;
import it.unibz.inf.ontop.spec.mapping.bootstrap.impl.DirectMappingAxiomProducer;
import it.unibz.inf.ontop.protege.core.OBDAModel;
import it.unibz.inf.ontop.protege.core.OBDAModelManager;
import it.unibz.inf.ontop.protege.utils.JDBCConnectionManager;
import org.apache.commons.rdf.api.RDF;
import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owlapi.model.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;


public class BootstrapGenerator {


    private final JDBCConnectionManager connManager;
    private final OntopSQLOWLAPIConfiguration configuration;
    private final OBDAModel activeOBDAModel;
    private final OWLModelManager owlManager;
    private static final SQLMappingFactory SQL_MAPPING_FACTORY = SQLMappingFactoryImpl.getInstance();
    private final TermFactory termFactory;
    private final TypeFactory typeFactory;
    private final TargetAtomFactory targetAtomFactory;
    private final RDF rdfFactory;
    private int currentMappingIndex = 1;
    private final DirectMappingEngine directMappingEngine;
    private final DBFunctionSymbolFactory dbFunctionSymbolFactory;

    public BootstrapGenerator(OBDAModelManager obdaModelManager, String baseUri,
                              OWLModelManager owlManager)
            throws DuplicateMappingException, SQLException {
        connManager = JDBCConnectionManager.getJDBCConnectionManager();
        this.owlManager =  owlManager;
        configuration = obdaModelManager.getConfigurationManager().buildOntopSQLOWLAPIConfiguration(owlManager.getActiveOntology());
        activeOBDAModel = obdaModelManager.getActiveOBDAModel();
        termFactory = obdaModelManager.getTermFactory();
        typeFactory = obdaModelManager.getTypeFactory();
        targetAtomFactory = obdaModelManager.getTargetAtomFactory();
        Injector injector = configuration.getInjector();
        directMappingEngine = injector.getInstance(DirectMappingEngine.class);
        dbFunctionSymbolFactory = injector.getInstance(DBFunctionSymbolFactory.class);
        rdfFactory = configuration.getRdfFactory();

        bootstrapMappingAndOntologyProtege(baseUri);
    }

    private void bootstrapMappingAndOntologyProtege(String baseUri) throws DuplicateMappingException, SQLException {

        List<SQLPPTriplesMap> sqlppTriplesMaps = bootstrapMapping(activeOBDAModel.generatePPMapping(), baseUri);

        // update protege ontology
        OWLOntologyManager manager = owlManager.getActiveOntology().getOWLOntologyManager();
        Set<OWLDeclarationAxiom> declarationAxioms = directMappingEngine.extractDeclarationAxioms(manager,
                sqlppTriplesMaps.stream()
                        .flatMap(ax -> ax.getTargetAtoms().stream()));

        List<AddAxiom> addAxioms = declarationAxioms.stream()
                .map(ax -> new AddAxiom(owlManager.getActiveOntology(), ax))
                .collect(Collectors.toList());

        owlManager.applyChanges(addAxioms);
    }

    private List<SQLPPTriplesMap> bootstrapMapping(SQLPPMapping ppMapping, String baseURI)
            throws DuplicateMappingException, SQLException {

        List<SQLPPTriplesMap> newTriplesMap = new ArrayList<>();

        currentMappingIndex = ppMapping.getTripleMaps().size() + 1;

        final Connection conn;
        try {
            conn = connManager.getConnection(configuration.getSettings());
        }
        catch (SQLException e) {
            throw new RuntimeException("JDBC connection is missing, have you setup Ontop Mapping properties?" +
                    " Message: " + e.getMessage());
        }
        RDBMetadata metadata = RDBMetadataExtractionTools.createMetadata(conn, typeFactory);

        // this operation is EXPENSIVE
        RDBMetadataExtractionTools.loadMetadata(metadata, conn, null);

        if (baseURI == null || baseURI.isEmpty()) {
            baseURI = ppMapping.getMetadata().getPrefixManager().getDefaultPrefix();
        }
        else {
            baseURI = DirectMappingEngine.fixBaseURI(baseURI);
        }
        Collection<DatabaseRelationDefinition> tables = metadata.getDatabaseRelations();

        for (DatabaseRelationDefinition td : tables) {
            newTriplesMap.addAll(getMapping(td, baseURI));
        }

        //add to the current model the boostrapped triples map
        for (SQLPPTriplesMap triplesMap: newTriplesMap) {
            activeOBDAModel.addTriplesMap(triplesMap, true);
        }
        return newTriplesMap;
    }


    private List<SQLPPTriplesMap> getMapping(DatabaseRelationDefinition table, String baseUri) {

        DirectMappingAxiomProducer dmap = new DirectMappingAxiomProducer(baseUri, termFactory, targetAtomFactory,
                rdfFactory, dbFunctionSymbolFactory, typeFactory);

        List<SQLPPTriplesMap> axioms = new ArrayList<>();
        Map<DatabaseRelationDefinition, BnodeStringTemplateFunctionSymbol> bnodeTemplateMap = new HashMap<>();

        axioms.add(new OntopNativeSQLPPTriplesMap("MAPPING-ID"+ currentMappingIndex,
                SQL_MAPPING_FACTORY.getSQLQuery(dmap.getSQL(table)),
                dmap.getCQ(table, bnodeTemplateMap)));
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
