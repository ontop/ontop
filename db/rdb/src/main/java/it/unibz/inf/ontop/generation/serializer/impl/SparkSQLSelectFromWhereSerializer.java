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
import it.unibz.inf.ontop.generation.algebra.SQLFlattenExpression;
import it.unibz.inf.ontop.generation.algebra.SQLOrderComparator;
import it.unibz.inf.ontop.generation.algebra.SelectFromWhereWithModifiers;
import it.unibz.inf.ontop.generation.algebra.impl.SelectFromWhereWithModifiersImpl;
import it.unibz.inf.ontop.generation.serializer.SQLSerializationException;
import it.unibz.inf.ontop.generation.serializer.SelectFromWhereSerializer;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.StringUtils;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Singleton
public class SparkSQLSelectFromWhereSerializer extends DefaultSelectFromWhereSerializer implements SelectFromWhereSerializer {

    private static final String SELECT_FROM_WHERE_MODIFIERS_TEMPLATE = "SELECT %s%s\nFROM %s\n%s%s%s%s";
    private static final ImmutableMap<Character, String> BACKSLASH = ImmutableMap.of('\\', "\\\\");


    @Inject
    private SparkSQLSelectFromWhereSerializer(TermFactory termFactory) {
        super(new DefaultSQLTermSerializer(termFactory) {
            @Override
            protected String serializeStringConstant(String constant) {
                // parent method + doubles backslashes
                return StringUtils.encode(super.serializeStringConstant(constant), BACKSLASH);
            }
        });
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
                String sliceString = serializeSlice(selectFromWhere.getLimit(), selectFromWhere.getOffset(),
                        selectFromWhere.getSortConditions().isEmpty());

                String sql = String.format(SELECT_FROM_WHERE_MODIFIERS_TEMPLATE, distinctString, projectionString,
                        fromString, whereString, groupByString, orderByString, sliceString);

                // Creates an alias for this SQLExpression and uses it for the projected columns
                RelationID alias = generateFreshViewAlias();
                return new QuerySerializationImpl(sql, attachRelationAlias(alias, variableAliases));
            }

            /**
             * SPARKSQL "ORDER BY" construct doesn't accept "relationID.attribute" notation for listing attributes.
             * It is needed a custom serialization for extracting the COLUMN ALIASES.
             */
            private String serializeOrderBy(ImmutableList<SQLOrderComparator> sortConditions,
                                            ImmutableMap<Variable, QualifiedAttributeID> columnIDs,
                                            ImmutableMap<Variable, QuotedID> variableAliases,
                                            Substitution<? extends ImmutableTerm> substitution) {
                if (sortConditions.isEmpty())
                    return "";

                String conditionString = sortConditions.stream()
                        .map(c -> serializeOrderByTerm(c.getTerm(), columnIDs, variableAliases, substitution)
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
                                                ImmutableMap<Variable, QuotedID> variableAliases,
                                                Substitution<? extends ImmutableTerm> substitution)
                    throws SQLSerializationException {

                if (term instanceof Constant) {
                    return getTermSerializer().serialize(term, columnIDs);
                }
                else if (term instanceof Variable) {
                    Optional<QuotedID> alias = Optional.ofNullable(variableAliases.get(term));
                    return alias.map(QuotedID::getSQLRendering)
                            .orElseThrow(() -> new SQLSerializationException(String.format(
                                    "Variable %s does not occur in the variableAliases %s", term, variableAliases)));
                }
                else {
                    // use the project expression alias instead of processing the expression itself
                    return substitution.getPreImage(t -> t.equals(term))
                            .stream()
                            .findFirst()
                            .map(v -> variableAliases.get(v).getSQLRendering())
                            .orElseThrow(() -> new SQLSerializationException(
                                    String.format("Term %s does not occur in the substitution %s", term, substitution)));
                }
            }

            @Override
            protected String formatBinaryJoin(String operatorString, QuerySerialization left, QuerySerialization right, String onString) {
                return String.format("(%s\n %s \n%s %s)", left.getString(), operatorString, right.getString(), onString);
            }

            @Override
            protected QuerySerialization serializeFlatten(SQLFlattenExpression sqlFlattenExpression, Variable flattenedVar, Variable outputVar, Optional<Variable> indexVar, DBTermType flattenedType, ImmutableMap<Variable, QualifiedAttributeID> allColumnIDs, QuerySerialization subQuerySerialization) {
                //We express the flatten call as a `SELECT *, EXPLODE_OUTER({array}) FROM child.

                //EXPLODE only works on ARRAY<T> types, so we first transform the JSON-array into an ARRAY<STRING> if it is not already one
                var expression = flattenedType.getCategory() == DBTermType.Category.ARRAY
                        ? allColumnIDs.get(flattenedVar).getSQLRendering()
                        : String.format("FROM_JSON(%s, 'ARRAY<STRING>')", allColumnIDs.get(flattenedVar).getSQLRendering());

                //If an index is required, we use POSEXPLODE instead of EXPLODE
                String flattenCall;
                String aliasFormat;
                if(indexVar.isPresent()) {
                    flattenCall = String.format("POSEXPLODE_OUTER(%s)", expression);
                    aliasFormat = String.format("(%s, %s)",
                            allColumnIDs.get(indexVar.get()).getSQLRendering(),
                            allColumnIDs.get(outputVar).getSQLRendering());
                } else {
                    flattenCall = String.format("EXPLODE_OUTER(%s)", expression);
                    aliasFormat = String.format("%s",
                            allColumnIDs.get(outputVar).getSQLRendering());
                }
                return serializeFlattenAsFunction(flattenedVar, allColumnIDs, subQuerySerialization, flattenCall, aliasFormat);
            }
        });
    }
}