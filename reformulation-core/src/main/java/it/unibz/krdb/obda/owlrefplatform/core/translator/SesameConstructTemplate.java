package it.unibz.krdb.obda.owlrefplatform.core.translator;

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

import java.util.ArrayList;
import java.util.List;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.algebra.Extension;
import org.openrdf.query.algebra.MultiProjection;
import org.openrdf.query.algebra.Projection;
import org.openrdf.query.algebra.ProjectionElemList;
import org.openrdf.query.algebra.Reduced;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.QueryParser;
import org.openrdf.query.parser.QueryParserUtil;

public class SesameConstructTemplate {
	private TupleExpr sesameAlgebra = null;
	private TupleExpr projection = null;
	private TupleExpr extension = null;
	
	public SesameConstructTemplate(TupleExpr te) {
		this.sesameAlgebra = te;
	}
	
	public SesameConstructTemplate(String strquery) throws MalformedQueryException {
		QueryParser qp = QueryParserUtil.createParser(QueryLanguage.SPARQL);
		ParsedQuery pq = qp.parseQuery(strquery, null); // base URI is null
		this.sesameAlgebra = pq.getTupleExpr();
		Reduced r = (Reduced) sesameAlgebra;
		projection = r.getArg();
		TupleExpr texpr = null;
		if (projection instanceof MultiProjection) {
			 texpr = ((MultiProjection) projection).getArg();
		} else {
			 texpr = ((Projection) projection).getArg();
		}
		if (texpr!= null && texpr instanceof Extension) 
			extension = texpr;
		
	}
	
	public List<ProjectionElemList> getProjectionElemList() {
		List<ProjectionElemList> projElemList = new ArrayList<ProjectionElemList>();
		if (projection instanceof Projection) {
			projElemList.add(((Projection) projection).getProjectionElemList());
		}
		else if (projection instanceof MultiProjection) {
			projElemList = ((MultiProjection) projection).getProjections();
		}
		return projElemList;
	}

	public Extension getExtension() {
		return (Extension) extension;
	}
}
