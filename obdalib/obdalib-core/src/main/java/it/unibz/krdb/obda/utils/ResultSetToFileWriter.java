package it.unibz.krdb.obda.utils;

import it.unibz.krdb.obda.model.OBDAResultSet;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.List;

public class ResultSetToFileWriter {

	
	public static void saveResultSet(OBDAResultSet result, File outputFile) throws Exception{
		
		if(outputFile.exists()){
			outputFile.delete();
			outputFile.createNewFile();
		}else{
			outputFile.createNewFile();
		}
		
		FileWriter fstream = new FileWriter(outputFile, true);
        BufferedWriter out = new BufferedWriter(fstream);
        
        /* Print the CSV header */
        List<String> columnNames = result.getSignature();
        StringBuffer header = new StringBuffer();
        boolean bNeedComma = false;
        for (String column : columnNames) {
        	if (bNeedComma) {
        		header.append(",");
        	}
        	header.append(column);
        	bNeedComma = true;
        }
        out.append(header+"\n");
        
        /* Print the CSV content */
        int column = result.getColumCount();
        while(result.nextRow()){
        	StringBuffer line = new StringBuffer();
        	for(int i=1;i<=column;i++){
        		if(line.length() >0){
        			line.append(",");
        		}
        		line.append(result.getAsString(i));
        	}
        	out.append(line+"\n");
        }
        out.close();
	}
}
