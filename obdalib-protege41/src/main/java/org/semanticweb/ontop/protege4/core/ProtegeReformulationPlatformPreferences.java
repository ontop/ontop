package org.semanticweb.ontop.protege4.core;

/*
 * #%L
 * ontop-protege4
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
import org.semanticweb.ontop.owlrefplatform.core.QuestPreferences;

import java.util.Properties;
import java.util.Set;

public class ProtegeReformulationPlatformPreferences extends QuestPreferences implements Disposable {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= -1699795366967423089L;

	public ProtegeReformulationPlatformPreferences() {
		super();
	}

	public ProtegeReformulationPlatformPreferences(Properties p) {
		super(p);
	}

	public void dispose() {
		// Do nothing.
	}

	public Set<Object> getKeys() {
		return copyProperties().keySet();
	}

	public ProtegeReformulationPlatformPreferences newProperties(Object key, Object value) {
		Properties newProperties = copyProperties();
		newProperties.put(key, value);
		return new ProtegeReformulationPlatformPreferences(newProperties);
	}
}
