package it.unibz.inf.ontop.iq.optimizer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.iq.node.InnerJoinNode;
import it.unibz.inf.ontop.iq.node.LeftJoinNode;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.iq.*;
import org.junit.Ignore;
import org.junit.Test;

import static it.unibz.inf.ontop.NoDependencyTestDBMetadata.*;
import static it.unibz.inf.ontop.OptimizationTestingTools.*;
import static it.unibz.inf.ontop.iq.node.BinaryOrderedOperatorNode.ArgumentPosition.LEFT;
import static it.unibz.inf.ontop.iq.node.BinaryOrderedOperatorNode.ArgumentPosition.RIGHT;

public class UriTemplateTest {
    private static String URI_TEMPLATE_STR_1_PREFIX =  "http://example.org/ds1/";
    private static String URI_TEMPLATE_STR_1 =  URI_TEMPLATE_STR_1_PREFIX + "{}";
    private static String URI_TEMPLATE_STR_2_PREFIX =  "http://example.org/ds2/";
    private static String URI_TEMPLATE_STR_2 =  URI_TEMPLATE_STR_2_PREFIX + "{}";
    private static String URI_TEMPLATE_STR_3 =  "{}";

    private final static Variable X = TERM_FACTORY.getVariable("x");
    private final static Variable Y = TERM_FACTORY.getVariable("y");
    private final static Variable Z = TERM_FACTORY.getVariable("z");
    private final static Variable A = TERM_FACTORY.getVariable("a");
    private final static Variable B = TERM_FACTORY.getVariable("b");
    private final static Variable C = TERM_FACTORY.getVariable("c");
    private final static Variable D = TERM_FACTORY.getVariable("d");
    private final static Variable E = TERM_FACTORY.getVariable("e");
    private final static Variable F = TERM_FACTORY.getVariable("f");

    private final static AtomPredicate ANS1_PREDICATE_1 = ATOM_FACTORY.getRDFAnswerPredicate(1);

    public UriTemplateTest() {
    }

    @Ignore
    @Test
    public void testCompatibleUriTemplates1() throws EmptyQueryException {
        IntermediateQueryBuilder initialQueryBuilder = createQueryBuilder(DB_METADATA);

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_1, X);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        initialQueryBuilder.init(projectionAtom, rootNode);

        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        initialQueryBuilder.addChild(rootNode, leftJoinNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        initialQueryBuilder.addChild(leftJoinNode, joinNode, LEFT);

        ConstructionNode leftConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateOneVarURITemplate(URI_TEMPLATE_STR_1, A)));
        initialQueryBuilder.addChild(joinNode, leftConstructionNode);

        ExtensionalDataNode leftDataNode = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_AR2, A, B));
        initialQueryBuilder.addChild(leftConstructionNode, leftDataNode);

        ConstructionNode middleConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateOneVarURITemplate(URI_TEMPLATE_STR_3, C)));
        initialQueryBuilder.addChild(joinNode, middleConstructionNode);

        ExtensionalDataNode middleDataNode = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_AR2, C, D));
        initialQueryBuilder.addChild(middleConstructionNode, middleDataNode);

        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateOneVarURITemplate(URI_TEMPLATE_STR_2, E)));
        initialQueryBuilder.addChild(leftJoinNode, rightConstructionNode, RIGHT);

        ExtensionalDataNode rightDataNode = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE3_AR1, E));
        initialQueryBuilder.addChild(rightConstructionNode, rightDataNode);

        IntermediateQuery initialQuery = initialQueryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  initialQuery);


        IntermediateQueryBuilder expectedQueryBuilder = initialQuery.newBuilder();
        expectedQueryBuilder.init(projectionAtom, leftConstructionNode);

        InnerJoinNode newJoinNode = IQ_FACTORY.createInnerJoinNode(
                TERM_FACTORY.getStrictEquality(
                        TERM_FACTORY.getNullRejectingDBConcatFunctionalTerm(
                                ImmutableList.of(TERM_FACTORY.getDBStringConstant(URI_TEMPLATE_STR_1_PREFIX), A)),
                        C));

        expectedQueryBuilder.addChild(leftConstructionNode, newJoinNode);
        expectedQueryBuilder.addChild(newJoinNode, leftDataNode);
        expectedQueryBuilder.addChild(newJoinNode, middleDataNode);

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();
        System.out.println("\n Expected query : \n" +  expectedQuery);

        IntermediateQuery optimizedQuery = BINDING_LIFT_OPTIMIZER.optimize(initialQuery);

        System.out.println("\n After optimization: \n" +  optimizedQuery);
    }


    private static ImmutableFunctionalTerm generateOneVarURITemplate(String templateString, ImmutableTerm value) {
        return TERM_FACTORY.getIRIFunctionalTerm(templateString, ImmutableList.of(value));
    }
}
