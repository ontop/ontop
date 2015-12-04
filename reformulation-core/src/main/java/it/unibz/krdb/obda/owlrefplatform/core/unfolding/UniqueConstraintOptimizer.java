package it.unibz.krdb.obda.owlrefplatform.core.unfolding;

import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.Substitution;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.SubstitutionUtilities;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.UnifierUtilities;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Multimap;

public class UniqueConstraintOptimizer {

	public static void selfJoinElimination(CQIE query, Multimap<Predicate, List<Integer>> primaryKeys) {

		List<Function> body = query.getBody();
		
		int startSize = body.size();
		int round = 0;

		while (true) {
			int roundStart = body.size();
			round++;
			
			Iterator<Function> it = body.iterator();
			while (it.hasNext()) {
				Function currentAtom = it.next();		
				if (!currentAtom.isDataFunction())
					continue;

				Collection<List<Integer>> pKeys = primaryKeys.get(currentAtom.getFunctionSymbol());
	            for (List<Integer> pKey : pKeys) {
	                if (pKey == null || pKey.isEmpty()) 
	                    continue;
	                
	                // the predicate has a primary key, looking for candidates for
	                // unification, when we find one we can stop, since the application
	                // of this optimization at each step of the derivation tree
	                // guarantees there wont be any other redundant atom.
	                
					Substitution mgu = null;
					
	                for (Function tempatom : body) {
	                	// reached the diagonal (i mean equality of pointers)
	                	if (tempatom == currentAtom)
	                		break;
	                	
	                    // predicates are different, atoms cannot be unified
	                    if (!tempatom.getFunctionSymbol().equals(currentAtom.getFunctionSymbol())) 
	                        continue;

	                    boolean redundant = true;
	                    for (Integer termidx2 : pKey) {
	                        if (!currentAtom.getTerm(termidx2 - 1).equals(tempatom.getTerm(termidx2 - 1))) {
	                            redundant = false;
	                            break;
	                        }
	                    }
	                    
	                    if (redundant) {
	                        // found a candidate replacement atom 
	                        mgu = UnifierUtilities.getMGU(currentAtom, tempatom);
	                        if (mgu != null) 
	                            break;
	                    }
	                }

	                if (mgu == null)
	                    continue;

					SubstitutionUtilities.applySubstitution(query, mgu, false);
					it.remove();
	                break;
	            }
			}
			// if have not removed any atom
			if (body.size() == roundStart)
				break;
		}
			
		int diff = startSize - body.size();
		if (diff != 0)
			System.out.println("ESJ ELIMINATION REMOVED " + diff + " ATOMS IN " + round + " ROUNDS");
	}
}
