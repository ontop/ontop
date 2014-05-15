package org.semanticweb.ontop.owlrefplatform.core.viewmanager;

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

import java.io.Serializable;

import org.semanticweb.ontop.model.Function;


/**
 * The view manager interface should be implemented by any class which is used to
 * establish a connection between data log queries and source queries. The Source generator
 * uses the information provided by it to translate datalog programs into source queries 
 * 
 * @author Manfred Gerstgrasser
 *
 */

public interface ViewManager extends Serializable {
	
	/**
	 * Translates the given atom into the alias which was used for it during
	 * some other processes, like abox dump, etc. Might not always necessary to
	 * implement
	 * 
	 * @param atom the atom
	 * @return the alias of the atom
	 * @throws Exception
	 */
	public String getTranslatedName(Function atom) throws Exception;
	
	/**
	 * Stores the original query head, so that also the source generator 
	 * knows the original variable names, which otherwise might get lost during the
	 * rewriting and unfolding process. Might not always necessary to
	 * implement
	 * @param head original head of the input query
	 */
	public void storeOrgQueryHead(Function head);
}
