package it.unibz.krdb.obda.gui.swing.utils;

import it.unibz.krdb.obda.model.OBDADataSource;

public interface DatasourceSelectorListener
{
	public void datasourceChanged(OBDADataSource oldSource, OBDADataSource newSource);
}
