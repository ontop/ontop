package it.unibz.inf.ontop.iq.executor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.OfflineMetadataProviderBuilder;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;

import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.template.Template;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.InequalityLabel;
import it.unibz.inf.ontop.model.type.DBTermType;
import org.junit.Ignore;
import org.junit.Test;


import static it.unibz.inf.ontop.OptimizationTestingTools.*;
import static org.junit.Assert.assertEquals;

/**
 * TODO: explain
 */

public class LeftJoinOptimizationTest {

    private final static NamedRelationDefinition TABLE1;
    private final static NamedRelationDefinition TABLE1a;
    private final static NamedRelationDefinition TABLE2;
    private final static NamedRelationDefinition TABLE2a;
    private final static NamedRelationDefinition TABLE3;
    private final static NamedRelationDefinition TABLE4;
    private final static NamedRelationDefinition TABLE5;
    private final static NamedRelationDefinition TABLE6;
    private final static NamedRelationDefinition TABLE21;
    private final static NamedRelationDefinition TABLE22;
    private final static AtomPredicate ANS1_ARITY_2_PREDICATE = ATOM_FACTORY.getRDFAnswerPredicate( 2);
    private final static AtomPredicate ANS1_ARITY_3_PREDICATE = ATOM_FACTORY.getRDFAnswerPredicate( 3);
    private final static AtomPredicate ANS1_ARITY_4_PREDICATE = ATOM_FACTORY.getRDFAnswerPredicate( 4);

    private final static ImmutableList<Template.Component> URI_TEMPLATE_STR_1 = Template.of("http://example.org/ds1/", 0);

    private final static Variable X = TERM_FACTORY.getVariable("x");
    private final static Variable Y = TERM_FACTORY.getVariable("y");
    private final static Variable Z = TERM_FACTORY.getVariable("z");
    private final static DBConstant ONE = TERM_FACTORY.getDBConstant("1", TYPE_FACTORY.getDBTypeFactory().getDBLargeIntegerType());
    private final static DBConstant TWO = TERM_FACTORY.getDBConstant("2", TYPE_FACTORY.getDBTypeFactory().getDBLargeIntegerType());

    private final static Variable M = TERM_FACTORY.getVariable("m");
    private final static Variable M1 = TERM_FACTORY.getVariable("m1");
    private final static Variable M1F0 = TERM_FACTORY.getVariable("m1f0");
    private final static Variable M2 = TERM_FACTORY.getVariable("m2");
    private final static Variable MF1 = TERM_FACTORY.getVariable("mf1");
    private final static Variable N = TERM_FACTORY.getVariable("n");
    private final static Variable NF1 = TERM_FACTORY.getVariable("nf1");
    private final static Variable N1 = TERM_FACTORY.getVariable("n1");
    private final static Variable N1F0 = TERM_FACTORY.getVariable("n1f0");
    private final static Variable N1F1 = TERM_FACTORY.getVariable("n1f1");
    private final static Variable N2 = TERM_FACTORY.getVariable("n2");
    private final static Variable O = TERM_FACTORY.getVariable("o");
    private final static Variable OF0 = TERM_FACTORY.getVariable("of0");
    private final static Variable OF1 = TERM_FACTORY.getVariable("of1");
    private final static Variable O1 = TERM_FACTORY.getVariable("o1");
    private final static Variable O2 = TERM_FACTORY.getVariable("o2");
    private final static Variable O1F1 = TERM_FACTORY.getVariable("o1f1");
    private final static Variable F0 = TERM_FACTORY.getVariable("f0");

    static {
        OfflineMetadataProviderBuilder builder = createMetadataProviderBuilder();
        DBTermType integerDBType = builder.getDBTypeFactory().getDBLargeIntegerType();

        /*
         * Table 1: non-composite unique constraint and regular field
         */
        TABLE1 = builder.createDatabaseRelation( "TABLE1",
            "col1", integerDBType, false,
            "col2", integerDBType, false,
            "col3", integerDBType, true);
        UniqueConstraint.primaryKeyOf(TABLE1.getAttribute(1));

        /*
         * Table 2: non-composite unique constraint and regular field
         */
        TABLE2 = builder.createDatabaseRelation("TABLE2",
           "col1", integerDBType, false,
           "col2", integerDBType, false,
           "col3", integerDBType, false);
        UniqueConstraint.primaryKeyOf(TABLE2.getAttribute(1));
        ForeignKeyConstraint.of("fk2-1", TABLE2.getAttribute(2), TABLE1.getAttribute(1));

        /*
         * Table 3: composite unique constraint over the first TWO columns
         */
        TABLE3 = builder.createDatabaseRelation("TABLE3",
            "col1", integerDBType, false,
            "col2", integerDBType, false,
            "col3", integerDBType, false);
        UniqueConstraint.primaryKeyOf(TABLE3.getAttribute(1), TABLE3.getAttribute(2));

        /*
         * Table 1a: non-composite unique constraint and regular field
         */
        TABLE1a = builder.createDatabaseRelation("TABLE1A",
            "col1", integerDBType, false,
            "col2", integerDBType, false,
            "col3", integerDBType, false,
            "col4", integerDBType, false);
        UniqueConstraint.primaryKeyOf(TABLE1a.getAttribute(1));

        /*
         * Table 2a: non-composite unique constraint and regular field
         */
        TABLE2a = builder.createDatabaseRelation("TABLE2A",
            "col1", integerDBType, false,
            "col2", integerDBType, false,
            "col3", integerDBType, false);
        UniqueConstraint.primaryKeyOf(TABLE2a.getAttribute(1));
        ForeignKeyConstraint.builder("composite-fk", TABLE2a, TABLE1a)
            .add(2, 1)
            .add(3, 2)
            .build();

        /*
         * Table 4: non-composite unique constraint and nullable fk
         */
        TABLE4 = builder.createDatabaseRelation("TABLE4",
            "col1", integerDBType, false,
            "col2", integerDBType, false,
            "col3", integerDBType, true);
        UniqueConstraint.primaryKeyOf(TABLE4.getAttribute(1));
        ForeignKeyConstraint.of("fk4-1", TABLE4.getAttribute(3), TABLE1.getAttribute(1));

        /*
         * Table 5: nullable unique constraint
         */
        TABLE5 = builder.createDatabaseRelation("TABLE5",
            "col1", integerDBType, true,
            "col2", integerDBType, false);
        UniqueConstraint.builder(TABLE5, "uc5")
                    .addDeterminant(1)
                    .build();

        /*
         * Table 6: PK + nullable column
         */
        TABLE6 = builder.createDatabaseRelation("TABLE6",
                "col1", integerDBType, false,
                "col2", integerDBType, true);
        UniqueConstraint.primaryKeyOf(TABLE6.getAttribute(1));

        TABLE21 = builder.createDatabaseRelation("table21",
                "col1", integerDBType, false,
                "col2", integerDBType, false,
                "col3", integerDBType, false,
                "col4", integerDBType, false,
                "col5", integerDBType, false,
                "col6", integerDBType, true);
        UniqueConstraint.primaryKeyOf(TABLE21.getAttribute(1));
        FunctionalDependency.defaultBuilder(TABLE21)
                .addDeterminant(2)
                .addDependent(3)
                .addDependent(4)
                .build();

        TABLE22 = builder.createDatabaseRelation("table22",
                "col1", integerDBType, false,
                "col2", integerDBType, true,
                "col3", integerDBType, true,
                "col4", integerDBType, true,
                "col5", integerDBType, true);
        UniqueConstraint.primaryKeyOf(TABLE22.getAttribute(1));
        FunctionalDependency.defaultBuilder(TABLE22)
                .addDeterminant(2)
                .addDependent(3)
                .addDependent(4)
                .build();
    }

