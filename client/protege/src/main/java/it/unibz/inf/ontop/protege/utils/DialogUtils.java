package it.unibz.inf.ontop.protege.utils;

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

import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.OntopStandaloneSQLSettings;
import it.unibz.inf.ontop.protege.core.OntopProtegeReasoner;
import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.protege.editor.core.editorkit.EditorKit;
import org.protege.editor.core.ui.workspace.Workspace;
import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.sql.SQLException;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DialogUtils {

	public static final String HTML_TAB = "&nbsp;&nbsp;&nbsp;&nbsp;";


	public static String renderElapsedTime(long millis) {
		if (millis < 1_000)
			return String.format("%dms", millis);

		if (millis < 10_000)
			return String.format("%d.%02ds", millis / 1000, (millis % 1000 + 5)/10);

		if (millis < 100_000)
			return String.format("%d.%01ds", millis / 1000, (millis % 1000 + 50)/100);

		return String.format("%ds", (millis + 500)/ 1000);
	}

	public static Function<String, String> getExtensionReplacer(String replacement) {
		return shortForm -> {
			int i = shortForm.lastIndexOf(".");
			String filename = (i < 1) ?
					shortForm :
					shortForm.substring(0, i);
			return filename + replacement;
		};
	}

	public static JFileChooser getFileChooser(EditorKit editorKit, Function<String, String> filenameTransformer) {
		OWLEditorKit owlEditorKit = (OWLEditorKit) editorKit;
		OWLModelManager modelManager = owlEditorKit.getOWLWorkspace().getOWLModelManager();
		OWLOntology activeOntology = modelManager.getActiveOntology();
		IRI documentIRI = modelManager.getOWLOntologyManager().getOntologyDocumentIRI(activeOntology);
		File ontologyDir = new File(documentIRI.toURI().getPath());
		JFileChooser fc = new JFileChooser(ontologyDir);
		if (filenameTransformer != null)
			fc.setSelectedFile(new File(filenameTransformer.apply(documentIRI.getShortForm())));
		return fc;
	}

	private static final int MAX_CHARACTERS_PER_LINE_COUNT = 150;

	public static void showPrettyMessageDialog(Component parent, Object message, String title, int type) {
		JOptionPane narrowPane = new JOptionPane(message, type) {
			@Override
			public int getMaxCharactersPerLineCount() {
				return MAX_CHARACTERS_PER_LINE_COUNT;
			}
		};
		JDialog errorDialog = narrowPane.createDialog(parent, title);
		errorDialog.setVisible(true);
	}


	public static Optional<OntopProtegeReasoner> getOntopProtegeReasoner(EditorKit editorKit) {
		Workspace workspace = editorKit.getWorkspace();
		if (!(editorKit instanceof OWLEditorKit))
			throw new MinorOntopInternalBugException("EditorKit is not an OWLEditorKit");

		OWLEditorKit owlEditorKit = (OWLEditorKit)editorKit;
		OWLReasoner reasoner = owlEditorKit.getModelManager().getOWLReasonerManager().getCurrentReasoner();
		if (!(reasoner instanceof OntopProtegeReasoner)) {
			JOptionPane.showMessageDialog(workspace,
					"Ontop reasoner must be started before using this feature. To proceed\n" +
							" * select Ontop in the \"Reasoners\" menu and\n" +
							" * click \"Start reasoner\" in the same menu.",
					"Warning",
					JOptionPane.WARNING_MESSAGE);
			return Optional.empty();
		}
		return Optional.of((OntopProtegeReasoner)reasoner);
	}

	public static void showErrorDialog(Component parent, String title, String message, Logger log, Throwable e, OntopStandaloneSQLSettings settings) {
		if (e.getCause() instanceof SQLException && settings != null) {
			JOptionPane.showMessageDialog(parent,
					"<html><b>Error connecting to the database:</b> " + e.getCause().getMessage() + ".<br><br>" +
							HTML_TAB + "JDBC driver: " + settings.getJdbcDriver() + "<br>" +
							HTML_TAB + "Connection URL: " + settings.getJdbcUrl() + "<br>" +
							HTML_TAB + "Username: " + settings.getJdbcUser() + "</html>",
					title,
					JOptionPane.ERROR_MESSAGE);
		}
		else
			DialogUtils.showSeeLogErrorDialog(parent, title, message, log, e);
	}


	public static void showSeeLogErrorDialog(Component parent, String title, String message, Logger log, Throwable e) {
		String text = message + "\n" +
				e.getMessage() + "\n" +
				"For more information, see the log.";

		JOptionPane narrowPane = new JOptionPane(text, JOptionPane.ERROR_MESSAGE) {
			@Override
			public int getMaxCharactersPerLineCount() {
				return MAX_CHARACTERS_PER_LINE_COUNT;
			}
		};
		JDialog errorDialog = narrowPane.createDialog(parent, title);
		errorDialog.setModal(true);
		errorDialog.setVisible(true);

		log.error(e.getMessage(), e);
		e.printStackTrace();
	}

	public static void showSeeLogErrorDialog(Component parent, String message, Logger log, Throwable e) {
		showSeeLogErrorDialog(parent, "Error", message, log, e);
	}

	public static void showQuickErrorDialog(Component parent, Exception e, String message) {
		SwingUtilities.invokeLater(() -> {
			JTextArea textArea = new JTextArea();
			textArea.setBackground(Color.WHITE);
			textArea.setFont(new Font("Monaco", Font.PLAIN, 11));
			textArea.setEditable(false);
			textArea.setWrapStyleWord(true);

			String debugInfo = e.getLocalizedMessage() + "\n\n"
					+ "###################################################\n"
					+ "##    Debugging information for developers    ##\n"
					+ "###################################################\n\n"
					+ Stream.of(e.getStackTrace())
						.map(StackTraceElement::toString)
						.collect(Collectors.joining("\n\t", "\t", ""));

			textArea.setText(debugInfo);
			textArea.setCaretPosition(0);

			JScrollPane scrollPane = new JScrollPane(textArea);
			scrollPane.setPreferredSize(new Dimension(800, 450));

			JOptionPane.showMessageDialog(parent, scrollPane, message, JOptionPane.ERROR_MESSAGE);
		});
	}

	public static void centerDialogWRTParent(Container parent, Component dialog) {
		Point topLeft = parent.getLocationOnScreen();
		Dimension parentSize = parent.getSize();
		Dimension mySize = dialog.getSize();

		int x = (parentSize.width > mySize.width)
				? ((parentSize.width - mySize.width) / 2) + topLeft.x
				: topLeft.x;

		int y = (parentSize.height > mySize.height)
				? ((parentSize.height - mySize.height) / 2) + topLeft.y
				: topLeft.y;

		dialog.setLocation(x, y);
	}

	private static final KeyStroke escapeStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
	public static final String dispatchWindowClosingActionMapKey = "com.spodding.tackline.dispatch:WINDOW_CLOSING";

	public static void installEscapeCloseOperation(JDialog dialog) {
		JRootPane root = dialog.getRootPane();
		root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeStroke, dispatchWindowClosingActionMapKey);
		root.getActionMap().put(dispatchWindowClosingActionMapKey, new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent event) {
				dialog.dispatchEvent(new WindowEvent(dialog, WindowEvent.WINDOW_CLOSING));
			}
		});
	}
}
