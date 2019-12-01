package it.unibz.inf.ontop.protege.core;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import it.unibz.inf.ontop.datalog.DatalogFactory;
import it.unibz.inf.ontop.dbschema.JdbcTypeMapper;
import it.unibz.inf.ontop.exception.DuplicateMappingException;
import it.unibz.inf.ontop.exception.InvalidMappingException;
import it.unibz.inf.ontop.exception.MappingIOException;
import it.unibz.inf.ontop.injection.OntopMappingSQLAllConfiguration;
import it.unibz.inf.ontop.injection.SQLPPMappingFactory;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.model.atom.*;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.protege.core.impl.OBDADataSourceFactoryImpl;
import it.unibz.inf.ontop.spec.mapping.OBDASQLQuery;
import it.unibz.inf.ontop.spec.mapping.PrefixManager;
import it.unibz.inf.ontop.spec.mapping.parser.SQLMappingParser;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.spec.mapping.pp.impl.OntopNativeSQLPPTriplesMap;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.UriTemplateMatcher;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.RDF;
import org.semanticweb.owlapi.formats.PrefixDocumentFormat;

import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
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

    private final SQLPPMappingFactory ppMappingFactory;
    private final SpecificationFactory specificationFactory;
    private Map<String, SQLPPTriplesMap> triplesMapMap;
    // Mutable
    private final OBDADataSource source;
    // Mutable and replaced after reset
    private MutablePrefixManager prefixManager;
    private final PrefixDocumentFormat owlPrefixManager;
    // Mutable and replaced after reset
    private MutableOntologyVocabulary currentMutableVocabulary;
    // Mutable and replaced after reset: contains the namespace associated with the prefix ":" if explicitly declared in the ontology
    public Optional<String> explicitDefaultPrefixNamespace = Optional.empty();

    private final List<OBDAModelListener> sourceListeners;
    private final List<OBDAMappingListener> mappingListeners;

    private static final OBDADataSourceFactory DS_FACTORY = OBDADataSourceFactoryImpl.getInstance();
    private final AtomFactory atomFactory;
    private final TermFactory termFactory;
    private final TargetAtomFactory targetAtomFactory;
    private final SubstitutionFactory substitutionFactory;
    private final TypeFactory typeFactory;
    private final DatalogFactory datalogFactory;
    private final JdbcTypeMapper jdbcTypeMapper;
    private final RDF rdfFactory;

    public OBDAModel(SpecificationFactory specificationFactory,
                     SQLPPMappingFactory ppMappingFactory,
                     PrefixDocumentFormat owlPrefixManager,
                     AtomFactory atomFactory, TermFactory termFactory,
                     TypeFactory typeFactory, DatalogFactory datalogFactory,
                     TargetAtomFactory targetAtomFactory, SubstitutionFactory substitutionFactory,
                     JdbcTypeMapper jdbcTypeMapper, RDF rdfFactory) {
        this.specificationFactory = specificationFactory;
        this.ppMappingFactory = ppMappingFactory;
        this.atomFactory = atomFactory;
        this.prefixManager = new MutablePrefixManager(owlPrefixManager);
        this.owlPrefixManager = owlPrefixManager;
        this.termFactory = termFactory;
        this.typeFactory = typeFactory;
        this.datalogFactory = datalogFactory;
        this.targetAtomFactory = targetAtomFactory;
        this.substitutionFactory = substitutionFactory;
        this.jdbcTypeMapper = jdbcTypeMapper;
        this.rdfFactory = rdfFactory;
        this.triplesMapMap = new LinkedHashMap<>();

        this.sourceListeners = new ArrayList<>();
        this.mappingListeners = new ArrayList<>();
        source = initDataSource();
        currentMutableVocabulary = new MutableOntologyVocabularyImpl();
    }

    private static OBDADataSource initDataSource() {
        return DS_FACTORY.getJDBCDataSource("","","","");
    }

    public SQLPPMapping generatePPMapping() {
        ImmutableList<SQLPPTriplesMap> triplesMaps = ImmutableList.copyOf(triplesMapMap.values());

        try {
            UriTemplateMatcher uriTemplateMatcher = UriTemplateMatcher.create(
                    triplesMaps.stream()
                            .flatMap(ax -> ax.getTargetAtoms().stream())
                            .flatMap(targetAtom -> targetAtom.getSubstitution().getImmutableMap().values().stream())
                            .filter(t -> t instanceof ImmutableFunctionalTerm)
                            .map(t -> (ImmutableFunctionalTerm) t),
                    termFactory);

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


    public void parseMapping(Reader mappingReader, Properties properties) throws DuplicateMappingException,
            InvalidMappingException, IOException, MappingIOException {


        OntopMappingSQLAllConfiguration configuration = OntopMappingSQLAllConfiguration.defaultBuilder()
                .nativeOntopMappingReader(mappingReader)
                .properties(properties)
                .build();

        SQLMappingParser mappingParser = configuration.getInjector().getInstance(SQLMappingParser.class);

        SQLPPMapping ppMapping = mappingParser.parse(mappingReader);
        prefixManager.addPrefixes(ppMapping.getMetadata().getPrefixManager().getPrefixMap());
        // New map
        triplesMapMap = ppMapping.getTripleMaps().stream()
                .collect(collectTriplesMaps(
                        SQLPPTriplesMap::getId,
                        m -> m));
    }

    public MutablePrefixManager getMutablePrefixManager() {
        return prefixManager;
    }

    public ImmutableList<SQLPPTriplesMap> getMapping(URI sourceUri) {
        if (sourceUri.equals(getSourceId()))
            return ImmutableList.copyOf(triplesMapMap.values());
        else
            return ImmutableList.of();
    }

    public ImmutableList<OBDADataSource> getSources() {
        return ImmutableList.of(source);
    }

    public SQLPPTriplesMap getTriplesMap(String mappingId) {
        return triplesMapMap.get(mappingId);
    }

    public void addPrefix(String prefix, String uri) {
        /**
         * The OBDA is still referencing this object
         */
        prefixManager.addPrefix(prefix, uri);
    }


    public int changePredicateIri(IRI removedPredicateIri, IRI newPredicatIri) {
        AtomicInteger counter = new AtomicInteger();

        triplesMapMap = triplesMapMap.entrySet().stream()
                .collect(collectTriplesMaps(
                        Map.Entry::getKey,
                        e -> changePredicateIri(e.getValue(), removedPredicateIri, newPredicatIri, counter)));

        return counter.get();
    }

    private SQLPPTriplesMap changePredicateIri(SQLPPTriplesMap formerTriplesMap,
                                               IRI removedIRI, IRI newIRI,
                                               AtomicInteger counter) {
        int formerCount = counter.get();

        ImmutableList<TargetAtom> newTargetAtoms = formerTriplesMap.getTargetAtoms().stream()
                .map(a -> {
                    if (a.getPredicateIRI()
                            .filter(i -> i.equals(removedIRI))
                            .isPresent()) {

                        DistinctVariableOnlyDataAtom projectionAtom = a.getProjectionAtom();
                        RDFAtomPredicate predicate = (RDFAtomPredicate)projectionAtom.getPredicate();

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
                                                (ImmutableTerm) termFactory.getImmutableUriTemplate(
                                                        termFactory.getConstantLiteral(newIRI.getIRIString())))
                                                : e)
                                        .collect(ImmutableCollectors.toMap()));

                        counter.incrementAndGet();
                        return  targetAtomFactory.getTargetAtom(projectionAtom, newSubstitution);
                    }
                    return a;
                })
                .collect(ImmutableCollectors.toList());

        if (counter.get() > formerCount) {
            SQLPPTriplesMap newTriplesMap = new OntopNativeSQLPPTriplesMap(formerTriplesMap.getId(),
                    formerTriplesMap.getSourceQuery(), newTargetAtoms);

            fireMappingUpdated();
            return newTriplesMap;
        }
        else
            return formerTriplesMap;

    }

    private void fireMappingUpdated() {
        for (OBDAMappingListener listener : mappingListeners) {
            listener.mappingUpdated();
        }
    }

    public void deletePredicateIRI(IRI removedPredicateIRI) {

        triplesMapMap = triplesMapMap.values().stream()
                .filter(m -> mustBePreserved(m, removedPredicateIRI, new AtomicInteger()))
                .map(m -> updateMapping(m, removedPredicateIRI, new AtomicInteger()))
               // .map(m -> deletePredicateIRI(m, removedPredicate, counter))
                //.filter(Optional::isPresent)
                //.map(Optional::get)
                .collect(collectTriplesMaps(
                        SQLPPTriplesMap::getId,
                        m -> m));

        fireMappingUpdated();

    }

    private boolean mustBePreserved(SQLPPTriplesMap formerTriplesMap, IRI removedPredicateIRI,
                                    AtomicInteger counter) {
        int initialCount = counter.get();

        ImmutableList<TargetAtom> newTargetAtoms = getNewTargetAtoms(formerTriplesMap, removedPredicateIRI, counter);

        if (counter.get() > initialCount) {
            if (newTargetAtoms.isEmpty()) {
                return false;
            }
            else {
                return true;
            }
        }
        else
            return true;
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
            if (newTargetAtoms.isEmpty()) {

                throw new IllegalStateException("Mapping should be deleted");
            }
            else {
                SQLPPTriplesMap newTriplesMap = new OntopNativeSQLPPTriplesMap(formerTriplesMap.getId(),
                        formerTriplesMap.getSourceQuery(),
                        newTargetAtoms);

                return newTriplesMap;
            }
        }
        else
            return formerTriplesMap;
    }

    private URI getSourceId() {
        return source.getSourceID();
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

    /**
     * TODO: make it private
     */
    public void fireSourceParametersUpdated() {
        for (OBDAModelListener listener : sourceListeners) {
            listener.datasourceParametersUpdated();
        }
    }

    /**
     *
     */
    public void reset(PrefixDocumentFormat owlPrefixMapper) {
        triplesMapMap.clear();
        prefixManager = new MutablePrefixManager(owlPrefixMapper);
        currentMutableVocabulary = new MutableOntologyVocabularyImpl();
        explicitDefaultPrefixNamespace = Optional.empty();
    }


    @Deprecated
    public void addTriplesMap(URI sourceID, SQLPPTriplesMap triplesMap, boolean disableFiringMappingInsertedEvent)
            throws DuplicateMappingException {
        String mapId = triplesMap.getId();

        if (triplesMapMap.containsKey(mapId))
            throw new DuplicateMappingException("ID " + mapId);
        triplesMapMap.put(mapId, triplesMap);

        if (!disableFiringMappingInsertedEvent)
            fireMappingInserted(sourceID);
    }

    public void addTriplesMap(SQLPPTriplesMap triplesMap, boolean disableFiringMappingInsertedEvent)
            throws DuplicateMappingException {
        String mapId = triplesMap.getId();

        if (triplesMapMap.containsKey(mapId))
            throw new DuplicateMappingException("ID " + mapId);
        triplesMapMap.put(mapId, triplesMap);

        if (!disableFiringMappingInsertedEvent)
            fireMappingInserted(source.getSourceID());
    }


    public void removeTriplesMap(URI dataSourceURI, String mappingId) {
        if (triplesMapMap.remove(mappingId) != null)
            fireMappingDeleted(dataSourceURI, mappingId);
    }

    public void updateMappingsSourceQuery(String triplesMapId, OBDASQLQuery sourceQuery) {
        SQLPPTriplesMap formerTriplesMap = getTriplesMap(triplesMapId);

        if (formerTriplesMap != null) {
            SQLPPTriplesMap newTriplesMap = new OntopNativeSQLPPTriplesMap(triplesMapId, sourceQuery,
                    formerTriplesMap.getTargetAtoms());
            triplesMapMap.put(triplesMapId, newTriplesMap);
            fireMappingUpdated();
        }
    }

    public void updateTargetQueryMapping(String id, ImmutableList<TargetAtom> targetQuery) {
        SQLPPTriplesMap formerTriplesMap = getTriplesMap(id);

        if (formerTriplesMap != null) {
            SQLPPTriplesMap newTriplesMap = new OntopNativeSQLPPTriplesMap(id, formerTriplesMap.getSourceQuery(),
                    targetQuery);
            triplesMapMap.put(id, newTriplesMap);
            fireMappingUpdated();
        }
    }

    public void updateMappingId(String formerMappingId, String newMappingId) throws DuplicateMappingException {
        //if the id are the same no need to update the mapping
        if(!formerMappingId.equals(newMappingId)) {
            SQLPPTriplesMap formerTriplesMap = getTriplesMap(formerMappingId);

            if (formerTriplesMap != null) {
                SQLPPTriplesMap newTriplesMap = new OntopNativeSQLPPTriplesMap(newMappingId, formerTriplesMap.getSourceQuery(),
                        formerTriplesMap.getTargetAtoms());
                addTriplesMap(newTriplesMap, false);
                triplesMapMap.remove(formerMappingId);
                fireMappingUpdated();
            }
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
    private void fireMappingInserted(URI srcuri) {
        for (OBDAMappingListener listener : mappingListeners) {
            listener.mappingInserted(srcuri);
        }
    }

    public OBDADataSource getDatasource() {
        return source;
    }

    public MutableOntologyVocabulary getCurrentVocabulary() { return currentMutableVocabulary; }

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

    public AtomFactory getAtomFactory() {
        return atomFactory;
    }

    public TermFactory getTermFactory() {
        return termFactory;
    }

    public TypeFactory getTypeFactory() {
        return typeFactory;
    }

    public DatalogFactory getDatalogFactory() {
        return datalogFactory;
    }

    public JdbcTypeMapper getJDBCTypeMapper() {
        return jdbcTypeMapper;
    }

    public TargetAtomFactory getTargetAtomFactory() {
        return targetAtomFactory;
    }

    public RDF getRdfFactory() {
        return rdfFactory;
    }

    public boolean hasTripleMaps(){
        return !triplesMapMap.isEmpty();
    }

    public Optional<String> getExplicitDefaultPrefixNamespace() {
        return explicitDefaultPrefixNamespace;
    }

    public void setExplicitDefaultPrefixNamespace(String ns) {
        this.explicitDefaultPrefixNamespace = Optional.of(ns);
        addPrefix(PrefixManager.DEFAULT_PREFIX, ns);
    }
}
