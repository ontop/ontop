package it.unibz.inf.ontop.iq.optimizer;

import com.google.common.collect.ImmutableList;
import com.google.inject.Injector;
import it.unibz.inf.ontop.constraints.LinearInclusionDependencies;
import it.unibz.inf.ontop.datalog.CQIE;
import it.unibz.inf.ontop.datalog.DatalogFactory;
import it.unibz.inf.ontop.datalog.impl.CQContainmentCheckUnderLIDs;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.OntopModelConfiguration;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.iq.tools.IQConverter;
import it.unibz.inf.ontop.iq.transform.NoNullValueEnforcer;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.atom.RelationPredicate;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.substitution.impl.UnifierUtilities;
import org.apache.commons.rdf.api.RDF;
import org.junit.Test;

import java.sql.Types;
import java.util.List;

import static it.unibz.inf.ontop.utils.MappingTestingTools.*;
import static org.junit.Assert.assertEquals;


public class MappingCQCOptimizerTest {



    private final static RelationPredicate company;
    private final static RelationPredicate companyReserves;
    private final static AtomPredicate ANS1_VAR2_PREDICATE = ATOM_FACTORY.getRDFAnswerPredicate(2);
    private final static Variable cmpShare1 = TERM_FACTORY.getVariable("cmpShare1");
    private final static Variable fldNpdidField1 = TERM_FACTORY.getVariable("fldNpdidField1");
    private final static Variable cmpNpdidCompany2 = TERM_FACTORY.getVariable("cmpNpdidCompany2");
    private final static Variable cmpShortName2 = TERM_FACTORY.getVariable("cmpShortName2");

    private final static Variable cmpShare1M = TERM_FACTORY.getVariable("cmpShare1M");
    private final static Variable fldNpdidField1M = TERM_FACTORY.getVariable("fldNpdidField1M");
    private final static Variable cmpNpdidCompany2M = TERM_FACTORY.getVariable("cmpNpdidCompany2M");
    private final static Variable cmpShortName2M = TERM_FACTORY.getVariable("cmpShortName2M");


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

        table3Def.addForeignKeyConstraint(
                ForeignKeyConstraint.builder(table3Def, table24Def)
                    .add(table3Def.getAttribute(3), table24Def.getAttribute(1))
                    .build("FK"));

        dbMetadata.freeze();
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

        LinearInclusionDependencies.Builder<AtomPredicate> b = LinearInclusionDependencies.builder(CORE_UTILS_FACTORY, ATOM_FACTORY);

        b.add(ATOM_FACTORY.getDataAtom(company, cmpShortName2M, cmpNpdidCompany2M),
                ATOM_FACTORY.getDataAtom(companyReserves, cmpShare1M, fldNpdidField1M, cmpNpdidCompany2M));

        CQContainmentCheckUnderLIDs foreignKeyCQC = new CQContainmentCheckUnderLIDs(b.build(),
                ATOM_FACTORY, TERM_FACTORY, IMMUTABILITY_TOOLS);

        CQIE q0 = INTERMEDIATE_QUERY_2_DATALOG_TRANSLATOR.translate(q).getRules().get(0);
        foreignKeyCQC.removeRedundantAtoms(q0);

        assertEquals(1, q0.getBody().size());
    }

}
