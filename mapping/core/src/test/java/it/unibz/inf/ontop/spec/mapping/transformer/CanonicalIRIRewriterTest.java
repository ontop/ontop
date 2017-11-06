package it.unibz.inf.ontop.spec.mapping.transformer;

import com.google.common.base.Joiner;
import it.unibz.inf.ontop.datalog.CQIE;
import it.unibz.inf.ontop.model.term.Function;
import it.unibz.inf.ontop.model.term.Term;
import it.unibz.inf.ontop.model.term.ValueConstant;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.spec.mapping.transformer.impl.CanonicalIRIRewriter;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static it.unibz.inf.ontop.utils.MappingTestingTools.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *Test correctness rewriting of mappings having a canonical URI
 */

public class CanonicalIRIRewriterTest {


    private static List<CQIE> mappings;

    private static Variable t0 = TERM_FACTORY.getVariable("t0");
    private static Variable t1 = TERM_FACTORY.getVariable("t1");
    private static Variable t2 = TERM_FACTORY.getVariable("t2");
    private static Variable t3 = TERM_FACTORY.getVariable("t3");
    private static Variable t4 = TERM_FACTORY.getVariable("t4");
    private static Variable t5 = TERM_FACTORY.getVariable("t5");
    private static Variable t0_can = TERM_FACTORY.getVariable("t0_canonical0");
    private static Variable t1_can = TERM_FACTORY.getVariable("t1_canonical0");
    private static Variable t2_can = TERM_FACTORY.getVariable("t2_canonical0");
    private static Variable t3_can = TERM_FACTORY.getVariable("t3_canonical0");
    private static Variable t4_can = TERM_FACTORY.getVariable("t4_canonical0");

    private static ValueConstant canonURI = TERM_FACTORY.getConstantLiteral("http://ontop/wellbore/{}/{}");
    private static ValueConstant npdURI = TERM_FACTORY.getConstantLiteral("http://npd/wellbore/{}");
    private static ValueConstant epdsURI = TERM_FACTORY.getConstantLiteral("http://epds/wellbore/{}");
    private static ValueConstant owURI = TERM_FACTORY.getConstantLiteral("http://ow/wellbore/{}");
    private static ValueConstant unchangedURI = TERM_FACTORY.getConstantLiteral("http://unchanged/Technician/{}");


    @Before
    public void setUp() throws Exception {

        mappings = new LinkedList<>();

        //sameAs mappings

        Function headM1 = getCanonIRIFunction(TERM_FACTORY.getUriTemplate(canonURI,t1,t0), TERM_FACTORY.getUriTemplate(epdsURI, t2));
        Function headM2 = getCanonIRIFunction(TERM_FACTORY.getUriTemplate(canonURI,t1,t0), TERM_FACTORY.getUriTemplate(npdURI, t3));
        Function headM3 = getCanonIRIFunction(TERM_FACTORY.getUriTemplate(canonURI,t1,t0), TERM_FACTORY.getUriTemplate(owURI, t4));

        List<Function> bodyM1 = new LinkedList<>();
        List<Term> atomTerms1 = new LinkedList<>();
        atomTerms1.add(t0);
        atomTerms1.add(t1);
        atomTerms1.add(t2);
        atomTerms1.add(t3);
        atomTerms1.add(t4);

        Function tableT_can = getFunction("PUBLIC.T_CAN_LINK", new LinkedList<>(atomTerms1));

        bodyM1.add(tableT_can);
        bodyM1.add(TERM_FACTORY.getFunctionIsNotNull(t2));
        bodyM1.add(TERM_FACTORY.getFunctionIsNotNull(t0));
        bodyM1.add(TERM_FACTORY.getFunctionIsNotNull(t1));


        mappings.add(DATALOG_FACTORY.getCQIE(headM1,bodyM1));

        List<Function> bodyM2 = new LinkedList<>();
        bodyM2.add(tableT_can);
        bodyM2.add(TERM_FACTORY.getFunctionIsNotNull(t3));
        bodyM2.add(TERM_FACTORY.getFunctionIsNotNull(t0));
        bodyM2.add(TERM_FACTORY.getFunctionIsNotNull(t1));

        mappings.add(DATALOG_FACTORY.getCQIE(headM2,bodyM2));

        List<Function> bodyM3 = new LinkedList<>();
        bodyM3.add(tableT_can);
        bodyM3.add(TERM_FACTORY.getFunctionIsNotNull(t4));
        bodyM3.add(TERM_FACTORY.getFunctionIsNotNull(t0));
        bodyM3.add(TERM_FACTORY.getFunctionIsNotNull(t1));

        mappings.add(DATALOG_FACTORY.getCQIE(headM3,bodyM3));





    }

