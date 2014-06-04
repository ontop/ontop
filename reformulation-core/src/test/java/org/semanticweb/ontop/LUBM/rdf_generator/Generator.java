package org.semanticweb.ontop.LUBM.rdf_generator;

/*
 * #%L
 * ontop-reformulation-core
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Random;

public class Generator {

    ///////////////////////////////////////////////////////////////////////////
    //ontology class information
    //NOTE: prefix "CS" was used because the predecessor of univ-bench ontology
    //is called cs ontolgy.
    ///////////////////////////////////////////////////////////////////////////
    /**
     * n/a
     */
    static final int CS_C_NULL = -1;
    /**
     * University
     */
    static final int CS_C_UNIV = 0;
    /**
     * Department
     */
    static final int CS_C_DEPT = CS_C_UNIV + 1;
    /**
     * Faculty
     */
    static final int CS_C_FACULTY = CS_C_DEPT + 1;
    /**
     * Professor
     */
    static final int CS_C_PROF = CS_C_FACULTY + 1;
    /**
     * FullProfessor
     */
    static final int CS_C_FULLPROF = CS_C_PROF + 1;
    /**
     * AssociateProfessor
     */
    static final int CS_C_ASSOPROF = CS_C_FULLPROF + 1;
    /**
     * AssistantProfessor
     */
    static final int CS_C_ASSTPROF = CS_C_ASSOPROF + 1;
    /**
     * Lecturer
     */
    static final int CS_C_LECTURER = CS_C_ASSTPROF + 1;
    /**
     * Student
     */
    static final int CS_C_STUDENT = CS_C_LECTURER + 1;
    /**
     * UndergraduateStudent
     */
    static final int CS_C_UNDERSTUD = CS_C_STUDENT + 1;
    /**
     * GraduateStudent
     */
    static final int CS_C_GRADSTUD = CS_C_UNDERSTUD + 1;
    /**
     * TeachingAssistant
     */
    static final int CS_C_TA = CS_C_GRADSTUD + 1;
    /**
     * ResearchAssistant
     */
    static final int CS_C_RA = CS_C_TA + 1;
    /**
     * Course
     */
    static final int CS_C_COURSE = CS_C_RA + 1;
    /**
     * GraduateCourse
     */
    static final int CS_C_GRADCOURSE = CS_C_COURSE + 1;
    /**
     * Publication
     */
    static final int CS_C_PUBLICATION = CS_C_GRADCOURSE + 1;
    /**
     * Chair
     */
    static final int CS_C_CHAIR = CS_C_PUBLICATION + 1;
    /**
     * Research
     */
    static final int CS_C_RESEARCH = CS_C_CHAIR + 1;
    /**
     * ResearchGroup
     */
    static final int CS_C_RESEARCHGROUP = CS_C_RESEARCH + 1;
    /**
     * class information
     */
    static final int[][] CLASS_INFO = {
            /*{instance number if not specified, direct super class}*/
            //NOTE: the super classes specifed here do not necessarily reflect the entailment of the ontology
            {2, CS_C_NULL}, //CS_C_UNIV
            {1, CS_C_NULL}, //CS_C_DEPT
            {0, CS_C_NULL}, //CS_C_FACULTY
            {0, CS_C_FACULTY}, //CS_C_PROF
            {0, CS_C_PROF}, //CS_C_FULLPROF
            {0, CS_C_PROF}, //CS_C_ASSOPROF
            {0, CS_C_PROF}, //CS_C_ASSTPROF
            {0, CS_C_FACULTY}, //CS_C_LECTURER
            {0, CS_C_NULL}, //CS_C_STUDENT
            {0, CS_C_STUDENT}, //CS_C_UNDERSTUD
            {0, CS_C_STUDENT}, //CS_C_GRADSTUD
            {0, CS_C_NULL}, //CS_C_TA
            {0, CS_C_NULL}, //CS_C_RA
            {0, CS_C_NULL}, //CS_C_COURSE, treated as undergrad course here
            {0, CS_C_NULL}, //CS_C_GRADCOURSE
            {0, CS_C_NULL}, //CS_C_PUBLICATION
            {0, CS_C_NULL}, //CS_C_CHAIR
            {0, CS_C_NULL}, //CS_C_RESEARCH
            {0, CS_C_NULL} //CS_C_RESEARCHGROUP
    };
    /**
     * class name strings
     */
    static final String[] CLASS_TOKEN = {
            "University", //CS_C_UNIV
            "Department", //CS_C_DEPT
            "Faculty", //CS_C_FACULTY
            "Professor", //CS_C_PROF
            "FullProfessor", //CS_C_FULLPROF
            "AssociateProfessor", //CS_C_ASSOPROF
            "AssistantProfessor", //CS_C_ASSTPROF
            "Lecturer", //CS_C_LECTURER
            "Student", //CS_C_STUDENT
            "UndergraduateStudent", //CS_C_UNDERSTUD
            "GraduateStudent", //CS_C_GRADSTUD
            "TeachingAssistant", //CS_C_TA
            "ResearchAssistant", //CS_C_RA
            "Course", //CS_C_COURSE
            "GraduateCourse", //CS_C_GRADCOURSE
            "Publication", //CS_C_PUBLICATION
            "Chair", //CS_C_CHAIR
            "Research", //CS_C_RESEARCH
            "ResearchGroup" //CS_C_RESEARCHGROUP
    };
    /**
     * number of classes
     */
    static final int CLASS_NUM = CLASS_INFO.length;
    /**
     * index of instance-number in the elements of array CLASS_INFO
     */
    static final int INDEX_NUM = 0;
    /**
     * index of super-class in the elements of array CLASS_INFO
     */
    static final int INDEX_SUPER = 1;

    ///////////////////////////////////////////////////////////////////////////
    //ontology property information
    ///////////////////////////////////////////////////////////////////////////
    /**
     * name
     */
    static final int CS_P_NAME = 0;
    /**
     * takesCourse
     */
    static final int CS_P_TAKECOURSE = CS_P_NAME + 1;
    /**
     * teacherOf
     */
    static final int CS_P_TEACHEROF = CS_P_TAKECOURSE + 1;
    /**
     * undergraduateDegreeFrom
     */
    static final int CS_P_UNDERGRADFROM = CS_P_TEACHEROF + 1;
    /**
     * mastersDegreeFrom
     */
    static final int CS_P_GRADFROM = CS_P_UNDERGRADFROM + 1;
    /**
     * doctoralDegreeFrom
     */
    static final int CS_P_DOCFROM = CS_P_GRADFROM + 1;
    /**
     * advisor
     */
    static final int CS_P_ADVISOR = CS_P_DOCFROM + 1;
    /**
     * memberOf
     */
    static final int CS_P_MEMBEROF = CS_P_ADVISOR + 1;
    /**
     * publicationAuthor
     */
    static final int CS_P_PUBLICATIONAUTHOR = CS_P_MEMBEROF + 1;
    /**
     * headOf
     */
    static final int CS_P_HEADOF = CS_P_PUBLICATIONAUTHOR + 1;
    /**
     * teachingAssistantOf
     */
    static final int CS_P_TAOF = CS_P_HEADOF + 1;
    /**
     * reseachAssistantOf
     */
    static final int CS_P_RESEARCHINTEREST = CS_P_TAOF + 1;
    /**
     * emailAddress
     */
    static final int CS_P_EMAIL = CS_P_RESEARCHINTEREST + 1;
    /**
     * telephone
     */
    static final int CS_P_TELEPHONE = CS_P_EMAIL + 1;
    /**
     * subOrganizationOf
     */
    static final int CS_P_SUBORGANIZATIONOF = CS_P_TELEPHONE + 1;
    /**
     * worksFor
     */
    static final int CS_P_WORKSFOR = CS_P_SUBORGANIZATIONOF + 1;
    /**
     * property name strings
     */
    static final String[] PROP_TOKEN = {
            "name",
            "takesCourse",
            "teacherOf",
            "undergraduateDegreeFrom",
            "mastersDegreeFrom",
            "doctoralDegreeFrom",
            "advisor",
            "memberOf",
            "publicationAuthor",
            "headOf",
            "teachingAssistantOf",
            "researchInterest",
            "emailAddress",
            "telephone",
            "subOrganizationOf",
            "worksFor"
    };
    /**
     * number of properties
     */
    static final int PROP_NUM = PROP_TOKEN.length;

    ///////////////////////////////////////////////////////////////////////////
    //restrictions for data generation
    ///////////////////////////////////////////////////////////////////////////
    /**
     * size of the pool of the undergraduate courses for one department
     */
    private static final int UNDER_COURSE_NUM = 100; //must >= max faculty # * FACULTY_COURSE_MAX
    /**
     * size of the pool of the graduate courses for one department
     */
    private static final int GRAD_COURSE_NUM = 100; //must >= max faculty # * FACULTY_GRADCOURSE_MAX
    /**
     * size of the pool of universities
     */
    private static final int UNIV_NUM = 1000;
    /**
     * size of the pool of reasearch areas
     */
    private static final int RESEARCH_NUM = 30;
    /**
     * minimum number of departments in a university
     */
    private static final int DEPT_MIN = 15;
    /**
     * maximum number of departments in a university
     */
    private static final int DEPT_MAX = 25;
    //must: DEPT_MAX - DEPT_MIN + 1 <> 2 ^ n
    /**
     * minimum number of publications of a full professor
     */
    private static final int FULLPROF_PUB_MIN = 15;
    /**
     * maximum number of publications of a full professor
     */
    private static final int FULLPROF_PUB_MAX = 20;
    /**
     * minimum number of publications of an associate professor
     */
    private static final int ASSOPROF_PUB_MIN = 10;
    /**
     * maximum number of publications of an associate professor
     */
    private static final int ASSOPROF_PUB_MAX = 18;
    /**
     * minimum number of publications of an assistant professor
     */
    private static final int ASSTPROF_PUB_MIN = 5;
    /**
     * maximum number of publications of an assistant professor
     */
    private static final int ASSTPROF_PUB_MAX = 10;
    /**
     * minimum number of publications of a graduate student
     */
    private static final int GRADSTUD_PUB_MIN = 0;
    /**
     * maximum number of publications of a graduate student
     */
    private static final int GRADSTUD_PUB_MAX = 5;
    /**
     * minimum number of publications of a lecturer
     */
    private static final int LEC_PUB_MIN = 0;
    /**
     * maximum number of publications of a lecturer
     */
    private static final int LEC_PUB_MAX = 5;
    /**
     * minimum number of courses taught by a faculty
     */
    private static final int FACULTY_COURSE_MIN = 1;
    /**
     * maximum number of courses taught by a faculty
     */
    private static final int FACULTY_COURSE_MAX = 2;
    /**
     * minimum number of graduate courses taught by a faculty
     */
    private static final int FACULTY_GRADCOURSE_MIN = 1;
    /**
     * maximum number of graduate courses taught by a faculty
     */
    private static final int FACULTY_GRADCOURSE_MAX = 2;
    /**
     * minimum number of courses taken by a undergraduate student
     */
    private static final int UNDERSTUD_COURSE_MIN = 2;
    /**
     * maximum number of courses taken by a undergraduate student
     */
    private static final int UNDERSTUD_COURSE_MAX = 4;
    /**
     * minimum number of courses taken by a graduate student
     */
    private static final int GRADSTUD_COURSE_MIN = 1;
    /**
     * maximum number of courses taken by a graduate student
     */
    private static final int GRADSTUD_COURSE_MAX = 3;
    /**
     * minimum number of research groups in a department
     */
    private static final int RESEARCHGROUP_MIN = 10;
    /**
     * maximum number of research groups in a department
     */
    private static final int RESEARCHGROUP_MAX = 20;
    //faculty number: 30-42
    /**
     * minimum number of full professors in a department
     */
    private static final int FULLPROF_MIN = 7;
    /**
     * maximum number of full professors in a department
     */
    private static final int FULLPROF_MAX = 10;
    /**
     * minimum number of associate professors in a department
     */
    private static final int ASSOPROF_MIN = 10;
    /**
     * maximum number of associate professors in a department
     */
    private static final int ASSOPROF_MAX = 14;
    /**
     * minimum number of assistant professors in a department
     */
    private static final int ASSTPROF_MIN = 8;
    /**
     * maximum number of assistant professors in a department
     */
    private static final int ASSTPROF_MAX = 11;
    /**
     * minimum number of lecturers in a department
     */
    private static final int LEC_MIN = 5;
    /**
     * maximum number of lecturers in a department
     */
    private static final int LEC_MAX = 7;
    /**
     * minimum ratio of undergraduate students to faculties in a department
     */
    private static final int R_UNDERSTUD_FACULTY_MIN = 8;
    /**
     * maximum ratio of undergraduate students to faculties in a department
     */
    private static final int R_UNDERSTUD_FACULTY_MAX = 14;
    /**
     * minimum ratio of graduate students to faculties in a department
     */
    private static final int R_GRADSTUD_FACULTY_MIN = 3;
    /**
     * maximum ratio of graduate students to faculties in a department
     */
    private static final int R_GRADSTUD_FACULTY_MAX = 4;
    //MUST: FACULTY_COURSE_MIN >= R_GRADSTUD_FACULTY_MAX / R_GRADSTUD_TA_MIN;
    /**
     * minimum ratio of graduate students to TA in a department
     */
    private static final int R_GRADSTUD_TA_MIN = 4;
    /**
     * maximum ratio of graduate students to TA in a department
     */
    private static final int R_GRADSTUD_TA_MAX = 5;
    /**
     * minimum ratio of graduate students to RA in a department
     */
    private static final int R_GRADSTUD_RA_MIN = 3;
    /**
     * maximum ratio of graduate students to RA in a department
     */
    private static final int R_GRADSTUD_RA_MAX = 4;
    /**
     * average ratio of undergraduate students to undergraduate student advising professors
     */
    private static final int R_UNDERSTUD_ADVISOR = 5;
    /**
     * average ratio of graduate students to graduate student advising professors
     */
    private static final int R_GRADSTUD_ADVISOR = 1;

    /**
     * delimiter between different parts in an id string
     */
    private static final char ID_DELIMITER = '/';
    /**
     * delimiter between name and index in a name string of an instance
     */
    private static final char INDEX_DELIMITER = '_';
    /**
     * name of the log file
     */
    private static final String LOG_FILE = "log.txt";

    /**
     * instance count of a class
     */
    private class InstanceCount {
        /**
         * instance number within one department
         */
        public int num = 0;
        /**
         * total instance num including sub-classes within one department
         */
        public int total = 0;
        /**
         * index of the current instance within the current department
         */
        public int count = 0;
        /**
         * total number so far within the current department
         */
        public int logNum = 0;
        /**
         * total number so far
         */
        public long logTotal = 0l;
    }

    /**
     * instance count of a property
     */
    private class PropertyCount {
        /**
         * total number so far within the current department
         */
        public int logNum = 0;
        /**
         * total number so far
         */
        public long logTotal = 0l;
    }

    /**
     * information a course instance
     */
    private class CourseInfo {
        /**
         * index of the faculty who teaches this course
         */
        public int indexInFaculty = 0;
        /**
         * index of this course
         */
        public int globalIndex = 0;
    }

    /**
     * information of an RA instance
     */
    private class RaInfo {
        /**
         * index of this RA in the graduate students
         */
        public int indexInGradStud = 0;
    }

    /**
     * information of a TA instance
     */
    private class TaInfo {
        /**
         * index of this TA in the graduate students
         */
        public int indexInGradStud = 0;
        /**
         * index of the course which this TA assists
         */
        public int indexInCourse = 0; //local index in courses
    }

    /**
     * informaiton of a publication instance
     */
    private class PublicationInfo {
        /**
         * id
         */
        public String id;
        /**
         * name
         */
        public String name;
        /**
         * list of authors
         */
        public ArrayList authors;
    }

    /**
     * univ-bench ontology url
     */
    String ontology;
    /**
     * (class) instance information
     */
    private InstanceCount[] instances_;
    /**
     * property instance information
     */
    private PropertyCount[] properties_;
    /**
     * data file writer
     */
    private Writer writer_;
    /**
     * generate DAML+OIL data (instead of OWL)
     */
    private boolean isDaml_;
    /**
     * random number generator
     */
    private Random random_;
    /**
     * seed of the random number genertor for the current university
     */
    private long seed_ = 0l;
    /**
     * user specified seed for the data generation
     */
    private long baseSeed_ = 0l;
    /**
     * list of undergraduate courses generated so far (in the current department)
     */
    private ArrayList underCourses_;
    /**
     * list of graduate courses generated so far (in the current department)
     */
    private ArrayList gradCourses_;
    /**
     * list of remaining available undergraduate courses (in the current department)
     */
    private ArrayList remainingUnderCourses_;
    /**
     * list of remaining available graduate courses (in the current department)
     */
    private ArrayList remainingGradCourses_;
    /**
     * list of publication instances generated so far (in the current department)
     */
    private ArrayList publications_;
    /**
     * index of the full professor who has been chosen as the department chair
     */
    private int chair_;
    /**
     * starting index of the universities
     */
    private int startIndex_;
    /**
     * log writer
     */
    private PrintStream log_ = null;

    /**
     * main method
     */
    public static void main(String[] args) {
        //default values
        int univNum = 1, startIndex = 0, seed = 0;
        boolean daml = false;
        String ontology = null;

        try {
            String arg;
            int i = 0;
            while (i < args.length) {
                arg = args[i++];
                if (arg.equals("-univ")) {
                    if (i < args.length) {
                        arg = args[i++];
                        univNum = Integer.parseInt(arg);
                        if (univNum < 1)
                            throw new NumberFormatException();
                    } else
                        throw new NumberFormatException();
                } else if (arg.equals("-index")) {
                    if (i < args.length) {
                        arg = args[i++];
                        startIndex = Integer.parseInt(arg);
                        if (startIndex < 0)
                            throw new NumberFormatException();
                    } else
                        throw new NumberFormatException();
                } else if (arg.equals("-seed")) {
                    if (i < args.length) {
                        arg = args[i++];
                        seed = Integer.parseInt(arg);
                        if (seed < 0)
                            throw new NumberFormatException();
                    } else
                        throw new NumberFormatException();
                } else if (arg.equals("-daml")) {
                    daml = true;
                } else if (arg.equals("-onto")) {
                    if (i < args.length) {
                        arg = args[i++];
                        ontology = arg;
                    } else
                        throw new Exception();
                } else
                    throw new Exception();
            }
            if (((long) startIndex + univNum - 1) > Integer.MAX_VALUE) {
                System.err.println("Index overflow!");
                throw new Exception();
            }
            if (null == ontology) {
                System.err.println("ontology url is requested!");
                throw new Exception();
            }
        } catch (Exception e) {
            System.err.println("Usage: Generator\n" +
                    "\t[-univ <num of universities(1~" + Integer.MAX_VALUE +
                    ")>]\n" +
                    "\t[-index <start index(0~" + Integer.MAX_VALUE +
                    ")>]\n" +
                    "\t[-seed <seed(0~" + Integer.MAX_VALUE + ")>]\n" +
                    "\t[-daml]\n" +
                    "\t-onto <univ-bench ontology url>");
            System.exit(0);
        }

        new Generator().start(univNum, startIndex, seed, daml, ontology);
    }

    /**
     * constructor
     */
    public Generator() {
        instances_ = new InstanceCount[CLASS_NUM];
        for (int i = 0; i < CLASS_NUM; i++) {
            instances_[i] = new InstanceCount();
        }
        properties_ = new PropertyCount[PROP_NUM];
        for (int i = 0; i < PROP_NUM; i++) {
            properties_[i] = new PropertyCount();
        }

        random_ = new Random();
        underCourses_ = new ArrayList();
        gradCourses_ = new ArrayList();
        remainingUnderCourses_ = new ArrayList();
        remainingGradCourses_ = new ArrayList();
        publications_ = new ArrayList();
    }

    /**
     * Begins the data generation.
     *
     * @param univNum    Number of universities to generate.
     * @param startIndex Starting index of the universities.
     * @param seed       Seed for data generation.
     * @param daml       Generates DAML+OIL data if true, OWL data otherwise.
     * @param ontology   Ontology url.
     */
    public void start(int univNum, int startIndex, int seed, boolean daml,
                      String ontology) {
        this.ontology = ontology;

        isDaml_ = daml;
        writer_ = new OwlWriter(this);

        startIndex_ = startIndex;
        baseSeed_ = seed;
        instances_[CS_C_UNIV].num = univNum;
        instances_[CS_C_UNIV].count = startIndex;
        _generate();
        System.out.println("See log.txt for more details.");
    }

    ///////////////////////////////////////////////////////////////////////////
    //writer callbacks

    /**
     * Callback by the writer when it starts an instance section.
     *
     * @param classType Type of the instance.
     */
    void startSectionCB(int classType) {
        instances_[classType].logNum++;
        instances_[classType].logTotal++;
    }

    /**
     * Callback by the writer when it starts an instance section identified by an rdf:about attribute.
     *
     * @param classType Type of the instance.
     */
    void startAboutSectionCB(int classType) {
        startSectionCB(classType);
    }

    /**
     * Callback by the writer when it adds a property statement.
     *
     * @param property Type of the property.
     */
    void addPropertyCB(int property) {
        properties_[property].logNum++;
        properties_[property].logTotal++;
    }

    /**
     * Callback by the writer when it adds a property statement whose value is an individual.
     *
     * @param classType Type of the individual.
     */
    void addValueClassCB(int classType) {
        instances_[classType].logNum++;
        instances_[classType].logTotal++;
    }

    ///////////////////////////////////////////////////////////////////////////

    /**
     * Sets instance specification.
     */
    private void _setInstanceInfo() {
        int subClass, superClass;

        for (int i = 0; i < CLASS_NUM; i++) {
            switch (i) {
                case CS_C_UNIV:
                    break;
                case CS_C_DEPT:
                    break;
                case CS_C_FULLPROF:
                    instances_[i].num = _getRandomFromRange(FULLPROF_MIN, FULLPROF_MAX);
                    break;
                case CS_C_ASSOPROF:
                    instances_[i].num = _getRandomFromRange(ASSOPROF_MIN, ASSOPROF_MAX);
                    break;
                case CS_C_ASSTPROF:
                    instances_[i].num = _getRandomFromRange(ASSTPROF_MIN, ASSTPROF_MAX);
                    break;
                case CS_C_LECTURER:
                    instances_[i].num = _getRandomFromRange(LEC_MIN, LEC_MAX);
                    break;
                case CS_C_UNDERSTUD:
                    instances_[i].num = _getRandomFromRange(R_UNDERSTUD_FACULTY_MIN *
                            instances_[CS_C_FACULTY].total,
                            R_UNDERSTUD_FACULTY_MAX *
                                    instances_[CS_C_FACULTY].total);
                    break;
                case CS_C_GRADSTUD:
                    instances_[i].num = _getRandomFromRange(R_GRADSTUD_FACULTY_MIN *
                            instances_[CS_C_FACULTY].total,
                            R_GRADSTUD_FACULTY_MAX *
                                    instances_[CS_C_FACULTY].total);
                    break;
                case CS_C_TA:
                    instances_[i].num = _getRandomFromRange(instances_[CS_C_GRADSTUD].total /
                            R_GRADSTUD_TA_MAX,
                            instances_[CS_C_GRADSTUD].total /
                                    R_GRADSTUD_TA_MIN);
                    break;
                case CS_C_RA:
                    instances_[i].num = _getRandomFromRange(instances_[CS_C_GRADSTUD].total /
                            R_GRADSTUD_RA_MAX,
                            instances_[CS_C_GRADSTUD].total /
                                    R_GRADSTUD_RA_MIN);
                    break;
                case CS_C_RESEARCHGROUP:
                    instances_[i].num = _getRandomFromRange(RESEARCHGROUP_MIN, RESEARCHGROUP_MAX);
                    break;
                default:
                    instances_[i].num = CLASS_INFO[i][INDEX_NUM];
                    break;
            }
            instances_[i].total = instances_[i].num;
            subClass = i;
            while ((superClass = CLASS_INFO[subClass][INDEX_SUPER]) != CS_C_NULL) {
                instances_[superClass].total += instances_[i].num;
                subClass = superClass;
            }
        }
    }

    /**
     * Begins data generation according to the specification
     */
    private void _generate() {
//        System.out.println("Started...");
        try {
            log_ = new PrintStream(new FileOutputStream(System.getProperty("user.dir") +
                    "\\" + LOG_FILE));
            writer_.start();
            for (int i = 0; i < instances_[CS_C_UNIV].num; i++) {
                _generateUniv(i + startIndex_);
            }
            writer_.end();
            log_.close();
        } catch (IOException e) {
//            System.out.println("Failed to create log file!");
        }
//        System.out.println("Completed!");
    }

    /**
     * Creates a university.
     *
     * @param index Index of the university.
     */
    private void _generateUniv(int index) {
        //this transformation guarantees no different pairs of (index, baseSeed) generate the same data
        seed_ = baseSeed_ * (Integer.MAX_VALUE + 1) + index;
        random_.setSeed(seed_);

        //determine department number
        instances_[CS_C_DEPT].num = _getRandomFromRange(DEPT_MIN, DEPT_MAX);
        instances_[CS_C_DEPT].count = 0;
        //generate departments
        for (int i = 0; i < instances_[CS_C_DEPT].num; i++) {
            _generateDept(index, i);
        }
    }

    public static String joinPath(String path1, String path2) {
        File file1 = new File(path1);
        File file2 = new File(file1, path2);
        return file2.getPath();
    }

    /**
     * Creates a department.
     *
     * @param univIndex Index of the current university.
     * @param index     Index of the department.
     *                  NOTE: Use univIndex instead of instances[CS_C_UNIV].count till generateASection(CS_C_UNIV, ) is invoked.
     */
    private void _generateDept(int univIndex, int index) {
        String fileName = joinPath(System.getProperty("user.dir"),
                _getName(CS_C_UNIV, univIndex) + INDEX_DELIMITER + index + _getFileSuffix());
        writer_.startFile(fileName);

        //reset
        _setInstanceInfo();
        underCourses_.clear();
        gradCourses_.clear();
        remainingUnderCourses_.clear();
        remainingGradCourses_.clear();
        for (int i = 0; i < UNDER_COURSE_NUM; i++) {
            remainingUnderCourses_.add(new Integer(i));
        }
        for (int i = 0; i < GRAD_COURSE_NUM; i++) {
            remainingGradCourses_.add(new Integer(i));
        }
        publications_.clear();
        for (int i = 0; i < CLASS_NUM; i++) {
            instances_[i].logNum = 0;
        }
        for (int i = 0; i < PROP_NUM; i++) {
            properties_[i].logNum = 0;
        }

        //decide the chair
        chair_ = random_.nextInt(instances_[CS_C_FULLPROF].total);

        if (index == 0) {
            _generateASection(CS_C_UNIV, univIndex);
        }
        _generateASection(CS_C_DEPT, index);
        for (int i = CS_C_DEPT + 1; i < CLASS_NUM; i++) {
            instances_[i].count = 0;
            for (int j = 0; j < instances_[i].num; j++) {
                _generateASection(i, j);
            }
        }

        _generatePublications();
        _generateCourses();
        _generateRaTa();

//        System.out.println(fileName + " generated");
        String bar = "";
        for (int i = 0; i < fileName.length(); i++)
            bar += '-';
        log_.println(bar);
        log_.println(fileName);
        log_.println(bar);
        _generateComments();
        writer_.endFile();
    }

    ///////////////////////////////////////////////////////////////////////////
    //instance generation

    /**
     * Generates an instance of the specified class
     *
     * @param classType Type of the instance.
     * @param index     Index of the instance.
     */
    private void _generateASection(int classType, int index) {
        _updateCount(classType);

        switch (classType) {
            case CS_C_UNIV:
                _generateAUniv(index);
                break;
            case CS_C_DEPT:
                _generateADept(index);
                break;
            case CS_C_FACULTY:
                _generateAFaculty(index);
                break;
            case CS_C_PROF:
                _generateAProf(index);
                break;
            case CS_C_FULLPROF:
                _generateAFullProf(index);
                break;
            case CS_C_ASSOPROF:
                _generateAnAssociateProfessor(index);
                break;
            case CS_C_ASSTPROF:
                _generateAnAssistantProfessor(index);
                break;
            case CS_C_LECTURER:
                _generateALecturer(index);
                break;
            case CS_C_UNDERSTUD:
                _generateAnUndergraduateStudent(index);
                break;
            case CS_C_GRADSTUD:
                _generateAGradudateStudent(index);
                break;
            case CS_C_COURSE:
                _generateACourse(index);
                break;
            case CS_C_GRADCOURSE:
                _generateAGraduateCourse(index);
                break;
            case CS_C_RESEARCHGROUP:
                _generateAResearchGroup(index);
                break;
            default:
                break;
        }
    }

    /**
     * Generates a university instance.
     *
     * @param index Index of the instance.
     */
    private void _generateAUniv(int index) {
        writer_.startSection(CS_C_UNIV, _getId(CS_C_UNIV, index));
        writer_.addProperty(CS_P_NAME, _getRelativeName(CS_C_UNIV, index), false);
        writer_.endSection(CS_C_UNIV);
    }

    /**
     * Generates a department instance.
     *
     * @param index Index of the department.
     */
    private void _generateADept(int index) {
        writer_.startSection(CS_C_DEPT, _getId(CS_C_DEPT, index));
        writer_.addProperty(CS_P_NAME, _getRelativeName(CS_C_DEPT, index), false);
        writer_.addProperty(CS_P_SUBORGANIZATIONOF, CS_C_UNIV,
                _getId(CS_C_UNIV, instances_[CS_C_UNIV].count - 1));
        writer_.endSection(CS_C_DEPT);
    }

    /**
     * Generates a faculty instance.
     *
     * @param index Index of the faculty.
     */
    private void _generateAFaculty(int index) {
        writer_.startSection(CS_C_FACULTY, _getId(CS_C_FACULTY, index));
        _generateAFaculty_a(CS_C_FACULTY, index);
        writer_.endSection(CS_C_FACULTY);
    }

    /**
     * Generates properties for the specified faculty instance.
     *
     * @param type  Type of the faculty.
     * @param index Index of the instance within its type.
     */
    private void _generateAFaculty_a(int type, int index) {
        int indexInFaculty;
        int courseNum;
        int courseIndex;
        boolean dup;
        CourseInfo course;

        indexInFaculty = instances_[CS_C_FACULTY].count - 1;

        writer_.addProperty(CS_P_NAME, _getRelativeName(type, index), false);

        //undergradutate courses
        courseNum = _getRandomFromRange(FACULTY_COURSE_MIN, FACULTY_COURSE_MAX);
        for (int i = 0; i < courseNum; i++) {
            courseIndex = _AssignCourse(indexInFaculty);
            writer_.addProperty(CS_P_TEACHEROF, _getId(CS_C_COURSE, courseIndex), true);
        }
        //gradutate courses
        courseNum = _getRandomFromRange(FACULTY_GRADCOURSE_MIN, FACULTY_GRADCOURSE_MAX);
        for (int i = 0; i < courseNum; i++) {
            courseIndex = _AssignGraduateCourse(indexInFaculty);
            writer_.addProperty(CS_P_TEACHEROF, _getId(CS_C_GRADCOURSE, courseIndex), true);
        }
        //person properties
        writer_.addProperty(CS_P_UNDERGRADFROM, CS_C_UNIV,
                _getId(CS_C_UNIV, random_.nextInt(UNIV_NUM)));
        writer_.addProperty(CS_P_GRADFROM, CS_C_UNIV,
                _getId(CS_C_UNIV, random_.nextInt(UNIV_NUM)));
        writer_.addProperty(CS_P_DOCFROM, CS_C_UNIV,
                _getId(CS_C_UNIV, random_.nextInt(UNIV_NUM)));
        writer_.addProperty(CS_P_WORKSFOR,
                _getId(CS_C_DEPT, instances_[CS_C_DEPT].count - 1), true);
        writer_.addProperty(CS_P_EMAIL, _getEmail(type, index), false);
        writer_.addProperty(CS_P_TELEPHONE, "xxx-xxx-xxxx", false);
    }

    /**
     * Assigns an undergraduate course to the specified faculty.
     *
     * @param indexInFaculty Index of the faculty.
     * @return Index of the selected course in the pool.
     */
    private int _AssignCourse(int indexInFaculty) {
        //NOTE: this line, although overriden by the next one, is deliberately kept
        // to guarantee identical random number generation to the previous version.
        int pos = _getRandomFromRange(0, remainingUnderCourses_.size() - 1);
        pos = 0; //fetch courses in sequence

        CourseInfo course = new CourseInfo();
        course.indexInFaculty = indexInFaculty;
        course.globalIndex = ((Integer) remainingUnderCourses_.get(pos)).intValue();
        underCourses_.add(course);

        remainingUnderCourses_.remove(pos);

        return course.globalIndex;
    }

    /**
     * Assigns a graduate course to the specified faculty.
     *
     * @param indexInFaculty Index of the faculty.
     * @return Index of the selected course in the pool.
     */
    private int _AssignGraduateCourse(int indexInFaculty) {
        //NOTE: this line, although overriden by the next one, is deliberately kept
        // to guarantee identical random number generation to the previous version.
        int pos = _getRandomFromRange(0, remainingGradCourses_.size() - 1);
        pos = 0; //fetch courses in sequence

        CourseInfo course = new CourseInfo();
        course.indexInFaculty = indexInFaculty;
        course.globalIndex = ((Integer) remainingGradCourses_.get(pos)).intValue();
        gradCourses_.add(course);

        remainingGradCourses_.remove(pos);

        return course.globalIndex;
    }

    /**
     * Generates a professor instance.
     *
     * @param index Index of the professor.
     */
    private void _generateAProf(int index) {
        writer_.startSection(CS_C_PROF, _getId(CS_C_PROF, index));
        _generateAProf_a(CS_C_PROF, index);
        writer_.endSection(CS_C_PROF);
    }

    /**
     * Generates properties for a professor instance.
     *
     * @param type  Type of the professor.
     * @param index Index of the intance within its type.
     */
    private void _generateAProf_a(int type, int index) {
        _generateAFaculty_a(type, index);
        writer_.addProperty(CS_P_RESEARCHINTEREST,
                _getRelativeName(CS_C_RESEARCH,
                        random_.nextInt(RESEARCH_NUM)), false);
    }

    /**
     * Generates a full professor instances.
     *
     * @param index Index of the full professor.
     */
    private void _generateAFullProf(int index) {
        String id;

        id = _getId(CS_C_FULLPROF, index);
        writer_.startSection(CS_C_FULLPROF, id);
        _generateAProf_a(CS_C_FULLPROF, index);
        if (index == chair_) {
            writer_.addProperty(CS_P_HEADOF,
                    _getId(CS_C_DEPT, instances_[CS_C_DEPT].count - 1), true);
        }
        writer_.endSection(CS_C_FULLPROF);
        _assignFacultyPublications(id, FULLPROF_PUB_MIN, FULLPROF_PUB_MAX);
    }

    /**
     * Generates an associate professor instance.
     *
     * @param index Index of the associate professor.
     */
    private void _generateAnAssociateProfessor(int index) {
        String id = _getId(CS_C_ASSOPROF, index);
        writer_.startSection(CS_C_ASSOPROF, id);
        _generateAProf_a(CS_C_ASSOPROF, index);
        writer_.endSection(CS_C_ASSOPROF);
        _assignFacultyPublications(id, ASSOPROF_PUB_MIN, ASSOPROF_PUB_MAX);
    }

    /**
     * Generates an assistant professor instance.
     *
     * @param index Index of the assistant professor.
     */
    private void _generateAnAssistantProfessor(int index) {
        String id = _getId(CS_C_ASSTPROF, index);
        writer_.startSection(CS_C_ASSTPROF, id);
        _generateAProf_a(CS_C_ASSTPROF, index);
        writer_.endSection(CS_C_ASSTPROF);
        _assignFacultyPublications(id, ASSTPROF_PUB_MIN, ASSTPROF_PUB_MAX);
    }

    /**
     * Generates a lecturer instance.
     *
     * @param index Index of the lecturer.
     */
    private void _generateALecturer(int index) {
        String id = _getId(CS_C_LECTURER, index);
        writer_.startSection(CS_C_LECTURER, id);
        _generateAFaculty_a(CS_C_LECTURER, index);
        writer_.endSection(CS_C_LECTURER);
        _assignFacultyPublications(id, LEC_PUB_MIN, LEC_PUB_MAX);
    }

    /**
     * Assigns publications to the specified faculty.
     *
     * @param author Id of the faculty
     * @param min    Minimum number of publications
     * @param max    Maximum number of publications
     */
    private void _assignFacultyPublications(String author, int min, int max) {
        int num;
        PublicationInfo publication;

        num = _getRandomFromRange(min, max);
        for (int i = 0; i < num; i++) {
            publication = new PublicationInfo();
            publication.id = _getId(CS_C_PUBLICATION, i, author);
            publication.name = _getRelativeName(CS_C_PUBLICATION, i);
            publication.authors = new ArrayList();
            publication.authors.add(author);
            publications_.add(publication);
        }
    }

    /**
     * Assigns publications to the specified graduate student. The publications are
     * chosen from some faculties'.
     *
     * @param author Id of the graduate student.
     * @param min    Minimum number of publications.
     * @param max    Maximum number of publications.
     */
    private void _assignGraduateStudentPublications(String author, int min, int max) {
        int num;
        PublicationInfo publication;

        num = _getRandomFromRange(min, max);
        ArrayList list = _getRandomList(num, 0, publications_.size() - 1);
        for (int i = 0; i < list.size(); i++) {
            publication = (PublicationInfo) publications_.get(((Integer) list.get(i)).
                    intValue());
            publication.authors.add(author);
        }
    }

    /**
     * Generates publication instances. These publications are assigned to some faculties
     * and graduate students before.
     */
    private void _generatePublications() {
        for (int i = 0; i < publications_.size(); i++) {
            _generateAPublication((PublicationInfo) publications_.get(i));
        }
    }

    /**
     * Generates a publication instance.
     *
     * @param publication Information of the publication.
     */
    private void _generateAPublication(PublicationInfo publication) {
        writer_.startSection(CS_C_PUBLICATION, publication.id);
        writer_.addProperty(CS_P_NAME, publication.name, false);
        for (int i = 0; i < publication.authors.size(); i++) {
            writer_.addProperty(CS_P_PUBLICATIONAUTHOR,
                    (String) publication.authors.get(i), true);
        }
        writer_.endSection(CS_C_PUBLICATION);
    }

    /**
     * Generates properties for the specified student instance.
     *
     * @param type  Type of the student.
     * @param index Index of the instance within its type.
     */
    private void _generateAStudent_a(int type, int index) {
        writer_.addProperty(CS_P_NAME, _getRelativeName(type, index), false);
        writer_.addProperty(CS_P_MEMBEROF,
                _getId(CS_C_DEPT, instances_[CS_C_DEPT].count - 1), true);
        writer_.addProperty(CS_P_EMAIL, _getEmail(type, index), false);
        writer_.addProperty(CS_P_TELEPHONE, "xxx-xxx-xxxx", false);
    }

    /**
     * Generates an undergraduate student instance.
     *
     * @param index Index of the undergraduate student.
     */
    private void _generateAnUndergraduateStudent(int index) {
        int n;
        ArrayList list;

        writer_.startSection(CS_C_UNDERSTUD, _getId(CS_C_UNDERSTUD, index));
        _generateAStudent_a(CS_C_UNDERSTUD, index);
        n = _getRandomFromRange(UNDERSTUD_COURSE_MIN, UNDERSTUD_COURSE_MAX);
        list = _getRandomList(n, 0, underCourses_.size() - 1);
        for (int i = 0; i < list.size(); i++) {
            CourseInfo info = (CourseInfo) underCourses_.get(((Integer) list.get(i)).
                    intValue());
            writer_.addProperty(CS_P_TAKECOURSE, _getId(CS_C_COURSE, info.globalIndex), true);
        }
        if (0 == random_.nextInt(R_UNDERSTUD_ADVISOR)) {
            writer_.addProperty(CS_P_ADVISOR, _selectAdvisor(), true);
        }
        writer_.endSection(CS_C_UNDERSTUD);
    }

    /**
     * Generates a graduate student instance.
     *
     * @param index Index of the graduate student.
     */
    private void _generateAGradudateStudent(int index) {
        int n;
        ArrayList list;
        String id;

        id = _getId(CS_C_GRADSTUD, index);
        writer_.startSection(CS_C_GRADSTUD, id);
        _generateAStudent_a(CS_C_GRADSTUD, index);
        n = _getRandomFromRange(GRADSTUD_COURSE_MIN, GRADSTUD_COURSE_MAX);
        list = _getRandomList(n, 0, gradCourses_.size() - 1);
        for (int i = 0; i < list.size(); i++) {
            CourseInfo info = (CourseInfo) gradCourses_.get(((Integer) list.get(i)).
                    intValue());
            writer_.addProperty(CS_P_TAKECOURSE,
                    _getId(CS_C_GRADCOURSE, info.globalIndex), true);
        }
        writer_.addProperty(CS_P_UNDERGRADFROM, CS_C_UNIV,
                _getId(CS_C_UNIV, random_.nextInt(UNIV_NUM)));
        if (0 == random_.nextInt(R_GRADSTUD_ADVISOR)) {
            writer_.addProperty(CS_P_ADVISOR, _selectAdvisor(), true);
        }
        _assignGraduateStudentPublications(id, GRADSTUD_PUB_MIN, GRADSTUD_PUB_MAX);
        writer_.endSection(CS_C_GRADSTUD);
    }

    /**
     * Select an advisor from the professors.
     *
     * @return Id of the selected professor.
     */
    private String _selectAdvisor() {
        int profType;
        int index;

        profType = _getRandomFromRange(CS_C_FULLPROF, CS_C_ASSTPROF);
        index = random_.nextInt(instances_[profType].total);
        return _getId(profType, index);
    }

    /**
     * Generates a TA instance according to the specified information.
     *
     * @param ta Information of the TA.
     */
    private void _generateATa(TaInfo ta) {
        writer_.startAboutSection(CS_C_TA, _getId(CS_C_GRADSTUD, ta.indexInGradStud));
        writer_.addProperty(CS_P_TAOF, _getId(CS_C_COURSE, ta.indexInCourse), true);
        writer_.endSection(CS_C_TA);
    }

    /**
     * Generates an RA instance according to the specified information.
     *
     * @param ra Information of the RA.
     */
    private void _generateAnRa(RaInfo ra) {
        writer_.startAboutSection(CS_C_RA, _getId(CS_C_GRADSTUD, ra.indexInGradStud));
        writer_.endSection(CS_C_RA);
    }

    /**
     * Generates a course instance.
     *
     * @param index Index of the course.
     */
    private void _generateACourse(int index) {
        writer_.startSection(CS_C_COURSE, _getId(CS_C_COURSE, index));
        writer_.addProperty(CS_P_NAME,
                _getRelativeName(CS_C_COURSE, index), false);
        writer_.endSection(CS_C_COURSE);
    }

    /**
     * Generates a graduate course instance.
     *
     * @param index Index of the graduate course.
     */
    private void _generateAGraduateCourse(int index) {
        writer_.startSection(CS_C_GRADCOURSE, _getId(CS_C_GRADCOURSE, index));
        writer_.addProperty(CS_P_NAME,
                _getRelativeName(CS_C_GRADCOURSE, index), false);
        writer_.endSection(CS_C_GRADCOURSE);
    }

    /**
     * Generates course/graduate course instances. These course are assigned to some
     * faculties before.
     */
    private void _generateCourses() {
        for (int i = 0; i < underCourses_.size(); i++) {
            _generateACourse(((CourseInfo) underCourses_.get(i)).globalIndex);
        }
        for (int i = 0; i < gradCourses_.size(); i++) {
            _generateAGraduateCourse(((CourseInfo) gradCourses_.get(i)).globalIndex);
        }
    }

    /**
     * Chooses RAs and TAs from graduate student and generates their instances accordingly.
     */
    private void _generateRaTa() {
        ArrayList list, courseList;
        TaInfo ta;
        RaInfo ra;
        ArrayList tas, ras;
        int i;

        tas = new ArrayList();
        ras = new ArrayList();
        list = _getRandomList(instances_[CS_C_TA].total + instances_[CS_C_RA].total,
                0, instances_[CS_C_GRADSTUD].total - 1);
        courseList = _getRandomList(instances_[CS_C_TA].total, 0,
                underCourses_.size() - 1);

        for (i = 0; i < instances_[CS_C_TA].total; i++) {
            ta = new TaInfo();
            ta.indexInGradStud = ((Integer) list.get(i)).intValue();
            ta.indexInCourse = ((CourseInfo) underCourses_.get(((Integer)
                    courseList.get(i)).intValue())).globalIndex;
            _generateATa(ta);
        }
        while (i < list.size()) {
            ra = new RaInfo();
            ra.indexInGradStud = ((Integer) list.get(i)).intValue();
            _generateAnRa(ra);
            i++;
        }
    }

    /**
     * Generates a research group instance.
     *
     * @param index Index of the research group.
     */
    private void _generateAResearchGroup(int index) {
        String id;
        id = _getId(CS_C_RESEARCHGROUP, index);
        writer_.startSection(CS_C_RESEARCHGROUP, id);
        writer_.addProperty(CS_P_SUBORGANIZATIONOF,
                _getId(CS_C_DEPT, instances_[CS_C_DEPT].count - 1), true);
        writer_.endSection(CS_C_RESEARCHGROUP);
    }

    ///////////////////////////////////////////////////////////////////////////

    /**
     * @return Suffix of the data file.
     */
    private String _getFileSuffix() {
        return isDaml_ ? ".daml" : ".owl";
    }

    /**
     * Gets the id of the specified instance.
     *
     * @param classType Type of the instance.
     * @param index     Index of the instance within its type.
     * @return Id of the instance.
     */
    private String _getId(int classType, int index) {
        String id;

        switch (classType) {
            case CS_C_UNIV:
                id = "http://www." + _getRelativeName(classType, index) + ".edu";
                break;
            case CS_C_DEPT:
                id = "http://www." + _getRelativeName(classType, index) + "." +
                        _getRelativeName(CS_C_UNIV, instances_[CS_C_UNIV].count - 1) +
                        ".edu";
                break;
            default:
                id = _getId(CS_C_DEPT, instances_[CS_C_DEPT].count - 1) + ID_DELIMITER +
                        _getRelativeName(classType, index);
                break;
        }

        return id;
    }

    /**
     * Gets the id of the specified instance.
     *
     * @param classType Type of the instance.
     * @param index     Index of the instance within its type.
     * @param param     Auxiliary parameter.
     * @return Id of the instance.
     */
    private String _getId(int classType, int index, String param) {
        String id;

        switch (classType) {
            case CS_C_PUBLICATION:
                //NOTE: param is author id
                id = param + ID_DELIMITER + CLASS_TOKEN[classType] + index;
                break;
            default:
                id = _getId(classType, index);
                break;
        }

        return id;
    }

    /**
     * Gets the globally unique name of the specified instance.
     *
     * @param classType Type of the instance.
     * @param index     Index of the instance within its type.
     * @return Name of the instance.
     */
    private String _getName(int classType, int index) {
        String name;

        switch (classType) {
            case CS_C_UNIV:
                name = _getRelativeName(classType, index);
                break;
            case CS_C_DEPT:
                name = _getRelativeName(classType, index) + INDEX_DELIMITER +
                        (instances_[CS_C_UNIV].count - 1);
                break;
            //NOTE: Assume departments with the same index share the same pool of courses and researches
            case CS_C_COURSE:
            case CS_C_GRADCOURSE:
            case CS_C_RESEARCH:
                name = _getRelativeName(classType, index) + INDEX_DELIMITER +
                        (instances_[CS_C_DEPT].count - 1);
                break;
            default:
                name = _getRelativeName(classType, index) + INDEX_DELIMITER +
                        (instances_[CS_C_DEPT].count - 1) + INDEX_DELIMITER +
                        (instances_[CS_C_UNIV].count - 1);
                break;
        }

        return name;
    }

    /**
     * Gets the name of the specified instance that is unique within a department.
     *
     * @param classType Type of the instance.
     * @param index     Index of the instance within its type.
     * @return Name of the instance.
     */
    private String _getRelativeName(int classType, int index) {
        String name;

        switch (classType) {
            case CS_C_UNIV:
                //should be unique too!
                name = CLASS_TOKEN[classType] + index;
                break;
            case CS_C_DEPT:
                name = CLASS_TOKEN[classType] + index;
                break;
            default:
                name = CLASS_TOKEN[classType] + index;
                break;
        }

        return name;
    }

    /**
     * Gets the email address of the specified instance.
     *
     * @param classType Type of the instance.
     * @param index     Index of the instance within its type.
     * @return The email address of the instance.
     */
    private String _getEmail(int classType, int index) {
        String email = "";

        switch (classType) {
            case CS_C_UNIV:
                email += _getRelativeName(classType, index) + "@" +
                        _getRelativeName(classType, index) + ".edu";
                break;
            case CS_C_DEPT:
                email += _getRelativeName(classType, index) + "@" +
                        _getRelativeName(classType, index) + "." +
                        _getRelativeName(CS_C_UNIV, instances_[CS_C_UNIV].count - 1) + ".edu";
                break;
            default:
                email += _getRelativeName(classType, index) + "@" +
                        _getRelativeName(CS_C_DEPT, instances_[CS_C_DEPT].count - 1) +
                        "." + _getRelativeName(CS_C_UNIV, instances_[CS_C_UNIV].count - 1) +
                        ".edu";
                break;
        }

        return email;
    }

    /**
     * Increases by 1 the instance count of the specified class. This also includes
     * the increase of the instacne count of all its super class.
     *
     * @param classType Type of the instance.
     */
    private void _updateCount(int classType) {
        int subClass, superClass;

        instances_[classType].count++;
        subClass = classType;
        while ((superClass = CLASS_INFO[subClass][INDEX_SUPER]) != CS_C_NULL) {
            instances_[superClass].count++;
            subClass = superClass;
        }
    }

    /**
     * Creates a list of the specified number of integers without duplication which
     * are randomly selected from the specified range.
     *
     * @param num Number of the integers.
     * @param min Minimum value of selectable integer.
     * @param max Maximum value of selectable integer.
     * @return So generated list of integers.
     */
    private ArrayList _getRandomList(int num, int min, int max) {
        ArrayList list = new ArrayList();
        ArrayList tmp = new ArrayList();
        for (int i = min; i <= max; i++) {
            tmp.add(new Integer(i));
        }

        for (int i = 0; i < num; i++) {
            int pos = _getRandomFromRange(0, tmp.size() - 1);
            list.add((Integer) tmp.get(pos));
            tmp.remove(pos);
        }

        return list;
    }

    /**
     * Randomly selects a integer from the specified range.
     *
     * @param min Minimum value of the selectable integer.
     * @param max Maximum value of the selectable integer.
     * @return The selected integer.
     */
    private int _getRandomFromRange(int min, int max) {
        return min + random_.nextInt(max - min + 1);
    }

    /**
     * Outputs log information to both the log file and the screen after a department
     * is generated.
     */
    private void _generateComments() {
        int classInstNum = 0; //total class instance num in this department
        long totalClassInstNum = 0l; //total class instance num so far
        int propInstNum = 0; //total property instance num in this department
        long totalPropInstNum = 0l; //total property instance num so far
        String comment;

        comment = "External Seed=" + baseSeed_ + " Interal Seed=" + seed_;
        log_.println(comment);
        log_.println();

        comment = "CLASS INSTANCE# TOTAL-SO-FAR";
        log_.println(comment);
        comment = "----------------------------";
        log_.println(comment);
        for (int i = 0; i < CLASS_NUM; i++) {
            comment = CLASS_TOKEN[i] + " " + instances_[i].logNum + " " +
                    instances_[i].logTotal;
            log_.println(comment);
            classInstNum += instances_[i].logNum;
            totalClassInstNum += instances_[i].logTotal;
        }
        log_.println();
        comment = "TOTAL: " + classInstNum;
        log_.println(comment);
        comment = "TOTAL SO FAR: " + totalClassInstNum;
        log_.println(comment);

        comment = "PROPERTY---INSTANCE NUM";
        log_.println();
        comment = "PROPERTY INSTANCE# TOTAL-SO-FAR";
        log_.println(comment);
        comment = "-------------------------------";
        log_.println(comment);
        for (int i = 0; i < PROP_NUM; i++) {
            comment = PROP_TOKEN[i] + " " + properties_[i].logNum;
            comment = comment + " " + properties_[i].logTotal;
            log_.println(comment);
            propInstNum += properties_[i].logNum;
            totalPropInstNum += properties_[i].logTotal;
        }
        log_.println();
        comment = "TOTAL: " + propInstNum;
        log_.println(comment);
        comment = "TOTAL SO FAR: " + totalPropInstNum;
        log_.println(comment);

//        System.out.println("CLASS INSTANCE #: " + classInstNum + ", TOTAL SO FAR: " +
//                totalClassInstNum);
//        System.out.println("PROPERTY INSTANCE #: " + propInstNum +
//                ", TOTAL SO FAR: " + totalPropInstNum);
//        System.out.println();

        log_.println();
    }

}
