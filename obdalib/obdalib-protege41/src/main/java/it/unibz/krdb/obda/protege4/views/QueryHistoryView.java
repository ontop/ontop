package it.unibz.krdb.obda.protege4.views;

import it.unibz.krdb.obda.gui.swing.model.QueryhistoryController;

import java.awt.BorderLayout;

import javax.swing.JScrollPane;
import javax.swing.JTree;

import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;

public class QueryHistoryView extends AbstractOWLViewComponent {

	private static final long serialVersionUID = 8641739937602849648L;

	@Override
	protected void disposeOWLView() {
		// NO-OP
	}

	@Override
	protected void initialiseOWLView() throws Exception {
		setLayout(new BorderLayout());
		JTree queryHistory = new JTree();
		queryHistory.setModel(QueryhistoryController.getInstance().getTreeModel());
		JScrollPane scrollPane = new JScrollPane(queryHistory);
		scrollPane.setAutoscrolls(true);
		add(scrollPane, BorderLayout.CENTER);
	}
}
