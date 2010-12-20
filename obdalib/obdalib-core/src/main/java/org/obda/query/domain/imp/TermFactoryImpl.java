package org.obda.query.domain.imp;

import java.net.URI;
import java.util.List;

import org.obda.query.domain.FunctionSymbol;
import org.obda.query.domain.Term;
import org.obda.query.domain.TermFactory;
import org.obda.query.domain.ValueConstant;

import com.sun.msv.datatype.xsd.XSDatatype;

public class TermFactoryImpl extends TermFactory{

	private int identifier = 1;

	@Override
	public Term createObjectConstant(FunctionSymbol functor, List<ValueConstant> terms) {

		return new ObjectConstantImpl(functor, terms);
	}

	@Override
	public Term createValueConstant(String name) {

		return new ValueConstantImpl(name, identifier++, null);
	}

	@Override
	public Term createValueConstant(String name, XSDatatype type) {
		return new ValueConstantImpl(name, identifier++, type);
	}

	@Override
	public Term createVariable(String name) {
		return new VariableImpl(name, identifier++, null);
	}

	@Override
	public Term createVariable(String name, XSDatatype type) {
		return new VariableImpl(name, identifier++, type);
	}

	@Override
	public FunctionSymbol getFunctionSymbol(String name) {
		return new FunctionSymbolImpl(name, identifier++);
	}

	@Override
	public Term createObjectTerm(FunctionSymbol fs, List<Term> vars){
		return new ObjectVariableImpl(fs, vars);
	}

	@Override
	public Term createURIConstant(URI uri) {
		return new URIConstantImpl(uri);
	}

}
