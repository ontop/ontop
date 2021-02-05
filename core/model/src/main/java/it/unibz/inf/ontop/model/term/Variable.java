package it.unibz.inf.ontop.model.term;

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

import it.unibz.inf.ontop.model.type.TermTypeInference;

import java.util.Optional;

/**
 * This class defines a type of {@link ImmutableTerm} in which it expresses a quantity
 * that during a calculation is assumed to vary or be capable of varying in
 * value.
 */
public interface Variable extends NonFunctionalTerm, NonConstantTerm, NonGroundTerm, Comparable<Variable> {

	String getName();

	@Override
	default Optional<TermTypeInference> inferType(){
		return Optional.empty();
	}
}
