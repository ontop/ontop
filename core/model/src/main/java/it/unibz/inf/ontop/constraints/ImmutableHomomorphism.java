package it.unibz.inf.ontop.constraints;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.term.*;

import java.util.HashMap;
import java.util.Map;

public class ImmutableHomomorphism {
    private final ImmutableMap<Variable, VariableOrGroundTerm> map;

    private ImmutableHomomorphism(ImmutableMap<Variable, VariableOrGroundTerm> map) {
        this.map = map;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(ImmutableHomomorphism homomorphism) {
        return new Builder(homomorphism);
    }

    public ImmutableSet<Map.Entry<Variable, VariableOrGroundTerm>> entrySet() {
        return map.entrySet();
    }

    public VariableOrGroundTerm apply(VariableOrGroundTerm term) {
        return (term instanceof Variable) ? map.get(term) : term;
    }

    @Override
    public boolean equals(Object other) {
        if (other != null && other instanceof ImmutableHomomorphism) {
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

        public Builder extend(VariableOrGroundTerm from, VariableOrGroundTerm to) {
            if (from instanceof Variable) {
                VariableOrGroundTerm t = map.put((Variable) from, to);
                // t is the previous value
                if (t == null || t.equals(to))
                    return this;
            }
            else if (from instanceof Constant) {
                // constants must match
                if (from.equals(to))
                    return this;
            }
            else /*if (from instanceof GroundFunctionalTerm)*/ {
                // the to term must also be a function
                if (to instanceof GroundFunctionalTerm) {
                    GroundFunctionalTerm fromF = (GroundFunctionalTerm)from;
                    GroundFunctionalTerm toF = (GroundFunctionalTerm)to;
                    if (fromF.getFunctionSymbol().equals(toF.getFunctionSymbol()))
                        return extend(fromF.getTerms(), toF.getTerms());
                }
            }
            return invalidate();
        }

        public Builder extend(ImmutableList<? extends VariableOrGroundTerm> from, ImmutableList<? extends VariableOrGroundTerm> to) {
            int arity = from.size();
            if (arity == to.size()) {
                for (int i = 0; i < arity; i++) {
                    // if we cannot find a match, terminate the process and return false
                    extend(from.get(i), to.get(i));
                    if (!valid)
                        return this;
                }
                return this;
            }
            return invalidate();
        }

        private Builder invalidate() {
            valid = false;
            return this;
        }

        public boolean isValid() { return valid; }
    }
}
