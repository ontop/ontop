package it.unibz.inf.ontop.answering.reformulation.generation;

/*
 * #%L
 * ontop-reformulation-core
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

import java.io.Serializable;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.OntopReformulationException;
import it.unibz.inf.ontop.answering.reformulation.ExecutableQuery;
import it.unibz.inf.ontop.iq.IntermediateQuery;

/**
 * Generates a source query in a given native query language.
 *
 */
public interface NativeQueryGenerator extends Serializable {

	/**
	 * Translates the given datalog program into a source query, which can later
	 * be evaluated by a evaluation engine.
	 *
	 */
	ExecutableQuery generateSourceQuery(IntermediateQuery query)
			throws OntopReformulationException;

	ExecutableQuery generateEmptyQuery(ImmutableList<String> signature);
}
