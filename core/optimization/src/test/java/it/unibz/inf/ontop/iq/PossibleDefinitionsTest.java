package it.unibz.inf.ontop.iq;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.template.Template;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.substitution.Substitution;
import org.junit.Test;

import static it.unibz.inf.ontop.NoDependencyTestDBMetadata.*;
import static it.unibz.inf.ontop.OptimizationTestingTools.*;
import static org.junit.Assert.assertEquals;

public class PossibleDefinitionsTest {

    private final ImmutableList<Template.Component> template1 = Template.builder()
            .addSeparator("http://example.org/type1/").addColumn()
            .build();

    private final ImmutableList<Template.Component> template2 = Template.builder()
            .addSeparator("http://example.org/type2/").addColumn()
            .build();

    @Test
    public void testJoinPartialUnfolding1() {
        var dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1_AR1, ImmutableMap.of(0, A));

        var constructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, TERM_FACTORY.getRDFFunctionalTerm(A, TERM_FACTORY.getRDFTermTypeConstant(TYPE_FACTORY.getIRITermType()))));

        var constructionTree = IQ_FACTORY.createUnaryIQTree(
                constructionNode,
                dataNode1);

        var tripleNode = IQ_FACTORY.createIntensionalDataNode(ATOM_FACTORY.getIntensionalTripleAtom(X, Y, Z));

        var joinTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(constructionTree, tripleNode));

        checkVariableDefinitions(joinTree, ImmutableSet.of(constructionNode.getSubstitution()));
    }

    @Test
    public void testUnionPartialUnfolding1() {
        var dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1_AR1, ImmutableMap.of(0, A));

        var projectionVariables = ImmutableSet.of(X);

        var constructionNode1 = IQ_FACTORY.createConstructionNode(projectionVariables,
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, TERM_FACTORY.getRDFFunctionalTerm(A, TERM_FACTORY.getRDFTermTypeConstant(TYPE_FACTORY.getIRITermType()))));

        var child1 = IQ_FACTORY.createUnaryIQTree(
                constructionNode1,
                dataNode1);

        var tripleNode = IQ_FACTORY.createIntensionalDataNode(ATOM_FACTORY.getIntensionalTripleAtom(X, Y, Z));

        var child2 = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(projectionVariables),
                tripleNode);

        var unionTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createUnionNode(projectionVariables),
                ImmutableList.of(child1, child2));

        checkVariableDefinitions(unionTree,
                ImmutableSet.of(
                        SUBSTITUTION_FACTORY.getSubstitution(),
                        constructionNode1.getSubstitution()));
    }

    @Test
    public void testUnion1() {
        var dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1_AR1, ImmutableMap.of(0, A));
        var dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE2_AR1, ImmutableMap.of(0, B));

        var projectionVariables = ImmutableSet.of(X);

        var constructionNode1 = IQ_FACTORY.createConstructionNode(projectionVariables,
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, TERM_FACTORY.getIRIFunctionalTerm(template1, ImmutableList.of(A))));

        var child1 = IQ_FACTORY.createUnaryIQTree(
                constructionNode1,
                dataNode1);

        var constructionNode2 = IQ_FACTORY.createConstructionNode(projectionVariables,
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, TERM_FACTORY.getIRIFunctionalTerm(template2, ImmutableList.of(B))));

        var child2 = IQ_FACTORY.createUnaryIQTree(
                constructionNode2,
                dataNode2);

        var unionTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createUnionNode(projectionVariables),
                ImmutableList.of(child1, child2));

        checkVariableDefinitions(unionTree,
                ImmutableSet.of(
                        constructionNode1.getSubstitution(),
                        constructionNode2.getSubstitution()));
    }

    @Test
    public void testJoin1() {
        var dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1_AR2, ImmutableMap.of(0, A, 1, D));
        var dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE2_AR2, ImmutableMap.of(0, B, 1, C));

        var xDef1 = TERM_FACTORY.getIRIFunctionalTerm(template1, ImmutableList.of(A));
        var wDef = TERM_FACTORY.getRDFLiteralFunctionalTerm(D, TYPE_FACTORY.getXsdStringDatatype());
        var zDef = TERM_FACTORY.getRDFLiteralFunctionalTerm(C, TYPE_FACTORY.getXsdStringDatatype());

        var constructionNode1 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, W),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, xDef1,
                        W, wDef));

        var child1 = IQ_FACTORY.createUnaryIQTree(
                constructionNode1,
                dataNode1);

        var constructionNode2 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Z),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, TERM_FACTORY.getIRIFunctionalTerm(template1, ImmutableList.of(B)),
                        Z, zDef));

        var child2 = IQ_FACTORY.createUnaryIQTree(
                constructionNode2,
                dataNode2);

        var unionTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(child1, child2));

        checkVariableDefinitions(unionTree,
                ImmutableSet.of(
                        SUBSTITUTION_FACTORY.getSubstitution(X, xDef1, W, wDef, Z, zDef)));
    }

    private void checkVariableDefinitions(IQTree tree, ImmutableSet<Substitution<ImmutableTerm>> expectedSubstitutions) {
        assertEquals(expectedSubstitutions, tree.getPossibleVariableDefinitions());
        assertEquals(expectedSubstitutions, tree
                        .normalizeForOptimization(CORE_UTILS_FACTORY.createVariableGenerator(tree.getKnownVariables()))
                        .getPossibleVariableDefinitions());
    }


}
