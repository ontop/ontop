package it.unibz.inf.ontop.iq.executor;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.OfflineMetadataProviderBuilder;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.type.DBTermType;
import org.junit.Ignore;
import org.junit.Test;

import static it.unibz.inf.ontop.OptimizationTestingTools.*;
import static it.unibz.inf.ontop.iq.node.BinaryOrderedOperatorNode.ArgumentPosition.LEFT;
import static it.unibz.inf.ontop.iq.node.BinaryOrderedOperatorNode.ArgumentPosition.RIGHT;
import static junit.framework.TestCase.assertTrue;

/**
 * Elimination of redundant self-joins using a non unique functional constraint
 */
public class FunctionalDependencyTest {

    private final static DatabaseRelationDefinition TABLE1;
    private final static DatabaseRelationDefinition TABLE2;
    private final static DatabaseRelationDefinition TABLE3;
    private final static DatabaseRelationDefinition TABLE4;

    private final static AtomPredicate ANS1_PREDICATE_AR_1 = ATOM_FACTORY.getRDFAnswerPredicate(1);
    private final static AtomPredicate ANS1_PREDICATE_AR_2 = ATOM_FACTORY.getRDFAnswerPredicate(2);
    private final static AtomPredicate ANS1_PREDICATE_AR_3 = ATOM_FACTORY.getRDFAnswerPredicate(3);
    private final static Variable A = TERM_FACTORY.getVariable("a");
    private final static Variable B = TERM_FACTORY.getVariable("b");
    private final static Variable C = TERM_FACTORY.getVariable("c");
    private final static Variable D = TERM_FACTORY.getVariable("d");
    private final static Variable E = TERM_FACTORY.getVariable("e");
    private final static Variable F = TERM_FACTORY.getVariable("f");
    private final static Variable G = TERM_FACTORY.getVariable("g");
    private final static Variable H = TERM_FACTORY.getVariable("h");
    private final static Variable I = TERM_FACTORY.getVariable("i");
    private final static Variable J = TERM_FACTORY.getVariable("j");
    private final static Variable K = TERM_FACTORY.getVariable("k");
    private final static Variable L = TERM_FACTORY.getVariable("l");
    private final static Variable M = TERM_FACTORY.getVariable("m");
    private final static Variable N = TERM_FACTORY.getVariable("n");
    private final static Variable O = TERM_FACTORY.getVariable("o");
    private final static Variable P = TERM_FACTORY.getVariable("p");
    private final static Variable Q = TERM_FACTORY.getVariable("q");
    private final static Variable R = TERM_FACTORY.getVariable("r");
    private final static Variable S = TERM_FACTORY.getVariable("s");
    private final static Variable T = TERM_FACTORY.getVariable("t");
    private final static Variable U = TERM_FACTORY.getVariable("u");
    private final static Variable V = TERM_FACTORY.getVariable("v");
    private final static Variable W = TERM_FACTORY.getVariable("w");
    private final static Variable X = TERM_FACTORY.getVariable("x");
    private final static Variable Y = TERM_FACTORY.getVariable("y");
    private final static Variable Z = TERM_FACTORY.getVariable("z");
    private final static DBConstant ONE = TERM_FACTORY.getDBConstant("1", TYPE_FACTORY.getDBTypeFactory().getDBLargeIntegerType());
    private final static DBConstant TWO = TERM_FACTORY.getDBConstant("2", TYPE_FACTORY.getDBTypeFactory().getDBLargeIntegerType());
    private final static DBConstant THREE = TERM_FACTORY.getDBConstant("3", TYPE_FACTORY.getDBTypeFactory().getDBLargeIntegerType());

