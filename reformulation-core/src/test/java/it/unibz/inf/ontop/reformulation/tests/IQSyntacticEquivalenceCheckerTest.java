package it.unibz.inf.ontop.reformulation.tests;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import fj.P;
import fj.P2;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.impl.AtomPredicateImpl;
import it.unibz.inf.ontop.model.impl.GroundExpressionImpl;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import it.unibz.inf.ontop.model.impl.OBDAVocabulary;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.pivotalrepr.equivalence.IQSyntacticEquivalenceChecker;
import it.unibz.inf.ontop.pivotalrepr.impl.*;
import it.unibz.inf.ontop.pivotalrepr.impl.tree.DefaultIntermediateQueryBuilder;
import org.junit.Test;

import java.util.Optional;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.TestCase.assertTrue;

public class IQSyntacticEquivalenceCheckerTest {

    private final static AtomPredicate TABLE1_PREDICATE = new AtomPredicateImpl("table1", 3);
    private final static OBDADataFactory DATA_FACTORY = OBDADataFactoryImpl.getInstance();
    private final static Variable X = DATA_FACTORY.getVariable("X");
    private final static Variable y = DATA_FACTORY.getVariable("y");
    private final static Variable z = DATA_FACTORY.getVariable("z");
    private final static Constant two = DATA_FACTORY.getConstantLiteral("2");

    private final MetadataForQueryOptimization metadata;

    public IQSyntacticEquivalenceCheckerTest() {
        metadata = initMetadata();
    }

    private static MetadataForQueryOptimization initMetadata() {
        ImmutableMultimap.Builder<AtomPredicate, ImmutableList<Integer>> uniqueKeyBuilder = ImmutableMultimap.builder();

        /**
         * Table 1: non-composite key and regular field
         */
        uniqueKeyBuilder.put(TABLE1_PREDICATE, ImmutableList.of(1));

        return new MetadataForQueryOptimizationImpl(uniqueKeyBuilder.build(), new UriTemplateMatcher());
    }

