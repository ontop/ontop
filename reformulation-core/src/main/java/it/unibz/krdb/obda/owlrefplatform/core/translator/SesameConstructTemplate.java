package it.unibz.krdb.obda.owlrefplatform.core.translator;

import java.util.ArrayList;
import java.util.List;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.algebra.Extension;
import org.openrdf.query.algebra.MultiProjection;
import org.openrdf.query.algebra.Projection;
import org.openrdf.query.algebra.ProjectionElem;
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
