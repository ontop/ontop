package it.unibz.inf.ontop.model.type;

import java.util.List;
import java.util.Optional;

import it.unibz.inf.ontop.model.predicate.DatatypePredicate;
import it.unibz.inf.ontop.model.predicate.Predicate;
import org.eclipse.rdf4j.model.IRI;

import it.unibz.inf.ontop.model.predicate.Predicate.COL_TYPE;

public interface DatatypeFactory {

	@Deprecated
	public COL_TYPE getDatatype(String uri);
	
	public COL_TYPE getDatatype(IRI uri);

	Optional<COL_TYPE> getInternalType(DatatypePredicate predicate);
	
	public IRI getDatatypeURI(COL_TYPE type);

	public DatatypePredicate getTypePredicate(COL_TYPE type);
		
	public boolean isBoolean(Predicate p);
	
	public boolean isInteger(Predicate p);
	
	public boolean isFloat(Predicate p);
	
	public boolean isLiteral(Predicate p);
	
	public boolean isString(Predicate p);
	
	public List<Predicate> getDatatypePredicates();

}
