package it.unibz.krdb.obda.owlrefplatform.owlapi3;

/*
 * #%L
 * ontop-quest-owlapi3
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;
import it.unibz.krdb.obda.owlrefplatform.core.mappingprocessing.TMappingExclusionConfig;
import it.unibz.krdb.sql.ImplicitDBConstraints;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

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
	
	/**
	 * The user can supply information about keys that are not in the
	 * database metadata. 
	 */
	private ImplicitDBConstraints userConstraints = null;
	private boolean applyUserConstraints = false;
	
	/**
	 * @author Davide>
	 * 
	 * The user can specify predicates for which the T-Mappings 
	 * algorithm should not do its job. This is useful in those
	 * cases where we know that a concept is already "complete", 
	 * and where adding individuals from mapping assertions 
	 * identified by the TMapping algorithm would NOT add any
	 * new individual.
	 */
	private TMappingExclusionConfig excludeFromTMappings = TMappingExclusionConfig.empty();
	
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

	/***
	 * Sets the user-supplied database constraints, i.e.
	 * Foreign and primary keys that are not in the database
	 * 
	 * @param userConstraints
	 */
	public void setImplicitDBConstraints(ImplicitDBConstraints userConstraints) {
		if(userConstraints == null)
			throw new NullPointerException();
		this.userConstraints = userConstraints;
		this.applyUserConstraints = true;
	}
	
	public void setPreferenceHolder(Properties preference) {
		this.preferences = preference;
	}

	/**
	 * @author Davide>
	 * 
	 * The user can specify predicates for which the T-Mappings 
	 * algorithm should not do its job. This is useful in those
	 * cases where we know that a concept is already "complete", 
	 * that means where adding individuals from mapping assertions 
	 * identified by the TMapping algorithm would NOT add any
	 * new individual in the concept.
	 */
	public void setExcludeFromTMappingsPredicates(TMappingExclusionConfig excludeFromTMappings){
		
		if( excludeFromTMappings == null ) throw new NullPointerException(); 
		
		this.excludeFromTMappings = excludeFromTMappings;
	}
	
	@Override
	public String getReasonerName() {
		return name;
	}

	@Override
	public QuestOWL createNonBufferingReasoner(OWLOntology ontology) {
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
		if(this.applyUserConstraints){
				return new QuestOWL(ontology, mappingManager, new SimpleConfiguration(), BufferingMode.NON_BUFFERING, preferences, userConstraints, excludeFromTMappings);
		}
		else{
				return new QuestOWL(ontology, mappingManager, new SimpleConfiguration(), BufferingMode.NON_BUFFERING, preferences, excludeFromTMappings);
		}
		
		
	}

	@Override
	public QuestOWL createNonBufferingReasoner(OWLOntology ontology, OWLReasonerConfiguration config)
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
		if(this.applyUserConstraints){
			//if( this.applyExcludeFromTMappings )
				return new QuestOWL(ontology, mappingManager, config, BufferingMode.NON_BUFFERING, preferences, userConstraints, excludeFromTMappings);
			//else
		    //		return new QuestOWL(ontology, mappingManager, config, BufferingMode.NON_BUFFERING, preferences, userConstraints);
		}
		else{
			//if( this.applyExcludeFromTMappings )
				return new QuestOWL(ontology, mappingManager, config, BufferingMode.NON_BUFFERING, preferences, excludeFromTMappings);
			//else
			//	return new QuestOWL(ontology, mappingManager, config, BufferingMode.NON_BUFFERING, preferences);
		}
	}
	
	@Override
	public QuestOWL createReasoner(OWLOntology ontology) {
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
		if(this.applyUserConstraints){
			//if( this.applyExcludeFromTMappings )
				return new QuestOWL(ontology, mappingManager, new SimpleConfiguration(), BufferingMode.BUFFERING, preferences, userConstraints, excludeFromTMappings);
			//else
			//	return new QuestOWL(ontology, mappingManager, new SimpleConfiguration(), BufferingMode.BUFFERING, preferences, userConstraints);
		}
		else{
			//if( this.applyExcludeFromTMappings )
				return new QuestOWL(ontology, mappingManager, new SimpleConfiguration(), BufferingMode.BUFFERING, preferences, excludeFromTMappings);
			//else
			//	return new QuestOWL(ontology, mappingManager, new SimpleConfiguration(), BufferingMode.BUFFERING, preferences);
		}
		
	}

	@Override
	public QuestOWL createReasoner(OWLOntology ontology, OWLReasonerConfiguration config) throws IllegalConfigurationException {
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
		if(this.applyUserConstraints){
			//if( this.applyExcludeFromTMappings )
				return  new QuestOWL(ontology, mappingManager, config, BufferingMode.BUFFERING, preferences, userConstraints, excludeFromTMappings);
			//else
			//	return  new QuestOWL(ontology, mappingManager, config, BufferingMode.BUFFERING, preferences, userConstraints);
		}
		else{
			//if( this.applyExcludeFromTMappings )
				return new QuestOWL(ontology, mappingManager, config, BufferingMode.BUFFERING, preferences, excludeFromTMappings);
			//else
			//	return new QuestOWL(ontology, mappingManager, config, BufferingMode.BUFFERING, preferences);
		}
	}

}
