package it.unibz.inf.ontop.datalog;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.injection.OntopOptimizationConfiguration;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.iq.node.UnionNode;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.model.term.Function;
import it.unibz.inf.ontop.model.term.Variable;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static it.unibz.inf.ontop.OptimizationTestingTools.*;
import static org.junit.Assert.assertTrue;


public class IntermediateQueryToDatalogTranslatorTest {


    private static Variable X = TERM_FACTORY.getVariable("x");
    private static AtomPredicate ANS1_IQ_PREDICATE = ATOM_FACTORY.getAtomPredicate("ans1", 1);
    private static DistinctVariableOnlyDataAtom ANS1_X_ATOM = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
            ANS1_IQ_PREDICATE, ImmutableList.of(X));
    private static AtomPredicate P1_IQ_PREDICATE = ATOM_FACTORY.getAtomPredicate("p1", 1);
    private static AtomPredicate P2_IQ_PREDICATE = ATOM_FACTORY.getAtomPredicate("p2", 1);
    private static DistinctVariableOnlyDataAtom P1_X_ATOM = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
            P1_IQ_PREDICATE, ImmutableList.of(X));
    private static DistinctVariableOnlyDataAtom P2_X_ATOM = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
            P2_IQ_PREDICATE, ImmutableList.of(X));

    private static Predicate ANS1_DATALOG_PREDICATE;
    private static Predicate SUBQUERY1_DATALOG_PREDICATE;
    private static Predicate P1_DATALOG_PREDICATE;
    private static Predicate P2_DATALOG_PREDICATE;

    static {
        ANS1_DATALOG_PREDICATE = ATOM_FACTORY.getClassPredicate("ans1");
        SUBQUERY1_DATALOG_PREDICATE = ATOM_FACTORY.getClassPredicate(DATALOG_FACTORY.getSubqueryPredicatePrefix()+"1");
        P1_DATALOG_PREDICATE = ATOM_FACTORY.getClassPredicate("p1");
        P2_DATALOG_PREDICATE = ATOM_FACTORY.getClassPredicate("p2");
    }

    @Test
    public void testUnionNodeChild() {

        IntermediateQuery2DatalogTranslator translator = OntopOptimizationConfiguration.defaultBuilder().enableTestMode().build()
                .getInjector()
                .getInstance(IntermediateQuery2DatalogTranslator.class);

        Exception thrownException = null;
        /**
         * Original query
         */
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);

        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X));

        queryBuilder.init(ANS1_X_ATOM, rootNode);

        UnionNode unionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(X));
        queryBuilder.addChild(rootNode, unionNode);

        ExtensionalDataNode extensionalDataNode1 = IQ_FACTORY.createExtensionalDataNode(P1_X_ATOM);
        queryBuilder.addChild(unionNode, extensionalDataNode1);

        ExtensionalDataNode extensionalDataNode2 = IQ_FACTORY.createExtensionalDataNode(P2_X_ATOM);
        queryBuilder.addChild(unionNode, extensionalDataNode2);


        IntermediateQuery inputQuery = queryBuilder.build();

        System.out.println("input query:\n" + inputQuery.getProjectionAtom() + ":-\n" +
                inputQuery);

        DatalogProgram dp = null;
        try {
            dp = translator.translate(inputQuery);
        } catch (ClassCastException e) {
            thrownException = e;
        }

        System.out.println("Datalog rewriting:\n" + dp);


        /**
         Expected Datalog program
         */
        Function ans1Atom = TERM_FACTORY.getFunction(ANS1_DATALOG_PREDICATE, X);
        Function ansSQ1Atom = TERM_FACTORY.getFunction(SUBQUERY1_DATALOG_PREDICATE, X);
        Function p1Atom = TERM_FACTORY.getFunction(P1_DATALOG_PREDICATE, X);
        Function p2Atom = TERM_FACTORY.getFunction(P2_DATALOG_PREDICATE, X);

        List<CQIE> cqies = new ArrayList<>();

        cqies.add(DATALOG_FACTORY.getCQIE(ans1Atom, ansSQ1Atom));
        cqies.add(DATALOG_FACTORY.getCQIE(ansSQ1Atom, p1Atom));
        cqies.add(DATALOG_FACTORY.getCQIE(ansSQ1Atom, p2Atom));


        DatalogProgram expectedDp = DATALOG_FACTORY.getDatalogProgram();
        expectedDp.appendRule(cqies);


        System.out.println("Expected Datalog program:\n" + expectedDp);

        assertTrue(thrownException == null);
    }
}

