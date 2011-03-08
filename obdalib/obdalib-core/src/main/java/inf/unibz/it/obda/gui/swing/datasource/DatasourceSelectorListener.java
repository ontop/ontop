package inf.unibz.it.obda.gui.swing.datasource;

import inf.unibz.it.obda.domain.DataSource;

public interface DatasourceSelectorListener
{
  public void datasourceChanged(DataSource oldSource, DataSource newSource);
}
