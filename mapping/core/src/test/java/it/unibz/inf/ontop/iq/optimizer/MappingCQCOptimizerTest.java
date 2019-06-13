package it.unibz.inf.ontop.iq.optimizer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Injector;
import it.unibz.inf.ontop.datalog.DatalogFactory;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.OntopModelConfiguration;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.IntermediateQueryBuilder;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.iq.node.InnerJoinNode;
import it.unibz.inf.ontop.iq.node.UnionNode;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.iq.tools.IQConverter;
import it.unibz.inf.ontop.iq.transform.NoNullValueEnforcer;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.atom.RelationPredicate;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.functionsymbol.ExpressionOperation;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.substitution.impl.UnifierUtilities;
import org.apache.commons.rdf.api.RDF;
import org.junit.Test;

import java.sql.Types;



public class MappingCQCOptimizerTest {


    private static final DBMetadata DB_METADATA;

    public static final IntermediateQueryFactory IQ_FACTORY;
    public static final ExecutorRegistry EXECUTOR_REGISTRY;
    public static final TermFactory TERM_FACTORY;
    public static final AtomFactory ATOM_FACTORY;
    public static final SubstitutionFactory SUBSTITUTION_FACTORY;
    public static final DatalogFactory DATALOG_FACTORY;
    public static final TypeFactory TYPE_FACTORY;
    public static final UnifierUtilities UNIFIER_UTILITIES;
    public static final NoNullValueEnforcer NO_NULL_VALUE_ENFORCER;
    public static final IQConverter IQ_CONVERTER;
    public static final RDF RDF_FACTORY;

    private static final DummyBasicDBMetadata DEFAULT_DUMMY_DB_METADATA;
    public static BasicDBMetadata createDummyMetadata() {
        return DEFAULT_DUMMY_DB_METADATA.clone();
    }

    static {
        OntopModelConfiguration defaultConfiguration = OntopModelConfiguration.defaultBuilder()
                .enableTestMode()
                .build();
        Injector injector = defaultConfiguration.getInjector();

        IQ_FACTORY = injector.getInstance(IntermediateQueryFactory.class);
        ATOM_FACTORY = injector.getInstance(AtomFactory.class);
        SUBSTITUTION_FACTORY = injector.getInstance(SubstitutionFactory.class);
        DATALOG_FACTORY = injector.getInstance(DatalogFactory.class);
        TERM_FACTORY = injector.getInstance(TermFactory.class);
        TYPE_FACTORY = injector.getInstance(TypeFactory.class);
        DEFAULT_DUMMY_DB_METADATA = injector.getInstance(DummyBasicDBMetadata.class);
        UNIFIER_UTILITIES = injector.getInstance(UnifierUtilities.class);
        IQ_CONVERTER = injector.getInstance(IQConverter.class);
        RDF_FACTORY = injector.getInstance(RDF.class);

        EXECUTOR_REGISTRY = defaultConfiguration.getExecutorRegistry();

        NO_NULL_VALUE_ENFORCER = injector.getInstance(NoNullValueEnforcer.class);
    }

    private final static RelationPredicate company;
    private final static RelationPredicate companyReserves;
    private final static AtomPredicate ANS1_VAR2_PREDICATE = ATOM_FACTORY.getRDFAnswerPredicate(2);
    private final static Variable cmpShare1 = TERM_FACTORY.getVariable("cmpShare1");
    private final static Variable fldNpdidField1 = TERM_FACTORY.getVariable("fldNpdidField1");
    private final static Variable cmpNpdidCompany2 = TERM_FACTORY.getVariable("cmpNpdidCompany2");
    private final static Variable cmpShortName2 = TERM_FACTORY.getVariable("cmpShortName2");


    static {

        BasicDBMetadata dbMetadata = createDummyMetadata();
        QuotedIDFactory idFactory = dbMetadata.getQuotedIDFactory();

        DatabaseRelationDefinition table24Def = dbMetadata.createDatabaseRelation(idFactory.createRelationID(null, "company"));
        table24Def.addAttribute(idFactory.createAttributeID("cmpNpdidCompany"), Types.INTEGER, null, false);
        table24Def.addAttribute(idFactory.createAttributeID("cmpShortName"), Types.INTEGER, null, false);
        company = table24Def.getAtomPredicate();

        DatabaseRelationDefinition table3Def = dbMetadata.createDatabaseRelation(idFactory.createRelationID(null, "company_reserves"));
        table3Def.addAttribute(idFactory.createAttributeID("cmpShare"), Types.INTEGER, null, false);
        table3Def.addAttribute(idFactory.createAttributeID("fldNpdidField"), Types.INTEGER, null, false);
        table3Def.addAttribute(idFactory.createAttributeID("cmpNpdidCompany"), Types.INTEGER, null, false);
        companyReserves = table3Def.getAtomPredicate();

        dbMetadata.freeze();
        DB_METADATA = dbMetadata;
    }

    @Test
    public void test() {
        ExtensionalDataNode companyReservesNode = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(companyReserves, cmpShare1, fldNpdidField1, cmpNpdidCompany2));
        ExtensionalDataNode companyNode = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(company, cmpShortName2, cmpNpdidCompany2));

        IQTree joinTree = IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(companyReservesNode, companyNode));

        DistinctVariableOnlyDataAtom root =
                ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                        ANS1_VAR2_PREDICATE, ImmutableList.of(cmpNpdidCompany2, fldNpdidField1));
        IQTree rootTree = IQ_FACTORY.createUnaryIQTree(IQ_FACTORY.createConstructionNode(root.getVariables()), joinTree);

        IQ q = IQ_FACTORY.createIQ(root, rootTree);

        System.out.println(q);
    }

}
