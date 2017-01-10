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

import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.Predicate.COL_TYPE;
import it.unibz.inf.ontop.model.LanguageTag;
import it.unibz.inf.ontop.model.TermType;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;


import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


import com.google.common.collect.ImmutableList;

import static it.unibz.inf.ontop.model.impl.DataAtomTools.areVariablesDistinct;
import static it.unibz.inf.ontop.model.impl.DataAtomTools.isVariableOnly;

public class OBDADataFactoryImpl implements OBDADataFactory {

	private static final long serialVersionUID = 1851116693137470887L;

	private static OBDADataFactory instance = null;
	private static ValueFactory irifactory = null;

	// Only builds these TermTypes once.
	private final Map<COL_TYPE, TermType> termTypeCache = new ConcurrentHashMap<>();

	private static int counter = 0;
	private final DatatypeFactory datatypeFactory;

	OBDADataFactoryImpl(DatatypeFactory datatypeFactory) {
		// protected constructor prevents instantiation from other classes.
		this.datatypeFactory = datatypeFactory;
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

	@Override
	public Predicate getObjectPropertyPredicate(String name) {
		return new PredicateImpl(name, 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.OBJECT });
	}

	@Override
	public Predicate getDataPropertyPredicate(String name) {
		return new PredicateImpl(name, 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.LITERAL });
	}

	@Override
	public Predicate getDataPropertyPredicate(String name, COL_TYPE type) {
		return new PredicateImpl(name, 2, new COL_TYPE[] { COL_TYPE.OBJECT, type }); // COL_TYPE.LITERAL
	}

	//defining annotation property we still don't know if the values that it will assume, will be an object or a data property
	@Override
	public Predicate getAnnotationPropertyPredicate(String name) {
		return new PredicateImpl(name, 2, new COL_TYPE[]{Predicate.COL_TYPE.OBJECT, Predicate.COL_TYPE.NULL});
	}

	@Override
	public Predicate getClassPredicate(String name) {
		return new PredicateImpl(name, 1, new COL_TYPE[] { COL_TYPE.OBJECT });
	}

	@Override
	public Predicate getOWLSameAsPredicate() {
		return new PredicateImpl(OBDAVocabulary.SAME_AS, 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.OBJECT });
	}

	@Override
	public Predicate getOBDACanonicalIRI() {
		return new PredicateImpl(OBDAVocabulary.CANONICAL_IRI, 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.OBJECT });
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
		Predicate pred = datatypeFactory.getTypePredicate(type);
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
		Predicate pred = datatypeFactory.getTypePredicate(COL_TYPE.LITERAL_LANG);
		return getFunction(pred, value, language);
	}

	@Override
	public Function getTypedTerm(Term value, String language) {
		Term lang = getConstantLiteral(language.toLowerCase(), COL_TYPE.LITERAL);		
		Predicate pred = datatypeFactory.getTypePredicate(COL_TYPE.LITERAL_LANG);
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
			return getExpression((OperationPredicate)functor, arguments);
		}

		// Default constructor
		return new FunctionalTermImpl(functor, arguments);
	}
	
	@Override
	public Expression getExpression(OperationPredicate functor, Term... arguments) {
		return new ExpressionImpl(functor, arguments);
	}

	@Override
	public Expression getExpression(OperationPredicate functor, List<Term> arguments) {
		return new ExpressionImpl(functor, arguments);
	}

	@Override
	public ImmutableExpression getImmutableExpression(OperationPredicate functor, ImmutableTerm... arguments) {
		return getImmutableExpression(functor, ImmutableList.copyOf(arguments));
	}

	@Override
	public ImmutableExpression getImmutableExpression(OperationPredicate functor,
													  ImmutableList<? extends ImmutableTerm> arguments) {
		if (GroundTermTools.areGroundTerms(arguments)) {
			return new GroundExpressionImpl(functor, (ImmutableList<GroundTerm>)arguments);
		}
		else {
			return new NonGroundExpressionImpl(functor, arguments);
		}
	}

	@Override
	public ImmutableExpression getImmutableExpression(Expression expression) {
		if (GroundTermTools.isGroundTerm(expression)) {
			return new GroundExpressionImpl(expression);
		}
		else {
			return new NonGroundExpressionImpl(expression);
		}
	}

	@Override
	public Function getFunction(Predicate functor, List<Term> arguments) {
		if (functor instanceof OperationPredicate) {
			return getExpression((OperationPredicate) functor, arguments);
		}

		// Default constructor
		return new FunctionalTermImpl(functor, arguments);
	}

	@Override
	public ImmutableFunctionalTerm getImmutableFunctionalTerm(Predicate functor, ImmutableList<ImmutableTerm> terms) {
		if (functor instanceof OperationPredicate) {
			return getImmutableExpression((OperationPredicate)functor, terms);
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
	public DistinctVariableOnlyDataAtom getDistinctVariableOnlyDataAtom(AtomPredicate predicate, Variable... arguments) {
		return getDistinctVariableOnlyDataAtom(predicate, ImmutableList.copyOf(arguments));
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
	public DatatypeFactory getDatatypeFactory() {
		return datatypeFactory;
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
	public DatalogProgram getDatalogProgram(OBDAQueryModifiers modifiers) {
		DatalogProgram p = new DatalogProgramImpl();
		p.getQueryModifiers().copy(modifiers);
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
	public Expression getFunctionEQ(Term firstTerm, Term secondTerm) {
		return getExpression(ExpressionOperation.EQ, firstTerm, secondTerm);
	}

	@Override
	public Expression getFunctionGTE(Term firstTerm, Term secondTerm) {
		return getExpression(ExpressionOperation.GTE, firstTerm, secondTerm);
	}

	@Override
	public Expression getFunctionGT(Term firstTerm, Term secondTerm) {
		return getExpression(ExpressionOperation.GT, firstTerm, secondTerm);
	}

	@Override
	public Expression getFunctionLTE(Term firstTerm, Term secondTerm) {
		return getExpression(ExpressionOperation.LTE, firstTerm, secondTerm);
	}

	@Override
	public Expression getFunctionLT(Term firstTerm, Term secondTerm) {
		return getExpression(ExpressionOperation.LT, firstTerm, secondTerm);
	}

	@Override
	public Expression getFunctionNEQ(Term firstTerm, Term secondTerm) {
		return getExpression(ExpressionOperation.NEQ, firstTerm, secondTerm);
	}

	@Override
	public Expression getFunctionNOT(Term term) {
		return getExpression(ExpressionOperation.NOT, term);
	}

	@Override
	public Expression getFunctionAND(Term term1, Term term2) {
		return getExpression(ExpressionOperation.AND, term1, term2);
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
	public Expression getFunctionOR(Term term1, Term term2) {
		return getExpression(ExpressionOperation.OR,term1, term2);
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
	public Expression getFunctionIsNull(Term term) {
		return getExpression(ExpressionOperation.IS_NULL, term);
	}

	@Override
	public Expression getFunctionIsNotNull(Term term) {
		return getExpression(ExpressionOperation.IS_NOT_NULL, term);
	}


	@Override
	public Expression getLANGMATCHESFunction(Term term1, Term term2) {
		return getExpression(ExpressionOperation.LANGMATCHES, term1, term2);
	}

	@Override
	public Expression getSQLFunctionLike(Term term1, Term term2) {
		return getExpression(ExpressionOperation.SQL_LIKE, term1, term2);
	}

	@Override
	public Expression getFunctionCast(Term term1, Term term2) {
		// TODO implement cast function
		return getExpression(ExpressionOperation.QUEST_CAST, term1, term2);
	}

	
	@Override
	public BNode getConstantBNode(String name) {
		return new BNodeConstantImpl(name);
	}

	@Override
	public Expression getFunctionIsTrue(Term term) {
		return getExpression(ExpressionOperation.IS_TRUE, term);
	}

	@Override
	public Function getSPARQLJoin(Function t1, Function t2, Function joinCondition) {
		return getFunction(OBDAVocabulary.SPARQL_JOIN, t1, t2, joinCondition);
	}


	@Override
	public Function getSPARQLLeftJoin(List<Function> leftAtoms, List<Function> rightAtoms,
									  Optional<Function> optionalCondition){

		if (leftAtoms.isEmpty() || rightAtoms.isEmpty()) {
			throw new IllegalArgumentException("Atoms on the left and right sides are required");
		}

		List<Term> joinTerms = new ArrayList<>(leftAtoms);

		joinTerms.addAll(rightAtoms);

		/**
		 * The joining condition goes with the right part
		 */
		optionalCondition.ifPresent(joinTerms::add);

		return getFunction(OBDAVocabulary.SPARQL_LEFTJOIN, joinTerms);
	}

	@Override
	public Function getSPARQLLeftJoin(Term t1, Term t2) {
		return getFunction(OBDAVocabulary.SPARQL_LEFTJOIN, t1, t2);
	}

	@Override
	public TermType getTermType(COL_TYPE type) {
		TermType cachedType = termTypeCache.get(type);
		if (cachedType != null) {
			return cachedType;
		}
		else {
			TermType termType = new TermTypeImpl(type);
			termTypeCache.put(type, termType);
			return termType;
		}
	}

	@Override
	public TermType getTermType(String languageTagString) {
		return new TermTypeImpl(getLanguageTag(languageTagString));
	}

	@Override
	public TermType getTermType(Term languageTagTerm) {
		return languageTagTerm instanceof Constant
				? getTermType(((Constant) languageTagTerm).getValue())
				: new TermTypeImpl(languageTagTerm);
	}

	private LanguageTag getLanguageTag(String languageTagString) {
		return new LanguageTagImpl(languageTagString);
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
