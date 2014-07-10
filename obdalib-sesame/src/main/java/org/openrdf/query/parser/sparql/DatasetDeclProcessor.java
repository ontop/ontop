/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.openrdf.query.parser.sparql;

import java.util.List;

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.SESAME;
import org.openrdf.query.Dataset;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.impl.DatasetImpl;
import org.openrdf.query.parser.sparql.ast.ASTDatasetClause;
import org.openrdf.query.parser.sparql.ast.ASTIRI;
import org.openrdf.query.parser.sparql.ast.ASTOperation;
import org.openrdf.query.parser.sparql.ast.ASTOperationContainer;

/**
 * Extracts a SPARQL {@link Dataset} from an ASTQueryContainer, if one is
 * contained.
 * 
 * @author Simon Schenk
 * @author Arjohn Kampman
 */
public class DatasetDeclProcessor {

	/**
	 * Extracts a SPARQL {@link Dataset} from an ASTQueryContainer, if one is
	 * contained. Returns null otherwise.
	 * 
	 * @param qc
	 *        The query model to resolve relative URIs in.
	 * @throws MalformedQueryException
	 *         If DatasetClause does not contain a valid URI.
	 */
	public static Dataset process(ASTOperationContainer qc)
		throws MalformedQueryException
	{
		DatasetImpl dataset = null;

		ASTOperation op = qc.getOperation();
		if (op != null) {

			List<ASTDatasetClause> datasetClauses = op.getDatasetClauseList();

			if (!datasetClauses.isEmpty()) {
				dataset = new DatasetImpl();

				for (ASTDatasetClause dc : datasetClauses) {

					ASTIRI astIri = dc.jjtGetChild(ASTIRI.class);

					try {
						URI uri = SESAME.NIL;
						
						if (astIri != null) {
							uri = new URIImpl(astIri.getValue());
						}
						
						if (dc.isNamed()) {
							dataset.addNamedGraph(uri);
						}
						else {
							dataset.addDefaultGraph(uri);
						}
					}
					catch (IllegalArgumentException e) {
						throw new MalformedQueryException(e.getMessage(), e);
					}
				}
			}
		}

		return dataset;
	}
}
