package it.unibz.krdb.obda.utils;

import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.Variable;

import java.util.LinkedHashSet;
import java.util.Set;

public class QueryValidator {

	public static Set<Variable> getUnboundHeadVariables(CQIE cq) {
		Set<Variable> headVariables = cq.getHead().getVariables();
		Set<Variable> missingVariables = new LinkedHashSet<Variable>();

		for (Variable v : headVariables) {
			boolean safe = false;
			for (Function atom : cq.getBody()) {
				safe = safe || atom.getVariables().contains(v);
				if (safe)
					break;
			}

			if (!safe) {
				missingVariables.add(v);
			}
		}

		return missingVariables;
	}

	public static Set<Variable> getUnboundHeadVariables(DatalogProgram prog) {
		Set<Variable> missingVariables = new LinkedHashSet<Variable>();
		for (CQIE cq : prog.getRules()) {
			for (Variable v : getUnboundHeadVariables(cq))
				missingVariables.add(v);
		}
		return missingVariables;
	}

}
