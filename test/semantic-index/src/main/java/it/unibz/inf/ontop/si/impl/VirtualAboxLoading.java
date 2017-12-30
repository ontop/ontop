package it.unibz.inf.ontop.si.impl;

import it.unibz.inf.ontop.exception.OntopConnectionException;
import it.unibz.inf.ontop.exception.OntopQueryAnsweringException;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.spec.ontology.Assertion;
import it.unibz.inf.ontop.materialization.MaterializationParams;
import it.unibz.inf.ontop.answering.resultset.MaterializedGraphResultSet;
import it.unibz.inf.ontop.materialization.OntopRDFMaterializer;
import it.unibz.inf.ontop.si.OntopSemanticIndexLoader;
import it.unibz.inf.ontop.si.SemanticIndexException;
import it.unibz.inf.ontop.si.impl.SILoadingTools.RepositoryInit;
import it.unibz.inf.ontop.spec.ontology.owlapi.OWLAPITranslatorOWL2QL;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Properties;

import static it.unibz.inf.ontop.si.impl.SILoadingTools.*;

public class VirtualAboxLoading {

    private static final Logger LOG = LoggerFactory.getLogger(VirtualAboxLoading.class);

    /**
     * TODO: do want to use a different ontology for the materialization and the output OBDA system?
     */
    public static OntopSemanticIndexLoader loadVirtualAbox(OntopSQLOWLAPIConfiguration obdaConfiguration, Properties properties)
            throws SemanticIndexException {

        try {
            OWLOntology inputOntology = obdaConfiguration.loadInputOntology()
                    .orElseThrow(() -> new IllegalArgumentException("The configuration must provide an ontology"));

            RepositoryInit init = createRepository(inputOntology, obdaConfiguration.getAtomFactory(),
                    obdaConfiguration.getTermFactory(),
                    obdaConfiguration.getInjector().getInstance(OWLAPITranslatorOWL2QL.class),
                    obdaConfiguration.getTypeFactory());

            OntopRDFMaterializer materializer = OntopRDFMaterializer.defaultMaterializer();
            MaterializationParams materializationParams = MaterializationParams.defaultBuilder()
                    .enableDBResultsStreaming(true)
                    .build();
            try (MaterializedGraphResultSet graphResultSet = materializer.materialize(
                    obdaConfiguration, materializationParams)) {

                int count = init.dataRepository.insertData(init.localConnection,
                        new GraphResultSetIterator(graphResultSet), 5000, 500);
                LOG.debug("Inserted {} triples from the mappings.", count);
            }

            /*
            Creates the configuration and the loader object
             */
            OntopSQLOWLAPIConfiguration configuration = createConfiguration(init.dataRepository, inputOntology, init.jdbcUrl, properties);
            return new OntopSemanticIndexLoaderImpl(configuration, init.localConnection);

        } catch (Exception e) {
            throw new SemanticIndexException(e.getMessage());
        }
    }



    private static class GraphResultSetIterator implements Iterator<Assertion> {

        private final MaterializedGraphResultSet graphResultSet;

        GraphResultSetIterator(MaterializedGraphResultSet graphResultSet) {
            this.graphResultSet = graphResultSet;
        }

        @Override
        public boolean hasNext() {
            try {
                return graphResultSet.hasNext();
            } catch (OntopConnectionException | OntopQueryAnsweringException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public Assertion next() {
            try {
                return graphResultSet.next();
            } catch (OntopQueryAnsweringException e) {
                throw new RuntimeException(e);
            }
        }
    }


}
