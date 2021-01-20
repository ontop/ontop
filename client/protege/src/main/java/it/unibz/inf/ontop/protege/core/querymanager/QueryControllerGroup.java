package it.unibz.inf.ontop.protege.core.querymanager;

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

import java.util.ArrayList;
import java.util.List;

public class QueryControllerGroup implements QueryControllerEntity {

	private final List<QueryControllerQuery> queries =  new ArrayList<>();
	private final String group_id;

	QueryControllerGroup(String group_id) {
		this.group_id = group_id;
	}

	@Override
	public String getID() {
		return group_id;
	}

	/**
	 * Return all queries of the vector QueryControllerQuery.
	 */
	public List<QueryControllerQuery> getQueries() {
		return queries;
	}


	void addQuery(QueryControllerQuery query) {
		queries.add(query);
	}

	void updateQuery(int position, QueryControllerQuery query) {
		queries.set(position, query);
	}

	void removeQuery(int position) {
		queries.remove(position);
	}
}
