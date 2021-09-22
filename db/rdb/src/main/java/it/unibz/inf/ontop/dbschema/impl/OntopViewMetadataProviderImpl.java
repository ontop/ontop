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
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.io.IOException;
import java.io.Reader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class OntopViewMetadataProviderImpl implements OntopViewMetadataProvider {

    private final MetadataProvider parentMetadataProvider;
    private final CachingMetadataLookupWithDependencies parentCacheMetadataLookup;
    private final OntopViewNormalizer ontopViewNormalizer;

    private final ImmutableMap<RelationID, JsonView> jsonMap;

    @AssistedInject
    protected OntopViewMetadataProviderImpl(@Assisted MetadataProvider parentMetadataProvider,
                                            @Assisted Reader ontopViewReader,
                                            OntopViewNormalizer ontopViewNormalizer) throws MetadataExtractionException {
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
        this.parentCacheMetadataLookup = new CachingMetadataLookupWithDependencies(parentMetadataProvider);

        try (Reader viewReader = ontopViewReader) {
            JsonViews jsonViews = loadAndDeserialize(viewReader);
            QuotedIDFactory quotedIdFactory = parentMetadataProvider.getQuotedIDFactory();
            this.jsonMap = jsonViews.relations.stream()
                    .collect(ImmutableCollectors.toMap(
                            t -> JsonMetadata.deserializeRelationID(quotedIdFactory, t.name),
                            t -> t));
        }
        catch (JsonProcessingException e) { // subsumed by IOException (redundant)
            throw new MetadataExtractionException("problem with JSON processing.\n" + e);
        }
        catch (IOException e) {
            throw new MetadataExtractionException(e);
        }

        this.ontopViewNormalizer = ontopViewNormalizer;
    }

    /**
     * Deserializes a JSON file into a POJO.
     */
    protected static JsonViews loadAndDeserialize(Reader viewReader) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new GuavaModule())
                .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                .enable(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT);

        // Create POJO object from JSON
        return objectMapper.readValue(viewReader, JsonViews.class);
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
        JsonView jsonView = jsonMap.get(id);
        if (jsonView != null)
            return jsonView.createViewDefinition(getDBParameters(), parentCacheMetadataLookup.getCachingMetadataLookupFor(id));

        return parentCacheMetadataLookup.getRelation(id);
    }

    @Override
    public RelationDefinition getBlackBoxView(String query) throws MetadataExtractionException {
        return parentMetadataProvider.getBlackBoxView(query);
    }

    @Override
    public void insertIntegrityConstraints(NamedRelationDefinition relation, MetadataLookup metadataLookupForFK) throws MetadataExtractionException {
        JsonView jsonView = jsonMap.get(relation.getID());
        if (jsonView != null) {

            ImmutableList<NamedRelationDefinition> baseRelations = parentCacheMetadataLookup.getBaseRelations(relation.getID());
            for (NamedRelationDefinition baseRelation : baseRelations)
                parentMetadataProvider.insertIntegrityConstraints(baseRelation, metadataLookupForFK);


            jsonView.insertIntegrityConstraints(relation, baseRelations, metadataLookupForFK);
        }
        else {
            parentMetadataProvider.insertIntegrityConstraints(relation, metadataLookupForFK);
        }
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
    public void normalizeRelations(List<NamedRelationDefinition> relationDefinitions) {
        // TODO: normalize the parents before the children using the OntopViewNormalizer.
        ImmutableList<OntopViewDefinition> viewDefinitions = relationDefinitions.stream()
                .filter(OntopViewDefinition.class::isInstance)
                .map(OntopViewDefinition.class::cast)
                // Sort by view level in ascending order - To be reviewed when level >1 views are introduced
                // .sorted(Comparator.comparing(OntopViewDefinition::getLevel))
                .collect(ImmutableCollectors.toList());

        // Apply normalization
        viewDefinitions.forEach(ontopViewNormalizer::normalize);
    }
}
