package it.unibz.inf.ontop.spec.mapping.impl;

import com.google.common.collect.*;
import com.google.inject.Inject;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.dbschema.RDBMetadata;
import it.unibz.inf.ontop.exception.*;
import it.unibz.inf.ontop.injection.NativeQueryLanguageComponentFactory;
import it.unibz.inf.ontop.injection.OntopMappingSQLSettings;
import it.unibz.inf.ontop.injection.TemporalSpecificationFactory;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.iq.tools.UnionBasedQueryMerger;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.spec.OBDASpecInput;
import it.unibz.inf.ontop.spec.TOBDASpecInput;
import it.unibz.inf.ontop.spec.dbschema.RDBMetadataExtractor;
import it.unibz.inf.ontop.spec.mapping.*;
import it.unibz.inf.ontop.spec.mapping.parser.TemporalMappingParser;
import it.unibz.inf.ontop.spec.mapping.pp.*;
import it.unibz.inf.ontop.spec.mapping.pp.impl.SQLPPMappingImpl;
import it.unibz.inf.ontop.spec.mapping.transformer.MappingDatatypeFiller;
import it.unibz.inf.ontop.spec.mapping.validation.MappingOntologyComplianceValidator;
import it.unibz.inf.ontop.spec.ontology.Ontology;
import it.unibz.inf.ontop.spec.ontology.TBoxReasoner;
import it.unibz.inf.ontop.temporal.mapping.impl.SQLTemporalMappingAssertionProvenance;

import javax.annotation.Nonnull;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

import static it.unibz.inf.ontop.model.OntopModelSingletons.ATOM_FACTORY;


public class TemporalMappingExtractorImpl implements TemporalMappingExtractor {

    private static final String ONTOLOGY_SATURATED_TBOX_ERROR_MSG = "the Ontology and TBoxReasoner must be both present, or none";

    private final TemporalMappingParser mappingParser;
    private final MappingOntologyComplianceValidator ontologyComplianceValidator;
    private final TemporalPPMappingConverter ppMappingConverter;
    private final RDBMetadataExtractor dbMetadataExtractor;
    private final OntopMappingSQLSettings settings;
    private final MappingDatatypeFiller mappingDatatypeFiller;
    private final TemporalSpecificationFactory temporalSpecificationFactory;
    private final UnionBasedQueryMerger queryMerger;

    @Inject
    private TemporalMappingExtractorImpl(TemporalMappingParser mappingParser, MappingOntologyComplianceValidator ontologyComplianceValidator,
                                         TemporalPPMappingConverter ppMappingConverter, MappingDatatypeFiller mappingDatatypeFiller,
                                NativeQueryLanguageComponentFactory nativeQLFactory, OntopMappingSQLSettings settings, TemporalSpecificationFactory specificationFactory, UnionBasedQueryMerger queryMerger) {

        this.mappingParser = mappingParser;
        this.ontologyComplianceValidator = ontologyComplianceValidator;
        this.ppMappingConverter = ppMappingConverter;
        this.dbMetadataExtractor = nativeQLFactory.create();
        this.mappingDatatypeFiller = mappingDatatypeFiller;
        this.settings = settings;
        this.temporalSpecificationFactory = specificationFactory;
        this.queryMerger = queryMerger;
    }
    @Override
    public MappingAndDBMetadata extract(@Nonnull OBDASpecInput specInput, @Nonnull Optional<DBMetadata> dbMetadata, @Nonnull Optional<Ontology> ontology, @Nonnull Optional<TBoxReasoner> saturatedTBox, @Nonnull ExecutorRegistry executorRegistry) throws MappingException, DBMetadataExtractionException {

        SQLPPMapping ppMapping = extractPPMapping(specInput);

        return extract(ppMapping, specInput, dbMetadata, ontology, saturatedTBox, executorRegistry);
    }

