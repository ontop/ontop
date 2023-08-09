package it.unibz.inf.ontop.dbschema.impl.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.*;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.LensImpl;
import it.unibz.inf.ontop.dbschema.impl.RawQuotedIDFactory;
import it.unibz.inf.ontop.exception.InvalidQueryException;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.normalization.ConstructionSubstitutionNormalizer;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.spec.sqlparser.ExpressionParser;
import it.unibz.inf.ontop.spec.sqlparser.JSqlParserTools;
import it.unibz.inf.ontop.spec.sqlparser.RAExpressionAttributes;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Stream;

public abstract class JsonBasicOrJoinLens extends JsonBasicOrJoinOrNestedLens {

    protected static final Logger LOGGER = LoggerFactory.getLogger(JsonBasicOrJoinLens.class);

    @Nonnull
    public final Columns columns;

    @Nonnull
    public final String filterExpression;

    protected JsonBasicOrJoinLens(List<String> name, @Nullable UniqueConstraints uniqueConstraints,
                                  @Nullable OtherFunctionalDependencies otherFunctionalDependencies,
                                  @Nullable ForeignKeys foreignKeys, @Nullable NonNullConstraints nonNullConstraints,
                                  @Nullable IRISafeConstraints iriSafeConstraints,
                                  @Nullable Columns columns, @Nonnull String filterExpression) {
        super(name, uniqueConstraints, otherFunctionalDependencies, foreignKeys, nonNullConstraints, iriSafeConstraints);
        this.columns = columns == null ? new Columns(new ArrayList<>(), new ArrayList<>()) : columns;
        this.filterExpression = filterExpression;
    }

    @Override
    public Lens createViewDefinition(DBParameters dbParameters, MetadataLookup parentCacheMetadataLookup)
            throws MetadataExtractionException {

        ImmutableList<ParentDefinition> parentDefinitions = extractParentDefinitions(dbParameters, parentCacheMetadataLookup);

        Integer maxParentLevel = parentDefinitions.stream()
                .map(p -> p.relation)
                .filter(r -> r instanceof Lens)
                .map(r -> (Lens)r)
                .map(Lens::getLevel)
                .reduce(0, Math::max, Math::max);

        QuotedIDFactory idFactory = dbParameters.getQuotedIDFactory();
        RelationID relationId = idFactory.createRelationID(name.toArray(new String[0]));

        IQ iq = createIQ(relationId, parentDefinitions, dbParameters);

        // For added columns the termtype, quoted ID and nullability all need to come from the IQ
        RelationDefinition.AttributeListBuilder attributeBuilder = createAttributeBuilder(iq, dbParameters);

        return new LensImpl(
                ImmutableList.of(relationId),
                attributeBuilder,
                iq,
                maxParentLevel + 1,
                dbParameters.getCoreSingletons());
    }

    @Override
    public void insertIntegrityConstraints(Lens relation,
                                           ImmutableList<NamedRelationDefinition> baseRelations,
                                           MetadataLookup metadataLookupForFK, DBParameters dbParameters) throws MetadataExtractionException {

        QuotedIDFactory idFactory = metadataLookupForFK.getQuotedIDFactory();

        CoreSingletons coreSingletons = dbParameters.getCoreSingletons();

        insertUniqueConstraints(relation, idFactory,
                (uniqueConstraints != null) ? uniqueConstraints.added : ImmutableList.of(),
                baseRelations, coreSingletons);

        ImmutableSet<QuotedID> hiddenColumns = columns.hidden.stream()
                .map(idFactory::createAttributeID)
                .collect(ImmutableCollectors.toSet());

        ImmutableSet<QuotedID> addedColumns = columns.added.stream()
                .map(a -> a.name)
                .map(idFactory::createAttributeID)
                .collect(ImmutableCollectors.toSet());

        insertFunctionalDependencies(relation, idFactory, hiddenColumns, addedColumns,
                (otherFunctionalDependencies != null) ? otherFunctionalDependencies.added : ImmutableList.of(),
                ImmutableList.of(), baseRelations, coreSingletons);

        insertForeignKeys(relation, metadataLookupForFK,
                (foreignKeys != null) ? foreignKeys.added : ImmutableList.of(),
                baseRelations);
    }

