package org.semanticweb.ontop.protege.core;

import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWL;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.IllegalConfigurationException;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;

import javax.annotation.Nonnull;
import javax.swing.*;

/**
 * Wrapper around QuestOWLFactory for use in the ontop Protege plugin
 * 
 * Used to provide an error message to the user whenever there is an exception during reasoner initialization
 * @author dagc
 *
 */
public class OntopOWLFactory extends QuestOWLFactory {

	
	private void handleError(Exception e){
		String message = "Error during reasoner initialization: " + e;
		JOptionPane.showMessageDialog(null, message, "Ontop Initialization Error", JOptionPane.ERROR_MESSAGE);
	}
			
	@Nonnull
    @Override
	public QuestOWL createReasoner(@Nonnull OWLOntology ontology, @Nonnull OWLReasonerConfiguration config) throws IllegalConfigurationException {
		try { 
			return super.createReasoner(ontology, config);
		} catch (Exception e){
			handleError(e);
			throw e;
		}
	}
	@Nonnull
    @Override
	public QuestOWL createReasoner(OWLOntology ontology) throws IllegalConfigurationException {
		try { 
			return super.createReasoner(ontology);
		} catch (Exception e){
			handleError(e);
			throw e;
		}
	}
	
	@Nonnull
    @Override
	public QuestOWL createNonBufferingReasoner(OWLOntology ontology, @Nonnull OWLReasonerConfiguration config) throws IllegalConfigurationException {
		try { 
			return super.createNonBufferingReasoner(ontology, config);
		} catch (Exception e){
			handleError(e);
			throw e;
		}
	}
	@Nonnull
    @Override
	public QuestOWL createNonBufferingReasoner(@Nonnull OWLOntology ontology) throws IllegalConfigurationException {
		try { 
			return super.createNonBufferingReasoner(ontology);
		} catch (Exception e){
			handleError(e);
			throw e;
		}
	}

}
