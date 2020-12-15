package it.unibz.inf.ontop.spec.mapping.pp;

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
import it.unibz.inf.ontop.spec.mapping.SQLPPSourceQuery;
import it.unibz.inf.ontop.spec.mapping.TargetAtom;

import java.util.Optional;


public interface SQLPPTriplesMap extends PreProcessedTriplesMap {

	ImmutableList<TargetAtom> getTargetAtoms();

	SQLPPSourceQuery getSourceQuery();

	String getId();

	// when created from OBDA files, or parsed from String, targetString is presented.
	// If generated, e.g. from R2RML, targetString might be null
	Optional<String> getOptionalTargetString();
}
