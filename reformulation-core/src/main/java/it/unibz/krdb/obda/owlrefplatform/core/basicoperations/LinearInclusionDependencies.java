package it.unibz.krdb.obda.owlrefplatform.core.basicoperations;

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
import it.unibz.krdb.obda.ontology.BasicClassDescription;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.Property;
import it.unibz.krdb.obda.ontology.PropertySomeRestriction;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.Equivalences;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasoner;

public class LinearInclusionDependencies {

    private static final OBDADataFactory ofac = OBDADataFactoryImpl.getInstance();
    
	private final Map<Predicate, List<CQIE>> rules = new HashMap<Predicate, List<CQIE>>();
	
	public List<CQIE> getRules(Predicate pred) {
		return rules.get(pred);
	}
		
	public Map<Predicate, List<CQIE>> getIndex() {
		return rules;
	}
	
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
		
		for (Equivalences<Property> propNode : reasoner.getProperties()) {
			// super might be more efficient
			for (Equivalences<Property> subpropNode : reasoner.getProperties().getSub(propNode)) {
				for (Property subprop : subpropNode) {
					if (subprop.isInverse())
						continue;
					
	                Function body = translate(subprop);

	                for (Property prop : propNode)  {
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

	                Function body = translate(subclass);
                	//if (!(subclass instanceof OClass) && !(subclass instanceof PropertySomeRestriction))
	                if (body == null)
	                	continue;

	                for (BasicClassDescription cla : classNode)  {
	                	if (!(cla instanceof OClass) && !(!full && (cla instanceof PropertySomeRestriction)))
	                		continue;
	                	
	                	if (cla == subclass)
	                		continue;
	                	
		                Function head = translate(cla);	
		                dependencies.addRule(head, body);
					}
				}
			}
		}
		
		return dependencies;
	}

	private static final String variableSuffix = "_4022013";
	
    private static Function translate(Property property) {
		final Variable varX = ofac.getVariable("x" + variableSuffix);
		final Variable varY = ofac.getVariable("y" + variableSuffix);

		if (property.isInverse()) 
			return ofac.getFunction(property.getPredicate(), varY, varX);
		else 
			return ofac.getFunction(property.getPredicate(), varX, varY);
	}
	
    private static Function translate(BasicClassDescription description) {
		final Variable varX = ofac.getVariable("x" + variableSuffix);
		final Variable varY = ofac.getVariable("y" + variableSuffix);
		if (description instanceof OClass) {
			OClass klass = (OClass) description;
			return ofac.getFunction(klass.getPredicate(), varX);
		} 
		else if (description instanceof PropertySomeRestriction) {
			PropertySomeRestriction property = (PropertySomeRestriction) description;
			if (property.isInverse()) 
				return ofac.getFunction(property.getPredicate(), varY, varX);
			else 
				return ofac.getFunction(property.getPredicate(), varX, varY);
		} 
		else 
			return null;  // if datatype then return null
	}

}
