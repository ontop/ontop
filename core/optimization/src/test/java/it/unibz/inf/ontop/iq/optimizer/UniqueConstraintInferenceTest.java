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

import static it.unibz.inf.ontop.DependencyTestDBMetadata.PK_TABLE1_AR2;
import static it.unibz.inf.ontop.DependencyTestDBMetadata.PK_TABLE1_AR3;
import static it.unibz.inf.ontop.OptimizationTestingTools.*;
import static junit.framework.TestCase.assertEquals;

public class UniqueConstraintInferenceTest {

    private final ImmutableList<Template.Component> URI_TEMPLATE_INJECTIVE_2 = Template.of("http://example.org/ds1/", 0, "/", 1);

    private final ImmutableList<Template.Component> URI_TEMPLATE_NOT_INJECTIVE_2 = Template.of("http://example.org/ds2/", 0, "", 1);

    private final ExtensionalDataNode DATA_NODE_1 = createExtensionalDataNode(PK_TABLE1_AR2, ImmutableList.of(A, B));
    private final ExtensionalDataNode DATA_NODE_2 = createExtensionalDataNode(PK_TABLE1_AR3, ImmutableList.of(A, B, C));

    private static final NamedRelationDefinition COMPOSITE_PK_REL;

    static {
        OptimizationTestingTools.OfflineMetadataProviderBuilder3 builder = createMetadataProviderBuilder();
        COMPOSITE_PK_REL = builder.createRelation("table", 2, TYPE_FACTORY.getDBTypeFactory().getDBStringType(), false);
        UniqueConstraint.primaryKeyOf(COMPOSITE_PK_REL.getAttribute(1), COMPOSITE_PK_REL.getAttribute(2));
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
}
