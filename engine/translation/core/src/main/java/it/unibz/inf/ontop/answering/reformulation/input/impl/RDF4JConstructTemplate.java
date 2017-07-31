package it.unibz.inf.ontop.answering.reformulation.input.impl;

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

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.answering.reformulation.input.ConstructTemplate;
import org.eclipse.rdf4j.query.algebra.*;
import org.eclipse.rdf4j.query.parser.ParsedQuery;

class RDF4JConstructTemplate implements ConstructTemplate {
    private TupleExpr projection = null;
	private TupleExpr extension = null;

	RDF4JConstructTemplate(ParsedQuery pq) {
		TupleExpr sesameAlgebra = pq.getTupleExpr();
		Reduced r = (Reduced) sesameAlgebra;
		projection = r.getArg();
		TupleExpr texpr;
		if (projection instanceof MultiProjection) {
			texpr = ((MultiProjection) projection).getArg();
		} else {
			texpr = ((Projection) projection).getArg();
		}
		if (texpr!= null && texpr instanceof Extension)
			extension = texpr;
	}


	@Override
	public ImmutableList<ProjectionElemList> getProjectionElemList() {
		if (projection instanceof Projection) {
			return ImmutableList.of(((Projection) projection).getProjectionElemList());
		}
		else if (projection instanceof MultiProjection) {
			return ImmutableList.copyOf(((MultiProjection) projection).getProjections());
		}
		else
			return ImmutableList.of();
	}

	@Override
	public Extension getExtension() {
		return (Extension) extension;
	}
}
