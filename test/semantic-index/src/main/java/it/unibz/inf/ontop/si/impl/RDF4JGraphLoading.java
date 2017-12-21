package it.unibz.inf.ontop.si.impl;


import it.unibz.inf.ontop.model.IriConstants;
import it.unibz.inf.ontop.model.term.ObjectConstant;
import it.unibz.inf.ontop.model.term.ValueConstant;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.si.repository.SIRepositoryManager;
import it.unibz.inf.ontop.spec.ontology.*;
import it.unibz.inf.ontop.spec.ontology.impl.OntologyBuilderImpl;
import it.unibz.inf.ontop.si.OntopSemanticIndexLoader;
import it.unibz.inf.ontop.si.SemanticIndexException;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.query.Dataset;
import org.eclipse.rdf4j.rio.*;
import org.eclipse.rdf4j.rio.helpers.AbstractRDFHandler;
import org.eclipse.rdf4j.rio.helpers.BasicParserSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.util.*;

import static it.unibz.inf.ontop.model.OntopModelSingletons.TERM_FACTORY;
import static it.unibz.inf.ontop.model.OntopModelSingletons.TYPE_FACTORY;
import static it.unibz.inf.ontop.si.impl.SILoadingTools.*;

public class RDF4JGraphLoading {

    private static final Logger LOG = LoggerFactory.getLogger(RDF4JGraphLoading.class);

    public static OntopSemanticIndexLoader loadRDFGraph(Dataset dataset, Properties properties) throws SemanticIndexException {
        // Merge default and named graphs to filter duplicates
        Set<IRI> graphURLs = new HashSet<>();
        graphURLs.addAll(dataset.getDefaultGraphs());
        graphURLs.addAll(dataset.getNamedGraphs());

        CollectRDFVocabulary collectVocabulary = new CollectRDFVocabulary();
        for (IRI graphURL : graphURLs) {
            processRDF(collectVocabulary, graphURL);
        }
        Ontology implicitTbox = collectVocabulary.vb.build();
        RepositoryInit init = createRepository(implicitTbox.tbox());

        //  Load the data
        SemanticIndexRDFHandler insertData = new SemanticIndexRDFHandler(init.dataRepository, init.localConnection);
        for (IRI graphURL : graphURLs) {
            processRDF(insertData, graphURL);
        }
        LOG.info("Inserted {} triples", insertData.count);

        return new OntopSemanticIndexLoaderImpl(init, properties);
    }


    private static final class CollectRDFVocabulary extends AbstractRDFHandler {
        private final OntologyBuilder vb;

        CollectRDFVocabulary() {
            this.vb = OntologyBuilderImpl.builder();
        }

        @Override
        public void handleStatement(Statement st) throws RDFHandlerException {
            String pred = st.getPredicate().stringValue();
            Value obj = st.getObject();
            if (pred.equals(IriConstants.RDF_TYPE)) {
                vb.declareClass(obj.stringValue());
            }
            else if (obj instanceof Literal) {
                vb.declareDataProperty(pred);
            }
            else {
                vb.declareObjectProperty(pred);
            }
        }
    }

    private static final class SemanticIndexRDFHandler extends AbstractRDFHandler {

        private final SIRepositoryManager repositoryManager;
        private final Connection connection;
        private final ABoxAssertionSupplier builder;

        private static final int MAX_BUFFER_SIZE = 5000;

        private List<Statement> buffer;
        private int count;

        public SemanticIndexRDFHandler(SIRepositoryManager repositoryManager, Connection connection) {
            this.repositoryManager = repositoryManager;
            this.builder = OntologyBuilderImpl.assertionSupplier();
            this.connection = connection;
            this.buffer = new ArrayList<>(MAX_BUFFER_SIZE);
            this.count = 0;
        }

        @Override
        public void endRDF() throws RDFHandlerException {
            loadBuffer();
        }

        @Override
        public void handleStatement(Statement st) throws RDFHandlerException {
            // Add statement to buffer
            buffer.add(st);
            if (buffer.size() == MAX_BUFFER_SIZE) {
                loadBuffer();
            }
        }

