package it.unibz.krdb.obda.owlrefplatform.core.abox;


import it.unibz.krdb.obda.exception.DuplicateMappingException;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.*;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.BasicDescriptionFactory;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.DLLiterConceptInclusionImpl;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.DLLiterOntologyImpl;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.OWLAPITranslator;

import java.net.URI;
import java.util.*;

public class DAGConstructor {

    private static final OBDADataFactory predicateFactory = OBDADataFactoryImpl.getInstance();
    private static final DescriptionFactory descFactory = new BasicDescriptionFactory();


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
            RoleDescription nodeDesc = (RoleDescription) node.getDescription();

            if (nodeDesc.getPredicate().getName().toString().startsWith(OWLAPITranslator.AUXROLEURI)) {
                continue;
            }

            if (nodeDesc.isInverse()) {
                RoleDescription posNode = descFactory.getRoleDescription(nodeDesc.getPredicate(), false);
                DAGNode newNode = rolles.get(posNode);
                if (newNode == null) {
                    newNode = new DAGNode(posNode);
                    rolles.put(posNode, newNode);
                }
                continue;
            }

            DAGNode newNode = rolles.get(nodeDesc);

            if (newNode == null) {
                newNode = new DAGNode(nodeDesc);
                newNode.equivalents = new LinkedList<DAGNode>(node.equivalents);
                rolles.put(nodeDesc, newNode);
            }
            for (DAGNode child : node.getChildren()) {
                RoleDescription childDesc = (RoleDescription) child.getDescription();
                if (childDesc.getPredicate().getName().toString().startsWith(OWLAPITranslator.AUXROLEURI)) {
                    continue;
                }
                if (childDesc.isInverse()) {
                    RoleDescription posChild = descFactory.getRoleDescription(childDesc.getPredicate(), false);
                    DAGNode newChild = rolles.get(posChild);
                    if (newChild == null) {
                        newChild = new DAGNode(posChild);
                        rolles.put(posChild, newChild);
                    }
                    continue;
                }

                DAGNode newChild = rolles.get(childDesc);
                if (newChild == null) {
                    newChild = new DAGNode(childDesc);
                    newChild.equivalents = new LinkedList<DAGNode>(child.equivalents);
                    rolles.put(childDesc, newChild);
                }
                if (!newChild.getDescription().equals(newNode.getDescription())) {
                    newChild.getParents().add(newNode);
                    newNode.getChildren().add(newChild);
                }
            }
        }
        Map<Description, Description> newEquivalentMappings = new HashMap<Description, Description>();
        for (Description desc : dag.equi_mappings.keySet()) {
            Description key = makePositive(desc);
            Description val = makePositive(dag.equi_mappings.get(desc));
            newEquivalentMappings.put(key, val);

        }
        DAG newDag = new DAG(classes, rolles, newEquivalentMappings);

        return newDag;
    }

    private static Description makePositive(Description desc) {

        if (desc instanceof RoleDescription) {
            RoleDescription roleKey = (RoleDescription) desc;
            return descFactory.getRoleDescription(roleKey.getPredicate(), false);

        } else if (desc instanceof ExistentialConceptDescription) {
            ExistentialConceptDescription conceptKey = (ExistentialConceptDescription) desc;
            return descFactory.getExistentialConceptDescription(conceptKey.getPredicate(), false);

        } else {
            return desc;
        }
    }

}
