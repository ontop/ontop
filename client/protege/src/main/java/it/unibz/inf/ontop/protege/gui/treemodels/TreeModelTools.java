package it.unibz.inf.ontop.protege.gui.treemodels;

import it.unibz.inf.ontop.model.atom.TargetAtom;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;

public class TreeModelTools {

    /** A helper method to check a match */
    public static boolean match(String keyword, TargetAtom atom) {
        return atom.getSubstitutedTerms().stream()
                .anyMatch(t -> match(keyword, t));

    }

    public static boolean match(String keyword, ImmutableTerm term) {
        if (term instanceof ImmutableFunctionalTerm) {
            ImmutableFunctionalTerm functionTerm = (ImmutableFunctionalTerm) term;
            if (functionTerm.getFunctionSymbol().toString().contains(keyword)) { // match found!
                return true;
            }
            // Recursive
            return functionTerm.getTerms().stream()
                    .anyMatch(t -> match(keyword, t));
        }
        if (term instanceof Variable) {
            return ((Variable) term).getName().contains(keyword); // match found!
        }
        return false;
    }
}
