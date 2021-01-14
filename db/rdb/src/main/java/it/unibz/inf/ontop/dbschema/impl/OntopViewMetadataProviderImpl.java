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
import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.json.JsonOpenObject;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.atom.impl.AtomPredicateImpl;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.spec.sqlparser.ExpressionParser;
import it.unibz.inf.ontop.spec.sqlparser.RAExpressionAttributes;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.iq.type.UniqueTermTypeExtractor;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class OntopViewMetadataProviderImpl implements OntopViewMetadataProvider {

    private final MetadataProvider parentMetadataProvider;
    private final MetadataLookup parentCacheMetadataLookup;
    private final IntermediateQueryFactory iqFactory;
    private final TermFactory termFactory;
    private final AtomFactory atomFactory;
    private final CoreUtilsFactory coreUtilsFactory;
    private final SubstitutionFactory substitutionFactory;
    private final ImmutableMap<RelationID, OntopViewDefinition> viewDefinitions;
    private final CoreSingletons coreSingletons;
    private final DBTermType dbRootType;
    private final QuotedIDFactory quotedIdFactory;
    private final UniqueTermTypeExtractor uniqueTermTypeExtractor;


    @AssistedInject
    protected OntopViewMetadataProviderImpl(@Assisted MetadataProvider parentMetadataProvider,
                                            @Assisted Reader ontopViewReader,
                                            IntermediateQueryFactory iqFactory,
                                            TermFactory termFactory,
                                            AtomFactory atomFactory,
                                            CoreUtilsFactory coreUtilsFactory,
                                            SubstitutionFactory substitutionFactory,
                                            CoreSingletons coreSingletons,
                                            UniqueTermTypeExtractor uniqueTermTypeExtractor) throws MetadataExtractionException {
        this.parentMetadataProvider = parentMetadataProvider;
        this.parentCacheMetadataLookup = new CachingMetadataLookup(parentMetadataProvider);
        this.iqFactory = iqFactory;
        this.termFactory = termFactory;
        this.atomFactory = atomFactory;
        this.coreUtilsFactory = coreUtilsFactory;
        this.substitutionFactory = substitutionFactory;
        this.coreSingletons = coreSingletons;
        this.dbRootType = coreSingletons.getTypeFactory().getDBTypeFactory().getAbstractRootDBType();
        this.quotedIdFactory = parentMetadataProvider.getQuotedIDFactory();
        this.uniqueTermTypeExtractor = uniqueTermTypeExtractor;

        try (Reader viewReader = ontopViewReader) {
            JsonViews jsonViews = loadAndDeserialize(viewReader);
            this.viewDefinitions = createViewDefinitions(jsonViews.relations);

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

    private ImmutableMap<RelationID, OntopViewDefinition> createViewDefinitions(List<JsonBasicView> views)
            throws MetadataExtractionException {
        QuotedIDFactory quotedIDFactory = getQuotedIDFactory();

        ImmutableMap.Builder<RelationID, OntopViewDefinition> mapBuilder = ImmutableMap.builder();

        for (JsonBasicView view : views) {

            // TODO: use an array
            RelationID relationId = quotedIDFactory.createRelationID(view.name.toArray(new String[0]));

            NamedRelationDefinition parentDefinition = parentCacheMetadataLookup.getRelation(quotedIDFactory.createRelationID(
                    view.baseRelation.toArray(new String[0])));

            IQ iq = createBasicIQ(relationId, parentDefinition, view);

            // For added columns the termtype, quoted ID and nullability all need to come from the IQ
            RelationDefinition.AttributeListBuilder attributeBuilder = createAttributeBuilder(iq);

            OntopViewDefinition viewDefinition = new OntopViewDefinitionImpl(
                    ImmutableList.of(relationId),
                    attributeBuilder,
                    iq,
                    // TODO: consider other levels
                    1,
                    coreSingletons);

            mapBuilder.put(relationId, viewDefinition);
        }

        return mapBuilder.build();
    }

    private IQ createBasicIQ(RelationID relationId, NamedRelationDefinition parentDefinition, JsonBasicView view) {

        // TODO: fix the Stream

        ImmutableSet<Variable> addedVariables = Stream.of(view.columns.added)
                .flatMap(List::stream)
                .map(a -> a.name)
                .map(this::normalizeAttributeName)
                .map(termFactory::getVariable)
                .collect(ImmutableCollectors.toSet());

        ImmutableList<Variable> projectedVariables = extractRelationVariables(addedVariables, view.columns.hidden, parentDefinition);

        ImmutableMap<Integer, Variable> parentArgumentMap = createParentArgumentMap(addedVariables, parentDefinition);
        ExtensionalDataNode parentDataNode = iqFactory.createExtensionalDataNode(parentDefinition, parentArgumentMap);

        ConstructionNode constructionNode = createConstructionNode(projectedVariables, view, parentDefinition,
                parentArgumentMap);

        IQTree iqTree = iqFactory.createUnaryIQTree(
                constructionNode,
                parentDataNode);

        AtomPredicate tmpPredicate = createTemporaryPredicate(relationId, projectedVariables.size());
        DistinctVariableOnlyDataAtom projectionAtom = atomFactory.getDistinctVariableOnlyDataAtom(tmpPredicate, projectedVariables);

        return iqFactory.createIQ(projectionAtom, iqTree)
                .normalizeForOptimization();
    }

    private ImmutableList<Variable> extractRelationVariables(ImmutableSet<Variable> addedVariables, List<String> hidden, NamedRelationDefinition parentDefinition) {
        ImmutableList<String> hiddenColumnNames = hidden.stream()
                .map(this::normalizeAttributeName)
                .collect(ImmutableCollectors.toList());

        ImmutableList<Variable> inheritedVariableStream = parentDefinition.getAttributes().stream()
                .map(a -> a.getID().getSQLRendering().replace("\"", ""))
                .filter(n -> !hiddenColumnNames.contains(n))
                .map(termFactory::getVariable)
                .filter(v -> !addedVariables.contains(v))
                .collect(ImmutableCollectors.toList());

        return Stream.concat(
                addedVariables.stream(),
                inheritedVariableStream.stream())
                .collect(ImmutableCollectors.toList());
    }

    private String normalizeAttributeName(String attributeName) {
        return quotedIdFactory.createAttributeID(attributeName).getName();
    }

    private AtomPredicate createTemporaryPredicate(RelationID relationId, int arity) {
        return new OntopViewMetadataProviderImpl.TemporaryViewPredicate(
                // TODO: update with array
                relationId.getSQLRendering(),
                // No precise base DB type for the temporary predicate
                IntStream.range(0, arity)
                        .boxed()
                        .map(i -> dbRootType).collect(ImmutableCollectors.toList()));
    }

    private ImmutableMap<Integer, Variable> createParentArgumentMap(ImmutableSet<Variable> addedVariables,
                                                                    NamedRelationDefinition parentDefinition) {
        VariableGenerator variableGenerator = coreUtilsFactory.createVariableGenerator(addedVariables);

        ImmutableList<Attribute> parentAttributes = parentDefinition.getAttributes();

        // NB: the non-necessary variables will be pruned out by normalizing the IQ
        return IntStream.range(0, parentAttributes.size())
                .boxed()
                .collect(ImmutableCollectors.toMap(
                        i -> i,
                        i -> variableGenerator.generateNewVariable(
                                parentAttributes.get(i).getID().getName())));

    }

    private ConstructionNode createConstructionNode(ImmutableList<Variable> projectedVariables,
                                                    JsonBasicView view,
                                                    NamedRelationDefinition parentDefinition,
                                                    ImmutableMap<Integer, Variable> parentArgumentMap) {


        ImmutableMap<QualifiedAttributeID, ImmutableTerm> parentAttributeMap = parentArgumentMap.entrySet().stream()
                .collect(ImmutableCollectors.toMap(
                        e -> new QualifiedAttributeID(null, parentDefinition.getAttributes().get(e.getKey()).getID()),
                        Map.Entry::getValue));

        // TODO: fix the Stream
        ImmutableMap<Variable, ImmutableTerm> substitutionMap = Stream.of(view.columns.added)
                .flatMap(List::stream)
                .collect(ImmutableCollectors.toMap(
                        a -> termFactory.getVariable(normalizeAttributeName(a.name)),
                        a -> {
                            try {
                                return extractExpression(a.expression, parentAttributeMap, view);
                            } catch (JSQLParserException e) {
                                e.printStackTrace(); // More meaningful exception needed
                            }
                            return null;
                        }
                ));

        return iqFactory.createConstructionNode(
                ImmutableSet.copyOf(projectedVariables),
                substitutionFactory.getSubstitution(substitutionMap));
    }

    private ImmutableTerm extractExpression(String partialExpression,
                                            ImmutableMap<QualifiedAttributeID, ImmutableTerm> parentAttributeMap,
                                            JsonBasicView view) throws JSQLParserException {
        String[] relation = view.baseRelation.toArray(new String[0]);
        String sqlQuery = "SELECT " + partialExpression + " FROM " + relation[0].replace("\"", "").toUpperCase();
        ExpressionParser parser = new ExpressionParser(quotedIdFactory, coreSingletons);
        Statement statement = CCJSqlParserUtil.parse(sqlQuery);
        SelectItem si = ((PlainSelect) ((Select) statement).getSelectBody()).getSelectItems().get(0);
        net.sf.jsqlparser.expression.Expression exp = ((SelectExpressionItem) si).getExpression();
        return parser.parseTerm(exp, new RAExpressionAttributes(parentAttributeMap, null));
    }

    private RelationDefinition.AttributeListBuilder createAttributeBuilder(IQ iq) throws MetadataExtractionException {
        RelationDefinition.AttributeListBuilder builder = AbstractRelationDefinition.attributeListBuilder();
        // TODO: implement it
        IQTree iqTree = iq.getTree();
        for (Variable v : iqTree.getVariables()) {
            builder.addAttribute(new QuotedIDImpl(v.getName(), quotedIdFactory.getIDQuotationString()),
                    (DBTermType) uniqueTermTypeExtractor.extractUniqueTermType(v, iqTree)
                            // TODO: give the name of the view
                            .orElseThrow(() -> new MetadataExtractionException("No type inferred for " + v + " in " + iq)),
                    iqTree.getVariableNullability().isPossiblyNullable(v));
        }
        return builder;
    }

    @Override
    public NamedRelationDefinition getRelation(RelationID id) throws MetadataExtractionException {
        if (viewDefinitions.containsKey(id))
            return viewDefinitions.get(id);
        return parentCacheMetadataLookup.getRelation(id);
    }

    @Override
    public QuotedIDFactory getQuotedIDFactory() {
        return quotedIdFactory;
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
    private static class JsonViews extends JsonOpenObject {
        @Nonnull
        public final List<JsonBasicView> relations;

        @JsonCreator
        public JsonViews(@JsonProperty("relations") List<JsonBasicView> relations) {
            this.relations = relations;
        }
    }

    @JsonPropertyOrder({
            "relations"
    })
    private static class JsonBasicView extends JsonOpenObject {
        @Nonnull
        public final Columns columns;
        @Nonnull
        public final List<String> name;
        @Nonnull
        public final List<String> baseRelation;

        @JsonCreator
        public JsonBasicView(@JsonProperty("columns") Columns columns, @JsonProperty("name") List<String> name,
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
    private static class Columns extends JsonOpenObject {
        @Nonnull
        public final List<OntopViewMetadataProviderImpl.AddColumns> added;
        @Nonnull
        public final List<String> hidden;

        @JsonCreator
        public Columns(@JsonProperty("added") List<AddColumns> added,//List<Object> added,
                       @JsonProperty("hidden") List<String> hidden) {//List<Object> hidden) {
            this.added = added;
            this.hidden = hidden;
        }
    }

    @JsonPropertyOrder({
            "name",
            "expression",
    })
    private static class AddColumns extends JsonOpenObject {
        @Nonnull
        public final String name;
        @Nonnull
        public final String expression;


        @JsonCreator
        public AddColumns(@JsonProperty("name") String name,
                          @JsonProperty("expression") String expression) {
            this.name = name;
            this.expression = expression;
        }
    }

    private static class TemporaryViewPredicate extends AtomPredicateImpl {

        protected TemporaryViewPredicate(String name, ImmutableList<TermType> baseTypesForValidation) {
            super(name, baseTypesForValidation);
        }
    }
}
