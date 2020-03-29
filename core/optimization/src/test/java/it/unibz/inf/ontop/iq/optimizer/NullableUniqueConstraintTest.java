package it.unibz.inf.ontop.iq.optimizer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.NaryIQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.iq.node.FilterNode;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.atom.RelationPredicate;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import org.junit.Test;

import static it.unibz.inf.ontop.OptimizationTestingTools.*;
import static org.junit.Assert.assertEquals;

public class NullableUniqueConstraintTest {

    private static final RelationPredicate TABLE1_PREDICATE;
    private static final RelationPredicate TABLE2_PREDICATE;
    private final static AtomPredicate ANS1_ARITY_2_PREDICATE = ATOM_FACTORY.getRDFAnswerPredicate( 2);
    private final static AtomPredicate ANS1_ARITY_3_PREDICATE = ATOM_FACTORY.getRDFAnswerPredicate( 3);
    private final static AtomPredicate ANS1_ARITY_4_PREDICATE = ATOM_FACTORY.getRDFAnswerPredicate( 4);

    static {
        BasicDBMetadata dbMetadata = createDummyMetadata();
        QuotedIDFactory idFactory = dbMetadata.getQuotedIDFactory();

        DBTypeFactory dbTypeFactory = TYPE_FACTORY.getDBTypeFactory();
        DBTermType integerDBType = dbTypeFactory.getDBLargeIntegerType();

        /*
         * Table 1: non-composite unique constraint and regular field
         */
        DatabaseRelationDefinition table1Def = dbMetadata.createDatabaseRelation(idFactory.createRelationID(null, "TABLE1"));
        Attribute table1Col1 = table1Def.addAttribute(idFactory.createAttributeID("col1"), integerDBType.getName(), integerDBType, true);
        table1Def.addAttribute(idFactory.createAttributeID("col2"), integerDBType.getName(), integerDBType, true);
        table1Def.addAttribute(idFactory.createAttributeID("col3"), integerDBType.getName(), integerDBType, true);
        table1Def.addUniqueConstraint(UniqueConstraint.builder(table1Def, "uc1")
                .addDeterminant(table1Col1)
                .build());
        TABLE1_PREDICATE = table1Def.getAtomPredicate();

        /*
         * Table 2: non-composite unique constraint and regular field
         */
        DatabaseRelationDefinition table2Def = dbMetadata.createDatabaseRelation(idFactory.createRelationID(null, "TABLE2"));
        Attribute table2Col1 = table2Def.addAttribute(idFactory.createAttributeID("col1"), integerDBType.getName(), integerDBType, true);
        table2Def.addAttribute(idFactory.createAttributeID("col2"), integerDBType.getName(), integerDBType, true);
        table2Def.addAttribute(idFactory.createAttributeID("col3"), integerDBType.getName(), integerDBType, true);
        table2Def.addUniqueConstraint(UniqueConstraint.builder(table2Def, "uc2")
                .addDeterminant(table2Col1)
                .build());
        TABLE2_PREDICATE = table2Def.getAtomPredicate();

        dbMetadata.freeze();
    }

