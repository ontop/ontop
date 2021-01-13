package it.unibz.inf.ontop.protege.core;

/*
 * #%L
 * ontop-protege
 * %%
 * Copyright (C) 2009 - 2013 KRDB Research Centre. Free University of Bozen Bolzano.
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

import org.protege.editor.core.Disposable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import static it.unibz.inf.ontop.protege.core.impl.DeprecatedConstants.*;

/**
 * Mutable
 */
public class DisposableProperties extends Properties implements Disposable {

	private static final long	serialVersionUID	= -1699795366967423089L;

	public DisposableProperties() {
	}

	public DisposableProperties(Properties properties) {
		putAll(properties);
	}

	@Override
    public void dispose() {

	}

	public List<String> getReformulationPlatformPreferencesKeys(){
		ArrayList<String> keys = new ArrayList<>();
		keys.add(ABOX_MODE);
		keys.add(DBTYPE);
		keys.add(OBTAIN_FROM_ONTOLOGY);
		keys.add(OBTAIN_FROM_MAPPINGS);
		return keys;
	}

	@Override
	public DisposableProperties clone() {
		return new DisposableProperties(this);
	}

	public boolean getBoolean(String key) {
		return Boolean.parseBoolean(getProperty(key));
	}

	public Optional<String> getOptionalProperty(String key) {
		return Optional.ofNullable(getProperty(key));
	}
}
