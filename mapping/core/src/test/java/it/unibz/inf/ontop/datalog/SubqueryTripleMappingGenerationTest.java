package it.unibz.inf.ontop.datalog;


import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.IntermediateQueryBuilder;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.iq.node.UnionNode;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.Constant;
import it.unibz.inf.ontop.model.term.Function;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.impl.PredicateImpl;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.spec.ontology.ClassifiedTBox;
import it.unibz.inf.ontop.spec.ontology.impl.OntologyBuilderImpl;
import it.unibz.inf.ontop.spec.ontology.impl.ClassifiedTBoxImpl;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Types;
import java.util.Optional;
import java.util.stream.Collectors;

import static it.unibz.inf.ontop.utils.MappingTestingTools.*;
import static junit.framework.TestCase.assertTrue;

/**
 * Bugfix
 * <p>
 * Checks that no triple mapping is created for the internal predicates generated when converting IQs with subqueries to Datalog.
 */
public class SubqueryTripleMappingGenerationTest {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private static final Variable X = TERM_FACTORY.getVariable("x");

    private final static AtomPredicate ANS1_PREDICATE1 = ATOM_FACTORY.getAtomPredicate("ans1", 1);
    private final static BasicDBMetadata METADATA;

    private static final AtomPredicate P1_PREDICATE;
    private static final AtomPredicate P2_PREDICATE;

    static {
        METADATA = createDummyMetadata();
        QuotedIDFactory idFactory = METADATA.getQuotedIDFactory();
        DatabaseRelationDefinition table1Def = METADATA.createDatabaseRelation(idFactory.createRelationID(null, "table1"));
        DatabaseRelationDefinition table2Def = METADATA.createDatabaseRelation(idFactory.createRelationID(null, "table2"));
        table1Def.addAttribute(idFactory.createAttributeID("col1"), Types.INTEGER, null, false);
        table2Def.addAttribute(idFactory.createAttributeID("col1"), Types.INTEGER, null, false);
        P1_PREDICATE = RELATION_2_PREDICATE.createAtomPredicateFromRelation(table1Def);
        P2_PREDICATE = RELATION_2_PREDICATE.createAtomPredicateFromRelation(table2Def);
    }

    @Test
    public void testSaturation() {

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, X);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom1.getVariables());

        UnionNode unionNode1 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X));
        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(projectionAtom1.getVariables());

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(P1_PREDICATE, X));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(P2_PREDICATE, X));

        queryBuilder1.init(projectionAtom1, constructionNode1);
        queryBuilder1.addChild(constructionNode1, unionNode1);
        queryBuilder1.addChild(unionNode1, dataNode1);
        queryBuilder1.addChild(unionNode1, constructionNode2);
        queryBuilder1.addChild(constructionNode2, dataNode2);


        IntermediateQuery query1 = queryBuilder1.build();

        log.debug("\nQuery: \n" + query1);

        DatalogProgram pg = INTERMEDIATE_QUERY_2_DATALOG_TRANSLATOR.translate(query1);

        Mapping mapping = DATALOG_2_QUERY_MAPPING_CONVERTER.convertMappingRules(
                pg.getRules().stream().collect(ImmutableCollectors.toList()),
                METADATA,
                EXECUTOR_REGISTRY,
                EMPTY_MAPPING_METADATA
        );
        ClassifiedTBox tBoxReasoner = OntologyBuilderImpl.builder().build().tbox();
        Mapping saturatedMapping = MAPPING_SATURATOR.saturate(mapping, METADATA, tBoxReasoner);
        String debug = saturatedMapping.getPredicates().stream()
                .map(p -> saturatedMapping.getDefinition(p).get().toString())
                .collect(Collectors.joining(""));
        log.debug("Saturated mapping:\n" + debug);

        IntermediateQuery iq = saturatedMapping.getDefinition(ATOM_FACTORY.getTripleAtomPredicate()).get();
        Optional<Constant> predConstant = iq.getNodesInTopDownOrder().stream()
            .filter(n -> n instanceof ConstructionNode)
            .flatMap(n -> ((ConstructionNode)n).getSubstitution().getImmutableMap().values().stream())
            .filter(v -> v instanceof Function)
            .map(v -> (Function)v)
            .filter(v -> v.getFunctionSymbol().getName().equals("URI1") && v.getArity() == 1)
            .map(v -> (Constant)v.getTerm(0))
            .filter(c -> c.getValue().startsWith(DATALOG_FACTORY.getSubqueryPredicatePrefix()))
            .findFirst();

        if(predConstant.isPresent()){
            System.out.println("Test failure: predicate "+ predConstant.get() +" used to generate triple predicates");
            assertTrue(false);
        }
    }
}
