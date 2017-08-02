package it.unibz.inf.ontop.protege.views;

/*
 * #%L
 * ontop-protege
 * %%
 * Copyright (C) 2009 - 2013 KRDB Research Centre. Free University of Bozen Bolzano.
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

import it.unibz.inf.ontop.spec.mapping.pp.impl.SQLPPMappingImpl;
import it.unibz.inf.ontop.protege.core.OBDAModelManager;
import it.unibz.inf.ontop.protege.core.OBDAModelManagerListener;
import it.unibz.inf.ontop.protege.core.OBDAModel;
import it.unibz.inf.ontop.protege.panels.MappingAssistantPanel;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.border.TitledBorder;
import java.awt.*;

public class MappingAssistantView extends AbstractOWLViewComponent implements OBDAModelManagerListener {

    private static final long serialVersionUID = 1L;

    private OBDAModelManager obdaModelManager;

    private OBDAModel activeOBDAModel;

    private static final Logger log = LoggerFactory.getLogger(SQLQueryInterfaceView.class);

    @Override
    protected void disposeOWLView() {
        obdaModelManager.removeListener(this);
    }

    @Override
    protected void initialiseOWLView() throws Exception {

        obdaModelManager = (OBDAModelManager) getOWLEditorKit().get(SQLPPMappingImpl.class.getName());
        obdaModelManager.addListener(this);

        activeOBDAModel = obdaModelManager.getActiveOBDAModel();

        MappingAssistantPanel queryPanel = new MappingAssistantPanel(activeOBDAModel, obdaModelManager.getConfigurationManager(),
                getOWLModelManager());

        queryPanel.setBorder(new TitledBorder("SQL Query Editor"));

        setLayout(new BorderLayout());
        add(queryPanel, BorderLayout.NORTH);


        log.debug("SQL Query view initialized");
    }

    @Override
    public void activeOntologyChanged() {

       activeOBDAModel = obdaModelManager.getActiveOBDAModel();

    }
}
