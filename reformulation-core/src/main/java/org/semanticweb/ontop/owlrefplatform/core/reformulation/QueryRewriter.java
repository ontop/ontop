package org.semanticweb.ontop.owlrefplatform.core.reformulation;

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

import org.semanticweb.ontop.model.OBDAException;
import org.semanticweb.ontop.model.OBDAQuery;
import org.semanticweb.ontop.ontology.Ontology;

public interface QueryRewriter extends Serializable {

	public OBDAQuery rewrite(OBDAQuery input) throws OBDAException;

	/***
	 * Sets the ontology that this rewriter should use to compute any
	 * reformulation.
	 * 
	 * @param ontology
	 */
	public void setTBox(Ontology ontology);

	/**
	 * Sets the ABox dependencies that the reformulator can use to optimize the
	 * reformulations (if it is able to do so).
	 * 
	 * @param sigma
	 */
	public void setCBox(Ontology sigma);

	/***
	 * Initializes the rewriter. This method must be called before calling
	 * "rewrite" and after the TBox and CBox have been updated.
	 */
	public void initialize();

}