    private SQLPPMapping extractPPMapping(OBDASpecInput specInput)
            throws DuplicateMappingException, MappingIOException, InvalidMappingException {

        Optional<File> optionalTemporalMappingFile = ((TOBDASpecInput)specInput).getTemporalMappingFile();
        if (optionalTemporalMappingFile.isPresent())
            return mappingParser.parse(optionalTemporalMappingFile.get());

        //TODO:implement them later
//        Optional<Reader> optionalMappingReader = specInput.getMappingReader();
//        if (optionalMappingReader.isPresent())
//            return mappingParser.parse(optionalMappingReader.get());
//
//        Optional<Graph> optionalMappingGraph = specInput.getMappingGraph();
//        if (optionalMappingGraph.isPresent())
//            return mappingParser.parse(optionalMappingGraph.get());

        throw new IllegalArgumentException("Bad internal configuration: no mapping input provided in the OBDASpecInput!\n" +
                " Should have been detected earlier (in case of an user mistake)");
    }

    @Override
    public MappingAndDBMetadata extract(@Nonnull PreProcessedMapping ppMapping, @Nonnull OBDASpecInput specInput, @Nonnull Optional<DBMetadata> dbMetadata, @Nonnull Optional<Ontology> ontology, @Nonnull Optional<TBoxReasoner> saturatedTBox, @Nonnull ExecutorRegistry executorRegistry) throws MappingException, DBMetadataExtractionException {
        if(ontology.isPresent() != saturatedTBox.isPresent()){
            throw new IllegalArgumentException(ONTOLOGY_SATURATED_TBOX_ERROR_MSG);
        }
        return convertPPMapping(castPPMapping(ppMapping), castDBMetadata(dbMetadata), specInput, ontology, saturatedTBox,
                executorRegistry);
    }

    /**
     * Converts the PPMapping into a Mapping.
     *
     * During the conversion, data types are inferred and mapping assertions are validated
     *
     */
    private MappingAndDBMetadata convertPPMapping(SQLPPMapping ppMapping, Optional<RDBMetadata> optionalDBMetadata,
                                                  OBDASpecInput specInput, Optional<Ontology> optionalOntology,
                                                  Optional<TBoxReasoner> optionalSaturatedTBox,
                                                  ExecutorRegistry executorRegistry)
            throws MetaMappingExpansionException, DBMetadataExtractionException, MappingOntologyMismatchException, InvalidMappingSourceQueriesException {


        RDBMetadata dbMetadata = extractDBMetadata(ppMapping, optionalDBMetadata, specInput);
        //TODO:check later if improvement is needed for expandPPMapping
        SQLPPMapping expandedPPMapping = expandPPMapping(ppMapping, settings, dbMetadata);

        // NB: may also add views in the DBMetadata (for non-understood SQL queries)
        MappingWithProvenance provMapping = ppMappingConverter.convert(expandedPPMapping, dbMetadata, executorRegistry);
        dbMetadata.freeze();

        MappingWithProvenance filledProvMapping = mappingDatatypeFiller.inferMissingDatatypes(provMapping, dbMetadata);

        //TODO: write a mapping validator for temporal mappings
        //validateMapping(optionalOntology, optionalSaturatedTBox, filledProvMapping);

        return new TemporalMappingAndDBMetadataImpl(toRegularMapping(filledProvMapping), dbMetadata);
    }

