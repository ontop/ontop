package it.unibz.inf.ontop.dbschema.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.json.JsonOpenObject;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.atom.RelationPredicate;
import it.unibz.inf.ontop.model.atom.impl.AtomPredicateImpl;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.dbschema.OntopViewMetadataProvider;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * "Dummy" implementation
 *
 * Throws an exception if non-mirror views are present.
 *
 * Only supports level-1 mirror views.
 *
 * Ignores the constraints
 */
public class MirrorViewOnlyOntopViewMetadataProvider implements OntopViewMetadataProvider {

    private final MetadataProvider parentMetadataProvider;
    private final MetadataLookup parentCacheMetadataLookup;
    private final IntermediateQueryFactory iqFactory;
    private final TermFactory termFactory;
    private final AtomFactory atomFactory;
    private final ImmutableMap<RelationID, OntopViewDefinition> viewDefinitions;
    private final CoreSingletons coreSingletons;


    @AssistedInject
    protected MirrorViewOnlyOntopViewMetadataProvider(@Assisted MetadataProvider parentMetadataProvider,
                                                      @Assisted Reader ontopViewReader,
                                                      IntermediateQueryFactory iqFactory,
                                                      TermFactory termFactory,
                                                      AtomFactory atomFactory, CoreSingletons coreSingletons) throws MetadataExtractionException {
        this.parentMetadataProvider = parentMetadataProvider;
        this.parentCacheMetadataLookup = new CachingMetadataLookup(parentMetadataProvider);
        this.iqFactory = iqFactory;
        this.termFactory = termFactory;
        this.atomFactory = atomFactory;
        this.coreSingletons = coreSingletons;

        try (Reader viewReader = ontopViewReader) {
            DummyJsonViews jsonViews = loadAndDeserialize(viewReader);
            this.viewDefinitions = createMirrorViewDefinitions(jsonViews.relations);

        } catch (IOException e) {
            throw new MetadataExtractionException(e);
        }
    }

    /**
     * Deserializes a JSON file into a POJO.
     */
    protected static DummyJsonViews loadAndDeserialize(Reader viewReader) throws MetadataExtractionException {

        try {
            ObjectMapper objectMapper = new ObjectMapper()
                    .registerModule(new GuavaModule())
                    .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                    .enable(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT);

            // Create POJO object from JSON
            return objectMapper.readValue(viewReader, DummyJsonViews.class);
        }
        catch (JsonProcessingException e) {
            throw new MetadataExtractionException("problem with JSON processing.\n" + e);
        } catch (IOException e) {
            throw new MetadataExtractionException(e);
        }
    }

    private ImmutableMap<RelationID, OntopViewDefinition> createMirrorViewDefinitions(List<DummyJsonBasicView> views)
            throws MetadataExtractionException {
        QuotedIDFactory quotedIDFactory = getQuotedIDFactory();

        ImmutableMap.Builder<RelationID, OntopViewDefinition> mapBuilder = ImmutableMap.builder();

        for (DummyJsonBasicView view : views) {
            if (!view.columns.isEmpty())
                throw new MetadataExtractionException("only mirror views are supported at the moment");

            RelationID relationId = quotedIDFactory.createRelationID(view.name.toArray(new String[0]));

            NamedRelationDefinition parentDefinition = parentCacheMetadataLookup.getRelation(quotedIDFactory.createRelationID(
                    view.baseRelation.toArray(new String[0])));

            AtomPredicate tmpPredicate = createTemporaryPredicate(relationId, parentDefinition.getAtomPredicate());

            IQ iq = createMirrorIQ(parentDefinition, tmpPredicate);

            RelationDefinition.AttributeListBuilder attributeBuilder = AbstractRelationDefinition.attributeListBuilder();
            parentDefinition.getAttributes().forEach(
                    a -> attributeBuilder.addAttribute(a.getID(), a.getTermType(), a.isNullable()));

            OntopViewDefinition viewDefinition = new OntopViewDefinitionImpl(
                    ImmutableList.of(relationId),
                    attributeBuilder,
                    iq,
                    1,
                    coreSingletons);

            mapBuilder.put(relationId, viewDefinition);
        }

        return mapBuilder.build();
    }

