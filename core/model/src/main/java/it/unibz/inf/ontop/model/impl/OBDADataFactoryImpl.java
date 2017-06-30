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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.predicate.AtomPredicate;
import it.unibz.inf.ontop.model.predicate.ExpressionOperation;
import it.unibz.inf.ontop.model.predicate.OperationPredicate;
import it.unibz.inf.ontop.model.predicate.Predicate;
import it.unibz.inf.ontop.model.predicate.Predicate.COL_TYPE;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.DatatypeFactory;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.InjectiveVar2VarSubstitution;
import it.unibz.inf.ontop.substitution.Var2VarSubstitution;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class OBDADataFactoryImpl implements OBDADataFactory {

	private static final long serialVersionUID = 1851116693137470887L;
	private static final OBDADataFactory INSTANCE = new OBDADataFactoryImpl(DatatypeFactoryImpl.getInstance());


	// Only builds these TermTypes once.
	private final Map<COL_TYPE, TermType> termTypeCache = new ConcurrentHashMap<>();

	private static int counter = 0;
	private final DatatypeFactory datatypeFactory;

	public static OBDADataFactory getInstance() {
		return INSTANCE;
	}

	private OBDADataFactoryImpl(DatatypeFactory datatypeFactory) {
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
		Term lang = getConstantLiteral(language.toLowerCase(), COL_TYPE.STRING);
		Predicate pred = datatypeFactory.getTypePredicate(COL_TYPE.LITERAL_LANG);
		return getFunction(pred, value, lang);
	}

	@Override
	public ImmutableFunctionalTerm getImmutableTypedTerm(ImmutableTerm value, COL_TYPE type) {
		Predicate pred = datatypeFactory.getTypePredicate(type);
		if (pred == null)
			throw new RuntimeException("Unknown data type!");

		return getImmutableFunctionalTerm(pred, value);
	}

	@Override
	public ImmutableFunctionalTerm getImmutableTypedTerm(ImmutableTerm value, ImmutableTerm language) {
		Predicate pred = datatypeFactory.getTypePredicate(COL_TYPE.LITERAL_LANG);
		return getImmutableFunctionalTerm(pred, value, language);
	}

	@Override
	public ImmutableFunctionalTerm getImmutableTypedTerm(ImmutableTerm value, String language) {
		ValueConstant lang = getConstantLiteral(language.toLowerCase(), COL_TYPE.STRING);
		Predicate pred = datatypeFactory.getTypePredicate(COL_TYPE.LITERAL_LANG);
		return getImmutableFunctionalTerm(pred, value, lang);
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
	public ImmutableFunctionalTerm getImmutableFunctionalTerm(Function functionalTerm) {
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
	public DatatypeFactory getDatatypeFactory() {
		return datatypeFactory;
	}

	@Override
	public Function getUriTemplate(Term... terms) {
		Predicate uriPred = new URITemplatePredicateImpl(terms.length);
		return getFunction(uriPred, terms);		
	}

	@Override
	public ImmutableFunctionalTerm getImmutableUriTemplate(ImmutableTerm... terms) {
		Predicate pred = new URITemplatePredicateImpl(terms.length);
		return getImmutableFunctionalTerm(pred, terms);
	}

	@Override
	public ImmutableFunctionalTerm getImmutableUriTemplate(ImmutableList<ImmutableTerm> terms) {
		Predicate pred = new URITemplatePredicateImpl(terms.size());
		return getImmutableFunctionalTerm(pred, terms);
	}

	@Override
	public Function getUriTemplate(List<Term> terms) {
		Predicate uriPred = new URITemplatePredicateImpl(terms.size());
		return getFunction(uriPred, terms);		
	}

	@Override
	public Function getUriTemplateForDatatype(String type) {
		return getFunction(new URITemplatePredicateImpl(1), getConstantLiteral(type));
	}
	
	@Override
	public Function getBNodeTemplate(Term... terms) {
		Predicate pred = new BNodePredicateImpl(terms.length);
		return getFunction(pred, terms);
	}

	@Override
	public ImmutableFunctionalTerm getImmutableBNodeTemplate(ImmutableTerm... terms) {
		Predicate pred = new BNodePredicateImpl(terms.length);
		return getImmutableFunctionalTerm(pred, terms);
	}

	@Override
	public ImmutableFunctionalTerm getImmutableBNodeTemplate(ImmutableList<ImmutableTerm> terms) {
		Predicate pred = new BNodePredicateImpl(terms.size());
		return getImmutableFunctionalTerm(pred, terms);
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

	@Override
	public AtomPredicate getAtomPredicate(String name, int arity) {
		return new AtomPredicateImpl(name, arity);
	}

	@Override
	public AtomPredicate getAtomPredicate(Predicate datalogPredicate) {
		return new AtomPredicateImpl(datalogPredicate);
	}

	@Override
	public <T extends ImmutableTerm> ImmutableSubstitution<T> getSubstitution(ImmutableMap<Variable, T> newSubstitutionMap) {
		return new ImmutableSubstitutionImpl<>(newSubstitutionMap);
	}

	@Override
	public <T extends ImmutableTerm> ImmutableSubstitution<T> getSubstitution(Variable k1, T v1) {
		return getSubstitution(ImmutableMap.of(k1, v1));
	}

	@Override
	public <T extends ImmutableTerm> ImmutableSubstitution<T> getSubstitution(Variable k1, T v1, Variable k2, T v2) {
		return getSubstitution(ImmutableMap.of(k1, v1, k2, v2));
	}

	@Override
	public <T extends ImmutableTerm> ImmutableSubstitution<T> getSubstitution(Variable k1, T v1, Variable k2, T v2, Variable k3, T v3) {
		return getSubstitution(ImmutableMap.of(k1, v1, k2, v2, k3, v3));
	}

	@Override
	public <T extends ImmutableTerm> ImmutableSubstitution<T> getSubstitution(Variable k1, T v1, Variable k2, T v2,
																			  Variable k3, T v3, Variable k4, T v4) {
		return getSubstitution(ImmutableMap.of(k1, v1, k2, v2, k3, v3, k4, v4));
	}

	@Override
	public <T extends ImmutableTerm> ImmutableSubstitution<T> getSubstitution() {
		return new ImmutableSubstitutionImpl<>(ImmutableMap.of());
	}

	@Override
	public Var2VarSubstitution getVar2VarSubstitution(ImmutableMap<Variable, Variable> substitutionMap) {
		return new Var2VarSubstitutionImpl(substitutionMap);
	}

	@Override
	public InjectiveVar2VarSubstitution getInjectiveVar2VarSubstitution(Map<Variable, Variable> substitutionMap) {
		return new InjectiveVar2VarSubstitutionImpl(substitutionMap);
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

	@Override
	public ImmutableFunctionalTerm getImmutableTripleAtom(ImmutableTerm subject, ImmutableTerm predicate,
														  ImmutableTerm object) {
		return getImmutableFunctionalTerm(PredicateImpl.QUEST_TRIPLE_PRED, subject, predicate, object);
	}

	/**
	 * Non-conflicting variable:
	 *   - initial variable of the variable set not known by the generator
	 *   - or a fresh variable generated by the generator NOT PRESENT in the variable set
	 */
	@Override
	public InjectiveVar2VarSubstitution generateNotConflictingRenaming(VariableGenerator variableGenerator,
																	   ImmutableSet<Variable> variables) {
		ImmutableMap<Variable, Variable> newMap = variables.stream()
				.map(v -> new AbstractMap.SimpleEntry<>(v, generateNonConflictingVariable(v, variableGenerator, variables)))
				.filter(pair -> ! pair.getKey().equals(pair.getValue()))
				.collect(ImmutableCollectors.toMap());

		return getInjectiveVar2VarSubstitution(newMap);
	}

	private static Variable generateNonConflictingVariable(Variable v, VariableGenerator variableGenerator,
														   ImmutableSet<Variable> variables) {

		Variable proposedVariable = variableGenerator.generateNewVariableIfConflicting(v);
		if (proposedVariable.equals(v)
				// Makes sure that a "fresh" variable does not exists in the variable set
				|| (!variables.contains(proposedVariable)))
			return proposedVariable;

		/*
		 * Generates a "really fresh" variable
		 */
		ImmutableSet<Variable> knownVariables = Sets.union(
					variableGenerator.getKnownVariables(),
					variables)
				.immutableCopy();

		VariableGenerator newVariableGenerator = new VariableGenerator(knownVariables);
		return newVariableGenerator.generateNewVariableFromVar(v);


	}
}
