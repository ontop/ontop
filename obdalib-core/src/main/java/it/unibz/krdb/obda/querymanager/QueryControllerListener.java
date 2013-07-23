package it.unibz.krdb.obda.querymanager;

import java.io.Serializable;

public interface QueryControllerListener extends Serializable {

	public void elementAdded(QueryControllerEntity element);

	public void elementAdded(QueryControllerQuery query, QueryControllerGroup group);

	public void elementRemoved(QueryControllerEntity element);

	public void elementRemoved(QueryControllerQuery query, QueryControllerGroup group);

	public void elementChanged(QueryControllerQuery query);

	public void elementChanged(QueryControllerQuery query, QueryControllerGroup group);
}
