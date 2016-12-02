package it.unibz.inf.ontop.reformulation.tests;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Injector;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.impl.AtomPredicateImpl;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import it.unibz.inf.ontop.model.impl.URITemplatePredicateImpl;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.ImmutableSubstitutionImpl;
import it.unibz.inf.ontop.owlrefplatform.core.optimization.SubstitutionLiftOptimizer;
import it.unibz.inf.ontop.owlrefplatform.core.optimization.TopDownSubstitutionLiftOptimizer;
import it.unibz.inf.ontop.owlrefplatform.injection.QuestCoreConfiguration;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.pivotalrepr.impl.*;
import it.unibz.inf.ontop.pivotalrepr.impl.tree.DefaultIntermediateQueryBuilder;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Optional;

import static it.unibz.inf.ontop.model.ExpressionOperation.CONCAT;
import static it.unibz.inf.ontop.model.ExpressionOperation.EQ;
import static it.unibz.inf.ontop.pivotalrepr.NonCommutativeOperatorNode.ArgumentPosition.LEFT;
import static it.unibz.inf.ontop.pivotalrepr.NonCommutativeOperatorNode.ArgumentPosition.RIGHT;

public class UriTemplateTest {

    private final static OBDADataFactory DATA_FACTORY = OBDADataFactoryImpl.getInstance();
    private static URITemplatePredicate URI_PREDICATE_ONE_VAR =  new URITemplatePredicateImpl(2);
    private static Constant URI_TEMPLATE_STR_1_PREFIX =  DATA_FACTORY.getConstantLiteral("http://example.org/ds1/");
    private static Constant URI_TEMPLATE_STR_1 =  DATA_FACTORY.getConstantLiteral(URI_TEMPLATE_STR_1_PREFIX.getValue() + "{}");
    private static Constant URI_TEMPLATE_STR_2_PREFIX =  DATA_FACTORY.getConstantLiteral("http://example.org/ds2/");
    private static Constant URI_TEMPLATE_STR_2 =  DATA_FACTORY.getConstantLiteral(URI_TEMPLATE_STR_2_PREFIX.getValue() + "{}");
    private static Constant URI_TEMPLATE_STR_3 =  DATA_FACTORY.getConstantLiteral("{}");

    private final static AtomPredicate TABLE1_PREDICATE = new AtomPredicateImpl("T1", 2);
    private final static AtomPredicate TABLE2_PREDICATE = new AtomPredicateImpl("T2", 2);
    private final static AtomPredicate TABLE3_PREDICATE = new AtomPredicateImpl("T3", 1);

    private final static Variable X = DATA_FACTORY.getVariable("x");
    private final static Variable Y = DATA_FACTORY.getVariable("y");
    private final static Variable Z = DATA_FACTORY.getVariable("z");
    private final static Variable A = DATA_FACTORY.getVariable("a");
    private final static Variable B = DATA_FACTORY.getVariable("b");
    private final static Variable C = DATA_FACTORY.getVariable("c");
    private final static Variable D = DATA_FACTORY.getVariable("d");
    private final static Variable E = DATA_FACTORY.getVariable("e");
    private final static Variable F = DATA_FACTORY.getVariable("f");

    private final static AtomPredicate ANS1_PREDICATE_1 = new AtomPredicateImpl("ans1", 1);

    private final MetadataForQueryOptimization metadata;
    private static final Injector INJECTOR = QuestCoreConfiguration.defaultBuilder().build().getInjector();

    public UriTemplateTest() {
        metadata = initMetadata();
    }

    /**
     * TODO: init the UriTemplateMatcher
     */
    private static MetadataForQueryOptimization initMetadata() {
        return new EmptyMetadataForQueryOptimization();
    }

    @Ignore
    @Test
    public void testCompatibleUriTemplates1() throws EmptyQueryException {
        IntermediateQueryBuilder initialQueryBuilder = new DefaultIntermediateQueryBuilder(metadata, INJECTOR);

        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_1, X);
        ConstructionNode rootNode = new ConstructionNodeImpl(projectionAtom.getVariables());
        initialQueryBuilder.init(projectionAtom, rootNode);

        LeftJoinNode leftJoinNode = new LeftJoinNodeImpl(Optional.empty());
        initialQueryBuilder.addChild(rootNode, leftJoinNode);

        InnerJoinNode joinNode = new InnerJoinNodeImpl(Optional.empty());
        initialQueryBuilder.addChild(leftJoinNode, joinNode, LEFT);

        ConstructionNode leftConstructionNode = new ConstructionNodeImpl(ImmutableSet.of(X),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateOneVarURITemplate(URI_TEMPLATE_STR_1, A))),
                Optional.empty());
        initialQueryBuilder.addChild(joinNode, leftConstructionNode);

        ExtensionalDataNode leftDataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, A, B));
        initialQueryBuilder.addChild(leftConstructionNode, leftDataNode);

        ConstructionNode middleConstructionNode = new ConstructionNodeImpl(ImmutableSet.of(X),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateOneVarURITemplate(URI_TEMPLATE_STR_3, C))),
                Optional.empty());
        initialQueryBuilder.addChild(joinNode, middleConstructionNode);

        ExtensionalDataNode middleDataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, C, D));
        initialQueryBuilder.addChild(middleConstructionNode, middleDataNode);

        ConstructionNode rightConstructionNode = new ConstructionNodeImpl(ImmutableSet.of(X),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateOneVarURITemplate(URI_TEMPLATE_STR_2, E))),
                Optional.empty());
        initialQueryBuilder.addChild(leftJoinNode, rightConstructionNode, RIGHT);

        ExtensionalDataNode rightDataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE3_PREDICATE, E));
        initialQueryBuilder.addChild(rightConstructionNode, rightDataNode);

        IntermediateQuery initialQuery = initialQueryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  initialQuery);


        IntermediateQueryBuilder expectedQueryBuilder = new DefaultIntermediateQueryBuilder(metadata, INJECTOR);
        expectedQueryBuilder.init(projectionAtom, leftConstructionNode);

        InnerJoinNode newJoinNode = new InnerJoinNodeImpl(Optional.of(
                DATA_FACTORY.getImmutableExpression(EQ,
                        DATA_FACTORY.getImmutableExpression(CONCAT, URI_TEMPLATE_STR_1_PREFIX, A),
                        C)));

        expectedQueryBuilder.addChild(leftConstructionNode, newJoinNode);
        expectedQueryBuilder.addChild(newJoinNode, leftDataNode);
        expectedQueryBuilder.addChild(newJoinNode, middleDataNode);

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();
        System.out.println("\n Expected query : \n" +  expectedQuery);

        SubstitutionLiftOptimizer optimizer = new TopDownSubstitutionLiftOptimizer();
        IntermediateQuery optimizedQuery = optimizer.optimize(initialQuery);

        System.out.println("\n After optimization: \n" +  optimizedQuery);
    }


    private static ImmutableFunctionalTerm generateOneVarURITemplate(Constant templateString, ImmutableTerm value) {
        return DATA_FACTORY.getImmutableFunctionalTerm(URI_PREDICATE_ONE_VAR, templateString, value);
    }
}
