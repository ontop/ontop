package it.unibz.inf.ontop.dbschema.impl.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.*;
import it.unibz.inf.ontop.dbschema.*;
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
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.spec.sqlparser.ExpressionParser;
import it.unibz.inf.ontop.spec.sqlparser.RAExpressionAttributes;
import it.unibz.inf.ontop.spec.sqlparser.exception.UnsupportedSelectQueryRuntimeException;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public abstract class JsonBasicOrJoinView extends JsonBasicOrJoinOrNestedView {

    @Nonnull
    public final Columns columns;

    @Nonnull
    public final String filterExpression;

    protected JsonBasicOrJoinView(List<String> name, @Nullable UniqueConstraints uniqueConstraints,
                                          @Nullable OtherFunctionalDependencies otherFunctionalDependencies,
                                          @Nullable ForeignKeys foreignKeys, @Nullable NonNullConstraints nonNullConstraints,
                                          Columns columns, String filterExpression) {
        super(name, uniqueConstraints, otherFunctionalDependencies, foreignKeys, nonNullConstraints);
        this.columns = columns;
        this.filterExpression = filterExpression;
    }

    protected IQ createIQ(RelationID relationId, ImmutableMap<NamedRelationDefinition, String> parentDefinitionMap, DBParameters dbParameters)
            throws MetadataExtractionException {

        CoreSingletons coreSingletons = dbParameters.getCoreSingletons();

        TermFactory termFactory = coreSingletons.getTermFactory();
        IntermediateQueryFactory iqFactory = coreSingletons.getIQFactory();
        AtomFactory atomFactory = coreSingletons.getAtomFactory();
        QuotedIDFactory quotedIdFactory = dbParameters.getQuotedIDFactory();

        ImmutableSet<Variable> addedVariables = columns.added.stream()
                .map(a -> a.name)
                .map(attributeName -> normalizeAttributeName(attributeName, quotedIdFactory))
                .map(termFactory::getVariable)
                .collect(ImmutableCollectors.toSet());

        ImmutableList<Variable> projectedVariables = extractRelationVariables(addedVariables, columns.hidden, parentDefinitionMap,
                dbParameters);

        ImmutableTable<NamedRelationDefinition, Integer, Variable> parentArgumentTable = createParentArgumentTable(
                addedVariables, parentDefinitionMap, coreSingletons.getCoreUtilsFactory());

        ImmutableMap<QualifiedAttributeID, ImmutableTerm> parentAttributeMap = extractParentAttributeMap(
                parentDefinitionMap, parentArgumentTable, quotedIdFactory);

        ConstructionSubstitutionNormalizer.ConstructionSubstitutionNormalization normalization =
                createConstructionSubstitution(projectedVariables, parentAttributeMap, dbParameters);

        IQTree parentTree = createParentTree(parentArgumentTable, iqFactory);

        ConstructionNode constructionNode = normalization.generateTopConstructionNode()
                // In case, we reintroduce a ConstructionNode to get rid of unnecessary variables from the parent relation
                // It may be eliminated by the IQ normalization
                .orElseGet(() -> iqFactory.createConstructionNode(ImmutableSet.copyOf(projectedVariables)));

        ImmutableList<ImmutableExpression> filterConditions;
        try {
            filterConditions = filterExpression != null && !filterExpression.isEmpty()
                    ? extractFilter(parentAttributeMap, quotedIdFactory, coreSingletons)
                    : ImmutableList.of();
        } catch (JSQLParserException e) {
            throw new MetadataExtractionException("Unsupported filter expression for " + ":\n" + e);
        }

        IQTree updatedParentDataNode = filterConditions.stream()
                .reduce(termFactory::getConjunction)
                .map(iqFactory::createFilterNode)
                .map(f -> updateParentDataNode(normalization,
                        iqFactory.createUnaryIQTree(f, parentTree)))
                .orElse(updateParentDataNode(normalization, parentTree));

        IQTree iqTree = iqFactory.createUnaryIQTree(constructionNode, updatedParentDataNode);

        AtomPredicate tmpPredicate = createTemporaryPredicate(relationId, projectedVariables.size(), coreSingletons);
        DistinctVariableOnlyDataAtom projectionAtom = atomFactory.getDistinctVariableOnlyDataAtom(tmpPredicate, projectedVariables);

        return iqFactory.createIQ(projectionAtom, iqTree)
                .normalizeForOptimization();
    }

    private ImmutableList<Variable> extractRelationVariables(ImmutableSet<Variable> addedVariables, List<String> hidden,
                                                             ImmutableMap<NamedRelationDefinition, String> parentDefinitions, DBParameters dbParameters) {
        TermFactory termFactory = dbParameters.getCoreSingletons().getTermFactory();
        QuotedIDFactory quotedIdFactory = dbParameters.getQuotedIDFactory();

        ImmutableList<String> hiddenColumnNames = hidden.stream()
                .map(attributeName -> normalizeAttributeName(attributeName, quotedIdFactory))
                .collect(ImmutableCollectors.toList());

        ImmutableList<Variable> inheritedVariableStream = parentDefinitions.entrySet().stream()
                .flatMap(e -> e.getKey().getAttributes().stream()
                        .map(a -> e.getValue() + a.getID().getName()))
                .filter(n -> !hiddenColumnNames.contains(n))
                .map(termFactory::getVariable)
                .filter(v -> !addedVariables.contains(v))
                .collect(ImmutableCollectors.toList());

        return Stream.concat(
                addedVariables.stream(),
                inheritedVariableStream.stream())
                .collect(ImmutableCollectors.toList());
    }

    protected ImmutableTable<NamedRelationDefinition, Integer, Variable> createParentArgumentTable(ImmutableSet<Variable> addedVariables,
                                                                                                   ImmutableMap<NamedRelationDefinition, String> parentDefinitionMap,
                                                                                                   CoreUtilsFactory coreUtilsFactory) {
        VariableGenerator variableGenerator = coreUtilsFactory.createVariableGenerator(addedVariables);

        // NB: the non-necessary variables will be pruned out by normalizing the IQ
        return parentDefinitionMap.entrySet().stream()
                .flatMap(e -> {
                    ImmutableList<Attribute> parentAttributes = e.getKey().getAttributes();
                    return IntStream.range(0, parentAttributes.size())
                            .mapToObj(i -> Tables.immutableCell(e.getKey(), i, variableGenerator.generateNewVariable(
                                    e.getValue() + parentAttributes.get(i).getID().getName())));
                })
                .collect(ImmutableCollectors.toTable());
    }


    protected ImmutableMap<QualifiedAttributeID, ImmutableTerm> extractParentAttributeMap(
            ImmutableMap<NamedRelationDefinition, String> parentDefinitionMap,
            ImmutableTable<NamedRelationDefinition, Integer, Variable> parentArgumentTable,
            QuotedIDFactory quotedIdFactory) throws MetadataExtractionException {

        ImmutableMap<QualifiedAttributeID, Collection<Variable>> map = parentArgumentTable.cellSet().stream()
                .collect(ImmutableCollectors.toMultimap(
                        c -> new QualifiedAttributeID(null,
                                quotedIdFactory.createAttributeID(
                                        quotedIdFactory.getIDQuotationString() +
                                                // Prefix
                                                parentDefinitionMap.get(c.getRowKey()) +
                                                // Original column name
                                                c.getRowKey().getAttributes().get(c.getColumnKey()).getID().getName() +
                                                quotedIdFactory.getIDQuotationString())),
                        Table.Cell::getValue
                )).asMap();

        ImmutableSet<QualifiedAttributeID> conflictingAttributeIds = map.entrySet().stream()
                .filter(e -> e.getValue().size() > 1)
                .map(Map.Entry::getKey)
                .collect(ImmutableCollectors.toSet());

        if (!conflictingAttributeIds.isEmpty())
            throw new ConflictingVariableInJoinViewException(conflictingAttributeIds);

        return map.entrySet().stream()
                .collect(ImmutableCollectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().iterator().next()
                ));

    }

    private IQTree updateParentDataNode(ConstructionSubstitutionNormalizer.ConstructionSubstitutionNormalization normalization,
                                        IQTree parentIQTree) {

        return normalization.updateChild(parentIQTree);
    }

    private ConstructionSubstitutionNormalizer.ConstructionSubstitutionNormalization createConstructionSubstitution(
            ImmutableList<Variable> projectedVariables,
            ImmutableMap<QualifiedAttributeID, ImmutableTerm> parentAttributeMap,
            DBParameters dbParameters) throws MetadataExtractionException {

        QuotedIDFactory quotedIdFactory = dbParameters.getQuotedIDFactory();
        CoreSingletons coreSingletons = dbParameters.getCoreSingletons();
        TermFactory termFactory = coreSingletons.getTermFactory();
        SubstitutionFactory substitutionFactory = coreSingletons.getSubstitutionFactory();



        ImmutableMap.Builder<Variable, ImmutableTerm> substitutionMapBuilder = ImmutableMap.builder();
        for (AddColumns a : columns.added) {
            Variable v = termFactory.getVariable(normalizeAttributeName(a.name, quotedIdFactory));
            try {
                ImmutableTerm value = extractExpression(a.expression, parentAttributeMap, quotedIdFactory, coreSingletons);
                substitutionMapBuilder.put(v, value);
            } catch (JSQLParserException | UnsupportedSelectQueryRuntimeException e) {
                throw new UnsupportedAddedColumnExpressionException("Unsupported expression for " + a.name + " in " + name + ":\n" + e, e);
            }
        }

        ConstructionSubstitutionNormalizer substitutionNormalizer = dbParameters.getCoreSingletons()
                .getConstructionSubstitutionNormalizer();

        return substitutionNormalizer.normalizeSubstitution(
                substitutionFactory.getSubstitution(substitutionMapBuilder.build()),
                ImmutableSet.copyOf(projectedVariables));
    }

    private IQTree createParentTree(ImmutableTable<NamedRelationDefinition, Integer, Variable> parentArgumentTable,
                                    IntermediateQueryFactory iqFactory) throws MetadataExtractionException {
        ImmutableList<IQTree> parents = parentArgumentTable.rowKeySet().stream()
                .map(d -> iqFactory.createExtensionalDataNode(d, parentArgumentTable.row(d)))
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

    private ImmutableList<ImmutableExpression> extractFilter(ImmutableMap<QualifiedAttributeID, ImmutableTerm> parentAttributeMap,
                                                             QuotedIDFactory quotedIdFactory,
                                                             CoreSingletons coreSingletons) throws JSQLParserException {
        String sqlQuery = "SELECT * FROM fakeTable WHERE " + filterExpression;
        ExpressionParser parser = new ExpressionParser(quotedIdFactory, coreSingletons);
        Statement statement = CCJSqlParserUtil.parse(sqlQuery);
        PlainSelect plainSelect = ((PlainSelect) ((Select) statement).getSelectBody());
        return plainSelect.getWhere() == null
                ? ImmutableList.of()
                : parser.parseBooleanExpression(plainSelect.getWhere(), new RAExpressionAttributes(parentAttributeMap, null));
    }

    private ImmutableTerm extractExpression(String partialExpression,
                                            ImmutableMap<QualifiedAttributeID, ImmutableTerm> parentAttributeMap,
                                            QuotedIDFactory quotedIdFactory, CoreSingletons coreSingletons)
            throws JSQLParserException, UnsupportedSelectQueryRuntimeException {
        String sqlQuery = "SELECT " + partialExpression + " FROM fakeTable";
        ExpressionParser parser = new ExpressionParser(quotedIdFactory, coreSingletons);
        Statement statement = CCJSqlParserUtil.parse(sqlQuery);
        SelectItem si = ((PlainSelect) ((Select) statement).getSelectBody()).getSelectItems().get(0);
        net.sf.jsqlparser.expression.Expression exp = ((SelectExpressionItem) si).getExpression();
        return parser.parseTerm(exp, new RAExpressionAttributes(parentAttributeMap, null));
    }

    @Override
    protected ImmutableSet<QuotedID> getAddedColumns(QuotedIDFactory idFactory) {
        return columns.added.stream()
                .map(a -> a.name)
                .map(idFactory::createAttributeID)
                .collect(ImmutableCollectors.toSet());
    }

    @Override
    protected ImmutableSet<QuotedID> getHiddenColumns(ImmutableList<NamedRelationDefinition> baseRelations, QuotedIDFactory idFactory) {
        return columns.hidden.stream()
                .map(idFactory::createAttributeID)
                .collect(ImmutableCollectors.toSet());
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
