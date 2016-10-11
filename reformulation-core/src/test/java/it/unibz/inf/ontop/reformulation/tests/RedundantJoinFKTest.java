package it.unibz.inf.ontop.reformulation.tests;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.impl.AtomPredicateImpl;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import it.unibz.inf.ontop.model.impl.URITemplatePredicateImpl;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.ImmutableSubstitutionImpl;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.pivotalrepr.equivalence.IQSyntacticEquivalenceChecker;
import it.unibz.inf.ontop.pivotalrepr.impl.*;
import it.unibz.inf.ontop.pivotalrepr.impl.tree.DefaultIntermediateQueryBuilder;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.InnerJoinOptimizationProposalImpl;
import it.unibz.inf.ontop.sql.*;
import org.junit.Test;

import java.sql.Types;
import java.util.Optional;

import static junit.framework.TestCase.assertTrue;

/**
 * Optimizations for inner joins based on foreign keys
 */
public class RedundantJoinFKTest {

    private final static AtomPredicate TABLE1_PREDICATE = new AtomPredicateImpl("table1", 2);
    private final static AtomPredicate TABLE2_PREDICATE = new AtomPredicateImpl("table2", 2);
    private final static AtomPredicate ANS1_PREDICATE = new AtomPredicateImpl("ans1", 1);
    private final static OBDADataFactory DATA_FACTORY = OBDADataFactoryImpl.getInstance();
    private final static Variable X = DATA_FACTORY.getVariable("X");
    private final static Variable A = DATA_FACTORY.getVariable("A");
    private final static Variable B = DATA_FACTORY.getVariable("B");
    private final static Variable C = DATA_FACTORY.getVariable("C");
    private final static Variable D = DATA_FACTORY.getVariable("D");
    private final static Variable P1 = DATA_FACTORY.getVariable("P");

    private final static Variable M = DATA_FACTORY.getVariable("m");
    private final static Variable N = DATA_FACTORY.getVariable("n");

    private final MetadataForQueryOptimization metadata;

    private static URITemplatePredicate URI_PREDICATE_ONE_VAR =  new URITemplatePredicateImpl(2);
    private static Constant URI_TEMPLATE_STR_1 =  DATA_FACTORY.getConstantLiteral("http://example.org/ds1/{}");
    private static Constant URI_TEMPLATE_STR_2 =  DATA_FACTORY.getConstantLiteral("http://example.org/ds2/{}");

    public RedundantJoinFKTest() {
        metadata = initMetadata();
    }

    private static MetadataForQueryOptimization initMetadata() {

        ImmutableMultimap.Builder<AtomPredicate, ImmutableList<Integer>> uniqueKeyBuilder = ImmutableMultimap.builder();

        /**
         * Table 1: non-composite unique constraint and regular field
         */
        uniqueKeyBuilder.put(TABLE1_PREDICATE, ImmutableList.of(1));

        /**
         * build the FKs
         */
        DBMetadata dbMetadata = DBMetadataExtractor.createDummyMetadata();
        QuotedIDFactory idFactory = dbMetadata.getQuotedIDFactory();
        DatabaseRelationDefinition table1Def = dbMetadata.createDatabaseRelation(idFactory.createRelationID(null,
                TABLE1_PREDICATE.getName()));
        Attribute pk1 = table1Def.addAttribute(idFactory.createAttributeID("col1"), Types.INTEGER, null, false);
        table1Def.addAttribute(idFactory.createAttributeID("col2"), Types.INTEGER, null, false);
        table1Def.addUniqueConstraint(UniqueConstraint.primaryKeyOf(pk1));

        DatabaseRelationDefinition table2Def = dbMetadata.createDatabaseRelation(idFactory.createRelationID(null,
                TABLE2_PREDICATE.getName()));
        table2Def.addAttribute(idFactory.createAttributeID("col1"), Types.INTEGER, null, false);
        Attribute table2Col2 = table2Def.addAttribute(idFactory.createAttributeID("col2"), Types.INTEGER, null, false);
        table2Def.addForeignKeyConstraint(ForeignKeyConstraint.of("fk2-1", table2Col2, pk1));

        return new MetadataForQueryOptimizationImpl(dbMetadata,
                ImmutableMultimap.of(TABLE1_PREDICATE, ImmutableList.of(1)),
                new UriTemplateMatcher());
    }


