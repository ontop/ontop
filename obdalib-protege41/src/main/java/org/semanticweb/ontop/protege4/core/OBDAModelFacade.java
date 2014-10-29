package org.semanticweb.ontop.protege4.core;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.semanticweb.ontop.exception.DuplicateMappingException;
import org.semanticweb.ontop.injection.NativeQueryLanguageComponentFactory;
import org.semanticweb.ontop.io.PrefixManager;
import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.protege4.panels.DatasourceSelector;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * TODO: describe
 */
public class OBDAModelFacade {

    /**
     *  Immutable OBDA model.
     *  This variable is frequently reaffected.
     */
    private OBDAModel obdaModel;
    private final NativeQueryLanguageComponentFactory nativeQLFactory;
    private PrefixManagerWrapper prefixManager;

    public OBDAModelFacade(NativeQueryLanguageComponentFactory nativeQLFactory) {
        this.nativeQLFactory = nativeQLFactory;
    }

    public OBDAModel getCurrentImmutableOBDAModel() {
        return obdaModel;
    }


    public PrefixManager getPrefixManager() {
        return obdaModel.getPrefixManager();
    }

    public OBDAMappingAxiom getMapping(String mappingId) {
        return obdaModel.getMapping(mappingId);
    }

    public ImmutableList<OBDAMappingAxiom> getMappings(URI sourceUri) {
        return obdaModel.getMappings(sourceUri);
    }

    public ImmutableMap<URI, ImmutableList<OBDAMappingAxiom>> getMappings() {
        return obdaModel.getMappings();
    }

    public OBDAModel newModel(Set<OBDADataSource> dataSources,
                              Map<URI, ImmutableList<OBDAMappingAxiom>> newMappings) throws DuplicateMappingException {
        return obdaModel.newModel(dataSources,
                newMappings);
    }

    public OBDAModel newModel(Set<OBDADataSource> dataSources,
                              Map<URI, ImmutableList<OBDAMappingAxiom>> newMappings, PrefixManager prefixManager) throws DuplicateMappingException {
        return obdaModel.newModel(dataSources,
                newMappings, prefixManager);
    }

    public ImmutableList<OBDADataSource> getSources() {
        return ImmutableList.copyOf(obdaModel.getSources());
    }

    public OBDADataSource getSource(URI sourceURI) {
        return obdaModel.getSource(sourceURI);
    }

    public boolean containsSource(URI sourceURI) {
        return obdaModel.containsSource(sourceURI);
    }

    @Deprecated
    public String getVersion() {
        return obdaModel.getVersion();
    }

    @Deprecated
    public String getBuiltDate() {
        return obdaModel.getBuiltDate();
    }

    @Deprecated
    public String getBuiltBy() {
        return obdaModel.getBuiltBy();
    }

    public OBDAMappingAxiom getMapping(URI sourceUri, String mappingId) {
        return obdaModel.getMapping(sourceUri, mappingId);
    }

    public void addPrefix(String prefix, String uri) {
        /**
         * The OBDA is still referencing this object
         */
        prefixManager.addPrefix(prefix, uri);
    }

    public void declareClass(Predicate c) {
    }

    public void declareObjectProperty(Predicate r) {
    }

    public void declareDataProperty(Predicate p) {
    }

    public void unDeclareClass(Predicate c) {
    }

    public void unDeclareObjectProperty(Predicate r) {
    }

    public void unDeclareDataProperty(Predicate p) {
    }

    public void renamePredicate(Predicate removedPredicate, Predicate newPredicate) {
    }

    public void deletePredicate(Predicate removedPredicate) {
    }

    public void addSourcesListener(OBDAModelListener listener) {
    }

    public void addMappingsListener(OBDAMappingListener mlistener) {
    }

    public void setPrefixManager(PrefixManagerWrapper prefixManager) {
        this.prefixManager = prefixManager;

        try {
            obdaModel = obdaModel.newModel(obdaModel.getSources(), obdaModel.getMappings(),
                    prefixManager);
        } catch (DuplicateMappingException e) {
            throw new RuntimeException("Duplicate mappings should have been detected earlier!");
        }
    }

    public void reset() {

    }

    public void updateSource(URI sourceID, OBDADataSource aux) {
    }

    public void addSource(OBDADataSource ds) {
    }

    @Deprecated
    public Predicate[] getDeclaredClasses() {
        return new Predicate[0];
    }

    public void fireSourceParametersUpdated() {
    }

    @Deprecated
    public Predicate[] getDeclaredDataProperties() {
        return new Predicate[0];
    }

    @Deprecated
    public Predicate[] getDeclaredObjectProperties() {
        return new Predicate[0];
    }

    public void removeSourcesListener(DatasourceSelector datasourceSelector) {

    }


    public void addMapping(URI sourceID, OBDAMappingAxiom mappingAxiom) throws DuplicateMappingException {
    }

    public void removeMapping(URI srcuri, String id) {
    }

    public void updateMappingsSourceQuery(URI sourceID, String id, OBDASQLQuery body) {
        
    }

    public void updateTargetQueryMapping(URI sourceID, String id, CQIE targetQuery) {
    }

    public void updateMapping(URI sourceID, String id, String trim) {
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

    public void removeSource(URI sourceID) {

    }
}