    private Function getFunction(String name, List<Term> terms) {
        return TERM_FACTORY.getFunction(TERM_FACTORY.getPredicate(name, terms.size()), terms);
    }

    private Function getCanonIRIFunction(Term term1, Term term2) {
        List<Term> list = new ArrayList<>(2);
        list.add(term1);
        list.add(term2);
        return TERM_FACTORY.getFunction(ATOM_FACTORY.getOBDACanonicalIRI(), list);
    }

    private Function getClassPropertyFunction(String name, Term term1) {
        return TERM_FACTORY.getFunction(ATOM_FACTORY.getClassPredicate(name), term1);

    }
    private Function getDataPropertyFunction(String name, Term term1, Term term2) {

        List<Term> list = new ArrayList<>(2);
        list.add(term1);
        list.add(term2);
        return TERM_FACTORY.getFunction(ATOM_FACTORY.getDataPropertyPredicate(name, TYPE_FACTORY.getAbstractRDFSLiteral()), list);

    }

    private Function getObjectPropertyFunction(String name, Term term1, Term term2) {
        List<Term> list = new ArrayList<>(2);
        list.add(term1);
        list.add(term2);
        return TERM_FACTORY.getFunction(ATOM_FACTORY.getObjectPropertyPredicate(name), list);

    }

    private void addDataPropertiesMappings(){

        //other mappings with data property
        Function headM1 = getDataPropertyFunction("http://ontop.inf.unibz.it/test/wellbore#name", TERM_FACTORY.getUriTemplate(epdsURI,t0), t2);
        Function headM2 = getDataPropertyFunction("http://ontop.inf.unibz.it/test/wellbore#name", TERM_FACTORY.getUriTemplate(npdURI,t0), t2);
        Function headM3 = getDataPropertyFunction("http://ontop.inf.unibz.it/test/wellbore#name", TERM_FACTORY.getUriTemplate(owURI,t0), t2);

        List<Function> bodyM1 = new LinkedList<>();
        List<Term> atomTerms = new LinkedList<>();
        atomTerms.add(t0);
        atomTerms.add(t1);
        atomTerms.add(t2);

        Function tableEPDS = getFunction("PUBLIC.T1", new LinkedList<>(atomTerms));
        bodyM1.add(tableEPDS);
        bodyM1.add(TERM_FACTORY.getFunctionIsNotNull(t0));

        mappings.add(DATALOG_FACTORY.getCQIE(headM1,bodyM1));

        List<Function> bodyM2 = new LinkedList<>();

        Function tableNPD = getFunction("PUBLIC.T2", new LinkedList<>(atomTerms));
        bodyM2.add(tableNPD);
        bodyM2.add(TERM_FACTORY.getFunctionIsNotNull(t0));

        mappings.add(DATALOG_FACTORY.getCQIE(headM2,bodyM2));

        List<Function> bodyM3 = new LinkedList<>();

        Function tableOW = getFunction("PUBLIC.T3", new LinkedList<>(atomTerms));
        bodyM3.add(tableOW);
        bodyM3.add(TERM_FACTORY.getFunctionIsNotNull(t0));

        mappings.add(DATALOG_FACTORY.getCQIE(headM3,bodyM3));

    }

