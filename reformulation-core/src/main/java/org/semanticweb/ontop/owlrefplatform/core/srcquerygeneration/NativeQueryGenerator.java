package org.semanticweb.ontop.owlrefplatform.core.srcquerygeneration;

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

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import org.semanticweb.ontop.model.OBDAException;
import org.semanticweb.ontop.owlrefplatform.core.ExecutableQuery;
import org.semanticweb.ontop.owlrefplatform.core.translator.SesameConstructTemplate;
import org.semanticweb.ontop.pivotalrepr.IntermediateQuery;

/**
 * Generates a source query in a given native query language.
 *
 */
public interface NativeQueryGenerator extends Serializable {

	/**
	 * Translates the given datalog program into a source query, which can later
	 * be evaluated by a evaluation engine.
	 *
	 */
	public ExecutableQuery generateSourceQuery(IntermediateQuery query, ImmutableList<String> signature,
										   Optional<SesameConstructTemplate> optionalConstructTemplate) throws OBDAException;

	public boolean hasDistinctResultSet() ;
    /**
     * If the generator is immutable, the generator
     * can return itself instead of a clone.
     */
    public NativeQueryGenerator cloneIfNecessary();

	ExecutableQuery generateEmptyQuery(ImmutableList<String> signatureContainer, Optional<SesameConstructTemplate> optionalConstructTemplate);
}
