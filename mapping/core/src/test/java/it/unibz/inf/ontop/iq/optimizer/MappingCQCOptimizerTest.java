package it.unibz.inf.ontop.iq.optimizer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.constraints.impl.LinearInclusionDependenciesImpl;
import it.unibz.inf.ontop.constraints.ImmutableHomomorphism;
import it.unibz.inf.ontop.constraints.ImmutableHomomorphismIterator;
import it.unibz.inf.ontop.constraints.impl.BasicLinearInclusionDependenciesImpl;
import it.unibz.inf.ontop.constraints.impl.ImmutableCQContainmentCheckUnderLIDs;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.OfflineMetadataProviderBuilder;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.atom.RelationPredicate;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.type.DBTermType;
import org.junit.Test;

import java.util.Optional;

import static it.unibz.inf.ontop.utils.MappingTestingTools.*;
import static org.junit.Assert.assertEquals;


public class MappingCQCOptimizerTest {

    @Test
    public void test() {
        OfflineMetadataProviderBuilder builder = createMetadataProviderBuilder();
        DBTermType integerType = builder.getDBTypeFactory().getDBLargeIntegerType();

        NamedRelationDefinition company = builder.createDatabaseRelation("company",
            "cmpNpdidCompany", integerType, false,
            "cmpShortName", integerType, false);

        NamedRelationDefinition companyReserves = builder.createDatabaseRelation("company_reserves",
            "cmpShare", integerType, false,
            "fldNpdidField", integerType, false,
            "cmpNpdidCompany", integerType, false);
        ForeignKeyConstraint.builder("FK", companyReserves, company)
                .add(2, 1)
                .build();

        final Variable cmpShare1 = TERM_FACTORY.getVariable("cmpShare1");
        final Variable fldNpdidField1 = TERM_FACTORY.getVariable("fldNpdidField1");
        final Variable cmpNpdidCompany2 = TERM_FACTORY.getVariable("cmpNpdidCompany2");
        final Variable cmpShortName2 = TERM_FACTORY.getVariable("cmpShortName2");

        ExtensionalDataNode companyReservesNode = IQ_FACTORY.createExtensionalDataNode(
                companyReserves, ImmutableMap.of(1, fldNpdidField1, 2, cmpNpdidCompany2));//cmpShare1,
        ExtensionalDataNode companyNode = IQ_FACTORY.createExtensionalDataNode(
                company, ImmutableMap.of(0, cmpShortName2, 1, cmpNpdidCompany2));

        IQTree joinTree = IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(companyReservesNode, companyNode));

