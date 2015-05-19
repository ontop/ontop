package it.unibz.krdb.obda.model;

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
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L% 
 */

/**
 * Made for string functions in the select part of the source sql of mappings. 
 * Presently only CONCAT and REPLACE
 *
 * See test {@link it.unibz.krdb.obda.reformulation.tests.ComplexSelectMappingVirtualABoxTest}
 * See test {@link it.unibz.krdb.obda.reformulation.tests.ComplexWhereMappingTest}
 * 
 * @author Dag Hovland
 *
 */
public interface StringOperationPredicate extends OperationPredicate {
	// NO-OP
}
