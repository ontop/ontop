package it.unibz.inf.ontop.iq.optimizer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.iq.node.FilterNode;
import it.unibz.inf.ontop.iq.node.ValuesNode;
import it.unibz.inf.ontop.model.template.Template;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.Substitution;
import org.junit.Ignore;
import org.junit.Test;

import static it.unibz.inf.ontop.NoDependencyTestDBMetadata.TABLE1_AR1;
import static it.unibz.inf.ontop.NoDependencyTestDBMetadata.TABLE1_AR2;
import static it.unibz.inf.ontop.OptimizationTestingTools.*;
import static it.unibz.inf.ontop.model.term.functionsymbol.InequalityLabel.LT;
import static it.unibz.inf.ontop.model.term.functionsymbol.InequalityLabel.LTE;
import static junit.framework.TestCase.assertTrue;

public class ValuesNodeTest {

    @Test
    public void test1normalization() {
        // Create initial node
        IQTree initialTree = IQ_FACTORY
                .createValuesNode(ImmutableList.of(X), ImmutableList.of(ImmutableList.of(ONE_STR)));

        // Create expected Tree
        IQTree expectedTree = IQ_FACTORY.createUnaryIQTree(IQ_FACTORY
                    .createConstructionNode(ImmutableSet.of(X), SUBSTITUTION_FACTORY.getSubstitution(X, ONE_STR)), IQ_FACTORY
                        .createTrueNode());

        assertTrue(baseTestNormalization(initialTree, expectedTree));
    }

    @Test
    public void test2normalization() {
        // Create initial node
        IQTree initialTree = IQ_FACTORY
                .createValuesNode(ImmutableList.of(X, Y), ImmutableList.of(ImmutableList.of(ONE_STR, TWO_STR), ImmutableList.of(ONE_STR, THREE_STR)));

        // Create expected Tree
        IQTree expectedTree = IQ_FACTORY.createUnaryIQTree(IQ_FACTORY
                    .createConstructionNode(ImmutableSet.of(X, Y), SUBSTITUTION_FACTORY.getSubstitution(X, ONE_STR)), IQ_FACTORY
                        .createValuesNode(ImmutableList.of(Y), ImmutableList.of(ImmutableList.of(TWO_STR), ImmutableList.of(THREE_STR))));

        assertTrue(baseTestNormalization(initialTree, expectedTree));
    }

    @Test
    public void test3normalization() {
        // Create initial node
        IQTree initialTree = IQ_FACTORY
                .createValuesNode(ImmutableList.of(X, Y), ImmutableList.of(ImmutableList.of(ONE_STR, TWO_STR), ImmutableList.of(ONE_STR, TWO_STR)));

        // Create expected Tree
        IQTree expectedTree = IQ_FACTORY.createUnaryIQTree(IQ_FACTORY
                .createConstructionNode(ImmutableSet.of(X, Y), SUBSTITUTION_FACTORY.getSubstitution(X, ONE_STR, Y, TWO_STR)), IQ_FACTORY
                    .createValuesNode(ImmutableList.of(), ImmutableList.of(ImmutableList.of(), ImmutableList.of()))
        );

        assertTrue(baseTestNormalization(initialTree, expectedTree));
    }

    @Test
    public void test4normalization() {
        // Create initial node
        IQTree initialTree = IQ_FACTORY
                .createValuesNode(ImmutableList.of(), ImmutableList.of(ImmutableList.of()));

        // Create expected Tree
        IQTree expectedTree = IQ_FACTORY
                .createTrueNode();

        assertTrue(baseTestNormalization(initialTree, expectedTree));
    }

    @Test
    public void test5normalization() {
        // Create initial node
        IQTree initialTree = IQ_FACTORY
                .createValuesNode(ImmutableList.of(X, Y), ImmutableList.of(ImmutableList.of(ONE_STR, TWO_STR), ImmutableList.of(THREE_STR, FOUR_STR)));

        // Create expected Tree
        IQTree expectedTree = IQ_FACTORY
                .createValuesNode(ImmutableList.of(X, Y), ImmutableList.of(ImmutableList.of(ONE_STR, TWO_STR), ImmutableList.of(THREE_STR, FOUR_STR)));

        assertTrue(baseTestNormalization(initialTree, expectedTree));
    }

