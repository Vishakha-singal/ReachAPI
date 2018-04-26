package edu.asu.heal.core.api.service;

import edu.asu.heal.core.api.models.*;
import edu.asu.heal.reachv3.api.models.MakeBelieveSituation;

import java.util.List;

public interface HealService {


    /****************************************  Service methods for Activity  ******************************************/
    List<Activity> getActivities(String domain);

    Activity createActivity(String title, String description);

    Activity getActivity(String activityId);

    Activity updateActivity(Activity activity);

    Activity deleteActivity(String activityId);

    /****************************************  Service methods for ActivityInstance  **********************************/
    List<ActivityInstance> getActivityInstances(int patientPin, int trialId);

    ActivityInstance getActivityInstance(String activityInstanceId);

    ActivityInstance createActivityInstance(ActivityInstance activityInstanceJson);

    ActivityInstance deleteActivityInstance(String activityInstanceId);

    ActivityInstance updateActivityInstance(String requestBody);

    /****************************************  Service methods for Domain  ********************************************/
    List<Domain> getDomains();

    Domain getDomain(String id);

    Domain addDomain(String title, String description, String state);

    String addTestDomain(String title, String description, String state);

    /****************************************  Service methods for Patient  *******************************************/
    List<Patient> getPatients(String trialId);

    Patient getPatient(int patientPin);

    Patient createPatient();

    Patient updatePatient(Patient patient);

    String deletePatient(String patientPin);

    /****************************************  Service methods for Trial  *********************************************/
    List<Trial> getTrials(String domain);

    Trial addTrial(Trial trialInstance);

    /****************************************  Other Service methods  *************************************************/

    MakeBelieveSituation getMakeBelieveSituation();

    String getMakeBelieveInstanceAnswer(int instanceId);

    int updateMakeBelieveInstance(int instanceId, String responses);

    String getWorryHeadsInstance();
}