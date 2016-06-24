package it.unibz.inf.ontop.model.impl;

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

import java.net.URI;
import java.util.*;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.exception.DuplicateMappingException;
import it.unibz.inf.ontop.model.OBDADataSource;
import it.unibz.inf.ontop.model.OBDAMappingAxiom;
import it.unibz.inf.ontop.model.OBDAModel;
import it.unibz.inf.ontop.io.PrefixManager;

import it.unibz.inf.ontop.ontology.*;

public class OBDAModelImpl implements OBDAModel {
	private final PrefixManager prefixManager;

    private final ImmutableSet<OBDADataSource> dataSources;
	private final ImmutableMap<URI, OBDADataSource> dataSourceIndex;

	private final ImmutableMap<URI, ImmutableList<OBDAMappingAxiom>> mappingIndexByDataSource;
    private final ImmutableMap<String, OBDAMappingAxiom> mappingIndexById;

    private final OntologyVocabulary ontologyVocabulary;

    /**
     * Normal constructor. Used by the QuestComponentFactory.
     */
    public OBDAModelImpl(Set<OBDADataSource> dataSources,
                         Map<URI, ImmutableList<OBDAMappingAxiom>> newMappings,
                         PrefixManager prefixManager,
                         OntologyVocabulary ontologyVocabulary) throws DuplicateMappingException {

        checkDuplicates(newMappings);
        this.mappingIndexByDataSource = ImmutableMap.copyOf(newMappings);
        this.mappingIndexById = indexMappingsById(newMappings);
        this.prefixManager = prefixManager;
        this.dataSources = ImmutableSet.copyOf(dataSources);
        this.dataSourceIndex = indexDataSources(this.dataSources);
        this.ontologyVocabulary = ontologyVocabulary;
    }

    /**
     * No mapping should be duplicate among all the data sources.
     */
    private static void checkDuplicates(Map<URI, ImmutableList<OBDAMappingAxiom>> mappings)
            throws DuplicateMappingException {

        Set<OBDAMappingAxiom> sourceMappingSet = new HashSet<>();

        for (URI sourceURI : mappings.keySet()) {
            List<OBDAMappingAxiom> currentSourceMappings = mappings.get(sourceURI);

            // Mutable (may be reused)
            int previousMappingCount = sourceMappingSet.size();
            sourceMappingSet.addAll(currentSourceMappings);

            int duplicateCount = currentSourceMappings.size() + previousMappingCount - sourceMappingSet.size();

            /**
             * If there are some mappings, finds them
             */
            if (duplicateCount > 0) {
                Set<String> duplicateIds = new HashSet<>();
                int remaining = duplicateCount;
                for (OBDAMappingAxiom mapping : currentSourceMappings) {
                    if (sourceMappingSet.contains(mapping)) {
                        sourceMappingSet.remove(mapping);
                    }
                    /**
                     * Duplicate
                     */
                    else {
                        duplicateIds.add(mapping.getId());
                        if (--remaining == 0)
                            break;
                    }
                }
                //TODO: indicate the source
                throw new DuplicateMappingException(String.format("Found %d duplicates in the following ids: %s",
                        duplicateCount, duplicateIds.toString()));
            }
        }
    }

    private static ImmutableMap<String, OBDAMappingAxiom> indexMappingsById(Map<URI, ImmutableList<OBDAMappingAxiom>> mappings)
            throws IllegalArgumentException {
        Map<String, OBDAMappingAxiom> mappingIndexById = new HashMap<>();
        for (List<OBDAMappingAxiom> axioms : mappings.values()) {
            for (OBDAMappingAxiom axiom : axioms) {
                String id = axiom.getId();
                if (mappingIndexById.containsKey(id)) {
                    // Should have already been detected by checkDuplicates.
                    throw new IllegalArgumentException(String.format("Not unique mapping ID found : %s", id));
                }
                mappingIndexById.put(id, axiom);
            }
        }
        return ImmutableMap.copyOf(mappingIndexById);
    }

    private static ImmutableMap<URI, OBDADataSource> indexDataSources(Set<OBDADataSource> dataSources) {
        Map<URI, OBDADataSource> dataSourceIndex = new HashMap<>();
        for (OBDADataSource source : dataSources) {
            dataSourceIndex.put(source.getSourceID(), source);
        }
        return ImmutableMap.copyOf(dataSourceIndex);
    }


    @Override
    public OBDAModel newModel(Set<OBDADataSource> dataSources,
                              Map<URI, ImmutableList<OBDAMappingAxiom>> newMappings) throws DuplicateMappingException {
        return newModel(dataSources, newMappings, prefixManager);
    }

    @Override
    public OBDAModel newModel(Set<OBDADataSource> dataSources,
                              Map<URI, ImmutableList<OBDAMappingAxiom>> newMappings,
                              PrefixManager prefixManager) throws DuplicateMappingException {
        return newModel(dataSources, newMappings, prefixManager, ontologyVocabulary);
    }

    @Override
    public OBDAModel newModel(Set<OBDADataSource> dataSources,
                              Map<URI, ImmutableList<OBDAMappingAxiom>> newMappings,
                              PrefixManager prefixManager, OntologyVocabulary ontologyVocabulary) throws DuplicateMappingException {
        return new OBDAModelImpl(dataSources, newMappings, prefixManager, ontologyVocabulary);
    }

    @Override
    public OBDAModel clone() {
        try {
            return new OBDAModelImpl(dataSources, mappingIndexByDataSource, prefixManager, ontologyVocabulary);
        } catch (DuplicateMappingException e) {
            throw new RuntimeException("Unexpected error (inconsistent cloning): " + e.getMessage());
        }
    }

    @Override
	public PrefixManager getPrefixManager() {
		return prefixManager;
	}

	@Override
	public Set<OBDADataSource> getSources() {
		return dataSources;
	}

	@Override
	public OBDADataSource getSource(URI name) {
		return dataSourceIndex.get(name);
	}

	@Override
	public boolean containsSource(URI name) {
		return (getSource(name) != null);
	}

    @Override
    public OntologyVocabulary getOntologyVocabulary() {
        return ontologyVocabulary;
    }

    @Override
    public OBDAMappingAxiom getMapping(String mappingId) {
        return mappingIndexById.get(mappingId);
    }

	@Override
	public ImmutableMap<URI, ImmutableList<OBDAMappingAxiom>> getMappings() {
        return mappingIndexByDataSource;
	}

	@Override
	public ImmutableList<OBDAMappingAxiom> getMappings(URI dataSourceUri) {
        ImmutableList<OBDAMappingAxiom> mappings = mappingIndexByDataSource.get(dataSourceUri);
        if (mappings != null) {
            return mappings;
        }
        /**
         * Sometimes, no mappings are defined.
         * Happens for instance with Protege if we save the OBDAModel without adding any mapping.
         */
        return ImmutableList.of();
	}
}
