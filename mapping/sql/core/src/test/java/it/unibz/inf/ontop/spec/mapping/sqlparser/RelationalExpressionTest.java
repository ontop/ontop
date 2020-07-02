package it.unibz.inf.ontop.spec.mapping.sqlparser;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.OfflineMetadataProviderBuilder;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.spec.mapping.sqlparser.exception.IllegalJoinException;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import org.junit.Before;
import org.junit.Test;

import static it.unibz.inf.ontop.utils.SQLMappingTestingTools.*;
import static org.junit.Assert.*;

/**
 * Created by Roman Kontchakov on 01/11/2016.
 *
 */

// TODO: REFACTOR

public class RelationalExpressionTest {

    private static QuotedIDFactory MDFAC;

    private DatabaseRelationDefinition TABLE_P, TABLE_Q, TABLE_R;

    private ExtensionalDataNode f1, f2;
    private ImmutableFunctionalTerm eq;
    private Variable x, y, u, v;
    private QualifiedAttributeID qaTx;
    private QualifiedAttributeID qaTy;
    private QualifiedAttributeID qaNx;
    private QualifiedAttributeID qaNy;
    private QualifiedAttributeID qaTu;
    private QualifiedAttributeID qaTv;
    private QualifiedAttributeID qaNv;
    private QuotedID attX, attY;
    private RAExpression re1, re2, re1_1, re3;
    private EqualsTo onExpression;

    @Before
    public void setupTest(){
        OfflineMetadataProviderBuilder builder = createMetadataProviderBuilder();
        MDFAC = builder.getQuotedIDFactory();

        x = TERM_FACTORY.getVariable("x");
        y = TERM_FACTORY.getVariable("y");

        DBTermType integerDBType = builder.getDBTypeFactory().getDBLargeIntegerType();

        TABLE_P = builder.createDatabaseRelation("P",
                "A", integerDBType, true,
                "B", integerDBType, true);

        TABLE_Q = builder.createDatabaseRelation("Q",
                "C", integerDBType, true,
                "D", integerDBType, true);

        TABLE_R = builder.createDatabaseRelation("R",
                "A", integerDBType, true,
                "B", integerDBType, true);


        RAExpressionOperations ops = new RAExpressionOperations(TERM_FACTORY, IQ_FACTORY);

        attX = TABLE_P.getAttribute(1).getID();
        attY = TABLE_P.getAttribute(2).getID();

        qaTx = new QualifiedAttributeID(TABLE_P.getID(), attX);
        qaTy = new QualifiedAttributeID(TABLE_P.getID(), attY);
        qaNx = new QualifiedAttributeID(null, attX);
        qaNy = new QualifiedAttributeID(null, attY);

        re1 = ops.create(TABLE_P, ImmutableList.of(x, y));
        f1 = re1.getDataAtoms().get(0);

        u = TERM_FACTORY.getVariable("u");
        v = TERM_FACTORY.getVariable("v");

        DatabaseRelationDefinition Q = builder.createDatabaseRelation("Q",
            "A", integerDBType, true,
            "C", integerDBType, true);

        QuotedID attu = Q.getAttribute(1).getID();
        QuotedID attv = Q.getAttribute(2).getID();

        qaTu = new QualifiedAttributeID(Q.getID(), attu);
        qaTv = new QualifiedAttributeID(Q.getID(), attv);
        qaNv = new QualifiedAttributeID(null, attv);

        re2 = ops.create(Q, ImmutableList.of(u, v));
        f2 = re2.getDataAtoms().get(0);

        Variable w = TERM_FACTORY.getVariable("u");
        Variable z = TERM_FACTORY.getVariable("v");

        // This is used to simulate an ambiguity during the operation of natural join
        re3 = ops.create(TABLE_R, ImmutableList.of(w, z));

        eq = TERM_FACTORY.getNotYetTypedEquality(x, u);

        onExpression = new EqualsTo();
        onExpression.setLeftExpression(new Column(new Table("P"), "A"));
        onExpression.setRightExpression(new Column(new Table("Q"), "A"));

        re1_1 = ops.withAlias(re2, builder.getQuotedIDFactory().createRelationID(null, "P"));


        System.out.println("****************************************************");
    }

