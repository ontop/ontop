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

    public void addDictEntry(String tableName,
                             String tableSchema,
                             String tableAlias,
                             String tableComment,
                             List<String> tableLabels,
                             List<DictEntry.AttAlias> attAliases,
                             List<DictEntry.Reference> references
    ){
        DictEntry toAdd = new DictEntry(tableName, tableSchema, tableAlias, tableComment, tableLabels, attAliases, references);
        this.dictEntries.add(toAdd);
    }

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

    public List<String> getTableLabels(String tableName){
        if( !this.containsTable(tableName) ){
            throw new RuntimeException("Asking for the labels of table "+ tableName +", which is not present in the dictionary"); // Use of Runtime exception because of programming error
        }
        return this.getTableEntry(tableName).tableLabels;
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
    public List<String> getAttributeLabels(String tableName, String attributeName){
        for( DictEntry entry : this.dictEntries ){
            if( entry.tableName.equalsIgnoreCase(tableName) ){
                for(DictEntry.AttAlias attAlias : entry.attAliases ){
                    if( attAlias.attName.equalsIgnoreCase(attributeName) ){
                        return attAlias.attLabels;
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
        private final List<String> tableLabels;
        private final List<AttAlias> attAliases;
        private final List<Reference> references;

        public DictEntry(String tableName, String tableSchema, String tableAlias, String tableComment, List<String> tableLabels, List<AttAlias> attAliases, List<Reference> references) {
            this.tableName = tableName;
            this.tableSchema = tableSchema;
            this.tableComment = tableComment;
            this.tableAlias = tableAlias;
            this.tableLabels = tableLabels;
            this.attAliases = attAliases;
            this.references = references;
        }

        public String getTableComment() {
            return this.tableComment;
        }

        public static class AttAlias {
            private final String attName;
            private final String attAlias;
            private final String attComment;
            private final List<String> attLabels;

            public AttAlias(String attName, String attAlias, String attComment, List<String> attLabels) {
                this.attName = attName;
                this.attAlias = attAlias;
                this.attComment = attComment;
                this.attLabels = attLabels;
            }

            public String getAttName() {
                return attName;
            }

            public String getAttAlias() {
                return attAlias;
            }

            public String getAttComment() {return attComment; }

            public List<String> getAttLabels() {
                return attLabels;
            }

            @Override
            public String toString() {
                return "AttAlias{" +
                        "attName='" + attName + '\'' +
                        ", attAlias='" + attAlias + '\'' +
                        ", attLabels=" + attLabels +
                        '}';
            }
        }

        public static class Reference {
            private final List<String> fromAtts;
            private final String toTable;
            private final List<String> toAtts;
            private final String joinAlias;
            private final List<String> joinLabels;

            public Reference(List<String> fromAtts, String toTable, List<String> toAtts, String joinAlias, List<String> joinLabels) {
                this.fromAtts = fromAtts;
                this.toTable = toTable;
                this.toAtts = toAtts;
                this.joinAlias = joinAlias;
                this.joinLabels = joinLabels;
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

            public List<String> getJoinLabels() {
                return joinLabels;
            }

            @Override
            public String toString() {
                return "Reference{" +
                        "fromAtts=" + fromAtts +
                        ", toTable='" + toTable + '\'' +
                        ", toAtts=" + toAtts +
                        ", joinAlias='" + joinAlias + '\'' +
                        ", joinLabels=" + joinLabels +
                        '}';
            }
        }

        @Override
        public String toString() {
            return "DictEntry{" +
                    "tableName='" + tableName + '\'' +
                    ", tableSchema='" + tableSchema + '\'' +
                    ", tableAlias='" + tableAlias + '\'' +
                    ", tableLabels=" + tableLabels +
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

        public List<String> getTableLabels() {
            return tableLabels;
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
