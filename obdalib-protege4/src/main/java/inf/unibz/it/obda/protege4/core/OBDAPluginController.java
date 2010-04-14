package inf.unibz.it.obda.protege4.core;

import inf.unibz.it.dl.assertion.Assertion;
import inf.unibz.it.dl.codec.xml.AssertionXMLCodec;
import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.api.controller.AssertionController;
import inf.unibz.it.obda.api.controller.AssertionControllerListener;
import inf.unibz.it.obda.api.controller.DatasourcesControllerListener;
import inf.unibz.it.obda.api.controller.MappingControllerListener;
import inf.unibz.it.obda.api.controller.QueryControllerEntity;
import inf.unibz.it.obda.api.controller.QueryControllerListener;
import inf.unibz.it.obda.constraints.AbstractConstraintAssertionController;
import inf.unibz.it.obda.dependencies.AbstractDependencyAssertionController;
import inf.unibz.it.obda.domain.OBDAMappingAxiom;
import inf.unibz.it.obda.gui.swing.preferences.OBDAPreferences;
import inf.unibz.it.obda.gui.swing.preferences.OBDAPreferences.MappingManagerPreferences;
import inf.unibz.it.obda.gui.swing.querycontroller.tree.QueryControllerGroup;
import inf.unibz.it.obda.gui.swing.querycontroller.tree.QueryControllerQuery;
import inf.unibz.it.obda.owlapi.OBDAOWLReasonerFactory;
import inf.unibz.it.obda.protege4.plugin.AssertionControllerFactoryPlugin;
import inf.unibz.it.obda.protege4.plugin.AssertionControllerFactoryPluginInstance;
import inf.unibz.it.obda.protege4.plugin.AssertionControllerFactoryPluginLoader;

import java.awt.Color;
import java.io.File;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.protege.editor.core.Disposable;
import org.protege.editor.core.ProtegeApplication;
import org.protege.editor.core.ProtegeManager;
import org.protege.editor.core.editorkit.EditorKit;
import org.protege.editor.core.editorkit.EditorKitManager;
import org.protege.editor.core.prefs.Preferences;
import org.protege.editor.core.prefs.PreferencesManager;
import org.protege.editor.core.ui.workspace.WorkspaceManager;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.event.EventType;
import org.protege.editor.owl.model.event.OWLModelManagerChangeEvent;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.protege.editor.owl.model.inference.ProtegeOWLReasonerFactory;
import org.semanticweb.owl.model.AddAxiom;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.RemoveAxiom;

public class OBDAPluginController extends APIController implements Disposable {

	OWLOntology currentOntology = null;
	URI currentOntologyPhysicalURI = null;

	ProtegeManager pmanager = null;
	EditorKitManager ekmanager = null;
	WorkspaceManager wsmanager = null;

	OWLEditorKit owlEditorKit = null;

