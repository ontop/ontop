package it.unibz.inf.ontop.protege.action;

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

import it.unibz.inf.ontop.protege.core.*;
import it.unibz.inf.ontop.protege.utils.DialogUtils;
import it.unibz.inf.ontop.protege.utils.SwingWorkerWithMonitor;
import it.unibz.inf.ontop.spec.mapping.serializer.impl.R2RMLMappingSerializer;
import org.protege.editor.core.ui.action.ProtegeAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;


public class R2RMLExportAction extends ProtegeAction {

	private static final long serialVersionUID = -1211395039869926309L;

    private static final Logger LOGGER = LoggerFactory.getLogger(R2RMLExportAction.class);

    private static final String DIALOG_TITLE = "R2RML Export";

	@Override
	public void actionPerformed(ActionEvent evt) {
        JFileChooser fc = DialogUtils.getFileChooser(getEditorKit(),
                DialogUtils.getExtensionReplacer("-mapping.ttl"));
        if (fc.showSaveDialog(getWorkspace()) != JFileChooser.APPROVE_OPTION)
            return;

        File file = fc.getSelectedFile();
        if (!DialogUtils.confirmCanWrite(file, getWorkspace(), DIALOG_TITLE))
            return;

        R2RMLExportWorker worker = new R2RMLExportWorker(file);
        worker.execute();
	}

    private class R2RMLExportWorker extends SwingWorkerWithMonitor<Void, Void> {
	    private final File file;
	    private final OBDAModel obdaModel;

        protected R2RMLExportWorker(File file) {
            super(getWorkspace(),
                    "<html><h3>Exporting R2RML mapping:</h3></html>", true);
            this.file = file;
            this.obdaModel = OBDAEditorKitSynchronizerPlugin.getCurrentOBDAModel(getEditorKit());
        }

        @Override
        protected Void doInBackground() throws Exception {
            start("initializing...");
            R2RMLMappingSerializer writer = new R2RMLMappingSerializer(
                    obdaModel.getOntopConfiguration().getRdfFactory());
            endLoop("writing to file...");
            writer.write(file, obdaModel.getTriplesMapManager().generatePPMapping());
            end();
            return null;
        }

        @Override
        public void done() {
            try {
                complete();
                DialogUtils.showInfoDialog(getWorkspace(),
                        "<html><h3>Export of R2RML mapping is complete.</h3><br></html>",
                        DIALOG_TITLE);
            }
            catch (CancellationException | InterruptedException e) {
                DialogUtils.showCancelledActionDialog(getWorkspace(), DIALOG_TITLE);
            }
            catch (ExecutionException e) {
                DialogUtils.showErrorDialog(getWorkspace(), DIALOG_TITLE, DIALOG_TITLE + " error.", LOGGER, e, obdaModel.getDataSource());
            }
        }
    }

    @Override
    public void initialise()  { /* NO-OP */ }

    @Override
    public void dispose()  { /* NO-OP */ }
}
