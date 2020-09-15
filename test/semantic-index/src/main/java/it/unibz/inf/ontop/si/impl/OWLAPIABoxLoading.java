package it.unibz.inf.ontop.si.impl;


import it.unibz.inf.ontop.owlapi.utils.OWLAPIABoxIterator;
import it.unibz.inf.ontop.si.OntopSemanticIndexLoader;
import it.unibz.inf.ontop.si.SemanticIndexException;
import it.unibz.inf.ontop.si.repository.impl.SIRepository;
import it.unibz.inf.ontop.spec.ontology.Ontology;
import it.unibz.inf.ontop.spec.ontology.owlapi.OWLAPITranslatorOWL2QL;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.model.parameters.OntologyCopy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;


public class OWLAPIABoxLoading {

    private static final Logger LOG = LoggerFactory.getLogger(OWLAPIABoxLoading.class);

    /**
     * High-level method
     */
    public static OntopSemanticIndexLoader loadOntologyIndividuals(OWLOntology owlOntology, Properties properties)
            throws SemanticIndexException {

        LoadingConfiguration loadingConfiguration = new LoadingConfiguration();
        OWLAPITranslatorOWL2QL translatorOWL2QL = loadingConfiguration.getTranslatorOWL2QL();

        Ontology ontology = translatorOWL2QL.translateAndClassify(owlOntology);

        SIRepository repo = new SIRepository(ontology.tbox(), loadingConfiguration);

        try {
            Connection connection = repo.createConnection();

            // load the data
            Set<OWLOntology> ontologyClosure = owlOntology.getOWLOntologyManager().getImportsClosure(owlOntology);
            OWLAPIABoxIterator aBoxIter = new OWLAPIABoxIterator(ontologyClosure, ontology.tbox(), translatorOWL2QL);
            int count = repo.insertData(connection, aBoxIter);
            LOG.debug("Inserted {} triples from the ontology.", count);

            return new OntopSemanticIndexLoaderImpl(repo, connection, properties,
                    Optional.of(extractTBox(owlOntology)));
        }
        catch (SQLException e) {
            throw new SemanticIndexException(e.getMessage());
        }
    }

    public static OWLOntology extractTBox(OWLOntology ontology) throws SemanticIndexException {
        //Tbox: ontology without the ABox axioms (are in the DB now).
        try {
            OWLOntologyManager newManager = OWLManager.createOWLOntologyManager();
            // TODO: there is a problem here
            // removing ABox from the current ontology does not remove it from the closure
            // so, the ABox assertions of the closure will remain
            OWLOntology tbox = newManager.copyOntology(ontology, OntologyCopy.SHALLOW);
            newManager.removeAxioms(tbox, tbox.getABoxAxioms(Imports.EXCLUDED));
            return  tbox;
        }
        catch (OWLOntologyCreationException e) {
            throw new SemanticIndexException(e.getMessage());
        }
    }
}
