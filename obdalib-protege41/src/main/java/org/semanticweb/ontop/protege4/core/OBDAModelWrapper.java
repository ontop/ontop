package org.semanticweb.ontop.protege4.core;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.semanticweb.ontop.exception.DuplicateMappingException;
import org.semanticweb.ontop.exception.InvalidMappingException;
import org.semanticweb.ontop.injection.NativeQueryLanguageComponentFactory;
import org.semanticweb.ontop.injection.OBDAFactoryWithException;
import org.semanticweb.ontop.io.InvalidDataSourceException;
import org.semanticweb.ontop.io.PrefixManager;
import org.semanticweb.ontop.mapping.MappingParser;
import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.ontology.DataPropertyExpression;
import org.semanticweb.ontop.ontology.OClass;
import org.semanticweb.ontop.ontology.ObjectPropertyExpression;
import org.semanticweb.ontop.ontology.OntologyVocabulary;
import org.semanticweb.ontop.protege4.panels.DatasourceSelector;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.*;

/**
 * Mutable wrapper that follows the previous implementation of OBDAModel.
 * The latter implementation is now immutable.
 *
 *
 * For the moment, this class always use the same factories
 * built according to INITIAL Quest preferences.
 * Late modified preferences are not taken into account.
 *
 */
public class OBDAModelWrapper {

    /**
     *  Immutable OBDA model.
     *  This variable is frequently re-affected.
     */
    private final NativeQueryLanguageComponentFactory nativeQLFactory;
    private final OBDAFactoryWithException obdaFactory;

    private OBDAModel obdaModel;
    private PrefixManagerWrapper prefixManager;

    private final List<OBDAModelListener> sourceListeners;
    private final List<OBDAMappingListener> mappingListeners;

    private static OBDADataFactory dataFactory = OBDADataFactoryImpl.getInstance();


    public OBDAModelWrapper(NativeQueryLanguageComponentFactory nativeQLFactory,
                            OBDAFactoryWithException obdaFactory, PrefixManagerWrapper prefixManager) {
        this.nativeQLFactory = nativeQLFactory;
        this.obdaFactory = obdaFactory;
        this.prefixManager = prefixManager;
        this.obdaModel = createNewOBDAModel(obdaFactory, prefixManager);
        this.sourceListeners = new ArrayList<>();
        this.mappingListeners = new ArrayList<>();
    }

    public OBDAModel getCurrentImmutableOBDAModel() {
        return obdaModel;
    }

    public void parseMappings(File mappingFile) throws DuplicateMappingException, InvalidMappingException, InvalidDataSourceException, IOException {
        MappingParser mappingParser = nativeQLFactory.create(mappingFile);
        obdaModel = mappingParser.getOBDAModel();
    }

    public PrefixManager getPrefixManager() {
        return obdaModel.getPrefixManager();
    }

    public ImmutableList<OBDAMappingAxiom> getMappings(URI sourceUri) {
        return obdaModel.getMappings(sourceUri);
    }

    public ImmutableMap<URI, ImmutableList<OBDAMappingAxiom>> getMappings() {
        return obdaModel.getMappings();
    }

    public ImmutableList<OBDADataSource> getSources() {
        return ImmutableList.copyOf(obdaModel.getSources());
    }

    public boolean containsSource(URI sourceURI) {
        return obdaModel.containsSource(sourceURI);
    }

    public OBDAMappingAxiom getMapping(String mappingId) {
        return obdaModel.getMapping(mappingId);
    }

    public void addPrefix(String prefix, String uri) {
        /**
         * The OBDA is still referencing this object
         */
        prefixManager.addPrefix(prefix, uri);
    }

    public void declareClass(OClass c) {
        obdaModel.declareClass(c);
    }

    public void declareObjectProperty(ObjectPropertyExpression r) {
        obdaModel.declareObjectProperty(r);
    }

    public void declareDataProperty(DataPropertyExpression p) {
        obdaModel.declareDataProperty(p);
    }

    public void unDeclareClass(OClass c) {
        obdaModel.unDeclareClass(c);
    }

