package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.injection.*;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.IntensionalDataNode;
import it.unibz.inf.ontop.iq.optimizer.impl.AbstractIntensionalQueryMerger;
import it.unibz.inf.ontop.iq.tools.UnionBasedQueryMerger;
import it.unibz.inf.ontop.model.atom.*;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.vocabulary.Ontop;
import it.unibz.inf.ontop.spec.mapping.MappingAssertion;
import it.unibz.inf.ontop.spec.mapping.transformer.MappingCanonicalTransformer;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;

public class MappingCanonicalTransformerImpl implements MappingCanonicalTransformer {

    private final IntermediateQueryFactory iqFactory;
    private final QueryTransformerFactory transformerFactory;
    private final SubstitutionFactory substitutionFactory;
    private final AtomFactory atomFactory;
    private final UnionBasedQueryMerger queryMerger;
    private final CoreUtilsFactory coreUtilsFactory;
    private final OntopMappingSettings settings;

    @Inject
    private MappingCanonicalTransformerImpl(CoreSingletons coreSingletons,
                                            QueryTransformerFactory transformerFactory,
                                            UnionBasedQueryMerger queryMerger,
                                            OntopMappingSettings settings) {
        this.coreUtilsFactory = coreSingletons.getCoreUtilsFactory();
        this.settings = settings;
        this.iqFactory = coreSingletons.getIQFactory();
        this.transformerFactory = transformerFactory;
        this.substitutionFactory = coreSingletons.getSubstitutionFactory();
        this.atomFactory = coreSingletons.getAtomFactory();
        this.queryMerger = queryMerger;
    }

    @Override
    public ImmutableList<MappingAssertion> transform(ImmutableList<MappingAssertion> mapping) {

        // Isolate mapping assertions with canIRI as predicate
        Optional<IQ> canIRIDefinition = extractCanIRIDefinition(mapping);

        // Transform the remaining mapping assertions
        return canIRIDefinition.isPresent() ?
                transformMapping(mapping, new IntensionalQueryMerger(canIRIDefinition.get())) :
                mapping;
    }

    private Optional<IQ> extractCanIRIDefinition(ImmutableList<MappingAssertion> mapping) {
        return queryMerger.mergeDefinitions(
                mapping.stream()
                        .filter(a -> a.getIndex().getIri().equals(Ontop.CANONICAL_IRI))
                        .map(MappingAssertion::getQuery)
                        .collect(ImmutableCollectors.toList()));
    }

    private ImmutableList<MappingAssertion> transformMapping(ImmutableList<MappingAssertion> mapping, IntensionalQueryMerger intensionalQueryMerger) {
        return mapping.stream()
                        .filter(a -> !(a.getIndex().getIri().equals(Ontop.CANONICAL_IRI)))
                        .map(a -> transformAssertion(a, intensionalQueryMerger))
                        .collect(ImmutableCollectors.toList());
    }

    private MappingAssertion transformAssertion(MappingAssertion assertion, IntensionalQueryMerger intensionalQueryMerger) {
        return settings.isCanIRIComplete() ?
                transformAssertionWithJoin(assertion, intensionalQueryMerger) :
                transformAssertionWithLeftJoin(assertion, intensionalQueryMerger);
    }

    private MappingAssertion transformAssertionWithLeftJoin(MappingAssertion assertion, IntensionalQueryMerger intensionalQueryMerger) {
        throw new RuntimeException("TODO: implement");
    }

    private MappingAssertion transformAssertionWithJoin(MappingAssertion assertion, IntensionalQueryMerger intensionalQueryMerger) {
        RDFAtomPredicate rdfAtomPredicate = assertion.getRDFAtomPredicate();
        if (assertion.getIndex().isClass()) {
            return canonizeWithJoin(assertion, intensionalQueryMerger, rdfAtomPredicate::getSubject, rdfAtomPredicate::updateSubject);
        }
        else {
            MappingAssertion assertionWithCanonizedSubject = canonizeWithJoin(assertion, intensionalQueryMerger, rdfAtomPredicate::getSubject, rdfAtomPredicate::updateSubject);
            return canonizeWithJoin(assertionWithCanonizedSubject, intensionalQueryMerger, rdfAtomPredicate::getObject, rdfAtomPredicate::updateObject);
        }
    }

