package it.unibz.inf.ontop.si;

import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.exception.DuplicateMappingException;
import it.unibz.inf.ontop.injection.MappingFactory;
import it.unibz.inf.ontop.injection.OntopMappingConfiguration;
import it.unibz.inf.ontop.injection.QuestConfiguration;
import it.unibz.inf.ontop.io.PrefixManager;
import it.unibz.inf.ontop.model.OBDAException;
import it.unibz.inf.ontop.model.OBDAModel;
import it.unibz.inf.ontop.model.impl.OBDAModelImpl;
import it.unibz.inf.ontop.model.impl.OBDAVocabulary;
import it.unibz.inf.ontop.ontology.*;
import it.unibz.inf.ontop.ontology.impl.OntologyFactoryImpl;
import it.unibz.inf.ontop.owlapi.OWLAPIABoxIterator;
import it.unibz.inf.ontop.owlapi.OWLAPITranslatorUtility;
import it.unibz.inf.ontop.owlrefplatform.core.abox.QuestMaterializer;
import it.unibz.inf.ontop.owlrefplatform.core.abox.RDBMSSIRepositoryManager;
import it.unibz.inf.ontop.owlrefplatform.core.dagjgrapht.TBoxReasoner;
import it.unibz.inf.ontop.owlrefplatform.core.dagjgrapht.TBoxReasonerImpl;
import it.unibz.inf.ontop.rdf4j.RDF4JRDFIterator;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.ValueFactoryImpl;
import org.eclipse.rdf4j.query.Dataset;
import org.eclipse.rdf4j.rio.*;
import org.eclipse.rdf4j.rio.helpers.BasicParserSettings;
import org.eclipse.rdf4j.rio.helpers.RDFHandlerBase;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.model.parameters.OntologyCopy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

/**
 * TODO: find a better name
 */
public class OntopSemanticIndexLoaderImpl implements OntopSemanticIndexLoader {

    private static final Logger log = LoggerFactory.getLogger(OntopSemanticIndexLoaderImpl.class);

    private static final String DEFAULT_USER = "sa";
    private static final String DEFAULT_PASSWORD = "";
    private static final boolean OPTIMIZE_EQUIVALENCES = true;;
    private final QuestConfiguration configuration;
    private final Connection connection;
    private static final OntologyFactory ONTOLOGY_FACTORY = OntologyFactoryImpl.getInstance();

    private static class RepositoryInit {
        final RDBMSSIRepositoryManager dataRepository;
        final Optional<Set<OWLOntology>> ontologyClosure;
        final String jdbcUrl;
        final ImmutableOntologyVocabulary vocabulary;
        final Connection localConnection;

        private RepositoryInit(RDBMSSIRepositoryManager dataRepository, Optional<Set<OWLOntology>> ontologyClosure, String jdbcUrl,
                               ImmutableOntologyVocabulary vocabulary, Connection localConnection) {
            this.dataRepository = dataRepository;
            this.ontologyClosure = ontologyClosure;
            this.jdbcUrl = jdbcUrl;
            this.vocabulary = vocabulary;
            this.localConnection = localConnection;
        }
    }


    private OntopSemanticIndexLoaderImpl(QuestConfiguration configuration, Connection connection) {
        this.configuration = configuration;
        this.connection = connection;
    }

    @Override
    public QuestConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public void close() {
        try {
            if (connection != null && (!connection.isClosed())) {
                connection.close();
            }
        } catch (SQLException e) {
            log.error("Error while closing the DB: " + e.getMessage());
        }
    }


