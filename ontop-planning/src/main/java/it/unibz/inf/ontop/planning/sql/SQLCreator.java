package it.unibz.inf.ontop.planning.sql;

import java.util.List;

import com.google.common.collect.LinkedListMultimap;

import it.unibz.inf.ontop.planning.OntopPlanning;
import it.unibz.inf.ontop.planning.datatypes.MFragIndexToVarIndex;
import it.unibz.inf.ontop.planning.datatypes.Restriction;
import it.unibz.krdb.obda.model.Variable;

public interface SQLCreator {
    
    public String makeSQL(OntopPlanning op, LinkedListMultimap<Variable, MFragIndexToVarIndex> mOutVariableToFragmentsVariables);
    
    
    public static interface Builder{
		
	public Builder addValidCombination(List<Restriction> combination);
	
	public SQLCreator build();
    }
}
