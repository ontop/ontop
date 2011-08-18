package it.unibz.krdb.obda.owlrefplatform.core.abox;

import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Assertion;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.ConceptDescription;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.DLLiterOntology;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Description;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.DescriptionFactory;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.ExistentialConceptDescription;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.RoleDescription;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.BasicDescriptionFactory;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.DLLiterConceptInclusionImpl;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.DLLiterRoleInclusionImpl;

import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DAG {

    private final Logger log = LoggerFactory.getLogger(DAG.class);

    private int index_counter = 1;

    public final static SemanticIndexRange NULL_RANGE = new SemanticIndexRange(-1, -1);
    public final static int NULL_INDEX = -1;

    public Map<Description, Description> equi_mappings = new HashMap<Description, Description>();


    private final Map<Description, DAGNode> classes;
    private final Map<Description, DAGNode> roles;

    private static final OBDADataFactory predicateFactory = OBDADataFactoryImpl.getInstance();
    private static final DescriptionFactory descFactory = new BasicDescriptionFactory();

    public final static String thingStr = "http://www.w3.org/2002/07/owl#Thing";
    public final static URI thingUri = URI.create(thingStr);
    public final static Predicate thingPred = predicateFactory.getPredicate(thingUri, 1);
    public final static ConceptDescription thingConcept = descFactory.getAtomicConceptDescription(thingPred);
    public final DAGNode thing = new DAGNode(thingConcept);


    /**
     * Build the DAG from the ontology
     *
     * @param ontology ontology that contain TBox assertions for the DAG
     */
    public DAG(DLLiterOntology ontology) {

        classes = new LinkedHashMap<Description, DAGNode>(ontology.getConcepts().size());
        roles = new LinkedHashMap<Description, DAGNode>(ontology.getRoles().size());


        classes.put(thingConcept, thing);

        for (ConceptDescription concept : ontology.getConcepts()) {
            DAGNode node = new DAGNode(concept);

            if (!concept.equals(thingConcept)) {
                addParent(node, thing);
                classes.put(concept, node);
            }
        }
        for (RoleDescription role : ontology.getRoles()) {
            roles.put(role, new DAGNode(role));

            RoleDescription roleInv = descFactory.getRoleDescription(role.getPredicate(), !role.isInverse());
            roles.put(roleInv, new DAGNode(roleInv));

            ExistentialConceptDescription existsRole = descFactory.getExistentialConceptDescription(
                    role.getPredicate(),
                    role.isInverse());
            ExistentialConceptDescription existsRoleInv = descFactory.getExistentialConceptDescription(
                    role.getPredicate(),
                    !role.isInverse()
            );
            DAGNode existsNode = new DAGNode(existsRole);
            DAGNode existsNodeInv = new DAGNode(existsRoleInv);
            classes.put(existsRole, existsNode);
            classes.put(existsRoleInv, existsNodeInv);

            addParent(existsNode, thing);
            addParent(existsNodeInv, thing);
        }


        for (Assertion assertion : ontology.getAssertions()) {

            if (assertion instanceof DLLiterConceptInclusionImpl) {
                DLLiterConceptInclusionImpl clsIncl = (DLLiterConceptInclusionImpl) assertion;
                ConceptDescription parent = clsIncl.getIncluding();
                ConceptDescription child = clsIncl.getIncluded();

                addClassEdge(parent, child);
            } else if (assertion instanceof DLLiterRoleInclusionImpl) {
                DLLiterRoleInclusionImpl roleIncl = (DLLiterRoleInclusionImpl) assertion;
                RoleDescription parent = roleIncl.getIncluding();
                RoleDescription child = roleIncl.getIncluded();

                addRoleEdge(parent, child);
            }
        }
        clean();
    }

    private void addParent(DAGNode child, DAGNode parent) {
        if (!child.getDescription().equals(parent.getDescription())) {
            child.getParents().add(parent);
            parent.getChildren().add(child);
        }
    }

    public DAG(Map<Description, DAGNode> classes, Map<Description, DAGNode> roles, Map<Description, Description> equiMap) {
        this.classes = classes;
        this.roles = roles;
        this.equi_mappings = equiMap;
    }


    private void addClassEdge(ConceptDescription parent, ConceptDescription child) {

        DAGNode parentNode;
        if (classes.containsKey(parent)) {
            parentNode = classes.get(parent);
        } else {
            parentNode = new DAGNode(parent);
            classes.put(parent, parentNode);
        }
        DAGNode childNode;
        if (classes.containsKey(child)) {
            childNode = classes.get(child);
        } else {
            childNode = new DAGNode(child);
            classes.put(child, childNode);
        }
        addParent(childNode, parentNode);
        addParent(parentNode, thing);

    }

    private void addRoleEdge(RoleDescription parent, RoleDescription child) {
        addRoleEdgeSingle(parent, child);

        addRoleEdgeSingle(descFactory.getRoleDescription(parent.getPredicate(), !parent.isInverse()),
                descFactory.getRoleDescription(child.getPredicate(), !child.isInverse()));
    }

    private void addRoleEdgeSingle(RoleDescription parent, RoleDescription child) {
        DAGNode parentNode = roles.get(parent);
        if (parentNode == null) {
            parentNode = new DAGNode(parent);
            roles.put(parent, parentNode);
        }

        DAGNode childNode = roles.get(child);
        if (childNode == null) {
            childNode = new DAGNode(child);
            roles.put(child, childNode);
        }
        addParent(childNode, parentNode);

        ConceptDescription existsParent = descFactory.getExistentialConceptDescription(
                parent.getPredicate(),
                parent.isInverse());


        ConceptDescription existChild = descFactory.getExistentialConceptDescription(
                child.getPredicate(),
                child.isInverse());

        addClassEdge(existsParent, existChild);
        addClassEdge(thingConcept, existsParent);

    }

    public void clean() {
        DAGOperations.removeCycles(classes, equi_mappings);
        DAGOperations.computeTransitiveReduct(classes);

        DAGOperations.removeCycles(roles, equi_mappings);
        DAGOperations.computeTransitiveReduct(roles);

        DAGOperations.buildDescendants(classes);
        DAGOperations.buildDescendants(roles);
    }

    public void index() {

        LinkedList<DAGNode> roots = new LinkedList<DAGNode>();

        for (DAGNode n : classes.values()) {
            if (n.getParents().isEmpty()) {
                roots.add(n);
            }
        }
        for (DAGNode n : roles.values()) {
            if (n.getParents().isEmpty()) {
                roots.add(n);
            }
        }

        for (DAGNode node : roots) {
            indexNode(node);
        }
        for (DAGNode node : roots) {
            mergeRangeNode(node);
        }
    }

    private void mergeRangeNode(DAGNode node) {


        for (DAGNode ch : node.getChildren()) {
            if (ch != node) {
                mergeRangeNode(ch);
                node.getRange().addRange(ch.getRange());
            }

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
            if (ch != node) {
                indexNode(ch);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        for (DAGNode node : classes.values()) {
            res.append(node);
            res.append("\n");
        }
        for (DAGNode node : roles.values()) {
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
        return this.classes.equals(otherDAG.classes) &&
                this.roles.equals(otherDAG.roles);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result += 37 * result + this.classes.hashCode();
        result += 37 * result + this.roles.hashCode();
        return result;
    }


    public Collection<DAGNode> getClasses() {
        return classes.values();
    }

    public Collection<DAGNode> getRoles() {
        return roles.values();
    }

    public DAGNode getClassNode(ConceptDescription conceptDescription) {
        DAGNode rv = classes.get(conceptDescription);
        if (rv == null) {
            rv = classes.get(equi_mappings.get(conceptDescription));
        }
        return rv;
    }

    public DAGNode getRoleNode(RoleDescription roleDescription) {
        DAGNode rv = roles.get(roleDescription);
        if (rv == null) {
            rv = roles.get(equi_mappings.get(roleDescription));
        }
        return rv;
    }

}
