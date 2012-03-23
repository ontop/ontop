package sesameWrapper;

import java.sql.SQLException;

import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.model.OBDAQuery;
import it.unibz.krdb.obda.model.OBDAQueryModifiers;
import it.unibz.krdb.obda.model.OBDAResultSet;
import it.unibz.krdb.obda.owlrefplatform.core.QuestDBStatement;
import it.unibz.krdb.obda.owlrefplatform.core.QuestStatement;

import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.Dataset;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;

public class SesameBooleanQuery implements BooleanQuery{

	private String queryString, baseURI;
	private QuestDBStatement stm;
	
	public SesameBooleanQuery(String queryString, String baseURI, QuestDBStatement questDBStatement) throws MalformedQueryException
	{
		//check if valid query string
		if (queryString.contains("ASK"))
		{
			this.queryString = queryString;
			this.baseURI = baseURI;
			this.stm = questDBStatement;
		  		
		}
		else
			throw new MalformedQueryException("Boolean Query expected!");
	}
	
	public boolean evaluate() throws QueryEvaluationException {
	
		try {
			OBDAResultSet rs = stm.execute(queryString);
			boolean next = rs.nextRow();
			if (next)
				 return (rs.getInt(1) == 1);
			
		} catch (OBDAException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
  		
		return false;
	}

	public int getMaxQueryTime() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void setMaxQueryTime(int maxQueryTime) {
		// TODO Auto-generated method stub
		
	}

	public void clearBindings() {
		// TODO Auto-generated method stub
		
	}

	public BindingSet getBindings() {
		// TODO Auto-generated method stub
		return null;
	}

	public Dataset getDataset() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean getIncludeInferred() {
		// TODO Auto-generated method stub
		return false;
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
		// TODO Auto-generated method stub
		
	}

	public OBDAQueryModifiers getQueryModifiers() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setQueryModifiers(OBDAQueryModifiers modifiers) {
		// TODO Auto-generated method stub
		
	}

}
