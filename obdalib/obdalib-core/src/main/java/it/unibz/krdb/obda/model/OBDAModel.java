package it.unibz.krdb.obda.model;

import it.unibz.krdb.obda.io.PrefixManager;
import it.unibz.krdb.obda.model.impl.OBDAModelImpl;
import it.unibz.krdb.obda.queryanswering.QueryController;

import java.net.URI;
import java.util.List;

public interface OBDAModel {

	public abstract QueryController getQueryController();

//	public abstract OBDAModelImpl getDatasourcesController();

	public abstract MappingController getMappingController();

	public abstract String getVersion();

	public abstract String getBuiltDate();

	public abstract String getBuiltBy();

	public abstract void setPrefixManager(PrefixManager prefman);

	public abstract PrefixManager getPrefixManager();
	
	/***
	 * DATASOURCES CONTROLLER
	 */

	public void addDataSource(DataSource source);

	public void addDataSource(String srcid);

	public void addDatasourceControllerListener(OBDAModelListener listener);

	public void fireAllDatasourcesDeleted();

	public void fireDatasourceAdded(DataSource source);

	public void fireDatasourceDeleted(DataSource source);

	public void fireParametersUpdated();

	public void fireDataSourceNameUpdated(URI old, DataSource neu);

	public List<DataSource> getAllSources();

	public DataSource getDataSource(URI name);

	public void removeDataSource(URI id);

	public void removeDatasourceControllerListener(OBDAModelListener listener);

	public void updateDataSource(URI id, DataSource dsd);
	
	public boolean containsDatasource(URI name);

}