    public static OntopSemanticIndexLoader loadOntologyIndividuals(String owlFile, Properties properties)
            throws SemanticIndexException {
        try {
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File(owlFile));

            return loadOntologyIndividuals(ontology, properties);

        } catch (OWLOntologyCreationException e) {
            throw new SemanticIndexException(e.getMessage());
        }
    }


    public static OntopSemanticIndexLoader loadOntologyIndividuals(OWLOntology owlOntology, Properties properties)
            throws SemanticIndexException {

        RepositoryInit init = createRepository(owlOntology);

        try {
            /*
            Loads the data
             */
            OWLAPIABoxIterator aBoxIter = new OWLAPIABoxIterator(init.ontologyClosure
                    .orElseThrow(() -> new IllegalStateException("An ontology closure was expected")), init.vocabulary);

            int count = init.dataRepository.insertData(init.localConnection, aBoxIter, 5000, 500);
            log.debug("Inserted {} triples from the ontology.", count);

            /*
            Creates the configuration and the loader object
             */
            QuestConfiguration configuration = createConfiguration(init.dataRepository, owlOntology, init.jdbcUrl, properties);
            return new OntopSemanticIndexLoaderImpl(configuration, init.localConnection);

        } catch (SQLException e) {
            throw new SemanticIndexException(e.getMessage());
        }
    }

    public static OntopSemanticIndexLoader loadRDFGraph(Dataset dataset, Properties properties) throws SemanticIndexException {
        try {
            OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
            OWLOntology emptyOntology = ontologyManager.createOntology();

            Ontology tbox =  loadTBoxFromDataset(dataset);

            RepositoryInit init = createRepository(tbox, Optional.empty());

            /*
            Loads the data
             */
            insertDataset(init.dataRepository, init.localConnection, dataset);

            /*
            Creates the configuration and the loader object
             */
            // TODO: insert the TBox
            QuestConfiguration configuration = createConfiguration(init.dataRepository, emptyOntology, init.jdbcUrl, properties);
            return new OntopSemanticIndexLoaderImpl(configuration, init.localConnection);

        } catch (OWLOntologyCreationException | IOException e) {
            throw new SemanticIndexException(e.getMessage());
        }
    }

    /**
     * TODO: do want to use a different ontology for the materialization and the output OBDA system?
     */
    public static OntopSemanticIndexLoader loadVirtualAbox(QuestConfiguration obdaConfiguration, Properties properties)
            throws SemanticIndexException {

        try {
            OWLOntology inputOntology = obdaConfiguration.loadInputOntology()
                    .orElseThrow(() -> new IllegalArgumentException("The configuration must provide an ontology"));

            RepositoryInit init = createRepository(inputOntology);

            QuestMaterializer materializer = new QuestMaterializer(obdaConfiguration, true);
            Iterator<Assertion> assertionIterator = materializer.getAssertionIterator();

            int count = init.dataRepository.insertData(init.localConnection, assertionIterator, 5000, 500);
            materializer.disconnect();
            log.debug("Inserted {} triples from the mappings.", count);

            /*
            Creates the configuration and the loader object
             */
            QuestConfiguration configuration = createConfiguration(init.dataRepository, inputOntology, init.jdbcUrl, properties);
            return new OntopSemanticIndexLoaderImpl(configuration, init.localConnection);

        } catch (Exception e) {
            throw new SemanticIndexException(e.getMessage());
        }
    }

    private static RepositoryInit createRepository(OWLOntology owlOntology) throws SemanticIndexException {

        Set<OWLOntology> ontologyClosure = owlOntology.getOWLOntologyManager().getImportsClosure(owlOntology);
        Ontology ontology = OWLAPITranslatorUtility.mergeTranslateOntologies(ontologyClosure);
        return createRepository(ontology, Optional.of(ontologyClosure));
    }

    private static RepositoryInit createRepository(Ontology ontology, Optional<Set<OWLOntology>> ontologyClosure)
            throws SemanticIndexException {
        ImmutableOntologyVocabulary vocabulary = ontology.getVocabulary();

        final TBoxReasoner reformulationReasoner = TBoxReasonerImpl.create(ontology, OPTIMIZE_EQUIVALENCES);

        RDBMSSIRepositoryManager dataRepository = new RDBMSSIRepositoryManager(reformulationReasoner, vocabulary);

        log.warn("Semantic index mode initializing: \nString operation over URI are not supported in this mode ");
        // we work in memory (with H2), the database is clean and
        // Quest will insert new Abox assertions into the database.
        dataRepository.generateMetadata();

        String jdbcUrl = buildNewJdbcUrl();

        try {
            Connection localConnection = DriverManager.getConnection(jdbcUrl, DEFAULT_USER, DEFAULT_PASSWORD);

            // Creating the ABox repository
            dataRepository.createDBSchemaAndInsertMetadata(localConnection);
            return new RepositoryInit(dataRepository, ontologyClosure, jdbcUrl, vocabulary, localConnection);

        } catch (SQLException e) {
            throw new SemanticIndexException(e.getMessage());
        }
    }

    private static QuestConfiguration createConfiguration(RDBMSSIRepositoryManager dataRepository, OWLOntology owlOntology,
                                                          String jdbcUrl, Properties properties) throws SemanticIndexException {
        OBDAModel ppMapping = createPPMapping(dataRepository);

        /**
         * Tbox: ontology without the ABox axioms (are in the DB now).
         */
        OWLOntologyManager newManager = OWLManager.createOWLOntologyManager();
        OWLOntology tbox;
        try {
            tbox = newManager.copyOntology(owlOntology, OntologyCopy.SHALLOW);
        } catch (OWLOntologyCreationException e) {
            throw new SemanticIndexException(e.getMessage());
        }
        newManager.removeAxioms(tbox, tbox.getABoxAxioms(Imports.EXCLUDED));

        return QuestConfiguration.defaultBuilder()
                .obdaModel(ppMapping)
                .ontology(tbox)
                .properties(properties)
                .jdbcUrl(jdbcUrl)
                .jdbcUser(DEFAULT_USER)
                .jdbcPassword(DEFAULT_PASSWORD)
                //TODO: remove it (required by Tomcat...)
                .jdbcDriver("org.h2.Driver")
                .iriDictionary(dataRepository.getUriMap())
                .build();
    }


    private static OBDAModel createPPMapping(RDBMSSIRepositoryManager dataRepository) {
        OntopMappingConfiguration defaultConfiguration = OntopMappingConfiguration.defaultBuilder()
                .build();
        MappingFactory mappingFactory = defaultConfiguration.getInjector().getInstance(MappingFactory.class);
        PrefixManager prefixManager = mappingFactory.create(ImmutableMap.of());

        try {
            return new OBDAModelImpl(dataRepository.getMappings(),
                    mappingFactory.create(prefixManager));

        } catch (DuplicateMappingException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    private static String buildNewJdbcUrl() {
        return "jdbc:h2:mem:questrepository:" + System.currentTimeMillis() + ";LOG=0;CACHE_SIZE=65536;LOCK_MODE=0;UNDO_LOG=0";
    }


    private static void insertDataset(RDBMSSIRepositoryManager dataRepository, Connection localConnection, Dataset dataset)
            throws SemanticIndexException {
        // Merge default and named graphs to filter duplicates
        Set<IRI> graphIRIs = new HashSet<IRI>();
        graphIRIs.addAll(dataset.getDefaultGraphs());
        graphIRIs.addAll(dataset.getNamedGraphs());

        for (Resource graphIRI : graphIRIs) {
            insertGraph(dataRepository, localConnection, ((IRI)graphIRI));
        }
    }

    private static void insertGraph(RDBMSSIRepositoryManager dataRepository, Connection localConnection,
                                   IRI graphIRI) throws SemanticIndexException {

        RDFFormat rdfFormat = Rio.getParserFormatForFileName(graphIRI.toString()).get();
        RDFParser rdfParser = Rio.createParser(rdfFormat);

        ParserConfig config = rdfParser.getParserConfig();
        // To emulate DatatypeHandling.IGNORE
        config.addNonFatalError(BasicParserSettings.FAIL_ON_UNKNOWN_DATATYPES);
        config.addNonFatalError(BasicParserSettings.VERIFY_DATATYPE_VALUES);
        config.addNonFatalError(BasicParserSettings.NORMALIZE_DATATYPE_VALUES);
//		config.set(BasicParserSettings.PRESERVE_BNODE_IDS, true);

        try {
            URL graphURL = new URL(graphIRI.toString());
            InputStream in = graphURL.openStream();

            RDF4JRDFIterator rdfHandler = new RDF4JRDFIterator();
            rdfParser.setRDFHandler(rdfHandler);

            Thread insert = new Thread(new Insert(rdfParser, in, graphIRI.toString()));
            Thread process = new Thread(new Process(rdfHandler, dataRepository, localConnection, graphIRI.toString()));

            //start threads
            insert.start();
            process.start();

            insert.join();
            process.join();
        } catch (InterruptedException e) {
            throw new SemanticIndexException(e.getMessage());
        } catch (MalformedURLException e) {
            throw new SemanticIndexException(e.getMessage());
        } catch (IOException e) {
            throw new SemanticIndexException(e.getMessage());
        }
    }



    private static class Insert implements Runnable{
        private RDFParser rdfParser;
        private InputStream inputStreamOrReader;
        private String baseIRI;
        public Insert(RDFParser rdfParser, InputStream inputStream, String baseIRI)
        {
            this.rdfParser = rdfParser;
            this.inputStreamOrReader = inputStream;
            this.baseIRI = baseIRI;
        }
        @Override
        public void run()
        {
            try {
                rdfParser.parse(inputStreamOrReader, baseIRI);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    }

    private static class Process implements Runnable{
        private final RDF4JRDFIterator iterator;
        private final RDBMSSIRepositoryManager dataRepository;
        private final Connection localConnection;
        private final String graphIRI;

        public Process(RDF4JRDFIterator iterator, RDBMSSIRepositoryManager dataRepository,
                       Connection localConnection, String graphIRI) throws OBDAException
        {
            this.iterator = iterator;
            this.dataRepository = dataRepository;
            this.localConnection = localConnection;
            this.graphIRI = graphIRI;
        }

        @Override
        public void run()
        {
            try {
                int count = dataRepository.insertData(localConnection, iterator, 5000, 500);
                log.debug("Inserted {} triples from the graph {}", count, graphIRI);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static Ontology loadTBoxFromDataset(Dataset dataset) throws IOException {
        // Merge default and named graphs to filter duplicates
        Set<IRI> graphURIs = new HashSet<>();
        graphURIs.addAll(dataset.getDefaultGraphs());
        graphURIs.addAll(dataset.getNamedGraphs());

        OntologyVocabulary vb = ONTOLOGY_FACTORY.createVocabulary();

        for (IRI graphURI : graphURIs) {
            Ontology o = getOntology(graphURI);
            vb.merge(o.getVocabulary());

            // TODO: restore copying ontology axioms (it was copying from result into result, at least since July 2013)

            //for (SubPropertyOfAxiom ax : result.getSubPropertyAxioms())
            //	result.add(ax);
            //for (SubClassOfAxiom ax : result.getSubClassAxioms())
            //	result.add(ax);
        }
        Ontology result = ONTOLOGY_FACTORY.createOntology(vb);

        return result;
    }

    private static Ontology getOntology(IRI graphURI) throws IOException {
        RDFFormat rdfFormat = Rio.getParserFormatForFileName(graphURI.toString()).get();
        RDFParser rdfParser = Rio.createParser(rdfFormat, ValueFactoryImpl.getInstance());
        ParserConfig config = rdfParser.getParserConfig();

        // To emulate DatatypeHandling.IGNORE
        config.addNonFatalError(BasicParserSettings.FAIL_ON_UNKNOWN_DATATYPES);
        config.addNonFatalError(BasicParserSettings.VERIFY_DATATYPE_VALUES);
        config.addNonFatalError(BasicParserSettings.NORMALIZE_DATATYPE_VALUES);
//		rdfParser.setVerifyData(false);
//		rdfParser.setDatatypeHandling(DatatypeHandling.IGNORE);
//		rdfParser.setPreserveBNodeIDs(true);

        RDFTBoxReader reader = new RDFTBoxReader();
        rdfParser.setRDFHandler(reader);

        URL graphURL = new URL(graphURI.toString());
        InputStream in = graphURL.openStream();
        try {
            rdfParser.parse(in, graphURI.toString());
        } finally {
            in.close();
        }
        return reader.getOntology();
    }

    public static class RDFTBoxReader extends RDFHandlerBase {
        private OntologyFactory ofac = OntologyFactoryImpl.getInstance();
        private OntologyVocabulary vb = ofac.createVocabulary();

        public Ontology getOntology() {
            return ofac.createOntology(vb);
        }

        @Override
        public void handleStatement(Statement st) throws RDFHandlerException {
            URI pred = st.getPredicate();
            Value obj = st.getObject();
            if (obj instanceof Literal) {
                String dataProperty = pred.stringValue();
                vb.createDataProperty(dataProperty);
            }
            else if (pred.stringValue().equals(OBDAVocabulary.RDF_TYPE)) {
                String className = obj.stringValue();
                vb.createClass(className);
            }
            else {
                String objectProperty = pred.stringValue();
                vb.createObjectProperty(objectProperty);
            }

		/* Roman 10/08/15: recover?
			Axiom axiom = getTBoxAxiom(st);
			ontology.addAssertionWithCheck(axiom);
		*/
        }

    }


}
