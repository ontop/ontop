package it.unibz.inf.ontop.iq.optimizer;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.constraints.ImmutableHomomorphism;
import it.unibz.inf.ontop.constraints.ImmutableHomomorphismIterator;
import it.unibz.inf.ontop.constraints.LinearInclusionDependencies;
import it.unibz.inf.ontop.constraints.impl.ImmutableCQContainmentCheckUnderLIDs;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.atom.RelationPredicate;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.spec.mapping.transformer.impl.IQ2CQ;
import org.junit.Test;

import java.sql.Types;
import java.util.Optional;

import static it.unibz.inf.ontop.utils.MappingTestingTools.*;
import static org.junit.Assert.assertEquals;


public class MappingCQCOptimizerTest {

    @Test
    public void test() {

        BasicDBMetadata dbMetadata = createDummyMetadata();
        QuotedIDFactory idFactory = dbMetadata.getQuotedIDFactory();

        DatabaseRelationDefinition table24Def = dbMetadata.createDatabaseRelation(idFactory.createRelationID(null, "company"));
        table24Def.addAttribute(idFactory.createAttributeID("cmpNpdidCompany"), Types.INTEGER, null, false);
        table24Def.addAttribute(idFactory.createAttributeID("cmpShortName"), Types.INTEGER, null, false);
        RelationPredicate company = table24Def.getAtomPredicate();

        DatabaseRelationDefinition table3Def = dbMetadata.createDatabaseRelation(idFactory.createRelationID(null, "company_reserves"));
        table3Def.addAttribute(idFactory.createAttributeID("cmpShare"), Types.INTEGER, null, false);
        table3Def.addAttribute(idFactory.createAttributeID("fldNpdidField"), Types.INTEGER, null, false);
        table3Def.addAttribute(idFactory.createAttributeID("cmpNpdidCompany"), Types.INTEGER, null, false);
        RelationPredicate companyReserves = table3Def.getAtomPredicate();

        table3Def.addForeignKeyConstraint(
                ForeignKeyConstraint.builder(table3Def, table24Def)
                        .add(table3Def.getAttribute(3), table24Def.getAttribute(1))
                        .build("FK"));

        dbMetadata.freeze();

        final Variable cmpShare1 = TERM_FACTORY.getVariable("cmpShare1");
        final Variable fldNpdidField1 = TERM_FACTORY.getVariable("fldNpdidField1");
        final Variable cmpNpdidCompany2 = TERM_FACTORY.getVariable("cmpNpdidCompany2");
        final Variable cmpShortName2 = TERM_FACTORY.getVariable("cmpShortName2");

        ExtensionalDataNode companyReservesNode = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(companyReserves, cmpShare1, fldNpdidField1, cmpNpdidCompany2));
        ExtensionalDataNode companyNode = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(company, cmpShortName2, cmpNpdidCompany2));

        IQTree joinTree = IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(companyReservesNode, companyNode));

        DistinctVariableOnlyDataAtom root =
                ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                        ATOM_FACTORY.getRDFAnswerPredicate(2), ImmutableList.of(cmpNpdidCompany2, fldNpdidField1));
        IQTree rootTree = IQ_FACTORY.createUnaryIQTree(IQ_FACTORY.createConstructionNode(root.getVariables()), joinTree);

        IQ q = IQ_FACTORY.createIQ(root, rootTree);

        LinearInclusionDependencies.Builder<RelationPredicate> b = LinearInclusionDependencies.builder(CORE_UTILS_FACTORY, ATOM_FACTORY);

        final Variable cmpShare1M = TERM_FACTORY.getVariable("cmpShare1M");
        final Variable fldNpdidField1M = TERM_FACTORY.getVariable("fldNpdidField1M");
        final Variable cmpNpdidCompany2M = TERM_FACTORY.getVariable("cmpNpdidCompany2M");
        final Variable cmpShortName2M = TERM_FACTORY.getVariable("cmpShortName2M");

        b.add(ATOM_FACTORY.getDataAtom(company, cmpShortName2M, cmpNpdidCompany2M),
                ATOM_FACTORY.getDataAtom(companyReserves, cmpShare1M, fldNpdidField1M, cmpNpdidCompany2M));

        ImmutableCQContainmentCheckUnderLIDs<RelationPredicate> foreignKeyCQC = new ImmutableCQContainmentCheckUnderLIDs<>(b.build());

        IQ r = MAPPING_CQC_OPTIMIZER.optimize(foreignKeyCQC, q);

        assertEquals(1, r.getTree().getChildren().size());
        assertEquals(companyReservesNode, r.getTree().getChildren().get(0));
    }


    @Test
    public void test_optimisation_order() {
        // TODO: code a test for film_category -> film -> language
        //                       film_category -> casting
    }

    @Test
    public void test_foreign_keys() {
        // store -> address, staff -> address

        BasicDBMetadata dbMetadata = createDummyMetadata();
        QuotedIDFactory idFactory = dbMetadata.getQuotedIDFactory();

        DatabaseRelationDefinition addressTable = dbMetadata.createDatabaseRelation(idFactory.createRelationID(null, "address"));
        addressTable.addAttribute(idFactory.createAttributeID("address_id"), Types.INTEGER, null, false);
        addressTable.addAttribute(idFactory.createAttributeID("address"), Types.INTEGER, null, false);
        RelationPredicate address = addressTable.getAtomPredicate();

        DatabaseRelationDefinition storeTable = dbMetadata.createDatabaseRelation(idFactory.createRelationID(null, "store"));
        storeTable.addAttribute(idFactory.createAttributeID("store_id"), Types.INTEGER, null, false);
        storeTable.addAttribute(idFactory.createAttributeID("address_id"), Types.INTEGER, null, false);
        RelationPredicate store = storeTable.getAtomPredicate();

        DatabaseRelationDefinition staffTable = dbMetadata.createDatabaseRelation(idFactory.createRelationID(null, "staff"));
        staffTable.addAttribute(idFactory.createAttributeID("staff_id"), Types.INTEGER, null, false);
        staffTable.addAttribute(idFactory.createAttributeID("address_id"), Types.INTEGER, null, false);
        RelationPredicate staff = staffTable.getAtomPredicate();

        storeTable.addForeignKeyConstraint(
                ForeignKeyConstraint.builder(storeTable, addressTable)
                        .add(storeTable.getAttribute(2), addressTable.getAttribute(1))
                        .build("FK"));

        staffTable.addForeignKeyConstraint(
                ForeignKeyConstraint.builder(staffTable, addressTable)
                        .add(staffTable.getAttribute(2), addressTable.getAttribute(1))
                        .build("FK"));
        dbMetadata.freeze();

        final Variable staffId1 = TERM_FACTORY.getVariable("staff_id2");
        final Variable addressId1 = TERM_FACTORY.getVariable("address_id2");
        final Variable addressId2 = TERM_FACTORY.getVariable("address_id5");
        final Variable address2 = TERM_FACTORY.getVariable("address7");
        final Variable addressId3 = TERM_FACTORY.getVariable("address_id4");
        final Variable storeId3 = TERM_FACTORY.getVariable("store_id1");

        DataAtom<RelationPredicate> staffAtom = ATOM_FACTORY.getDataAtom(staff, staffId1, addressId1);
        DataAtom<RelationPredicate> addressAtom1 = ATOM_FACTORY.getDataAtom(address, addressId2, address2);
        ImmutableList<DataAtom<RelationPredicate>> one = ImmutableList.of(addressAtom1, staffAtom);

        DataAtom<RelationPredicate> storeAtom = ATOM_FACTORY.getDataAtom(store, storeId3, addressId3);
        DataAtom<RelationPredicate> addressAtom2 = ATOM_FACTORY.getDataAtom(address, addressId2, address2);
        ImmutableList<DataAtom<RelationPredicate>> two = ImmutableList.of(storeAtom, addressAtom2);

        System.out.println("ONE " + one + "\n" + "TWO " + two);

        LinearInclusionDependencies.Builder<RelationPredicate> b = LinearInclusionDependencies.builder(CORE_UTILS_FACTORY, ATOM_FACTORY);

        final Variable addressIdM = TERM_FACTORY.getVariable("address_id_m");
        final Variable addressM = TERM_FACTORY.getVariable("address_m");
        final Variable storeIdM = TERM_FACTORY.getVariable("store_id_m");
        final Variable staffIdM = TERM_FACTORY.getVariable("staff_id_m");

        b.add(ATOM_FACTORY.getDataAtom(address, addressIdM, addressM),
                ATOM_FACTORY.getDataAtom(store, storeIdM, addressIdM));

        b.add(ATOM_FACTORY.getDataAtom(address, addressIdM, addressM),
                ATOM_FACTORY.getDataAtom(staff, staffIdM, addressIdM));

        LinearInclusionDependencies<RelationPredicate> lids = b.build();
        System.out.println("LIDS: " + lids);

        ImmutableCQContainmentCheckUnderLIDs<RelationPredicate> foreignKeyCQC = new ImmutableCQContainmentCheckUnderLIDs<>(lids);

        Optional<ImmutableHomomorphism> to =
                Optional.of(ImmutableHomomorphism.builder().extend(address2, address2).extend(addressId2, addressId2).build())
                        .map(h -> foreignKeyCQC.homomorphismIterator(h, one, two))
                        .filter(ImmutableHomomorphismIterator::hasNext)
                        .map(ImmutableHomomorphismIterator::next);

//        System.out.println(to.get());
    }
}
