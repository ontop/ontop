package it.unibz.inf.ontop.iq.optimizer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.iq.node.InnerJoinNode;
import it.unibz.inf.ontop.iq.node.LeftJoinNode;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.template.Template;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.iq.*;
import org.junit.Ignore;
import org.junit.Test;

import static it.unibz.inf.ontop.NoDependencyTestDBMetadata.*;
import static it.unibz.inf.ontop.OptimizationTestingTools.*;

public class UriTemplateTest {
    private final static String URI_TEMPLATE_STR_1_PREFIX =  "http://example.org/ds1/";
    private final static String URI_TEMPLATE_STR_2_PREFIX =  "http://example.org/ds2/";
    private final static String URI_TEMPLATE_STR_3_PREFIX =  "";

    private final static Variable X = TERM_FACTORY.getVariable("x");
    private final static Variable A = TERM_FACTORY.getVariable("a");
    private final static Variable B = TERM_FACTORY.getVariable("b");
    private final static Variable C = TERM_FACTORY.getVariable("c");
    private final static Variable D = TERM_FACTORY.getVariable("d");
    private final static Variable E = TERM_FACTORY.getVariable("e");

    private final static AtomPredicate ANS1_PREDICATE_1 = ATOM_FACTORY.getRDFAnswerPredicate(1);

    @Ignore
    @Test
    public void testCompatibleUriTemplates1()  {

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_1, X);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();

        ConstructionNode leftConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateOneVarURITemplate(URI_TEMPLATE_STR_1_PREFIX, A)));
        ExtensionalDataNode leftDataNode = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B));

        ConstructionNode middleConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateOneVarURITemplate(URI_TEMPLATE_STR_3_PREFIX, C)));
        ExtensionalDataNode middleDataNode = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(C, D));

        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateOneVarURITemplate(URI_TEMPLATE_STR_2_PREFIX, E)));
        ExtensionalDataNode rightDataNode = createExtensionalDataNode(TABLE3_AR1, ImmutableList.of(E));

        IQ initialQuery = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(rootNode,
                        IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode,
                                IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(
                                        IQ_FACTORY.createUnaryIQTree(leftConstructionNode, leftDataNode),
                                        IQ_FACTORY.createUnaryIQTree(middleConstructionNode, middleDataNode))),
                                IQ_FACTORY.createUnaryIQTree(rightConstructionNode, rightDataNode))));

        System.out.println("\nBefore optimization: \n" +  initialQuery);


        InnerJoinNode newJoinNode = IQ_FACTORY.createInnerJoinNode(
                TERM_FACTORY.getStrictEquality(
                        TERM_FACTORY.getNullRejectingDBConcatFunctionalTerm(
                                ImmutableList.of(TERM_FACTORY.getDBStringConstant(URI_TEMPLATE_STR_1_PREFIX), A)),
                        C));

        IQ expectedQuery = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(leftConstructionNode,
                        IQ_FACTORY.createNaryIQTree(newJoinNode, ImmutableList.of(leftDataNode, middleDataNode))));

        System.out.println("\n Expected query : \n" +  expectedQuery);

        IQ optimizedQuery = UNION_AND_BINDING_LIFT_OPTIMIZER.optimize(initialQuery);

        System.out.println("\n After optimization: \n" +  optimizedQuery);
    }


    private static ImmutableFunctionalTerm generateOneVarURITemplate(String prefix, ImmutableTerm value) {
        return TERM_FACTORY.getIRIFunctionalTerm(Template.of(prefix, 0), ImmutableList.of(value));
    }
}