    static{
        OfflineMetadataProviderBuilder builder = createMetadataProviderBuilder();
        DBTermType integerDBType = builder.getDBTypeFactory().getDBLargeIntegerType();

        /*
         * Table 1: PK + non-unique functional constraint + 2 dependent fields + 1 independent
         */
        TABLE1 = builder.createDatabaseRelation("table1",
            "col1", integerDBType, false,
            "col2", integerDBType, false,
            "col3", integerDBType, false,
            "col4", integerDBType, false,
            "col5", integerDBType, false);
        UniqueConstraint.primaryKeyOf(TABLE1.getAttribute(1));
        FunctionalDependency.defaultBuilder(TABLE1)
                .addDeterminant(2)
                .addDependent(3)
                .addDependent(4)
                .build();

        /*
         * Table 2: non-composite unique constraint and regular field
         */
        TABLE2 = builder.createDatabaseRelation("table2",
            "col1", integerDBType, false,
            "col2", integerDBType, false,
            "col3", integerDBType, false);
        UniqueConstraint.primaryKeyOf(TABLE2.getAttribute(2));

        /*
         * Table 3: PK + 2 independent non-unique functional constraints + 1 independent
         */
        TABLE3 = builder.createDatabaseRelation("table3",
            "col1", integerDBType, false,
            "col2", integerDBType, false,
            "col3", integerDBType, false,
            "col4", integerDBType, false,
            "col5", integerDBType, false,
            "col6", integerDBType, false);
        UniqueConstraint.primaryKeyOf(TABLE3.getAttribute(1));
        FunctionalDependency.defaultBuilder(TABLE3)
                .addDeterminant(2)
                .addDependent(3)
                .build();
        FunctionalDependency.defaultBuilder(TABLE3)
                .addDeterminant(4)
                .addDependent(5)
                .build();

        /*
         * Table 4: PK + 2 non-unique functional constraints (one is nested) + 1 independent attribute
         */
        TABLE4 = builder.createDatabaseRelation("table4",
            "col1", integerDBType, false,
            "col2", integerDBType, false,
            "col3", integerDBType, false,
            "col4", integerDBType, false,
            "col5", integerDBType, false);
        UniqueConstraint.primaryKeyOf(TABLE4.getAttribute(1));
        FunctionalDependency.defaultBuilder(TABLE4)
                .addDeterminant(3)
                .addDependent(4)
                .build();
        FunctionalDependency.defaultBuilder(TABLE4)
                .addDeterminant(2)
                .addDependent(3)
                .addDependent(4)
                .build();
    }

    @Test
    public void testRedundantSelfJoin1()  {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_AR_2, X, Y);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        queryBuilder.init(projectionAtom, distinctNode);

