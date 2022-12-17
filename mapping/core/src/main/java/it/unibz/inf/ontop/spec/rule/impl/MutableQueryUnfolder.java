package it.unibz.inf.ontop.spec.rule.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.QueryTransformerFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.IntensionalDataNode;
import it.unibz.inf.ontop.iq.optimizer.impl.AbstractIntensionalQueryMerger;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.model.vocabulary.RDF;
import it.unibz.inf.ontop.spec.mapping.MappingAssertion;
import it.unibz.inf.ontop.spec.mapping.MappingAssertionIndex;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.VariableGenerator;
import org.apache.commons.rdf.api.IRI;

import java.util.Map;
import java.util.Optional;

/**
 * Operates over a mutable mapping. Does not modify itself.
 */
public class MutableQueryUnfolder extends AbstractIntensionalQueryMerger {

    private final Map<MappingAssertionIndex, MappingAssertion> mutableMapping;
    private final SubstitutionFactory substitutionFactory;
    private final QueryTransformerFactory transformerFactory;
    private final CoreUtilsFactory coreUtilsFactory;
    private final AtomFactory atomFactory;

    public MutableQueryUnfolder(Map<MappingAssertionIndex, MappingAssertion> mutableMapping,
                                IntermediateQueryFactory iqFactory,
                                SubstitutionFactory substitutionFactory, QueryTransformerFactory transformerFactory,
                                CoreUtilsFactory coreUtilsFactory, AtomFactory atomFactory) {
        super(iqFactory);
        this.mutableMapping = mutableMapping;
        this.substitutionFactory = substitutionFactory;
        this.transformerFactory = transformerFactory;
        this.coreUtilsFactory = coreUtilsFactory;
        this.atomFactory = atomFactory;
    }

    @Override
    protected QueryMergingTransformer createTransformer(ImmutableSet<Variable> knownVariables) {
        return new MutableQueryUnfoldingTransformer(coreUtilsFactory.createVariableGenerator(knownVariables), iqFactory);
    }

    protected class MutableQueryUnfoldingTransformer extends AbstractIntensionalQueryMerger.QueryMergingTransformer {

        protected MutableQueryUnfoldingTransformer(VariableGenerator variableGenerator, IntermediateQueryFactory iqFactory) {
            super(variableGenerator, iqFactory, substitutionFactory, atomFactory, transformerFactory);
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
                            : getRDFPropertyDefinition(predicate, i))
                    .orElseGet(() -> getStarDefinition(predicate));
        }

        private Optional<IQ> getRDFPropertyDefinition(RDFAtomPredicate predicate, IRI iri) {
            return Optional.ofNullable(mutableMapping.get(MappingAssertionIndex.ofProperty(predicate, iri)))
                    .map(MappingAssertion::getQuery);
        }

        private Optional<IQ> getRDFClassDefinition(RDFAtomPredicate predicate,
                                                   ImmutableList<? extends VariableOrGroundTerm> arguments) {
            return predicate.getClassIRI(arguments)
                    .map(i -> getRDFClassDefinition(predicate, i))
                    .orElseGet(() -> getStarClassDefinition(predicate));
        }

        private Optional<IQ> getRDFClassDefinition(RDFAtomPredicate predicate, IRI iri) {
            return Optional.ofNullable(mutableMapping.get(MappingAssertionIndex.ofClass(predicate, iri)))
                    .map(MappingAssertion::getQuery);
        }

        private Optional<IQ> getStarClassDefinition(RDFAtomPredicate predicate) {
            // Should have been detected before
            throw new MinorOntopInternalBugException("Triple/quad patterns where the class or the non-rdf-type property are not specified " +
                    "are not supported for SPARQL INSERT rules");
        }

        private Optional<IQ> getStarDefinition(RDFAtomPredicate predicate) {
            // Should have been detected before
            throw new MinorOntopInternalBugException("Triple/quad patterns where the class or the non-rdf-type property are not specified " +
                    "are not supported for SPARQL INSERT rules");
        }

        @Override
        protected IQTree handleIntensionalWithoutDefinition(IntensionalDataNode dataNode) {
            return iqFactory.createEmptyNode(dataNode.getVariables());
        }
    }


}
