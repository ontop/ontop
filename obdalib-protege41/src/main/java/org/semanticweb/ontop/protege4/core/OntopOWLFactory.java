package org.semanticweb.ontop.protege4.core;

import javax.swing.JOptionPane;

import org.semanticweb.ontop.exception.DuplicateMappingException;
import org.semanticweb.ontop.exception.InvalidMappingException;
import org.semanticweb.ontop.io.InvalidDataSourceException;
import org.semanticweb.ontop.owlrefplatform.core.QuestPreferences;
import org.semanticweb.ontop.owlrefplatform.owlapi3.QuestOWLFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.IllegalConfigurationException;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;

import java.io.File;
import java.io.IOException;


/**
 * Wrapper around QuestOWLFactory for use in the ontop Protege plugin
 * 
 * Used to provide an error message to the user whenever there is an exception during reasoner initialization
 * @author dagc
 *
 */
public class OntopOWLFactory extends QuestOWLFactory {

    public OntopOWLFactory(File mappingFile, QuestPreferences preferences)
            throws IOException, InvalidMappingException, InvalidDataSourceException, DuplicateMappingException {
        super(mappingFile, preferences);
    }

	
	private void handleError(Exception e){
		String message = "Error during reasoner initialization: " + e;
		JOptionPane.showMessageDialog(null, message, "Ontop Initialization Error", JOptionPane.ERROR_MESSAGE);
	}
			
	@Override
	public OWLReasoner createReasoner(OWLOntology ontology, OWLReasonerConfiguration config) throws IllegalConfigurationException {
		try { 
			return super.createReasoner(ontology, config);
		} catch (Exception e){
			handleError(e);
			throw e;
		}
	}
	@Override
	public OWLReasoner createReasoner(OWLOntology ontology) throws IllegalConfigurationException {
		try { 
			return super.createReasoner(ontology);
		} catch (Exception e){
			handleError(e);
			throw e;
		}
	}
	
	@Override
	public OWLReasoner createNonBufferingReasoner(OWLOntology ontology, OWLReasonerConfiguration config) throws IllegalConfigurationException {
		try { 
			return super.createNonBufferingReasoner(ontology, config);
		} catch (Exception e){
			handleError(e);
			throw e;
		}
	}
	@Override
	public OWLReasoner createNonBufferingReasoner(OWLOntology ontology) throws IllegalConfigurationException {
		try { 
			return super.createNonBufferingReasoner(ontology);
		} catch (Exception e){
			handleError(e);
			throw e;
		}
	}

}
