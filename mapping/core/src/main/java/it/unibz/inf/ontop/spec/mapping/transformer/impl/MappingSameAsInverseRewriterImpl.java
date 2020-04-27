package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.*;
import com.google.inject.Inject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.OntopMappingSettings;
import it.unibz.inf.ontop.injection.QueryTransformerFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.transform.QueryRenamer;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.vocabulary.OWL;
import it.unibz.inf.ontop.spec.mapping.MappingAssertion;
import it.unibz.inf.ontop.spec.mapping.transformer.MappingSameAsInverseRewriter;
import it.unibz.inf.ontop.substitution.InjectiveVar2VarSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.stream.Stream;


public class MappingSameAsInverseRewriterImpl implements MappingSameAsInverseRewriter {

    private final AtomFactory atomFactory;
    private final IntermediateQueryFactory iqFactory;
    private final QueryTransformerFactory transformerFactory;
    private final SubstitutionFactory substitutionFactory;
    private final boolean enabled;

    @Inject
    private MappingSameAsInverseRewriterImpl(AtomFactory atomFactory, IntermediateQueryFactory iqFactory,
                                             QueryTransformerFactory transformerFactory,
                                             SubstitutionFactory substitutionFactory,
                                             OntopMappingSettings settings) {
        this.atomFactory = atomFactory;
        this.iqFactory = iqFactory;
        this.transformerFactory = transformerFactory;
        this.substitutionFactory = substitutionFactory;
        this.enabled = settings.isSameAsInMappingsEnabled();
    }

    @Override
    public ImmutableList<MappingAssertion> rewrite(ImmutableList<MappingAssertion> mapping) {
        if (!enabled)
            return mapping;

        return mapping.stream()
                .flatMap(this::transform)
                .collect(ImmutableCollectors.toList());
    }

    private Stream<MappingAssertion> transform(MappingAssertion assertion) {
        return (assertion.getIndex().isClass() || !assertion.getIndex().getIri().equals(OWL.SAME_AS))
            ? Stream.of(assertion)
            : Stream.of(assertion, getInverse(assertion));
    }

    private MappingAssertion getInverse(MappingAssertion assertion) {

        RDFAtomPredicate rdfAtomPredicate = assertion.getRDFAtomPredicate();
        ImmutableList<Variable> variables = assertion.getProjectionAtom().getArguments();
        Variable originalSubject = rdfAtomPredicate.getSubject(variables);
        Variable originalObject = rdfAtomPredicate.getObject(variables);

        VariableGenerator generator = assertion.getQuery().getVariableGenerator();
        Variable newSubject = generator.generateNewVariableFromVar(originalSubject);
        Variable newObject = generator.generateNewVariableFromVar(originalObject);

        DistinctVariableOnlyDataAtom newProjectionAtom = atomFactory.getDistinctVariableOnlyDataAtom(
                rdfAtomPredicate,
                rdfAtomPredicate.updateSPO(variables, newSubject, rdfAtomPredicate.getProperty(variables), newObject));

        // swap subjects and objects
        InjectiveVar2VarSubstitution renamingSubstitution = substitutionFactory.getInjectiveVar2VarSubstitution(
                ImmutableMap.of(
                        originalSubject, newObject,
                        originalObject, newSubject));

        QueryRenamer queryRenamer = transformerFactory.createRenamer(renamingSubstitution);

        return assertion.copyOf(iqFactory.createIQ(newProjectionAtom,
                queryRenamer.transform(assertion.getQuery()).getTree()));
    }
}
