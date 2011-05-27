package inf.unibz.it.obda.model;

import inf.unibz.it.obda.io.PrefixManager;
import inf.unibz.it.obda.queryanswering.QueryController;

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