	public OBDAPluginController(EditorKit editorKit) {
		super();

		// loading JDBC Drivers

		// OBDAPluginController.class.getClassLoader().

		if (!(editorKit instanceof OWLEditorKit)) {
			throw new IllegalArgumentException(
					"Received non OWLEditorKit editor kit");
		}
		this.owlEditorKit = (OWLEditorKit) editorKit;

		// registerAsListener(owlEditorKit);

		apicoupler = new OWLAPICoupler(this, owlEditorKit.getOWLModelManager()
				.getEntityFinder());
		setCoupler(apicoupler);

		/***
		 * Setting up the current reasoner factories to have a reference to this
		 * OBDA Plugin controller
		 */
		Set<ProtegeOWLReasonerFactory> factories = owlEditorKit
				.getOWLWorkspace().getOWLModelManager().getOWLReasonerManager()
				.getInstalledReasonerFactories();
		for (ProtegeOWLReasonerFactory protegeOWLReasonerFactory : factories) {
			if (protegeOWLReasonerFactory instanceof OBDAOWLReasonerFactory) {
				OBDAOWLReasonerFactory obdaFactory = (OBDAOWLReasonerFactory) protegeOWLReasonerFactory;
				obdaFactory.setOBDAController(this);
			}
		}

		// pmanager = ProtegeManager.getInstance();
		// ekmanager = pmanager.getEditorKitManager();
		//		
		//		
		//		
		// wsmanager = ekmanager.getWorkspaceManager();
		//		

		// setController(this);

		/***
		 * Adding standard listeners
		 */

		this.getDatasourcesController().addDatasourceControllerListener(
				new DatasourcesControllerListener() {

					public void alldatasourcesDeleted() {
						triggerOntologyChanged();

					}

					public void currentDatasourceChange(
							inf.unibz.it.obda.domain.DataSource previousdatasource,
							inf.unibz.it.obda.domain.DataSource currentsource) {
					}

					public void datasourceAdded(
							inf.unibz.it.obda.domain.DataSource source) {
						triggerOntologyChanged();

					}

					public void datasourceDeleted(
							inf.unibz.it.obda.domain.DataSource source) {
						triggerOntologyChanged();

					}

					public void datasourceUpdated(String oldname,
							inf.unibz.it.obda.domain.DataSource currendata) {
						triggerOntologyChanged();

					}

				});

		this.getMappingController().addMappingControllerListener(
				new MappingControllerListener() {

					public void allMappingsRemoved() {
						triggerOntologyChanged();
					}

					public void currentSourceChanged(String oldsrcuri,
							String newsrcuri) {

					}

					public void mappingDeleted(String srcuri, String mapping_id) {
						triggerOntologyChanged();
					}

					public void mappingInserted(String srcuri, String mapping_id) {
						triggerOntologyChanged();
					}

					public void mappingUpdated(String srcuri,
							String mapping_id, OBDAMappingAxiom mapping) {
						triggerOntologyChanged();
					}

				});

		queryController.addListener(new QueryControllerListener() {

			public void elementAdded(QueryControllerEntity element) {
				triggerOntologyChanged();

			}

			public void elementAdded(QueryControllerQuery query,
					QueryControllerGroup group) {
				triggerOntologyChanged();

			}

			public void elementRemoved(QueryControllerEntity element) {
				triggerOntologyChanged();

			}

			public void elementRemoved(QueryControllerQuery query,
					QueryControllerGroup group) {
				triggerOntologyChanged();

			}

			public void elementChanged(QueryControllerQuery query) {
				triggerOntologyChanged();

			}

			public void elementChanged(QueryControllerQuery query,
					QueryControllerGroup group) {
				triggerOntologyChanged();

			}

		});

		/***
		 * Looking for intances of AssertionControllerFactory Plugins
		 */
		loadAssertionControllerFactoryPlugins();
		loadPreferences();
	}

	private void loadAssertionControllerFactoryPlugins() {

		AssertionControllerListener<Assertion> defaultAssertionControllerListener = new AssertionControllerListener<Assertion>() {

			public void assertionAdded(Assertion assertion) {
				triggerOntologyChanged();

			}

			public void assertionChanged(Assertion oldAssertion,
					Assertion newAssertion) {
				triggerOntologyChanged();

			}

			public void assertionRemoved(Assertion assertion) {
				triggerOntologyChanged();

			}

			public void assertionsCleared() {
				triggerOntologyChanged();

			}

		};

		AssertionControllerFactoryPluginLoader loader = new AssertionControllerFactoryPluginLoader();
		for (AssertionControllerFactoryPlugin pl : loader.getPlugins()) {
			try {
				AssertionControllerFactoryPluginInstance instance = pl
						.newInstance();
				Class assertionClass = instance.getAssertionClass();
				AssertionController controller = instance
						.getControllerInstance();
				AssertionXMLCodec xmlCodec = instance.getXMLCodec();
				boolean triggerUpddate = instance.triggersOntologyChanged();
				if (triggerUpddate) {
					controller
							.addControllerListener(defaultAssertionControllerListener);
				}
				addAssertionController(assertionClass, controller, xmlCodec);
				if (controller instanceof AbstractDependencyAssertionController) {
					AbstractDependencyAssertionController depController = (AbstractDependencyAssertionController) controller;
					dscontroller.addDatasourceControllerListener(depController);
				}
				if (controller instanceof AbstractConstraintAssertionController) {
					AbstractConstraintAssertionController depController = (AbstractConstraintAssertionController) controller;
					dscontroller.addDatasourceControllerListener(depController);
				}
			} catch (Throwable e) {
				ProtegeApplication.getErrorLog().logError(e);
			}
		}
	}