    public void unDeclareObjectProperty(ObjectPropertyExpression r) {
        obdaModel.unDeclareObjectProperty(r);
    }

    public void unDeclareDataProperty(DataPropertyExpression p) {
        obdaModel.unDeclareDataProperty(p);
    }

    public int renamePredicate(Predicate removedPredicate, Predicate newPredicate) {
        int modifiedCount = 0;
        for (Map.Entry<URI, ImmutableList<OBDAMappingAxiom>> mappingEntry : obdaModel.getMappings().entrySet()) {
            URI sourceURI = mappingEntry.getKey();
            for (OBDAMappingAxiom mapping : mappingEntry.getValue()) {
                CQIE cq = (CQIE) mapping.getTargetQuery();
                List<Function> body = cq.getBody();
                for (int idx = 0; idx < body.size(); idx++) {
                    Function oldAtom = body.get(idx);
                    if (!oldAtom.getFunctionSymbol().equals(removedPredicate)) {
                        continue;
                    }
                    modifiedCount += 1;
                    Function newAtom = dataFactory.getFunction(newPredicate, oldAtom.getTerms());
                    body.set(idx, newAtom);
                }
                fireMappingUpdated(sourceURI, mapping.getId(), mapping);
            }
        }
        return modifiedCount;
    }

    private void fireMappingUpdated(URI sourceURI, String mappingId, OBDAMappingAxiom mapping) {
        for (OBDAMappingListener listener : mappingListeners) {
            listener.mappingUpdated(sourceURI, mappingId, mapping);
        }
    }

    public int deletePredicate(Predicate removedPredicate) {
        int modifiedCount = 0;
        for (Map.Entry<URI, ImmutableList<OBDAMappingAxiom>> mappingEntry : obdaModel.getMappings().entrySet()) {
            URI sourceURI = mappingEntry.getKey();
            for (OBDAMappingAxiom mapping : mappingEntry.getValue()) {
                CQIE cq = (CQIE) mapping.getTargetQuery();
                List<Function> body = cq.getBody();
                for (int idx = 0; idx < body.size(); idx++) {
                    Function oldatom = body.get(idx);
                    if (!oldatom.getFunctionSymbol().equals(removedPredicate)) {
                        continue;
                    }
                    modifiedCount += 1;
                    body.remove(idx);
                }
                if (body.size() != 0) {
                    fireMappingUpdated(sourceURI, mapping.getId(), mapping);
                } else {
                    removeMapping(sourceURI, mapping.getId());
                }
            }
        }
        return modifiedCount;
    }

    public void addSourceListener(OBDAModelListener listener) {
        if (sourceListeners.contains(listener)) {
            return;
        }
        sourceListeners.add(listener);
    }

    public void addMappingsListener(OBDAMappingListener mlistener) {
        if (mappingListeners.contains(mlistener))
            return;
        mappingListeners.add(mlistener);
    }

    private void fireSourceAdded(OBDADataSource source) {
        for (OBDAModelListener listener : sourceListeners) {
            listener.datasourceAdded(source);
        }
    }

    private void fireSourceRemoved(OBDADataSource source) {
        for (OBDAModelListener listener : sourceListeners) {
            listener.datasourceDeleted(source);
        }
    }

    /**
     * TODO: make it private
     */
    public void fireSourceParametersUpdated() {
        for (OBDAModelListener listener : sourceListeners) {
            listener.datasourcParametersUpdated();
        }
    }

    private void fireSourceNameUpdated(URI old, OBDADataSource newDataSource) {
        for (OBDAModelListener listener : sourceListeners) {
            listener.datasourceUpdated(old.toString(), newDataSource);
        }
    }

    public void reset() {
        obdaModel = createNewOBDAModel(obdaFactory, prefixManager);
    }

