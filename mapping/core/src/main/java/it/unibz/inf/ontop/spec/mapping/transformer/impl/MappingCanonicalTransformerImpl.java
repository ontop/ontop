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

    private enum Position {SUBJECT, PROPERTY, OBJECT}

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
        MappingAssertion assertionWithCanonizedSubject = canonizeWithJoin(assertion, intensionalQueryMerger, Position.SUBJECT);
        return canonizeWithJoin(assertionWithCanonizedSubject, intensionalQueryMerger, Position.OBJECT);
    }

    private MappingAssertion canonizeWithJoin(MappingAssertion assertion, IntensionalQueryMerger intensionalQueryMerger, Position pos) {

        Optional<Variable> replacedVar = getReplacedVar(assertion, pos);
        if (replacedVar.isPresent()) {
            IQ iq = assertion.getQuery();
            Variable newVariable = createFreshVariable(iq, intensionalQueryMerger, replacedVar.get());
            IntensionalDataNode idn = iqFactory.createIntensionalDataNode(
                    atomFactory.getIntensionalTripleAtom(newVariable, Ontop.CANONICAL_IRI, replacedVar.get()));

            DistinctVariableOnlyDataAtom projAtom = atomFactory.getDistinctVariableOnlyDataAtom(
                    assertion.getRDFAtomPredicate(),
                    replaceProjVars(assertion, pos, newVariable));

            IQ intensionalCanonizedQuery = iqFactory.createIQ(
                    projAtom,
                    getIntensionalCanonizedTree(iq, projAtom, idn));

            IQ canonizedQuery = intensionalQueryMerger.optimize(intensionalCanonizedQuery)
                    .normalizeForOptimization();

            return canonizedQuery.getTree().isDeclaredAsEmpty()
                    // No matching canonical IRI template
                    ? assertion
                    : assertion.copyOf(canonizedQuery);
        }
        return assertion;
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

    private static Optional<Variable> getReplacedVar(MappingAssertion assertion, Position pos) {
        switch (pos) {
            case SUBJECT:
                return Optional.of(assertion.getSubject());
            case OBJECT:
                return assertion.getIndex().isClass()
                        ? Optional.empty()
                        : Optional.of(assertion.getObject());
            default:
                throw new UnexpectedPositionException(pos);
        }
    }

    private static ImmutableList<Variable> replaceProjVars(MappingAssertion assertion, Position pos, Variable replacementVar) {
        switch (pos) {
            case SUBJECT:
                return assertion.updateSubject(replacementVar);
            case OBJECT:
                return assertion.updateObject(replacementVar);
            default:
                throw new UnexpectedPositionException(pos);
        }
    }


    private static class CanonicalTransformerException extends OntopInternalBugException {
        CanonicalTransformerException(String text) {
            super(text);
        }
    }

    private static class UnexpectedPositionException extends CanonicalTransformerException {
        UnexpectedPositionException(Position pos) {
            super("Unexpected position: " + pos);
        }
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
                super(variableGenerator, MappingCanonicalTransformerImpl.this.iqFactory, substitutionFactory, transformerFactory);
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
