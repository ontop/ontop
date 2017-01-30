package it.unibz.inf.ontop.si.impl;


import it.unibz.inf.ontop.injection.QuestConfiguration;
import it.unibz.inf.ontop.owlapi.OWLAPIABoxIterator;
import it.unibz.inf.ontop.si.OntopSemanticIndexLoader;
import it.unibz.inf.ontop.si.SemanticIndexException;
import it.unibz.inf.ontop.si.impl.SILoadingTools.RepositoryInit;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.SQLException;
import java.util.Properties;

import static it.unibz.inf.ontop.si.impl.SILoadingTools.*;


public class OntologyIndividualLoading {

    private static final Logger LOG = LoggerFactory.getLogger(OntologyIndividualLoading.class);

    /**
     * High-level method
     */
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

    /**
     * High-level method
     */
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
            LOG.debug("Inserted {} triples from the ontology.", count);

            /*
            Creates the configuration and the loader object
             */
            QuestConfiguration configuration = createConfiguration(init.dataRepository, owlOntology, init.jdbcUrl, properties);
            return new OntopSemanticIndexLoaderImpl(configuration, init.localConnection);

        } catch (SQLException e) {
            throw new SemanticIndexException(e.getMessage());
        }
    }
}
