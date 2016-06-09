package it.unibz.inf.ontop.planning.visitors;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.LinkedListMultimap;

import it.unibz.inf.ontop.planning.datatypes.MFragIndexToVarIndex;
import it.unibz.inf.ontop.planning.datatypes.Restriction;
import it.unibz.inf.ontop.planning.datatypes.Template;
import it.unibz.inf.ontop.planning.sql.SQLCreator;
import it.unibz.inf.ontop.planning.utils.combinations.CombinationVisitor;
import it.unibz.krdb.obda.model.Variable;

public class FragmentsVisitor implements CombinationVisitor<Restriction>{
    
    // State
    private final LinkedListMultimap<Variable, MFragIndexToVarIndex> mOutVariableToFragmentsVariables;
    private final SQLCreator sqlCreator;
    
    public FragmentsVisitor(
	    LinkedListMultimap<Variable, MFragIndexToVarIndex> mOutVariableToFragmentsVariables) {
	this.mOutVariableToFragmentsVariables = mOutVariableToFragmentsVariables;	
	this.sqlCreator = SQLCreator.getInstance();
    }

    public SQLCreator getSQLCreatorInstance(){
	return this.sqlCreator;
    }
    
    private boolean joinVar(Variable v) {
	return mOutVariableToFragmentsVariables.get(v).size() > 1;
    }

    @Override
    public void visit(List<Restriction> combination) {
	// Check if if ALL THE JOINS are valid, by using mOutVariableToFragmentsVariables
	
	for( Variable v : mOutVariableToFragmentsVariables.keySet() ){
	    if( joinVar(v) && checkTemplates(v, combination) ){
		// The joins are on the same template, produce the SQL
		sqlCreator.addValidCombination(combination);
	    }
	}
    }

    private boolean checkTemplates(Variable v, List<Restriction> combination) {

	boolean templatesOk = false;
	
	// [fragIndex := 0, varIndex := 1, fragIndex := 1, varIndex := 0, ... ]
	List<MFragIndexToVarIndex> list = this.mOutVariableToFragmentsVariables.get(v);
	
	Set<Template> templates = new HashSet<>();
	for( MFragIndexToVarIndex el : list ){
	    Restriction r = combination.get(el.getFragIndex());
	    Template template = r.getSignature().getTemplateOfIndex( el.getVarIndex() );
	    templates.add(template);
	}
	
	if( templates.size() == 1 ) templatesOk = true;
	
	return templatesOk;	
    }    
}
