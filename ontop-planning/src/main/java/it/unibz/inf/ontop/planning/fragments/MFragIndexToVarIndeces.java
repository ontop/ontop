package it.unibz.inf.ontop.planning.fragments;

import java.util.ArrayList;
import java.util.List;

public class MFragIndexToVarIndeces{
    int fragIndex;
    List<Integer> varIndeces;
    
    MFragIndexToVarIndeces(int fragIndex) {
        this.fragIndex = fragIndex;
        this.varIndeces = new ArrayList<>();
    }
    
    void addVarIndex(int varIndex){
        this.varIndeces.add(varIndex);
    }
    
    @Override
    public String toString(){
	return "(" + "fragIndex := " + this.fragIndex + ", varIndeces := " + varIndeces.toString() + ")";
    }
};