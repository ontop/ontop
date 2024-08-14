package it.unibz.inf.ontop.rdf4j.repository;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Injector;
import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.NaryIQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.tools.UnionBasedQueryMerger;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.query.unfolding.QueryUnfolder;
import it.unibz.inf.ontop.spec.OBDASpecification;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assume.assumeTrue;

public class TwoPhaseQueryUnfolderIQTreeTest {
    private static final String OBDA_FILE = "/new-unfolder/new-unfolder.obda";
    //private static final String SQL_SCRIPT = "/new-unfolder/schema.sql";
    private static final String ONTOLOGY_FILE = "/new-unfolder/new-unfolder.owl";
    private static final String PROPERTIES_FILE = "/new-unfolder/new-unfolder.properties";
    private static final String DB_METADATA_FILE = "/new-unfolder/db-metadata.json";
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    public static final IntermediateQueryFactory IQ_FACTORY;
    public static final QueryUnfolder UNFOLDER;
    public static final Mapping MAPPING;
    public static final AtomFactory ATOM_FACTORY;
    public static final TermFactory TERM_FACTORY;
    public static final QueryUnfolder.Factory QUERYUNFOLDER_FACTORY;
    public static final UnionBasedQueryMerger UNIONBASEDQUERYMERGER;
    public static final AtomPredicate ANS1_AR0_PREDICATE, ANS1_AR1_PREDICATE, ANS1_AR2_PREDICATE, ANS1_AR3_PREDICATE;
    public static final Variable A;
    public static final Variable B;
    public static final Variable C;
    public static final IRIConstant MUN_IRI_CONST, HOS_IRI_CONST, GEO_IRI_CONST, RDF_TYPE, CLASS_MUNICIPALITY;
    public static final long LEAF_DEF_IN_MAPPING_ALL_DEF;
    public static final long LEAF_DEF_IN_MAPPING_ALL_CLASS;
    public static final CoreUtilsFactory CORE_UTILS_FACTORY;

    static {
        OntopSQLOWLAPIConfiguration.Builder<?> builder = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .nativeOntopMappingFile(AbstractRDF4JTest.class.getResource(OBDA_FILE).getPath())
                .jdbcUrl(H2RDF4JTestTools.generateJdbcUrl())
                .enableTestMode();
        builder.ontologyFile(AbstractRDF4JTest.class.getResource(ONTOLOGY_FILE).getPath());
        builder.propertyFile(AbstractRDF4JTest.class.getResource(PROPERTIES_FILE).getPath())
                .jdbcUser(USER)
                .jdbcPassword(PASSWORD)
                .dbMetadataFile(AbstractRDF4JTest.class.getResource(DB_METADATA_FILE).getPath());
        OntopSQLOWLAPIConfiguration config = builder.build();
        Injector injector = config.getInjector();

        CORE_UTILS_FACTORY = injector.getInstance(CoreUtilsFactory.class);
        IQ_FACTORY = injector.getInstance(IntermediateQueryFactory.class);
        ATOM_FACTORY = injector.getInstance(AtomFactory.class);
        TERM_FACTORY = injector.getInstance(TermFactory.class);
        QUERYUNFOLDER_FACTORY = injector.getInstance(QueryUnfolder.Factory.class);
        UNIONBASEDQUERYMERGER = injector.getInstance(UnionBasedQueryMerger.class);
        A = TERM_FACTORY.getVariable("a");
        B = TERM_FACTORY.getVariable("b");
        C = TERM_FACTORY.getVariable("c");
        ANS1_AR3_PREDICATE = ATOM_FACTORY.getRDFAnswerPredicate(3);
        ANS1_AR2_PREDICATE = ATOM_FACTORY.getRDFAnswerPredicate(2);
        ANS1_AR1_PREDICATE = ATOM_FACTORY.getRDFAnswerPredicate(1);
        ANS1_AR0_PREDICATE = ATOM_FACTORY.getRDFAnswerPredicate(0);
        MUN_IRI_CONST = TERM_FACTORY.getConstantIRI("http://destination.example.org/data/municipality/021118");
        HOS_IRI_CONST = TERM_FACTORY.getConstantIRI("http://destination.example.org/data/source1/hospitality/A1B9B1850E0B035D21D93D3FCD3AA257");
        GEO_IRI_CONST = TERM_FACTORY.getConstantIRI("http://schema.org/geo");
        RDF_TYPE = TERM_FACTORY.getConstantIRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
        CLASS_MUNICIPALITY = TERM_FACTORY.getConstantIRI("http://destination.example.org/ontology/dest#Municipality");
        LEAF_DEF_IN_MAPPING_ALL_DEF = 398;
        LEAF_DEF_IN_MAPPING_ALL_CLASS = 277;
        try {
            OBDASpecification specification = config.loadSpecification();
            MAPPING = specification.getSaturatedMapping();
            UNFOLDER = QUERYUNFOLDER_FACTORY.create(MAPPING);
        } catch (OBDASpecificationException e) {
            throw new RuntimeException(e);
        }
    }