    /**
     * Updates the source. If its URI has changed, updates the mapping index.
     */
    public void updateSource(URI formerSourceId, OBDADataSource newSource) {
        OBDADataSource formerDataSource = obdaModel.getSource(formerSourceId);

        Set<OBDADataSource> newDataSources = new HashSet<>(obdaModel.getSources());

        if (formerDataSource != null) {
            newDataSources.remove(formerDataSource);
        }
        newDataSources.add(newSource);

        Map<URI, ImmutableList<OBDAMappingAxiom>> mappingIndex;

        /**
         * Source URI has been updated: update the mapping index
         */
        if (!formerSourceId.equals(newSource.getSourceID())) {
            mappingIndex = new HashMap<>(obdaModel.getMappings());
            ImmutableList<OBDAMappingAxiom> sourceMappings = mappingIndex.get(formerSourceId);
            if (sourceMappings != null) {
                mappingIndex.put(newSource.getSourceID(), sourceMappings);
                mappingIndex.remove(formerSourceId);
            }
        }
        else {
            mappingIndex = obdaModel.getMappings();
        }

        try {
            obdaModel = obdaModel.newModel(newDataSources, mappingIndex);
        } catch (DuplicateMappingException e) {
            throw new RuntimeException("Duplicated mappings should have been detected earlier.");
        }

        fireSourceNameUpdated(formerSourceId, newSource);
    }

    /**
     * Removes the source and the corresponding mappings.
     */
    public void removeSource(URI sourceID){
        OBDADataSource dataSource = obdaModel.getSource(sourceID);
        Set<OBDADataSource> newDataSources = new HashSet<>(obdaModel.getSources());

        if (dataSource != null) {
            newDataSources.remove(dataSource);
        }

        Map<URI, ImmutableList<OBDAMappingAxiom>> newMappingIndex = new HashMap<>(obdaModel.getMappings());
        newMappingIndex.remove(sourceID);

        try {
            obdaModel = obdaModel.newModel(newDataSources, newMappingIndex);
        } catch (DuplicateMappingException e) {
            throw new RuntimeException("Duplicated mappings should have been detected earlier.");
        }
        fireSourceRemoved(dataSource);
    }

    public void addSource(OBDADataSource ds) {
        Set<OBDADataSource> existingSources = obdaModel.getSources();

        if (existingSources.contains(ds))
            return;

        Set<OBDADataSource> newDataSources = new HashSet<>(existingSources);
        newDataSources.add(ds);

        try {
            obdaModel = obdaModel.newModel(newDataSources, obdaModel.getMappings());
        } catch (DuplicateMappingException e) {
            throw new RuntimeException("Duplicated mappings should have been detected earlier.");
        }
        fireSourceAdded(ds);
    }

    public Set<OClass> getDeclaredClasses() {
        return obdaModel.getDeclaredClasses();
    }

    public Set<DataPropertyExpression> getDeclaredDataProperties() {
        return obdaModel.getDeclaredDataProperties();
    }

    public Set<ObjectPropertyExpression> getDeclaredObjectProperties() {
        return obdaModel.getDeclaredObjectProperties();
    }

    public void removeSourcesListener(DatasourceSelector datasourceSelector) {
        sourceListeners.remove(datasourceSelector);
    }


    public void addMapping(URI sourceID, OBDAMappingAxiom mappingAxiom) throws DuplicateMappingException {
        ImmutableMap<URI, ImmutableList<OBDAMappingAxiom>> formerMappingIndex = obdaModel.getMappings();

        ImmutableList<OBDAMappingAxiom> sourceMappings = formerMappingIndex.get(sourceID);
        List<OBDAMappingAxiom> newSourceMappings;
        if (sourceMappings == null) {
            newSourceMappings = Arrays.asList(mappingAxiom);
        }
        else if (sourceMappings.contains(mappingAxiom)) {
            throw new DuplicateMappingException("ID " + mappingAxiom.getId());
        }
        else {
            newSourceMappings = new ArrayList<>(sourceMappings);
            newSourceMappings.add(mappingAxiom);
        }

        Map<URI, ImmutableList<OBDAMappingAxiom>> newMappingIndex = new HashMap<>(formerMappingIndex);
        newMappingIndex.put(sourceID, ImmutableList.copyOf(newSourceMappings));

        try {
            obdaModel = obdaModel.newModel(obdaModel.getSources(), newMappingIndex);
        } catch (DuplicateMappingException e) {
            throw new RuntimeException("Duplicated mappings should have been detected earlier.");
        }
        fireMappingInserted(sourceID, mappingAxiom.getId());
    }

