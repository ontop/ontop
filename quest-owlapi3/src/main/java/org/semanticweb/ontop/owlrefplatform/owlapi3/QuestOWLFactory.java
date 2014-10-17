package org.semanticweb.ontop.owlrefplatform.owlapi3;

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

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.semanticweb.ontop.exception.InvalidMappingException;
import org.semanticweb.ontop.injection.NativeQueryLanguageComponentFactory;
import org.semanticweb.ontop.injection.OntopCoreModule;
import org.semanticweb.ontop.mapping.MappingParser;
import org.semanticweb.ontop.model.OBDAModel;
import org.semanticweb.ontop.owlrefplatform.core.QuestConstants;
import org.semanticweb.ontop.owlrefplatform.core.QuestPreferences;
import org.semanticweb.ontop.owlrefplatform.injection.QuestComponentFactory;
import org.semanticweb.ontop.owlrefplatform.injection.QuestComponentModule;
import org.semanticweb.ontop.sql.ImplicitDBConstraints;


import java.io.File;
import java.io.IOException;
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
 * TODO: update comments
 *
 * Implementation of an OWLReasonerFactory that can create instances of Quest.
 * Note, to create an instance of Quest first you must call the method
 * {@link #setPreferenceHolder(Properties)} with your parameters see Quest.java
 * for a description of the preferences. Also, if you use Quest in Virtual ABox
 * mode you must set an {@link org.semanticweb.ontop.model.OBDAModel} with your mappings.
 * 
 * @see org.semanticweb.ontop.model.OBDAModel
 * 
 * @author Mariano Rodriguez Muro <mariano.muro@gmail.com>
 * 
 */
public class QuestOWLFactory implements OWLReasonerFactory {

	private final OBDAModel obdaModel;
	private final Properties preferences;
    private final QuestComponentFactory componentFactory;

    /**
	 * The user can supply information about keys that are not in the
	 * database metadata. 
	 */
	private ImplicitDBConstraints userConstraints = null;
	private boolean applyUserConstraints = false;
	
	private String name = "Quest";

	private final Logger log = LoggerFactory.getLogger(QuestOWLFactory.class);


    /**
     * Virtual mode (because there is a file)
     * TODO: further explain
     *
     * @param mappingFile
     * @param preferences
     */
    public QuestOWLFactory(File mappingFile, Properties preferences) throws IOException, InvalidMappingException {

        Injector injector = Guice.createInjector(new OntopCoreModule(preferences), new QuestComponentModule(preferences));
        this.componentFactory = injector.getInstance(QuestComponentFactory.class);

        /**
         * OBDA model extraction (virtual mode)
         */
        if (mappingFile != null) {
            NativeQueryLanguageComponentFactory nativeQLFactory = injector.getInstance(NativeQueryLanguageComponentFactory.class);
            MappingParser mappingParser = nativeQLFactory.create(mappingFile);
            this.obdaModel = mappingParser.getOBDAModel();
        }
        else {
            this.obdaModel = null;
        }

        this.preferences = preferences;
    }

    /**
     * Classic mode (no mapping)
     *TODO: further explain
     *
     * @param preferences
     */
    public QuestOWLFactory(Properties preferences) throws IOException, InvalidMappingException {
        this(null, preferences);
    }


//	/***
//	 * Sets the mappings that will be used to create instances of Quest. If this
//	 * is not set, mappings will be null and Quest will be started in
//	 * "classic ABox" mode. If the mappings are not null, then the mode must be
//	 * "Virtual ABox" model.
//	 *
//	 * @param apic
//	 */
//    @Deprecated
//	public void setOBDAController(OBDAModel apic) {
//		this.mappingManager = apic;
//	}

	/***
	 * Sets the user-suppplied database constraints, i.e.
	 * Foreign and primary keys that are not in the databse
	 * 
	 * @param apic
	 */
	public void setImplicitDBConstraints(ImplicitDBConstraints userConstraints) {
		if(userConstraints == null)
			throw new NullPointerException();
		this.userConstraints = userConstraints;
		this.applyUserConstraints = true;
	}

//    @Deprecated
//	public void setPreferenceHolder(Properties preference) {
//		this.preferences = preference;
//	}

	@Override
	public String getReasonerName() {
		return name;
	}

	@Override
	public OWLReasoner createNonBufferingReasoner(OWLOntology ontology)  {
		if (obdaModel == null && !preferences.get(QuestPreferences.ABOX_MODE).equals(QuestConstants.CLASSIC)) {
			preferences.put(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
			log.warn("You didn't specified mappings, Quest will assume you want to work in 'classic ABox' mode' even though you set the ABox mode to: '"
					+ preferences.get(QuestPreferences.ABOX_MODE) + "'");
			log.warn("To avoid this warning, set the value of '" + QuestPreferences.ABOX_MODE + "' to '" + QuestConstants.CLASSIC + "'");
		} else if (obdaModel != null && !preferences.get(QuestPreferences.ABOX_MODE).equals(QuestConstants.VIRTUAL)) {
			preferences.put(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
			log.warn("You specified mappings, Quest will assume you want to work in 'virtual ABox' mode' even though you set the ABox mode to: '"
					+ preferences.get(QuestPreferences.ABOX_MODE) + "'");
			log.warn("To avoid this warning, set the value of '" + QuestPreferences.ABOX_MODE + "' to '" + QuestConstants.VIRTUAL + "'");
		}
        try {
            if (this.applyUserConstraints)
                return new QuestOWL(ontology, obdaModel, new SimpleConfiguration(), BufferingMode.NON_BUFFERING, preferences, userConstraints,
                        componentFactory);
            else
                return new QuestOWL(ontology, obdaModel, new SimpleConfiguration(), BufferingMode.NON_BUFFERING, preferences,
                        componentFactory);
        } catch (Exception e) {
            /**
             * Unfortunately this OWLAPI interface does not allow exception declaration.
             */
            throw new RuntimeException(e.getMessage());
        }
	}

	@Override
	public OWLReasoner createNonBufferingReasoner(OWLOntology ontology, OWLReasonerConfiguration config)
			throws IllegalConfigurationException {
		if (obdaModel == null && !preferences.get(QuestPreferences.ABOX_MODE).equals(QuestConstants.CLASSIC)) {
			preferences.put(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
			log.warn("You didn't specified mappings, Quest will assume you want to work in 'classic ABox' mode' even though you set the ABox mode to: '"
					+ preferences.get(QuestPreferences.ABOX_MODE) + "'");
			log.warn("To avoid this warning, set the value of '" + QuestPreferences.ABOX_MODE + "' to '" + QuestConstants.CLASSIC + "'");
		} else if (obdaModel != null && !preferences.get(QuestPreferences.ABOX_MODE).equals(QuestConstants.VIRTUAL)) {
			preferences.put(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
			log.warn("You specified mappings, Quest will assume you want to work in 'virtual ABox' mode' even though you set the ABox mode to: '"
					+ preferences.get(QuestPreferences.ABOX_MODE) + "'");
			log.warn("To avoid this warning, set the value of '" + QuestPreferences.ABOX_MODE + "' to '" + QuestConstants.VIRTUAL + "'");
		}
        try {
            if (this.applyUserConstraints)
                return new QuestOWL(ontology, obdaModel, config, BufferingMode.NON_BUFFERING, preferences, userConstraints,
                        componentFactory);
            else
                return new QuestOWL(ontology, obdaModel, config, BufferingMode.NON_BUFFERING, preferences, componentFactory);
        } catch (Exception e) {
            /**
             * Unfortunately this OWLAPI interface does not allow exception declaration.
             */
            throw new RuntimeException(e.getMessage());
        }
	}

	@Override
	public OWLReasoner createReasoner(OWLOntology ontology) {
		if (obdaModel == null && !preferences.get(QuestPreferences.ABOX_MODE).equals(QuestConstants.CLASSIC)) {
			preferences.put(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
			log.warn("You didn't specified mappings, Quest will assume you want to work in 'classic ABox' mode' even though you set the ABox mode to: '"
					+ preferences.get(QuestPreferences.ABOX_MODE) + "'");
			log.warn("To avoid this warning, set the value of '" + QuestPreferences.ABOX_MODE + "' to '" + QuestConstants.CLASSIC + "'");
		} else if (obdaModel != null && !preferences.get(QuestPreferences.ABOX_MODE).equals(QuestConstants.VIRTUAL)) {
			preferences.put(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
			log.warn("You specified mappings, Quest will assume you want to work in 'virtual ABox' mode' even though you set the ABox mode to: '"
					+ preferences.get(QuestPreferences.ABOX_MODE) + "'");
			log.warn("To avoid this warning, set the value of '" + QuestPreferences.ABOX_MODE + "' to '" + QuestConstants.VIRTUAL + "'");
		}
		if(this.applyUserConstraints)
			return new QuestOWL(ontology, obdaModel, new SimpleConfiguration(), BufferingMode.BUFFERING, preferences, userConstraints,
                    componentFactory);
		else
			return new QuestOWL(ontology, obdaModel, new SimpleConfiguration(), BufferingMode.BUFFERING, preferences,
                    componentFactory);
	}

	@Override
	public OWLReasoner createReasoner(OWLOntology ontology, OWLReasonerConfiguration config) throws IllegalConfigurationException {
		if (obdaModel == null && !preferences.get(QuestPreferences.ABOX_MODE).equals(QuestConstants.CLASSIC)) {
			preferences.put(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
			log.warn("You didn't specified mappings, Quest will assume you want to work in 'classic ABox' mode' even though you set the ABox mode to: '"
					+ preferences.get(QuestPreferences.ABOX_MODE) + "'");
			log.warn("To avoid this warning, set the value of '" + QuestPreferences.ABOX_MODE + "' to '" + QuestConstants.CLASSIC + "'");
		} else if (obdaModel != null && !preferences.get(QuestPreferences.ABOX_MODE).equals(QuestConstants.VIRTUAL)) {
			preferences.put(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
			log.warn("You specified mappings, Quest will assume you want to work in 'virtual ABox' mode' even though you set the ABox mode to: '"
					+ preferences.get(QuestPreferences.ABOX_MODE) + "'");
			log.warn("To avoid this warning, set the value of '" + QuestPreferences.ABOX_MODE + "' to '" + QuestConstants.VIRTUAL + "'");
		}
		if(this.applyUserConstraints)
			return new QuestOWL(ontology, obdaModel, config, BufferingMode.BUFFERING, preferences, userConstraints, componentFactory);
		else
			return new QuestOWL(ontology, obdaModel, config, BufferingMode.BUFFERING, preferences, componentFactory);
	}

}