    @Test
    public void test6substitutionNoChange() {
        IQTree initialTree = IQ_FACTORY
                .createValuesNode(ImmutableList.of(X), ImmutableList.of(ImmutableList.of(ONE_STR), ImmutableList.of(TWO_STR)));
        Substitution<VariableOrGroundTerm> substitution = SUBSTITUTION_FACTORY.getSubstitution(Y, ONE_STR);
        IQTree expectedTree = IQ_FACTORY
                .createValuesNode(ImmutableList.of(X), ImmutableList.of(ImmutableList.of(ONE_STR), ImmutableList.of(TWO_STR)));

        assertTrue(baseTestApplyDescSubstitution(initialTree, substitution, expectedTree));
    }

    @Test
    public void test7substitutionConstant() {
        IQTree initialTree = IQ_FACTORY
                .createValuesNode(ImmutableList.of(X, Y), ImmutableList.of(
                        ImmutableList.of(ONE_STR, TWO_STR),
                        ImmutableList.of(ONE_STR, THREE_STR),
                        ImmutableList.of(FOUR_STR, FIVE_STR)));
        Substitution<VariableOrGroundTerm> substitution = SUBSTITUTION_FACTORY.getSubstitution(X, ONE_STR);
        IQTree expectedTree = IQ_FACTORY
                .createValuesNode(ImmutableList.of(Y), ImmutableList.of(
                        ImmutableList.of(TWO_STR),
                        ImmutableList.of(THREE_STR)));

        assertTrue(baseTestApplyDescSubstitution(initialTree, substitution, expectedTree));
    }

    @Test
    public void test8substitutionFunction() {
        // Test handling of GroundFunctionalTerm
        IQTree initialTree = IQ_FACTORY
                .createValuesNode(ImmutableList.of(X), ImmutableList.of(ImmutableList.of(ONE_STR), ImmutableList.of(TWO_STR)));
        GroundFunctionalTerm groundFunctionalTerm = (GroundFunctionalTerm) TERM_FACTORY.getDBContains(ImmutableList.of(THREE_STR, FOUR_STR));
        Substitution<VariableOrGroundTerm> substitution = SUBSTITUTION_FACTORY.getSubstitution(X, groundFunctionalTerm);

        IQTree expectedTree = IQ_FACTORY.createUnaryIQTree(IQ_FACTORY
                .createConstructionNode(ImmutableSet.of()),IQ_FACTORY.createUnaryIQTree(IQ_FACTORY
                    .createFilterNode(TERM_FACTORY.getStrictEquality(XF0, groundFunctionalTerm)), IQ_FACTORY
                        .createValuesNode(ImmutableList.of(XF0), ImmutableList.of(ImmutableList.of(ONE_STR), ImmutableList.of(TWO_STR)))));

        assertTrue(baseTestApplyDescSubstitution(initialTree, substitution, expectedTree));
    }

    @Test
    public void test9substitutionVariable() {
        // Test handling of GroundFunctionalTerm
        IQTree initialTree = IQ_FACTORY
                .createValuesNode(ImmutableList.of(X, Y, Z), ImmutableList.of(
                        ImmutableList.of(ONE_STR, TWO_STR, THREE_STR),
                        ImmutableList.of(TWO_STR, TWO_STR, FOUR_STR)));
        Substitution<VariableOrGroundTerm> substitution = SUBSTITUTION_FACTORY.getSubstitution(X, Y, Z, W);

        IQTree expectedTree = IQ_FACTORY
                .createValuesNode(ImmutableList.of(Y, W), ImmutableList.of(ImmutableList.of(TWO_STR, FOUR_STR)));

        assertTrue(baseTestApplyDescSubstitution(initialTree, substitution, expectedTree));
    }

    @Test
    public void test10trivialSubstitutionVariable() {
        // Test handling of GroundFunctionalTerm
        IQTree initialTree = IQ_FACTORY
                .createValuesNode(ImmutableList.of(X), ImmutableList.of(
                        ImmutableList.of(ONE_STR)));
        Substitution<VariableOrGroundTerm> substitution = SUBSTITUTION_FACTORY.getSubstitution(X, Y);

        IQTree expectedTree = IQ_FACTORY
                .createValuesNode(ImmutableList.of(Y), ImmutableList.of(
                        ImmutableList.of(ONE_STR)));

        assertTrue(baseTestApplyDescSubstitution(initialTree, substitution, expectedTree));
    }

