package it.unibz.inf.ontop.spec.mapping.bootstrap.util.mpbootstrapper.dictionary;

import java.util.LinkedList;
import java.util.List;

public class Dictionary {

    private final List<DictEntry> dictEntries;

    // Empty dictionary
    public Dictionary(){
        this.dictEntries = new LinkedList<>();
    }

    public Dictionary(List<DictEntry> entries) {
        this.dictEntries = entries;
    }

//    public void addDictEntry(String tableName,
//                             String tableSchema,
//                             String tableAlias,
//                             String tableComment,
//                             List<String> tableLabels,
//                             List<DictEntry.AttAlias> attAliases,
//                             List<DictEntry.Reference> references
//    ){
//        DictEntry toAdd = new DictEntry(tableName, tableSchema, tableAlias, tableComment, tableLabels, attAliases, references);
//        this.dictEntries.add(toAdd);
//    }

    public boolean containsTable(String tableName){
        for( DictEntry entry : this.dictEntries ){
            if(entry.tableName.equalsIgnoreCase(tableName)) return true;
        }
        return false;
    }

    public boolean containsAttribute(String tableName, String attributeName) {
        if (!containsTable(tableName)) return false;

        for (DictEntry entry : this.dictEntries) {
            if (entry.tableName.equalsIgnoreCase(tableName)) {
                for (DictEntry.AttAlias attAlias : entry.attAliases) {
                    if (attAlias.attName.equalsIgnoreCase(attributeName)) return true;
                }
            }
        }
        return false;
    }

    public String getTableAlias(String tableName){
        if( !this.containsTable(tableName) ){
            throw new RuntimeException("Asking for the alias of table "+ tableName +", which is not present in the dictionary"); // Use of Runtime exception because of programming error
        }
        return this.getTableEntry(tableName).tableAlias;
    }

    public String getTableLabel(String tableName){
        if( !this.containsTable(tableName) ){
            throw new RuntimeException("Asking for the labels of table "+ tableName +", which is not present in the dictionary"); // Use of Runtime exception because of programming error
        }
        return this.getTableEntry(tableName).tableLabel;
    }

    public String getAttributeAlias(String tableName, String attributeName){
        for( DictEntry entry : this.dictEntries ){
            if( entry.tableName.equalsIgnoreCase(tableName) ){
                for(DictEntry.AttAlias attAlias : entry.attAliases ){
                    if( attAlias.attName.equalsIgnoreCase(attributeName) ){
                        return attAlias.attAlias;
                    }
                }
            }
        }
        throw new RuntimeException("Mandatory attribute " + tableName + "." + attributeName + "(maybe pkey?) undeclared in the bootstrapper dictionary. Please declare it.");
    }

    /**
     *
     * @param tableName
     * @param attributeName
     * @return the labels of attribute tableName.attributeName
     */
    public String getAttributeLabel(String tableName, String attributeName){
        for( DictEntry entry : this.dictEntries ){
            if( entry.tableName.equalsIgnoreCase(tableName) ){
                for(DictEntry.AttAlias attAlias : entry.attAliases ){
                    if( attAlias.attName.equalsIgnoreCase(attributeName) ){
                        return attAlias.attLabel;
                    }
                }
            }
        }
        throw new RuntimeException("Mandatory attribute " + tableName + "." + attributeName + "(maybe pkey?) undeclared in the bootstrapper dictionary. Please declare it.");
    }

    private DictEntry getTableEntry(String tableName){
        for( DictEntry entry : this.dictEntries ){
            if( entry.tableName.equalsIgnoreCase(tableName) )
                return entry;
        }
        return null;
    }

    public static class DictEntry {

        private final String tableComment;
        private final String tableName;
        private final String tableSchema;
        private final String tableAlias;
        private final String tableLabel;
        private final List<AttAlias> attAliases;
        private final List<Reference> references;