    private void addClassPropertiesMappings(){

        //mappings with class
        Function headM1 = getClassPropertyFunction("http://ontop.inf.unibz.it/test/wellbore#Wellbore",
                TERM_FACTORY.getUriTemplate(epdsURI,t0));
        Function headM2 = getClassPropertyFunction("http://ontop.inf.unibz.it/test/wellbore#Wellbore",
                TERM_FACTORY.getUriTemplate(npdURI,t0));
        Function headM3 = getClassPropertyFunction("http://ontop.inf.unibz.it/test/wellbore#Wellbore",
                TERM_FACTORY.getUriTemplate(owURI,t0));

        List<Function> bodyM1 = new LinkedList<>();
        List<Term> atomTerms = new LinkedList<>();
        atomTerms.add(t0);
        atomTerms.add(t1);
        atomTerms.add(t2);

        Function tableEPDS = getFunction("PUBLIC.T1", new LinkedList<>(atomTerms));
        bodyM1.add(tableEPDS);
        bodyM1.add(TERM_FACTORY.getFunctionIsNotNull(t0));

        mappings.add(DATALOG_FACTORY.getCQIE(headM1,bodyM1));

        List<Function> bodyM2 = new LinkedList<>();

        Function tableNPD = getFunction("PUBLIC.T2", new LinkedList<>(atomTerms));
        bodyM2.add(tableNPD);
        bodyM2.add(TERM_FACTORY.getFunctionIsNotNull(t0));

        mappings.add(DATALOG_FACTORY.getCQIE(headM2,bodyM2));

        List<Function> bodyM3 = new LinkedList<>();

        Function tableOW = getFunction("PUBLIC.T3", new LinkedList<>(atomTerms));
        bodyM3.add(tableOW);
        bodyM3.add(TERM_FACTORY.getFunctionIsNotNull(t0));

        mappings.add(DATALOG_FACTORY.getCQIE(headM3,bodyM3));

    }

    private void addObjectPropertiesMappings() {

        //other mappings with object property
        Function headM1 = getObjectPropertyFunction("http://ontop.inf.unibz.it/test/wellbore#checkedBy",
                TERM_FACTORY.getUriTemplate(epdsURI,t0), TERM_FACTORY.getUriTemplate(unchangedURI, t1) );
        Function headM2 = getObjectPropertyFunction("http://ontop.inf.unibz.it/test/wellbore#checkedBy",
                TERM_FACTORY.getUriTemplate(npdURI,t0), TERM_FACTORY.getUriTemplate(unchangedURI, t1));
        Function headM3 = getObjectPropertyFunction("http://ontop.inf.unibz.it/test/wellbore#checkedBy",
                TERM_FACTORY.getUriTemplate(owURI,t0), TERM_FACTORY.getUriTemplate(unchangedURI, t1));

        List<Function> bodyM1 = new LinkedList<>();
        List<Term> atomTerms = new LinkedList<>();
        atomTerms.add(t0);
        atomTerms.add(t1);
        atomTerms.add(t2);

        Function tableEPDS = getFunction("PUBLIC.T1", new LinkedList<>(atomTerms));
        bodyM1.add(tableEPDS);
        bodyM1.add(TERM_FACTORY.getFunctionIsNotNull(t0));

        mappings.add(DATALOG_FACTORY.getCQIE(headM1,bodyM1));

        List<Function> bodyM2 = new LinkedList<>();

        Function tableNPD = getFunction("PUBLIC.T2", new LinkedList<>(atomTerms));
        bodyM2.add(tableNPD);
        bodyM2.add(TERM_FACTORY.getFunctionIsNotNull(t0));

        mappings.add(DATALOG_FACTORY.getCQIE(headM2,bodyM2));

        List<Function> bodyM3 = new LinkedList<>();

        Function tableOW = getFunction("PUBLIC.T3", new LinkedList<>(atomTerms));
        bodyM3.add(tableOW);
        bodyM3.add(TERM_FACTORY.getFunctionIsNotNull(t0));

        mappings.add(DATALOG_FACTORY.getCQIE(headM3,bodyM3));
    }

