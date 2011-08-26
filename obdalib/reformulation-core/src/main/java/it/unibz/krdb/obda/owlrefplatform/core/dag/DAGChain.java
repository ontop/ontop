package it.unibz.krdb.obda.owlrefplatform.core.dag;

import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.ClassDescription;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Description;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.OntologyFactory;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.PropertySomeRestriction;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.BasicDescriptionFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reachability DAG
 */
public class DAGChain {


    private final Logger log = LoggerFactory.getLogger(this.getClass());

    // XXX: classes and roles are stored in one Map
    private final Map<Description, DAGNode> dag_nodes = new HashMap<Description, DAGNode>();
    private final Map<Description, Description> equi_map = new HashMap<Description, Description>();

    private static final OBDADataFactory predicateFactory = OBDADataFactoryImpl.getInstance();
    private static final OntologyFactory descFactory = new BasicDescriptionFactory();

    public DAGChain(DAG dag) {

        // First iteration to create all nodes and link ER and ER- parents and children
        for (DAGNode cls : dag.getClasses()) {
            ClassDescription clsc = (ClassDescription) cls.getDescription();

            DAGNode tnode = dag_nodes.get(clsc);
            if (tnode == null) {
                tnode = new DAGNode(cls.getDescription());
                dag_nodes.put(clsc, tnode);
            }

            // Link ER and ER- nodes children and parents object to single objects
            if (clsc instanceof PropertySomeRestriction) {
                PropertySomeRestriction nodeInv = descFactory.getPropertySomeRestriction(
                        ((PropertySomeRestriction) clsc).getPredicate(),
                        !((PropertySomeRestriction) clsc).isInverse()
                );
                DAGNode tnodeInv = dag_nodes.get(nodeInv);
                if (tnodeInv == null) {
                    tnodeInv = new DAGNode(nodeInv);
                    dag_nodes.put(nodeInv, tnodeInv);
                    tnodeInv.setParents(tnode.getParents());
                    tnodeInv.setChildren(tnode.getChildren());
                } else if (tnodeInv.getParents() != tnode.getParents() ||
                        tnodeInv.getChildren() != tnode.getChildren()) {
                    tnode.setParents(tnodeInv.getParents());
                    tnode.setChildren(tnodeInv.getChildren());
                }

            }
        }

        for (DAGNode node : dag.getClasses()) {

            ClassDescription nodeDesc = (ClassDescription) node.getDescription();
            DAGNode tnode = dag_nodes.get(nodeDesc);

            PropertySomeRestriction invNodeExDesc = null;

            if (nodeDesc instanceof PropertySomeRestriction) {
                PropertySomeRestriction nodeExDesc = (PropertySomeRestriction) nodeDesc;
                invNodeExDesc = descFactory.getPropertySomeRestriction(
                        nodeExDesc.getPredicate(),
                        !nodeExDesc.isInverse()
                );
            }

            for (DAGNode child : node.getChildren()) {
                ClassDescription childNodeDesc = (ClassDescription) child.getDescription();
                DAGNode tchild = dag_nodes.get(childNodeDesc);

                PropertySomeRestriction childInvNodeExDesc = null;
                if (childNodeDesc instanceof PropertySomeRestriction) {
                    PropertySomeRestriction childNodeExDesc = (PropertySomeRestriction) childNodeDesc;
                    childInvNodeExDesc = descFactory.getPropertySomeRestriction(
                            childNodeExDesc.getPredicate(),
                            !childNodeExDesc.isInverse()
                    );
                }

                tnode.getChildren().add(tchild);
                tchild.getParents().add(tnode);

                // Current node is ER, link it's child to ER-
                if (invNodeExDesc != null) {
                    tchild.getParents().add(dag_nodes.get(invNodeExDesc));
                }

                //Current child is ER, link it's ER- to parent
                if (childInvNodeExDesc != null) {
                    tnode.getChildren().add(dag_nodes.get(childInvNodeExDesc));
                }
            }
        }

        for (DAGNode node : dag.getRoles()) {
            DAGNode tnode = dag_nodes.get(node.getDescription());
            if (tnode == null) {
                tnode = new DAGNode(node.getDescription());
                dag_nodes.put(node.getDescription(), tnode);
            }
            for (DAGNode child : node.getChildren()) {
                DAGNode tchild = dag_nodes.get(child.getDescription());
                if (tchild == null) {
                    tchild = new DAGNode(child.getDescription());
                    dag_nodes.put(child.getDescription(), tchild);
                }
                tnode.getChildren().add(tchild);
                tchild.getParents().add(tnode);
            }
        }

        DAGOperations.removeCycles(dag_nodes, equi_map);
        for (Description uri : equi_map.keySet()) {
            DAGNode n = new DAGNode(uri);

            DAGNode equi_n = dag_nodes.get(equi_map.get(uri));


            equi_n.descendans = new HashSet<DAGNode>(equi_n.equivalents);
            equi_n.descendans.add(equi_n);

            n.setChildren(equi_n.getChildren());
            n.setParents(equi_n.getParents());
            n.descendans = equi_n.descendans;

            dag_nodes.put(uri, n);

        }


        DAGOperations.computeTransitiveReduct(dag_nodes);
        DAGOperations.buildDescendants(dag_nodes);
    }

    public Map<Description, DAGNode> chain() {
        return dag_nodes;
    }

}