    @Test
    public void test11substitutionTriple() {
        // Test handling of GroundFunctionalTerm & NonFunctionalTerm
        IQTree initialTree = IQ_FACTORY
                .createValuesNode(ImmutableList.of(X, Y, Z, W), ImmutableList.of(
                        ImmutableList.of(ONE_STR, TWO_STR, TWO_STR, TWO_STR),
                        ImmutableList.of(TWO_STR, TWO_STR, TWO_STR, ONE_STR),
                        ImmutableList.of(ONE_STR, TWO_STR, THREE_STR, FOUR_STR)));
        GroundFunctionalTerm groundFunctionalTerm = (GroundFunctionalTerm) TERM_FACTORY.getDBContains(ImmutableList.of(THREE_STR, FOUR_STR));
        Substitution<VariableOrGroundTerm> substitution = SUBSTITUTION_FACTORY.getSubstitution(X, groundFunctionalTerm, Y, Z, W, ONE_STR);

        IQTree expectedTree = IQ_FACTORY.createUnaryIQTree(IQ_FACTORY
                .createConstructionNode(ImmutableSet.of(Z)),IQ_FACTORY.createUnaryIQTree(IQ_FACTORY
                    .createFilterNode(TERM_FACTORY.getStrictEquality(XF0, groundFunctionalTerm)), IQ_FACTORY
                        .createValuesNode(ImmutableList.of(XF0, Z), ImmutableList.of(
                                ImmutableList.of(TWO_STR, TWO_STR)))));

        assertTrue(baseTestApplyDescSubstitution(initialTree, substitution, expectedTree));
    }

    @Test
    public void test12propagateDownConstraint() {
        IQTree initialTree = IQ_FACTORY.createUnaryIQTree(IQ_FACTORY
                .createFilterNode(TERM_FACTORY.getDBNumericInequality(LT, X, TERM_FACTORY.getDBIntegerConstant(2))), IQ_FACTORY
                    .createValuesNode(ImmutableList.of(X, Y), ImmutableList.of(ImmutableList.of(ONE, TWO), ImmutableList.of(TWO, ONE), ImmutableList.of(TWO, TWO))));

        IQTree expectedTree = IQ_FACTORY.createUnaryIQTree(IQ_FACTORY
                .createFilterNode(TERM_FACTORY.getDBNumericInequality(LT, X, TERM_FACTORY.getDBIntegerConstant(2))), IQ_FACTORY
                    .createValuesNode(ImmutableList.of(X, Y), ImmutableList.of(ImmutableList.of(ONE, TWO))));

        assertTrue(baseTestPropagateDownConstraints(initialTree, expectedTree));
    }

