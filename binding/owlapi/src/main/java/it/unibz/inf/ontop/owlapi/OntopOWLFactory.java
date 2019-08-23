package it.unibz.inf.ontop.owlapi;

import it.unibz.inf.ontop.injection.OntopSystemOWLAPIConfiguration;
import it.unibz.inf.ontop.owlapi.impl.QuestOWLFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.reasoner.IllegalConfigurationException;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import javax.annotation.Nonnull;

/**
 * Ontop OWLAPI reasoner factory
 */
public interface OntopOWLFactory extends OWLReasonerFactory {

    @Nonnull
    OntopOWLReasoner createReasoner(@Nonnull OWLOntology ontology, @Nonnull OWLReasonerConfiguration config)
            throws IllegalConfigurationException;

    @Nonnull
    OntopOWLReasoner createReasoner(@Nonnull OntopSystemOWLAPIConfiguration config)
            throws IllegalConfigurationException, OWLOntologyCreationException;

    static OntopOWLFactory defaultFactory() {
        return new QuestOWLFactory();
    }
}
