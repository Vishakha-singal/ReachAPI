package edu.asu.heal.core.api.dao.impl;

import com.mongodb.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import edu.asu.heal.core.api.dao.DAO;
import edu.asu.heal.core.api.dao.DAOException;
import edu.asu.heal.core.api.models.*;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.types.ObjectId;

import java.util.*;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class MongoDBDAO implements DAO {

    // collection captions
    public static final String DOMAINS_COLLECTION = "domains";
    public static final String TRIALS_COLLECTION = "trials";
    public static final String ACTIVITIES_COLLECTION = "activities";
    public static final String PATIENTS_COLLECTION = "patients";
    public static final String ACTIVITYINSTANCES_COLLECTION = "activityInstances";

    private String __mongoUser;
    private String __mongoPassword;
    private String __mongoHost;
    private int __mongoPort;
    private String __mongoDBName;
    private String __mongoURI;

    public MongoDBDAO() {}

    public MongoDBDAO(Properties properties){
        __mongoURI = properties.getProperty("mongo.uri");
        __mongoDBName = properties.getProperty("mongo.dbname");
    }

    private MongoDatabase getConnectedDatabase(){
        try {

            // create codec registry for POJOs
            CodecRegistry pojoCodecRegistry = fromRegistries(MongoClient.getDefaultCodecRegistry(),
                    fromProviders(PojoCodecProvider.builder().automatic(true).build()));

            MongoClient mongoClient = new MongoClient(new MongoClientURI(__mongoURI));

            // get handle to "_mongoDBName" database
            return mongoClient.getDatabase(__mongoDBName).withCodecRegistry(pojoCodecRegistry);
        }catch (Exception e){
            System.out.println("SOME ERROR GETTING MONGO CONNECTION");
            e.printStackTrace();
            return null;
        }

    }

    @Override
    public List<Domain> getDomains(){
        MongoDatabase database = getConnectedDatabase();
        MongoCollection<Domain> domainCollection = database.getCollection("domains", Domain.class);

        return domainCollection.find().into(new ArrayList<>());
    }

    @Override
    public Object getDomain(String id){
        MongoDatabase database = getConnectedDatabase();
        MongoCollection<Document> domainCollection = database.getCollection(MongoDBDAO.DOMAINS_COLLECTION);

        return domainCollection.find(Filters.eq("_id", new ObjectId(id))).first();
    }

    @Override
    public String createDomain(Domain instance) {
        MongoDatabase database = getConnectedDatabase();
        MongoCollection<Domain> domainCollection = database.getCollection(MongoDBDAO.DOMAINS_COLLECTION, Domain.class);

        domainCollection.insertOne(instance);

        return "SUCCESS";
    }

    @Override
    public List<ActivityInstance> getScheduledActivities(int patientPin, int currentDay) throws DAOException {
        try {
            MongoDatabase database = getConnectedDatabase();
            MongoCollection<Patient> patientCollection = database.getCollection(MongoDBDAO.PATIENTS_COLLECTION, Patient.class);

            Patient patient = patientCollection.find(Filters.eq(Patient.PIN_ATTRIBUTE, patientPin)).first();

            MongoCollection<ActivityInstance> activityInstanceCollection =
                    database.getCollection(MongoDBDAO.ACTIVITYINSTANCES_COLLECTION, ActivityInstance.class);

            return activityInstanceCollection
                    .find(Filters.in(ActivityInstance.ID_ATTRIBUTE,
                            patient.getActivityInstances().toArray(new ObjectId[] {})))
                    .into(new ArrayList<>());
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean scheduleSTOPActivity(int day, boolean completed) throws DAOException {
        return false;
    }

    @Override
    public boolean scheduleSTICActivity(int day, int sticVariable) throws DAOException {
        return false;
    }

    @Override
    public boolean scheduleRelaxationActivity(int day, boolean completed) throws DAOException {
        return false;
    }

    @Override
    public boolean scheduleDailyDiaryActivity(String dailyDiaryWeeklySchedule) {
        return false;
    }

    @Override
    public boolean scheduleABMTActivity(int day, boolean completed) throws DAOException {
        return false;
    }

    @Override
    public boolean scheduleWorryHeadsActivity(String worryHeadsWeeklySchedule) {
        return false;
    }

    @Override
    public boolean scheduleSAFEACtivity(int day, boolean completed) throws DAOException {
        return false;
    }

    @Override
    public Object getMakeBelieveActivityInstance() throws DAOException {
        return null;
    }

    @Override
    public boolean checkSituationExists(int situationId) throws DAOException {
        return false;
    }

    @Override
    public Object getMakeBelieveActivityAnswers(int situationId) throws DAOException {
        return null;
    }

    @Override
    public boolean updateMakeBelieveActivityInstance(Object makeBelieveResponse) throws DAOException {
        return false;
    }

    // methods pertaining to Activity (activities) collection
    @Override
    public List<Activity> getActivities(String domain) throws DAOException {
        try {
            MongoDatabase database = getConnectedDatabase();
            MongoCollection<Domain> domainCollection = database.getCollection(MongoDBDAO.DOMAINS_COLLECTION, Domain.class);

            Domain domain1 = domainCollection.find(Filters.eq(Domain.TITLE_ATTRIBUTE, domain)).first();
            MongoCollection<Activity> activityCollection = database.getCollection(MongoDBDAO.ACTIVITIES_COLLECTION, Activity.class);

            return activityCollection
                    .find(Filters.in(Activity.ID_ATTRIBUTE, domain1.getActivities().toArray(new ObjectId[]{})))
                    .into(new ArrayList<>());

        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String createActivity(Activity activity) throws DAOException {
        try {
            MongoDatabase database = getConnectedDatabase();
            MongoCollection<Activity> activitiesCollection = database.getCollection(MongoDBDAO.ACTIVITIES_COLLECTION,
                    Activity.class);

            activitiesCollection.insertOne(activity);

            return "SUCCESS";
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    // methods pertaining to the Trial Model
    @Override
    public List<Trial> getTrials(String domain) throws DAOException {
        try{
            MongoDatabase database = getConnectedDatabase();
            MongoCollection<Domain> domainCollection = database.getCollection(MongoDBDAO.DOMAINS_COLLECTION, Domain.class);

            Domain domain1 = domainCollection.find(Filters.eq(Domain.TITLE_ATTRIBUTE, domain)).first();
            MongoCollection<Trial> trialCollection = database.getCollection(MongoDBDAO.TRIALS_COLLECTION, Trial.class);

            return  trialCollection.find(
                    Filters.in(Trial.ID_ATTRIBUTE, domain1.getTrials().toArray(new ObjectId[]{})))
                    .into(new ArrayList<Trial>());
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String createTrial(Trial trialInstance) throws DAOException {
        try {
            MongoDatabase database = getConnectedDatabase();
            MongoCollection<Trial> trialCollection = database.getCollection(MongoDBDAO.TRIALS_COLLECTION, Trial.class);
            trialCollection.insertOne(trialInstance);

            return "SUCCESS";
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Patient> getPatients() throws DAOException {
        try {
            MongoDatabase database = getConnectedDatabase();
            MongoCollection<Patient> patientCollection = database.getCollection(PATIENTS_COLLECTION, Patient.class);

            return patientCollection.find().into(new ArrayList<>());
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Patient> getPatients(String trialId) throws DAOException {
        try{
            MongoDatabase database = getConnectedDatabase();
            MongoCollection<Trial> trialCollection = database.getCollection(MongoDBDAO.TRIALS_COLLECTION, Trial.class);

            Trial trial = trialCollection.find(Filters.eq(Trial.TRIALID_ATTRIBUTE, trialId)).first();

            MongoCollection<Patient> patientsCollection = database
                    .getCollection(MongoDBDAO.PATIENTS_COLLECTION, Patient.class);

            return patientsCollection
                    .find(Filters.in(Patient.ID_ATTRIBUTE, trial.getPatients().toArray(new ObjectId[] {})))
                    .into(new ArrayList<>());
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public int createPatient(String patientDetails) {
        try{

            //Temporary code to generate new pin. It just increments the largest pin number in the database by 1
            int newPin = getConnectedDatabase()
                    .getCollection(MongoDBDAO.PATIENTS_COLLECTION)
                    .aggregate(Arrays.asList(Aggregates.group(null,
                            Accumulators.max(Patient.PIN_ATTRIBUTE, "$"+Patient.PIN_ATTRIBUTE))))
                    .first()
                    .getInteger(Patient.PIN_ATTRIBUTE);
            ++newPin;

            Patient patient = new Patient();
            patient.setId(new ObjectId());
            patient.setPin(newPin);
            patient.setCreatedAt(new Date());
            patient.setEndDate(new Date());
            patient.setStartDate(new Date());
            patient.setUpdatedAt(new Date());
            patient.setState("created");
            patient.setActivityInstances(null);

            MongoDatabase database = getConnectedDatabase();
            MongoCollection<Patient> patientCollection = database.getCollection(MongoDBDAO.PATIENTS_COLLECTION, Patient.class);

            patientCollection.insertOne(patient);

            MongoCollection<Trial> trialsCollection = getConnectedDatabase()
                    .getCollection(MongoDBDAO.TRIALS_COLLECTION, Trial.class);

            Trial trial = trialsCollection.find(Filters.eq(Trial.TRIALID_ATTRIBUTE, patientDetails)).first();

            trial.getPatients().add(patient.getId());

            trialsCollection.replaceOne(Filters.eq(Trial.TRIALID_ATTRIBUTE, patientDetails), trial);

            return newPin;
        }catch (Exception e){
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public Patient getPatient(int patientPin) {
        try{
            MongoDatabase database = getConnectedDatabase();
            return database.getCollection(MongoDBDAO.PATIENTS_COLLECTION, Patient.class)
                    .find(Filters.eq(Patient.PIN_ATTRIBUTE, patientPin))
                    .first();
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
