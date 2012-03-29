package sesameWrapper;

import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.model.OBDAQuery;
import it.unibz.krdb.obda.model.OBDAQueryModifiers;
import it.unibz.krdb.obda.model.OBDAResultSet;
import it.unibz.krdb.obda.owlrefplatform.core.QuestDBStatement;
import it.unibz.krdb.obda.owlrefplatform.core.QuestStatement;

import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.TupleQueryResultHandler;
import org.openrdf.query.TupleQueryResultHandlerException;

public class SesameTupleQuery implements TupleQuery{
	
	private static final long serialVersionUID = 1L;
	
	private String queryString, baseURI;
	private QuestDBStatement stm;
	
	
	public SesameTupleQuery(String queryString, String baseURI, QuestDBStatement statement) throws MalformedQueryException
	{
		if (queryString.contains("SELECT"))
		{
			this.queryString = queryString;
			this.baseURI = baseURI;
			this.stm = statement;
		}
		else
			throw new MalformedQueryException("Tuple query expected!");
	}

	//needed by TupleQuery interface
	public TupleQueryResult evaluate() throws QueryEvaluationException {
		
		try {
			//execute query and return new type of result
			OBDAResultSet res = stm.execute(queryString);
			return new SesameTupleQueryResult(res);
			
		} catch (OBDAException e) {
			e.printStackTrace();
			throw new QueryEvaluationException(e.getMessage());
		}
	}

	//needed by TupleQuery interface
	public void evaluate(TupleQueryResultHandler handler)
			throws QueryEvaluationException, TupleQueryResultHandlerException {
		
		SesameTupleQueryResult result = (SesameTupleQueryResult) evaluate();
		handler.startQueryResult(result.getBindingNames());
		while (result.hasNext())
			handler.handleSolution(result.next());
		handler.endQueryResult();
		
	}

	public int getMaxQueryTime() {
		try {
			return stm.getQueryTimeout();
		} catch (OBDAException e) {
			e.printStackTrace();
		}
		return -1;
	}

	public void setMaxQueryTime(int maxQueryTime) {
		try {
			stm.setQueryTimeout(maxQueryTime);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
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
		//always true
		
	}

}
