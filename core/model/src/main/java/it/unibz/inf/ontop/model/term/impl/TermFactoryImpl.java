package it.unibz.inf.ontop.model.term.impl;

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
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.injection.OntopModelSettings;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.*;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.model.vocabulary.XSD;
import org.apache.commons.rdf.api.IRI;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.model.vocabulary.RDF.LANGSTRING;

@Singleton
public class TermFactoryImpl implements TermFactory {

	private final TypeFactory typeFactory;
	private final ValueConstant valueTrue;
	private final ValueConstant valueFalse;
	private final ValueConstant valueNull;
	private final ValueConstant provenanceConstant;
	private final ImmutabilityTools immutabilityTools;
	private final Map<RDFDatatype, DatatypePredicate> type2FunctionSymbolMap;
	private final boolean isTestModeEnabled;
	private final AtomicInteger templateCounter;
	private final Map<String, String> templateSuffix;

	@Inject
	private TermFactoryImpl(TypeFactory typeFactory, OntopModelSettings settings) {
		// protected constructor prevents instantiation from other classes.
		this.typeFactory = typeFactory;
		RDFDatatype xsdBoolean = typeFactory.getXsdBooleanDatatype();
		this.valueTrue = new ValueConstantImpl("true", xsdBoolean);
		this.valueFalse = new ValueConstantImpl("false", xsdBoolean);
		this.valueNull = new ValueConstantImpl("null", typeFactory.getXsdStringDatatype());
		this.provenanceConstant = new ValueConstantImpl("ontop-provenance-constant", typeFactory.getXsdStringDatatype());
		this.immutabilityTools = new ImmutabilityTools(this);
		this.type2FunctionSymbolMap = new HashMap<>();
		this.isTestModeEnabled = settings.isTestModeEnabled();
		this.templateCounter = new AtomicInteger();
		this.templateSuffix = new ConcurrentHashMap<>();
	}

	@Override
	public IRIConstant getConstantIRI(IRI iri) {
		return new IRIConstantImpl(iri, typeFactory);
	}
	
	@Override
	public ValueConstant getConstantLiteral(String value) {
		return new ValueConstantImpl(value, typeFactory.getXsdStringDatatype());
	}

	@Override
	public ValueConstant getConstantLiteral(String value, RDFDatatype type) {
		return new ValueConstantImpl(value, type);
	}

	@Override
	public ValueConstant getConstantLiteral(String value, IRI type) {
		return getConstantLiteral(value, typeFactory.getDatatype(type));
	}

	@Override
	public Function getTypedTerm(Term value, RDFDatatype type) {
		return getFunction(getRequiredTypePredicate(type), value);
	}

	@Override
	public Function getTypedTerm(Term value, IRI datatypeIRI) {
		return getTypedTerm(value, typeFactory.getDatatype(datatypeIRI));
	}

	@Override
	public ValueConstant getConstantLiteral(String value, String language) {
		return new ValueConstantImpl(value, language.toLowerCase(), typeFactory);
	}

	@Override
	public Function getTypedTerm(Term value, String language) {
		DatatypePredicate functionSymbol = getRequiredTypePredicate(typeFactory.getLangTermType(language));
		return getFunction(functionSymbol, value);
	}

	@Override
	public ImmutableFunctionalTerm getImmutableTypedTerm(ImmutableTerm value, RDFDatatype type) {
		FunctionSymbol pred = getRequiredTypePredicate(type);
		if (pred == null)
			throw new RuntimeException("Unknown data type: " + type);

		return getImmutableFunctionalTerm(pred, value);
	}

	@Override
	public ImmutableFunctionalTerm getImmutableTypedTerm(ImmutableTerm value, IRI datatypeIRI) {
		return getImmutableTypedTerm(value, typeFactory.getDatatype(datatypeIRI));
	}

	@Override
	public ImmutableFunctionalTerm getImmutableTypedTerm(ImmutableTerm value, String language) {
		return getImmutableTypedTerm(value, typeFactory.getLangTermType(language));
	}

	@Override
	public Variable getVariable(String name) {
		return new VariableImpl(name);
	}

	@Override
	public Function getFunction(Predicate functor, Term... arguments) {
		return getFunction(functor, Arrays.asList(arguments));
	}
	