    private void addObjectPropertiesDoubleURIMappings() {

        //other mappings with object property, having a wellbore as subject and object
        Function headM1 = getObjectPropertyFunction("http://ontop.inf.unibz.it/test/wellbore#linkedTo",
                TERM_FACTORY.getUriTemplate(epdsURI,t0), TERM_FACTORY.getUriTemplate(npdURI,t3) );


        List<Function> bodyM1 = new LinkedList<>();
        List<Term> atomTerms = new LinkedList<>();
        atomTerms.add(t0);
        atomTerms.add(t1);
        atomTerms.add(t2);

        Function tableEPDS = getFunction("PUBLIC.T1", new LinkedList<>(atomTerms));
        bodyM1.add(tableEPDS);
        bodyM1.add(TERM_FACTORY.getFunctionIsNotNull(t0));



        List<Term> atomTerms2 = new LinkedList<>();
        atomTerms2.add(t3);
        atomTerms2.add(t4);
        atomTerms2.add(t5);

        Function tableNPD = getFunction("PUBLIC.T2", new LinkedList<>(atomTerms2));
        bodyM1.add(tableNPD);
        bodyM1.add(TERM_FACTORY.getFunctionIsNotNull(t3));

        mappings.add(DATALOG_FACTORY.getCQIE(headM1,bodyM1));

    }

    private void addObjectPropertiesOnlyObjectURIMappings() {

        //other mappings with object property, having a wellbore as  object
        Function headM1 = getObjectPropertyFunction("http://ontop.inf.unibz.it/test/wellbore#check",
                TERM_FACTORY.getUriTemplate(unchangedURI, t0) , TERM_FACTORY.getUriTemplate(epdsURI,t0));
        Function headM2 = getObjectPropertyFunction("http://ontop.inf.unibz.it/test/wellbore#check",
                TERM_FACTORY.getUriTemplate(unchangedURI, t0) , TERM_FACTORY.getUriTemplate(npdURI,t0));
        Function headM3 = getObjectPropertyFunction("http://ontop.inf.unibz.it/test/wellbore#check",
                TERM_FACTORY.getUriTemplate(unchangedURI, t0) , TERM_FACTORY.getUriTemplate(owURI,t0));

        List<Function> bodyM1 = new LinkedList<>();
        List<Term> atomTerms = new LinkedList<>();
        atomTerms.add(t0);
        atomTerms.add(t1);
        atomTerms.add(t2);

        Function tableEPDS = getFunction("PUBLIC.T1", new LinkedList<>(atomTerms));
        bodyM1.add(tableEPDS);
        bodyM1.add(TERM_FACTORY.getFunctionIsNotNull(t0));

        mappings.add(DATALOG_FACTORY.getCQIE(headM1,bodyM1));

        List<Function> bodyM2 = new LinkedList<>();

        Function tableNPD = getFunction("PUBLIC.T2", new LinkedList<>(atomTerms));
        bodyM2.add(tableNPD);
        bodyM2.add(TERM_FACTORY.getFunctionIsNotNull(t0));

        mappings.add(DATALOG_FACTORY.getCQIE(headM2,bodyM2));

        List<Function> bodyM3 = new LinkedList<>();

        Function tableOW = getFunction("PUBLIC.T3", new LinkedList<>(atomTerms));
        bodyM3.add(tableOW);
        bodyM3.add(TERM_FACTORY.getFunctionIsNotNull(t0));

        mappings.add(DATALOG_FACTORY.getCQIE(headM3,bodyM3));
    }


    @Test
    public void testCanonicalIRIClass() throws Exception {

        addClassPropertiesMappings();

        List<CQIE> canonicalSameAsMappings = new CanonicalIRIRewriter(SUBSTITUTION_UTILITIES, TERM_FACTORY, UNIFIER_UTILITIES)
                .buildCanonicalIRIMappings(mappings);

        System.out.print(Joiner.on("\n").join(canonicalSameAsMappings));

        assertEquals(3, canonicalSameAsMappings.size() );
        Function head = getClassPropertyFunction("http://ontop.inf.unibz.it/test/wellbore#Wellbore", TERM_FACTORY.getUriTemplate(canonURI,t1_can,t0_can));
        List<Function> body = new ArrayList<>();
        List<Term> atomTerms1 = new LinkedList<>();
        atomTerms1.add(t0_can);
        atomTerms1.add(t1_can);
        atomTerms1.add(t2_can);
        atomTerms1.add(t3_can);
        atomTerms1.add(t4_can);

        Function tableT_can = getFunction("PUBLIC.T_CAN_LINK", new LinkedList<>(atomTerms1));
        body.add(tableT_can);
        body.add(TERM_FACTORY.getFunctionIsNotNull(t2_can));
        body.add(TERM_FACTORY.getFunctionIsNotNull(t0_can));
        body.add(TERM_FACTORY.getFunctionIsNotNull(t1_can));

        List<Term> atomTerms2 = new LinkedList<>();
        atomTerms2.add(t2_can);
        atomTerms2.add(t1);
        atomTerms2.add(t2);

        Function tableT1 = getFunction("PUBLIC.T1", new LinkedList<>(atomTerms2));
        body.add(tableT1);

        assertTrue(canonicalSameAsMappings.contains(DATALOG_FACTORY.getCQIE(head,body)));

    }

