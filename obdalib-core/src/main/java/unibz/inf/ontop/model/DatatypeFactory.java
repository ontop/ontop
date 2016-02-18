package unibz.inf.ontop.model;


import org.openrdf.model.URI;


import java.util.List;

public interface DatatypeFactory {

	@Deprecated
	public Predicate.COL_TYPE getDatatype(String uri);
	
	public Predicate.COL_TYPE getDatatype(URI uri);
	
	public URI getDatatypeURI(Predicate.COL_TYPE type);

	
	public Predicate getTypePredicate(Predicate.COL_TYPE type);
	
		
	public boolean isBoolean(Predicate p);
	
	public boolean isInteger(Predicate p);
	
	public boolean isFloat(Predicate p);
	
	public boolean isLiteral(Predicate p);
	
	public boolean isString(Predicate p);

	
	
	public List<Predicate> getDatatypePredicates();

}
