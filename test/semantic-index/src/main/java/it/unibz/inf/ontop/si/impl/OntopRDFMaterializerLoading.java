package it.unibz.inf.ontop.si.impl;

import it.unibz.inf.ontop.exception.OntopConnectionException;
import it.unibz.inf.ontop.exception.OntopQueryAnsweringException;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.model.atom.TargetAtomFactory;
import it.unibz.inf.ontop.si.repository.impl.SIRepository;
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

import static it.unibz.inf.ontop.si.impl.OWLAPIABoxLoading.extractTBox;

public class OntopRDFMaterializerLoading {

    private static final Logger LOG = LoggerFactory.getLogger(OntopRDFMaterializerLoading.class);

    /**
     * TODO: do want to use a different ontology for the materialization and the output OBDA system?
     */
    public static OntopSemanticIndexLoader loadVirtualAbox(OntopSQLOWLAPIConfiguration obdaConfiguration, Properties properties)
            throws SemanticIndexException {

        try {
            OWLOntology inputOntology = obdaConfiguration.loadInputOntology()
                    .orElseThrow(() -> new IllegalArgumentException("The configuration must provide an ontology"));

            OWLAPITranslatorOWL2QL translatorOWL2QL = obdaConfiguration.getInjector().getInstance(OWLAPITranslatorOWL2QL.class);

            Ontology ontology = translatorOWL2QL.translateAndClassify(inputOntology);
            SIRepository repo = new SIRepository(ontology.tbox(),
                    obdaConfiguration.getTermFactory(), obdaConfiguration.getTypeFactory(),
                    obdaConfiguration.getInjector().getInstance(TargetAtomFactory.class));

            MaterializationParams materializationParams = MaterializationParams.defaultBuilder()
                    .enableDBResultsStreaming(true)
                    .build();
            OntopRDFMaterializer materializer = OntopRDFMaterializer.defaultMaterializer(obdaConfiguration, materializationParams);
            try (MaterializedGraphResultSet graphResultSet = materializer.materialize()) {

                Connection connection = repo.createConnection();
                int count = repo.insertData(connection,
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
                        });
                LOG.debug("Inserted {} triples from the mappings.", count);
                return new OntopSemanticIndexLoaderImpl(repo, connection, properties,
                        Optional.of(extractTBox(inputOntology)));
            }
        }
        catch (Exception e) {
            throw new SemanticIndexException(e.getMessage());
        }
    }
}
