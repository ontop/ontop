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

    //private Map<String, DAGNode> dagnodes = new HashMap<String, DAGNode>();
    private Map<String, DAGNode> cls_nodes = new HashMap<String, DAGNode>();
    private Map<String, DAGNode> objectprop_nodes = new HashMap<String, DAGNode>();
    private Map<String, DAGNode> dataprop_nodes = new HashMap<String, DAGNode>();


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

            for (OWLClass ax : onto.getClassesInSignature()) {
                OWLClass cls = (OWLClass) ax;
                Set<OWLDescription> sup_cls = cls.getSuperClasses(onto);
                if (sup_cls.size() == 0 && !cls.isOWLThing()) {
                    // top level class, manually add owl:Thing as superClass
                    addEdge(cls.getURI().toString(), owl_thing, cls_nodes);
                } else {
                    for (OWLDescription sc : sup_cls) {
                        //FIXME: not handling existential quantification
                        addEdge(cls.getURI().toString(), sc.asOWLClass().getURI().toString(), cls_nodes);
                    }
                }
            }
            for (OWLObjectProperty ax : onto.getObjectPropertiesInSignature()) {
                OWLObjectProperty obj = (OWLObjectProperty) ax;
                Set<OWLObjectPropertyExpression> sup_prop = obj.getSuperProperties(onto);
                String obj_str = obj.asOWLObjectProperty().getURI().toString();

                if (sup_prop.size() == 0) {
                    addNode(obj_str, objectprop_nodes);
                }

                for (OWLObjectPropertyExpression spe : sup_prop) {
                    addEdge(obj_str, spe.asOWLObjectProperty().getURI().toString(), objectprop_nodes);

                }

                addNode(owl_exists + obj_str, cls_nodes);
                addEdge(owl_exists + obj_str, owl_thing, cls_nodes);

                addNode(owl_inverse_exists + obj_str, cls_nodes);
                addEdge(owl_inverse_exists + obj_str, owl_thing, cls_nodes);
            }

            for (OWLDataProperty ax : onto.getDataPropertiesInSignature()) {
                OWLDataProperty data = (OWLDataProperty) ax;
                Set<OWLDataPropertyExpression> sup_pro = data.getSuperProperties(onto);
                String data_str = data.asOWLDataProperty().getURI().toString();

                for (OWLDataPropertyExpression spe : sup_pro) {
                    addEdge(data_str, spe.asOWLDataProperty().getURI().toString(), dataprop_nodes);
                }


                addNode(owl_exists + data_str, cls_nodes);
                addEdge(owl_exists + data_str, owl_thing, cls_nodes);

                addNode(owl_inverse_exists + data_str, cls_nodes);
                addEdge(owl_inverse_exists + data_str, owl_thing, cls_nodes);

            }
            // Domain and Range
            for (OWLPropertyAxiom ax : onto.getObjectPropertyAxioms()) {
                if (ax instanceof OWLObjectPropertyDomainAxiom) {
                    OWLObjectPropertyDomainAxiom domainAxiom = (OWLObjectPropertyDomainAxiom) ax;
                    addEdge(owl_exists + domainAxiom.getProperty().asOWLObjectProperty().getURI().toString(),
                            domainAxiom.getDomain().asOWLClass().getURI().toString(), cls_nodes);

                } else if (ax instanceof OWLObjectPropertyRangeAxiom) {
                    OWLObjectPropertyRangeAxiom rangeAxiom = (OWLObjectPropertyRangeAxiom) ax;
                    addEdge(owl_inverse_exists + rangeAxiom.getProperty().asOWLObjectProperty().getURI().toString(),
                            rangeAxiom.getRange().asOWLClass().getURI().toString(), cls_nodes);
                }
            }

        }
        index();
    }

    /**
     * Create DAG from previously saved index
     *
     * @param cls_index        list of TBox class assertions with computed ranges and indexes
     * @param objectprop_index list of TBox role assertions with computed ranges and indexes
     * @param dataprop_index   list of TBox literal assertions with computed ranges and indexes
     */
    public DAG(List<DAGNode> cls_index, List<DAGNode> objectprop_index, List<DAGNode> dataprop_index) {
        for (DAGNode node : cls_index) {
            cls_nodes.put(node.getUri(), node);
        }
        for (DAGNode node : objectprop_index) {
            objectprop_nodes.put(node.getUri(), node);
        }
        for (DAGNode node : dataprop_index) {
            dataprop_nodes.put(node.getUri(), node);
        }
    }

    public Map<String, DAGNode> getClassIndex() {
        return cls_nodes;
    }

    public Map<String, DAGNode> getObjectPropertyIndex() {
        return objectprop_nodes;
    }

    public Map<String, DAGNode> getDataPropertyIndex() {
        return dataprop_nodes;
    }

    private void addEdge(String from, String to, Map<String, DAGNode> dagnodes) {

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

    private void addNode(String node, Map<String, DAGNode> dagnodes) {
        DAGNode dagNode = dagnodes.get(node);
        if (dagNode == null) {
            dagNode = new DAGNode(node);
            dagnodes.put(node, dagNode);
        }
    }

    private void index() {
        LinkedList<DAGNode> roots = new LinkedList<DAGNode>();
        for (DAGNode n : cls_nodes.values()) {
            if (n.getParents().isEmpty()) {
                roots.add(n);
            }
        }
        for (DAGNode n : objectprop_nodes.values()) {
            if (n.getParents().isEmpty()) {
                roots.add(n);
            }
        }
        for (DAGNode n : dataprop_nodes.values()) {
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
        for (DAGNode node : cls_nodes.values()) {
            res.append(node);
            res.append("\n");
        }
        for (DAGNode node : objectprop_nodes.values()) {
            res.append(node);
            res.append("\n");
        }
        for (DAGNode node : dataprop_nodes.values()) {
            res.append(node);
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
        return this.cls_nodes.equals(otherDAG.cls_nodes) &&
                this.objectprop_nodes.equals(otherDAG.objectprop_nodes) &&
                this.dataprop_nodes.equals(otherDAG.dataprop_nodes);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result += 37 * result + this.cls_nodes.hashCode();
        result += 37 * result + this.objectprop_nodes.hashCode();
        result += 37 * result + this.dataprop_nodes.hashCode();
        return result;
    }


}
