/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano This source code is
 * available under the terms of the Affero General Public License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.owlrefplatform.owlapi3;

import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;

import java.util.Properties;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.BufferingMode;
import org.semanticweb.owlapi.reasoner.IllegalConfigurationException;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 * Implementation of an OWLReasonerFactory that can create instances of Quest.
 * Note, to create an instance of Quest first you must call the method
 * {@link #setPreferenceHolder(Properties)} with your parameters see Quest.java
 * for a description of the preferences. Also, if you use Quest in Virtual ABox
 * mode you must set an {@link OBDAModel} with your mappings.
 * 
 * @see OBDAModel
 * 
 * @author Mariano Rodriguez Muro <mariano.muro@gmail.com>
 * 
 */
public class QuestOWLFactory implements OWLReasonerFactory {

	private OBDAModel mappingManager = null;
	private Properties preferences = null;
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

	public void setPreferenceHolder(Properties preference) {
		this.preferences = preference;
	}

	@Override
	public String getReasonerName() {
		return name;
	}

	@Override
	public OWLReasoner createNonBufferingReasoner(OWLOntology ontology) {
		if (mappingManager == null && !preferences.get(QuestPreferences.ABOX_MODE).equals(QuestConstants.CLASSIC)) {
			preferences.put(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
			log.warn("You didn't specified mappings, Quest will assume you want to work in 'classic ABox' mode' even though you set the ABox mode to: '"
					+ preferences.get(QuestPreferences.ABOX_MODE) + "'");
			log.warn("To avoid this warning, set the value of '" + QuestPreferences.ABOX_MODE + "' to '" + QuestConstants.CLASSIC + "'");
		} else if (mappingManager != null && !preferences.get(QuestPreferences.ABOX_MODE).equals(QuestConstants.VIRTUAL)) {
			preferences.put(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
			log.warn("You specified mappings, Quest will assume you want to work in 'virtual ABox' mode' even though you set the ABox mode to: '"
					+ preferences.get(QuestPreferences.ABOX_MODE) + "'");
			log.warn("To avoid this warning, set the value of '" + QuestPreferences.ABOX_MODE + "' to '" + QuestConstants.VIRTUAL + "'");
		}
		return new QuestOWL(ontology, mappingManager, new SimpleConfiguration(), BufferingMode.NON_BUFFERING, preferences);
	}

	@Override
	public OWLReasoner createNonBufferingReasoner(OWLOntology ontology, OWLReasonerConfiguration config)
			throws IllegalConfigurationException {
		if (mappingManager == null && !preferences.get(QuestPreferences.ABOX_MODE).equals(QuestConstants.CLASSIC)) {
			preferences.put(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
			log.warn("You didn't specified mappings, Quest will assume you want to work in 'classic ABox' mode' even though you set the ABox mode to: '"
					+ preferences.get(QuestPreferences.ABOX_MODE) + "'");
			log.warn("To avoid this warning, set the value of '" + QuestPreferences.ABOX_MODE + "' to '" + QuestConstants.CLASSIC + "'");
		} else if (mappingManager != null && !preferences.get(QuestPreferences.ABOX_MODE).equals(QuestConstants.VIRTUAL)) {
			preferences.put(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
			log.warn("You specified mappings, Quest will assume you want to work in 'virtual ABox' mode' even though you set the ABox mode to: '"
					+ preferences.get(QuestPreferences.ABOX_MODE) + "'");
			log.warn("To avoid this warning, set the value of '" + QuestPreferences.ABOX_MODE + "' to '" + QuestConstants.VIRTUAL + "'");
		}
		return new QuestOWL(ontology, mappingManager, config, BufferingMode.NON_BUFFERING, preferences);
	}

	@Override
	public OWLReasoner createReasoner(OWLOntology ontology) {
		if (mappingManager == null && !preferences.get(QuestPreferences.ABOX_MODE).equals(QuestConstants.CLASSIC)) {
			preferences.put(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
			log.warn("You didn't specified mappings, Quest will assume you want to work in 'classic ABox' mode' even though you set the ABox mode to: '"
					+ preferences.get(QuestPreferences.ABOX_MODE) + "'");
			log.warn("To avoid this warning, set the value of '" + QuestPreferences.ABOX_MODE + "' to '" + QuestConstants.CLASSIC + "'");
		} else if (mappingManager != null && !preferences.get(QuestPreferences.ABOX_MODE).equals(QuestConstants.VIRTUAL)) {
			preferences.put(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
			log.warn("You specified mappings, Quest will assume you want to work in 'virtual ABox' mode' even though you set the ABox mode to: '"
					+ preferences.get(QuestPreferences.ABOX_MODE) + "'");
			log.warn("To avoid this warning, set the value of '" + QuestPreferences.ABOX_MODE + "' to '" + QuestConstants.VIRTUAL + "'");
		}
		return new QuestOWL(ontology, mappingManager, new SimpleConfiguration(), BufferingMode.BUFFERING, preferences);
	}

	@Override
	public OWLReasoner createReasoner(OWLOntology ontology, OWLReasonerConfiguration config) throws IllegalConfigurationException {
		if (mappingManager == null && !preferences.get(QuestPreferences.ABOX_MODE).equals(QuestConstants.CLASSIC)) {
			preferences.put(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
			log.warn("You didn't specified mappings, Quest will assume you want to work in 'classic ABox' mode' even though you set the ABox mode to: '"
					+ preferences.get(QuestPreferences.ABOX_MODE) + "'");
			log.warn("To avoid this warning, set the value of '" + QuestPreferences.ABOX_MODE + "' to '" + QuestConstants.CLASSIC + "'");
		} else if (mappingManager != null && !preferences.get(QuestPreferences.ABOX_MODE).equals(QuestConstants.VIRTUAL)) {
			preferences.put(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
			log.warn("You specified mappings, Quest will assume you want to work in 'virtual ABox' mode' even though you set the ABox mode to: '"
					+ preferences.get(QuestPreferences.ABOX_MODE) + "'");
			log.warn("To avoid this warning, set the value of '" + QuestPreferences.ABOX_MODE + "' to '" + QuestConstants.VIRTUAL + "'");
		}
		return new QuestOWL(ontology, mappingManager, config, BufferingMode.BUFFERING, preferences);
	}

}
