package it.unibz.inf.ontop.planning;

import java.util.List;

import com.google.common.collect.LinkedListMultimap;

import it.unibz.inf.ontop.planning.utils.combinations.CombinationVisitor;

public class FragmentsVisitor implements CombinationVisitor<Restriction>{

    // {wc=[fragIndex := 0, varIndex := 0)], x=[fragIndex := 0, varIndex := 1), fragIndex := 1, varIndex := 0)], ...}
    private LinkedListMultimap<String, MFragIndexToVarIndex> mJoinVariableToFragmentsVariables;
    
    public FragmentsVisitor(
	    LinkedListMultimap<String, MFragIndexToVarIndex> mOutVariableToFragmentsVariables) {
	
	this.mOutVariableToFragmentsVariables = LinkedListMultimap.create();
	
	for( String key : mOutVariableToFragmentsVariables ){
	    if( mOutVariableToFragmentsVariables.get(key).size() > 1 ){
		// Join variable
		this.
	    }
	}
	this.mOutVariableToFragmentsVariables = mOutVariableToFragmentsVariables;
    }

    @Override
    public void visit(List<Restriction> combination) {
	// Check if if ALL THE JOINS are valid, by using mOutVariableToFragmentsVariables
	
	for( String key : mJoinVariableToFragmentsVariables.keySet() ){
	    List<MFragIndexToVarIndex> pairs = mJoinVariableToFragmentsVariables.get(key);
	    
	    for( MFragIndexToVarIndex pair : pairs ){
		
	    }
	    
	}
    }
    
}
