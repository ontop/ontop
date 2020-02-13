package it.unibz.inf.ontop.protege.gui.treemodels;

import it.unibz.inf.ontop.spec.mapping.TargetAtom;
import it.unibz.inf.ontop.model.term.*;

public class TreeModelTools {

    /**
     * A helper method to check a match
     */
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
//        } else if (term instanceof Variable) {
//            return ((Variable) term).getName().contains(keyword); // match found!
        } else if (term instanceof RDFConstant) {
            return ((RDFConstant) term).getValue().contains(keyword); // match found!
        } else
            return false;
    }
}
