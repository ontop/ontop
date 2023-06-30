package it.unibz.inf.ontop.generation.serializer.impl;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.dbschema.DBParameters;
import it.unibz.inf.ontop.generation.algebra.SQLOneTupleDummyQueryExpression;
import it.unibz.inf.ontop.generation.algebra.SelectFromWhereWithModifiers;
import it.unibz.inf.ontop.generation.serializer.SelectFromWhereSerializer;
import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.DBTermType;

@Singleton
public class CDataDynamoDBSelectFromWhereSerializer extends DefaultSelectFromWhereSerializer implements SelectFromWhereSerializer {

    @Inject
    private CDataDynamoDBSelectFromWhereSerializer(TermFactory termFactory) {
        super(new DefaultSQLTermSerializer(termFactory) {
            @Override
            protected String serializeDBConstant(DBConstant constant) {
                // Remove leading '+' if constant is e.g. '+1'.
                if(constant.getType().getCategory() == DBTermType.Category.INTEGER) {
                    if(constant.getValue().stripLeading().startsWith("+"))
                        return constant.getValue().stripLeading().substring(1);
                }
                return super.serializeDBConstant(constant);
            }

        });
    }

    @Override
    public QuerySerialization serialize(SelectFromWhereWithModifiers selectFromWhere, DBParameters dbParameters) {
        return selectFromWhere.acceptVisitor(
                new DefaultRelationVisitingSerializer(dbParameters.getQuotedIDFactory()) {

                    @Override
                    public QuerySerialization visit(SQLOneTupleDummyQueryExpression sqlOneTupleDummyQueryExpression) {
                        String fromString = serializeDummyTable();
                        String sqlSubString = String.format("(SELECT 1 %s) tdummy", fromString);
                        return new QuerySerializationImpl(sqlSubString, ImmutableMap.of());
                    }

                    @Override
                    protected String formatBinaryJoin(String operatorString, QuerySerialization left, QuerySerialization right, String onString) {
                        return String.format("(%s\n %s \n%s %s)", left.getString(), operatorString, right.getString(), onString);
                    }


                });
    }
}