    @Test
    public void cross_join_test() throws IllegalJoinException {
        System.out.println(re1);
        System.out.println(re2);

        RAExpressionOperations ops = new RAExpressionOperations(TERM_FACTORY, IQ_FACTORY);
        RAExpression relationalExpression = ops.crossJoin(re1, re2);
        System.out.println(relationalExpression);

        crossJoinAndJoinOnCommonAsserts(relationalExpression);
        assertTrue(relationalExpression.getFilterAtoms().isEmpty());
    }

    @Test(expected = IllegalJoinException.class)
    public void cross_join_exception_test() throws IllegalJoinException {
        System.out.println(re1);
        System.out.println(re1_1);

        RAExpressionOperations ops = new RAExpressionOperations(TERM_FACTORY, IQ_FACTORY);
        ops.crossJoin(re1, re1_1);
    }

    @Test
    public void join_on_test() throws IllegalJoinException {
        System.out.println(re1);
        System.out.println(re2);
        System.out.println(eq);

        RAExpressionOperations ops = new RAExpressionOperations(TERM_FACTORY, IQ_FACTORY);
        RAExpression relationalExpression = ops.joinOn(re1, re2,
                attributes -> new ExpressionParser(MDFAC, CORE_SINGLETONS)
                        .parseBooleanExpression(onExpression,  attributes));

        System.out.println(relationalExpression);

        crossJoinAndJoinOnCommonAsserts(relationalExpression);
        assertEquals(ImmutableList.of(eq), relationalExpression.getFilterAtoms());
    }

    @Test(expected = IllegalJoinException.class)
    public void join_on_exception_test() throws IllegalJoinException {
        System.out.println(re1);
        System.out.println(re1_1);

        RAExpressionOperations ops = new RAExpressionOperations(TERM_FACTORY, IQ_FACTORY);
        ops.joinOn(re1, re1_1,
                attributes -> new ExpressionParser(MDFAC, CORE_SINGLETONS)
                        .parseBooleanExpression(onExpression, attributes));
    }

    @Test
    public void natural_join_test() throws IllegalJoinException {
        System.out.println(re1);
        System.out.println(re2);
        System.out.println(eq);

        RAExpressionOperations ops = new RAExpressionOperations(TERM_FACTORY, IQ_FACTORY);
        RAExpression relationalExpression = ops.naturalJoin(re1, re2);
        System.out.println(relationalExpression);

        naturalUsingCommonAsserts(relationalExpression);
    }

    @Test(expected = IllegalJoinException.class)
    public void natural_join_exception_test() throws IllegalJoinException {
        System.out.println(re1);
        System.out.println(re1_1);

        RAExpressionOperations ops = new RAExpressionOperations(TERM_FACTORY, IQ_FACTORY);
        RAExpression relationalExpression = ops.naturalJoin(re1, re1_1);
        System.out.println(relationalExpression);
    }

    @Test(expected = IllegalJoinException.class)
    public void natural_join_ambiguity_test() throws IllegalJoinException {
        System.out.println(re1);
        System.out.println(re2);

        RAExpressionOperations ops = new RAExpressionOperations(TERM_FACTORY, IQ_FACTORY);
        RAExpression re = ops.joinOn(re1, re2,
                attributes -> new ExpressionParser(MDFAC, CORE_SINGLETONS)
                        .parseBooleanExpression(onExpression, attributes));

        System.out.println(re);
        System.out.println(re3);

        ops.naturalJoin(re, re3);
    }

