package it.unibz.inf.ontop.constraints;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.HashMap;
import java.util.Map;

public class ImmutableHomomorphism {
    private final ImmutableMap<Variable, VariableOrGroundTerm> map;

    private ImmutableHomomorphism(ImmutableMap<Variable, VariableOrGroundTerm> map) {
        this.map = map;
    }

    /*
        start a new homomorphism
     */
    public static Builder builder() {
        return new Builder();
    }

    /*
        extend an existing homomorphism
     */
    public static Builder builder(ImmutableHomomorphism homomorphism) {
        return new Builder(homomorphism);
    }

    public VariableOrGroundTerm apply(VariableOrGroundTerm term) {
        return (term instanceof Variable) ? map.get(term) : term;
    }

    public ImmutableMap<Variable, VariableOrGroundTerm> asMap() { return map; }


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
    public boolean equals(Object other) {
        if (other instanceof ImmutableHomomorphism) {
            ImmutableHomomorphism o = (ImmutableHomomorphism)other;
            return this.map.equals(o.map);
        }
        return false;
    }

    @Override
    public String toString() {
        return "IH " + map;
    }

    public static class Builder {
        private final Map<Variable, VariableOrGroundTerm> map = new HashMap<>();
        private boolean valid = true;

        public Builder() {
        }

        public Builder(ImmutableHomomorphism homomorphism) {
            map.putAll(homomorphism.map);
        }

        public ImmutableHomomorphism build() {
            if (!valid)
                throw new IllegalArgumentException();
            return new ImmutableHomomorphism(ImmutableMap.copyOf(map));
        }

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
                    if (fromIFT.getFunctionSymbol().equals(toIFT.getFunctionSymbol())) {
                        for (int i = 0; i < fromIFT.getArity(); i++) {
                            if (!extend(fromIFT.getTerm(i), toIFT.getTerm(i)).isValid())
                                return this; // fail - early exit
                        }
                        return this; // success
                    }
                }
            }

            valid = false;
            return this;  // fail
        }

        public Builder extend(ImmutableList<? extends VariableOrGroundTerm> from, ImmutableList<? extends VariableOrGroundTerm> to) {
            int arity = from.size();
            if (arity == to.size()) {
                for (int i = 0; i < arity; i++) {
                    // if we cannot find a match, then terminate the process and return false
                    extend(from.get(i), to.get(i));
                    if (!valid)
                        return this;
                }
                return this;
            }
            valid = false;
            return this;
        }

        public boolean isValid() { return valid; }
    }
}
