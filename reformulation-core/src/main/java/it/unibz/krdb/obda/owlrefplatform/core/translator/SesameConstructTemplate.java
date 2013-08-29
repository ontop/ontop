package it.unibz.krdb.obda.owlrefplatform.core.translator;
import java.util.Set;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.algebra.Extension;
import org.openrdf.query.algebra.Projection;
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
		TupleExpr te = ((Projection) projection).getArg();
		if (te instanceof Extension) extension = te;
	}
	
	public Projection getProjection() {
		return (Projection) projection;
	}

	public Extension getExtension() {
		return (Extension) extension;
	}
}
