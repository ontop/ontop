package it.unibz.inf.ontop.protege.views;

/*
 * #%L
 * ontop-protege
 * %%
 * Copyright (C) 2009 - 2013 KRDB Research Centre. Free University of Bozen-Bolzano.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import it.unibz.inf.ontop.protege.panels.QueryInterfacePanel;
import it.unibz.inf.ontop.protege.panels.SavedQueriesPanelListener;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.semanticweb.owlapi.model.OWLOntologyChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class QueryInterfaceView extends AbstractOWLViewComponent implements SavedQueriesPanelListener {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LoggerFactory.getLogger(QueryInterfaceView.class);

    private QueryInterfacePanel queryEditorPanel;
    private OWLOntologyChangeListener ontologyListener;

    @Override
    protected void initialiseOWLView() {
        setLayout(new BorderLayout());
        queryEditorPanel = new QueryInterfacePanel(getOWLEditorKit());
        add(queryEditorPanel, BorderLayout.CENTER);

        // Setting up model listeners
        ontologyListener = changes ->
                SwingUtilities.invokeLater(() -> queryEditorPanel.setTableModel(new DefaultTableModel()));

        getOWLModelManager().addOntologyChangeListener(ontologyListener);

        /* On creation of a new view, we register it globally and make sure that its selector is listened
           by all other instances of query view in this editor kit. Also, we make this new instance listen
           to the selection of all other query selectors in the views. */

        // Getting the list of views
        QueryInterfaceViewsList queryInterfaceViews = (QueryInterfaceViewsList) getOWLEditorKit().get(QueryInterfaceViewsList.class.getName());
        if (queryInterfaceViews == null) {
            queryInterfaceViews = new QueryInterfaceViewsList();
            getOWLEditorKit().put(QueryInterfaceViewsList.class.getName(), queryInterfaceViews);
        }

        // Adding the new instance (this)
        queryInterfaceViews.add(this);

        // Registering the current query view with all existing query manager views
        QueryManagerViewsList queryManagerViews = (QueryManagerViewsList) getOWLEditorKit().get(QueryManagerViewsList.class.getName());
        if (queryManagerViews != null)
            for (QueryManagerView queryInterfaceView : queryManagerViews)
                queryInterfaceView.addListener(this);


        LOGGER.debug("Ontop QueryInterfaceView initialized");
    }

    @Override
    protected void disposeOWLView() {
        getOWLModelManager().removeOntologyChangeListener(ontologyListener);

        QueryInterfaceViewsList queryInterfaceViews = (QueryInterfaceViewsList) getOWLEditorKit().get(QueryInterfaceViewsList.class.getName());
        if (queryInterfaceViews != null)
            queryInterfaceViews.remove(this);

        QueryManagerViewsList queryManagerViews = (QueryManagerViewsList) getOWLEditorKit().get(QueryManagerViewsList.class.getName());
        if (queryManagerViews != null)
            for (QueryManagerView queryInterfaceView : queryManagerViews)
                queryInterfaceView.removeListener(this);
    }



    @Override
    public void selectedQueryChanged(String groupId, String queryId, String query) {
        queryEditorPanel.selectedQueryChanged(groupId, queryId, query);
    }
}
