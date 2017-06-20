package it.unibz.inf.ontop.protege.core;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.DuplicateMappingException;
import it.unibz.inf.ontop.exception.InvalidMappingException;
import it.unibz.inf.ontop.exception.MappingIOException;
import it.unibz.inf.ontop.injection.SQLPPMappingFactory;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.io.DataSource2PropertiesConvertor;
import it.unibz.inf.ontop.mapping.SQLMappingParser;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.impl.OntopNativeSQLPPTriplesMap;
import it.unibz.inf.ontop.ontology.OntologyFactory;
import it.unibz.inf.ontop.ontology.OntologyVocabulary;
import it.unibz.inf.ontop.ontology.impl.OntologyFactoryImpl;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static it.unibz.inf.ontop.model.impl.OntopModelSingletons.DATA_FACTORY;

/**
 *
 *
 * For the moment, this class always use the same factories
 * built according to INITIAL Quest preferences.
 * Late modified preferences are not taken into account.
 *
 *
 *
 *
 * An OBDA model contains mapping information.
 *
 * An OBDA model is a container for the database and mapping declarations needed to define a
 * Virtual ABox or Virtual RDF graph. That is, this is a manager for a
 * collection of JDBC databases (when SQL is the native query language) and their corresponding mappings.
 * It is used as input to any Quest instance (either OWLAPI or Sesame).
 *
 * <p>
 * OBDAModels are also used indirectly by the Protege plugin and many other
 * utilities including the mapping materializer (e.g. to generate ABox assertions or
 * RDF triples from a .obda file and a database).
 *
 * <p>
 *
 */
public class OBDAModel {

    private final static OntologyFactory ONTOLOGY_FACTORY = OntologyFactoryImpl.getInstance();
    private final SQLPPMappingFactory ppMappingFactory;
    private final SpecificationFactory specificationFactory;
    private Map<String, SQLPPTriplesMap> triplesMapMap;
    private Optional<OBDADataSource> source;

    private MutablePrefixManager prefixManager;

    private final List<OBDAModelListener> sourceListeners;
    private final List<OBDAMappingListener> mappingListeners;
    private final OntologyVocabulary ontologyVocabulary;

    public OBDAModel(SpecificationFactory specificationFactory,
                     SQLPPMappingFactory ppMappingFactory, MutablePrefixManager prefixManager) {
        this.specificationFactory = specificationFactory;
        this.ppMappingFactory = ppMappingFactory;
        this.prefixManager = prefixManager;
        this.triplesMapMap = new LinkedHashMap<>();
        this.sourceListeners = new ArrayList<>();
        this.mappingListeners = new ArrayList<>();
        source = Optional.empty();
        ontologyVocabulary = ONTOLOGY_FACTORY.createVocabulary();
    }

    public SQLPPMapping generatePPMapping() {
        ImmutableList<SQLPPTriplesMap> triplesMaps = ImmutableList.copyOf(triplesMapMap.values());

        try {
            UriTemplateMatcher uriTemplateMatcher = UriTemplateMatcher.create(
                    triplesMaps.stream()
                            .flatMap(ax -> ax.getTargetAtoms().stream())
                            .flatMap(atom -> atom.getArguments().stream())
                            .filter(t -> t instanceof ImmutableFunctionalTerm)
                            .map(t -> (ImmutableFunctionalTerm) t));

            return ppMappingFactory.createSQLPreProcessedMapping(triplesMaps,
                    // TODO: give an immutable prefix manager!!
                    specificationFactory.createMetadata(prefixManager,
                    uriTemplateMatcher));
            /**
             * No mapping so should never happen
             */
        } catch(DuplicateMappingException e) {
            throw new RuntimeException("A DuplicateMappingException has been thrown while no mapping has been given." +
                    "What is going on? Message: " + e.getMessage());
        }
    }

    /**
     * The sources and mappings are taken from the parsed model
     * and the ontology taken from the previous model
     *
     * UGLY!
     */
    public void parseMappings(File mappingFile) throws DuplicateMappingException, InvalidMappingException, IOException, MappingIOException {
        Properties properties = source
                .map(DataSource2PropertiesConvertor::convert)
                .orElseThrow(() -> new IllegalStateException("Cannot parse the mapping without a data source"));

        OntopSQLOWLAPIConfiguration configuration = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .properties(properties)
                .nativeOntopMappingFile(mappingFile)
                .build();
        SQLMappingParser mappingParser = configuration.getInjector().getInstance(SQLMappingParser.class);

        SQLPPMapping newObdaModel = mappingParser.parse(mappingFile);
        prefixManager.addPrefixes(newObdaModel.getMetadata().getPrefixManager().getPrefixMap());
        // New map
        triplesMapMap = newObdaModel.getTripleMaps().stream()
                .collect(collectTriplesMaps(
                        SQLPPTriplesMap::getId,
                        m -> m));
    }

