package it.unibz.krdb.obda.model;

import it.unibz.krdb.obda.model.Predicate.COL_TYPE;

import java.io.Serializable;
import java.net.URI;
import java.util.List;

import com.sun.msv.datatype.xsd.XSDatatype;

public interface OBDADataFactory extends Serializable {

	public OBDAModel getOBDAModel();

	public Atom getAtom(Predicate predicate, List<Term> terms);

	public Atom getAtom(Predicate predicate, Term term1);

	public Atom getAtom(Predicate predicate, Term term1, Term term2);

	public CQIE getCQIE(Atom head, List<Atom> body);

	public CQIE getCQIE(Atom head, Atom body);

	public OBDADataSource getDataSource(URI id);

	public DatalogProgram getDatalogProgram();

	public DatalogProgram getDatalogProgram(CQIE rule);

	public DatalogProgram getDatalogProgram(List<CQIE> rules);

	/**
	 * Construct a {@link Predicate} object.
	 * 
	 * @param name
	 *            the name of the predicate (defined as a URI).
	 * @param arity
	 *            the number of elements inside the predicate.
	 * @return a predicate object.
	 */
	@Deprecated
	public Predicate getPredicate(URI name, int arity);
	
	@Deprecated
	public Predicate getPredicate(String uri, int arity);
	
	public Predicate getPredicate(URI name, int arity, COL_TYPE[] types);
	
	public Predicate getPredicate(String uri, int arity, COL_TYPE[] types);
	
	public Predicate getObjectPropertyPredicate(URI name);
	
	public Predicate getObjectPropertyPredicate(String name);
	
	public Predicate getDataPropertyPredicate(URI name);
	
	public Predicate getDataPropertyPredicate(String name);
	
	public Predicate getClassPredicate(String  name);
	
	public Predicate getClassPredicate(URI name);

	/*
	 * Data types
	 */
	
	public Predicate getDataTypePredicateLiteral(URI name);
	
	public Predicate getDataTypePredicateString(URI name);
	
	public Predicate getDataTypePredicateInteger(URI name);
	
	public Predicate getDataTypePredicateDouble(URI name);
	
	public Predicate getDataTypePredicateDate(URI name);
	
	public Predicate getDataTypePredicateBoolean(URI name);
	
	/*
	 * Boolean atoms
	 */

	public Atom getEQAtom(Term firstTerm, Term secondTerm);

	public Atom getGTEAtom(Term firstTerm, Term secondTerm);

	public Atom getGTAtom(Term firstTerm, Term secondTerm);

	public Atom getLTEAtom(Term firstTerm, Term secondTerm);

	public Atom getLTAtom(Term firstTerm, Term secondTerm);

	public Atom getNEQAtom(Term firstTerm, Term secondTerm);

	public Atom getNOTAtom(Term term);

	public Atom getANDAtom(Term term1, Term term2);

	public Atom getANDAtom(Term term1, Term term2, Term term3);

	public Atom getANDAtom(List<Term> terms);

	public Atom getORAtom(Term term1, Term term2);

	public Atom getORAtom(Term term1, Term term2, Term term3);

	public Atom getORAtom(List<Term> terms);

	/*
	 * Boolean fuctional terms
	 */
	
	/*
	 * Boolean atoms
	 */

	public Function getEQFunction(Term firstTerm, Term secondTerm);

	public Function getGTEFunction(Term firstTerm, Term secondTerm);

	public Function getGTFunction(Term firstTerm, Term secondTerm);

	public Function getLTEFunction(Term firstTerm, Term secondTerm);

	public Function getLTFunction(Term firstTerm, Term secondTerm);

	public Function getNEQFunction(Term firstTerm, Term secondTerm);

	public Function getNOTFunction(Term term);

	public Function getANDFunction(Term term1, Term term2);

	public Function getANDFunction(Term term1, Term term2, Term term3);

	public Function getANDFunction(List<Term> terms);

	public Function getORFunction(Term term1, Term term2);

	public Function getORFunction(Term term1, Term term2, Term term3);

	public Function getORFunction(List<Term> terms);
	
	public OBDADataSource getJDBCDataSource(String jdbcurl, String username, String password, String driverclass);
	
	public OBDADataSource getJDBCDataSource(String sourceuri, String jdbcurl, String username, String password, String driverclass);

	/**
	 * Construct a {@link URIConstant} object. This type of term is written as a
	 * usual URI construction following the generic URI syntax specification
	 * (RFC 3986).
	 * <p>
	 * <code>
	 * scheme://host:port/path#fragment
	 * </code>
	 * <p>
	 * Examples:
	 * <p>
	 * <code>
	 * http://example.org/some/paths <br />
	 * http://example.org/some/paths/to/resource#frag01 <br />
	 * ftp://example.org/resource.txt <br />
	 * </code>
	 * <p>
	 * are all well-formed URI strings.
	 * 
	 * @param uri
	 *            the URI.
	 * @return a URI constant.
	 */
	public URIConstant getURIConstant(URI uri);

	/**
	 * Construct a {@link ValueConstant} object.
	 * 
	 * @param value
	 *            the value of the constant.
	 * @return the value constant.
	 */
	public ValueConstant getValueConstant(String value);

	/**
	 * Construct a {@link ValueConstant} object with a type definition.
	 * <p>
	 * Example:
	 * <p>
	 * <code>
	 * "Person"^^xsd:String <br />
	 * 22^^xsd:Integer
	 * </code>
	 * 
	 * @param value
	 *            the value of the constant.
	 * @param type
	 *            the type of the constant.
	 * @return the value constant.
	 */
	public ValueConstant getValueConstant(String value, Predicate.COL_TYPE type);

	/**
	 * Construct a {@link Variable} object. The variable name is started by a
	 * dollar sign ('$') or a question mark sign ('?'), e.g.:
	 * <p>
	 * <code>
	 * pred($x) <br />
	 * func(?x, ?y)
	 * </code>
	 * 
	 * @param name
	 *            the name of the variable.
	 * @return the variable object.
	 */
	public Variable getVariable(String name);

	/**
	 * Construct a {@link Variable} object with a type definition. The variable
	 * name is started by a dollar sign ('$') or a question mark sign ('?'),
	 * e.g.:
	 * <p>
	 * <code>
	 * pred($x) <br />
	 * func(?x, ?y)
	 * </code>
	 * 
	 * @param name
	 *            the name of the variable.
	 * @return the variable object.
	 */
	public Variable getVariable(String name, XSDatatype type);

	/**
	 * Construct a {@link Variable} object with empty name.
	 * 
	 * @return the variable object.
	 */
	public Variable getNondistinguishedVariable();

	/**
	 * Construct a {@link Function} object. A function expression consists of
	 * functional symbol (or functor) and one or more arguments.
	 * 
	 * @param functor
	 *            the function symbol name.
	 * @param arguments
	 *            a list of arguments.
	 * @return the function object.
	 */
	public Function getFunctionalTerm(Predicate functor, List<Term> terms);

	public Function getFunctionalTerm(Predicate functor, Term term1);

	public Function getFunctionalTerm(Predicate functor, Term term1, Term term2);

	public OBDARDBMappingAxiom getRDBMSMappingAxiom(String id, OBDAQuery sourceQuery, OBDAQuery targetQuery);

	public OBDARDBMappingAxiom getRDBMSMappingAxiom(String id, String sql, OBDAQuery targetQuery);

	public OBDARDBMappingAxiom getRDBMSMappingAxiom(String sql, OBDAQuery targetQuery);

	public OBDASQLQuery getSQLQuery(String query);

}
