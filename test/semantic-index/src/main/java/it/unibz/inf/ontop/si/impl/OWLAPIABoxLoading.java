package it.unibz.inf.ontop.si.impl;


import it.unibz.inf.ontop.si.OntopSemanticIndexLoader;
import it.unibz.inf.ontop.si.SemanticIndexException;
import it.unibz.inf.ontop.si.repository.impl.SIRepository;
import it.unibz.inf.ontop.spec.ontology.ClassifiedTBox;
import it.unibz.inf.ontop.spec.ontology.Ontology;
import it.unibz.inf.ontop.spec.ontology.RDFFact;
import it.unibz.inf.ontop.spec.ontology.owlapi.OWLAPITranslatorOWL2QL;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.model.parameters.OntologyCopy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;
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
        ClassifiedTBox tbox = ontology.tbox();

        SIRepository repo = new SIRepository(ontology.tbox(), loadingConfiguration);


        try {
            Connection connection = repo.createConnection();

            // load the data
            Set<OWLOntology> ontologyClosure = owlOntology.getOWLOntologyManager().getImportsClosure(owlOntology);
            Iterator<RDFFact> aBoxIter = ontologyClosure.stream()
                    .flatMap(o -> o.getAxioms().stream())
                    .map(ax -> translate(translatorOWL2QL, tbox, ax))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .iterator();
            int count = repo.insertData(connection, aBoxIter);
            LOG.debug("Inserted {} triples from the ontology.", count);

            return new OntopSemanticIndexLoaderImpl(repo, connection, properties,
                    Optional.of(extractTBox(owlOntology)));
        }
        catch (SQLException e) {
            throw new SemanticIndexException(e.getMessage());
        }
    }

    private static Optional<RDFFact> translate(OWLAPITranslatorOWL2QL translatorOWL2QL, ClassifiedTBox tbox, OWLAxiom axiom) {

        try {
            if (axiom instanceof OWLClassAssertionAxiom)
                return Optional.of(translatorOWL2QL.translate((OWLClassAssertionAxiom)axiom, tbox.classes()));
            else if (axiom instanceof OWLObjectPropertyAssertionAxiom)
                return Optional.of(translatorOWL2QL.translate((OWLObjectPropertyAssertionAxiom)axiom, tbox.objectProperties()));
            else if (axiom instanceof OWLDataPropertyAssertionAxiom)
                return Optional.of(translatorOWL2QL.translate((OWLDataPropertyAssertionAxiom)axiom, tbox.dataProperties()));
        }
        catch (OWLAPITranslatorOWL2QL.TranslationException e) {
            return Optional.empty();
        }
        return Optional.empty();
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
