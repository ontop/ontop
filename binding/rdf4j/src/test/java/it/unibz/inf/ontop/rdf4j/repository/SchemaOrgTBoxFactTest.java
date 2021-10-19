package it.unibz.inf.ontop.rdf4j.repository;

import com.google.common.collect.ImmutableSet;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

public class SchemaOrgTBoxFactTest extends AbstractRDF4JTest {

    private static final String CREATE_DB_FILE = "/tbox-facts/schemaorg.sql";
    private static final String OBDA_FILE = "/tbox-facts/schemaorg.obda";
    private static final String OWL_FILE = "/tbox-facts/schemaorg.owl";
    private static final String PROPERTIES_FILE = "/tbox-facts/factextraction.properties";

    @BeforeClass
    public static void before() throws IOException, SQLException {
        initOBDA(CREATE_DB_FILE, OBDA_FILE, OWL_FILE, PROPERTIES_FILE);
    }

    @AfterClass
    public static void after() throws SQLException {
        release();
    }

    @Test
    public void testSubClasses() {
        String query = "PREFIX schema: <https://schema.org/>" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "SELECT  ?v\n" +
                "WHERE {\n" +
                "?v rdfs:subClassOf schema:Clip .\n" +
                "}\n";

        ImmutableSet<String> results = ImmutableSet.of("https://schema.org/Clip",
                "https://schema.org/MovieClip",
                "https://schema.org/RadioClip",
                "https://schema.org/TVClip",
                "https://schema.org/VideoGameClip");
        runQueryAndCompare(query, results);
    }

    @Test
    public void testRDFSClasses() {
        String query = "SELECT  *\n" +
                "WHERE {\n" +
                "   ?v a rdfs:Class .\n" +
                "}\n";

        runQueryAndCompare(query, getExpectedClasses());
    }

    @Test
    public void testOWLClasses() {
        String query = "SELECT  *\n" +
                "WHERE {\n" +
                "   ?v a owl:Class .\n" +
                "}\n";

        runQueryAndCompare(query, getExpectedClasses());
    }

    /**
     * Cannot retrieve any domain info from file format
     */
    @Ignore
    @Test
    public void testRDFSDomainRelation() {
        String query = "SELECT  ?v\n" +
                "WHERE {\n" +
                "   ?v rdfs:domain ?z .\n" +
                "}\n";

        ImmutableSet<String> results = ImmutableSet.of("");
        runQueryAndCompare(query, results);
    }

    /**
     * Cannot retrieve any domain info from file format
     */
    @Ignore
    @Test
    public void testRDFSDomain() {
        String query = "PREFIX schema: <https://schema.org/>" +
                "SELECT  ?v\n" +
                "WHERE {\n" +
                "   ?z rdfs:domain ?v .\n" +
                "}\n";

        ImmutableSet<String> results = ImmutableSet.of("");
        runQueryAndCompare(query, results);
    }

    @Test
    public void testOWLInverseOf() {
        String query = "PREFIX schema: <https://schema.org/>" +
                "SELECT  ?v\n" +
                "WHERE {\n" +
                "   ?z owl:inverseOf ?v .\n" +
                "}\n";

        ImmutableSet<String> results = ImmutableSet.of("https://schema.org/recordedAt",
                "https://schema.org/about", "https://schema.org/alumni", "https://schema.org/isPartOf",
                "https://schema.org/member", "https://schema.org/albumRelease", "https://schema.org/subOrganization",
                "https://schema.org/makesOffer", "https://schema.org/hasPart", "https://schema.org/parentOrganization",
                "https://schema.org/providesBroadcastService", "https://schema.org/exampleOfWork",
                "https://schema.org/archiveHeld", "https://schema.org/subTrip", "https://schema.org/offers",
                "https://schema.org/alumniOf", "https://schema.org/parentTaxon", "https://schema.org/itemOffered",
                "https://schema.org/childTaxon", "https://schema.org/subEvent", "https://schema.org/releaseOf",
                "https://schema.org/subjectOf", "https://schema.org/mainEntityOfPage",
                "https://schema.org/holdingArchive", "https://schema.org/dataset", "https://schema.org/partOfTrip",
                "https://schema.org/isPartOfBioChemEntity", "https://schema.org/game",
                "https://schema.org/encodesCreativeWork", "https://schema.org/encoding", "https://schema.org/offeredBy",
                "https://schema.org/containedInPlace", "https://schema.org/hasVariant", "https://schema.org/recordingOf",
                "https://schema.org/hasBioChemEntityPart", "https://schema.org/superEvent",
                "https://schema.org/containsPlace", "https://schema.org/mainEntity", "https://schema.org/recordedAs",
                "https://schema.org/memberOf", "https://schema.org/encodesBioChemEntity",
                "https://schema.org/gameServer", "https://schema.org/hasBroadcastChannel",
                "https://schema.org/isVariantOf", "https://schema.org/translationOfWork",
                "https://schema.org/isEncodedByBioChemEntity", "https://schema.org/workExample",
                "https://schema.org/recordedIn", "https://schema.org/workTranslation",
                "https://schema.org/includedInDataCatalog");
        runQueryAndCompare(query, results);
    }

