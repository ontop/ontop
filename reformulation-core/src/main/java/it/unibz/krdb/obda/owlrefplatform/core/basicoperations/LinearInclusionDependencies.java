package it.unibz.krdb.obda.owlrefplatform.core.basicoperations;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.ontology.ClassExpression;
import it.unibz.krdb.obda.ontology.DataPropertyExpression;
import it.unibz.krdb.obda.ontology.DataSomeValuesFrom;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.ObjectPropertyExpression;
import it.unibz.krdb.obda.ontology.ObjectSomeValuesFrom;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.Equivalences;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasoner;

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
		
		for (Equivalences<ObjectPropertyExpression> propNode : reasoner.getObjectProperties()) {
			// super might be more efficient
			for (Equivalences<ObjectPropertyExpression> subpropNode : reasoner.getObjectProperties().getSub(propNode)) {
				for (ObjectPropertyExpression subprop : subpropNode) {
					if (subprop.isInverse())
						continue;
					
	                Function body = translate(subprop);

	                for (ObjectPropertyExpression prop : propNode)  {
	                	if (prop == subprop)
	                		continue;
	                	
		                Function head = translate(prop);	
		                dependencies.addRule(head, body);
					}
				}
			}
		}
		for (Equivalences<DataPropertyExpression> propNode : reasoner.getDataProperties()) {
			// super might be more efficient
			for (Equivalences<DataPropertyExpression> subpropNode : reasoner.getDataProperties().getSub(propNode)) {
				for (DataPropertyExpression subprop : subpropNode) {
					
	                Function body = translate(subprop);

	                for (DataPropertyExpression prop : propNode)  {
	                	if (prop == subprop)
	                		continue;
	                	
		                Function head = translate(prop);	
		                dependencies.addRule(head, body);
					}
				}
			}
		}
		for (Equivalences<ClassExpression> classNode : reasoner.getClasses()) {
			// super might be more efficient
			for (Equivalences<ClassExpression> subclassNode : reasoner.getClasses().getSub(classNode)) {
				for (ClassExpression subclass : subclassNode) {

	                Function body = translate(subclass, variableYname);
                	//if (!(subclass instanceof OClass) && !(subclass instanceof PropertySomeRestriction))
	                if (body == null)
	                	continue;

	                for (ClassExpression cla : classNode)  {
	                	if (!(cla instanceof OClass) && !(!full && ((cla instanceof ObjectSomeValuesFrom) || (cla instanceof DataSomeValuesFrom))))
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
	
    private static Function translate(ObjectPropertyExpression property) {
		final Variable varX = ofac.getVariable(variableXname);
		final Variable varY = ofac.getVariable(variableYname);

		if (property.isInverse()) 
			return ofac.getFunction(property.getPredicate(), varY, varX);
		else 
			return ofac.getFunction(property.getPredicate(), varX, varY);
	}
    
    private static Function translate(DataPropertyExpression property) {
		final Variable varX = ofac.getVariable(variableXname);
		final Variable varY = ofac.getVariable(variableYname);

		return ofac.getFunction(property.getPredicate(), varX, varY);
	}
	
    private static Function translate(ClassExpression description, String existentialVariableName) {
		final Variable varX = ofac.getVariable(variableXname);
		if (description instanceof OClass) {
			OClass klass = (OClass) description;
			return ofac.getFunction(klass.getPredicate(), varX);
		} 
		else if (description instanceof ObjectSomeValuesFrom) {
			final Variable varY = ofac.getVariable(existentialVariableName);
			ObjectPropertyExpression property = ((ObjectSomeValuesFrom) description).getProperty();
			if (property.isInverse()) 
				return ofac.getFunction(property.getPredicate(), varY, varX);
			else 
				return ofac.getFunction(property.getPredicate(), varX, varY);
		} 
		else {
			assert (description instanceof DataSomeValuesFrom);
			final Variable varY = ofac.getVariable(existentialVariableName);
			DataPropertyExpression property = ((DataSomeValuesFrom) description).getProperty();
			return ofac.getFunction(property.getPredicate(), varX, varY);
		} 
	}

}
