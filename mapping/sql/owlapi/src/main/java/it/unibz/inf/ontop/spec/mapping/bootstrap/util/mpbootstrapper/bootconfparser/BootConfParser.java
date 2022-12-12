package it.unibz.inf.ontop.spec.mapping.bootstrap.util.mpbootstrapper.bootconfparser;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.unibz.inf.ontop.spec.mapping.bootstrap.util.mpbootstrapper.BootConf;
import it.unibz.inf.ontop.spec.mapping.bootstrap.util.mpbootstrapper.clusters.Cluster;
import it.unibz.inf.ontop.spec.mapping.bootstrap.util.mpbootstrapper.dictionary.Dictionary;
import it.unibz.inf.ontop.spec.mapping.bootstrap.util.mpbootstrapper.shdefinitions.SHDefinition;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class BootConfParser {

    private BootConfParser(){}

    public static Dictionary parseDictionary(String dictFile) throws IOException {
        String json = Files.lines(Paths.get(dictFile)).collect(Collectors.joining(" "));
        JsonElement jsonElement = JsonParser.parseString(json);

        List<Dictionary.DictEntry> entries = new LinkedList<>();

        if( jsonElement.isJsonObject() ){
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            if( jsonObject.has("dictionary") ){
                JsonArray jsonDict = jsonObject.getAsJsonArray("dictionary");
                for( JsonElement el : jsonDict ){
                    if( el.isJsonObject() ){
                        JsonObject elObj = el.getAsJsonObject();
                        String tName = elObj.get("tableName").getAsString();
                        String tSchema = elObj.has("tableSchema") ? elObj.get("tableSchema").getAsString() : "";
                        String tAlias = elObj.get("tableAlias").getAsString();
                        String tComment = elObj.has("tableComment") ? elObj.get("tableComment").getAsString() : "";
                        List<String> tLabels = elObj.has("tableLabels") ? toList(elObj.getAsJsonArray("tableLabels")) : new LinkedList<>();
                        List<Dictionary.DictEntry.AttAlias> attAliases = elObj.has("attAliases") ? toAttributeEntryList(elObj.get("attAliases")) : new LinkedList<>();
                        List<Dictionary.DictEntry.Reference> references = elObj.has("references") ? toReferenceEntryList(elObj.get("references")) : new LinkedList<>();

                        Dictionary.DictEntry entry = new Dictionary.DictEntry(tName, tSchema, tAlias, tComment, tLabels, attAliases, references);
                        entries.add(entry);
                    }
                }
            }
        }
        return new Dictionary(entries);
    }

    // TODO: Test
    public static List<Cluster> parseClustering(String dictFile) throws IOException {
        String json = Files.lines(Paths.get(dictFile)).collect(Collectors.joining(" "));
        JsonElement jsonElement = JsonParser.parseString(json);

        List<Cluster> result = new LinkedList<>();

        if( jsonElement.isJsonObject() ){
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            if( jsonObject.has("clusters") ){
                JsonArray clusters = jsonObject.getAsJsonArray("clusters");
                for( JsonElement el : clusters ){
                    if( el.isJsonObject() ){
                        JsonObject elObj = el.getAsJsonObject();
                        String type = elObj.get("type").getAsString();
                        String tableSchema = elObj.has("tableSchema") ? elObj.get("tableSchema").getAsString() : "";
                        String tableName = elObj.get("tableName").getAsString();
                        String clusteringAttribute = elObj.get("clusteringAttribute").getAsString();
                        List<Cluster.ClusteringMapEntry> clusteringMap = elObj.has("clusteringMap") ? extractClusteringMap(elObj.getAsJsonArray("clusteringMap")) : new LinkedList<>();

                        result.add(new Cluster(type, tableSchema, tableName, clusteringAttribute, clusteringMap));
                    }
                }
            }
        }
        return result;
    }

    private static List<Cluster.ClusteringMapEntry> extractClusteringMap(JsonArray entriesArray){
        List<Cluster.ClusteringMapEntry> result = new LinkedList<>();

        for( JsonElement el : entriesArray ){
            if( el.isJsonObject() ){
                JsonObject elObj = el.getAsJsonObject();
                result.add(new Cluster.ClusteringMapEntry(elObj.get("key").getAsString(), elObj.get("value").getAsString()));
            }
        }
        return result;
    }

    private static List<Dictionary.DictEntry.Reference> toReferenceEntryList(JsonElement references) {

        List<Dictionary.DictEntry.Reference> result = new LinkedList<>();

        for( JsonElement refEl : references.getAsJsonArray() ){
            JsonObject obj = refEl.getAsJsonObject();
            List<String> fromAtts = toList(obj.get("fromAtts").getAsJsonArray());
            String toTable = obj.get("toTable").getAsString();
            List<String> toAtts = toList(obj.get("toAtts").getAsJsonArray());
            String joinAlias = obj.get("joinAlias").getAsString();
            List<String> joinLabels = obj.has("joinLabels") ? toList(obj.getAsJsonArray("joinLabels")) : new LinkedList<>();

            result.add(new Dictionary.DictEntry.Reference(fromAtts, toTable, toAtts, joinAlias, joinLabels));
        }

        return result;
    }

    private static List<Dictionary.DictEntry.AttAlias> toAttributeEntryList(JsonElement attAliases) {

        List<Dictionary.DictEntry.AttAlias> result = new LinkedList<>();

        for( JsonElement attEl : attAliases.getAsJsonArray() ){
            JsonObject obj = attEl.getAsJsonObject();
            String attName = obj.get("attName").getAsString();
            String attAlias = obj.get("attAlias").getAsString();
            String attComment = obj.has("attComment") ? obj.get("attComment").getAsString() : "";
            List<String> attLabels = obj.has("attLabels") ? toList(obj.getAsJsonArray("attLabels")) : new LinkedList<>();
            result.add(new Dictionary.DictEntry.AttAlias(attName, attAlias, attComment, attLabels));
        }
        return result;
    }

    private static List<String> toList(JsonArray jsonArray){
        List<String> result = new LinkedList<>();
        for( JsonElement el : jsonArray ){
            result.add(el.getAsString());
        }
        return result;
    }

    // TODO Finish implementing this
    public static List<SHDefinition> parseSHDefs(String confFile) throws IOException {
        String json = Files.lines(Paths.get(confFile)).collect(Collectors.joining(" "));
        JsonElement jsonElement = JsonParser.parseString(json);

        List<SHDefinition> result = new LinkedList<>();

        if( jsonElement.isJsonObject() ){
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            JsonArray jsonDict = jsonObject.getAsJsonArray("sh");
            for( JsonElement el : jsonDict ){
                if( el.isJsonObject() ){
                    JsonObject elObj = el.getAsJsonObject();
                    String parent = elObj.get("parent").getAsString();
                    String child = elObj.get("child").getAsString();
                    result.add(new SHDefinition(parent, child));
                }
            }
        }
        return result;
    }

    public static boolean parseEnableSH(String confFile) throws IOException {
        String json = Files.lines(Paths.get(confFile)).collect(Collectors.joining(" "));
        JsonElement jsonElement = JsonParser.parseString(json);

        boolean enableSH = false;

        if( jsonElement.isJsonObject() ){
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            if( jsonObject.has("enableSH") )
                enableSH = jsonObject.get("enableSH").getAsBoolean();
        }
        return enableSH;
    }

    public static String parseSchema(String confFile) throws IOException {
        String json = Files.lines(Paths.get(confFile)).collect(Collectors.joining(" "));
        JsonElement jsonElement = JsonParser.parseString(json);

        String schema = "";

        if( jsonElement.isJsonObject() ){
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            if( jsonObject.has("schema") )
                schema = jsonObject.get("schema").getAsString();
        }
        return schema;
    }

    public static BootConf.NullValue parseNullValue(String confFile) throws IOException {
        String json = Files.lines(Paths.get(confFile)).collect(Collectors.joining(" "));
        JsonElement jsonElement = JsonParser.parseString(json);

        if( jsonElement.isJsonObject() ){
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            if( jsonObject.has("nullValue") ) {
                JsonObject jsonObj;
                jsonObj = jsonObject.get("nullValue").getAsJsonObject();

                String stringNull = jsonObj.has("string") ? jsonObj.get("string").getAsString() : null;
                int intNull = jsonObj.has("numeric") ? jsonObj.get("numeric").getAsInt() : null;
                String dateNull = jsonObj.has("date") ? jsonObj.get("date").getAsString() : null;
                return new BootConf.NullValue(stringNull, intNull, dateNull);
            }
        }
        return null;
    }

    // TODO Remove completely?
    public static List<BootConf.GenerateOnlyEntry> parseGenerateOnly(String confFile) throws IOException {
        String json = Files.lines(Paths.get(confFile)).collect(Collectors.joining(" "));
        JsonElement jsonElement = JsonParser.parseString(json);

        List<BootConf.GenerateOnlyEntry> entries = new ArrayList<>();

        if( jsonElement.isJsonObject() ){
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            JsonArray jsonGenerateOnly = jsonObject.getAsJsonArray("generateOnly");
            for( JsonElement el : jsonGenerateOnly ){
                if( el.isJsonObject() ){
                    JsonObject elObj = el.getAsJsonObject();
                    String tName = elObj.get("tableName").getAsString();
                    String tSchema = elObj.has("tableSchema") ? elObj.get("tableSchema").getAsString() : "";
                    List<String> attributes = toList(elObj.get("attributes").getAsJsonArray());

                    entries.add(new BootConf.GenerateOnlyEntry(tName, tSchema, attributes));
                }
            }
        }
        return entries;
    }
}