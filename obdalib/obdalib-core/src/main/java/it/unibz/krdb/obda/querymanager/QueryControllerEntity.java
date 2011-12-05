package it.unibz.krdb.obda.querymanager;

import java.io.Serializable;


public abstract class QueryControllerEntity implements Serializable {

	public abstract String getNodeName();

	public abstract String getID();

}
