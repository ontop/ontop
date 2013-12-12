package it.unibz.krdb.obda.protege4.views;

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

import java.util.ArrayList;

import org.protege.editor.core.Disposable;

/**
 * A holder for the list of all Query Managers views
 */
public class QueryManagerViewsList extends ArrayList<QueryManagerView> implements Disposable {

	private static final long serialVersionUID = 2986737849606126197L;

	public void dispose() throws Exception {
		// NO-OP
	}
}
