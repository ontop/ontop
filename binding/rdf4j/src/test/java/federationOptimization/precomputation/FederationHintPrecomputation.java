package federationOptimization.precomputation;

import federationOptimization.ObdfTest;
import it.unibz.inf.ontop.dbschema.DBParameters;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.spec.mapping.TargetAtom;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

//import net.sf.jsqlparser.statement.Statement;
//import net.sf.jsqlparser.statement.Statement;

@Category(ObdfTest.class)
public class FederationHintPrecomputation {


    public Map<String, String> getLabsOfSources(String labFile) throws Exception {
        Map<String, String> sourceLab = new HashMap<String, String>();
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(labFile)));
        String line = null;
        while((line=br.readLine()) != null){
            String[] arr = line.split("-");
            sourceLab.put(arr[0], arr[1]);
        }
        return sourceLab;
    }

    public Map<String, String> getSourcesOfRelations(String sourceRelationsFile) throws Exception {
        Map<String, String> sourcesOfRelations = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(sourceRelationsFile)))) {
            String line = null;
            while ((line = br.readLine()) != null) {
                String[] arr = line.split("-");
                if (arr[1].startsWith("\"")) {
                    arr[1] = arr[1].substring(1, arr[1].length() - 1);
                }
                sourcesOfRelations.put(arr[0], arr[1]);
            }
        }
        return sourcesOfRelations;
    }

    public String getIRIFunction(ImmutableTerm it){
        String str = it.toString();
        return str.endsWith("IRI)") ? str.substring(4, str.indexOf("{")): "";
    }

    public String getAttribute(ImmutableTerm it){
        String attr=it.getVariableStream().collect(Collectors.toList()).toString();
        return attr.substring(1, attr.length()-1);
    }

    public String getDataType(ImmutableTerm it){
        String str=it.toString();
        return str.contains("xsd") ? str.substring(str.indexOf(",")+1, str.lastIndexOf(")")) : "";
    }

    public List<String> getTableNamesFromSQL(String SQL) throws JSQLParserException {
//        Statement statement = CCJSqlParserUtil.parse(SQL);
        Select selectStatement = (Select) CCJSqlParserUtil.parse(SQL);
        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        List<String> tableList = tablesNamesFinder.getTableList(selectStatement);
        return tableList;
    }

    public List<String> getSelectItemsFromSQL(String SQL) throws JSQLParserException {
        List<String> attributes = new ArrayList<String>();
        Select selectStatement = (Select) CCJSqlParserUtil.parse(SQL);
        PlainSelect pl = (PlainSelect) selectStatement.getSelectBody();
        for(SelectItem item: pl.getSelectItems()){
            attributes.add(item.toString());
        }
        return attributes;
    }

    /**
     * Zhenzhen: assume the relations/tables in the VDBs or SQL queries are denoted as s.t, where s denotes the data source
     * @param tables
     * @param sourceLab
     * @return
     */
    public boolean dynamicSourceCheck(List<String> tables, Map<String, String> sourceLab, @Nullable Map<String, String> sourceOfRelations){
        // Original code based on schema component of relation full name
        boolean b = false;
        for (String t : tables) {
            String source = getSourceOfRelation(t, sourceOfRelations);
            if (sourceLab.get(source).equals(SourceLab.DYNAMIC.toString())) {
                return true;
            }
        }
        return b;
    }

    /**
     * Zhenzhen: check duplication of candidates
     * @param candidate
     * @param candidates
     * @return
     */
    public boolean candidateDuplicationCheck(EmptyFederatedJoin candidate, Set<EmptyFederatedJoin> candidates){
        boolean b = false;
        for(EmptyFederatedJoin can: candidates){
            if(can.relation1.equals(candidate.relation1) && can.relation2.equals(candidate.relation2) && can.joinCondition.equals(candidate.joinCondition)){
                return true;
            } else if(can.relation1.equals(candidate.relation2) && can.relation2.equals(candidate.relation1)){
                String[] arr = candidate.joinCondition.split("=");
                String str = arr[1]+"="+arr[0];
                if(can.joinCondition.equals(str)){
                    return true;
                }
            }
        }
        return b;
    }

    public boolean candidateDuplicationCheck(Redundancy candidate, Set<Redundancy> candidates){
        boolean b = false;
        for(Redundancy can: candidates){
            if((can.relation1.equals(candidate.relation1) && can.relation2.equals(candidate.relation2)) ||
                    (can.relation1.equals(candidate.relation2) && can.relation2.equals(candidate.relation1))){
                return true;
            }
        }
        return b;
    }

    /**
     * Zhenzhen: check whether tables_1 and tables_2 are rferring different data sources
     * @param tables_1
     * @param tables_2
     * @return
     */

    public boolean differentSourceCheck(List<String> tables_1, List<String> tables_2, @Nullable Map<String, String> sourceOfRelations){
        boolean b = true;
        Set<String> sources_1 = new HashSet<String>();
        Set<String> sources_2 = new HashSet<String>();

        for (String t : tables_1) {
            // sources_1.add(t.substring(0, t.indexOf(".")));
            sources_1.add(getSourceOfRelation(t, sourceOfRelations));
        }

        for (String t : tables_2) {
            // sources_2.add(t.substring(0, t.indexOf(".")));
            sources_2.add(getSourceOfRelation(t, sourceOfRelations));
        }

        if ((sources_1.size() > 1) || (sources_2.size() > 1)) {
            return true;
        } else {
            if (sources_1.equals(sources_2)) {
                return false;
            }
        }
        return b;
    }

    private String getSourceOfRelation(String rel, @Nullable Map<String, String> sourceOfRelations) {
        if (sourceOfRelations == null) {
            return rel.substring(rel.startsWith("\"") ? 1 : 0, rel.indexOf("."));
        } else {
            int idx = rel.lastIndexOf('.');
            String relName = idx < 0 ? rel : rel.substring(idx + 1);
            return Objects.requireNonNull(sourceOfRelations.get(relName));
        }
    }

    public Connection getConnectionOfDB(String DBPropertyFile) throws Exception{
        String driver = "", url = "", user = "", password = "";
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(DBPropertyFile)));
        String line = null;
        while((line=br.readLine()) != null){
            String[] arr = line.split("=");
            if(arr[0].equals("jdbc.url")){
                url = arr[1];
            } else if(arr[0].equals("jdbc.user")){
                user = arr[1];
            } else if(arr[0].equals("jdbc.password")){
                password = arr[1];
            } else if(arr[0].equals("jdbc.driver")){
                driver = arr[1];
            }
        }

        Class.forName(driver);
        Connection conn = DriverManager.getConnection(url, user, password);
        return conn;
    }

    //利用sql1-sql2, sql2-sql1来替代这个检测
    public String checkRedundancy(Statement stmt, String relation1, String relation2) throws Exception{

        Set<String> ans1 = new HashSet<String>();
        Set<String> ans2 = new HashSet<String>();

        ResultSet rs1 = stmt.executeQuery(relation1.replace(" language ", " \"language\" ")); // TODO
        ResultSetMetaData rsmd1 = rs1.getMetaData();
        int column_count1 = rsmd1.getColumnCount();

        while(rs1.next()){
            String res1 = "";
            for(int i=1; i<column_count1+1; i++){
                res1 = res1+rs1.getString(i)+",";
            }
            ans1.add(res1);
        }
        //rs1 is not possible to be empty


        rs1.close();
        rs1 = null;

        ResultSet rs2 = stmt.executeQuery(relation2.replace(" language ", " \"language\" ")); // TODO
        ResultSetMetaData rsmd2 = rs2.getMetaData();
        int column_count2 = rsmd2.getColumnCount();

        while(rs2.next()){
            String res2 = "";
            for(int i=1; i<column_count2+1; i++){
                res2 = res2+rs2.getString(i)+",";
            }
            ans2.add(res2);
        }
        //rs2 is not possible to be empty;

        rs2.close();
        rs2 = null;

        for(String str1: ans1){
            if(ans2.size() == 0){
                return RedundancyRelation.STRICT_CONTAINMENT.toString();
            } else if(!ans2.contains(str1)){
                return "";
            } else {
                ans2.remove(str1);
            }
        }

        if(ans2.size()==0){
            return RedundancyRelation.EQUIVALENCE.toString();
        } else{
            return RedundancyRelation.STRICT_CONTAINMENT.toString();
        }

    }

    public String getFromClauseFromSQL(String sql){
        String from = "";
        return sql.contains(" FROM ") ? sql.substring(sql.indexOf(" FROM ")) : sql.substring(sql.indexOf(" from "));
    }

    public boolean checkDuplicationAmongCandidateRedundancyAndJoins(Set<Redundancy> candidateReds, EmptyFederatedJoin candidateEFJ) throws Exception {
        boolean b = false;
        String sql1 = candidateEFJ.relation1;
        String sql2 = candidateEFJ.relation2;
        String[] attributes = candidateEFJ.joinCondition.split("=");
        String attribute1 = attributes[0];
        String attribute2 = attributes[1];
        String fromClause1 = getFromClauseFromSQL(sql1);
        String fromClause2 = getFromClauseFromSQL(sql2);

        for(Redundancy candidate: candidateReds){
            String fromClause1_new = getFromClauseFromSQL(candidate.relation1);
            String fromClause2_new = getFromClauseFromSQL(candidate.relation2);
            List<String> attributes_1 = getSelectItemsFromSQL(candidate.relation1);
            List<String> attributes_2 = getSelectItemsFromSQL(candidate.relation2);

            if(fromClause1.equals(fromClause1_new) && fromClause2.equals(fromClause2_new)){
                if( (attributes_1.get(0).equals(attribute1)) && (attributes_2.get(0).equals(attribute2))){
                    return true;
                } else if ((attributes_1.size() == 2) && (attributes_1.get(1).equals(attribute1)) && (attributes_2.get(1).equals(attribute2))) {
                    return true;
                }
            } else if (fromClause1.equals(fromClause2_new) && fromClause2.equals(fromClause1_new)){
                if((attributes_1.get(0).equals(attribute2)) && (attributes_2.get(0).equals(attribute1))){
                    return true;
                } else if ((attributes_1.size() == 2) && (attributes_1.get(1).equals(attribute2)) && (attributes_2.get(1).equals(attribute1))){
                    return true;
                }
            }
        }
        return b;
    }



    /**
     * Zhenzhen
     * the functions above are assistant functions
     * the functions below are the core functions, detection, computation,
     *                        and the combination of detection and computation
     */

    /**
     * Zhenzhen: detect the candidate joins and unions for checking
     * @param owlFile
     * @param obdaFile
     * @param propertyFile
     * @param labFile
     * @return
     */
    public SourceHints detectCandidateHints(String owlFile, String obdaFile, String propertyFile, String labFile, @Nullable String sourceRelationFiles) throws Exception{
        long start_time = System.currentTimeMillis();

        SourceHints candidateHints = new SourceHints();

        OntopSQLOWLAPIConfiguration configure = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .ontologyFile(owlFile)
                .nativeOntopMappingFile(obdaFile)
                .propertyFile(propertyFile)
                .enableTestMode()
                .build();

        Map<String, String> labOfSources = getLabsOfSources(labFile);

        Map<String, String> sourceOfRelations = sourceRelationFiles == null ? null : getSourcesOfRelations(sourceRelationFiles);

        Map<String, Set<AttributeSQL>> classfication_IRIFunction = new HashMap<String, Set<AttributeSQL>>();

        Map<String, Set<ClassMap>> classfication_class = new HashMap<String, Set<ClassMap>>();
        Map<String, Set<PropertyMap>> classfication_property = new HashMap<String, Set<PropertyMap>>();

        SQLPPMapping mappings = configure.loadProvidedPPMapping();
        for( SQLPPTriplesMap tripleMap : mappings.getTripleMaps() ){
            String sql = tripleMap.getSourceQuery().getSQL();
            List<TargetAtom> atoms = tripleMap.getTargetAtoms();
            for(TargetAtom ta: atoms){
                ImmutableTerm subject = ta.getSubstitutedTerm(0);
                ImmutableTerm predicate = ta.getSubstitutedTerm(1);
                ImmutableTerm object = ta.getSubstitutedTerm(2);

                String subjectIRIFunction = getIRIFunction(subject);
                String subjectAttribute = getAttribute(subject);
                AttributeSQL as = new AttributeSQL(subjectAttribute, "subject", sql);
                if(classfication_IRIFunction.containsKey(subjectIRIFunction)){
                    classfication_IRIFunction.get(subjectIRIFunction).add(as);
                } else {
                    Set<AttributeSQL> set = new HashSet<AttributeSQL>();
                    set.add(as);
                    classfication_IRIFunction.put(subjectIRIFunction, set);
                }

                if((predicate.toString()).equals("<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>")){
                    String Class = object.toString();
                    ClassMap clamap = new ClassMap(subjectAttribute, subjectIRIFunction, sql);
                    if(classfication_class.containsKey(Class)){
                        classfication_class.get(Class).add(clamap);
                    } else {
                        Set<ClassMap> clamaps = new HashSet<ClassMap>();
                        clamaps.add(clamap);
                        classfication_class.put(Class, clamaps);
                    }

                } else {
                    String property = predicate.toString();
                    String objectIRIFunction = getIRIFunction(object);
                    String objectAttribute = getAttribute(object);
                    if(objectIRIFunction.length() > 0){
                        AttributeSQL as1 = new AttributeSQL(objectAttribute, "object",sql);
                        if(classfication_IRIFunction.containsKey(objectIRIFunction)){
                            classfication_IRIFunction.get(objectIRIFunction).add(as1);
                        } else {
                            Set<AttributeSQL> set = new HashSet<AttributeSQL>();
                            set.add(as1);
                            classfication_IRIFunction.put(objectIRIFunction, set);
                        }
                    }

                    String objectDataType = getDataType(object);
                    PropertyMap promap = new PropertyMap(subjectAttribute, subjectIRIFunction, objectAttribute, objectIRIFunction, objectDataType, sql);
                    if(classfication_property.containsKey(property)){
                        classfication_property.get(property).add(promap);
                    } else {
                        Set<PropertyMap> set = new HashSet<PropertyMap>();
                        set.add(promap);
                        classfication_property.put(property, set);
                    }
                }

            }
        }

        //compute candidate federated joins for empty checking and materialization
        //for sources labels, labeled to relations in the VDB or labeled to data sources??
        for(String IRIFunction: classfication_IRIFunction.keySet()){
            Set<AttributeSQL> set = classfication_IRIFunction.get(IRIFunction);
            for(AttributeSQL as1: set){
                List<String> tables1 = getTableNamesFromSQL(as1.sourceSQL);
                if(dynamicSourceCheck(tables1, labOfSources, sourceOfRelations)){
                    continue;
                }
                for(AttributeSQL as2: set){
                    List<String> tables2 = getTableNamesFromSQL(as2.sourceSQL);
                    if(as2.sourceSQL.equals(as1.sourceSQL) || dynamicSourceCheck(tables2, labOfSources, sourceOfRelations)
                            || !differentSourceCheck(tables1, tables2, sourceOfRelations)){
                        continue;
                    }
                    EmptyFederatedJoin candidate = null;  //下面是为了固定输出，所增加的部分
                    int diff = as1.sourceSQL.compareTo(as2.sourceSQL);
                    if(diff >= 0){
                        candidate = new EmptyFederatedJoin(as1.sourceSQL, as2.sourceSQL, as1.attribute+"="+as2.attribute);
                    } else {
                        candidate = new EmptyFederatedJoin(as2.sourceSQL, as1.sourceSQL, as2.attribute+"="+as1.attribute);
                    }
                    if(!candidateDuplicationCheck(candidate, candidateHints.emptyFJs)){
                        candidateHints.emptyFJs.add(candidate);

                        //extra conditions for filtering out materialized views
                        String as1n = as1.sourceSQL.replace("\"", "");
                        String as2n = as2.sourceSQL.replace("\"", "");
                        if(as1n.contains("s1.reviewc") || as1n.contains("s2.review")||
                                as1n.contains("s1.producttypeproduct1") || as1n.contains("s5.producttypeproduct2")||
                                as2n.contains("s1.reviewc") || as2n.contains("s2.review")||
                                as2n.contains("s1.producttypeproduct1") || as2n.contains("s5.producttypeproduct2")){
                            continue;
                        }

                        if(as1.position.equals("subject") || as2.position.equals("subject")){
                            candidateHints.FJsForMatV.add(candidate);
                        }
                    }
                }
            }
        }

        //compute candidate pairs of expressions for redundancy checking
        for(String cla: classfication_class.keySet()){
            for(ClassMap cm1: classfication_class.get(cla)){
                List<String> tables1 = getTableNamesFromSQL(cm1.sourceSQL);
                if(dynamicSourceCheck(tables1, labOfSources, sourceOfRelations)){
                    continue;
                }
                for(ClassMap cm2: classfication_class.get(cla)){
                    List<String> tables2 = getTableNamesFromSQL(cm2.sourceSQL);
                    if(dynamicSourceCheck(tables2, labOfSources, sourceOfRelations) || cm2.sourceSQL.equals(cm1.sourceSQL) ||
                            !cm2.IRIFunction.equals(cm1.IRIFunction) || !differentSourceCheck(tables1, tables2, sourceOfRelations)){
                        continue;
                    }

                    String sql1 = "", sql2 = "";
                    if(cm1.sourceSQL.contains(" FROM ")){
                        sql1 = "SELECT "+cm1.attribute+" "+cm1.sourceSQL.substring(cm1.sourceSQL.indexOf(" FROM "));
                    } else if(cm1.sourceSQL.contains(" from ")){
                        sql1 = "SELECT "+cm1.attribute+" "+cm1.sourceSQL.substring(cm1.sourceSQL.indexOf(" from "));
                    }
                    if(cm2.sourceSQL.contains(" FROM ")){
                        sql2 = "SELECT "+cm2.attribute+" "+cm2.sourceSQL.substring(cm2.sourceSQL.indexOf(" FROM "));
                    } else if(cm2.sourceSQL.contains(" from ")){
                        sql2 = "SELECT "+cm2.attribute+" "+cm2.sourceSQL.substring(cm2.sourceSQL.indexOf(" from "));
                    }
                    Redundancy candidate = null; //新添加的部分，确保输出的唯一性；
                    int diff = sql1.compareTo(sql2);
                    if(diff >= 0){
                        candidate = new Redundancy(sql1, sql2, null);
                    } else {
                        candidate = new Redundancy(sql2, sql1, null);
                    }
                    if(!candidateDuplicationCheck(candidate, candidateHints.redundancy)){
                        candidateHints.redundancy.add(candidate);
                    }
                }
            }
        }

        for(String pro: classfication_property.keySet()){
            for(PropertyMap cm1: classfication_property.get(pro)){
                List<String> tables1 = getTableNamesFromSQL(cm1.sourceSQL);
                if(dynamicSourceCheck(tables1, labOfSources, sourceOfRelations)){
                    continue;
                }
                for(PropertyMap cm2: classfication_property.get(pro)){
                    List<String> tables2 = getTableNamesFromSQL(cm2.sourceSQL);
                    if(dynamicSourceCheck(tables2, labOfSources, sourceOfRelations) || cm2.sourceSQL.equals(cm1.sourceSQL)
                            ||!cm2.subjectIRIFunction.equals(cm1.subjectIRIFunction) || !cm2.objectIRIFunction.equals(cm1.objectIRIFunction) || !cm2.objectDataType.equals(cm1.objectDataType)
                            ||!differentSourceCheck(tables1, tables2, sourceOfRelations)){
                        continue;
                    }
                    String sql1 = "", sql2 = "";
                    if(cm1.sourceSQL.contains(" FROM ")){
                        sql1 = "SELECT "+cm1.subjectAttribute+", "+cm1.objectAttribute+" "+cm1.sourceSQL.substring(cm1.sourceSQL.indexOf(" FROM "));
                    } else if (cm1.sourceSQL.contains(" from ")){
                        sql1 = "SELECT "+cm1.subjectAttribute+", "+cm1.objectAttribute+" "+cm1.sourceSQL.substring(cm1.sourceSQL.indexOf(" from "));
                    }
                    if(cm2.sourceSQL.contains(" FROM ")){
                        sql2 = "SELECT "+cm2.subjectAttribute+", "+cm2.objectAttribute+" "+cm2.sourceSQL.substring(cm2.sourceSQL.indexOf(" FROM "));
                    } else if (cm2.sourceSQL.contains(" from ")){
                        sql2 = "SELECT "+cm2.subjectAttribute+", "+cm2.objectAttribute+" "+cm2.sourceSQL.substring(cm2.sourceSQL.indexOf(" from "));
                    }

                    Redundancy candidate = new Redundancy(sql1, sql2, null);
                    if(!candidateDuplicationCheck(candidate, candidateHints.redundancy)){
                        candidateHints.redundancy.add(candidate);
                    }
                }
            }
        }

        long end_time = System.currentTimeMillis();
        System.out.println("time used for candidate detection: "+(end_time-start_time));

        printDetectedHints(candidateHints);

        return candidateHints;
    }

    public void printDetectedHints(SourceHints candidates){
        System.out.println("number of detected federated joins for empty checking or materialization: "+candidates.emptyFJs.size());
        for(EmptyFederatedJoin efj: candidates.emptyFJs){
            efj.print();
        }

        System.out.println("number of detected relation pairs for redundancy checking: "+candidates.redundancy.size());
        for(Redundancy rd: candidates.redundancy){
            rd.print();
        }
    }

    /**
     * Zhenzhen: function for checking the computed candidate joins and unions, unfinished, only framework
     * @param candidateHints
     * @param federationSystemPropertyFile
     * @param matvDBPropertyFile
     * @return
     * @throws Exception
     */

    public SourceHints computeSourceHints(SourceHints candidateHints, String federationSystemPropertyFile, @Nullable String matvDBPropertyFile) throws Exception {
       long start_time = System.currentTimeMillis();

        SourceHints computedHints = new SourceHints();
        Connection conn_federation = getConnectionOfDB(federationSystemPropertyFile);
        Statement stmt_federation = conn_federation.createStatement();

        Connection conn_matvDB = null;
        Statement stmt_matvDB = null;
        if (matvDBPropertyFile != null) {
            conn_matvDB = getConnectionOfDB(matvDBPropertyFile);
            stmt_matvDB = conn_matvDB.createStatement();
        }

        int matv_count = 0;

        //first redundancy checking, then checking candidate federated joins to filter out some candidates
        for(Redundancy candidate: candidateHints.redundancy){
            long start = System.currentTimeMillis();
            System.out.println("checking: ");
            System.out.println("relation1: "+candidate.relation1);
            System.out.println("relation2: "+candidate.relation2);
            String res = checkRedundancy(stmt_federation, candidate.relation1, candidate.relation2);

            if(res.length() > 0){
                System.out.println("redundancy: "+res);
                candidate.redundancyRelation = res;
                computedHints.redundancy.add(candidate);
            } else {
                System.out.println("Do not exist redundancy");
            }
            long end = System.currentTimeMillis();
            System.out.println("time for redundancy checking: "+(end-start));
            System.out.println("");
        }

        for(EmptyFederatedJoin candidate: candidateHints.emptyFJs){
            //check whether exists duplication with candidate redundancy;
            long start = System.currentTimeMillis();
            if(! checkDuplicationAmongCandidateRedundancyAndJoins(computedHints.redundancy, candidate)){
                String[] arrs = candidate.joinCondition.split("=");
                String sql = "SELECT * FROM (" + candidate.relation1+") AS V1, "+"("+candidate.relation2+") AS V2"+" WHERE "+"V1."+arrs[0]+"="+"V2."+arrs[1];
                System.out.println("checked join: "+sql);

                //the sql query needs to be modified;
                //the sql query needs to be make sure whether the two relations containing attributes with the same name;
                ResultSet rs = stmt_federation.executeQuery(sql);
                ResultSetMetaData rsmd = rs.getMetaData();
                int column_count = rsmd.getColumnCount();

                if(!rs.isBeforeFirst()){
                    //if(!rs.next()){ //this check will make materialized views miss one row
                    computedHints.emptyFJs.add(candidate);
                    System.out.println("checking result: empty");
                } else {
                    //write into the local sources
                    //import the schema of the local sources into federation system automatically
                    System.out.println("checking result: not empty");
                    if(!candidateHints.FJsForMatV.contains(candidate)){
                        continue;
                    }
                    System.out.println("preparing for creating materialized views: ");

                    //String viewName = "MatV_"+matv_count;
                    String viewName = "MatV_";
                    for(String t: getTableNamesFromSQL(candidate.relation1)){
                        t = t.replace(".","_");
                        viewName = viewName+t+"_";
                    }
                    for(String t: getTableNamesFromSQL(candidate.relation2)){
                        t = t.replace(".","_");
                        viewName = viewName+t+"_";
                    }
                    viewName = viewName.substring(0,viewName.length()-1);

                    //here, index of the attributes of the relations started from zero / 0;
                    List<String> attributes_1 = getSelectItemsFromSQL(candidate.relation1);
                    List<Integer> attr_index_1 = getIndexOfAttributes(conn_federation, attributes_1, candidate.relation1);
                    List<String> attributes_2 = getSelectItemsFromSQL(candidate.relation2);
                    List<Integer> attr_index_2 = getIndexOfAttributes(conn_federation, attributes_2, candidate.relation2);

                    int count = 1;

                    //if relation1 (relation2) has the form SELECT attributes FROM table1 (table2);
                    //then 1_i, 2_j in the matv denotes that i (j) are the i(j)th attribute of the table1 (table2)
                    //otherwise, i and j denote the i (j) th attriubte in the select items of relation1 (relation2)
                    List<String> attributes = new ArrayList<String>();
                    if((attr_index_1.size() >0) && (attr_index_2.size()>0)){
                        for(int i: attr_index_1){
                            String attr_new_name = "\""+1+"_"+i+"\"";
                            String attr_new_type = rsmd.getColumnTypeName(count);
                            if(attr_new_type.equals("string")){
                                attr_new_type = "varchar";
                            } else if (attr_new_type.equals("integer")){
                                attr_new_type = "int4";
                            } else if (attr_new_type.equals("double")){
                                attr_new_type = "double precision";
                            }
                            attributes.add(attr_new_name+" "+attr_new_type);
                            count = count+1;
                        }
                        for(int i: attr_index_2){
                            String attr_new_name = "\""+2+"_"+i+"\"";
                            String attr_new_type = rsmd.getColumnTypeName(count);
                            if(attr_new_type.equals("string")){
                                attr_new_type = "varchar";
                            } else if (attr_new_type.equals("integer")){
                                attr_new_type = "int4";
                            } else if (attr_new_type.equals("double")){
                                attr_new_type = "double precision";
                            }
                            attributes.add(attr_new_name+" "+attr_new_type);
                            count = count+1;
                        }
                    } else {
                        for(int i=0; i<attributes_1.size(); i++){
                            String attr_new_name = "\""+1+"_"+i+"\"";
                            String attr_new_type = rsmd.getColumnTypeName(count);
                            if(attr_new_type.equals("string")){
                                attr_new_type = "varchar";
                            } else if (attr_new_type.equals("integer")){
                                attr_new_type = "int4";
                            } else if (attr_new_type.equals("double")){
                                attr_new_type = "double precision";
                            }
                            attributes.add(attr_new_name+" "+attr_new_type);
                            count = count+1;
                        }
                        for(int i=0; i<attributes_2.size(); i++){
                            String attr_new_name = "\""+2+"_"+i+"\"";
                            String attr_new_type = rsmd.getColumnTypeName(count);
                            if(attr_new_type.equals("string")){
                                attr_new_type = "varchar";
                            } else if (attr_new_type.equals("integer")){
                                attr_new_type = "int4";
                            } else if (attr_new_type.equals("double")){
                                attr_new_type = "double precision";
                            }
                            attributes.add(attr_new_name+" "+attr_new_type);
                            count = count+1;
                        }
                    }

                    if (matvDBPropertyFile != null) {
                        materializeData(conn_matvDB, stmt_matvDB, rs, viewName, attributes);
                    }

                    MaterializedView mv = new MaterializedView();
                    mv.table = viewName;
                    mv.attributes = attributes;
                    mv.relation1 = candidate.relation1;
                    mv.relation2 = candidate.relation2;
                    mv.joinCondition = candidate.joinCondition;

                    computedHints.matView.add(mv);

                    matv_count = matv_count+1;
                }
                rs.close();
                rs = null;
            }
            long end = System.currentTimeMillis();
            System.out.println("time for federated join checking: "+(end-start));
            System.out.println("");

        }

        long end_time = System.currentTimeMillis();
        System.out.println("total time used for computing data hints: "+(end_time-start_time));

        printComputedHintsInGeneralForm(computedHints);
        printComputedHintsInTheFormNeedByCurrentImplementation(conn_federation, computedHints);

        if (matvDBPropertyFile != null) {
            stmt_matvDB.close();
            conn_matvDB.close();
        }
        stmt_federation.close();
        conn_federation.close();

        return computedHints;
    }

    /**
     * Zhenzhen: materialize data for non-empty federated joins, including create table and insert data in the DB for materialized views.
     * @param conn
     * @param rs
     * @param tableName
     * @param attributes
     * @throws Exception
     */

    public void materializeData(Connection conn, Statement stmt, ResultSet rs, String tableName, List<String> attributes) throws Exception {
        String create_table = "CREATE TABLE "+tableName+" (";

        for(int i=0; i<attributes.size(); i++){
            create_table = create_table + attributes.get(i)+",";
        }

        create_table = create_table.substring(0, create_table.length()-1)+" )";
        System.out.println("create_table for the join: "+create_table);
        int b = stmt.executeUpdate(create_table);


        String sql = "insert into "+tableName+" values (";
        for(int i=0; i<attributes.size(); i++){
            sql = sql + "?,";
        }
        sql = sql.substring(0, sql.length()-1)+")";
        PreparedStatement ps = conn.prepareStatement(sql);
        conn.setAutoCommit(false);
        int count = 0;
        while(rs.next()){
            count = count +1;
            for(int i=1; i<attributes.size()+1; i++){
                ps.setObject(i, rs.getObject(i));
            }
            ps.addBatch();
            if(count % 10000 ==0){
                ps.executeBatch();
                ps.clearBatch();
            }
        }
        ps.executeBatch();
        ps.clearBatch();
        conn.commit();

        ps.close();
        rs.close();
    }

    //only for teiid, for different systems, the way of identifying relations maybe different
    public List<Integer> getIndexOfAttributes(Connection conn, List<String> attributes, String sql) throws Exception {
        List<Integer> index = new ArrayList<Integer>();
        List<String> tables = getTableNamesFromSQL(sql);
        if(tables.size()>1){
            return index;
        }
        List<String> attributes_original = new ArrayList<String>();

        String table_name = tables.get(0);
        if(table_name.contains(".")){
            table_name = table_name.substring(table_name.lastIndexOf(".")+1, table_name.length());
        }
        if(table_name.startsWith("\"")){
            table_name = table_name.substring(1, table_name.length()-1);
        }

        DatabaseMetaData metadata = conn.getMetaData();
        ResultSet rs = metadata.getColumns(null, "%", table_name, "%");
        while(rs.next()){
            attributes_original.add(rs.getString("COLUMN_NAME"));
        }
        for(String a: attributes){
            int ind = attributes_original.indexOf(a);
            if(ind != -1){
                index.add(ind);
            } else {
                if(a.startsWith("\"")){
                    a = a.substring(1, a.length()-1);
                    ind = attributes_original.indexOf(a);
                    if(ind != -1){
                        index.add(ind);
                    } else {
                        index.add(ind);
                        System.out.println("handling other situations");
                    }
                }
            }
        }

//        String sql_new = "select * from "+tables.get(0)+" limit 0";
//        List<String> attributes_original = new ArrayList<String>();
//        ResultSet rs = stmt.executeQuery(sql_new);
//        ResultSetMetaData rsmd = rs.getMetaData();
//        int attr_num = rsmd.getColumnCount();
//        System.out.println(attr_num);
//        for(int i=1; i<attr_num+1; i++){
//            attributes_original.add(rsmd.getColumnName(i));
//        }
//        for(String a: attributes){
//            index.add(attributes_original.indexOf(a));
//        }
        return index;
    }

   public void printComputedHintsInGeneralForm(SourceHints hints){
        System.out.println("======print the computed data hints in general form======");

        System.out.println("number of computed empty federated joins: "+hints.emptyFJs.size());
        for(EmptyFederatedJoin efj: hints.emptyFJs){
            efj.print();
        }

        System.out.println("number of computed redudnacy pairs: "+hints.redundancy.size());
        for(Redundancy rd: hints.redundancy){
            rd.print();
        }

        System.out.println("number of created materialized views: "+hints.matView.size());
        for(MaterializedView mv: hints.matView){
            mv.print();
        }

   }

   public void printComputedHintsInTheFormNeedByCurrentImplementation (Connection conn, SourceHints hints) throws Exception {
        //仅适用于mappings中的source query具有形式: select attributes from table
       System.out.println("======print the computed hints in the form used by the current implementation======");

       System.out.println("number of computed empty federated joins: "+hints.emptyFJs.size());
       for(EmptyFederatedJoin efj: hints.emptyFJs){
           List<String> tables_1 = getTableNamesFromSQL(efj.relation1);
           List<String> tables_2 = getTableNamesFromSQL(efj.relation2);
           String[] cond_attributes = efj.joinCondition.split("=");
           List<String> attributes_1 = new ArrayList<String>();
           attributes_1.add(cond_attributes[0]);
           List<String> attributes_2 = new ArrayList<String>();
           attributes_2.add(cond_attributes[1]);
           List<Integer> index_1 = getIndexOfAttributes(conn, attributes_1, efj.relation1);
           List<Integer> index_2 = getIndexOfAttributes(conn, attributes_2, efj.relation2);
           String name1 = tables_1.get(0);
           if(!name1.startsWith("\"")){
               name1 = "\""+name1+"\"";
           }
           if(name1.contains(".") && !name1.contains("\".\"")){
               name1 = name1.replace(".","\".\"");
           }
           String name2 = tables_2.get(0);
           if(!name2.startsWith("\"")){
               name2 = "\""+name2+"\"";
           }
           if(name2.contains(".") && !name2.contains("\".\"")){
               name2 = name2.replace(".","\".\"");
           }

           System.out.println("empty_federated_join:"+name1+"<>"+name2+"<>"+index_1.get(0)+"<>"+index_2.get(0));
       }


       Set<String> RED = new HashSet<String>();
       for(Redundancy rd: hints.redundancy){
           List<String> tables_1 = getTableNamesFromSQL(rd.relation1);
           List<String> tables_2 = getTableNamesFromSQL(rd.relation2);
           List<String> attributes_1 = getSelectItemsFromSQL(rd.relation1);
           List<String> attributes_2 = getSelectItemsFromSQL(rd.relation2);
           List<Integer> index_1 = getIndexOfAttributes(conn, attributes_1, rd.relation1);
           List<Integer> index_2 = getIndexOfAttributes(conn, attributes_2, rd.relation2);

           String name1 = tables_1.get(0);
           if(!name1.startsWith("\"")){
               name1 = "\""+name1+"\"";
           }
           if(name1.contains(".") && !name1.contains("\".\"")){
               name1 = name1.replace(".","\".\"");
           }
           String part1 = name1+"(";
           String part1_p= part1;

           String name2 = tables_2.get(0);
           if(!name2.startsWith("\"")){
               name2 = "\""+name2+"\"";
           }
           if(name2.contains(".") && !name2.contains("\".\"")){
               name2 = name2.replace(".","\".\"");
           }
           String part2 = name2+"(";
           String part2_p = part2;

           for(int i=0; i<index_1.size(); i++){
               RED.add(part1_p+index_1.get(i)+")"+"<>"+part2_p+index_2.get(i)+")");

               part1 = part1 + index_1.get(i) +",";
               part2 = part2 + index_2.get(i) +",";

           }
           part1 = part1.substring(0, part1.length()-1)+")";
           part2 = part2.substring(0, part2.length()-1)+")";

           RED.add(part1+"<>"+part2);
       }
       System.out.println("number of computed redudnacy pairs: "+RED.size());
       for(String s: RED){
           System.out.println("equivalent_redundancy:"+s);
       }

       System.out.println("number of created materialized views: "+hints.matView.size());
       for(MaterializedView mv: hints.matView){
           String part1 = "\"smatv\"."+"\""+mv.table+"\""+"(";
           for(String s: mv.attributes){
               part1 = part1 + s +",";
           }
           part1 = part1.substring(0, part1.length()-1)+")";

           List<String> tables_1 = getTableNamesFromSQL(mv.relation1);
           List<String> tables_2 = getTableNamesFromSQL(mv.relation2);
           String[] cond_attributes = mv.joinCondition.split("=");
           List<String> attributes_1 = new ArrayList<String>();
           attributes_1.add(cond_attributes[0]);
           List<String> attributes_2 = new ArrayList<String>();
           attributes_2.add(cond_attributes[1]);
           List<Integer> index_1 = getIndexOfAttributes(conn, attributes_1, mv.relation1);
           List<Integer> index_2 = getIndexOfAttributes(conn, attributes_2, mv.relation2);

           String name1 = tables_1.get(0);
           if(!name1.startsWith("\"")){
               name1 = "\""+name1+"\"";
           }
           if(name1.contains(".") && !name1.contains("\".\"")){
               name1 = name1.replace(".","\".\"");
           }
           String name2 = tables_2.get(0);
           if(!name2.startsWith("\"")){
               name2 = "\""+name2+"\"";
           }
           if(name2.contains(".") && !name2.contains("\".\"")){
               name2 = name2.replace(".","\".\"");
           }

           System.out.println("materialized_view:"+part1+"<-"+name1+"<>"+name2+"<>"+index_1.get(0)+"<>"+index_2.get(0));
       }
   }


    @Test
    public void myTest() throws Exception {

        String setting = "hom";

        SourceHints sh = detectCandidateHints("src/test/resources/federation-test/bsbm-ontology.owl",
                "src/test/resources/federation-test/teiid/bsbm-mappings-hom-het.obda",
                "src/test/resources/federation-test/teiid/system-" + setting + ".properties",
                "src/test/resources/federation-test/teiid/SourceLab.txt",
                null);

        System.out.println("start computing: ");

        SourceHints sh_new = computeSourceHints(sh,
                "src/test/resources/federation-test/teiid/system-" + setting + ".properties",
                null); // "src/test/resources/federation-test/matvDB-property.txt");

//        List<String> tables = getTableNamesFromSQL("select nr, label, comment, producer, propertynum1, propertynum2, propertynum3, propertynum4, propertynum5, propertynum6, propertytex1, propertytex2, propertytex3, propertytex4, propertytex5, publisher, propertytex6, publishdate from ss5.product2");
//        System.out.println(tables);
    }

    @Test
    public void myTestWithUpdatedConfig() throws Exception {

        String setting = "hom";

        SourceHints sh = detectCandidateHints("src/test/resources/federation/ontology.owl",
                "src/test/resources/federation/mappings.fed.teiid.obda",
                "src/test/resources/federation/system-teiid-" + setting + ".properties",
                "src/test/resources/federation/source_dynamicity_labels." + setting + ".txt",
                "src/test/resources/federation/source_relations." + setting + ".txt");

        System.out.println("start computing: ");

        SourceHints sh_new = computeSourceHints(sh,
                "src/test/resources/federation/system-teiid-" + setting + ".properties",
                null);

//        List<String> tables = getTableNamesFromSQL("select nr, label, comment, producer, propertynum1, propertynum2, propertynum3, propertynum4, propertynum5, propertynum6, propertytex1, propertytex2, propertytex3, propertytex4, propertytex5, publisher, propertytex6, publishdate from ss5.product2");
//        System.out.println(tables);
    }

}


