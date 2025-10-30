package it.unibz.inf.ontop.spec.sqlparser;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.OfflineMetadataProviderBuilder;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.spec.sqlparser.exception.IllegalJoinException;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static it.unibz.inf.ontop.spec.sqlparser.SQLTestingTools.*;
import static org.junit.jupiter.api.Assertions.*;

// TODO: REFACTOR

public class RelationalExpressionTest {

    private QuotedIDFactory idFactory;
    private RAExpressionOperations ops;

    private Variable x, y, u, v;
    private NamedRelationDefinition TABLE_P, TABLE_Q, TABLE_R, TABLE_T;
    private RAExpression re1, re2;

    @BeforeEach
    public void setupTest(){
        x = TERM_FACTORY.getVariable("x");
        y = TERM_FACTORY.getVariable("y");
        u = TERM_FACTORY.getVariable("u");
        v = TERM_FACTORY.getVariable("v");

        OfflineMetadataProviderBuilder builder = createMetadataProviderBuilder();
        idFactory = builder.getQuotedIDFactory();

        ops = new RAExpressionOperations(TERM_FACTORY, IQ_FACTORY);

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

        TABLE_T = builder.createDatabaseRelation("Q",
            "A", integerDBType, true,
            "C", integerDBType, true);

        re1 = ops.create(TABLE_P, ImmutableList.of(x, y));
        re2 = ops.create(TABLE_T, ImmutableList.of(u, v));
    }

    @Test
    public void cross_join_test() throws IllegalJoinException {
        RAExpression relationalExpression = ops.crossJoin(re1, re2);

        crossJoinAndJoinOnCommonAsserts(relationalExpression);
        assertTrue(relationalExpression.getFilterAtoms().isEmpty());
    }

    @Test
    public void cross_join_exception_test() throws IllegalJoinException {
        RAExpression re1_1 = ops.withAlias(re2, idFactory.createRelationID("P"));

        var ex = assertThrows(IllegalJoinException.class, () -> ops.crossJoin(re1, re1_1));
        assertEquals("Relation alias P occurs in both arguments of the JOIN", ex.getMessage());
    }

    @Test
    public void join_on_test() throws IllegalJoinException {
        ImmutableFunctionalTerm eq = TERM_FACTORY.getNotYetTypedEquality(x, u);

        EqualsTo onExpression = new EqualsTo();
        onExpression.setLeftExpression(new Column(new Table(TABLE_P.getID().getSQLRendering()), "A"));
        onExpression.setRightExpression(new Column(new Table(TABLE_Q.getID().getSQLRendering()), "A"));

        RAExpression relationalExpression = ops.joinOn(re1, re2,
                attributes -> ImmutableList.of(new ExpressionParser(idFactory, CORE_SINGLETONS)
                        .parseBooleanExpression(onExpression,  attributes)));

        crossJoinAndJoinOnCommonAsserts(relationalExpression);
        assertEquals(ImmutableList.of(eq), relationalExpression.getFilterAtoms());
    }

    @Test
    public void join_on_exception_test() throws IllegalJoinException {
        RAExpression re1_1 = ops.withAlias(re2, idFactory.createRelationID("P"));

        EqualsTo onExpression = new EqualsTo();
        onExpression.setLeftExpression(new Column(new Table(TABLE_P.getID().getSQLRendering()), "A"));
        onExpression.setRightExpression(new Column(new Table(TABLE_Q.getID().getSQLRendering()), "A"));

        var ex = assertThrows(IllegalJoinException.class, () -> ops.joinOn(re1, re1_1,
                attributes -> ImmutableList.of(new ExpressionParser(idFactory, CORE_SINGLETONS)
                        .parseBooleanExpression(onExpression, attributes))));

        assertEquals("Relation alias P occurs in both arguments of the JOIN", ex.getMessage());
    }

    @Test
    public void natural_join_test() throws IllegalJoinException {
        ImmutableFunctionalTerm eq = TERM_FACTORY.getNotYetTypedEquality(x, u);

        RAExpression relationalExpression = ops.naturalJoin(re1, re2);

        naturalUsingCommonAsserts(relationalExpression);
    }

    @Test
    public void natural_join_exception_test() throws IllegalJoinException {
        RAExpression re1_1 = ops.withAlias(re2, idFactory.createRelationID("P"));

        var ex = assertThrows(IllegalJoinException.class, () -> ops.naturalJoin(re1, re1_1));
        assertEquals("Relation alias P occurs in both arguments of the JOIN", ex.getMessage());
    }

    @Test
    public void natural_join_ambiguity_test() throws IllegalJoinException {
        EqualsTo onExpression = new EqualsTo();
        onExpression.setLeftExpression(new Column(new Table(TABLE_P.getID().getSQLRendering()), "A"));
        onExpression.setRightExpression(new Column(new Table(TABLE_Q.getID().getSQLRendering()), "A"));

        RAExpression re = ops.joinOn(re1, re2,
                attributes -> ImmutableList.of(new ExpressionParser(idFactory, CORE_SINGLETONS)
                        .parseBooleanExpression(onExpression, attributes)));

        // This is used to simulate an ambiguity during the operation of natural join
        RAExpression re3 = ops.create(TABLE_R, ImmutableList.of(u, v));

        var ex = assertThrows(IllegalJoinException.class, () -> ops.naturalJoin(re, re3));
        assertEquals("Attribute(s) [A] are ambiguous", ex.getMessage());
    }

