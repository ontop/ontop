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

import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.model.vocabulary.XSD;

import javax.annotation.Nonnull;
import java.util.stream.Stream;

public class RDFLiteralConstantImpl extends AbstractNonNullConstant implements RDFLiteralConstant {

	private final String value;
	private final String string;
	private final RDFDatatype termType;

	/**
	 * The default constructor.
	 * 
	 * @param value
	 *            the constant value.
	 * @param type
	 *            the constant type.
	 */
	protected RDFLiteralConstantImpl(@Nonnull String value, @Nonnull RDFDatatype type) {
		this.value = value;
		this.termType = type;
		String suffix = type.getIRI().equals(XSD.STRING) ? "" : "^^" + type.toString();
		this.string = "\"" + value + "\"" + suffix;
	}

	protected RDFLiteralConstantImpl(@Nonnull String value, @Nonnull String language, TypeFactory typeFactory) {
		this.value = value;
		String l = language.toLowerCase();
		this.termType = typeFactory.getLangTermType(l);
		this.string = "\"" + value + "@" + l + "\"";
	}

	@Override
	public boolean equals(Object other) {
		return (other instanceof RDFLiteralConstantImpl
				&& this.string.equals(((RDFLiteralConstantImpl) other).string));
	}

	@Override
	public int hashCode() {		
		return string.hashCode();
	}

	@Override
	public boolean isGround() {
		return true;
	}

	@Override
	public Stream<Variable> getVariableStream() {
		return Stream.of();
	}

	/**
	 * Strict equality: require to have the same lexical value and the same datatype
	 *
	 * To be distinguished from https://www.w3.org/TR/sparql11-query/#func-RDFterm-equal
	 *  (which returns NULL instead of FALSE for two different literals)
	 */
	@Override
	public IncrementalEvaluation evaluateStrictEq(ImmutableTerm otherTerm, VariableNullability variableNullability) {
		if (otherTerm instanceof RDFLiteralConstant) {
			return equals(otherTerm)
					? IncrementalEvaluation.declareIsTrue()
					: IncrementalEvaluation.declareIsFalse();
		}
		else if (otherTerm instanceof Constant)
			return ((Constant) otherTerm).isNull()
				? IncrementalEvaluation.declareIsNull()
				: IncrementalEvaluation.declareIsFalse();
		else
			return otherTerm.evaluateStrictEq(this, variableNullability);
	}

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public RDFDatatype getType() {
		return termType;
	}

	@Override
	public String toString() {
		return string;
	}
}
