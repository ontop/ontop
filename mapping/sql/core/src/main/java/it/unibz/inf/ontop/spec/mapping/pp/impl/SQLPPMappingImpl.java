package it.unibz.inf.ontop.spec.mapping.pp.impl;

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
import it.unibz.inf.ontop.spec.mapping.PrefixManager;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;


public class SQLPPMappingImpl implements SQLPPMapping {

	private final ImmutableList<SQLPPTriplesMap> triplesMaps;
    private final PrefixManager prefixManager;

    public SQLPPMappingImpl(ImmutableList<SQLPPTriplesMap> newMappings, PrefixManager prefixManager) {
        this.triplesMaps = newMappings;
        this.prefixManager = prefixManager;
    }

    @Override
    public PrefixManager getPrefixManager() {
		return prefixManager;
	}

	@Override
	public ImmutableList<SQLPPTriplesMap> getTripleMaps() {
        return triplesMaps;
	}
}
