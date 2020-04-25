package it.unibz.inf.ontop.spec.mapping.sqlparser;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.OfflineMetadataProviderBuilder;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.atom.RelationPredicate;
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

    private DataAtom<RelationPredicate> f1, f2;
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

        f1 = ATOM_FACTORY.getDataAtom(P.getAtomPredicate(), ImmutableList.of(x, y));

        qaTx = new QualifiedAttributeID(P.getID(), attX);
        qaTy = new QualifiedAttributeID(P.getID(), attY);
        qaNx = new QualifiedAttributeID(null, attX);
        qaNy = new QualifiedAttributeID(null, attY);

        re1 = new RAExpression(ImmutableList.of(f1),
                ImmutableList.of(),
                new RAExpressionAttributes(
                        ImmutableMap.of(qaTx, x, qaTy, y, qaNx, x, qaNy, y),
                        ImmutableMap.of(attX, ImmutableSet.of(P.getID()), attY, P.getAllIDs())));

        u = TERM_FACTORY.getVariable("u");
        v = TERM_FACTORY.getVariable("v");

        DatabaseRelationDefinition Q = builder.createDatabaseRelation("Q",
            "A", integerDBType, true,
            "C", integerDBType, true);

        QuotedID attu = Q.getAttribute(1).getID();
        QuotedID attv = Q.getAttribute(2).getID();

        f2 = ATOM_FACTORY.getDataAtom(Q.getAtomPredicate(), ImmutableList.of(u, v));

        qaTu = new QualifiedAttributeID(Q.getID(), attu);
        qaTv = new QualifiedAttributeID(Q.getID(), attv);
        qaNu = new QualifiedAttributeID(null, attu);
        qaNv = new QualifiedAttributeID(null, attv);

        re2 = new RAExpression(ImmutableList.of(f2),
                ImmutableList.of(),
                new RAExpressionAttributes(
                        ImmutableMap.of(qaTu, u,qaTv, v, qaNu, u, qaNv, v),
                        ImmutableMap.of(attu, ImmutableSet.of(Q.getID()), attv, ImmutableSet.of(Q.getID()))));


        Variable w = TERM_FACTORY.getVariable("u");
        Variable z = TERM_FACTORY.getVariable("v");

        DataAtom<RelationPredicate> f3 = ATOM_FACTORY.getDataAtom(Q.getAtomPredicate(), ImmutableList.of(w, z));

        RelationID table3 = MDFAC.createRelationID(null, "R");
        QuotedID attW = MDFAC.createAttributeID("A");
        QuotedID attZ = MDFAC.createAttributeID("B");


        // This is used to simulate an ambiguity during the operation of natural join
        re3 = new RAExpression(
                ImmutableList.of(f3),
                ImmutableList.of(),
                RAExpressionAttributes.create(ImmutableMap.of(attW, w, attZ, z), ImmutableSet.of(table3)));

        eq = TERM_FACTORY.getNotYetTypedEquality(x, u);

        onExpression = new EqualsTo();
        onExpression.setLeftExpression(new Column(new Table("P"), "A"));
        onExpression.setRightExpression(new Column(new Table("Q"), "A"));

        // this relation contains just a common attribute with the RAExpression "re1"
        // and it is used to simulate an exception during the operations of:
        // "cross join" and "join on" and "natural join"
        re1_1 = new RAExpression(ImmutableList.of(f2),
                ImmutableList.of(),
                RAExpressionAttributes.create(ImmutableMap.of(attX, x), P.getAllIDs()));

        System.out.println("****************************************************");
    }

    @Test
    public void cross_join_test() throws IllegalJoinException {
        System.out.println(re1);
        System.out.println(re2);

        RAExpression relationalExpression = RAExpression.crossJoin(re1, re2);
        System.out.println(relationalExpression);

        crossJoinAndJoinOnCommonAsserts(relationalExpression);
        assertTrue(relationalExpression.getFilterAtoms().isEmpty());
    }

    @Test(expected = IllegalJoinException.class)
    public void cross_join_exception_test() throws IllegalJoinException {
        System.out.println(re1);
        System.out.println(re1_1);

        RAExpression.crossJoin(re1, re1_1);
    }

    @Test
    public void join_on_test() throws IllegalJoinException {
        System.out.println(re1);
        System.out.println(re2);
        System.out.println(eq);

        RAExpression relationalExpression = RAExpression.joinOn(re1, re2,
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

        RAExpression.joinOn(re1, re1_1,
                attributes -> new ExpressionParser(MDFAC, CORE_SINGLETONS)
                        .parseBooleanExpression(onExpression, attributes));
    }

    @Test
    public void natural_join_test() throws IllegalJoinException {
        System.out.println(re1);
        System.out.println(re2);
        System.out.println(eq);

        RAExpression relationalExpression = RAExpression.naturalJoin(re1, re2, TERM_FACTORY);
        System.out.println(relationalExpression);

        naturalUsingCommonAsserts(relationalExpression);
    }

    @Test(expected = IllegalJoinException.class)
    public void natural_join_exception_test() throws IllegalJoinException {
        System.out.println(re1);
        System.out.println(re1_1);

        RAExpression relationalExpression = RAExpression.naturalJoin(re1, re1_1, TERM_FACTORY);
        System.out.println(relationalExpression);
    }

    @Test(expected = IllegalJoinException.class)
    public void natural_join_ambiguity_test() throws IllegalJoinException {
        System.out.println(re1);
        System.out.println(re2);

        RAExpression relationalExpression = RAExpression.joinOn(re1, re2,
                attributes -> new ExpressionParser(MDFAC, CORE_SINGLETONS)
                        .parseBooleanExpression(onExpression, attributes));

        System.out.println(relationalExpression);
        System.out.println(re3);

        RAExpression.naturalJoin(relationalExpression, re3, TERM_FACTORY);
    }

    @Test()
    public void join_using_test() throws IllegalJoinException {
        System.out.println(re1);
        System.out.println(re2);
        System.out.println(eq);

        RAExpression relationalExpression =
                RAExpression.joinUsing(re1, re2, ImmutableSet.of(MDFAC.createAttributeID("A")), TERM_FACTORY);

        System.out.println(relationalExpression);

        naturalUsingCommonAsserts(relationalExpression);
    }


    @Test(expected = IllegalJoinException.class)
    public void join_using_exception_test() throws IllegalJoinException {
        System.out.println(re1);
        System.out.println(re1_1);

        RAExpression relationalExpression = RAExpression.joinUsing(re1, re1_1,
                ImmutableSet.of(MDFAC.createAttributeID("A")), TERM_FACTORY);
        System.out.println(relationalExpression);
    }

    @Test(expected = IllegalJoinException.class)
    public void join_using_no_commons_test() throws IllegalJoinException {

        // a new relationId without any common attribute with the re1 is created to simulate an exception
        RAExpression re2 =  new RAExpression(ImmutableList.of(f2),
                ImmutableList.of(),
                RAExpressionAttributes.create(
                        ImmutableMap.of(MDFAC.createAttributeID("C"), u,  MDFAC.createAttributeID("D"), v),
                        ImmutableSet.of(MDFAC.createRelationID(null, "Q"))));

        System.out.println(re1);
        System.out.println(re2);

        RAExpression.joinUsing(re1, re2, ImmutableSet.of(MDFAC.createAttributeID("A")), TERM_FACTORY);
    }

    @Test(expected = IllegalJoinException.class)
    public void join_using_ambiguity_test() throws IllegalJoinException {
        System.out.println(re1);
        System.out.println(re2);

        RAExpression relationalExpression = RAExpression.joinOn(re1, re2,
                attributes -> new ExpressionParser(MDFAC, CORE_SINGLETONS)
                        .parseBooleanExpression(onExpression, attributes));

        System.out.println(relationalExpression);
        System.out.println(re3);

        RAExpression.joinUsing(relationalExpression, re3, ImmutableSet.of(MDFAC.createAttributeID("A")), TERM_FACTORY);
    }


    @Test
    public void alias_test() {
        RelationID tableAlias = MDFAC.createRelationID(null, "S");
        QualifiedAttributeID qaAx = new QualifiedAttributeID(tableAlias, attX);
        QualifiedAttributeID qaAy = new QualifiedAttributeID(tableAlias, attY);

        System.out.println(re1);
        RAExpression actual =  RAExpression.alias(re1, tableAlias);
        System.out.println(actual);

        assertTrue(actual.getDataAtoms().contains(f1));

        ImmutableMap<QualifiedAttributeID, ImmutableTerm> attrs = actual.getAttributes();
        assertEquals(x, attrs.get(qaNx));
        assertEquals(y, attrs.get(qaNy));
        assertEquals(x, attrs.get(qaAx));
        assertEquals(y, attrs.get(qaAy));
    }

    @Test
    public void  create_test(){
        RAExpression actual = new RAExpression(re1.getDataAtoms(),
                re1.getFilterAtoms(),
                RAExpressionAttributes.create(ImmutableMap.of(attX, x, attY, y), P.getAllIDs()));
        System.out.println(actual);

        ImmutableMap<QualifiedAttributeID, ImmutableTerm> attrs = actual.getAttributes();
        assertEquals(x, attrs.get(qaNx));
        assertEquals(y, attrs.get(qaNy));
        assertEquals(x, attrs.get(qaTx));
        assertEquals(y, attrs.get(qaTy));

    }


    private void naturalUsingCommonAsserts(RAExpression relationalExpression){
        assertTrue(relationalExpression.getDataAtoms().contains(f1));
        assertTrue(relationalExpression.getDataAtoms().contains(f2));
        assertTrue(relationalExpression.getFilterAtoms().contains(eq));

        ImmutableMap<QualifiedAttributeID, ImmutableTerm> attrs = relationalExpression.getAttributes();
        assertEquals(x, attrs.get(qaNx));
        assertNull(attrs.get(qaTx));
        assertEquals(y, attrs.get(qaTy));
        assertEquals(y, attrs.get(qaNy));
        assertNull(attrs.get(qaTu));
        assertEquals(v, attrs.get(qaTv));
        assertEquals(v, attrs.get(qaNv));

    }

    private void crossJoinAndJoinOnCommonAsserts(RAExpression relationalExpression ){
        assertTrue(relationalExpression.getDataAtoms().contains(f1));
        assertTrue(relationalExpression.getDataAtoms().contains(f2));

        ImmutableMap<QualifiedAttributeID, ImmutableTerm> attrs = relationalExpression.getAttributes();
        assertEquals(x, attrs.get(qaTx));
        assertNull(attrs.get(qaNx));
        assertEquals(y, attrs.get(qaTy));
        assertEquals(y, attrs.get(qaNy));
        assertEquals(u, attrs.get(qaTu));
        assertNull(attrs.get(qaNu));
        assertEquals(v, attrs.get(qaTv));
        assertEquals(v, attrs.get(qaNv));

    }
}
