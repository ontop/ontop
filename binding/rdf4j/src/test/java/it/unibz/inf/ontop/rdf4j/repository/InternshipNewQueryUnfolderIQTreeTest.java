package it.unibz.inf.ontop.rdf4j.repository;

import com.google.common.collect.ImmutableList;
import com.google.inject.Injector;
import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.NaryIQTree;
import it.unibz.inf.ontop.iq.node.InnerJoinNode;
import it.unibz.inf.ontop.iq.node.IntensionalDataNode;
import it.unibz.inf.ontop.iq.tools.UnionBasedQueryMerger;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.vocabulary.RDF;
import it.unibz.inf.ontop.query.unfolding.QueryUnfolder;
import it.unibz.inf.ontop.query.unfolding.impl.InternshipQueryUnfolder;
import it.unibz.inf.ontop.spec.OBDASpecification;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class InternshipNewQueryUnfolderIQTreeTest {
    private static final String OBDA_FILE = "/new-unfolder/new-unfolder.obda";
    //private static final String SQL_SCRIPT = "/new-unfolder/schema.sql";
    private static final String ONTOLOGY_FILE = "/new-unfolder/new-unfolder.owl";
    private static final String PROPERTIES_FILE = "/new-unfolder/new-unfolder.properties";
    private static final String DB_METADATA_FILE = "/new-unfolder/db-metadata.json";

    private static final String USER = "sa";
    private static final String PASSWORD = "";

    public static final IntermediateQueryFactory IQ_FACTORY;
    public static final InternshipQueryUnfolder INTERNSHIP_UNFOLDER;
    public static final Mapping MAPPING;
    public static final AtomFactory ATOM_FACTORY;
    public static final TermFactory TERM_FACTORY;
    public static final QueryUnfolder.Factory QUERYUNFOLDER_FACTORY;
    public static final UnionBasedQueryMerger UNIONBASEDQUERYMERGER;
    public static final AtomPredicate ANS1_AR0_PREDICATE, ANS1_AR1_PREDICATE, ANS1_AR2_PREDICATE, ANS1_AR3_PREDICATE;
    public static final Variable A;
    public static final Variable B;
    public static final Variable C;
    public static final IRIConstant MUN_IRI_CONST, GEO_IRI_CONST, RDF_TYPE, CLASS_MUNICIPALITY;

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
        GEO_IRI_CONST = TERM_FACTORY.getConstantIRI("http://schema.org/geo");
        RDF_TYPE = TERM_FACTORY.getConstantIRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
        CLASS_MUNICIPALITY = TERM_FACTORY.getConstantIRI("http://destination.example.org/ontology/dest#Municipality");
        try {
            OBDASpecification specification = config.loadSpecification();
            MAPPING = specification.getSaturatedMapping();
            INTERNSHIP_UNFOLDER = (InternshipQueryUnfolder) QUERYUNFOLDER_FACTORY.create(MAPPING);
        } catch (OBDASpecificationException e) {
            throw new RuntimeException(e);
        }
    }

    private DistinctVariableOnlyDataAtom automaticallyConstructedProjectAtom(VariableOrGroundTerm sub, VariableOrGroundTerm pred, VariableOrGroundTerm obj){
        int countVar = 0;
        var immutableList = ImmutableList.<Variable>builder();
        AtomPredicate atomPredicate = null;
        if (sub instanceof Variable){
            ++countVar;
            immutableList.add((Variable) sub);
        }
        if (pred instanceof Variable){
            ++countVar;
            immutableList.add((Variable) pred);
        }
        if (obj instanceof Variable){
            ++countVar;
            immutableList.add((Variable) obj);
        }
        atomPredicate = (countVar == 0) ? ANS1_AR0_PREDICATE : atomPredicate;
        atomPredicate = (countVar == 1) ? ANS1_AR1_PREDICATE : atomPredicate;
        atomPredicate = (countVar == 2) ? ANS1_AR2_PREDICATE : atomPredicate;
        atomPredicate = (countVar == 3) ? ANS1_AR3_PREDICATE : atomPredicate;
        return ATOM_FACTORY.getDistinctVariableOnlyDataAtom(atomPredicate, immutableList.build());
    }

    private void checkForSimpleTripleUnfoldingInFirstPhase(VariableOrGroundTerm sub, VariableOrGroundTerm pred, VariableOrGroundTerm obj){
        boolean isIQUnfoldedAfterFirstPhase = true;

        DistinctVariableOnlyDataAtom projectionAtom = automaticallyConstructedProjectAtom(sub, pred, obj);
        IntensionalDataNode intensionalDataNode = IQ_FACTORY.createIntensionalDataNode(ATOM_FACTORY.getIntensionalTripleAtom(sub, pred, obj));
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, intensionalDataNode);

        IQTree partiallyUnfoldedIQ = INTERNSHIP_UNFOLDER.executeFirstPhaseUnfolding(initialIQ.getTree());

        if (partiallyUnfoldedIQ.getRootNode() instanceof IntensionalDataNode)
            isIQUnfoldedAfterFirstPhase = false;

        assertEquals(true, isIQUnfoldedAfterFirstPhase);
    }

    private void checkForSimpleTripleUnfoldingInSecondPhase(VariableOrGroundTerm sub, VariableOrGroundTerm pred, VariableOrGroundTerm obj){
        boolean isIQUnfoldedAfterFirstPhase = false;
        boolean isIQUnfoldedAfterSecondPhase = true;

        DistinctVariableOnlyDataAtom projectionAtom = automaticallyConstructedProjectAtom(sub, pred, obj);
        IntensionalDataNode intensionalDataNode = IQ_FACTORY.createIntensionalDataNode(ATOM_FACTORY.getIntensionalTripleAtom(sub, pred, obj));
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, intensionalDataNode);

        IQTree partiallyUnfoldedIQ = INTERNSHIP_UNFOLDER.executeFirstPhaseUnfolding(initialIQ.getTree());
        IQTree unfoldedIQ = INTERNSHIP_UNFOLDER.executeSecondPhaseUnfoldingIfNecessary(partiallyUnfoldedIQ);

        if (!(partiallyUnfoldedIQ.getRootNode() instanceof IntensionalDataNode))
            isIQUnfoldedAfterFirstPhase = true;
        if (unfoldedIQ.getChildren().size() <= 0)
            isIQUnfoldedAfterSecondPhase = false;

        assertEquals(false, isIQUnfoldedAfterFirstPhase);
        assertEquals(true, isIQUnfoldedAfterSecondPhase);
    }

    @Test
    public void simpleTripleVarVarVarShouldBeUnfoldedInSecondPhase(){
        checkForSimpleTripleUnfoldingInSecondPhase(A, B, C);
    }

    @Test
    public void simpleTripleIriVarVarShouldBeUnfoldedInFirstPhase(){
        checkForSimpleTripleUnfoldingInFirstPhase(MUN_IRI_CONST, B, C);
    }

    @Test
    public void simpleTripleVarVarIriShouldBeUnfoldedInFirstPhase(){
        checkForSimpleTripleUnfoldingInFirstPhase(A, B, MUN_IRI_CONST);
    }

    @Test
    public void simpleTripleIriVarIriShouldBeUnfoldedInFirstPhase(){
        checkForSimpleTripleUnfoldingInFirstPhase(MUN_IRI_CONST, B, MUN_IRI_CONST);
    }

    @Test
    public void simpleTripleVarAVarShouldBeUnfoldedInSecondPhase(){
        checkForSimpleTripleUnfoldingInSecondPhase(A, RDF_TYPE, C);
    }

    @Test
    public void simpleTripleIriAVarShouldBeUnfoldedInFirstPhase(){
        checkForSimpleTripleUnfoldingInFirstPhase(MUN_IRI_CONST, RDF_TYPE, C);
    }

    @Test
    public void simpleTripleIriAIriShouldBeUnfoldedInFirstPhase(){
        checkForSimpleTripleUnfoldingInFirstPhase(MUN_IRI_CONST, RDF_TYPE, CLASS_MUNICIPALITY);
    }

    @Test
    public void simpleTripleVarAIriShouldBeUnfoldedInFirstPhase(){
        checkForSimpleTripleUnfoldingInFirstPhase(A, RDF_TYPE, CLASS_MUNICIPALITY);
    }

    @Test
    public void simpleTripleIriIriNotAIriShouldBeUnfoldedInFirstPhase(){
        checkForSimpleTripleUnfoldingInFirstPhase(MUN_IRI_CONST, GEO_IRI_CONST, MUN_IRI_CONST);
    }

    @Test
    public void simpleTripleIriIriNotAVarShouldBeUnfoldedInFirstPhase(){
        checkForSimpleTripleUnfoldingInFirstPhase(MUN_IRI_CONST, GEO_IRI_CONST, C);
    }

    @Test
    public void simpleTripleVarIriNotAIriShouldBeUnfoldedInFirstPhase(){
        checkForSimpleTripleUnfoldingInFirstPhase(A, GEO_IRI_CONST, MUN_IRI_CONST);
    }

    @Test
    public void simpleTripleVarIriNotAVarShouldBeUnfoldedInFirstPhase(){
        checkForSimpleTripleUnfoldingInFirstPhase(A, GEO_IRI_CONST, C);
    }

    @Test
    public void tripleVarVarVarUnfoldedMappingShouldBeSmallerThanAllDefinitions(){
        DistinctVariableOnlyDataAtom projectionAtom = automaticallyConstructedProjectAtom(A, B, C);
        IntensionalDataNode varAIriClass = IQ_FACTORY.createIntensionalDataNode(ATOM_FACTORY.getIntensionalTripleAtom(A, RDF.TYPE, CLASS_MUNICIPALITY));
        IntensionalDataNode varVarVar = IQ_FACTORY.createIntensionalDataNode(ATOM_FACTORY.getIntensionalTripleAtom(A, B, C));
        InnerJoinNode join = IQ_FACTORY.createInnerJoinNode();
        NaryIQTree naryIQTree = IQ_FACTORY.createNaryIQTree(join, ImmutableList.of(varAIriClass, varVarVar));
        IQ iq = IQ_FACTORY.createIQ(projectionAtom, naryIQTree);

        IQTree unfoldedIQTree = INTERNSHIP_UNFOLDER.optimize(iq).getTree();

    }

    @Test
    public void tripleVarAVarUnfoldedMappingShouldBeSmallerThanAllDefinitions(){
        DistinctVariableOnlyDataAtom projectionAtom = automaticallyConstructedProjectAtom(A, B, C);
        IntensionalDataNode varAIriClass = IQ_FACTORY.createIntensionalDataNode(ATOM_FACTORY.getIntensionalTripleAtom(A, RDF.TYPE, CLASS_MUNICIPALITY));
        IntensionalDataNode varVarVar = IQ_FACTORY.createIntensionalDataNode(ATOM_FACTORY.getIntensionalTripleAtom(A, RDF.TYPE, C));
        InnerJoinNode join = IQ_FACTORY.createInnerJoinNode();
        NaryIQTree naryIQTree = IQ_FACTORY.createNaryIQTree(join, ImmutableList.of(varAIriClass, varVarVar));
        IQ iq = IQ_FACTORY.createIQ(projectionAtom, naryIQTree);

    }
}
