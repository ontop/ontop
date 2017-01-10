package it.unibz.inf.ontop.protege.gui.preferences;

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


import it.unibz.inf.ontop.protege.core.DisposableProperties;
import org.protege.editor.owl.ui.preferences.OWLPreferencesPanel;
import it.unibz.inf.ontop.protege.panels.QuestConfigPanel;

import java.awt.*;

public class QuestPreferencesPanel extends OWLPreferencesPanel {

	private static final long	serialVersionUID	= 2017399622537704497L;

    @Override
	public void applyChanges() {
		// Do nothing.
	}

	@Override
	public void initialise() throws Exception {
		DisposableProperties preference = (DisposableProperties)getEditorKit().get(DisposableProperties.class.getName());
		
		this.setLayout(new BorderLayout());
        QuestConfigPanel configPanel = new QuestConfigPanel(preference);
		this.add(configPanel,BorderLayout.CENTER);
	}

	@Override
	public void dispose() throws Exception {
		// Do nothing.
	}
}
