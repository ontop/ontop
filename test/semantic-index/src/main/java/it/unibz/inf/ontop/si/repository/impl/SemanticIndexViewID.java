package it.unibz.inf.ontop.si.repository.impl;

import it.unibz.inf.ontop.model.type.ObjectRDFType;
import it.unibz.inf.ontop.model.type.RDFTermType;

import javax.annotation.Nullable;
import java.util.Objects;

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


/***
 * A record to keep track of which tables in the semantic index tables have rows
 * in it. It allows to know the type of rows each has, as in, for which
 * indexes and which type of object.
 */

public class SemanticIndexViewID {
	private final ObjectRDFType type1;
	@Nullable
	private final RDFTermType type2;

	public SemanticIndexViewID(ObjectRDFType type1, RDFTermType type2) {
		this.type1 = type1;
		this.type2 = type2;
	}
	
	public SemanticIndexViewID(ObjectRDFType type1) {
		this.type1 = type1;
		this.type2 = null;
	}

	public ObjectRDFType getType1() {
		return type1;
	}

	public RDFTermType getType2() {
		return type2;
	}

	@Override
	public String toString() {
		return " T1: " + type1 + (type2 != null ? ", T2: " + type2 : "");
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof SemanticIndexViewID) {
			SemanticIndexViewID other = (SemanticIndexViewID) obj;
			return Objects.equals(this.type1, other.type1) && Objects.equals(this.type2, other.type2);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(type1, type2);
	}
}