    @Test
    public void testJoinOnLeft1() throws EmptyQueryException {
        ExtensionalDataNode leftNode1 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, A, B, C));
        ExtensionalDataNode leftNode2 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, A, D, E));
        ExtensionalDataNode rightNode = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, A, TWO, G));

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_2_PREDICATE, A, G);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        NaryIQTree joinTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(leftNode1, leftNode2));

        UnaryIQTree initialTree = IQ_FACTORY.createUnaryIQTree(
                constructionNode,
                IQ_FACTORY.createBinaryNonCommutativeIQTree(
                        IQ_FACTORY.createLeftJoinNode(), joinTree,
                        rightNode));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, initialTree);

        ConstructionNode newConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(G, TERM_FACTORY.getIfElseNull(
                        TERM_FACTORY.getStrictEquality(B, TWO), C)));

        IQ expectedIQ = IQ_FACTORY.createIQ(
                projectionAtom,
                IQ_FACTORY.createUnaryIQTree(newConstructionNode, joinTree));

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testJoinOnLeft2() throws EmptyQueryException {
        ExtensionalDataNode leftNode1 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, A, B, C));
        ExtensionalDataNode leftNode2 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, A, D, E));
        ExtensionalDataNode rightNode = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, A, TWO, G));

        ImmutableFunctionalTerm hDefinition = TERM_FACTORY.getIfElseNull(TERM_FACTORY.getDBIsNotNull(I), ONE);

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_3_PREDICATE, A, G, H);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(H, hDefinition));

        NaryIQTree joinTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(leftNode1, leftNode2));

        UnaryIQTree rightTree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(
                        ImmutableSet.of(A, G, I),
                        SUBSTITUTION_FACTORY.getSubstitution(I, TERM_FACTORY.getProvenanceSpecialConstant())),
                        rightNode);

        UnaryIQTree initialTree = IQ_FACTORY.createUnaryIQTree(
                constructionNode,
                IQ_FACTORY.createBinaryNonCommutativeIQTree(
                        IQ_FACTORY.createLeftJoinNode(), joinTree,
                        rightTree));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, initialTree);

        ImmutableExpression bEquality = TERM_FACTORY.getStrictEquality(B, TWO);

        ImmutableFunctionalTerm newHDefinition = TERM_FACTORY.getIfElseNull(bEquality, ONE);

        ConstructionNode newConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(
                        G, TERM_FACTORY.getIfElseNull(bEquality, C),
                        H, newHDefinition));

        IQ expectedIQ = IQ_FACTORY.createIQ(
                projectionAtom,
                IQ_FACTORY.createUnaryIQTree(newConstructionNode, joinTree));

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testNotSimplified1() throws EmptyQueryException {
        ExtensionalDataNode leftNode1 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, A, B, C));
        ExtensionalDataNode rightNode = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, A, TWO, G));

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_2_PREDICATE, A, G);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        UnaryIQTree initialTree = IQ_FACTORY.createUnaryIQTree(
                constructionNode,
                IQ_FACTORY.createBinaryNonCommutativeIQTree(
                        IQ_FACTORY.createLeftJoinNode(),
                        leftNode1,
                        rightNode));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, initialTree);

        optimizeAndCompare(initialIQ, initialIQ);
    }

    @Test
    public void testFilterAbove1() throws EmptyQueryException {
        ExtensionalDataNode leftNode1 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, A, B, C));
        ExtensionalDataNode rightNode = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, A, TWO, G));

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_2_PREDICATE, A, G);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        FilterNode filterNode = IQ_FACTORY.createFilterNode(TERM_FACTORY.getDBIsNotNull(A));

        UnaryIQTree initialTree = IQ_FACTORY.createUnaryIQTree(
                constructionNode,
                IQ_FACTORY.createUnaryIQTree(
                        filterNode,
                        IQ_FACTORY.createBinaryNonCommutativeIQTree(
                                IQ_FACTORY.createLeftJoinNode(),
                                leftNode1,
                                rightNode)));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, initialTree);

        ConstructionNode newConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(G, TERM_FACTORY.getIfElseNull(
                        TERM_FACTORY.getStrictEquality(B, TWO), C)));

        IQ expectedIQ = IQ_FACTORY.createIQ(
                projectionAtom,
                IQ_FACTORY.createUnaryIQTree(
                        newConstructionNode,
                        IQ_FACTORY.createUnaryIQTree(
                                filterNode,
                                leftNode1)));

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testSimpleJoin1() throws EmptyQueryException {
        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, A, B, C));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, A, D, E));

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_2_PREDICATE, A, E);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        UnaryIQTree initialTree = IQ_FACTORY.createUnaryIQTree(
                constructionNode,
                IQ_FACTORY.createNaryIQTree(
                                IQ_FACTORY.createInnerJoinNode(),
                                ImmutableList.of(dataNode1, dataNode2)));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, initialTree);

        FilterNode newFilterNode = IQ_FACTORY.createFilterNode(TERM_FACTORY.getDBIsNotNull(A));

        IQ expectedIQ = IQ_FACTORY.createIQ(
                projectionAtom,
                IQ_FACTORY.createUnaryIQTree(
                        constructionNode,
                        IQ_FACTORY.createUnaryIQTree(
                                newFilterNode,
                                dataNode2)));

        optimizeAndCompare(initialIQ, expectedIQ);
    }


    private void optimizeAndCompare(IQ initialIQ, IQ expectedIQ) throws EmptyQueryException {
        IntermediateQuery newIntermediateQuery = JOIN_LIKE_OPTIMIZER.optimize(
                IQ_CONVERTER.convert(initialIQ.normalizeForOptimization(), EXECUTOR_REGISTRY));

        assertEquals(expectedIQ, IQ_CONVERTER.convert(newIntermediateQuery));
    }

}
