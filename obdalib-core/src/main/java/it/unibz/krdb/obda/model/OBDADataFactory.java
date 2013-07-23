package it.unibz.krdb.obda.model;

import it.unibz.krdb.obda.model.Predicate.COL_TYPE;

import java.io.Serializable;
import java.net.URI;
import java.util.Collection;
import java.util.List;

public interface OBDADataFactory extends Serializable {

	public OBDAModel getOBDAModel();

	public Atom getAtom(Predicate predicate, List<NewLiteral> terms);

	public Atom getAtom(Predicate predicate, NewLiteral term1);

	public Atom getAtom(Predicate predicate, NewLiteral term1, NewLiteral term2);

	public CQIE getCQIE(Function head, List<Function> body);

	public CQIE getCQIE(Function head, Function body);
	
	public OBDADataSource getDataSource(URI id);

	public DatalogProgram getDatalogProgram();

	public DatalogProgram getDatalogProgram(CQIE rule);

	public DatalogProgram getDatalogProgram(Collection<CQIE> rules);

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
	public Predicate getPredicate(String uri, int arity);


	public Predicate getPredicate(String uri, int arity, COL_TYPE[] types);

	public Predicate getObjectPropertyPredicate(String name);

	public Predicate getDataPropertyPredicate(String name);

	public Predicate getClassPredicate(String name);


	/*
	 * Data types
	 */

	public Predicate getDataTypePredicateUnsupported(String uri);

	public Predicate getDataTypePredicateLiteral();

	public Predicate getDataTypePredicateLiteralLang();

	public Predicate getDataTypePredicateString();

	public Predicate getDataTypePredicateInteger();

	public Predicate getDataTypePredicateDecimal();

	public Predicate getDataTypePredicateDouble();

	public Predicate getDataTypePredicateDateTime();

	public Predicate getDataTypePredicateBoolean();

	/*
	 * Built-in function predicates
	 */

	public Predicate getUriTemplatePredicate(int arity);
	
	public Function getUriTemplate(NewLiteral...terms);

	public Predicate getBNodeTemplatePredicate(int arity);

	/*
	 * Boolean atoms
	 */

	public Atom getEQAtom(NewLiteral firstTerm, NewLiteral secondTerm);

	public Atom getGTEAtom(NewLiteral firstTerm, NewLiteral secondTerm);

	public Atom getGTAtom(NewLiteral firstTerm, NewLiteral secondTerm);

	public Atom getLTEAtom(NewLiteral firstTerm, NewLiteral secondTerm);

	public Atom getLTAtom(NewLiteral firstTerm, NewLiteral secondTerm);

	public Atom getNEQAtom(NewLiteral firstTerm, NewLiteral secondTerm);

	public Atom getNOTAtom(NewLiteral term);

	public Atom getANDAtom(NewLiteral term1, NewLiteral term2);

	public Atom getANDAtom(NewLiteral term1, NewLiteral term2, NewLiteral term3);

	public Atom getANDAtom(List<NewLiteral> terms);

	public Atom getORAtom(NewLiteral term1, NewLiteral term2);

	public Atom getORAtom(NewLiteral term1, NewLiteral term2, NewLiteral term3);

	public Atom getORAtom(List<NewLiteral> terms);

	public Atom getIsNullAtom(NewLiteral term);

	public Atom getIsNotNullAtom(NewLiteral term);

	/*
	 * Boolean function terms
	 */

	public Function getEQFunction(NewLiteral firstTerm, NewLiteral secondTerm);

	public Function getGTEFunction(NewLiteral firstTerm, NewLiteral secondTerm);

	public Function getGTFunction(NewLiteral firstTerm, NewLiteral secondTerm);

	public Function getLTEFunction(NewLiteral firstTerm, NewLiteral secondTerm);

	public Function getLTFunction(NewLiteral firstTerm, NewLiteral secondTerm);

	public Function getNEQFunction(NewLiteral firstTerm, NewLiteral secondTerm);

	public Function getNOTFunction(NewLiteral term);

	public Function getANDFunction(NewLiteral term1, NewLiteral term2);

	public Function getANDFunction(NewLiteral term1, NewLiteral term2, NewLiteral term3);

	public Function getANDFunction(List<NewLiteral> terms);

	public Function getORFunction(NewLiteral term1, NewLiteral term2);

	public Function getORFunction(NewLiteral term1, NewLiteral term2, NewLiteral term3);

	public Function getORFunction(List<NewLiteral> terms);

	public Function getIsNullFunction(NewLiteral term);

	public Function getIsNotNullFunction(NewLiteral term);

	public Function getLANGMATCHESFunction(NewLiteral term1, NewLiteral term2);

	/*
	 * Numerical operation functions
	 */

	public Function getMinusFunction(NewLiteral term1);

	public Function getAddFunction(NewLiteral term1, NewLiteral term2);

	public Function getSubstractFunction(NewLiteral term1, NewLiteral term2);

	public Function getMultiplyFunction(NewLiteral term1, NewLiteral term2);

	/*
	 * JDBC objects
	 */

	public OBDADataSource getJDBCDataSource(String jdbcurl, String username,
			String password, String driverclass);

	public OBDADataSource getJDBCDataSource(String sourceuri, String jdbcurl,
			String username, String password, String driverclass);

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
	public URIConstant getURIConstant(String uri);
	
//	public URIConstant getURIConstant(IRI uri);

	public BNode getBNodeConstant(String name);

	public Constant getNULL();

	public Constant getTrue();

	public Constant getFalse();

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
	 * Construct a {@link ValueConstant} object with a language tag.
	 * <p>
	 * Example:
	 * <p>
	 * <code>
	 * "This is American English"@en-US <br />
	 * </code>
	 * 
	 * @param value
	 *            the value of the constant.
	 * @param language
	 *            the language tag for the constant.
	 * @return the value constant.
	 */
	public ValueConstant getValueConstant(String value, String language);

	/**
	 * Construct a {@link ValueConstant} object with a system-assigned name
	 * that is automatically generated.
	 * 
	 * @return the value constant.
	 */
	public ValueConstant getFreshValueConstant();
	
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
	public Function getFunctionalTerm(Predicate functor, NewLiteral... terms);

	public Function getFunctionalTerm(Predicate functor, List<NewLiteral> terms);

	public Function getFunctionalTerm(Predicate functor, NewLiteral term1);

	public Function getFunctionalTerm(Predicate functor, NewLiteral term1, NewLiteral term2);

	public OBDARDBMappingAxiom getRDBMSMappingAxiom(String id, OBDAQuery sourceQuery, OBDAQuery targetQuery);

	public OBDARDBMappingAxiom getRDBMSMappingAxiom(String id, String sql, OBDAQuery targetQuery);

	public OBDARDBMappingAxiom getRDBMSMappingAxiom(String sql, OBDAQuery targetQuery);

	public OBDASQLQuery getSQLQuery(String query);

	public Predicate getTypePredicate(Predicate.COL_TYPE type);

	Predicate getJoinPredicate();

	Predicate getLeftJoinPredicate();
}
