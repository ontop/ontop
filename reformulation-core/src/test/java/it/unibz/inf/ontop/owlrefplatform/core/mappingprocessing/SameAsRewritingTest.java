package it.unibz.inf.ontop.owlrefplatform.core.mappingprocessing;

import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 *Test correctness rewriting of sameAs mapping with canonical URI
 */

public class SameAsRewritingTest {


    private static List<CQIE> mappings;
    private static OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
    private static Variable t0 =fac.getVariable("t0");
    private static Variable t1 =fac.getVariable("t1");
    private static Variable t2 =fac.getVariable("t2");
    private static Variable t3 =fac.getVariable("t3");
    private static Variable t4 =fac.getVariable("t4");
    private static ValueConstant canonURI = fac.getConstantLiteral("http://ontop/wellbore/{}/{}");
    private static ValueConstant npdURI = fac.getConstantLiteral("http://npd/wellbore/{}");
    private static ValueConstant epdsURI = fac.getConstantLiteral("http://epds/wellbore/{}");
    private static ValueConstant owURI = fac.getConstantLiteral("http://ow/wellbore/{}");


    @Before
    public void setUp() throws Exception {

        mappings = new LinkedList<>();

        //sameAs mappings

        Function headM1 = getSameAsFunction(fac.getUriTemplate(canonURI,t1,t0), fac.getUriTemplate(epdsURI, t2));
        Function headM2 = getSameAsFunction(fac.getUriTemplate(canonURI,t1,t0), fac.getUriTemplate(npdURI, t3));
        Function headM3 = getSameAsFunction(fac.getUriTemplate(canonURI,t1,t0), fac.getUriTemplate(owURI, t4));

        List<Function> bodyM1 = new LinkedList<>();
        List<Term> atomTerms1 = new LinkedList<>();
        atomTerms1.add(t0);
        atomTerms1.add(t1);
        atomTerms1.add(t2);
        atomTerms1.add(t3);
        atomTerms1.add(t4);

        Function tableT_can = getFunction("PUBLIC.T_CAN_LINK", new LinkedList<>(atomTerms1));

        bodyM1.add(tableT_can);
        bodyM1.add(fac.getFunctionIsNotNull(t4));
        bodyM1.add(fac.getFunctionIsNotNull(t0));
        bodyM1.add(fac.getFunctionIsNotNull(t1));


        mappings.add(fac.getCQIE(headM1,bodyM1));

        List<Function> bodyM2 = new LinkedList<>();
        bodyM2.add(tableT_can);
        bodyM2.add(fac.getFunctionIsNotNull(t4));
        bodyM2.add(fac.getFunctionIsNotNull(t0));
        bodyM2.add(fac.getFunctionIsNotNull(t2));

        mappings.add(fac.getCQIE(headM2,bodyM2));

        List<Function> bodyM3 = new LinkedList<>();
        bodyM3.add(tableT_can);
        bodyM3.add(fac.getFunctionIsNotNull(t4));
        bodyM3.add(fac.getFunctionIsNotNull(t0));
        bodyM3.add(fac.getFunctionIsNotNull(t3));

        mappings.add(fac.getCQIE(headM3,bodyM3));


        //other mappings
        Function headM4 = getDataPropertyFunction("http://ontop.inf.unibz.it/test/wellbore#name", fac.getUriTemplate(epdsURI,t0), t2, Predicate.COL_TYPE.LITERAL);
        Function headM5 = getDataPropertyFunction("http://ontop.inf.unibz.it/test/wellbore#name", fac.getUriTemplate(npdURI,t0), t2, Predicate.COL_TYPE.LITERAL);
        Function headM6 = getDataPropertyFunction("http://ontop.inf.unibz.it/test/wellbore#name", fac.getUriTemplate(owURI,t0), t2, Predicate.COL_TYPE.LITERAL);

        List<Function> bodyM4 = new LinkedList<>();
        List<Term> atomTerms2 = new LinkedList<>();
        atomTerms2.add(t0);
        atomTerms2.add(t1);
        atomTerms2.add(t2);

        Function tableEPDS = getFunction("PUBLIC.T1", new LinkedList<>(atomTerms2));
        bodyM4.add(tableEPDS);
        bodyM4.add(fac.getFunctionIsNotNull(t0));

        mappings.add(fac.getCQIE(headM4,bodyM4));

        List<Function> bodyM5 = new LinkedList<>();

        Function tableNPD = getFunction("PUBLIC.T2", new LinkedList<>(atomTerms2));
        bodyM5.add(tableNPD);
        bodyM5.add(fac.getFunctionIsNotNull(t0));

        mappings.add(fac.getCQIE(headM5,bodyM5));

        List<Function> bodyM6 = new LinkedList<>();

        Function tableOW = getFunction("PUBLIC.T3", new LinkedList<>(atomTerms2));
        bodyM6.add(tableOW);
        bodyM6.add(fac.getFunctionIsNotNull(t0));

        mappings.add(fac.getCQIE(headM6,bodyM6));


    }

    private Function getFunction(String name, List<Term> terms) {
        return fac.getFunction(fac.getPredicate(name, terms.size()), terms);
    }

    private Function getSameAsFunction( Term term1, Term term2) {
        List<Term> list = new ArrayList<>(2);
        list.add(term1);
        list.add(term2);
        return fac.getFunction(fac.getOWLSameASPredicate(), list);
    }

    private Function getDataPropertyFunction(String name, Term term1, Term term2, Predicate.COL_TYPE type) {

        List<Term> list = new ArrayList<>(2);
        list.add(term1);
        list.add(term2);
        return fac.getFunction(fac.getDataPropertyPredicate(name, type), list);

    }

    private Function getObjectPropertyFunction(String name, Term term1, Term term2) {
        List<Term> list = new ArrayList<>(2);
        list.add(term1);
        list.add(term2);
        return fac.getFunction(fac.getObjectPropertyPredicate(name), list);

    }



    @Test
    public void testSameAs1() throws Exception {

        List<CQIE> canonicalSameAsMappings = SameAsRewriting.getCanonicalSameAsMappings(mappings);

        System.out.print(canonicalSameAsMappings);

    }










}
