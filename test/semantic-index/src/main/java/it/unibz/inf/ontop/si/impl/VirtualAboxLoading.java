package it.unibz.inf.ontop.si.impl;

import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.ontology.Assertion;
import it.unibz.inf.ontop.owlrefplatform.core.abox.QuestMaterializer;
import it.unibz.inf.ontop.si.OntopSemanticIndexLoader;
import it.unibz.inf.ontop.si.SemanticIndexException;
import it.unibz.inf.ontop.si.impl.SILoadingTools.RepositoryInit;
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

            RepositoryInit init = createRepository(inputOntology);

            QuestMaterializer materializer = new QuestMaterializer(obdaConfiguration, true);
            Iterator<Assertion> assertionIterator = materializer.getAssertionIterator();

            int count = init.dataRepository.insertData(init.localConnection, assertionIterator, 5000, 500);
            materializer.disconnect();
            LOG.debug("Inserted {} triples from the mappings.", count);

            /*
            Creates the configuration and the loader object
             */
            OntopSQLOWLAPIConfiguration configuration = createConfiguration(init.dataRepository, inputOntology, init.jdbcUrl, properties);
            return new OntopSemanticIndexLoaderImpl(configuration, init.localConnection);

        } catch (Exception e) {
            throw new SemanticIndexException(e.getMessage());
        }
    }
}
