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

    private Map<String, DAGNode> cls_nodes = new HashMap<String, DAGNode>();
    private Map<String, DAGNode> objectprop_nodes = new HashMap<String, DAGNode>();
    private Map<String, DAGNode> dataprop_nodes = new HashMap<String, DAGNode>();

    private Set<OWLDataProperty> dataprops = new HashSet<OWLDataProperty>();
    private Set<OWLObjectProperty> objprops = new HashSet<OWLObjectProperty>();


    private int index_counter = 1;
    public final static String owl_thing = "http://www.w3.org/2002/07/owl#Thing";
    public final static String owl_exists = "::__exists__";
    public final static String owl_inverse_exists = "::__inverse__exists__";
    public final static String owl_inverse = "::__inverse__::";

    public final static String owl_exists_obj = owl_exists + "object_property::";
    public final static String owl_exists_data = owl_exists + "data_property::";
    public final static String owl_inverse_exists_obj = owl_inverse_exists + "object_property::";
    public final static String owl_inverse_exists_data = owl_inverse_exists + "data_property::";


    public final static SemanticIndexRange NULL_RANGE = new SemanticIndexRange(-1, -1);
    public final static int NULL_INDEX = -1;

    public Map<String, String> equi_mappings = new HashMap<String, String>();


    /**
     * Build the DAG from the ontologies
     *
     * @param ontologies ontologies that contain TBox assertions for the DAG
     */
    public DAG(Set<OWLOntology> ontologies) {

        for (OWLOntology onto : ontologies) {
            log.info("Generating SemanticIndex for ontology: " + onto);

            dataprops = onto.getDataPropertiesInSignature();
            for (OWLDataProperty prop : dataprops) {
                addNode(prop.getURI().toString(), dataprop_nodes);
                addNode(owl_inverse + prop.getURI().toString(), dataprop_nodes);

                addNode(owl_exists_data + prop.getURI().toString(), cls_nodes);
                addNode(owl_inverse_exists_data + prop.getURI().toString(), cls_nodes);

                addEdge(owl_exists_data + prop.getURI().toString(), owl_thing, cls_nodes);
                addEdge(owl_inverse_exists_data + prop.getURI().toString(), owl_thing, cls_nodes);
            }
            objprops = onto.getObjectPropertiesInSignature();
            for (OWLObjectProperty prop : objprops) {
                addNode(prop.getURI().toString(), objectprop_nodes);
                addNode(owl_inverse + prop.getURI().toString(), objectprop_nodes);

                addNode(owl_exists_obj + prop.getURI().toString(), cls_nodes);
                addNode(owl_inverse_exists_obj + prop.getURI().toString(), cls_nodes);

                addEdge(owl_exists_obj + prop.getURI().toString(), owl_thing, cls_nodes);
                addEdge(owl_inverse_exists_obj + prop.getURI().toString(), owl_thing, cls_nodes);
            }

            for (OWLAxiom ax : onto.getAxioms()) {
                if (ax instanceof OWLSubClassAxiom) {
                    OWLSubClassAxiom axiom = (OWLSubClassAxiom) ax;
                    String sup_cls = getOwlClass(axiom.getSuperClass());
                    String sub_cls = getOwlClass(axiom.getSubClass());
                    addEdge(sup_cls, owl_thing, cls_nodes);
                    addEdge(sub_cls, sup_cls, cls_nodes);

                    log.debug("Class: {} {}", sub_cls, sup_cls);
                } else if (ax instanceof OWLSubPropertyAxiom) {
                    OWLSubPropertyAxiom axiom = (OWLSubPropertyAxiom) ax;
                    String sup_prop = getOWlProperty(axiom.getSuperProperty());
                    String sub_prop = getOWlProperty(axiom.getSubProperty());

                    addEdge(sub_prop, sup_prop, objectprop_nodes);

                    String implicit_sub;
                    if (sub_prop.startsWith(owl_inverse)) {
                        implicit_sub = sub_prop.substring(owl_inverse.length());
                    } else {
                        implicit_sub = owl_inverse + sub_prop;
                    }
                    String implicit_sup;
                    if (sup_prop.startsWith(owl_inverse)) {
                        implicit_sup = sup_prop.substring(owl_inverse.length());
                    } else {
                        implicit_sup = owl_inverse + sup_prop;
                    }
                    addEdge(implicit_sub, implicit_sup, objectprop_nodes);


                    log.debug("SubProperty: {} {}", sub_prop, sup_prop);
                } else if (ax instanceof OWLObjectPropertyDomainAxiom) {
                    OWLObjectPropertyDomainAxiom domainAxiom = (OWLObjectPropertyDomainAxiom) ax;
                    OWLObjectProperty prop = domainAxiom.getProperty().asOWLObjectProperty();
                    String domain = getOwlClass(domainAxiom.getDomain());

                    addEdge(owl_exists_obj + prop.getURI().toString(), domain, cls_nodes);
                } else if (ax instanceof OWLObjectPropertyRangeAxiom) {
                    OWLObjectPropertyRangeAxiom rangeAxiom = (OWLObjectPropertyRangeAxiom) ax;
                    String range = getOwlClass(rangeAxiom.getRange());

                    addEdge(owl_inverse_exists_obj + rangeAxiom.getProperty().asOWLObjectProperty().getURI().toString(),
                            range, cls_nodes);
                } else {
                    log.debug("Unsupported axiom: {}", ax);
                }
            }
        }
        DAGOperations.removeCycles(cls_nodes, equi_mappings);
        DAGOperations.computeTransitiveReduct(cls_nodes);

        DAGOperations.removeCycles(objectprop_nodes, equi_mappings);
        DAGOperations.computeTransitiveReduct(objectprop_nodes);

        DAGOperations.removeCycles(dataprop_nodes, equi_mappings);
        DAGOperations.computeTransitiveReduct(dataprop_nodes);

        index();
    }

    private String getOwlClass(OWLObject obj) {
        if (obj instanceof OWLObjectMinCardinalityRestriction) {
            OWLObjectMinCardinalityRestriction restriction = (OWLObjectMinCardinalityRestriction) obj;
            assert (restriction.getCardinality() == 1);

            OWLObjectProperty rv;
            if (restriction.getProperty() instanceof OWLObjectPropertyInverse) {
                rv = ((OWLObjectPropertyInverse) restriction.getProperty()).getInverse().asOWLObjectProperty();
                if (dataprops.contains(rv)) {
                    return owl_inverse_exists_data + rv.getURI().toString();
                } else if (objprops.contains(rv)) {
                    return owl_inverse_exists_obj + rv.getURI().toString();
                }
            } else {
                rv = restriction.getProperty().asOWLObjectProperty();

                if (dataprops.contains(rv)) {
                    return owl_exists_data + rv.getURI().toString();
                } else if (objprops.contains(rv)) {
                    return owl_exists_obj + rv.getURI().toString();
                }
            }


        } else if (obj instanceof OWLObjectSomeRestriction) {
            OWLObjectSomeRestriction restriction = (OWLObjectSomeRestriction) obj;
            OWLObjectProperty rv;
            if (restriction.getProperty() instanceof OWLObjectPropertyInverse) {
                rv = ((OWLObjectPropertyInverse) restriction.getProperty()).getInverse().asOWLObjectProperty();
                if (dataprops.contains(rv)) {
                    return owl_inverse_exists_data + rv.getURI().toString();
                } else if (objprops.contains(rv)) {
                    return owl_inverse_exists_obj + rv.getURI().toString();
                }
            } else {
                rv = restriction.getProperty().asOWLObjectProperty();
                if (dataprops.contains(rv)) {
                    return owl_exists_data + rv.getURI().toString();
                } else if (objprops.contains(rv)) {
                    return owl_exists_obj + rv.getURI().toString();
                }
            }

        } else if (obj instanceof OWLObjectProperty) {
            return ((OWLObjectProperty) obj).asOWLObjectProperty().getURI().toString();
        }
        OWLClass cls = (OWLClass) obj;
        if (cls.isOWLThing()) {
            return owl_thing;
        }

        return cls.asOWLClass().getURI().toString();
    }

    private String getOWlProperty(OWLObject obj) {
        if (obj instanceof OWLObjectProperty) {
            return ((OWLObjectProperty) obj).asOWLObjectProperty().getURI().toString();
        } else if (obj instanceof OWLObjectPropertyInverse) {
            OWLObjectPropertyInverse inverse = (OWLObjectPropertyInverse) obj;
            return owl_inverse + inverse.getInverse().asOWLObjectProperty().getURI().toString();

        }
        return null;
    }

    /**
     * Create DAG from previously saved index
     *
     * @param cls_index list of TBox class assertions with computed ranges and indexes
     */
    public DAG(List<DAGNode> cls_index, List<DAGNode> objectprop_index, List<DAGNode> dataprop_index) {
        for (DAGNode node : cls_index) {
            cls_nodes.put(node.getUri(), node);
        }
        for (DAGNode node : objectprop_index) {
            objectprop_nodes.put(node.getUri(), node);
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
        if (!t.getChildren().contains(f)) {
            t.getChildren().add(f);
        }
        if (!f.getParents().contains(t)) {
            f.getParents().add(t);
        }
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
