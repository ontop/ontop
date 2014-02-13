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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.io.Serializable;
import java.net.URI;
import java.util.Set;

public interface OBDADataSource extends Cloneable, Serializable {

	public abstract void setParameter(String parameter_uri, String value);

	public abstract URI getSourceID();

	public abstract void setNewID(URI newid);

	public abstract String getParameter(String parameter_uri);

	public abstract Set<Object> getParameters();

	public abstract void setEnabled(boolean enabled);

	public abstract boolean isEnabled();

	public abstract void setRegistred(boolean registred);

	public abstract boolean isRegistred();
	
	public Object clone();
}
