package it.unibz.krdb.obda.ontology.impl;

import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.ObjectConstant;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.ontology.AssertionFactory;
import it.unibz.krdb.obda.ontology.ClassAssertion;
import it.unibz.krdb.obda.ontology.DataPropertyAssertion;
import it.unibz.krdb.obda.ontology.DataPropertyExpression;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.ObjectPropertyAssertion;
import it.unibz.krdb.obda.ontology.ObjectPropertyExpression;
import it.unibz.krdb.obda.ontology.OntologyFactory;

public class AssertionFactoryImpl implements AssertionFactory {

	private static final AssertionFactoryImpl instance = new AssertionFactoryImpl();

	private final OntologyFactory ofac = OntologyFactoryImpl.getInstance();
	private final OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
			
	private AssertionFactoryImpl() {
		// NO-OP to make the default constructor private
	}
	
	public static AssertionFactory getInstance() {
		return instance;
	}
	
	@Override
	public ClassAssertion createClassAssertion(String className, ObjectConstant o) {
		Predicate classp = fac.getClassPredicate(className);
		OClass oc = new ClassImpl(classp);
		return ofac.createClassAssertion(oc, o);
	}

	@Override
	public ObjectPropertyAssertion createObjectPropertyAssertion(String propertyName, ObjectConstant o1, ObjectConstant o2) {
		ObjectPropertyExpression ope = ofac.createObjectProperty(propertyName);
		return ofac.createObjectPropertyAssertion(ope, o1, o2);
	}

	@Override
	public DataPropertyAssertion createDataPropertyAssertion(String propertyName, ObjectConstant o1, ValueConstant o2) {
		Predicate prop = fac.getDataPropertyPredicate(propertyName);
		DataPropertyExpression dpe = new DataPropertyExpressionImpl(prop);
		return ofac.createDataPropertyAssertion(dpe, o1, o2);
	}

}
