package org.semanticweb.ontop.model.impl;

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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.semanticweb.ontop.exception.DuplicateMappingException;
import org.semanticweb.ontop.io.PrefixManager;
import org.semanticweb.ontop.model.*;

public class OBDAModelImpl implements OBDAModel {
	private final PrefixManager prefixManager;

    private final ImmutableSet<OBDADataSource> dataSources;
	private final ImmutableMap<URI, OBDADataSource> dataSourceIndex;

	private final ImmutableMap<URI, ImmutableList<OBDAMappingAxiom>> mappingIndexByDataSource;
    private final ImmutableMap<String, OBDAMappingAxiom> mappingIndexById;

    /**
     * TODO: remove them
     */
	private final Set<Predicate> declaredClasses = new HashSet<>();
	private final Set<Predicate> declaredObjectProperties = new HashSet<>();
	private final Set<Predicate> declaredDataProperties = new HashSet<>();

	// All other predicates (not classes or properties)
    private final Set<Predicate> declaredPredicates = new HashSet<>();

    /**
     * Normal constructor. Used by the QuestComponentFactory.
     */
    public OBDAModelImpl(Set<OBDADataSource> dataSources,
                         Map<URI, ImmutableList<OBDAMappingAxiom>> newMappings,
                         PrefixManager prefixManager)
            throws DuplicateMappingException{
        checkDuplicates(newMappings);
        this.mappingIndexByDataSource = ImmutableMap.copyOf(newMappings);
        this.mappingIndexById = indexMappingsById(newMappings);
        this.prefixManager = prefixManager;
        this.dataSources = ImmutableSet.copyOf(dataSources);
        this.dataSourceIndex = indexDataSources(this.dataSources);
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
        return new OBDAModelImpl(dataSources, newMappings, prefixManager);
    }

	@Override
	public String getVersion() {
		try {
			InputStream stream = getClass().getResourceAsStream("/META-INF/MANIFEST.MF");
			Manifest manifest = new Manifest(stream);
			Attributes attributes = manifest.getMainAttributes();
			String implementationVersion = attributes.getValue("Implementation-Version");
			return implementationVersion;
		} catch (IOException e) {
			return "";
		}
	}

	@Override
	public String getBuiltDate() {
		try {
			InputStream stream = getClass().getResourceAsStream("/META-INF/MANIFEST.MF");
			Manifest manifest = new Manifest(stream);
			Attributes attributes = manifest.getMainAttributes();
			String builtDate = attributes.getValue("Built-Date");
			return builtDate;
		} catch (IOException e) {
			return "";
		}
	}

	@Override
	public String getBuiltBy() {
		try {
			InputStream stream = getClass().getResourceAsStream("/META-INF/MANIFEST.MF");
			Manifest manifest = new Manifest(stream);
			Attributes attributes = manifest.getMainAttributes();
			String builtBy = attributes.getValue("Built-By");
			return builtBy;
		} catch (IOException e) {
			return "";
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

    /**
     * Deprecated: sourceUri is irrelevant.
     */
    @Deprecated
	@Override
	public OBDAMappingAxiom getMapping(URI sourceUri, String mappingId) {
        return getMapping(mappingId);
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
		return mappingIndexByDataSource.get(dataSourceUri);
	}

	@Override
	public Set<Predicate> getDeclaredPredicates() {
		LinkedHashSet<Predicate> result = new LinkedHashSet<Predicate>();
		result.addAll(declaredClasses);
		result.addAll(declaredObjectProperties);
		result.addAll(declaredDataProperties);
		result.addAll(declaredPredicates);
		return result;
	}

	@Override
	public Set<Predicate> getDeclaredClasses() {
		LinkedHashSet<Predicate> result = new LinkedHashSet<Predicate>();
		result.addAll(declaredClasses);
		return result;
	}

	@Override
	public Set<Predicate> getDeclaredObjectProperties() {
		LinkedHashSet<Predicate> result = new LinkedHashSet<Predicate>();
		result.addAll(declaredObjectProperties);
		return result;
	}

	@Override
	public Set<Predicate> getDeclaredDataProperties() {
		LinkedHashSet<Predicate> result = new LinkedHashSet<Predicate>();
		result.addAll(declaredDataProperties);
		return result;
	}

	@Override
	public boolean declarePredicate(Predicate predicate) {
		if (predicate.isClass()) {
			return declaredClasses.add(predicate);
		} else if (predicate.isObjectProperty()) {
			return declaredObjectProperties.add(predicate);
		} else if (predicate.isDataProperty()) {
			return declaredDataProperties.add(predicate);
		} else {
			return declaredPredicates.add(predicate);
		}
	}

	@Override
	public boolean declareClass(Predicate classname) {
		if (!classname.isClass()) {
			throw new RuntimeException("Cannot declare a non-class predicate as a class. Offending predicate: " + classname);
		}
		return declaredClasses.add(classname);
	}

	@Override
	public boolean declareObjectProperty(Predicate property) {
		if (!property.isObjectProperty()) {
			throw new RuntimeException("Cannot declare a non-object property predicate as an object property. Offending predicate: " + property);
		}
		return declaredObjectProperties.add(property);
	}

	@Override
	public boolean declareDataProperty(Predicate property) {
		if (!property.isDataProperty()) {
			throw new RuntimeException("Cannot declare a non-data property predicate as an data property. Offending predicate: " + property);
		}
		return declaredDataProperties.add(property);
	}

	@Override
	public boolean unDeclarePredicate(Predicate predicate) {
		return declaredPredicates.remove(predicate);
	}

	@Override
	public boolean unDeclareClass(Predicate classname) {
		return declaredClasses.remove(classname);
	}

	@Override
	public boolean unDeclareObjectProperty(Predicate property) {
		return declaredObjectProperties.remove(property);
	}

	@Override
	public boolean unDeclareDataProperty(Predicate property) {
		return declaredDataProperties.remove(property);
	}
	
	@Override
	public boolean isDeclaredClass(Predicate classname) {
		return declaredClasses.contains(classname);
	}

	@Override
	public boolean isDeclaredObjectProperty(Predicate property) {
		return declaredObjectProperties.contains(property);
	}

	@Override
	public boolean isDeclaredDataProperty(Predicate property) {
		return declaredDataProperties.contains(property);
	}

	@Override
	public boolean isDeclared(Predicate predicate) {
		return (isDeclaredClass(predicate) || isDeclaredObjectProperty(predicate) || isDeclaredDataProperty(predicate)
                || declaredPredicates.contains(predicate));
	}
}
