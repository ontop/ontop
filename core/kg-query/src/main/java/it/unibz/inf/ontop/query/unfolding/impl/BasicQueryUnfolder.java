package it.unibz.inf.ontop.query.unfolding.impl;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.exception.OntopUnsupportedKGQueryRuntimeException;
import it.unibz.inf.ontop.iq.optimizer.impl.AbstractQueryMergingTransformer;
import it.unibz.inf.ontop.model.atom.*;
import it.unibz.inf.ontop.query.unfolding.QueryUnfolder;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.QueryTransformerFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.IntensionalDataNode;
import it.unibz.inf.ontop.iq.optimizer.impl.AbstractIntensionalQueryMerger;
import it.unibz.inf.ontop.iq.tools.UnionBasedQueryMerger;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.vocabulary.RDF;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;

/**
 * See {@link QueryUnfolder.Factory} for creating a new instance.
 */
public class BasicQueryUnfolder extends AbstractIntensionalQueryMerger implements QueryUnfolder {

    private final Mapping mapping;
    private final SubstitutionFactory substitutionFactory;
    private final QueryTransformerFactory transformerFactory;
    private final AtomFactory atomFactory;
    private final UnionBasedQueryMerger queryMerger;

    /**
     * See {@link QueryUnfolder.Factory#create(Mapping)}
     */
    @AssistedInject
    private BasicQueryUnfolder(@Assisted Mapping mapping, IntermediateQueryFactory iqFactory,
                               SubstitutionFactory substitutionFactory, QueryTransformerFactory transformerFactory,
                               UnionBasedQueryMerger queryMerger,
                               AtomFactory atomFactory) {
        super(iqFactory);
        this.mapping = mapping;
        this.substitutionFactory = substitutionFactory;
        this.transformerFactory = transformerFactory;
        this.queryMerger = queryMerger;
        this.atomFactory = atomFactory;
    }

    @Override
    protected IQTree transformTree(IQTree tree, VariableGenerator variableGenerator) {
        return tree.acceptVisitor(new BasicQueryUnfoldingTransformer(variableGenerator));
    }


    private class BasicQueryUnfoldingTransformer extends AbstractQueryMergingTransformer {

        BasicQueryUnfoldingTransformer(VariableGenerator variableGenerator) {
            super(variableGenerator, BasicQueryUnfolder.this.iqFactory, substitutionFactory, atomFactory, transformerFactory);
        }

        @Override
        protected Optional<IQ> getDefinition(IntensionalDataNode dataNode) {
            DataAtom<AtomPredicate> atom = dataNode.getProjectionAtom();
            AtomPredicate atomPredicate = atom.getPredicate();

            if (atomPredicate instanceof RDFAtomPredicate) {
                return Optional.of((RDFAtomPredicate) atomPredicate)
                        .flatMap(p -> getDefinition(p, atom.getArguments()));
            }
            if (atomPredicate instanceof NodeInGraphPredicate) {
                // TODO: in the case of a constant node but a variable graph, shall we list all the possible graphs?
                throw new OntopUnsupportedKGQueryRuntimeException(
                        "Unfolding NodeInGraphPredicate is not supported. " +
                                "Please consider joining the variables of ZeroOrOne or ZeroOrMore property paths " +
                                "with triple or quad patterns over the same graphs (default or named).");
            }
            return Optional.empty();
        }

        private Optional<IQ> getDefinition(RDFAtomPredicate predicate,
                                           ImmutableList<? extends VariableOrGroundTerm> arguments) {
            return predicate.getPropertyIRI(arguments)
                    .map(i -> i.equals(RDF.TYPE)
                            ? getRDFClassDefinition(predicate, arguments)
                            : mapping.getRDFPropertyDefinition(predicate, i))
                    .orElseGet(() -> getStarDefinition(predicate));
        }

        private Optional<IQ> getRDFClassDefinition(RDFAtomPredicate predicate,
                                                   ImmutableList<? extends VariableOrGroundTerm> arguments) {
            return predicate.getClassIRI(arguments)
                    .map(i -> mapping.getRDFClassDefinition(predicate, i))
                    .orElseGet(() -> getStarClassDefinition(predicate));
        }

        private Optional<IQ> getStarClassDefinition(RDFAtomPredicate predicate) {
            return queryMerger.mergeDefinitions(mapping.getRDFClasses(predicate).stream()
                    .flatMap(i -> mapping.getRDFClassDefinition(predicate, i).stream())
                    .collect(ImmutableCollectors.toList()));
        }

        private Optional<IQ> getStarDefinition(RDFAtomPredicate predicate) {
            return mapping.getMergedDefinitions(predicate);
        }

        @Override
        protected IQTree handleIntensionalWithoutDefinition(IntensionalDataNode dataNode) {
            return iqFactory.createEmptyNode(dataNode.getVariables());
        }
    }
}
