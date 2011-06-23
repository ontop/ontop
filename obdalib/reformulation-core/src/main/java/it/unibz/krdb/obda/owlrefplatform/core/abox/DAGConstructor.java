package it.unibz.krdb.obda.owlrefplatform.core.abox;


import it.unibz.krdb.obda.exception.DuplicateMappingException;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.*;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.DLLiterConceptInclusionImpl;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.DLLiterOntologyImpl;

import java.net.URI;
import java.util.*;

public class DAGConstructor {


    public static DAG getISADAG(DLLiterOntology ontology) {
        return new DAG(ontology);
    }


    public static DAG getSigma(DLLiterOntology ontology) {

        DLLiterOntology sigma = new DLLiterOntologyImpl(URI.create(""));

        for (Assertion assertion : ontology.getAssertions()) {
            if (assertion instanceof DLLiterConceptInclusionImpl) {
                DLLiterConceptInclusionImpl inclusion = (DLLiterConceptInclusionImpl) assertion;
                Description parent = inclusion.getIncluding();
                Description child = inclusion.getIncluded();
                if (parent instanceof ExistentialConceptDescription) {
                    continue;
                }
            }
            sigma.addAssertion(assertion);
        }

        sigma.addConcepts(new ArrayList<ConceptDescription>(ontology.getConcepts()));
        sigma.addRoles(new ArrayList<RoleDescription>(ontology.getRoles()));

        return getISADAG(sigma);
    }

    public List<OBDAMappingAxiom> getMappings(DAG dag) throws DuplicateMappingException {
        return null;
//        return SemanticIndexMappingGenerator.build(dag);
    }

    public DAGChain getTChainDAG(DAG dag) {

        return new DAGChain(dag);
    }

    public Set<Assertion> getSigmaChainDAG(Set<Assertion> assertions) {
        return null;
    }

    public static DAG filterPureISA(DAG dag) {

        Map<Description, DAGNode> classes = new HashMap<Description, DAGNode>();
        Map<Description, DAGNode> rolles = new HashMap<Description, DAGNode>();

        for (DAGNode node : dag.getClasses()) {

            if (node.getDescription() instanceof ExistentialConceptDescription) {
                continue;
            }
            DAGNode newNode = classes.get(node.getDescription());
            if (newNode == null) {
                newNode = new DAGNode(node.getDescription());
                newNode.equivalents = new LinkedList<DAGNode>(node.equivalents);
                classes.put(node.getDescription(), newNode);
            }

            for (DAGNode child : node.getChildren()) {
                if (child.getDescription() instanceof ExistentialConceptDescription) {
                    continue;
                }
                DAGNode newChild = classes.get(child.getDescription());
                if (newChild == null) {
                    newChild = new DAGNode(child.getDescription());
                    newChild.equivalents = new LinkedList<DAGNode>(child.equivalents);
                    classes.put(child.getDescription(), newChild);
                }

                if (!newChild.getDescription().equals(newNode.getDescription())) {
                    newChild.getParents().add(newNode);
                    newNode.getChildren().add(newChild);
                }

            }
        }

        for (DAGNode node : dag.getRoles()) {
            DAGNode newNode = rolles.get(node.getDescription());
            if (newNode == null) {
                newNode = new DAGNode(node.getDescription());
                newNode.equivalents = new LinkedList<DAGNode>(node.equivalents);
                rolles.put(node.getDescription(), newNode);
            }
            for (DAGNode child : node.getChildren()) {
                DAGNode newChild = rolles.get(child.getDescription());
                if (newChild == null) {
                    newChild = new DAGNode(child.getDescription());
                    newChild.equivalents = new LinkedList<DAGNode>(child.equivalents);
                    rolles.put(child.getDescription(), newChild);
                }
                if (!newChild.getDescription().equals(newNode.getDescription())) {
                    newChild.getParents().add(newNode);
                    newNode.getChildren().add(newChild);
                }
            }
        }
        DAG newDag = new DAG(classes, rolles, dag.equi_mappings);

        return newDag;
    }

}
