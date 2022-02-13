package it.unibz.inf.ontop.answering.reformulation.unfolding.impl;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.answering.reformulation.unfolding.QueryUnfolder;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.QueryTransformerFactory;
import it.unibz.inf.ontop.injection.TranslationFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.IntensionalDataNode;
import it.unibz.inf.ontop.iq.optimizer.impl.AbstractIntensionalQueryMerger;
import it.unibz.inf.ontop.iq.tools.UnionBasedQueryMerger;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.vocabulary.RDF;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * See {@link TranslationFactory} for creating a new instance.
 */
public class BasicQueryUnfolder extends AbstractIntensionalQueryMerger implements QueryUnfolder {

    private final Mapping mapping;
    private final SubstitutionFactory substitutionFactory;
    private final QueryTransformerFactory transformerFactory;
    private final AtomFactory atomFactory;
    private final UnionBasedQueryMerger queryMerger;
    private final CoreUtilsFactory coreUtilsFactory;

    /**
     * See {@link TranslationFactory#create(Mapping)}
     */
    @AssistedInject
    private BasicQueryUnfolder(@Assisted Mapping mapping, IntermediateQueryFactory iqFactory,
                               SubstitutionFactory substitutionFactory, QueryTransformerFactory transformerFactory,
                               UnionBasedQueryMerger queryMerger, CoreUtilsFactory coreUtilsFactory,
                               AtomFactory atomFactory) {
        super(iqFactory);
        this.mapping = mapping;
        this.substitutionFactory = substitutionFactory;
        this.transformerFactory = transformerFactory;
        this.queryMerger = queryMerger;
        this.coreUtilsFactory = coreUtilsFactory;
        this.atomFactory = atomFactory;
    }

    @Override
    protected QueryMergingTransformer createTransformer(ImmutableSet<Variable> knownVariables) {
        return new BasicQueryUnfoldingTransformer(coreUtilsFactory.createVariableGenerator(knownVariables));
    }

    protected class BasicQueryUnfoldingTransformer extends AbstractIntensionalQueryMerger.QueryMergingTransformer {

        protected BasicQueryUnfoldingTransformer(VariableGenerator variableGenerator) {
            super(variableGenerator, BasicQueryUnfolder.this.iqFactory, substitutionFactory, atomFactory, transformerFactory);
        }

        @Override
        protected Optional<IQ> getDefinition(IntensionalDataNode dataNode) {
            DataAtom<AtomPredicate> atom = dataNode.getProjectionAtom();
            return Optional.of(atom)
                    .map(DataAtom::getPredicate)
                    .filter(p -> p instanceof RDFAtomPredicate)
                    .map(p -> (RDFAtomPredicate) p)
                    .flatMap(p -> getDefinition(p, atom.getArguments()));
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
                    .flatMap(i -> mapping.getRDFClassDefinition(predicate, i)
                            .map(Stream::of)
                            .orElseGet(Stream::empty))
                    .collect(ImmutableCollectors.toList()));
        }

        private Optional<IQ> getStarDefinition(RDFAtomPredicate predicate) {
            return queryMerger.mergeDefinitions(mapping.getQueries(predicate));
        }

        @Override
        protected IQTree handleIntensionalWithoutDefinition(IntensionalDataNode dataNode) {
            return iqFactory.createEmptyNode(dataNode.getVariables());
        }
    }
}
