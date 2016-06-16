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

import it.unibz.inf.ontop.owlrefplatform.core.QuestPreferences;
import org.protege.editor.core.Disposable;
<<<<<<< HEAD:ontop-protege/src/main/java/it/unibz/inf/ontop/protege/core/ProtegeReformulationPlatformPreferences.java
import it.unibz.inf.ontop.owlrefplatform.core.QuestPreferences;

import java.util.Properties;
import java.util.Set;
=======
>>>>>>> v3/package-names-changed:ontop-protege/src/main/java/it/unibz/inf/ontop/protege/core/ProtegeReformulationPlatformPreferences.java

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
