package it.unibz.krdb.obda.model.impl;

import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.BNode;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.Constant;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.NewLiteral;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDADataSource;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.OBDAQuery;
import it.unibz.krdb.obda.model.OBDARDBMappingAxiom;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.URIConstant;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.utils.IDGenerator;

import java.net.URI;
import java.security.InvalidParameterException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import com.sun.msv.datatype.xsd.XSDatatype;

public class OBDADataFactoryImpl implements OBDADataFactory {

	private static final long serialVersionUID = 1851116693137470887L;
	private static OBDADataFactory instance = null;
	
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
			return new PredicateImpl(name, arity,
					new COL_TYPE[] { COL_TYPE.OBJECT });
		} else {
			return new PredicateImpl(name, arity, null);
		}
	}

	public PredicateImpl getPredicate(URI name, int arity, COL_TYPE[] types) {
		return new PredicateImpl(name, arity, types);
	}

	public Predicate getObjectPropertyPredicate(URI name) {
		return new PredicateImpl(name, 2, new COL_TYPE[] { COL_TYPE.OBJECT,
				COL_TYPE.OBJECT });
	}

	public Predicate getDataPropertyPredicate(URI name) {
		return new PredicateImpl(name, 2, new COL_TYPE[] { COL_TYPE.OBJECT,
				COL_TYPE.LITERAL });
	}

	public Predicate getClassPredicate(URI name) {
		return new PredicateImpl(name, 1, new COL_TYPE[] { COL_TYPE.OBJECT });
	}

	@Override
	public URIConstant getURIConstant(URI uri) {
		return new URIConstantImpl(uri);
	}

	@Override
	public ValueConstant getValueConstant(String value) {
		return new ValueConstantImpl(value, COL_TYPE.LITERAL);
	}

	@Override
	public ValueConstant getValueConstant(String value, COL_TYPE type) {
		return new ValueConstantImpl(value, type);
	}

	@Override
	public ValueConstant getValueConstant(String value, String language) {
		return new ValueConstantImpl(value, language.toLowerCase(), COL_TYPE.LITERAL_LANG);
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
	public Function getFunctionalTerm(Predicate functor,
			NewLiteral... arguments) {
		return new FunctionalTermImpl(functor, arguments);
	}
	
	@Override
	public Function getFunctionalTerm(Predicate functor,
			List<NewLiteral> arguments) {
		return new FunctionalTermImpl(functor, arguments);
	}

	@Override
	public Function getFunctionalTerm(Predicate functor, NewLiteral term1) {
		return new FunctionalTermImpl(functor, Collections.singletonList(term1));
	}

	@Override
	public Function getFunctionalTerm(Predicate functor, NewLiteral term1,
			NewLiteral term2) {
		LinkedList<NewLiteral> terms = new LinkedList<NewLiteral>();
		terms.add(term1);
		terms.add(term2);
		return new FunctionalTermImpl(functor, terms);
	}

	@Override
	public OBDADataSource getDataSource(URI id) {
		return new DataSourceImpl(id);
	}

	@Override
	public Atom getAtom(Predicate predicate, List<NewLiteral> terms) {
		// return new PredicateAtomImpl(predicate, terms);
		return getFunctionalTerm(predicate, terms).asAtom();
	}

	@Override
	public Atom getAtom(Predicate predicate, NewLiteral term1) {
		return getAtom(predicate, Collections.singletonList(term1));
	}

	@Override
	public Atom getAtom(Predicate predicate, NewLiteral term1, NewLiteral term2) {
		LinkedList<NewLiteral> terms = new LinkedList<NewLiteral>();
		terms.add(term1);
		terms.add(term2);
		return getAtom(predicate, terms);
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
	public DatalogProgram getDatalogProgram(Collection<CQIE> rules) {
		DatalogProgram p = new DatalogProgramImpl();
		p.appendRule(rules);
		return p;
	}

	@Override
	public RDBMSMappingAxiomImpl getRDBMSMappingAxiom(String id,
			OBDAQuery sourceQuery, OBDAQuery targetQuery) {
		return new RDBMSMappingAxiomImpl(id, sourceQuery, targetQuery);
	}

	@Override
	public SQLQueryImpl getSQLQuery(String query) {
		return new SQLQueryImpl(query);
	}

	@Override
	public OBDARDBMappingAxiom getRDBMSMappingAxiom(String id, String sql,
			OBDAQuery targetQuery) {
		return new RDBMSMappingAxiomImpl(id, new SQLQueryImpl(sql), targetQuery);
	}

	@Override
	public OBDARDBMappingAxiom getRDBMSMappingAxiom(String sql, OBDAQuery targetQuery) {
		String id = new String(IDGenerator.getNextUniqueID("MAPID-"));
		return getRDBMSMappingAxiom(id, sql, targetQuery);
	}

	@Override
	public Atom getEQAtom(NewLiteral firstTerm, NewLiteral secondTerm) {

		return getAtom(OBDAVocabulary.EQ, firstTerm, secondTerm);
	}

	@Override
	public Atom getGTEAtom(NewLiteral firstTerm, NewLiteral secondTerm) {

		return getAtom(OBDAVocabulary.GTE, firstTerm, secondTerm);
	}

	@Override
	public Atom getGTAtom(NewLiteral firstTerm, NewLiteral secondTerm) {

		return getAtom(OBDAVocabulary.GT, firstTerm, secondTerm);
	}

	@Override
	public Atom getLTEAtom(NewLiteral firstTerm, NewLiteral secondTerm) {

		return getAtom(OBDAVocabulary.LTE, firstTerm, secondTerm);
	}

	@Override
	public Atom getLTAtom(NewLiteral firstTerm, NewLiteral secondTerm) {
		return getAtom(OBDAVocabulary.LT, firstTerm, secondTerm);
	}

	@Override
	public Atom getNEQAtom(NewLiteral firstTerm, NewLiteral secondTerm) {
		return getAtom(OBDAVocabulary.NEQ, firstTerm, secondTerm);
	}

	@Override
	public Atom getNOTAtom(NewLiteral term) {
		return getAtom(OBDAVocabulary.NOT, term);
	}

	@Override
	public Atom getANDAtom(NewLiteral term1, NewLiteral term2) {
		return getAtom(OBDAVocabulary.AND, term1, term2);
	}

	@Override
	public Atom getANDAtom(NewLiteral term1, NewLiteral term2, NewLiteral term3) {
		List<NewLiteral> terms = new LinkedList<NewLiteral>();
		terms.add(term1);
		terms.add(term2);
		terms.add(term3);
		return getANDAtom(terms);
	}

	@Override
	public Atom getANDAtom(List<NewLiteral> terms) {
		if (terms.size() < 2)
			throw new InvalidParameterException("AND requires at least 2 terms");

		LinkedList<NewLiteral> auxTerms = new LinkedList<NewLiteral>();

		if (terms.size() == 2)
			return getAtom(OBDAVocabulary.AND, terms.get(0), terms.get(1));

		NewLiteral nested = getFunctionalTerm(OBDAVocabulary.AND, terms.get(0),
				terms.get(1));
		terms.remove(0);
		terms.remove(0);
		while (auxTerms.size() > 1) {
			nested = getFunctionalTerm(OBDAVocabulary.AND, nested, terms.get(0));
			terms.remove(0);
		}
		return getAtom(OBDAVocabulary.AND, nested, terms.get(0));
	}

	@Override
	public Atom getORAtom(NewLiteral term1, NewLiteral term2) {
		return getAtom(OBDAVocabulary.OR, term1, term2);
	}

	@Override
	public Atom getORAtom(NewLiteral term1, NewLiteral term2, NewLiteral term3) {
		List<NewLiteral> terms = new LinkedList<NewLiteral>();
		terms.add(term1);
		terms.add(term2);
		terms.add(term3);
		return getANDAtom(terms);
	}

	@Override
	public Atom getORAtom(List<NewLiteral> terms) {
		if (terms.size() < 2)
			throw new InvalidParameterException("OR requires at least 2 terms");

		LinkedList<NewLiteral> auxTerms = new LinkedList<NewLiteral>();

		if (terms.size() == 2)
			return getAtom(OBDAVocabulary.OR, terms.get(0), terms.get(1));

		NewLiteral nested = getFunctionalTerm(OBDAVocabulary.OR, terms.get(0),
				terms.get(1));
		terms.remove(0);
		terms.remove(0);
		while (auxTerms.size() > 1) {
			nested = getFunctionalTerm(OBDAVocabulary.OR, nested, terms.get(0));
			terms.remove(0);
		}
		return getAtom(OBDAVocabulary.OR, nested, terms.get(0));
	}

	@Override
	public Atom getIsNullAtom(NewLiteral term) {
		return getAtom(OBDAVocabulary.IS_NULL, term);
	}

	@Override
	public Atom getIsNotNullAtom(NewLiteral term) {
		return getAtom(OBDAVocabulary.IS_NOT_NULL, term);
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

	@Override
	public Predicate getDataTypePredicateLiteral() {
		return OBDAVocabulary.RDFS_LITERAL;
	}
	
	@Override
	public Predicate getDataTypePredicateLiteralLang() {
		return OBDAVocabulary.RDFS_LITERAL_LANG;
	}

	@Override
	public Predicate getDataTypePredicateString() {
		return OBDAVocabulary.XSD_STRING;
	}

	@Override
	public Predicate getDataTypePredicateInteger() {
		return OBDAVocabulary.XSD_INTEGER;
	}

	@Override
	public Predicate getDataTypePredicateDecimal() {
		return OBDAVocabulary.XSD_DECIMAL;
	}

	@Override
	public Predicate getDataTypePredicateDouble() {
		return OBDAVocabulary.XSD_DOUBLE;
	}

	@Override
	public Predicate getDataTypePredicateDateTime() {
		return OBDAVocabulary.XSD_DATETIME;
	}

	@Override
	public Predicate getDataTypePredicateBoolean() {
		return OBDAVocabulary.XSD_BOOLEAN;
	}

	@Override
	public Predicate getUriTemplatePredicate(int arity) {
		return new URITemplatePredicateImpl(arity);
	}

	@Override
	public Predicate getBNodeTemplatePredicate(int arity) {
		return new BNodePredicateImpl(arity);
	}

	@Override
	public Function getEQFunction(NewLiteral firstTerm, NewLiteral secondTerm) {
		return getFunctionalTerm(OBDAVocabulary.EQ, firstTerm, secondTerm);
	}

	@Override
	public Function getGTEFunction(NewLiteral firstTerm, NewLiteral secondTerm) {
		return getFunctionalTerm(OBDAVocabulary.GTE, firstTerm, secondTerm);
	}

	@Override
	public Function getGTFunction(NewLiteral firstTerm, NewLiteral secondTerm) {
		return getFunctionalTerm(OBDAVocabulary.GT, firstTerm, secondTerm);
	}

	@Override
	public Function getLTEFunction(NewLiteral firstTerm, NewLiteral secondTerm) {
		return getFunctionalTerm(OBDAVocabulary.LTE, firstTerm, secondTerm);
	}

	@Override
	public Function getLTFunction(NewLiteral firstTerm, NewLiteral secondTerm) {
		return getFunctionalTerm(OBDAVocabulary.LT, firstTerm, secondTerm);
	}

	@Override
	public Function getNEQFunction(NewLiteral firstTerm, NewLiteral secondTerm) {
		return getFunctionalTerm(OBDAVocabulary.NEQ, firstTerm, secondTerm);
	}

	@Override
	public Function getNOTFunction(NewLiteral term) {
		return getFunctionalTerm(OBDAVocabulary.NOT, term);
	}

	@Override
	public Function getANDFunction(NewLiteral term1, NewLiteral term2) {
		return getFunctionalTerm(OBDAVocabulary.AND, term1, term2);
	}

	@Override
	public Function getANDFunction(NewLiteral term1, NewLiteral term2,
			NewLiteral term3) {
		List<NewLiteral> terms = new LinkedList<NewLiteral>();
		terms.add(term1);
		terms.add(term2);
		terms.add(term3);
		return getANDFunction(terms);
	}

	@Override
	public Function getANDFunction(List<NewLiteral> terms) {
		if (terms.size() < 2)
			throw new InvalidParameterException("AND requires at least 2 terms");

		LinkedList<NewLiteral> auxTerms = new LinkedList<NewLiteral>();

		if (terms.size() == 2)
			return getFunctionalTerm(OBDAVocabulary.AND, terms.get(0),
					terms.get(1));

		NewLiteral nested = getFunctionalTerm(OBDAVocabulary.AND, terms.get(0),
				terms.get(1));
		terms.remove(0);
		terms.remove(0);
		while (auxTerms.size() > 1) {
			nested = getFunctionalTerm(OBDAVocabulary.AND, nested, terms.get(0));
			terms.remove(0);
		}
		return getFunctionalTerm(OBDAVocabulary.AND, nested, terms.get(0));
	}

	@Override
	public Function getORFunction(NewLiteral term1, NewLiteral term2) {
		return getFunctionalTerm(OBDAVocabulary.OR, term1, term2);
	}

	@Override
	public Function getORFunction(NewLiteral term1, NewLiteral term2,
			NewLiteral term3) {
		List<NewLiteral> terms = new LinkedList<NewLiteral>();
		terms.add(term1);
		terms.add(term2);
		terms.add(term3);
		return getANDFunction(terms);
	}

	@Override
	public Function getORFunction(List<NewLiteral> terms) {
		if (terms.size() < 2)
			throw new InvalidParameterException("OR requires at least 2 terms");

		LinkedList<NewLiteral> auxTerms = new LinkedList<NewLiteral>();

		if (terms.size() == 2)
			return getFunctionalTerm(OBDAVocabulary.OR, terms.get(0),
					terms.get(1));

		NewLiteral nested = getFunctionalTerm(OBDAVocabulary.OR, terms.get(0),
				terms.get(1));
		terms.remove(0);
		terms.remove(0);
		while (auxTerms.size() > 1) {
			nested = getFunctionalTerm(OBDAVocabulary.OR, nested, terms.get(0));
			terms.remove(0);
		}
		return getFunctionalTerm(OBDAVocabulary.OR, nested, terms.get(0));
	}

	@Override
	public Function getIsNullFunction(NewLiteral term) {
		return getFunctionalTerm(OBDAVocabulary.IS_NULL, term);
	}

	@Override
	public Function getIsNotNullFunction(NewLiteral term) {
		return getFunctionalTerm(OBDAVocabulary.IS_NOT_NULL, term);
	}

	@Override
	public Function getLANGMATCHESFunction(NewLiteral term1, NewLiteral term2) {
		return getFunctionalTerm(OBDAVocabulary.SPARQL_LANGMATCHES, term1,
				term2);
	}

	@Override
	public Function getMinusFunction(NewLiteral term1) {
		return getFunctionalTerm(OBDAVocabulary.MINUS, term1);
	}

	@Override
	public Function getAddFunction(NewLiteral term1, NewLiteral term2) {
		return getFunctionalTerm(OBDAVocabulary.ADD, term1, term2);
	}

	@Override
	public Function getSubstractFunction(NewLiteral term1, NewLiteral term2) {
		return getFunctionalTerm(OBDAVocabulary.SUBSTRACT, term1, term2);
	}

	@Override
	public Function getMultiplyFunction(NewLiteral term1, NewLiteral term2) {
		return getFunctionalTerm(OBDAVocabulary.MULTIPLY, term1, term2);
	}

	@Override
	public OBDADataSource getJDBCDataSource(String jdbcurl, String username,
			String password, String driverclass) {
		URI id = URI.create(UUID.randomUUID().toString());
		return getJDBCDataSource(id.toString(), jdbcurl, username, password,
				driverclass);
	}

	@Override
	public OBDADataSource getJDBCDataSource(String sourceuri, String jdbcurl,
			String username, String password, String driverclass) {
		DataSourceImpl source = new DataSourceImpl(URI.create(sourceuri));
		source.setParameter(RDBMSourceParameterConstants.DATABASE_URL, jdbcurl);
		source.setParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD,
				password);
		source.setParameter(RDBMSourceParameterConstants.DATABASE_USERNAME,
				username);
		source.setParameter(RDBMSourceParameterConstants.DATABASE_DRIVER,
				driverclass);
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
	public BNode getBNodeConstant(String name) {
		return new BNodeConstantImpl(name);
	}

	@Override
	public Constant getNULL() {
		return OBDAVocabulary.NULL;
	}

	@Override
	public Constant getTrue() {
		return OBDAVocabulary.TRUE;
	}

	@Override
	public Constant getFalse() {
		return OBDAVocabulary.FALSE;
	}

	@Override
	public Predicate getDataTypePredicateUnsupported(String uri) {
		return getDataTypePredicateUnsupported(URI.create(uri));

	}

	@Override
	public Predicate getDataTypePredicateUnsupported(URI uri) {
		return new DataTypePredicateImpl(uri, COL_TYPE.UNSUPPORTED);

	}

	@Override
	public Predicate getTypePredicate(Predicate.COL_TYPE type) {
		switch (type) {
		case LITERAL:
			return getDataTypePredicateLiteral();
		case LITERAL_LANG:
			return getDataTypePredicateLiteral();
		case STRING:
			return getDataTypePredicateString();
		case INTEGER:
			return getDataTypePredicateInteger();
		case DECIMAL:
			return getDataTypePredicateDecimal();
		case DOUBLE:
			return getDataTypePredicateDouble();
		case DATETIME:
			return getDataTypePredicateDateTime();
		case BOOLEAN:
			return getDataTypePredicateBoolean();
		case OBJECT:
			return getUriTemplatePredicate(1);
		case BNODE:
			return getBNodeTemplatePredicate(1);
		default:
			throw new RuntimeException("Cannot get URI for unsupported type: " + type);
		}
	}
}
