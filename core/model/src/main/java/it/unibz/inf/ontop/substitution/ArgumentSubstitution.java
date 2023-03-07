package it.unibz.inf.ontop.substitution;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.IntStream;

public class ArgumentSubstitution<T extends ImmutableTerm> {

    private final ImmutableMap<Integer, Variable> map;
    private final Function<Variable, Optional<T>> optionalProvider;

    public ArgumentSubstitution(ImmutableMap<Integer, Variable> map, Function<Variable, Optional<T>> optionalProvider) {
        this.map = map;
        this.optionalProvider = optionalProvider;
    }

    public ImmutableList<T> replaceTerms(ImmutableList<? extends T> terms) {
        return IntStream.range(0, terms.size())
                .mapToObj(i -> optionalProvider.apply(map.get(i)).orElseGet(() -> terms.get(i)))
                .collect(ImmutableCollectors.toList());
    }

    public ImmutableMap<Integer, ? extends T> replaceTerms(ImmutableMap<Integer, ? extends T> terms) {
        return terms.entrySet().stream()
                .collect(ImmutableCollectors.toMap(Map.Entry::getKey, e -> optionalProvider.apply(map.get(e.getKey())).orElseGet(e::getValue)));
    }

    public Substitution<T> getSubstitution(SubstitutionFactory substitutionFactory, ImmutableList<? extends T> terms) {
        return map.entrySet().stream()
                .collect(substitutionFactory.toSubstitution(
                        Map.Entry::getValue,
                        e -> terms.get(e.getKey())));
    }

    public Substitution<T> getSubstitution(SubstitutionFactory substitutionFactory, ImmutableMap<Integer, ? extends T> terms) {
        return map.entrySet().stream()
                .collect(substitutionFactory.toSubstitution(
                        Map.Entry::getValue,
                        e -> terms.get(e.getKey())));
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public ImmutableExpression getConjunction(TermFactory termFactory, ImmutableList<? extends T> terms) {
        return termFactory.getConjunction(map.entrySet().stream()
                .map(e -> termFactory.getStrictEquality(terms.get(e.getKey()), e.getValue()))
                .collect(ImmutableCollectors.toList()));
    }

    public ImmutableExpression getConjunction(TermFactory termFactory, ImmutableMap<Integer, ? extends T> terms) {
        return termFactory.getConjunction(map.entrySet().stream()
                .map(e -> termFactory.getStrictEquality(terms.get(e.getKey()), e.getValue()))
                .collect(ImmutableCollectors.toList()));
    }
}
