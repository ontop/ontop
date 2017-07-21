package it.unibz.inf.ontop.protege.core;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.exception.DuplicateMappingException;
import it.unibz.inf.ontop.exception.InvalidMappingException;
import it.unibz.inf.ontop.exception.MappingIOException;
import it.unibz.inf.ontop.injection.OBDAFactoryWithException;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.io.DataSource2PropertiesConvertor;
import it.unibz.inf.ontop.io.PrefixManager;
import it.unibz.inf.ontop.mapping.SQLMappingParser;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.ontology.OntologyFactory;
import it.unibz.inf.ontop.ontology.OntologyVocabulary;
import it.unibz.inf.ontop.ontology.impl.OntologyFactoryImpl;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.model.impl.OntopModelSingletons.DATA_FACTORY;

/**
 * Mutable wrapper that follows the previous implementation of OBDAModel.
 * The latter implementation is now immutable.
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
 * This interface is generic regarding the targeted native query language.
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
public class OBDAModelWrapper {

    /**
     *  Immutable OBDA model.
     *  This variable is frequently re-affected.
     */
    private final static OntologyFactory ONTOLOGY_FACTORY = OntologyFactoryImpl.getInstance();
    private final OBDAFactoryWithException obdaFactory;
    private final SpecificationFactory specificationFactory;
    private Optional<OBDADataSource> source;

    private OBDAModel obdaModel;
    private PrefixManagerWrapper prefixManager;

    private final List<OBDAModelListener> sourceListeners;
    private final List<OBDAMappingListener> mappingListeners;
    private final OntologyVocabulary ontologyVocabulary;

    public OBDAModelWrapper(SpecificationFactory specificationFactory,
                            OBDAFactoryWithException obdaFactory, PrefixManagerWrapper prefixManager) {
        this.specificationFactory = specificationFactory;
        this.obdaFactory = obdaFactory;
        this.prefixManager = prefixManager;
        this.obdaModel = createNewOBDAModel(specificationFactory, obdaFactory, prefixManager);
        this.sourceListeners = new ArrayList<>();
        this.mappingListeners = new ArrayList<>();
        source = Optional.empty();
        ontologyVocabulary = ONTOLOGY_FACTORY.createVocabulary();
    }

    public OBDAModel getCurrentImmutableOBDAModel() {
        return obdaModel;
    }

    /**
     * The mappings are merged, the sources and mappings are taken from the parsed model
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

        OBDAModel newObdaModel = mappingParser.parse(mappingFile);

        ImmutableMap<String, String> mergedPrefixes = Stream.concat(
                obdaModel.getMetadata().getPrefixManager().getPrefixMap().entrySet().stream(),
                newObdaModel.getMetadata().getPrefixManager().getPrefixMap().entrySet().stream())
                .distinct()
                .collect(ImmutableCollectors.toMap());

        PrefixManager mergedPrefixManager = specificationFactory.createPrefixManager(mergedPrefixes);

        ImmutableList<OBDAMappingAxiom> mappingAxioms = newObdaModel.getMappingAssertions();

        UriTemplateMatcher uriTemplateMatcher = UriTemplateMatcher.create(
                mappingAxioms.stream()
                        .flatMap(ax -> ax.getTargetQuery().stream())
                        .flatMap(atom -> atom.getTerms().stream())
                        .filter(t -> t instanceof Function)
                        .map(t -> (Function) t));

        obdaModel = obdaModel.newModel(newObdaModel.getMappingAssertions(),
                specificationFactory.createMetadata(mergedPrefixManager, uriTemplateMatcher));
    }

    public PrefixManager getPrefixManager() {
        return obdaModel.getMetadata().getPrefixManager();
    }

    public ImmutableList<OBDAMappingAxiom> getMappings(URI sourceUri) {
        if (sourceUri.equals(getSourceId()))
            return obdaModel.getMappingAssertions();
        else
            return ImmutableList.of();
    }

    public ImmutableMap<URI, ImmutableList<OBDAMappingAxiom>> getMappings() {
        return ImmutableMap.of(getSourceId(), obdaModel.getMappingAssertions());
    }

    public ImmutableList<OBDADataSource> getSources() {
        return source.isPresent()
                ? ImmutableList.of(source.get())
                : ImmutableList.of();
    }

    public OBDAMappingAxiom getMapping(String mappingId) {
        return obdaModel.getMappingAssertion(mappingId);
    }

    public void addPrefix(String prefix, String uri) {
        /**
         * The OBDA is still referencing this object
         */
        prefixManager.addPrefix(prefix, uri);
    }


    public int renamePredicate(Predicate removedPredicate, Predicate newPredicate) {
        int modifiedCount = 0;
        for (OBDAMappingAxiom mapping : obdaModel.getMappingAssertions()) {
            CQIE cq = (CQIE) mapping.getTargetQuery();
            List<Function> body = cq.getBody();
            for (int idx = 0; idx < body.size(); idx++) {
                Function oldAtom = body.get(idx);
                if (!oldAtom.getFunctionSymbol().equals(removedPredicate)) {
                    continue;
                }
                modifiedCount += 1;
                Function newAtom = DATA_FACTORY.getFunction(newPredicate, oldAtom.getTerms());
                body.set(idx, newAtom);
            }
            fireMappingUpdated(getSourceId(), mapping.getId(), mapping);
        }
        return modifiedCount;
    }

    private void fireMappingUpdated(URI sourceURI, String mappingId, OBDAMappingAxiom mapping) {
        for (OBDAMappingListener listener : mappingListeners) {
            listener.mappingUpdated(sourceURI);
        }
    }

    public int deletePredicate(Predicate removedPredicate) {
        int modifiedCount = 0;
        for (OBDAMappingAxiom mapping : obdaModel.getMappingAssertions()) {
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
                fireMappingUpdated(getSourceId(), mapping.getId(), mapping);
            } else {
                removeMapping(getSourceId(), mapping.getId());
            }
        }
        return modifiedCount;
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

    public void reset() {
        obdaModel = createNewOBDAModel(specificationFactory, obdaFactory, prefixManager);
    }


    public void addMapping(URI sourceID, OBDAMappingAxiom mappingAxiom, boolean disableFiringMappingInsertedEvent)
            throws DuplicateMappingException {
        ImmutableList<OBDAMappingAxiom> sourceMappings = obdaModel.getMappingAssertions();
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
        try {
            obdaModel = obdaModel.newModel(ImmutableList.copyOf(newSourceMappings));
        } catch (DuplicateMappingException e) {
            throw new RuntimeException("Duplicated mappings should have been detected earlier.");
        }
        if (!disableFiringMappingInsertedEvent)
            fireMappingInserted(sourceID, mappingAxiom.getId());
    }

    public void removeMapping(URI dataSourceURI, String mappingId) {
        OBDAMappingAxiom mapping = obdaModel.getMappingAssertion(mappingId);
        if (mapping == null)
            return;

        ImmutableList<OBDAMappingAxiom> newMappingAssertions = obdaModel.getMappingAssertions().stream()
                .filter(a -> ! a.getId().equals(mappingId))
                .collect(ImmutableCollectors.toList());

        try {
            obdaModel = obdaModel.newModel(newMappingAssertions);
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

    public void updateTargetQueryMapping(URI sourceID, String id, List<Function> targetQuery) {
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
        ImmutableList<OBDAMappingAxiom> sourceMappings = obdaModel.getMappingAssertions();
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

    private static OBDAModel createNewOBDAModel(SpecificationFactory specificationFactory, OBDAFactoryWithException obdaFactory,
                                                PrefixManagerWrapper prefixManager) {
        try {
            return obdaFactory.createOBDAModel(ImmutableList.of(), specificationFactory.createMetadata(prefixManager,
                    UriTemplateMatcher.create(Stream.of())));
            /**
             * No mapping so should never happen
             */
        } catch(DuplicateMappingException e) {
            throw new RuntimeException("A DuplicateMappingException has been thrown while no mapping has been given." +
                    "What is going on? Message: " + e.getMessage());
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
}
