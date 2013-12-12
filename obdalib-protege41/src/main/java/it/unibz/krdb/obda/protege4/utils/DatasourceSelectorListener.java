package it.unibz.krdb.obda.protege4.utils;

import it.unibz.krdb.obda.model.OBDADataSource;

public interface DatasourceSelectorListener
{
	public void datasourceChanged(OBDADataSource oldSource, OBDADataSource newSource);
}