    private IQ createIQ(RelationID relationId, ImmutableList<ParentDefinition> parentDefinitions, DBParameters dbParameters)
            throws MetadataExtractionException {

        QuotedIDFactory idFactory = dbParameters.getQuotedIDFactory();
        CoreSingletons coreSingletons = dbParameters.getCoreSingletons();

        TermFactory termFactory = coreSingletons.getTermFactory();
        IntermediateQueryFactory iqFactory = coreSingletons.getIQFactory();
        AtomFactory atomFactory = coreSingletons.getAtomFactory();
        SubstitutionFactory substitutionFactory = coreSingletons.getSubstitutionFactory();

        // cannot use the keySet of substitutionMap because need to createAttributeVariableMap first
        ImmutableSet<Variable> addedVariables = columns.added.stream()
                .map(a -> getVariable(a.name, idFactory, termFactory))
                .collect(ImmutableCollectors.toSet());

        VariableGenerator variableGenerator = coreSingletons.getCoreUtilsFactory().createVariableGenerator(addedVariables);
        parentDefinitions.forEach(p -> p.createAttributeVariableMap(variableGenerator));

        RAExpressionAttributes parentAttributeMap = extractParentAttributeMap(parentDefinitions, idFactory);

        Substitution<ImmutableTerm> substitution = substitutionFactory.getSubstitutionThrowsExceptions(
                columns.added,
                a -> getVariable(a.name, idFactory, termFactory),
                a -> extractExpression(a, parentAttributeMap, idFactory, coreSingletons));

        ConstructionSubstitutionNormalizer substitutionNormalizer = dbParameters.getCoreSingletons()
                .getConstructionSubstitutionNormalizer();

        ImmutableSet<Variable> hiddenVariables = columns.hidden.stream()
                .map(a -> getVariable(a, idFactory, termFactory))
                .collect(ImmutableCollectors.toSet());

        ImmutableList<Variable> projectedVariables = extractRelationVariables(addedVariables, hiddenVariables, parentDefinitions, termFactory);

        ConstructionSubstitutionNormalizer.ConstructionSubstitutionNormalization normalization =
                substitutionNormalizer.normalizeSubstitution(substitution,
                        ImmutableSet.copyOf(projectedVariables));

        IQTree parentTree = createParentTree(parentDefinitions, iqFactory);

        ConstructionNode constructionNode = normalization.generateTopConstructionNode()
                // In case, we reintroduce a ConstructionNode to get rid of unnecessary variables from the parent relation
                // It may be eliminated by the IQ normalization
                .orElseGet(() -> iqFactory.createConstructionNode(ImmutableSet.copyOf(projectedVariables)));

        ImmutableList<ImmutableExpression> filterConditions = extractFilter(parentAttributeMap, idFactory, coreSingletons);

        IQTree updatedParentDataNode = filterConditions.stream()
                .reduce(termFactory::getConjunction)
                .map(iqFactory::createFilterNode)
                .map(f -> normalization.updateChild(iqFactory.createUnaryIQTree(f, parentTree), variableGenerator))
                .orElse(normalization.updateChild(parentTree, variableGenerator));

        IQTree iqTreeBeforeIRISafeConstraints = iqFactory.createUnaryIQTree(constructionNode, updatedParentDataNode);

        IQTree iqTree = addIRISafeConstraints(iqTreeBeforeIRISafeConstraints, dbParameters);

        AtomPredicate tmpPredicate = createTemporaryPredicate(relationId, projectedVariables.size(), coreSingletons);
        DistinctVariableOnlyDataAtom projectionAtom = atomFactory.getDistinctVariableOnlyDataAtom(tmpPredicate, projectedVariables);

        return iqFactory.createIQ(projectionAtom, iqTree)
                .normalizeForOptimization();
    }

    protected static class ParentDefinition {
        private final String prefix;
        private final NamedRelationDefinition relation;
        @Nullable
        private ImmutableMap<Attribute, Variable> attributeVariableMap; // initialized by createAttributeVariableMap

        public ParentDefinition(NamedRelationDefinition relation, String prefix) {
            this.relation = relation;
            this.prefix = prefix;
        }

        public String getPrefixedAttributeName(Attribute a) {
            return prefix + a.getID().getName();
        }

        public void createAttributeVariableMap(VariableGenerator variableGenerator) {
            // NB: the non-necessary variables will be pruned out by normalizing the IQ
            attributeVariableMap = relation.getAttributes().stream()
                                    .collect(ImmutableCollectors.toMap(
                                            a -> a,
                                            a -> variableGenerator.generateNewVariable(getPrefixedAttributeName(a))));
        }

        public ImmutableMap<Integer, Variable> getArgumentMap() {
            return attributeVariableMap.entrySet().stream()
                    .collect(ImmutableCollectors.toMap(ae -> ae.getKey().getIndex() - 1, Map.Entry::getValue));
        }
    }

    abstract protected ImmutableList<ParentDefinition> extractParentDefinitions(DBParameters dbParameters,
                                                                                              MetadataLookup parentCacheMetadataLookup)
            throws MetadataExtractionException;

    private IQTree createParentTree(Collection<ParentDefinition> parentArgumentTable,
                                    IntermediateQueryFactory iqFactory) throws MetadataExtractionException {
        ImmutableList<IQTree> parents = parentArgumentTable.stream()
                .map(p -> iqFactory.createExtensionalDataNode(p.relation, p.getArgumentMap()))
                .collect(ImmutableCollectors.toList());

        switch (parents.size()) {
            case 0:
                throw new MetadataExtractionException("At least one base relation was expected");
            case 1:
                return parents.get(0);
            default:
                return iqFactory.createNaryIQTree(
                        iqFactory.createInnerJoinNode(),
                        parents);
        }
    }

