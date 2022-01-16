package it.unibz.inf.ontop.protege.query;

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

import org.protege.editor.core.Disposable;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class QueryInterfaceView extends AbstractOWLViewComponent {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LoggerFactory.getLogger(QueryInterfaceView.class);

    private QueryInterfacePanel panel;

    @Override
    protected void initialiseOWLView() {
        OWLEditorKit editorKit = getOWLEditorKit();

        setLayout(new BorderLayout());
        panel = new QueryInterfacePanel(editorKit);
        add(panel, BorderLayout.CENTER);

        getOWLModelManager().addOntologyChangeListener(panel);

        /* On creation of a new view, we register it globally and make sure that
           its selector is listened by all other instances of query view in this editor kit.
           Also, we make this new instance listen
           to the selection of all other query selectors in the views. */
        List<QueryInterfaceView> queryInterfaceViews = getList(editorKit);
        if (queryInterfaceViews.isEmpty())
            queryInterfaceViews = new QueryInterfaceViewsList(editorKit);

        queryInterfaceViews.add(this);

        // Registering the current query view with ALL existing query manager views
        for (QueryManagerView queryInterfaceView : QueryManagerView.getList(editorKit))
            queryInterfaceView.addSelectionListener(getSelectionListener());

        LOGGER.debug("Ontop QueryInterfaceView initialized");
    }

    @Override
    protected void disposeOWLView() {
        getOWLModelManager().removeOntologyChangeListener(panel);

        List<QueryInterfaceView> queryInterfaceViews = getList(getOWLEditorKit());
        queryInterfaceViews.remove(this);

        for (QueryManagerView queryInterfaceView : QueryManagerView.getList(getOWLEditorKit()))
            queryInterfaceView.removeSelectionListener(getSelectionListener());
    }

    public QueryManagerPanelSelectionListener getSelectionListener() {
        return panel;
    }


    @Nonnull
    public static List<QueryInterfaceView> getList(OWLEditorKit editorKit) {
        QueryInterfaceViewsList list = (QueryInterfaceViewsList) editorKit.get(QueryInterfaceViewsList.class.getName());
        return list == null ? Collections.emptyList() : list;
    }

    private static class QueryInterfaceViewsList extends ArrayList<QueryInterfaceView> implements Disposable {

        private static final long serialVersionUID = -7082548696764069555L;

        private QueryInterfaceViewsList(OWLEditorKit editorKit) {
            editorKit.put(QueryInterfaceViewsList.class.getName(), this);
        }

        @Override
        public void dispose() { /* NO-OP */ }
    }
}