        DistinctVariableOnlyDataAtom root =
                ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                        ATOM_FACTORY.getRDFAnswerPredicate(2), ImmutableList.of(cmpNpdidCompany2, fldNpdidField1));
        IQTree rootTree = IQ_FACTORY.createUnaryIQTree(IQ_FACTORY.createConstructionNode(root.getVariables()), joinTree);

        IQ q = IQ_FACTORY.createIQ(root, rootTree);

        LinearInclusionDependenciesImpl.Builder<RelationPredicate> b = LinearInclusionDependenciesImpl.builder(CORE_UTILS_FACTORY, ATOM_FACTORY);

        final Variable cmpShare1M = TERM_FACTORY.getVariable("cmpShare1M");
        final Variable fldNpdidField1M = TERM_FACTORY.getVariable("fldNpdidField1M");
        final Variable cmpNpdidCompany2M = TERM_FACTORY.getVariable("cmpNpdidCompany2M");
        final Variable cmpShortName2M = TERM_FACTORY.getVariable("cmpShortName2M");

        b.add(ATOM_FACTORY.getDataAtom(company.getAtomPredicate(), cmpShortName2M, cmpNpdidCompany2M),
                ATOM_FACTORY.getDataAtom(companyReserves.getAtomPredicate(), cmpShare1M, fldNpdidField1M, cmpNpdidCompany2M));

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
        // store (address_id/NN, manager_staff_id/NN) -> address (address_id/PL), staff (staff_id/PK)
        // staff (address_id/NN, store_id/NN) -> address (address_id/PK), store (store_id/PK)
        OfflineMetadataProviderBuilder builder = createMetadataProviderBuilder();
        DBTermType integerType = builder.getDBTypeFactory().getDBLargeIntegerType();

        NamedRelationDefinition addressTable = builder.createDatabaseRelation("address",
            "address_id", integerType, false,
            "address", integerType, false);
        RelationPredicate address = addressTable.getAtomPredicate();

        NamedRelationDefinition storeTable = builder.createDatabaseRelation("store",
            "store_id", integerType, false,
            "address_id", integerType, false,
            "manager_staff_id", integerType, false);
        RelationPredicate store = storeTable.getAtomPredicate();

        NamedRelationDefinition staffTable = builder.createDatabaseRelation("staff",
            "staff_id", integerType, false,
            "address_id", integerType, false,
            "store_id", integerType, false);
        RelationPredicate staff = staffTable.getAtomPredicate();

        ForeignKeyConstraint.of("FK", storeTable.getAttribute(2), addressTable.getAttribute(1));
        ForeignKeyConstraint.of("FK", storeTable.getAttribute(3), staffTable.getAttribute(1));
        ForeignKeyConstraint.of("FK", staffTable.getAttribute(2), addressTable.getAttribute(1));
        ForeignKeyConstraint.of("FK", staffTable.getAttribute(3), storeTable.getAttribute(1));

        final Variable staffId1 = TERM_FACTORY.getVariable("staff_id2");
        final Variable addressId1 = TERM_FACTORY.getVariable("address_id2");
        final Variable storeId1 = TERM_FACTORY.getVariable("store_id2");
        final Variable addressId2 = TERM_FACTORY.getVariable("address_id5");
        final Variable address2 = TERM_FACTORY.getVariable("address7");
        final Variable storeId3 = TERM_FACTORY.getVariable("store_id1");
        final Variable addressId3 = TERM_FACTORY.getVariable("address_id4");
        final Variable managerStaffId3 = TERM_FACTORY.getVariable("manager_staff_id1");

        DataAtom<RelationPredicate> staffAtom = ATOM_FACTORY.getDataAtom(staff, staffId1, addressId1, storeId1);
        DataAtom<RelationPredicate> addressAtom1 = ATOM_FACTORY.getDataAtom(address, addressId2, address2);
        ImmutableList<DataAtom<RelationPredicate>> one = ImmutableList.of(addressAtom1, staffAtom);

        DataAtom<RelationPredicate> storeAtom = ATOM_FACTORY.getDataAtom(store, storeId3, addressId3, managerStaffId3);
        DataAtom<RelationPredicate> addressAtom2 = ATOM_FACTORY.getDataAtom(address, addressId2, address2);
        ImmutableList<DataAtom<RelationPredicate>> two = ImmutableList.of(storeAtom, addressAtom2);

        System.out.println("ONE " + one + "\n" + "TWO " + two);

        LinearInclusionDependenciesImpl.Builder<RelationPredicate> b = LinearInclusionDependenciesImpl.builder(CORE_UTILS_FACTORY, ATOM_FACTORY);

        final Variable addressIdM = TERM_FACTORY.getVariable("address_id_m");
        final Variable addressIdM2 = TERM_FACTORY.getVariable("address_id_m2");
        final Variable addressM = TERM_FACTORY.getVariable("address_m");
        final Variable storeIdM = TERM_FACTORY.getVariable("store_id_m");
        final Variable storeIdM2 = TERM_FACTORY.getVariable("store_id_m2");
        final Variable staffIdM = TERM_FACTORY.getVariable("staff_id_m");
        final Variable staffIdM2 = TERM_FACTORY.getVariable("staff_id_m2");
        final Variable staffManagerIdM = TERM_FACTORY.getVariable("staff_manager_id_m");

        b.add(ATOM_FACTORY.getDataAtom(address, addressIdM, addressM),
                ATOM_FACTORY.getDataAtom(store, storeIdM, addressIdM, staffManagerIdM));
        b.add(ATOM_FACTORY.getDataAtom(staff, staffManagerIdM, addressIdM, storeIdM2),
                ATOM_FACTORY.getDataAtom(store, storeIdM, addressIdM2, staffManagerIdM));

        b.add(ATOM_FACTORY.getDataAtom(address, addressIdM, addressM),
                ATOM_FACTORY.getDataAtom(staff, staffIdM, addressIdM, storeIdM));
        b.add(ATOM_FACTORY.getDataAtom(store, storeIdM, addressIdM2, staffIdM2),
                ATOM_FACTORY.getDataAtom(staff, staffIdM, addressIdM, storeIdM));

        BasicLinearInclusionDependenciesImpl<RelationPredicate> lids = b.build();
        System.out.println("LIDS: " + lids);

        ImmutableCQContainmentCheckUnderLIDs<RelationPredicate> foreignKeyCQC = new ImmutableCQContainmentCheckUnderLIDs<>(lids);

        Optional<ImmutableHomomorphism> to =
                Optional.of(ImmutableHomomorphism.builder().extend(address2, address2).extend(addressId2, addressId2).build())
                        .map(h -> foreignKeyCQC.homomorphismIterator(h, one, two))
                        .filter(ImmutableHomomorphismIterator::hasNext)
                        .map(ImmutableHomomorphismIterator::next);
        System.out.println(to.get());

        Optional<ImmutableHomomorphism> from =
                Optional.of(ImmutableHomomorphism.builder().extend(address2, address2).extend(addressId2, addressId2).build())
                        .map(h -> foreignKeyCQC.homomorphismIterator(h, two, one))
                        .filter(ImmutableHomomorphismIterator::hasNext)
                        .map(ImmutableHomomorphismIterator::next);
        System.out.println(from.get());
    }
}
