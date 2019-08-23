package it.unibz.inf.ontop.substitution;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.impl.PredicateImpl;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import javax.annotation.Nonnull;
import java.util.stream.IntStream;

import static it.unibz.inf.ontop.OntopModelTestingTools.TYPE_FACTORY;

public class OntopModelTestPredicate extends PredicateImpl {

    protected OntopModelTestPredicate(@Nonnull String name, int arity) {
        super(name, createExpectedBaseTermTypeList(arity));
    }

    private static ImmutableList<TermType> createExpectedBaseTermTypeList(int arity) {
        TermType rootTermType = TYPE_FACTORY.getAbstractAtomicTermType();

        return IntStream.range(0, arity)
                .boxed()
                .map(i -> rootTermType)
                .collect(ImmutableCollectors.toList());
    }
}
