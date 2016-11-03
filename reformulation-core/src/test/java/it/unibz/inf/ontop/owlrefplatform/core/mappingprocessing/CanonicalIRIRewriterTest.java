package it.unibz.inf.ontop.owlrefplatform.core.mappingprocessing;

import com.google.common.base.Joiner;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *Test correctness rewriting of mappings having a canonical URI
 */

public class CanonicalIRIRewriterTest {


    private static List<CQIE> mappings;
    private static OBDADataFactory fac = OBDADataFactoryImpl.getInstance();

    private static Variable t0 =fac.getVariable("t0");
    private static Variable t1 =fac.getVariable("t1");
    private static Variable t2 =fac.getVariable("t2");
    private static Variable t3 =fac.getVariable("t3");
    private static Variable t4 =fac.getVariable("t4");
    private static Variable t5 =fac.getVariable("t5");
    private static Variable t0_can =fac.getVariable("t0_canonical0");
    private static Variable t1_can =fac.getVariable("t1_canonical0");
    private static Variable t2_can =fac.getVariable("t2_canonical0");
    private static Variable t3_can =fac.getVariable("t3_canonical0");
    private static Variable t4_can =fac.getVariable("t4_canonical0");

    private static ValueConstant canonURI = fac.getConstantLiteral("http://ontop/wellbore/{}/{}");
    private static ValueConstant npdURI = fac.getConstantLiteral("http://npd/wellbore/{}");
    private static ValueConstant epdsURI = fac.getConstantLiteral("http://epds/wellbore/{}");
    private static ValueConstant owURI = fac.getConstantLiteral("http://ow/wellbore/{}");


    @Before
    public void setUp() throws Exception {

        mappings = new LinkedList<>();

        //sameAs mappings

        Function headM1 = getCanonURIFunction(fac.getUriTemplate(canonURI,t1,t0), fac.getUriTemplate(epdsURI, t2));
        Function headM2 = getCanonURIFunction(fac.getUriTemplate(canonURI,t1,t0), fac.getUriTemplate(npdURI, t3));
        Function headM3 = getCanonURIFunction(fac.getUriTemplate(canonURI,t1,t0), fac.getUriTemplate(owURI, t4));

        List<Function> bodyM1 = new LinkedList<>();
        List<Term> atomTerms1 = new LinkedList<>();
        atomTerms1.add(t0);
        atomTerms1.add(t1);
        atomTerms1.add(t2);
        atomTerms1.add(t3);
        atomTerms1.add(t4);

        Function tableT_can = getFunction("PUBLIC.T_CAN_LINK", new LinkedList<>(atomTerms1));

        bodyM1.add(tableT_can);
        bodyM1.add(fac.getFunctionIsNotNull(t2));
        bodyM1.add(fac.getFunctionIsNotNull(t0));
        bodyM1.add(fac.getFunctionIsNotNull(t1));


        mappings.add(fac.getCQIE(headM1,bodyM1));

        List<Function> bodyM2 = new LinkedList<>();
        bodyM2.add(tableT_can);
        bodyM2.add(fac.getFunctionIsNotNull(t3));
        bodyM2.add(fac.getFunctionIsNotNull(t0));
        bodyM2.add(fac.getFunctionIsNotNull(t1));

        mappings.add(fac.getCQIE(headM2,bodyM2));

        List<Function> bodyM3 = new LinkedList<>();
        bodyM3.add(tableT_can);
        bodyM3.add(fac.getFunctionIsNotNull(t4));
        bodyM3.add(fac.getFunctionIsNotNull(t0));
        bodyM3.add(fac.getFunctionIsNotNull(t1));

        mappings.add(fac.getCQIE(headM3,bodyM3));





    }

    private Function getFunction(String name, List<Term> terms) {
        return fac.getFunction(fac.getPredicate(name, terms.size()), terms);
    }

    private Function getCanonURIFunction( Term term1, Term term2) {
        List<Term> list = new ArrayList<>(2);
        list.add(term1);
        list.add(term2);
        return fac.getFunction(fac.getOntopCanonicalIRI(), list);
    }