    @Test
    public void testSelfJoinElimination1()  {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_3_PREDICATE, M, N, O);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, N, O1));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE1,  ImmutableList.of(M, N1, O));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode, dataNode1, dataNode2)));

        ExtensionalDataNode dataNode5 =  createExtensionalDataNode(TABLE1,  ImmutableList.of(M, N, O));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, dataNode5);

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testSelfJoinElimination2()  {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_3_PREDICATE, M, N, O);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode(TERM_FACTORY.getDBIsNotNull(O));
        ExtensionalDataNode dataNode1 =  createExtensionalDataNode(TABLE1, ImmutableList.of(M, N, O1));
        ExtensionalDataNode dataNode2 =  createExtensionalDataNode(TABLE1, ImmutableList.of(M, N1, O));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode, dataNode1, dataNode2)));

        ExtensionalDataNode dataNode5 =  createExtensionalDataNode(TABLE1, ImmutableList.of(M, N, O));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, dataNode5);

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testNoSelfLeftJoin3() {

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_3_PREDICATE, M, N, O);
        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        ExtensionalDataNode dataNode1 =  IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, M, 1, N));
        ExtensionalDataNode dataNode2 =  IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(1, N, 2, O));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                        IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode, dataNode1, dataNode2));

        IQ expectedIQ = initialIQ;

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testSelfJoinWithCondition() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_3_PREDICATE, M, N, O);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode(TERM_FACTORY.getStrictEquality(O, TWO));
        ExtensionalDataNode dataNode1 =  createExtensionalDataNode(TABLE1, ImmutableList.of(M, N, O1));
        ExtensionalDataNode dataNode2 =  createExtensionalDataNode(TABLE1, ImmutableList.of(M, N1, O));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode, dataNode1, dataNode2)));

        ConstructionNode newConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(O, TERM_FACTORY.getIfElseNull(
                        TERM_FACTORY.getStrictEquality(F1, TWO), TWO)));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(newConstructionNode,
                        createExtensionalDataNode(TABLE1, ImmutableList.of(M, N, F1))));

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testSelfLeftJoinNonUnification1() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_2_PREDICATE, M, N);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        ExtensionalDataNode dataNode1 =  createExtensionalDataNode(TABLE1, ImmutableList.of(M, N1, ONE));
        ExtensionalDataNode dataNode2 =  createExtensionalDataNode(TABLE1, ImmutableList.of(M, N, TWO));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode, dataNode1, dataNode2)));

        ConstructionNode newConstructionNode = IQ_FACTORY.createConstructionNode(
                projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(N, NULL));
        ExtensionalDataNode newDataNode =  IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, M, 2, ONE));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(newConstructionNode, newDataNode));

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testSelfLeftJoinNonUnification1NotSimplifiedExpression() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_2_PREDICATE, M, N);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        ExtensionalDataNode dataNode1 =  createExtensionalDataNode(TABLE1, ImmutableList.of(M, N1, ONE));
        ExtensionalDataNode dataNode2 =  createExtensionalDataNode(TABLE1, ImmutableList.of(M, N, TWO));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode, dataNode1, dataNode2)));

        ConstructionNode newConstructionNode = IQ_FACTORY.createConstructionNode(
                projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(N,
                        TERM_FACTORY.getNullConstant()));
        ExtensionalDataNode newDataNode =  IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, M, 2, ONE));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(newConstructionNode, newDataNode));

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testSelfLeftJoinNonUnificationEmptyResult() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_2_PREDICATE, M, N);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        FilterNode filterNode = IQ_FACTORY.createFilterNode(TERM_FACTORY.getDBIsNotNull(N));
        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        ExtensionalDataNode dataNode1 =  createExtensionalDataNode(TABLE1, ImmutableList.of(M, N1, ONE));
        ExtensionalDataNode dataNode2 =  createExtensionalDataNode(TABLE1, ImmutableList.of(M, N, TWO));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createUnaryIQTree(filterNode,
                            IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode, dataNode1, dataNode2))));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, IQ_FACTORY.createEmptyNode(ImmutableSet.of(M, N)));

        optimizeAndCompare(initialIQ, expectedIQ);
    }


    @Test
    public void testSelfLeftJoinIfElseNull1() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_2_PREDICATE, M, N);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        ExtensionalDataNode dataNode1 =  createExtensionalDataNode(TABLE1, ImmutableList.of(M, N1, O));
        ExtensionalDataNode dataNode2 =  createExtensionalDataNode(TABLE1, ImmutableList.of(M, N, TWO));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode, dataNode1, dataNode2)));

        ConstructionNode newConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(N, TERM_FACTORY.getIfElseNull(
                        TERM_FACTORY.getStrictEquality(F1, TWO), NF0)));
        ExtensionalDataNode newDataNode = createExtensionalDataNode(TABLE1, ImmutableList.of(M, NF0, F1));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(newConstructionNode, newDataNode));

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testSelfLeftJoinIfElseNull2() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_2_PREDICATE, M, N);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        ExtensionalDataNode dataNode1 =  createExtensionalDataNode(TABLE1, ImmutableList.of(M, N1, O));
        ExtensionalDataNode dataNode2 =  createExtensionalDataNode(TABLE1, ImmutableList.of(M, N, N));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode, dataNode1, dataNode2)));

        ConstructionNode newConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(N, TERM_FACTORY.getIfElseNull( 
                        TERM_FACTORY.getConjunction(
                                TERM_FACTORY.getStrictEquality(NF0, NF1),
                                TERM_FACTORY.getDBIsNotNull(NF1)),
                        NF0)));
        ExtensionalDataNode newDataNode = createExtensionalDataNode(TABLE1, ImmutableList.of(M, NF0, NF1));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(newConstructionNode, newDataNode));

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testNoSelfLeftJoin1() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_2_PREDICATE, M, N);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        ExtensionalDataNode dataNode1 =  createExtensionalDataNode(TABLE1, ImmutableList.of(M, N1, ONE));
        ExtensionalDataNode dataNode2 =  createExtensionalDataNode(TABLE1, ImmutableList.of(N1, N, TWO));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode, dataNode1, dataNode2)));

        IQ expectedIQ = initialIQ;

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testNoSelfLeftJoin2() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_2_PREDICATE, M, N);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        ExtensionalDataNode dataNode1 =  createExtensionalDataNode(TABLE1, ImmutableList.of(M, N1, ONE));
        ExtensionalDataNode dataNode2 =  createExtensionalDataNode(TABLE1, ImmutableList.of(N1, M, N));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode, dataNode1, dataNode2)));

        IQ expectedIQ = initialIQ;

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testLeftJoinElimination1()  {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_4_PREDICATE, M, M1, O, N1);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        ExtensionalDataNode dataNode1 =  createExtensionalDataNode(TABLE2, ImmutableList.of(M, M1, O));
        ExtensionalDataNode dataNode2 =  createExtensionalDataNode(TABLE1, ImmutableList.of(M1, N1, O1));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode, dataNode1, dataNode2)));

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        ExtensionalDataNode newDataNode2 =  IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, M1, 1, N1));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(dataNode1, newDataNode2)));

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testLeftJoinElimination2() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_4_PREDICATE, M, M1, M2, N1);
        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        ExtensionalDataNode dataNode1 =  createExtensionalDataNode(TABLE2a, ImmutableList.of(M, M1, M2));
        ExtensionalDataNode dataNode2 =  IQ_FACTORY.createExtensionalDataNode(TABLE1a, ImmutableMap.of(0, M1, 1, M2, 2, N1));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode, dataNode1, dataNode2));

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(dataNode1, dataNode2)));

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testLeftJoinElimination3() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_4_PREDICATE, M, M1, M2, N1);
        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        ExtensionalDataNode dataNode1 =  createExtensionalDataNode(TABLE2a, ImmutableList.of(M, M1, M2));
        ExtensionalDataNode dataNode2 =  IQ_FACTORY.createExtensionalDataNode(TABLE1a, ImmutableMap.of(0, M1, 1, M, 2, N1));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode, dataNode1, dataNode2));

        IQ expectedIQ = initialIQ;

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testLeftJoinElimination4() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_4_PREDICATE, M, M1, O, N1);
        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        ExtensionalDataNode dataNode1 =  createExtensionalDataNode(TABLE2, ImmutableList.of(M, M1, O));
        ExtensionalDataNode dataNode2 =  IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, M1,  2, N1));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode, dataNode1, dataNode2));

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(dataNode1, dataNode2)));

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testLeftJoinElimination5() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_4_PREDICATE, M, M1, O, O1);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        ExtensionalDataNode dataNode1 =  createExtensionalDataNode(TABLE2, ImmutableList.of(M, M1, O));
        ExtensionalDataNode dataNode2 =  createExtensionalDataNode(TABLE1, ImmutableList.of(M1, M1, O1));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode, dataNode1, dataNode2)));

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                    SUBSTITUTION_FACTORY.getSubstitution(O1, TERM_FACTORY.getIfElseNull(
                            TERM_FACTORY.getStrictEquality(M1, M1F0),
                            O1F1)));
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE1, ImmutableList.of(M1, M1F0, O1F1));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode1,
                        IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(dataNode1, dataNode3))));

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testLeftJoinNonElimination1() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_3_PREDICATE, M, N, O);
        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        ExtensionalDataNode dataNode1 =  IQ_FACTORY.createExtensionalDataNode(TABLE4, ImmutableMap.of(0, M, 2, O));
        ExtensionalDataNode dataNode2 =  IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, O, 1, N));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode, dataNode1, dataNode2));

        IQ expectedIQ = initialIQ;

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testLeftJoinEliminationWithFilterCondition2() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_4_PREDICATE, M, M1, O, N1);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode(TERM_FACTORY.getDBIsNotNull(N1));
        ExtensionalDataNode dataNode1 =  createExtensionalDataNode(TABLE2, ImmutableList.of(M, M1, O));
        ExtensionalDataNode dataNode2 =  IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, M1, 2, N1));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode, dataNode1, dataNode2)));

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(dataNode1, dataNode2)));

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testLeftJoinEliminationWithFilterCondition4() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_4_PREDICATE, M, M1, O, N1);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        ImmutableExpression expression = TERM_FACTORY.getStrictEquality(O1, TWO);
        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode(expression);
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE2, ImmutableList.of(M, M1, O));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE1, ImmutableList.of(M1, N1, O1));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode, dataNode1, dataNode2)));

        ConstructionNode newConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(N1,
                        TERM_FACTORY.getIfElseNull(TERM_FACTORY.getStrictEquality(F1, TWO), N1F0)));
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        ExtensionalDataNode newDataNode2 = createExtensionalDataNode(TABLE1, ImmutableList.of(M1, N1F0, F1));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(newConstructionNode,
                    IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(dataNode1, newDataNode2))));

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testLeftJoinEliminationWithImplicitFilterCondition() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_4_PREDICATE, M, M1, O, N1);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE2, ImmutableList.of(M, M1, O));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE1, ImmutableList.of(M1, N1, TWO));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode, dataNode1, dataNode2)));

        ImmutableExpression expression = TERM_FACTORY.getStrictEquality(F1, TWO);
        ConstructionNode newConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(N1,
                        TERM_FACTORY.getIfElseNull( expression, N1F0)));
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        ExtensionalDataNode newDataNode2 =  createExtensionalDataNode(TABLE1, ImmutableList.of(M1, N1F0, F1));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(newConstructionNode,
                        IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(dataNode1, newDataNode2))));

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testSelfLeftJoinWithJoinOnLeft1() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_3_PREDICATE, M, N, O);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, N, O1));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, N1, O));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE3, ImmutableMap.of());
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode,
                                IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(dataNode1, dataNode3)),
                                dataNode2)));

        ExtensionalDataNode dataNode5 =  createExtensionalDataNode(TABLE1, ImmutableList.of(M, N, O));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(dataNode5, dataNode3)));

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testSelfLeftJoinWithJoinOnLeft2() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_3_PREDICATE, M, N, O);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, N, O1));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, N1, O));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE3, ImmutableMap.of(1, N1));
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode,
                                IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(dataNode1, dataNode3)),
                                dataNode2)));

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(O,
                        TERM_FACTORY.getIfElseNull(
                            TERM_FACTORY.getStrictEquality(N1, N),
                            OF1)));
        ExtensionalDataNode dataNode5 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, N, OF1));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode1,
                    IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(dataNode5, dataNode3))));

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testSelfJoinNullableUniqueConstraint() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_2_PREDICATE, M, N);
        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        ExtensionalDataNode dataNode1 =  IQ_FACTORY.createExtensionalDataNode(TABLE5, ImmutableMap.of(0, M));
        ExtensionalDataNode dataNode2 =  createExtensionalDataNode(TABLE5, ImmutableList.of(M, N));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                        IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode, dataNode1, dataNode2));

        IQ expectedIQ = initialIQ;

        optimizeAndCompare(initialIQ, expectedIQ);
    }


    @Test
    public void testLeftJoinEliminationUnnecessaryConstructionNode1() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_4_PREDICATE, M, M1, O, N1);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        ExtensionalDataNode dataNode1 =  createExtensionalDataNode(TABLE2, ImmutableList.of(M, M1, O));
        ExtensionalDataNode dataNode2 =  createExtensionalDataNode(TABLE1, ImmutableList.of(M1, N1, O1));
        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(dataNode2.getVariables());

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode,
                                dataNode1,
                                IQ_FACTORY.createUnaryIQTree(rightConstructionNode, dataNode2))));

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        ExtensionalDataNode newDataNode2 =  IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, M1, 1, N1));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(dataNode1, newDataNode2)));

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testLeftJoinEliminationConstructionNode1() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_2_PREDICATE, X, Y);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(M1)));

        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE2, ImmutableMap.of(1, M1));
        ImmutableExpression o1IsNotNull = TERM_FACTORY.getDBIsNotNull(O1);
        FilterNode rightFilterNode = IQ_FACTORY.createFilterNode(o1IsNotNull);
        ImmutableFunctionalTerm uri1O1Term = generateURI1(O1);
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, M1, 2, O1));
        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(M1, Y),
                SUBSTITUTION_FACTORY.getSubstitution(Y, uri1O1Term));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode,
                                dataNode1,
                                IQ_FACTORY.createUnaryIQTree(rightConstructionNode,
                                        IQ_FACTORY.createUnaryIQTree(rightFilterNode, dataNode2)))));

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, generateURI1(M1),
                        Y, TERM_FACTORY.getRDFFunctionalTerm(
                                uri1O1Term.getTerm(0),
                                TERM_FACTORY.getIfElseNull(
                                        o1IsNotNull,
                                        uri1O1Term.getTerm(1)))));
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode1,
                        IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(dataNode1, dataNode2))));

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Ignore("TODO: let the LJ optimizer consider equalities in the LJ condition for detecting constraint matching")
    @Test
    public void testLeftJoinEliminationConstructionNode2_1() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_3_PREDICATE, X, Y, Z);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, generateURI1(M),
                        Y, generateURI1(N)));
        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        ExtensionalDataNode dataNode1 =  createExtensionalDataNode(TABLE2, ImmutableList.of(M, N, O));

        ImmutableExpression o1IsNotNull = TERM_FACTORY.getDBIsNotNull(O1);
        FilterNode rightFilterNode = IQ_FACTORY.createFilterNode(o1IsNotNull);

        ExtensionalDataNode dataNode2 =  createExtensionalDataNode(TABLE1, ImmutableList.of(M, M, O1));
        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(M, N, Z),
                SUBSTITUTION_FACTORY.getSubstitution(
                        Z, generateURI1(O1),
                        N, M));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode,
                                dataNode1,
                                IQ_FACTORY.createUnaryIQTree(rightConstructionNode,
                                        IQ_FACTORY.createUnaryIQTree(rightFilterNode, dataNode2)))));


        ImmutableExpression zCondition = TERM_FACTORY.getConjunction(
                TERM_FACTORY.getStrictEquality(M, N),
                TERM_FACTORY.getStrictEquality(M, MF1),
                o1IsNotNull);

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, generateURI1(M),
                        Y, generateURI1(N),
                        Z, TERM_FACTORY.getIfElseNull(
                                zCondition,
                                generateURI1(O1))));
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        ExtensionalDataNode newRightDataNode = createExtensionalDataNode(TABLE1, ImmutableList.of(M, MF1, O1));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode1,
                        IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(dataNode1, newRightDataNode))));

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Ignore("TODO: clean the expression")
    @Test
    public void testLeftJoinEliminationConstructionNode2_2() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_3_PREDICATE, X, Y, Z);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, generateURI1(M),
                        Y, generateURI1(N)));
        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        ExtensionalDataNode dataNode1 =  IQ_FACTORY.createExtensionalDataNode(TABLE2, ImmutableMap.of(0, M, 1, N));
        ImmutableExpression o1IsNotNull = TERM_FACTORY.getDBIsNotNull(O1);
        FilterNode rightFilterNode = IQ_FACTORY.createFilterNode(o1IsNotNull);
        ImmutableFunctionalTerm uri1O1Term = generateURI1(O1);
        ExtensionalDataNode dataNode2 =  createExtensionalDataNode(TABLE1, ImmutableList.of(N, N, O1));
        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(M, N, Z),
                SUBSTITUTION_FACTORY.getSubstitution(
                        Z, uri1O1Term,
                        M, N));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode,
                                dataNode1,
                                IQ_FACTORY.createUnaryIQTree(rightConstructionNode,
                                        IQ_FACTORY.createUnaryIQTree(rightFilterNode, dataNode2)))));


        ImmutableExpression zCondition = TERM_FACTORY.getConjunction(
                TERM_FACTORY.getStrictEquality(F0, N),
                TERM_FACTORY.getDBIsNotNull(O1F1),
                TERM_FACTORY.getStrictEquality(M, N));

        ImmutableFunctionalTerm uri1O1F1Term = generateURI1(O1F1);

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, generateURI1(M),
                        Y, generateURI1(N),
                        Z, TERM_FACTORY.getRDFFunctionalTerm(
                            TERM_FACTORY.getIfElseNull(
                                    zCondition,
                                    uri1O1F1Term.getTerm(0)),
                            TERM_FACTORY.getIfElseNull(
                                    zCondition,
                                    uri1O1F1Term.getTerm(1)))));

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        ExtensionalDataNode newRightDataNode = createExtensionalDataNode(TABLE1, ImmutableList.of(N, F0, O1F1));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode1,
                        IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(dataNode1, newRightDataNode))));

        optimizeAndCompare(initialIQ, expectedIQ);
    }
    
    @Test
    public void testLeftJoinOrder1() {

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ATOM_FACTORY.getRDFAnswerPredicate(2), ImmutableList.of(A, B));

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, A));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE3, ImmutableMap.of(0, A));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, A, 1, B));

        BinaryNonCommutativeIQTree leftJoinTree1 = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                dataNode1,
                dataNode2);

        BinaryNonCommutativeIQTree leftJoinTree2 = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                leftJoinTree1,
                dataNode3);

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, leftJoinTree2);

        BinaryNonCommutativeIQTree newLeftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                dataNode3,
                dataNode2);

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newLeftJoinTree);

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testLeftJoinDenormalized1() {

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ATOM_FACTORY.getRDFAnswerPredicate(2), ImmutableList.of(A, B));

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, A));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, A, 2, B));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(2, B));

        NaryIQTree joinTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode2, dataNode3));

        ImmutableExpression expression = TERM_FACTORY.getDBIsNotNull(B);

        BinaryNonCommutativeIQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(expression),
                dataNode1,
                joinTree);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(distinctNode, leftJoinTree));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, dataNode2);

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Ignore("Not clear how to reach that state")
    @Test
    public void testLeftJoinDenormalized2() {

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ATOM_FACTORY.getRDFAnswerPredicate(2), ImmutableList.of(A, B));

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, A, 2, C));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(2, C));

        ImmutableExpression expression = TERM_FACTORY.getDBIsNotNull(C);

        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(C, D),
                SUBSTITUTION_FACTORY.getSubstitution(D, TERM_FACTORY.getProvenanceSpecialConstant()));

        BinaryNonCommutativeIQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(expression),
                dataNode1,
                IQ_FACTORY.createUnaryIQTree(rightConstructionNode, dataNode2));

        ConstructionNode topConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(B, TERM_FACTORY.getIfElseNull(TERM_FACTORY.getDBIsNotNull(D), C)));

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(distinctNode,
                        IQ_FACTORY.createUnaryIQTree(topConstructionNode, leftJoinTree)));

        ExtensionalDataNode newDataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, A, 2, B));
        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newDataNode1);

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testJoinTransfer1() {

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ATOM_FACTORY.getRDFAnswerPredicate(3), ImmutableList.of(A, B, C));

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, A));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, A,1, B));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE3, ImmutableMap.of(0, B,2, C));

        NaryIQTree rightJoin = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode2, dataNode3));

        BinaryNonCommutativeIQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                dataNode1,
                rightJoin);

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, leftJoinTree);

        ExtensionalDataNode dataNode4 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, A, 1, BF0));
        ExtensionalDataNode dataNode5 = IQ_FACTORY.createExtensionalDataNode(TABLE3, ImmutableMap.of(0, BF0,2, C));

        BinaryNonCommutativeIQTree newLeftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                dataNode4, dataNode5);

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(B, TERM_FACTORY.getIfElseNull(TERM_FACTORY.getDBIsNotNull(C), BF0)));

        UnaryIQTree newTree = IQ_FACTORY.createUnaryIQTree(constructionNode, newLeftJoinTree);

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newTree);

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testJoinTransfer2() {

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ATOM_FACTORY.getRDFAnswerPredicate(3), ImmutableList.of(A, B, C));

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE2, ImmutableMap.of(0, A));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE2, ImmutableMap.of(0, A,1, B));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, B,2, C));

        NaryIQTree rightJoin = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode2, dataNode3));

        BinaryNonCommutativeIQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                dataNode1,
                rightJoin);

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, leftJoinTree);

        ExtensionalDataNode dataNode4 = IQ_FACTORY.createExtensionalDataNode(TABLE2, ImmutableMap.of(0, A, 1, B));
        ExtensionalDataNode dataNode5 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, B,2, C));

        NaryIQTree newJoinTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode4, dataNode5));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newJoinTree);

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testJoinTransfer3() {

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ATOM_FACTORY.getRDFAnswerPredicate(3), ImmutableList.of(A, B, C));

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE2, ImmutableMap.of(0, A));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE2, ImmutableMap.of(0, A,1, B));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, B,2, C));

        NaryIQTree rightJoin = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode2, dataNode3));

        BinaryNonCommutativeIQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(TERM_FACTORY.getDBNumericInequality(InequalityLabel.LT, B, TWO)),
                dataNode1,
                rightJoin);

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, leftJoinTree);

        ExtensionalDataNode dataNode4 = IQ_FACTORY.createExtensionalDataNode(TABLE2, ImmutableMap.of(0, A, 1, BF0));
        ExtensionalDataNode dataNode5 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, BF0,2, CF0));

        NaryIQTree newJoinTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode4, dataNode5));

        ImmutableExpression cond = TERM_FACTORY.getDBNumericInequality(InequalityLabel.LT, BF0, TWO);

        UnaryIQTree newTree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(
                        projectionAtom.getVariables(),
                        SUBSTITUTION_FACTORY.getSubstitution(
                                B, TERM_FACTORY.getIfElseNull(cond, BF0),
                                C, TERM_FACTORY.getIfElseNull(cond, CF0))),
                newJoinTree);

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newTree);

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testJoinTransfer4() {

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ATOM_FACTORY.getRDFAnswerPredicate(3), ImmutableList.of(A, B, C));

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE2, ImmutableMap.of(0, A, 1, B));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, B,2, C));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE3, ImmutableMap.of(0, C));

        NaryIQTree rightJoin = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode2, dataNode3));

        BinaryNonCommutativeIQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(TERM_FACTORY.getDBNumericInequality(InequalityLabel.LT, C, TWO)),
                dataNode1,
                rightJoin);

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, leftJoinTree);

        ExtensionalDataNode dataNode4 = IQ_FACTORY.createExtensionalDataNode(TABLE2, ImmutableMap.of(0, A, 1, B));
        ExtensionalDataNode dataNode5 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, B,2, CF0));
        ExtensionalDataNode dataNode6 = IQ_FACTORY.createExtensionalDataNode(TABLE3, ImmutableMap.of(0, CF0, 1, PROV));

        NaryIQTree newJoinTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode4, dataNode5));


        BinaryNonCommutativeIQTree newLeftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(TERM_FACTORY.getDBNumericInequality(InequalityLabel.LT, CF0, TWO)),
                newJoinTree,
                dataNode6);

        ImmutableExpression cond = TERM_FACTORY.getDBIsNotNull(PROV);

        UnaryIQTree newTree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(
                        projectionAtom.getVariables(),
                        SUBSTITUTION_FACTORY.getSubstitution(
                                C, TERM_FACTORY.getIfElseNull(cond, CF0))),
                newLeftJoinTree);

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newTree);

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testJoinTransfer5() {

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ATOM_FACTORY.getRDFAnswerPredicate(3), ImmutableList.of(A, B, C));

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE2, ImmutableMap.of(0, A, 1, B));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, B,2, C));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE6, ImmutableMap.of(0, C));

        NaryIQTree rightJoin = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode2, dataNode3));

        BinaryNonCommutativeIQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(TERM_FACTORY.getDBNumericInequality(InequalityLabel.LT, C, TWO)),
                dataNode1,
                rightJoin);

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, leftJoinTree);

        ExtensionalDataNode dataNode5 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, B,2, CF0));
        ExtensionalDataNode dataNode6 = IQ_FACTORY.createExtensionalDataNode(TABLE6, ImmutableMap.of(0, CF0));

        NaryIQTree newJoinTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode1, dataNode5));

        UnaryIQTree newRight = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(ImmutableSet.of(CF0, F1),
                        SUBSTITUTION_FACTORY.getSubstitution(F1, TERM_FACTORY.getProvenanceSpecialConstant())),
                dataNode6);


        BinaryNonCommutativeIQTree newLeftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(TERM_FACTORY.getDBNumericInequality(InequalityLabel.LT, CF0, TWO)),
                newJoinTree,
                newRight);

        ImmutableExpression cond = TERM_FACTORY.getDBIsNotNull(F1);

        UnaryIQTree newTree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(
                        projectionAtom.getVariables(),
                        SUBSTITUTION_FACTORY.getSubstitution(
                                C, TERM_FACTORY.getIfElseNull(cond, CF0))),
                newLeftJoinTree);

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newTree);

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testJoinTransfer6() {

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ATOM_FACTORY.getRDFAnswerPredicate(3), ImmutableList.of(A, B, C));

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE4, ImmutableMap.of(0, A, 2, B));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, B,2, C));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE6, ImmutableMap.of(0, C));

        NaryIQTree rightJoin = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode2, dataNode3));

        BinaryNonCommutativeIQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(TERM_FACTORY.getDBNumericInequality(InequalityLabel.LT, C, TWO)),
                dataNode1,
                rightJoin);

        FilterNode filterNode = IQ_FACTORY.createFilterNode(TERM_FACTORY.getDBIsNotNull(B));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, IQ_FACTORY.createUnaryIQTree(filterNode, leftJoinTree));

        ExtensionalDataNode dataNode5 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, B,2, CF0));
        ExtensionalDataNode dataNode6 = IQ_FACTORY.createExtensionalDataNode(TABLE6, ImmutableMap.of(0, CF0));

        NaryIQTree newJoinTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode1, dataNode5));

        UnaryIQTree newRight = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(ImmutableSet.of(CF0, F1),
                        SUBSTITUTION_FACTORY.getSubstitution(F1, TERM_FACTORY.getProvenanceSpecialConstant())),
                dataNode6);


        BinaryNonCommutativeIQTree newLeftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(TERM_FACTORY.getDBNumericInequality(InequalityLabel.LT, CF0, TWO)),
                newJoinTree,
                newRight);

        ImmutableExpression cond = TERM_FACTORY.getDBIsNotNull(F1);

        UnaryIQTree newTree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(
                        projectionAtom.getVariables(),
                        SUBSTITUTION_FACTORY.getSubstitution(
                                C, TERM_FACTORY.getIfElseNull(cond, CF0))),
                newLeftJoinTree);

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newTree);

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testJoinTransfer7() {

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ATOM_FACTORY.getRDFAnswerPredicate(3), ImmutableList.of(A, B, C));

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE5, ImmutableMap.of(0, A));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE5, ImmutableMap.of(0, A,1, B));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE3, ImmutableMap.of(0, B,2, C));

        NaryIQTree rightJoin = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode2, dataNode3));

        FilterNode filterNode = IQ_FACTORY.createFilterNode(TERM_FACTORY.getDBIsNotNull(A));

        BinaryNonCommutativeIQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                dataNode1,
                rightJoin);

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, IQ_FACTORY.createUnaryIQTree(filterNode, leftJoinTree));

        ExtensionalDataNode dataNode4 = IQ_FACTORY.createExtensionalDataNode(TABLE5, ImmutableMap.of(0, A, 1, BF0));
        ExtensionalDataNode dataNode5 = IQ_FACTORY.createExtensionalDataNode(TABLE3, ImmutableMap.of(0, BF0,2, C));

        BinaryNonCommutativeIQTree newLeftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                dataNode4, dataNode5);

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(B, TERM_FACTORY.getIfElseNull(TERM_FACTORY.getDBIsNotNull(C), BF0)));

        UnaryIQTree newTree = IQ_FACTORY.createUnaryIQTree(constructionNode,
                IQ_FACTORY.createUnaryIQTree(filterNode,
                        newLeftJoinTree));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newTree);

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testJoinTransfer8() {

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                ATOM_FACTORY.getRDFAnswerPredicate(4), ImmutableList.of(A, B, C, D));

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE4, ImmutableMap.of(0, A, 2, D));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE4, ImmutableMap.of(0, A,1, B, 2, D));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE3, ImmutableMap.of(0, B,2, C));

        NaryIQTree rightJoin = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode2, dataNode3));

        FilterNode filterNode = IQ_FACTORY.createFilterNode(TERM_FACTORY.getDBIsNotNull(D));

        BinaryNonCommutativeIQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                dataNode1,
                rightJoin);

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, IQ_FACTORY.createUnaryIQTree(filterNode, leftJoinTree));

        ExtensionalDataNode dataNode4 = IQ_FACTORY.createExtensionalDataNode(TABLE4, ImmutableMap.of(0, A, 1, BF0, 2, D));
        ExtensionalDataNode dataNode5 = IQ_FACTORY.createExtensionalDataNode(TABLE3, ImmutableMap.of(0, BF0,2, C));

        BinaryNonCommutativeIQTree newLeftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(TERM_FACTORY.getDBIsNotNull(D)),
                dataNode4, dataNode5);

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(B, TERM_FACTORY.getIfElseNull(TERM_FACTORY.getDBIsNotNull(C), BF0)));

        UnaryIQTree newTree = IQ_FACTORY.createUnaryIQTree(constructionNode,
                IQ_FACTORY.createUnaryIQTree(filterNode,
                        newLeftJoinTree));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newTree);

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testJoinTransfer9() {

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                ATOM_FACTORY.getRDFAnswerPredicate(3), ImmutableList.of(A, B, C));

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE4, ImmutableMap.of(0, A, 2, C));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE4, ImmutableMap.of(0, A,1, B, 2, C));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE3, ImmutableMap.of(0, B,2, C));

        NaryIQTree rightJoin = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode2, dataNode3));

        FilterNode filterNode = IQ_FACTORY.createFilterNode(TERM_FACTORY.getDBIsNotNull(C));

        BinaryNonCommutativeIQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                dataNode1,
                rightJoin);

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, IQ_FACTORY.createUnaryIQTree(filterNode, leftJoinTree));

        ExtensionalDataNode dataNode4 = IQ_FACTORY.createExtensionalDataNode(TABLE4, ImmutableMap.of(0, A, 1, BF0, 2, C));
        ExtensionalDataNode dataNode5 = IQ_FACTORY.createExtensionalDataNode(TABLE3, ImmutableMap.of(0, BF0, 1, PROV, 2, C));

        BinaryNonCommutativeIQTree newLeftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                dataNode4, dataNode5);

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(B, TERM_FACTORY.getIfElseNull(TERM_FACTORY.getDBIsNotNull(PROV), BF0)));

        UnaryIQTree newTree = IQ_FACTORY.createUnaryIQTree(constructionNode,
                IQ_FACTORY.createUnaryIQTree(filterNode,
                        newLeftJoinTree));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newTree);

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testJoinTransfer10() {

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                ATOM_FACTORY.getRDFAnswerPredicate(3), ImmutableList.of(A, B, C));

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE4, ImmutableMap.of(0, A, 2, C));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE4, ImmutableMap.of(0, A,1, B, 2, C));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE3, ImmutableMap.of(0, B,2, C));

        NaryIQTree rightJoin = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode2, dataNode3));

        BinaryNonCommutativeIQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                dataNode1,
                rightJoin);

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, leftJoinTree);

        ExtensionalDataNode dataNode4 = IQ_FACTORY.createExtensionalDataNode(TABLE4, ImmutableMap.of(0, A, 1, BF0, 2, C));
        ExtensionalDataNode dataNode5 = IQ_FACTORY.createExtensionalDataNode(TABLE3, ImmutableMap.of(0, BF0, 1, PROV, 2, C));

        BinaryNonCommutativeIQTree newLeftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                dataNode4, dataNode5);

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(B, TERM_FACTORY.getIfElseNull(TERM_FACTORY.getDBIsNotNull(PROV), BF0)));

        UnaryIQTree newTree = IQ_FACTORY.createUnaryIQTree(constructionNode, newLeftJoinTree);

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newTree);

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testJoinTransfer11() {

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ATOM_FACTORY.getRDFAnswerPredicate(3), ImmutableList.of(A, B, C));

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, A));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, A,1, ONE));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE3, ImmutableMap.of(0, ONE,2, C));

        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(A, B, C),
                SUBSTITUTION_FACTORY.getSubstitution(B, ONE));

        IQTree rightJoin = IQ_FACTORY.createUnaryIQTree(
                rightConstructionNode,
                IQ_FACTORY.createNaryIQTree(
                        IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(dataNode2, dataNode3)));

        BinaryNonCommutativeIQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                dataNode1,
                rightJoin);

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, leftJoinTree);

        ExtensionalDataNode dataNode4 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, A, 1, F0));
        ExtensionalDataNode dataNode5 = IQ_FACTORY.createExtensionalDataNode(TABLE3, ImmutableMap.of(0, ONE,2, C));

        BinaryNonCommutativeIQTree newLeftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(TERM_FACTORY.getStrictEquality(F0, ONE)),
                dataNode4, dataNode5);

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(B, TERM_FACTORY.getIfElseNull(TERM_FACTORY.getDBIsNotNull(C), ONE)));

        UnaryIQTree newTree = IQ_FACTORY.createUnaryIQTree(constructionNode, newLeftJoinTree);

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newTree);

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testNonJoinTransfer6() {

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ATOM_FACTORY.getRDFAnswerPredicate(3), ImmutableList.of(A, B, C));

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE4, ImmutableMap.of(0, A, 2, B));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, B,2, C));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE6, ImmutableMap.of(0, C));

        NaryIQTree rightJoin = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode2, dataNode3));

        BinaryNonCommutativeIQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(TERM_FACTORY.getDBNumericInequality(InequalityLabel.LT, C, TWO)),
                dataNode1,
                rightJoin);

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, leftJoinTree);

        optimizeAndCompare(initialIQ, initialIQ);
    }

    @Test
    public void testNonJoinTransfer7() {

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ATOM_FACTORY.getRDFAnswerPredicate(3), ImmutableList.of(A, B, C));

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE5, ImmutableMap.of(0, A));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE5, ImmutableMap.of(0, A,1, B));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE3, ImmutableMap.of(0, B,2, C));

        NaryIQTree rightJoin = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode2, dataNode3));

        BinaryNonCommutativeIQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                dataNode1,
                rightJoin);

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, leftJoinTree);

        optimizeAndCompare(initialIQ, initialIQ);
    }

    /**
     * TODO: remove the top distinct in the expected query
     */
    @Test
    public void testJoinTransferFD1() {

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ATOM_FACTORY.getRDFAnswerPredicate(4), ImmutableList.of(A, B, C, D));

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE21, ImmutableMap.of(0, D, 1, A));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE21, ImmutableMap.of(1, A,2, B));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, B,1, C));

        NaryIQTree rightJoin = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode2, dataNode3));

        BinaryNonCommutativeIQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                dataNode1,
                rightJoin);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, IQ_FACTORY.createUnaryIQTree(distinctNode, leftJoinTree));

        ExtensionalDataNode dataNode4 = IQ_FACTORY.createExtensionalDataNode(TABLE21, ImmutableMap.of(0, D, 1, A, 2, BF0));
        ExtensionalDataNode dataNode5 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, BF0,1, C));

        BinaryNonCommutativeIQTree newLeftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                dataNode4, dataNode5);

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(B, TERM_FACTORY.getIfElseNull(TERM_FACTORY.getDBIsNotNull(C), BF0)));

        // TODO: get rid of the distinct here
        UnaryIQTree newTree = IQ_FACTORY.createUnaryIQTree(distinctNode,
                IQ_FACTORY.createUnaryIQTree(constructionNode, newLeftJoinTree));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newTree);

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testJoinTransferFD2() {

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                ATOM_FACTORY.getRDFAnswerPredicate(3), ImmutableList.of(A, B, C));

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE21, ImmutableMap.of(0, C, 1, A));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE21, ImmutableMap.of(1, A,2, B));

        BinaryNonCommutativeIQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                dataNode1,
                dataNode2);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, IQ_FACTORY.createUnaryIQTree(distinctNode, leftJoinTree));

        ExtensionalDataNode newDataNode = IQ_FACTORY.createExtensionalDataNode(TABLE21, ImmutableMap.of(0, C, 1, A, 2, B));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newDataNode);

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    /**
     * TODO: remove the top distinct in the expected query
     */
    @Test
    public void testJoinTransferFD3() {

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ATOM_FACTORY.getRDFAnswerPredicate(4), ImmutableList.of(A, B, C, D));

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE21, ImmutableMap.of(0, D, 1, A, 4, E));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE21, ImmutableMap.of(1, A,2, B, 4, E));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, B,1, C));

        NaryIQTree rightJoin = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode2, dataNode3));

        BinaryNonCommutativeIQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                dataNode1,
                rightJoin);

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(distinctNode,
                        IQ_FACTORY.createUnaryIQTree(constructionNode, leftJoinTree)));

        ExtensionalDataNode dataNode4 = IQ_FACTORY.createExtensionalDataNode(TABLE21, ImmutableMap.of(0, D, 1, A, 2, BF0));
        ExtensionalDataNode dataNode5 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, BF0,1, C));

        BinaryNonCommutativeIQTree newLeftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                dataNode4, dataNode5);

        ConstructionNode newConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(B, TERM_FACTORY.getIfElseNull(TERM_FACTORY.getDBIsNotNull(C), BF0)));

        // TODO: get rid of the distinct here
        UnaryIQTree newTree = IQ_FACTORY.createUnaryIQTree(distinctNode,
                IQ_FACTORY.createUnaryIQTree(newConstructionNode, newLeftJoinTree));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newTree);

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    /**
     * TODO: remove the top distinct in the expected query
     */
    @Test
    public void testJoinTransferFD4() {

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ATOM_FACTORY.getRDFAnswerPredicate(4), ImmutableList.of(A, B, C, D));

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE22, ImmutableMap.of(0, D, 1, A));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE22, ImmutableMap.of(1, A,2, B));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, B,1, C));

        NaryIQTree rightJoin = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode2, dataNode3));

        BinaryNonCommutativeIQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                dataNode1,
                rightJoin);

        FilterNode filterNode = IQ_FACTORY.createFilterNode(TERM_FACTORY.getDBIsNotNull(A));

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(distinctNode,
                        IQ_FACTORY.createUnaryIQTree(filterNode, leftJoinTree)));

        ExtensionalDataNode dataNode4 = IQ_FACTORY.createExtensionalDataNode(TABLE22, ImmutableMap.of(0, D, 1, A, 2, BF0));
        ExtensionalDataNode dataNode5 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, BF0,1, C));

        BinaryNonCommutativeIQTree newLeftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                dataNode4, dataNode5);

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(B, TERM_FACTORY.getIfElseNull(TERM_FACTORY.getDBIsNotNull(C), BF0)));

        // TODO: get rid of the distinct here
        UnaryIQTree newTree = IQ_FACTORY.createUnaryIQTree(distinctNode,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createUnaryIQTree(filterNode, newLeftJoinTree)));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newTree);

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testJoinTransferFD5() {

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ATOM_FACTORY.getRDFAnswerPredicate(3), ImmutableList.of(A, B, C));

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE21, ImmutableMap.of(1, A));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE21, ImmutableMap.of(1, A,2, ONE));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE3, ImmutableMap.of(0, ONE,2, C));

        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(A, B, C),
                SUBSTITUTION_FACTORY.getSubstitution(B, ONE));

        IQTree rightJoin = IQ_FACTORY.createUnaryIQTree(
                rightConstructionNode,
                IQ_FACTORY.createNaryIQTree(
                        IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(dataNode2, dataNode3)));

        BinaryNonCommutativeIQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                dataNode1,
                rightJoin);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(distinctNode, leftJoinTree));

        ExtensionalDataNode dataNode4 = IQ_FACTORY.createExtensionalDataNode(TABLE21, ImmutableMap.of(1, A, 2, F0));
        ExtensionalDataNode dataNode5 = IQ_FACTORY.createExtensionalDataNode(TABLE3, ImmutableMap.of(0, ONE,2, C));

        BinaryNonCommutativeIQTree newLeftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(TERM_FACTORY.getStrictEquality(F0, ONE)),
                dataNode4, dataNode5);

        ConstructionNode topConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(B, TERM_FACTORY.getIfElseNull(TERM_FACTORY.getDBIsNotNull(C), ONE)));

        ConstructionNode subConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(A, C));

        UnaryIQTree newTree = IQ_FACTORY.createUnaryIQTree(topConstructionNode,
                IQ_FACTORY.createUnaryIQTree(distinctNode,
                        IQ_FACTORY.createUnaryIQTree(subConstructionNode, newLeftJoinTree)));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newTree);

        optimizeAndCompare(initialIQ, expectedIQ);
    }


    /**
     * TODO: remove the top distinct in the expected query
     */
    @Test
    public void testJoinTransferFD6() {

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ATOM_FACTORY.getRDFAnswerPredicate(5), ImmutableList.of(A, B, C, D, E));

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE21, ImmutableMap.of(0, D, 1, A));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE21, ImmutableMap.of(1, A,2, B));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, B,1, C));
        ExtensionalDataNode dataNode4 = IQ_FACTORY.createExtensionalDataNode(TABLE3, ImmutableMap.of(0, D,1, E));

        IQTree leftChild = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(), dataNode1, dataNode4);

        NaryIQTree rightJoin = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode2, dataNode3));

        BinaryNonCommutativeIQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                leftChild,
                rightJoin);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, IQ_FACTORY.createUnaryIQTree(distinctNode, leftJoinTree));

        ExtensionalDataNode dataNode5 = IQ_FACTORY.createExtensionalDataNode(TABLE21, ImmutableMap.of(0, D, 1, A, 2, BF0));
        ExtensionalDataNode dataNode6 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, BF0,1, C));

        IQTree newLeftChild = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(), dataNode5, dataNode4);

        BinaryNonCommutativeIQTree newLeftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                newLeftChild, dataNode6);

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(B, TERM_FACTORY.getIfElseNull(TERM_FACTORY.getDBIsNotNull(C), BF0)));

        // TODO: get rid of the distinct here
        UnaryIQTree newTree = IQ_FACTORY.createUnaryIQTree(distinctNode,
                IQ_FACTORY.createUnaryIQTree(constructionNode, newLeftJoinTree));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newTree);

        optimizeAndCompare(initialIQ, expectedIQ);
    }



    /**
     * Missing DISTINCT
     */
    @Test
    public void testNonJoinTransferFD1() {

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ATOM_FACTORY.getRDFAnswerPredicate(4), ImmutableList.of(A, B, C, D));

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE21, ImmutableMap.of(0, D, 1, A));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE21, ImmutableMap.of(1, A,2, B));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE3, ImmutableMap.of(0, B,2, C));

        NaryIQTree rightJoin = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode2, dataNode3));

        BinaryNonCommutativeIQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                dataNode1,
                rightJoin);

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, leftJoinTree);

        optimizeAndCompare(initialIQ, initialIQ);
    }

    @Test
    public void testNonJoinTransferFD2() {

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                ATOM_FACTORY.getRDFAnswerPredicate(4), ImmutableList.of(A, B, C, D));

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE21, ImmutableMap.of(0, C, 1, A));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE21, ImmutableMap.of(0, D, 1, A,2, B));

        BinaryNonCommutativeIQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                dataNode1,
                dataNode2);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, IQ_FACTORY.createUnaryIQTree(distinctNode, leftJoinTree));
        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, leftJoinTree);

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    /**
     * E is nullable
     */
    @Test
    public void testNonJoinTransferFD3() {

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ATOM_FACTORY.getRDFAnswerPredicate(4), ImmutableList.of(A, B, C, D));

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE21, ImmutableMap.of(0, D, 1, A, 5, E));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE21, ImmutableMap.of(1, A,2, B, 5, E));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, B,1, C));

        NaryIQTree rightJoin = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode2, dataNode3));

        BinaryNonCommutativeIQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                dataNode1,
                rightJoin);

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(distinctNode,
                        IQ_FACTORY.createUnaryIQTree(constructionNode, leftJoinTree)));

        optimizeAndCompare(initialIQ, initialIQ);
    }

    /**
     * A is nullable
     */
    @Test
    public void testNonJoinTransferFD4() {

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ATOM_FACTORY.getRDFAnswerPredicate(4), ImmutableList.of(A, B, C, D));

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE22, ImmutableMap.of(0, D, 1, A));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE22, ImmutableMap.of(1, A,2, B));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, B,1, C));

        NaryIQTree rightJoin = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode2, dataNode3));

        BinaryNonCommutativeIQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                dataNode1,
                rightJoin);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, IQ_FACTORY.createUnaryIQTree(distinctNode, leftJoinTree));

        optimizeAndCompare(initialIQ, initialIQ);
    }

    @Test
    public void testJoinTransferSameTerms1() {

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ATOM_FACTORY.getRDFAnswerPredicate(3), ImmutableList.of(A, B, C));

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE3, ImmutableMap.of(1, A, 2, B));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE3, ImmutableMap.of(1, A));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, B,1, C));

        NaryIQTree rightJoin = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode2, dataNode3));

        BinaryNonCommutativeIQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                dataNode1,
                rightJoin);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, IQ_FACTORY.createUnaryIQTree(distinctNode, leftJoinTree));

        BinaryNonCommutativeIQTree newLeftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                dataNode1, dataNode3);

        UnaryIQTree newTree = IQ_FACTORY.createUnaryIQTree(distinctNode, newLeftJoinTree);

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newTree);

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testJoinTransferSameTerms2() {

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ATOM_FACTORY.getRDFAnswerPredicate(2), ImmutableList.of(A, B));

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE3, ImmutableMap.of(1, A, 2, B));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE3, ImmutableMap.of(1, A));

        BinaryNonCommutativeIQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                dataNode1,
                dataNode2);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, IQ_FACTORY.createUnaryIQTree(distinctNode, leftJoinTree));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(distinctNode, dataNode1));

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    /**
     * Nullable column (A)
     */
    @Test
    public void testNonJoinTransferSameTerms1() {

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                ATOM_FACTORY.getRDFAnswerPredicate(3), ImmutableList.of(A, B, C));

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(1, B, 2, A));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(2, A));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE21, ImmutableMap.of(0, B,1, C));

        NaryIQTree rightJoin = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode2, dataNode3));

        BinaryNonCommutativeIQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                dataNode1,
                rightJoin);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, IQ_FACTORY.createUnaryIQTree(distinctNode, leftJoinTree));

        optimizeAndCompare(initialIQ, initialIQ);
    }

    @Ignore("TODO: support (no new variable coming from the right)")
    @Test
    public void testLJSameTerms1() {

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ATOM_FACTORY.getRDFAnswerPredicate(2), ImmutableList.of(A, B));

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(1, B, 2, A));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(2, A));

        BinaryNonCommutativeIQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                dataNode1,
                dataNode2);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, IQ_FACTORY.createUnaryIQTree(distinctNode, leftJoinTree));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(distinctNode, dataNode1));

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testLeftJoinUnionConstants() {

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ATOM_FACTORY.getRDFAnswerPredicate(3), ImmutableList.of(A, B, C));

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, A, 2, C));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, A, 1, B));

        UnionNode unionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(C));
        IQTree fact1 = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(unionNode.getVariables(), SUBSTITUTION_FACTORY.getSubstitution(C, ONE)),
                IQ_FACTORY.createTrueNode());
        IQTree fact2 = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(unionNode.getVariables(), SUBSTITUTION_FACTORY.getSubstitution(C, TWO)),
                IQ_FACTORY.createTrueNode());
        NaryIQTree unionTree = IQ_FACTORY.createNaryIQTree(
                unionNode,
                ImmutableList.of(fact1, fact2));

        NaryIQTree leftTree = IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode1, unionTree));

        BinaryNonCommutativeIQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                leftTree,
                dataNode2);

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, leftJoinTree);

        ExtensionalDataNode newDataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, A, 1, B, 2, C));
        IQTree newJoinTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(newDataNode1, unionTree));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newJoinTree);

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testLeftJoinValues() {

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ATOM_FACTORY.getRDFAnswerPredicate(3), ImmutableList.of(A, B, C));

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, A, 2, C));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, A, 1, B));

        ValuesNode valuesNode = IQ_FACTORY.createValuesNode(ImmutableList.of(C),
                ImmutableList.of(
                        ImmutableList.of(ONE),
                        ImmutableList.of(TWO)));

        NaryIQTree leftTree = IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode1, valuesNode));

        BinaryNonCommutativeIQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                leftTree,
                dataNode2);

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, leftJoinTree);

        ExtensionalDataNode newDataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, A, 1, B, 2, C));
        IQTree newJoinTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(newDataNode1, valuesNode));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newJoinTree);

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testLeftJoinJoinLimit() {

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ATOM_FACTORY.getRDFAnswerPredicate(3), ImmutableList.of(A, B, C));

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, A, 2, C));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, A, 1, B));

        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(2, C));
        IQTree limitTree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createSliceNode(1),
                dataNode3);

        NaryIQTree leftTree = IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode1, limitTree));

        BinaryNonCommutativeIQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                leftTree,
                dataNode2);

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, leftJoinTree);

        ExtensionalDataNode newDataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, A, 1, B, 2, C));
        IQTree newJoinTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(newDataNode1, limitTree));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newJoinTree);

        optimizeAndCompare(initialIQ, expectedIQ);
    }


    private static void optimizeAndCompare(IQ initialIQ, IQ expectedIQ) {
        System.out.println("Initial query: "+ initialIQ);
        IQ optimizedIQ = JOIN_LIKE_OPTIMIZER.optimize(initialIQ);
        System.out.println("Optimized query: "+ optimizedIQ);

        System.out.println("Expected query: "+ expectedIQ);
        assertEquals(expectedIQ, optimizedIQ);
    }

    private static ImmutableFunctionalTerm generateURI1(VariableOrGroundTerm argument) {
        return TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_STR_1, ImmutableList.of(argument));
    }
}