class AttributeSQL{
    public String attribute;
    public String position;
    public String sourceSQL;

    public AttributeSQL(){
        attribute = null;
        position = null;
        sourceSQL = null;
    }

    public AttributeSQL(String attribute, String position, String sourceSQL){
        this.attribute = attribute;
        this.position = position;
        this.sourceSQL = sourceSQL;
    }
}

class ClassMap{
    public String attribute;
    public String IRIFunction;
    public String sourceSQL;

    public ClassMap(){
        attribute = null;
        IRIFunction = null;
        sourceSQL = null;
    }

    public ClassMap(String attribute, String IRIFunction, String sourceSQL){
        this.attribute = attribute;
        this.IRIFunction = IRIFunction;
        this.sourceSQL = sourceSQL;
    }
}

class PropertyMap{
    public String subjectAttribute;
    public String subjectIRIFunction;
    public String objectAttribute;
    public String objectIRIFunction;
    public String objectDataType;
    public String sourceSQL;

    public PropertyMap(){
        subjectAttribute = null;
        subjectIRIFunction = null;
        objectAttribute = null;
        objectIRIFunction = null;
        objectDataType = null;
        sourceSQL = null;
    }

    public PropertyMap(String subjectAttribute, String subjectIRIFunction, String objectAttribute, String objectIRIFunction, String objectDataType, String sourceSQL){
        this.subjectAttribute = subjectAttribute;
        this.subjectIRIFunction = subjectIRIFunction;
        this.objectAttribute = objectAttribute;
        this.objectIRIFunction = objectIRIFunction;
        this.objectDataType = objectDataType;
        this.sourceSQL = sourceSQL;
    }
}