package it.unibz.inf.ontop.model.impl;

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

import com.google.common.base.Preconditions;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.Predicate.COL_TYPE;
import it.unibz.inf.ontop.utils.IDGenerator;
import it.unibz.inf.ontop.utils.JdbcTypeMapper;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;


import com.google.common.base.Preconditions;
import java.net.URI;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;


import com.google.common.collect.ImmutableList;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.Predicate.COL_TYPE;
import it.unibz.inf.ontop.utils.IDGenerator;
import it.unibz.inf.ontop.utils.JdbcTypeMapper;

import static it.unibz.inf.ontop.model.impl.DataAtomTools.areVariablesDistinct;
import static it.unibz.inf.ontop.model.impl.DataAtomTools.isVariableOnly;

public class OBDADataFactoryImpl implements OBDADataFactory {

	private static final long serialVersionUID = 1851116693137470887L;
	
	private static OBDADataFactory instance = null;
	private static ValueFactory irifactory = null;
	private DatatypeFactoryImpl datatypes = null;
	private final JdbcTypeMapper jdbcTypeMapper =  new JdbcTypeMapper(); 
	

	private static int counter = 0;
	
	private OBDADataFactoryImpl() {
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
	public Function getFunction(Predicate functor, Term... arguments) {
		if (functor instanceof OperationPredicate) {
			return getBooleanExpression((OperationPredicate)functor, arguments);
		}

		// Default constructor
		return new FunctionalTermImpl(functor, arguments);
	}
	
	@Override
	public BooleanExpression getBooleanExpression(OperationPredicate functor, Term... arguments) {
		return new BooleanExpressionImpl(functor, arguments);
	}

	@Override
	public BooleanExpression getBooleanExpression(OperationPredicate functor, List<Term> arguments) {
		return new BooleanExpressionImpl(functor, arguments);
	}

	@Override
	public ImmutableBooleanExpression getImmutableBooleanExpression(OperationPredicate functor, ImmutableTerm... arguments) {
		return getImmutableBooleanExpression(functor, ImmutableList.copyOf(arguments));
	}

	@Override
	public ImmutableBooleanExpression getImmutableBooleanExpression(OperationPredicate functor,
																	ImmutableList<? extends ImmutableTerm> arguments) {
		if (GroundTermTools.areGroundTerms(arguments)) {
			return new GroundBooleanExpressionImpl(functor, (ImmutableList<GroundTerm>)arguments);
		}
		else {
			return new NonGroundBooleanExpressionImpl(functor, arguments);
		}
	}

	@Override
	public ImmutableBooleanExpression getImmutableBooleanExpression(BooleanExpression booleanExpression) {
		if (GroundTermTools.isGroundTerm(booleanExpression)) {
			return new GroundBooleanExpressionImpl(booleanExpression);
		}
		else {
			return new NonGroundBooleanExpressionImpl(booleanExpression);
		}
	}

	@Override
	public Function getFunction(Predicate functor, List<Term> arguments) {
		if (functor instanceof OperationPredicate) {
			return getBooleanExpression((OperationPredicate) functor, arguments);
		}

		// Default constructor
		return new FunctionalTermImpl(functor, arguments);
	}

	@Override
	public ImmutableFunctionalTerm getImmutableFunctionalTerm(Predicate functor, ImmutableList<ImmutableTerm> terms) {
		if (functor instanceof OperationPredicate) {
			return getImmutableBooleanExpression((OperationPredicate)functor, terms);
		}

		if (GroundTermTools.areGroundTerms(terms)) {
			return new GroundFunctionalTermImpl(functor, terms);
		}
		else {
			// Default constructor
			return new NonGroundFunctionalTermImpl(functor, terms);
		}
	}

	@Override
	public ImmutableFunctionalTerm getImmutableFunctionalTerm(Predicate functor, ImmutableTerm... terms) {
		return getImmutableFunctionalTerm(functor, ImmutableList.copyOf(terms));
	}

	@Override
	public ImmutableTerm getImmutableFunctionalTerm(Function functionalTerm) {
		if (GroundTermTools.isGroundTerm(functionalTerm)) {
			return new GroundFunctionalTermImpl(functionalTerm);
		}
		else {
			return new NonGroundFunctionalTermImpl(functionalTerm);
		}

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
	public DataAtom getDataAtom(AtomPredicate predicate, ImmutableList<? extends VariableOrGroundTerm> arguments) {
		/**
		 * NB: A GroundDataAtom is a DistinctVariableDataAtom
		 */
		if(areVariablesDistinct(arguments)) {
			return getDistinctVariableDataAtom(predicate, arguments);
		}
		else if (isVariableOnly(arguments)) {
			return new VariableOnlyDataAtomImpl(predicate, (ImmutableList<Variable>)(ImmutableList<?>)arguments);
		}
		else {
			return new NonGroundDataAtomImpl(predicate, arguments);
		}
	}

	@Override
	public DataAtom getDataAtom(AtomPredicate predicate, VariableOrGroundTerm... terms) {
		return getDataAtom(predicate, ImmutableList.copyOf(terms));
	}

	@Override
	public DistinctVariableDataAtom getDistinctVariableDataAtom(AtomPredicate predicate,
																ImmutableList<? extends VariableOrGroundTerm> arguments) {
		if (isVariableOnly(arguments)) {
			return new DistinctVariableOnlyDataAtomImpl(predicate, (ImmutableList<Variable>)(ImmutableList<?>)arguments);
		}
		else if (GroundTermTools.areGroundTerms(arguments)) {
			return new GroundDataAtomImpl(predicate, (ImmutableList<GroundTerm>)(ImmutableList<?>)arguments);
		}
		else {
			return new NonGroundDistinctVariableDataAtomImpl(predicate, arguments);
		}
	}

	@Override
	public DistinctVariableDataAtom getDistinctVariableDataAtom(AtomPredicate predicate, VariableOrGroundTerm... arguments) {
		return getDistinctVariableDataAtom(predicate, ImmutableList.copyOf(arguments));
	}

	@Override
	public DistinctVariableOnlyDataAtom getDistinctVariableOnlyDataAtom(AtomPredicate predicate, ImmutableList<Variable> arguments) {
		return new DistinctVariableOnlyDataAtomImpl(predicate, arguments);
	}

	@Override
	public VariableOnlyDataAtom getVariableOnlyDataAtom(AtomPredicate predicate, Variable... arguments) {
		return getVariableOnlyDataAtom(predicate, ImmutableList.copyOf(arguments));
	}

	@Override
	public VariableOnlyDataAtom getVariableOnlyDataAtom(AtomPredicate predicate, ImmutableList<Variable> arguments) {
		if (areVariablesDistinct(arguments)) {
			return new DistinctVariableOnlyDataAtomImpl(predicate, arguments);
		}
		else {
			return new VariableOnlyDataAtomImpl(predicate, arguments);
		}
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
	public OBDAMappingAxiom getRDBMSMappingAxiom(String id, OBDASQLQuery sourceQuery, List<Function> targetQuery) {
		return new RDBMSMappingAxiomImpl(id, sourceQuery, targetQuery);
	}

	@Override
	public OBDAMappingAxiom getRDBMSMappingAxiom(OBDASQLQuery sourceQuery, List<Function> targetQuery) {
		String id = IDGenerator.getNextUniqueID("MAPID-");
		return getRDBMSMappingAxiom(id, sourceQuery, targetQuery);
	}

	@Override
	public SQLQueryImpl getSQLQuery(String query) {
		return new SQLQueryImpl(query);
	}

	@Override
	public Function getSPARQLJoin(Function t1, Function t2) {
		return getFunction(OBDAVocabulary.SPARQL_JOIN, t1, t2);
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
		return getBooleanExpression(ExpressionOperation.EQ, firstTerm, secondTerm);
	}

	@Override
	public BooleanExpression getFunctionGTE(Term firstTerm, Term secondTerm) {
		return getBooleanExpression(ExpressionOperation.GTE, firstTerm, secondTerm);
	}

	@Override
	public BooleanExpression getFunctionGT(Term firstTerm, Term secondTerm) {
		return getBooleanExpression(ExpressionOperation.GT, firstTerm, secondTerm);
	}

	@Override
	public BooleanExpression getFunctionLTE(Term firstTerm, Term secondTerm) {
		return getBooleanExpression(ExpressionOperation.LTE, firstTerm, secondTerm);
	}

	@Override
	public BooleanExpression getFunctionLT(Term firstTerm, Term secondTerm) {
		return getBooleanExpression(ExpressionOperation.LT, firstTerm, secondTerm);
	}

	@Override
	public BooleanExpression getFunctionNEQ(Term firstTerm, Term secondTerm) {
		return getBooleanExpression(ExpressionOperation.NEQ, firstTerm, secondTerm);
	}

	@Override
	public BooleanExpression getFunctionNOT(Term term) {
		return getBooleanExpression(ExpressionOperation.NOT, term);
	}

	@Override
	public BooleanExpression getFunctionAND(Term term1, Term term2) {
		return getBooleanExpression(ExpressionOperation.AND, term1, term2);
	}

//	@Override
//	public Function getANDFunction(List<Term> terms) {
//		if (terms.size() < 2) {
//			throw new IllegalArgumentException("AND requires at least 2 terms");
//		}
//		LinkedList<Term> auxTerms = new LinkedList<Term>();
//
//		if (terms.size() == 2) {
//			return getFunctionalTerm(ExpressionOperation.AND, terms.get(0), terms.get(1));
//		}
//		Term nested = getFunctionalTerm(ExpressionOperation.AND, terms.get(0), terms.get(1));
//		terms.remove(0);
//		terms.remove(0);
//		while (auxTerms.size() > 1) {
//			nested = getFunctionalTerm(ExpressionOperation.AND, nested, terms.get(0));
//			terms.remove(0);
//		}
//		return getFunctionalTerm(ExpressionOperation.AND, nested, terms.get(0));
//	}

	@Override
	public BooleanExpression getFunctionOR(Term term1, Term term2) {
		return getBooleanExpression(ExpressionOperation.OR,term1, term2);
	}

	
//	@Override
//	public Function getORFunction(List<Term> terms) {
//		if (terms.size() < 2) {
//			throw new IllegalArgumentException("OR requires at least 2 terms");
//		}
//		LinkedList<Term> auxTerms = new LinkedList<Term>();
//
//		if (terms.size() == 2) {
//			return getFunctionalTerm(ExpressionOperation.OR, terms.get(0), terms.get(1));
//		}
//		Term nested = getFunctionalTerm(ExpressionOperation.OR, terms.get(0), terms.get(1));
//		terms.remove(0);
//		terms.remove(0);
//		while (auxTerms.size() > 1) {
//			nested = getFunctionalTerm(ExpressionOperation.OR, nested, terms.get(0));
//			terms.remove(0);
//		}
//		return getFunctionalTerm(ExpressionOperation.OR, nested, terms.get(0));
//	}

	@Override
	public BooleanExpression getFunctionIsNull(Term term) {
		return getBooleanExpression(ExpressionOperation.IS_NULL, term);
	}

	@Override
	public BooleanExpression getFunctionIsNotNull(Term term) {
		return getBooleanExpression(ExpressionOperation.IS_NOT_NULL, term);
	}


	@Override
	public BooleanExpression getLANGMATCHESFunction(Term term1, Term term2) {
		return getBooleanExpression(ExpressionOperation.LANGMATCHES, term1, term2);
	}

	@Override
	public BooleanExpression getSQLFunctionLike(Term term1, Term term2) {
		return getBooleanExpression(ExpressionOperation.SQL_LIKE, term1, term2);
	}
	
	@Override
	public BooleanExpression getFunctionRegex(Term term1, Term term2, Term term3) {
		return getBooleanExpression(ExpressionOperation.REGEX, term1, term2, term3);
	}
	
	@Override
	public Function getFunctionReplace(Term term1, Term term2, Term term3) {
		return getFunction(ExpressionOperation.REPLACE, term1, term2, term3 );
	}
	
    @Override
    public Function getFunctionConcat(Term term1, Term term2) {
        return getFunction(ExpressionOperation.CONCAT, term1, term2);
    }

    @Override
    public Function getFunctionSubstring(Term term1, Term term2, Term term3) {
        return getFunction(ExpressionOperation.SUBSTR, term1, term2, term3);
    } //added by Nika

	@Override
	public Function getFunctionSubstring(Term term1, Term term2) {
		return getFunction(ExpressionOperation.SUBSTR, term1, term2);
	}
        
	@Override
	public Function getFunctionCast(Term term1, Term term2) {
		// TODO implement cast function
		return getFunction(ExpressionOperation.QUEST_CAST, term1, term2);
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
        Preconditions.checkNotNull(sourceuri, "sourceuri is null");
        Preconditions.checkNotNull(jdbcurl, "jdbcurl is null");
        Preconditions.checkNotNull(password, "password is null");
        Preconditions.checkNotNull(username, "username is null");
        Preconditions.checkNotNull(driverclass, "driverclass is null");

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
		return getBooleanExpression(ExpressionOperation.IS_TRUE, term);
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
		return getFunction(PredicateImpl.QUEST_TRIPLE_PRED, subject, predicate, object);
	}

	private int suffix = 0;
	
	/***
	 * Replaces each variable 'v' in the query for a new variable constructed
	 * using the name of the original variable plus the counter. For example
	 * 
	 * <pre>
	 * q(x) :- C(x)
	 * 
	 * results in
	 * 
	 * q(x_1) :- C(x_1)
	 * 
	 * if counter = 1.
	 * </pre>
	 * 
	 * <p>
	 * This method can be used to generate "fresh" rules from a datalog program
	 * so that it can be used during a resolution step.
	 * suffix
	 *            The integer that will be apended to every variable name
	 * @param rule
	 * @return
	 */
	@Override
	public CQIE getFreshCQIECopy(CQIE rule) {
		
		int suff = ++suffix;
		
		// This method doesn't support nested functional terms
		CQIE freshRule = rule.clone();
		Function head = freshRule.getHead();
		List<Term> headTerms = head.getTerms();
		for (int i = 0; i < headTerms.size(); i++) {
			Term term = headTerms.get(i);
			Term newTerm = getFreshTerm(term, suff);
			headTerms.set(i, newTerm);
		}

		List<Function> body = freshRule.getBody();
		for (Function atom : body) {
			List<Term> atomTerms = atom.getTerms();
			for (int i = 0; i < atomTerms.size(); i++) {
				Term term = atomTerms.get(i);
				Term newTerm = getFreshTerm(term, suff);
				atomTerms.set(i, newTerm);
			}
		}
		return freshRule;
	}

	private Term getFreshTerm(Term term, int suff) {
		Term newTerm;
		if (term instanceof Variable) {
			Variable variable = (Variable) term;
			newTerm = getVariable(variable.getName() + "_" + suff);
		} 
		else if (term instanceof Function) {
			Function functionalTerm = (Function) term;
			List<Term> innerTerms = functionalTerm.getTerms();
			List<Term> newInnerTerms = new LinkedList<>();
			for (int j = 0; j < innerTerms.size(); j++) {
				Term innerTerm = innerTerms.get(j);
				newInnerTerms.add(getFreshTerm(innerTerm, suff));
			}
			Predicate newFunctionSymbol = functionalTerm.getFunctionSymbol();
			Function newFunctionalTerm = getFunction(newFunctionSymbol, newInnerTerms);
			newTerm = newFunctionalTerm;
		} 
		else if (term instanceof Constant) {
			newTerm = term.clone();
		} 
		else {
			throw new RuntimeException("Unsupported term: " + term);
		}
		return newTerm;
	}
}
