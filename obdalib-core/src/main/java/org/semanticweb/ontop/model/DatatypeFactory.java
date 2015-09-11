package org.semanticweb.ontop.model;


import org.openrdf.model.URI;


import org.semanticweb.ontop.model.Predicate.COL_TYPE;

import java.util.List;

public interface DatatypeFactory {

	@Deprecated
	public COL_TYPE getDatatype(String uri);
	
	public COL_TYPE getDatatype(URI uri);
	
	public URI getDatatypeURI(COL_TYPE type);

	
	public Predicate getTypePredicate(COL_TYPE type);
	
		
	public boolean isBoolean(Predicate p);
	
	public boolean isInteger(Predicate p);
	
	public boolean isFloat(Predicate p);
	
	public boolean isLiteral(Predicate p);
	
	public boolean isString(Predicate p);

	
	
	public List<Predicate> getDatatypePredicates();

}
