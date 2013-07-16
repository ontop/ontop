package it.unibz.krdb.obda.owlrefplatform.owlapi3;

import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.owlapi3.OBDAOWLReasonerFactory;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;

import java.util.Properties;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.BufferingMode;
import org.semanticweb.owlapi.reasoner.IllegalConfigurationException;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The implementation of the factory for creating reformulation's platform
 * reasoner
 */

public class QuestOWLFactory implements OBDAOWLReasonerFactory {

	private OBDAModel mappingManager = null;
	private Properties preferences = null;
	private String id = "Quest";
	private String name = "Quest";

	private final Logger log = LoggerFactory.getLogger(QuestOWLFactory.class);

	/***
	 * Sets the mappings that will be used to create instances of Quest. If this
	 * is not set, mappings will be null and Quest will be started in
	 * "classic ABox" mode. If the mappings are not null, then the mode must be
	 * "Virtual ABox" model.
	 * 
	 * @param apic
	 */
	public void setOBDAController(OBDAModel apic) {
		this.mappingManager = apic;
	}

	@Override
	public void setPreferenceHolder(Properties preference) {
		this.preferences = preference;
	}

	public String getReasonerName() {
		return name;
	}

	/**
	 * Returns the current mapping manager.
	 * 
	 * @return the current api controller
	 */
	public OBDAModel getApiController() {
		return mappingManager;
	}

	@Override
	public OWLReasoner createNonBufferingReasoner(OWLOntology ontology) {
		if (mappingManager == null && !preferences.get(QuestPreferences.ABOX_MODE).equals(QuestConstants.CLASSIC)) {
			preferences.put(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
			log.warn("You didn't specified mappings, Quest will assume you want to work in 'classic ABox' mode' even though you set the ABox mode to: '"+preferences.get(QuestPreferences.ABOX_MODE)+"'");
			log.warn("To avoid this warning, set the value of '"+QuestPreferences.ABOX_MODE+"' to '"+QuestConstants.CLASSIC+"'");
		} else if (mappingManager != null && !preferences.get(QuestPreferences.ABOX_MODE).equals(QuestConstants.VIRTUAL)) { 
			preferences.put(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
			log.warn("You specified mappings, Quest will assume you want to work in 'virtual ABox' mode' even though you set the ABox mode to: '"+preferences.get(QuestPreferences.ABOX_MODE)+"'");
			log.warn("To avoid this warning, set the value of '"+QuestPreferences.ABOX_MODE+"' to '"+QuestConstants.VIRTUAL+"'");
		}
		return new QuestOWL(ontology, mappingManager, new SimpleConfiguration(), BufferingMode.NON_BUFFERING, preferences);
	}

	@Override
	public OWLReasoner createNonBufferingReasoner(OWLOntology ontology, OWLReasonerConfiguration config)
			throws IllegalConfigurationException {
		if (mappingManager == null && !preferences.get(QuestPreferences.ABOX_MODE).equals(QuestConstants.CLASSIC)) {
			preferences.put(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
			log.warn("You didn't specified mappings, Quest will assume you want to work in 'classic ABox' mode' even though you set the ABox mode to: '"+preferences.get(QuestPreferences.ABOX_MODE)+"'");
			log.warn("To avoid this warning, set the value of '"+QuestPreferences.ABOX_MODE+"' to '"+QuestConstants.CLASSIC+"'");
		} else if (mappingManager != null && !preferences.get(QuestPreferences.ABOX_MODE).equals(QuestConstants.VIRTUAL)) { 
			preferences.put(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
			log.warn("You specified mappings, Quest will assume you want to work in 'virtual ABox' mode' even though you set the ABox mode to: '"+preferences.get(QuestPreferences.ABOX_MODE)+"'");
			log.warn("To avoid this warning, set the value of '"+QuestPreferences.ABOX_MODE+"' to '"+QuestConstants.VIRTUAL+"'");
		}
		return new QuestOWL(ontology, mappingManager, config, BufferingMode.NON_BUFFERING, preferences);
	}

	@Override
	public OWLReasoner createReasoner(OWLOntology ontology) {
		if (mappingManager == null && !preferences.get(QuestPreferences.ABOX_MODE).equals(QuestConstants.CLASSIC)) {
			preferences.put(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
			log.warn("You didn't specified mappings, Quest will assume you want to work in 'classic ABox' mode' even though you set the ABox mode to: '"+preferences.get(QuestPreferences.ABOX_MODE)+"'");
			log.warn("To avoid this warning, set the value of '"+QuestPreferences.ABOX_MODE+"' to '"+QuestConstants.CLASSIC+"'");
		} else if (mappingManager != null && !preferences.get(QuestPreferences.ABOX_MODE).equals(QuestConstants.VIRTUAL)) { 
			preferences.put(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
			log.warn("You specified mappings, Quest will assume you want to work in 'virtual ABox' mode' even though you set the ABox mode to: '"+preferences.get(QuestPreferences.ABOX_MODE)+"'");
			log.warn("To avoid this warning, set the value of '"+QuestPreferences.ABOX_MODE+"' to '"+QuestConstants.VIRTUAL+"'");
		}
		return new QuestOWL(ontology, mappingManager, new SimpleConfiguration(), BufferingMode.BUFFERING, preferences);
	}

	@Override
	public OWLReasoner createReasoner(OWLOntology ontology, OWLReasonerConfiguration config) throws IllegalConfigurationException {
		if (mappingManager == null && !preferences.get(QuestPreferences.ABOX_MODE).equals(QuestConstants.CLASSIC)) {
			preferences.put(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
			log.warn("You didn't specified mappings, Quest will assume you want to work in 'classic ABox' mode' even though you set the ABox mode to: '"+preferences.get(QuestPreferences.ABOX_MODE)+"'");
			log.warn("To avoid this warning, set the value of '"+QuestPreferences.ABOX_MODE+"' to '"+QuestConstants.CLASSIC+"'");
		} else if (mappingManager != null && !preferences.get(QuestPreferences.ABOX_MODE).equals(QuestConstants.VIRTUAL)) { 
			preferences.put(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
			log.warn("You specified mappings, Quest will assume you want to work in 'virtual ABox' mode' even though you set the ABox mode to: '"+preferences.get(QuestPreferences.ABOX_MODE)+"'");
			log.warn("To avoid this warning, set the value of '"+QuestPreferences.ABOX_MODE+"' to '"+QuestConstants.VIRTUAL+"'");
		}
		return new QuestOWL(ontology, mappingManager, config, BufferingMode.BUFFERING, preferences);
	}

}
