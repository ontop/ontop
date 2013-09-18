/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.model.impl;

import it.unibz.krdb.obda.model.BNode;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.Constant;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDADataSource;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.OBDAQuery;
import it.unibz.krdb.obda.model.OBDARDBMappingAxiom;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.URIConstant;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.utils.IDGenerator;

import java.net.URI;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

//import com.hp.hpl.jena.iri.IRI;
//import com.hp.hpl.jena.iri.IRIFactory;

public class OBDADataFactoryImpl implements OBDADataFactory {

	private static final long serialVersionUID = 1851116693137470887L;
	
	private static OBDADataFactory instance = null;
	private static ValueFactory irifactory = null;

	private static int counter = 0;
	
	protected OBDADataFactoryImpl() {
		// protected constructor prevents instantiation from other classes.
	}

	public static OBDADataFactory getInstance() {
		if (instance == null) {
			instance = new OBDADataFactoryImpl();
		}
		return instance;
	}
	
	public static ValueFactory getIRIFactory() {
		if (irifactory == null) {
			irifactory = new ValueFactoryImpl();
		}
		return irifactory;
	}

	public static org.openrdf.model.URI getIRI(String s){
		return getIRIFactory().createURI(s);
				}
	
	public OBDAModel getOBDAModel() {
		return new OBDAModelImpl();
	}

	@Deprecated
	public PredicateImpl getPredicate(String name, int arity) {
		if (arity == 1) {
			return new PredicateImpl(name, arity,
					new COL_TYPE[] { COL_TYPE.OBJECT });
		} else {
			return new PredicateImpl(name, arity, null);
		}
	}

