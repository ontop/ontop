package it.unibz.inf.ontop.generation.serializer.impl;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.dbschema.QualifiedAttributeID;
import it.unibz.inf.ontop.generation.algebra.SQLFlattenExpression;
import it.unibz.inf.ontop.generation.algebra.SelectFromWhereWithModifiers;
import it.unibz.inf.ontop.dbschema.DBParameters;
import it.unibz.inf.ontop.injection.OntopSQLCoreSettings;
import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.type.DBTermType;

import java.util.Optional;

@Singleton
public class OracleSelectFromWhereSerializer extends DefaultSelectFromWhereSerializer {

    @Inject
    private OracleSelectFromWhereSerializer(TermFactory termFactory, OntopSQLCoreSettings settings) {
        super(new DefaultSQLTermSerializer(termFactory) {
            @Override
            protected String serializeDBConstant(DBConstant constant) {
                DBTermType dbType = constant.getType();
                switch (dbType.getCategory()) {
                    case DATETIME:
                        return String.format("TIMESTAMP '%s'", constant.getValue());
                    default:
                        return super.serializeDBConstant(constant);
                }
            }
        }, settings);
    }

    public static final int NAME_MAX_LENGTH = 30;
    public static final int NAME_NUMBER_LENGTH = 3;

    @Override
    public QuerySerialization serialize(SelectFromWhereWithModifiers selectFromWhere, DBParameters dbParameters) {
        return selectFromWhere.acceptVisitor(new DefaultRelationVisitingSerializer(dbParameters.getQuotedIDFactory()) {

            @Override
            protected AttributeAliasFactory createAttributeAliasFactory() {
                return new LimitLengthAttributeAliasFactory(idFactory, NAME_MAX_LENGTH, NAME_NUMBER_LENGTH);
            }

            @Override
            protected String serializeDummyTable() {
                return "FROM dual";
            }

            /**
             * Versions < 12.1 are not supported
             * Reason: In 12.1 and later, you can use the OFFSET and/or FETCH [FIRST | NEXT] operators
             */
            @Override
            protected String serializeLimitOffset(long limit, long offset, boolean noSortCondition) {
                return String.format("OFFSET %d ROWS\nFETCH NEXT %d ROWS ONLY", offset, limit);
            }

            @Override
            protected String serializeLimit(long limit, boolean noSortCondition) {
                // ROWNUM <= limit could also be used
                return String.format("FETCH NEXT %d ROWS ONLY", limit);
            }

            @Override
            protected String serializeOffset(long offset, boolean noSortCondition) {
                return String.format("OFFSET %d ROWS\nFETCH NEXT 99999999 ROWS ONLY", offset);
            }

            @Override
            protected QuerySerialization serializeFlatten(SQLFlattenExpression sqlFlattenExpression, Variable flattenedVar, Variable outputVar, Optional<Variable> indexVar, DBTermType flattenedType, ImmutableMap<Variable, QualifiedAttributeID> allColumnIDs, QuerySerialization subQuerySerialization) {
                // `SELECT <variables> FROM <subquery> CROSS JOIN JSON_TABLE(<flattenedVar>, '$[*]' COLUMNS (<outputVar> VARCHAR2(1000) FORMAT JSON PATH '$' [, <indexVar> FOR ORDINALITY]))
                return serializeFlattenAsJoin(
                        flattenedVar,
                        String.format("JSON_TABLE(%s, '$[*]' COLUMNS(%s VARCHAR2(1000) FORMAT JSON PATH '$'%s))",
                                serializeTerm(flattenedVar, allColumnIDs),
                                serializeTerm(outputVar, allColumnIDs),
                                serializeOptionalTerm(", %s FOR ORDINALITY", indexVar, allColumnIDs)),
                        allColumnIDs,
                        subQuerySerialization);
            }
        });
    }
}
