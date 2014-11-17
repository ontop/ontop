package org.semanticweb.ontop.owlrefplatform.core.basicoperations;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.ontology.BasicClassDescription;
import org.semanticweb.ontop.ontology.OClass;
import org.semanticweb.ontop.ontology.PropertyExpression;
import org.semanticweb.ontop.ontology.SomeValuesFrom;
import org.semanticweb.ontop.owlrefplatform.core.dagjgrapht.Equivalences;
import org.semanticweb.ontop.owlrefplatform.core.dagjgrapht.TBoxReasoner;

public class LinearInclusionDependencies {

    private static final OBDADataFactory ofac = OBDADataFactoryImpl.getInstance();
    
	private final Map<Predicate, List<CQIE>> rules = new HashMap<Predicate, List<CQIE>>();
	
	public List<CQIE> getRules(Predicate pred) {
		List<CQIE> rrs = rules.get(pred);
		if (rrs == null)
			return Collections.emptyList();
		
		return rrs;
	}
		
	/*
	 * adds a rule to the indexed linear dependencies
	 * 
	 * IMPORTANT: this method does NOT rename variables
	 * 
	 * @param head: atom
	 * @param body: atom
	 */
	
	public void addRule(Function head, Function body) {
        CQIE rule = ofac.getCQIE(head, body);
		//rule = DatalogUnfolder.getFreshRule(rule, 4022013); // Random suffix number
		
        List<CQIE> list = rules.get(body.getFunctionSymbol());
        if (list == null) {
        	list = new LinkedList<CQIE>();
        	rules.put(body.getFunctionSymbol(), list);
        }
		
        list.add(rule);		
	}
	
	public static LinearInclusionDependencies getABoxDependencies(TBoxReasoner reasoner, boolean full) {
		LinearInclusionDependencies dependencies = new LinearInclusionDependencies();
		
		for (Equivalences<PropertyExpression> propNode : reasoner.getProperties()) {
			// super might be more efficient
			for (Equivalences<PropertyExpression> subpropNode : reasoner.getProperties().getSub(propNode)) {
				for (PropertyExpression subprop : subpropNode) {
					if (subprop.isInverse())
						continue;
					
	                Function body = translate(subprop);

	                for (PropertyExpression prop : propNode)  {
	                	if (prop == subprop)
	                		continue;
	                	
		                Function head = translate(prop);	
		                dependencies.addRule(head, body);
					}
				}
			}
		}
		for (Equivalences<BasicClassDescription> classNode : reasoner.getClasses()) {
			// super might be more efficient
			for (Equivalences<BasicClassDescription> subclassNode : reasoner.getClasses().getSub(classNode)) {
				for (BasicClassDescription subclass : subclassNode) {

	                Function body = translate(subclass, variableYname);
                	//if (!(subclass instanceof OClass) && !(subclass instanceof PropertySomeRestriction))
	                if (body == null)
	                	continue;

	                for (BasicClassDescription cla : classNode)  {
	                	if (!(cla instanceof OClass) && !(!full && (cla instanceof SomeValuesFrom)))
	                		continue;
	                	
	                	if (cla == subclass)
	                		continue;

	                	// use a different variable name in case th ebody has an existetial as well
		                Function head = translate(cla, variableZname);	
		                dependencies.addRule(head, body);
					}
				}
			}
		}
		
		return dependencies;
	}

	private static final String variableXname = "x_4022013";
	private static final String variableYname = "y_4022013";
	private static final String variableZname = "z_4022013";
	
    private static Function translate(PropertyExpression property) {
		final Variable varX = ofac.getVariable(variableXname);
		final Variable varY = ofac.getVariable(variableYname);

		if (property.isInverse()) 
			return ofac.getFunction(property.getPredicate(), varY, varX);
		else 
			return ofac.getFunction(property.getPredicate(), varX, varY);
	}
	
    private static Function translate(BasicClassDescription description, String existentialVariableName) {
		final Variable varX = ofac.getVariable(variableXname);
		if (description instanceof OClass) {
			OClass klass = (OClass) description;
			return ofac.getFunction(klass.getPredicate(), varX);
		} 
		else if (description instanceof SomeValuesFrom) {
			final Variable varY = ofac.getVariable(existentialVariableName);
			PropertyExpression property = ((SomeValuesFrom) description).getProperty();
			if (property.isInverse()) 
				return ofac.getFunction(property.getPredicate(), varY, varX);
			else 
				return ofac.getFunction(property.getPredicate(), varX, varY);
		} 
		else 
			return null;  // if datatype then return null
	}

}
