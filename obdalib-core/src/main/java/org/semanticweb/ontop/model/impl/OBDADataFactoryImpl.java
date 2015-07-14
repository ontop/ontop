package org.semanticweb.ontop.model.impl;

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



import java.net.URI;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;


import com.google.common.collect.ImmutableList;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.model.Predicate.COL_TYPE;
import org.semanticweb.ontop.utils.IDGenerator;
import org.semanticweb.ontop.utils.JdbcTypeMapper;

public class OBDADataFactoryImpl implements OBDADataFactory {

	private static final long serialVersionUID = 1851116693137470887L;
	
	private static OBDADataFactory instance = null;
	private static ValueFactory irifactory = null;
	private DatatypeFactoryImpl datatypes = null;
	private final JdbcTypeMapper jdbcTypeMapper =  new JdbcTypeMapper();
	

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
	
	@Override
	public DatatypeFactory getDatatypeFactory() {
		if (datatypes == null) {
			datatypes = new DatatypeFactoryImpl();
		}
		return datatypes;
	}

	
	@Override 
	public JdbcTypeMapper getJdbcTypeMapper() {
		return jdbcTypeMapper;
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
	public Function getTypedTerm(Term value, COL_TYPE type) {
		Predicate pred = getDatatypeFactory().getTypePredicate(type);
		if (pred == null)
			throw new RuntimeException("Unknown data type!");
		
		return getFunction(pred, value);
	}
	
	@Override
	public ValueConstant getConstantLiteral(String value, String language) {
		return new ValueConstantImpl(value, language.toLowerCase());
	}

	@Override
	public Function getTypedTerm(Term value, Term language) {
		Predicate pred = getDatatypeFactory().getTypePredicate(COL_TYPE.LITERAL_LANG);
		return getFunction(pred, value, language);
	}

	@Override
	public Function getTypedTerm(Term value, String language) {
		Term lang = getConstantLiteral(language.toLowerCase(), COL_TYPE.LITERAL);		
		Predicate pred = getDatatypeFactory().getTypePredicate(COL_TYPE.LITERAL_LANG);
		return getFunction(pred, value, lang);
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
		if (functor instanceof BooleanOperationPredicate) {
			return getBooleanExpression((BooleanOperationPredicate)functor, arguments);
		}

		// Default constructor
		return new FunctionalTermImpl(functor, arguments);
	}

	@Override
	public BooleanExpression getBooleanExpression(BooleanOperationPredicate functor, Term... arguments) {
		return new BooleanExpressionImpl(functor, arguments);
	}

	@Override
	public BooleanExpression getBooleanExpression(BooleanOperationPredicate functor, List<Term> arguments) {
		return new BooleanExpressionImpl(functor, arguments);
	}

	@Override
	public ImmutableBooleanExpression getImmutableBooleanExpression(BooleanOperationPredicate functor, ImmutableTerm... arguments) {
		return new ImmutableBooleanExpressionImpl(functor, arguments);
	}

	@Override
	public ImmutableBooleanExpression getImmutableBooleanExpression(BooleanOperationPredicate functor,
																	ImmutableList<ImmutableTerm> arguments) {
		return new ImmutableBooleanExpressionImpl(functor, arguments);
	}

	@Override
	public Function getFunction(Predicate functor, List<Term> arguments) {
		if (functor instanceof BooleanOperationPredicate) {
			return getBooleanExpression((BooleanOperationPredicate) functor, arguments);
		}

		// Default constructor
		return new FunctionalTermImpl(functor, arguments);
	}

	@Override
	public ImmutableFunctionalTerm getImmutableFunctionalTerm(Predicate functor, ImmutableList<ImmutableTerm> terms) {
		if (functor instanceof BooleanOperationPredicate) {
			return getImmutableBooleanExpression((BooleanOperationPredicate)functor, terms);
		}

		// Default constructor
		return new ImmutableFunctionalTermImpl(functor, terms);
	}

	@Override
	public ImmutableFunctionalTerm getImmutableFunctionalTerm(Predicate functor, ImmutableTerm... terms) {
		if (functor instanceof BooleanOperationPredicate) {
			return getImmutableBooleanExpression((BooleanOperationPredicate)functor, terms);
		}

		// Default constructor
		return new ImmutableFunctionalTermImpl(functor, terms);
	}

	@Override
	public NonGroundFunctionalTerm getNonGroundFunctionalTerm(Predicate functor, ImmutableTerm... terms) {
		return new NonGroundFunctionalTermImpl(functor, terms);
	}

	@Override
	public NonGroundFunctionalTerm getNonGroundFunctionalTerm(Predicate functor, ImmutableList<ImmutableTerm> terms) {
		return new NonGroundFunctionalTermImpl(functor, terms);
	}

	@Override
	public DataAtom getDataAtom(AtomPredicate predicate, ImmutableList<? extends VariableOrGroundTerm> terms) {
		return new DataAtomImpl(predicate, terms);
	}

	@Override
	public DataAtom getDataAtom(AtomPredicate predicate, VariableOrGroundTerm... terms) {
		return new DataAtomImpl(predicate, terms);
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
	public DatalogProgram getDatalogProgram(OBDAQueryModifiers modifiers) {
		DatalogProgram p = new DatalogProgramImpl();
		p.getQueryModifiers().copy(modifiers);
		return p;
	}
	@Override
	public DatalogProgram getDatalogProgram(Collection<CQIE> rules) {
		DatalogProgram p = new DatalogProgramImpl();
		p.appendRule(rules);
		return p;
	}
	@Override
	public DatalogProgram getDatalogProgram(OBDAQueryModifiers modifiers, Collection<CQIE> rules) {
		DatalogProgram p = new DatalogProgramImpl();
		p.appendRule(rules);
		p.getQueryModifiers().copy(modifiers);
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
	public Function getUriTemplate(Term... terms) {
		Predicate uriPred = new URITemplatePredicateImpl(terms.length);
		return getFunction(uriPred, terms);		
	}
	
	@Override
	public Function getUriTemplate(List<Term> terms) {
		Predicate uriPred = new URITemplatePredicateImpl(terms.size());
		return getFunction(uriPred, terms);		
	}

	@Override
	public Function getUriTemplateForDatatype(String type) {
		return getFunction(new URITemplatePredicateImpl(1), getConstantLiteral(type, COL_TYPE.OBJECT));
	}
	
	@Override
	public Function getBNodeTemplate(Term... terms) {
		Predicate pred = new BNodePredicateImpl(terms.length);
		return getFunction(pred, terms);
	}
	
	@Override
	public Function getBNodeTemplate(List<Term> terms) {
		Predicate pred = new BNodePredicateImpl(terms.size());
		return getFunction(pred, terms);
	}

	@Override
	public BooleanExpression getFunctionEQ(Term firstTerm, Term secondTerm) {
		return getBooleanExpression(OBDAVocabulary.EQ, firstTerm, secondTerm);
	}

	@Override
	public BooleanExpression getFunctionGTE(Term firstTerm, Term secondTerm) {
		return getBooleanExpression(OBDAVocabulary.GTE, firstTerm, secondTerm);
	}

	@Override
	public BooleanExpression getFunctionGT(Term firstTerm, Term secondTerm) {
		return getBooleanExpression(OBDAVocabulary.GT, firstTerm, secondTerm);
	}

	@Override
	public BooleanExpression getFunctionLTE(Term firstTerm, Term secondTerm) {
		return getBooleanExpression(OBDAVocabulary.LTE, firstTerm, secondTerm);
	}

	@Override
	public BooleanExpression getFunctionLT(Term firstTerm, Term secondTerm) {
		return getBooleanExpression(OBDAVocabulary.LT, firstTerm, secondTerm);
	}

	@Override
	public BooleanExpression getFunctionNEQ(Term firstTerm, Term secondTerm) {
		return getBooleanExpression(OBDAVocabulary.NEQ, firstTerm, secondTerm);
	}

	@Override
	public BooleanExpression getFunctionNOT(Term term) {
		return getBooleanExpression(OBDAVocabulary.NOT, term);
	}

	@Override
	public BooleanExpression getFunctionAND(Term term1, Term term2) {
		checkInnerBooleanExpressionTerm(term1);
		checkInnerBooleanExpressionTerm(term2);

		return getBooleanExpression(OBDAVocabulary.AND, term1, term2);
	}

	private static void checkInnerBooleanExpressionTerm(Term term) {
		if (term instanceof Function) {
			Function functionalTerm = (Function) term;

			if (functionalTerm.isBooleanFunction()) {
				return;
			}

			String functionSymbol = functionalTerm.getFunctionSymbol().getName();
			if (functionSymbol.equals(OBDAVocabulary.XSD_BOOLEAN_URI)) {
				return;
			}
		}
		else if (term.equals(OBDAVocabulary.FALSE)
				|| term.equals(OBDAVocabulary.TRUE)) {
			return;
		} else if (term instanceof Variable) {
			return;
		}

		// Illegal argument given to the caller of this
		throw new IllegalArgumentException(term + " is not a boolean expression");
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
	public BooleanExpression getFunctionOR(Term term1, Term term2) {
		return getBooleanExpression(OBDAVocabulary.OR, term1, term2);
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
	public BooleanExpression getFunctionIsNull(Term term) {
		return getBooleanExpression(OBDAVocabulary.IS_NULL, term);
	}

	@Override
	public BooleanExpression getFunctionIsNotNull(Term term) {
		return getBooleanExpression(OBDAVocabulary.IS_NOT_NULL, term);
	}


	@Override
	public BooleanExpression getLANGMATCHESFunction(Term term1, Term term2) {
		return getBooleanExpression(OBDAVocabulary.SPARQL_LANGMATCHES, term1, term2);
	}

	@Override
	public BooleanExpression getFunctionLike(Term term1, Term term2) {
		return getBooleanExpression(OBDAVocabulary.SPARQL_LIKE, term1, term2);
	}
	
	@Override
	public BooleanExpression getFunctionRegex(Term term1, Term term2, Term term3) {
		return getBooleanExpression(OBDAVocabulary.SPARQL_REGEX, term1, term2, term3);
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
		return getFunction(OBDAVocabulary.SUBTRACT, term1, term2);
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
	public BooleanExpression getFunctionIsTrue(Term term) {
		return getBooleanExpression(OBDAVocabulary.IS_TRUE, term);
	}

	@Override
	public Function getSPARQLJoin(Term t1, Term t2) {
		return getFunction(OBDAVocabulary.SPARQL_JOIN, t1, t2);
	}

	@Override
	public Function getSPARQLJoin(Function t1, Function t2, Function joinCondition) {
		return getFunction(OBDAVocabulary.SPARQL_JOIN, t1, t2, joinCondition);
	}

	@Override
	public Function getSPARQLJoin(List<Function> atoms, Function filter) {
		

		int size = atoms.size();
		
		if (size>1){
			Function join = getSPARQLJoin(atoms.get(0), getSPARQLJoin((List<Function>) atoms.subList(1, size)), filter);
			return join;

		}else{
			return atoms.get(0);
		}
	
	}

	
	
	
	
	
	@Override
	public Function getSPARQLJoin(List<Function> atoms) {
		
		int size = atoms.size();
		
		if (size>1){
			List<Function> remainingAtoms = (List<Function>) atoms.subList(1, size);
			Function join = getSPARQLJoin(atoms.get(0), getSPARQLJoin(remainingAtoms)  );
			return join;

		}else{
			return atoms.get(0);
		}
	
		
	}	
	
	
	

	
	
	@Override
	public Function getSPARQLLeftJoin(List<Function> atoms, List<Function> atoms2, Function filter){
		
		atoms2.add(filter);
		
		return  getSPARQLLeftJoin(atoms,atoms2);
		
	}

	@Override
	public Function getSPARQLLeftJoin(List<Function> atoms, List<Function> atoms2){

		List<Term> termList= new LinkedList<Term>();
		atoms.addAll(atoms2);
		
		for (Function f: atoms){
			termList.add(f);
		}
		
		Function function = getFunction(OBDAVocabulary.SPARQL_LEFTJOIN, termList );
		return  function;
		
	}



	
	
	@Override
	public Function getSPARQLLeftJoin(Term t1, Term t2) {
		return getFunction(OBDAVocabulary.SPARQL_LEFTJOIN, t1, t2);
	}

	@Override
	public Function getSPARQLLeftJoin(Function t1, Function t2, Function filter) {
		return getFunction(OBDAVocabulary.SPARQL_LEFTJOIN, t1, t2, filter);
	}

	
	@Override
	public ValueConstant getBooleanConstant(boolean value) {
		return value ? OBDAVocabulary.TRUE : OBDAVocabulary.FALSE;
	}

	@Override
	public Function getTripleAtom(Term subject, Term predicate, Term object) {
		return getFunction(OBDAVocabulary.QUEST_TRIPLE_PRED, subject, predicate, object);
	}

	
}
