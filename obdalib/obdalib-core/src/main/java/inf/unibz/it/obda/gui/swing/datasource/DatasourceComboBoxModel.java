package inf.unibz.it.obda.gui.swing.datasource;

import inf.unibz.it.obda.domain.DataSource;
import inf.unibz.it.obda.rdbmsgav.domain.RDBMSsourceParameterConstants;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

public class DatasourceComboBoxModel extends AbstractListModel implements
		ComboBoxModel {

	private DataSource[] datasources;
	private DataSource selectedItem;
		
	public DatasourceComboBoxModel(DataSource[] datasources) {
		this.datasources = datasources;
	}
	
	@Override
	public int getSize() {
		return datasources.length;
	}

	@Override
	public DataSource getElementAt(int index) {
		if (index >= 0 && index < datasources.length)
			return datasources[index];
		return null;
	}

	@Override
	public void setSelectedItem(Object item) {
		selectedItem = (DataSource)item;
	}

	@Override
	public DataSource getSelectedItem() {
		return selectedItem;
	}

}
