package it.unibz.inf.ontop.answering.reformulation.unfolding.impl;


import com.google.common.collect.ImmutableList;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.answering.reformulation.unfolding.QueryUnfolder;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.node.IntensionalDataNode;
import it.unibz.inf.ontop.iq.proposal.QueryMergingProposal;
import it.unibz.inf.ontop.iq.proposal.impl.QueryMergingProposalImpl;
import it.unibz.inf.ontop.model.term.GroundFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.ValueConstant;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.model.vocabulary.RDF;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.simple.SimpleRDF;

import java.util.Optional;

public class BasicQueryUnfolder implements QueryUnfolder {

    private final Mapping mapping;

    @AssistedInject
    private BasicQueryUnfolder(@Assisted Mapping mapping) {
        this.mapping = mapping;
    }

    @Override
    public IntermediateQuery optimize(IntermediateQuery query) throws EmptyQueryException {

        // Non-final
        Optional<IntensionalDataNode> optionalCurrentIntensionalNode = query.getIntensionalNodes().findFirst();


        while (optionalCurrentIntensionalNode.isPresent()) {

            //FIXME :check if it is correct. It should get the iri from the intensional node in triple form in second position (for a property) or in  third position  (for a class)
            IntensionalDataNode intensionalNode = optionalCurrentIntensionalNode.get();
            ImmutableList<? extends VariableOrGroundTerm> projectedVariables = intensionalNode.getProjectionAtom().getArguments();
            VariableOrGroundTerm variableOrGroundTerm = projectedVariables.get(1);
            IRI predicateIRI;
            Optional<IntermediateQuery> optionalMappingAssertion;
            if (variableOrGroundTerm.isGround()){
                ImmutableTerm groundTerm = ((GroundFunctionalTerm) variableOrGroundTerm).getTerm(0);
                if ( groundTerm instanceof ValueConstant) {
                    predicateIRI = new SimpleRDF().createIRI( ((ValueConstant) groundTerm).getValue());
                }

                else {
                    throw new IllegalStateException("Problem retrieving the predicate IRI");
                }

                if (predicateIRI.equals(RDF.TYPE)) {
                    VariableOrGroundTerm className = projectedVariables.get(2);

                    if (variableOrGroundTerm.isGround()) {
                        ImmutableTerm groundTerm2 = ((GroundFunctionalTerm) className).getTerm(0);

                        if (groundTerm2 instanceof ValueConstant) {
                            predicateIRI = new SimpleRDF().createIRI(((ValueConstant) groundTerm2).getValue());
                            optionalMappingAssertion = mapping.getRDFClassDefinition(predicateIRI);

                        } else {
                            throw new IllegalStateException("Problem retrieving the predicate IRI");
                        }
                    }else {
                        throw new IllegalStateException("Variables are not supported ");
                    }
                }
                else {
                    optionalMappingAssertion = mapping.getRDFPropertyDefinition(predicateIRI);
                }
            }
            else {
                throw new IllegalStateException("Variables are not supported ");
            }

            //old code
//            Optional<IntermediateQuery> optionalMappingAssertion = mapping.getDefinition(
//                    intensionalNode.getProjectionAtom().getPredicate());
//

            QueryMergingProposal queryMerging = new QueryMergingProposalImpl(intensionalNode, optionalMappingAssertion);
            query.applyProposal(queryMerging);

            /*
             * Next intensional node
             *
             * NB: some intensional nodes may have dropped during the last merge
             */
            optionalCurrentIntensionalNode = query.getIntensionalNodes().findFirst();
        }

        return query;
    }
}
