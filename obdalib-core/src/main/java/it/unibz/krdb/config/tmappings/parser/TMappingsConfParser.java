package it.unibz.krdb.config.tmappings.parser;

import it.unibz.krdb.config.tmappings.types.SimplePredicate;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TMappingsConfParser {
    private String file;

    private List<SimplePredicate> parsedPredicates = null;

    public TMappingsConfParser(String file){
        this.file = file;
    }

    public List<SimplePredicate> parsePredicates(){
        if( parsedPredicates != null ) return parsedPredicates;

        CSVPlayer player = new CSVPlayer(file);

        List<List<String>> proj = player.getProjection(1,2); // predicateName arity

        parsedPredicates = new ArrayList<SimplePredicate>();
        // For each line
        for( List<String> row : proj ){
            SimplePredicate pred = new SimplePredicate(row.get(0), Integer.parseInt(row.get(1)));
            parsedPredicates.add(pred);
        }

        return parsedPredicates;
    }
}

class CSVPlayer {

    private String file;
    private String separator;

    CSVPlayer(String csvFile){
        file = csvFile;
        separator = " ";
    }

    CSVPlayer(String csvFile, String separator){
        this.file = csvFile;
        this.separator = separator;
    }
    /**
     *
     * @return String format of the CSV fileA
     */
    String printCSVFile(){
        StringBuilder builder = new StringBuilder();
        try{
            BufferedReader in = new BufferedReader(new FileReader(this.file));

            String s;
            while( (s = in.readLine()) != null ){
                builder.append(s);
                builder.append("\n");
            }
            in.close();
        }catch(IOException e){
            e.printStackTrace();
        }

        return builder.toString();
    }

    /**
     * It takes a csv row <b>csvRow</b> and returns the list of all column values in <b>csvRow</b>
     * @param csvRow
     * @param separator
     * @return
     */
    static List<String> parseRow(String csvRow, String separator){

        List<String> result = new ArrayList<String>();

        String[] s = csvRow.split("\\"+separator);
        for( String colValue : s){
            result.add(colValue);
        }

        return result;
    }

    /**
     *
     * @param <T>
     * @param cols
     * @return The list content in csv format
     */
    static <T> String toCSVString(List<T> cols){
        StringBuilder builder = new StringBuilder();

        for( T s : cols ){
            builder.append(s.toString());
            builder.append(" ");
        }
        builder.delete(builder.length() -1, builder.length());

        return builder.toString();
    }

    String searchFirstOccurrenceOfTagInFirstColumn(String tag){
        try{
            BufferedReader in = new BufferedReader(
                    new FileReader(this.file));
            String s;
            String[] s2 = new String[2];
            while ((s = in.readLine()) != null){
                s2 = s.split("\\"+separator);
                if (s2[0].equals(tag)){ in.close(); return s2[1]; }
            }
            in.close();
        }catch(IOException e){
            e.printStackTrace();
        }
        return "error";
    }

    /**
     * It returns the projection on the specified columns. <br />
     * Column indexing starts from 1.
     *
     * @param columnIndex (The first column has index 1)
     */
    List<List<String>> getProjection(int... columnIndexes) {

        List<List<String>> result = new ArrayList<List<String>>();

        try{
            BufferedReader in = new BufferedReader(
                    new FileReader(this.file));
            String s;
            String[] s2 = new String[2];
            while ((s = in.readLine()) != null){
                List<String> row = new ArrayList<String>();
                s2 = s.split("\\"+separator);
                for( int i : columnIndexes )
                    row.add(s2[i-1]);
                result.add(row);
            }
            in.close();
        }catch(IOException e){
            e.printStackTrace();
        }
        return result;
    }
};