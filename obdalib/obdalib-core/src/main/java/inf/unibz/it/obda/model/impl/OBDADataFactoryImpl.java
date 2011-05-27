package inf.unibz.it.obda.model.impl;

import inf.unibz.it.obda.model.Function;
import inf.unibz.it.obda.model.OBDADataFactory;
import inf.unibz.it.obda.model.Predicate;
import inf.unibz.it.obda.model.Term;
import inf.unibz.it.obda.model.URIConstant;
import inf.unibz.it.obda.model.ValueConstant;
import inf.unibz.it.obda.model.Variable;

import java.net.URI;
import java.util.List;

import com.sun.msv.datatype.xsd.XSDatatype;


public class OBDADataFactoryImpl  implements OBDADataFactory {

	private static OBDADataFactoryImpl instance = null;

	protected OBDADataFactoryImpl(){
		// protected constructor prevents instantiation from other classes.
	}

	public static OBDADataFactory getInstance(){
		if(instance == null){
			instance = new OBDADataFactoryImpl();
		}
		return instance;
	}

	public PredicateImp createPredicate(URI name, int arity) {
		return new PredicateImp(name, arity);
	}
	
	@Override
	public URIConstant createURIConstant(URI uri) {
		return new URIConstantImpl(uri);
	}

	@Override
	public ValueConstant createValueConstant(String value) {
		return new ValueConstantImpl(value, null);
	}

	@Override
	public ValueConstant createValueConstant(String value, XSDatatype type) {
		return new ValueConstantImpl(value, type);
	}

	@Override
	public Variable createVariable(String name) {
		return new VariableImpl(name, null);
	}

	@Override
	public Variable createVariable(String name, XSDatatype type) {
		return new VariableImpl(name, type);
	}

	@Override
	public Variable createUndistinguishedVariable() {
		return new UndistinguishedVariable();
	}

	@Override
	public Function createFunctionalTerm(Predicate functor, List<Term> arguments){
		return new FunctionalTermImpl(functor, arguments);
	}

}
