package it.unibz.inf.ontop.mapping;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.pivotalrepr.ConstructionNode;
import it.unibz.inf.ontop.pivotalrepr.ExtensionalDataNode;
import it.unibz.inf.ontop.pivotalrepr.IntermediateQuery;
import it.unibz.inf.ontop.pivotalrepr.IntermediateQueryBuilder;
import it.unibz.inf.ontop.sql.*;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.junit.Test;

import java.sql.Types;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.model.impl.OntopModelSingletons.DATA_FACTORY;
import static it.unibz.inf.ontop.utils.MappingTestingTools.*;
import static org.junit.Assert.fail;

public class MappingTest {

    private static final AtomPredicate P1_PREDICATE;
    private static final AtomPredicate P3_PREDICATE;
    private static final AtomPredicate P4_PREDICATE;
    private static final AtomPredicate P5_PREDICATE;

    private static final DBMetadata DB_METADATA;

    private static Variable X = DATA_FACTORY.getVariable("x");
    private static Variable S = DATA_FACTORY.getVariable("s");
    private static Variable T = DATA_FACTORY.getVariable("t");

    private static final DistinctVariableOnlyDataAtom P1_ST_ATOM;
    private static final DistinctVariableOnlyDataAtom P2_ST_ATOM;
    private static final DistinctVariableOnlyDataAtom P3_X_ATOM;
    private static final DistinctVariableOnlyDataAtom P4_X_ATOM;
    private static final DistinctVariableOnlyDataAtom P5_X_ATOM;

    static {
        BasicDBMetadata dbMetadata = DBMetadataTestingTools.createDummyMetadata();
        QuotedIDFactory idFactory = dbMetadata.getQuotedIDFactory();

        DatabaseRelationDefinition table1Def = dbMetadata.createDatabaseRelation(idFactory.createRelationID(null, "p1"));
        table1Def.addAttribute(idFactory.createAttributeID("col1"), Types.INTEGER, null, false);
        table1Def.addAttribute(idFactory.createAttributeID("col12"), Types.INTEGER, null, false);
        P1_PREDICATE = Relation2DatalogPredicate.createAtomPredicateFromRelation(table1Def);

        DatabaseRelationDefinition table3Def = dbMetadata.createDatabaseRelation(idFactory.createRelationID(null, "p3"));
        table3Def.addAttribute(idFactory.createAttributeID("col31"), Types.INTEGER, null, false);
        P3_PREDICATE = Relation2DatalogPredicate.createAtomPredicateFromRelation(table3Def);

        DatabaseRelationDefinition table4Def = dbMetadata.createDatabaseRelation(idFactory.createRelationID(null, "p4"));
        table4Def.addAttribute(idFactory.createAttributeID("col41"), Types.INTEGER, null, false);
        P4_PREDICATE = Relation2DatalogPredicate.createAtomPredicateFromRelation(table4Def);

        DatabaseRelationDefinition table5Def = dbMetadata.createDatabaseRelation(idFactory.createRelationID(null, "p5"));
        table5Def.addAttribute(idFactory.createAttributeID("col51"), Types.INTEGER, null, false);
        P5_PREDICATE = Relation2DatalogPredicate.createAtomPredicateFromRelation(table5Def);

        P1_ST_ATOM = DATA_FACTORY.getDistinctVariableOnlyDataAtom(P1_PREDICATE, ImmutableList.of(S, T));
        P2_ST_ATOM = DATA_FACTORY.getDistinctVariableOnlyDataAtom(P1_PREDICATE, ImmutableList.of(S, T));
        P3_X_ATOM = DATA_FACTORY.getDistinctVariableOnlyDataAtom(P3_PREDICATE, ImmutableList.of(X));
        P4_X_ATOM = DATA_FACTORY.getDistinctVariableOnlyDataAtom(P4_PREDICATE, ImmutableList.of(X));
        P5_X_ATOM = DATA_FACTORY.getDistinctVariableOnlyDataAtom(P5_PREDICATE, ImmutableList.of(X));

        DB_METADATA = dbMetadata;
    }

    @Test
    public void testOfflineMappingAssertionsRenaming() {

        List<IntermediateQuery> mappingAssertions = new ArrayList<>();
        DataAtom[] dataAtoms = new DataAtom[]{
                P3_X_ATOM,
                P3_X_ATOM,
                P1_ST_ATOM
        };
        DistinctVariableOnlyDataAtom[] projectionAtoms = new DistinctVariableOnlyDataAtom[]{
                P4_X_ATOM,
                P5_X_ATOM,
                P2_ST_ATOM
        };

        /**
         * Mappings assertions
         */
        for (int i =0; i < projectionAtoms.length;  i++){
            IntermediateQueryBuilder mappingBuilder = createQueryBuilder(DB_METADATA);
            ConstructionNode mappingRootNode = IQ_FACTORY.createConstructionNode(projectionAtoms[i].getVariables());
            mappingBuilder.init(projectionAtoms[i], mappingRootNode);
            ExtensionalDataNode extensionalDataNode = IQ_FACTORY.createExtensionalDataNode(dataAtoms[i]);
            mappingBuilder.addChild(mappingRootNode, extensionalDataNode);
            IntermediateQuery mappingAssertion = mappingBuilder.build();
            mappingAssertions.add(mappingAssertion);
            System.out.println("Mapping assertion "+i+":\n" +mappingAssertion);
        }

        /**
         * Renaming
         */
        MappingMetadata mappingMetadata = MAPPING_FACTORY.createMetadata(MAPPING_FACTORY.createPrefixManager(ImmutableMap.of()),
                UriTemplateMatcher.create(Stream.of()));
        ImmutableMap<AtomPredicate, IntermediateQuery> mappingMap = mappingAssertions.stream()
                .collect(ImmutableCollectors.toMap(
                        q -> q.getProjectionAtom().getPredicate(),
                        q -> q));

        Mapping nonNormalizedMapping = MAPPING_FACTORY.createMapping(mappingMetadata, mappingMap, EXECUTOR_REGISTRY);
        Mapping normalizedMapping = MAPPING_NORMALIZER.normalize(nonNormalizedMapping);

        /**
         * Test whether two mapping assertions share a variable
         */
        System.out.println("After renaming:");
        Set<Variable> variableUnion = new HashSet<Variable>();
        for (DistinctVariableOnlyDataAtom projectionAtom : projectionAtoms){

            IntermediateQuery mappingAssertion = normalizedMapping.getDefinition(projectionAtom.getPredicate())
                    .orElseThrow(() -> new IllegalStateException("Test fail: missing mapping assertion "));

            System.out.println(mappingAssertion);
            ImmutableSet<Variable> mappingAssertionVariables = mappingAssertion.getVariables(mappingAssertion.getRootConstructionNode());
            if(Stream.of(mappingAssertionVariables)
                    .anyMatch(variableUnion::contains)){
                fail();
                break;
            }
            variableUnion.addAll(mappingAssertionVariables);
            System.out.println("All variables thus far: "+variableUnion+"\n");
        }
    }
}
