package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
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
import it.unibz.inf.ontop.model.atom.*;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.vocabulary.Ontop;
import it.unibz.inf.ontop.spec.mapping.MappingAssertion;
import it.unibz.inf.ontop.spec.mapping.MappingWithProvenance;
import it.unibz.inf.ontop.spec.mapping.transformer.MappingCanonicalTransformer;
import it.unibz.inf.ontop.spec.mapping.utils.MappingTools;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;
import org.apache.commons.rdf.api.IRI;

import java.util.Map;
import java.util.Optional;

public class MappingCanonicalTransformerImpl implements MappingCanonicalTransformer {


    private final IntermediateQueryFactory iqFactory;
    private final ProvenanceMappingFactory provenanceMappingFactory;
    private final QueryTransformerFactory transformerFactory;
    private final SubstitutionFactory substitutionFactory;
    private final AtomFactory atomFactory;
    private final UnionBasedQueryMerger queryMerger;
    private final CoreUtilsFactory coreUtilsFactory;
    private final OntopMappingSettings settings;

    private enum Position {SUBJECT, PROPERTY, OBJECT}

    @Inject
    private MappingCanonicalTransformerImpl(IntermediateQueryFactory iqFactory,
                                            ProvenanceMappingFactory provenanceMappingFactory,
                                            QueryTransformerFactory transformerFactory,
                                            SubstitutionFactory substitutionFactory,
                                            AtomFactory atomFactory,
                                            CoreUtilsFactory coreUtilsFactory,
                                            UnionBasedQueryMerger queryMerger,
                                            OntopMappingSettings settings) {
        this.coreUtilsFactory = coreUtilsFactory;
        this.settings = settings;
        this.iqFactory = iqFactory;
        this.provenanceMappingFactory = provenanceMappingFactory;
        this.transformerFactory = transformerFactory;
        this.substitutionFactory = substitutionFactory;
        this.atomFactory = atomFactory;
        this.queryMerger = queryMerger;
    }

    @Override
    public MappingWithProvenance transform(MappingWithProvenance mapping) {

        // Isolate mapping assertions with canIRI as predicate
        Optional<IQ> canIRIDefinition = extractCanIRIDefinition(mapping);

        // Transform the remaining mapping assertions
        return canIRIDefinition.isPresent() ?
                transformMapping(mapping, new IntensionalQueryMerger(canIRIDefinition.get())) :
                mapping;
    }

    private Optional<IQ> extractCanIRIDefinition(MappingWithProvenance mapping) {
        return queryMerger.mergeDefinitions(
                mapping.getMappingAssertions().stream()
                        .filter(a -> a.getIndex().getIri().equals(Ontop.CANONICAL_IRI))
                        .map(a -> a.getQuery())
                        .collect(ImmutableCollectors.toList()));
    }

    private MappingWithProvenance transformMapping(MappingWithProvenance mapping, IntensionalQueryMerger intensionalQueryMerger) {
        return provenanceMappingFactory.create(
                mapping.getMappingAssertions().stream()
                        .filter(a -> !(a.getIndex().getIri().equals(Ontop.CANONICAL_IRI)))
                        .map(a -> new MappingAssertion(a.getIndex(),
                                transformAssertion(a.getQuery(), intensionalQueryMerger),
                                a.getProvenance()))
                        .collect(ImmutableCollectors.toList()));
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
        IQ assertionWithCanonizedSubject = canonizeWithJoin(assertion, intensionalQueryMerger, Position.SUBJECT);
        return canonizeWithJoin(assertionWithCanonizedSubject, intensionalQueryMerger, Position.OBJECT);
    }