	public Predicate getObjectPropertyPredicate(String name) {
		return new PredicateImpl(name, 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.OBJECT });
	}

	public Predicate getDataPropertyPredicate(String name) {
		return new PredicateImpl(name, 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.LITERAL });
	}

	public Predicate getClassPredicate(String name) {
		return new PredicateImpl(name, 1, new COL_TYPE[] { COL_TYPE.OBJECT });
	}
	
	@Override
	@Deprecated
	public URIConstant getConstantURI(String uriString) {
		return new URIConstantImpl(uriString);
	}
	
	@Override
	public ValueConstant getConstantLiteral(String value) {
		return new ValueConstantImpl(value, COL_TYPE.LITERAL);
	}

	@Override
	public ValueConstant getConstantLiteral(String value, COL_TYPE type) {
		return new ValueConstantImpl(value, type);
	}

	@Override
	public ValueConstant getConstantLiteral(String value, String language) {
		return new ValueConstantImpl(value, language.toLowerCase(), COL_TYPE.LITERAL_LANG);
	}
	
	@Override
	public ValueConstant getConstantFreshLiteral() {
		return new ValueConstantImpl("f" + (counter++), COL_TYPE.LITERAL);
	}

	@Override
	public Variable getVariable(String name) {
		return new VariableImpl(name);
	}

	@Override
	public Variable getVariableNondistinguished() {
		return new AnonymousVariable();
	}

	@Override
	public Function getFunction(Predicate functor, Term... arguments) {
		return new FunctionalTermImpl(functor, arguments);
	}
	
	@Override
	public Function getFunction(Predicate functor, List<Term> arguments) {
		return new FunctionalTermImpl(functor, arguments);
	}

	@Override
	public OBDADataSource getDataSource(URI id) {
		return new DataSourceImpl(id);
	}

	@Override
	public CQIE getCQIE(Function head, Function... body) {
		return new CQIEImpl(head, body);
	}
	
	@Override
	public CQIE getCQIE(Function head, List<Function> body) {
		return new CQIEImpl(head, body);
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
		String id = new String(IDGenerator.getNextUniqueID("MAPID-"));
		return getRDBMSMappingAxiom(id, sql, targetQuery);
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
	public Function getUriTemplate(Term... terms) {
		Predicate uriPred = getUriTemplatePredicate(terms.length);
		return getFunction(uriPred, terms);		
	}

	@Override
	public Predicate getBNodeTemplatePredicate(int arity) {
		return new BNodePredicateImpl(arity);
	}

	@Override
	public Function getFunctionEQ(Term firstTerm, Term secondTerm) {
		return getFunction(OBDAVocabulary.EQ, firstTerm, secondTerm);
	}

	@Override
	public Function getFunctionGTE(Term firstTerm, Term secondTerm) {
		return getFunction(OBDAVocabulary.GTE, firstTerm, secondTerm);
	}

	@Override
	public Function getFunctionGT(Term firstTerm, Term secondTerm) {
		return getFunction(OBDAVocabulary.GT, firstTerm, secondTerm);
	}

	@Override
	public Function getFunctionLTE(Term firstTerm, Term secondTerm) {
		return getFunction(OBDAVocabulary.LTE, firstTerm, secondTerm);
	}

	@Override
	public Function getFunctionLT(Term firstTerm, Term secondTerm) {
		return getFunction(OBDAVocabulary.LT, firstTerm, secondTerm);
	}

	@Override
	public Function getFunctionNEQ(Term firstTerm, Term secondTerm) {
		return getFunction(OBDAVocabulary.NEQ, firstTerm, secondTerm);
	}

	@Override
	public Function getFunctionNOT(Term term) {
		return getFunction(OBDAVocabulary.NOT, term);
	}

	@Override
	public Function getFunctionAND(Term term1, Term term2) {
		return getFunction(OBDAVocabulary.AND, term1, term2);
	}

//	@Override
//	public Function getANDFunction(List<Term> terms) {
//		if (terms.size() < 2) {
//			throw new IllegalArgumentException("AND requires at least 2 terms");
//		}
//		LinkedList<Term> auxTerms = new LinkedList<Term>();
//
//		if (terms.size() == 2) {
//			return getFunctionalTerm(OBDAVocabulary.AND, terms.get(0), terms.get(1));
//		}
//		Term nested = getFunctionalTerm(OBDAVocabulary.AND, terms.get(0), terms.get(1));
//		terms.remove(0);
//		terms.remove(0);
//		while (auxTerms.size() > 1) {
//			nested = getFunctionalTerm(OBDAVocabulary.AND, nested, terms.get(0));
//			terms.remove(0);
//		}
//		return getFunctionalTerm(OBDAVocabulary.AND, nested, terms.get(0));
//	}

	@Override
	public Function getFunctionOR(Term term1, Term term2) {
		return getFunction(OBDAVocabulary.OR, term1, term2);
	}

	
//	@Override
//	public Function getORFunction(List<Term> terms) {
//		if (terms.size() < 2) {
//			throw new IllegalArgumentException("OR requires at least 2 terms");
//		}
//		LinkedList<Term> auxTerms = new LinkedList<Term>();
//
//		if (terms.size() == 2) {
//			return getFunctionalTerm(OBDAVocabulary.OR, terms.get(0), terms.get(1));
//		}
//		Term nested = getFunctionalTerm(OBDAVocabulary.OR, terms.get(0), terms.get(1));
//		terms.remove(0);
//		terms.remove(0);
//		while (auxTerms.size() > 1) {
//			nested = getFunctionalTerm(OBDAVocabulary.OR, nested, terms.get(0));
//			terms.remove(0);
//		}
//		return getFunctionalTerm(OBDAVocabulary.OR, nested, terms.get(0));
//	}

	@Override
	public Function getFunctionIsNull(Term term) {
		return getFunction(OBDAVocabulary.IS_NULL, term);
	}

	@Override
	public Function getFunctionIsNotNull(Term term) {
		return getFunction(OBDAVocabulary.IS_NOT_NULL, term);
	}


	@Override
	public Predicate getJoinPredicate() {
		return OBDAVocabulary.SPARQL_JOIN;
	}
	
	@Override
	public Predicate getLeftJoinPredicate() {
		return OBDAVocabulary.SPARQL_LEFTJOIN;
	}
	
	@Override
	public Function getLANGMATCHESFunction(Term term1, Term term2) {
		return getFunction(OBDAVocabulary.SPARQL_LANGMATCHES, term1, term2);
	}

	@Override
	public Function getFunctionMinus(Term term1) {
		return getFunction(OBDAVocabulary.MINUS, term1);
	}

	@Override
	public Function getFunctionAdd(Term term1, Term term2) {
		return getFunction(OBDAVocabulary.ADD, term1, term2);
	}

	@Override
	public Function getFunctionSubstract(Term term1, Term term2) {
		return getFunction(OBDAVocabulary.SUBSTRACT, term1, term2);
	}

	@Override
	public Function getFunctionMultiply(Term term1, Term term2) {
		return getFunction(OBDAVocabulary.MULTIPLY, term1, term2);
	}

	@Override
	public OBDADataSource getJDBCDataSource(String jdbcurl, String username, 
			String password, String driverclass) {
		URI id = URI.create(UUID.randomUUID().toString());
		return getJDBCDataSource(id.toString(), jdbcurl, username, password, driverclass);
	}

	@Override
	public OBDADataSource getJDBCDataSource(String sourceuri, String jdbcurl, 
			String username, String password, String driverclass) {
		DataSourceImpl source = new DataSourceImpl(URI.create(sourceuri));
		source.setParameter(RDBMSourceParameterConstants.DATABASE_URL, jdbcurl);
		source.setParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD, password);
		source.setParameter(RDBMSourceParameterConstants.DATABASE_USERNAME, username);
		source.setParameter(RDBMSourceParameterConstants.DATABASE_DRIVER, driverclass);
		return source;
	}

	
	@Override
	public Predicate getPredicate(String uri, int arity, COL_TYPE[] types) {
		return new PredicateImpl(uri, arity, types);
	}

	@Override
	public BNode getConstantBNode(String name) {
		return new BNodeConstantImpl(name);
	}

	@Override
	public Constant getConstantNULL() {
		return OBDAVocabulary.NULL;
	}

	@Override
	public Constant getConstantTrue() {
		return OBDAVocabulary.TRUE;
	}

	@Override
	public Constant getConstantFalse() {
		return OBDAVocabulary.FALSE;
	}

	@Override
	public Predicate getDataTypePredicateUnsupported(String uri) {
		return getDataTypePredicateUnsupported(uri);
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
