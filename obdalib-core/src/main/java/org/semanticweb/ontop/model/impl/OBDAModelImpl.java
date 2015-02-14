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

import java.net.URI;
import java.util.*;

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
     * TODO: make these sets immutable
     */
	private final Set<Predicate> declaredClasses;
	private final Set<Predicate> declaredObjectProperties;
	private final Set<Predicate> declaredDataProperties;
	// All other predicates (not classes or properties)
    private final Set<Predicate> otherDeclaredPredicates;

    /**
     * Normal constructor. Used by the QuestComponentFactory.
     */
    public OBDAModelImpl(Set<OBDADataSource> dataSources,
                         Map<URI, ImmutableList<OBDAMappingAxiom>> newMappings,
                         PrefixManager prefixManager) throws DuplicateMappingException {

        this(dataSources, newMappings, prefixManager, new HashSet<Predicate>(),
                new HashSet<Predicate>(), new HashSet<Predicate>(), new HashSet<Predicate>());
    }

    /**
     * Is protected so as the OBDAFactoryWithException method just have to consider the
     * first and unique constructor.
     * TODO: integrate it to the factory (make it public).
     */
    protected OBDAModelImpl(Set<OBDADataSource> dataSources,
                         Map<URI, ImmutableList<OBDAMappingAxiom>> newMappings,
                         PrefixManager prefixManager,
                         Set<Predicate> declaredClasses,
                         Set<Predicate> declaredObjectProperties,
                         Set<Predicate> declaredDataProperties,
                         Set<Predicate> otherDeclaredPredicates
                         )
            throws DuplicateMappingException{
        checkDuplicates(newMappings);
        this.mappingIndexByDataSource = ImmutableMap.copyOf(newMappings);
        this.mappingIndexById = indexMappingsById(newMappings);
        this.prefixManager = prefixManager;
        this.dataSources = ImmutableSet.copyOf(dataSources);
        this.dataSourceIndex = indexDataSources(this.dataSources);

        this.declaredClasses = declaredClasses;
        this.declaredObjectProperties = declaredObjectProperties;
        this.declaredDataProperties = declaredDataProperties;
        this.otherDeclaredPredicates = otherDeclaredPredicates;
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
        return newModel(dataSources, newMappings, prefixManager, declaredClasses, declaredObjectProperties, declaredDataProperties,
                otherDeclaredPredicates);
    }

    @Override
    public OBDAModel newModel(Set<OBDADataSource> dataSources,
                              Map<URI, ImmutableList<OBDAMappingAxiom>> newMappings,
                              PrefixManager prefixManager, Set<Predicate> declaredClasses,
                              Set<Predicate> declaredObjectProperties,
                              Set<Predicate> declaredDataProperties,
                              Set<Predicate> otherDeclaredPredicates) throws DuplicateMappingException {
        return new OBDAModelImpl(dataSources, newMappings, prefixManager, declaredClasses, declaredObjectProperties, declaredDataProperties,
                otherDeclaredPredicates);
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

	@Override
	public Set<Predicate> getDeclaredPredicates() {
		Set<Predicate> result = new HashSet<>();
		result.addAll(declaredClasses);
		result.addAll(declaredObjectProperties);
		result.addAll(declaredDataProperties);
		result.addAll(otherDeclaredPredicates);
		return result;
	}

	@Override
	public Set<Predicate> getDeclaredClasses() {
        return new HashSet<>(declaredClasses);
	}

	@Override
	public Set<Predicate> getDeclaredObjectProperties() {
		return new HashSet<>(declaredObjectProperties);
	}

	@Override
	public Set<Predicate> getDeclaredDataProperties() {
        return new HashSet<>(declaredDataProperties);
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
                || otherDeclaredPredicates.contains(predicate));
	}

    //--------------------------------
    // Side-effect methods (mutable)
    // TODO: remove them
    //--------------------------------

    @Override
    public boolean declarePredicate(Predicate predicate) {
        if (predicate.isClass()) {
            return declaredClasses.add(predicate);
        } else if (predicate.isObjectProperty()) {
            return declaredObjectProperties.add(predicate);
        } else if (predicate.isDataProperty()) {
            return declaredDataProperties.add(predicate);
        } else {
            return otherDeclaredPredicates.add(predicate);
        }
    }

    @Override
    public boolean declareClass(Predicate className) {
        if (!className.isClass()) {
            throw new RuntimeException("Cannot declare a non-class predicate as a class. Offending predicate: " + className);
        }
        return declaredClasses.add(className);
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
        return otherDeclaredPredicates.remove(predicate);
    }

    @Override
    public boolean unDeclareClass(Predicate className) {
        return declaredClasses.remove(className);
    }

    @Override
    public boolean unDeclareObjectProperty(Predicate property) {
        return declaredObjectProperties.remove(property);
    }

    @Override
    public boolean unDeclareDataProperty(Predicate property) {
        return declaredDataProperties.remove(property);
    }
}
