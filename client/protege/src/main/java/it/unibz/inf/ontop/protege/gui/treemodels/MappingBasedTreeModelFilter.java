package it.unibz.inf.ontop.protege.gui.treemodels;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.RDFConstant;
import it.unibz.inf.ontop.spec.mapping.TargetAtom;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;

public class MappingBasedTreeModelFilter extends TreeModelFilter<SQLPPTriplesMap> {

    public MappingBasedTreeModelFilter() {
        super.bNegation = false;
    }

    @Override
    public boolean match(SQLPPTriplesMap object) {
        ImmutableList<TargetAtom> atoms = object.getTargetAtoms();

        boolean isMatch = false;
        for (String keyword : vecKeyword) {
            for (TargetAtom atom : atoms) {
                isMatch = isMatch || match(keyword.trim(), atom);
            }
            if (isMatch) {
                break; // end loop if a match is found!
            }
        }
        return bNegation != isMatch;
    }



    /**
     * A helper method to check a match
     */
    private static boolean match(String keyword, TargetAtom atom) {
        return atom.getSubstitutedTerms().stream()
                .anyMatch(t -> match(keyword, t));

    }

    private static boolean match(String keyword, ImmutableTerm term) {
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
