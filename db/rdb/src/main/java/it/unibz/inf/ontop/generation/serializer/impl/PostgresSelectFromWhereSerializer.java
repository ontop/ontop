package it.unibz.inf.ontop.generation.serializer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.dbschema.QualifiedAttributeID;
import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.generation.algebra.SQLFlattenExpression;
import it.unibz.inf.ontop.generation.algebra.SelectFromWhereWithModifiers;
import it.unibz.inf.ontop.generation.serializer.SQLSerializationException;
import it.unibz.inf.ontop.generation.serializer.SelectFromWhereSerializer;
import it.unibz.inf.ontop.dbschema.DBParameters;
import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TermTypeInference;
import it.unibz.inf.ontop.model.vocabulary.XSD;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Singleton
public class PostgresSelectFromWhereSerializer extends DefaultSelectFromWhereSerializer implements SelectFromWhereSerializer {

    @Inject
    private PostgresSelectFromWhereSerializer(TermFactory termFactory) {
        super(new DefaultSQLTermSerializer(termFactory) {
            @Override
            protected String castFloatingConstant(String value, DBTermType dbType) {
                return String.format("%s::%s", value, dbType.getCastName());
            }

            @Override
            protected String serializeDatetimeConstant(String datetime, DBTermType dbType) {
                return String.format("CAST(%s AS %s)", serializeStringConstant(datetime), dbType.getCastName());
            }
        });
    }

    @Override
    public SelectFromWhereSerializer.QuerySerialization serialize(SelectFromWhereWithModifiers selectFromWhere, DBParameters dbParameters) {
        return selectFromWhere.acceptVisitor(
                new DefaultSelectFromWhereSerializer.DefaultRelationVisitingSerializer(dbParameters.getQuotedIDFactory()) {
                    /**
                     * https://www.postgresql.org/docs/8.1/queries-limit.html
                     * <p>
                     * [LIMIT { number | ALL }] [OFFSET number]
                     * <p>
                     * If a limit count is given, no more than that many rows will be returned
                     * (but possibly less, if the query itself yields less rows).
                     * LIMIT ALL is the same as omitting the LIMIT clause.
                     * <p>
                     * OFFSET says to skip that many rows before beginning to return rows.
                     * OFFSET 0 is the same as omitting the OFFSET clause. If both OFFSET and LIMIT
                     * appear, then OFFSET rows are skipped before starting to count the LIMIT rows
                     * that are returned.
                     */

                    // serializeLimit and serializeOffset are standard
                    @Override
                    protected String serializeLimitOffset(long limit, long offset, boolean noSortCondition) {
                        return String.format("LIMIT %d\nOFFSET %d", limit, offset);
                    }

                    @Override
                    public QuerySerialization visit(SQLFlattenExpression sqlFlattenExpression) {

                        QuerySerialization subQuerySerialization = getSQLSerializationForChild(sqlFlattenExpression.getSubExpression());
                        ImmutableMap<Variable, QualifiedAttributeID> columnIDs = buildFlattenColumIDMap(
                                sqlFlattenExpression,
                                subQuerySerialization
                        );

                        String sql = getFlattenFunctionSymbolString(sqlFlattenExpression.getFlattenendVar()) +
                                "( " +
                                subQuerySerialization.getString() +
                                " ) AS " +
                                columnIDs.get(sqlFlattenExpression.getOutputVar()).getSQLRendering() + " "+
                                getFlattenIndexInString(sqlFlattenExpression.getIndexVar(), columnIDs);

                        return new QuerySerializationImpl(sql, columnIDs);

                    }

                    private ImmutableMap<Variable, QualifiedAttributeID> buildFlattenColumIDMap(SQLFlattenExpression sqlFlattenExpression,
                                                                                                QuerySerialization subQuerySerialization) {

                        Variable flattenedVar = sqlFlattenExpression.getFlattenendVar();

                        ImmutableMap<Variable, QualifiedAttributeID> freshVariableAliases = createVariableAliases(getFreshVariables(sqlFlattenExpression)).entrySet().stream()
                                .collect(ImmutableCollectors.toMap(
                                        e -> e.getKey(),
                                        e -> new QualifiedAttributeID(null, e.getValue())
                                ));

                        ImmutableMap<Variable, QualifiedAttributeID> retainedVariableAliases = subQuerySerialization.getColumnIDs().entrySet().stream()
                                .filter(e -> e.getKey() != flattenedVar)
                                .collect(ImmutableCollectors.toMap());

                        return ImmutableMap.<Variable, QualifiedAttributeID>builder()
                                .putAll(freshVariableAliases)
                                .putAll(retainedVariableAliases)
                                .build();
                    }

                    private ImmutableSet<Variable> getFreshVariables(SQLFlattenExpression sqlFlattenExpression) {
                        ImmutableSet.Builder<Variable> builder = ImmutableSet.builder();
                        builder.add(sqlFlattenExpression.getOutputVar());
                        sqlFlattenExpression.getIndexVar().ifPresent(builder::add);
                        return builder.build();
                    }

                    private String getFlattenIndexInString(Optional<Variable> indexVar, ImmutableMap<Variable, QualifiedAttributeID> columnIDs) {
                        return indexVar.isPresent()?
                                "WITH ORDINALITY AS " + columnIDs.get(indexVar.get()).getSQLRendering()+" ":
                                "";
                    }

                    private String getFlattenFunctionSymbolString(Variable flattenedVar) {
                        DBTermType termType = getTermType(flattenedVar);
                        switch (termType.getCategory()) {
                            case JSON:
                                return "json_array_elements";
                            case ARRAY:
                                return "unnest";
                            default:
                                throw new SQLSerializationException("Unexpected type for the flattened variable " + flattenedVar);
                        }
                    }

                    private DBTermType getTermType(Variable flattenedVar) {
                        TermType termType = flattenedVar.inferType()
                                .flatMap(t -> t.getTermType())
                                .orElseThrow(
                                        () -> new SQLSerializationException("Unable to infer term type for flattened variable " + flattenedVar));
                        if (termType instanceof DBTermType) {
                            return (DBTermType) termType;
                        }
                        throw new SQLSerializationException("The type of the flattened variable " + flattenedVar + " should be an instance of DBTermType");
                    }
                });
        }
}
