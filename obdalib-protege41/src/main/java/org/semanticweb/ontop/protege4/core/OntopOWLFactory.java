package org.semanticweb.ontop.protege4.core;

import javax.swing.JOptionPane;

import org.semanticweb.ontop.exception.DuplicateMappingException;
import org.semanticweb.ontop.exception.InvalidMappingException;
import org.semanticweb.ontop.io.InvalidDataSourceException;
import org.semanticweb.ontop.io.OntopNativeMappingSerializer;
import org.semanticweb.ontop.model.OBDAModel;
import org.semanticweb.ontop.owlrefplatform.core.QuestPreferences;
import org.semanticweb.ontop.owlrefplatform.owlapi3.QuestOWL;
import org.semanticweb.ontop.owlrefplatform.owlapi3.QuestOWLFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.IllegalConfigurationException;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;

import java.io.*;


/**
 * Wrapper around QuestOWLFactory for use in the ontop Protege plugin
 * 
 * Used to provide an error message to the user whenever there is an exception during reasoner initialization
 * @author dagc
 *
 */
public class OntopOWLFactory extends QuestOWLFactory {

    public OntopOWLFactory(OBDAModel obdaModel, QuestPreferences preferences)
            throws IOException, InvalidMappingException, InvalidDataSourceException, DuplicateMappingException {
        super(preferences, convertOBDAModelToReader(obdaModel));
    }

	
	public static void handleError(Exception e){
		String message = "Error during reasoner initialization: " + e;
		JOptionPane.showMessageDialog(null, message, "Ontop Initialization Error", JOptionPane.ERROR_MESSAGE);
	}
			
	@Override
	public QuestOWL createReasoner(OWLOntology ontology, OWLReasonerConfiguration config) throws IllegalConfigurationException {
		try { 
			return super.createReasoner(ontology, config);
		} catch (Exception e){
			handleError(e);
			throw e;
		}
	}
	@Override
	public QuestOWL createReasoner(OWLOntology ontology) throws IllegalConfigurationException {
		try { 
			return super.createReasoner(ontology);
		} catch (Exception e){
			handleError(e);
			throw e;
		}
	}
	
	@Override
	public QuestOWL createNonBufferingReasoner(OWLOntology ontology, OWLReasonerConfiguration config) throws IllegalConfigurationException {
		try { 
			return super.createNonBufferingReasoner(ontology, config);
		} catch (Exception e){
			handleError(e);
			throw e;
		}
	}
	@Override
	public QuestOWL createNonBufferingReasoner(OWLOntology ontology) throws IllegalConfigurationException {
		try { 
			return super.createNonBufferingReasoner(ontology);
		} catch (Exception e){
			handleError(e);
			throw e;
		}
	}

    @Override
    public void reload(QuestPreferences preferences) {
        try {
            super.reload(preferences);
        } catch (Exception e) {
            handleError(e);
            //TODO: see if we should return an exception
        }
    }

    @Override
    public void reload(File mappingFile, QuestPreferences preferences) {
        try {
            super.reload(mappingFile, preferences);
        } catch (Exception e) {
            handleError(e);
            //TODO: see if we should return an exception
        }
    }

    @Override
    public void reload(Reader mappingReader, QuestPreferences preferences) {
        try {
            super.reload(mappingReader, preferences);
        } catch (Exception e) {
            handleError(e);
            //TODO: see if we should return an exception
        }
    }

    public void reload(OBDAModel currentModel, QuestPreferences preferences) {
        try {
            Reader mappingReader = convertOBDAModelToReader(currentModel);
            super.reload(mappingReader, preferences);
        } catch (Exception e) {
            handleError(e);
            //TODO: see if we should return an exception
        }
    }

    /**
     * TODO: make it generic (not OntopNativeMapping-specific).
     *
     */
    private static Reader convertOBDAModelToReader(OBDAModel currentModel) throws IOException {
        OntopNativeMappingSerializer mappingSerializer = new OntopNativeMappingSerializer(currentModel);
        StringWriter stringWriter = new StringWriter();

        mappingSerializer.save(stringWriter);
        String mappingDump = stringWriter.toString();

        return new StringReader(mappingDump);
    }
}