    @Test
    public void testJoinIRITemplateString1() {

        ExtensionalDataNode dataNode = IQ_FACTORY.createExtensionalDataNode(TABLE1_AR1, ImmutableMap.of(0, A));

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, TERM_FACTORY.getIRIFunctionalTerm(
                                Template.builder()
                                        .addSeparator("http://localhost/thing/")
                                        .addColumn()
                                        .build(),
                                ImmutableList.of(A)
                        ).getTerm(0)));

        ValuesNode valuesNode = IQ_FACTORY.createValuesNode(
                ImmutableList.of(X),
                ImmutableList.of(
                        ImmutableList.of(TERM_FACTORY.getDBStringConstant("http://localhost/thing/1")),
                        ImmutableList.of(TERM_FACTORY.getDBStringConstant("http://localhost/thing/2"))));

        IQTree initialTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(valuesNode, IQ_FACTORY.createUnaryIQTree(constructionNode, dataNode)));

        ValuesNode newValuesNode = IQ_FACTORY.createValuesNode(
                ImmutableList.of(A),
                ImmutableList.of(
                        ImmutableList.of(TERM_FACTORY.getDBStringConstant("1")),
                        ImmutableList.of(TERM_FACTORY.getDBStringConstant("2"))));

        IQTree expectedTree = IQ_FACTORY.createUnaryIQTree(
                constructionNode,
                IQ_FACTORY.createNaryIQTree(
                        IQ_FACTORY.createInnerJoinNode(),
                        ImmutableList.of(newValuesNode, dataNode)));

        assertTrue(baseTestNormalization(initialTree, expectedTree));
    }

    @Test
    public void testJoinIRITemplateString2() {

        ExtensionalDataNode dataNode = IQ_FACTORY.createExtensionalDataNode(TABLE1_AR1, ImmutableMap.of(0, A));

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, TERM_FACTORY.getIRIFunctionalTerm(
                                Template.builder()
                                        .addSeparator("http://localhost/thing/")
                                        .addColumn()
                                        .build(),
                                ImmutableList.of(A)
                        ).getTerm(0)));

        ValuesNode valuesNode = IQ_FACTORY.createValuesNode(
                ImmutableList.of(X),
                ImmutableList.of(
                        ImmutableList.of(TERM_FACTORY.getDBStringConstant("http://localhost/thing/1")),
                        ImmutableList.of(TERM_FACTORY.getDBStringConstant("http://localhost/thing/2")),
                        ImmutableList.of(TERM_FACTORY.getDBStringConstant("http://localhost/somethingelse"))));

        IQTree initialTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(valuesNode, IQ_FACTORY.createUnaryIQTree(constructionNode, dataNode)));

        ValuesNode newValuesNode = IQ_FACTORY.createValuesNode(
                ImmutableList.of(A),
                ImmutableList.of(
                        ImmutableList.of(TERM_FACTORY.getDBStringConstant("1")),
                        ImmutableList.of(TERM_FACTORY.getDBStringConstant("2"))));

        IQTree expectedTree = IQ_FACTORY.createUnaryIQTree(
                constructionNode,
                IQ_FACTORY.createNaryIQTree(
                        IQ_FACTORY.createInnerJoinNode(),
                        ImmutableList.of(newValuesNode, dataNode)));

        assertTrue(baseTestNormalization(initialTree, expectedTree));
    }

    @Test
    public void testJoinIRITemplateString3() {

        ExtensionalDataNode dataNode = IQ_FACTORY.createExtensionalDataNode(TABLE1_AR1, ImmutableMap.of(0, A));

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, TERM_FACTORY.getIRIFunctionalTerm(
                                Template.builder()
                                        .addSeparator("http://localhost/thing/")
                                        .addColumn()
                                        .build(),
                                ImmutableList.of(A)
                        ).getTerm(0)));

        ValuesNode valuesNode = IQ_FACTORY.createValuesNode(
                ImmutableList.of(X),
                ImmutableList.of(
                        ImmutableList.of(TERM_FACTORY.getDBStringConstant("http://localhost/somethingelse"))));

        IQTree initialTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(valuesNode, IQ_FACTORY.createUnaryIQTree(constructionNode, dataNode)));

        IQTree expectedTree = IQ_FACTORY.createEmptyNode(initialTree.getVariables());

        assertTrue(baseTestNormalization(initialTree, expectedTree));
    }

    @Test
    public void testJoinIRITemplateString4() {

        ExtensionalDataNode dataNode = IQ_FACTORY.createExtensionalDataNode(TABLE1_AR2, ImmutableMap.of(0, A, 1, B));

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, TERM_FACTORY.getIRIFunctionalTerm(
                                Template.builder()
                                        .addSeparator("http://localhost/thing/")
                                        .addColumn()
                                        .addSeparator("/")
                                        .addColumn()
                                        .build(),
                                ImmutableList.of(A, B)
                        ).getTerm(0)));

        ValuesNode valuesNode = IQ_FACTORY.createValuesNode(
                ImmutableList.of(X),
                ImmutableList.of(
                        ImmutableList.of(TERM_FACTORY.getDBStringConstant("http://localhost/thing/1/3")),
                        ImmutableList.of(TERM_FACTORY.getDBStringConstant("http://localhost/thing/2/4")),
                        ImmutableList.of(TERM_FACTORY.getDBStringConstant("http://localhost/somethingelse"))));

        IQTree initialTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(valuesNode, IQ_FACTORY.createUnaryIQTree(constructionNode, dataNode)));

        ValuesNode newValuesNode = IQ_FACTORY.createValuesNode(
                ImmutableList.of(A, B),
                ImmutableList.of(
                        ImmutableList.of(TERM_FACTORY.getDBStringConstant("1"), TERM_FACTORY.getDBStringConstant("3")),
                        ImmutableList.of(TERM_FACTORY.getDBStringConstant("2"), TERM_FACTORY.getDBStringConstant("4"))));

        IQTree expectedTree = IQ_FACTORY.createUnaryIQTree(
                constructionNode,
                IQ_FACTORY.createNaryIQTree(
                        IQ_FACTORY.createInnerJoinNode(),
                        ImmutableList.of(newValuesNode, dataNode)));

        assertTrue(baseTestNormalization(initialTree, expectedTree));
    }

    /**
     * Non-injective
     */
    @Test
    public void testJoinIRITemplateString5() {

        ExtensionalDataNode dataNode = IQ_FACTORY.createExtensionalDataNode(TABLE1_AR2, ImmutableMap.of(0, A, 1, B));

        ImmutableTerm functionalTerm = TERM_FACTORY.getIRIFunctionalTerm(
                Template.builder()
                        .addSeparator("http://localhost/thing/")
                        .addColumn()
                        // Non-injective!!
                        .addColumn()
                        .build(),
                ImmutableList.of(A, B)).getTerm(0);

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, functionalTerm));

        ValuesNode valuesNode = IQ_FACTORY.createValuesNode(
                ImmutableList.of(X),
                ImmutableList.of(
                        ImmutableList.of(TERM_FACTORY.getDBStringConstant("http://localhost/thing/13")),
                        ImmutableList.of(TERM_FACTORY.getDBStringConstant("http://localhost/thing/24")),
                        ImmutableList.of(TERM_FACTORY.getDBStringConstant("http://localhost/somethingelse"))));

        IQTree initialTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(valuesNode, IQ_FACTORY.createUnaryIQTree(constructionNode, dataNode)));

        ValuesNode newValuesNode = IQ_FACTORY.createValuesNode(
                ImmutableList.of(XF0),
                ImmutableList.of(
                        ImmutableList.of(TERM_FACTORY.getDBStringConstant("http://localhost/thing/13")),
                        ImmutableList.of(TERM_FACTORY.getDBStringConstant("http://localhost/thing/24"))));

        IQTree expectedTree = IQ_FACTORY.createUnaryIQTree(constructionNode,
                IQ_FACTORY.createNaryIQTree(
                        IQ_FACTORY.createInnerJoinNode(TERM_FACTORY.getStrictEquality(functionalTerm, XF0)),
                        ImmutableList.of(newValuesNode, dataNode)));

        assertTrue(baseTestNormalization(initialTree, expectedTree));
    }

    @Test
    public void testJoinIRITemplateString6() {

        ExtensionalDataNode dataNode = IQ_FACTORY.createExtensionalDataNode(TABLE1_AR2, ImmutableMap.of(0, A, 1, B));

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, TERM_FACTORY.getIRIFunctionalTerm(
                                Template.builder()
                                        .addSeparator("http://localhost/thing/")
                                        .addColumn()
                                        .addSeparator("/")
                                        .addColumn()
                                        .build(),
                                ImmutableList.of(A, B)
                        ).getTerm(0)));

        ValuesNode valuesNode = IQ_FACTORY.createValuesNode(
                ImmutableList.of(X),
                ImmutableList.of(
                        ImmutableList.of(TERM_FACTORY.getDBStringConstant("http://localhost/thing/1/3")),
                        ImmutableList.of(TERM_FACTORY.getDBStringConstant("http://localhost/thing/2/4")),
                        ImmutableList.of(TERM_FACTORY.getNullConstant()),
                        ImmutableList.of(TERM_FACTORY.getDBStringConstant("http://localhost/somethingelse"))));

        IQTree initialTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(valuesNode, IQ_FACTORY.createUnaryIQTree(constructionNode, dataNode)));

        ValuesNode newValuesNode = IQ_FACTORY.createValuesNode(
                ImmutableList.of(A, B),
                ImmutableList.of(
                        ImmutableList.of(TERM_FACTORY.getDBStringConstant("1"), TERM_FACTORY.getDBStringConstant("3")),
                        ImmutableList.of(TERM_FACTORY.getDBStringConstant("2"), TERM_FACTORY.getDBStringConstant("4"))));

        IQTree expectedTree = IQ_FACTORY.createUnaryIQTree(
                constructionNode,
                IQ_FACTORY.createNaryIQTree(
                        IQ_FACTORY.createInnerJoinNode(),
                        ImmutableList.of(newValuesNode, dataNode)));

        assertTrue(baseTestNormalization(initialTree, expectedTree));
    }

    @Test
    public void testJoinIRITemplateString7() {

        ExtensionalDataNode dataNode = IQ_FACTORY.createExtensionalDataNode(TABLE1_AR2, ImmutableMap.of(0, A, 1, B));

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, TERM_FACTORY.getIRIFunctionalTerm(
                                Template.builder()
                                        .addSeparator("http://localhost/thing/")
                                        .addColumn()
                                        .addSeparator("/")
                                        .addColumn()
                                        .build(),
                                ImmutableList.of(TERM_FACTORY.getDBCastFunctionalTerm(
                                            TYPE_FACTORY.getDBTypeFactory().getDBLargeIntegerType(),
                                            TYPE_FACTORY.getDBTypeFactory().getDBStringType(),
                                            A),
                                        B)
                        ).getTerm(0)));

        ValuesNode valuesNode = IQ_FACTORY.createValuesNode(
                ImmutableList.of(X),
                ImmutableList.of(
                        ImmutableList.of(TERM_FACTORY.getDBStringConstant("http://localhost/thing/1/3")),
                        ImmutableList.of(TERM_FACTORY.getDBStringConstant("http://localhost/thing/2/4")),
                        ImmutableList.of(TERM_FACTORY.getDBStringConstant("http://localhost/thing/notANumber/5")),
                        ImmutableList.of(TERM_FACTORY.getNullConstant()),
                        ImmutableList.of(TERM_FACTORY.getDBStringConstant("http://localhost/somethingelse"))));

        IQTree initialTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(valuesNode, IQ_FACTORY.createUnaryIQTree(constructionNode, dataNode)));

        ValuesNode newValuesNode = IQ_FACTORY.createValuesNode(
                ImmutableList.of(B, A),
                ImmutableList.of(
                        ImmutableList.of(TERM_FACTORY.getDBStringConstant("3"), TERM_FACTORY.getDBIntegerConstant(1)),
                        ImmutableList.of(TERM_FACTORY.getDBStringConstant("4"), TERM_FACTORY.getDBIntegerConstant(2))));

        IQTree expectedTree = IQ_FACTORY.createUnaryIQTree(
                constructionNode,
                IQ_FACTORY.createNaryIQTree(
                        IQ_FACTORY.createInnerJoinNode(),
                        ImmutableList.of(newValuesNode, dataNode)));

        assertTrue(baseTestNormalization(initialTree, expectedTree));
    }

    @Test
    public void testJoinIRITemplateString8() {

        ExtensionalDataNode dataNode = IQ_FACTORY.createExtensionalDataNode(TABLE1_AR2, ImmutableMap.of(0, A, 1, B));

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, TERM_FACTORY.getIRIFunctionalTerm(
                                Template.builder()
                                        .addSeparator("http://localhost/thing/")
                                        .addColumn()
                                        .addSeparator("/")
                                        .addColumn()
                                        .build(),
                                ImmutableList.of(A, B)
                        ).getTerm(0)));

        ValuesNode valuesNode = IQ_FACTORY.createValuesNode(
                ImmutableList.of(X),
                ImmutableList.of(
                        ImmutableList.of(TERM_FACTORY.getDBStringConstant("http://localhost/thing/1%2F/3")),
                        ImmutableList.of(TERM_FACTORY.getDBStringConstant("http://localhost/thing/2/4")),
                        ImmutableList.of(TERM_FACTORY.getNullConstant()),
                        ImmutableList.of(TERM_FACTORY.getDBStringConstant("http://localhost/somethingelse"))));

        IQTree initialTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(valuesNode, IQ_FACTORY.createUnaryIQTree(constructionNode, dataNode)));

        ValuesNode newValuesNode = IQ_FACTORY.createValuesNode(
                ImmutableList.of(A, B),
                ImmutableList.of(
                        ImmutableList.of(TERM_FACTORY.getDBStringConstant("1/"), TERM_FACTORY.getDBStringConstant("3")),
                        ImmutableList.of(TERM_FACTORY.getDBStringConstant("2"), TERM_FACTORY.getDBStringConstant("4"))));

        IQTree expectedTree = IQ_FACTORY.createUnaryIQTree(
                constructionNode,
                IQ_FACTORY.createNaryIQTree(
                        IQ_FACTORY.createInnerJoinNode(),
                        ImmutableList.of(newValuesNode, dataNode)));

        assertTrue(baseTestNormalization(initialTree, expectedTree));
    }

    @Test
    public void testJoinIRITemplateString9() {

        ExtensionalDataNode dataNode = IQ_FACTORY.createExtensionalDataNode(TABLE1_AR2, ImmutableMap.of(0, A, 1, B));

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Y),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, TERM_FACTORY.getIRIFunctionalTerm(
                                Template.builder()
                                        .addSeparator("http://localhost/thing/")
                                        .addColumn()
                                        .addSeparator("/")
                                        .addColumn()
                                        .build(),
                                ImmutableList.of(A, B)
                        ).getTerm(0),
                Y, TERM_FACTORY.getIRIFunctionalTerm(
                        Template.builder()
                                .addSeparator("http://localhost/other/")
                                .addColumn()
                                .build(),
                        ImmutableList.of(B)
                        ).getTerm(0)));

        DBConstant zero = TERM_FACTORY.getDBIntegerConstant(0);

        ValuesNode valuesNode = IQ_FACTORY.createValuesNode(
                ImmutableList.of(X,Y,Z),
                ImmutableList.of(
                        ImmutableList.of(TERM_FACTORY.getDBStringConstant("http://localhost/thing/1%2F/3"),
                                TERM_FACTORY.getDBStringConstant("http://localhost/other/3"),
                                zero),
                        ImmutableList.of(TERM_FACTORY.getDBStringConstant("http://localhost/thing/2/4"),
                                TERM_FACTORY.getDBStringConstant("http://localhost/other/4"),
                                ONE),
                        ImmutableList.of(TERM_FACTORY.getNullConstant(),
                                TERM_FACTORY.getDBStringConstant("http://localhost/other/4"),
                                ONE),
                        ImmutableList.of(TERM_FACTORY.getDBStringConstant("http://localhost/somethingelse"),
                                TERM_FACTORY.getDBStringConstant("http://localhost/other/5"),
                                ONE),
                ImmutableList.of(TERM_FACTORY.getDBStringConstant("http://localhost/thing/1%2F/3"),
                        TERM_FACTORY.getDBStringConstant("http://localhost/other/11"),
                        ONE),
                ImmutableList.of(TERM_FACTORY.getDBStringConstant("http://localhost/thing/1%2F/3"),
                        TERM_FACTORY.getDBStringConstant("http://localhost/other/3"),
                        TWO)));

        ImmutableExpression condition = TERM_FACTORY.getDBNumericInequality(LTE, Z, ONE);

        IQTree initialTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(condition),
                ImmutableList.of(valuesNode, IQ_FACTORY.createUnaryIQTree(constructionNode, dataNode)));

        ValuesNode newValuesNode = IQ_FACTORY.createValuesNode(
                ImmutableList.of(Z, A, B),
                ImmutableList.of(
                        ImmutableList.of(zero, TERM_FACTORY.getDBStringConstant("1/"), TERM_FACTORY.getDBStringConstant("3")),
                        ImmutableList.of(ONE, TERM_FACTORY.getDBStringConstant("2"), TERM_FACTORY.getDBStringConstant("4"))));

        IQTree expectedTree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(initialTree.getVariables(), constructionNode.getSubstitution()),
                IQ_FACTORY.createNaryIQTree(
                        IQ_FACTORY.createInnerJoinNode(condition),
                        ImmutableList.of(newValuesNode, dataNode)));

        assertTrue(baseTestNormalization(initialTree, expectedTree));
    }

    @Ignore("TODO: use type-specific casting function in the mockup factory")
    @Test
    public void testJoinIRITemplateString10() {

        ExtensionalDataNode dataNode = IQ_FACTORY.createExtensionalDataNode(TABLE1_AR2, ImmutableMap.of(0, A, 1, B));

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, TERM_FACTORY.getIRIFunctionalTerm(
                                Template.builder()
                                        .addSeparator("http://localhost/thing/")
                                        .addColumn()
                                        .addSeparator("/")
                                        .addColumn()
                                        .build(),
                                ImmutableList.of(TERM_FACTORY.getDBCastFunctionalTerm(
                                                TYPE_FACTORY.getDBTypeFactory().getDBLargeIntegerType(),
                                                TYPE_FACTORY.getDBTypeFactory().getDBStringType(),
                                                A),
                                        B)
                        ).getTerm(0)));

        ValuesNode valuesNode = IQ_FACTORY.createValuesNode(
                ImmutableList.of(X),
                ImmutableList.of(
                        ImmutableList.of(TERM_FACTORY.getDBStringConstant("http://localhost/thing/%2B1/3")),
                        ImmutableList.of(TERM_FACTORY.getDBStringConstant("http://localhost/thing/2/4")),
                        ImmutableList.of(TERM_FACTORY.getDBStringConstant("http://localhost/thing/5/6")),
                        ImmutableList.of(TERM_FACTORY.getDBStringConstant("http://localhost/thing/notANumber/5")),
                        ImmutableList.of(TERM_FACTORY.getNullConstant()),
                        ImmutableList.of(TERM_FACTORY.getDBStringConstant("http://localhost/somethingelse"))));

        IQTree initialTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(valuesNode, IQ_FACTORY.createUnaryIQTree(constructionNode, dataNode)));

        ValuesNode newValuesNode = IQ_FACTORY.createValuesNode(
                ImmutableList.of(B, A),
                ImmutableList.of(
                        ImmutableList.of(TERM_FACTORY.getDBStringConstant("4"), TERM_FACTORY.getDBIntegerConstant(2)),
                        ImmutableList.of(TERM_FACTORY.getDBStringConstant("6"), TERM_FACTORY.getDBIntegerConstant(5))));

        IQTree expectedTree = IQ_FACTORY.createUnaryIQTree(
                constructionNode,
                IQ_FACTORY.createNaryIQTree(
                        IQ_FACTORY.createInnerJoinNode(),
                        ImmutableList.of(newValuesNode, dataNode)));

        assertTrue(baseTestNormalization(initialTree, expectedTree));
    }

    private Boolean baseTestNormalization(IQTree initialTree, IQTree expectedTree) {
        System.out.println('\n' + "Tree before normalizing:");
        System.out.println(initialTree);
        System.out.println('\n' + "Expected tree:");
        System.out.println(expectedTree);
        IQTree normalizedTree = initialTree.normalizeForOptimization(CORE_UTILS_FACTORY
                .createVariableGenerator(initialTree.getVariables()));
        System.out.println('\n' + "Normalized tree:");
        System.out.println(normalizedTree);
        return normalizedTree.equals(expectedTree);
    }

    private Boolean baseTestApplyDescSubstitution(IQTree initialTree,
                                                  Substitution<VariableOrGroundTerm> substitution,
                                                  IQTree expectedTree) {
        System.out.println('\n' + "Tree before applying descending substitution without optimizing:");
        System.out.println(initialTree);
        System.out.println('\n' + "Substitution:");
        System.out.println(substitution);
        System.out.println('\n' + "Expected tree:");
        System.out.println(expectedTree);
        IQTree resultingTree = initialTree.applyDescendingSubstitutionWithoutOptimizing(substitution,
                CORE_UTILS_FACTORY.createVariableGenerator(initialTree.getKnownVariables()));
        System.out.println('\n' + "Resulting tree:");
        System.out.println(resultingTree);
        return resultingTree.equals(expectedTree);
    }

    private Boolean baseTestPropagateDownConstraints(IQTree initialTree,
                                                     IQTree expectedTree) {
        System.out.println('\n' + "Tree before propagating down constraint:");
        System.out.println(initialTree);
        System.out.println('\n' + "Expected tree:");
        System.out.println(expectedTree);
        IQTree resultingTree = initialTree.propagateDownConstraint(
                ((FilterNode) initialTree.getRootNode()).getFilterCondition(),
                CORE_UTILS_FACTORY.createVariableGenerator(initialTree.getKnownVariables()));
        System.out.println('\n' + "Resulting tree:");
        System.out.println(resultingTree);
        return resultingTree.equals(expectedTree);
    }
}