    private Function getClassPropertyFunction(String name, Term term1) {
        return fac.getFunction(fac.getClassPredicate(name), term1);

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

    private void addDataPropertiesMappings(){

        //other mappings with data property
        Function headM1 = getDataPropertyFunction("http://ontop.inf.unibz.it/test/wellbore#name", fac.getUriTemplate(epdsURI,t0), t2, Predicate.COL_TYPE.LITERAL);
        Function headM2 = getDataPropertyFunction("http://ontop.inf.unibz.it/test/wellbore#name", fac.getUriTemplate(npdURI,t0), t2, Predicate.COL_TYPE.LITERAL);
        Function headM3 = getDataPropertyFunction("http://ontop.inf.unibz.it/test/wellbore#name", fac.getUriTemplate(owURI,t0), t2, Predicate.COL_TYPE.LITERAL);

        List<Function> bodyM1 = new LinkedList<>();
        List<Term> atomTerms = new LinkedList<>();
        atomTerms.add(t0);
        atomTerms.add(t1);
        atomTerms.add(t2);

        Function tableEPDS = getFunction("PUBLIC.T1", new LinkedList<>(atomTerms));
        bodyM1.add(tableEPDS);
        bodyM1.add(fac.getFunctionIsNotNull(t0));

        mappings.add(fac.getCQIE(headM1,bodyM1));

        List<Function> bodyM2 = new LinkedList<>();

        Function tableNPD = getFunction("PUBLIC.T2", new LinkedList<>(atomTerms));
        bodyM2.add(tableNPD);
        bodyM2.add(fac.getFunctionIsNotNull(t0));

        mappings.add(fac.getCQIE(headM2,bodyM2));

        List<Function> bodyM3 = new LinkedList<>();

        Function tableOW = getFunction("PUBLIC.T3", new LinkedList<>(atomTerms));
        bodyM3.add(tableOW);
        bodyM3.add(fac.getFunctionIsNotNull(t0));

        mappings.add(fac.getCQIE(headM3,bodyM3));

    }

    private void addClassPropertiesMappings(){

        //mappings with class
        Function headM1 = getClassPropertyFunction("http://ontop.inf.unibz.it/test/wellbore#Wellbore", fac.getUriTemplate(epdsURI,t0));
        Function headM2 = getClassPropertyFunction("http://ontop.inf.unibz.it/test/wellbore#Wellbore", fac.getUriTemplate(npdURI,t0));
        Function headM3 = getClassPropertyFunction("http://ontop.inf.unibz.it/test/wellbore#Wellbore", fac.getUriTemplate(owURI,t0));

        List<Function> bodyM1 = new LinkedList<>();
        List<Term> atomTerms = new LinkedList<>();
        atomTerms.add(t0);
        atomTerms.add(t1);
        atomTerms.add(t2);

        Function tableEPDS = getFunction("PUBLIC.T1", new LinkedList<>(atomTerms));
        bodyM1.add(tableEPDS);
        bodyM1.add(fac.getFunctionIsNotNull(t0));

        mappings.add(fac.getCQIE(headM1,bodyM1));

        List<Function> bodyM2 = new LinkedList<>();

        Function tableNPD = getFunction("PUBLIC.T2", new LinkedList<>(atomTerms));
        bodyM2.add(tableNPD);
        bodyM2.add(fac.getFunctionIsNotNull(t0));

        mappings.add(fac.getCQIE(headM2,bodyM2));

        List<Function> bodyM3 = new LinkedList<>();

        Function tableOW = getFunction("PUBLIC.T3", new LinkedList<>(atomTerms));
        bodyM3.add(tableOW);
        bodyM3.add(fac.getFunctionIsNotNull(t0));

        mappings.add(fac.getCQIE(headM3,bodyM3));

    }

    private void addObjectPropertiesMappings() {

        //other mappings with object property
        Function headM1 = getObjectPropertyFunction("http://ontop.inf.unibz.it/test/wellbore#checkedBy", fac.getUriTemplate(epdsURI,t0), fac.getUriTemplate(fac.getConstantLiteral("http://ontop.inf.unibz.it/test/wellbore#Technician"), t1) );
        Function headM2 = getObjectPropertyFunction("http://ontop.inf.unibz.it/test/wellbore#checkedBy", fac.getUriTemplate(npdURI,t0), fac.getUriTemplate(fac.getConstantLiteral("http://ontop.inf.unibz.it/test/wellbore#Technician"), t1));
        Function headM3 = getObjectPropertyFunction("http://ontop.inf.unibz.it/test/wellbore#checkedBy", fac.getUriTemplate(owURI,t0), fac.getUriTemplate(fac.getConstantLiteral("http://ontop.inf.unibz.it/test/wellbore#Technician"), t1));

        List<Function> bodyM1 = new LinkedList<>();
        List<Term> atomTerms = new LinkedList<>();
        atomTerms.add(t0);
        atomTerms.add(t1);
        atomTerms.add(t2);

        Function tableEPDS = getFunction("PUBLIC.T1", new LinkedList<>(atomTerms));
        bodyM1.add(tableEPDS);
        bodyM1.add(fac.getFunctionIsNotNull(t0));

        mappings.add(fac.getCQIE(headM1,bodyM1));

        List<Function> bodyM2 = new LinkedList<>();

        Function tableNPD = getFunction("PUBLIC.T2", new LinkedList<>(atomTerms));
        bodyM2.add(tableNPD);
        bodyM2.add(fac.getFunctionIsNotNull(t0));

        mappings.add(fac.getCQIE(headM2,bodyM2));

        List<Function> bodyM3 = new LinkedList<>();

        Function tableOW = getFunction("PUBLIC.T3", new LinkedList<>(atomTerms));
        bodyM3.add(tableOW);
        bodyM3.add(fac.getFunctionIsNotNull(t0));

        mappings.add(fac.getCQIE(headM3,bodyM3));
    }

    private void addObjectPropertiesDoubleURIMappings() {

        //other mappings with object property, having a wellbore as subject and object
        Function headM1 = getObjectPropertyFunction("http://ontop.inf.unibz.it/test/wellbore#linkedTo", fac.getUriTemplate(epdsURI,t0), fac.getUriTemplate(npdURI,t3) );


        List<Function> bodyM1 = new LinkedList<>();
        List<Term> atomTerms = new LinkedList<>();
        atomTerms.add(t0);
        atomTerms.add(t1);
        atomTerms.add(t2);

        Function tableEPDS = getFunction("PUBLIC.T1", new LinkedList<>(atomTerms));
        bodyM1.add(tableEPDS);
        bodyM1.add(fac.getFunctionIsNotNull(t0));



        List<Term> atomTerms2 = new LinkedList<>();
        atomTerms2.add(t3);
        atomTerms2.add(t4);
        atomTerms2.add(t5);

        Function tableNPD = getFunction("PUBLIC.T2", new LinkedList<>(atomTerms2));
        bodyM1.add(tableNPD);
        bodyM1.add(fac.getFunctionIsNotNull(t3));

        mappings.add(fac.getCQIE(headM1,bodyM1));

    }


    @Test
    public void testSameAsClass() throws Exception {

        addClassPropertiesMappings();

        List<CQIE> canonicalSameAsMappings = new CanonicalIRIRewriter().buildCanonicalSameAsMappings(mappings);

        System.out.print(Joiner.on("\n").join(canonicalSameAsMappings));

        assertEquals(3, canonicalSameAsMappings.size() );
        Function head = getClassPropertyFunction("http://ontop.inf.unibz.it/test/wellbore#Wellbore", fac.getUriTemplate(canonURI,t1_can,t0_can));
        List<Function> body = new ArrayList<>();
        List<Term> atomTerms1 = new LinkedList<>();
        atomTerms1.add(t0_can);
        atomTerms1.add(t1_can);
        atomTerms1.add(t2_can);
        atomTerms1.add(t3_can);
        atomTerms1.add(t4_can);

        Function tableT_can = getFunction("PUBLIC.T_CAN_LINK", new LinkedList<>(atomTerms1));
        body.add(tableT_can);
        body.add(fac.getFunctionIsNotNull(t2_can));
        body.add(fac.getFunctionIsNotNull(t0_can));
        body.add(fac.getFunctionIsNotNull(t1_can));

        List<Term> atomTerms2 = new LinkedList<>();
        atomTerms2.add(t2_can);
        atomTerms2.add(t1);
        atomTerms2.add(t2);

        Function tableT1 = getFunction("PUBLIC.T1", new LinkedList<>(atomTerms2));
        body.add(tableT1);

        assertTrue(canonicalSameAsMappings.contains(fac.getCQIE(head,body)));

    }

    @Test
    public void testSameAsDataProperty() throws Exception {

        addClassPropertiesMappings();
        addDataPropertiesMappings();

        List<CQIE> canonicalSameAsMappings = new CanonicalIRIRewriter().buildCanonicalSameAsMappings(mappings);

        System.out.print(Joiner.on("\n").join(canonicalSameAsMappings));

        assertEquals(6, canonicalSameAsMappings.size() );
        Function head = getDataPropertyFunction("http://ontop.inf.unibz.it/test/wellbore#name", fac.getUriTemplate(canonURI,t1_can,t0_can), t2, Predicate.COL_TYPE.LITERAL);
        List<Function> body = new ArrayList<>();
        List<Term> atomTerms1 = new LinkedList<>();
        atomTerms1.add(t0_can);
        atomTerms1.add(t1_can);
        atomTerms1.add(t2_can);
        atomTerms1.add(t3_can);
        atomTerms1.add(t4_can);

        Function tableT_can = getFunction("PUBLIC.T_CAN_LINK", new LinkedList<>(atomTerms1));
        body.add(tableT_can);
        body.add(fac.getFunctionIsNotNull(t2_can));
        body.add(fac.getFunctionIsNotNull(t0_can));
        body.add(fac.getFunctionIsNotNull(t1_can));

        List<Term> atomTerms2 = new LinkedList<>();
        atomTerms2.add(t2_can);
        atomTerms2.add(t1);
        atomTerms2.add(t2);

        Function tableT1 = getFunction("PUBLIC.T1", new LinkedList<>(atomTerms2));
        body.add(tableT1);

        assertTrue(canonicalSameAsMappings.contains(fac.getCQIE(head,body)));

    }

    @Test
    public void testSameAsObjectProperty() throws Exception {

        addClassPropertiesMappings();
        addDataPropertiesMappings();
        addObjectPropertiesMappings();

        List<CQIE> canonicalSameAsMappings = new CanonicalIRIRewriter().buildCanonicalSameAsMappings(mappings);

        System.out.print(Joiner.on("\n").join(canonicalSameAsMappings));

        assertEquals(9, canonicalSameAsMappings.size() );
        Function head = getObjectPropertyFunction("http://ontop.inf.unibz.it/test/wellbore#checkedBy", fac.getUriTemplate(canonURI,t1_can,t0_can),  fac.getUriTemplate(fac.getConstantLiteral("http://ontop.inf.unibz.it/test/wellbore#Technician"), t1));
        List<Function> body = new ArrayList<>();
        List<Term> atomTerms1 = new LinkedList<>();
        atomTerms1.add(t0_can);
        atomTerms1.add(t1_can);
        atomTerms1.add(t2_can);
        atomTerms1.add(t3_can);
        atomTerms1.add(t4_can);

        Function tableT_can = getFunction("PUBLIC.T_CAN_LINK", new LinkedList<>(atomTerms1));
        body.add(tableT_can);
        body.add(fac.getFunctionIsNotNull(t2_can));
        body.add(fac.getFunctionIsNotNull(t0_can));
        body.add(fac.getFunctionIsNotNull(t1_can));

        List<Term> atomTerms2 = new LinkedList<>();
        atomTerms2.add(t2_can);
        atomTerms2.add(t1);
        atomTerms2.add(t2);

        Function tableT1 = getFunction("PUBLIC.T1", new LinkedList<>(atomTerms2));
        body.add(tableT1);

        assertTrue(canonicalSameAsMappings.contains(fac.getCQIE(head,body)));

    }

    @Test
    public void testSameAsObjectPropertyDoubleURI() throws Exception {

        addClassPropertiesMappings();
        addDataPropertiesMappings();
        addObjectPropertiesMappings();
        addObjectPropertiesDoubleURIMappings();

        List<CQIE> canonicalSameAsMappings = new CanonicalIRIRewriter().buildCanonicalSameAsMappings(mappings);

        System.out.print( Joiner.on("\n").join(canonicalSameAsMappings));

        assertEquals(10, canonicalSameAsMappings.size() );
        Function head = getObjectPropertyFunction("http://ontop.inf.unibz.it/test/wellbore#linkedTo", fac.getUriTemplate(canonURI,t1_can,t0_can), fac.getUriTemplate(canonURI,t1_can,t0_can) );
        List<Function> body = new ArrayList<>();
        List<Term> atomTerms1 = new ArrayList<>();
        atomTerms1.add(t0_can);
        atomTerms1.add(t1_can);
        atomTerms1.add(t2_can);
        atomTerms1.add(t3_can);
        atomTerms1.add(t4_can);

        Function tableT_can = getFunction("PUBLIC.T_CAN_LINK", new LinkedList<>(atomTerms1));
        body.add(tableT_can);
        body.add(fac.getFunctionIsNotNull(t3_can));
        body.add(fac.getFunctionIsNotNull(t0_can));
        body.add(fac.getFunctionIsNotNull(t1_can));
        body.add(fac.getFunctionIsNotNull(t2_can));

        List<Term> atomTerms2 = new ArrayList<>();
        atomTerms2.add(t2_can);
        atomTerms2.add(t1);
        atomTerms2.add(t2);

        Function tableT1 = getFunction("PUBLIC.T1", new LinkedList<>(atomTerms2));
        body.add(tableT1);

        List<Term> atomTerms = new ArrayList<>();
        atomTerms.add(t3_can);
        atomTerms.add(t4);
        atomTerms.add(t5);

        Function tableT2 = getFunction("PUBLIC.T2", new LinkedList<>(atomTerms));
        body.add(tableT2);

        assertTrue(canonicalSameAsMappings.contains(fac.getCQIE(head,body)));

    }




}