    //TODO: move it to a proper place
    public TemporalMapping toRegularMapping( MappingWithProvenance mappingWithProvenance) {
        class MapItem{
            Predicate pred;
            Predicate projPred;
            IntermediateQuery iq;

            public MapItem(Predicate pred,
                    Predicate projPred,
                    IntermediateQuery iq){
                this.pred = pred;
                this.projPred = projPred;
                this.iq = iq;
            }

            public Predicate getPred() {
                return pred;
            }

            public Predicate getProjPred() {
                return projPred;
            }

            public IntermediateQuery getIq() {
                return iq;
            }

            @Override
            public String toString(){
                return String.format("pred: %s \nprojPred: %s \niq:%s", pred.getName(), projPred.getName(), iq.toString());
            }
        }
        List<MapItem> mapItems = new ArrayList<>();
         mappingWithProvenance.getProvenanceMap().forEach((iq, tmap )-> {
            mapItems.add(new MapItem(((SQLTemporalMappingAssertionProvenance)tmap).getTriplesMap()
                    .getProvenanceTemporalPredicate(), iq.getProjectionAtom().getFunctionSymbol(), iq));
        });

        mapItems.sort(Comparator.comparing(a -> a.getProjPred().getName()));

        Map<Predicate, Multimap<String, IntermediateQuery>> map = new HashMap<>();

        //TODO: inXSDTimes are duplicated. fix it!
        for(MapItem mapItem : mapItems){
            map.putIfAbsent(mapItem.getPred(), ArrayListMultimap.create());
            if (mapItem.getProjPred().getName().equals(QuadrupleElements.inXSDTime.toString())){
                mapItem.getIq().getRootConstructionNode().getSubstitution().getImmutableMap().values().forEach(immutableTerm -> {
                    mapItems.forEach(mapItem1 -> {
                        if (mapItem1.getProjPred().getName().equals(QuadrupleElements.hasBeginning.toString())){
                           if(mapItem1.getIq().getRootConstructionNode().getSubstitution().getImmutableMap().containsValue(immutableTerm)){
                               map.get(mapItem.getPred()).put(mapItem.getProjPred().getName()+"$begin", mapItem.getIq());
                           }
                        }else if(mapItem1.getProjPred().getName().equals(QuadrupleElements.hasEnd.toString())){
                            if(mapItem1.getIq().getRootConstructionNode().getSubstitution().getImmutableMap().containsValue(immutableTerm)){
                                map.get(mapItem.getPred()).put(mapItem.getProjPred().getName()+"$end", mapItem.getIq());
                            }
                        }

                    });
                });
            }else {
                map.get(mapItem.getPred()).put(mapItem.getProjPred().getName(), mapItem.getIq());
            }
        }

        Map<AtomPredicate, QuadrupleDefinition> quadrupleDefinitionMap = Maps.newHashMap();

        map.forEach(
                (keyPred, item) -> {
                    List<IntermediateQuery> definitions = Lists.newLinkedList();
                    List<String> keys = Lists.newLinkedList();
                    Map<String, IntermediateQuery> defMap = new HashMap<>();
                    item.asMap().forEach(
                            (mmk, mmi) -> {
                                queryMerger.mergeDefinitions(mmi).ifPresent(definitions::add);
                                keys.add(mmk);
                            }
                    );
                    quadrupleDefinitionMap.put(ATOM_FACTORY.getAtomPredicate(keyPred), toQuadrupleDefinition(keys, definitions));
                });

        return temporalSpecificationFactory.createTemporalMapping(mappingWithProvenance.getMetadata(), ImmutableMap.copyOf(quadrupleDefinitionMap), mappingWithProvenance.getExecutorRegistry());
    }

    private QuadrupleDefinition toQuadrupleDefinition(List <String> keyList, List<IntermediateQuery> iqList){
        QuadrupleDefinition qd = new QuadrupleDefinition();
        int idx = 0;
        for(IntermediateQuery iq : iqList) {
            String predName = keyList.get(idx);

            if (predName.equals(QuadrupleElements.quadruple.toString())) {
                qd.setQuadruple(new QuadrupleItem(iq.getProjectionAtom().getPredicate(), iq));

            } else if (predName.equals(QuadrupleElements.hasTime.toString())) {
                qd.setHasTime(new QuadrupleItem(iq.getProjectionAtom().getPredicate(), iq));

            } else if(predName.equals(QuadrupleElements.isBeginInclusive.toString())){
                qd.setIsBeginInclusive(new QuadrupleItem(iq.getProjectionAtom().getPredicate(), iq));

            } else if (predName.equals(QuadrupleElements.hasBeginning.toString())){
                qd.setHasBeginning(new QuadrupleItem(iq.getProjectionAtom().getPredicate(), iq));

            } else if (predName.equals(QuadrupleElements.inXSDTimeBegin.toString()+"$begin")){
                qd.setInXSDTimeBegin(new QuadrupleItem(iq.getProjectionAtom().getPredicate(), iq));

            } else if (predName.equals(QuadrupleElements.isEndInclusive.toString())){
                qd.setIsEndInclusive(new QuadrupleItem(iq.getProjectionAtom().getPredicate(), iq));

            } else if (predName.equals(QuadrupleElements.hasEnd.toString())){
                qd.setHasEnd(new QuadrupleItem(iq.getProjectionAtom().getPredicate(), iq));

            } else if (predName.equals(QuadrupleElements.inXSDTimeEnd.toString()+"$end")){
                qd.setInXSDTimeEnd(new QuadrupleItem(iq.getProjectionAtom().getPredicate(), iq));

            }
            idx ++;
        }
        return qd;
    }

