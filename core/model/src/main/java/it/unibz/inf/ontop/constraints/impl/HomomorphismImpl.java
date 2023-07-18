package it.unibz.inf.ontop.constraints.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.constraints.Homomorphism;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.HashMap;
import java.util.Map;

public class HomomorphismImpl implements Homomorphism {

    private final ImmutableMap<Variable, VariableOrGroundTerm> map;

    HomomorphismImpl(ImmutableMap<Variable, VariableOrGroundTerm> map) {
        this.map = map;
    }

    @Override
    public VariableOrGroundTerm apply(VariableOrGroundTerm term) {
        return (term instanceof Variable) ? map.get(term) : term;
    }

    @Override
    public ImmutableExpression applyToBooleanExpression(ImmutableExpression booleanExpression, TermFactory termFactory) {
        return (ImmutableExpression) applyToImmutableTerm(booleanExpression, termFactory);
    }

    private ImmutableTerm applyToImmutableTerm(ImmutableTerm term, TermFactory termFactory) {
        if (term instanceof Constant) {
            return term;
        }
        else if (term instanceof Variable) {
            return map.get(term);
        }
        else if (term instanceof ImmutableFunctionalTerm) {
            ImmutableFunctionalTerm functionalTerm = (ImmutableFunctionalTerm) term;

            ImmutableList<ImmutableTerm> terms = functionalTerm.getTerms().stream()
                    .map(t -> applyToImmutableTerm(t, termFactory))
                    .collect(ImmutableCollectors.toList());

            FunctionSymbol functionSymbol = functionalTerm.getFunctionSymbol();
            return termFactory.getImmutableFunctionalTerm(functionSymbol, terms);
        }
        else {
            throw new IllegalArgumentException("Unexpected kind of term: " + term.getClass());
        }
    }

    @Override
    public Builder builder() {
        return new BuilderImpl(this);
    }


    @Override
    public boolean equals(Object other) {
        if (other instanceof HomomorphismImpl) {
            HomomorphismImpl o = (HomomorphismImpl)other;
            return this.map.equals(o.map);
        }
        return false;
    }

    @Override
    public String toString() {
        return "IH " + map;
    }


    static class BuilderImpl implements Builder {

        private final Map<Variable, VariableOrGroundTerm> map = new HashMap<>();
        private boolean valid = true;

        BuilderImpl() {
        }

        BuilderImpl(HomomorphismImpl homomorphism) {
            map.putAll(homomorphism.map);
        }

        @Override
        public Homomorphism build() {
            if (!valid)
                throw new IllegalArgumentException();
            return new HomomorphismImpl(ImmutableMap.copyOf(map));
        }

        @Override
        public Builder extend(ImmutableTerm from, ImmutableTerm to) {
            if (from instanceof Variable) {
                if (to instanceof VariableOrGroundTerm) {
                    VariableOrGroundTerm t = map.put((Variable) from, (VariableOrGroundTerm)to);
                    // t is the previous value
                    if (t == null || t.equals(to))
                        return this; // success
                }
            }
            else if (from instanceof Constant) {
                // constants must match
                if (from.equals(to))
                    return this; // success
            }
            else {
                // the from term can now only be a ImmutableFunctionalTerm
                ImmutableFunctionalTerm fromIFT = (ImmutableFunctionalTerm)from;
                // then the to term must be a ImmutableFunctionalTerm - no match otherwise
                if (to instanceof ImmutableFunctionalTerm) {
                    ImmutableFunctionalTerm toIFT = (ImmutableFunctionalTerm)to;
                    if (fromIFT.getFunctionSymbol().equals(toIFT.getFunctionSymbol()))
                        return extend(fromIFT.getTerms(), toIFT.getTerms());
                }
            }

            valid = false;
            return this;  // fail
        }

        @Override
        public Builder extend(ImmutableList<? extends ImmutableTerm> from, ImmutableList<? extends ImmutableTerm> to) {
            int arity = from.size();
            if (arity == to.size()) {
                for (int i = 0; i < arity; i++) {
                    // if we cannot find a match, then terminate the process and return false
                    if (!extend(from.get(i), to.get(i)).isValid())
                        return this;
                }
                return this;
            }
            valid = false;
            return this;
        }

        @Override
        public boolean isValid() {
            return valid;
        }
    }
}
