package it.unibz.krdb.obda.querymanager;

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

import java.io.Serializable;

public interface QueryControllerListener extends Serializable {

	public void elementAdded(QueryControllerEntity element);

	public void elementAdded(QueryControllerQuery query, QueryControllerGroup group);

	public void elementRemoved(QueryControllerEntity element);

	public void elementRemoved(QueryControllerQuery query, QueryControllerGroup group);

	public void elementChanged(QueryControllerQuery query);

	public void elementChanged(QueryControllerQuery query, QueryControllerGroup group);
}
