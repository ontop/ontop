package it.unibz.inf.ontop.si.impl;


import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.model.IriConstants;
import it.unibz.inf.ontop.si.repository.SIRepositoryManager;
import it.unibz.inf.ontop.spec.ontology.OntologyBuilder;
import it.unibz.inf.ontop.spec.ontology.impl.OntologyBuilderImpl;
import it.unibz.inf.ontop.rdf4j.rio.helpers.SemanticIndexRDFHandler;
import it.unibz.inf.ontop.si.OntopSemanticIndexLoader;
import it.unibz.inf.ontop.si.SemanticIndexException;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.ValueFactoryImpl;
import org.eclipse.rdf4j.query.Dataset;
import org.eclipse.rdf4j.rio.*;
import org.eclipse.rdf4j.rio.helpers.BasicParserSettings;
import org.eclipse.rdf4j.rio.helpers.RDFHandlerBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import static it.unibz.inf.ontop.si.impl.SILoadingTools.*;

public class GraphLoading {

    private static final Logger LOG = LoggerFactory.getLogger(GraphLoading.class);

    public static OntopSemanticIndexLoader loadRDFGraph(Dataset dataset, Properties properties) throws SemanticIndexException {
        try {
            OntologyBuilder implicitTbox = loadTBoxFromDataset(dataset);
            RepositoryInit init = createRepository(implicitTbox);

            /*
            Loads the data
             */
            insertDataset(init.dataRepository, init.localConnection, dataset);

            /*
            Creates the configuration and the loader object
             */
            OntopSQLOWLAPIConfiguration configuration = createConfiguration(init.dataRepository, init.jdbcUrl, properties);
            return new OntopSemanticIndexLoaderImpl(configuration, init.localConnection);
        }
        catch (IOException e) {
            throw new SemanticIndexException(e.getMessage());
        }
    }



    private static void insertDataset(SIRepositoryManager dataRepository, Connection localConnection, Dataset dataset)
            throws SemanticIndexException {
        // Merge default and named graphs to filter duplicates
        Set<IRI> graphIRIs = new HashSet<>();
        graphIRIs.addAll(dataset.getDefaultGraphs());
        graphIRIs.addAll(dataset.getNamedGraphs());

        for (Resource graphIRI : graphIRIs) {
            insertGraph(dataRepository, localConnection, ((IRI)graphIRI));
        }
    }

    private static void insertGraph(SIRepositoryManager dataRepository, Connection localConnection,
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

            SemanticIndexRDFHandler rdfHandler = new SemanticIndexRDFHandler(dataRepository, localConnection);
            rdfParser.setRDFHandler(rdfHandler);

            rdfParser.parse(in, graphIRI.toString());
            LOG.debug("Inserted {} triples from the graph {}", rdfHandler.getCount(), graphIRI);
        }
        catch (IOException e) {
            throw new SemanticIndexException(e.getMessage());
        }
    }

    private static OntologyBuilder loadTBoxFromDataset(Dataset dataset) throws IOException {
        // Merge default and named graphs to filter duplicates
        Set<IRI> graphURIs = new HashSet<>();
        graphURIs.addAll(dataset.getDefaultGraphs());
        graphURIs.addAll(dataset.getNamedGraphs());

        OntologyBuilder vb = OntologyBuilderImpl.builder();
        for (IRI graphURI : graphURIs) {
            collectOntologyVocabulary(graphURI, vb);
        }
        return vb;
    }

    private static void collectOntologyVocabulary(IRI graphURI, OntologyBuilder vb) throws IOException {
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

        rdfParser.setRDFHandler(new RDFHandlerBase() {
            @Override
            public void handleStatement(Statement st) throws RDFHandlerException {
                URI pred = st.getPredicate();
                Value obj = st.getObject();
                if (pred.stringValue().equals(IriConstants.RDF_TYPE)) {
                    vb.declareClass(obj.stringValue());
                }
                else if (obj instanceof Literal) {
                    vb.declareDataProperty(pred.stringValue());
                }
                else {
                    vb.declareObjectProperty(pred.stringValue());
                }
            }
        });

        URL graphURL = new URL(graphURI.toString());
        InputStream in = graphURL.openStream();
        try {
            rdfParser.parse(in, graphURI.toString());
        }
        finally {
            in.close();
        }
    }
}
