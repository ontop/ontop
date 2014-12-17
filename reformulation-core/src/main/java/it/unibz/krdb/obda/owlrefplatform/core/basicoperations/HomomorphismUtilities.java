package it.unibz.krdb.obda.owlrefplatform.core.basicoperations;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import it.unibz.krdb.obda.model.Constant;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.impl.AnonymousVariable;
import it.unibz.krdb.obda.model.impl.VariableImpl;

public class HomomorphismUtilities {

	
	public static boolean extendHomomorphism(SubstitutionBuilder sb, Function from, Function to) {
		
		if ((from.getArity() != to.getArity()) || !(from.getFunctionSymbol().equals(to.getFunctionSymbol())))
			return false;
		
		int arity = from.getArity();
		for (int i = 0; i < arity; i++) {
			Term fromTerm = from.getTerm(i);
			Term toTerm = to.getTerm(i);
			if (fromTerm instanceof VariableImpl) {
				boolean result = sb.extend((VariableImpl)fromTerm, toTerm);
				// if we cannot find a match, terminate the process and return false
				if (!result)
					return false;
			}
			else if (fromTerm instanceof AnonymousVariable) {
				// ??
			}
			else if (fromTerm instanceof Constant) {
				// constants must match 
				if (!fromTerm.equals(toTerm))
					return false;
			}
			else /*if (fromTerm instanceof Function)*/ {
				// the to term must also be a function
				if (!(toTerm instanceof Function))
					return false;
				
				boolean result = extendHomomorphism(sb, (Function)fromTerm, (Function)toTerm);
				// if we cannot find a match, terminate the process and return false
				if (!result)
					return false;
			}
		}
		
		return true;
	}

	/**
	 * Extends a given substitution that maps each atom in {@code from} to match at least one atom in {@code to}
	 *
	 * @param sb
	 * @param from
	 * @param to
	 * @return
	 */
	public static Substitution computeHomomorphism(SubstitutionBuilder sb, List<Function> from, Map<Predicate, List<Function>> to) {
	
		int fromSize = from.size();
		if (fromSize == 0)
			return sb.getSubstituition();

		// stack of partial homomorphisms
		Stack<SubstitutionBuilder> sbStack = new Stack<>();				
		// set the capacity to reduce memory re-allocations
		List<Stack<Function>> choicesMap = new ArrayList<>(fromSize); 
		
		int currentAtomIdx = 0;
		while (currentAtomIdx >= 0) {		
			Function currentAtom = from.get(currentAtomIdx);
			
			Stack<Function> choices;
			if (currentAtomIdx >= choicesMap.size()) {
				// we have never reached this atom (this is lazy initialization)
				// initializing the stack
				choices = new Stack<>();
				 // add all choices for the current predicate symbol
				choices.addAll(to.get(currentAtom.getFunctionSymbol()));
				choicesMap.add(currentAtomIdx, choices);
			}
			else
				choices = choicesMap.get(currentAtomIdx);
			
			boolean choiceMade = false;
			while (!choices.isEmpty() && !choiceMade) {
				SubstitutionBuilder sb1 = sb.clone(); // clone!
				choiceMade = extendHomomorphism(sb1, currentAtom, choices.pop());
				if (choiceMade) 
					sb = sb1;
			}
			if (!choiceMade) {
				// backtracking
				 // restore all choices for the current predicate symbol
				choices.addAll(to.get(currentAtom.getFunctionSymbol()));
				currentAtomIdx--;   // move to the previous atom
				if (currentAtomIdx >= 0)
					sb = sbStack.pop();   // restore the partial homomorphism
			}
			else {
				if (currentAtomIdx == fromSize - 1) {
					// we reached the last atom -- return the result
					return sb.getSubstituition();
				}
				sbStack.push(sb);   // save the partial homomorphism
				currentAtomIdx++;  // move to the next atom
			}
		}
		
		// checked all possible substitutions and have not found anything
		return null;
	}
}
