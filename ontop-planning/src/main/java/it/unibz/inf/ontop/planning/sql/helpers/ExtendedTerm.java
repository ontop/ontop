package it.unibz.inf.ontop.planning.sql.helpers;

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
    
    public ExtendedTerm(Term component, List<Variable> termVariables, Map<Variable, Set<QualifiedAttributeID>> aliases) {
	super(component);
	
	this.variables = termVariables;
	this.aliases = new HashMap<>();
	
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
    
    @Override
    public String toString(){
	StringBuilder builder = new StringBuilder();
	builder.append("VARS: " + this.variables.toString());
	builder.append("\n");
	builder.append("ALIASES: " + this.aliases.toString());
	
	return builder.toString();
    }
}
