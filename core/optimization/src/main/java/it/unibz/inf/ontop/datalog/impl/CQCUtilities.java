package it.unibz.inf.ontop.datalog.impl;

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

import it.unibz.inf.ontop.datalog.CQIE;
import it.unibz.inf.ontop.datalog.CQContainmentCheck;

import java.util.*;

/***
 * A class that allows you to perform different operations related to query
 * containment on conjunctive queries.
 * 
 * Two usages: 
 *    - simplifying queries with DL atoms
 *    - simplifying mapping queries with SQL atoms
 * 
 * @author Mariano Rodriguez Muro
 * 
 */
public class CQCUtilities {


	/***
	 * Removes queries that are contained syntactically, using the method
	 * isContainedIn(CQIE q1, CQIE 2). 
	 * 
	 * Removal of queries is done in two main double scans. The first scan goes
	 * top-down/down-top, the second scan goes down-top/top-down
	 * 
	 * @param queries
	 */
	
	public static void removeContainedQueries(List<CQIE> queries, CQContainmentCheck containment) {

		{
			Iterator<CQIE> iterator = queries.iterator();
			while (iterator.hasNext()) {
				CQIE query = iterator.next();
				ListIterator<CQIE> iterator2 = queries.listIterator(queries.size());
				while (iterator2.hasPrevious()) {
					CQIE query2 = iterator2.previous(); 
					if (query2 == query)
						break;
					if (containment.isContainedIn(query, query2)) {
						iterator.remove();
						break;
					}
				}
			}
		}
		{
			// second pass from the end
			ListIterator<CQIE> iterator = queries.listIterator(queries.size());
			while (iterator.hasPrevious()) {
				CQIE query = iterator.previous();
				Iterator<CQIE> iterator2 = queries.iterator();
				while (iterator2.hasNext()) {
					CQIE query2 = iterator2.next();
					if (query2 == query)
						break;
					if (containment.isContainedIn(query, query2)) {
						iterator.remove();
						break;
					}
				}
			}
		}
	}

}
