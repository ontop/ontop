package it.unibz.krdb.obda.model.impl;

import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDADataSource;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.OBDAQuery;
import it.unibz.krdb.obda.model.OBDARDBMappingAxiom;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.URIConstant;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.Variable;

import java.net.URI;
import java.security.InvalidParameterException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import com.sun.msv.datatype.xsd.XSDatatype;

public class OBDADataFactoryImpl implements OBDADataFactory {

	private static OBDADataFactoryImpl	instance	= null;

	protected OBDADataFactoryImpl() {
		// protected constructor prevents instantiation from other classes.
	}

	public static OBDADataFactory getInstance() {
		if (instance == null) {
			instance = new OBDADataFactoryImpl();
		}
		return instance;
	}

	public OBDAModel getOBDAModel() {
		return new OBDAModelImpl();
	}

	@Deprecated
	public PredicateImpl getPredicate(URI name, int arity) {
		if (arity == 1) {
			return new PredicateImpl(name, arity, new COL_TYPE[] {COL_TYPE.OBJECT});
		} else {
			return new PredicateImpl(name, arity, null);
		}
	}

	public PredicateImpl getPredicate(URI name, int arity, COL_TYPE[] types) {
		return new PredicateImpl(name, arity, types);
	}
	
	public Predicate getObjectPropertyPredicate(URI name) {
		return new PredicateImpl(name, 2, new COL_TYPE[] {COL_TYPE.OBJECT, COL_TYPE.OBJECT});
	}

	
	public Predicate getDataPropertyPredicate(URI name) {
		return new PredicateImpl(name, 2, new COL_TYPE[] {COL_TYPE.OBJECT, COL_TYPE.LITERAL});
	}

	
	public Predicate getClassPredicate(URI name) {
		return new PredicateImpl(name, 1, new COL_TYPE[] {COL_TYPE.OBJECT});
	}

	@Override
	public URIConstant getURIConstant(URI uri) {
		return new URIConstantImpl(uri);
	}

	@Override
	public ValueConstant getValueConstant(String value) {
		return new ValueConstantImpl(value, null);
	}

	@Override
	public ValueConstant getValueConstant(String value, XSDatatype type) {
		return new ValueConstantImpl(value, type);
	}

	@Override
	public Variable getVariable(String name) {
		return new VariableImpl(name, null);
	}

	@Override
	public Variable getVariable(String name, XSDatatype type) {
		return new VariableImpl(name, type);
	}

	@Override
	public Variable getNondistinguishedVariable() {
		return new AnonymousVariable();
	}

	@Override
	public Function getFunctionalTerm(Predicate functor, List<Term> arguments) {
		return new FunctionalTermImpl(functor, arguments);
	}

	@Override
	public Function getFunctionalTerm(Predicate functor, Term term1) {
		return new FunctionalTermImpl(functor, Collections.singletonList(term1));
	}

	@Override
	public Function getFunctionalTerm(Predicate functor, Term term1, Term term2) {
		LinkedList<Term> terms = new LinkedList<Term>();
		terms.add(term1);
		terms.add(term2);
		return new FunctionalTermImpl(functor, terms);
	}

	@Override
	public OBDADataSource getDataSource(URI id) {
		return new DataSourceImpl(id);
	}

	@Override
	public Atom getAtom(Predicate predicate, List<Term> terms) {
		return new PredicateAtomImpl(predicate, terms);
	}

	@Override
	public Atom getAtom(Predicate predicate, Term term1) {
		return new PredicateAtomImpl(predicate, Collections.singletonList(term1));
	}

	@Override
	public Atom getAtom(Predicate predicate, Term term1, Term term2) {
		LinkedList<Term> terms = new LinkedList<Term>();
		terms.add(term1);
		terms.add(term2);
		return new PredicateAtomImpl(predicate, terms);
	}

	@Override
	public CQIE getCQIE(Atom head, List<Atom> body) {
		return new CQIEImpl(head, body);
	}

	@Override
	public CQIE getCQIE(Atom head, Atom body) {
		return new CQIEImpl(head, Collections.singletonList(body));
	}

	@Override
	public DatalogProgram getDatalogProgram() {
		return new DatalogProgramImpl();
	}

	@Override
	public DatalogProgram getDatalogProgram(CQIE rule) {
		DatalogProgram p = new DatalogProgramImpl();
		p.appendRule(rule);
		return p;
	}

	@Override
	public DatalogProgram getDatalogProgram(List<CQIE> rules) {
		DatalogProgram p = new DatalogProgramImpl();
		p.appendRule(rules);
		return p;
	}

	@Override
	public RDBMSMappingAxiomImpl getRDBMSMappingAxiom(String id, OBDAQuery sourceQuery, OBDAQuery targetQuery) {
		return new RDBMSMappingAxiomImpl(id, sourceQuery, targetQuery);
	}

	@Override
	public SQLQueryImpl getSQLQuery(String query) {
		return new SQLQueryImpl(query);
	}

