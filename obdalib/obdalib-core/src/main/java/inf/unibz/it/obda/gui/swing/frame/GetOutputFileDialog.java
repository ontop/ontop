package inf.unibz.it.obda.gui.swing.frame;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

/**
 * This class allows the user to select an output file (only .csf files)
 * using a JFileChooser. If the selected file already exists it asks the
 * user whether he really wants to overwrite the file using a confirm dialog.
 * If the the user says no or cancels the confirm dialog the file chooser 
 * is reopened, otherwise the selected file object is returned. It returns
 * null if the user cancels the the file selection.
 * 
 * @author obda
 *
 */

//TODO Fix this class. manfred

public class GetOutputFileDialog {
	
	private static final int SELECTION_CANCELED = 1;
	private static final int SELECTION_SUCCESSFUL = 2;
	private static final int SELECTION_PENDING = 3;
	
	private File outputFile = null;
	
	public File getOutPutFile(){
		
		int status = SELECTION_PENDING;
		do{
			status = selectFile();
		}while(status == SELECTION_PENDING);
		return outputFile;
	}
	
	private int selectFile(){
		JFrame frame = new JFrame();
		String filename = File.separator+"tmp";
		JFileChooser fc = new JFileChooser(new File(filename));
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setMultiSelectionEnabled(false);
		fc.setSelectedFile(new File(File.separator+"tmp"+File.separator+"result.csv"));
		fc.setFileFilter(new FileFilter() {
			
			@Override
			public String getDescription() {
				return ".csv files";
			}
			
			@Override
			public boolean accept(File f) {
				
				return f.isDirectory()|| f.getName().toLowerCase().endsWith(".csv");
			}
		});
		fc.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(e.getActionCommand() == JFileChooser.CANCEL_SELECTION){
					((JFileChooser)e.getSource()).setSelectedFile(null);
				}
				
			}
		});
		fc.showSaveDialog(frame);
		File selFile = fc.getSelectedFile();
		if(selFile != null){
			if(selFile.exists()){
				int selection = JOptionPane.showConfirmDialog(fc, "Do you realy want to overwrite the existing file?");
				if(selection==JOptionPane.OK_OPTION){
					outputFile = selFile;
					return SELECTION_SUCCESSFUL;
				}else{
					return SELECTION_PENDING;
				}
			}else{
				outputFile = selFile;
				return SELECTION_SUCCESSFUL;
			}
		}else{
			outputFile = null;
			return SELECTION_CANCELED;
		}
	}
}
