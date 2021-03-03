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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.spec.mapping.PrefixManager;
import it.unibz.inf.ontop.spec.mapping.impl.AbstractPrefixManager;
import org.protege.editor.owl.model.entity.EntityCreationPreferences;
import org.protege.editor.owl.ui.prefix.PrefixUtilities;
import org.semanticweb.owlapi.formats.PrefixDocumentFormat;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.rdf.rdfxml.renderer.OWLOntologyXMLNamespaceManager;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.StreamSupport;


/**
 * This PrefixManager wraps Protege's prefix manager.
 * Any prefixes defined in Protege are transparently passed to all OBDA lib classes.
 */

public class OntologyPrefixManager extends AbstractPrefixManager {

    private final PrefixDocumentFormat owlmapper;

	private final boolean hasExplicitDefaultPrefixNamespace;

	public OntologyPrefixManager(@Nonnull OWLOntology ontology) {
		this.owlmapper = PrefixUtilities.getPrefixOWLOntologyFormat(ontology);

		OWLOntologyXMLNamespaceManager nsm = new OWLOntologyXMLNamespaceManager(
				ontology,
				Objects.requireNonNull(ontology.getOWLOntologyManager().getOntologyFormat(ontology)));

		if (StreamSupport.stream(nsm.getPrefixes().spliterator(), false)
				.anyMatch(p -> p.equals(""))) {
			String prefix = nsm.getNamespaceForPrefix("");
			hasExplicitDefaultPrefixNamespace = true;
			addPrefix(PrefixManager.DEFAULT_PREFIX, prefix);
		}
		else {
			hasExplicitDefaultPrefixNamespace = false;
			generateDefaultPrefixNamespaceIfPossible(ontology.getOntologyID());
		}
	}

	public void updateOntologyID(OWLOntologyID newID) {
		if (!hasExplicitDefaultPrefixNamespace)
			generateDefaultPrefixNamespaceIfPossible(newID);
	}

	public String generateUniquePrefixForBootstrapper(String baseIri) {
		String prefix = "g:";
		Map<String, String> map = owlmapper.getPrefixName2PrefixMap();
		while (map.containsKey(prefix))
			prefix = "g" + prefix;

		addPrefix(prefix, baseIri);
		return prefix;
	}


	private void generateDefaultPrefixNamespaceIfPossible(OWLOntologyID ontologyID) {
		if (!ontologyID.getOntologyIRI().isPresent())
			return;

		String prefixUri = ontologyID.getOntologyIRI().get().toString();
		if (!prefixUri.endsWith("#") && !prefixUri.endsWith("/")) {
			String defaultSeparator = EntityCreationPreferences.getDefaultSeparator();
			if (!prefixUri.endsWith(defaultSeparator))  {
				prefixUri += defaultSeparator;
			}
		}
		addPrefix(PrefixManager.DEFAULT_PREFIX, prefixUri);
	}


	@Override
	protected Optional<String> getIriDefinition(String prefix) {
		return Optional.ofNullable(owlmapper.getPrefix(prefix));
	}

	@Override
	protected ImmutableList<Map.Entry<String, String>> getOrderedMap() {
		return orderMap(owlmapper.getPrefixName2PrefixMap());
	}

	@Override
	public ImmutableMap<String, String> getPrefixMap() {
		return ImmutableMap.copyOf(owlmapper.getPrefixName2PrefixMap());
	}

	public void addPrefix(String name, String uri) {
		owlmapper.setPrefix(name, uri);
	}
}
