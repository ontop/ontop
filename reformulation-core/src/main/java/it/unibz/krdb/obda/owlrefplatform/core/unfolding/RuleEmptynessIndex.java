package it.unibz.krdb.obda.owlrefplatform.core.unfolding;

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

import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.URIConstant;

/***
 * An emptyness index is an utility that helps two components comunicate about
 * the status of the data returned by a query. The purpose of the index is for
 * the client to be able to ask the index if a rule is known NOT to have any
 * data.
 * <p>
 * The emptyness index is directly used by the unfolder datalog unfolder to
 * avoid unfolding with respect to rules that will not generate any data anyway,
 * since their body is always empty.
 * <p>
 * Emptyness checking is specially critical for the sematnic index mappings. The
 * Semantic Index repository generates many mappings. E.g. for each class C
 * defined in hte vocabulary, it generates at leaste two mappings, one to be
 * able to retrieve Abox assertions (triples) of the form C(\<a\>) and another
 * one to be able to retrieve assertions of the form C(_:bnode1), since the
 * repository stores this data diferently. Same happens with data and object
 * property mappings.
 * <p>
 * However, at the same time the SI repository keeps track of the data that has
 * been inserted into the repo. Most of the mappings will not have data, so it
 * is possible to avoid using all the mappings by asking the repo. This is done
 * through this interface.
 * 
 * @author mariano
 * 
 */
public interface RuleEmptynessIndex {

	/***
	 * Asks if the mapping for Predicate p, with arity types.size() is empty.
	 * The mapping must match the type of each of the empty components. For
	 * example. A query of the form isEmpty("Person", COL_TYPE.BNODE) returns
	 * empty if the mapping with head Person(BNODE(x)) is empty.
	 * 
	 * <p>
	 * This should not be used for mapping of the for triple(x,y,z) see next
	 * method.
	 * <p>
	 * Note that types.size() must be equals to the arity of the mapping being
	 * checking. E.g., Class(x) requires types.size = 1. hasFather(x,y) requires
	 * types.size() = 2.
	 * 
	 * @param p
	 * @param types
	 * @return
	 */
	public boolean isEmpty(Function atom);

	
}
