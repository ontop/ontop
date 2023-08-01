package it.unibz.inf.ontop.iq.optimizer;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.NamedRelationDefinition;
import it.unibz.inf.ontop.dbschema.UniqueConstraint;
import it.unibz.inf.ontop.dbschema.impl.OfflineMetadataProviderBuilder;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.type.DBTermType;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static it.unibz.inf.ontop.OptimizationTestingTools.*;
import static junit.framework.TestCase.assertEquals;

public class FlattenLiftTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlattenLiftTest.class);

    private final static NamedRelationDefinition TABLE1;
    private final static NamedRelationDefinition TABLE2;
    private final static NamedRelationDefinition TABLE3;
    private final static NamedRelationDefinition TABLE4;
    private final static NamedRelationDefinition TABLE5;


    private final static Variable A = TERM_FACTORY.getVariable("a");
    private final static Variable B = TERM_FACTORY.getVariable("b");
    private final static Variable C = TERM_FACTORY.getVariable("c");
    private final static Variable D = TERM_FACTORY.getVariable("d");
    private final static Variable N1 = TERM_FACTORY.getVariable("n1");
    private final static Variable N2 = TERM_FACTORY.getVariable("n2");
    private final static Variable N4 = TERM_FACTORY.getVariable("n4");
    private final static Variable N5 = TERM_FACTORY.getVariable("n5");
    private final static Variable O1 = TERM_FACTORY.getVariable("o1");
    private final static Variable O2 = TERM_FACTORY.getVariable("o2");
    private final static Variable O3 = TERM_FACTORY.getVariable("o3");
    private final static Variable O4 = TERM_FACTORY.getVariable("o4");
    private final static Variable O5 = TERM_FACTORY.getVariable("o5");
    private final static Variable X1 = TERM_FACTORY.getVariable("x1");
    private final static Variable X2 = TERM_FACTORY.getVariable("x2");

    private final static DBConstant ONE = TERM_FACTORY.getDBConstant("1", TYPE_FACTORY.getDBTypeFactory().getDBLargeIntegerType());
    private final static DBConstant TWO = TERM_FACTORY.getDBConstant("2", TYPE_FACTORY.getDBTypeFactory().getDBLargeIntegerType());
    private final static ImmutableExpression X1_EQ_X2 = TERM_FACTORY.getStrictEquality(X1, X2);
    private final static ImmutableExpression C_EQ_ONE = TERM_FACTORY.getStrictEquality(C, ONE);
    private final static ImmutableExpression X1_EQ_X2_AND_C_EQ_ONE = TERM_FACTORY.getConjunction(
            X1_EQ_X2, C_EQ_ONE
    );



    static {


        OfflineMetadataProviderBuilder builder = createMetadataProviderBuilder();
        DBTermType integerDBType = builder.getDBTypeFactory().getDBLargeIntegerType();
        DBTermType arrayDBType = builder.getDBTypeFactory().getDBArrayType();

        TABLE1 = builder.createDatabaseRelation( "TABLE1",
                "pk", integerDBType, false,
                "col2", integerDBType, true);
        UniqueConstraint.primaryKeyOf(TABLE1.getAttribute(1));

        TABLE2 = builder.createDatabaseRelation( "TABLE2",
                "pk", integerDBType, false,
                "arr", arrayDBType, true);
        UniqueConstraint.primaryKeyOf(TABLE2.getAttribute(1));

        TABLE3 = builder.createDatabaseRelation( "TABLE3",
                "pk", integerDBType, false,
                "arr", arrayDBType, true);
        UniqueConstraint.primaryKeyOf(TABLE3.getAttribute(1));

        TABLE4 = builder.createDatabaseRelation( "TABLE4",
                "pk", integerDBType, false,
                "arr", arrayDBType, true,
                "col3", integerDBType, true);
        UniqueConstraint.primaryKeyOf(TABLE4.getAttribute(1));

        TABLE5 = builder.createDatabaseRelation( "TABLE5",
                "pk", integerDBType, false,
                "arr1", arrayDBType, true,
                "arr2", arrayDBType, true,
                "arr3", arrayDBType, true,
                "arr4", arrayDBType, true);
        UniqueConstraint.primaryKeyOf(TABLE5.getAttribute(1));

    }


    @Test
    public void testLiftFlatten1() {

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                ATOM_FACTORY.getRDFAnswerPredicate(3), X1, O, B);

        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode(X1_EQ_X2);
        ExtensionalDataNode leftDataNode = createExtensionalDataNode(TABLE1, ImmutableList.of(X1, B));
        FlattenNode flattenNode = IQ_FACTORY.createFlattenNode(O, N, Optional.empty(), JSON_TYPE);
        ExtensionalDataNode rightDataNode = createExtensionalDataNode(TABLE2, ImmutableList.of(X2, N));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(
                        rootNode,
                        IQ_FACTORY.createNaryIQTree(
                                joinNode,
                                ImmutableList.of(
                                        leftDataNode,
                                        IQ_FACTORY.createUnaryIQTree(
                                                flattenNode,
                                                rightDataNode
                                        )))));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(
                        rootNode,
                        IQ_FACTORY.createUnaryIQTree(
                                flattenNode,
                                IQ_FACTORY.createNaryIQTree(
                                        joinNode,
                                        ImmutableList.of(
                                                leftDataNode,
                                                rightDataNode
                                        )))));

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testLiftFlatten2() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                ATOM_FACTORY.getRDFAnswerPredicate(3), X1, O, B);

        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode(X1_EQ_X2_AND_C_EQ_ONE);
        ExtensionalDataNode leftDataNode = createExtensionalDataNode(TABLE1, ImmutableList.of(X1, B));
        FlattenNode flattenNode = IQ_FACTORY.createFlattenNode(O, N, Optional.empty(), JSON_TYPE);
        ExtensionalDataNode rightDataNode = createExtensionalDataNode(TABLE4, ImmutableList.of(X2, N, C));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(
                        rootNode,
                        IQ_FACTORY.createNaryIQTree(
                                joinNode,
                                ImmutableList.of(
                                        leftDataNode,
                                        IQ_FACTORY.createUnaryIQTree(
                                                flattenNode,
                                                rightDataNode
                                        )))));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(
                        rootNode,
                        IQ_FACTORY.createUnaryIQTree(
                                flattenNode,
                                IQ_FACTORY.createNaryIQTree(
                                        joinNode,
                                        ImmutableList.of(
                                                leftDataNode,
                                                rightDataNode
                                        )))));

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testLiftFlatten3() {

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                ATOM_FACTORY.getRDFAnswerPredicate(3), X1, O1, O2);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode(X1_EQ_X2_AND_C_EQ_ONE);
        FlattenNode leftFlattenNode = IQ_FACTORY.createFlattenNode(O1, N1, Optional.empty(), JSON_TYPE);
        ExtensionalDataNode leftDataNode = createExtensionalDataNode(TABLE2, ImmutableList.of(X1, N1));
        FlattenNode rightFlattenNode = IQ_FACTORY.createFlattenNode(O2, N2, Optional.empty(), JSON_TYPE);
        ExtensionalDataNode rightDataNode = createExtensionalDataNode(TABLE4, ImmutableList.of(X2, N2, C));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(
                        rootNode,
                        IQ_FACTORY.createNaryIQTree(
                                joinNode,
                                ImmutableList.of(
                                        IQ_FACTORY.createUnaryIQTree(
                                                leftFlattenNode,
                                                leftDataNode
                                        ),
                                        IQ_FACTORY.createUnaryIQTree(
                                                rightFlattenNode,
                                                rightDataNode
                                        )))));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(
                        rootNode,
                        IQ_FACTORY.createUnaryIQTree(
                                leftFlattenNode,
                                IQ_FACTORY.createUnaryIQTree(
                                        rightFlattenNode,
                                        IQ_FACTORY.createNaryIQTree(
                                                joinNode,
                                                ImmutableList.of(
                                                        leftDataNode,
                                                        rightDataNode
                                                ))))));

        optimizeAndCompare(initialIQ, expectedIQ);
    }


    @Test
    public void testLiftFlattenAndJoinCondition1() {

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                ATOM_FACTORY.getRDFAnswerPredicate(3), X1, O, B);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        ImmutableExpression O_EQ_ONE = TERM_FACTORY.getStrictEquality(O, ONE);
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode(TERM_FACTORY.getConjunction(X1_EQ_X2, O_EQ_ONE));
        ExtensionalDataNode leftDataNode = createExtensionalDataNode(TABLE1, ImmutableList.of(X1, B));
        FlattenNode flattenNode = IQ_FACTORY.createFlattenNode(O, N, Optional.empty(), JSON_TYPE);
        ExtensionalDataNode rightDataNode = createExtensionalDataNode(TABLE2, ImmutableList.of(X2, N));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(
                        rootNode,
                        IQ_FACTORY.createNaryIQTree(
                                joinNode,
                                ImmutableList.of(
                                        leftDataNode,
                                        IQ_FACTORY.createUnaryIQTree(
                                                flattenNode,
                                                rightDataNode
                                        )))));

        FilterNode filterNode = IQ_FACTORY.createFilterNode(O_EQ_ONE);
        InnerJoinNode newJoinNode = IQ_FACTORY.createInnerJoinNode(X1_EQ_X2);

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(
                        rootNode,
                        IQ_FACTORY.createUnaryIQTree(
                                filterNode,
                                IQ_FACTORY.createUnaryIQTree(
                                flattenNode,
                                        IQ_FACTORY.createNaryIQTree(
                                                newJoinNode,
                                                ImmutableList.of(
                                                        leftDataNode,
                                                        rightDataNode
                                        ))))));

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testLiftDoubleFlatten() {

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                ATOM_FACTORY.getRDFAnswerPredicate(3), X1, O, B);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode(X1_EQ_X2);
        ExtensionalDataNode leftDataNode = createExtensionalDataNode(TABLE1, ImmutableList.of(X1, B));
        FlattenNode level2FlattenNode = IQ_FACTORY.createFlattenNode(O, N1, Optional.empty(), JSON_TYPE);
        FlattenNode level1FlattenNode = IQ_FACTORY.createFlattenNode(N1, N2, Optional.empty(), JSON_TYPE);
        ExtensionalDataNode rightDataNode = createExtensionalDataNode(TABLE2, ImmutableList.of(X2, N2));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(
                        rootNode,
                        IQ_FACTORY.createNaryIQTree(
                                joinNode,
                                ImmutableList.of(
                                        leftDataNode,
                                        IQ_FACTORY.createUnaryIQTree(
                                                level2FlattenNode,
                                                IQ_FACTORY.createUnaryIQTree(
                                                        level1FlattenNode,
                                                        rightDataNode
                                                ))))));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(
                        rootNode,
                        IQ_FACTORY.createUnaryIQTree(
                                level2FlattenNode,
                                IQ_FACTORY.createUnaryIQTree(
                                        level1FlattenNode,
                                        IQ_FACTORY.createNaryIQTree(
                                                joinNode,
                                                ImmutableList.of(
                                                        leftDataNode,
                                                        rightDataNode
                                                ))))));

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testLiftFlattenAndJoinCondition2() {

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                ATOM_FACTORY.getRDFAnswerPredicate(3), X1, O1, O2);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        ImmutableExpression O1_EQ_ONE = TERM_FACTORY.getStrictEquality(O1, ONE);
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode(TERM_FACTORY.getConjunction(X1_EQ_X2, O1_EQ_ONE));
        FlattenNode leftFlattenNode = IQ_FACTORY.createFlattenNode(O1, N1, Optional.empty(), JSON_TYPE);
        ExtensionalDataNode leftDataNode = createExtensionalDataNode(TABLE2, ImmutableList.of(X1, N1));
        FlattenNode rightFlattenNode = IQ_FACTORY.createFlattenNode(O2, N2, Optional.empty(), JSON_TYPE);
        ExtensionalDataNode rightDataNode = createExtensionalDataNode(TABLE3, ImmutableList.of(X2, N2));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(
                        rootNode,
                        IQ_FACTORY.createNaryIQTree(
                                joinNode,
                                ImmutableList.of(
                                        IQ_FACTORY.createUnaryIQTree(
                                                leftFlattenNode,
                                                leftDataNode
                                        ),
                                        IQ_FACTORY.createUnaryIQTree(
                                                rightFlattenNode,
                                                rightDataNode
                                        )))));


        FilterNode filterNode = IQ_FACTORY.createFilterNode(O1_EQ_ONE);
        InnerJoinNode newJoinNode = IQ_FACTORY.createInnerJoinNode(X1_EQ_X2);

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(
                        rootNode,
                        IQ_FACTORY.createUnaryIQTree(
                                rightFlattenNode,
                                IQ_FACTORY.createUnaryIQTree(
                                        filterNode,
                                        IQ_FACTORY.createUnaryIQTree(
                                                leftFlattenNode,
                                                IQ_FACTORY.createNaryIQTree(
                                                        newJoinNode,
                                                        ImmutableList.of(
                                                                leftDataNode,
                                                                rightDataNode
                                                        )))))));

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testFlattenAndJoinCondition3() {

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                ATOM_FACTORY.getRDFAnswerPredicate(3), X1, O, B);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        ImmutableExpression O_EQ_ONE = TERM_FACTORY.getStrictEquality(O, ONE);
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode(TERM_FACTORY.getConjunction(
                TERM_FACTORY.getStrictEquality(C, ONE), O_EQ_ONE));
        ExtensionalDataNode leftDataNode = createExtensionalDataNode(TABLE1, ImmutableList.of(X1, B));
        FlattenNode flattenNode = IQ_FACTORY.createFlattenNode(O, N, Optional.of(C), JSON_TYPE);
        ExtensionalDataNode rightDataNode = createExtensionalDataNode(TABLE2, ImmutableList.of(X2, N));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(
                        rootNode,
                        IQ_FACTORY.createNaryIQTree(
                                joinNode,
                                ImmutableList.of(
                                        leftDataNode,
                                        IQ_FACTORY.createUnaryIQTree(
                                                flattenNode,
                                                rightDataNode
                                        )))));

        FilterNode filterNode = IQ_FACTORY.createFilterNode(joinNode.getOptionalFilterCondition().get());

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(
                        rootNode,
                        IQ_FACTORY.createUnaryIQTree(
                                filterNode,
                                IQ_FACTORY.createUnaryIQTree(
                                        flattenNode,
                                        IQ_FACTORY.createNaryIQTree(
                                                IQ_FACTORY.createInnerJoinNode(),
                                                ImmutableList.of(
                                                        leftDataNode,
                                                        rightDataNode
                                                ))))));

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testLiftLeftAndRightFlattenWithLeftJoin() {

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                ATOM_FACTORY.getRDFAnswerPredicate(3), X1, O1, O2);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode(X1_EQ_X2);
        FlattenNode leftFlattenNode = IQ_FACTORY.createFlattenNode(O1, N1, Optional.empty(), JSON_TYPE);
        ExtensionalDataNode leftDataNode = createExtensionalDataNode(TABLE2, ImmutableList.of(X1, N1));
        FlattenNode rightFlattenNode = IQ_FACTORY.createFlattenNode(O2, N2, Optional.empty(), JSON_TYPE);
        ExtensionalDataNode rightDataNode = createExtensionalDataNode(TABLE3, ImmutableList.of(X2, N2));

        IQ initialIQ = IQ_FACTORY.createIQ(
                projectionAtom,
                IQ_FACTORY.createUnaryIQTree(
                        rootNode,
                        IQ_FACTORY.createBinaryNonCommutativeIQTree(
                                leftJoinNode,
                                IQ_FACTORY.createUnaryIQTree(
                                        leftFlattenNode,
                                        leftDataNode
                                ),
                                IQ_FACTORY.createUnaryIQTree(
                                        rightFlattenNode,
                                        rightDataNode
                                ))));

        IQ expectedIQ = IQ_FACTORY.createIQ(
                projectionAtom,
                IQ_FACTORY.createUnaryIQTree(
                        rootNode,
                        IQ_FACTORY.createUnaryIQTree(
                                leftFlattenNode,
                                IQ_FACTORY.createUnaryIQTree(
                                        rightFlattenNode,
                                        IQ_FACTORY.createBinaryNonCommutativeIQTree(
                                                leftJoinNode,
                                                leftDataNode,
                                                rightDataNode
                                        )))));

        optimizeAndCompare(initialIQ, expectedIQ);
    }

