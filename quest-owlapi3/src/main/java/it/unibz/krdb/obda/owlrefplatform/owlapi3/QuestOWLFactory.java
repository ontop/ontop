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
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.IllegalConfigurationException;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Properties;

import static com.google.common.base.Preconditions.checkArgument;

/***
 * TODO: rewrite the doc
 * <p>
 * Implementation of an OWLReasonerFactory that can create instances of Quest.
 * Note, to create an instance of Quest first you must call the method
 * {@code #setPreferenceHolder(Properties)} with your parameters see Quest.java
 * for a description of the preferences. Also, if you use Quest in Virtual ABox
 * mode you must set an {@link OBDAModel} with your mappings.
 *
 * @see OBDAModel
 */
public class QuestOWLFactory implements OWLReasonerFactory {

    @SuppressWarnings("unused")
    private final Logger log = LoggerFactory.getLogger(QuestOWLFactory.class);

    @Nonnull
    @Override
    public String getReasonerName() {
        return "Ontop/Quest";
    }

    @Nonnull
    @Override
    public QuestOWL createNonBufferingReasoner(@Nonnull OWLOntology ontology) {
        throw new UnsupportedOperationException("Quest is a buffering reasoner");
    }

    @Nonnull
    @Override
    public QuestOWL createNonBufferingReasoner(@Nonnull OWLOntology ontology, @Nonnull OWLReasonerConfiguration config)
            throws IllegalConfigurationException {
        throw new UnsupportedOperationException("Quest is a buffering reasoner");
    }

    @Nonnull
    @Override
    public QuestOWL createReasoner(@Nonnull OWLOntology ontology) {
    	QuestPreferences preferences = new QuestPreferences();
    	preferences.get(QuestPreferences.ABOX_MODE).equals(QuestConstants.CLASSIC);
        return createReasoner(ontology, QuestOWLConfiguration.builder().preferences(preferences).build());
    }

    /**
     *
     * @deprecated use {@link #createReasoner(OWLOntology, QuestOWLConfiguration)} instead
     *
     * @throws IllegalConfigurationException
     */
    @Nonnull
    @Override
    @Deprecated
    public QuestOWL createReasoner(@Nonnull OWLOntology ontology, @Nonnull OWLReasonerConfiguration config) throws IllegalConfigurationException {
        checkArgument(config instanceof QuestOWLConfiguration, "config %s is not an instance of QuestOWLConfiguration", config);
        return createReasoner(ontology, (QuestOWLConfiguration) config);
    }


    @Nonnull
    public QuestOWL createReasoner(@Nonnull OWLOntology ontology, @Nonnull QuestOWLConfiguration config) throws IllegalConfigurationException {
        OBDAModel obdaModel = config.getObdaModel();
        Properties preferences = config.getPreferences();

        if (obdaModel == null && preferences.get(QuestPreferences.ABOX_MODE).equals(QuestConstants.VIRTUAL)) {
            throw new IllegalConfigurationException("mappings are not specified in virtual mode", config);
        } else if (obdaModel != null && preferences.get(QuestPreferences.ABOX_MODE).equals(QuestConstants.CLASSIC)) {
            throw new IllegalConfigurationException("mappings are specified in classic mode", config);
        }

        return new QuestOWL(ontology, config);

    }

}
