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
import java.util.stream.Stream;

public class OntopViewMetadataProviderImpl implements OntopViewMetadataProvider {

    private final MetadataProvider parentMetadataProvider;
    private final CachingMetadataLookupWithDependencies parentCacheMetadataLookup;
    private final QuotedIDFactory quotedIdFactory;

    private final ImmutableMap<RelationID, JsonView> jsonMap;


    @AssistedInject
    protected OntopViewMetadataProviderImpl(@Assisted MetadataProvider parentMetadataProvider,
                                            @Assisted Reader ontopViewReader) throws MetadataExtractionException {
        this.parentMetadataProvider = parentMetadataProvider;
        this.parentCacheMetadataLookup = new CachingMetadataLookupWithDependencies(parentMetadataProvider);
        this.quotedIdFactory = parentMetadataProvider.getQuotedIDFactory();

        try (Reader viewReader = ontopViewReader) {
            JsonViews jsonViews = loadAndDeserialize(viewReader);
            this.jsonMap = jsonViews.relations.stream()
                    // TODO: consider using deserializeRelationID
                    .collect(ImmutableCollectors.toMap(t -> quotedIdFactory.createRelationID(t.name.toArray(new String[0])), t -> t));

        } catch (IOException e) {
            throw new MetadataExtractionException(e);
        }
    }

    /**
     * Deserializes a JSON file into a POJO.
     */
    protected static JsonViews loadAndDeserialize(Reader viewReader) throws MetadataExtractionException {

        try {
            ObjectMapper objectMapper = new ObjectMapper()
                    .registerModule(new GuavaModule())
                    .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                    .enable(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT);

            // Create POJO object from JSON
            return objectMapper.readValue(viewReader, JsonViews.class);
        }
        catch (JsonProcessingException e) {
            throw new MetadataExtractionException("problem with JSON processing.\n" + e);
        } catch (IOException e) {
            throw new MetadataExtractionException(e);
        }
    }

    @Override
    public NamedRelationDefinition getRelation(RelationID id) throws MetadataExtractionException {
        if (jsonMap.containsKey(id))
            return jsonMap.get(id).createViewDefinition(getDBParameters(), parentCacheMetadataLookup.getCachingMetadataLookup(id));
        return parentCacheMetadataLookup.getRelation(id);
    }

    @Override
    public QuotedIDFactory getQuotedIDFactory() {
        return quotedIdFactory;
    }

    @Override
    public ImmutableList<RelationID> getRelationIDs() throws MetadataExtractionException {
        return Stream.concat(
                jsonMap.keySet().stream(),
                parentMetadataProvider.getRelationIDs().stream())
                .collect(ImmutableCollectors.toList());
    }

    @Override
    public void insertIntegrityConstraints(NamedRelationDefinition relation, MetadataLookup metadataLookup) throws MetadataExtractionException {
        RelationID id = relation.getID();
        JsonView jsonView = jsonMap.get(id);
        if (jsonView != null) {
            for (RelationID baseId : parentCacheMetadataLookup.getBaseRelations(id)) {
                boolean complete = parentCacheMetadataLookup.completeRelation(baseId);
                if (!complete)
                    parentMetadataProvider.insertIntegrityConstraints(
                            parentCacheMetadataLookup.getRelation(baseId),
                            metadataLookup);
            }
            jsonView.insertIntegrityConstraints(metadataLookup);
        }
        else {
            boolean complete = parentCacheMetadataLookup.completeRelation(id);
            if (!complete)
                parentMetadataProvider.insertIntegrityConstraints(relation, metadataLookup);
        }
    }

    @Override
    public DBParameters getDBParameters() {
        return parentMetadataProvider.getDBParameters();
    }

}