    @Test
    public void testCanonicalIRIDataProperty() throws Exception {

        addClassPropertiesMappings();
        addDataPropertiesMappings();

        List<CQIE> canonicalSameAsMappings = new CanonicalIRIRewriter(SUBSTITUTION_UTILITIES, TERM_FACTORY,
                UNIFIER_UTILITIES).buildCanonicalIRIMappings(mappings);

        System.out.print(Joiner.on("\n").join(canonicalSameAsMappings));

        assertEquals(6, canonicalSameAsMappings.size() );
        Function head = getDataPropertyFunction("http://ontop.inf.unibz.it/test/wellbore#name", TERM_FACTORY.getUriTemplate(canonURI,t1_can,t0_can), t2);
        List<Function> body = new ArrayList<>();
        List<Term> atomTerms1 = new LinkedList<>();
        atomTerms1.add(t0_can);
        atomTerms1.add(t1_can);
        atomTerms1.add(t2_can);
        atomTerms1.add(t3_can);
        atomTerms1.add(t4_can);

        Function tableT_can = getFunction("PUBLIC.T_CAN_LINK", new LinkedList<>(atomTerms1));
        body.add(tableT_can);
        body.add(TERM_FACTORY.getFunctionIsNotNull(t2_can));
        body.add(TERM_FACTORY.getFunctionIsNotNull(t0_can));
        body.add(TERM_FACTORY.getFunctionIsNotNull(t1_can));

        List<Term> atomTerms2 = new LinkedList<>();
        atomTerms2.add(t2_can);
        atomTerms2.add(t1);
        atomTerms2.add(t2);

        Function tableT1 = getFunction("PUBLIC.T1", new LinkedList<>(atomTerms2));
        body.add(tableT1);

        assertTrue(canonicalSameAsMappings.contains(DATALOG_FACTORY.getCQIE(head,body)));

    }

    @Test
    public void testSubjectCanonicalIRIObjectProperty() throws Exception {

        addClassPropertiesMappings();
        addDataPropertiesMappings();
        addObjectPropertiesMappings();

        List<CQIE> canonicalSameAsMappings = new CanonicalIRIRewriter(SUBSTITUTION_UTILITIES, TERM_FACTORY,
                UNIFIER_UTILITIES).buildCanonicalIRIMappings(mappings);

        System.out.print(Joiner.on("\n").join(canonicalSameAsMappings));

        assertEquals(9, canonicalSameAsMappings.size() );
        Function head = getObjectPropertyFunction("http://ontop.inf.unibz.it/test/wellbore#checkedBy",
                TERM_FACTORY.getUriTemplate(canonURI,t1_can,t0_can),  TERM_FACTORY.getUriTemplate(unchangedURI, t1));
        List<Function> body = new ArrayList<>();
        List<Term> atomTerms1 = new LinkedList<>();
        atomTerms1.add(t0_can);
        atomTerms1.add(t1_can);
        atomTerms1.add(t2_can);
        atomTerms1.add(t3_can);
        atomTerms1.add(t4_can);

        Function tableT_can = getFunction("PUBLIC.T_CAN_LINK", new LinkedList<>(atomTerms1));
        body.add(tableT_can);
        body.add(TERM_FACTORY.getFunctionIsNotNull(t2_can));
        body.add(TERM_FACTORY.getFunctionIsNotNull(t0_can));
        body.add(TERM_FACTORY.getFunctionIsNotNull(t1_can));

        List<Term> atomTerms2 = new LinkedList<>();
        atomTerms2.add(t2_can);
        atomTerms2.add(t1);
        atomTerms2.add(t2);

        Function tableT1 = getFunction("PUBLIC.T1", new LinkedList<>(atomTerms2));
        body.add(tableT1);

        assertTrue(canonicalSameAsMappings.contains(DATALOG_FACTORY.getCQIE(head,body)));

    }