    private DistinctVariableOnlyDataAtom automaticallyConstructedProjectAtom(ImmutableList<VariableOrGroundTerm> variableOrGroundTermImmutableList){
        int countVar = Math.toIntExact(variableOrGroundTermImmutableList.stream().filter(e -> (e instanceof Variable)).count());
        AtomPredicate atomPredicate = null;
        atomPredicate = (countVar == 0) ? ANS1_AR0_PREDICATE : atomPredicate;
        atomPredicate = (countVar == 1) ? ANS1_AR1_PREDICATE : atomPredicate;
        atomPredicate = (countVar == 2) ? ANS1_AR2_PREDICATE : atomPredicate;
        atomPredicate = (countVar == 3) ? ANS1_AR3_PREDICATE : atomPredicate;
        return ATOM_FACTORY.getDistinctVariableOnlyDataAtom(atomPredicate, (ImmutableList<Variable>) variableOrGroundTermImmutableList.stream().filter(e -> (e instanceof Variable)).map(e -> (Variable) e).collect(ImmutableList.toImmutableList()));
    }

    private int countExtensionalAndValuesNode(IQTree iqTree){
        if (iqTree instanceof ExtensionalDataNode || iqTree instanceof ValuesNode) {
            return 1;
        }
        if (iqTree.isLeaf()) {
            return 0;
        }
        return iqTree.getChildren().stream().mapToInt(this::countExtensionalAndValuesNode).sum();
    }

    @Test
    public void spoUnfoldedCorrectlyWithAllDefinitions(){
        long counterOfBasicQueryUnfolderExtensionalAndValuesNode = 277;
        DistinctVariableOnlyDataAtom projectionAtom = automaticallyConstructedProjectAtom(ImmutableList.of(A, C));
        InnerJoinNode innerJoinNode = IQ_FACTORY.createInnerJoinNode();
        IQTree rootIQ = IQ_FACTORY.createNaryIQTree(innerJoinNode, ImmutableList.of(
                IQ_FACTORY.createIntensionalDataNode(ATOM_FACTORY.getIntensionalTripleAtom(A, RDF_TYPE, CLASS_MUNICIPALITY)),
                IQ_FACTORY.createIntensionalDataNode(ATOM_FACTORY.getIntensionalTripleAtom(A, RDF_TYPE, C))
        ));

        IQ iq = IQ_FACTORY.createIQ(projectionAtom, rootIQ);
        IQ unfoldedIQ = UNFOLDER.optimize(iq);
        assertNotEquals(counterOfBasicQueryUnfolderExtensionalAndValuesNode, countExtensionalAndValuesNode(unfoldedIQ.getTree()));
    }

    @Test
    public void spoUnfoldedCorrectlyWithAllClassDefinitions(){
        long counterOfBasicQueryUnfolderExtensionalAndValuesNode = 398;
        DistinctVariableOnlyDataAtom projectionAtom = automaticallyConstructedProjectAtom(ImmutableList.of(A, B, C));
        InnerJoinNode innerJoinNode = IQ_FACTORY.createInnerJoinNode();
        IQTree rootIQ = IQ_FACTORY.createNaryIQTree(innerJoinNode, ImmutableList.of(
                IQ_FACTORY.createIntensionalDataNode(ATOM_FACTORY.getIntensionalTripleAtom(A, RDF_TYPE, CLASS_MUNICIPALITY)),
                IQ_FACTORY.createIntensionalDataNode(ATOM_FACTORY.getIntensionalTripleAtom(A, B, C))
        ));

        IQ iq = IQ_FACTORY.createIQ(projectionAtom, rootIQ);
        IQ unfoldedIQ = UNFOLDER.optimize(iq);
        assertNotEquals(counterOfBasicQueryUnfolderExtensionalAndValuesNode, countExtensionalAndValuesNode(unfoldedIQ.getTree()));
    }

