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

    private DatabaseRelationDefinition P;

    private ExtensionalDataNode f1, f2;
    private ImmutableFunctionalTerm eq;
    private Variable x, y, u, v;
    private QualifiedAttributeID qaTx, qaTy, qaNx, qaNy, qaTu, qaTv, qaNu, qaNv;
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

        P = builder.createDatabaseRelation("P",
                "A", integerDBType, true,
                "B", integerDBType, true);

        attX = P.getAttribute(1).getID();
        attY = P.getAttribute(2).getID();

        f1 = IQ_FACTORY.createExtensionalDataNode(P, ImmutableMap.of(0, x, 1, y));

        qaTx = new QualifiedAttributeID(P.getID(), attX);
        qaTy = new QualifiedAttributeID(P.getID(), attY);
        qaNx = new QualifiedAttributeID(null, attX);
        qaNy = new QualifiedAttributeID(null, attY);

        re1 = new RAExpression(ImmutableList.of(f1),
                ImmutableList.of(),
                new RAExpressionAttributes(
                        ImmutableMap.of(qaTx, x, qaTy, y, qaNx, x, qaNy, y),
                        ImmutableMap.of(attX, ImmutableSet.of(P.getID()), attY, P.getAllIDs())), TERM_FACTORY);

        u = TERM_FACTORY.getVariable("u");
        v = TERM_FACTORY.getVariable("v");

        DatabaseRelationDefinition Q = builder.createDatabaseRelation("Q",
            "A", integerDBType, true,
            "C", integerDBType, true);

        QuotedID attu = Q.getAttribute(1).getID();
        QuotedID attv = Q.getAttribute(2).getID();

        f2 = IQ_FACTORY.createExtensionalDataNode(Q, ImmutableMap.of(0, u, 1, v));

        qaTu = new QualifiedAttributeID(Q.getID(), attu);
        qaTv = new QualifiedAttributeID(Q.getID(), attv);
        qaNu = new QualifiedAttributeID(null, attu);
        qaNv = new QualifiedAttributeID(null, attv);

        re2 = new RAExpression(ImmutableList.of(f2),
                ImmutableList.of(),
                new RAExpressionAttributes(
                        ImmutableMap.of(qaTu, u,qaTv, v, qaNu, u, qaNv, v),
                        ImmutableMap.of(attu, ImmutableSet.of(Q.getID()), attv, ImmutableSet.of(Q.getID()))), TERM_FACTORY);
        
        Variable w = TERM_FACTORY.getVariable("u");
        Variable z = TERM_FACTORY.getVariable("v");

        ExtensionalDataNode f3 = IQ_FACTORY.createExtensionalDataNode(Q, ImmutableMap.of(0, w, 1, z));

        RelationID table3 = MDFAC.createRelationID(null, "R");
        QuotedID attW = MDFAC.createAttributeID("A");
        QuotedID attZ = MDFAC.createAttributeID("B");


        // This is used to simulate an ambiguity during the operation of natural join
        re3 = new RAExpression(
                ImmutableList.of(f3),
                ImmutableList.of(),
                RAExpressionAttributes.create(ImmutableMap.of(attW, w, attZ, z), ImmutableSet.of(table3)), TERM_FACTORY);

        eq = TERM_FACTORY.getNotYetTypedEquality(x, u);

        onExpression = new EqualsTo();
        onExpression.setLeftExpression(new Column(new Table("P"), "A"));
        onExpression.setRightExpression(new Column(new Table("Q"), "A"));

        // this relation contains just a common attribute with the RAExpression "re1"
        // and it is used to simulate an exception during the operations of:
        // "cross join" and "join on" and "natural join"
        re1_1 = new RAExpression(ImmutableList.of(f2),
                ImmutableList.of(),
                RAExpressionAttributes.create(ImmutableMap.of(attX, x), P.getAllIDs()), TERM_FACTORY);

        System.out.println("****************************************************");
    }

    @Test
    public void cross_join_test() throws IllegalJoinException {
        System.out.println(re1);
        System.out.println(re2);

        RAExpression relationalExpression = re1.crossJoin(re2);
        System.out.println(relationalExpression);

        crossJoinAndJoinOnCommonAsserts(relationalExpression);
        assertTrue(relationalExpression.getFilterAtoms().isEmpty());
    }

    @Test(expected = IllegalJoinException.class)
    public void cross_join_exception_test() throws IllegalJoinException {
        System.out.println(re1);
        System.out.println(re1_1);

        re1.crossJoin(re1_1);
    }

    @Test
    public void join_on_test() throws IllegalJoinException {
        System.out.println(re1);
        System.out.println(re2);
        System.out.println(eq);

        RAExpression relationalExpression = re1.joinOn(re2,
                attributes -> new ExpressionParser(MDFAC, CORE_SINGLETONS)
                        .parseBooleanExpression(onExpression,  attributes));

        System.out.println(relationalExpression);

        crossJoinAndJoinOnCommonAsserts(relationalExpression);
        assertTrue(relationalExpression.getFilterAtoms().contains(eq));
    }

    @Test(expected = IllegalJoinException.class)
    public void join_on_exception_test() throws IllegalJoinException {
        System.out.println(re1);
        System.out.println(re1_1);

        re1.joinOn(re1_1,
                attributes -> new ExpressionParser(MDFAC, CORE_SINGLETONS)
                        .parseBooleanExpression(onExpression, attributes));
    }

    @Test
    public void natural_join_test() throws IllegalJoinException {
        System.out.println(re1);
        System.out.println(re2);
        System.out.println(eq);

        RAExpression relationalExpression = re1.naturalJoin(re2);
        System.out.println(relationalExpression);

        naturalUsingCommonAsserts(relationalExpression);
    }

    @Test(expected = IllegalJoinException.class)
    public void natural_join_exception_test() throws IllegalJoinException {
        System.out.println(re1);
        System.out.println(re1_1);

        RAExpression relationalExpression = re1.naturalJoin(re1_1);
        System.out.println(relationalExpression);
    }

    @Test(expected = IllegalJoinException.class)
    public void natural_join_ambiguity_test() throws IllegalJoinException {
        System.out.println(re1);
        System.out.println(re2);

        RAExpression re = re1.joinOn(re2,
                attributes -> new ExpressionParser(MDFAC, CORE_SINGLETONS)
                        .parseBooleanExpression(onExpression, attributes));

        System.out.println(re);
        System.out.println(re3);

        re.naturalJoin(re3);
    }

    @Test
    public void join_using_test() throws IllegalJoinException {
        System.out.println(re1);
        System.out.println(re2);
        System.out.println(eq);

        RAExpression relationalExpression =
                re1.joinUsing(re2, ImmutableSet.of(MDFAC.createAttributeID("A")));

        System.out.println(relationalExpression);

        naturalUsingCommonAsserts(relationalExpression);
    }


    @Test(expected = IllegalJoinException.class)
    public void join_using_exception_test() throws IllegalJoinException {
        System.out.println(re1);
        System.out.println(re1_1);

        RAExpression relationalExpression = re1.joinUsing(re1_1,
                ImmutableSet.of(MDFAC.createAttributeID("A")));
        System.out.println(relationalExpression);
    }

    @Test(expected = IllegalJoinException.class)
    public void join_using_no_commons_test() throws IllegalJoinException {

        // a new relationId without any common attribute with the re1 is created to simulate an exception
        RAExpression re2 =  new RAExpression(ImmutableList.of(f2),
                ImmutableList.of(),
                RAExpressionAttributes.create(
                        ImmutableMap.of(MDFAC.createAttributeID("C"), u,  MDFAC.createAttributeID("D"), v),
                        ImmutableSet.of(MDFAC.createRelationID(null, "Q"))), TERM_FACTORY);

        System.out.println(re1);
        System.out.println(re2);

        re1.joinUsing(re2, ImmutableSet.of(MDFAC.createAttributeID("A")));
    }

    @Test(expected = IllegalJoinException.class)
    public void join_using_ambiguity_test() throws IllegalJoinException {
        System.out.println(re1);
        System.out.println(re2);

        RAExpression relationalExpression = re1.joinOn(re2,
                attributes -> new ExpressionParser(MDFAC, CORE_SINGLETONS)
                        .parseBooleanExpression(onExpression, attributes));

        System.out.println(relationalExpression);
        System.out.println(re3);

        relationalExpression.joinUsing(re3, ImmutableSet.of(MDFAC.createAttributeID("A")));
    }


    @Test
    public void alias_test() {
        RelationID tableAlias = MDFAC.createRelationID(null, "S");
        QualifiedAttributeID qaAx = new QualifiedAttributeID(tableAlias, attX);
        QualifiedAttributeID qaAy = new QualifiedAttributeID(tableAlias, attY);

        System.out.println(re1);
        RAExpression actual =  re1.withAlias(tableAlias);
        System.out.println(actual);

        assertTrue(actual.getDataAtoms().contains(f1));

        assertEquals(ImmutableMap.of(qaNx, x, qaNy, y, qaAx, x, qaAy, y), actual.getAttributes());
    }

    @Test
    public void create_test() {
        RAExpression actual = new RAExpression(re1.getDataAtoms(),
                re1.getFilterAtoms(),
                RAExpressionAttributes.create(ImmutableMap.of(attX, x, attY, y), P.getAllIDs()), TERM_FACTORY);
        System.out.println(actual);

        assertEquals(ImmutableMap.of(qaNx, x, qaNy, y, qaTx, x, qaTy, y), actual.getAttributes());
    }


    private void naturalUsingCommonAsserts(RAExpression relationalExpression) {
        assertTrue(relationalExpression.getDataAtoms().contains(f1));
        assertTrue(relationalExpression.getDataAtoms().contains(f2));
        assertEquals(ImmutableList.of(eq), relationalExpression.getFilterAtoms());

        assertEquals(ImmutableMap.of(qaNx, x, qaTy, y, qaNy, y, qaTv, v, qaNv, v), relationalExpression.getAttributes());
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
                .put(qaNv, v).build(), relationalExpression.getAttributes());
    }
}
