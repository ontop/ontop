package it.unibz.inf.ontop.si.impl;


import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.owlapi.utils.OWLAPIABoxIterator;
import it.unibz.inf.ontop.si.OntopSemanticIndexLoader;
import it.unibz.inf.ontop.si.SemanticIndexException;
import it.unibz.inf.ontop.spec.ontology.Ontology;
import it.unibz.inf.ontop.spec.ontology.owlapi.OWLAPITranslatorOWL2QL;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Properties;
import java.util.Set;

import static it.unibz.inf.ontop.si.impl.SILoadingTools.*;


public class OntologyIndividualLoading {

    private static final Logger LOG = LoggerFactory.getLogger(OntologyIndividualLoading.class);

    /**
     * High-level method
     */
    public static OntopSemanticIndexLoader loadOntologyIndividuals(OWLOntology owlOntology, Properties properties)
            throws SemanticIndexException {

        Set<OWLOntology> ontologyClosure = owlOntology.getOWLOntologyManager().getImportsClosure(owlOntology);
        Ontology ontology = OWLAPITranslatorOWL2QL.translateAndClassify(ontologyClosure);

        RepositoryInit init = createRepository(ontology.tbox());

        try {
            /*
            Loads the data
             */
            OWLAPIABoxIterator aBoxIter = new OWLAPIABoxIterator(ontologyClosure, init.dataRepository.getClassifiedTBox());

            int count = init.dataRepository.insertData(init.localConnection, aBoxIter, 5000, 500);
            LOG.debug("Inserted {} triples from the ontology.", count);

            /*
            Creates the configuration and the loader object
             */
            return new OntopSemanticIndexLoaderImpl(init, properties, owlOntology);
        }
        catch (SQLException e) {
            throw new SemanticIndexException(e.getMessage());
        }
    }
}