	@Override
	public File getCurrentOntologyFile() {
		currentOntologyPhysicalURI = owlEditorKit.getOWLModelManager()
				.getOntologyPhysicalURI(
						owlEditorKit.getOWLModelManager().getActiveOntology());

		if (currentOntologyPhysicalURI == null)
			return null;
		File owlfile = new File(currentOntologyPhysicalURI);
		File obdafile = new File(ioManager.getOBDAFile(owlfile.toURI()));
		return obdafile;
	}

	// public void setEditorKit(OWLEditorKit ek) {
	// if (owlEditorKit != ek) {
	//			
	// //
	// setCurrentOntologyURI(owlEditorKit.getOWLModelManager().getActiveOntology().getURI());
	// }
	//
	// }

	// private void registerAsListener(OWLEditorKit owlek) {
	//
	// final OWLModelManager owlmm = owlek.getOWLModelManager();
	// owlmm.a
	//		
	//
	// }

	private OWLModelManagerListener modelManagerListener = new OWLModelManagerListener() {

		public void handleChange(OWLModelManagerChangeEvent event) {
			// System.out.println("HANDLEEEEEEEEEEEEEEEEEEEEEEEE");
			EventType type = event.getType();
			OWLModelManager source = event.getSource();
			OWLOntology ontology = owlEditorKit.getOWLModelManager()
					.getActiveOntology();
			// URI
			// uri
			// =
			// ontology.getURI();

			switch (type) {
			case ABOUT_TO_CLASSIFY:
				break;
			case ENTITY_RENDERER_CHANGED:
				break;
			case ONTOLOGY_CLASSIFIED:
				break;
			case ACTIVE_ONTOLOGY_CHANGED:
				OBDAPluginController.this.currentOntology = ontology;
				OBDAPluginController.this.currentOntologyURI = ontology
						.getURI();
				loadData(source.getOntologyPhysicalURI(ontology));
				break;
			case ENTITY_RENDERING_CHANGED:
				break;
			case ONTOLOGY_CREATED:
				break;
			case ONTOLOGY_LOADED:
				break;
			case ONTOLOGY_SAVED:
				break;
			case ONTOLOGY_VISIBILITY_CHANGED:
				break;
			case REASONER_CHANGED:
				break;
			}
		}
	};

	private OWLAPICoupler apicoupler;
	private boolean loadingData;

	// public void removeListener() {
	//		
	//
	// // final OWLModelManager owlmm = owlEditorKit.getOWLModelManager();
	// // owlmm.addListener(modelManagerListener);
	// }

	private void triggerOntologyChanged() {
		if (!this.loadingData) {
			OWLModelManager owlmm = owlEditorKit.getOWLModelManager();
			OWLOntology ontology = owlmm.getActiveOntology();

			if (ontology != null) {
				OWLClass newClass = owlmm.getOWLDataFactory().getOWLClass(
						URI.create(ontology.getURI()
								+ "#RandomMarianoTest6677841155"));
				OWLAxiom axiom = owlmm.getOWLDataFactory()
						.getOWLDeclarationAxiom(newClass);

				AddAxiom addChange = new AddAxiom(ontology, axiom);
				owlmm.applyChange(addChange);

				RemoveAxiom removeChange = new RemoveAxiom(ontology, axiom);
				owlmm.applyChange(removeChange);
			}
		}
	}

	public void setupModelManagerListener() {
		OWLModelManager owlmm = owlEditorKit.getModelManager();
		owlmm.addListener(modelManagerListener);
	}

	public void removeModelManagerListener() {
		owlEditorKit.getModelManager().removeListener(modelManagerListener);
	}

	/***
	 * Called from ModelManager dispose method since this object is setup as the
	 * APIController.class.getName() property with the put method.
	 */
	public void dispose() throws Exception {
		owlEditorKit.getModelManager().removeListener(modelManagerListener);

	}

	public void loadData(URI owlFile) {
		loadingData = true;
		try {
			URI obdafile = getIOManager().getOBDAFile(owlFile);
			getIOManager().loadOBDADataFromURI(obdafile);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			loadingData = false;
		}
	}

