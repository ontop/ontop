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
import org.semanticweb.ontop.protege4.panels.DatasourceSelector;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.util.Map;
import java.util.Set;

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

    public OBDAModelWrapper(NativeQueryLanguageComponentFactory nativeQLFactory,
                            OBDAFactoryWithException obdaFactory, PrefixManagerWrapper prefixManager) {
        this.nativeQLFactory = nativeQLFactory;
        this.obdaFactory = obdaFactory;
        this.prefixManager = prefixManager;
        this.obdaModel = createNewOBDAModel(obdaFactory, prefixManager);
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

//    public OBDAModel newModel(Set<OBDADataSource> dataSources,
//                              Map<URI, ImmutableList<OBDAMappingAxiom>> newMappings) throws DuplicateMappingException {
//        return obdaModel.newModel(dataSources,
//                newMappings);
//    }
//
//    public OBDAModel newModel(Set<OBDADataSource> dataSources,
//                              Map<URI, ImmutableList<OBDAMappingAxiom>> newMappings, PrefixManager prefixManager) throws DuplicateMappingException {
//        return obdaModel.newModel(dataSources,
//                newMappings, prefixManager);
//    }

    public ImmutableList<OBDADataSource> getSources() {
        return ImmutableList.copyOf(obdaModel.getSources());
    }

    public boolean containsSource(URI sourceURI) {
        return obdaModel.containsSource(sourceURI);
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
        //TODO: implement it
    }

    public void declareObjectProperty(Predicate r) {
    }

    public void declareDataProperty(Predicate p) {
        //TODO: implement it
    }

    public void unDeclareClass(Predicate c) {
        //TODO: implement it
    }

    public void unDeclareObjectProperty(Predicate r) {
        //TODO: implement it
    }

    public void unDeclareDataProperty(Predicate p) {
        //TODO: implement it
    }

    public void renamePredicate(Predicate removedPredicate, Predicate newPredicate) {
        //TODO: implement it
    }

    public void deletePredicate(Predicate removedPredicate) {
        //TODO: implement it
    }

    public void addSourcesListener(OBDAModelListener listener) {
        //TODO: implement it
    }

    public void addMappingsListener(OBDAMappingListener mlistener) {
        //TODO: implement it
    }

    public void reset() {
        obdaModel = createNewOBDAModel(obdaFactory, prefixManager);
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

    public void updateSource(URI sourceID, OBDADataSource aux) {
        //TODO: implement it
    }

    public void addSource(OBDADataSource ds) {
        //TODO: implement it
    }

    public Predicate[] getDeclaredClasses() {
        return new Predicate[0];
    }

    public void fireSourceParametersUpdated() {
        //TODO: implement it
    }

    public Predicate[] getDeclaredDataProperties() {
        return new Predicate[0];
    }

    public Predicate[] getDeclaredObjectProperties() {
        return new Predicate[0];
    }

    public void removeSourcesListener(DatasourceSelector datasourceSelector) {
        //TODO: implement it

    }


    public void addMapping(URI sourceID, OBDAMappingAxiom mappingAxiom) throws DuplicateMappingException {
        //TODO: implement it
    }

    public void removeMapping(URI srcuri, String id) {
        //TODO: implement it
    }

    public void updateMappingsSourceQuery(URI sourceID, String id, OBDASQLQuery body) {
        //TODO: implement it
    }

    public void updateTargetQueryMapping(URI sourceID, String id, CQIE targetQuery) {
        //TODO: implement it
    }

    public void updateMapping(URI sourceID, String id, String trim) {
        //TODO: implement it
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
        //TODO: implement it
    }
}
