package it.unibz.inf.ontop.generation.serializer.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.dbschema.QualifiedAttributeID;
import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.generation.algebra.SQLFlattenExpression;
import it.unibz.inf.ontop.generation.algebra.SelectFromWhereWithModifiers;
import it.unibz.inf.ontop.generation.serializer.SQLSerializationException;
import it.unibz.inf.ontop.generation.serializer.SelectFromWhereSerializer;
import it.unibz.inf.ontop.dbschema.DBParameters;
import it.unibz.inf.ontop.injection.OntopSQLCoreSettings;
import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.model.type.GenericDBTermType;
import it.unibz.inf.ontop.model.type.impl.ArrayDBTermType;

import java.util.Optional;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.model.type.impl.PostgreSQLDBTypeFactory.*;

@Singleton
public class PostgresSelectFromWhereSerializer extends DefaultSelectFromWhereSerializer implements SelectFromWhereSerializer {

    @Inject
    protected PostgresSelectFromWhereSerializer(TermFactory termFactory, OntopSQLCoreSettings settings) {
        super(new DefaultSQLTermSerializer(termFactory) {
            @Override
            protected String castFloatingConstant(String value, DBTermType dbType) {
                return String.format("%s::%s", value, dbType.getCastName());
            }

            @Override
            protected String serializeDatetimeConstant(String datetime, DBTermType dbType) {
                return String.format("CAST(%s AS %s)", serializeStringConstant(datetime), dbType.getCastName());
            }

            @Override
            protected String serializeBooleanConstant(DBConstant booleanConstant) {
                String value = booleanConstant.getValue();
                switch (value.toLowerCase()) {
                    case "false":
                    case "true":
                        return value;
                        // E.g. f and t need single quotes
                    default:
                        return "'" + value + "'";
                }
            }
        }, settings);
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

                    /**
                     * Generate a new variable name as an intermediate term for the Array unnest operation.
                     */
                    private QuotedID generateIntermediateVariable(String outputVarName, ImmutableSet<Variable> existingVariables) {
                        int index = 1;
                        while (true) {
                            String newVarName = outputVarName + "_intermediate" + index;
                            if(existingVariables.stream()
                                    .noneMatch(v -> v.getName().equals(newVarName))) {
                                return createAttributeAliasFactory().createAttributeAlias(newVarName);
                            }
                            index++;
                        }
                    }

                    @Override
                    protected QuerySerialization serializeFlatten(SQLFlattenExpression sqlFlattenExpression,
                                                                  Variable flattenedVar, Variable outputVar,
                                                                  Optional<Variable> indexVar, DBTermType flattenedType,
                                                                  ImmutableMap<Variable, QualifiedAttributeID> allColumnIDs,
                                                                  QuerySerialization subQuerySerialization) {
                        //We need special treatment, if we are trying to flatten an array of type T[][], T[][][], etc.
                        boolean flatteningNDArray = (sqlFlattenExpression.getFlattenedType() instanceof ArrayDBTermType
                                && ((GenericDBTermType) sqlFlattenExpression.getFlattenedType()).getGenericArguments().get(0).getCategory() == DBTermType.Category.ARRAY);

                        //We now build the query string of the form SELECT <variables> FROM <subquery> JOIN LATERAL <flatten_function>(<flattenedVariable>) WITH ORDINALITY AS <name>

                        /*
                         * If we are flattening an ND-Array, we need to first transform it into a JSONB array,
                         * call jsonb_array_elements on it, then transform it back into an Array in a further subquery.
                         */
                        if (flatteningNDArray) {
                            RelationID castAlias = generateFreshViewAlias();

                            QuotedID intermediateOutputVar = generateIntermediateVariable(outputVar.getName(), allColumnIDs.keySet());
                            String string = String.format("%s JOIN LATERAL %s %s AS %s ON TRUE",
                                    subQuerySerialization.getString(),
                                    String.format(getFlattenFunctionSymbolString(sqlFlattenExpression.getFlattenedType()),
                                            serializeTerm(flattenedVar, allColumnIDs)),
                                    serializeOptionalTerm("WITH ORDINALITY", indexVar, allColumnIDs),
                                    getOutputVarsRendering(intermediateOutputVar.getSQLRendering(), indexVar, allColumnIDs, castAlias));

                            QuerySerialization qs = new QuerySerializationImpl(string, subQuerySerialization.getColumnIDs(), subQuerySerialization.getCTEMap());

                            return serializeFlattenAsSubQuery(flattenedVar, allColumnIDs, qs,
                                    Stream.concat(
                                            indexVar.stream().map(ind -> serializeColumnAlias(
                                                    new QualifiedAttributeID(castAlias, allColumnIDs.get(ind).getAttribute()).toString(),
                                                    indexVar.get().getName())),
                                            Stream.of(serializeColumnAlias(
                                                    String.format("ARRAY(SELECT jsonb_array_elements_text(%s))::%s",
                                                            intermediateOutputVar.getSQLRendering(),
                                                            ((ArrayDBTermType) sqlFlattenExpression.getFlattenedType()).getGenericArguments().get(0).getCastName()),
                                                    serializeTerm(outputVar, allColumnIDs)))));
                        }

                        String string = String.format("%s JOIN LATERAL %s %s AS %s ON TRUE",
                                subQuerySerialization.getString(),
                                String.format(getFlattenFunctionSymbolString(sqlFlattenExpression.getFlattenedType()),
                                        serializeTerm(flattenedVar, allColumnIDs)),
                                serializeOptionalTerm("WITH ORDINALITY", indexVar, allColumnIDs),
                                getOutputVarsRendering(serializeTerm(outputVar, allColumnIDs), indexVar, allColumnIDs, generateFreshViewAlias()));

                        return new QuerySerializationImpl(
                                string,
                                getFlattenAllColumnIDs(flattenedVar, allColumnIDs),
                                subQuerySerialization.getCTEMap());
                    }

                    private String getOutputVarsRendering(String outputVarString, Optional<Variable> indexVar, ImmutableMap<Variable, QualifiedAttributeID> allColumnIDs, RelationID viewAlias) {
                        return indexVar.isPresent()
                                ? String.format("%s(%s, %s)",
                                        viewAlias.getSQLRendering(),
                                        outputVarString,
                                        serializeTerm(indexVar.get(), allColumnIDs))
                                : outputVarString;
                    }

                    private String getFlattenFunctionSymbolString(DBTermType dbType) {
                        DBTypeFactory dbTypeFactory = dbParameters.getDBTypeFactory();

                        if (dbTypeFactory.getDBTermType(JSON_STR).equals(dbType)) {
                            return "json_array_elements(%s)";
                        }
                        if (dbTypeFactory.getDBTermType(JSONB_STR).equals(dbType)) {
                            return "jsonb_array_elements(%s)";
                        }
                        if (dbType.getCategory() == DBTermType.Category.ARRAY) {
                            GenericDBTermType genericDbType = (GenericDBTermType) dbType;
                            //When it is a multidimensional array, we cannot use unnest, because it would flatten all levels at once.
                            return (genericDbType.getGenericArguments().get(0).getCategory() == DBTermType.Category.ARRAY)
                                ? "jsonb_array_elements(to_jsonb(%s))"
                                : "unnest(%s)";
                        }

                        throw new SQLSerializationException("Unsupported nested type for flattening: " + dbType.getName());
                    }
                });
        }
}
