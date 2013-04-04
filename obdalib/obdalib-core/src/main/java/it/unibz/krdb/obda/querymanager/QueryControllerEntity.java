package it.unibz.krdb.obda.querymanager;

import java.io.Serializable;

public abstract class QueryControllerEntity implements Serializable {

	private static final long serialVersionUID = -5241238894055210463L;

	public abstract String getNodeName();

	public abstract String getID();
}
