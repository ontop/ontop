package sesameWrapper;

import it.unibz.krdb.obda.ontology.Assertion;

import java.util.*;

import org.openrdf.OpenRDFUtil;
import org.openrdf.model.BNode;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.util.*;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.helpers.RDFHandlerBase;

public class SesameRDFHandler extends RDFHandlerBase{
	
	private List<Statement> collector;
	
	public SesameRDFHandler() {
		collector = new LinkedList<Statement>();
	}
	
	@Override
	public void handleStatement(Statement st)
		throws RDFHandlerException
	{
		collector.add(st);
		
	}
	
	public Iterator<Statement> getStatementIterator()
	{
		return collector.iterator();
	}
	
	public SesameStatementIterator getAssertionIterator()
	{
		return new SesameStatementIterator(collector.iterator());
	}

}
