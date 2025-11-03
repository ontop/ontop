package it.unibz.inf.ontop.spec.sqlparser;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.OfflineMetadataProviderBuilder;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.spec.sqlparser.exception.IllegalJoinException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static it.unibz.inf.ontop.spec.sqlparser.SQLTestingTools.*;
import static org.junit.jupiter.api.Assertions.*;

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

        assertEquals(IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(re1.getIQTree(), re2.getIQTree())), relationalExpression.getIQTree());

        assertEquals(RAExpressionAttributes.of(ImmutableMap.of(
                        TABLE_P.getAttribute(1).getID(), RAExpressionAttributes.Occurrences.of(TABLE_P.getAllIDs(), x, TABLE_T.getAllIDs(), u),
                        TABLE_P.getAttribute(2).getID(), RAExpressionAttributes.Occurrences.of(TABLE_P.getAllIDs(), y),
                        TABLE_T.getAttribute(2).getID(), RAExpressionAttributes.Occurrences.of(TABLE_T.getAllIDs(), v))),
                relationalExpression.getAttributes());
    }

    @Test
    public void cross_join_exception_test() throws IllegalJoinException {
        RAExpression re1_1 = ops.withAlias(re2, idFactory.createRelationID("P"));

        var ex = assertThrows(IllegalJoinException.class, () -> ops.crossJoin(re1, re1_1));
        assertEquals("Relation alias P occurs in both arguments of the JOIN attributes: {A={[P]=x}, B={[P]=y}} and attributes: {A={[P]=u}, C={[P]=v}}", ex.getMessage());
    }

    @Test
    public void join_on_test() throws IllegalJoinException {
        ImmutableExpression eq = TERM_FACTORY.getNotYetTypedEquality(x, u);

        RAExpression relationalExpression = ops.joinOn(re1, re2, a -> Optional.of(eq));

        assertEquals(IQ_FACTORY.createUnaryIQTree(IQ_FACTORY.createFilterNode(eq),
                IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(),
                        ImmutableList.of(re1.getIQTree(), re2.getIQTree()))), relationalExpression.getIQTree());

        assertEquals(RAExpressionAttributes.of(ImmutableMap.of(
                        TABLE_P.getAttribute(1).getID(), RAExpressionAttributes.Occurrences.of(TABLE_P.getAllIDs(), x, TABLE_T.getAllIDs(), u),
                        TABLE_P.getAttribute(2).getID(), RAExpressionAttributes.Occurrences.of(TABLE_P.getAllIDs(), y),
                        TABLE_T.getAttribute(2).getID(), RAExpressionAttributes.Occurrences.of(TABLE_T.getAllIDs(), v))),
                relationalExpression.getAttributes());
    }

    @Test
    public void join_on_exception_test() throws IllegalJoinException {
        RAExpression re1_1 = ops.withAlias(re2, idFactory.createRelationID("P"));

        var ex = assertThrows(IllegalJoinException.class, () -> ops.joinOn(re1, re1_1,
                a -> Optional.of(TERM_FACTORY.getNotYetTypedEquality(x, u))));

        assertEquals("Relation alias P occurs in both arguments of the JOIN attributes: {A={[P]=x}, B={[P]=y}} and attributes: {A={[P]=u}, C={[P]=v}}", ex.getMessage());
    }

    @Test
    public void natural_join_test() throws IllegalJoinException {
        ImmutableFunctionalTerm eq = TERM_FACTORY.getNotYetTypedEquality(x, u);

        RAExpression relationalExpression = ops.naturalJoin(re1, re2);

        assertEquals(IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createFilterNode(TERM_FACTORY.getNotYetTypedEquality(x, u)),
                IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(),
                        ImmutableList.of(re1.getIQTree(), re2.getIQTree()))), relationalExpression.getIQTree());

        assertEquals(RAExpressionAttributes.of(ImmutableMap.of(
                        TABLE_P.getAttribute(1).getID(), RAExpressionAttributes.Occurrences.of(Sets.union(TABLE_P.getAllIDs(), TABLE_T.getAllIDs()).immutableCopy(), x),
                        TABLE_P.getAttribute(2).getID(), RAExpressionAttributes.Occurrences.of(TABLE_P.getAllIDs(), y),
                        TABLE_T.getAttribute(2).getID(), RAExpressionAttributes.Occurrences.of(TABLE_T.getAllIDs(), v))),
                relationalExpression.getAttributes());
    }

    @Test
    public void natural_join_exception_test() throws IllegalJoinException {
        RAExpression re1_1 = ops.withAlias(re2, idFactory.createRelationID("P"));

        var ex = assertThrows(IllegalJoinException.class, () -> ops.naturalJoin(re1, re1_1));
        assertEquals("Relation alias P occurs in both arguments of the JOIN attributes: {A={[P]=x}, B={[P]=y}} and attributes: {A={[P]=u}, C={[P]=v}}", ex.getMessage());
    }

    @Test
    public void natural_join_ambiguity_test() throws IllegalJoinException {
        RAExpression re = ops.joinOn(re1, re2,a -> Optional.of(TERM_FACTORY.getNotYetTypedEquality(x, u)));

        // This is used to simulate an ambiguity during the operation of natural join
        RAExpression re3 = ops.create(TABLE_R, ImmutableList.of(u, v));

        var ex = assertThrows(IllegalJoinException.class, () -> ops.naturalJoin(re, re3));
        assertEquals("Attribute A is ambiguous with attributes: {A={[P]=x, [Q]=u}, B={[P]=y}, C={[Q]=v}} and attributes: {A={[R]=u}, B={[R]=v}}", ex.getMessage());
    }

    @Test
    public void join_using_test() throws IllegalJoinException {
        RAExpression relationalExpression =
                ops.joinUsing(re1, re2, ImmutableSet.of(idFactory.createAttributeID("A")));

        assertEquals(IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createFilterNode(TERM_FACTORY.getNotYetTypedEquality(x, u)),
                IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(),
                        ImmutableList.of(re1.getIQTree(), re2.getIQTree()))), relationalExpression.getIQTree());

        assertEquals(RAExpressionAttributes.of(ImmutableMap.of(
                        TABLE_P.getAttribute(1).getID(), RAExpressionAttributes.Occurrences.of(Sets.union(TABLE_P.getAllIDs(), TABLE_T.getAllIDs()).immutableCopy(), x),
                        TABLE_P.getAttribute(2).getID(), RAExpressionAttributes.Occurrences.of(TABLE_P.getAllIDs(), y),
                        TABLE_T.getAttribute(2).getID(), RAExpressionAttributes.Occurrences.of(TABLE_T.getAllIDs(), v))),
                relationalExpression.getAttributes());
    }

    @Test
    public void join_using_exception_test() throws IllegalJoinException {
        RAExpression re1_1 = ops.withAlias(re2, idFactory.createRelationID("P"));

        var ex = assertThrows(IllegalJoinException.class, () -> ops.joinUsing(re1, re1_1,
                ImmutableSet.of(idFactory.createAttributeID("A"))));

        assertEquals("Relation alias P occurs in both arguments of the JOIN attributes: {A={[P]=x}, B={[P]=y}} and attributes: {A={[P]=u}, C={[P]=v}}", ex.getMessage());
    }

    @Test
    public void join_using_no_commons_test() throws IllegalJoinException {
        RAExpression re2p = ops.create(TABLE_Q, ImmutableList.of(u, v));

        var ex = assertThrows(IllegalJoinException.class, () -> ops.joinUsing(re1, re2p, ImmutableSet.of(idFactory.createAttributeID("A"))));
        assertEquals("Attribute A cannot be found with attributes: {A={[P]=x}, B={[P]=y}} and attributes: {C={[Q]=u}, D={[Q]=v}}", ex.getMessage());
    }

    @Test
    public void join_using_ambiguity_test() throws IllegalJoinException {
        RAExpression relationalExpression = ops.joinOn(re1, re2,
                a -> Optional.of(TERM_FACTORY.getNotYetTypedEquality(x, u)));

        // This is used to simulate an ambiguity during the operation of natural join
        RAExpression re3 = ops.create(TABLE_R, ImmutableList.of(u, v));

        var ex = assertThrows(IllegalJoinException.class, () -> ops.joinUsing(relationalExpression, re3, ImmutableSet.of(idFactory.createAttributeID("A"))));
        assertEquals("Attribute A is ambiguous with attributes: {A={[P]=x, [Q]=u}, B={[P]=y}, C={[Q]=v}} and attributes: {A={[R]=u}, B={[R]=v}}", ex.getMessage());
    }


    @Test
    public void alias_test() {
        RelationID tableAlias = idFactory.createRelationID("S");

        RAExpression actual = ops.withAlias(re1, tableAlias);

        assertEquals(re1.getIQTree(), actual.getIQTree());
        assertEquals(RAExpressionAttributes.of(ImmutableMap.of(
                        TABLE_P.getAttribute(1).getID(), RAExpressionAttributes.Occurrences.of(ImmutableSet.of(tableAlias), x),
                        TABLE_P.getAttribute(2).getID(), RAExpressionAttributes.Occurrences.of(ImmutableSet.of(tableAlias), y))),
                actual.getAttributes());
    }

    @Test
    public void create_test() {
        assertEquals(RAExpressionAttributes.of(ImmutableMap.of(
                        TABLE_P.getAttribute(1).getID(), RAExpressionAttributes.Occurrences.of(TABLE_P.getAllIDs(), x),
                        TABLE_P.getAttribute(2).getID(), RAExpressionAttributes.Occurrences.of(TABLE_P.getAllIDs(), y))),
                re1.getAttributes());
    }
}