    private IQ canonizeWithJoin(IQ assertion, IntensionalQueryMerger intensionalQueryMerger, Position pos) {

        Optional<Variable> replacedVar = getReplacedVar(assertion, pos);

        if (replacedVar.isPresent()) {
            Variable newVariable = createFreshVariable(assertion, intensionalQueryMerger, replacedVar.get());
            IntensionalDataNode idn = getIDN(replacedVar.get(), newVariable);
            RDFAtomPredicate pred = getRDFAtomPredicate(assertion.getProjectionAtom());

            DistinctVariableOnlyDataAtom projAtom = atomFactory.getDistinctVariableOnlyDataAtom(
                    pred,
                    replaceProjVars(
                            pred,
                            assertion.getProjectionAtom().getArguments(),
                            pos,
                            newVariable));

            IQ intensionalCanonizedQuery = iqFactory.createIQ(
                    projAtom,
                    getIntensionalCanonizedTree(assertion, projAtom, idn));

            IQ canonizedQuery = intensionalQueryMerger.optimize(intensionalCanonizedQuery)
                    .normalizeForOptimization();

            return canonizedQuery.getTree().isDeclaredAsEmpty()
                    // No matching canonical IRI template
                    ? assertion
                    : canonizedQuery;
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

    private Variable createFreshVariable(IQ assertion, IntensionalQueryMerger intensionalQueryMerger, Variable formerVariable) {
        VariableGenerator variableGenerator = coreUtilsFactory.createVariableGenerator(
                Sets.union(
                        assertion.getTree().getKnownVariables(),
                        intensionalQueryMerger.getKnownVariables()).immutableCopy());

        return variableGenerator.generateNewVariableFromVar(formerVariable);
    }

    private IntensionalDataNode getIDN(Variable formerVariable, Variable newVariable) {
        return iqFactory.createIntensionalDataNode(
                atomFactory.getIntensionalTripleAtom(
                        newVariable,
                        Ontop.CANONICAL_IRI,
                        formerVariable
                ));
    }

    private Optional<Variable> getReplacedVar(IQ assertion, Position pos) {
        switch (pos) {
            case SUBJECT:
                return Optional.of(getVarFromRDFAtom(assertion.getProjectionAtom(), pos));
            case OBJECT:
                return MappingTools.extractRDFPredicate(assertion).isClass()
                        ? Optional.empty()
                        : Optional.of(getVarFromRDFAtom(assertion.getProjectionAtom(), pos));
            default:
                throw new UnexpectedPositionException(pos);
        }
    }

    private ImmutableList<Variable> replaceProjVars(RDFAtomPredicate pred, ImmutableList<Variable> arguments, Position pos,
                                                    Variable replacementVar) {
        switch (pos) {
            case SUBJECT:
                return pred.updateSubject(arguments, replacementVar);
            case OBJECT:
                return pred.updateObject(arguments, replacementVar);
            case PROPERTY:
            default:
                throw new UnexpectedPositionException(pos);
        }
    }

    private Optional<IRI> getPropertyIRI(DataAtom atom) {
        AtomPredicate atomPredicate = atom.getPredicate();

        return Optional.of(atomPredicate)
                .filter(p -> p instanceof RDFAtomPredicate)
                .map(p -> (RDFAtomPredicate) p)
                .flatMap(p -> p.getPropertyIRI(atom.getArguments()));
    }

    private Variable getVarFromRDFAtom(DistinctVariableOnlyDataAtom atom, Position position) {
        switch (position) {
            case SUBJECT:
                return getRDFAtomPredicate(atom).getSubject(atom.getArguments());
            case OBJECT:
                return getRDFAtomPredicate(atom).getObject(atom.getArguments());
            case PROPERTY:
                return getRDFAtomPredicate(atom).getProperty(atom.getArguments());
            default:
                throw new UnexpectedPositionException(position);
        }
    }

    private RDFAtomPredicate getRDFAtomPredicate(DataAtom atom){
        return Optional.of(atom.getPredicate())
                .filter(p -> p instanceof RDFAtomPredicate)
                .map(p -> (RDFAtomPredicate) p)
                .orElseThrow(() -> new CanonicalTransformerException(RDFAtomPredicate.class.getName() + " expected"));
    }

    private class CanonicalTransformerException extends OntopInternalBugException {
        CanonicalTransformerException(String text) {
            super(text);
        }
    }

    private class UnexpectedPositionException extends CanonicalTransformerException {
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
                if (getPropertyIRI(dataNode.getProjectionAtom())
                        .filter(i -> i.equals(Ontop.CANONICAL_IRI))
                        .isPresent()) {
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
