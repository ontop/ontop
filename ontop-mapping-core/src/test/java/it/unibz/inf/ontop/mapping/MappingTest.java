package it.unibz.inf.ontop.mapping;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.AtomPredicate;
import it.unibz.inf.ontop.model.DataAtom;
import it.unibz.inf.ontop.model.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.Variable;
import it.unibz.inf.ontop.model.impl.AtomPredicateImpl;
import it.unibz.inf.ontop.pivotalrepr.ConstructionNode;
import it.unibz.inf.ontop.pivotalrepr.ExtensionalDataNode;
import it.unibz.inf.ontop.pivotalrepr.IntermediateQuery;
import it.unibz.inf.ontop.pivotalrepr.IntermediateQueryBuilder;
import it.unibz.inf.ontop.pivotalrepr.impl.ConstructionNodeImpl;
import it.unibz.inf.ontop.pivotalrepr.impl.ExtensionalDataNodeImpl;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.model.impl.OntopModelSingletons.DATA_FACTORY;
import static it.unibz.inf.ontop.utils.MappingTestingTools.*;
import static org.junit.Assert.fail;

public class MappingTest {

    private static AtomPredicate P1_PREDICATE = new AtomPredicateImpl("p1", 2);
    private static AtomPredicate P3_PREDICATE = new AtomPredicateImpl("p3", 1);
    private static AtomPredicate P4_PREDICATE = new AtomPredicateImpl("p4", 1);
    private static AtomPredicate P5_PREDICATE = new AtomPredicateImpl("p5", 1);

    private static Variable X = DATA_FACTORY.getVariable("x");
    private static Variable S = DATA_FACTORY.getVariable("s");
    private static Variable T = DATA_FACTORY.getVariable("t");

    private static DistinctVariableOnlyDataAtom P1_ST_ATOM = DATA_FACTORY.getDistinctVariableOnlyDataAtom(
            P1_PREDICATE, ImmutableList.of(S, T));
    private static DistinctVariableOnlyDataAtom P2_ST_ATOM = DATA_FACTORY.getDistinctVariableOnlyDataAtom(
            P1_PREDICATE, ImmutableList.of(S, T));
    private static DistinctVariableOnlyDataAtom P3_X_ATOM = DATA_FACTORY.getDistinctVariableOnlyDataAtom(
            P3_PREDICATE, ImmutableList.of(X));
    private static DistinctVariableOnlyDataAtom P4_X_ATOM = DATA_FACTORY.getDistinctVariableOnlyDataAtom(
            P4_PREDICATE, ImmutableList.of(X));
    private static DistinctVariableOnlyDataAtom P5_X_ATOM = DATA_FACTORY.getDistinctVariableOnlyDataAtom(
            P5_PREDICATE, ImmutableList.of(X));

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
            IntermediateQueryBuilder mappingBuilder = createQueryBuilder(EMPTY_METADATA);
            ConstructionNode mappingRootNode = new ConstructionNodeImpl(projectionAtoms[i].getVariables());
            mappingBuilder.init(projectionAtoms[i], mappingRootNode);
            ExtensionalDataNode extensionalDataNode = new ExtensionalDataNodeImpl(dataAtoms[i]);
            mappingBuilder.addChild(mappingRootNode, extensionalDataNode);
            IntermediateQuery mappingAssertion = mappingBuilder.build();
            mappingAssertions.add(mappingAssertion);
            System.out.println("Mapping assertion "+i+":\n" +mappingAssertion);
        }

        /**
         * Renaming
         */
        MappingMetadata mappingMetadata = MAPPING_FACTORY.create(MAPPING_FACTORY.create(ImmutableMap.of()));
        Mapping mapping = MAPPING_FACTORY.create(mappingMetadata, mappingAssertions.stream());

        /**
         * Test whether two mapping assertions share a variable
         */
        System.out.println("After renaming:");
        Set<Variable> variableUnion = new HashSet<Variable>();
        for (DistinctVariableOnlyDataAtom projectionAtom : projectionAtoms){

            IntermediateQuery mappingAssertion = mapping.getDefinition(projectionAtom.getPredicate())
                    .orElseThrow(() -> new IllegalStateException("Test fail: missing mapping assertion "));

            System.out.println(mappingAssertion);
            ImmutableSet<Variable> mappingAssertionVariables = mappingAssertion.getVariables(mappingAssertion.getRootConstructionNode());
            if(Stream.of(mappingAssertionVariables)
                    .anyMatch(v -> variableUnion.contains(v))){
                fail();
                break;
            }
            variableUnion.addAll(mappingAssertionVariables);
            System.out.println("All variables thus far: "+variableUnion+"\n");
        }
    }
}