    public void removeMapping(URI dataSourceURI, String mappingId) {
        OBDAMappingAxiom mapping = obdaModel.getMapping(mappingId);
        if (mapping == null)
            return;

        List<OBDAMappingAxiom> newSourceMappings = new LinkedList<>(obdaModel.getMappings(dataSourceURI));
        newSourceMappings.remove(mapping);

        Map<URI, ImmutableList<OBDAMappingAxiom>> newMappingIndex = new HashMap<>(obdaModel.getMappings());
        newMappingIndex.put(dataSourceURI, ImmutableList.copyOf(newSourceMappings));

        try {
            obdaModel = obdaModel.newModel(obdaModel.getSources(), ImmutableMap.copyOf(newMappingIndex));
        } catch (DuplicateMappingException e) {
            throw new RuntimeException("Duplicated mappings should have been detected earlier.");
        }

        fireMappingDeleted(dataSourceURI, mappingId);
    }

    public void updateMappingsSourceQuery(URI sourceURI, String mappingId, OBDASQLQuery nativeSourceQuery) {
        OBDAMappingAxiom mapping = getMapping(mappingId);
        // TODO: make it immutable
        mapping.setSourceQuery(nativeSourceQuery);
        fireMappingUpdated(sourceURI, mapping.getId(), mapping);
    }

    public void updateTargetQueryMapping(URI sourceID, String id, CQIE targetQuery) {
        OBDAMappingAxiom mapping = getMapping(id);
        if (mapping == null) {
            return;
        }
        // TODO: make it immutable
        mapping.setTargetQuery(targetQuery);
        fireMappingUpdated(sourceID, mapping.getId(), mapping);
    }

    public void updateMapping(URI dataSourceIRI, String formerMappingId, String newMappingId) {
        OBDAMappingAxiom mapping = getMapping(formerMappingId);
        if (mapping != null) {
            mapping.setId(newMappingId);
            fireMappingUpdated(dataSourceIRI, formerMappingId, mapping);
        }
    }

    public int indexOf(URI currentSource, String mappingId) {
        ImmutableList<OBDAMappingAxiom> sourceMappings = obdaModel.getMappings(currentSource);
        if (sourceMappings == null) {
            return -1;
        }

        for(int i=0; i < sourceMappings.size(); i++) {
            if (sourceMappings.get(i).getId() == mappingId)
                return i;
        }
        return -1;
    }

    /**
     * Announces to the listeners that a mapping was deleted.
     */
    private void fireMappingDeleted(URI srcuri, String mapping_id) {
        for (OBDAMappingListener listener : mappingListeners) {
            listener.mappingDeleted(srcuri, mapping_id);
        }
    }
    /**
     * Announces to the listeners that a mapping was inserted.
     */
    private void fireMappingInserted(URI srcuri, String mapping_id) {
        for (OBDAMappingListener listener : mappingListeners) {
            listener.mappingInserted(srcuri, mapping_id);
        }
    }

    private static OBDAModel createNewOBDAModel(OBDAFactoryWithException obdaFactory, PrefixManagerWrapper prefixManager) {
        try {
            return obdaFactory.createOBDAModel(ImmutableSet.<OBDADataSource>of(), ImmutableMap.<URI, ImmutableList<OBDAMappingAxiom>>of(),
                    prefixManager);
            /**
             * No mapping so should never happen
             */
        } catch(DuplicateMappingException e) {
            throw new RuntimeException("A DuplicateMappingException has been thrown while no mapping has been given." +
                    "What is going on? Message: " + e.getMessage());
        }
    }

    public void declareAll(OntologyVocabulary vocabulary) {
        for (OClass p : vocabulary.getClasses())
            declareClass(p);
        for (ObjectPropertyExpression p : vocabulary.getObjectProperties())
            declareObjectProperty(p);
        for (DataPropertyExpression p : vocabulary.getDataProperties())
            declareDataProperty(p);
    }
}