        public DictEntry(String tableName, String tableSchema, String tableAlias, String tableComment, String tableLabel, List<AttAlias> attAliases, List<Reference> references) {
            this.tableName = tableName;
            this.tableSchema = tableSchema;
            this.tableComment = tableComment;
            this.tableAlias = tableAlias;
            this.tableLabel = tableLabel;
            this.attAliases = attAliases;
            this.references = references;
        }

        public String getTableComment() {
            return this.tableComment;
        }

        public static class AttAlias {
            private final String attName;  // Name of the attribute in the DB
            private final String attAlias;  // Name of the attribute in the RDF graph
            private final String attComment;  // rdfs:comment
            private final String attLabel;  // rdfs:label

            public AttAlias(String attName, String attAlias, String attComment, String attLabel) {
                this.attName = attName;
                this.attAlias = attAlias;
                this.attComment = attComment;
                this.attLabel = attLabel;
            }

            /** Name of the attribute in the DB **/
            public String getAttName() {
                return attName;
            }

            /** Name of the attribute in the RDF graph **/
            public String getAttAlias() {
                return attAlias;
            }

            /** rdfs:comment **/
            public String getAttComment() {return attComment; }

            /** rdfs:label **/
            public String getAttLabel() {
                return attLabel;
            }

            @Override
            public String toString() {
                return "AttAlias{" +
                        "attName='" + attName + '\'' +
                        ", attAlias='" + attAlias + '\'' +
                        ", attLabel='" + attLabel + '\'' +
                        '}';
            }
        }

        public static class Reference {
            private final List<String> fromAtts;
            private final String toTable;
            private final List<String> toAtts;
            private final String joinAlias;
            private final String joinLabel;

            private final String joinComment;

            public Reference(List<String> fromAtts, String toTable, List<String> toAtts, String joinAlias, String joinLabel, String joinComment) {
                this.fromAtts = fromAtts;
                this.toTable = toTable;
                this.toAtts = toAtts;
                this.joinAlias = joinAlias;
                this.joinLabel = joinLabel;
                this.joinComment = joinComment;
            }

            public List<String> getFromAtts() {
                return fromAtts;
            }

            public String getToTable() {
                return toTable;
            }

            public List<String> getToAtts() {
                return toAtts;
            }

            public String getJoinAlias() {
                return joinAlias;
            }

            public String getJoinLabel() {
                return joinLabel;
            }

            public String getJoinComment(){
                return joinComment;
            }

            @Override
            public String toString() {
                return "Reference{" +
                        "fromAtts=" + fromAtts +
                        ", toTable='" + toTable + '\'' +
                        ", toAtts=" + toAtts +
                        ", joinAlias='" + joinAlias + '\'' +
                        ", joinLabel='" + joinLabel + '\'' +
                        ", joinComment='" + joinComment + '\'' +
                        '}';
            }
        }

        @Override
        public String toString() {
            return "DictEntry{" +
                    "tableName='" + tableName + '\'' +
                    ", tableSchema='" + tableSchema + '\'' +
                    ", tableAlias='" + tableAlias + '\'' +
                    ", tableLabel='" + tableLabel + '\'' +
                    ", attAliases=" + attAliases +
                    ", references=" + references +
                    '}';
        }

        public String getTableName() {
            return tableName;
        }

        public String getTableSchema() {
            return tableSchema;
        }

        public String getTableAlias() {
            return tableAlias;
        }

        public String getTableLabel() {
            return tableLabel;
        }

        public List<AttAlias> getAttAliases() {
            return attAliases;
        }

        public List<Reference> getReferences() {
            return references;
        }
    }

    /** If the dictionary is empty **/
    public boolean isEmpty(){
        return this.dictEntries.isEmpty();
    }

    public List<DictEntry> getDictEntries() {
        return dictEntries;
    }

    @Override
    public String toString() {
        return "Dictionary{" +
                "dictEntries=" + dictEntries +
                '}';
    }
}
