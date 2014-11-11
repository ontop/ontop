package it.unibz.krdb.obda.model.impl;

/*
 * #%L
 * ontop-obdalib-core
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
import java.util.List;
import java.util.UUID;

import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

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
//		if (arity == 1) {
//			return new PredicateImpl(name, arity, new COL_TYPE[] { COL_TYPE.OBJECT });
//		} else {
			return new PredicateImpl(name, arity, null);
//		}
	}
	
	@Override
	public Predicate getPredicate(String uri, COL_TYPE[] types) {
		return new PredicateImpl(uri, types.length, types);
	}


	public Predicate getObjectPropertyPredicate(String name) {
		return new PredicateImpl(name, 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.OBJECT });
	}

	public Predicate getDataPropertyPredicate(String name) {
		return new PredicateImpl(name, 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.LITERAL }); 
	}
	public Predicate getDataPropertyPredicate(String name, COL_TYPE type) {
		return new PredicateImpl(name, 2, new COL_TYPE[] { COL_TYPE.OBJECT, type }); // COL_TYPE.LITERAL
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
		// TODO: a bit more elaborate name is needed to avoid conflicts
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

	
	
	
	/* Data type predicates */

	public static boolean isLiteralOrLiteralLang(Predicate pred) {
		return (pred == RDFS_LITERAL) || (pred == RDFS_LITERAL_LANG);
	}
	
	// TODO: make this one private
	public static final Predicate RDFS_LITERAL = new DataTypePredicateImpl(
			OBDAVocabulary.RDFS_LITERAL_URI, new COL_TYPE[] { COL_TYPE.LITERAL });

	private static final Predicate RDFS_LITERAL_LANG = new DataTypePredicateImpl(
			OBDAVocabulary.RDFS_LITERAL_URI, new COL_TYPE[] { COL_TYPE.LITERAL, COL_TYPE.LITERAL });

	private static final Predicate XSD_STRING = new DataTypePredicateImpl(
			OBDAVocabulary.XSD_STRING_URI, COL_TYPE.STRING);

	private static final Predicate XSD_INTEGER = new DataTypePredicateImpl(
			OBDAVocabulary.XSD_INTEGER_URI, COL_TYPE.INTEGER);

	private static final Predicate XSD_NEGATIVE_INTEGER = new DataTypePredicateImpl(
    		OBDAVocabulary.XSD_NEGATIVE_INTEGER_URI, COL_TYPE.NEGATIVE_INTEGER);

	private static final Predicate XSD_INT = new DataTypePredicateImpl(
    		OBDAVocabulary.XSD_INT_URI, COL_TYPE.INT);

	private static final Predicate XSD_NON_NEGATIVE_INTEGER = new DataTypePredicateImpl(
    		OBDAVocabulary.XSD_NON_NEGATIVE_INTEGER_URI, COL_TYPE.NON_NEGATIVE_INTEGER);

	private static final Predicate XSD_UNSIGNED_INT = new DataTypePredicateImpl(
    		OBDAVocabulary.XSD_UNSIGNED_INT_URI, COL_TYPE.UNSIGNED_INT);

	private static final Predicate XSD_POSITIVE_INTEGER = new DataTypePredicateImpl(
    		OBDAVocabulary.XSD_POSITIVE_INTEGER_URI, COL_TYPE.POSITIVE_INTEGER);

	private static final Predicate XSD_NON_POSITIVE_INTEGER = new DataTypePredicateImpl(
    		OBDAVocabulary.XSD_NON_POSITIVE_INTEGER_URI, COL_TYPE.NON_POSITIVE_INTEGER);

	private static final Predicate XSD_LONG = new DataTypePredicateImpl(
    		OBDAVocabulary.XSD_LONG_URI, COL_TYPE.LONG);

	private static final Predicate XSD_DECIMAL = new DataTypePredicateImpl(
			OBDAVocabulary.XSD_DECIMAL_URI, COL_TYPE.DECIMAL);

	private static final Predicate XSD_DOUBLE = new DataTypePredicateImpl(
			OBDAVocabulary.XSD_DOUBLE_URI, COL_TYPE.DOUBLE);

	private static final Predicate XSD_FLOAT = new DataTypePredicateImpl(
    		OBDAVocabulary.XSD_FLOAT_URI, COL_TYPE.FLOAT);

	private static final Predicate XSD_DATETIME = new DataTypePredicateImpl(
			OBDAVocabulary.XSD_DATETIME_URI, COL_TYPE.DATETIME);

	private static final Predicate XSD_BOOLEAN = new DataTypePredicateImpl(
			OBDAVocabulary.XSD_BOOLEAN_URI, COL_TYPE.BOOLEAN);

	private static final Predicate XSD_DATE = new DataTypePredicateImpl(
			OBDAVocabulary.XSD_DATE_URI, COL_TYPE.DATE);

	private static final Predicate XSD_TIME = new DataTypePredicateImpl(
			OBDAVocabulary.XSD_TIME_URI, COL_TYPE.TIME);
	
	private static final Predicate XSD_YEAR = new DataTypePredicateImpl(
			OBDAVocabulary.XSD_YEAR_URI, COL_TYPE.YEAR);

	public static final Predicate[] QUEST_DATATYPE_PREDICATES = new Predicate[] {
			RDFS_LITERAL, XSD_STRING, XSD_INTEGER, XSD_NEGATIVE_INTEGER,
    XSD_NON_NEGATIVE_INTEGER, XSD_POSITIVE_INTEGER, XSD_NON_POSITIVE_INTEGER, XSD_INT,
    XSD_UNSIGNED_INT, XSD_LONG, XSD_FLOAT, XSD_DECIMAL, XSD_DOUBLE,
			XSD_DATETIME, XSD_BOOLEAN, XSD_DATE, XSD_TIME, XSD_YEAR };
	
