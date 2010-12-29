package org.obda.query.domain;

import java.net.URI;
import java.util.List;

import org.obda.query.domain.imp.TermFactoryImpl;

import com.sun.msv.datatype.xsd.XSDatatype;

public abstract class TermFactory {

	// TODO Add create undistinguished variable to this interface and to the
	// implemenation

	private static TermFactory	instance	= null;

	public TermFactory() {
		instance = this;
	}

	public abstract Term createURIConstant(URI uri);

	public abstract Term createObjectConstant(FunctionSymbol functor, List<ValueConstant> terms);

	public abstract Term createValueConstant(String name);

	public abstract Term createValueConstant(String name, XSDatatype type);

	public abstract Term createVariable(String name);

	public abstract Term createVariable(String name, XSDatatype type);

	public abstract FunctionSymbol getFunctionSymbol(String name);

	public abstract Term createObjectTerm(FunctionSymbol fs, List<Term> vars);

	public static TermFactory getInstance() {
		if (instance == null) {
			instance = new TermFactoryImpl();
		}
		return instance;
	}
}
