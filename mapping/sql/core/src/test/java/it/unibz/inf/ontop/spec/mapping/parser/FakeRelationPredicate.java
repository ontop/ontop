package it.unibz.inf.ontop.spec.mapping.parser;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.impl.PredicateImpl;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import javax.annotation.Nonnull;
import java.util.stream.IntStream;

/**
 * For low-level tests only!
 */
public class FakeRelationPredicate extends PredicateImpl {

    protected FakeRelationPredicate(@Nonnull String name, int arity, TypeFactory typeFactory) {
        super(name, arity, createExpectedTermTypes(arity, typeFactory), false);
    }

    private static ImmutableList<TermType> createExpectedTermTypes(int arity, TypeFactory typeFactory) {
        TermType rootTermType = typeFactory.getAbstractAtomicTermType();

        return IntStream.range(0, arity)
                .boxed()
                .map(i -> rootTermType)
                .collect(ImmutableCollectors.toList());
    }
}
