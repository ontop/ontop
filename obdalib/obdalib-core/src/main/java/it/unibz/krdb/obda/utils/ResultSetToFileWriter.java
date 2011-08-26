package it.unibz.krdb.obda.utils;

import it.unibz.krdb.obda.model.OBDAResultSet;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

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
