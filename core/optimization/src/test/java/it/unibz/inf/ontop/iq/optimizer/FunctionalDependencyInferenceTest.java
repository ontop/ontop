package it.unibz.inf.ontop.iq.optimizer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.dbschema.NamedRelationDefinition;
import it.unibz.inf.ontop.dbschema.UniqueConstraint;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.iq.request.FunctionalDependencies;
import it.unibz.inf.ontop.model.template.Template;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.junit.Test;

import java.util.UUID;
import java.util.stream.IntStream;

import static it.unibz.inf.ontop.DependencyTestDBMetadata.*;
import static it.unibz.inf.ontop.OptimizationTestingTools.*;
import static junit.framework.TestCase.assertEquals;

public class FunctionalDependencyInferenceTest {

    private final ImmutableList<Template.Component> URI_TEMPLATE_INJECTIVE_2 = Template.builder().string("http://example.org/ds1/").placeholder().string("/").placeholder().build();

    private final ImmutableList<Template.Component> URI_TEMPLATE_INJECTIVE_2_1 = Template.builder().string("http://example.org/ds3/").placeholder().string("/").placeholder().build();

    private final ExtensionalDataNode DATA_NODE_1 = createExtensionalDataNode(PK_TABLE1_AR2, ImmutableList.of(A, B));
    private final ExtensionalDataNode DATA_NODE_1_WITH_ADDED_FD = createExtensionalDataNode(FD_TABLE1_AR2 , ImmutableList.of(A, B));
    private final ExtensionalDataNode DATA_NODE_2_WITH_ADDED_FD = createExtensionalDataNode(FD_TABLE2_AR2 , ImmutableList.of(C, D));
    private final ExtensionalDataNode DATA_NODE_2 = createExtensionalDataNode(PK_TABLE1_AR3, ImmutableList.of(A, B, C));
    private final ExtensionalDataNode DATA_NODE_3_WITH_ADDED_FD = createExtensionalDataNode(FD_TABLE1_AR5 , ImmutableList.of(A, B, C, D, E));
    private final ExtensionalDataNode DATA_NODE_4_WITH_ADDED_FD = createExtensionalDataNode(FD_TABLE1_AR2 , ImmutableList.of(A, C));

    private static final NamedRelationDefinition COMPOSITE_PK_REL;

    static {
        OfflineMetadataProviderBuilder3 builder = createMetadataProviderBuilder();
        COMPOSITE_PK_REL = builder.createRelation("table", 2, TYPE_FACTORY.getDBTypeFactory().getDBStringType(), false);
        UniqueConstraint.primaryKeyOf(COMPOSITE_PK_REL.getAttribute(1), COMPOSITE_PK_REL.getAttribute(2));
    }




