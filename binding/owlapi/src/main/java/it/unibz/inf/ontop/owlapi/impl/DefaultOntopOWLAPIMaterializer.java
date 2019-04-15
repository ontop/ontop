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
import it.unibz.inf.ontop.answering.resultset.MaterializedGraphResultSet;
import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.injection.OntopSystemConfiguration;
import it.unibz.inf.ontop.materialization.MaterializationParams;
import it.unibz.inf.ontop.materialization.OntopRDFMaterializer;
import it.unibz.inf.ontop.materialization.impl.DefaultOntopRDFMaterializer;
import it.unibz.inf.ontop.owlapi.OntopOWLAPIMaterializer;
import it.unibz.inf.ontop.owlapi.exception.OntopOWLException;
import it.unibz.inf.ontop.owlapi.resultset.MaterializedGraphOWLResultSet;
import it.unibz.inf.ontop.owlapi.resultset.impl.OntopMaterializedGraphOWLResultSet;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.RDF;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLException;

import javax.annotation.Nonnull;

public class DefaultOntopOWLAPIMaterializer implements OntopOWLAPIMaterializer {

	private final OntopRDFMaterializer materializer;
	private RDF rdfFactory;

	public DefaultOntopOWLAPIMaterializer(OntopSystemConfiguration configuration, MaterializationParams materializationParams) throws OBDASpecificationException {
		materializer = new DefaultOntopRDFMaterializer(configuration, materializationParams);
		rdfFactory = configuration.getInjector().getInstance(RDF.class);
	}

	/**
	 * Materializes the saturated RDF graph with the default options
	 */
	public DefaultOntopOWLAPIMaterializer(OntopSystemConfiguration configuration) throws OBDASpecificationException {
		this(configuration, MaterializationParams.defaultBuilder().build());
	}

	@Override
	public MaterializedGraphOWLResultSet materialize()
			throws OWLException {
		try {
			return wrap(materializer.materialize());
		} catch (OBDASpecificationException e) {
			throw new OntopOWLException(e);
		}
	}

	@Override
	public MaterializedGraphOWLResultSet materialize(@Nonnull ImmutableSet<IRI> selectedVocabulary)
			throws OWLException {
		try {
			return wrap(
					materializer.materialize(
							selectedVocabulary.stream()
									.map(i -> rdfFactory.createIRI(i.toString()))
									.collect(ImmutableCollectors.toSet()))
			);
		} catch (OBDASpecificationException e) {
			throw new OntopOWLException(e);
		}
	}

	@Override
	public ImmutableSet<IRI> getClasses() {
		return materializer.getClasses().stream()
				.map(i -> IRI.create(i.getIRIString()))
				.collect(ImmutableCollectors.toSet());
	}

	@Override
	public ImmutableSet<IRI> getProperties() {
		return materializer.getProperties().stream()
				.map(i -> IRI.create(i.getIRIString()))
				.collect(ImmutableCollectors.toSet());
	}

	private MaterializedGraphOWLResultSet wrap(MaterializedGraphResultSet graphResultSet) {
		return new OntopMaterializedGraphOWLResultSet(graphResultSet);
	}
}
