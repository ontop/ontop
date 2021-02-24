package it.unibz.inf.ontop.protege.mapping;

import com.google.common.collect.ImmutableList;
import com.google.inject.Injector;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.exception.TargetQueryParserException;
import it.unibz.inf.ontop.injection.OntopMappingSQLAllConfiguration;
import it.unibz.inf.ontop.injection.SQLPPMappingFactory;
import it.unibz.inf.ontop.injection.TargetQueryParserFactory;
import it.unibz.inf.ontop.model.term.IRIConstant;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.protege.core.OntologyPrefixManager;
import it.unibz.inf.ontop.protege.core.OBDAModel;
import it.unibz.inf.ontop.protege.core.OldSyntaxMappingConverter;
import it.unibz.inf.ontop.protege.utils.EventListenerList;
import it.unibz.inf.ontop.spec.mapping.*;
import it.unibz.inf.ontop.spec.mapping.parser.SQLMappingParser;
import it.unibz.inf.ontop.spec.mapping.parser.TargetQueryParser;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.spec.mapping.serializer.impl.OntopNativeMappingSerializer;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.*;
import java.nio.file.Files;
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
public class TriplesMapCollection implements Iterable<TriplesMap> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TriplesMapCollection.class);
    
    private Map<String, TriplesMap> map = new LinkedHashMap<>();
    // Mutable and replaced after reset
    private final OntologyPrefixManager prefixManager;

    private final SQLPPMappingFactory ppMappingFactory;
    private final TermFactory termFactory;
    private final TargetQueryParserFactory targetQueryParserFactory;

    final TargetAtomFactory targetAtomFactory;
    final SubstitutionFactory substitutionFactory;
    final SQLPPSourceQueryFactory sourceQueryFactory;

    private final EventListenerList<TriplesMapCollectionListener> listeners = new EventListenerList<>();

    public TriplesMapCollection(OntologyPrefixManager prefixManager) {

        this.prefixManager = prefixManager;

        /*
         * TODO: avoid using Default injector
         */
        OntopMappingSQLAllConfiguration configuration = OntopMappingSQLAllConfiguration.defaultBuilder()
                .jdbcDriver("")
                .jdbcUrl("")
                .jdbcUser("")
                .jdbcPassword("")
                .build();

        Injector injector = configuration.getInjector();
        ppMappingFactory = injector.getInstance(SQLPPMappingFactory.class);
        termFactory = injector.getInstance(TermFactory.class);
        targetAtomFactory = injector.getInstance(TargetAtomFactory.class);
        substitutionFactory = injector.getInstance(SubstitutionFactory.class);
        targetQueryParserFactory = injector.getInstance(TargetQueryParserFactory.class);
        sourceQueryFactory = injector.getInstance(SQLPPSourceQueryFactory.class);
    }

    /**
     * No need to remove listeners - this is handled by OBDAModelManager
     * @param listener
     */

    public void addListener(TriplesMapCollectionListener listener) {
        listeners.add(listener);
    }


    public SQLPPMapping generatePPMapping() {
        ImmutableList<SQLPPTriplesMap> triplesMaps = map.values().stream()
                .map(TriplesMap::asSQLPPTriplesMap)
                .collect(ImmutableCollectors.toList());

        return ppMappingFactory.createSQLPreProcessedMapping(triplesMaps,
                // TODO: give an immutable prefix manager!!
                prefixManager);
    }

    OntologyPrefixManager getMutablePrefixManager() {
        return prefixManager;
    }

    public ImmutableList<TargetAtom> parseTargetQuery(String target) throws TargetQueryParserException {
        TargetQueryParser textParser = targetQueryParserFactory.createParser(prefixManager);
        return textParser.parse(target);
    }


    public void renamePredicate(IRI predicateIri, IRI newPredicateIri) {

        if (map.values().stream().anyMatch(m -> m.containsIri(predicateIri))) {
            // We build a ground term for the IRI
            IRIConstant replacementTerm = termFactory.getConstantIRI(newPredicateIri);

            map = map.values().stream()
                    .map(m -> m.renamePredicate(predicateIri, replacementTerm))
                    .collect(toIndexedTripleMaps());

            listeners.fire(l -> l.triplesMapCollectionChanged(this));
        }
    }

    public void removePredicate(IRI predicateIri) {

        if (map.values().stream().anyMatch(m -> m.containsIri(predicateIri))) {
            map = map.values().stream()
                    .map(m -> m.removePredicate(predicateIri))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(toIndexedTripleMaps());

            listeners.fire(l -> l.triplesMapCollectionChanged(this));
        }
    }

    public void add(String id, String sqlQuery, String target) throws DuplicateTriplesMapException, TargetQueryParserException {
        if (map.containsKey(id))
            throw new DuplicateTriplesMapException(ImmutableList.of(id));

        map.put(id, new TriplesMap(id, sqlQuery, parseTargetQuery(target), this));
        listeners.fire(l -> l.triplesMapCollectionChanged(this));
    }

    public void addAll(ImmutableList<SQLPPTriplesMap> list) throws DuplicateTriplesMapException {

        List<String> duplicateIds = new ArrayList<>();
        for (SQLPPTriplesMap triplesMap : list) {
            String id = triplesMap.getId();
            if (map.containsKey(id))
                duplicateIds.add(id);
            else
                map.put(id, new TriplesMap(triplesMap, this));
        }
        if (!duplicateIds.isEmpty())
            throw new DuplicateTriplesMapException(duplicateIds);

        listeners.fire(l -> l.triplesMapCollectionChanged(this));
    }

    public void duplicate(String id) {
        TriplesMap triplesMap = map.get(id);
        if (triplesMap == null)
            throw new MinorOntopInternalBugException("Triples map not found: " + id);

        String newId = generateFreshId(id);
        map.put(newId, triplesMap.createDuplicate(newId));
        listeners.fire(l -> l.triplesMapCollectionChanged(this));
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

        TriplesMap replacement = new TriplesMap(newId, sqlQuery, parseTargetQuery(target), this);
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
        listeners.fire(l -> l.triplesMapCollectionChanged(this));
    }

    public void remove(String id) {
        if (map.remove(id) == null)
            throw new MinorOntopInternalBugException("Triples map not found: " + id);

        listeners.fire(l -> l.triplesMapCollectionChanged(this));
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
        listeners.fire(l -> l.triplesMapCollectionChanged(this));
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


    /**
     *  CAN BE CALLED ONLY ONCE: ADDS TO THE CURRENT PREFIX MANAGER
     */

    public void load(File obdaFile, OBDAModel obdaModel) throws Exception {
        try (Reader reader = new FileReader(obdaFile)) {
            OldSyntaxMappingConverter converter = new OldSyntaxMappingConverter(reader, obdaFile.getName());

            converter.getDataSourceProperties().ifPresent(obdaModel.getDataSource()::update);

            Reader mappingReader = new StringReader(converter.getRestOfFile());
            SQLMappingParser mappingParser = obdaModel.getConfigurationManager()
                    .getSQLMappingParser(obdaModel.getDataSource(), mappingReader);
            SQLPPMapping ppMapping = mappingParser.parse(mappingReader);

            ppMapping.getPrefixManager().getPrefixMap().forEach(prefixManager::addPrefix);

            map = ppMapping.getTripleMaps().stream()
                    .map(m -> new TriplesMap(m, this))
                    .collect(toIndexedTripleMaps());

            listeners.fire(l -> l.triplesMapCollectionChanged(this));
        }
        catch (Exception ex) {
            throw new Exception("Exception occurred while loading OBDA document: " + obdaFile + "\n\n" + ex.getMessage());
        }
    }

    public void store(File obdaFile) throws IOException {
        if (!map.isEmpty()) {
            OntopNativeMappingSerializer writer = new OntopNativeMappingSerializer();
            writer.write(obdaFile, generatePPMapping());
            LOGGER.info("mapping file saved to {}", obdaFile);
        }
        else {
            Files.deleteIfExists(obdaFile.toPath());
        }
    }
}
