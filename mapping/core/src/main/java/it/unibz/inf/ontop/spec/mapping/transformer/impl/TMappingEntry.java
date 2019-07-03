package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.tools.UnionBasedQueryMerger;
import it.unibz.inf.ontop.iq.transform.NoNullValueEnforcer;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;
import java.util.stream.Collectors;

public class TMappingEntry {
    private ImmutableList<TMappingRule> rules;

    private final NoNullValueEnforcer noNullValueEnforcer;
    private final UnionBasedQueryMerger queryMerger;


    public TMappingEntry(ImmutableList<TMappingRule> rules, NoNullValueEnforcer noNullValueEnforcer, UnionBasedQueryMerger queryMerger) {
        this.rules = rules;
        this.noNullValueEnforcer = noNullValueEnforcer;
        this.queryMerger = queryMerger;
    }

    public TMappingEntry createCopy(java.util.function.Function<TMappingRule, TMappingRule> headReplacer) {
        return new TMappingEntry(
                rules.stream().map(headReplacer).collect(ImmutableCollectors.toList()),
                noNullValueEnforcer,
                queryMerger);
    }

    public IQ asIQ() {
        // In case some legacy implementations do not preserve IS_NOT_NULL conditions
        return noNullValueEnforcer.transform(
                    queryMerger.mergeDefinitions(rules.stream()
                        .map(r -> r.asIQ())
                        .collect(ImmutableCollectors.toList())).get())
               .liftBinding();
    }

    public boolean isEmpty() {
        return rules.isEmpty();
    }
}
