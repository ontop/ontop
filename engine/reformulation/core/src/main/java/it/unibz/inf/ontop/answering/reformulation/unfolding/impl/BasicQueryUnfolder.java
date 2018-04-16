package it.unibz.inf.ontop.answering.reformulation.unfolding.impl;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.answering.reformulation.unfolding.QueryUnfolder;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.QueryTransformerFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.IntensionalDataNode;
import it.unibz.inf.ontop.iq.optimizer.impl.AbstractIntensionalQueryMerger;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.vocabulary.RDF;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.VariableGenerator;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.simple.SimpleRDF;

import java.util.Optional;

public class BasicQueryUnfolder extends AbstractIntensionalQueryMerger implements QueryUnfolder {

    private final Mapping mapping;
    private final IntermediateQueryFactory iqFactory;
    private final SubstitutionFactory substitutionFactory;
    private final QueryTransformerFactory transformerFactory;
    private final TermFactory termFactory;

    @AssistedInject
    private BasicQueryUnfolder(@Assisted Mapping mapping, IntermediateQueryFactory iqFactory,
                               SubstitutionFactory substitutionFactory, QueryTransformerFactory transformerFactory,
                               TermFactory termFactory) {
        super(iqFactory);
        this.mapping = mapping;
        this.iqFactory = iqFactory;
        this.substitutionFactory = substitutionFactory;
        this.transformerFactory = transformerFactory;
        this.termFactory = termFactory;
    }

    @Override
    protected QueryMergingTransformer createTransformer(ImmutableSet<Variable> knownVariables) {
        return new BasicQueryUnfoldingTransformer(new VariableGenerator(knownVariables, termFactory));
    }

    protected class BasicQueryUnfoldingTransformer extends AbstractIntensionalQueryMerger.QueryMergingTransformer {

        protected BasicQueryUnfoldingTransformer(VariableGenerator variableGenerator) {
            super(variableGenerator, iqFactory, substitutionFactory, transformerFactory);
        }

        /**
         * TODO: clean it
         */
        @Override
        protected Optional<IQ> getDefinition(IntensionalDataNode dataNode) {
            ImmutableList<? extends VariableOrGroundTerm> projectedVariables = dataNode.getProjectionAtom().getArguments();
            VariableOrGroundTerm variableOrGroundTerm = projectedVariables.get(1);
            IRI predicateIRI;
            Optional<IQ> optionalMappingAssertion;
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
            return optionalMappingAssertion;
        }

        @Override
        protected IQTree handleIntensionalWithoutDefinition(IntensionalDataNode dataNode) {
            return iqFactory.createEmptyNode(dataNode.getVariables());
        }
    }
}
