package inf.unibz.it.obda.gui.swing.utils;

import inf.unibz.it.obda.model.DataSource;
public interface DatasourceSelectorListener
{
  public void datasourceChanged(DataSource oldSource, DataSource newSource);
}
