package it.unibz.inf.ontop.planning.datatypes;

import it.unibz.krdb.obda.model.DatalogProgram;

/**
 * A Restriction is a pair (Signature, DatalogProgram) 
 * 
 * @author Davide Lanti
 *
 */
public class Restriction{
    
    private Signature signature;
    private DatalogProgram program;
    
    
    public Restriction(Signature s, DatalogProgram d){
	this.signature = s;
	this.program = d;
    }
    
    public Signature getSignature(){
	return this.signature;
    }
    
    public DatalogProgram getDLog(){
	return this.program;
    }
    
    @Override
    public String toString(){

	String result = "Signature:= " + signature + "\n" + 
		"DLog:= " + program;

	return result;
    }
}