package org.semanticweb.ontop.model;

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

import java.util.Collection;
import java.util.List;

public interface DatalogProgram extends OBDAQuery {

	public List<CQIE> getRules();

	public void appendRule(CQIE rule);

	public void appendRule(Collection<CQIE> rule);

	public void removeRule(CQIE rule);

	public void removeRules(Collection<CQIE> rule);
	
	public void removeAllRules();

	public boolean isUCQ();

	/***
	 * Returns all the rules that have the given predicate in their heads
	 * 
	 * @param headPredicate
	 * @return
	 */
	public List<CQIE> getRules(Predicate headPredicate);

	public DatalogProgram clone();

}
