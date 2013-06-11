package sesameWrapper;

import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.model.TupleResultSet;
import it.unibz.krdb.obda.owlrefplatform.core.QuestDBConnection;
import it.unibz.krdb.obda.owlrefplatform.core.QuestDBStatement;
import java.util.LinkedList;
import java.util.List;

import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.TupleQueryResultHandler;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.impl.MapBindingSet;
import org.openrdf.query.impl.TupleQueryResultImpl;

public class SesameTupleQuery implements TupleQuery {

	private static final long serialVersionUID = 1L;

	private String queryString, baseURI;
	//private QuestDBStatement stm;
	private QuestDBConnection conn;
	private ValueFactory fact = new ValueFactoryImpl();
	private SesameAbstractRepo repo;
	
	public SesameTupleQuery(String queryString, String baseURI,
			QuestDBConnection conn) throws MalformedQueryException {
		if (queryString.toLowerCase().contains("select")) {
			this.queryString = queryString;
			this.baseURI = baseURI;
			this.conn = conn;
		} else
			throw new MalformedQueryException("Tuple query expected!");
	}
	
	// needed by TupleQuery interface
	public TupleQueryResult evaluate() throws QueryEvaluationException {
		TupleResultSet res = null;
		QuestDBStatement stm = null;
		try {
		
			stm = conn.createStatement();
			res = (TupleResultSet) stm.execute(queryString);
			
			List<String> signature = res.getSignature();
			List<BindingSet> results = new LinkedList<BindingSet>();

			while (res.nextRow()) {
				MapBindingSet set = new MapBindingSet(signature.size() * 2);
				for (String name : signature) {
					Binding binding = createBinding(name, res);
					if (binding != null) {
						set.addBinding(binding);
					}
				}
				results.add(set);
			}
			
			
			return new TupleQueryResultImpl(signature, results);

		} catch (OBDAException e) {
			e.printStackTrace();
			throw new QueryEvaluationException(e);
		}
		finally{
			try {
				if (res != null)
				res.close();
			} catch (OBDAException e) {
				e.printStackTrace();
			}
			try {
				if (stm != null)
				stm.close();
			} catch (OBDAException e) {
				e.printStackTrace();
			}
		}
	}

	private Binding createBinding(String bindingName, TupleResultSet set) {
		
		SesameBindingSet bset = new SesameBindingSet(set);
		return bset.getBinding(bindingName);
	}

	// needed by TupleQuery interface
	public void evaluate(TupleQueryResultHandler handler)
			throws QueryEvaluationException, TupleQueryResultHandlerException {

		TupleQueryResult result = evaluate();
		handler.startQueryResult(result.getBindingNames());
		while (result.hasNext())
			handler.handleSolution(result.next());
		handler.endQueryResult();

	}

	public int getMaxQueryTime() {
		return -1;
	}

	public void setMaxQueryTime(int maxQueryTime) {
	}

	public void clearBindings() {
		// TODO Auto-generated method stub

	}

	public BindingSet getBindings() {
		try {
			return evaluate().next();
		} catch (QueryEvaluationException e) {
			e.printStackTrace();
		}
		return null;
	}

	public Dataset getDataset() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean getIncludeInferred() {
		return true;
	}

	public void removeBinding(String name) {
		// TODO Auto-generated method stub

	}

	public void setBinding(String name, Value value) {
		// TODO Auto-generated method stub

	}

	public void setDataset(Dataset dataset) {
		// TODO Auto-generated method stub

	}

	public void setIncludeInferred(boolean includeInferred) {
		// always true

	}

}
