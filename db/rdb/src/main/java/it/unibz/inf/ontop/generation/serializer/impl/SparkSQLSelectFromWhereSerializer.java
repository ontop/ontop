package it.unibz.inf.ontop.generation.serializer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.dbschema.DBParameters;
import it.unibz.inf.ontop.dbschema.QualifiedAttributeID;
import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.generation.algebra.SQLExpression;
import it.unibz.inf.ontop.generation.algebra.SQLOrderComparator;
import it.unibz.inf.ontop.generation.algebra.SelectFromWhereWithModifiers;
import it.unibz.inf.ontop.generation.serializer.SQLSerializationException;
import it.unibz.inf.ontop.generation.serializer.SelectFromWhereSerializer;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Singleton
public class SparkSQLSelectFromWhereSerializer extends DefaultSelectFromWhereSerializer implements SelectFromWhereSerializer {

    private final TermFactory termFactory;
    private static final String SELECT_FROM_WHERE_MODIFIERS_TEMPLATE = "SELECT %s%s\nFROM %s\n%s%s%s%s";

    @Inject
    private SparkSQLSelectFromWhereSerializer(TermFactory termFactory) {
        super(new DefaultSQLTermSerializer(termFactory)
        {
            @Override
            protected String serializeStringConstant(String constant) {
                // parent method + doubles backslashes
                return super.serializeStringConstant(constant)
                        .replace("\\", "\\\\");
            }
        });
        this.termFactory = termFactory;
    }

