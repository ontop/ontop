package it.unibz.inf.ontop.iq.optimizer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.OptimizationTestingTools;
import it.unibz.inf.ontop.dbschema.NamedRelationDefinition;
import it.unibz.inf.ontop.dbschema.UniqueConstraint;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.model.template.Template;
import org.junit.Test;

import static it.unibz.inf.ontop.DependencyTestDBMetadata.*;
import static it.unibz.inf.ontop.OptimizationTestingTools.*;
import static junit.framework.TestCase.assertEquals;

public class UniqueConstraintInferenceTest {

    private final ImmutableList<Template.Component> URI_TEMPLATE_INJECTIVE_2 = Template.of("http://example.org/ds1/", 0, "/", 1);

    private final ImmutableList<Template.Component> URI_TEMPLATE_INJECTIVE_2_1 = Template.of("http://example.org/ds3/", 0, "/", 1);

    private final ImmutableList<Template.Component> URI_TEMPLATE_NOT_INJECTIVE_2 = Template.of("http://example.org/ds2/", 0, "", 1);

    private final ExtensionalDataNode DATA_NODE_1 = createExtensionalDataNode(PK_TABLE1_AR2, ImmutableList.of(A, B));
    private final ExtensionalDataNode DATA_NODE_2 = createExtensionalDataNode(PK_TABLE1_AR3, ImmutableList.of(A, B, C));

    private static final NamedRelationDefinition COMPOSITE_PK_REL;
    private static final NamedRelationDefinition COMPOSITE_PK_REL_3;

    static {
        OptimizationTestingTools.OfflineMetadataProviderBuilder3 builder = createMetadataProviderBuilder();
        COMPOSITE_PK_REL = builder.createRelation("table", 2, TYPE_FACTORY.getDBTypeFactory().getDBStringType(), false);
        COMPOSITE_PK_REL_3 = builder.createRelation("table3", 3, TYPE_FACTORY.getDBTypeFactory().getDBStringType(), false);
        UniqueConstraint.primaryKeyOf(COMPOSITE_PK_REL.getAttribute(1), COMPOSITE_PK_REL.getAttribute(2));
        UniqueConstraint.primaryKeyOf(COMPOSITE_PK_REL_3.getAttribute(1), COMPOSITE_PK_REL_3.getAttribute(2));
    }




