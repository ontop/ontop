package it.unibz.krdb.obda.gui.swing.utils;

import it.unibz.krdb.obda.model.DataSource;
public interface DatasourceSelectorListener
{
  public void datasourceChanged(DataSource oldSource, DataSource newSource);
}