    public MutablePrefixManager getMutablePrefixManager() {
        return prefixManager;
    }

    public ImmutableList<SQLPPTriplesMap> getMappings(URI sourceUri) {
        if (sourceUri.equals(getSourceId()))
            return ImmutableList.copyOf(triplesMapMap.values());
        else
            return ImmutableList.of();
    }

    public ImmutableList<OBDADataSource> getSources() {
        return source.isPresent()
                ? ImmutableList.of(source.get())
                : ImmutableList.of();
    }

    public SQLPPTriplesMap getMapping(String mappingId) {
        return triplesMapMap.get(mappingId);
    }

    public void addPrefix(String prefix, String uri) {
        /**
         * The OBDA is still referencing this object
         */
        prefixManager.addPrefix(prefix, uri);
    }


    public int renamePredicate(Predicate removedPredicate, Predicate newPredicate) {
        AtomicInteger counter = new AtomicInteger();

        triplesMapMap = triplesMapMap.entrySet().stream()
                .collect(collectTriplesMaps(
                        Map.Entry::getKey,
                        e -> renamePredicate(e.getValue(), removedPredicate, newPredicate, counter)));

        return counter.get();
    }

    private SQLPPTriplesMap renamePredicate(SQLPPTriplesMap formerTriplesMap,
                                            Predicate removedPredicate, Predicate newPredicate,
                                            AtomicInteger counter) {
        int formerCount = counter.get();

        ImmutableList<ImmutableFunctionalTerm> newTargetAtoms = formerTriplesMap.getTargetAtoms().stream()
                .map(a -> {
                    if (a.getFunctionSymbol().equals(removedPredicate)) {
                        counter.incrementAndGet();
                        return  DATA_FACTORY.getImmutableFunctionalTerm(newPredicate,
                                ImmutableList.copyOf(a.getArguments()));
                    }
                    return a;
                })
                .collect(ImmutableCollectors.toList());

        if (counter.get() > formerCount) {
            SQLPPTriplesMap newTriplesMap = new OntopNativeSQLPPTriplesMap(formerTriplesMap.getId(),
                    formerTriplesMap.getSourceQuery(), newTargetAtoms);

            fireMappingUpdated(getSourceId(), newTriplesMap.getId(), newTriplesMap);
            return newTriplesMap;
        }
        else
            return formerTriplesMap;

    }

    private void fireMappingUpdated(URI sourceURI, String mappingId, SQLPPTriplesMap mapping) {
        for (OBDAMappingListener listener : mappingListeners) {
            listener.mappingUpdated(sourceURI);
        }
    }

    public int deletePredicate(Predicate removedPredicate) {
        AtomicInteger counter = new AtomicInteger();

        triplesMapMap = triplesMapMap.values().stream()
                .map(m -> deletePredicate(m, removedPredicate, counter))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(collectTriplesMaps(
                        SQLPPTriplesMap::getId,
                        m -> m));

        return counter.get();
    }

    /**
     * TODO: find a better name
     */
    private Optional<SQLPPTriplesMap> deletePredicate(SQLPPTriplesMap formerTriplesMap, Predicate removedPredicate,
                                                      AtomicInteger counter) {
        int initialCount = counter.get();

        ImmutableList<ImmutableFunctionalTerm> newTargetAtoms = formerTriplesMap.getTargetAtoms().stream()
                .filter(a -> {
                    if (a.getFunctionSymbol().equals(removedPredicate)) {
                        counter.incrementAndGet();
                        return false;
                    }
                    return true;
                })
                .collect(ImmutableCollectors.toList());

        if (counter.get() > initialCount) {
            if (newTargetAtoms.isEmpty()) {
                removeMapping(getSourceId(), formerTriplesMap.getId());
                return Optional.empty();
            }
            else {
                SQLPPTriplesMap newTriplesMap = new OntopNativeSQLPPTriplesMap(formerTriplesMap.getId(),
                        formerTriplesMap.getSourceQuery(),
                        newTargetAtoms);
                fireMappingUpdated(getSourceId(), newTriplesMap.getId(), newTriplesMap);
                return Optional.of(newTriplesMap);
            }
        }
        else
            return Optional.of(formerTriplesMap);
    }

