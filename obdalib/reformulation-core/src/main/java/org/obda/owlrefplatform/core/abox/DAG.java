package org.obda.owlrefplatform.core.abox;

import org.semanticweb.owl.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

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
    public final static String owl_thing = "http://www.w3.org/2002/07/owl#Thing";
    public final static String owl_exists = "::__exists__::";
    public final static String owl_inverse_exists = "::__inverse__exists__::";


    public final static SemanticIndexRange NULL_RANGE = new SemanticIndexRange(-1, -1);
    public final static int NULL_INDEX = -1;


    /**
     * Build the DAG from the ontologies
     *
     * @param ontologies ontologies that contain TBox assertions for the DAG
     */
    public DAG(Set<OWLOntology> ontologies) {

        for (OWLOntology onto : ontologies) {
            log.info("Generating SemanticIndex for ontology: " + onto);

            for (OWLClass ax : onto.getReferencedClasses()) {
                OWLClass cls = (OWLClass) ax;
                Set<OWLDescription> sup_cls = cls.getSuperClasses(onto);
                if (sup_cls.size() == 0 && !cls.isOWLThing()) {
                    // top level class, manually add owl:Thing as superClass
                    addEdge(cls.getURI().toString(), owl_thing);
                } else {
                    for (OWLDescription sc : sup_cls) {
                        //FIXME: not handling existential quantification
                        addEdge(cls.getURI().toString(), sc.asOWLClass().getURI().toString());
                    }
                }
            }
            for (OWLObjectProperty ax : onto.getReferencedObjectProperties()) {
                OWLObjectProperty obj = (OWLObjectProperty) ax;
                Set<OWLObjectPropertyExpression> sup_prop = obj.getSuperProperties(onto);
                String obj_str = obj.asOWLObjectProperty().getURI().toString();
                if (sup_prop.size() == 0) {
                    addEdge(obj_str, owl_thing);
                } else {
                    for (OWLObjectPropertyExpression spe : sup_prop) {
                        addEdge(obj_str, spe.asOWLObjectProperty().getURI().toString());
                    }
                }

                addNode(owl_exists + obj_str);
                addNode(owl_inverse_exists + obj_str);
            }

            for (OWLDataProperty ax : onto.getReferencedDataProperties()) {
                OWLDataProperty data = (OWLDataProperty) ax;
                Set<OWLDataPropertyExpression> sup_pro = data.getSuperProperties(onto);
                String data_str = data.asOWLDataProperty().getURI().toString();
                if (sup_pro.size() == 0) {
                    addEdge(data_str, owl_thing);
                } else {
                    for (OWLDataPropertyExpression spe : sup_pro) {
                        addEdge(data_str, spe.asOWLDataProperty().getURI().toString());
                    }
                }

                addNode(owl_exists + data_str);
                addNode(owl_inverse_exists + data_str);

            }
            // Domain and Range
            for (OWLPropertyAxiom ax : onto.getObjectPropertyAxioms()) {
                if (ax instanceof OWLObjectPropertyDomainAxiom) {
                    OWLObjectPropertyDomainAxiom domainAxiom = (OWLObjectPropertyDomainAxiom) ax;
                    addEdge(owl_exists + domainAxiom.getProperty().toString(), domainAxiom.getDomain().toString());

                } else if (ax instanceof OWLObjectPropertyRangeAxiom) {
                    OWLObjectPropertyRangeAxiom rangeAxiom = (OWLObjectPropertyRangeAxiom) ax;
                    addEdge(owl_inverse_exists + rangeAxiom.getProperty().toString(), rangeAxiom.getRange().toString());
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

    private void addNode(String node) {
        DAGNode dagNode = dagnodes.get(node);
        if (dagNode == null) {
            dagNode = new DAGNode(node);
            dagnodes.put(node, dagNode);
            addEdge(node, owl_thing);
        }
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
            res.append(dagnodes.get(uri));
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
        return this.dagnodes.equals(otherDAG.dagnodes);
    }

    @Override
    public int hashCode() {
        return this.dagnodes.hashCode();
    }


}