        private void loadBuffer() throws RDFHandlerException {
            try {
                Iterator<Assertion> assertionIterator = buffer.stream()
                        .map(st -> constructAssertion(st, builder))
                        .iterator();
                count += repositoryManager.insertData(connection, assertionIterator, 5000, 500);
                buffer.clear();
            }
            catch (Exception e) {
                throw new RDFHandlerException(e);
            }
        }
    }


    /***
     * Constructs an ABox assertion with the data from the current result set.
     * This can be a Class, Object or Data Property assertion. It is a class
     * assertion if the predicate is rdf:type. Its an Object property if the
     * predicate is not type and the object is URI or BNode. Its a data property
     * if the predicate is not rdf:type and the object is a Literal.
     */
    private static Assertion constructAssertion(Statement st, ABoxAssertionSupplier builder) {

        Resource subject = st.getSubject();
        final ObjectConstant c;
        if (subject instanceof IRI) {
            c = TERM_FACTORY.getConstantURI(subject.stringValue());
        }
        else if (subject instanceof BNode) {
            c = TERM_FACTORY.getConstantBNode(subject.stringValue());
        }
        else {
            throw new RuntimeException("Unsupported subject found in triple: "	+ st + " (Required URI or BNode)");
        }

        String predicateName = st.getPredicate().stringValue();
        Value object = st.getObject();

        // Create the assertion
        try {
            if (predicateName.equals(IriConstants.RDF_TYPE)) {
                return builder.createClassAssertion(object.stringValue(), c);
            }
            else if (object instanceof IRI) {
                ObjectConstant c2 = TERM_FACTORY.getConstantURI(object.stringValue());
                return builder.createObjectPropertyAssertion(predicateName, c, c2);
            }
            else if (object instanceof BNode) {
                ObjectConstant c2 = TERM_FACTORY.getConstantBNode(object.stringValue());
                return builder.createObjectPropertyAssertion(predicateName, c, c2);
            }
            else if (object instanceof Literal) {
                Literal l = (Literal) object;
                Optional<String> lang = l.getLanguage();
                final ValueConstant c2;
                if (!lang.isPresent()) {
                    IRI datatype = l.getDatatype();
                    Predicate.COL_TYPE type;
                    if (datatype == null) {
                        type = Predicate.COL_TYPE.LITERAL;
                    }
                    else {
                        type = TYPE_FACTORY.getDatatype(datatype);
                        if (type == null)
                            type = Predicate.COL_TYPE.UNSUPPORTED;
                    }
                    c2 = TERM_FACTORY.getConstantLiteral(l.getLabel(), type);
                }
                else {
                    c2 = TERM_FACTORY.getConstantLiteral(l.getLabel(), lang.get());
                }
                return builder.createDataPropertyAssertion(predicateName, c, c2);
            }
            throw new RuntimeException("Unsupported object found in triple: " + st + " (Required URI, BNode or Literal)");
        }
        catch (InconsistentOntologyException e) {
            throw new RuntimeException("InconsistentOntologyException: " + st);
        }
    }


    private static void processRDF(AbstractRDFHandler rdfHandler,  IRI graphURL) throws SemanticIndexException {

        RDFFormat rdfFormat = Rio.getParserFormatForFileName(graphURL.toString()).get();
        RDFParser rdfParser = Rio.createParser(rdfFormat);

        ParserConfig config = rdfParser.getParserConfig();
        // To emulate DatatypeHandling.IGNORE
        config.addNonFatalError(BasicParserSettings.FAIL_ON_UNKNOWN_DATATYPES);
        config.addNonFatalError(BasicParserSettings.VERIFY_DATATYPE_VALUES);
        config.addNonFatalError(BasicParserSettings.NORMALIZE_DATATYPE_VALUES);

        rdfParser.setRDFHandler(rdfHandler);

        InputStream in = null;
        try {
            URL url = new URL(graphURL.toString());
            in = url.openStream();
            rdfParser.parse(in, graphURL.toString());
        }
        catch (IOException e) {
            throw new SemanticIndexException(e.getMessage());
        }
        finally {
            try {
                if (in != null)
                    in.close();
            }
            catch (IOException e) {
                throw new SemanticIndexException(e.getMessage());
            }
        }
    }
}