    @Test
    public void testObjectCanonicalIRIObjectProperty() throws Exception {

        addClassPropertiesMappings();
        addDataPropertiesMappings();
        addObjectPropertiesOnlyObjectURIMappings();

        List<CQIE> canonicalSameAsMappings = new CanonicalIRIRewriter(SUBSTITUTION_UTILITIES, TERM_FACTORY,
                UNIFIER_UTILITIES).buildCanonicalIRIMappings(mappings);

        System.out.print(Joiner.on("\n").join(canonicalSameAsMappings));

        assertEquals(9, canonicalSameAsMappings.size() );
        Function head = getObjectPropertyFunction("http://ontop.inf.unibz.it/test/wellbore#check",
                TERM_FACTORY.getUriTemplate(unchangedURI, t2_can),  TERM_FACTORY.getUriTemplate(canonURI,t1_can,t0_can));
        List<Function> body = new ArrayList<>();
        List<Term> atomTerms1 = new LinkedList<>();
        atomTerms1.add(t0_can);
        atomTerms1.add(t1_can);
        atomTerms1.add(t2_can);
        atomTerms1.add(t3_can);
        atomTerms1.add(t4_can);

        Function tableT_can = getFunction("PUBLIC.T_CAN_LINK", new LinkedList<>(atomTerms1));
        body.add(tableT_can);
        body.add(TERM_FACTORY.getFunctionIsNotNull(t2_can));
        body.add(TERM_FACTORY.getFunctionIsNotNull(t0_can));
        body.add(TERM_FACTORY.getFunctionIsNotNull(t1_can));

        List<Term> atomTerms2 = new LinkedList<>();
        atomTerms2.add(t2_can);
        atomTerms2.add(t1);
        atomTerms2.add(t2);

        Function tableT1 = getFunction("PUBLIC.T1", new LinkedList<>(atomTerms2));
        body.add(tableT1);

        assertTrue(canonicalSameAsMappings.contains(DATALOG_FACTORY.getCQIE(head,body)));

    }


    @Test
    public void testCanonicalIRIObjectPropertyDoubleURI() throws Exception {

        addClassPropertiesMappings();
        addDataPropertiesMappings();
        addObjectPropertiesMappings();
        addObjectPropertiesDoubleURIMappings();

        List<CQIE> canonicalSameAsMappings = new CanonicalIRIRewriter(SUBSTITUTION_UTILITIES, TERM_FACTORY,
                UNIFIER_UTILITIES).buildCanonicalIRIMappings(mappings);

        System.out.print( Joiner.on("\n").join(canonicalSameAsMappings));

        assertEquals(10, canonicalSameAsMappings.size() );
        Function head = getObjectPropertyFunction("http://ontop.inf.unibz.it/test/wellbore#linkedTo",
                TERM_FACTORY.getUriTemplate(canonURI,t1_can,t0_can), TERM_FACTORY.getUriTemplate(canonURI,t1_can,t0_can) );
        List<Function> body = new ArrayList<>();
        List<Term> atomTerms1 = new ArrayList<>();
        atomTerms1.add(t0_can);
        atomTerms1.add(t1_can);
        atomTerms1.add(t2_can);
        atomTerms1.add(t3_can);
        atomTerms1.add(t4_can);

        Function tableT_can = getFunction("PUBLIC.T_CAN_LINK", new LinkedList<>(atomTerms1));
        body.add(tableT_can);
        body.add(TERM_FACTORY.getFunctionIsNotNull(t3_can));
        body.add(TERM_FACTORY.getFunctionIsNotNull(t0_can));
        body.add(TERM_FACTORY.getFunctionIsNotNull(t1_can));
        body.add(TERM_FACTORY.getFunctionIsNotNull(t2_can));

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

        assertTrue(canonicalSameAsMappings.contains(DATALOG_FACTORY.getCQIE(head,body)));

    }




}
