package it.unibz.inf.ontop.planning.sql.helpers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.sql.QualifiedAttributeID;

public class ExtendedTerm extends TermDecorator {

    private List<Variable> variables;
    private Map<Variable, Set<QualifiedAttributeID>> aliases;
    private int termID;
    
    public ExtendedTerm(Term component, List<Variable> termVariables, Map<Variable, Set<QualifiedAttributeID>> aliases, int termID) {
	super(component);
	
	this.variables = termVariables;
	this.aliases = new HashMap<>();
	this.termID = termID;
	
	// Copy only the portion of the aliases relevant for the variables in the term
	for( Variable v :  aliases.keySet() ){
	    if( termVariables.contains(v) ){
		this.aliases.put(v, aliases.get(v));
	    }
	}
	
    }
    
    public List<Variable> getTermVariables(){
	return Collections.unmodifiableList(this.variables);
    }
    
    public Set<QualifiedAttributeID> getAliasesFor( Variable v ){
	return this.aliases.get(v);
    }
    
    /**
     * 
     * @param termVariable
     * @return The name in the projection for the given termVariable
     */
    public String getProjNameForTermVariable( Variable termVariable ){
	
	String result = null;
	
	for( int i = 0; i < this.variables.size(); ++i ){
	    if( this.variables.get(i).equals(termVariable) ){
		result = "t"+this.termID+"v"+i;
	    }
	}
	return result;
    }
    
    /**
     * @return 
     * 
     * URI("http://sws.ifi.uio.no/data/npd-v2/wellbore/{}/stratum/{}/cores",t9_7,t5_7) -> 
     * http://www.w3.org/2001/XMLSchema#decimal(t1_7)
     *  
     */
    public List<String> split(){
	
	List<String> result;
	
	String termString = super.component.toString();
	if( termString.startsWith("URI") ){
	    String pruned = termString.substring(termString.indexOf("(") + 2, termString.indexOf(",") -1 ); // Remove apixes " too
	    result = Arrays.asList(pruned.split("\\{"));
//	    System.out.println(result);
	    for( int i = 0; i < result.size(); ++i ){
		String split = result.get(i);
		split = split.replace('}', ' ');
		split = split.trim();
		result.set(i, split);
	    }
//	    System.out.println(splits);
	}
	else{
	    String pruned = termString.substring(0, termString.indexOf("("));
	    result = new ArrayList<>();
	    result.add(pruned);
	}
	return result;
    }
    
    @Override
    public String toString(){
	StringBuilder builder = new StringBuilder();
	builder.append("Term: " + super.component.toString() + "\n");
	builder.append("VARS: " + this.variables.toString() + "\n");
	builder.append("ALIASES: " + this.aliases.toString() + "\n");
	
	return builder.toString();
    }
}
