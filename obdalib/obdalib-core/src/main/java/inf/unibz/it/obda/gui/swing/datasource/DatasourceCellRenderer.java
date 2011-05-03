package inf.unibz.it.obda.gui.swing.datasource;

import inf.unibz.it.obda.domain.DataSource;
import inf.unibz.it.obda.rdbmsgav.domain.RDBMSsourceParameterConstants;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

public class DatasourceCellRenderer extends JLabel implements
		ListCellRenderer {

	@Override
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		
		if (value == null) {
			setText("<Select a datasource>");
		}
		else {
			DataSource datasource = (DataSource)value;
			String datasourceUri = datasource.getSourceID().toString();
			setText(datasourceUri);
		}
		
		return this;
	}
}