    @Test
    public void join_using_test() throws IllegalJoinException {
        System.out.println(re1);
        System.out.println(re2);
        System.out.println(eq);

        RAExpressionOperations ops = new RAExpressionOperations(TERM_FACTORY, IQ_FACTORY);
        RAExpression relationalExpression =
                ops.joinUsing(re1, re2, ImmutableSet.of(MDFAC.createAttributeID("A")));

        System.out.println(relationalExpression);

        naturalUsingCommonAsserts(relationalExpression);
    }


    @Test(expected = IllegalJoinException.class)
    public void join_using_exception_test() throws IllegalJoinException {
        System.out.println(re1);
        System.out.println(re1_1);

        RAExpressionOperations ops = new RAExpressionOperations(TERM_FACTORY, IQ_FACTORY);
        RAExpression relationalExpression = ops.joinUsing(re1, re1_1,
                ImmutableSet.of(MDFAC.createAttributeID("A")));
        System.out.println(relationalExpression);
    }

    @Test(expected = IllegalJoinException.class)
    public void join_using_no_commons_test() throws IllegalJoinException {

        RAExpressionOperations ops = new RAExpressionOperations(TERM_FACTORY, IQ_FACTORY);
        RAExpression re2p = ops.create(TABLE_Q, ImmutableList.of(u, v));

        System.out.println(re1);
        System.out.println(re2p);

        ops.joinUsing(re1, re2p, ImmutableSet.of(MDFAC.createAttributeID("A")));
    }

    @Test(expected = IllegalJoinException.class)
    public void join_using_ambiguity_test() throws IllegalJoinException {
        System.out.println(re1);
        System.out.println(re2);

        RAExpressionOperations ops = new RAExpressionOperations(TERM_FACTORY, IQ_FACTORY);
        RAExpression relationalExpression = ops.joinOn(re1, re2,
                attributes -> new ExpressionParser(MDFAC, CORE_SINGLETONS)
                        .parseBooleanExpression(onExpression, attributes));

        System.out.println(relationalExpression);
        System.out.println(re3);

        ops.joinUsing(relationalExpression, re3, ImmutableSet.of(MDFAC.createAttributeID("A")));
    }


    @Test
    public void alias_test() {
        RelationID tableAlias = MDFAC.createRelationID(null, "S");
        QualifiedAttributeID qaAx = new QualifiedAttributeID(tableAlias, attX);
        QualifiedAttributeID qaAy = new QualifiedAttributeID(tableAlias, attY);

        System.out.println(re1);
        RAExpressionOperations ops = new RAExpressionOperations(TERM_FACTORY, IQ_FACTORY);
        RAExpression actual =  ops.withAlias(re1, tableAlias);
        System.out.println(actual);

        assertTrue(actual.getDataAtoms().contains(f1));

        assertEquals(ImmutableMap.of(qaNx, x, qaNy, y, qaAx, x, qaAy, y), actual.getAttributes().asMap());
    }

    @Test
    public void create_test() {
        System.out.println(re1);

        assertEquals(ImmutableMap.of(qaNx, x, qaNy, y, qaTx, x, qaTy, y), re1.getAttributes().asMap());
    }


    private void naturalUsingCommonAsserts(RAExpression relationalExpression) {
        assertTrue(relationalExpression.getDataAtoms().contains(f1));
        assertTrue(relationalExpression.getDataAtoms().contains(f2));
        assertEquals(ImmutableList.of(eq), relationalExpression.getFilterAtoms());

        assertEquals(ImmutableMap.of(qaNx, x, qaTy, y, qaNy, y, qaTv, v, qaNv, v), relationalExpression.getAttributes().asMap());
    }

    private void crossJoinAndJoinOnCommonAsserts(RAExpression relationalExpression ){
        assertTrue(relationalExpression.getDataAtoms().contains(f1));
        assertTrue(relationalExpression.getDataAtoms().contains(f2));

        assertEquals(ImmutableMap.builder()
                .put(qaTx, x)
                .put(qaTy, y)
                .put(qaNy, y)
                .put(qaTu, u)
                .put(qaTv, v)
                .put(qaNv, v).build(), relationalExpression.getAttributes().asMap());
    }
}
