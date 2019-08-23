package it.unibz.inf.ontop.datalog;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.ExpressionOperation;
import it.unibz.inf.ontop.model.term.Function;
import it.unibz.inf.ontop.model.term.Term;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.impl.SubstitutionImpl;
import it.unibz.inf.ontop.substitution.impl.SubstitutionUtilities;

import java.util.List;

@Singleton
public class EQNormalizer {

    private final TermFactory termFactory;
    private final SubstitutionUtilities substitutionUtilities;

    @Inject
    private EQNormalizer(TermFactory termFactory, SubstitutionUtilities substitutionUtilities) {
        this.termFactory = termFactory;
        this.substitutionUtilities = substitutionUtilities;
    }

    /***
	 * Enforces all equalities in the query, that is, for every equivalence
	 * class (among variables) defined by a set of equalities, it chooses one
	 * representative variable and replaces all other variables in the equivalence
	 * class with the representative variable. For example, if the query body
	 * is R(x,y,z), x=y, y=z. It will choose x and produce the following body
	 * R(x,x,x).
	 * <p>
     * We ignore the equalities with disjunctions. For example R(x,y,z), x=y OR y=z
	 * Note the process will also remove from the body all the equalities that are
	 * here processed.
	 * 
	 * 
	 * @param result
	 */
	public void enforceEqualities(CQIE result) {

		List<Function> body = result.getBody();
		Substitution mgu = new SubstitutionImpl(termFactory);

		// collecting all equalities as substitutions 

		for (int i = 0; i < body.size(); i++) {
			Function atom = body.get(i);
			substitutionUtilities.applySubstitution(atom, mgu);

            if (atom.getFunctionSymbol() == ExpressionOperation.EQ && ! ((atom.getTerm(0) instanceof Function) && (atom.getTerm(1) instanceof Function)) ) {
                if (!mgu.composeTerms(atom.getTerm(0), atom.getTerm(1)))
                    continue;

                body.remove(i);
                i--;
            }
            //search for nested equalities in AND function
            else if (atom.getFunctionSymbol() == ExpressionOperation.AND) {
                nestedEQSubstitutions(atom, mgu);

                //we remove the function if empty because all its terms were equalities
                if (atom.getTerms().isEmpty()) {
                    body.remove(i);
                    i--;
                }
                else {
                    //if there is only a term left we remove the conjunction
                    if (atom.getTerms().size() == 1) {
                        body.set(i, (Function) atom.getTerm(0));
                    }
                }
            }
        }

		substitutionUtilities.applySubstitution(result, mgu, false);
	}

    /**
     * We search for equalities in conjunctions. This recursive methods explore AND functions 
     * and removes EQ functions, substituting the values using the class
     * 
     * @param atom the atom that can contain equalities
     * @param mgu mapping between a variable and a term
     */
    private void nestedEQSubstitutions(Function atom, Substitution mgu) {
    	
        List<Term> terms = atom.getTerms();
        for (int i = 0; i < terms.size(); i++) {
            Term t = terms.get(i);

            if (t instanceof Function) {
                Function t2 = (Function) t;
                substitutionUtilities.applySubstitution(t2, mgu);

                //in case of equalities do the substitution and remove the term
                if (t2.getFunctionSymbol() == ExpressionOperation.EQ && ! ((atom.getTerm(0) instanceof Function) && (atom.getTerm(1) instanceof Function))) {
                    if (!mgu.composeTerms(t2.getTerm(0), t2.getTerm(1)))
                        continue;
                    
                    terms.remove(i);
                    i -= 1;
                }
                //consider the case of  AND function. Calls recursive method to consider nested equalities
                else {
                    if (t2.getFunctionSymbol() == ExpressionOperation.AND) {
                        nestedEQSubstitutions(t2, mgu);

                        //we remove the function if empty because all its terms were equalities
                        if (t2.getTerms().isEmpty()) {
                            terms.remove(i);
                            i--;
                        } 
                        else {
                            //if there is only a term left we remove the conjunction
                            //we remove and function and we set  atom equals to the term that remained
                            if (t2.getTerms().size() == 1) {
                                atom.setTerm(i, t2.getTerm(0));
                            }
                        }
                    }
                }
            }
        }
    }

}