//    @Test
//    public void testBlockRightAndLeftFlattenWithLeftJoin1() {
//        testBlockRightAndLeftFlattenWithLeftJoin(TERM_FACTORY.getStrictEquality(O1, X2));
//    }
//
//    @Test
//    public void testBlockRightAndLeftFlattenWithLeftJoin2() {
//        testBlockRightAndLeftFlattenWithLeftJoin(TERM_FACTORY.getStrictEquality(O1, X1));
//    }
//
//    @Test
//    public void testBlockRightAndLeftFlattenWithLeftJoin3() {
//        testBlockRightAndLeftFlattenWithLeftJoin(TERM_FACTORY.getStrictEquality(O1, O2));
//    }


    @Test
    public void testLiftLeftFlattenWithLeftJoin() {

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                ATOM_FACTORY.getRDFAnswerPredicate(3), X1, O1, O2);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode(TERM_FACTORY.getStrictEquality(O2, X2));
        FlattenNode leftFlattenNode = IQ_FACTORY.createFlattenNode(O1, N1, Optional.empty(), JSON_TYPE);
        ExtensionalDataNode leftDataNode = createExtensionalDataNode(TABLE2, ImmutableList.of(X1, N1));
        FlattenNode rightFlattenNode = IQ_FACTORY.createFlattenNode(O2, N2, Optional.empty(), JSON_TYPE);
        ExtensionalDataNode rightDataNode = createExtensionalDataNode(TABLE3, ImmutableList.of(X2, N2));

        IQ initialIQ = IQ_FACTORY.createIQ(
                projectionAtom,
                IQ_FACTORY.createUnaryIQTree(
                        rootNode,
                        IQ_FACTORY.createBinaryNonCommutativeIQTree(
                                leftJoinNode,
                                IQ_FACTORY.createUnaryIQTree(
                                        leftFlattenNode,
                                        leftDataNode
                                ),
                                IQ_FACTORY.createUnaryIQTree(
                                        rightFlattenNode,
                                        rightDataNode
                                ))));

        IQ expectedIQ = IQ_FACTORY.createIQ(
                projectionAtom,
                IQ_FACTORY.createUnaryIQTree(
                        rootNode,
                        IQ_FACTORY.createUnaryIQTree(
                                leftFlattenNode,
                                IQ_FACTORY.createBinaryNonCommutativeIQTree(
                                        leftJoinNode,
                                        leftDataNode,
                                        IQ_FACTORY.createUnaryIQTree(
                                                rightFlattenNode,
                                                rightDataNode
                                        )))));

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testLiftRightFlattenWithLeftJoin() {

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                ATOM_FACTORY.getRDFAnswerPredicate(3), X1, O1, O2);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode(TERM_FACTORY.getStrictEquality(O1, X2));
        FlattenNode leftFlattenNode = IQ_FACTORY.createFlattenNode(O1, N1, Optional.empty(), JSON_TYPE);
        ExtensionalDataNode leftDataNode = createExtensionalDataNode(TABLE2, ImmutableList.of(X1, N1));
        FlattenNode rightFlattenNode = IQ_FACTORY.createFlattenNode(O2, N2, Optional.empty(), JSON_TYPE);
        ExtensionalDataNode rightDataNode = createExtensionalDataNode(TABLE3, ImmutableList.of(X2, N2));

        IQ initialIQ = IQ_FACTORY.createIQ(
                projectionAtom,
                IQ_FACTORY.createUnaryIQTree(
                        rootNode,
                        IQ_FACTORY.createBinaryNonCommutativeIQTree(
                                leftJoinNode,
                                IQ_FACTORY.createUnaryIQTree(
                                        leftFlattenNode,
                                        leftDataNode
                                ),
                                IQ_FACTORY.createUnaryIQTree(
                                        rightFlattenNode,
                                        rightDataNode
                                ))));

        IQ expectedIQ = IQ_FACTORY.createIQ(
                projectionAtom,
                IQ_FACTORY.createUnaryIQTree(
                        rootNode,
                        IQ_FACTORY.createUnaryIQTree(
                                rightFlattenNode,
                                IQ_FACTORY.createBinaryNonCommutativeIQTree(
                                        leftJoinNode,
                                        IQ_FACTORY.createUnaryIQTree(
                                                leftFlattenNode,
                                                leftDataNode
                                        ),
                                        rightDataNode
                                ))));

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testNoLiftLeftJoin() {

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                ATOM_FACTORY.getRDFAnswerPredicate(3), X1, O1, O2);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode(TERM_FACTORY.getStrictEquality(O1, O2));
        FlattenNode leftFlattenNode = IQ_FACTORY.createFlattenNode(O1, N1, Optional.empty(), JSON_TYPE);
        ExtensionalDataNode leftDataNode = createExtensionalDataNode(TABLE2, ImmutableList.of(X1, N1));
        FlattenNode rightFlattenNode = IQ_FACTORY.createFlattenNode(O2, N2, Optional.empty(), JSON_TYPE);
        ExtensionalDataNode rightDataNode = createExtensionalDataNode(TABLE3, ImmutableList.of(X2, N2));

        IQ initialIQ = IQ_FACTORY.createIQ(
                projectionAtom,
                IQ_FACTORY.createUnaryIQTree(
                        rootNode,
                        IQ_FACTORY.createBinaryNonCommutativeIQTree(
                                leftJoinNode,
                                IQ_FACTORY.createUnaryIQTree(
                                        leftFlattenNode,
                                        leftDataNode
                                ),
                                IQ_FACTORY.createUnaryIQTree(
                                        rightFlattenNode,
                                        rightDataNode
                                ))));

        optimizeAndCompare(initialIQ, initialIQ);
    }

    @Test
    @Ignore
    public void testConsecutiveFlatten1() {

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                ATOM_FACTORY.getRDFAnswerPredicate(5), X, O1, O2, O3, O4);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        FilterNode filter = IQ_FACTORY.createFilterNode(TERM_FACTORY.getStrictEquality(O1, O3));


        FlattenNode flatten1 = IQ_FACTORY.createFlattenNode(O1, N1, Optional.empty(), JSON_TYPE);
        FlattenNode flatten2 = IQ_FACTORY.createFlattenNode(O2, N2, Optional.empty(), JSON_TYPE);
        FlattenNode flatten3 = IQ_FACTORY.createFlattenNode(O3, O5, Optional.empty(), JSON_TYPE);
        FlattenNode flatten4 = IQ_FACTORY.createFlattenNode(O4, N4, Optional.empty(), JSON_TYPE);
        FlattenNode flatten5 = IQ_FACTORY.createFlattenNode(O5, N5, Optional.empty(), JSON_TYPE);

        ExtensionalDataNode dataNode = createExtensionalDataNode(TABLE5, ImmutableList.of(X, N1, N2, N4, N5));

        IQ initialIQ = IQ_FACTORY.createIQ(
                projectionAtom,
                IQ_FACTORY.createUnaryIQTree(
                        rootNode,
                        IQ_FACTORY.createUnaryIQTree(
                                filter,
                                IQ_FACTORY.createUnaryIQTree(
                                        flatten1,
                                        IQ_FACTORY.createUnaryIQTree(
                                                flatten2,
                                                IQ_FACTORY.createUnaryIQTree(
                                                        flatten3,
                                                        IQ_FACTORY.createUnaryIQTree(
                                                                flatten4,
                                                                IQ_FACTORY.createUnaryIQTree(
                                                                        flatten5,
                                                                        dataNode
                                                                ))))))));

        IQ expectedIQ = IQ_FACTORY.createIQ(
                projectionAtom,
                IQ_FACTORY.createUnaryIQTree(
                        rootNode,
                        IQ_FACTORY.createUnaryIQTree(
                                flatten4,
                                IQ_FACTORY.createUnaryIQTree(
                                        flatten2,
                                        IQ_FACTORY.createUnaryIQTree(
                                                filter,
                                                IQ_FACTORY.createUnaryIQTree(
                                                        flatten3,
                                                        IQ_FACTORY.createUnaryIQTree(
                                                                flatten5,
                                                                IQ_FACTORY.createUnaryIQTree(
                                                                        flatten1,
                                                                        dataNode
                                                                ))))))));

        optimizeAndCompare(initialIQ, expectedIQ);
    }


    @Test
    @Ignore
    public void testConsecutiveFlatten2() {

        ImmutableExpression exp1 = TERM_FACTORY.getStrictEquality(O1, ONE);
        ImmutableExpression exp2 = TERM_FACTORY.getStrictEquality(O3, TWO);
        ImmutableExpression exp3 = TERM_FACTORY.getConjunction(exp1, exp2);

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                ATOM_FACTORY.getRDFAnswerPredicate(5), X, O1, O2, O3, O4);

        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        FilterNode filter = IQ_FACTORY.createFilterNode(exp3);

        FlattenNode flatten1 = IQ_FACTORY.createFlattenNode(O1, A, Optional.empty(), JSON_TYPE);
        FlattenNode flatten2 = IQ_FACTORY.createFlattenNode(O2, B, Optional.empty(), JSON_TYPE);
        FlattenNode flatten3 = IQ_FACTORY.createFlattenNode(O3, O5, Optional.empty(), JSON_TYPE);
        FlattenNode flatten4 = IQ_FACTORY.createFlattenNode(O4, D, Optional.empty(), JSON_TYPE);
        FlattenNode flatten5 = IQ_FACTORY.createFlattenNode(O5, C, Optional.empty(), JSON_TYPE);

        ExtensionalDataNode dataNode = createExtensionalDataNode(TABLE5, ImmutableList.of(X, N1, N2, N4, N5));

        IQ initialIQ = IQ_FACTORY.createIQ(
                projectionAtom,
                IQ_FACTORY.createUnaryIQTree(
                        rootNode,
                        IQ_FACTORY.createUnaryIQTree(
                                filter,
                                IQ_FACTORY.createUnaryIQTree(
                                        flatten1,
                                        IQ_FACTORY.createUnaryIQTree(
                                                flatten2,
                                                IQ_FACTORY.createUnaryIQTree(
                                                        flatten3,
                                                        IQ_FACTORY.createUnaryIQTree(
                                                                flatten4,
                                                                IQ_FACTORY.createUnaryIQTree(
                                                                        flatten5,
                                                                        dataNode
                                                                ))))))));


        FilterNode filter1 = IQ_FACTORY.createFilterNode(exp1);
        FilterNode filter2 = IQ_FACTORY.createFilterNode(exp2);

        IQ expectedIQ = IQ_FACTORY.createIQ(
                projectionAtom,
                IQ_FACTORY.createUnaryIQTree(
                        rootNode,
                        IQ_FACTORY.createUnaryIQTree(
                                flatten4,
                                IQ_FACTORY.createUnaryIQTree(
                                        flatten2,
                                        IQ_FACTORY.createUnaryIQTree(
                                                flatten1,
                                                IQ_FACTORY.createUnaryIQTree(
                                                        filter2,
                                                        IQ_FACTORY.createUnaryIQTree(
                                                                flatten3,
                                                                IQ_FACTORY.createUnaryIQTree(
                                                                        flatten5,
                                                                        dataNode
                                                                ))))))));

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    private static void optimizeAndCompare(IQ query, IQ expectedQuery) {
        LOGGER.debug("\nBefore optimization: \n" +  query);
        LOGGER.debug("\nExpected: \n" +  expectedQuery);

        IQ optimizedIQ = FLATTEN_LIFTER.optimize(query);
        LOGGER.debug("\nAfter optimization: \n" +  optimizedIQ);

        assertEquals(expectedQuery, optimizedIQ);
    }
}
