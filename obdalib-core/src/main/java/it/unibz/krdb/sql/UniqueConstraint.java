package it.unibz.krdb.sql;

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

/**
 * Primary key or a unique constraint<br>
 * 
 * PRIMARY KEY (columnName (, columnName)*)<br>		
 * UNIQUE (columnName (, columnName)*)<br>	
 * 
 * (a form of equality-generating dependencies)
 * 
 * @author Roman Kontchakov
 *
 */

public class UniqueConstraint {

	private ImmutableList<Attribute> attributes;
	
	public UniqueConstraint(ImmutableList<Attribute> attributes) {
		this.attributes = attributes;
		if (attributes.isEmpty())
			throw new IllegalArgumentException("Empty UNIQUE constraint");
	}
	
	public ImmutableList<Attribute> getAttributes() {
		return attributes;
	}
}
