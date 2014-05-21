package org.semanticweb.ontop.protege4.core;

/*
 * #%L
 * ontop-protege4
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

import org.protege.editor.owl.model.io.IOListenerEvent;
import org.protege.editor.owl.model.io.IOListenerPluginInstance;

@Deprecated
public class OBDAIOListenerPlugin extends IOListenerPluginInstance {

//	ProtegeManager			pmanager	= null;
//	EditorKitManager		ekmanager	= null;
//	WorkspaceManager		wsmanager	= null;
	
//	OBDAPluginController	apic		= null;

	
//	public OBDAIOListenerPlugin(OBDAPluginController apic) {
//		super();
//		this.apic = apic;
//	}

	@Override
	public void afterLoad(IOListenerEvent ioevent) {
		
		
		
//		List<Bundle> plugins = ProtegeApplication.getBundleManager().getPlugins();
//    	Bundle obdaBundle = null;
//    	for(Bundle plugin: plugins) {
//    		String name = plugin.getSymbolicName();
//    		if (name.equals("inf.unibz.it.obda.protege4")) {
//    			obdaBundle =  plugin;
//    		}
//    	}
//    	if (obdaBundle == null)
//    		throw new NullPointerException("Error initializing OBDA IO Plugin, couldnt find OBDA Bundle");
//    	
//    	ServiceReference apicServiceReference = obdaBundle.getBundleContext().getServiceReference(APIController.class.getName().getName());
//    	apic = (OBDAPluginController)obdaBundle.getBundleContext().getService(apicServiceReference);
//    	OWLEditorKit editorKit = getOWLEditorKit();
//		apic.setEditorKit(editorKit);
		
//		 URI uri = ioevent.getPhysicalURI();
//		 ((OBDAPluginController)getOWLEditorKit().get(APIController.class.getName())).loadData(uri);

	}

	@Override
	public void afterSave(IOListenerEvent ioevent) {
//		URI uri = ioevent.getPhysicalURI();
//		((OBDAPluginController)getOWLEditorKit().get(APIController.class.getName())).saveData(uri);
	}

	@Override
	public void beforeLoad(IOListenerEvent ioevent) {

	}

	@Override
	public void beforeSave(IOListenerEvent ioevent) {
	}

	public void dispose() throws Exception {
//		apic.removeListener();
	}

	public void initialise() throws Exception {
		
//		apic = getOWLEditorKit().get(APIController.class.getName());
//		
//		List<Bundle> plugins = ProtegeApplication.getBundleManager().getPlugins();
//    	Bundle obdaBundle = null;
//    	for(Bundle plugin: plugins) {
//    		String name = plugin.getSymbolicName();
//    		if (name.equals("inf.unibz.it.obda.protege4")) {
//    			obdaBundle =  plugin;
//    		}
//    	}
//    	if (obdaBundle == null)
//    		throw new Exception("Error initializing SQLQuery interface view, couldnt find OBDA Bundle");
//    	
//    	ServiceReference apicServiceReference = obdaBundle.getBundleContext().getServiceReference(APIController.class.getName().getName());
//    	apic = (OBDAPluginController)obdaBundle.getBundleContext().getService(apicServiceReference);
//		
//		pmanager = ProtegeManager.getInstance();
//		ekmanager = pmanager.getEditorKitManager();
//		wsmanager = ekmanager.getWorkspaceManager();
//		OWLEditorKit editorKit = getOWLEditorKit();
//		apic.setEditorKit(editorKit);
	}

}
