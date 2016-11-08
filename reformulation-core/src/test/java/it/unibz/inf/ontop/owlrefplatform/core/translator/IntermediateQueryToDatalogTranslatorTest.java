package it.unibz.inf.ontop.owlrefplatform.core.translator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.impl.AtomPredicateImpl;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.FunctionFlattener;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.ImmutableSubstitutionImpl;
import it.unibz.inf.ontop.owlrefplatform.core.queryevaluation.PostgreSQLDialectAdapter;
import it.unibz.inf.ontop.owlrefplatform.core.srcquerygeneration.SQLGenerator;
import it.unibz.inf.ontop.owlrefplatform.core.srcquerygeneration.SQLQueryGenerator;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.pivotalrepr.impl.*;
import it.unibz.inf.ontop.pivotalrepr.impl.tree.DefaultIntermediateQueryBuilder;
import it.unibz.inf.ontop.sql.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertTrue;


public class IntermediateQueryToDatalogTranslatorTest {

    private static MetadataForQueryOptimization METADATA = new EmptyMetadataForQueryOptimization();
    private static final OBDADataFactory DATA_FACTORY = OBDADataFactoryImpl.getInstance();
    private static Variable X = DATA_FACTORY.getVariable("x");
    private static AtomPredicate ANS1_IQ_PREDICATE = new AtomPredicateImpl("ans1", 1);
    private static DistinctVariableOnlyDataAtom ANS1_X_ATOM = DATA_FACTORY.getDistinctVariableOnlyDataAtom(
            ANS1_IQ_PREDICATE, ImmutableList.of(X));
    private static AtomPredicate P1_IQ_PREDICATE = new AtomPredicateImpl("p1", 1);
    private static AtomPredicate P2_IQ_PREDICATE = new AtomPredicateImpl("p2", 1);
    private static DistinctVariableOnlyDataAtom P1_X_ATOM = DATA_FACTORY.getDistinctVariableOnlyDataAtom(
            P1_IQ_PREDICATE, ImmutableList.of(X));
    private static DistinctVariableOnlyDataAtom P2_X_ATOM = DATA_FACTORY.getDistinctVariableOnlyDataAtom(
            P2_IQ_PREDICATE, ImmutableList.of(X));

    private static Predicate ANS1_DATALOG_PREDICATE;
    private static Predicate ANSSQ1_DATALOG_PREDICATE;
    private static Predicate P1_DATALOG_PREDICATE;
    private static Predicate P2_DATALOG_PREDICATE;

    static {
        ANS1_DATALOG_PREDICATE = DATA_FACTORY.getClassPredicate("ans1");
        ANSSQ1_DATALOG_PREDICATE = DATA_FACTORY.getClassPredicate("ansSQ1");
        P1_DATALOG_PREDICATE = DATA_FACTORY.getClassPredicate("p1");
        P2_DATALOG_PREDICATE = DATA_FACTORY.getClassPredicate("p2");
    }

    @Test
    public void testUnionNodeChild() {

        Exception thrownException = null;
        /**
         * Original query
         */
        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(METADATA);

        ConstructionNode rootNode = new ConstructionNodeImpl(ImmutableSet.of(X),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of()), Optional.empty());

        queryBuilder.init(ANS1_X_ATOM, rootNode);

        UnionNode unionNode = new UnionNodeImpl(ImmutableSet.of(X));
        queryBuilder.addChild(rootNode, unionNode);

        ExtensionalDataNode extensionalDataNode1 = new ExtensionalDataNodeImpl(
                P1_X_ATOM);
        queryBuilder.addChild(unionNode, extensionalDataNode1);

        ExtensionalDataNode extensionalDataNode2 = new ExtensionalDataNodeImpl(
                P2_X_ATOM);
        queryBuilder.addChild(unionNode, extensionalDataNode2);


        IntermediateQuery inputQuery = queryBuilder.build();

        System.out.println("input query:\n" + inputQuery.getProjectionAtom() + ":-\n" +
                inputQuery);

        DatalogProgram dp = null;
        try {
            dp = IntermediateQueryToDatalogTranslator.translate(inputQuery);
        } catch (ClassCastException e) {
            thrownException = e;
        }

        System.out.println("Datalog rewriting:\n" + dp);


        /**
         Expected Datalog program
         */

        Function ans1Atom = DATA_FACTORY.getFunction(ANS1_DATALOG_PREDICATE, X);
        Function ansSQ1Atom = DATA_FACTORY.getFunction(ANSSQ1_DATALOG_PREDICATE, X);
        Function p1Atom = DATA_FACTORY.getFunction(P1_DATALOG_PREDICATE, X);
        Function p2Atom = DATA_FACTORY.getFunction(P2_DATALOG_PREDICATE, X);

        List<CQIE> cqies = new ArrayList<>();

        cqies.add(DATA_FACTORY.getCQIE(ans1Atom, ansSQ1Atom));
        cqies.add(DATA_FACTORY.getCQIE(ansSQ1Atom, p1Atom));
        cqies.add(DATA_FACTORY.getCQIE(ansSQ1Atom, p2Atom));


        DatalogProgram expectedDp = DATA_FACTORY.getDatalogProgram();
        expectedDp.appendRule(cqies);


        System.out.println("Expected Datalog program:\n" + expectedDp);

        assertTrue(thrownException == null);
    }


    @Test
    public void testTrueNodeAsLeftJoinLeftChild() {

        /**
         * Intermediate query
         */
        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(METADATA);

        ConstructionNode rootNode = new ConstructionNodeImpl(ImmutableSet.of(X),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of()), Optional.empty());

        queryBuilder.init(ANS1_X_ATOM,rootNode);

        LeftJoinNode leftJoinNode = new LeftJoinNodeImpl(Optional.empty());
        queryBuilder.addChild(rootNode, leftJoinNode);

        TrueNode trueNode = new TrueNodeImpl();
        queryBuilder.addChild(leftJoinNode, trueNode, NonCommutativeOperatorNode.ArgumentPosition.LEFT);
        ExtensionalDataNode extensionalDataNode = new ExtensionalDataNodeImpl(P1_X_ATOM);
        queryBuilder.addChild(leftJoinNode, extensionalDataNode, NonCommutativeOperatorNode.ArgumentPosition.RIGHT);

        IntermediateQuery inputQuery = queryBuilder.build();

        System.out.println("input query:\n" + inputQuery.getProjectionAtom() + ":-\n" +
                inputQuery);

        DatalogProgram dp = IntermediateQueryToDatalogTranslator.translate(inputQuery);
        System.out.println(dp);
        dp = FunctionFlattener.flattenDatalogProgram(dp);
        System.out.println(dp);
        DBMetadata dbMetadata = DBMetadataExtractor.createDummyMetadata();

        QuotedIDFactory idfac = dbMetadata.getQuotedIDFactory();
        dbMetadata.createDatabaseRelation(idfac.createRelationID("p1",""));
        SQLQueryGenerator sqlQueryGenerator = new SQLGenerator(dbMetadata, new PostgreSQLDialectAdapter());
        sqlQueryGenerator.generateSourceQuery(dp, ImmutableList.of("x"));
    }
}

