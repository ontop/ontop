package it.unibz.inf.ontop.planning.fragments;

import java.util.ArrayList;
import java.util.List;

public class MapOutVariableToFragVariables{
    
    final String outVariable;
    final List<MFragIndexToVarIndeces> mFragIndexToVarIndeces;
    
    private MapOutVariableToFragVariables(Builder builder){
	this.outVariable = builder.outVariable;
	this.mFragIndexToVarIndeces = builder.list;
    }
    
    public static class Builder{
	private String outVariable;
	private List<MFragIndexToVarIndeces> list = new ArrayList<>();
	
	public Builder(String outVariable){
	    this.outVariable = outVariable;
	}
	
	public Builder add(MFragIndexToVarIndeces map){
	    list.add(map);
	    return this;
	}
	
	MapOutVariableToFragVariables build(){
	    MapOutVariableToFragVariables result = new MapOutVariableToFragVariables(this);
	    this.list = new ArrayList<>();
	    return result;
	}
    }
    
    @Override
    public String toString(){
	StringBuilder builder = new StringBuilder();
	builder.append( "OutVariable := " + this.outVariable + "->");
	builder.append( "fragsVariables := " + this.mFragIndexToVarIndeces.toString() );
	
	return builder.toString();
    }
}