	public void saveData(URI owlFile) {
		// TODO fix exception handling
		Logger.getLogger(OBDAPluginController.class).info(
				"Saving OBDA data for " + owlFile.toString());
		URI obdafile = getIOManager().getOBDAFile(owlFile);
		try {
			getIOManager().saveOBDAData(obdafile);
		} catch (Exception e) {
			e.printStackTrace();
			triggerOntologyChanged();
			Logger.getLogger(OBDAPluginController.class).error(e);
		}

	}

	@Override
	public Set<URI> getOntologyURIs() {
		HashSet<URI> uris = new HashSet<URI>();
		Set<OWLOntology> ontologies = owlEditorKit.getModelManager()
				.getOntologies();
		for (OWLOntology owlOntology : ontologies) {
			uris.add(owlOntology.getURI());
		}
		return uris;
	}

	private void loadPreferences() {

		PreferencesManager man = PreferencesManager.getInstance();
		Preferences pref = man.getApplicationPreferences("OBDA Plugin");
		MappingManagerPreferences mmp = OBDAPreferences.getOBDAPreferences()
				.getMappingsPreference();

		Color colClass = mmp.getColor(MappingManagerPreferences.CLASS_COLOR);
		int rgb0 = pref.getInt(MappingManagerPreferences.CLASS_COLOR, colClass
				.getRGB());
		mmp.setColor(MappingManagerPreferences.CLASS_COLOR, new Color(rgb0));

		Color colOP = mmp
				.getColor(MappingManagerPreferences.OBJECTPROPTERTY_COLOR);
		int rgb1 = pref.getInt(MappingManagerPreferences.OBJECTPROPTERTY_COLOR,
				colOP.getRGB());
		mmp.setColor(MappingManagerPreferences.OBJECTPROPTERTY_COLOR,
				new Color(rgb1));

		Color colDP = mmp
				.getColor(MappingManagerPreferences.DATAPROPERTY_COLOR);
		int rgb2 = pref.getInt(MappingManagerPreferences.DATAPROPERTY_COLOR,
				colDP.getRGB());
		mmp.setColor(MappingManagerPreferences.DATAPROPERTY_COLOR, new Color(
				rgb2));

		Color colPar = mmp.getColor(MappingManagerPreferences.PARAMETER_COLOR);
		int rgb3 = pref.getInt(MappingManagerPreferences.PARAMETER_COLOR,
				colPar.getRGB());
		mmp
				.setColor(MappingManagerPreferences.PARAMETER_COLOR, new Color(
						rgb3));

		Color colFunc = mmp.getColor(MappingManagerPreferences.FUCNTOR_COLOR);
		int rgb4 = pref.getInt(MappingManagerPreferences.FUCNTOR_COLOR, colFunc
				.getRGB());
		mmp.setColor(MappingManagerPreferences.FUCNTOR_COLOR, new Color(rgb4));

		Color colVar = mmp.getColor(MappingManagerPreferences.VARIABLE_COLOR);
		int rgb5 = pref.getInt(MappingManagerPreferences.VARIABLE_COLOR, colVar
				.getRGB());
		mmp.setColor(MappingManagerPreferences.VARIABLE_COLOR, new Color(rgb5));

		Color colIQ = mmp
				.getColor(MappingManagerPreferences.INVALIDQUERY_COLOR);
		int rgb8 = pref.getInt(MappingManagerPreferences.INVALIDQUERY_COLOR,
				colIQ.getRGB());
		mmp.setColor(MappingManagerPreferences.INVALIDQUERY_COLOR, new Color(
				rgb8));

		Color colDep = mmp
				.getColor(MappingManagerPreferences.DEPENDENCIES_COLOR);
		int rgb9 = pref.getInt(MappingManagerPreferences.DEPENDENCIES_COLOR,
				colDep.getRGB());
		mmp.setColor(MappingManagerPreferences.DEPENDENCIES_COLOR, new Color(
				rgb9));

		// Color colBody =
		// mmp.getColor(MappingManagerPreferences.MAPPING_BODY_COLOR);
		// int rgb6 = pref.getInt(MappingManagerPreferences.MAPPING_BODY_COLOR,
		// colBody.getRGB());
		// mmp.setColor(MappingManagerPreferences.MAPPING_BODY_COLOR, new
		// Color(rgb6));
		//		
		// Color colID =
		// mmp.getColor(MappingManagerPreferences.MAPPING_ID_COLOR);
		// int rgb7 = pref.getInt(MappingManagerPreferences.MAPPING_ID_COLOR,
		// colID.getRGB());
		// mmp.setColor(MappingManagerPreferences.MAPPING_ID_COLOR, new
		// Color(rgb7));

		int classSize = mmp
				.getFontSize(MappingManagerPreferences.CLASS_FONTSIZE);
		int size0 = pref.getInt(MappingManagerPreferences.CLASS_FONTSIZE,
				classSize);
		mmp.setFontSize(MappingManagerPreferences.CLASS_FONTSIZE, size0);

		int OPSize = mmp
				.getFontSize(MappingManagerPreferences.OBJECTPROPTERTY_FONTSIZE);
		int size1 = pref.getInt(
				MappingManagerPreferences.OBJECTPROPTERTY_FONTSIZE, OPSize);
		mmp.setFontSize(MappingManagerPreferences.OBJECTPROPTERTY_FONTSIZE,
				size1);

		int DPSize = mmp
				.getFontSize(MappingManagerPreferences.DATAPROPERTY_FONTSIZE);
		int size2 = pref.getInt(
				MappingManagerPreferences.DATAPROPERTY_FONTSIZE, DPSize);
		mmp.setFontSize(MappingManagerPreferences.DATAPROPERTY_FONTSIZE, size2);

		int parSize = mmp
				.getFontSize(MappingManagerPreferences.PARAMETER_FONTSIZE);
		int size3 = pref.getInt(MappingManagerPreferences.PARAMETER_FONTSIZE,
				parSize);
		mmp.setFontSize(MappingManagerPreferences.PARAMETER_FONTSIZE, size3);

		int funSize = mmp
				.getFontSize(MappingManagerPreferences.FUCNTOR_FONTSIZE);
		int size4 = pref.getInt(MappingManagerPreferences.FUCNTOR_FONTSIZE,
				funSize);
		mmp.setFontSize(MappingManagerPreferences.FUCNTOR_FONTSIZE, size4);

		int varSize = mmp
				.getFontSize(MappingManagerPreferences.VARIABLE_FONTSIZE);
		int size5 = pref.getInt(MappingManagerPreferences.VARIABLE_FONTSIZE,
				varSize);
		mmp.setFontSize(MappingManagerPreferences.VARIABLE_FONTSIZE, size5);

		int bodySize = mmp
				.getFontSize(MappingManagerPreferences.MAPPING_BODY_FONTSIZE);
		int size6 = pref.getInt(
				MappingManagerPreferences.MAPPING_BODY_FONTSIZE, bodySize);
		mmp.setFontSize(MappingManagerPreferences.MAPPING_BODY_FONTSIZE, size6);

		int idSize = mmp
				.getFontSize(MappingManagerPreferences.MAPPING_ID_FONTSIZE);
		int size7 = pref.getInt(MappingManagerPreferences.MAPPING_ID_FONTSIZE,
				idSize);
		mmp.setFontSize(MappingManagerPreferences.MAPPING_ID_FONTSIZE, size7);

		int iqSize = mmp
				.getFontSize(MappingManagerPreferences.INVALIDQUERY_FONTSIZE);
		int size8 = pref.getInt(
				MappingManagerPreferences.INVALIDQUERY_FONTSIZE, iqSize);
		mmp.setFontSize(MappingManagerPreferences.INVALIDQUERY_FONTSIZE, size8);

		int depSize = mmp
				.getFontSize(MappingManagerPreferences.DEPENDENCIES_FONTSIZE);
		int size9 = pref.getInt(
				MappingManagerPreferences.DEPENDENCIES_FONTSIZE, depSize);
		mmp.setFontSize(MappingManagerPreferences.DEPENDENCIES_FONTSIZE, size9);

		String classFF = mmp
				.getFontFamily(MappingManagerPreferences.CLASS_FONTFAMILY);
		String ff0 = pref.getString(MappingManagerPreferences.CLASS_FONTFAMILY,
				classFF);
		mmp.setFontFamily(MappingManagerPreferences.CLASS_FONTFAMILY, ff0);

		String opFF = mmp
				.getFontFamily(MappingManagerPreferences.OBJECTPROPTERTY_FONTFAMILY);
		String ff1 = pref.getString(
				MappingManagerPreferences.OBJECTPROPTERTY_FONTFAMILY, opFF);
		mmp.setFontFamily(MappingManagerPreferences.OBJECTPROPTERTY_FONTFAMILY,
				ff1);

		String dpFF = mmp
				.getFontFamily(MappingManagerPreferences.DATAPROPERTY_FONTFAMILY);
		String ff2 = pref.getString(
				MappingManagerPreferences.DATAPROPERTY_FONTFAMILY, dpFF);
		mmp.setFontFamily(MappingManagerPreferences.DATAPROPERTY_FONTFAMILY,
				ff2);

		String parFF = mmp
				.getFontFamily(MappingManagerPreferences.PARAMETER_FONTFAMILY);
		String ff3 = pref.getString(
				MappingManagerPreferences.PARAMETER_FONTFAMILY, parFF);
		mmp.setFontFamily(MappingManagerPreferences.PARAMETER_FONTFAMILY, ff3);

		String funcFF = mmp
				.getFontFamily(MappingManagerPreferences.FUCNTOR_FONTFAMILY);
		String ff4 = pref.getString(
				MappingManagerPreferences.FUCNTOR_FONTFAMILY, funcFF);
		mmp.setFontFamily(MappingManagerPreferences.FUCNTOR_FONTFAMILY, ff4);

		String varFF = mmp
				.getFontFamily(MappingManagerPreferences.VARIABLE_FONTFAMILY);
		String ff5 = pref.getString(
				MappingManagerPreferences.VARIABLE_FONTFAMILY, varFF);
		mmp.setFontFamily(MappingManagerPreferences.VARIABLE_FONTFAMILY, ff5);

		String bodyFF = mmp
				.getFontFamily(MappingManagerPreferences.MAPPING_BODY_FONTFAMILY);
		String ff6 = pref.getString(
				MappingManagerPreferences.MAPPING_BODY_FONTFAMILY, bodyFF);
		mmp.setFontFamily(MappingManagerPreferences.MAPPING_BODY_FONTFAMILY,
				ff6);

		String idFF = mmp
				.getFontFamily(MappingManagerPreferences.MAPPING_ID_FONTFAMILY);
		String ff7 = pref.getString(
				MappingManagerPreferences.MAPPING_ID_FONTFAMILY, idFF);
		mmp.setFontFamily(MappingManagerPreferences.MAPPING_ID_FONTFAMILY, ff7);

		String iqFF = mmp
				.getFontFamily(MappingManagerPreferences.INVALIDQUERY_FONTFAMILY);
		String ff8 = pref.getString(
				MappingManagerPreferences.INVALIDQUERY_FONTFAMILY, iqFF);
		mmp.setFontFamily(MappingManagerPreferences.INVALIDQUERY_FONTFAMILY,
				ff8);

		String depFF = mmp
				.getFontFamily(MappingManagerPreferences.DEPENDENCIES_FONTFAMILY);
		String ff9 = pref.getString(
				MappingManagerPreferences.DEPENDENCIES_FONTFAMILY, depFF);
		mmp.setFontFamily(MappingManagerPreferences.DEPENDENCIES_FONTFAMILY,
				ff9);

		Boolean classIsBold = mmp
				.isBold(MappingManagerPreferences.CLASS_ISBOLD);
		boolean bold0 = pref.getBoolean(MappingManagerPreferences.CLASS_ISBOLD,
				classIsBold.booleanValue());
		mmp.setIsBold(MappingManagerPreferences.CLASS_ISBOLD,
				new Boolean(bold0));

		Boolean opIsBold = mmp
				.isBold(MappingManagerPreferences.OBJECTPROPTERTY_ISBOLD);
		boolean bold1 = pref.getBoolean(
				MappingManagerPreferences.OBJECTPROPTERTY_ISBOLD, opIsBold
						.booleanValue());
		mmp.setIsBold(MappingManagerPreferences.OBJECTPROPTERTY_ISBOLD,
				new Boolean(bold1));

		Boolean dpIsBold = mmp
				.isBold(MappingManagerPreferences.DATAPROPERTY_ISBOLD);
		boolean bold2 = pref.getBoolean(
				MappingManagerPreferences.DATAPROPERTY_ISBOLD, dpIsBold
						.booleanValue());
		mmp.setIsBold(MappingManagerPreferences.DATAPROPERTY_ISBOLD,
				new Boolean(bold2));

		Boolean parIsBold = mmp
				.isBold(MappingManagerPreferences.PARAMETER_ISBOLD);
		boolean bold3 = pref.getBoolean(
				MappingManagerPreferences.PARAMETER_ISBOLD, parIsBold
						.booleanValue());
		mmp.setIsBold(MappingManagerPreferences.PARAMETER_ISBOLD, new Boolean(
				bold3));

		Boolean funIsBold = mmp
				.isBold(MappingManagerPreferences.FUCNTOR_ISBOLD);
		boolean bold4 = pref.getBoolean(
				MappingManagerPreferences.FUCNTOR_ISBOLD, funIsBold
						.booleanValue());
		mmp.setIsBold(MappingManagerPreferences.FUCNTOR_ISBOLD, new Boolean(
				bold4));

		Boolean varIsBold = mmp
				.isBold(MappingManagerPreferences.VARIABLE_ISBOLD);
		boolean bold5 = pref.getBoolean(
				MappingManagerPreferences.VARIABLE_ISBOLD, varIsBold
						.booleanValue());
		mmp.setIsBold(MappingManagerPreferences.VARIABLE_ISBOLD, new Boolean(
				bold5));

		Boolean bodyIsBold = mmp
				.isBold(MappingManagerPreferences.MAPPING_BODY_ISBOLD);
		boolean bold6 = pref.getBoolean(
				MappingManagerPreferences.MAPPING_BODY_ISBOLD, bodyIsBold
						.booleanValue());
		mmp.setIsBold(MappingManagerPreferences.MAPPING_BODY_ISBOLD,
				new Boolean(bold6));

		Boolean idIsBold = mmp
				.isBold(MappingManagerPreferences.MAPPING_ID_ISBOLD);
		boolean bold7 = pref.getBoolean(
				MappingManagerPreferences.MAPPING_ID_ISBOLD, idIsBold
						.booleanValue());
		mmp.setIsBold(MappingManagerPreferences.MAPPING_ID_ISBOLD, new Boolean(
				bold7));

		Boolean iqIsBold = mmp
				.isBold(MappingManagerPreferences.INVALIDQUERY_ISBOLD);
		boolean bold8 = pref.getBoolean(
				MappingManagerPreferences.INVALIDQUERY_ISBOLD, iqIsBold
						.booleanValue());
		mmp.setIsBold(MappingManagerPreferences.INVALIDQUERY_ISBOLD,
				new Boolean(bold8));

		Boolean depIsBold = mmp
				.isBold(MappingManagerPreferences.DEPENDENCIES_ISBOLD);
		boolean bold9 = pref.getBoolean(
				MappingManagerPreferences.DEPENDENCIES_ISBOLD, depIsBold
						.booleanValue());
		mmp.setIsBold(MappingManagerPreferences.DEPENDENCIES_ISBOLD,
				new Boolean(bold9));

		String add = mmp.getShortCut(MappingManagerPreferences.ADD_MAPPING);
		String aux1 = pref
				.getString(MappingManagerPreferences.ADD_MAPPING, add);
		mmp.setFontFamily(MappingManagerPreferences.ADD_MAPPING, aux1);

		String delete = mmp
				.getShortCut(MappingManagerPreferences.DELETE_MAPPING);
		String aux2 = pref.getString(MappingManagerPreferences.DELETE_MAPPING,
				delete);
		mmp.setFontFamily(MappingManagerPreferences.DELETE_MAPPING, aux2);

		String editBody = mmp.getShortCut(MappingManagerPreferences.EDIT_BODY);
		String aux3 = pref.getString(MappingManagerPreferences.EDIT_BODY,
				editBody);
		mmp.setFontFamily(MappingManagerPreferences.EDIT_BODY, aux3);

		String editHead = mmp.getShortCut(MappingManagerPreferences.EDIT_HEAD);
		String aux4 = pref.getString(MappingManagerPreferences.EDIT_HEAD,
				editHead);
		mmp.setFontFamily(MappingManagerPreferences.EDIT_HEAD, aux4);

		String editID = mmp.getShortCut(MappingManagerPreferences.EDIT_ID);
		String aux5 = pref.getString(MappingManagerPreferences.EDIT_ID, editID);
		mmp.setFontFamily(MappingManagerPreferences.EDIT_ID, aux5);

	}

}