	@Override
	public Expression getExpression(OperationPredicate functor, Term... arguments) {
		return getExpression(functor, Arrays.asList(arguments));
	}

	@Override
	public Expression getExpression(OperationPredicate functor, List<Term> arguments) {
		if (isTestModeEnabled) {
			checkMutability(arguments);
		}
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
			return new GroundExpressionImpl(expression.getFunctionSymbol(),
					(ImmutableList<? extends GroundTerm>)(ImmutableList<?>)convertTerms(expression));
		}
		else {
			return new NonGroundExpressionImpl(expression.getFunctionSymbol(), convertTerms(expression));
		}
	}

	@Override
	public Function getFunction(Predicate functor, List<Term> arguments) {
		if (isTestModeEnabled) {
			checkMutability(arguments);
		}

		if (functor instanceof OperationPredicate) {
			return getExpression((OperationPredicate) functor, arguments);
		}

		// Default constructor
		return new FunctionalTermImpl(functor, arguments);
	}

	private void checkMutability(List<Term> terms) {
		for(Term term : terms) {
			if (term instanceof ImmutableFunctionalTerm)
				throw new IllegalArgumentException("Was expecting a mutable term, not a " + term.getClass());
			else if (term instanceof Function)
				checkMutability(((Function) term).getTerms());
		}
	}

	@Override
	public ImmutableFunctionalTerm getImmutableFunctionalTerm(FunctionSymbol functor, ImmutableList<? extends ImmutableTerm> terms) {
		if (functor instanceof OperationPredicate) {
			return getImmutableExpression((OperationPredicate)functor, terms);
		}

		if (GroundTermTools.areGroundTerms(terms)) {
			return new GroundFunctionalTermImpl((ImmutableList<? extends GroundTerm>)terms, functor);
		}
		else {
			// Default constructor
			return new NonGroundFunctionalTermImpl(functor, terms);
		}
	}

	@Override
	public ImmutableFunctionalTerm getImmutableFunctionalTerm(FunctionSymbol functor, ImmutableTerm... terms) {
		return getImmutableFunctionalTerm(functor, ImmutableList.copyOf(terms));
	}

	@Override
	public NonGroundFunctionalTerm getNonGroundFunctionalTerm(FunctionSymbol functor, ImmutableTerm... terms) {
		return new NonGroundFunctionalTermImpl(functor, terms);
	}

	@Override
	public NonGroundFunctionalTerm getNonGroundFunctionalTerm(FunctionSymbol functor, ImmutableList<ImmutableTerm> terms) {
		return new NonGroundFunctionalTermImpl(functor, terms);
	}

	public TypeFactory getTypeFactory() {
		return typeFactory;
	}

	@Override
	public Function getUriTemplate(Term... terms) {
		return getUriTemplate(Arrays.asList(terms));
	}

	@Override
	public ImmutableFunctionalTerm getImmutableUriTemplate(ImmutableTerm... terms) {
		return getImmutableUriTemplate(ImmutableList.copyOf(terms));
	}

	@Override
	public ImmutableFunctionalTerm getImmutableUriTemplate(ImmutableList<ImmutableTerm> terms) {
		FunctionSymbol pred = getURITemplatePredicate(terms);
		return getImmutableFunctionalTerm(pred, terms);
	}

	@Override
	public Function getUriTemplate(List<Term> terms) {
		FunctionSymbol uriPred = getURITemplatePredicate(terms.size(), terms);
		return getFunction(uriPred, terms);		
	}

	@Override
	public Function getUriTemplateForDatatype(String type) {
		ValueConstant term = getConstantLiteral(type);
		return getFunction(getURITemplatePredicate(ImmutableList.of(term)), term);
	}
	
	@Override
	public Function getBNodeTemplate(Term... terms) {
		FunctionSymbol pred = new BNodePredicateImpl(terms.length, typeFactory);
		return getFunction(pred, terms);
	}

	@Override
	public ImmutableFunctionalTerm getImmutableBNodeTemplate(ImmutableTerm... terms) {
		FunctionSymbol pred = new BNodePredicateImpl(terms.length, typeFactory);
		return getImmutableFunctionalTerm(pred, terms);
	}

	@Override
	public ImmutableFunctionalTerm getImmutableBNodeTemplate(ImmutableList<ImmutableTerm> terms) {
		FunctionSymbol pred = new BNodePredicateImpl(terms.size(), typeFactory);
		return getImmutableFunctionalTerm(pred, terms);
	}

	@Override
	public Function getBNodeTemplate(List<Term> terms) {
		FunctionSymbol pred = new BNodePredicateImpl(terms.size(), typeFactory);
		return getFunction(pred, terms);
	}

	@Override
	public Expression getFunctionEQ(Term firstTerm, Term secondTerm) {
		return getExpression(ExpressionOperation.EQ, firstTerm, secondTerm);
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

	@Override
	public Expression getFunctionOR(Term term1, Term term2) {
		return getExpression(ExpressionOperation.OR,term1, term2);
	}


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
	public Expression getFunctionCast(Term term1, Term term2) {
		// TODO implement cast function
		return getExpression(ExpressionOperation.QUEST_CAST, term1, term2);
	}

	
	@Override
	public BNode getConstantBNode(String name) {
		return new BNodeConstantImpl(name, typeFactory);
	}

	@Override
	public Expression getFunctionIsTrue(Term term) {
		return getExpression(ExpressionOperation.IS_TRUE, term);
	}


	@Override
	public ValueConstant getBooleanConstant(boolean value) {
		return value ? valueTrue : valueFalse;
	}

	@Override
	public ValueConstant getNullConstant() {
		return valueNull;
	}

	@Override
	public ValueConstant getProvenanceSpecialConstant() {
		return provenanceConstant;
	}

	private ImmutableList<ImmutableTerm> convertTerms(Function functionalTermToClone) {
		ImmutableList.Builder<ImmutableTerm> builder = ImmutableList.builder();
		for (Term term : functionalTermToClone.getTerms()) {
			builder.add(immutabilityTools.convertIntoImmutableTerm(term));
		}
		return builder.build();
	}


	@Override
	public DatatypePredicate getRequiredTypePredicate(RDFDatatype type) {
		return getOptionalTypePredicate(type)
				.orElseThrow(() -> new NoConstructionFunctionException(type));
	}

	@Override
	public DatatypePredicate getRequiredTypePredicate(IRI datatypeIri) {
		if (datatypeIri.equals(LANGSTRING))
			throw new IllegalArgumentException("Lang string predicates are not unique (they depend on the language tag)");
		return getRequiredTypePredicate(typeFactory.getDatatype(datatypeIri));
	}

	@Override
	public Optional<DatatypePredicate> getOptionalTypePredicate(RDFDatatype type) {
		if (type.isAbstract())
			throw new IllegalArgumentException("The datatype " + type + " is abstract and therefore cannot be constructed");

		return Optional.of(type2FunctionSymbolMap
				.computeIfAbsent(
						type,
						t -> t.getLanguageTag()
							// Lang string
							.map(tag -> new DatatypePredicateImpl(type, typeFactory.getDatatype(XSD.STRING)))
							// Other datatypes
							.orElseGet(() -> new DatatypePredicateImpl(type, type))));
	}

	private URITemplatePredicate getURITemplatePredicate(int arity, List<? extends Term> terms) {
		String suffix = computeSuffix(terms.stream()
				.findFirst()
				.filter(t -> t instanceof Constant)
				.map(t -> (Constant) t));

		return new URITemplatePredicateImpl(arity, suffix, typeFactory);
	}

	private String computeSuffix(Optional<Constant> optionalConstant) {
		return optionalConstant
				.map(Constant::getValue)
				.filter(v -> v.contains("{}"))
				.map(v -> templateSuffix.computeIfAbsent(v,
						k -> "T" + templateCounter.incrementAndGet()))
				.orElse("");
	}

	private URITemplatePredicate getURITemplatePredicate(ImmutableList<? extends ImmutableTerm> terms) {
		String suffix = computeSuffix(terms.stream()
				.findFirst()
				.filter(t -> t instanceof Constant)
				.map(t -> (Constant) t));
		return new URITemplatePredicateImpl(terms.size(), suffix, typeFactory);
	}

	private static class NoConstructionFunctionException extends OntopInternalBugException {

		private NoConstructionFunctionException(TermType type) {
			super("No construction function found for " + type);
		}
	}

}
