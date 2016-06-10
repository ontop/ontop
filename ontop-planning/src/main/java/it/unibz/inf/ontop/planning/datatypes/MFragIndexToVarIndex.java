package it.unibz.inf.ontop.planning.datatypes;

public class MFragIndexToVarIndex{
    
    private final int fragIndex;
    private final int varIndex;
    
    public MFragIndexToVarIndex(Integer fragIndex, Integer varIndex) {
	this.fragIndex = fragIndex;
	this.varIndex = varIndex;
    }
    
    public int getFragIndex(){
	return this.fragIndex;
    }
    
    public int getVarIndex(){
	return this.varIndex;
    }
    
    @Override
    public String toString() {
	return "(" + "fragIndex := " + this.fragIndex + ", varIndex := " + this.varIndex + ")";
    }
};