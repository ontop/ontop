package it.unibz.inf.ontop.protege.core;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import it.unibz.inf.ontop.exception.InvalidMappingException;
import it.unibz.inf.ontop.exception.MappingIOException;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.exception.TargetQueryParserException;
import it.unibz.inf.ontop.injection.OntopMappingSQLAllConfiguration;
import it.unibz.inf.ontop.injection.SQLPPMappingFactory;
import it.unibz.inf.ontop.injection.TargetQueryParserFactory;
import it.unibz.inf.ontop.model.atom.*;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.spec.mapping.*;
import it.unibz.inf.ontop.spec.mapping.parser.SQLMappingParser;
import it.unibz.inf.ontop.spec.mapping.parser.TargetQueryParser;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.spec.mapping.pp.impl.OntopNativeSQLPPTriplesMap;
import it.unibz.inf.ontop.spec.mapping.serializer.impl.TargetQueryRenderer;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.IRI;
import org.semanticweb.owlapi.model.OWLOntology;

import java.io.Reader;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

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

    private Map<String, SQLPPTriplesMap> map = new LinkedHashMap<>();
    // Mutable and replaced after reset
    private MutablePrefixManager prefixManager;

    private final List<OBDAMappingListener> mappingListeners = new ArrayList<>();

    private final SQLPPMappingFactory ppMappingFactory;
    private final TermFactory termFactory;
    private final TargetAtomFactory targetAtomFactory;
    private final SubstitutionFactory substitutionFactory;
    private final TargetQueryParserFactory targetQueryParserFactory;
    private final SQLPPSourceQueryFactory sourceQueryFactory;

    public OBDAModel(OWLOntology ontology,
                     SQLPPMappingFactory ppMappingFactory,
                     TermFactory termFactory,
                     TargetAtomFactory targetAtomFactory,
                     SubstitutionFactory substitutionFactory,
                     TargetQueryParserFactory targetQueryParserFactory,
                     SQLPPSourceQueryFactory sourceQueryFactory) {
        this.prefixManager = new MutablePrefixManager(ontology);

        this.ppMappingFactory = ppMappingFactory;
        this.termFactory = termFactory;
        this.targetAtomFactory = targetAtomFactory;
        this.substitutionFactory = substitutionFactory;
        this.targetQueryParserFactory = targetQueryParserFactory;
        this.sourceQueryFactory = sourceQueryFactory;
    }

    public SQLPPMapping generatePPMapping() {
        ImmutableList<SQLPPTriplesMap> triplesMaps = ImmutableList.copyOf(map.values());

        return ppMappingFactory.createSQLPreProcessedMapping(triplesMaps,
                // TODO: give an immutable prefix manager!!
                prefixManager);
    }


    public void parseMapping(Reader mappingReader, Properties properties) throws InvalidMappingException, MappingIOException {

        OntopMappingSQLAllConfiguration configuration = OntopMappingSQLAllConfiguration.defaultBuilder()
                .nativeOntopMappingReader(mappingReader)
                .properties(properties)
                .build();

        SQLMappingParser mappingParser = configuration.getInjector().getInstance(SQLMappingParser.class);

        SQLPPMapping ppMapping = mappingParser.parse(mappingReader);
        ppMapping.getPrefixManager().getPrefixMap().forEach((k, v) -> prefixManager.addPrefix(k, v));

        map = ppMapping.getTripleMaps().stream()
                .collect(collectTriplesMaps(
                        SQLPPTriplesMap::getId,
                        m -> m));
    }

    /**
     * DO NOT CACHE: A NEW INSTANCE IS CREATED FOR EACH ONTOLOGY
     * @return
     */

    public MutablePrefixManager getMutablePrefixManager() {
        return prefixManager;
    }

    public Collection<SQLPPTriplesMap> getMapping() {
        return map.values();
    }

    public boolean containsMappingId(String mappingId) {
        return map.containsKey(mappingId);
    }

    public ImmutableList<TargetAtom> parseTargetQuery(String target) throws TargetQueryParserException {
        TargetQueryParser textParser = targetQueryParserFactory.createParser(prefixManager);
        return textParser.parse(target);
    }

    public String getTargetRendering(SQLPPTriplesMap mapping) {
        TargetQueryRenderer targetQueryRenderer = new TargetQueryRenderer(prefixManager);
        return targetQueryRenderer.encode(mapping.getTargetAtoms());
    }


    public void renamePredicateInMapping(IRI removedPredicateIri, IRI newPredicatIri) {
        AtomicInteger counter = new AtomicInteger();
        map = map.entrySet().stream()
                .collect(collectTriplesMaps(
                        Map.Entry::getKey,
                        e -> renamePredicateInMapping(e.getValue(), removedPredicateIri, newPredicatIri, counter)));
    }

    private SQLPPTriplesMap renamePredicateInMapping(SQLPPTriplesMap formerTriplesMap,
                                                     IRI removedIRI, IRI newIRI,
                                                     AtomicInteger counter) {
        int formerCount = counter.get();

        ImmutableList<TargetAtom> newTargetAtoms = formerTriplesMap.getTargetAtoms().stream()
                .map(a -> {
                    if (a.getPredicateIRI()
                            .filter(i -> i.equals(removedIRI))
                            .isPresent()) {

                        DistinctVariableOnlyDataAtom projectionAtom = a.getProjectionAtom();
                        RDFAtomPredicate predicate = (RDFAtomPredicate) projectionAtom.getPredicate();

                        boolean isClass = predicate.getClassIRI(a.getSubstitutedTerms())
                                .isPresent();

                        Variable predicateVariable = isClass
                                ? predicate.getObject(projectionAtom.getArguments())
                                : predicate.getProperty(projectionAtom.getArguments());

                        ImmutableSubstitution<ImmutableTerm> newSubstitution = substitutionFactory.getSubstitution(
                                a.getSubstitution().getImmutableMap().entrySet().stream()
                                        .map(e -> e.getKey().equals(predicateVariable)
                                                ? Maps.immutableEntry(predicateVariable,
                                                // We build a ground term for the IRI
                                                (ImmutableTerm) termFactory.getConstantIRI(newIRI))
                                                : e)
                                        .collect(ImmutableCollectors.toMap()));

                        counter.incrementAndGet();
                        return targetAtomFactory.getTargetAtom(projectionAtom, newSubstitution);
                    }
                    return a;
                })
                .collect(ImmutableCollectors.toList());

        if (counter.get() > formerCount) {
            SQLPPTriplesMap newTriplesMap = new OntopNativeSQLPPTriplesMap(formerTriplesMap.getId(),
                    formerTriplesMap.getSourceQuery(), newTargetAtoms);

            mappingListeners.forEach(OBDAMappingListener::mappingUpdated);
            return newTriplesMap;
        }
        else
            return formerTriplesMap;
    }

    public void removePredicateFromMapping(IRI removedPredicateIRI) {

        map = map.values().stream()
                .filter(m -> mustBePreserved(m, removedPredicateIRI, new AtomicInteger()))
                .map(m -> updateMapping(m, removedPredicateIRI, new AtomicInteger()))
                // .map(m -> deletePredicateIRI(m, removedPredicate, counter))
                //.filter(Optional::isPresent)
                //.map(Optional::get)
                .collect(collectTriplesMaps(SQLPPTriplesMap::getId, Function.identity()));

        mappingListeners.forEach(OBDAMappingListener::mappingUpdated);
    }

    private boolean mustBePreserved(SQLPPTriplesMap formerTriplesMap, IRI removedPredicateIRI,
                                    AtomicInteger counter) {
        int initialCount = counter.get();

        ImmutableList<TargetAtom> newTargetAtoms = getNewTargetAtoms(formerTriplesMap, removedPredicateIRI, counter);

        return counter.get() <= initialCount || !newTargetAtoms.isEmpty();
    }

    private ImmutableList<TargetAtom> getNewTargetAtoms(SQLPPTriplesMap formerTriplesMap, IRI removedPredicateIRI, AtomicInteger counter) {
        return formerTriplesMap.getTargetAtoms().stream()
                .filter(a -> {
                    if (a.getPredicateIRI()
                            .filter(i -> i.equals(removedPredicateIRI))
                            .isPresent()) {
                        counter.incrementAndGet();
                        return false;
                    }
                    return true;
                })
                .collect(ImmutableCollectors.toList());
    }


    private SQLPPTriplesMap updateMapping(SQLPPTriplesMap formerTriplesMap, IRI removedPredicateIRI,
                                          AtomicInteger counter) {
        int initialCount = counter.get();

        ImmutableList<TargetAtom> newTargetAtoms = getNewTargetAtoms(formerTriplesMap, removedPredicateIRI, counter);

        if (counter.get() > initialCount) {
            if (newTargetAtoms.isEmpty())
                throw new IllegalStateException("Mapping should be deleted");

            return new OntopNativeSQLPPTriplesMap(formerTriplesMap.getId(),
                    formerTriplesMap.getSourceQuery(),
                    formerTriplesMap.getOptionalTargetString().get(), // we are sure at this point, it is present
                    newTargetAtoms);
        }
        else
            return formerTriplesMap;
    }


    public void addMappingsListener(OBDAMappingListener mlistener) {
        if (!mappingListeners.contains(mlistener))
            mappingListeners.add(mlistener);
    }

    public void reset(OWLOntology ontology) {
        map.clear();
        prefixManager = new MutablePrefixManager(ontology);
    }


    @Deprecated
    public void addTriplesMap(SQLPPTriplesMap triplesMap, boolean disableFiringMappingInsertedEvent) throws DuplicateMappingException {
        String mapId = triplesMap.getId();

        if (map.containsKey(mapId))
            throw new DuplicateMappingException("ID " + mapId);
        map.put(mapId, triplesMap);

        if (!disableFiringMappingInsertedEvent)
            mappingListeners.forEach(OBDAMappingListener::mappingInserted);
    }

    public void insertMapping(String id, String source, ImmutableList<TargetAtom> targetQuery) throws DuplicateMappingException {
        if (map.containsKey(id))
            throw new DuplicateMappingException("ID " + id);

        map.put(id, new OntopNativeSQLPPTriplesMap(id, sourceQueryFactory.createSourceQuery(source), targetQuery));
        mappingListeners.forEach(OBDAMappingListener::mappingInserted);
    }

    public void removeMapping(String id) {
        if (map.remove(id) != null)
            mappingListeners.forEach(OBDAMappingListener::mappingDeleted);
    }

    public void updateMapping(String id, String newId, String source, ImmutableList<TargetAtom> targetQuery) throws DuplicateMappingException {
        if (!map.containsKey(id))
            throw new MinorOntopInternalBugException("Mapping not found: " + id);

        SQLPPTriplesMap replacement = new OntopNativeSQLPPTriplesMap(newId, sourceQueryFactory.createSourceQuery(source), targetQuery);
        if (newId.equals(id)) {
            map.put(id, replacement);
        }
        else {
            if (map.containsKey(newId))
                throw new DuplicateMappingException("ID " + newId);

            map = map.values().stream()
                    .map(m -> m.getId().equals(id) ? replacement : m)
                    .collect(collectTriplesMaps(SQLPPTriplesMap::getId, m -> m));
        }
        mappingListeners.forEach(OBDAMappingListener::mappingUpdated);
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
