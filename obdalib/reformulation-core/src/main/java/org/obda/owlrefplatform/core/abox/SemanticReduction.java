package org.obda.owlrefplatform.core.abox;


import org.obda.owlrefplatform.core.ontology.Assertion;
import org.obda.owlrefplatform.core.ontology.ConceptDescription;
import org.obda.owlrefplatform.core.ontology.DescriptionFactory;
import org.obda.owlrefplatform.core.ontology.RoleDescription;
import org.obda.owlrefplatform.core.ontology.imp.BasicDescriptionFactory;
import org.obda.owlrefplatform.core.ontology.imp.DLLiterConceptInclusionImpl;
import org.obda.owlrefplatform.core.ontology.imp.DLLiterRoleInclusionImpl;
import org.obda.query.domain.Predicate;
import org.obda.query.domain.imp.BasicPredicateFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;

/**
 * Prune Ontology for redundant assertions based on dependencies
 */
public class SemanticReduction {

    private static final Logger log = LoggerFactory.getLogger(SemanticReduction.class);
    private final DAG dag;
    private final TDAG tdag;
    private final SDAG sdag;

    private BasicPredicateFactoryImpl predicateFactory;
    private DescriptionFactory descFactory;

    public SemanticReduction(DAG dag, TDAG tdag, SDAG sdag) {
        this.dag = dag;
        this.tdag = tdag;
        this.sdag = sdag;

        predicateFactory = BasicPredicateFactoryImpl.getInstance();
        descFactory = new BasicDescriptionFactory();

    }

    public List<Assertion> reduce() {
        List<Assertion> rv = new LinkedList<Assertion>();


        for (DAGNode node : dag.getClassIndex().values()) {

            for (DAGNode child : node.descendans) {

                if (!check_redundant(node, child)) {
                    URI node_uri = URI.create(node.getUri());
                    Predicate node_p = predicateFactory.createPredicate(node_uri, 1);
                    ConceptDescription node_cd = descFactory.getConceptDescription(node_p);

                    URI child_uri = URI.create(child.getUri());
                    Predicate child_p = predicateFactory.createPredicate(child_uri, 1);
                    ConceptDescription child_cd = descFactory.getConceptDescription(child_p);

                    rv.add(new DLLiterConceptInclusionImpl(node_cd, child_cd));
                }
            }
        }
        for (DAGNode node : dag.getObjectPropertyIndex().values()) {

            for (DAGNode child : node.descendans) {
                if (!check_redundant(node, child)) {
                    URI node_uri = URI.create(node.getUri());
                    Predicate node_p = predicateFactory.createPredicate(node_uri, 2);
                    RoleDescription node_rd = descFactory.getRoleDescription(node_p);

                    URI child_uri = URI.create(child.getUri());
                    Predicate child_p = predicateFactory.createPredicate(child_uri, 2);
                    RoleDescription child_rd = descFactory.getRoleDescription(child_p);

                    rv.add(new DLLiterRoleInclusionImpl(node_rd, child_rd));
                }
            }
        }
        for (DAGNode node : dag.getDataPropertyIndex().values()) {

            for (DAGNode child : node.descendans) {
                if (!check_redundant(node, child)) {
                    URI node_uri = URI.create(node.getUri());
                    Predicate node_p = predicateFactory.createPredicate(node_uri, 2);
                    RoleDescription node_rd = descFactory.getRoleDescription(node_p);

                    URI child_uri = URI.create(child.getUri());
                    Predicate child_p = predicateFactory.createPredicate(child_uri, 2);
                    RoleDescription child_rd = descFactory.getRoleDescription(child_p);

                    rv.add(new DLLiterRoleInclusionImpl(node_rd, child_rd));
                }
            }
        }

        return rv;
    }

    private boolean check_redundant(DAGNode parent, DAGNode child) {
        if (check_directly_redundant(parent, child))
            return true;
        else {
            for (DAGNode child_prime : parent.getChildren()) {
                if (!child_prime.equals(child) &&
                        check_directly_redundant(child_prime, child) &&
                        !check_redundant(child_prime, parent)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean check_directly_redundant(DAGNode parent, DAGNode child) {
        DAGNode sp = sdag.getTDAG().get(parent.getUri());
        DAGNode sc = sdag.getTDAG().get(child.getUri());
        DAGNode tp = tdag.getTDAG().get(parent.getUri());
        DAGNode tc = tdag.getTDAG().get(child.getUri());

        return (sp.descendans.contains(sc) && sc.descendans.containsAll(tc.descendans));

    }

}
