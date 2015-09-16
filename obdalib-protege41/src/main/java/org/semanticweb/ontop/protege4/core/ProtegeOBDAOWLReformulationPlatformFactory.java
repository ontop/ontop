package org.semanticweb.ontop.protege4.core;

/*
 * #%L
 * ontop-protege4
 * %%
 * Copyright (C) 2009 - 2013 KRDB Research Centre. Free University of Bozen Bolzano.
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

import org.semanticweb.ontop.model.OBDAModel;

import org.protege.editor.owl.model.inference.AbstractProtegeOWLReasonerInfo;
import org.semanticweb.owlapi.reasoner.BufferingMode;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

/**
 * As a Protege plugin, this class can only provide a default constructor.
 *
 * Because the ReasonerFactory needs some parameters to be initialized,
 * please make sure to call the load() method at least one before getting the
 * reasoner factory.
 *
 */
public class ProtegeOBDAOWLReformulationPlatformFactory extends AbstractProtegeOWLReasonerInfo {

    private OntopOWLFactory factory;

    public ProtegeOBDAOWLReformulationPlatformFactory() {
        // Please call the load() method ASAP.
        factory = null;
    }

	@Override
	public BufferingMode getRecommendedBuffering() {
		return BufferingMode.BUFFERING;
	}

	@Override
	public OWLReasonerFactory getReasonerFactory() {
        return factory;
	}

    /**
     * Loads or reloads.
     *
     * Must be called before getting the reasoner factory.
     *
     */
    public void load(OBDAModel currentModel, ProtegeReformulationPlatformPreferences reasonerPreferences) {
        if (factory == null) {
            try {
                factory = new OntopOWLFactory(currentModel, reasonerPreferences);
            } catch (Exception e) {
                OntopOWLFactory.handleError(e);
            }
        }
        else {
            factory.reload(currentModel, reasonerPreferences);
        }
    }
}