    private ImmutableSet<String> getExpectedClasses() {
        return ImmutableSet.of("https://schema.org/Thing", "https://schema.org/Intangible", "https://schema.org/Audience",
                "https://schema.org/PeopleAudience", "https://schema.org/Action", "https://schema.org/ConsumeAction", "https://schema.org/UseAction",
                "https://schema.org/WearAction", "http://www.w3.org/2000/01/rdf-schema#Class", "https://schema.org/DataType", "https://schema.org/Text",
                "https://schema.org/PronounceableText", "https://schema.org/Enumeration", "https://schema.org/StatusEnumeration",
                "https://schema.org/ReservationStatusType", "https://schema.org/Place", "https://schema.org/Organization",
                "https://schema.org/LocalBusiness", "https://schema.org/LegalService", "https://schema.org/Notary", "https://schema.org/ListItem",
                "https://schema.org/CreativeWork", "https://schema.org/ItemList", "https://schema.org/HowToSection", "https://schema.org/Dataset",
                "https://schema.org/SportsActivityLocation", "https://schema.org/PublicSwimmingPool", "https://schema.org/CivicStructure",
                "https://schema.org/SubwayStation", "https://schema.org/Number", "https://schema.org/EntertainmentBusiness",
                "https://schema.org/ComedyClub", "https://schema.org/Store", "https://schema.org/LiquorStore", "https://schema.org/FundingScheme",
                "https://schema.org/InteractAction", "https://schema.org/MarryAction", "https://schema.org/DefinedTerm",
                "https://schema.org/CategoryCode", "https://schema.org/Rating", "https://schema.org/AggregateRating",
                "https://schema.org/EmployerAggregateRating", "https://schema.org/GovernmentBuilding", "https://schema.org/LegislativeBuilding",
                "https://schema.org/MedicalEnumeration", "https://schema.org/MedicalProcedureType", "https://schema.org/LodgingBusiness",
                "https://schema.org/BedAndBreakfast", "https://schema.org/DrinkAction", "https://schema.org/StructuredValue",
                "https://schema.org/PriceSpecification", "https://schema.org/PaymentChargeSpecification", "https://schema.org/MedicalEntity",
                "https://schema.org/AnatomicalStructure", "https://schema.org/Joint", "https://schema.org/AdministrativeArea",
                "https://schema.org/Country", "https://schema.org/Episode", "https://schema.org/PodcastEpisode", "https://schema.org/DryCleaningOrLaundry",
                "https://schema.org/Project", "https://schema.org/ParkingFacility", "https://schema.org/Class", "https://schema.org/Article",
                "https://schema.org/NewsArticle", "https://schema.org/AnalysisNewsArticle", "https://schema.org/CreativeWorkSeason",
                "https://schema.org/PodcastSeason", "https://schema.org/PlaceOfWorship", "https://schema.org/Poster", "https://schema.org/Accommodation",
                "https://schema.org/Room", "https://schema.org/HotelRoom", "https://schema.org/Demand", "https://schema.org/CommunicateAction",
                "https://schema.org/InformAction", "https://schema.org/ConfirmAction", "https://schema.org/Event", "https://schema.org/PublicationEvent",
                "https://schema.org/BroadcastEvent", "https://schema.org/Clip", "https://schema.org/TVClip", "https://schema.org/Attorney",
                "https://schema.org/AutomotiveBusiness", "https://schema.org/AutoRental", "https://schema.org/TradeAction",
                "https://schema.org/PayAction", "https://schema.org/SolveMathAction", "https://schema.org/Festival",
                "https://schema.org/OrganizeAction", "https://schema.org/AllocateAction", "https://schema.org/AuthorizeAction",
                "https://schema.org/AmusementPark", "https://schema.org/ExhibitionEvent", "https://schema.org/MonetaryAmount",
                "https://schema.org/MedicalProcedure", "https://schema.org/TherapeuticProcedure", "https://schema.org/MedicalTherapy",
                "https://schema.org/BookFormatType", "https://schema.org/BackgroundNewsArticle", "https://schema.org/MusicReleaseFormatType",
                "https://schema.org/RadioSeason", "https://schema.org/Quotation", "https://schema.org/EngineSpecification", "https://schema.org/GeoShape",
                "https://schema.org/OrderAction", "https://schema.org/Time", "https://schema.org/SocialMediaPosting", "https://schema.org/BlogPosting",
                "https://schema.org/PerformingGroup", "https://schema.org/Landform", "https://schema.org/Volcano", "https://schema.org/InstallAction",
                "https://schema.org/MediaObject", "https://schema.org/ImageObject", "https://schema.org/Barcode", "https://schema.org/NonprofitType",
                "https://schema.org/UKNonprofitType", "https://schema.org/TravelAgency", "https://schema.org/ComputerStore", "https://schema.org/House",
                "https://schema.org/RsvpAction", "https://schema.org/Vessel", "https://schema.org/LymphaticVessel", "https://schema.org/VirtualLocation",
                "https://schema.org/ClothingStore", "https://schema.org/Service", "https://schema.org/BroadcastService",
                "https://schema.org/RadioBroadcastService", "https://schema.org/HowToDirection", "https://schema.org/GasStation",
                "https://schema.org/BusinessEntityType", "https://schema.org/MenuSection", "https://schema.org/WarrantyScope",
                "https://schema.org/MoveAction", "https://schema.org/MeasurementTypeEnumeration", "https://schema.org/ParcelDelivery",
                "https://schema.org/Person", "https://schema.org/WebPage", "https://schema.org/ProfilePage", "https://schema.org/RecyclingCenter",
                "https://schema.org/CssSelectorType", "https://schema.org/SpeakableSpecification", "https://schema.org/URL", "https://schema.org/Product",
                "https://schema.org/Vehicle", "https://schema.org/MotorizedBicycle", "https://schema.org/UserInteraction", "https://schema.org/UserDownloads",
                "https://schema.org/ControlAction", "https://schema.org/ActivateAction", "https://schema.org/SubscribeAction",
                "https://schema.org/SoftwareSourceCode", "https://schema.org/HomeAndConstructionBusiness", "https://schema.org/HousePainter",
                "https://schema.org/Bridge", "https://schema.org/ReturnMethodEnumeration",  "https://schema.org/PublicationVolume",
                "https://schema.org/FoodEstablishment", "https://schema.org/FastFoodRestaurant", "https://schema.org/ConvenienceStore",
                "https://schema.org/MedicalContraindication", "https://schema.org/DigitalDocumentPermissionType", "https://schema.org/QualitativeValue",
                "https://schema.org/SteeringPositionValue", "https://schema.org/PriceComponentTypeEnumeration", "https://schema.org/HealthAspectEnumeration",
                "https://schema.org/GeoCircle", "https://schema.org/SellAction", "https://schema.org/AnimalShelter", "https://schema.org/Trip",
                "https://schema.org/Series", "https://schema.org/EventSeries", "https://schema.org/MedicalAudience", "https://schema.org/Claim",
                "https://schema.org/Order", "https://schema.org/OccupationalTherapy", "https://schema.org/MusicVideoObject", "https://schema.org/AutoRepair",
                "https://schema.org/FindAction", "https://schema.org/CheckAction", "https://schema.org/Offer", "https://schema.org/OfferForPurchase",
                "https://schema.org/SearchResultsPage", "https://schema.org/HealthAndBeautyBusiness", "https://schema.org/MedicalOrganization",
                "https://schema.org/MedicalBusiness", "https://schema.org/Pharmacy", "https://schema.org/WebPageElement", "https://schema.org/WPSideBar",
                "https://schema.org/EventVenue", "https://schema.org/WholesaleStore", "https://schema.org/SportsOrganization",
                "https://schema.org/SportsTeam", "https://schema.org/Drawing", "https://schema.org/FinancialProduct", "https://schema.org/BoatTerminal",
                "https://schema.org/TransferAction", "https://schema.org/ReceiveAction", "https://schema.org/CreateAction",
                "https://schema.org/CookAction", "https://schema.org/EducationalOrganization", "https://schema.org/ElementarySchool",
                "https://schema.org/Reservation", "https://schema.org/FoodEstablishmentReservation", "https://schema.org/OfferForLease",
                "https://schema.org/Role", "https://schema.org/LinkRole", "https://schema.org/EmergencyService", "https://schema.org/Hospital",
                "https://schema.org/TVSeason", "https://schema.org/AssessAction", "https://schema.org/ReactAction", "https://schema.org/DisagreeAction",
                "https://schema.org/OpeningHoursSpecification", "https://schema.org/GenderType", "https://schema.org/SheetMusic",
                "https://schema.org/HowToItem", "https://schema.org/HowToTool", "https://schema.org/CurrencyConversionService", "https://schema.org/RadioClip",
                "https://schema.org/PhysicalActivityCategory", "https://schema.org/CheckInAction", "https://schema.org/DigitalDocument",
                "https://schema.org/PresentationDigitalDocument", "https://schema.org/AggregateOffer", "https://schema.org/OutletStore",
                "https://schema.org/EducationalOccupationalProgram", "https://schema.org/MediaManipulationRatingEnumeration",
                "https://schema.org/LibrarySystem", "https://schema.org/SelfStorage", "https://schema.org/MusicPlaylist",
                "https://schema.org/MedicalIntangible", "https://schema.org/DoseSchedule", "https://schema.org/MaximumDoseSchedule",
                "https://schema.org/ChildrensEvent", "https://schema.org/MedicalGuideline", "https://schema.org/MedicalGuidelineRecommendation",
                "https://schema.org/Seat", "https://schema.org/UpdateAction", "https://schema.org/AddAction", "https://schema.org/InsertAction",
                "https://schema.org/Car", "https://schema.org/Code", "https://schema.org/ParentAudience", "https://schema.org/MeetingRoom",
                "https://schema.org/TaxiReservation", "https://schema.org/AudioObject", "https://schema.org/WorkersUnion", "https://schema.org/Distillery",
                "https://schema.org/Patient", "https://schema.org/UserBlocks", "https://schema.org/Optician", "https://schema.org/CommentAction",
                "https://schema.org/DrugStrength", "https://schema.org/BroadcastChannel", "https://schema.org/RadioChannel",
                "https://schema.org/ViewAction", "https://schema.org/HowTo", "https://schema.org/SchoolDistrict", "https://schema.org/BioChemEntity",
                "https://schema.org/BoardingPolicyType",  "https://schema.org/UserPlusOnes", "https://schema.org/BarOrPub", "https://schema.org/Specialty",
                "https://schema.org/HowToSupply", "https://schema.org/MedicalTest", "https://schema.org/MedicalTestPanel", "https://schema.org/TipAction",
                "https://schema.org/AchieveAction", "https://schema.org/HyperToc", "https://schema.org/EducationalOccupationalCredential",
                "https://schema.org/FoodService", "https://schema.org/FlightReservation", "https://schema.org/Brand", "https://schema.org/Date",
                "https://schema.org/BedType", "https://schema.org/FAQPage", "https://schema.org/FireStation", "https://schema.org/OrganizationRole",
                "https://schema.org/EmployeeRole", "https://schema.org/Blog", "https://schema.org/Substance", "https://schema.org/Drug",
                "https://schema.org/AppendAction", "https://schema.org/MedicalCondition", "https://schema.org/MedicalRiskEstimator",
                "https://schema.org/MedicalRiskCalculator", "https://schema.org/TaxiStand", "https://schema.org/HyperTocEntry", "https://schema.org/Game",
                "https://schema.org/SoftwareApplication", "https://schema.org/VideoGame", "https://schema.org/ImageObjectSnapshot",
                "https://schema.org/School", "https://schema.org/MedicalConditionStage", "https://schema.org/MerchantReturnPolicy",
                "https://schema.org/MedicalAudienceType", "https://schema.org/ComicStory", "https://schema.org/TrainStation", "https://schema.org/Quantity",
                "https://schema.org/Energy", "https://schema.org/PlanAction", "https://schema.org/DateTime", "https://schema.org/Muscle",
                "https://schema.org/StadiumOrArena", "https://schema.org/FoodEvent", "https://schema.org/BankAccount", "https://schema.org/DayOfWeek",
                "https://schema.org/BodyOfWater", "https://schema.org/MusicVenue", "https://schema.org/BeautySalon", "https://schema.org/Duration",
                "https://schema.org/CompoundPriceSpecification", "https://schema.org/ArriveAction", "https://schema.org/SportingGoodsStore",
                "https://schema.org/Comment", "https://schema.org/HighSchool", "https://schema.org/TattooParlor", "https://schema.org/DepartAction",
                "https://schema.org/MoneyTransfer", "https://schema.org/City", "https://schema.org/DrawAction", "https://schema.org/AutoPartsStore",
                "https://schema.org/PhotographAction", "https://schema.org/DeliveryMethod", "https://schema.org/Continent", "https://schema.org/AmpStory",
                "https://schema.org/DiagnosticProcedure", "https://schema.org/BusReservation", "https://schema.org/Legislation",
                "https://schema.org/MusicRelease", "https://schema.org/Ligament", "https://schema.org/HealthPlanCostSharingSpecification",
                "https://schema.org/Language", "https://schema.org/MedicalSpecialty", "https://schema.org/DrugCost", "https://schema.org/Ticket",
                "https://schema.org/FMRadioChannel", "https://schema.org/MedicalStudy", "https://schema.org/MedicalObservationalStudy",
                "https://schema.org/GovernmentBenefitsType", "https://schema.org/FinancialService", "https://schema.org/InsuranceAgency",
                "https://schema.org/Bone", "https://schema.org/PsychologicalTreatment", "https://schema.org/Manuscript", "https://schema.org/Conversation",
                "https://schema.org/BroadcastFrequencySpecification", "https://schema.org/DatedMoneySpecification", "https://schema.org/SurgicalProcedure",
                "https://schema.org/DanceGroup", "https://schema.org/GovernmentOrganization", "https://schema.org/Gene", "https://schema.org/ComedyEvent",
                "https://schema.org/Book", "https://schema.org/PropertyValue", "https://schema.org/LocationFeatureSpecification",
                "https://schema.org/SportsClub", "https://schema.org/IgnoreAction", "https://schema.org/BusinessEvent",
                "https://schema.org/SpecialAnnouncement", "https://schema.org/TouristAttraction", "https://schema.org/Review",
                "https://schema.org/BoatReservation", "https://schema.org/DownloadAction", "https://schema.org/OceanBodyOfWater",
                "https://schema.org/UserPlays", "https://schema.org/MedicalIndication", "https://schema.org/PreventionIndication",
                "https://schema.org/TechArticle", "https://schema.org/APIReference", "https://schema.org/BusOrCoach",
                "https://schema.org/ProductReturnPolicy", "https://schema.org/Occupation", "https://schema.org/InviteAction",
                "https://schema.org/InvestmentOrDeposit", "https://schema.org/InvestmentFund", "https://schema.org/PaymentStatusType",
                "https://schema.org/ServiceChannel", "https://schema.org/CancelAction", "https://schema.org/PaymentMethod",
                "https://schema.org/MedicalObservationalStudyDesign", "https://schema.org/FundingAgency", "https://schema.org/Painting",
                "https://schema.org/SendAction", "https://schema.org/GeneralContractor", "https://schema.org/Researcher",
                "https://schema.org/FurnitureStore", "https://schema.org/ResearchProject", "https://schema.org/WPFooter", "https://schema.org/Flight",
                "https://schema.org/CollegeOrUniversity", "https://schema.org/QuantitativeValue", "https://schema.org/VeterinaryCare",
                "https://schema.org/MedicalSignOrSymptom", "https://schema.org/MedicalSign", "https://schema.org/VitalSign", "https://schema.org/Taxon",
                "https://schema.org/LakeBodyOfWater", "https://schema.org/VideoObject", "https://schema.org/VideoObjectSnapshot",
                "https://schema.org/Locksmith", "https://schema.org/TravelAction", "https://schema.org/AlignmentObject", "https://schema.org/MovieTheater",
                "https://schema.org/RiverBodyOfWater", "https://schema.org/RVPark", "https://schema.org/OfferCatalog",
                "https://schema.org/DiscussionForumPosting", "https://schema.org/MenuItem", "https://schema.org/DepartmentStore",
                "https://schema.org/SocialEvent", "https://schema.org/Resort", "https://schema.org/SkiResort", "https://schema.org/Crematorium",
                "https://schema.org/Report", "https://schema.org/DeliveryChargeSpecification", "https://schema.org/CheckoutPage",
                "https://schema.org/Recipe", "https://schema.org/Playground", "https://schema.org/Protein", "https://schema.org/Artery",
                "https://schema.org/DeactivateAction", "https://schema.org/LeaveAction", "https://schema.org/SuperficialAnatomy",
                "https://schema.org/GroceryStore", "https://schema.org/MotorcycleRepair", "https://schema.org/Question",
                "https://schema.org/InfectiousDisease", "https://schema.org/Menu", "https://schema.org/RadioEpisode",
                "https://schema.org/BloodTest", "https://schema.org/DrugPrescriptionStatus", "https://schema.org/Mountain",
                "https://schema.org/ContactPage", "https://schema.org/LandmarksOrHistoricalBuildings", "https://schema.org/EventAttendanceModeEnumeration",
                "https://schema.org/Casino", "https://schema.org/Zoo", "https://schema.org/GameServer", "https://schema.org/MedicalStudyStatus",
                "https://schema.org/NLNonprofitType", "https://schema.org/BusStation", "https://schema.org/LendAction", "https://schema.org/AMRadioChannel",
                "https://schema.org/MobileApplication", "https://schema.org/LikeAction", "https://schema.org/Reservoir",
                "https://schema.org/ProfessionalService", "https://schema.org/Electrician", "https://schema.org/MedicalTrialDesign",
                "https://schema.org/Taxi", "https://schema.org/ShortStory", "https://schema.org/LearningResource",
                "https://schema.org/DigitalDocumentPermission", "https://schema.org/PublicToilet", "https://schema.org/ChooseAction",
                "https://schema.org/VoteAction", "https://schema.org/Courthouse", "https://schema.org/QuoteAction",
                "https://schema.org/SpreadsheetDigitalDocument", "https://schema.org/CollectionPage", "https://schema.org/SomeProducts",
                "https://schema.org/DanceEvent", "https://schema.org/VisualArtwork", "https://schema.org/CoverArt", "https://schema.org/ComicCoverArt",
                "https://schema.org/ImagingTest", "https://schema.org/WearableMeasurementTypeEnumeration", "https://schema.org/WinAction",
                "https://schema.org/RegisterAction", "https://schema.org/DataFeedItem", "https://schema.org/ReserveAction",
                "https://schema.org/DietarySupplement", "https://schema.org/RentAction", "https://schema.org/BrainStructure", "https://schema.org/ItemPage",
                "https://schema.org/PreOrderAction", "https://schema.org/ExchangeRateSpecification", "https://schema.org/ScheduleAction",
                "https://schema.org/GolfCourse", "https://schema.org/MedicineSystem", "https://schema.org/ActionStatusType",
                "https://schema.org/DefinedTermSet", "https://schema.org/TextDigitalDocument", "https://schema.org/MediaSubscription",
                "https://schema.org/EatAction", "https://schema.org/WatchAction", "https://schema.org/FloorPlan", "https://schema.org/Permit",
                "https://schema.org/GovernmentPermit", "https://schema.org/AutoBodyShop", "https://schema.org/ReportageNewsArticle",
                "https://schema.org/ShoeStore", "https://schema.org/Grant", "https://schema.org/MonetaryGrant", "https://schema.org/Pond",
                "https://schema.org/DDxElement", "https://schema.org/DaySpa", "https://schema.org/GovernmentService",
                "https://schema.org/RentalCarReservation", "https://schema.org/SiteNavigationElement", "https://schema.org/SuspendAction",
                "https://schema.org/MedicalClinic", "https://schema.org/HomeGoodsStore", "https://schema.org/Airport",
                "https://schema.org/MedicalImagingTechnique", "https://schema.org/Invoice", "https://schema.org/UnRegisterAction",
                "https://schema.org/CreativeWorkSeries", "https://schema.org/TVSeries", "https://schema.org/AdvertiserContentArticle",
                "https://schema.org/NailSalon", "https://schema.org/DeliveryTimeSettings", "https://schema.org/RefundTypeEnumeration",
                "https://schema.org/HealthPlanFormulary", "https://schema.org/WPHeader", "https://schema.org/LoanOrCredit",
                "https://schema.org/MortgageLoan", "https://schema.org/ChildCare", "https://schema.org/ContactPointOption", "https://schema.org/Residence",
                "https://schema.org/DriveWheelConfigurationValue", "https://schema.org/CDCPMDRecord", "https://schema.org/VideoGameClip",
                "https://schema.org/TrackAction", "https://schema.org/Mosque", "https://schema.org/Church", "https://schema.org/CatholicChurch",
                "https://schema.org/Message", "https://schema.org/EmailMessage", "https://schema.org/Hackathon", "https://schema.org/SizeGroupEnumeration",
                "https://schema.org/WearableSizeGroupEnumeration", "https://schema.org/MedicalEvidenceLevel", "https://schema.org/Periodical",
                "https://schema.org/Campground", "https://schema.org/MusicAlbumReleaseType", "https://schema.org/UserLikes",
                "https://schema.org/InfectiousAgentClass", "https://schema.org/MovieRentalStore", "https://schema.org/RoofingContractor",
                "https://schema.org/PostalCodeRangeSpecification", "https://schema.org/StupidType", "https://schema.org/DataCatalog",
                "https://schema.org/OccupationalExperienceRequirements", "https://schema.org/PerformanceRole", "https://schema.org/SizeSystemEnumeration",
                "https://schema.org/WearableSizeSystemEnumeration", "https://schema.org/DiagnosticLab", "https://schema.org/HealthInsurancePlan",
                "https://schema.org/Quiz", "https://schema.org/ReturnLabelSourceEnumeration", "https://schema.org/ChemicalSubstance",
                "https://schema.org/Museum", "https://schema.org/GardenStore", "https://schema.org/EmploymentAgency",
                "https://schema.org/OfficeEquipmentStore", "https://schema.org/ToyStore", "https://schema.org/Consortium",
                "https://schema.org/SeekToAction", "https://schema.org/PaymentService", "https://schema.org/BikeStore",
                "https://schema.org/AnatomicalSystem", "https://schema.org/TennisComplex", "https://schema.org/Integer", "https://schema.org/Motel",
                "https://schema.org/CarUsageType", "https://schema.org/InteractionCounter", "https://schema.org/MiddleSchool",
                "https://schema.org/StatisticalPopulation", "https://schema.org/LifestyleModification", "https://schema.org/BodyMeasurementTypeEnumeration",
                "https://schema.org/PropertyValueSpecification", "https://schema.org/ArtGallery", "https://schema.org/Physician",
                "https://schema.org/OwnershipInfo", "https://schema.org/BedDetails", "https://schema.org/AutoWash", "https://schema.org/WPAdBlock",
                "https://schema.org/PoliceStation", "https://schema.org/EnergyEfficiencyEnumeration", "https://schema.org/EUEnergyEfficiencyEnumeration",
                "https://schema.org/PublicationIssue", "https://schema.org/MolecularEntity", "https://schema.org/BrokerageAccount",
                "https://schema.org/MovingCompany", "https://schema.org/Restaurant", "https://schema.org/ItemListOrderType", "https://schema.org/NightClub",
                "https://schema.org/DefinedRegion", "https://schema.org/HardwareStore", "https://schema.org/AboutPage",
                "https://schema.org/ReturnFeesEnumeration", "https://schema.org/TaxiService", "https://schema.org/DrugClass", "https://schema.org/Park",
                "https://schema.org/DrugCostCategory", "https://schema.org/BuddhistTemple", "https://schema.org/MedicalCause", "https://schema.org/Canal",
                "https://schema.org/WebSite", "https://schema.org/DepositAccount", "https://schema.org/Schedule", "https://schema.org/Synagogue",
                "https://schema.org/TelevisionChannel", "https://schema.org/QAPage", "https://schema.org/Suite", "https://schema.org/ArchiveComponent",
                "https://schema.org/AssignAction", "https://schema.org/LegalForceStatus", "https://schema.org/UserComments", "https://schema.org/Dentist",
                "https://schema.org/OfferShippingDetails", "https://schema.org/DonateAction", "https://schema.org/DefenceEstablishment",
                "https://schema.org/VisualArtsEvent", "https://schema.org/RsvpResponseType", "https://schema.org/ComicSeries",
                "https://schema.org/TrainTrip", "https://schema.org/WebContent", "https://schema.org/HealthTopicContent",
                "https://schema.org/CafeOrCoffeeShop", "https://schema.org/CriticReview", "https://schema.org/GiveAction", "https://schema.org/PawnShop",
                "https://schema.org/ContactPoint", "https://schema.org/Recommendation", "https://schema.org/ReservationPackage",
                "https://schema.org/SearchAction", "https://schema.org/TrainReservation", "https://schema.org/ScreeningEvent",
                "https://schema.org/Aquarium", "https://schema.org/RecommendedDoseSchedule", "https://schema.org/AudioObjectSnapshot",
                "https://schema.org/DrugLegalStatus", "https://schema.org/OrderStatus", "https://schema.org/PalliativeProcedure",
                "https://schema.org/OpinionNewsArticle", "https://schema.org/VideoGameSeries", "https://schema.org/TouristTrip",
                "https://schema.org/TelevisionStation", "https://schema.org/CorrectionComment", "https://schema.org/AskPublicNewsArticle",
                "https://schema.org/SatiricalArticle", "https://schema.org/TVEpisode", "https://schema.org/TireShop", "https://schema.org/FollowAction",
                "https://schema.org/DataFeed", "https://schema.org/MedicalDevice", "https://schema.org/TypeAndQuantityNode",
                "https://schema.org/Corporation", "https://schema.org/LegalValueLevel", "https://schema.org/UserReview",
                "https://schema.org/EndorsementRating", "https://schema.org/TieAction", "https://schema.org/HealthClub",
                "https://schema.org/ComputerLanguage", "https://schema.org/PlayAction", "https://schema.org/ExerciseAction",
                "https://schema.org/Collection", "https://schema.org/Nerve", "https://schema.org/Preschool", "https://schema.org/TouristDestination",
                "https://schema.org/MediaReview", "https://schema.org/BorrowAction", "https://schema.org/SingleFamilyResidence",
                "https://schema.org/AcceptAction", "https://schema.org/PodcastSeries", "https://schema.org/InternetCafe", "https://schema.org/HinduTemple",
                "https://schema.org/BusStop", "https://schema.org/HealthPlanNetwork", "https://schema.org/Chapter", "https://schema.org/3DModel",
                "https://schema.org/IceCreamShop", "https://schema.org/PetStore", "https://schema.org/Distance", "https://schema.org/MediaReviewItem",
                "https://schema.org/Thesis", "https://schema.org/EventReservation", "https://schema.org/HVACBusiness", "https://schema.org/PaymentCard",
                "https://schema.org/PriceTypeEnumeration", "https://schema.org/LoseAction", "https://schema.org/MedicalWebPage",
                "https://schema.org/ApplyAction", "https://schema.org/Table", "https://schema.org/ReviewNewsArticle", "https://schema.org/MusicEvent",
                "https://schema.org/CreditCard", "https://schema.org/BuyAction", "https://schema.org/Guide", "https://schema.org/EndorseAction",
                "https://schema.org/LiveBlogPosting", "https://schema.org/MedicalGuidelineContraindication", "https://schema.org/RadiationTherapy",
                "https://schema.org/TreatmentIndication", "https://schema.org/Airline", "https://schema.org/BookStore",
                "https://schema.org/CableOrSatelliteService", "https://schema.org/MediaGallery", "https://schema.org/TheaterEvent",
                "https://schema.org/GovernmentOffice", "https://schema.org/ReviewAction", "https://schema.org/QuantitativeValueDistribution",
                "https://schema.org/MonetaryAmountDistribution", "https://schema.org/BreadcrumbList", "https://schema.org/WebApplication",
                "https://schema.org/ApprovedIndication", "https://schema.org/ClaimReview", "https://schema.org/PostOffice", "https://schema.org/Plumber",
                "https://schema.org/WorkBasedProgram", "https://schema.org/MerchantReturnPolicySeasonalOverride", "https://schema.org/Photograph",
                "https://schema.org/AskAction", "https://schema.org/Season", "https://schema.org/MedicalTrial", "https://schema.org/WriteAction",
                "https://schema.org/JewelryStore", "https://schema.org/SaleEvent", "https://schema.org/AutoDealer", "https://schema.org/FilmAction",
                "https://schema.org/DrugPregnancyCategory", "https://schema.org/MensClothingStore", "https://schema.org/RestrictedDiet",
                "https://schema.org/MedicalRiskFactor", "https://schema.org/PerformingArtsTheater", "https://schema.org/Course",
                "https://schema.org/DataDownload", "https://schema.org/PerformAction", "https://schema.org/EntryPoint",
                "https://schema.org/LodgingReservation", "https://schema.org/ComicIssue", "https://schema.org/Hostel",
                "https://schema.org/NoteDigitalDocument", "https://schema.org/VideoGallery", "https://schema.org/EducationEvent",
                "https://schema.org/MusicStore", "https://schema.org/ReadAction", "https://schema.org/Beach", "https://schema.org/ScholarlyArticle",
                "https://schema.org/ProductReturnEnumeration", "https://schema.org/ResearchOrganization", "https://schema.org/SeaBodyOfWater",
                "https://schema.org/PostalAddress", "https://schema.org/AgreeAction", "https://schema.org/Cemetery", "https://schema.org/SportsEvent",
                "https://schema.org/MusicComposition", "https://schema.org/Statement", "https://schema.org/BookSeries", "https://schema.org/HairSalon",
                "https://schema.org/CityHall", "https://schema.org/WebAPI", "https://schema.org/Hotel", "https://schema.org/ListenAction",
                "https://schema.org/ProductModel", "https://schema.org/BusinessFunction", "https://schema.org/MobilePhoneStore",
                "https://schema.org/Florist", "https://schema.org/LiteraryEvent", "https://schema.org/Apartment", "https://schema.org/Movie",
                "https://schema.org/BusTrip", "https://schema.org/PathologyTest", "https://schema.org/CourseInstance", "https://schema.org/Play",
                "https://schema.org/Waterfall", "https://schema.org/MathSolver", "https://schema.org/RealEstateListing",
                "https://schema.org/NewsMediaOrganization", "https://schema.org/MusicGroup", "https://schema.org/ItemAvailability",
                "https://schema.org/ProductGroup", "https://schema.org/PaintAction", "https://schema.org/ProgramMembership",
                "https://schema.org/ActionAccessSpecification", "https://schema.org/TheaterGroup", "https://schema.org/HowToStep",
                "https://schema.org/ShippingDeliveryTime", "https://schema.org/UserCheckins", "https://schema.org/Vein", "https://schema.org/PhysicalExam",
                "https://schema.org/BankOrCreditUnion", "https://schema.org/PhysicalActivity", "https://schema.org/ExercisePlan",
                "https://schema.org/ReplaceAction", "https://schema.org/Diet", "https://schema.org/MedicalSymptom", "https://schema.org/ImageGallery",
                "https://schema.org/MedicalRiskScore", "https://schema.org/BoatTrip", "https://schema.org/JobPosting", "https://schema.org/Motorcycle",
                "https://schema.org/AutomatedTeller", "https://schema.org/UnitPriceSpecification", "https://schema.org/OnDemandEvent",
                "https://schema.org/CovidTestingFacility", "https://schema.org/Winery", "https://schema.org/LegislationObject", "https://schema.org/Brewery",
                "https://schema.org/Observation", "https://schema.org/Sculpture", "https://schema.org/CheckOutAction", "https://schema.org/Audiobook",
                "https://schema.org/RealEstateAgent", "https://schema.org/USNonprofitType", "https://schema.org/Property",
                "https://schema.org/ArchiveOrganization", "https://schema.org/BusinessAudience", "https://schema.org/MovieSeries",
                "https://schema.org/Atlas", "https://schema.org/Library",  "https://schema.org/IndividualProduct", "https://schema.org/CampingPitch",
                "https://schema.org/MerchantReturnEnumeration", "https://schema.org/GeospatialGeometry", "https://schema.org/BefriendAction",
                "https://schema.org/JoinAction", "https://schema.org/BowlingAlley", "https://schema.org/DiscoverAction", "https://schema.org/Answer",
                "https://schema.org/DislikeAction", "https://schema.org/Newspaper", "https://schema.org/State", "https://schema.org/HobbyShop",
                "https://schema.org/GatedResidenceCommunity", "https://schema.org/UserPageVisits", "https://schema.org/Embassy",
                "https://schema.org/SizeSpecification", "https://schema.org/MedicalCode", "https://schema.org/ExerciseGym", "https://schema.org/DeleteAction",
                "https://schema.org/MusicAlbum", "https://schema.org/EnergyStarEnergyEfficiencyEnumeration", "https://schema.org/ResumeAction",
                "https://schema.org/Bakery", "https://schema.org/MovieClip", "https://schema.org/MedicalDevicePurpose", "https://schema.org/RadioSeries",
                "https://schema.org/EnergyConsumptionDetails", "https://schema.org/GeoCoordinates", "https://schema.org/MedicalScholarlyArticle",
                "https://schema.org/TakeAction", "https://schema.org/EventStatusType", "https://schema.org/AdultEntertainment", "https://schema.org/Boolean",
                "https://schema.org/WarrantyPromise", "https://schema.org/GameServerStatus", "https://schema.org/Mass", "https://schema.org/Float",
                "https://schema.org/HowToTip", "https://schema.org/MusicRecording", "https://schema.org/ApartmentComplex",
                "https://schema.org/ShoppingCenter", "https://schema.org/GamePlayMode", "https://schema.org/DeliveryEvent",
                "https://schema.org/RepaymentSpecification", "https://schema.org/Map", "https://schema.org/RadioStation", "https://schema.org/PrependAction",
                "https://schema.org/TouristInformationCenter", "https://schema.org/OrderItem", "https://schema.org/ReplyAction",
                "https://schema.org/UserTweets", "https://schema.org/OfferItemCondition", "https://schema.org/WantAction", "https://schema.org/RejectAction",
                "https://schema.org/CompleteDataFeed", "https://schema.org/ShareAction", "https://schema.org/NGO", "https://schema.org/ElectronicsStore",
                "https://schema.org/ProductCollection", "https://schema.org/PhysicalTherapy", "https://schema.org/CategoryCodeSet",
                "https://schema.org/MapCategoryType", "https://schema.org/ReportedDoseSchedule", "https://schema.org/EducationalAudience",
                "https://schema.org/ReturnAction", "https://schema.org/BookmarkAction", "https://schema.org/EmployerReview",
                "https://schema.org/ShippingRateSettings", "https://schema.org/AccountingService", "https://schema.org/NutritionInformation",
                "https://schema.org/XPathType", "https://schema.org/MusicAlbumProductionType", "https://schema.org/MotorcycleDealer"
        );
    }
}
