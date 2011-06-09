package it.unibz.krdb.obda.owlrefplatform.core.abox;


import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Assertion;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.ConceptDescription;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.DescriptionFactory;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.RoleDescription;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.BasicDescriptionFactory;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.DLLiterConceptInclusionImpl;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.DLLiterRoleInclusionImpl;
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

    private final OBDADataFactory predicateFactory;
    private final DescriptionFactory descFactory;

    public SemanticReduction(DAG dag, TDAG tdag, SDAG sdag) {
        this.dag = dag;
        this.tdag = tdag;
        this.sdag = sdag;

        predicateFactory = OBDADataFactoryImpl.getInstance();
        descFactory = new BasicDescriptionFactory();

    }

    public List<Assertion> reduce() {
        log.debug("Starting semantic-reduction");
        List<Assertion> rv = new LinkedList<Assertion>();


        for (DAGNode node : dag.getClassIndex().values()) {

            // Ingore all edges from T
            if (node.getUri().equals(DAG.owl_thing)) {
                continue;
            }

            for (DAGNode child : node.descendans) {

                if (!check_redundant(node, child)) {

                    String uri = node.getUri();
                    int arity;
                    boolean inverted;
                    if (uri.startsWith(DAG.owl_exists_data)) {
                        uri = uri.substring(DAG.owl_exists_data.length());
                        arity = 2;
                        inverted = false;
                    } else if (uri.startsWith(DAG.owl_exists_obj)) {
                        uri = uri.substring(DAG.owl_exists_obj.length());
                        arity = 2;
                        inverted = false;
                    } else if (uri.startsWith(DAG.owl_inverse_exists_data)) {
                        uri = uri.substring(DAG.owl_inverse_exists_data.length());
                        arity = 2;
                        inverted = true;
                    } else if (uri.startsWith(DAG.owl_inverse_exists_obj)) {
                        uri = uri.substring(DAG.owl_inverse_exists_obj.length());
                        arity = 2;
                        inverted = true;
                    } else {
                        arity = 1;
                        inverted = false;
                    }
                    URI node_uri = URI.create(uri);
                    Predicate node_p = predicateFactory.getPredicate(node_uri, arity);
                    ConceptDescription node_cd = null;
                    if (arity == 1) {
                     node_cd = descFactory.getAtomicConceptDescription(node_p);
                    } else {
                    	node_cd = descFactory.getExistentialConceptDescription(node_p, inverted);
                    }

                    // Do same dispatch in child
                    uri = child.getUri();
                    if (uri.startsWith(DAG.owl_exists_data)) {
                        uri = uri.substring(DAG.owl_exists_data.length());
                        arity = 2;
                        inverted = false;
                    } else if (uri.startsWith(DAG.owl_exists_obj)) {
                        uri = uri.substring(DAG.owl_exists_obj.length());
                        arity = 2;
                        inverted = false;
                    } else if (uri.startsWith(DAG.owl_inverse_exists_data)) {
                        uri = uri.substring(DAG.owl_inverse_exists_data.length());
                        arity = 2;
                        inverted = true;
                    } else if (uri.startsWith(DAG.owl_inverse_exists_obj)) {
                        uri = uri.substring(DAG.owl_inverse_exists_obj.length());
                        arity = 2;
                        inverted = true;
                    } else {
                        arity = 1;
                        inverted = false;
                    }
                    URI child_uri = URI.create(uri);
                    Predicate child_p = predicateFactory.getPredicate(child_uri, arity);
                    
                    ConceptDescription child_cd = null;
                    if (arity == 1) {
                    	child_cd = descFactory.getAtomicConceptDescription(child_p);
                    } else {
                    	child_cd = descFactory.getExistentialConceptDescription(child_p, inverted);
                    }

                    rv.add(new DLLiterConceptInclusionImpl(child_cd, node_cd));
                }
            }
        }
        for (DAGNode node : dag.getObjectPropertyIndex().values()) {

            for (DAGNode child : node.descendans) {
                if (!check_redundant(node, child)) {
                    String uri = node.getUri();
                    boolean inverted = false;
                    if (uri.startsWith(DAG.owl_inverse)) {
                        uri = uri.substring(DAG.owl_inverse.length());
                        inverted = true;
                    }

                    URI node_uri = URI.create(uri);
                    Predicate node_p = predicateFactory.getPredicate(node_uri, 2);
                    RoleDescription node_rd = descFactory.getRoleDescription(node_p, inverted);

                    uri = child.getUri();
                    inverted = false;
                    if (uri.startsWith(DAG.owl_inverse)) {
                        uri = uri.substring(DAG.owl_inverse.length());
                        inverted = true;
                    }
                    URI child_uri = URI.create(uri);
                    Predicate child_p = predicateFactory.getPredicate(child_uri, 2);
                    RoleDescription child_rd = descFactory.getRoleDescription(child_p, inverted);

                    rv.add(new DLLiterRoleInclusionImpl(child_rd, node_rd));
                }
            }
        }
        for (DAGNode node : dag.getDataPropertyIndex().values()) {

            for (DAGNode child : node.descendans) {
                if (!check_redundant(node, child)) {
                    String uri = node.getUri();
                    boolean inverted = false;
                    if (uri.startsWith(DAG.owl_inverse)) {
                        uri = uri.substring(DAG.owl_inverse.length());
                        inverted = true;
                    }

                    URI node_uri = URI.create(uri);
                    Predicate node_p = predicateFactory.getPredicate(node_uri, 2);
                    RoleDescription node_rd = descFactory.getRoleDescription(node_p, inverted);

                    uri = child.getUri();
                    inverted = false;
                    if (uri.startsWith(DAG.owl_inverse)) {
                        uri = uri.substring(DAG.owl_inverse.length());
                        inverted = true;
                    }
                    URI child_uri = URI.create(uri);
                    Predicate child_p = predicateFactory.getPredicate(child_uri, 2);
                    RoleDescription child_rd = descFactory.getRoleDescription(child_p, inverted);

                    rv.add(new DLLiterRoleInclusionImpl(child_rd, node_rd));
                }
            }
        }
        log.debug("Finished semantic-reduction");
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
        DAGNode tc = tdag.getTDAG().get(child.getUri());

        if (sp == null || sc == null || tc == null) {
            return false;

        }
        return (sp.descendans.contains(sc) && sc.descendans.containsAll(tc.descendans));

    }

}
