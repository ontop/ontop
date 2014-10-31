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

import org.semanticweb.ontop.exception.DuplicateMappingException;
import org.semanticweb.ontop.exception.InvalidMappingException;
import org.semanticweb.ontop.io.InvalidDataSourceException;
import org.semanticweb.ontop.io.OntopMappingWriter;
import org.semanticweb.ontop.model.OBDAModel;
import org.semanticweb.ontop.owlrefplatform.core.QuestPreferences;
import org.semanticweb.ontop.sql.ImplicitDBConstraints;


import java.io.*;
import java.util.Properties;

import org.protege.editor.owl.model.inference.AbstractProtegeOWLReasonerInfo;
import org.semanticweb.owlapi.reasoner.BufferingMode;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

public class ProtegeOBDAOWLReformulationPlatformFactory extends AbstractProtegeOWLReasonerInfo {

    private final OntopOWLFactory factory;

    public ProtegeOBDAOWLReformulationPlatformFactory(File mappingFile, QuestPreferences preferences) throws DuplicateMappingException, InvalidMappingException, InvalidDataSourceException, IOException {
        this.factory = new OntopOWLFactory(mappingFile, preferences);
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
	 * Allows the user to supply database keys that are not in the database metadata
	 * 
	 * @param uc The user-supplied database constraints
	 */
	public void setImplicitDBConstraints(ImplicitDBConstraints uc) {
		if(uc == null)
			throw new NullPointerException();
		factory.setImplicitDBConstraints(uc);
	}

    public void reload(ProtegeReformulationPlatformPreferences reasonerPreferences) {
        factory.reload(reasonerPreferences);
    }

    public void reload(OBDAModel currentModel, ProtegeReformulationPlatformPreferences reasonerPreferences) {
        factory.reload(currentModel, reasonerPreferences);
    }
}
