package it.unibz.inf.ontop.dbschema.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.json.*;
import it.unibz.inf.ontop.exception.InvalidQueryException;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.Reader;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class LensMetadataProviderImpl implements LensMetadataProvider {

    private final MetadataProvider parentMetadataProvider;
    private final CachingMetadataLookupWithDependencies dependencyCacheMetadataLookup;
    private final LensNormalizer lensNormalizer;
    private final LensFKSaturator fkSaturator;

    private final ImmutableMap<RelationID, JsonLens> jsonMap;
    private final CachingMetadataLookup parentCachingMetadataLookup;

    // "Processed": constraints already inserted
    private final Set<RelationID> alreadyProcessedViews = new HashSet<>();

    // Lazy (built lately)
    @Nullable
    private MetadataLookup mergedMetadataLookupForFK;

    @AssistedInject
    protected LensMetadataProviderImpl(@Assisted MetadataProvider parentMetadataProvider,
                                       @Assisted Reader lensesReader,
                                       LensNormalizer lensNormalizer,
                                       LensFKSaturator fkSaturator) throws MetadataExtractionException {
        this.parentMetadataProvider = new DelegatingMetadataProvider(parentMetadataProvider) {
            private final Set<RelationID> completeRelations = new HashSet<>();

            /**
             * inserts integrity constraints only ONCE for each relation
             */
            @Override
            public void insertIntegrityConstraints(NamedRelationDefinition relation, MetadataLookup metadataLookup) throws MetadataExtractionException {
                boolean notComplete = completeRelations.add(relation.getID());
                if (notComplete)
                    provider.insertIntegrityConstraints(relation, metadataLookup);
            }
        };
        // Safety for making sure the parent never builds the same relation twice
        this.parentCachingMetadataLookup = new CachingMetadataLookup(parentMetadataProvider);
        this.fkSaturator = fkSaturator;

        try (Reader lensReader = lensesReader) {
            JsonLenses jsonLenses = loadAndDeserialize(lensReader);
            QuotedIDFactory quotedIdFactory = parentMetadataProvider.getQuotedIDFactory();
            this.jsonMap = jsonLenses.relations.stream()
                    .collect(ImmutableCollectors.toMap(
                            t -> JsonMetadata.deserializeRelationID(quotedIdFactory, t.name),
                            t -> t));
        }
        catch (JsonProcessingException e) { // subsumed by IOException (redundant)
            throw new MetadataExtractionException("Problem with JSON processing.\n" + e);
        }
        catch (IOException e) {
            throw new MetadataExtractionException(e);
        }

        this.lensNormalizer = lensNormalizer;
        // Depends on this provider for supporting views of level >1
        this.dependencyCacheMetadataLookup = new CachingMetadataLookupWithDependencies(this);
    }

    /**
     * Deserializes a JSON file into a POJO.
     */
    protected static JsonLenses loadAndDeserialize(Reader viewReader) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new GuavaModule())
                .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                .enable(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT);

        // Create POJO object from JSON
        return objectMapper.readValue(viewReader, JsonLenses.class);
    }

    @Override
    public ImmutableList<RelationID> getRelationIDs() throws MetadataExtractionException {
        return Stream.concat(
                jsonMap.keySet().stream(),
                parentMetadataProvider.getRelationIDs().stream())
                .collect(ImmutableCollectors.toList());
    }

    @Override
    public NamedRelationDefinition getRelation(RelationID id) throws MetadataExtractionException {
        JsonLens jsonLens = jsonMap.get(id);
        if (jsonLens != null)
            return jsonLens.createViewDefinition(getDBParameters(), dependencyCacheMetadataLookup.getCachingMetadataLookupFor(id));

        return parentCachingMetadataLookup.getRelation(id);
    }

    @Override
    public RelationDefinition getBlackBoxView(String query) throws MetadataExtractionException, InvalidQueryException {
        return parentCachingMetadataLookup.getBlackBoxView(query);
    }

    @Override
    public void insertIntegrityConstraints(NamedRelationDefinition relation, MetadataLookup initialMetadataLookupForFK)
            throws MetadataExtractionException {

        MetadataLookup metadataLookupForFK = getMergedMetadataLookupForFK(initialMetadataLookupForFK);

        RelationID relationId = relation.getID();
        JsonLens jsonLens = jsonMap.get(relationId);
        if (jsonLens != null) {
            // Useful for views having multiple children
            boolean notInserted = alreadyProcessedViews.add(relationId);
            if (notInserted) {
                ImmutableList<NamedRelationDefinition> baseRelations = dependencyCacheMetadataLookup.getBaseRelations(relation.getID());
                for (NamedRelationDefinition baseRelation : baseRelations)
                    insertIntegrityConstraints(baseRelation, metadataLookupForFK);

                jsonLens.insertIntegrityConstraints((Lens) relation, baseRelations, metadataLookupForFK,
                        getDBParameters());
            }
        }
        else {
            parentMetadataProvider.insertIntegrityConstraints(relation, metadataLookupForFK);
        }
    }

    /**
     * Creates a new metadata lookup including all the view dependencies.
     * Important for getting FKs where these dependencies are the target.
     */
    private synchronized MetadataLookup getMergedMetadataLookupForFK(MetadataLookup initialMetadataLookupForFK) {
        if (mergedMetadataLookupForFK != null)
            return mergedMetadataLookupForFK;

        return new MergingMetadataLookup(initialMetadataLookupForFK, dependencyCacheMetadataLookup.extractImmutableMetadataLookup());
    }

    @Override
    public QuotedIDFactory getQuotedIDFactory() {
        return parentMetadataProvider.getQuotedIDFactory();
    }

    @Override
    public DBParameters getDBParameters() {
        return parentMetadataProvider.getDBParameters();
    }

    @Override
    public void normalizeAndOptimizeRelations(List<NamedRelationDefinition> relationDefinitions) {
        ImmutableList<Lens> viewDefinitions = relationDefinitions.stream()
                .filter(Lens.class::isInstance)
                .map(Lens.class::cast)
                .sorted(Comparator.comparingInt(Lens::getLevel))
                .collect(ImmutableCollectors.toList());

        // Apply normalization
        viewDefinitions.forEach(lensNormalizer::normalize);

        optimizeViews(viewDefinitions);

        viewDefinitions.forEach(Lens::freeze);
    }

    private void optimizeViews(ImmutableList<Lens> viewDefinitions) {
        fkSaturator.saturateForeignKeys(viewDefinitions, dependencyCacheMetadataLookup.getChildrenMultimap(), jsonMap);
    }

    private static class MergingMetadataLookup implements MetadataLookup {

        private final MetadataLookup mainLookup;
        private final MetadataLookup secondaryLookup;

        public MergingMetadataLookup(MetadataLookup mainLookup, MetadataLookup secondaryLookup) {
            this.mainLookup = mainLookup;
            this.secondaryLookup = secondaryLookup;
        }

        @Override
        public NamedRelationDefinition getRelation(RelationID id) throws MetadataExtractionException {
            try {
                return mainLookup.getRelation(id);
            } catch (MetadataExtractionException e) {
                return secondaryLookup.getRelation(id);
            }
        }

        @Override
        public RelationDefinition getBlackBoxView(String query) {
            throw new UnsupportedOperationException();
        }

        @Override
        public QuotedIDFactory getQuotedIDFactory() {
            return mainLookup.getQuotedIDFactory();
        }
    }
}