        ConstructionNode topConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.addChild(distinctNode, topConstructionNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(topConstructionNode, joinNode);

        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1, ImmutableList.of(X, A, B, C, D));
        queryBuilder.addChild(joinNode, dataNode1);

        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE1, ImmutableList.of(E, A, Y, F, G));
        queryBuilder.addChild(joinNode, dataNode2);

        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        expectedQueryBuilder.init(projectionAtom, topConstructionNode);

        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE1, ImmutableList.of(X, A, Y, C, D));

        expectedQueryBuilder.addChild(topConstructionNode, dataNode3);

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();

        optimizeAndCompare(query, expectedQuery);
    }

    @Test
    public void testRedundantSelfJoin2()  {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_AR_3,
                X, Y, Z);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        queryBuilder.init(projectionAtom, distinctNode);

        ConstructionNode topConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.addChild(distinctNode, topConstructionNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(topConstructionNode, joinNode);

        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1, ImmutableList.of(X, A, B, Z, C));
        queryBuilder.addChild(joinNode, dataNode1);

        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE1, ImmutableList.of(E, A, Y, F, G));
        queryBuilder.addChild(joinNode, dataNode2);

        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        expectedQueryBuilder.init(projectionAtom, topConstructionNode);

        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE1, ImmutableList.of(X, A, Y, Z, C));

        expectedQueryBuilder.addChild(topConstructionNode, dataNode3);

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();

        optimizeAndCompare(query, expectedQuery);
    }

    @Ignore("TODO: re-enable it after re-allowing binding lift above distincts")
    @Test
    public void testRedundantSelfJoin3() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_AR_3,
                X, Y, Z);
        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        queryBuilder.init(projectionAtom, distinctNode);

        ConstructionNode topConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.addChild(distinctNode, topConstructionNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(topConstructionNode, joinNode);

        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1, ImmutableList.of(D, A, Z, B, C));
        queryBuilder.addChild(joinNode, dataNode1);

        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE1, ImmutableList.of(X, A, Y, F, G));
        queryBuilder.addChild(joinNode, dataNode2);

        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        ConstructionNode newRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(Z, Y));
        expectedQueryBuilder.init(projectionAtom, newRootNode);
        expectedQueryBuilder.addChild(newRootNode, distinctNode);

        ConstructionNode otherConstructionNode = IQ_FACTORY.createConstructionNode(newRootNode.getChildVariables());
        expectedQueryBuilder.addChild(distinctNode, otherConstructionNode);

        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE1, ImmutableList.of(X, A, Y, B, G));
        expectedQueryBuilder.addChild(otherConstructionNode, dataNode3);

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();

        optimizeAndCompare(query, expectedQuery);
    }

    @Test
    public void testRedundantSelfJoin4() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_AR_2, X, Y);
        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        queryBuilder.init(projectionAtom, distinctNode);

        ConstructionNode topConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.addChild(distinctNode, topConstructionNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(topConstructionNode, joinNode);

        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1, ImmutableList.of(X, A, B, C, D));
        queryBuilder.addChild(joinNode, dataNode1);

        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE1, ImmutableList.of(E, A, Y, X, F));
        queryBuilder.addChild(joinNode, dataNode2);

        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        expectedQueryBuilder.init(projectionAtom, topConstructionNode);

        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE1, ImmutableList.of(X, A, Y, X, D));

        expectedQueryBuilder.addChild(topConstructionNode, dataNode3);

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();

        optimizeAndCompare(query, expectedQuery);
    }

    @Test
    public void testRedundantSelfJoin5() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_AR_3, X, Y, Z);
        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        queryBuilder.init(projectionAtom, distinctNode);

        ConstructionNode topConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.addChild(distinctNode, topConstructionNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(topConstructionNode, joinNode);

        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1, ImmutableList.of(X, A, B, C, Y));
        queryBuilder.addChild(joinNode, dataNode1);

        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE1, ImmutableList.of(D, A, E, Z, G));
        queryBuilder.addChild(joinNode, dataNode2);

        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        expectedQueryBuilder.init(projectionAtom, topConstructionNode);

        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE1, ImmutableList.of(X, A, B, Z, Y));

        expectedQueryBuilder.addChild(topConstructionNode, dataNode3);

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();

        optimizeAndCompare(query, expectedQuery);
    }

    @Test
    public void testRedundantSelfJoin6() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_AR_3, X, Y, Z);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        queryBuilder.init(projectionAtom, distinctNode);

        ConstructionNode topConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.addChild(distinctNode, topConstructionNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(topConstructionNode, joinNode);

        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1, ImmutableList.of(X, A, B, C, Y));
        queryBuilder.addChild(joinNode, dataNode1);

        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE1, ImmutableList.of(D, A, E, Z, G));
        queryBuilder.addChild(joinNode, dataNode2);

        ExtensionalDataNode dataNode3= createExtensionalDataNode(TABLE1, ImmutableList.of(H, A, I, J, K));
        queryBuilder.addChild(joinNode, dataNode3);

        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        expectedQueryBuilder.init(projectionAtom, topConstructionNode);

        ExtensionalDataNode dataNode4 = createExtensionalDataNode(TABLE1, ImmutableList.of(X, A, B, Z, Y));

        expectedQueryBuilder.addChild(topConstructionNode, dataNode4);

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();

        optimizeAndCompare(query, expectedQuery);
    }

    @Test
    public void testRedundantSelfJoin7() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_AR_3, X, Y, Z);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        queryBuilder.init(projectionAtom, distinctNode);

        ConstructionNode topConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.addChild(distinctNode, topConstructionNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(topConstructionNode, joinNode);

        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1, ImmutableList.of(X, A, B, C, Y));
        queryBuilder.addChild(joinNode, dataNode1);

        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE1, ImmutableList.of(D, A, E, Z, G));
        queryBuilder.addChild(joinNode, dataNode2);

        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE1, ImmutableList.of(H, F, I, J, K));
        queryBuilder.addChild(joinNode, dataNode3);

        ExtensionalDataNode dataNode4 = createExtensionalDataNode(TABLE1, ImmutableList.of(L, F, M, N, O));
        queryBuilder.addChild(joinNode, dataNode4);

        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        expectedQueryBuilder.init(projectionAtom, distinctNode);
        expectedQueryBuilder.addChild(distinctNode, topConstructionNode);

        expectedQueryBuilder.addChild(topConstructionNode, joinNode);

        ExtensionalDataNode dataNode5 = createExtensionalDataNode(TABLE1, ImmutableList.of(X, A, B, Z, Y));
        expectedQueryBuilder.addChild(joinNode, dataNode5);

        ExtensionalDataNode dataNode6 = createExtensionalDataNode(TABLE1, ImmutableList.of(L, F, I, J, O));
        expectedQueryBuilder.addChild(joinNode, dataNode6);

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();

        optimizeAndCompare(query, expectedQuery);
    }

    @Test
    public void testRedundantSelfJoin7_1()  {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_AR_3, X, Y, Z);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        queryBuilder.init(projectionAtom, distinctNode);

        ConstructionNode topConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.addChild(distinctNode, topConstructionNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(topConstructionNode, joinNode);

        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1, ImmutableList.of(X, A, B, C, Y));
        queryBuilder.addChild(joinNode, dataNode1);

        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE1, ImmutableList.of(D, A, E, Z, G));
        queryBuilder.addChild(joinNode, dataNode2);

        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE1, ImmutableList.of(H, F, I, J, K));
        queryBuilder.addChild(joinNode, dataNode3);

        ExtensionalDataNode dataNode4 = createExtensionalDataNode(TABLE1, ImmutableList.of(L, F, M, N, Z));
        queryBuilder.addChild(joinNode, dataNode4);

        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        expectedQueryBuilder.init(projectionAtom, distinctNode);
        expectedQueryBuilder.addChild(distinctNode, topConstructionNode);

        expectedQueryBuilder.addChild(topConstructionNode, joinNode);

        ExtensionalDataNode dataNode5 = createExtensionalDataNode(TABLE1, ImmutableList.of(X, A, B, Z, Y));
        expectedQueryBuilder.addChild(joinNode, dataNode5);

        ExtensionalDataNode dataNode6 = createExtensionalDataNode(TABLE1, ImmutableList.of(L, F, I, J, Z));
        expectedQueryBuilder.addChild(joinNode, dataNode6);

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();

        optimizeAndCompare(query, expectedQuery);
    }

    @Test
    public void testRedundantSelfJoin7_2() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_AR_3, X, Y, Z);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        queryBuilder.init(projectionAtom, distinctNode);

        ConstructionNode topConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.addChild(distinctNode, topConstructionNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(topConstructionNode, joinNode);

        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1, ImmutableList.of(X, A, B, C, Y));
        queryBuilder.addChild(joinNode, dataNode1);

        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE1, ImmutableList.of(D, A, E, Z, G));
        queryBuilder.addChild(joinNode, dataNode2);

        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE1, ImmutableList.of(H, F, I, J, K));
        queryBuilder.addChild(joinNode, dataNode3);

        ExtensionalDataNode dataNode4 = createExtensionalDataNode(TABLE1, ImmutableList.of(L, F, M, N, G));
        queryBuilder.addChild(joinNode, dataNode4);

        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        expectedQueryBuilder.init(projectionAtom, topConstructionNode);

        ExtensionalDataNode dataNode5 = createExtensionalDataNode(TABLE1, ImmutableList.of(X, A, B, Z, Y));
        expectedQueryBuilder.addChild(topConstructionNode, dataNode5);

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();

        optimizeAndCompare(query, expectedQuery);
    }

    @Test
    public void testRedundantSelfJoin7_3() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_AR_3, X, Y, Z);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        queryBuilder.init(projectionAtom, distinctNode);

        ConstructionNode topConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.addChild(distinctNode, topConstructionNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(topConstructionNode, joinNode);

        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1, ImmutableList.of(X, A, B, C, Y));
        queryBuilder.addChild(joinNode, dataNode1);

        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE1, ImmutableList.of(D, A, E, Z, TWO));
        queryBuilder.addChild(joinNode, dataNode2);

        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE1, ImmutableList.of(H, F, I, J, K));
        queryBuilder.addChild(joinNode, dataNode3);

        ExtensionalDataNode dataNode4 = createExtensionalDataNode(TABLE1, ImmutableList.of(L, F, M, N, O));
        queryBuilder.addChild(joinNode, dataNode4);

        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        expectedQueryBuilder.init(projectionAtom, distinctNode);
        expectedQueryBuilder.addChild(distinctNode, topConstructionNode);

        expectedQueryBuilder.addChild(topConstructionNode, joinNode);

        ExtensionalDataNode dataNode5 = createExtensionalDataNode(TABLE1, ImmutableList.of(X, A, B, Z, Y));
        expectedQueryBuilder.addChild(joinNode, dataNode5);

        ExtensionalDataNode dataNode6 = createExtensionalDataNode(TABLE1, ImmutableList.of(D, A, B, Z, TWO));
        expectedQueryBuilder.addChild(joinNode, dataNode6);

        ExtensionalDataNode dataNode7 = createExtensionalDataNode(TABLE1, ImmutableList.of(L, F, I, J, O));
        expectedQueryBuilder.addChild(joinNode, dataNode7);

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();

        optimizeAndCompare(query, expectedQuery);
    }


    @Test
    public void testRedundantSelfJoin8() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_AR_1, X);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        queryBuilder.init(projectionAtom, distinctNode);

        ConstructionNode topConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.addChild(distinctNode, topConstructionNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(topConstructionNode, joinNode);

        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1, ImmutableList.of(A, X, B, C, D));
        queryBuilder.addChild(joinNode, dataNode1);

        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE1, ImmutableList.of(E, X, F, G, H));
        queryBuilder.addChild(joinNode, dataNode2);

        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        expectedQueryBuilder.init(projectionAtom, distinctNode);
        expectedQueryBuilder.addChild(distinctNode, topConstructionNode);

        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE1, ImmutableList.of(E, X, B, C, H));

        expectedQueryBuilder.addChild(topConstructionNode, dataNode3);

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();

        optimizeAndCompare(query, expectedQuery);
    }

    @Test
    public void testRedundantSelfJoin9() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_AR_2, X, Y);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        queryBuilder.init(projectionAtom, distinctNode);

        ConstructionNode topConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.addChild(distinctNode, topConstructionNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(topConstructionNode, joinNode);

        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1, ImmutableList.of(A, X, B, C, D));
        queryBuilder.addChild(joinNode, dataNode1);

        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE1, ImmutableList.of(E, X, Y, F, G));
        queryBuilder.addChild(joinNode, dataNode2);

        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        expectedQueryBuilder.init(projectionAtom, distinctNode);
        expectedQueryBuilder.addChild(distinctNode, topConstructionNode);

        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE1, ImmutableList.of(E, X, Y, C, G));

        expectedQueryBuilder.addChild(topConstructionNode, dataNode3);

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();

        optimizeAndCompare(query, expectedQuery);
    }

    /**
     * Y --> from an independent attribute
     */
    @Test
    public void testNonRedundantSelfJoin1()  {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_AR_2, X, Y);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        queryBuilder.init(projectionAtom, distinctNode);

        ConstructionNode topConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.addChild(distinctNode, topConstructionNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(topConstructionNode, joinNode);

        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1, ImmutableList.of(X, A, B, C, D));
        queryBuilder.addChild(joinNode, dataNode1);

        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE1, ImmutableList.of(E, A, F, G, Y));
        queryBuilder.addChild(joinNode, dataNode2);

        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder expectedQueryBuilder = query.newBuilder();
        expectedQueryBuilder.init(projectionAtom, distinctNode);
        expectedQueryBuilder.addChild(distinctNode, topConstructionNode);
        expectedQueryBuilder.addChild(topConstructionNode, joinNode);

        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE1, ImmutableList.of(X, A, B, C, D));
        expectedQueryBuilder.addChild(joinNode, dataNode3);

        ExtensionalDataNode dataNode4 = createExtensionalDataNode(TABLE1, ImmutableList.of(E, A, B, C, Y));
        expectedQueryBuilder.addChild(joinNode, dataNode4);

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();

        optimizeAndCompare(query, expectedQuery);
    }

    @Test
    public void testNonRedundantSelfJoin2() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_AR_2, X, Y);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        queryBuilder.init(projectionAtom, distinctNode);

        ConstructionNode topConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.addChild(distinctNode, topConstructionNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(topConstructionNode, joinNode);

        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1, ImmutableList.of(X, A, Y, C, D));
        queryBuilder.addChild(joinNode, dataNode1);

        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE1, ImmutableList.of(Y, A, Y, E, F));
        queryBuilder.addChild(joinNode, dataNode2);

        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder expectedQueryBuilder = query.newBuilder();
        expectedQueryBuilder.init(projectionAtom, topConstructionNode);
        expectedQueryBuilder.addChild(topConstructionNode, joinNode);

        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE1, ImmutableList.of(X, A, Y, C, D));
        expectedQueryBuilder.addChild(joinNode, dataNode3);

        ExtensionalDataNode dataNode4 = createExtensionalDataNode(TABLE1, ImmutableList.of(Y, A, Y, C, F));
        expectedQueryBuilder.addChild(joinNode, dataNode4);

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();

        optimizeAndCompare(query, expectedQuery);
    }

    @Test(expected = EmptyQueryException.class)
    public void testRejectedJoin1() throws EmptyQueryException {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_AR_2, X, Y);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        queryBuilder.init(projectionAtom, distinctNode);

        ConstructionNode topConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.addChild(distinctNode, topConstructionNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(topConstructionNode, joinNode);

        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1, ImmutableList.of(X, A, ONE, B, C));
        queryBuilder.addChild(joinNode, dataNode1);

        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE1, ImmutableList.of(D, A, TWO, E, Y));
        queryBuilder.addChild(joinNode, dataNode2);

        IntermediateQuery query = queryBuilder.build();

        System.out.println("\nBefore optimization: \n" +  query);

        IntermediateQuery optimizedQuery = optimize(query);
        System.err.println("\nUnexpected optimized query: \n" +  optimizedQuery);
    }

    @Test(expected = EmptyQueryException.class)
    public void testRejectedJoin2() throws EmptyQueryException {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_AR_2, X, Y);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        queryBuilder.init(projectionAtom, distinctNode);

        ConstructionNode topConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.addChild(distinctNode, topConstructionNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode(TERM_FACTORY.getStrictNEquality(B, TWO));
        queryBuilder.addChild(topConstructionNode, joinNode);

        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1, ImmutableList.of(X, A, B, C, D));
        queryBuilder.addChild(joinNode, dataNode1);

        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE1, ImmutableList.of(E, A, TWO, F, Y));
        queryBuilder.addChild(joinNode, dataNode2);

        IntermediateQuery query = queryBuilder.build();

        System.out.println("\nBefore optimization: \n" +  query);

        IntermediateQuery optimizedQuery = optimize(query);
        System.err.println("\nUnexpected optimized query: \n" +  optimizedQuery);
    }

    @Test(expected = EmptyQueryException.class)
    public void testRejectedJoin3() throws EmptyQueryException {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_AR_2, X, Y);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        queryBuilder.init(projectionAtom, distinctNode);

        ConstructionNode topConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.addChild(distinctNode, topConstructionNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode(TERM_FACTORY.getStrictNEquality(F, TWO));
        queryBuilder.addChild(topConstructionNode, joinNode);

        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1, ImmutableList.of(X, A, B, B, D));
        queryBuilder.addChild(joinNode, dataNode1);

        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE1, ImmutableList.of(E, A, TWO, F, Y));
        queryBuilder.addChild(joinNode, dataNode2);

        IntermediateQuery query = queryBuilder.build();

        System.out.println("\nBefore optimization: \n" +  query);

        IntermediateQuery optimizedQuery = optimize(query);
        System.err.println("\nUnexpected optimized query: \n" +  optimizedQuery);
    }

    @Test
    public void testRedundantSelfJoin1_T3() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_AR_2, X, Y);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        queryBuilder.init(projectionAtom, distinctNode);

        ConstructionNode topConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.addChild(distinctNode, topConstructionNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(topConstructionNode, joinNode);

        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE3, ImmutableList.of(X, A, B, C, D, E));
        queryBuilder.addChild(joinNode, dataNode1);

        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE3, ImmutableList.of(F, A, Y, G, H, I));
        queryBuilder.addChild(joinNode, dataNode2);

        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        expectedQueryBuilder.init(projectionAtom, topConstructionNode);

        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE3, ImmutableList.of(X, A, Y, C, D, E));

        expectedQueryBuilder.addChild(topConstructionNode, dataNode3);

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();

        optimizeAndCompare(query, expectedQuery);
    }

    @Test
    public void testRedundantSelfJoin2_T3()  {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_AR_2, X, Y);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        queryBuilder.init(projectionAtom, distinctNode);

        ConstructionNode topConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.addChild(distinctNode, topConstructionNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(topConstructionNode, joinNode);

        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE3, ImmutableList.of(X, A, B, C, D, E));
        queryBuilder.addChild(joinNode, dataNode1);

        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE3, ImmutableList.of(F, A, Y, G, H, I));
        queryBuilder.addChild(joinNode, dataNode2);

        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE3, ImmutableList.of(J, K, L, M, O, P));
        queryBuilder.addChild(joinNode, dataNode3);

        ExtensionalDataNode dataNode4 = createExtensionalDataNode(TABLE3, ImmutableList.of(Q, R, S, M, T, U));
        queryBuilder.addChild(joinNode, dataNode4);

        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        expectedQueryBuilder.init(projectionAtom, distinctNode);
        expectedQueryBuilder.addChild(distinctNode, topConstructionNode);

        expectedQueryBuilder.addChild(topConstructionNode, joinNode);

        ExtensionalDataNode dataNode5 = createExtensionalDataNode(TABLE3, ImmutableList.of(X, A, Y, C, D, E));

        expectedQueryBuilder.addChild(joinNode, dataNode5);

        ExtensionalDataNode dataNode6 = createExtensionalDataNode(TABLE3, ImmutableList.of(Q, R, S, M, O, U));
        expectedQueryBuilder.addChild(joinNode, dataNode6);

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();

        optimizeAndCompare(query, expectedQuery);
    }

    @Ignore("TODO: remove the redundant join")
    @Test
    public void testRedundantSelfJoin3_T3()  {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_AR_2, X, Y);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        queryBuilder.init(projectionAtom, distinctNode);

        ConstructionNode topConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.addChild(distinctNode, topConstructionNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(topConstructionNode, joinNode);

        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE3, ImmutableList.of(X, A, B, C, D, E));
        queryBuilder.addChild(joinNode, dataNode1);

        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE3, ImmutableList.of(F, A, Y, C, H, I));
        queryBuilder.addChild(joinNode, dataNode2);

        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        expectedQueryBuilder.init(projectionAtom, distinctNode);
        expectedQueryBuilder.addChild(distinctNode, topConstructionNode);

        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE3, ImmutableList.of(X, A, Y, C, D, E));

        expectedQueryBuilder.addChild(topConstructionNode, dataNode3);

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();

        optimizeAndCompare(query, expectedQuery);
    }

    @Test
    public void testRedundantSelfJoin1_T4()  {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_AR_2, X, Y);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        queryBuilder.init(projectionAtom, distinctNode);

        ConstructionNode topConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.addChild(distinctNode, topConstructionNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(topConstructionNode, joinNode);

        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE4, ImmutableList.of(X, A, B, C, D));
        queryBuilder.addChild(joinNode, dataNode1);

        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE4, ImmutableList.of(E, A, Y, F, G));
        queryBuilder.addChild(joinNode, dataNode2);

        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        expectedQueryBuilder.init(projectionAtom, topConstructionNode);

        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE4, ImmutableList.of(X, A, Y, C, D));

        expectedQueryBuilder.addChild(topConstructionNode, dataNode3);

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();

        optimizeAndCompare(query, expectedQuery);
    }

    @Test
    public void testRedundantSelfJoin2_T4() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_AR_2, X, Y);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        queryBuilder.init(projectionAtom, distinctNode);

        ConstructionNode topConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.addChild(distinctNode, topConstructionNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(topConstructionNode, joinNode);

        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE4, ImmutableList.of(X, A, B, C, D));
        queryBuilder.addChild(joinNode, dataNode1);

        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE4, ImmutableList.of(E, A, B, Y, G));
        queryBuilder.addChild(joinNode, dataNode2);

        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        expectedQueryBuilder.init(projectionAtom, topConstructionNode);

        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE4, ImmutableList.of(X, A, B, Y, D));

        expectedQueryBuilder.addChild(topConstructionNode, dataNode3);

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();

        optimizeAndCompare(query, expectedQuery);
    }

    @Test
    public void testLJNonRedundantSelfLeftJoin1() throws EmptyQueryException {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_AR_3, X, Y, Z);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        queryBuilder.init(projectionAtom, distinctNode);

        ConstructionNode topConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.addChild(distinctNode, topConstructionNode);

        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        queryBuilder.addChild(topConstructionNode, leftJoinNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(leftJoinNode, joinNode, LEFT);

        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1, ImmutableList.of(X, A, B, Z, Y));
        queryBuilder.addChild(joinNode, dataNode1);

        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE1, ImmutableList.of(C, A, D, E, F));
        queryBuilder.addChild(joinNode, dataNode2);

        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE1, ImmutableList.of(F, H, I, J, K));
        queryBuilder.addChild(leftJoinNode, dataNode3, RIGHT);

        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder expectedBuilder = createQueryBuilder();
        expectedBuilder.init(projectionAtom, distinctNode);
        expectedBuilder.addChild(distinctNode, topConstructionNode);
        expectedBuilder.addChild(topConstructionNode, leftJoinNode);

        ExtensionalDataNode dataNode4 = createExtensionalDataNode(TABLE1, ImmutableList.of(X, A, B, Z, Y));

        expectedBuilder.addChild(leftJoinNode, dataNode4, LEFT);

        expectedBuilder.addChild(leftJoinNode, dataNode3, RIGHT);

        optimizeAndCompare(query, expectedBuilder.build());
    }

    private static void optimizeAndCompare(IQ initialIQ, IQ expectedIQ) {
        System.out.println("Initial query: "+ initialIQ);
        System.out.println("Expected query: "+ expectedIQ);
        IQ optimizedIQ = JOIN_LIKE_OPTIMIZER.optimize(initialIQ, EXECUTOR_REGISTRY);
        System.out.println("Optimized query: "+ optimizedIQ);
    }

    private static void optimizeAndCompare(IntermediateQuery initialQuery, IntermediateQuery expectedQuery) {
        optimizeAndCompare(IQ_CONVERTER.convert(initialQuery), IQ_CONVERTER.convert(expectedQuery));
    }

    private IntermediateQuery optimize(IntermediateQuery query) throws EmptyQueryException {
        IQ initialIQ =  IQ_CONVERTER.convert(query);

        IQ optimizedIQ = JOIN_LIKE_OPTIMIZER.optimize(initialIQ, EXECUTOR_REGISTRY);
        if (optimizedIQ.getTree().isDeclaredAsEmpty())
            throw new EmptyQueryException();

        return IQ_CONVERTER.convert(optimizedIQ, EXECUTOR_REGISTRY);
    }
}
