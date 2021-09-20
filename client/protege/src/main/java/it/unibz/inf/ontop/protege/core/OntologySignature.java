package it.unibz.inf.ontop.protege.core;


/*
 * #%L
 * ontop-obdalib-core
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


import it.unibz.inf.ontop.model.vocabulary.OWL;
import it.unibz.inf.ontop.model.vocabulary.Ontop;
import org.apache.commons.rdf.api.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.parameters.Imports;


public class OntologySignature {
	
	private static final Imports IMPORTS = Imports.INCLUDED;
	
	private final OWLOntology ontology;

	public OntologySignature(OWLOntology ontology) {
		this.ontology = ontology;
	}

	public boolean containsClass(IRI iri) {
		return ontology.containsClassInSignature(convert(iri), IMPORTS);
	}

	public boolean containsObjectProperty(IRI iri) {
		return ontology.containsObjectPropertyInSignature(convert(iri), IMPORTS);
	}

	public boolean containsDataProperty(IRI iri) {
		return ontology.containsDataPropertyInSignature(convert(iri),IMPORTS);
	}

	public boolean containsAnnotationProperty(IRI iri) {
		return ontology.containsAnnotationPropertyInSignature(convert(iri), IMPORTS);
	}

	public boolean isBuiltinProperty(IRI iri) {
		return iri.equals(OWL.SAME_AS) || iri.equals(Ontop.CANONICAL_IRI);
	}

	private static org.semanticweb.owlapi.model.IRI convert(IRI iri) {
		return org.semanticweb.owlapi.model.IRI.create(iri.getIRIString());
	}
}
