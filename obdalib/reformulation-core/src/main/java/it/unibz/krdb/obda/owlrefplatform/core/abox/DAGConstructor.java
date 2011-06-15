package it.unibz.krdb.obda.owlrefplatform.core.abox;


import it.unibz.krdb.obda.exception.DuplicateMappingException;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.*;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.DLLiterConceptInclusionImpl;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.DLLiterOntologyImpl;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
        return SemanticIndexMappingGenerator.build(dag);
    }

    public DAGChain getTChainDAG(DAG dag) {

        return new DAGChain(dag);
    }

    public Set<Assertion> getSigmaChainDAG(Set<Assertion> assertions) {
        return null;

    }

}
