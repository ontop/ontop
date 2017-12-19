package it.unibz.inf.ontop.si.impl;


import it.unibz.inf.ontop.injection.OntopModelConfiguration;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.model.IriConstants;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.si.repository.SIRepositoryManager;
import it.unibz.inf.ontop.spec.ontology.Ontology;
import it.unibz.inf.ontop.spec.ontology.OntologyFactory;
import it.unibz.inf.ontop.spec.ontology.OntologyVocabulary;
import it.unibz.inf.ontop.spec.ontology.impl.OntologyFactoryImpl;
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

    private static final OntologyFactory ONTOLOGY_FACTORY = OntologyFactoryImpl.getInstance();
    private static final Logger LOG = LoggerFactory.getLogger(GraphLoading.class);

    public static OntopSemanticIndexLoader loadRDFGraph(Dataset dataset, Properties properties) throws SemanticIndexException {
        try {
            Ontology implicitTbox =  loadTBoxFromDataset(dataset);

            OntopModelConfiguration defaultConfiguration = OntopModelConfiguration.defaultBuilder().build();
            AtomFactory atomFactory = defaultConfiguration.getAtomFactory();
            TermFactory termFactory = defaultConfiguration.getTermFactory();
            TypeFactory typeFactory = defaultConfiguration.getTypeFactory();

            RepositoryInit init = createRepository(implicitTbox, atomFactory, termFactory, typeFactory);

            /*
            Loads the data
             */
            insertDataset(init.dataRepository, init.localConnection, dataset, termFactory, typeFactory);

            /*
            Creates the configuration and the loader object
             */
            OntopSQLOWLAPIConfiguration configuration = createConfiguration(init.dataRepository, init.jdbcUrl, properties);
            return new OntopSemanticIndexLoaderImpl(configuration, init.localConnection);

        } catch (IOException e) {
            throw new SemanticIndexException(e.getMessage());
        }
    }



    private static void insertDataset(SIRepositoryManager dataRepository, Connection localConnection, Dataset dataset,
                                      TermFactory termFactory, TypeFactory typeFactory)
            throws SemanticIndexException {
        // Merge default and named graphs to filter duplicates
        Set<IRI> graphIRIs = new HashSet<>();
        graphIRIs.addAll(dataset.getDefaultGraphs());
        graphIRIs.addAll(dataset.getNamedGraphs());

        for (Resource graphIRI : graphIRIs) {
            insertGraph(dataRepository, localConnection, ((IRI)graphIRI), termFactory, typeFactory);
        }
    }

    private static void insertGraph(SIRepositoryManager dataRepository, Connection localConnection,
                                    IRI graphIRI, TermFactory termFactory, TypeFactory typeFactory) throws SemanticIndexException {

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

            SemanticIndexRDFHandler rdfHandler = new SemanticIndexRDFHandler(dataRepository, localConnection,
                    termFactory, typeFactory);
            rdfParser.setRDFHandler(rdfHandler);

            rdfParser.parse(in, graphIRI.toString());
            LOG.debug("Inserted {} triples from the graph {}", rdfHandler.getCount(), graphIRI);


        } catch (IOException e) {
            throw new SemanticIndexException(e.getMessage());
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
            else if (pred.stringValue().equals(IriConstants.RDF_TYPE)) {
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
