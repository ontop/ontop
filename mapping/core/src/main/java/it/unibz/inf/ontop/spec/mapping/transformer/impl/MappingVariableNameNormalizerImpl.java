package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.*;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.injection.QueryTransformerFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.transform.QueryRenamer;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.spec.mapping.MappingAssertion;
import it.unibz.inf.ontop.spec.mapping.transformer.MappingVariableNameNormalizer;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.concurrent.atomic.AtomicInteger;


@Singleton
public class MappingVariableNameNormalizerImpl implements MappingVariableNameNormalizer {

    private final QueryTransformerFactory transformerFactory;
    private final SubstitutionFactory substitutionFactory;
    private final TermFactory termFactory;

    @Inject
    private MappingVariableNameNormalizerImpl(QueryTransformerFactory transformerFactory,
                                              SubstitutionFactory substitutionFactory,
                                              TermFactory termFactory) {
        this.transformerFactory = transformerFactory;
        this.substitutionFactory = substitutionFactory;
        this.termFactory = termFactory;
    }

    @Override
    public ImmutableList<MappingAssertion> normalize(ImmutableList<MappingAssertion> mapping) {
        AtomicInteger i = new AtomicInteger(0);
        return mapping.stream()
                .map(a -> a.copyOf(appendSuffixToVariableNames(a.getQuery(), i.incrementAndGet())))
                .collect(ImmutableCollectors.toList());
    }

    private IQ appendSuffixToVariableNames(IQ query, int suffix) {
        InjectiveSubstitution<Variable> substitution = query.getTree().getKnownVariables().stream()
                .collect(substitutionFactory.toSubstitution(v -> termFactory.getVariable(v.getName() + "m" + suffix)))
                .injective();

        return transformerFactory.createRenamer(substitution).transform(query);
    }
}
