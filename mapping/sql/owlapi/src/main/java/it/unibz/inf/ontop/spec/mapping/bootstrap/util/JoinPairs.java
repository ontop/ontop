package it.unibz.inf.ontop.spec.mapping.bootstrap.util;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.QualifiedAttributeID;
import it.unibz.inf.ontop.dbschema.QuotedIDFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Davide Lanti

 CSV format: table1.col1,table2.col2,table3.col3 table4.col4,table5.col5,table6.col6
 */
public class JoinPairs {

    // List of <Left, Right>
    private Set<Pair<List<String>, List<String>>> pairs;

    @Override
    public String toString(){
        return this.pairs.toString();
    }

    public JoinPairs() {
        this.pairs = new HashSet<>();
    }

    public void unite(JoinPairs other){
        this.pairs.addAll(other.pairs);
    }

    public JoinPairs(String filename) throws IOException {
        pairs = new HashSet<>();

        ImmutableList<String> parsedPairs = Files.lines(Paths.get(filename)).collect(ImmutableCollectors.toList());

        for (String s : parsedPairs) {
            String[] splits = s.split(" ");
            if (s.startsWith("#") || splits.length != 2) continue;
            try {
                String from = splits[0];
                String to = splits[1];

                this.pairs.add(new Pair<>(toAttrList(from), toAttrList(to)));
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    public void addJoinPair(List<String> left, List<String> right){
        this.pairs.add(new Pair<>(left, right));
    }

    private List<String> toAttrList(String cslist) {
        return Arrays.asList(cslist.split(","));
    }

    List<Pair<List<QualifiedAttributeID>, List<QualifiedAttributeID>>> getQualifiedAttributePairs(QuotedIDFactory quotedIDFactory) {

        List<Pair<List<QualifiedAttributeID>, List<QualifiedAttributeID>>> result = new ArrayList<>();

        for( Pair<List<String>, List<String>> pair : this.pairs ){
            List<QualifiedAttributeID> left = pair.first().stream()
                    .map(el -> new QualifiedAttributeID(quotedIDFactory.createRelationID(getSchema(el), getTable(el)), quotedIDFactory.createAttributeID(getAttribute(el))))
                    .collect(Collectors.toList());

            List<QualifiedAttributeID> right = pair.second().stream()
                    .map(el -> new QualifiedAttributeID(quotedIDFactory.createRelationID(getSchema(el), getTable(el)), quotedIDFactory.createAttributeID(getAttribute(el))))
                    .collect(Collectors.toList());

            result.add(new Pair<>(left, right));
        }
        return result;
    }

    private static String getSchema(String split) {
        String[] splits = split.split("\\.");

        if (splits.length == 3) {
            return splits[0];
        }
        return null;
    }

    private static String getTable(String split){
        String[] splits = split.split("\\.");

        return splits.length == 3 ? splits[1] : splits[0];
    }

    private static String getAttribute(String split){
        String[] splits = split.split("\\.");

        return splits.length == 3 ? splits[2] : splits[1];
    }
};