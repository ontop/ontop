package it.unibz.krdb.obda.model;

import java.util.List;

import it.unibz.krdb.obda.model.Predicate.COL_TYPE;

public interface DatatypeFactory {

	
	public COL_TYPE getDataType(String uri);
	
	public String getDataTypeURI(COL_TYPE type);

	
	public Predicate getTypePredicate(COL_TYPE type);
	
		
	public boolean isBoolean(Predicate p);
	
	public boolean isInteger(Predicate p);
	
	public boolean isFloat(Predicate p);
	
	public boolean isLiteral(Predicate p);
	
	public boolean isString(Predicate p);

	
	
	public List<Predicate> getDatatypePredicates();

}
