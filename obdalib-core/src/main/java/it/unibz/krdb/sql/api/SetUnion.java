package it.unibz.krdb.sql.api;

/*
 * #%L
 * ontop-obdalib-core
 * %%
 * Copyright (C) 2009 - 2013 Free University of Bozen-Bolzano
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

public class SetUnion extends Operator {
	
	private static final long serialVersionUID = 455219736799583763L;
	
	private static final int UNION_DEFAULT = 0;
	private static final int UNION_ALL = 1;
	private static final int UNION_DISTINCT = 2;
	
	private int type;
	
	public SetUnion() {
		type = UNION_DEFAULT;
	}
	
	public SetUnion(int type) {
		setType(type);
	}
	
	public void setType(int value) {
		type = value;
	}
	
	public String getType() {
		switch(type) {
			case UNION_DEFAULT: return "union";
			case UNION_ALL: return "union all";
			case UNION_DISTINCT: return "union distinct";
		}
		return "";
	}
	
	@Override
	public String toString() {
		String str = "%s " + getType() + " %s";
		return str;
	}
}
