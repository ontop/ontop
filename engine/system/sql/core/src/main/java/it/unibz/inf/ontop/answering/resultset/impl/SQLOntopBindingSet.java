package it.unibz.inf.ontop.answering.resultset.impl;

import com.google.common.collect.ImmutableSortedSet;
import it.unibz.inf.ontop.answering.resultset.OntopBinding;
import it.unibz.inf.ontop.answering.resultset.OntopBindingSet;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;

import java.util.AbstractMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class SQLOntopBindingSet extends AbstractOntopBindingSet implements OntopBindingSet {

    SQLOntopBindingSet(ImmutableSortedSet<Variable> signature,
                       ImmutableSubstitution<DBConstant> sqlVar2DBConstant,
                       ImmutableSubstitution<ImmutableTerm> sparqlVar2Term) {
        super(computeBindingMap(signature, sqlVar2DBConstant, sparqlVar2Term));
    }

    private static LinkedHashMap<String, OntopBinding> computeBindingMap(ImmutableSortedSet<Variable> signature,
                                                                    ImmutableSubstitution<DBConstant> sqlVar2DBConstant,
                                                                    ImmutableSubstitution<ImmutableTerm> sparqlVar2Term) {

        ImmutableSubstitution<ImmutableTerm> composition = sqlVar2DBConstant.composeWith(sparqlVar2Term);

        return signature.stream()
                .map(v -> getBinding(v,composition))
                .filter(o -> o.isPresent())
                .map(o -> o.get())
                .collect(Collectors.toMap(
                        e -> e.getKey(),
                        e -> e.getValue(),
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    private static Optional<Map.Entry<String,OntopBinding>> getBinding(Variable v, ImmutableSubstitution<ImmutableTerm> composition) {
        Optional<RDFConstant> constant = evaluate(composition.apply(v));
        return constant.isPresent()?
                Optional.of(new AbstractMap.SimpleImmutableEntry<String, OntopBinding>(v.getName(), new OntopBindingImpl(v, constant.get()))):
                Optional.empty();
    }

    private static Optional<RDFConstant> evaluate(ImmutableTerm term) {
        ImmutableTerm simplifiedTerm = term.simplify(false);
        if (simplifiedTerm instanceof Constant){
            if (simplifiedTerm instanceof RDFConstant) {
                return Optional.of((RDFConstant) simplifiedTerm);
            }
            Constant constant = (Constant) simplifiedTerm;
            if (constant.isNull()) {
                return Optional.empty();
            }
            if(constant instanceof DBConstant){
                throw new InvalidConstantTypeInResultException(
                         constant +"is a DB constant. But a binding cannot have a DB constant as value");
            }
            throw new InvalidConstantTypeInResultException("Unexpected constant type for "+constant);
        }
        throw new InvalidTermAsResultException(simplifiedTerm);
    }

    public static class InvalidTermAsResultException extends OntopInternalBugException {
        InvalidTermAsResultException(ImmutableTerm term) {
            super("Term " + term + " does not evaluate to a constant");
        }
    }

    public static class InvalidConstantTypeInResultException extends OntopInternalBugException {
        InvalidConstantTypeInResultException (String message) {
            super(message);
        }
    }
}