    @Test
    public void testConstructionOneVariable() {

        IQTree tree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(
                        ImmutableSet.of(X, B),
                        SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_INJECTIVE_2, ImmutableList.of(A, B)))),
                DATA_NODE_1);
        assertEquals(FunctionalDependencies.of(ImmutableSet.of(X), ImmutableSet.of(B)), tree.normalizeForOptimization(CORE_UTILS_FACTORY.createVariableGenerator(tree.getKnownVariables())).inferFunctionalDependencies());
    }

    @Test
    public void testConstructionMultipleUCs() {

        IQTree tree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(
                        ImmutableSet.of(X, A, B),
                        SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_INJECTIVE_2, ImmutableList.of(A, B)))),
                DATA_NODE_1);
        assertEquals(FunctionalDependencies.of(
                ImmutableSet.of(X), ImmutableSet.of(A, B),
                ImmutableSet.of(A), ImmutableSet.of(X, B)
        ), tree.normalizeForOptimization(CORE_UTILS_FACTORY.createVariableGenerator(tree.getKnownVariables())).inferFunctionalDependencies());
    }

    @Test
    public void testConstructionInferFromUCs() {

        IQTree tree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(
                        ImmutableSet.of(X),
                        SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_INJECTIVE_2, ImmutableList.of(A, B)))),
                DATA_NODE_1);
        assertEquals(FunctionalDependencies.of(), tree.normalizeForOptimization(CORE_UTILS_FACTORY.createVariableGenerator(tree.getKnownVariables())).inferFunctionalDependencies());
    }

    @Test
    public void testConstructionInferFromDirectSubstitution() {
        IQTree tree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(
                        ImmutableSet.of(X, B),
                        SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_INJECTIVE_2, ImmutableList.of(A, B)))),
                DATA_NODE_1_WITH_ADDED_FD);
        assertEquals(FunctionalDependencies.of(ImmutableSet.of(X), ImmutableSet.of(B)), tree.normalizeForOptimization(CORE_UTILS_FACTORY.createVariableGenerator(tree.getKnownVariables())).inferFunctionalDependencies());
    }

    @Test
    public void testConstructionInferFromSubstitutionOnDeterminantAndDependent() {
        IQTree tree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(
                        ImmutableSet.of(X, Y),
                        SUBSTITUTION_FACTORY.getSubstitution(
                                X, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_INJECTIVE_2, ImmutableList.of(A, B)),
                                Y, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_INJECTIVE_2, ImmutableList.of(B, B))
                        )),
                DATA_NODE_1_WITH_ADDED_FD);
        assertEquals(FunctionalDependencies.of(ImmutableSet.of(X), ImmutableSet.of(Y)), tree.normalizeForOptimization(CORE_UTILS_FACTORY.createVariableGenerator(tree.getKnownVariables())).inferFunctionalDependencies());
    }

    @Test
    public void testConstructionInferFromSubstitutionWithNonDeterministicDependent() {
        IQTree tree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(
                        ImmutableSet.of(X, Y),
                        SUBSTITUTION_FACTORY.getSubstitution(
                                X, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_INJECTIVE_2, ImmutableList.of(A, B)),
                                Y, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_INJECTIVE_2, ImmutableList.of(B, TERM_FACTORY.getDBRand(UUID.randomUUID())))
                        )),
                DATA_NODE_1_WITH_ADDED_FD);
        assertEquals(FunctionalDependencies.empty(), tree.normalizeForOptimization(CORE_UTILS_FACTORY.createVariableGenerator(tree.getKnownVariables())).inferFunctionalDependencies());
    }

    @Test
    public void testConstructionInferFromDirectSubstitutionWithMultipleDependents() {
        IQTree tree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(
                        ImmutableSet.of(X, Y, Z),
                        SUBSTITUTION_FACTORY.getSubstitution(
                                X, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_INJECTIVE_2, ImmutableList.of(A, B)),
                                Y, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_INJECTIVE_2, ImmutableList.of(B, B)),
                                Z, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_INJECTIVE_2, ImmutableList.of(B, B))
                        )),
                DATA_NODE_1_WITH_ADDED_FD);
        assertEquals(FunctionalDependencies.of(ImmutableSet.of(X), ImmutableSet.of(Y, Z), ImmutableSet.of(Z), ImmutableSet.of(Y), ImmutableSet.of(Y), ImmutableSet.of(Z)), tree.normalizeForOptimization(CORE_UTILS_FACTORY.createVariableGenerator(tree.getKnownVariables())).inferFunctionalDependencies());
    }

    @Test
    public void testConstructionInferFromDirectSubstitutionWithMultipleDeterminants() {
        IQTree tree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(
                        ImmutableSet.of(X, Y, Z),
                        SUBSTITUTION_FACTORY.getSubstitution(
                                X, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_INJECTIVE_2, ImmutableList.of(A, B)),
                                Y, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_INJECTIVE_2, ImmutableList.of(A, B)),
                                Z, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_INJECTIVE_2, ImmutableList.of(B, B))
                        )),
                DATA_NODE_1_WITH_ADDED_FD);
        assertEquals(FunctionalDependencies.of(ImmutableSet.of(X), ImmutableSet.of(Z, Y), ImmutableSet.of(Y), ImmutableSet.of(Z, X)), tree.normalizeForOptimization(CORE_UTILS_FACTORY.createVariableGenerator(tree.getKnownVariables())).inferFunctionalDependencies());
    }

    @Test
    public void testConstructionInferNewFromDirectSubstitution() {
        IQTree tree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(
                        ImmutableSet.of(X, Y, A, B),
                        SUBSTITUTION_FACTORY.getSubstitution(
                                X, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_INJECTIVE_2, ImmutableList.of(B, B)),
                                Y, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_INJECTIVE_2, ImmutableList.of(A, B))
                        )),
                DATA_NODE_1_WITH_ADDED_FD);
        assertEquals(FunctionalDependencies.of(
                ImmutableSet.of(A), ImmutableSet.of(B, X, Y),
                ImmutableSet.of(B), ImmutableSet.of(X),
                ImmutableSet.of(Y), ImmutableSet.of(B, X, A),
                ImmutableSet.of(X), ImmutableSet.of(B)
        ), tree.normalizeForOptimization(CORE_UTILS_FACTORY.createVariableGenerator(tree.getKnownVariables())).inferFunctionalDependencies());
    }

    @Test
    public void testConstructionInferNewFromDirectSubstitutionWithHiddenVariable() {
        IQTree tree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(
                        ImmutableSet.of(X, Y, B),
                        SUBSTITUTION_FACTORY.getSubstitution(
                                X, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_INJECTIVE_2, ImmutableList.of(B, B)),
                                Y, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_INJECTIVE_2, ImmutableList.of(A, B))
                        )),
                DATA_NODE_1_WITH_ADDED_FD);
        assertEquals(FunctionalDependencies.of(ImmutableSet.of(B), ImmutableSet.of(X), ImmutableSet.of(Y), ImmutableSet.of(X, B), ImmutableSet.of(X), ImmutableSet.of(B)), tree.normalizeForOptimization(CORE_UTILS_FACTORY.createVariableGenerator(tree.getKnownVariables())).inferFunctionalDependencies());
    }

    @Test
    public void testConstructionInferFromDirectSubstitutionNonInjective() {
        IQTree tree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(
                        ImmutableSet.of(X, B),
                        SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getDBLower(A))),
                DATA_NODE_1_WITH_ADDED_FD);
        assertEquals(FunctionalDependencies.empty(), tree.normalizeForOptimization(CORE_UTILS_FACTORY.createVariableGenerator(tree.getKnownVariables())).inferFunctionalDependencies());
    }

    @Test
    public void testConstructionInferFromDirectSubstitutionTransitive() {
        IQTree tree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(
                        ImmutableSet.of(X, A),
                        SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_INJECTIVE_2, ImmutableList.of(B, B)))),
                DATA_NODE_1_WITH_ADDED_FD);
        assertEquals(FunctionalDependencies.of(ImmutableSet.of(A), ImmutableSet.of(X)), tree.normalizeForOptimization(CORE_UTILS_FACTORY.createVariableGenerator(tree.getKnownVariables())).inferFunctionalDependencies());
    }

    @Test
    public void testUnionNoProvenance() {
        IQTree tree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createUnionNode(ImmutableSet.of(A, B)),
                ImmutableList.of(DATA_NODE_1_WITH_ADDED_FD, DATA_NODE_1_WITH_ADDED_FD));
        assertEquals(FunctionalDependencies.empty(), tree.normalizeForOptimization(CORE_UTILS_FACTORY.createVariableGenerator(tree.getKnownVariables())).inferFunctionalDependencies());
    }

    @Test
    public void testUnionWithProvenance() {
        ImmutableList<IQTree> children = IntStream.range(0, 5)
                .mapToObj(i -> IQ_FACTORY.createUnaryIQTree(
                        IQ_FACTORY.createConstructionNode(
                                ImmutableSet.of(A, B, X),
                                SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getDBConstant(i + "", TYPE_FACTORY.getDBTypeFactory().getDBStringType()))),
                        DATA_NODE_1_WITH_ADDED_FD))
                .collect(ImmutableCollectors.toList());
        IQTree tree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createUnionNode(ImmutableSet.of(A, B, X)),
                children);
        assertEquals(FunctionalDependencies.of(ImmutableSet.of(A, X), ImmutableSet.of(B)), tree.normalizeForOptimization(CORE_UTILS_FACTORY.createVariableGenerator(tree.getKnownVariables())).inferFunctionalDependencies());
    }

    @Test
    public void testUnionWithMultipleProvenances() {
        ImmutableList<IQTree> children = IntStream.range(0, 2)
                .mapToObj(i -> IQ_FACTORY.createUnaryIQTree(
                        IQ_FACTORY.createConstructionNode(
                                ImmutableSet.of(A, B, X, Y),
                                SUBSTITUTION_FACTORY.getSubstitution(
                                        X, TERM_FACTORY.getDBConstant(i + "", TYPE_FACTORY.getDBTypeFactory().getDBStringType()),
                                        Y, TERM_FACTORY.getDBConstant(i + "!", TYPE_FACTORY.getDBTypeFactory().getDBStringType())
                                )),
                        DATA_NODE_1_WITH_ADDED_FD))
                .collect(ImmutableCollectors.toList());
        IQTree tree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createUnionNode(ImmutableSet.of(A, B, X, Y)),
                children);
        assertEquals(FunctionalDependencies.of(ImmutableSet.of(A, X), ImmutableSet.of(B, Y), ImmutableSet.of(A, Y), ImmutableSet.of(B, X)), tree.normalizeForOptimization(CORE_UTILS_FACTORY.createVariableGenerator(tree.getKnownVariables())).inferFunctionalDependencies());
    }

    @Test
    public void testUnionWithDisjointDeterminants() {
        ImmutableList<IQTree> children = IntStream.range(0, 2)
                .mapToObj(i -> IQ_FACTORY.createUnaryIQTree(
                        IQ_FACTORY.createConstructionNode(
                                ImmutableSet.of(A, B, X),
                                SUBSTITUTION_FACTORY.getSubstitution(
                                        X, TERM_FACTORY.getIRIFunctionalTerm(i == 0 ? URI_TEMPLATE_INJECTIVE_2 : URI_TEMPLATE_INJECTIVE_2_1, ImmutableList.of(A, A))
                                )),
                        DATA_NODE_1_WITH_ADDED_FD))
                .collect(ImmutableCollectors.toList());
        IQTree tree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createUnionNode(ImmutableSet.of(A, B, X)),
                children);
        assertEquals(FunctionalDependencies.of(ImmutableSet.of(X), ImmutableSet.of(B, A)), tree.normalizeForOptimization(CORE_UTILS_FACTORY.createVariableGenerator(tree.getKnownVariables())).inferFunctionalDependencies());
    }

    @Test
    public void testUnionWithSmallerDependentSet() {
        IQTree leftChild = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(
                        ImmutableSet.of(A, B, C, D, E, X),
                        SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getDBConstant("1", TYPE_FACTORY.getDBTypeFactory().getDBStringType()))),
                IQ_FACTORY.createUnaryIQTree(
                        IQ_FACTORY.createConstructionNode(
                                ImmutableSet.of(A, B, C, D, E),
                                SUBSTITUTION_FACTORY.getSubstitution(D, TERM_FACTORY.getDBRand(UUID.randomUUID()))),
                        DATA_NODE_3_WITH_ADDED_FD
                )
        );
        IQTree rightChild = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(
                        ImmutableSet.of(A, B, C, D, E, X),
                        SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getDBConstant("2", TYPE_FACTORY.getDBTypeFactory().getDBStringType()))),
                DATA_NODE_3_WITH_ADDED_FD
        );

        IQTree tree1 = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createUnionNode(ImmutableSet.of(A, B, C, D, E, X)),
                ImmutableList.of(leftChild, rightChild));
        assertEquals(FunctionalDependencies.of(ImmutableSet.of(A, B, X), ImmutableSet.of(C)), tree1.normalizeForOptimization(CORE_UTILS_FACTORY.createVariableGenerator(tree1.getKnownVariables())).inferFunctionalDependencies());

        IQTree tree2 = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createUnionNode(ImmutableSet.of(A, B, C, D, E, X)),
                ImmutableList.of(rightChild, leftChild));
        assertEquals(FunctionalDependencies.of(ImmutableSet.of(A, B, X), ImmutableSet.of(C)), tree2.normalizeForOptimization(CORE_UTILS_FACTORY.createVariableGenerator(tree2.getKnownVariables())).inferFunctionalDependencies());
    }

    @Test
    public void testUnionWithSmallerDeterminantSet() {
        IQTree leftChild = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(
                        ImmutableSet.of(A, B, C, D, X),
                        SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getDBConstant("1", TYPE_FACTORY.getDBTypeFactory().getDBStringType()))),
                IQ_FACTORY.createUnaryIQTree(
                        IQ_FACTORY.createConstructionNode(
                                ImmutableSet.of(A, B, C, D),
                                SUBSTITUTION_FACTORY.getSubstitution(
                                        B, TERM_FACTORY.getDBRand(UUID.randomUUID()),
                                        D, TERM_FACTORY.getDBRand(UUID.randomUUID())
                                )),
                        DATA_NODE_4_WITH_ADDED_FD
                )
        );
        IQTree rightChild = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(
                        ImmutableSet.of(A, B, C, D, X),
                        SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getDBConstant("2", TYPE_FACTORY.getDBTypeFactory().getDBStringType()))),
                DATA_NODE_3_WITH_ADDED_FD
        );

        IQTree tree1 = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createUnionNode(ImmutableSet.of(A, B, C, D, X)),
                ImmutableList.of(leftChild, rightChild));
        assertEquals(FunctionalDependencies.of(ImmutableSet.of(A, B, X), ImmutableSet.of(C)), tree1.normalizeForOptimization(CORE_UTILS_FACTORY.createVariableGenerator(tree1.getKnownVariables())).inferFunctionalDependencies());

        IQTree tree2 = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createUnionNode(ImmutableSet.of(A, B, C, D, X)),
                ImmutableList.of(rightChild, leftChild));
        assertEquals(FunctionalDependencies.of(ImmutableSet.of(A, B, X), ImmutableSet.of(C)), tree2.normalizeForOptimization(CORE_UTILS_FACTORY.createVariableGenerator(tree2.getKnownVariables())).inferFunctionalDependencies());
    }

    @Test
    public void testLeftJoinFromChildren1() {
        IQTree tree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                DATA_NODE_1_WITH_ADDED_FD,
                DATA_NODE_2_WITH_ADDED_FD
        );
        assertEquals(FunctionalDependencies.of(ImmutableSet.of(A), ImmutableSet.of(B), ImmutableSet.of(C), ImmutableSet.of(D)), tree.normalizeForOptimization(CORE_UTILS_FACTORY.createVariableGenerator(tree.getKnownVariables())).inferFunctionalDependencies());
    }

    @Test
    public void testLeftJoinFromChildren2() {
        IQTree tree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(TERM_FACTORY.getStrictEquality(A, C)),
                DATA_NODE_1_WITH_ADDED_FD,
                DATA_NODE_2_WITH_ADDED_FD
        );
        assertEquals(FunctionalDependencies.of(ImmutableSet.of(A), ImmutableSet.of(B, C, D)), tree.normalizeForOptimization(CORE_UTILS_FACTORY.createVariableGenerator(tree.getKnownVariables())).inferFunctionalDependencies());
    }

    @Test
    public void testLeftJoinCrossInfer() {
        IQTree tree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(TERM_FACTORY.getStrictEquality(A, C)),
                DATA_NODE_1_WITH_ADDED_FD,
                DATA_NODE_2_WITH_ADDED_FD
        );
        assertEquals(FunctionalDependencies.of(ImmutableSet.of(A), ImmutableSet.of(B, D, C)), tree.normalizeForOptimization(CORE_UTILS_FACTORY.createVariableGenerator(tree.getKnownVariables())).inferFunctionalDependencies());
    }

    @Test
    public void testInnerJoinFromChildren1() {
        ImmutableList<IQTree> children = ImmutableList.of(
                IQ_FACTORY.createUnaryIQTree(
                        IQ_FACTORY.createConstructionNode(
                                ImmutableSet.of(A, B),
                                SUBSTITUTION_FACTORY.getSubstitution()),
                        DATA_NODE_1_WITH_ADDED_FD),
                IQ_FACTORY.createUnaryIQTree(
                        IQ_FACTORY.createConstructionNode(
                                ImmutableSet.of(C, D),
                                SUBSTITUTION_FACTORY.getSubstitution()),
                        DATA_NODE_2_WITH_ADDED_FD));
        IQTree tree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(children.get(0), children.get(1))
        );
        assertEquals(FunctionalDependencies.of(ImmutableSet.of(A), ImmutableSet.of(B), ImmutableSet.of(C), ImmutableSet.of(D)), tree.normalizeForOptimization(CORE_UTILS_FACTORY.createVariableGenerator(tree.getKnownVariables())).inferFunctionalDependencies());
    }

    @Test
    public void testInnerJoinFromChildren2() {
        ImmutableList<IQTree> children = ImmutableList.of(
                IQ_FACTORY.createUnaryIQTree(
                        IQ_FACTORY.createConstructionNode(
                                ImmutableSet.of(A, B),
                                SUBSTITUTION_FACTORY.getSubstitution()),
                        DATA_NODE_1_WITH_ADDED_FD),
                IQ_FACTORY.createUnaryIQTree(
                        IQ_FACTORY.createConstructionNode(
                                ImmutableSet.of(C, D),
                                SUBSTITUTION_FACTORY.getSubstitution()),
                        DATA_NODE_2_WITH_ADDED_FD));
        IQTree tree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(TERM_FACTORY.getStrictEquality(A, D)),
                ImmutableList.of(children.get(0), children.get(1))
        );
        assertEquals(FunctionalDependencies.of(ImmutableSet.of(A), ImmutableSet.of(B, D),
                ImmutableSet.of(C), ImmutableSet.of(D, B, A),
                ImmutableSet.of(D), ImmutableSet.of(A, B)),
                tree.normalizeForOptimization(CORE_UTILS_FACTORY.createVariableGenerator(tree.getKnownVariables())).inferFunctionalDependencies());
    }

    @Test
    public void testInnerJoinFromChildren3() {
        ImmutableList<IQTree> children = ImmutableList.of(
                IQ_FACTORY.createUnaryIQTree(
                        IQ_FACTORY.createConstructionNode(
                                ImmutableSet.of(A, B),
                                SUBSTITUTION_FACTORY.getSubstitution()),
                        DATA_NODE_1_WITH_ADDED_FD),
                IQ_FACTORY.createUnaryIQTree(
                        IQ_FACTORY.createConstructionNode(
                                ImmutableSet.of(C, D),
                                SUBSTITUTION_FACTORY.getSubstitution()),
                        DATA_NODE_2_WITH_ADDED_FD));
        IQTree tree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(TERM_FACTORY.getStrictEquality(A, C)),
                ImmutableList.of(children.get(0), children.get(1))
        );
        assertEquals(FunctionalDependencies.of(ImmutableSet.of(A), ImmutableSet.of(B, C, D),
                        ImmutableSet.of(C), ImmutableSet.of(D, B, A)),
                tree.normalizeForOptimization(CORE_UTILS_FACTORY.createVariableGenerator(tree.getKnownVariables())).inferFunctionalDependencies());
    }

    @Test
    public void testInnerJoinCrossInfer() {
        ImmutableList<IQTree> children = ImmutableList.of(
                IQ_FACTORY.createUnaryIQTree(
                        IQ_FACTORY.createConstructionNode(
                                ImmutableSet.of(A, B),
                                SUBSTITUTION_FACTORY.getSubstitution()),
                        DATA_NODE_1_WITH_ADDED_FD),
                IQ_FACTORY.createUnaryIQTree(
                        IQ_FACTORY.createConstructionNode(
                                ImmutableSet.of(C, D),
                                SUBSTITUTION_FACTORY.getSubstitution()),
                        DATA_NODE_2_WITH_ADDED_FD));
        IQTree tree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(TERM_FACTORY.getStrictEquality(A, C)),
                ImmutableList.of(children.get(0), children.get(1))
        );
        assertEquals(FunctionalDependencies.of(ImmutableSet.of(A), ImmutableSet.of(B, D, C), ImmutableSet.of(C), ImmutableSet.of(B, D, A)), tree.normalizeForOptimization(CORE_UTILS_FACTORY.createVariableGenerator(tree.getKnownVariables())).inferFunctionalDependencies());
    }
}
