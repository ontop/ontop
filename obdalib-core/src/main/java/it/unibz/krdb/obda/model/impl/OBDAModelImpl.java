package it.unibz.krdb.obda.model.impl;

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

import it.unibz.krdb.obda.exception.DuplicateMappingException;
import it.unibz.krdb.obda.io.PrefixManager;
import it.unibz.krdb.obda.io.SimplePrefixManager;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDADataSource;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.OBDAMappingListener;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.OBDAModelListener;
import it.unibz.krdb.obda.model.OBDASQLQuery;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.ontology.DataPropertyExpression;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.ObjectPropertyExpression;
import it.unibz.krdb.obda.ontology.OntologyVocabulary;
import it.unibz.krdb.obda.querymanager.QueryController;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OBDAModelImpl implements OBDAModel {

	private static final long serialVersionUID = 1L;

	private QueryController queryController;

	private PrefixManager prefixManager;

	private HashMap<URI, OBDADataSource> datasources;

	private ArrayList<OBDAModelListener> sourceslisteners;

	private Hashtable<URI, ArrayList<OBDAMappingAxiom>> mappings;

	private ArrayList<OBDAMappingListener> mappinglisteners;

	private static OBDADataFactory dfac = OBDADataFactoryImpl.getInstance();

	private static final Logger log = LoggerFactory.getLogger(OBDAModelImpl.class);

	private final Set<OClass> declaredClasses = new LinkedHashSet<OClass>();

	private final Set<ObjectPropertyExpression> declaredObjectProperties = new LinkedHashSet<ObjectPropertyExpression>();

	private final Set<DataPropertyExpression> declaredDataProperties = new LinkedHashSet<DataPropertyExpression>();

	// All other predicates (not classes or properties)
	// private final LinkedHashSet<Predicate> declaredPredicates = new LinkedHashSet<Predicate>();

	/**
	 * The default constructor
	 */
	public OBDAModelImpl() {
		log.debug("OBDA model is initialized!");

		queryController = new QueryController();
		prefixManager = new SimplePrefixManager();

		datasources = new HashMap<URI, OBDADataSource>();
		sourceslisteners = new ArrayList<OBDAModelListener>();

		mappings = new Hashtable<URI, ArrayList<OBDAMappingAxiom>>();
		mappinglisteners = new ArrayList<OBDAMappingListener>();
	}

	@Override
	public QueryController getQueryController() {
		return queryController;
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
	public void setPrefixManager(PrefixManager prefman) {
		this.prefixManager = prefman;
	}

	@Override
	public PrefixManager getPrefixManager() {
		return prefixManager;
	}

	@Override
	public OBDADataFactory getDataFactory() {
		return dfac;
	}

	@Override
	public void addSource(OBDADataSource source) {
		datasources.put(source.getSourceID(), source);
		fireSourceAdded(source);
	}

	@Override
	public void addSourcesListener(OBDAModelListener listener) {
		if (sourceslisteners.contains(listener)) {
			return;
		}
		sourceslisteners.add(listener);
	}

	@Override
	public void fireSourceAdded(OBDADataSource source) {
		for (OBDAModelListener listener : sourceslisteners) {
			listener.datasourceAdded(source);
		}
	}

	@Override
	public void fireSourceRemoved(OBDADataSource source) {
		for (OBDAModelListener listener : sourceslisteners) {
			listener.datasourceDeleted(source);
		}
	}

	@Override
	public void fireSourceParametersUpdated() {
		for (OBDAModelListener listener : sourceslisteners) {
			listener.datasourcParametersUpdated();
		}
	}

	@Override
	public void fireSourceNameUpdated(URI old, OBDADataSource neu) {
		for (OBDAModelListener listener : sourceslisteners) {
			listener.datasourceUpdated(old.toString(), neu);
		}
	}

	@Override
	public List<OBDADataSource> getSources() {
		List<OBDADataSource> sources = new LinkedList<OBDADataSource>(datasources.values());
		return Collections.unmodifiableList(sources);
	}

	@Override
	public OBDADataSource getSource(URI name) {
		return datasources.get(name);
	}

	@Override
	public boolean containsSource(URI name) {
		if (getSource(name) != null) {
			return true;
		}
		return false;
	}

	@Override
	public void removeSource(URI id) {
		OBDADataSource source = getSource(id);
		datasources.remove(id);
		fireSourceRemoved(source);
	}

	@Override
	public void removeSourcesListener(OBDAModelListener listener) {
		sourceslisteners.remove(listener);
	}

	@Override
	public void updateSource(URI id, OBDADataSource dsd) {
		datasources.remove(id);
		datasources.put(dsd.getSourceID(), dsd);
		fireSourceNameUpdated(id, dsd);
	}

	@Override
	public void addMappingsListener(OBDAMappingListener listener) {
		mappinglisteners.add(listener);
	}

	@Override
	public void removeMapping(URI datasource_uri, String mapping_id) {
		int index = indexOf(datasource_uri, mapping_id);
		if (index != -1) {
			ArrayList<OBDAMappingAxiom> current_mappings = mappings.get(datasource_uri);
			current_mappings.remove(index);
			fireMappingDeleted(datasource_uri, mapping_id);
		}
	}

	@Override
	public void removeAllMappings(URI datasource_uri) {
		ArrayList<OBDAMappingAxiom> mappings = getMappings(datasource_uri);
		while (!mappings.isEmpty()) {
			mappings.remove(0);
		}
		fireAllMappingsRemoved();
	}

	private void fireAllMappingsRemoved() {
		for (OBDAMappingListener listener : mappinglisteners) {
			listener.allMappingsRemoved();
		}
	}

	/**
	 * Announces that a mapping has been updated.
	 */
	private void fireMappigUpdated(URI srcuri, String mapping_id, OBDAMappingAxiom mapping) {
		for (OBDAMappingListener listener : mappinglisteners) {
			listener.mappingUpdated(srcuri, mapping_id, mapping);
		}
	}

	/**
	 * Announces to the listeners that a mapping was deleted.
	 */
	private void fireMappingDeleted(URI srcuri, String mapping_id) {
		for (OBDAMappingListener listener : mappinglisteners) {
			listener.mappingDeleted(srcuri, mapping_id);
		}
	}

	/**
	 * Announces to the listeners that a mapping was inserted.
	 */
	private void fireMappingInserted(URI srcuri, String mapping_id) {
		for (OBDAMappingListener listener : mappinglisteners) {
			listener.mappingInserted(srcuri, mapping_id);
		}
	}

	@Override
	public OBDAMappingAxiom getMapping(URI source_uri, String mapping_id) {
		int pos = indexOf(source_uri, mapping_id);
		if (pos == -1) {
			return null;
		}
		ArrayList<OBDAMappingAxiom> mappings = getMappings(source_uri);
		return mappings.get(pos);
	}

	@Override
	public Hashtable<URI, ArrayList<OBDAMappingAxiom>> getMappings() {
		return mappings;
	}

	@Override
	public ArrayList<OBDAMappingAxiom> getMappings(URI datasource_uri) {
		if (datasource_uri == null)
			return null;
		ArrayList<OBDAMappingAxiom> current_mappings = mappings.get(datasource_uri);
		if (current_mappings == null) {
			initMappingsArray(datasource_uri);
		}
		return mappings.get(datasource_uri);
	}

	@Override
	public int indexOf(URI datasource_uri, String mapping_id) {
		ArrayList<OBDAMappingAxiom> current_mappings = mappings.get(datasource_uri);
		if (current_mappings == null) {
			initMappingsArray(datasource_uri);
			current_mappings = mappings.get(datasource_uri);
		}
		int position = -1;
		for (int i = 0; i < current_mappings.size(); i++) {
			if (current_mappings.get(i).getId().equals(mapping_id)) {
				position = i;
				break;
			}
		}
		return position;
	}

	private void initMappingsArray(URI datasource_uri) {
		mappings.put(datasource_uri, new ArrayList<OBDAMappingAxiom>());
	}

	@Override
	public void addMapping(URI datasource_uri, OBDAMappingAxiom mapping) throws DuplicateMappingException {
		int index = indexOf(datasource_uri, mapping.getId());
		if (index != -1) {
			throw new DuplicateMappingException("ID " + mapping.getId());
		}
		mappings.get(datasource_uri).add(mapping);
		fireMappingInserted(datasource_uri, mapping.getId());
	}

	@Override
	public void removeAllMappings() {
		mappings.clear();
		mappings = new Hashtable<URI, ArrayList<OBDAMappingAxiom>>();
		fireAllMappingsRemoved();
	}

	@Override
	public void removeMappingsListener(OBDAMappingListener listener) {
		mappinglisteners.remove(listener);
	}

	@Override
	public void updateMappingsSourceQuery(URI datasource_uri, String mapping_id, OBDASQLQuery sourceQuery) {
		OBDAMappingAxiom mapping = getMapping(datasource_uri, mapping_id);
		mapping.setSourceQuery(sourceQuery);
		fireMappigUpdated(datasource_uri, mapping.getId(), mapping);
	}

	@Override
	public int updateMapping(URI datasource_uri, String mapping_id, String new_mappingid) throws DuplicateMappingException {
		OBDAMappingAxiom mapping = getMapping(datasource_uri, mapping_id);

		// adds a new mapping
		if (!containsMapping(datasource_uri, new_mappingid)) {
			mapping.setId(new_mappingid);
			fireMappigUpdated(datasource_uri, mapping_id, mapping);
			return 0;
		} 
		// updates an existing mapping
		else {
			// updates the mapping without changing the mapping id
			if (new_mappingid.equals(mapping_id)) {
				return -1;
			} 
			// changes the mapping id to an existing one  
			else {
				throw new DuplicateMappingException(new_mappingid);
			}
		}
	}

	@Override
	public void updateTargetQueryMapping(URI datasource_uri, String mapping_id, CQIE targetQuery) {
		OBDAMappingAxiom mapping = getMapping(datasource_uri, mapping_id);
		if (mapping == null) {
			return;
		}
		mapping.setTargetQuery(targetQuery);
		fireMappigUpdated(datasource_uri, mapping.getId(), mapping);
	}

	@Override
	public boolean containsMapping(URI datasourceUri, String mappingId) {
		if (getMapping(datasourceUri, mappingId) != null) {
			return true;
		}
		return false;
	}

	@Override
	public void addMappings(URI datasource_uri, Collection<OBDAMappingAxiom> mappings) throws DuplicateMappingException {
		List<String> duplicates = new ArrayList<String>();
		for (OBDAMappingAxiom map : mappings) {
			try {
				addMapping(datasource_uri, map);
			} catch (DuplicateMappingException e) {
				duplicates.add(map.getId());
			}
		}
		if (duplicates.size() > 0) {
			String msg = String.format("Found %d duplicates in the following ids: %s", duplicates.size(), duplicates.toString());
			throw new DuplicateMappingException(msg);
		}
	}

	@Override
	public Object clone() {
		OBDAModel clone = dfac.getOBDAModel();
		for (OBDADataSource source : datasources.values()) {
			clone.addSource((OBDADataSource) source.clone());
			for (ArrayList<OBDAMappingAxiom> mappingList : mappings.values()) {
				for (OBDAMappingAxiom mapping : mappingList) {
					try {
						clone.addMapping(source.getSourceID(), (OBDAMappingAxiom) mapping.clone());
					} catch (DuplicateMappingException e) {
						// Does nothing
					}
				}
			}
		}
		return clone;
	}

	@Override
	public int renamePredicate(Predicate oldname, Predicate newName) {
		int modifiedCount = 0;
		for (OBDADataSource source : datasources.values()) {
			ArrayList<OBDAMappingAxiom> mp = mappings.get(source.getSourceID());
			for (OBDAMappingAxiom mapping : mp) {
				CQIE cq = (CQIE) mapping.getTargetQuery();
				List<Function> body = cq.getBody();
				for (int idx = 0; idx < body.size(); idx++) {
					Function oldatom = body.get(idx);
					if (!oldatom.getFunctionSymbol().equals(oldname)) {
						continue;
					}
					modifiedCount += 1;
					Function newatom = dfac.getFunction(newName, oldatom.getTerms());
					body.set(idx, newatom);
				}
				fireMappigUpdated(source.getSourceID(), mapping.getId(), mapping);
			}
		}
		return modifiedCount;
	}

	@Override
	public int deletePredicate(Predicate predicate) {
		int modifiedCount = 0;
		for (OBDADataSource source : datasources.values()) {
			List<OBDAMappingAxiom> mp = new ArrayList<OBDAMappingAxiom>(mappings.get(source.getSourceID()));
			for (OBDAMappingAxiom mapping : mp) {
				CQIE cq = (CQIE) mapping.getTargetQuery();
				List<Function> body = cq.getBody();
				for (int idx = 0; idx < body.size(); idx++) {
					Function oldatom = body.get(idx);
					if (!oldatom.getFunctionSymbol().equals(predicate)) {
						continue;
					}
					modifiedCount += 1;
					body.remove(idx);
				}
				if (body.size() != 0) {
					fireMappigUpdated(source.getSourceID(), mapping.getId(), mapping);
				} else {
					removeMapping(source.getSourceID(), mapping.getId());
				}
			}
		}
		return modifiedCount;
	}

	@Override
	public void reset() {
		log.debug("OBDA model is reset");
		prefixManager.clear();
		datasources.clear();
		mappings.clear();
	}

/*	
	@Override
	public Set<Predicate> getDeclaredPredicates() {
		LinkedHashSet<Predicate> result = new LinkedHashSet<Predicate>();
		result.addAll(declaredClasses);
		result.addAll(declaredObjectProperties);
		result.addAll(declaredDataProperties);
		result.addAll(declaredPredicates);
		return result;
	}
*/

	@Override
	public Set<OClass> getDeclaredClasses() {
		return Collections.unmodifiableSet(declaredClasses);
	}

	@Override
	public Set<ObjectPropertyExpression> getDeclaredObjectProperties() {
		return Collections.unmodifiableSet(declaredObjectProperties);
	}

	@Override
	public Set<DataPropertyExpression> getDeclaredDataProperties() {
		return Collections.unmodifiableSet(declaredDataProperties);
	}
/*
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
*/
	
	@Override
	public boolean declareClass(OClass classname) {
//		if (!classname.isClass()) {
//			throw new RuntimeException("Cannot declare a non-class predicate as a class. Offending predicate: " + classname);
//		}
		return declaredClasses.add(classname);
	}

	@Override
	public boolean declareObjectProperty(ObjectPropertyExpression property) {
//		if (!property.getPredicate().isObjectProperty()) {
//			throw new RuntimeException("Cannot declare a non-object property predicate as an object property. Offending predicate: " + property);
//		}
		return declaredObjectProperties.add(property);
	}

	@Override
	public boolean declareDataProperty(DataPropertyExpression property) {
//		if (!property.getPredicate().isDataProperty()) {
//			throw new RuntimeException("Cannot declare a non-data property predicate as an data property. Offending predicate: " + property);
//		}
		return declaredDataProperties.add(property);
	}
	
	@Override
	public void declareAll(OntologyVocabulary vocabulary) {
		for (OClass p : vocabulary.getClasses()) 
			declareClass(p);
		
		for (ObjectPropertyExpression p : vocabulary.getObjectProperties()) 
			declareObjectProperty(p);
		
		for (DataPropertyExpression p : vocabulary.getDataProperties()) 
			declareDataProperty(p);
	}
	
/*
	@Override
	public boolean unDeclarePredicate(Predicate predicate) {
		return declaredPredicates.remove(predicate);
	}
*/
	@Override
	public boolean unDeclareClass(OClass classname) {
		return declaredClasses.remove(classname);
	}

	@Override
	public boolean unDeclareObjectProperty(ObjectPropertyExpression property) {
		return declaredObjectProperties.remove(property);
	}

	@Override
	public boolean unDeclareDataProperty(DataPropertyExpression property) {
		return declaredDataProperties.remove(property);
	}
	
	@Override
	public boolean isDeclaredClass(OClass classname) {
		return declaredClasses.contains(classname);
	}

	@Override
	public boolean isDeclaredObjectProperty(ObjectPropertyExpression property) {
		return declaredObjectProperties.contains(property);
	}

	@Override
	public boolean isDeclaredDataProperty(DataPropertyExpression property) {
		return declaredDataProperties.contains(property);
	}

/*
	@Override
	public boolean isDeclared(Predicate predicate) {
		return (isDeclaredClass(predicate) || isDeclaredObjectProperty(predicate) || isDeclaredDataProperty(predicate) || declaredPredicates.contains(predicate));
	}
*/	
}
