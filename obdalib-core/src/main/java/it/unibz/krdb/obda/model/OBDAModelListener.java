package it.unibz.krdb.obda.model;

import java.io.Serializable;

public interface OBDAModelListener extends Serializable {

	public void datasourceAdded(OBDADataSource source);

	public void datasourceDeleted(OBDADataSource source);

	public void datasourceUpdated(String oldname, OBDADataSource currendata);

	public void alldatasourcesDeleted();

	public void datasourcParametersUpdated();
}