    private MappingAssertion canonizeWithJoin(MappingAssertion assertion, IntensionalQueryMerger intensionalQueryMerger, RDFAtomPredicate.ComponentGetter componentGetter, RDFAtomPredicate.ComponentUpdater componentUpdater) {

        ImmutableList<Variable> variables = assertion.getProjectionAtom().getArguments();
        Variable replacedVar = componentGetter.get(variables);

        IQ iq = assertion.getQuery();
        Variable newVariable = createFreshVariable(iq, intensionalQueryMerger, replacedVar);
        IntensionalDataNode idn = iqFactory.createIntensionalDataNode(
                atomFactory.getIntensionalTripleAtom(newVariable, Ontop.CANONICAL_IRI, replacedVar));

        DistinctVariableOnlyDataAtom projectionAtom = atomFactory.getDistinctVariableOnlyDataAtom(
                assertion.getRDFAtomPredicate(),
                componentUpdater.update(variables, newVariable));

        IQ intensionalCanonizedQuery = iqFactory.createIQ(
                projectionAtom,
                getIntensionalCanonizedTree(iq, projectionAtom, idn));

        IQ canonizedQuery = intensionalQueryMerger.optimize(intensionalCanonizedQuery)
                .normalizeForOptimization();

        return canonizedQuery.getTree().isDeclaredAsEmpty()
                // No matching canonical IRI template
                ? assertion
                : assertion.copyOf(canonizedQuery);
    }

    private IQTree getIntensionalCanonizedTree(IQ assertion, DistinctVariableOnlyDataAtom projAtom, IntensionalDataNode intensionalDataNode) {
        return iqFactory.createUnaryIQTree(
                iqFactory.createConstructionNode(projAtom.getVariables()),
                iqFactory.createNaryIQTree(
                        iqFactory.createInnerJoinNode(),
                        ImmutableList.of(
                                assertion.getTree(),
                                intensionalDataNode
                        )));
    }

    private Variable createFreshVariable(IQ iq, IntensionalQueryMerger intensionalQueryMerger, Variable formerVariable) {
        VariableGenerator variableGenerator = coreUtilsFactory.createVariableGenerator(
                Sets.union(
                        iq.getTree().getKnownVariables(),
                        intensionalQueryMerger.getKnownVariables()).immutableCopy());

        return variableGenerator.generateNewVariableFromVar(formerVariable);
    }

    private class IntensionalQueryMerger extends AbstractIntensionalQueryMerger {

        private final IQ definition;

        IntensionalQueryMerger(IQ definition) {
            super(MappingCanonicalTransformerImpl.this.iqFactory);
            this.definition = definition;
        }

        @Override
        protected AbstractIntensionalQueryMerger.QueryMergingTransformer createTransformer(ImmutableSet<Variable> knownVariables) {
            VariableGenerator variableGenerator = coreUtilsFactory.createVariableGenerator(knownVariables);
            return new QueryMergingTransformer(variableGenerator);
        }

        public ImmutableSet<Variable> getKnownVariables() {
            return definition.getTree().getKnownVariables();
        }

        private class QueryMergingTransformer extends AbstractIntensionalQueryMerger.QueryMergingTransformer {

            QueryMergingTransformer(VariableGenerator variableGenerator) {
                super(variableGenerator, MappingCanonicalTransformerImpl.this.iqFactory, substitutionFactory, atomFactory, transformerFactory);
            }

            @Override
            protected Optional<IQ> getDefinition(IntensionalDataNode dataNode) {
                DataAtom<AtomPredicate> atom =  dataNode.getProjectionAtom();
                if (Optional.of(atom.getPredicate())
                        .filter(p -> p instanceof RDFAtomPredicate)
                        .map(p -> (RDFAtomPredicate) p)
                        .flatMap(p -> p.getPropertyIRI(atom.getArguments()))
                        .filter(i -> i.equals(Ontop.CANONICAL_IRI))
                        .isPresent()) {
                    return Optional.of(definition);
                }
                throw new UnexpectedPredicateException(atom.getPredicate());
            }

            @Override
            protected IQTree handleIntensionalWithoutDefinition(IntensionalDataNode dataNode) {
                return dataNode;
            }
        }

        private class UnexpectedPredicateException extends OntopInternalBugException {
            UnexpectedPredicateException(AtomPredicate predicate) {
                super("canonical IRI predicate expected instead of :" + predicate);
            }
        }
    }
}
