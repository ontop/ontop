package org.obda.owlrefplatform.core.abox;

import java.util.*;

import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owl.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLSubClassAxiom;
import org.semanticweb.owl.model.OWLSubPropertyAxiom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stores the SemanticIndex of TBox and implements operations for serializing
 * and deserializing it
 *
 * @author Sergejs Pugacs
 */
public class DAG {

    private final Logger log = LoggerFactory.getLogger(DAG.class);

    private Map<String, DAGNode> dagnodes = new HashMap<String, DAGNode>();

    private int index_counter = 1;

    public final static SemanticIndexRange NULL_RANGE = new SemanticIndexRange(
            -1, -1);
    public final static int NULL_INDEX = -1;

    /**
     * Build the DAG from the ontologies
     *
     * @param ontologies ontologies that contain TBox assertions for the DAG
     */
    public DAG(Set<OWLOntology> ontologies) {

        for (OWLOntology onto : ontologies) {
            log.debug("Generating SemanticIndex for ontology: " + onto);

            for (OWLAxiom ax : onto.getAxioms()) {
                if (ax instanceof OWLSubClassAxiom) {
                    OWLSubClassAxiom edge = (OWLSubClassAxiom) ax;

                    // FIXME: not handling existencial on the left side

                    if (!edge.getSubClass().isOWLNothing() && !edge.getSuperClass().isOWLThing()) {
                        String subClass = edge.getSubClass().asOWLClass().getURI().toString();
                        String superClass = edge.getSuperClass().asOWLClass().getURI().toString();
                        addEdge(subClass, superClass);
                    }
                } else if (ax instanceof OWLSubPropertyAxiom) {
                    OWLSubPropertyAxiom<OWLObjectProperty> edge = (OWLSubPropertyAxiom<OWLObjectProperty>) ax;

                    if (!edge.getSubProperty().isAnonymous()
                            && !edge.getSuperProperty().isAnonymous()) {

                        String subProperty = edge.getSubProperty().getURI().toString();
                        String superProperty = edge.getSuperProperty().getURI().toString();
                        addEdge(subProperty, superProperty);
                    }
                } else if (ax instanceof OWLObjectPropertyRangeAxiom) {

                    // FIXME: Create an Exists(Inverse R)
                    log.debug("ObjectPropRange: " + ax);

                } else if (ax instanceof OWLObjectPropertyDomainAxiom) {

                    // FIXME: Create an Exists(R)
                    log.debug("ObjectPropDomain: " + ax);

                } else {
                    log.debug("Not supported axiom: " + ax);
                }
            }
        }
        index();
    }

    /**
     * Create DAG from previously saved index
     *
     * @param index list of TBox nodes with computed ranges and indexes
     */
    public DAG(List<DAGNode> index) {
        for (DAGNode node : index) {
            dagnodes.put(node.getUri(), node);
        }
    }

    public Map<String, DAGNode> getIndex() {
        return dagnodes;
    }

    private void addEdge(String from, String to) {

        DAGNode f = dagnodes.get(from);
        if (f == null) {
            f = new DAGNode(from);
            dagnodes.put(from, f);
        }

        DAGNode t = dagnodes.get(to);
        if (t == null) {
            t = new DAGNode(to);
            dagnodes.put(to, t);
        }
        t.getChildren().add(f);
        f.getParents().add(t);
    }

    private void index() {
        LinkedList<DAGNode> roots = new LinkedList<DAGNode>();
        for (DAGNode n : dagnodes.values()) {
            if (n.getParents().isEmpty()) {
                roots.add(n);
            }
        }
        // The unit tests depend on this to guarantee certain numberings
        Collections.sort(roots);

        for (DAGNode node : roots) {
            indexNode(node);
        }
    }

    private void indexNode(DAGNode node) {

        if (node.getIndex() == NULL_INDEX) {
            node.setIndex(index_counter);
            node.setRange(new SemanticIndexRange(index_counter, index_counter));
            index_counter++;
        } else {
            return;
        }
        for (DAGNode ch : node.getChildren()) {
            indexNode(ch);
            node.getRange().addRange(ch.getRange());
        }
    }

    @Override
    public String toString() {
        StringBuffer res = new StringBuffer();
        for (String uri : dagnodes.keySet()) {
            res.append(uri + dagnodes.get(uri));
            res.append("\n");
        }
        return res.toString();
    }

    @Override
    public boolean equals(Object other) {
        if (other == null)
            return false;
        if (other == this)
            return true;
        if (this.getClass() != other.getClass())
            return false;

        DAG otherDAG = (DAG) other;
        if (this.dagnodes.size() != otherDAG.dagnodes.size())
            return false;

        for (String i : otherDAG.dagnodes.keySet()) {
            if (!this.dagnodes.containsKey(i)) {
                return false;
            }
        }
        return true;
    }

}
