package org.obda.query.domain;

import java.util.List;

public interface ObjectConstant extends Constant{

	 public FunctionSymbol getFunctionSymbol() ;
	 public List<ValueConstant> getTerms();
}
