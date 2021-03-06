package it.unibz.inf.ontop.protege.mapping;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.exception.TargetQueryParserException;
import it.unibz.inf.ontop.protege.core.OntologyPrefixManager;
import it.unibz.inf.ontop.protege.core.OBDAModel;
import it.unibz.inf.ontop.protege.core.OldSyntaxMappingConverter;
import it.unibz.inf.ontop.protege.utils.DialogUtils;
import it.unibz.inf.ontop.protege.utils.EventListenerList;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.spec.mapping.serializer.impl.OntopNativeMappingSerializer;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.*;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * For the moment, this class always use the same factories
 * built according to INITIAL Quest preferences.
 * Late modified preferences are not taken into account.
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
public class TriplesMapManager implements Iterable<TriplesMap> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TriplesMapManager.class);
    
    private Map<String, TriplesMap> map = new LinkedHashMap<>();
    // reflects the ontology prefix manager
    private final OntologyPrefixManager prefixManager;

    private final TriplesMapFactory triplesMapFactory;

    private final EventListenerList<TriplesMapManagerListener> listeners = new EventListenerList<>();

    public TriplesMapManager(TriplesMapFactory triplesMapFactory, OntologyPrefixManager prefixManager) {
        this.triplesMapFactory = triplesMapFactory;
        this.prefixManager = prefixManager;
    }


    /**
     * No need to remove listeners - this is handled by OBDAModelManager
     * @param listener
     */

    public void addListener(TriplesMapManagerListener listener) {
        listeners.add(listener);
    }


    public SQLPPMapping generatePPMapping() {
        ImmutableList<SQLPPTriplesMap> triplesMaps = map.values().stream()
                .map(TriplesMap::asSQLPPTriplesMap)
                .collect(ImmutableCollectors.toList());

        return triplesMapFactory.createSQLPreProcessedMapping(triplesMaps);
    }


    public void renamePredicate(IRI predicateIri, IRI newPredicateIri) {

        if (map.values().stream().anyMatch(m -> m.containsIri(predicateIri))) {
            map = map.values().stream()
                    .map(m -> m.renamePredicate(predicateIri, newPredicateIri))
                    .collect(toIndexedTripleMaps());

            listeners.fire(l -> l.changed(this));
        }
    }

    public void removePredicate(IRI predicateIri) {

        if (map.values().stream().anyMatch(m -> m.containsIri(predicateIri))) {
            map = map.values().stream()
                    .map(m -> m.removePredicate(predicateIri))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(toIndexedTripleMaps());

            listeners.fire(l -> l.changed(this));
        }
    }

    public void add(String id, String sqlQuery, String target) throws DuplicateTriplesMapException, TargetQueryParserException {
        if (map.containsKey(id))
            throw new DuplicateTriplesMapException(ImmutableList.of(id));

        map.put(id, new TriplesMap(id, sqlQuery, triplesMapFactory.getTargetQuery(target), triplesMapFactory));
        listeners.fire(l -> l.changed(this));
    }

    public void addAll(ImmutableList<SQLPPTriplesMap> list) throws DuplicateTriplesMapException {

        List<String> duplicateIds = new ArrayList<>();
        for (SQLPPTriplesMap triplesMap : list) {
            String id = triplesMap.getId();
            if (map.containsKey(id))
                duplicateIds.add(id);
            else
                map.put(id, new TriplesMap(triplesMap, triplesMapFactory));
        }
        if (!duplicateIds.isEmpty())
            throw new DuplicateTriplesMapException(duplicateIds);

        listeners.fire(l -> l.changed(this));
    }

    public void duplicate(String id) {
        TriplesMap triplesMap = map.get(id);
        if (triplesMap == null)
            throw new MinorOntopInternalBugException("Triples map not found: " + id);

        String newId = generateFreshId(id);
        map.put(newId, triplesMap.createDuplicate(newId));
        listeners.fire(l -> l.changed(this));
    }

    private String generateFreshId(String id) {
        for (int index = 0; index < 999999999; index++) {
            String newId = id + "(" + index + ")";
            if (!map.containsKey(newId))
                return newId;
        }
        throw new MinorOntopInternalBugException("Unable to generate a fresh triples map ID from " + id);
    }


    public void update(String id, String newId, String sqlQuery, String target) throws DuplicateTriplesMapException, TargetQueryParserException {
        if (!map.containsKey(id))
            throw new MinorOntopInternalBugException("Triples map not found: " + id);

        TriplesMap replacement = new TriplesMap(newId, sqlQuery, triplesMapFactory.getTargetQuery(target), triplesMapFactory);
        if (newId.equals(id)) {
            map.put(id, replacement);
        }
        else {
            if (map.containsKey(newId))
                throw new DuplicateTriplesMapException(ImmutableList.of(newId));

            map = map.values().stream()
                    .map(m -> m.getId().equals(id) ? replacement : m)
                    .collect(toIndexedTripleMaps());
        }
        listeners.fire(l -> l.changed(this));
    }

    public void remove(String id) {
        if (map.remove(id) == null)
            throw new MinorOntopInternalBugException("Triples map not found: " + id);

        listeners.fire(l -> l.changed(this));
    }

    public void setStatus(String id, TriplesMap.Status status, String sqlErrorMessage, ImmutableList<String> invalidPlaceholders) {
        TriplesMap triplesMap = map.get(id);
        if (triplesMap == null)
            throw new MinorOntopInternalBugException("Triples map not found: " + id);

        triplesMap.setStatus(status);
        if (sqlErrorMessage != null) {
            if (status != TriplesMap.Status.INVALID)
                throw new MinorOntopInternalBugException("Invalid state for an SQL error message: " + sqlErrorMessage);

            triplesMap.setSqlErrorMessage(sqlErrorMessage);
        }
        if (!invalidPlaceholders.isEmpty()) {
            if (status != TriplesMap.Status.INVALID)
                throw new MinorOntopInternalBugException("Invalid state for a non-empty list of invalid placeholders: " + invalidPlaceholders);

            triplesMap.setInvalidPlaceholders(invalidPlaceholders);
        }
        listeners.fire(l -> l.changed(this));
    }

    private static Collector<TriplesMap, ?, LinkedHashMap<String, TriplesMap>> toIndexedTripleMaps() {
        return Collectors.toMap(TriplesMap::getId, m -> m,
                (id1, id2) -> { throw new IllegalStateException("Duplicate triples map ID: " + id1); },
                LinkedHashMap::new);
    }

    @Nonnull
    @Override
    public Iterator<TriplesMap> iterator() {
        return map.values().iterator();
    }

    public int size() { return map.size(); }

    public Stream<TriplesMap> stream() { return map.values().stream(); }


    public void clear() {
        map.clear();
    }

    /**
     *  Assumes that the file exists.
     *
     *  Adds mapping prefixes to the current ontology prefixes
     *  but calling it twice in a row is safe
     *  provided that the ontology has been reloaded too
     */

    public void load(File obdaFile, OBDAModel obdaModel) throws Exception {
        try (Reader reader = new FileReader(obdaFile)) {
            OldSyntaxMappingConverter converter = new OldSyntaxMappingConverter(reader, obdaFile.getName());

            converter.getDataSourceProperties().ifPresent(obdaModel.getDataSource()::update);

            SQLPPMapping ppMapping = obdaModel
                    .parseNativeMapping(new StringReader(converter.getRestOfFile()));

            ppMapping.getPrefixManager().getPrefixMap().forEach(prefixManager::addPrefix);

            map = ppMapping.getTripleMaps().stream()
                    .map(m -> new TriplesMap(m, obdaModel.getTriplesMapFactory()))
                    .collect(toIndexedTripleMaps());

            listeners.fire(l -> l.changed(this));
        }
        catch (Exception ex) {
            throw new Exception("Exception occurred while loading OBDA document: " + obdaFile + "\n\n" + ex.getMessage());
        }
    }

    public void store(File obdaFile) throws IOException {
        DialogUtils.saveFileOrDeleteEmpty(map.isEmpty(), obdaFile, file -> {
            OntopNativeMappingSerializer writer = new OntopNativeMappingSerializer();
            writer.write(file, generatePPMapping());
        }, LOGGER);
    }
}