//	public static final Predicate[] QUEST_NUMERICAL_DATATYPES = new Predicate[] {
//			XSD_INTEGER, XSD_NEGATIVE_INTEGER,
//           XSD_NON_NEGATIVE_INTEGER, XSD_POSITIVE_INTEGER, XSD_NON_POSITIVE_INTEGER, XSD_INT,
//            XSD_UNSIGNED_INT, XSD_FLOAT, XSD_DECIMAL, XSD_DOUBLE, XSD_LONG };
	
	
	@Override
	public Predicate getDataTypePredicateLiteral() {
		return RDFS_LITERAL;
	}
	
	@Override
	public Predicate getDataTypePredicateLiteralLang() {
		return RDFS_LITERAL_LANG;
	}

	@Override
	public Predicate getDataTypePredicateString() {
		return XSD_STRING;
	}

	@Override
	public Predicate getDataTypePredicateInteger() {
		return XSD_INTEGER;
	}

    @Override
    public Predicate getDataTypePredicateNonNegativeInteger() {
        return XSD_NON_NEGATIVE_INTEGER;
    }

    @Override
    public Predicate getDataTypePredicateInt() {
        return XSD_INT;
    }

    @Override
    public Predicate getDataTypePredicatePositiveInteger() {
        return XSD_POSITIVE_INTEGER;
    }

    @Override
    public Predicate getDataTypePredicateNegativeInteger() {
        return XSD_NEGATIVE_INTEGER;
    }

    @Override
    public Predicate getDataTypePredicateNonPositiveInteger() {
        return XSD_NON_POSITIVE_INTEGER;
    }

    @Override
    public Predicate getDataTypePredicateUnsignedInt() {
        return XSD_UNSIGNED_INT;
    }

    @Override
    public Predicate getDataTypePredicateLong() {
        return XSD_LONG;
    }

	@Override
	public Predicate getDataTypePredicateDecimal() {
		return XSD_DECIMAL;
	}

	@Override
	public Predicate getDataTypePredicateDouble() {
		return XSD_DOUBLE;
	}

    @Override
    public Predicate getDataTypePredicateFloat() {
        return XSD_FLOAT;
    }

	@Override
	public Predicate getDataTypePredicateDateTime() {
		return XSD_DATETIME;
	}

	@Override
	public Predicate getDataTypePredicateBoolean() {
		return XSD_BOOLEAN;
	}

	@Override
	public Predicate getDataTypePredicateDate() {
		return XSD_DATE;
	}
	
	@Override
	public Predicate getDataTypePredicateYear() {
		return XSD_YEAR;
	}

	@Override
	public Predicate getDataTypePredicateTime() {
		return XSD_TIME;
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
	public Function getFunctionLike(Term term1, Term term2) {
		return getFunction(OBDAVocabulary.SPARQL_LIKE, term1, term2);
	}
	
	@Override
	public Function getFunctionRegex(Term term1, Term term2, Term term3) {
		return getFunction(OBDAVocabulary.SPARQL_REGEX, term1, term2, term3 );
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
	public Function getFunctionCast(Term term1, Term term2) {
		// TODO implement cast function
		return getFunction(OBDAVocabulary.QUEST_CAST, term1, term2);
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
	public Predicate getTypePredicate(Predicate.COL_TYPE type) {
		switch (type) {
		case LITERAL:          // 1
			return getDataTypePredicateLiteral();
		case STRING:   // 2
			return getDataTypePredicateString();
		case INTEGER:  // 3
			return getDataTypePredicateInteger();
        case NEGATIVE_INTEGER:  // 4
            return getDataTypePredicateNegativeInteger();
        case INT:  // 5
            return getDataTypePredicateInt();
        case POSITIVE_INTEGER:  // 6
            return getDataTypePredicatePositiveInteger();
        case NON_POSITIVE_INTEGER:  // 7
            return getDataTypePredicateNonPositiveInteger();
        case NON_NEGATIVE_INTEGER: // 8
            return getDataTypePredicateNonNegativeInteger();
        case UNSIGNED_INT:  // 9
            return getDataTypePredicateUnsignedInt();
        case LONG:   // 10
            return getDataTypePredicateLong();
		case DECIMAL: // 11
			return getDataTypePredicateDecimal();
        case FLOAT:  // 12
            return getDataTypePredicateFloat();
		case DOUBLE:  // 13
			return getDataTypePredicateDouble();
		case DATETIME:  // 14
			return getDataTypePredicateDateTime();
		case BOOLEAN:  // 15
			return getDataTypePredicateBoolean();
		case DATE:   // 16
			return getDataTypePredicateDate();
		case TIME:  // 17
			return getDataTypePredicateTime();
		case YEAR: // 18
			return getDataTypePredicateYear();
		case LITERAL_LANG: // used in ExpressionEvaluator only(?) use proper method here? 
			return getDataTypePredicateLiteral();
		//case OBJECT:   // different uses
		//	return getUriTemplatePredicate(1);
		//case BNODE:    // different uses			
		//	return getBNodeTemplatePredicate(1);
		default:
			return null;
			//throw new RuntimeException("Cannot get URI for unsupported type: " + type);
		}
	}

	

	
}
