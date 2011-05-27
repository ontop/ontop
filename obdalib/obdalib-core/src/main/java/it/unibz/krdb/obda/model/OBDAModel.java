package it.unibz.krdb.obda.model;

import it.unibz.krdb.obda.io.PrefixManager;
import it.unibz.krdb.obda.queryanswering.QueryController;

public interface OBDAModel {

	public abstract QueryController getQueryController();

	public abstract DatasourcesController getDatasourcesController();

	public abstract MappingController getMappingController();

	public abstract String getVersion();

	public abstract String getBuiltDate();

	public abstract String getBuiltBy();

	public abstract void setPrefixManager(PrefixManager prefman);

	public abstract PrefixManager getPrefixManager();

}