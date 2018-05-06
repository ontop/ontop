package it.unibz.inf.ontop.answering.resultset.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.answering.resultset.OntopBinding;
import it.unibz.inf.ontop.answering.resultset.OntopBindingSet;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.model.term.Function;
import it.unibz.inf.ontop.model.term.Term;
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

    protected final ImmutableList<String> signature;

    // ImmutableMap is order-preserving (similar to a LinkedHashMap)
    protected Optional<ImmutableMap<String, OntopBinding>> variableName2BindingMap;

    protected AbstractOntopBindingSet(ImmutableList<String> signature) {
        this.signature = signature;
        this.variableName2BindingMap = Optional.empty();
    }

    @Override
    @Nonnull
    public Iterator<OntopBinding> iterator() {
        return getVariableName2BindingMap().values().iterator();
    }

    @Override
    public Stream<OntopBinding> getBindings() {
        return getVariableName2BindingMap().values().stream();
    }

    private ImmutableMap<String, OntopBinding> getVariableName2BindingMap(){
        return variableName2BindingMap.isPresent()?
                variableName2BindingMap.get():
                computeVariable2BindingMap();
    }

    private ImmutableMap<String, OntopBinding> computeVariable2BindingMap() {
        this.variableName2BindingMap = Optional.of(
                signature.stream()
                .map(s -> new AbstractMap.SimpleImmutableEntry(s, computeBinding(s)))
                .filter(e -> e.getValue() != null)
                .collect(ImmutableCollectors.toMap())
        );
        return variableName2BindingMap.get();
    }

    @Nullable
    protected abstract OntopBinding computeBinding(String variableName);

    @Override
    public ImmutableList<String> getBindingNames() {
        return ImmutableList.copyOf(getVariableName2BindingMap().keySet());
    }

    @Override
    public String toString() {
        return getVariableName2BindingMap().values().stream()
                .map(OntopBinding::toString)
                .collect(joining(",", "[", "]"));
    }

    @Override
    @Nullable
    public OntopBinding getBinding(String name) {
        return variableName2BindingMap.isPresent()?
                variableName2BindingMap.get().get(name):
                computeBinding(name);
    }

    public static class UnexpectedTargeTermTypeException extends OntopInternalBugException {
        public UnexpectedTargeTermTypeException(Term term) {
            super("Unexpected type "+ term.getClass()+" for term "+term);
        }
    }

    public static class UnexpectedTargetPredicateTypeException extends OntopInternalBugException {
        public UnexpectedTargetPredicateTypeException(Predicate predicate) {
            super("Unexpected type "+ predicate.getClass()+" for predicate "+predicate);
        }
    }
}
