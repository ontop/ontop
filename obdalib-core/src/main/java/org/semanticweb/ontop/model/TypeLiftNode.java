package org.semanticweb.ontop.model;

import fj.P3;
import fj.data.List;
import fj.data.Option;

/**
 * TODO: describe
 *
 * TODO: find a better name.
 *
 * Immutable.
 */
public class TypeLiftNode {

    private final Predicate predicate;
    private final List<CQIE> definitionRules;
    private final Option<PredicateLevelProposal> proposal;

    TypeLiftNode(Predicate predicate, List<CQIE> definitionRules, Option<PredicateLevelProposal> proposal) {
        this.predicate = predicate;
        this.definitionRules = definitionRules;
        this.proposal = proposal;
    }

    public Predicate getPredicate() {
        return predicate;
    }

    public List<CQIE> getDefinitionRules() {
        return definitionRules;
    }

    public Option<PredicateLevelProposal> getOptionalProposal() {
        return proposal;
    }

    public TypeLiftNode newRulesNoProposal(List<CQIE> rules) {
        return new TypeLiftNode(predicate, rules, Option.<PredicateLevelProposal>none());
    }

    public TypeLiftNode newRulesAndProposal(List<CQIE> rules, PredicateLevelProposal proposal) {
        return new TypeLiftNode(predicate, rules, Option.some(proposal));
    }
}