    private AtomPredicate createTemporaryPredicate(RelationID relationId, RelationPredicate parentAtomPredicate) {
        return new TemporaryViewPredicate(relationId.getSQLRendering(), parentAtomPredicate.getBaseTypesForValidation());
    }

    private IQ createMirrorIQ(NamedRelationDefinition parentDefinition, AtomPredicate tmpPredicate) {
        ImmutableList<Variable> variables = parentDefinition.getAttributes().stream()
                .map(a -> termFactory.getVariable(a.getID().getName()))
                .collect(ImmutableCollectors.toList());
        DistinctVariableOnlyDataAtom projectionAtom = atomFactory.getDistinctVariableOnlyDataAtom(tmpPredicate, variables);

        ImmutableMap<Integer, Variable> parentArgumentMap = IntStream.range(0, variables.size())
                .boxed()
                .collect(ImmutableCollectors.toMap(
                        i -> i,
                        variables::get));

        ExtensionalDataNode dataNode = iqFactory.createExtensionalDataNode(parentDefinition, parentArgumentMap);
        return iqFactory.createIQ(projectionAtom, dataNode);
    }

    @Override
    public NamedRelationDefinition getRelation(RelationID id) throws MetadataExtractionException {
        if (viewDefinitions.containsKey(id))
            return viewDefinitions.get(id);
        return parentCacheMetadataLookup.getRelation(id);
    }

    @Override
    public QuotedIDFactory getQuotedIDFactory() {
        return parentMetadataProvider.getQuotedIDFactory();
    }

    @Override
    public ImmutableList<RelationID> getRelationIDs() throws MetadataExtractionException {
        return Stream.concat(
                    viewDefinitions.keySet().stream(),
                    parentMetadataProvider.getRelationIDs().stream())
                .collect(ImmutableCollectors.toList());
    }

    @Override
    public void insertIntegrityConstraints(NamedRelationDefinition relation, MetadataLookup metadataLookup) throws MetadataExtractionException {
        parentMetadataProvider.insertIntegrityConstraints(relation, metadataLookup);
    }

    @Override
    public DBParameters getDBParameters() {
        return parentMetadataProvider.getDBParameters();
    }

    @JsonPropertyOrder({
            "relations"
    })
    private static class DummyJsonViews extends JsonOpenObject {
        @Nonnull
        public final List<DummyJsonBasicView> relations;

        @JsonCreator
        public DummyJsonViews(@JsonProperty("relations") List<DummyJsonBasicView> relations) {
            this.relations = relations;
        }
    }

    @JsonPropertyOrder({
            "relations"
    })
    private static class DummyJsonBasicView extends JsonOpenObject {
        @Nonnull
        public final DummyColumns columns;
        @Nonnull
        public final List<String> name;
        @Nonnull
        public final List<String> baseRelation;

        @JsonCreator
        public DummyJsonBasicView(@JsonProperty("columns") DummyColumns columns, @JsonProperty("name") List<String> name,
                                  @JsonProperty("baseRelation") List<String> baseRelation) {
            this.columns = columns;
            this.name = name;
            this.baseRelation = baseRelation;
        }
    }

    @JsonPropertyOrder({
            "added",
            "hidden"
    })
    private static class DummyColumns extends JsonOpenObject {
        @Nonnull
        public final List<Object> added;
        @Nonnull
        public final List<Object> hidden;

        @JsonCreator
        public DummyColumns(@JsonProperty("added") List<Object> added,
                            @JsonProperty("hidden") List<Object> hidden) {
            this.added = added;
            this.hidden = hidden;
        }

        public boolean isEmpty() {
            return added.isEmpty() && hidden.isEmpty();
        }
    }

    private static class TemporaryViewPredicate extends AtomPredicateImpl {

        protected TemporaryViewPredicate(String name, ImmutableList<TermType> baseTypesForValidation) {
            super(name, baseTypesForValidation);
        }
    }

}