	@Override
	public OBDARDBMappingAxiom getRDBMSMappingAxiom(String id, String sql, OBDAQuery targetQuery) {
		return new RDBMSMappingAxiomImpl(id, new SQLQueryImpl(sql), targetQuery);
	}

	@Override
	public OBDARDBMappingAxiom getRDBMSMappingAxiom(String sql, OBDAQuery targetQuery) {
		String id = new String("MAP" + System.nanoTime());
		return getRDBMSMappingAxiom(id, sql, targetQuery);
	}

	@Override
	public Atom getEQAtom(Term firstTerm, Term secondTerm) {

		return getAtom(OBDAVocabulary.EQ, firstTerm, secondTerm);
	}

	@Override
	public Atom getGTEAtom(Term firstTerm, Term secondTerm) {

		return getAtom(OBDAVocabulary.GTE, firstTerm, secondTerm);
	}

	@Override
	public Atom getGTAtom(Term firstTerm, Term secondTerm) {

		return getAtom(OBDAVocabulary.GT, firstTerm, secondTerm);
	}

	@Override
	public Atom getLTEAtom(Term firstTerm, Term secondTerm) {

		return getAtom(OBDAVocabulary.LTE, firstTerm, secondTerm);
	}

	@Override
	public Atom getLTAtom(Term firstTerm, Term secondTerm) {
		return getAtom(OBDAVocabulary.LT, firstTerm, secondTerm);
	}

	@Override
	public Atom getNEQAtom(Term firstTerm, Term secondTerm) {
		return getAtom(OBDAVocabulary.NEQ, firstTerm, secondTerm);
	}

	@Override
	public Atom getNOTAtom(Term term) {
		return getAtom(OBDAVocabulary.NOT, term);
	}

	@Override
	public Atom getANDAtom(Term term1, Term term2) {
		return getAtom(OBDAVocabulary.AND, term1, term2);
	}

	@Override
	public Atom getANDAtom(Term term1, Term term2, Term term3) {
		List<Term> terms = new LinkedList<Term>();
		terms.add(term1);
		terms.add(term2);
		terms.add(term3);
		return getANDAtom(terms);
	}

	@Override
	public Atom getANDAtom(List<Term> terms) {
		if (terms.size() < 2)
			throw new InvalidParameterException("AND requires at least 2 terms");

		LinkedList<Term> auxTerms = new LinkedList<Term>();

		if (terms.size() == 2)
			return getAtom(OBDAVocabulary.AND, terms.get(0), terms.get(1));

		Term nested = getFunctionalTerm(OBDAVocabulary.AND, terms.get(0), terms.get(1));
		terms.remove(0);
		terms.remove(0);
		while (auxTerms.size() > 1) {
			nested = getFunctionalTerm(OBDAVocabulary.AND, nested, terms.get(0));
			terms.remove(0);
		}
		return getAtom(OBDAVocabulary.AND, nested, terms.get(0));
	}

	@Override
	public Atom getORAtom(Term term1, Term term2) {
		return getAtom(OBDAVocabulary.OR, term1, term2);
	}

	@Override
	public Atom getORAtom(Term term1, Term term2, Term term3) {
		List<Term> terms = new LinkedList<Term>();
		terms.add(term1);
		terms.add(term2);
		terms.add(term3);
		return getANDAtom(terms);
	}

	@Override
	public Atom getORAtom(List<Term> terms) {
		if (terms.size() < 2)
			throw new InvalidParameterException("OR requires at least 2 terms");

		LinkedList<Term> auxTerms = new LinkedList<Term>();

		if (terms.size() == 2)
			return getAtom(OBDAVocabulary.OR, terms.get(0), terms.get(1));

		Term nested = getFunctionalTerm(OBDAVocabulary.OR, terms.get(0), terms.get(1));
		terms.remove(0);
		terms.remove(0);
		while (auxTerms.size() > 1) {
			nested = getFunctionalTerm(OBDAVocabulary.OR, nested, terms.get(0));
			terms.remove(0);
		}
		return getAtom(OBDAVocabulary.OR, nested, terms.get(0));
	}

	@Override
	public Function getEQFunction(Term firstTerm, Term secondTerm) {
		return getFunctionalTerm(OBDAVocabulary.EQ, firstTerm, secondTerm);
	}

	@Override
	public Function getGTEFunction(Term firstTerm, Term secondTerm) {
		return getFunctionalTerm(OBDAVocabulary.GTE, firstTerm, secondTerm);
	}

	@Override
	public Function getGTFunction(Term firstTerm, Term secondTerm) {
		return getFunctionalTerm(OBDAVocabulary.GT, firstTerm, secondTerm);
	}

	@Override
	public Function getLTEFunction(Term firstTerm, Term secondTerm) {
		return getFunctionalTerm(OBDAVocabulary.LTE, firstTerm, secondTerm);
	}

	@Override
	public Function getLTFunction(Term firstTerm, Term secondTerm) {
		return getFunctionalTerm(OBDAVocabulary.LT, firstTerm, secondTerm);
	}

