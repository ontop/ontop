package it.unibz.inf.ontop.model.type;

import it.unibz.inf.ontop.model.term.Term;
import it.unibz.inf.ontop.model.term.functionsymbol.DatatypePredicate;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate.COL_TYPE;
import org.eclipse.rdf4j.model.IRI;

import java.util.List;
import java.util.Optional;

public interface TypeFactory {

	@Deprecated
	Optional<COL_TYPE> getDatatype(String uri);
	
	COL_TYPE getDatatype(IRI uri);

	Optional<COL_TYPE> getInternalType(DatatypePredicate predicate);
	
	IRI getDatatypeURI(COL_TYPE type);

	DatatypePredicate getTypePredicate(COL_TYPE type);
		
	boolean isBoolean(Predicate p);
	
	boolean isInteger(Predicate p);
	
	boolean isFloat(Predicate p);
	
//	boolean isLiteral(Predicate p);
	
	boolean isString(Predicate p);
	
	List<Predicate> getDatatypePredicates();

	TermType getTermType(COL_TYPE type);
	TermType getTermType(String languageTagString);
	TermType getTermType(Term languageTagTerm);

}