    private SQLPPMapping expandPPMapping(SQLPPMapping ppMapping, OntopMappingSQLSettings settings, RDBMetadata dbMetadata)
            throws MetaMappingExpansionException {
        ImmutableList<SQLPPTriplesMap> expandedMappingAxioms = MetaMappingExpander.expand(
                ppMapping.getTripleMaps(),
                settings,
                dbMetadata);
        try {
            return new SQLPPMappingImpl(expandedMappingAxioms, ppMapping.getMetadata());
        } catch (DuplicateMappingException e) {
            // Internal bug
            throw new IllegalStateException(e);
        }
    }

    /**
     * Makes use of the DB connection
     */
    private RDBMetadata extractDBMetadata(final SQLPPMapping ppMapping, Optional<RDBMetadata> optionalDBMetadata,
                                          OBDASpecInput specInput)
            throws DBMetadataExtractionException, MetaMappingExpansionException {

        boolean isDBMetadataProvided = optionalDBMetadata.isPresent();

        /*
         * Metadata extraction can be disabled when DBMetadata is already provided
         */
        if (isDBMetadataProvided && (!settings.isProvidedDBMetadataCompletionEnabled()))
            return optionalDBMetadata.get();

        try (Connection localConnection = createConnection()) {
            return isDBMetadataProvided
                    ? dbMetadataExtractor.extract(ppMapping, localConnection, optionalDBMetadata.get(),
                    specInput.getConstraintFile())
                    : dbMetadataExtractor.extract(ppMapping, localConnection, specInput.getConstraintFile());
        }
        /*
         * Problem while creating the connection
         */
        catch (SQLException e) {
            throw new DBMetadataExtractionException(e.getMessage());
        }
    }

    /**
     * Validation:
     *    - Mismatch between the ontology and the mapping
     */
    private void validateMapping(Optional<Ontology> optionalOntology, Optional<TBoxReasoner> optionalSaturatedTBox,
                                 MappingWithProvenance filledProvMapping) throws MappingOntologyMismatchException {
        if (optionalOntology.isPresent()) {
            Ontology ontology = optionalOntology.get();
            TBoxReasoner saturatedTBox = optionalSaturatedTBox
                    .orElseThrow(() -> new IllegalArgumentException(ONTOLOGY_SATURATED_TBOX_ERROR_MSG));

            ontologyComplianceValidator.validate(filledProvMapping, ontology.getVocabulary(), saturatedTBox);
        }
    }

    private Connection createConnection() throws SQLException {
        return DriverManager.getConnection(settings.getJdbcUrl(), settings.getJdbcUser(), settings.getJdbcPassword());
    }

    private SQLPPMapping castPPMapping(PreProcessedMapping ppMapping) {
        if(ppMapping instanceof SQLPPMapping){
            return (SQLPPMapping) ppMapping;
        }
        throw new IllegalArgumentException(SQLMappingExtractor.class.getSimpleName()+" only supports instances of " +
                SQLPPMapping.class.getSimpleName());
    }

    private Optional<RDBMetadata> castDBMetadata(@Nonnull Optional<DBMetadata> optionalDBMetadata) {
        if(optionalDBMetadata.isPresent()){
            DBMetadata md = optionalDBMetadata.get();
            if(optionalDBMetadata.get() instanceof RDBMetadata){
                return Optional.of((RDBMetadata) md);
            }
            throw new IllegalArgumentException(SQLMappingExtractor.class.getSimpleName()+" only supports instances of " +
                    RDBMetadata.class.getSimpleName());
        }
        return Optional.empty();
    }

}
