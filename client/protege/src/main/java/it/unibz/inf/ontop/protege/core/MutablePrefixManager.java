package it.unibz.inf.ontop.protege.core;

/*
 * #%L
 * ontop-protege
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

import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.spec.mapping.impl.AbstractPrefixManager;
import org.protege.editor.owl.model.entity.EntityCreationPreferences;
import org.semanticweb.owlapi.formats.PrefixDocumentFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.rdf.rdfxml.renderer.OWLOntologyXMLNamespaceManager;

import java.util.*;
import java.util.stream.StreamSupport;


/**
 * This PrefixManager is meant to 'wrap' Protege's prefix manager. That way any
 * prefix defined in Protege are transparently passed to all OBDA lib classes.
 */
public class MutablePrefixManager extends AbstractPrefixManager {

    private final PrefixDocumentFormat owlmapper;

	MutablePrefixManager(PrefixDocumentFormat owlmapper) {
		this.owlmapper = owlmapper;
	}

	@Override
	protected Optional<String> getIriDefinition(String prefix) {
		return Optional.ofNullable(owlmapper.getPrefix(prefix));
	}

	@Override
	public ImmutableMap<String, String> getPrefixMap() {
		return ImmutableMap.copyOf(owlmapper.getPrefixName2PrefixMap());
	}

	public boolean contains(String prefix) {
		return owlmapper.containsPrefixMapping(prefix);
	}

	public void addPrefix(String name, String uri) {
		owlmapper.setPrefix(name, uri);
	}

	public void clear() {
		owlmapper.clear();
	}

	/**
	 *  Returns the namespace declared in the ontology for the default prefix.
	*/
	 static Optional<String> getDeclaredDefaultPrefixNamespace(OWLOntology ontology){
		OWLOntologyXMLNamespaceManager nsm = new OWLOntologyXMLNamespaceManager(
				ontology,
				ontology.getOWLOntologyManager().getOntologyFormat(ontology));

		if (StreamSupport.stream(nsm.getPrefixes().spliterator(), false)
			.anyMatch(p ->  p.equals(""))){
			return Optional.ofNullable(nsm.getNamespaceForPrefix(""));
		}
		return Optional.empty();
	}

	static Optional<String> generateDefaultPrefixNamespaceFromID(OWLOntologyID ontologyID) {
		com.google.common.base.Optional<IRI> ontologyIRI = ontologyID.getOntologyIRI();
		return ontologyIRI.isPresent()?
				Optional.of(getProperPrefixURI(ontologyIRI.get().toString())):
				Optional.empty();
	}

	/**
	 * A utility method to ensure a proper naming for prefix URIs
	 */
	private static String getProperPrefixURI(String prefixUri) {
		if (!prefixUri.endsWith("#")) {
			if (!prefixUri.endsWith("/")) {
				String defaultSeparator = EntityCreationPreferences.getDefaultSeparator();
				if (!prefixUri.endsWith(defaultSeparator))  {
					prefixUri += defaultSeparator;
				}
			}
		}
		return prefixUri;
	}
}