    @Ignore("TODO: check the results")
    @Test
    public void joinStrictEqBarrierIfRanOnNormalizedIQBefore(){
        long counterOfBasicQueryUnfolderExtensionalAndValuesNode = 298;
        DistinctVariableOnlyDataAtom projectionAtom = automaticallyConstructedProjectAtom(ImmutableList.of(A,C));

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();
        InnerJoinNode innerJoinNode = IQ_FACTORY.createInnerJoinNode();
        UnionNode unionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(A));
        ConstructionNode constructionSecondTriple = IQ_FACTORY.createConstructionNode(ImmutableSet.of(A));
        ConstructionNode constructionThirdTriple = IQ_FACTORY.createConstructionNode(ImmutableSet.of(A));

        UnaryIQTree secondTripleIQ = IQ_FACTORY.createUnaryIQTree(constructionSecondTriple,
                IQ_FACTORY.createIntensionalDataNode(ATOM_FACTORY.getIntensionalTripleAtom(A, B, HOS_IRI_CONST)));
        UnaryIQTree thirdTripleIQ = IQ_FACTORY.createUnaryIQTree(constructionThirdTriple,
                IQ_FACTORY.createIntensionalDataNode(ATOM_FACTORY.getIntensionalTripleAtom(HOS_IRI_CONST, B, A)));
        NaryIQTree unionSubTree = IQ_FACTORY.createNaryIQTree(unionNode, ImmutableList.of(secondTripleIQ, thirdTripleIQ));
        NaryIQTree naryIQTree = IQ_FACTORY.createNaryIQTree(innerJoinNode, ImmutableList.of(IQ_FACTORY.createIntensionalDataNode(ATOM_FACTORY.getIntensionalTripleAtom(A, RDF_TYPE, C)), unionSubTree));

        IQTree rootIQ = IQ_FACTORY.createUnaryIQTree(distinctNode, naryIQTree);

