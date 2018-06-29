package it.unibz.inf.ontop.answering.resultset.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.answering.resultset.OntopBinding;
import it.unibz.inf.ontop.answering.resultset.OntopBindingSet;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.model.term.Term;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.AbstractMap;
import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

public abstract class AbstractOntopBindingSet implements OntopBindingSet {

    // ImmutableMap is order preserving
    protected final ImmutableMap<String, Integer> signature;

    // ImmutableMap is order-preserving (similar to a LinkedHashMap)
    protected Optional<ImmutableMap<String, OntopBinding>> variable2BindingMap;

    protected AbstractOntopBindingSet(ImmutableMap<String, Integer> signature) {
        this.signature = signature;
        this.variable2BindingMap = Optional.empty();
    }

    @Override
    @Nonnull
    public Iterator<OntopBinding> iterator() {
        return getVariable2BindingMap().values().iterator();
    }

    @Override
    public Stream<OntopBinding> getBindings() {
        return variable2BindingMap.isPresent()?
                variable2BindingMap.get().values().stream():
                computeBindings();
    }

    private ImmutableMap<String, OntopBinding> getVariable2BindingMap(){
        return variable2BindingMap.isPresent()?
                variable2BindingMap.get():
                computeVariable2BindingMap();
    }

    private ImmutableMap<String, OntopBinding> computeVariable2BindingMap() {
        this.variable2BindingMap = Optional.of(
                computeBindings()
                        .collect(ImmutableCollectors.toMap(
                                b -> b.getName(),
                                b -> b
                        )));
        return variable2BindingMap.get();
    }

    protected abstract Stream<OntopBinding> computeBindings();

    @Override
    public ImmutableList<String> getBindingNames() {
        return ImmutableList.copyOf(getVariable2BindingMap().keySet());
    }

    @Override
    public String toString() {
        return getVariable2BindingMap().values().stream()
                .map(OntopBinding::toString)
                .collect(joining(",", "[", "]"));
    }

    @Override
    @Nullable
    public OntopBinding getBinding(String name) {
        return getVariable2BindingMap().get(name);
    }

    @Override
    @Nullable
    public OntopBinding getBinding(int index) {
        return getVariable2BindingMap().get(index - 1);
    }

//    public static class UnexpectedTargeTermTypeException extends OntopInternalBugException {
//        public UnexpectedTargeTermTypeException(Term term) {
//            super("Unexpected type "+ term.getClass()+" for term "+term);
//        }
//    }
//
//    public static class UnexpectedTargetPredicateTypeException extends OntopInternalBugException {
//        public UnexpectedTargetPredicateTypeException(Predicate predicate) {
//            super("Unexpected type "+ predicate.getClass()+" for predicate "+predicate);
//        }
//    }
}