    @Test
    public void testConstructionInjectiveTemplate1() {

        IQTree tree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(
                        ImmutableSet.of(X),
                        SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_INJECTIVE_2, ImmutableList.of(A, B)))),
                DATA_NODE_1);
        assertEquals(ImmutableSet.of(ImmutableSet.of(X)), tree.inferUniqueConstraints());
    }

    @Test
    public void testConstructionInjectiveTemplate2() {

        IQTree tree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(
                        ImmutableSet.of(X),
                        SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_INJECTIVE_2, ImmutableList.of(B, C)))),
                DATA_NODE_2);
        assertEquals(ImmutableSet.of(), tree.inferUniqueConstraints());
    }

    @Test
    public void testConstructionInjectiveTemplate3() {

        IQTree tree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(
                        ImmutableSet.of(X),
                        SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_INJECTIVE_2, ImmutableList.of(A, B)))),
                IQ_FACTORY.createExtensionalDataNode(COMPOSITE_PK_REL, ImmutableMap.of(0, A, 1, B)));
        assertEquals(ImmutableSet.of(ImmutableSet.of(X)), tree.inferUniqueConstraints());
    }

    @Test
    public void testConstructionNonInjectiveTemplate1() {

        IQTree tree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(
                        ImmutableSet.of(X),
                        SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_NOT_INJECTIVE_2, ImmutableList.of(A, B)))),
                DATA_NODE_1);
        assertEquals(ImmutableSet.of(), tree.inferUniqueConstraints());
    }

    @Test
    public void testConstructionNonInjectiveTemplate2() {

        IQTree tree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(
                        ImmutableSet.of(X),
                        SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_NOT_INJECTIVE_2, ImmutableList.of(A, B)))),
                IQ_FACTORY.createExtensionalDataNode(COMPOSITE_PK_REL, ImmutableMap.of(0, A, 1, B)));
        assertEquals(ImmutableSet.of(), tree.inferUniqueConstraints());
    }

    @Test
    public void testConstructionCompositeUniqueConstraint1() {
        IQTree tree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(
                        ImmutableSet.of(X, Y),
                        SUBSTITUTION_FACTORY.getSubstitution(
                                X, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_INJECTIVE_2, ImmutableList.of(A, A)),
                                Y, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_INJECTIVE_2, ImmutableList.of(B, B)
                        ))
                ),
                IQ_FACTORY.createExtensionalDataNode(COMPOSITE_PK_REL, ImmutableMap.of(0, A, 1, B)));
        assertEquals(ImmutableSet.of(ImmutableSet.of(X, Y)), tree.inferUniqueConstraints());
    }

    @Test
    public void testConstructionCompositeUniqueConstraint2() {
        IQTree tree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(
                        ImmutableSet.of(X, Y),
                        SUBSTITUTION_FACTORY.getSubstitution(
                                X, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_INJECTIVE_2, ImmutableList.of(A, A)),
                                Y, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_INJECTIVE_2, ImmutableList.of(A, B)
                                ))
                ),
                IQ_FACTORY.createExtensionalDataNode(COMPOSITE_PK_REL, ImmutableMap.of(0, A, 1, B)));
        assertEquals(ImmutableSet.of(ImmutableSet.of(Y)), tree.inferUniqueConstraints());
    }

    @Test
    public void testConstructionCompositeUniqueConstraint3() {
        IQTree tree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(
                        ImmutableSet.of(X, Y),
                        SUBSTITUTION_FACTORY.getSubstitution(
                                X, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_INJECTIVE_2, ImmutableList.of(A, B)),
                                Y, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_INJECTIVE_2, ImmutableList.of(B, B)
                                ))
                ),
                IQ_FACTORY.createExtensionalDataNode(COMPOSITE_PK_REL, ImmutableMap.of(0, A, 1, B)));
        assertEquals(ImmutableSet.of(ImmutableSet.of(X)), tree.inferUniqueConstraints());
    }

    @Test
    public void testConstructionCompositeUniqueConstraint4() {
        IQTree tree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(
                        ImmutableSet.of(X, Y),
                        SUBSTITUTION_FACTORY.getSubstitution(
                                X, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_INJECTIVE_2, ImmutableList.of(A, C)),
                                Y, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_INJECTIVE_2, ImmutableList.of(B, C)
                                ))
                ),
                IQ_FACTORY.createExtensionalDataNode(COMPOSITE_PK_REL_3, ImmutableMap.of(0, A, 1, B, 2, C)));
        assertEquals(ImmutableSet.of(ImmutableSet.of(X, Y)), tree.inferUniqueConstraints());
    }

    @Test
    public void testDuplicateColumn1() {

        IQTree tree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(
                        ImmutableSet.of(X, A),
                        SUBSTITUTION_FACTORY.getSubstitution(X, A)),
                DATA_NODE_1);
        assertEquals(ImmutableSet.of(ImmutableSet.of(X), ImmutableSet.of(A)), tree.inferUniqueConstraints());
    }

    @Test
    public void testDuplicateColumn2() {

        IQTree tree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(
                        ImmutableSet.of(X, A, B),
                        SUBSTITUTION_FACTORY.getSubstitution(X, A)),
                IQ_FACTORY.createExtensionalDataNode(COMPOSITE_PK_REL, ImmutableMap.of(0, A, 1, B)));
        assertEquals(ImmutableSet.of(ImmutableSet.of(X, B), ImmutableSet.of(A, B)), tree.inferUniqueConstraints());
    }

    @Test
    public void testDuplicateColumn3() {

        IQTree tree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(
                        ImmutableSet.of(X, Y, A, B),
                        SUBSTITUTION_FACTORY.getSubstitution(X, A, Y, B)),
                IQ_FACTORY.createExtensionalDataNode(COMPOSITE_PK_REL, ImmutableMap.of(0, A, 1, B)));
        assertEquals(ImmutableSet.of(ImmutableSet.of(X, B), ImmutableSet.of(A, B),
                ImmutableSet.of(A, Y), ImmutableSet.of(X, Y)), tree.inferUniqueConstraints());
    }

    @Test
    public void testDuplicateColumn4() {

        IQTree tree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(
                        ImmutableSet.of(X, Y, A),
                        SUBSTITUTION_FACTORY.getSubstitution(X, A, Y, A)),
                DATA_NODE_1);
        assertEquals(ImmutableSet.of(ImmutableSet.of(X), ImmutableSet.of(Y), ImmutableSet.of(A)), tree.inferUniqueConstraints());
    }

    /**
     * Unique constraint but not disjoint
     */
    @Test
    public void testUnion1() {
        IQTree tree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createUnionNode(ImmutableSet.of(A, B)),
                ImmutableList.of(DATA_NODE_1, DATA_NODE_1));
        assertEquals(ImmutableSet.of(), tree.inferUniqueConstraints());
    }

    @Test
    public void testUnion2() {
        UnaryIQTree child1 = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(
                        ImmutableSet.of(X, A, B),
                        SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_INJECTIVE_2, ImmutableList.of(A, B)))),
                DATA_NODE_1);

        UnaryIQTree child2 = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(
                        ImmutableSet.of(X, A, B),
                        SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_INJECTIVE_2_1, ImmutableList.of(A, B)))),
                DATA_NODE_1);

        IQTree tree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createUnionNode(ImmutableSet.of(X, A, B)),
                ImmutableList.of(child1, child2));
        assertEquals(ImmutableSet.of(ImmutableSet.of(X)), tree.inferUniqueConstraints());
    }

    /**
     * Same template, not disjoint
     */
    @Test
    public void testUnion3() {
        UnaryIQTree child1 = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(
                        ImmutableSet.of(X, A, B),
                        SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_INJECTIVE_2, ImmutableList.of(A, B)))),
                DATA_NODE_1);

        UnaryIQTree child2 = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(
                        ImmutableSet.of(X, A, B),
                        SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_INJECTIVE_2, ImmutableList.of(A, B)))),
                DATA_NODE_1);

        IQTree tree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createUnionNode(ImmutableSet.of(X, A, B)),
                ImmutableList.of(child1, child2));
        assertEquals(ImmutableSet.of(), tree.inferUniqueConstraints());
    }

    @Test
    public void testUnion4() {
        UnaryIQTree child1 = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(
                        ImmutableSet.of(X, A, B),
                        SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_INJECTIVE_2, ImmutableList.of(A, ONE)))),
                DATA_NODE_1);

        UnaryIQTree child2 = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(
                        ImmutableSet.of(X, A, B),
                        SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_INJECTIVE_2, ImmutableList.of(A, TWO)))),
                DATA_NODE_1);

        IQTree tree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createUnionNode(ImmutableSet.of(X, A, B)),
                ImmutableList.of(child1, child2));
        assertEquals(ImmutableSet.of(ImmutableSet.of(X)), tree.inferUniqueConstraints());
    }

    @Test
    public void testUnion5() {
        UnaryIQTree child1 = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(
                        ImmutableSet.of(X, A, B),
                        SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_INJECTIVE_2, ImmutableList.of(ONE, B)))),
                DATA_NODE_1);

        UnaryIQTree child2 = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(
                        ImmutableSet.of(X, A, B),
                        SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_INJECTIVE_2, ImmutableList.of(TWO, B)))),
                DATA_NODE_1);

        IQTree tree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createUnionNode(ImmutableSet.of(X, A, B)),
                ImmutableList.of(child1, child2));
        assertEquals(ImmutableSet.of(ImmutableSet.of(X, A)), tree.inferUniqueConstraints());
    }

    @Test
    public void testUnion6() {
        UnaryIQTree child1 = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(
                        ImmutableSet.of(X),
                        SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_INJECTIVE_2, ImmutableList.of(A, ONE)))),
                DATA_NODE_1);

        UnaryIQTree child2 = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(
                        ImmutableSet.of(X),
                        SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_INJECTIVE_2, ImmutableList.of(A, TWO)))),
                DATA_NODE_1);

        IQTree tree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createUnionNode(ImmutableSet.of(X)),
                ImmutableList.of(child1, child2));
        assertEquals(ImmutableSet.of(ImmutableSet.of(X)), tree.inferUniqueConstraints());
    }

    @Test
    public void testUnion7() {
        UnaryIQTree child1 = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(
                        ImmutableSet.of(X),
                        SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_INJECTIVE_2, ImmutableList.of(A, ONE)))),
                DATA_NODE_1);

        UnaryIQTree child2 = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(
                        ImmutableSet.of(X),
                        SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_INJECTIVE_2, ImmutableList.of(A, TWO)))),
                DATA_NODE_1);

        // Not distinct
        ValuesNode child3 = IQ_FACTORY.createValuesNode(ImmutableList.of(X), ImmutableList.of(ImmutableList.of(ONE, ONE)));

        IQTree tree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createUnionNode(ImmutableSet.of(X)),
                ImmutableList.of(child1, child2, child3));
        assertEquals(ImmutableSet.of(), tree.inferUniqueConstraints());
    }

    @Test
    public void testUnion8() {
        UnaryIQTree child1 = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(
                        ImmutableSet.of(X),
                        SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_INJECTIVE_2, ImmutableList.of(A, ONE)))),
                DATA_NODE_1);

        // Not distinct
        ValuesNode child3 = IQ_FACTORY.createValuesNode(ImmutableList.of(X), ImmutableList.of(ImmutableList.of(ONE, ONE)));

        IQTree tree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createUnionNode(ImmutableSet.of(X)),
                ImmutableList.of(child1, child3));
        assertEquals(ImmutableSet.of(), tree.inferUniqueConstraints());
    }

    @Test
    public void testUnion9() {
        ExtensionalDataNode dataNode = IQ_FACTORY.createExtensionalDataNode(PK_TABLE1_AR1, ImmutableMap.of(0, A));

        UnaryIQTree child1 = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(
                        ImmutableSet.of(X),
                        SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_INJECTIVE_2, ImmutableList.of(A, ONE)))),
                dataNode);

        UnaryIQTree child2 = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(
                        ImmutableSet.of(X),
                        SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_INJECTIVE_2_1, ImmutableList.of(A, ONE)))),
                dataNode);

        IQTree tree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createUnionNode(ImmutableSet.of(X)),
                ImmutableList.of(child1, child2, child2));
        assertEquals(ImmutableSet.of(), tree.inferUniqueConstraints());
    }
}
