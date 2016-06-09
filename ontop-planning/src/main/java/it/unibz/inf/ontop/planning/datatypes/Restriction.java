package it.unibz.inf.ontop.planning.datatypes;

import it.unibz.krdb.obda.model.DatalogProgram;

public class Restriction{
    private Pair<Signature, DatalogProgram> restrictionToTemplateSignature;
    
    public Restriction(Signature s, DatalogProgram d){
	this.restrictionToTemplateSignature = new Pair<>(s,d);
    }
    
    public Signature getSignature(){
	return this.restrictionToTemplateSignature.first;
    }
    
    public DatalogProgram getDLog(){
	return this.restrictionToTemplateSignature.second;
    }
    
    @Override
    public String toString(){
	
	String result = "Signature:= " + restrictionToTemplateSignature.first + "\n" + 
		"DLog:= " + restrictionToTemplateSignature.second;
	
	return result;
    }
}