    @Test
    public void testForeignKeyOptimization() throws EmptyQueryException {

        /**
         * Sub-query
         */
        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(metadata);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, A);
        ConstructionNode constructionNode = new ConstructionNodeImpl(projectionAtom.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(P1, generateURI1(A), C, generateURI1(D))), Optional.empty());
        queryBuilder.init(projectionAtom, constructionNode);

        InnerJoinNode joinNode = new InnerJoinNodeImpl(Optional.empty());
        queryBuilder.addChild(constructionNode, joinNode);
        ExtensionalDataNode dataNode1 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, A, B));
        ExtensionalDataNode dataNode2 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, D, A));

        queryBuilder.addChild(joinNode, dataNode1);
        queryBuilder.addChild(joinNode, dataNode2);

        IntermediateQuery query = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  query);

        IntermediateQuery optimizedQuery = query.applyProposal(new InnerJoinOptimizationProposalImpl(joinNode))
                .getResultingQuery();

        System.out.println("\n After optimization: \n" +  optimizedQuery);

        IntermediateQueryBuilder expectedQueryBuilder = new DefaultIntermediateQueryBuilder(metadata);
        DistinctVariableOnlyDataAtom projectionAtom1 = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, A);
        ConstructionNode constructionNode1 = new ConstructionNodeImpl(projectionAtom.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(P1, generateURI1(A), C, generateURI1(D))), Optional.empty());
        expectedQueryBuilder.init(projectionAtom1, constructionNode1);
        expectedQueryBuilder.addChild(constructionNode1, dataNode2);

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();

        System.out.println("\n Expected query: \n" +  expectedQuery);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, expectedQuery));
    }


    @Test
    public void testForeignKeyNonOptimization() throws EmptyQueryException {

        /**
         * Sub-query
         */
        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(metadata);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, A);
        ConstructionNode constructionNode = new ConstructionNodeImpl(projectionAtom.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(
                        P1, generateURI1(A),
                        C, generateURI1(D),
                        X, generateURI1(B))), Optional.empty());
        queryBuilder.init(projectionAtom, constructionNode);

        InnerJoinNode joinNode = new InnerJoinNodeImpl(Optional.empty());
        queryBuilder.addChild(constructionNode, joinNode);
        ExtensionalDataNode dataNode1 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, A, B));
        ExtensionalDataNode dataNode2 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, D, A));

        queryBuilder.addChild(joinNode, dataNode1);
        queryBuilder.addChild(joinNode, dataNode2);

        IntermediateQuery query = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  query);

        IntermediateQuery expectedQuery = query.createSnapshot();
        System.out.println("\n Expected query: \n" +  expectedQuery);

        IntermediateQuery optimizedQuery = query.applyProposal(new InnerJoinOptimizationProposalImpl(joinNode))
                .getResultingQuery();

        System.out.println("\n After optimization: \n" +  optimizedQuery);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, expectedQuery));
    }

    private static ImmutableFunctionalTerm generateURI1(VariableOrGroundTerm argument) {
        return DATA_FACTORY.getImmutableFunctionalTerm(URI_PREDICATE_ONE_VAR, URI_TEMPLATE_STR_1, argument);
    }

    private static ImmutableFunctionalTerm generateURI2(VariableOrGroundTerm argument) {
        return DATA_FACTORY.getImmutableFunctionalTerm(URI_PREDICATE_ONE_VAR, URI_TEMPLATE_STR_2, argument);
    }
}
