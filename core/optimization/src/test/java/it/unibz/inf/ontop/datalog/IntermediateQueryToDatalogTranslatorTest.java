package it.unibz.inf.ontop.datalog;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.datalog.exception.UnsupportedFeatureForDatalogConversionException;
import it.unibz.inf.ontop.injection.OntopOptimizationConfiguration;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.IntensionalDataNode;
import it.unibz.inf.ontop.iq.node.UnionNode;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.model.term.Function;
import it.unibz.inf.ontop.model.term.Variable;
import org.apache.commons.rdf.api.IRI;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static it.unibz.inf.ontop.OptimizationTestingTools.*;
import static org.junit.Assert.assertTrue;


public class IntermediateQueryToDatalogTranslatorTest {


    private static final IRI C1_IRI, C2_IRI;
    private static Variable X = TERM_FACTORY.getVariable("x");
    private static AtomPredicate ANS1_IQ_PREDICATE = ATOM_FACTORY.getRDFAnswerPredicate(1);
    private static DistinctVariableOnlyDataAtom ANS1_X_ATOM = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
            ANS1_IQ_PREDICATE, ImmutableList.of(X));

    private static Predicate SUBQUERY1_DATALOG_PREDICATE;

    static {
        String prefix = "http://example.com/voc#";
        C1_IRI = RDF_FACTORY.createIRI(prefix + "C1");
        C2_IRI = RDF_FACTORY.createIRI(prefix + "C2");
        SUBQUERY1_DATALOG_PREDICATE = DATALOG_FACTORY.getSubqueryPredicate("1", 1);

    }

    @Test
    public void testUnionNodeChild() {

        IQ2DatalogTranslator translator = OntopOptimizationConfiguration.defaultBuilder().enableTestMode().build()
                .getInjector()
                .getInstance(IQ2DatalogTranslator.class);

        Exception thrownException = null;
        /**
         * Original query
         */
        IntermediateQueryBuilder queryBuilder = createQueryBuilder(EMPTY_METADATA);

        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X));

        queryBuilder.init(ANS1_X_ATOM, rootNode);

        UnionNode unionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(X));
        queryBuilder.addChild(rootNode, unionNode);

        IntensionalDataNode intensionalDataNode1 = IQ_FACTORY.createIntensionalDataNode(
                ATOM_FACTORY.getIntensionalTripleAtom(X, C1_IRI));
        queryBuilder.addChild(unionNode, intensionalDataNode1);
        IntensionalDataNode intensionalDataNode2 = IQ_FACTORY.createIntensionalDataNode(
                ATOM_FACTORY.getIntensionalTripleAtom(X, C2_IRI));

        queryBuilder.addChild(unionNode, intensionalDataNode2);


        IntermediateQuery inputQuery = queryBuilder.build();

        System.out.println("input query:\n" + inputQuery.getProjectionAtom() + ":-\n" +
                inputQuery);

        DatalogProgram dp = null;
        try {
            dp = translator.translate(IQ_CONVERTER.convert(inputQuery));
        } catch (ClassCastException | UnsupportedFeatureForDatalogConversionException e) {
            thrownException = e;
        }
        System.out.println("Datalog rewriting:\n" + dp);


        /**
         Expected Datalog program
         */
        Function ans1Atom = TERM_FACTORY.getFunction(ANS1_IQ_PREDICATE, X);
        Function ansSQ1Atom = TERM_FACTORY.getFunction(SUBQUERY1_DATALOG_PREDICATE, X);
        Function c1Atom = ATOM_FACTORY.getMutableTripleBodyAtom(X, C1_IRI);
        Function c2Atom = ATOM_FACTORY.getMutableTripleBodyAtom(X, C2_IRI);

        List<CQIE> cqies = new ArrayList<>();

        cqies.add(DATALOG_FACTORY.getCQIE(ans1Atom, ansSQ1Atom));
        cqies.add(DATALOG_FACTORY.getCQIE(ansSQ1Atom, c1Atom));
        cqies.add(DATALOG_FACTORY.getCQIE(ansSQ1Atom, c2Atom));


        DatalogProgram expectedDp = DATALOG_FACTORY.getDatalogProgram();
        expectedDp.appendRule(cqies);


        System.out.println("Expected Datalog program:\n" + expectedDp);

        assertTrue(thrownException == null);
    }
}

