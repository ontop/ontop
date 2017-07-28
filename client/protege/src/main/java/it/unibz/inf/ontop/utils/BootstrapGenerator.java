package it.unibz.inf.ontop.utils;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.DatabaseRelationDefinition;
import it.unibz.inf.ontop.dbschema.RDBMetadata;
import it.unibz.inf.ontop.dbschema.RDBMetadataExtractionTools;
import it.unibz.inf.ontop.exception.DuplicateMappingException;
import it.unibz.inf.ontop.exception.InvalidMappingException;
import it.unibz.inf.ontop.exception.MappingIOException;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.spec.mapping.OBDASQLQuery;
import it.unibz.inf.ontop.spec.mapping.SQLMappingFactory;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.spec.mapping.pp.impl.OntopNativeSQLPPTriplesMap;
import it.unibz.inf.ontop.spec.mapping.impl.SQLMappingFactoryImpl;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.spec.ontology.DataPropertyExpression;
import it.unibz.inf.ontop.spec.ontology.ImmutableOntologyVocabulary;
import it.unibz.inf.ontop.spec.ontology.OClass;
import it.unibz.inf.ontop.spec.ontology.ObjectPropertyExpression;
import it.unibz.inf.ontop.spec.ontology.MappingVocabularyExtractor;
import it.unibz.inf.ontop.spec.mapping.bootstrap.impl.DirectMappingAxiomProducer;
import it.unibz.inf.ontop.protege.core.OBDAModel;
import it.unibz.inf.ontop.protege.core.OBDAModelManager;
import it.unibz.inf.ontop.protege.utils.JDBCConnectionManager;
import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owlapi.model.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static it.unibz.inf.ontop.model.OntopModelSingletons.TERM_FACTORY;


public class BootstrapGenerator {


    private final JDBCConnectionManager connManager;
    private final OntopSQLOWLAPIConfiguration configuration;
    private final OBDAModel activeOBDAModel;
    private final OWLModelManager owlManager;
    private static final SQLMappingFactory SQL_MAPPING_FACTORY = SQLMappingFactoryImpl.getInstance();
    private final MappingVocabularyExtractor vocabularyExtractor;
    private int currentMappingIndex = 1;

    public BootstrapGenerator(OBDAModelManager obdaModelManager, String baseUri, OWLModelManager owlManager) throws DuplicateMappingException, InvalidMappingException, MappingIOException, SQLException, OWLOntologyCreationException, OWLOntologyStorageException {

        connManager = JDBCConnectionManager.getJDBCConnectionManager();
        this.owlManager =  owlManager;
        configuration = obdaModelManager.getConfigurationManager().buildOntopSQLOWLAPIConfiguration(owlManager.getActiveOntology());
        activeOBDAModel = obdaModelManager.getActiveOBDAModel();
        vocabularyExtractor = configuration.getInjector().getInstance(MappingVocabularyExtractor.class);

        bootstrapMappingAndOntologyProtege(baseUri);
    }

    private void bootstrapMappingAndOntologyProtege(String baseUri) throws DuplicateMappingException, MappingIOException,
            InvalidMappingException, SQLException, OWLOntologyCreationException, OWLOntologyStorageException {


        List<SQLPPTriplesMap> sqlppTriplesMaps = bootstrapMapping(activeOBDAModel.generatePPMapping(), baseUri);


        ImmutableOntologyVocabulary newVocabulary = vocabularyExtractor.extractVocabulary(
                sqlppTriplesMaps.stream()
                        .flatMap(ax -> ax.getTargetAtoms().stream()));

        updateProtegeOntology (owlManager.getActiveOntology(), newVocabulary);


    }

