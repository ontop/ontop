package inf.unibz.it.obda.model;

import java.net.URI;
import java.util.List;

import com.sun.msv.datatype.xsd.XSDatatype;

public interface OBDADataFactory {

	public OBDAModel getOBDAModel();
	
	public Atom getAtom(Predicate predicate, List<Term> terms);
	
	public Atom getAtom(Predicate predicate, Term term1);
	
	public Atom getAtom(Predicate predicate, Term term1, Term term2);
	
	public CQIE getCQIE(Atom head, List<Atom> body);
	
	public CQIE getCQIE(Atom head, Atom body);
	
	public DataSource getDataSource(URI id);
	
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
	public Predicate createPredicate(URI name, int arity);

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
	public abstract URIConstant createURIConstant(URI uri);

	/**
	 * Construct a {@link ValueConstant} object.
	 * 
	 * @param value
	 *            the value of the constant.
	 * @return the value constant.
	 */
	public abstract ValueConstant createValueConstant(String value);

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
	public abstract ValueConstant createValueConstant(String value, XSDatatype type);

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
	public abstract Variable createVariable(String name);

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
	public abstract Variable createVariable(String name, XSDatatype type);

	/**
	 * Construct a {@link Variable} object with empty name.
	 * 
	 * @return the variable object.
	 */
	public abstract Variable createUndistinguishedVariable();

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
	public abstract Function createFunctionalTerm(Predicate functor, List<Term> terms);
	
	public abstract Function createFunctionalTerm(Predicate functor, Term term1);
	
	public abstract Function createFunctionalTerm(Predicate functor, Term term1, Term term2);

}
