package it.unibz.inf.ontop.substitution;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.impl.ImmutableUnificationTools;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public interface UnifierBuilder<T extends ImmutableTerm> {

    UnifierBuilder<T> unifyTermLists(ImmutableList<? extends T> args1, ImmutableList<? extends T> args2);

    UnifierBuilder<T> unifyTermStreams(IntStream indexes, IntFunction<? extends T> args1, IntFunction<? extends T> args2);

    <B> UnifierBuilder<T>  unifyTermStreams(Stream<B> stream, Function<B, T> args1, Function<B, T> args2);

    UnifierBuilder<T>  unifyTerms(T t1, T t2);

    Optional<ImmutableSubstitution<T>> build();
}