    private URI getSourceId() {
        return source
                .map(OBDADataSource::getSourceID)
                .orElseGet(() -> URI.create("ontop-data-source"));
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
            listener.datasourceParametersUpdated();
        }
    }

    private void fireSourceNameUpdated(URI old, OBDADataSource newDataSource) {
        for (OBDAModelListener listener : sourceListeners) {
            listener.datasourceUpdated(old.toString(), newDataSource);
        }
    }

    /**
     * Removes the mapping assertions
     */
    public void reset() {
        triplesMapMap.clear();
    }


    public void addMapping(URI sourceID, SQLPPTriplesMap triplesMap, boolean disableFiringMappingInsertedEvent)
            throws DuplicateMappingException {
        String mapId = triplesMap.getId();

        if (triplesMapMap.containsKey(mapId))
            throw new DuplicateMappingException("ID " + mapId);
        triplesMapMap.put(mapId, triplesMap);

        if (!disableFiringMappingInsertedEvent)
            fireMappingInserted(sourceID, mapId);
    }

    public void removeMapping(URI dataSourceURI, String mappingId) {
        if (triplesMapMap.remove(mappingId) != null)
            fireMappingDeleted(dataSourceURI, mappingId);
    }

    public void updateMappingsSourceQuery(URI sourceURI, String triplesMapId, OBDASQLQuery sourceQuery) {
        SQLPPTriplesMap formerTriplesMap = getMapping(triplesMapId);

        if (formerTriplesMap != null) {
            SQLPPTriplesMap newTriplesMap = new OntopNativeSQLPPTriplesMap(triplesMapId, sourceQuery,
                    formerTriplesMap.getTargetAtoms());
            triplesMapMap.put(triplesMapId, newTriplesMap);
            fireMappingUpdated(sourceURI, triplesMapId, newTriplesMap);
        }
    }

    public void updateTargetQueryMapping(URI sourceID, String id, ImmutableList<ImmutableFunctionalTerm> targetQuery) {
        SQLPPTriplesMap formerTriplesMap = getMapping(id);

        if (formerTriplesMap != null) {
            SQLPPTriplesMap newTriplesMap = new OntopNativeSQLPPTriplesMap(id, formerTriplesMap.getSourceQuery(),
                    targetQuery);
            triplesMapMap.put(id, newTriplesMap);
            fireMappingUpdated(sourceID, id, newTriplesMap);
        }
    }

    public void updateMapping(URI dataSourceIRI, String formerMappingId, String newMappingId) {
        SQLPPTriplesMap formerTriplesMap = getMapping(formerMappingId);

        if (formerTriplesMap != null) {
            SQLPPTriplesMap newTriplesMap = new OntopNativeSQLPPTriplesMap(newMappingId, formerTriplesMap.getSourceQuery(),
                    formerTriplesMap.getTargetAtoms());
            triplesMapMap.remove(formerMappingId);
            triplesMapMap.put(newMappingId, newTriplesMap);
            fireMappingUpdated(dataSourceIRI, formerMappingId, newTriplesMap);
        }
    }

    public int indexOf(URI currentSource, String mappingId) {
        ImmutableList<SQLPPTriplesMap> sourceMappings = ImmutableList.copyOf(triplesMapMap.values());
        if (sourceMappings == null) {
            return -1;
        }

        for(int i=0; i < sourceMappings.size(); i++) {
            if (sourceMappings.get(i).getId().equals(mappingId))
                return i;
        }
        return -1;
    }

    /**
     * Announces to the listeners that a mapping was deleted.
     */
    private void fireMappingDeleted(URI srcuri, String mapping_id) {
        for (OBDAMappingListener listener : mappingListeners) {
            listener.mappingDeleted(srcuri);
        }
    }
    /**
     * Announces to the listeners that a mapping was inserted.
     */
    private void fireMappingInserted(URI srcuri, String mapping_id) {
        for (OBDAMappingListener listener : mappingListeners) {
            listener.mappingInserted(srcuri);
        }
    }

    public Optional<OBDADataSource> getDatasource() {
        return source;
    }

    public void addSource(OBDADataSource currentDataSource) {
        source = Optional.of(currentDataSource);
    }

    public OntologyVocabulary getOntologyVocabulary() {
        return ontologyVocabulary;

    }

    private static <I> Collector<I, ?, LinkedHashMap<String, SQLPPTriplesMap>> collectTriplesMaps(
            java.util.function.Function<I, String> keyFunction,
            java.util.function.Function<I, SQLPPTriplesMap> mapFunction) {
        return Collectors.toMap(
                keyFunction,
                mapFunction,
                (u, v) -> {
                    throw new IllegalStateException(String.format("Duplicate key %s", u));
                },
                LinkedHashMap::new);
    }
}