	@Override
	public Function getNEQFunction(Term firstTerm, Term secondTerm) {
		return getFunctionalTerm(OBDAVocabulary.NEQ, firstTerm, secondTerm);
	}

	@Override
	public Function getNOTFunction(Term term) {
		return getFunctionalTerm(OBDAVocabulary.NOT, term);
	}

	@Override
	public Function getANDFunction(Term term1, Term term2) {
		return getFunctionalTerm(OBDAVocabulary.AND, term1, term2);
	}

	@Override
	public Function getANDFunction(Term term1, Term term2, Term term3) {
		List<Term> terms = new LinkedList<Term>();
		terms.add(term1);
		terms.add(term2);
		terms.add(term3);
		return getANDFunction(terms);
	}

	@Override
	public Function getANDFunction(List<Term> terms) {
		if (terms.size() < 2)
			throw new InvalidParameterException("AND requires at least 2 terms");

		LinkedList<Term> auxTerms = new LinkedList<Term>();

		if (terms.size() == 2)
			return getFunctionalTerm(OBDAVocabulary.AND, terms.get(0), terms.get(1));

		Term nested = getFunctionalTerm(OBDAVocabulary.AND, terms.get(0), terms.get(1));
		terms.remove(0);
		terms.remove(0);
		while (auxTerms.size() > 1) {
			nested = getFunctionalTerm(OBDAVocabulary.AND, nested, terms.get(0));
			terms.remove(0);
		}
		return getFunctionalTerm(OBDAVocabulary.AND, nested, terms.get(0));
	}

	@Override
	public Function getORFunction(Term term1, Term term2) {
		return getFunctionalTerm(OBDAVocabulary.OR, term1, term2);
	}

	@Override
	public Function getORFunction(Term term1, Term term2, Term term3) {
		List<Term> terms = new LinkedList<Term>();
		terms.add(term1);
		terms.add(term2);
		terms.add(term3);
		return getANDFunction(terms);
	}

	@Override
	public Function getORFunction(List<Term> terms) {
		if (terms.size() < 2)
			throw new InvalidParameterException("OR requires at least 2 terms");

		LinkedList<Term> auxTerms = new LinkedList<Term>();

		if (terms.size() == 2)
			return getFunctionalTerm(OBDAVocabulary.OR, terms.get(0), terms.get(1));

		Term nested = getFunctionalTerm(OBDAVocabulary.OR, terms.get(0), terms.get(1));
		terms.remove(0);
		terms.remove(0);
		while (auxTerms.size() > 1) {
			nested = getFunctionalTerm(OBDAVocabulary.OR, nested, terms.get(0));
			terms.remove(0);
		}
		return getFunctionalTerm(OBDAVocabulary.OR, nested, terms.get(0));
	}

	@Override
	public OBDADataSource getJDBCDataSource(String jdbcurl, String username, String password, String driverclass) {
		URI id = URI.create(UUID.randomUUID().toString());
		return getJDBCDataSource(id.toString(), jdbcurl, username, password, driverclass);
	}

	@Override
	public OBDADataSource getJDBCDataSource(String sourceuri, String jdbcurl, String username, String password, String driverclass) {
		DataSourceImpl source = new DataSourceImpl(URI.create(sourceuri));
		source.setParameter(RDBMSourceParameterConstants.DATABASE_URL, jdbcurl);
		source.setParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD, password);
		source.setParameter(RDBMSourceParameterConstants.DATABASE_USERNAME, username);
		source.setParameter(RDBMSourceParameterConstants.DATABASE_DRIVER, driverclass);
		return source;

	}

	@Override
	@Deprecated
	public Predicate getPredicate(String uri, int arity) {
		return getPredicate(URI.create(uri), arity);
	}

	@Override
	public Predicate getPredicate(String uri, int arity, COL_TYPE[] types) {
		if (uri == OBDAVocabulary.strEQ) {
			return OBDAVocabulary.EQ;
		} else if (uri == OBDAVocabulary.strLT) {
			return OBDAVocabulary.LT;
		} else if (uri == OBDAVocabulary.strLTE) {
			return OBDAVocabulary.LTE;
		} else if (uri == OBDAVocabulary.strGT) {
			return OBDAVocabulary.GT;
		} else if (uri == OBDAVocabulary.strGTE) {
			return OBDAVocabulary.GTE;
		} else if (uri == OBDAVocabulary.strAND) {
			return OBDAVocabulary.AND;
		} else if (uri == OBDAVocabulary.strOR) {
			return OBDAVocabulary.OR;
		} else if (uri == OBDAVocabulary.strNEQ) {
			return OBDAVocabulary.NEQ;
		}
		return getPredicate(URI.create(uri), arity, types);
	}

	@Override
	public Predicate getObjectPropertyPredicate(String name) {
		return getObjectPropertyPredicate(URI.create(name));
	}

	@Override
	public Predicate getDataPropertyPredicate(String name) {
		return getDataPropertyPredicate(URI.create(name));

	}

	@Override
	public Predicate getClassPredicate(String name) {
		return getClassPredicate(URI.create(name));

	}

}