    @Override
    public QuerySerialization serialize(SelectFromWhereWithModifiers selectFromWhere, DBParameters dbParameters) {
        return selectFromWhere.acceptVisitor(new DefaultRelationVisitingSerializer(dbParameters.getQuotedIDFactory()) {

            /**
             * Override the original method in DefaultSelectFromWhereSerializer, because is needed to pass the
             * substitutionMap parameter to the serializeOrderBy() function.
             */
            @Override
            public QuerySerialization visit(SelectFromWhereWithModifiers selectFromWhere) {

                QuerySerialization fromQuerySerialization = getSQLSerializationForChild(selectFromWhere.getFromSQLExpression());

                ImmutableMap<Variable, QuotedID> variableAliases = createVariableAliases(selectFromWhere.getProjectedVariables());

                String distinctString = selectFromWhere.isDistinct() ? "DISTINCT " : "";

                ImmutableMap<Variable, QualifiedAttributeID> columnIDs = fromQuerySerialization.getColumnIDs();
                String projectionString = serializeProjection(selectFromWhere.getProjectedVariables(),
                        variableAliases, selectFromWhere.getSubstitution(), columnIDs);

                String fromString = fromQuerySerialization.getString();

                // TODO: if selectFromWhere.getLimit is 0, then replace it with an additional filter 0 = 1
                String whereString = selectFromWhere.getWhereExpression()
                        .map(e -> sqlTermSerializer.serialize(e, columnIDs))
                        .map(s -> String.format("WHERE %s\n", s))
                        .orElse("");

                String groupByString = serializeGroupBy(selectFromWhere.getGroupByVariables(), columnIDs);
                String orderByString = serializeOrderBy(selectFromWhere.getSortConditions(), columnIDs, variableAliases, selectFromWhere.getSubstitution());
                String sliceString = serializeSlice(selectFromWhere.getLimit(), selectFromWhere.getOffset());

                String sql = String.format(SELECT_FROM_WHERE_MODIFIERS_TEMPLATE, distinctString, projectionString,
                        fromString, whereString, groupByString, orderByString, sliceString);

                // Creates an alias for this SQLExpression and uses it for the projected columns
                RelationID alias = generateFreshViewAlias();
                return new QuerySerializationImpl(sql, attachRelationAlias(alias, variableAliases));
            }

            //this function is required in case at least one of the children is
            // SelectFromWhereWithModifiers expression
            private QuerySerialization getSQLSerializationForChild(SQLExpression expression) {
                if (expression instanceof SelectFromWhereWithModifiers) {
                    QuerySerialization serialization = expression.acceptVisitor(this);
                    RelationID alias = generateFreshViewAlias();
                    String sql = String.format("(%s) %s", serialization.getString(), alias.getSQLRendering());
                    return new QuerySerializationImpl(sql,
                            replaceRelationAlias(alias, serialization.getColumnIDs()));
                }
                return expression.acceptVisitor(this);
            }

            private ImmutableMap<Variable, QualifiedAttributeID> replaceRelationAlias(RelationID alias, ImmutableMap<Variable, QualifiedAttributeID> columnIDs) {
                return columnIDs.entrySet().stream()
                        .collect(ImmutableCollectors.toMap(
                                Map.Entry::getKey,
                                e -> new QualifiedAttributeID(alias, e.getValue().getAttribute())));
            }

            private ImmutableMap<Variable, QuotedID> createVariableAliases(ImmutableSet<Variable> variables) {
                AttributeAliasFactory aliasFactory = createAttributeAliasFactory();
                return variables.stream()
                        .collect(ImmutableCollectors.toMap(
                                Function.identity(),
                                v -> aliasFactory.createAttributeAlias(v.getName())));
            }

            ImmutableMap<Variable, QualifiedAttributeID> attachRelationAlias(RelationID alias, ImmutableMap<Variable, QuotedID> variableAliases) {
                return variableAliases.entrySet().stream()
                        .collect(ImmutableCollectors.toMap(
                                Map.Entry::getKey,
                                e -> new QualifiedAttributeID(alias, e.getValue())));
            }

            @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
            private String serializeSlice(Optional<Long> limit, Optional<Long> offset) {
                if (!limit.isPresent() && !offset.isPresent())
                    return "";

                if (limit.isPresent() && offset.isPresent())
                    return serializeLimitOffset(limit.get(), offset.get(), true);

                if (limit.isPresent())
                    return serializeLimit(limit.get(), true);

                return serializeOffset(offset.get(), true);
            }

            /**
             * SPARKSQL "ORDER BY" construct doesn't accept "relationID.attribute" notation for listing attributes.
             * It is needed a custom serialization for extracting the COLUMN ALIASES.
             */
            protected String serializeOrderBy(ImmutableList<SQLOrderComparator> sortConditions,
                                              ImmutableMap<Variable, QualifiedAttributeID> columnIDs,
                                              ImmutableMap<Variable, QuotedID> variableAliases,
                                              ImmutableSubstitution<? extends ImmutableTerm> substitution) {
                if (sortConditions.isEmpty())
                    return "";

                String conditionString = sortConditions.stream()
                        .map(c -> serializeOrderByTerm(c.getTerm(), columnIDs, substitution)
                                + (c.isAscending() ? " NULLS FIRST" : " DESC NULLS LAST"))
                        .collect(Collectors.joining(", "));

                return String.format("ORDER BY %s\n", conditionString);
            }

            /**
             * Custom term serializer used for the "ORDER BY" construct serialization. If the term is in the substitution
             * list (eg. immediates with data manipulation operations) is used the checkSubstitutionMap(), otherwise the
             * variable alias is extracted from the columnID map using the checkColumnID() function.
             */
            private String serializeOrderByTerm(ImmutableTerm term,
                                                ImmutableMap<Variable, QualifiedAttributeID> columnIDs,
                                                ImmutableSubstitution<? extends ImmutableTerm> substitution)
                    throws SQLSerializationException {

                String result = checkSubstitutionMap(term,substitution);
                if (result == ""){
                    result = checkColumnID(term, columnIDs);
                    return result;
                }
                return result;
            }

            /**
             * Check the substitutionMap and extract the column alias if available.
             */
            private String checkSubstitutionMap(ImmutableTerm term,
                                                ImmutableSubstitution<? extends ImmutableTerm> substitution){
                for (Map.Entry<Variable, ? extends ImmutableTerm> entry : substitution.getImmutableMap().entrySet()) {
                    if (entry.getValue().equals(term)) {
                        return ("`"+entry.getKey().getName()+"`");   // Return the COLUMN ALIAS
                    }
                }
                return "";
            }

            /**
             * Check the columnIDs Map and extract the column alias if available. If not available, throw an exception.
             */
            private String checkColumnID(ImmutableTerm term, ImmutableMap<Variable, QualifiedAttributeID> columnIDs)
                    throws SQLSerializationException {

                if (term instanceof Constant) {
                    return serializeConstant((Constant)term);
                } else if (term instanceof Variable) {
                    for (Map.Entry<Variable, QualifiedAttributeID> entry : columnIDs.entrySet()) {
                        if (entry.getValue().equals(columnIDs.get(term))) {
                            return ("`"+entry.getKey().getName()+"`");   // Return the COLUMN ALIAS
                        }
                    }
                    throw new SQLSerializationException(String.format(
                            "The variable %s does not appear in the columnIDs", term));
                } else {
                    return Optional.of(term)
                            .filter(t -> t instanceof ImmutableFunctionalTerm)
                            .map(t -> (ImmutableFunctionalTerm) t)
                            .filter(t -> t.getFunctionSymbol() instanceof DBFunctionSymbol)
                            .map(t -> ((DBFunctionSymbol) t.getFunctionSymbol()).getNativeDBString(
                                    t.getTerms(),
                                    t2 -> checkColumnID(t2, columnIDs),
                                    termFactory))
                            .orElseThrow(() -> new SQLSerializationException("Only DBFunctionSymbols must be provided " +
                                    "to a SQLTermSerializer"));
                }
            }

            private String serializeConstant(Constant constant) {
                if (constant.isNull())
                    return constant.getValue();
                if (!(constant instanceof DBConstant)) {
                    throw new SQLSerializationException(
                            "Only DBConstants or NULLs are expected in sub-tree to be translated into SQL");
                }
                return serializeDBConstant((DBConstant) constant);
            }

            protected String serializeDBConstant(DBConstant constant) {
                DBTermType dbType = constant.getType();

                switch (dbType.getCategory()) {
                    case DECIMAL:
                    case FLOAT_DOUBLE:
                        // TODO: handle the special case of not-a-number!
                        return castFloatingConstant(constant.getValue(), dbType);
                    case INTEGER:
                    case BOOLEAN:
                        return constant.getValue();
                    default:
                        return serializeStringConstant(constant.getValue());
                }
            }

            protected String castFloatingConstant(String value, DBTermType dbType) {
                return String.format("CAST(%s AS %s)", value, dbType.getCastName());
            }

            protected String serializeStringConstant(String constant) {
                // duplicates single quotes, and adds outermost quotes
                return "'" + constant.replace("'", "''") + "'";
            }
        });
    }
}