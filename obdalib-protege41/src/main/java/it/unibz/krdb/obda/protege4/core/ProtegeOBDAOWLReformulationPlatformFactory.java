package it.unibz.krdb.obda.protege4.core;

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
import it.unibz.krdb.sql.ImplicitDBConstraints;

import java.util.Properties;

import org.protege.editor.owl.model.inference.AbstractProtegeOWLReasonerInfo;
import org.semanticweb.owlapi.reasoner.BufferingMode;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

public class ProtegeOBDAOWLReformulationPlatformFactory extends AbstractProtegeOWLReasonerInfo {

	OntopOWLFactory factory = new OntopOWLFactory();

	@Override
	public BufferingMode getRecommendedBuffering() {
		return BufferingMode.BUFFERING;
	}

	@Override
	public OWLReasonerFactory getReasonerFactory() {
		return factory;
	}

	public void setPreferences(Properties preferences) {
		factory.setPreferenceHolder(preferences);
	}

	public void setOBDAModel(OBDAModel model) {
		factory.setOBDAController(model);
	}

	/**
	 * Allows the user to supply database keys that are not in the database metadata
	 * 
	 * @param uc The user-supplied database constraints
	 */
	public void setImplicitDBConstraints(ImplicitDBConstraints uc) {
		if(uc == null)
			throw new NullPointerException();
		factory.setImplicitDBConstraints(uc);
	}
}
