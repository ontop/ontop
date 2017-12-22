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
import it.unibz.inf.ontop.spec.ontology.Ontology;
import it.unibz.inf.ontop.spec.ontology.owlapi.OWLAPITranslatorOWL2QL;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.util.Iterator;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

import static it.unibz.inf.ontop.si.impl.OntologyIndividualLoading.extractTBox;

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

            Set<OWLOntology> ontologyClosure = inputOntology.getOWLOntologyManager().getImportsClosure(inputOntology);
            Ontology ontology = OWLAPITranslatorOWL2QL.translateAndClassify(ontologyClosure);
            SIRepository repo = new SIRepository(ontology.tbox());

            OntopRDFMaterializer materializer = OntopRDFMaterializer.defaultMaterializer();
            MaterializationParams materializationParams = MaterializationParams.defaultBuilder()
                    .enableDBResultsStreaming(true)
                    .build();
            try (MaterializedGraphResultSet graphResultSet = materializer.materialize(
                    obdaConfiguration, materializationParams)) {

                Connection connection = repo.createConnection();
                int count = repo.getDataRepository().insertData(connection,
                        new Iterator<Assertion>() {
                            @Override
                            public boolean hasNext() {
                                try {
                                    return graphResultSet.hasNext();
                                }
                                catch (OntopConnectionException | OntopQueryAnsweringException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                            @Override
                            public Assertion next() {
                                try {
                                    return graphResultSet.next();
                                }
                                catch (OntopQueryAnsweringException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }, 5000, 500);
                LOG.debug("Inserted {} triples from the mappings.", count);
                return new OntopSemanticIndexLoaderImpl(repo, connection, properties,
                        // TODO: use ontologyClosure?
                        Optional.of(extractTBox(inputOntology)));
            }
        }
        catch (Exception e) {
            throw new SemanticIndexException(e.getMessage());
        }
    }
}
