package org.semanticweb.ontop.protege.core;

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

import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLConfiguration;
import it.unibz.krdb.sql.ImplicitDBConstraintsReader;
import org.protege.editor.owl.model.inference.AbstractProtegeOWLReasonerInfo;
import org.semanticweb.owlapi.reasoner.BufferingMode;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.ReasonerProgressMonitor;

public class OntopReasonerInfo extends AbstractProtegeOWLReasonerInfo {

	OntopOWLFactory factory = new OntopOWLFactory();

    private final QuestOWLConfiguration.Builder configBuilder = QuestOWLConfiguration.builder();

    @Override
	public BufferingMode getRecommendedBuffering() {
		return BufferingMode.BUFFERING;
	}

	@Override
	public OWLReasonerFactory getReasonerFactory() {
		return factory;
	}

	public void setPreferences(QuestPreferences preferences) {
        configBuilder.preferences(preferences);
	}

	public void setOBDAModel(OBDAModel model) {
        configBuilder.obdaModel(model);
	}

	/**
	 * Allows the user to supply database keys that are not in the database metadata
	 * 
	 * @param uc The user-supplied database constraints
	 */
	public void setImplicitDBConstraints(ImplicitDBConstraintsReader uc) {
		if(uc == null)
			throw new NullPointerException();
        configBuilder.dbConstraintsReader(uc);
	}

    @Override
    public OWLReasonerConfiguration getConfiguration(ReasonerProgressMonitor monitor) {
        return configBuilder.progressMonitor(monitor).build();
    }
}
