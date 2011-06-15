package it.unibz.krdb.obda.owlrefplatform.core.abox;


import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Assertion;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.ConceptDescription;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.DescriptionFactory;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.ExistentialConceptDescription;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.RoleDescription;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.BasicDescriptionFactory;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.DLLiterConceptInclusionImpl;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.DLLiterRoleInclusionImpl;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Prune Ontology for redundant assertions based on dependencies
 */
public class SemanticReduction {

    private static final Logger log = LoggerFactory.getLogger(SemanticReduction.class);
    private final DAG isa;
    private final DAG sigma;
    private final DAGChain isaChain;
    private final DAGChain sigmaChain;

    private final OBDADataFactory predicateFactory;
    private final DescriptionFactory descFactory;

    public SemanticReduction(DAG isa, DAG sigma) {
        this.isa = isa;
        this.sigma = sigma;

        this.isaChain = new DAGChain(isa);
        this.sigmaChain = new DAGChain(sigma);

        predicateFactory = OBDADataFactoryImpl.getInstance();
        descFactory = new BasicDescriptionFactory();

    }

    public List<Assertion> reduce() {
        log.debug("Starting semantic-reduction");
        List<Assertion> rv = new LinkedList<Assertion>();


        for (DAGNode node : isa.getClasses()) {

            // Ignore all edges from T
            if (node.getDescription().equals(DAG.thingConcept)) {
                continue;
            }

            for (DAGNode child : node.descendans) {

                if (!check_redundant(node, child)) {

                    rv.add(new DLLiterConceptInclusionImpl(
                            (ConceptDescription) child.getDescription(),
                            (ConceptDescription) node.getDescription()));
                }
            }
        }
        for (DAGNode node : isa.getRoles()) {

            for (DAGNode child : node.descendans) {
                if (!check_redundant_role(node, child)) {

                    rv.add(new DLLiterRoleInclusionImpl(
                            (RoleDescription) child.getDescription(),
                            (RoleDescription) node.getDescription()));
                }
            }
        }

        log.debug("Finished semantic-reduction");
        return rv;
    }

    private boolean check_redundant_role(DAGNode parent, DAGNode child) {

        if (check_directly_redundant_role(parent, child))
            return true;
        else {
            log.debug("Not directly redundant role {} {}", parent, child);
            for (DAGNode child_prime : parent.getChildren()) {
                if (!child_prime.equals(child) &&
                        check_directly_redundant_role(child_prime, child) &&
                        !check_redundant(child_prime, parent)) {
                    return true;
                }
            }
        }
        log.debug("Not redundant role {} {}", parent, child);

        return false;
    }

    private boolean check_directly_redundant_role(DAGNode parent, DAGNode child) {
        RoleDescription parentDesc = (RoleDescription) parent.getDescription();
        RoleDescription childDesc = (RoleDescription) child.getDescription();

        ExistentialConceptDescription existParentDesc = descFactory.getExistentialConceptDescription(
                parentDesc.getPredicate(),
                parentDesc.isInverse()
        );
        ExistentialConceptDescription existChildDesc = descFactory.getExistentialConceptDescription(
                childDesc.getPredicate(),
                childDesc.isInverse()
        );

        DAGNode exists_parent = new DAGNode(existChildDesc);
        DAGNode exists_child = new DAGNode(existChildDesc);

        return check_directly_redundant(parent, child) && check_directly_redundant(exists_parent, exists_child);
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
        DAGNode sp = sigmaChain.chain().get(parent.getDescription());
        DAGNode sc = sigmaChain.chain().get(child.getDescription());
        DAGNode tc = isaChain.chain().get(child.getDescription());
//
//        if (sp == null || sc == null || tc == null) {
//            return false;
//        }

        return (sp.descendans.contains(sc) && sc.descendans.containsAll(tc.descendans));

    }

}
