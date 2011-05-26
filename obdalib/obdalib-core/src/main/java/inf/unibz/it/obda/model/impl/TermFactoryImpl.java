package inf.unibz.it.obda.model.impl;

import java.net.URI;
import java.util.List;

import org.obda.query.domain.Function;
import org.obda.query.domain.OBDADataFactory;
import org.obda.query.domain.Predicate;
import org.obda.query.domain.Term;
import org.obda.query.domain.URIConstant;
import org.obda.query.domain.ValueConstant;
import org.obda.query.domain.Variable;

import com.sun.msv.datatype.xsd.XSDatatype;

public class TermFactoryImpl implements OBDADataFactory {

	protected static TermFactoryImpl instance = null;

	protected TermFactoryImpl() {
		// protected constructor prevents instantiation from other classes.
	}

	public static TermFactoryImpl getInstance() {
		if (instance == null) {
			instance = new TermFactoryImpl();
		}
		return instance;
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
