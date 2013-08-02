/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.protege4.core;

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
