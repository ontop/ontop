/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de la classe NotrequiredVariableRemoverTest.
 * @brief @~english NotrequiredVariableRemoverTest class implementation.
 */

package it.unibz.inf.ontop.iq;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.model.template.Template;
import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.term.IRIConstant;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.Variable;
import org.junit.Test;

import static it.unibz.inf.ontop.OntopModelTestingTools.CORE_UTILS_FACTORY;
import static it.unibz.inf.ontop.OntopModelTestingTools.IQ_FACTORY;
import static it.unibz.inf.ontop.OntopModelTestingTools.SUBSTITUTION_FACTORY;
import static it.unibz.inf.ontop.OntopModelTestingTools.TERM_FACTORY;
import static junit.framework.TestCase.assertTrue;

public class NotRequiredVariableRemoverTest {
  public static final Variable S;
  public static final Variable P;
  public static final Variable O;
  public static final Variable V0;
  public static final Variable V1;
  public static final Variable V2;
  public static final Variable V3;

  public static final IRIConstant IRI_CONSTANT;
  public static final DBConstant ONE_STR, TWO_STR, THREE_STR, FOUR_STR;

  static {
    S = TERM_FACTORY.getVariable("s");
    P = TERM_FACTORY.getVariable("p");
    O = TERM_FACTORY.getVariable("o");
    V0 = TERM_FACTORY.getVariable("v0");
    V1 = TERM_FACTORY.getVariable("v1");
    V2 = TERM_FACTORY.getVariable("v2");
    V3 = TERM_FACTORY.getVariable("v3");

    IRI_CONSTANT = TERM_FACTORY.getConstantIRI("http://example.org/iri");

    ONE_STR = TERM_FACTORY.getDBStringConstant("1");
    TWO_STR = TERM_FACTORY.getDBStringConstant("2");
    THREE_STR = TERM_FACTORY.getDBStringConstant("3");
    FOUR_STR = TERM_FACTORY.getDBStringConstant("4");
  }

  /**
   * Tree before normalizing:
   * CONSTRUCT [s, p, o] [s/o, p/<http://example.org/iri>, o/s]
   *    UNION [s, o]
   *       CONSTRUCT [s, o] [s/RDF(http://example.org/dom1/{}(v0),IRI), o/RDF(http://example.org/range1/{}(v1),IRI)]
   *          VALUES [v0, v1] ("1"^^STRING,"2"^^STRING)
   *       CONSTRUCT [s, o] [s/RDF(http://example.org/dom2/{}(v2),IRI), o/RDF(http://example.org/range2/{}(v3),IRI)]
   *          VALUES [v2, v3] ("3"^^STRING,"4"^^STRING)
   * Expected tree:
   * CONSTRUCT [s, p, o] [p/<http://example.org/iri>, s/RDF(v1,IRI), o/RDF(v0,IRI)]
   *    VALUES [v0, v1] ("http://example.org/dom1/1"^^STRING,"http://example.org/range1/2"^^STRING) ("http://example.org/dom2/3"^^STRING,"http://example.org/range2/4"^^STRING)
   */
  @Test
  public void swappingSubstitutionInTopConstructionNodeNotRemoved() {
    ImmutableFunctionalTerm iriDom1 = TERM_FACTORY.getIRIFunctionalTerm(
        Template.builder().string("http://example.org/dom1").string("/").placeholder()
            .build(), ImmutableList.of(V0));
    ImmutableFunctionalTerm iriRange1 = TERM_FACTORY.getIRIFunctionalTerm(
        Template.builder().string("http://example.org/range1").string("/").placeholder()
            .build(), ImmutableList.of(V1));
    ImmutableFunctionalTerm iriDom2 = TERM_FACTORY.getIRIFunctionalTerm(
        Template.builder().string("http://example.org/dom2").string("/").placeholder()
            .build(), ImmutableList.of(V2));
    ImmutableFunctionalTerm iriRange2 = TERM_FACTORY.getIRIFunctionalTerm(
        Template.builder().string("http://example.org/range2").string("/").placeholder()
            .build(), ImmutableList.of(V3));

    ConstructionNode child1ConstructionNode = IQ_FACTORY.createConstructionNode(
        ImmutableSet.of(S, O), SUBSTITUTION_FACTORY.getSubstitution(S, iriDom1, O, iriRange1));
    ConstructionNode child2ConstructionNode = IQ_FACTORY.createConstructionNode(
        ImmutableSet.of(S, O), SUBSTITUTION_FACTORY.getSubstitution(S, iriDom2, O, iriRange2));
    IQTree child1Tree = IQ_FACTORY.createUnaryIQTree(child1ConstructionNode,
        IQ_FACTORY.createValuesNode(ImmutableList.of(V0, V1),
            ImmutableList.of(ImmutableList.of(ONE_STR, TWO_STR))));
    IQTree child2Tree = IQ_FACTORY.createUnaryIQTree(child2ConstructionNode,
        IQ_FACTORY.createValuesNode(ImmutableList.of(V2, V3),
            ImmutableList.of(ImmutableList.of(THREE_STR, FOUR_STR))));

    UnaryIQTree baseTree = IQ_FACTORY.createUnaryIQTree(
        IQ_FACTORY.createConstructionNode(ImmutableSet.of(S, P, O),
            SUBSTITUTION_FACTORY.getSubstitution(S, O, P, IRI_CONSTANT, O, S)),
        IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createUnionNode(ImmutableSet.of(S, O)),
            ImmutableList.of(child1Tree, child2Tree)));

    DBConstant iriStringDom1 = TERM_FACTORY.getDBStringConstant("http://example.org/dom1/1");
    DBConstant iriStringRange1 = TERM_FACTORY.getDBStringConstant("http://example.org/range1/2");
    DBConstant iriStringDom2 = TERM_FACTORY.getDBStringConstant("http://example.org/dom2/3");
    DBConstant iriStringRange2 = TERM_FACTORY.getDBStringConstant("http://example.org/range2/4");
    UnaryIQTree expectedTree = IQ_FACTORY.createUnaryIQTree(
        IQ_FACTORY.createConstructionNode(ImmutableSet.of(S, P, O),
            SUBSTITUTION_FACTORY.getSubstitution(P, IRI_CONSTANT, S,
                TERM_FACTORY.getIRIFunctionalTerm(V1), O, TERM_FACTORY.getIRIFunctionalTerm(V0))),
        IQ_FACTORY.createValuesNode(ImmutableList.of(V0, V1),
            ImmutableList.of(ImmutableList.of(iriStringDom1, iriStringRange1),
                ImmutableList.of(iriStringDom2, iriStringRange2))));

    assertTrue(baseTestVariableRemover(baseTree, expectedTree));
  }

  private Boolean baseTestVariableRemover(IQTree initialTree, IQTree expectedTree) {
    System.out.println('\n' + "Tree before normalizing:");
    System.out.println(initialTree);
    System.out.println('\n' + "Expected tree:");
    System.out.println(expectedTree);
    IQTree normalizedTree = initialTree.normalizeForOptimization(CORE_UTILS_FACTORY
        .createVariableGenerator(initialTree.getVariables()));
    System.out.println('\n' + "Optimized tree:");
    System.out.println(normalizedTree);
    return normalizedTree.equals(expectedTree);
  }
}