    @Test
    public void join_using_test() throws IllegalJoinException {
        RAExpression relationalExpression =
                ops.joinUsing(re1, re2, ImmutableSet.of(idFactory.createAttributeID("A")));

        naturalUsingCommonAsserts(relationalExpression);
    }


    @Test
    public void join_using_exception_test() throws IllegalJoinException {
        RAExpression re1_1 = ops.withAlias(re2, idFactory.createRelationID("P"));

        var ex = assertThrows(IllegalJoinException.class, () -> ops.joinUsing(re1, re1_1,
                ImmutableSet.of(idFactory.createAttributeID("A"))));

        assertEquals("Relation alias P occurs in both arguments of the JOIN", ex.getMessage());
    }

    @Test
    public void join_using_no_commons_test() throws IllegalJoinException {
        RAExpression re2p = ops.create(TABLE_Q, ImmutableList.of(u, v));

        var ex = assertThrows(IllegalJoinException.class, () -> ops.joinUsing(re1, re2p, ImmutableSet.of(idFactory.createAttributeID("A"))));
        assertEquals("Attribute(s) [A] cannot be found", ex.getMessage());
    }

    @Test
    public void join_using_ambiguity_test() throws IllegalJoinException {
        EqualsTo onExpression = new EqualsTo();
        onExpression.setLeftExpression(new Column(new Table(TABLE_P.getID().getSQLRendering()), "A"));
        onExpression.setRightExpression(new Column(new Table(TABLE_Q.getID().getSQLRendering()), "A"));

        RAExpression relationalExpression = ops.joinOn(re1, re2,
                attributes -> ImmutableList.of(new ExpressionParser(idFactory, CORE_SINGLETONS)
                        .parseBooleanExpression(onExpression, attributes)));

        // This is used to simulate an ambiguity during the operation of natural join
        RAExpression re3 = ops.create(TABLE_R, ImmutableList.of(u, v));

        var ex = assertThrows(IllegalJoinException.class, () -> ops.joinUsing(relationalExpression, re3, ImmutableSet.of(idFactory.createAttributeID("A"))));
        assertEquals("Attribute(s) [A] are ambiguous", ex.getMessage());
    }


    @Test
    public void alias_test() {
        RelationID tableAlias = idFactory.createRelationID("S");

        RAExpression actual =  ops.withAlias(re1, tableAlias);

        ExtensionalDataNode f1 = re1.getDataAtoms().get(0);
        assertTrue(actual.getDataAtoms().contains(f1));

        assertEquals(ImmutableMap.of(
                unqualified(TABLE_P,1), x,
                unqualified(TABLE_P,2), y,
                new QualifiedAttributeID(tableAlias, TABLE_P.getAttribute(1).getID()), x,
                new QualifiedAttributeID(tableAlias, TABLE_P.getAttribute(2).getID()), y),
                actual.getAttributes().asMap());
    }

    @Test
    public void create_test() {
        assertEquals(ImmutableMap.of(
                unqualified(TABLE_P,1), x,
                unqualified(TABLE_P,2), y,
                qualified(TABLE_P,1), x,
                qualified(TABLE_P,2), y),
                re1.getAttributes().asMap());
    }


    private void naturalUsingCommonAsserts(RAExpression relationalExpression) {
        ExtensionalDataNode f1 = re1.getDataAtoms().get(0);
        ExtensionalDataNode f2 = re2.getDataAtoms().get(0);

        assertTrue(relationalExpression.getDataAtoms().contains(f1));
        assertTrue(relationalExpression.getDataAtoms().contains(f2));
        assertEquals(ImmutableList.of(TERM_FACTORY.getNotYetTypedEquality(x, u)), relationalExpression.getFilterAtoms());

        assertEquals(ImmutableMap.of(
                unqualified(TABLE_P,1), x,
                qualified(TABLE_P,2), y,
                unqualified(TABLE_P,2), y,
                qualified(TABLE_T,2), v,
                unqualified(TABLE_T,2), v),
                relationalExpression.getAttributes().asMap());
    }

    private void crossJoinAndJoinOnCommonAsserts(RAExpression relationalExpression) {
        ExtensionalDataNode f1 = re1.getDataAtoms().get(0);
        assertTrue(relationalExpression.getDataAtoms().contains(f1));

        ExtensionalDataNode f2 = re2.getDataAtoms().get(0);
        assertTrue(relationalExpression.getDataAtoms().contains(f2));

        assertEquals(ImmutableMap.builder()
                .put(qualified(TABLE_P,1), x)
                .put(qualified(TABLE_P,2), y)
                .put(unqualified(TABLE_P,2), y)
                .put(qualified(TABLE_T,1), u)
                .put(qualified(TABLE_T,2), v)
                .put(unqualified(TABLE_T,2), v).build(),
                relationalExpression.getAttributes().asMap());
    }

    private static QualifiedAttributeID qualified(NamedRelationDefinition table, int index) {
        return new QualifiedAttributeID(table.getID(), table.getAttribute(index).getID());
    }
    private static QualifiedAttributeID unqualified(NamedRelationDefinition table, int index) {
        return new QualifiedAttributeID(null, table.getAttribute(index).getID());
    }
}
