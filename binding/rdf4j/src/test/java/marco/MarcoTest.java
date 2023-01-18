package marco;

import com.google.common.collect.Lists;
import it.unibz.inf.ontop.exception.MappingException;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.spec.mapping.TargetAtom;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MarcoTest {

    private static final String PROPERTY = "src/test/resources/compareIRI/boot-multiple-inheritance.properties";
    private static final String OWL = "src/test/resources/compareIRI/boot-multiple-inheritance.owl";
    private static final String OBDA = "src/test/resources/compareIRI/boot-multiple-inheritance.obda";

    static OntopSQLOWLAPIConfiguration configure(String propertyPath, String owlPath, String obdaPath) {

        OntopSQLOWLAPIConfiguration configuration = OntopSQLOWLAPIConfiguration.defaultBuilder().ontologyFile(owlPath).nativeOntopMappingFile(obdaPath).propertyFile(PROPERTY).enableTestMode().build();

        return configuration;
    }

    public List<Variable> getAttributeFromSubject(TargetAtom targetAtom) {
        return targetAtom.getSubstitutedTerm(0).getVariableStream().collect(Collectors.toList());
    }

    public String getSubjectIRIFromTargetAtom(TargetAtom targetAtom) {
        ImmutableTerm immutableTerm = targetAtom.getSubstitutedTerm(0);
        return getIRIFromSubjectRDF(immutableTerm);
    }

    public String getIRIFromSubjectRDF(ImmutableTerm t1) {
        int indexEqual = t1.toString().indexOf("(", 4);
        return t1.toString().substring(4, indexEqual);
    }

    public String getObjectIRIFromTargetAtom(TargetAtom targetAtom) {
        String objectIRI = targetAtom.getSubstitutedTerm(2).toString();

        Pattern pattern = Pattern.compile("<.*>");
        Matcher matcher = pattern.matcher(objectIRI);
        if (matcher.matches()) {
            return objectIRI.substring(1, objectIRI.length() - 1);
        } else {
            return objectIRI;
        }
    }

    public List<String> getSourceTableNamesFromMapping(SQLPPTriplesMap tripleMap) throws JSQLParserException {
        return getTableNamesFromSQL(tripleMap.getSourceQuery().getSQL());
    }

    public List<String> getTableNamesFromSQL(String SQL) throws JSQLParserException {
        Statement statement = CCJSqlParserUtil.parse(SQL);
        Select selectStatement = (Select) statement;
        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        List<String> tableList = tablesNamesFinder.getTableList(selectStatement);
        return tableList;
    }

    @Test
    public void testExtractInfoFromMapping() throws MappingException, JSQLParserException {
        OntopSQLOWLAPIConfiguration initialConfiguration = configure(PROPERTY, OWL, OBDA);

        SQLPPMapping mappings = initialConfiguration.loadProvidedPPMapping();

        Set<String> subjectIRIs = new HashSet<>();

        for (SQLPPTriplesMap tripleMap : mappings.getTripleMaps()) {
            System.out.println(StringUtils.repeat('#', 50));
            System.out.println("Parsing mapping:");
            System.out.println(tripleMap);
            System.out.println(StringUtils.repeat('*', 50));

            System.out.println("The source of the mapping is:");
            System.out.println(tripleMap.getSourceQuery().getSQL());
            System.out.println("The source of the mapping contains the following tables:");
            System.out.println(getSourceTableNamesFromMapping(tripleMap));

            for (TargetAtom targetAtom : tripleMap.getTargetAtoms()) {
                System.out.println(StringUtils.repeat('*', 50));

                System.out.println("The IRI of the subject is:");
                System.out.println(getSubjectIRIFromTargetAtom(targetAtom));

                subjectIRIs.add(getSubjectIRIFromTargetAtom(targetAtom));

                System.out.println("The attribute of the subjects are:");
                System.out.println(getAttributeFromSubject(targetAtom));

                System.out.println("The IRI of the object is:");
                System.out.println(getObjectIRIFromTargetAtom(targetAtom));
            }
        }

        System.out.println(StringUtils.repeat('*', 50));
        System.out.println("Found following different IRIs in subjects of target triplets:");
        System.out.println(subjectIRIs);
//        List<List<String>> compareIRIsList = Lists.cartesianProduct(subjectIRIs.stream().collect(Collectors.toList()), subjectIRIs.stream().collect(Collectors.toList()));
//        for (List<String> IRICouple : compareIRIsList) {
//            System.out.println("Is IRI " + IRICouple.get(0) + " equal to IRI " + IRICouple.get(1) + "?");
//            System.out.println(IRICouple.get(0).equals(IRICouple.get(1)));
//        }
    }

    @Test
    public void testOntop() throws MappingException {
        OntopSQLOWLAPIConfiguration initialConfiguration = configure(PROPERTY, OWL, OBDA);

        SQLPPMapping mappings = initialConfiguration.loadProvidedPPMapping();

        List<ImmutableTerm> subjectIRIs = new ArrayList<>();

        for (SQLPPTriplesMap tripleMap : mappings.getTripleMaps()) {
            TargetAtom targetAtom1 = tripleMap.getTargetAtoms().get(0);
            TargetAtom targetAtom2 = tripleMap.getTargetAtoms().get(1);

            System.out.println(targetAtom1);
            System.out.println(targetAtom2);

            ImmutableTerm t1 = targetAtom1.getSubstitutedTerm(0);
            ImmutableTerm t2 = targetAtom2.getSubstitutedTerm(0);

            subjectIRIs.add(t1);

            System.out.println(t1);
            System.out.println(t2);
            System.out.println(t1.equals(t2));
            //System.out.println(t1.evaluateStrictEq(t2));

            // extract object from mapping
            System.out.println(targetAtom1.getSubstitutedTerm(2));

            //System.out.println(getIRIFromSubjectRDF(targetAtom1.getSubstitutedTerm(0)));

            // extract attribute of triplmap
            System.out.println(targetAtom1.getSubstitutedTerm(0).getVariableStream().collect(Collectors.toList()));

            // get the source of the mapping
            System.out.println(tripleMap.getSourceQuery().getSQL());

            //break;
        }

        System.out.println(subjectIRIs);
        System.out.println(subjectIRIs.get(0).equals(subjectIRIs.get(1)));
    }

    @Test
    public void testSQLParserGetTables() throws JSQLParserException {
        String SQL = "SELECT\n" + "c.calendar_date,\n" + "c.calendar_year,\n" + "c.calendar_month,\n" + "c.calendar_dayname,\n" + "COUNT(DISTINCT sub.order_id) AS num_orders,\n" + "COUNT(sub.book_id) AS num_books,\n" + "SUM(sub.price) AS total_price,\n" + "SUM(COUNT(sub.book_id)) OVER (\n" + "  PARTITION BY c.calendar_year, c.calendar_month\n" + "  ORDER BY c.calendar_date\n" + ") AS running_total_num_books,\n" + "LAG(COUNT(sub.book_id), 7) OVER (ORDER BY c.calendar_date) AS prev_books\n" + "FROM calendar_days c\n" + "LEFT JOIN (\n" + "  SELECT\n" + "  DATE_FORMAT(co.order_date, '%Y-%m') AS order_month,\n" + "  DATE_FORMAT(co.order_date, '%Y-%m-%d') AS order_day,\n" + "  co.order_id,\n" + "  ol.book_id,\n" + "  ol.price\n" + "  FROM cust_order co\n" + "  INNER JOIN order_line ol ON co.order_id = ol.order_id\n" + ") sub ON c.calendar_date = sub.order_day\n" + "GROUP BY c.calendar_date, c.calendar_year, c.calendar_month, c.calendar_dayname\n" + "ORDER BY c.calendar_date ASC;";
        List<String> tableNames = getTableNamesFromSQL(SQL);
        System.out.println(tableNames);
    }

}