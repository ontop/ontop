package federationOptimization;

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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

//import net.sf.jsqlparser.statement.Statement;
//import net.sf.jsqlparser.statement.Statement;


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
    public boolean dynamicSourceCheck(List<String> tables, Map<String, String> sourceLab){
        boolean b = false;
        for(String t: tables){
            String source = "";
            if(t.startsWith("\"")){
                source = t.substring(1, t.indexOf("."));
            } else {
                source = t.substring(0, t.indexOf("."));
            }
            if(sourceLab.get(source).equals(SourceLab.DYNAMIC.toString())){
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

    public boolean differentSourceCheck(List<String> tables_1, List<String> tables_2){
        boolean b = true;
        Set<String> sources_1 = new HashSet<String>();
        Set<String> sources_2 = new HashSet<String>();

        for(String t: tables_1){
            sources_1.add(t.substring(0, t.indexOf(".")));
        }

        for(String t: tables_2){
            sources_2.add(t.substring(0, t.indexOf(".")));
        }

        if( (sources_1.size()>1) || (sources_2.size()>1) ){
            return true;
        } else {
            if(sources_1.equals(sources_2)){
                return false;
            }
        }
      return b;
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

    public String checkRedundancy(Statement stmt, String relation1, String relation2) throws Exception{

        Set<String> ans1 = new HashSet<String>();
        Set<String> ans2 = new HashSet<String>();

        ResultSet rs1 = stmt.executeQuery(relation1);
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

        ResultSet rs2 = stmt.executeQuery(relation2);
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



    public void insertData(Statement stmt, ResultSet rs, String tableName, List<String> attributes) throws Exception {

        ResultSetMetaData rsmd = rs.getMetaData();

        int column_count = rsmd.getColumnCount();
        String create_table = "CREATE TABLE "+tableName+" ( ";
        for(int i=1; i<column_count+1; i++){
            String column_name = attributes.get(i-1);
            String data_type = rsmd.getColumnTypeName(i);
            //the mappings of datatypes between federation system and the DB system.
            if(data_type.equals("string")){
                data_type = "varchar";
            } else if (data_type.equals("integer")){
                data_type = "int4";
            } else if (data_type.equals("double")){
                data_type = "double precision";
            }

            create_table = create_table + column_name+" "+data_type+" , ";
        }
        create_table = create_table.substring(0, create_table.length()-2)+" )";
        System.out.println("create_table: "+create_table);
        int b = stmt.executeUpdate(create_table);

        while(rs.next()){
            String values = "";
            for(int i=1; i<column_count+1; i++){
                values = values +"'"+rs.getString(i)+"'"+",";
            }
            values = values.substring(0, values.length()-1);
           String update = "INSERT INTO "+tableName+" VALUES "+"("+values+")";
           stmt.execute(update);
        }
        //bulk insertion
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
    public SourceHints detectCandidateHints(String owlFile, String obdaFile, String propertyFile, String labFile) throws Exception{
        SourceHints candidateHints = new SourceHints();

        OntopSQLOWLAPIConfiguration configure = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .ontologyFile(owlFile)
                .nativeOntopMappingFile(obdaFile)
                .propertyFile(propertyFile)
                .enableTestMode()
                .build();

        Map<String, String> labOfSources = getLabsOfSources(labFile);

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
                 AttributeSQL as = new AttributeSQL(subjectAttribute, sql);
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
                         AttributeSQL as1 = new AttributeSQL(objectAttribute, sql);
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
                if(dynamicSourceCheck(tables1, labOfSources)){
                    continue;
                }
                for(AttributeSQL as2: set){
                    List<String> tables2 = getTableNamesFromSQL(as2.sourceSQL);
                    if(as2.sourceSQL.equals(as1.sourceSQL) || dynamicSourceCheck(tables2, labOfSources)
                            || !differentSourceCheck(tables1, tables2)){
                        continue;
                    }
                    EmptyFederatedJoin candidate = new EmptyFederatedJoin(as1.sourceSQL, as2.sourceSQL, as1.attribute+"="+as2.attribute);
                    if(!candidateDuplicationCheck(candidate, candidateHints.emptyFJs)){
                        candidateHints.emptyFJs.add(candidate);
                    }
                }
            }
        }

        //compute candidate unions for redundancy checking
        for(String cla: classfication_class.keySet()){
            for(ClassMap cm1: classfication_class.get(cla)){
                List<String> tables1 = getTableNamesFromSQL(cm1.sourceSQL);
                if(dynamicSourceCheck(tables1, labOfSources)){
                    continue;
                }
                for(ClassMap cm2: classfication_class.get(cla)){
                    List<String> tables2 = getTableNamesFromSQL(cm2.sourceSQL);
                    if(dynamicSourceCheck(tables2, labOfSources) || cm2.sourceSQL.equals(cm1.sourceSQL) ||
                            !cm2.IRIFunction.equals(cm1.IRIFunction) || !differentSourceCheck(tables1, tables2)){
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
                    Redundancy candidate = new Redundancy(sql1, sql2, null);
                    if(!candidateDuplicationCheck(candidate, candidateHints.redundancy)){
                        candidateHints.redundancy.add(candidate);
                    }
                }
            }
        }

        for(String pro: classfication_property.keySet()){
            for(PropertyMap cm1: classfication_property.get(pro)){
                List<String> tables1 = getTableNamesFromSQL(cm1.sourceSQL);
                if(dynamicSourceCheck(tables1, labOfSources)){
                    continue;
                }
                for(PropertyMap cm2: classfication_property.get(pro)){
                    List<String> tables2 = getTableNamesFromSQL(cm2.sourceSQL);
                    if(dynamicSourceCheck(tables2, labOfSources) || cm2.sourceSQL.equals(cm1.sourceSQL)
                            ||!cm2.subjectIRIFunction.equals(cm1.subjectIRIFunction) || !cm2.objectIRIFunction.equals(cm1.objectIRIFunction) || !cm2.objectDataType.equals(cm1.objectDataType)
                            ||!differentSourceCheck(tables1, tables2)){
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

        return candidateHints;
    }

    /**
     * Zhenzhen: function for checking the computed candidate joins and unions, unfinished, only framework
     * @param candidateHints
     * @param federationSystemPropertyFile
     * @param matvDBPropertyFile
     * @return
     * @throws Exception
     */

    public SourceHints computeSourceHints(SourceHints candidateHints, String federationSystemPropertyFile, String matvDBPropertyFile) throws Exception {
        SourceHints computedHints = new SourceHints();
        Connection conn_federation = getConnectionOfDB(federationSystemPropertyFile);
        Statement stmt_federation = conn_federation.createStatement();

        Connection conn_matvDB = getConnectionOfDB(matvDBPropertyFile);
        Statement stmt_matvDB = conn_matvDB.createStatement();

        int matv_count = 0;

        //first redundancy checking, then checking candidate federated joins to filter out some candidates
        for(Redundancy candidate: candidateHints.redundancy){
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
        }

        for(EmptyFederatedJoin candidate: candidateHints.emptyFJs){
            //check whether exists duplication with candidate redundancy;
            if(! checkDuplicationAmongCandidateRedundancyAndJoins(computedHints.redundancy, candidate)){
                String[] arrs = candidate.joinCondition.split("=");
                String sql = "SELECT * FROM (" + candidate.relation1+") AS V1, "+"("+candidate.relation2+") AS V2"+" WHERE "+"V1."+arrs[0]+"="+"V2."+arrs[1];
                //System.out.println("sql: "+sql);

                //the sql query needs to be modified;
                //the sql query needs to be make sure whether the two relations containing attributes with the same name;
                ResultSet rs = stmt_federation.executeQuery(sql);
                if(!rs.next()){
                    computedHints.emptyFJs.add(candidate);
                } else {
                    //write into the local sources
                    //import the schema of the local sources into federation system automatically
                    String viewName = "MatV_"+matv_count;

                    List<String> attributes_1 = getSelectItemsFromSQL(candidate.relation1);
                    List<String> attributes_2 = getSelectItemsFromSQL(candidate.relation2);
                    List<String> attributes = new ArrayList<String>();
                    for(String a: attributes_1){
                        if(a.startsWith("\"")){
                            attributes.add("V1_"+a.substring(1, a.length()-1));
                        } else {
                            attributes.add("V1_"+a);
                        }
                    }
                    for(String b: attributes_2){
                        if(b.startsWith("\"")){
                            attributes.add("V2_"+b.substring(1, b.length()-1));
                        } else {
                            attributes.add("V2_"+b);
                        }
                    }

                    insertData(stmt_matvDB, rs, viewName, attributes);

                    MaterializedView mv = new MaterializedView();
                    mv.sourceSQL = sql;
                    mv.table = viewName;
                    mv.attributes = attributes;
                    computedHints.matView.add(mv);

                    matv_count = matv_count+1;
                }
                rs.close();
                rs = null;
            }

        }

        stmt_matvDB.close();
        conn_matvDB.close();
        stmt_federation.close();
        conn_federation.close();

        return computedHints;
    }

    /**
     * Zhenzhen: combine the candidate detection and computation part
     * @param owlFile
     * @param obdaFile
     * @param propertyFile
     * @param labFile
     * @param federationSystemPropertyFile
     * @param localDBPropertyFile
     * @return
     * @throws Exception
     */
    public SourceHints precomputeSourceHints(String owlFile, String obdaFile, String propertyFile, String labFile, String federationSystemPropertyFile, String localDBPropertyFile) throws Exception{
        SourceHints candidates = detectCandidateHints(owlFile, obdaFile, propertyFile, labFile);
        return computeSourceHints(candidates, federationSystemPropertyFile, localDBPropertyFile);
    }


    @Test
    /**Mapping test*********************************/
    public void testMappings() throws Exception {
        OntopSQLOWLAPIConfiguration configure = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .ontologyFile("src/test/resources/compareIRI/boot-multiple-inheritance.owl")
                .nativeOntopMappingFile("src/test/resources/compareIRI/boot-multiple-inheritance.obda")
                .propertyFile("src/test/resources/compareIRI/boot-multiple-inheritance.properties")
                .enableTestMode()
                .build();

        SQLPPMapping mappings = configure.loadProvidedPPMapping();
        for( SQLPPTriplesMap tripleMap : mappings.getTripleMaps() ) {
            System.out.println(tripleMap.getSourceQuery());
            List<TargetAtom> atoms = tripleMap.getTargetAtoms();
            for(TargetAtom ta: atoms) {
                ImmutableTerm subject = ta.getSubstitutedTerm(0);
                ImmutableTerm predicate = ta.getSubstitutedTerm(1);
                ImmutableTerm object = ta.getSubstitutedTerm(2);

                System.out.println(subject);
                System.out.println(predicate);
                System.out.println(object);
                System.out.println(getDataType(object));

                System.out.println("one triple patterns");
            }
        }
    }

   @Test
    public void myTest() throws Exception {
        SourceHints sh = detectCandidateHints("src/test/resources/federation-test/bsbm-ontology.owl",
                "src/test/resources/federation-test/bsbm-mappings-hom-het.obda",
                "src/test/resources/federation-test/teiid.properties",
                "src/test/resources/federation-test/SourceLab.txt");
        System.out.println("detected candidate federated joins for checking: "+sh.emptyFJs.size());
        for(EmptyFederatedJoin candidate: sh.emptyFJs){
            candidate.print();
        }

        System.out.println("detected candidate unions for checking: "+sh.redundancy.size());
        for(Redundancy candidate: sh.redundancy){
            candidate.print();
        }

        System.out.println("start computing: ");

        SourceHints sh_new = computeSourceHints(sh,
                "src/test/resources/federation-test/federationSystem-property.txt",
                "src/test/resources/federation-test/matvDB-property.txt");
        System.out.println("");

        System.out.println("finally computed source hints: ");
        System.out.println("finally obtained empty federated joins:");
        for(EmptyFederatedJoin efj: sh_new.emptyFJs){
            efj.print();
        }

        System.out.println("finally obtained redundancy relations");
        for(Redundancy rd: sh_new.redundancy){
            rd.print();
        }

        System.out.println("finally obtained materialize views:");
        for(MaterializedView mv: sh_new.matView){
            mv.print();
        }


   }

}


class AttributeSQL{
    public String attribute;
    public String sourceSQL;

    public AttributeSQL(){
        attribute = null;
        sourceSQL = null;
    }

    public AttributeSQL(String attribute, String sourceSQL){
        this.attribute = attribute;
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




