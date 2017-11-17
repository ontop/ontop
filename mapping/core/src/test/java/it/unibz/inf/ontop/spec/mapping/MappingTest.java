package it.unibz.inf.ontop.spec.mapping;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.BasicDBMetadata;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.IntermediateQueryBuilder;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.term.functionsymbol.URITemplatePredicate;
import it.unibz.inf.ontop.model.term.Constant;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.UriTemplateMatcher;
import org.junit.Test;

import java.sql.Types;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.utils.MappingTestingTools.*;
import static org.junit.Assert.fail;

public class MappingTest {

    private static final AtomPredicate P1_PREDICATE;
    private static final AtomPredicate P3_PREDICATE;
    private static final AtomPredicate P4_PREDICATE;
    private static final AtomPredicate P5_PREDICATE;
    private static final AtomPredicate BROKER_PREDICATE;

    private static final DBMetadata DB_METADATA;

    private static Variable X = TERM_FACTORY.getVariable("x");
    private static Variable S = TERM_FACTORY.getVariable("s");
    private static Variable T = TERM_FACTORY.getVariable("t");

    private final static Variable F = TERM_FACTORY.getVariable("f0");
    private final static Variable C = TERM_FACTORY.getVariable("client");
    private final static Variable Y = TERM_FACTORY.getVariable("company");

    private static final DistinctVariableOnlyDataAtom P1_ST_ATOM;
    private static final DistinctVariableOnlyDataAtom P2_ST_ATOM;
    private static final DistinctVariableOnlyDataAtom P3_X_ATOM;
    private static final DistinctVariableOnlyDataAtom P4_X_ATOM;
    private static final DistinctVariableOnlyDataAtom P5_X_ATOM;
    private static final DataAtom BROKER_3_ATOM;

    private static final URITemplatePredicate URI_PREDICATE;
    private static final AtomPredicate ANS1_VAR1_PREDICATE;
    private static final Constant URI_TEMPLATE_STR_1;

    static {
        BasicDBMetadata dbMetadata = createDummyMetadata();
        QuotedIDFactory idFactory = dbMetadata.getQuotedIDFactory();

        DatabaseRelationDefinition table1Def = dbMetadata.createDatabaseRelation(idFactory.createRelationID(null, "p1"));
        table1Def.addAttribute(idFactory.createAttributeID("col1"), Types.INTEGER, null, false);
        table1Def.addAttribute(idFactory.createAttributeID("col12"), Types.INTEGER, null, false);
        P1_PREDICATE = RELATION_2_PREDICATE.createAtomPredicateFromRelation(table1Def);

        DatabaseRelationDefinition table3Def = dbMetadata.createDatabaseRelation(idFactory.createRelationID(null, "p3"));
        table3Def.addAttribute(idFactory.createAttributeID("col31"), Types.INTEGER, null, false);
        P3_PREDICATE = RELATION_2_PREDICATE.createAtomPredicateFromRelation(table3Def);

        DatabaseRelationDefinition table4Def = dbMetadata.createDatabaseRelation(idFactory.createRelationID(null, "p4"));
        table4Def.addAttribute(idFactory.createAttributeID("col41"), Types.INTEGER, null, false);
        P4_PREDICATE = RELATION_2_PREDICATE.createAtomPredicateFromRelation(table4Def);

        DatabaseRelationDefinition table5Def = dbMetadata.createDatabaseRelation(idFactory.createRelationID(null, "p5"));
        table5Def.addAttribute(idFactory.createAttributeID("col51"), Types.INTEGER, null, false);
        P5_PREDICATE = RELATION_2_PREDICATE.createAtomPredicateFromRelation(table5Def);

        P1_ST_ATOM = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(P1_PREDICATE, ImmutableList.of(S, T));
        P2_ST_ATOM = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(P1_PREDICATE, ImmutableList.of(S, T));
        P3_X_ATOM = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(P3_PREDICATE, ImmutableList.of(X));
        P4_X_ATOM = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(P4_PREDICATE, ImmutableList.of(X));
        P5_X_ATOM = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(P5_PREDICATE, ImmutableList.of(X));


        DatabaseRelationDefinition tableBrokerDef = dbMetadata.createDatabaseRelation(idFactory.createRelationID("DB2INST1", "brokerworksfor"));
        tableBrokerDef.addAttribute(idFactory.createAttributeID("broker"), Types.INTEGER, null, false);
        tableBrokerDef.addAttribute(idFactory.createAttributeID("company"), Types.INTEGER, null, true);
        tableBrokerDef.addAttribute(idFactory.createAttributeID("client"), Types.INTEGER, null, true);
        BROKER_PREDICATE = RELATION_2_PREDICATE.createAtomPredicateFromRelation(tableBrokerDef);

        URI_PREDICATE =  TERM_FACTORY.getURITemplatePredicate(2);
        ANS1_VAR1_PREDICATE = ATOM_FACTORY.getAtomPredicate("http://example.org/Dealer", 1);
        URI_TEMPLATE_STR_1 =  TERM_FACTORY.getConstantLiteral("http://example.org/person/{}");

        BROKER_3_ATOM = ATOM_FACTORY.getDataAtom(BROKER_PREDICATE, ImmutableList.of(C,Y,C));

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
                UriTemplateMatcher.create(Stream.of(), TERM_FACTORY));
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
            ImmutableSet<Variable> mappingAssertionVariables = mappingAssertion.getProjectionAtom().getVariables();
            if(Stream.of(mappingAssertionVariables)
                    .anyMatch(variableUnion::contains)){
                fail();
                break;
            }
            variableUnion.addAll(mappingAssertionVariables);
            System.out.println("All variables thus far: "+variableUnion+"\n");
        }
    }

    @Test
    public void testTwoEqualVariablesInExtensionalTable() {

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(F),
                SUBSTITUTION_FACTORY.getSubstitution(F, generateURI1(C)));

        ExtensionalDataNode table1DataNode = IQ_FACTORY.createExtensionalDataNode(BROKER_3_ATOM);

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_VAR1_PREDICATE, F);

        IntermediateQueryBuilder queryBuilder = createQueryBuilder(DB_METADATA);
        queryBuilder.init(projectionAtom, constructionNode);
        queryBuilder.addChild(constructionNode, table1DataNode);


        IntermediateQuery query = queryBuilder.build();
        System.out.print (query);

        List<IntermediateQuery> mappingAssertions = new ArrayList<>();
        mappingAssertions.add(query);

        MappingMetadata mappingMetadata = MAPPING_FACTORY.createMetadata(MAPPING_FACTORY.createPrefixManager(ImmutableMap.of()),
                UriTemplateMatcher.create(Stream.of(), TERM_FACTORY));
        ImmutableMap<AtomPredicate, IntermediateQuery> mappingMap = mappingAssertions.stream()
                .collect(ImmutableCollectors.toMap(
                        q -> q.getProjectionAtom().getPredicate(),
                        q -> q));

        MAPPING_FACTORY.createMapping(mappingMetadata, mappingMap, EXECUTOR_REGISTRY);
    }

    private ImmutableFunctionalTerm generateURI1(VariableOrGroundTerm argument) {
        return TERM_FACTORY.getImmutableFunctionalTerm(URI_PREDICATE, URI_TEMPLATE_STR_1, argument);
    }
}