    private ImmutableList<Variable> extractRelationVariables(ImmutableSet<Variable> addedVariables, ImmutableSet<Variable> hiddenVariables,
                                                             ImmutableList<ParentDefinition> parentDefinitions, TermFactory termFactory) {

        Stream<Variable> inheritedVariableStream = parentDefinitions.stream()
                .flatMap(p -> p.relation.getAttributes().stream()
                        .map(p::getPrefixedAttributeName))
                .map(termFactory::getVariable)
                .filter(v -> !hiddenVariables.contains(v))
                .filter(v -> !addedVariables.contains(v));

        return Stream.concat(addedVariables.stream(), inheritedVariableStream)
                .collect(ImmutableCollectors.toList());
    }

    private RAExpressionAttributes extractParentAttributeMap(ImmutableList<ParentDefinition> parentDefinitionMap, QuotedIDFactory quotedIdFactory)
            throws MetadataExtractionException {

        RawQuotedIDFactory idFactory = new RawQuotedIDFactory(quotedIdFactory);

        ImmutableMap<QuotedID, Collection<Variable>> map = parentDefinitionMap.stream()
                .flatMap(p -> p.attributeVariableMap.entrySet().stream()
                        .map(e -> Maps.immutableEntry(
                                idFactory.createAttributeID(p.getPrefixedAttributeName(e.getKey())),
                                e.getValue())))
                .collect(ImmutableCollectors.toMultimap()).asMap();

        ImmutableSet<QuotedID> conflictingAttributeIds = map.entrySet().stream()
                .filter(e -> e.getValue().size() > 1)
                .map(Map.Entry::getKey)
                .collect(ImmutableCollectors.toSet());

        if (!conflictingAttributeIds.isEmpty())
            throw new ConflictingVariableInJoinViewException(conflictingAttributeIds);

        return new RAExpressionAttributes(map.entrySet().stream()
                .collect(ImmutableCollectors.toMap(
                        e -> new QualifiedAttributeID(null, e.getKey()),
                        e -> e.getValue().iterator().next())), null);
    }

    private ImmutableTerm extractExpression(AddColumns column,
                                            RAExpressionAttributes parentAttributeMap,
                                            QuotedIDFactory quotedIdFactory, CoreSingletons coreSingletons)
            throws MetadataExtractionException {

        try {
            ExpressionParser parser = new ExpressionParser(quotedIdFactory, coreSingletons);
            net.sf.jsqlparser.expression.Expression exp;
            String sqlQuery = "SELECT " + column.expression + " FROM fakeTable";
            Select statement = JSqlParserTools.parse(sqlQuery, !quotedIdFactory.supportsSquareBracketQuotation());
            SelectItem si = ((PlainSelect) statement.getSelectBody()).getSelectItems().get(0);
            exp = ((SelectExpressionItem) si).getExpression();
            return parser.parseTerm(exp, parentAttributeMap);
        }
        catch (Exception e) {
            throw new MetadataExtractionException("Unsupported expression for " + column.name + " in " + name + ":\n" + e, e);
        }
    }

    private ImmutableList<ImmutableExpression> extractFilter(RAExpressionAttributes parentAttributeMap,
                                                             QuotedIDFactory quotedIdFactory,
                                                             CoreSingletons coreSingletons) throws MetadataExtractionException {
        if (filterExpression == null || filterExpression.isEmpty())
            return ImmutableList.of();

        try {
            String sqlQuery = "SELECT * FROM fakeTable WHERE " + filterExpression;
            ExpressionParser parser = new ExpressionParser(quotedIdFactory, coreSingletons);
            Select statement = JSqlParserTools.parse(sqlQuery, !quotedIdFactory.supportsSquareBracketQuotation());
            PlainSelect plainSelect = (PlainSelect) statement.getSelectBody();
            return plainSelect.getWhere() == null
                    ? ImmutableList.of()
                    : parser.parseBooleanExpression(plainSelect.getWhere(), parentAttributeMap);
        }
        catch (InvalidQueryException | JSQLParserException e) {
            throw new MetadataExtractionException("Unsupported filter expression for " + ":\n" + e);
        }
    }


    protected Variable getVariable(String attributeName, QuotedIDFactory quotedIdFactory, TermFactory termFactory) {
        return termFactory.getVariable(quotedIdFactory.createAttributeID(attributeName).getName());
    }

    protected static class Columns extends JsonOpenObject {
        @Nonnull
        public final List<AddColumns> added;
        @Nonnull
        public final List<String> hidden;

        @JsonCreator
        public Columns(@JsonProperty("added") List<AddColumns> added,
                       @JsonProperty("hidden") List<String> hidden) {
            this.added = added;
            this.hidden = hidden;
        }
    }

    protected static class AddColumns extends JsonOpenObject {
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
}