        IQ iq = IQ_FACTORY.createIQ(projectionAtom, rootIQ);
        IQ unfoldedIQ = UNFOLDER.optimize(iq);
        assertNotEquals(counterOfBasicQueryUnfolderExtensionalAndValuesNode, countExtensionalAndValuesNode(unfoldedIQ.getTree()));
    }

    /*
    PLEASE DO NOT REMOVE THIS

    private void unfoldAndCompareSecondPhaseSimpleTripleWithStandardUnfolding(VariableOrGroundTerm sub, VariableOrGroundTerm pred, VariableOrGroundTerm obj, long expected){
        DistinctVariableOnlyDataAtom projectionAtom = automaticallyConstructedProjectAtom(ImmutableList.of(sub, pred, obj));
        IntensionalDataNode varAIriClass = IQ_FACTORY.createIntensionalDataNode(ATOM_FACTORY.getIntensionalTripleAtom(A, RDF.TYPE, CLASS_MUNICIPALITY));
        IntensionalDataNode varVarVar = IQ_FACTORY.createIntensionalDataNode(ATOM_FACTORY.getIntensionalTripleAtom(sub, pred, obj));
        InnerJoinNode join = IQ_FACTORY.createInnerJoinNode();
        NaryIQTree naryIQTree = IQ_FACTORY.createNaryIQTree(join, ImmutableList.of(varAIriClass, varVarVar));
        IQ iq = IQ_FACTORY.createIQ(projectionAtom, naryIQTree);

        IQTree unfoldedIQTree = UNFOLDER.optimize(iq).getTree();
        assertNotEquals(expected, countExtensionalAndValuesNode(unfoldedIQTree));
    }

    private void unfoldAndCompareFirstPhaseSimpleTripleWithStandardUnfolding(VariableOrGroundTerm sub, VariableOrGroundTerm pred, VariableOrGroundTerm obj, long expected){
        DistinctVariableOnlyDataAtom projectionAtom = automaticallyConstructedProjectAtom(ImmutableList.of(sub, pred, obj));
        IntensionalDataNode iriVarVar = IQ_FACTORY.createIntensionalDataNode(ATOM_FACTORY.getIntensionalTripleAtom(sub, pred, obj));
        IQ iq = IQ_FACTORY.createIQ(projectionAtom, iriVarVar);

        IQTree unfoldedIQTree = UNFOLDER.optimize(iq).getTree();
        assertEquals(expected, countExtensionalAndValuesNode(unfoldedIQTree));
    }

    private void checkForSimpleTripleUnfoldingInFirstPhase(VariableOrGroundTerm sub, VariableOrGroundTerm pred, VariableOrGroundTerm obj){
        boolean isIQUnfoldedAfterFirstPhase = true;

        DistinctVariableOnlyDataAtom projectionAtom = automaticallyConstructedProjectAtom(ImmutableList.of(sub, pred, obj));
        IntensionalDataNode intensionalDataNode = IQ_FACTORY.createIntensionalDataNode(ATOM_FACTORY.getIntensionalTripleAtom(sub, pred, obj));
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, intensionalDataNode);

        IQTree partiallyUnfoldedIQ = ((InternshipQueryUnfolder)UNFOLDER).executeFirstPhaseUnfolding(initialIQ.getTree());

        if (partiallyUnfoldedIQ.getRootNode() instanceof IntensionalDataNode)
            isIQUnfoldedAfterFirstPhase = false;

        assertEquals(true, isIQUnfoldedAfterFirstPhase);
    }

    private void checkForSimpleTripleUnfoldingInSecondPhase(VariableOrGroundTerm sub, VariableOrGroundTerm pred, VariableOrGroundTerm obj){
        boolean isIQUnfoldedAfterFirstPhase = false;
        boolean isIQUnfoldedAfterSecondPhase = true;

        DistinctVariableOnlyDataAtom projectionAtom = automaticallyConstructedProjectAtom(ImmutableList.of(sub, pred, obj));
        IntensionalDataNode intensionalDataNode = IQ_FACTORY.createIntensionalDataNode(ATOM_FACTORY.getIntensionalTripleAtom(sub, pred, obj));
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, intensionalDataNode);

        IQTree partiallyUnfoldedIQ = ((InternshipQueryUnfolder)UNFOLDER).executeFirstPhaseUnfolding(initialIQ.getTree());
        IQTree unfoldedIQ = ((InternshipQueryUnfolder)UNFOLDER).executeSecondPhaseUnfolding(partiallyUnfoldedIQ);

        if (!(partiallyUnfoldedIQ.getRootNode() instanceof IntensionalDataNode))
            isIQUnfoldedAfterFirstPhase = true;
        if (unfoldedIQ.getChildren().size() <= 0)
            isIQUnfoldedAfterSecondPhase = false;

        assertEquals(false, isIQUnfoldedAfterFirstPhase);
        assertEquals(true, isIQUnfoldedAfterSecondPhase);
    }

    private boolean isInternshipQueryUnfolder(){
        return (UNFOLDER instanceof InternshipQueryUnfolder);
    }

    @Test
    public void simpleTripleVarVarVarShouldBeUnfoldedInSecondPhase(){
        assumeTrue("Condition is not met, skipping test", isInternshipQueryUnfolder());
        checkForSimpleTripleUnfoldingInSecondPhase(A, B, C);
    }

    @Test
    public void simpleTripleIriVarVarShouldBeUnfoldedInFirstPhase(){
        assumeTrue("Condition is not met, skipping test", isInternshipQueryUnfolder());
        checkForSimpleTripleUnfoldingInFirstPhase(MUN_IRI_CONST, B, C);
    }

    @Test
    public void simpleTripleVarVarIriShouldBeUnfoldedInFirstPhase(){
        assumeTrue("Condition is not met, skipping test", isInternshipQueryUnfolder());
        checkForSimpleTripleUnfoldingInFirstPhase(A, B, MUN_IRI_CONST);
    }

    @Test
    public void simpleTripleIriVarIriShouldBeUnfoldedInFirstPhase(){
        assumeTrue("Condition is not met, skipping test", isInternshipQueryUnfolder());
        checkForSimpleTripleUnfoldingInFirstPhase(MUN_IRI_CONST, B, MUN_IRI_CONST);
    }

    @Test
    public void simpleTripleVarAVarShouldBeUnfoldedInSecondPhase(){
        assumeTrue("Condition is not met, skipping test", isInternshipQueryUnfolder());
        checkForSimpleTripleUnfoldingInSecondPhase(A, RDF_TYPE, C);
    }

    @Test
    public void simpleTripleIriAVarShouldBeUnfoldedInFirstPhase(){
        assumeTrue("Condition is not met, skipping test", isInternshipQueryUnfolder());
        checkForSimpleTripleUnfoldingInFirstPhase(MUN_IRI_CONST, RDF_TYPE, C);
    }

    @Test
    public void simpleTripleIriAIriShouldBeUnfoldedInFirstPhase(){
        assumeTrue("Condition is not met, skipping test", isInternshipQueryUnfolder());
        checkForSimpleTripleUnfoldingInFirstPhase(MUN_IRI_CONST, RDF_TYPE, CLASS_MUNICIPALITY);
    }

    @Test
    public void simpleTripleVarAIriShouldBeUnfoldedInFirstPhase(){
        assumeTrue("Condition is not met, skipping test", isInternshipQueryUnfolder());
        checkForSimpleTripleUnfoldingInFirstPhase(A, RDF_TYPE, CLASS_MUNICIPALITY);
    }

    @Test
    public void simpleTripleIriIriNotAIriShouldBeUnfoldedInFirstPhase(){
        assumeTrue("Condition is not met, skipping test", isInternshipQueryUnfolder());
        checkForSimpleTripleUnfoldingInFirstPhase(MUN_IRI_CONST, GEO_IRI_CONST, MUN_IRI_CONST);
    }

    @Test
    public void simpleTripleIriIriNotAVarShouldBeUnfoldedInFirstPhase(){
        assumeTrue("Condition is not met, skipping test", isInternshipQueryUnfolder());
        checkForSimpleTripleUnfoldingInFirstPhase(MUN_IRI_CONST, GEO_IRI_CONST, C);
    }

    @Test
    public void simpleTripleVarIriNotAIriShouldBeUnfoldedInFirstPhase(){
        assumeTrue("Condition is not met, skipping test", isInternshipQueryUnfolder());
        checkForSimpleTripleUnfoldingInFirstPhase(A, GEO_IRI_CONST, MUN_IRI_CONST);
    }

    @Test
    public void simpleTripleVarIriNotAVarShouldBeUnfoldedInFirstPhase(){
        assumeTrue("Condition is not met, skipping test", isInternshipQueryUnfolder());
        checkForSimpleTripleUnfoldingInFirstPhase(A, GEO_IRI_CONST, C);
    }

    @Test
    public void tripleVarVarVarUnfoldedMappingShouldBeSmallerThanAllDefinitions(){
        unfoldAndCompareSecondPhaseSimpleTripleWithStandardUnfolding(A, B, C, LEAF_DEF_IN_MAPPING_ALL_DEF + 1);
    }

    @Test
    public void tripleVarAVarUnfoldedMappingShouldBeSmallerThanAllDefinitions(){
        unfoldAndCompareSecondPhaseSimpleTripleWithStandardUnfolding(A, RDF_TYPE, C, LEAF_DEF_IN_MAPPING_ALL_CLASS + 1);
    }

    @Test
    public void tripleIriVarVarUnfoldedMappingShouldBeTheSame(){
        unfoldAndCompareFirstPhaseSimpleTripleWithStandardUnfolding(MUN_IRI_CONST, B, C, 8);
    }

    @Test
    public void tripleVarVarIriUnfoldedMappingShouldBeTheSame(){
        unfoldAndCompareFirstPhaseSimpleTripleWithStandardUnfolding(A, B, MUN_IRI_CONST, 3);
    }

    @Test
    public void tripleIriVarIriUnfoldedMappingShouldBeTheSame(){
        unfoldAndCompareFirstPhaseSimpleTripleWithStandardUnfolding(MUN_IRI_CONST, B, MUN_IRI_CONST, 0);
    }

    @Test
    public void tripleIriAVarUnfoldedMappingShouldBeTheSame(){
        unfoldAndCompareFirstPhaseSimpleTripleWithStandardUnfolding(MUN_IRI_CONST, RDF_TYPE, C, 3);
    }
    */
}