    private List<SQLPPTriplesMap> bootstrapMapping(SQLPPMapping ppMapping, String baseURI)
            throws DuplicateMappingException, SQLException {

        List<SQLPPTriplesMap> newTriplesMap = new ArrayList<>();

        currentMappingIndex = ppMapping.getTripleMaps().size() + 1;


        Connection conn = null;
        try {
            conn = connManager.getConnection(configuration.getSettings());
        } catch (SQLException e) {
            throw new RuntimeException("JDBC connection are missing, have you setup Ontop Mapping properties?" +
                    " Message: " + e.getMessage());
        }
        RDBMetadata metadata = RDBMetadataExtractionTools.createMetadata(conn);

        // this operation is EXPENSIVE
        RDBMetadataExtractionTools.loadMetadata(metadata, conn, null);

        if (baseURI == null || baseURI.isEmpty())
            baseURI = ppMapping.getMetadata().getPrefixManager().getDefaultPrefix();
        else{
            baseURI = fixBaseURI(baseURI);
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

    private String fixBaseURI(String prefix) {
        if (prefix.endsWith("#")) {
            return prefix.replace("#", "/");
        } else if (prefix.endsWith("/")) {
            return prefix;
        } else {
            return prefix + "/";
        }
    }

    private void updateProtegeOntology(OWLOntology ontology, ImmutableOntologyVocabulary vocabulary)
            throws OWLOntologyCreationException, OWLOntologyStorageException, SQLException {


        OWLDataFactory dataFactory = owlManager.getOWLDataFactory();

        owlManager.applyChanges( extractDeclarationAxioms(ontology, vocabulary, dataFactory));


    }

    private List<AddAxiom> extractDeclarationAxioms(OWLOntology ontology, ImmutableOntologyVocabulary vocabulary, OWLDataFactory dataFactory) {
        List<AddAxiom> declarationAxioms = new ArrayList<>();



        //Add all the classes
        for (OClass c :  vocabulary.getClasses()) {
            OWLClass owlClass = dataFactory.getOWLClass(IRI.create(c.getName()));
            declarationAxioms.add(new AddAxiom(ontology, dataFactory.getOWLDeclarationAxiom(owlClass)));
        }

        //Add all the object properties
        for (ObjectPropertyExpression p : vocabulary.getObjectProperties()){
            OWLObjectProperty property = dataFactory.getOWLObjectProperty(IRI.create(p.getName()));
            declarationAxioms.add(new AddAxiom(ontology, dataFactory.getOWLDeclarationAxiom(property)));
        }

        //Add all the data properties
        for (DataPropertyExpression p : vocabulary.getDataProperties()){
            OWLDataProperty property = dataFactory.getOWLDataProperty(IRI.create(p.getName()));
            declarationAxioms.add(new AddAxiom(ontology, dataFactory.getOWLDeclarationAxiom(property)));
        }

        return declarationAxioms;

    }

    private List<SQLPPTriplesMap> getMapping(DatabaseRelationDefinition table, String baseUri) {

        DirectMappingAxiomProducer dmap = new DirectMappingAxiomProducer(baseUri, TERM_FACTORY);

        List<SQLPPTriplesMap> axioms = new ArrayList<>();
        axioms.add(new OntopNativeSQLPPTriplesMap("MAPPING-ID"+ currentMappingIndex, SQL_MAPPING_FACTORY.getSQLQuery(dmap.getSQL(table)), dmap.getCQ(table)));
        currentMappingIndex++;

        Map<String, ImmutableList<ImmutableFunctionalTerm>> refAxioms = dmap.getRefAxioms(table);
        for (Map.Entry<String, ImmutableList<ImmutableFunctionalTerm>> e : refAxioms.entrySet()) {
            OBDASQLQuery sqlQuery = SQL_MAPPING_FACTORY.getSQLQuery(e.getKey());
            ImmutableList<ImmutableFunctionalTerm> targetQuery = e.getValue();
            axioms.add(new OntopNativeSQLPPTriplesMap("MAPPING-ID"+ currentMappingIndex, sqlQuery, targetQuery));
            currentMappingIndex++;
        }

        return axioms;
    }

}
