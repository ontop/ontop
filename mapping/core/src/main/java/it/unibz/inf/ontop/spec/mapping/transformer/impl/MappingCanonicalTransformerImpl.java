package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.OntopMappingSettings;
import it.unibz.inf.ontop.injection.ProvenanceMappingFactory;
import it.unibz.inf.ontop.injection.QueryTransformerFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.IntensionalDataNode;
import it.unibz.inf.ontop.iq.optimizer.impl.AbstractIntensionalQueryMerger;
import it.unibz.inf.ontop.iq.tools.UnionBasedQueryMerger;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.RDFConstant;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.vocabulary.Ontop;
import it.unibz.inf.ontop.spec.mapping.MappingWithProvenance;
import it.unibz.inf.ontop.spec.mapping.transformer.MappingCanonicalTransformer;
import it.unibz.inf.ontop.spec.mapping.utils.MappingTools;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class MappingCanonicalTransformerImpl implements MappingCanonicalTransformer {


    private final IntermediateQueryFactory iqFactory;
    private final ProvenanceMappingFactory provenanceMappingFactory;
    private final QueryTransformerFactory transformerFactory;
    private final TermFactory termFactory;
    private final SubstitutionFactory substitutionFactory;
    private final AtomFactory atomFactory;
    private final UnionBasedQueryMerger queryMerger;
    private final OntopMappingSettings settings;

    @Inject
    private MappingCanonicalTransformerImpl(IntermediateQueryFactory iqFactory,
                                            ProvenanceMappingFactory provenanceMappingFactory,
                                            QueryTransformerFactory transformerFactory,
                                            TermFactory termFactory,
                                            SubstitutionFactory substitutionFactory,
                                            AtomFactory atomFactory,
                                            UnionBasedQueryMerger queryMerger,
                                            OntopMappingSettings settings) {
        this.settings = settings;
        this.iqFactory = iqFactory;
        this.provenanceMappingFactory = provenanceMappingFactory;
        this.transformerFactory = transformerFactory;


        this.termFactory = termFactory;
        this.substitutionFactory = substitutionFactory;
        this.atomFactory = atomFactory;
        this.queryMerger = queryMerger;
    }

    @Override
    public MappingWithProvenance rewrite(MappingWithProvenance mapping) {

        // Isolate mapping assertions with canIRI as predicate
        Optional<IQ> canIRIMapping = extractCanIRIMapping(mapping);

        // Transform the remaining mapping assertions
        return canIRIMapping.isPresent() ?
                transformMapping(mapping, new IntensionalQueryMerger(canIRIMapping.get())) :
                mapping;
    }

    private Optional<IQ> extractCanIRIMapping(MappingWithProvenance mapping) {
        return queryMerger.mergeDefinitions(
                mapping.getProvenanceMap().keySet().stream()
                        .filter(q -> !(MappingTools.extractRDFPredicate(q).getIri().equals(Ontop.CANONICAL_IRI)))
                        .collect(ImmutableCollectors.toList()));
    }

    private MappingWithProvenance transformMapping(MappingWithProvenance mapping, IntensionalQueryMerger intensionalQueryMerger) {
        return provenanceMappingFactory.create(
                mapping.getProvenanceMap().entrySet().stream()
                        .filter(e -> !(MappingTools.extractRDFPredicate(e.getKey()).getIri().equals(Ontop.CANONICAL_IRI)))
                        .collect(ImmutableCollectors.toMap(
                                e -> transformAssertion(
                                        e.getKey(),
                                        intensionalQueryMerger
                                ),
                                Map.Entry::getValue
                        )),
                mapping.getMetadata()
        );
    }

    private IQ transformAssertion(IQ assertion, IntensionalQueryMerger intensionalQueryMerger) {
        return settings.isCanIRIComplete() ?
                transformAssertionWithJoin(assertion, intensionalQueryMerger) :
                transformAssertionWithLeftJoin(assertion, intensionalQueryMerger);
    }

    private IQ transformAssertionWithLeftJoin(IQ assertion, IntensionalQueryMerger intensionalQueryMerger) {
        throw new RuntimeException("TODO: implement");
    }

    private IQ transformAssertionWithJoin(IQ assertion, IntensionalQueryMerger intensionalQueryMerger) {
        IQ assertionWithCanonizedSubject = canonizeWithJoin(assertion, intensionalQueryMerger, 0);
        return canonizeWithJoin(assertionWithCanonizedSubject, intensionalQueryMerger, 2);
    }

    private IQ canonizeWithJoin(IQ assertion, IntensionalQueryMerger intensionalQueryMerger, int iriPosition) {

        Optional<Variable> replacedVar = getReplacedVar(assertion, iriPosition);

        if (replacedVar.isPresent()) {

            IntensionalDataNode intensionalDataNode = getIDN(assertion, iriPosition);

            DistinctVariableOnlyDataAtom projAtom =
                    atomFactory.getDistinctVariableOnlyDataAtom(
                            assertion.getProjectionAtom().getPredicate(),
                            replaceProjVars(
                                    assertion.getProjectionAtom().getArguments(),
                                    iriPosition,
                                    (Variable) intensionalDataNode.getProjectionAtom().getArguments().get(2)
                            ));

            IQ join = intensionalQueryMerger.optimize(iqFactory.createIQ(
                    projAtom,
                    getIntensionalQueryTree(
                            assertion,
                            projAtom,
                            intensionalDataNode
                    ))).liftBinding();

            return join.getTree().isDeclaredAsEmpty() ?
                    assertion :
                    join;
        }
        return assertion;
    }

    private IQTree getIntensionalQueryTree(IQ assertion, DistinctVariableOnlyDataAtom projAtom, IntensionalDataNode intensionalDataNode) {
        return iqFactory.createUnaryIQTree(
                iqFactory.createConstructionNode(ImmutableSet.copyOf(projAtom.getArguments())),
                iqFactory.createNaryIQTree(
                        iqFactory.createInnerJoinNode(),
                        ImmutableList.of(
                                assertion.getTree(),
                                intensionalDataNode
                        )));
    }

    private IntensionalDataNode getIDN(IQ assertion, int iriPosition) {
        return iqFactory.createIntensionalDataNode(
                atomFactory.getIntensionalTripleAtom(
                        assertion.getProjectionAtom().getArguments().get(iriPosition),
                        Ontop.CANONICAL_IRI,
                        assertion.getVariableGenerator().generateNewVariable()
                ));
    }

    private Optional<Variable> getReplacedVar(IQ assertion, int iriPosition) {
        if (iriPosition == 0) {
            return Optional.of(assertion.getProjectionAtom().getTerm(iriPosition));
        }
        if (iriPosition == 2) {
            if (MappingTools.extractRDFPredicate(assertion).isClass()) {
                return Optional.empty();
            }
            return Optional.of(assertion.getProjectionAtom().getTerm(iriPosition));
        }
        return Optional.empty();
    }

    private ImmutableList<Variable> replaceProjVars(ImmutableList<Variable> arguments, int iriPosition, Variable replacementVar) {
        AtomicInteger i = new AtomicInteger(0);
        return arguments.stream()
                .map(v -> (i.getAndIncrement() == iriPosition) ?
                        replacementVar :
                        v
                )
                .collect(ImmutableCollectors.toList());
    }

    private class IntensionalQueryMerger extends AbstractIntensionalQueryMerger {

        private final IQ definition;

        IntensionalQueryMerger(IQ definition) {
            super(iqFactory);
            this.definition = definition;
        }

        @Override
        protected AbstractIntensionalQueryMerger.QueryMergingTransformer createTransformer(ImmutableSet<Variable> knownVariables) {
            VariableGenerator variableGenerator = new VariableGenerator(knownVariables, termFactory);
            return new QueryMergingTransformer(variableGenerator);
        }

        private class QueryMergingTransformer extends AbstractIntensionalQueryMerger.QueryMergingTransformer {

            QueryMergingTransformer(VariableGenerator variableGenerator) {
                super(variableGenerator, iqFactory, substitutionFactory, transformerFactory);
            }

            @Override
            protected Optional<IQ> getDefinition(IntensionalDataNode dataNode) {

                ImmutableTerm t = dataNode.getProjectionAtom().getArguments().get(1);
                if (t instanceof RDFConstant && ((RDFConstant) t).getValue().equals(Ontop.CANONICAL_IRI)) {
                    return Optional.of(definition);
                }
                throw new UnexpectedPredicateException(dataNode.getProjectionAtom().getPredicate());
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