    public static QueryNode getQueryNode(String type) {

        if (type.equals("constructionNode")) {
            return new ConstructionNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, X));
        } else if (type.equals("leftJoinNode")) {
            return new LeftJoinNodeImpl(Optional.<ImmutableExpression>empty());
        } else if (type.equals("groupNode")) {
            ImmutableList.Builder<NonGroundTerm> groupingTermBuilder = ImmutableList.builder();
            ImmutableList<NonGroundTerm> newGroupingTerms = groupingTermBuilder.build();
            return new GroupNodeImpl(newGroupingTerms);
        } else if (type.equals("filterNode")) {
            ImmutableExpression expression = DATA_FACTORY.getImmutableExpression(
                    ExpressionOperation.IS_TRUE, OBDAVocabulary.TRUE);
            return new FilterNodeImpl(expression);
        } else if (type.equals("innerJoinNode")) {
            return new InnerJoinNodeImpl(Optional.<ImmutableExpression>empty());
        } else if (type.equals("dataNode")) {
            return new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, X, y, z));
        } else if (type.equals("unionNode")) {
           return new UnionNodeImpl();
        }

        return null;
    }

    @Test
    public void testGroupNodeEquivalence() {
        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(metadata);
        ConstructionNode constructionNode = (ConstructionNode) getQueryNode("constructionNode");
        queryBuilder.init(constructionNode);
        GroupNode groupNode = (GroupNode) getQueryNode("groupNode");
        queryBuilder.addChild(constructionNode, groupNode);

        System.out.println();

    }

    @Test
    public void testEquivalence() {

        P2<IntermediateQueryBuilder, InnerJoinNode> initPair = initAns1(metadata);
        IntermediateQueryBuilder queryBuilder = initPair._1();
        InnerJoinNode joinNode = initPair._2();

        ExtensionalDataNode dataNode1 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, X, y, z));
        queryBuilder.addChild(joinNode, dataNode1);
        ExtensionalDataNode dataNode2 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, X, y, two));
        queryBuilder.addChild(joinNode, dataNode2);

        IntermediateQuery query = queryBuilder.build();

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(query, query));

        ConstructionNode constructionNode = new ConstructionNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, X));
        IntermediateQueryBuilder queryBuilder1 = new DefaultIntermediateQueryBuilder(metadata);
        queryBuilder1.init(constructionNode);

        IntermediateQuery query1 = queryBuilder1.build();

        assertFalse(IQSyntacticEquivalenceChecker.areEquivalent(query, query1));

    }

    @Test
    public void testConstructionNodeEquivalence() {

        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(metadata);
        ConstructionNode constructionNode = new ConstructionNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, X));
        queryBuilder.init(constructionNode);
        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder queryBuilder1 = new DefaultIntermediateQueryBuilder(metadata);
        ConstructionNode constructionNode1 = new ConstructionNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, X));
        queryBuilder1.init(constructionNode1);
        IntermediateQuery query1 = queryBuilder1.build();

        IntermediateQueryBuilder queryBuilder2 = new DefaultIntermediateQueryBuilder(metadata);
        ConstructionNode constructionNode2 = new ConstructionNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, y));
        queryBuilder2.init(constructionNode2);
        IntermediateQuery query2 = queryBuilder2.build();

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(query, query1));

        assertFalse(IQSyntacticEquivalenceChecker.areEquivalent(query, query2));
    }

    @Test
    public void testLeftJoinNodeEquivalence() {

        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(metadata);
        ConstructionNode constructionNode = new ConstructionNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, X));
        queryBuilder.init(constructionNode);
        LeftJoinNode leftJoinNode = new LeftJoinNodeImpl(Optional.<ImmutableExpression>empty());
        queryBuilder.addChild(constructionNode, leftJoinNode);
        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder queryBuilder1 = new DefaultIntermediateQueryBuilder(metadata);
        ConstructionNode constructionNode1 = new ConstructionNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, X));
        queryBuilder1.init(constructionNode1);
        LeftJoinNode leftJoinNode1 = new LeftJoinNodeImpl(Optional.<ImmutableExpression>empty());
        queryBuilder1.addChild(constructionNode1, leftJoinNode1);
        IntermediateQuery query1 = queryBuilder.build();

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(query, query1));

    }

    @Test
    public void testInnerJoinNodeEquivalence() {

        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(metadata);
        ConstructionNode constructionNode = new ConstructionNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, X));
        queryBuilder.init(constructionNode);
        InnerJoinNode joinNode = new InnerJoinNodeImpl(Optional.<ImmutableExpression>empty());
        queryBuilder.addChild(constructionNode, joinNode);
        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder queryBuilder1 = new DefaultIntermediateQueryBuilder(metadata);
        ConstructionNode constructionNode1 = new ConstructionNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, X));
        queryBuilder1.init(constructionNode1);
        InnerJoinNode joinNode1 = new InnerJoinNodeImpl(Optional.<ImmutableExpression>empty());
        queryBuilder1.addChild(constructionNode1, joinNode1);
        IntermediateQuery query1 = queryBuilder.build();

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(query, query1));
    }



    private static P2<IntermediateQueryBuilder, InnerJoinNode> initAns1(MetadataForQueryOptimization metadata) throws IntermediateQueryBuilderException {
        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(metadata);

        DataAtom ans1Atom = DATA_FACTORY.getDataAtom(new AtomPredicateImpl("ans1", 1), y);
        ConstructionNode rootNode = new ConstructionNodeImpl(ans1Atom);
        queryBuilder.init(rootNode);
        InnerJoinNode joinNode = new InnerJoinNodeImpl(Optional.<ImmutableExpression>empty());
        queryBuilder.addChild(rootNode, joinNode);

        return P.p(queryBuilder, joinNode);
    }

}
