package it.unibz.inf.ontop.owlapi.impl;

/*
 * #%L
 * ontop-quest-owlapi
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

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.injection.OntopSystemConfiguration;
import it.unibz.inf.ontop.owlapi.exception.OntopOWLException;
import it.unibz.inf.ontop.materialization.MaterializationParams;
import it.unibz.inf.ontop.answering.resultset.MaterializedGraphResultSet;
import it.unibz.inf.ontop.materialization.OntopRDFMaterializer;
import it.unibz.inf.ontop.materialization.impl.DefaultOntopRDFMaterializer;
import it.unibz.inf.ontop.owlapi.resultset.impl.OntopMaterializedGraphOWLResultSet;
import it.unibz.inf.ontop.owlapi.OntopOWLAPIMaterializer;
import it.unibz.inf.ontop.owlapi.resultset.MaterializedGraphOWLResultSet;
import org.semanticweb.owlapi.model.OWLException;

import javax.annotation.Nonnull;
import java.net.URI;

public class DefaultOntopOWLAPIMaterializer implements OntopOWLAPIMaterializer {

	private final OntopRDFMaterializer materializer;

	public DefaultOntopOWLAPIMaterializer() {
		materializer = new DefaultOntopRDFMaterializer();
	}

	@Override
	public MaterializedGraphOWLResultSet materialize(@Nonnull OntopSystemConfiguration configuration,
                                                     @Nonnull MaterializationParams params)
			throws OWLException {
		try {
			return wrap(materializer.materialize(configuration, params));
		} catch (OBDASpecificationException e) {
			throw new OntopOWLException(e);
		}
	}

	@Override
	public MaterializedGraphOWLResultSet materialize(@Nonnull OntopSystemConfiguration configuration,
                                                     @Nonnull ImmutableSet<URI> selectedVocabulary,
                                                     @Nonnull MaterializationParams params)
			throws OWLException {
		try {
			return wrap(materializer.materialize(configuration, selectedVocabulary, params));
		} catch (OBDASpecificationException e) {
			throw new OntopOWLException(e);
		}
	}

	private MaterializedGraphOWLResultSet wrap(MaterializedGraphResultSet graphResultSet) {
		return new OntopMaterializedGraphOWLResultSet(graphResultSet);
	}
}
