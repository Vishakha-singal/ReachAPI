package edu.asu.heal.reachv3.api.service;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.asu.heal.reachv3.api.models.*;
import edu.asu.heal.reachv3.api.models.schedule.ActivityScheduleJSON;
import edu.asu.heal.reachv3.api.models.schedule.AvailableTime;
import edu.asu.heal.reachv3.api.models.schedule.ModuleJSON;
import edu.asu.heal.reachv3.api.models.schedule.PatientScheduleJSON;
import edu.asu.heal.reachv3.api.models.schedule.ScheduleArrayJSON;
import edu.asu.heal.reachv3.api.notification.INotificationInterface;

import org.apache.http.ParseException;

import edu.asu.heal.core.api.dao.DAO;
import edu.asu.heal.core.api.dao.DAOFactory;
import edu.asu.heal.core.api.models.*;
import edu.asu.heal.core.api.service.HealService;
import edu.asu.heal.core.api.service.SuggestedActivityiesMappingService.MappingFactory;
import edu.asu.heal.core.api.service.SuggestedActivityiesMappingService.MappingInterface;

import java.io.InputStream;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class ReachService implements HealService {

	private static final String DATE_FORMAT = "MM/dd/yyyy";
	private static String TRIAL_NAME="Compass";
	private static String MODULE="module";
	private static String DAY="day";
	private static String MODULE_LENGTH="moduleLength";
	private static String SKILL_WH_L1_MIN = "WorryHeads.skill_level_1_min";
	private static String SKILL_WH_L1_MAX = "WorryHeads.skill_level_1_max";
	private static String SKILL_WH_L2_MIN = "WorryHeads.skill_level_2_min";
	private static String SKILL_WH_L2_MAX = "WorryHeads.skill_level_2_max";
	private static String SKILL_SU_L1_MIN = "StandUp.skill_level_1_min";
	private static String SKILL_SU_L1_MAX = "StandUp.skill_level_1_max";
	private static String SKILL_SU_L2_MIN = "StandUp.skill_level_2_min";
	private static String SKILL_SU_L2_MAX = "StandUp.skill_level_2_max";
	private static String SKILL_MB_L1_MIN = "MakeBelieve.skill_level_1_min";
	private static String SKILL_MB_L1_MAX = "MakeBelieve.skill_level_1_max";
	private static String SKILL_MB_L2_MIN = "MakeBelieve.skill_level_2_min";
	private static String SKILL_MB_L2_MAX = "MakeBelieve.skill_level_2_max";
	private static String MB_RESET_COUNT = "MakeBeleieve.reset.count";
	private static String WH_RESET_COUNT = "WorryHeads.reset.count";
	private static String SU_RESET_COUNT = "StandUp.reset.count";
	private static String LEVEL_1_ADHERENCE_THRESHOLD = "Level_1.adherence.threshold";
	private static String LEVEL_2_ADHERENCE_THRESHOLD = "Level_2.adherence.threshold";
	private static String AVERAGE_UI_FOR_DAYS ="average_ui_for_days";
	private static Integer INTERNAL_ERROR_CODE =500;
	private static Integer BAD_REQUEST_STATUS_CODE =400;

	private static Properties _properties;
	static {
		_properties = new Properties();
		try {
			InputStream propFile = ReachService.class.getResourceAsStream("notificationRule.properties");
			_properties.load(propFile);
			propFile.close();
		} catch (Exception e) {
			ServerExceptionLogger exceptionLogger = new ServerExceptionLogger();
			int lineNumber = e.getStackTrace()[0].getLineNumber(); 
			String classOfException = e.toString();
			String nameOfClass = "ReachService";
			String nameOfMethod = e.getStackTrace()[0].getMethodName();
			exceptionLogger.logServerException(lineNumber, classOfException, nameOfClass, nameOfMethod, -1,INTERNAL_ERROR_CODE);

		}
	}

	/****************************************  Service methods for Activity  ******************************************/
	@Override
	public List<Activity> getActivities(String domain) {
		try {
			DAO dao = DAOFactory.getTheDAO();
			List<Activity> result = dao.getActivities(domain);

			return result;
		} catch (Exception e) {

			ServerExceptionLogger exceptionLogger = new ServerExceptionLogger();
			int lineNumber = e.getStackTrace()[0].getLineNumber(); 
			String classOfException = e.toString();
			String nameOfClass = this.getClass().getSimpleName();
			String nameOfMethod = e.getStackTrace()[0].getMethodName();
			exceptionLogger.logServerException(lineNumber, classOfException, nameOfClass,
					nameOfMethod, -1,INTERNAL_ERROR_CODE);
			
			return null;
		}
	}

	@Override
	public Activity createActivity(String title, String description) {
		try {
			DAO dao = DAOFactory.getTheDAO();
			Activity newActivity = new Activity();
			newActivity.setTitle(title);
			newActivity.setDescription(description);
			newActivity.setUpdatedAt(new Date());
			newActivity.setCreatedAt(new Date());
			Activity createdActivity = dao.createActivity(newActivity);

			return createdActivity;
		} catch (Exception e) {

			ServerExceptionLogger exceptionLogger = new ServerExceptionLogger();
			int lineNumber = e.getStackTrace()[0].getLineNumber(); 
			String classOfException = e.toString();
			String nameOfClass = this.getClass().getSimpleName();
			String nameOfMethod = e.getStackTrace()[0].getMethodName();
			exceptionLogger.logServerException(lineNumber, classOfException, nameOfClass, 
					nameOfMethod, -1,INTERNAL_ERROR_CODE);

			return null;
		}
	}

	@Override
	public Activity getActivity(String activityId) {
		try {
			DAO dao = DAOFactory.getTheDAO();
			return dao.getActivity(activityId);
		} catch (Exception e) {

			ServerExceptionLogger exceptionLogger = new ServerExceptionLogger();
			int lineNumber = e.getStackTrace()[0].getLineNumber(); 
			String classOfException = e.toString();
			String nameOfClass = this.getClass().getSimpleName();
			String nameOfMethod = e.getStackTrace()[0].getMethodName();
			exceptionLogger.logServerException(lineNumber, classOfException,
					nameOfClass, nameOfMethod, -1,INTERNAL_ERROR_CODE);

			return null;
		}
	}

	@Override
	public Activity updateActivity(Activity activity) {
		try {
			DAO dao = DAOFactory.getTheDAO();
			Activity activityInDatabase = dao.getActivity(activity.getActivityId());
			if (activityInDatabase == null || activityInDatabase.equals(NullObjects.getNullActivity()))
				return activityInDatabase;

			activityInDatabase.setTitle(
					activity.getTitle() != null ? activity.getTitle() : activityInDatabase.getTitle());
			activityInDatabase.setDescription(
					activity.getDescription() != null ? activity.getDescription() : activityInDatabase.getDescription());
			activityInDatabase.setUpdatedAt(new Date());

			return dao.updateActivity(activityInDatabase);
		} catch (Exception e) {

			ServerExceptionLogger exceptionLogger = new ServerExceptionLogger();
			int lineNumber = e.getStackTrace()[0].getLineNumber(); 
			String classOfException = e.toString();
			String nameOfClass = this.getClass().getSimpleName();
			String nameOfMethod = e.getStackTrace()[0].getMethodName();
			exceptionLogger.logServerException(lineNumber, classOfException, nameOfClass, 
					nameOfMethod, -1,INTERNAL_ERROR_CODE);

			return null;
		}
	}

	@Override
	public Activity deleteActivity(String activityId) {
		try {
			DAO dao = DAOFactory.getTheDAO();
			return dao.deleteActivity(activityId);
		} catch (Exception e) {

			ServerExceptionLogger exceptionLogger = new ServerExceptionLogger();
			int lineNumber = e.getStackTrace()[0].getLineNumber(); 
			String classOfException = e.toString();
			String nameOfClass = this.getClass().getSimpleName();
			String nameOfMethod = e.getStackTrace()[0].getMethodName();
			exceptionLogger.logServerException(lineNumber, classOfException, nameOfClass, 
					nameOfMethod, -1,INTERNAL_ERROR_CODE);

			return null;
		}
	}

	/****************************************  Service methods for ActivityInstance  **********************************/
	@Override
	public List<ActivityInstance> getActivityInstances(int patientPin) {
		try {
			DAO dao = DAOFactory.getTheDAO();
			List<ActivityInstance> instances = dao.getScheduledActivities(patientPin);

			if(instances != null && instances.size() > 0 && instances.get(0).equals(NullObjects.getNullActivityInstance())) {
				StackTraceElement[] trace = Thread.currentThread().getStackTrace();
				ServerExceptionLogger exceptionLogger = new ServerExceptionLogger();

				// Below line number returns the where the lineNumber variable is declared.
				// This will be helpful when we have multiple null pointer for status code 400.

				int lineNumber = trace[1].getLineNumber(); 
				String classOfException = NullPointerException.class.getSimpleName();
				String nameOfClass = this.getClass().getSimpleName();
				String nameOfMethod = trace[1].getClassName();
				exceptionLogger.logServerException(lineNumber, classOfException, nameOfClass, nameOfMethod, patientPin,400);
			}
			return instances;
		} catch (Exception e) {
			ServerExceptionLogger exceptionLogger = new ServerExceptionLogger();
			int lineNumber = e.getStackTrace()[0].getLineNumber(); 
			String classOfException = e.toString();
			String nameOfClass = this.getClass().getSimpleName();
			String nameOfMethod = e.getStackTrace()[0].getMethodName();
			exceptionLogger.logServerException(lineNumber, classOfException, nameOfClass, 
					nameOfMethod, patientPin,INTERNAL_ERROR_CODE);
			System.out.println("SOME ERROR IN GETACTIVITYINSTANCES() IN REACHSERVICE");
			return null;
		}
	}

	public String getEmotionsActivityInstance(int patientPin, String emotion, int intensity) {
		try {
			DAO dao = DAOFactory.getTheDAO();
			MappingInterface mapper = MappingFactory.getTheMapper();
			String intensityVal = (String)mapper.intensityMappingToDifficultyLevel(intensity);

			String sessionId = "";

			PatientScheduleJSON patientSchedule = dao.getSchedule(patientPin);
			HashMap<String, Integer> modules = getModuleAndDay(patientSchedule, new Date());
			Integer module =-1;

			if (modules != null && modules.size() > 0 && modules.containsKey(MODULE)
					&& modules.get(MODULE) != null) {
				module = modules.get(MODULE);
				sessionId = patientSchedule.getSchedule().get(module).getModule();
			}
			List<String> results = dao.getEmotionsActivityInstance(emotion.toLowerCase(), intensityVal, sessionId);
			if(results == null || results.size() == 0)
				return "";

			StringWriter writer = new StringWriter();
			JsonGenerator generator = new JsonFactory().createGenerator(writer);
			generator.setCodec(new ObjectMapper());
			generator.writeStartObject();
			generator.writeObjectField("activities", results);
			generator.writeEndObject();

			generator.close();
			String emotionsActivities = writer.toString();
			writer.close();
			
			return emotionsActivities;

		} catch (Exception e) {

			ServerExceptionLogger exceptionLogger = new ServerExceptionLogger();
			int lineNumber = e.getStackTrace()[0].getLineNumber(); 
			String classOfException = e.toString();
			String nameOfClass = this.getClass().getSimpleName();
			String nameOfMethod = e.getStackTrace()[0].getMethodName();
			exceptionLogger.logServerException(lineNumber, classOfException, nameOfClass, 
					nameOfMethod, patientPin,INTERNAL_ERROR_CODE);

			return null;
		}
	}

	@Override
	public ActivityInstance getActivityInstance(String activityInstanceId) {
		Integer ppin =-1;
		try {
			DAO dao = DAOFactory.getTheDAO();
			ActivityInstance instance = dao.getActivityInstance(activityInstanceId);

			// Code to log state of activity instance in the Mongo...

			String trialTitle = TRIAL_NAME; // Refactor : needs to be done in a better way...
			SimpleDateFormat timeStampFormat = new SimpleDateFormat("MM.dd.YYYY HH:mm:ss", Locale.US);
			String date = timeStampFormat.format(new Date());
			ppin = instance.getPatientPin();
			String metaData = "{ \"activityInstanceId :\" \"" + instance.getActivityInstanceId() + "\" , \"ACTIVITY_INSTANCE_STATE\" : \"" + ActivityInstanceStatus.IN_EXECUTION.status() + "\" } ";
			Logger log = new Logger(dao.getTrialIdByTitle(trialTitle), date, "INFO", "ACTIVITY_INSTANCE_STATE", "JSON",
					instance.getInstanceOf().getName(), ppin.toString(), metaData);

			ArrayList<Logger> al = new ArrayList<Logger>();
			al.add(log);
			Logger[] logs = new Logger[al.size()];

			logs = al.toArray(logs);
			dao.logMessage(logs);

			return instance;
		} catch (Exception e) {

			ServerExceptionLogger exceptionLogger = new ServerExceptionLogger();
			int lineNumber = e.getStackTrace()[0].getLineNumber(); 
			String classOfException = e.toString();
			String nameOfClass = this.getClass().getSimpleName();
			String nameOfMethod = e.getStackTrace()[0].getMethodName();
			exceptionLogger.logServerException(lineNumber, classOfException, nameOfClass,
					nameOfMethod, ppin,INTERNAL_ERROR_CODE);
			return null;
		}
	}

	@Override
	public ActivityInstance createActivityInstance(ActivityInstance activityInstance) {
		try {
			DAO dao = DAOFactory.getTheDAO();
			if (activityInstance.getCreatedAt() == null) activityInstance.setCreatedAt(new Date());
			if (activityInstance.getState() == null) activityInstance.setState(ActivityInstanceStatus.CREATED.status());
			if (activityInstance.getUpdatedAt() == null) activityInstance.setUpdatedAt(new Date());

			HashMap<String,Integer> mapCount = getCurrentAndToBeDoneCount(activityInstance.getPatientPin(),
					activityInstance.getInstanceOf().getName());
			int currentCount = mapCount.get(ActivityInstance.CURRENT_COUNT);
			int toBeDoneCount = mapCount.get(ActivityInstance.TO_BE_DONE_COUNT);

			if (activityInstance.getInstanceOf().getName().equals("MakeBelieve")) { //todo need a more elegant way of making the check whether it is of type make believe
				activityInstance = new MakeBelieveActivityInstance(
						activityInstance.getActivityInstanceId(),
						activityInstance.getCreatedAt(), activityInstance.getUpdatedAt(),
						activityInstance.getDescription(), activityInstance.getStartTime(), activityInstance.getEndTime(),
						activityInstance.getUserSubmissionTime(), activityInstance.getActualSubmissionTime(),
						activityInstance.getInstanceOf(), activityInstance.getState(),
						activityInstance.getPatientPin(), dao.getMakeBelieveSituation(),
						activityInstance.getActivityGlowing(),currentCount,toBeDoneCount);
			} else if (activityInstance.getInstanceOf().getName().equals("FaceIt")) {
				activityInstance = new FaceitActivityInstance(
						activityInstance.getActivityInstanceId(),
						activityInstance.getCreatedAt(), activityInstance.getUpdatedAt(),
						activityInstance.getDescription(), activityInstance.getStartTime(), activityInstance.getEndTime(),
						activityInstance.getUserSubmissionTime(), activityInstance.getActualSubmissionTime(),
						activityInstance.getInstanceOf(), activityInstance.getState(),
						activityInstance.getPatientPin(), dao.getFaceItChallenges(),
						activityInstance.getActivityGlowing(), currentCount,toBeDoneCount);
			} else if (activityInstance.getInstanceOf().getName().equals("DailyDiary")) {
				if(isLastDayOfModule(activityInstance.getPatientPin())) {
					activityInstance = new DailyDiaryActivityInstance(
							activityInstance.getActivityInstanceId(),
							activityInstance.getCreatedAt(), activityInstance.getUpdatedAt(),
							activityInstance.getDescription(), activityInstance.getStartTime(), activityInstance.getEndTime(),
							activityInstance.getUserSubmissionTime(), activityInstance.getActualSubmissionTime(),
							activityInstance.getInstanceOf(), activityInstance.getState(),
							activityInstance.getPatientPin(), activityInstance.getActivityGlowing(),currentCount,toBeDoneCount,
							null,0,null,null,dao.getSUDSQuestion());
				}else {
					activityInstance = new DailyDiaryActivityInstance(
							activityInstance.getActivityInstanceId(),
							activityInstance.getCreatedAt(), activityInstance.getUpdatedAt(),
							activityInstance.getDescription(), activityInstance.getStartTime(), activityInstance.getEndTime(),
							activityInstance.getUserSubmissionTime(), activityInstance.getActualSubmissionTime(),
							activityInstance.getInstanceOf(), activityInstance.getState(),
							activityInstance.getPatientPin(), activityInstance.getActivityGlowing(),currentCount,
							toBeDoneCount,null,0,null,null,null);
				}
			} else if (activityInstance.getInstanceOf().getName().equals("STOP")) {
				activityInstance = new SwapActivityInstance(activityInstance.getActivityInstanceId(),
						activityInstance.getCreatedAt(), activityInstance.getUpdatedAt(),
						activityInstance.getDescription(), activityInstance.getStartTime(), activityInstance.getEndTime(),
						activityInstance.getUserSubmissionTime(), activityInstance.getActualSubmissionTime(),
						activityInstance.getInstanceOf(), activityInstance.getState(),
						activityInstance.getPatientPin(), activityInstance.getActivityGlowing(),
						currentCount,toBeDoneCount);
			} else if (activityInstance.getInstanceOf().getName().equals("WorryHeads")) {
				activityInstance = new WorryHeadsActivityInstance(
						activityInstance.getActivityInstanceId(),
						activityInstance.getCreatedAt(), activityInstance.getUpdatedAt(),
						activityInstance.getDescription(), activityInstance.getStartTime(), activityInstance.getEndTime(),
						activityInstance.getUserSubmissionTime(), activityInstance.getActualSubmissionTime(),
						activityInstance.getInstanceOf(), activityInstance.getState(),
						activityInstance.getPatientPin(), dao.getAllWorryHeadsSituations(),
						activityInstance.getActivityGlowing(),currentCount,toBeDoneCount);
			} else if (activityInstance.getInstanceOf().getName().equals("StandUp")) {
				activityInstance = new StandUpActivityInstance(
						activityInstance.getActivityInstanceId(),
						activityInstance.getCreatedAt(), activityInstance.getUpdatedAt(),
						activityInstance.getDescription(), activityInstance.getStartTime(), activityInstance.getEndTime(),
						activityInstance.getUserSubmissionTime(), activityInstance.getActualSubmissionTime(),
						activityInstance.getInstanceOf(), activityInstance.getState(),
						activityInstance.getPatientPin(), dao.getStandUpSituations(), 
						activityInstance.getActivityGlowing(),currentCount,toBeDoneCount);
			}
			ActivityInstance newActivityInstance = dao.createActivityInstance(activityInstance);

			try {
				// Code to add AI in patient schedule
				String activityInstanceId = newActivityInstance.getActivityInstanceId();
				Integer patientPin = newActivityInstance.getPatientPin();
				PatientScheduleJSON patientSchedule = dao.getSchedule(patientPin);
				if (patientSchedule != null) {
					Date today = new Date();
					HashMap<String, Integer> map = getModuleAndDay(patientSchedule, today);
					Integer module =-1, dayOfModule =-1, moduleLen =-1;
					if(map != null && map.size() > 0) {
						if (map.containsKey(MODULE) && map.get(MODULE) != null)
							module = map.get(MODULE);
						if (map.containsKey(DAY) && map.get(DAY) != null)
							dayOfModule=map.get(DAY);
						if (map.containsKey(MODULE_LENGTH) && map.get(MODULE_LENGTH) != null)
							moduleLen = map.get(MODULE_LENGTH);
					}
					if(module != -1 && patientSchedule != null
							&& patientSchedule.getSchedule() != null
							&& patientSchedule.getSchedule().size() > module) {
						ScheduleArrayJSON schedule = patientSchedule.getSchedule().get(module).getSchedule().get(dayOfModule);
						ArrayList<ActivityScheduleJSON> actList = schedule.getActivitySchedule();
						if (actList.size() > 0) {
							int indexOfActivity = -1;
							for (ActivityScheduleJSON obj : actList) {
								indexOfActivity++;
								if (obj.getActivity().equals(newActivityInstance.getInstanceOf().getName())) {
									if (dao.updateActivityInstanceInPatientSchedule(patientPin, module, dayOfModule, indexOfActivity, activityInstanceId)) {
										break;
									} else {
										System.out.println("AI update is failed in schedule");
									}

								}
							}
						}
					}else {
						// do nothing
					}
					map=null;
				} else {
				}
				mapCount = null;
				
				
			} catch (Exception e) {

				ServerExceptionLogger exceptionLogger = new ServerExceptionLogger();
				int lineNumber = e.getStackTrace()[0].getLineNumber(); 
				String classOfException = e.toString();
				String nameOfClass = this.getClass().getSimpleName();
				String nameOfMethod = e.getStackTrace()[0].getMethodName();
				exceptionLogger.logServerException(lineNumber, classOfException,
						nameOfClass, nameOfMethod, activityInstance.getPatientPin(),INTERNAL_ERROR_CODE);

			} finally {
				// Code to log state of activity instance in the Mongo...

				String trialTitle = TRIAL_NAME; // Refactor : needs to be done in a better way...
				SimpleDateFormat timeStampFormat = new SimpleDateFormat("MM.dd.YYYY HH:mm:ss", Locale.US);
				String date = timeStampFormat.format(new Date());
				Integer ppin = newActivityInstance.getPatientPin();
				String metaData = "{ \"activityInstanceId :\" \"" + activityInstance.getActivityInstanceId() + "\" , " +
						"\"ACTIVITY_INSTANCE_STATE\" : \"" + ActivityInstanceStatus.CREATED.status() + "\" } ";
				Logger log = new Logger(dao.getTrialIdByTitle(trialTitle), date, "INFO", "ACTIVITY_INSTANCE_STATE",
						"JSON", activityInstance.getInstanceOf().getName(), ppin.toString(), metaData);

				ArrayList<Logger> al = new ArrayList<Logger>();
				al.add(log);
				Logger[] logs = new Logger[al.size()];

				logs = al.toArray(logs);
				dao.logMessage(logs);

				return newActivityInstance;
			}


		} catch (Exception e) {

			ServerExceptionLogger exceptionLogger = new ServerExceptionLogger();
			int lineNumber = e.getStackTrace()[0].getLineNumber(); 
			String classOfException = e.toString();
			String nameOfClass = this.getClass().getSimpleName();
			String nameOfMethod = e.getStackTrace()[0].getMethodName();
			exceptionLogger.logServerException(lineNumber, classOfException,
					nameOfClass, nameOfMethod, activityInstance.getPatientPin(),INTERNAL_ERROR_CODE);

			return null;

		}
	}

	@Override
	public ActivityInstance updateActivityInstance(String requestBody) {
		Integer patientPin = -1;
		try {
			DAO dao = DAOFactory.getTheDAO();
			ObjectMapper mapper = new ObjectMapper();
			SimpleDateFormat format = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss");
			mapper.setDateFormat(format);

			JsonNode activityInstanceAsTree = mapper.readTree(requestBody);
			String activityInstanceType = activityInstanceAsTree.get("instanceOf").get("name").asText();

			ActivityInstance instance;
			if (activityInstanceType.equals("MakeBelieve")) { // todo Need to find a more elegant way to do this
				instance = mapper.readValue(requestBody, MakeBelieveActivityInstance.class);
				instance.setUpdatedAt(new Date());

			} else if (activityInstanceType.equals("FaceIt")) {
				instance = mapper.readValue(requestBody, FaceitActivityInstance.class);

				//List<FaceItModel> faceItList=faceItInstance.getFaceItChallenges();
				//if the size of the faceItList is more than one then that means the payload is improper 
				//and the error needs to be handled
				if (dao.updateFaceitActivityInstance(instance)) {
					return instance;
				}
				return NullObjects.getNullActivityInstance();
			} else if (activityInstanceType.equals("DailyDiary")) {
				instance = mapper.readValue(requestBody, DailyDiaryActivityInstance.class);
				instance.setUpdatedAt(new Date());
			} else if (activityInstanceType.equals("STOP")) {
				instance = mapper.readValue(requestBody, SwapActivityInstance.class);
				instance.setUpdatedAt(new Date());
			} else if (activityInstanceType.equals("WorryHeads")) {
				instance = mapper.readValue(requestBody, WorryHeadsActivityInstance.class);
				instance.setUpdatedAt(new Date());
			} else if (activityInstanceType.equals("StandUp")) {
				instance = mapper.readValue(requestBody, StandUpActivityInstance.class);
				instance.setUpdatedAt(new Date());  
			}else if(activityInstanceType.equals("Emotion")){
				instance = mapper.readValue(requestBody, Emotions.class);
				instance.setUpdatedAt(new Date());  
			}else{
				instance  = mapper.readValue(requestBody, ActivityInstance.class);
				instance.setUpdatedAt(new Date());      
			}
			instance.setUserSubmissionTime(new Date());
			patientPin = instance.getPatientPin();
			// Code to log state of activity instance in the Mongo...

			String trialTitle = TRIAL_NAME; // Refactor : needs to be done in a better way...
			SimpleDateFormat timeStampFormat = new SimpleDateFormat("MM.dd.YYYY HH:mm:ss", Locale.US);
			String date = timeStampFormat.format(new Date());
			Integer ppin = instance.getPatientPin();
			String metaData = "{ \"activityInstanceId :\" \"" + instance.getActivityInstanceId() + "\" , \"ACTIVITY_INSTANCE_STATE\" : \"" + instance.getState() + "\" } ";
			Logger log = new Logger(dao.getTrialIdByTitle(trialTitle), date, "INFO", "ACTIVITY_INSTANCE_STATE", "JSON",
					instance.getInstanceOf().getName(), ppin.toString(), metaData);

			ArrayList<Logger> al = new ArrayList<Logger>();
			al.add(log);
			Logger[] logs = new Logger[al.size()];

			logs = al.toArray(logs);
			dao.logMessage(logs);

			if (dao.updateActivityInstance(instance)) {
				//
				if(instance.getState().equals(ActivityInstanceStatus.COMPLETED.status())) {

					PatientScheduleJSON patientSchedule = dao.getSchedule(instance.getPatientPin());
					HashMap<String, Integer> modules = getModuleAndDay(patientSchedule, new Date());
					int module = -1;
					int days = -1;
					if (modules != null && modules.size() > 0) {
						if (modules.containsKey(MODULE) && modules.get(MODULE) != null)
							module = modules.get(MODULE);
						if (modules.containsKey(DAY) && modules.get(DAY) != null)
							days = modules.get(DAY);
					}

					if (module != -1 && patientSchedule != null
							&& patientSchedule.getSchedule() != null
							&& patientSchedule.getSchedule().size() > 0) {
						ArrayList<ActivityScheduleJSON> activityScheduleJSON = patientSchedule.getSchedule().get(module).getSchedule().get(days).getActivitySchedule();
						for (int i = 0; i < activityScheduleJSON.size(); i++) {
							String activityName = activityScheduleJSON.get(i).getActivity();
							if (instance.getInstanceOf().getName().equals(activityName)) {
								int score = activityScheduleJSON.get(i).getScore() + instance.fetchResponseCount();
								int actualCount = activityScheduleJSON.get(i).getActualCount() + 1;
								if (dao.updatePatientScoreActualCount(ppin, module, days, i, score, actualCount)) {
									System.out.println("Update sucessful");
								} else {
									System.out.println("Update failed.");
								}

							}
						}
					}

					//patientSchedule.getSchedule().get(0).getSchedule().get(0).getActivitySchedule().get(0).getScore();
					modules = null;
				}

				//
				return instance;
			}
			return NullObjects.getNullActivityInstance();
		} catch (NullPointerException e) {
			ServerExceptionLogger exceptionLogger = new ServerExceptionLogger();
			int lineNumber = e.getStackTrace()[0].getLineNumber(); 
			String classOfException = e.toString();
			String nameOfClass = this.getClass().getSimpleName();
			String nameOfMethod = e.getStackTrace()[0].getMethodName();
			exceptionLogger.logServerException(lineNumber, classOfException, nameOfClass, 
					nameOfMethod, patientPin,BAD_REQUEST_STATUS_CODE);
			return NullObjects.getNullActivityInstance();

		} catch (Exception e) {

			ServerExceptionLogger exceptionLogger = new ServerExceptionLogger();
			int lineNumber = e.getStackTrace()[0].getLineNumber(); 
			String classOfException = e.toString();
			String nameOfClass = this.getClass().getSimpleName();
			String nameOfMethod = e.getStackTrace()[0].getMethodName();
			exceptionLogger.logServerException(lineNumber, classOfException, nameOfClass, 
					nameOfMethod, patientPin,INTERNAL_ERROR_CODE);
			return null;
		}

	}

	@Override
	public ActivityInstance deleteActivityInstance(String activityInstanceId) {
		try {
			DAO dao = DAOFactory.getTheDAO();
			return dao.deleteActivityInstance(activityInstanceId);
		} catch (Exception e) {

			ServerExceptionLogger exceptionLogger = new ServerExceptionLogger();
			int lineNumber = e.getStackTrace()[0].getLineNumber(); 
			String classOfException = e.toString();
			String nameOfClass = this.getClass().getSimpleName();
			String nameOfMethod = e.getStackTrace()[0].getMethodName();
			exceptionLogger.logServerException(lineNumber, classOfException, 
					nameOfClass, nameOfMethod, -1,INTERNAL_ERROR_CODE);

			return null;
		}
	}

	/****************************************  Service methods for Domain  ********************************************/
	@Override
	public List<Domain> getDomains() {
		try {
			DAO dao = DAOFactory.getTheDAO();

			return dao.getDomains();
		} catch (Exception e) {

			ServerExceptionLogger exceptionLogger = new ServerExceptionLogger();
			int lineNumber = e.getStackTrace()[0].getLineNumber(); 
			String classOfException = e.toString();
			String nameOfClass = this.getClass().getSimpleName();
			String nameOfMethod = e.getStackTrace()[0].getMethodName();
			exceptionLogger.logServerException(lineNumber, classOfException, nameOfClass, nameOfMethod,
					-1,INTERNAL_ERROR_CODE);

			return null;
		}
	}

	@Override
	public Domain getDomain(String id) {
		try {
			DAO dao = DAOFactory.getTheDAO();

			return dao.getDomain(id);
		} catch (Exception e) {

			ServerExceptionLogger exceptionLogger = new ServerExceptionLogger();
			int lineNumber = e.getStackTrace()[0].getLineNumber(); 
			String classOfException = e.toString();
			String nameOfClass = this.getClass().getSimpleName();
			String nameOfMethod = e.getStackTrace()[0].getMethodName();
			exceptionLogger.logServerException(lineNumber, classOfException, 
					nameOfClass, nameOfMethod, -1,INTERNAL_ERROR_CODE);
			return null;
		}
	}

	@Override
	public Domain addDomain(String title, String description, String state) {

		try {
			DAO dao = DAOFactory.getTheDAO();
			Domain instance = new Domain(title, description, state);
			instance.setCreatedAt(new Date());
			if (instance.getState() == null) instance.setState(DomainState.CREATED.state());

			return dao.createDomain(instance);
		} catch (Exception e) {

			ServerExceptionLogger exceptionLogger = new ServerExceptionLogger();
			int lineNumber = e.getStackTrace()[0].getLineNumber(); 
			String classOfException = e.toString();
			String nameOfClass = this.getClass().getSimpleName();
			String nameOfMethod = e.getStackTrace()[0].getMethodName();
			exceptionLogger.logServerException(lineNumber, classOfException, nameOfClass,
					nameOfMethod, -1,INTERNAL_ERROR_CODE);

			return null;
		}
	}

	@Override
	public String addTestDomain(String title, String description, String state) {
		return null;
	}

	/****************************************  Service methods for Patient  *******************************************/
	@Override
	public List<Patient> getPatients(String trialId) {
		Integer patientPin =-1;
		try {
			DAO dao = DAOFactory.getTheDAO();
			List<Patient> result;

			if (trialId == null) {
				// return list of all patients present
				result = dao.getPatients();
			} else {
				// return list of patients for given trialId
				result = dao.getPatients(trialId);
			}
			return result;
		} catch (Exception e) {

			ServerExceptionLogger exceptionLogger = new ServerExceptionLogger();
			int lineNumber = e.getStackTrace()[0].getLineNumber(); 
			String classOfException = e.toString();
			String nameOfClass = this.getClass().getSimpleName();
			String nameOfMethod = e.getStackTrace()[0].getMethodName();
			exceptionLogger.logServerException(lineNumber, classOfException, nameOfClass,
					nameOfMethod, patientPin,INTERNAL_ERROR_CODE);

			return null;
		}
	}

	@Override
	public Patient getPatient(int patientPin) {
		try {
			DAO dao = DAOFactory.getTheDAO();
			return dao.getPatient(patientPin);
		} catch (Exception e) {

			ServerExceptionLogger exceptionLogger = new ServerExceptionLogger();
			int lineNumber = e.getStackTrace()[0].getLineNumber(); 
			String classOfException = e.toString();
			String nameOfClass = this.getClass().getSimpleName();
			String nameOfMethod = e.getStackTrace()[0].getMethodName();
			exceptionLogger.logServerException(lineNumber, classOfException, nameOfClass,
					nameOfMethod, patientPin,INTERNAL_ERROR_CODE);

			return null;
		}
	}

	@Override
	public Patient createPatient(String trialId, int patientPin) {
		try {
			DAO dao = DAOFactory.getTheDAO();
			return dao.createPatient(trialId,patientPin);
		} catch (Exception e) {

			ServerExceptionLogger exceptionLogger = new ServerExceptionLogger();
			int lineNumber = e.getStackTrace()[0].getLineNumber(); 
			String classOfException = e.toString();
			String nameOfClass = this.getClass().getSimpleName();
			String nameOfMethod = e.getStackTrace()[0].getMethodName();
			exceptionLogger.logServerException(lineNumber, classOfException, nameOfClass,
					nameOfMethod, patientPin,INTERNAL_ERROR_CODE);

			return null;
		}
	}

	@Override
	public Patient updatePatient(Patient patient) {
		try {
			DAO dao = DAOFactory.getTheDAO();
			Patient patientInDatabase = dao.getPatient(patient.getPin());
			if (patientInDatabase == null || patientInDatabase.equals(NullObjects.getNullPatient()))
				return patientInDatabase;

			patientInDatabase.setStartDate(
					patient.getStartDate() != null ? patient.getStartDate() : patientInDatabase.getStartDate());
			patientInDatabase.setEndDate(
					patient.getEndDate() != null ? patient.getEndDate() : patientInDatabase.getEndDate());
			patientInDatabase.setState(
					patient.getState() != null ? patient.getState() : patientInDatabase.getState());
			patientInDatabase.setCreatedAt(
					patient.getCreatedAt() != null ? patient.getCreatedAt() : patientInDatabase.getCreatedAt());
			patientInDatabase.setUpdatedAt(new Date());

			return dao.updatePatient(patientInDatabase);
		} catch (Exception e) {

			ServerExceptionLogger exceptionLogger = new ServerExceptionLogger();
			int lineNumber = e.getStackTrace()[0].getLineNumber(); 
			String classOfException = e.toString();
			String nameOfClass = this.getClass().getSimpleName();
			String nameOfMethod = e.getStackTrace()[0].getMethodName();
			exceptionLogger.logServerException(lineNumber, classOfException, nameOfClass,
					nameOfMethod, patient.getPin(),INTERNAL_ERROR_CODE);

			return null;
		}
	}

	@Override
	public String deletePatient(String patientPin) {
		return "DELETE PATIENT";
	}

	/****************** Patient DeviceId update ********************************/

	public Patient updatePatientDeviceId(int patientPin, String regiToken) {
		try {
			DAO dao = DAOFactory.getTheDAO();

			Patient p = getPatient(patientPin);
			p.getRegistrationToken().add(regiToken);
			return dao.updatePatient(p);
		} catch (Exception e) {

			ServerExceptionLogger exceptionLogger = new ServerExceptionLogger();
			int lineNumber = e.getStackTrace()[0].getLineNumber(); 
			String classOfException = e.toString();
			String nameOfClass = this.getClass().getSimpleName();
			String nameOfMethod = e.getStackTrace()[0].getMethodName();
			exceptionLogger.logServerException(lineNumber, classOfException, nameOfClass,
					nameOfMethod, patientPin,INTERNAL_ERROR_CODE);

			return null;
		}

	}

	/****************************************  Service methods for Trial  *********************************************/

	@Override
	public List<Trial> getTrials(String domain) {
		try {
			DAO dao = DAOFactory.getTheDAO();
			List<Trial> trials = null;

			if (domain == null)
				trials = dao.getTrials();
			else
				trials = dao.getTrials(domain);

			return trials;
		} catch (Exception e) {

			ServerExceptionLogger exceptionLogger = new ServerExceptionLogger();
			int lineNumber = e.getStackTrace()[0].getLineNumber(); 
			String classOfException = e.toString();
			String nameOfClass = this.getClass().getSimpleName();
			String nameOfMethod = e.getStackTrace()[0].getMethodName();
			exceptionLogger.logServerException(lineNumber, classOfException, nameOfClass,
					nameOfMethod, -1,INTERNAL_ERROR_CODE);

			return null;
		}
	}

	@Override
	public Trial addTrial(Trial trialInstance) {
		try {
			DAO dao = DAOFactory.getTheDAO();

			// check if the domain exist, if yes get the id of domain
			Domain domain = dao.getDomain(trialInstance.getDomainId());
			if (domain != null) {

				Date startDateFormat = new SimpleDateFormat(ReachService.DATE_FORMAT).parse(trialInstance.getStartDate().toString());
				Date endDateFormat = new SimpleDateFormat(ReachService.DATE_FORMAT).parse(trialInstance.getEndDate().toString());

				trialInstance.setUpdatedAt(new Date());
				trialInstance.setCreatedAt(new Date());
				trialInstance.setStartDate(startDateFormat);
				trialInstance.setEndDate(endDateFormat);
				trialInstance.setDomainId(domain.getDomainId());

				return dao.createTrial(trialInstance);
			} else {
				return NullObjects.getNullTrial();
			}

		} catch (Exception e) {

			ServerExceptionLogger exceptionLogger = new ServerExceptionLogger();
			int lineNumber = e.getStackTrace()[0].getLineNumber(); 
			String classOfException = e.toString();
			String nameOfClass = this.getClass().getSimpleName();
			String nameOfMethod = e.getStackTrace()[0].getMethodName();
			exceptionLogger.logServerException(lineNumber, classOfException, nameOfClass, 
					nameOfMethod, -1,INTERNAL_ERROR_CODE);

			return null;
		}
	}

	@Override
	public String getTrialIdByTitle(String trialName) {
		try {
			DAO dao = DAOFactory.getTheDAO();

			String trialId = dao.getTrialIdByTitle(trialName);
			return trialId;
		} catch (Exception e) {

			ServerExceptionLogger exceptionLogger = new ServerExceptionLogger();
			int lineNumber = e.getStackTrace()[0].getLineNumber(); 
			String classOfException = e.toString();
			String nameOfClass = this.getClass().getSimpleName();
			String nameOfMethod = e.getStackTrace()[0].getMethodName();
			exceptionLogger.logServerException(lineNumber, classOfException, nameOfClass,
					nameOfMethod, -1,INTERNAL_ERROR_CODE);

			return null;
		}
	}
	/****************************************  Service methods for Logger *********************************************/
	@Override
	public Logger[] logMessage(Logger[] loggerInstance) {
		try {
			DAO dao = DAOFactory.getTheDAO();

			Logger[] logger = dao.logMessage(loggerInstance);
			return logger;
		} catch (Exception e) {

			ServerExceptionLogger exceptionLogger = new ServerExceptionLogger();
			int lineNumber = e.getStackTrace()[0].getLineNumber(); 
			String classOfException = e.toString();
			String nameOfClass = this.getClass().getSimpleName();
			String nameOfMethod = e.getStackTrace()[0].getMethodName();
			exceptionLogger.logServerException(lineNumber, classOfException, nameOfClass,
					nameOfMethod, -1,INTERNAL_ERROR_CODE);

			return null;
		}
	}

	@Override
	public Logger[] logPersonalizationMessage(Logger[] loggerInstance) {
		try {
			DAO dao = DAOFactory.getTheDAO();

			Logger[] logger = dao.logPersonalizationMessage(loggerInstance);
			return logger;
		} catch (Exception e) {

			ServerExceptionLogger exceptionLogger = new ServerExceptionLogger();
			int lineNumber = e.getStackTrace()[0].getLineNumber(); 
			String classOfException = e.toString();
			String nameOfClass = this.getClass().getSimpleName();
			String nameOfMethod = e.getStackTrace()[0].getMethodName();
			exceptionLogger.logServerException(lineNumber, classOfException, nameOfClass, 
					nameOfMethod, -1,INTERNAL_ERROR_CODE);

			return null;
		}
	}


	/****************************************  Personalization methods  *************************************************/

	@Override
	public boolean personalizeUserExperience(int patientPin) {
		//Fetch schedule for passed pin

		// check available time and current time match?

		// if available time = current time
		// check status of completion and decide type of notification to be sent
		// check if notification already sent
		// if not, then send it
		boolean rval = false;
		try {
			DAO dao = DAOFactory.getTheDAO();
			PatientScheduleJSON patientScheduleJSON = dao.getSchedule(patientPin);
			if(patientScheduleJSON == null
					|| patientScheduleJSON.getSchedule() == null
					|| patientScheduleJSON.getSchedule().size() == 0) {
				return false;
			}


			Integer module =-1;
			Integer dayOfModule =-1;
			Integer moduleLen=0;
			// HashMap to store activity and its adherence till today
			HashMap<String,Double> adherenceActivityMap = new HashMap<>();

			Date today = new Date();//new SimpleDateFormat(ReachService.DATE_FORMAT).parse(.toString());
			DateFormat dateFormat = new SimpleDateFormat("HH");
			Integer currHour = Integer.valueOf(dateFormat.format(today));


			// create method  to get module and day of module - done
			HashMap<String, Integer> map = this.getModuleAndDay(patientScheduleJSON,today);

			if(map != null && map.size() > 0) {
				if (map.containsKey(MODULE) && map.get(MODULE) != null)
					module = map.get(MODULE);
				if (map.containsKey(DAY) && map.get(DAY) != null)
					dayOfModule = map.get(DAY);
				if (map.containsKey(MODULE_LENGTH) && map.get(MODULE_LENGTH) != null)
					moduleLen=map.get(MODULE_LENGTH);
			}
			if(module ==-1) {
				rval = false;// No module selected so no trials for this patient pin
			}else {

				ArrayList<ScheduleArrayJSON> schedule = patientScheduleJSON.getSchedule()
						.get(module).getSchedule();

				//get previous day
				int prevDay = dayOfModule -1 ;
				int prevDayModule = module;
				ArrayList<ActivityScheduleJSON> prevDayActivityList = null;
				if (prevDay == -1) {
					prevDayModule --;
					if(prevDayModule < 0) {
						Logger log;
						String trialTitle = TRIAL_NAME; // Refactor : needs to be done in a better way...
						SimpleDateFormat timeStampFormat = new SimpleDateFormat("MM.dd.YYYY HH:mm:ss", Locale.US);
						String date = timeStampFormat.format(new Date());
						Integer ppin = Integer.valueOf(patientPin);
						String metaData ="";
						String type ="PERSONALIZATION";
						String format = "JSON";
						String subType = "UX";
						String level="NOT DONE";
						log = new Logger(dao.getTrialIdByTitle(trialTitle),date,level,
								type, format, subType, ppin.toString(), metaData);
						ArrayList<Logger> al = new ArrayList<Logger>();
						al.add(log);
						Logger[] logs = new Logger[al.size()];

						logs = al.toArray(logs);
						dao.logPersonalizationMessage(logs);

						return true;
					}
					ArrayList<ScheduleArrayJSON> prevDaySchedule = patientScheduleJSON.getSchedule().
							get(prevDayModule).getSchedule();
					prevDay = prevDaySchedule.size()-1;
					prevDayActivityList = patientScheduleJSON.getSchedule()
							.get(prevDayModule).getSchedule().get(prevDay).getActivitySchedule();

				} else {
					prevDayActivityList = patientScheduleJSON.getSchedule()
							.get(prevDayModule).getSchedule().get(prevDay).getActivitySchedule();
				}


				boolean isPending = false;
				// get schedule array for specific day
				ArrayList<ActivityScheduleJSON> activityList = schedule.get(dayOfModule).getActivitySchedule();
				for(ActivityScheduleJSON activityJson : activityList) {
					if (activityJson.getActualCount() < activityJson.getMinimumCount()) {
						isPending = true;
					}
				}

				if (!isPending) {
					Logger log;
					String trialTitle = TRIAL_NAME; // Refactor : needs to be done in a better way...
					SimpleDateFormat timeStampFormat = new SimpleDateFormat("MM.dd.YYYY HH:mm:ss", Locale.US);
					String date = timeStampFormat.format(new Date());
					Integer ppin = Integer.valueOf(patientPin);
					String metaData ="";
					String type ="PERSONALIZATION";
					String format = "JSON";
					String subType = "UX";
					String level="NOTHING PENDING";
					log = new Logger(dao.getTrialIdByTitle(trialTitle),date,level,
							type, format, subType, ppin.toString(), metaData);
					ArrayList<Logger> al = new ArrayList<Logger>();
					al.add(log);
					Logger[] logs = new Logger[al.size()];

					logs = al.toArray(logs);
					dao.logPersonalizationMessage(logs);

					return true;
				}


				for(ActivityScheduleJSON activityJson : prevDayActivityList) {
					double value = getAverageLevelOfAdherenceOfActivity(patientScheduleJSON, 
							prevDayModule, prevDay, activityJson.getActivity());
					adherenceActivityMap.put(activityJson.getActivity(),value);
				}

				int averageLevelOfPersonalization;
				Double totalAverageAdherence =100.0;
				if(!adherenceActivityMap.isEmpty()) {
					totalAverageAdherence = 
							(adherenceActivityMap.values().stream()
									.mapToDouble(Double :: doubleValue).sum())/adherenceActivityMap.size();
				}

				averageLevelOfPersonalization = convertTotalAverageAdherenceToLevel(totalAverageAdherence);

				// setting average level of personalization for this day.

				dao.updateAverageLevelOfPersonalization(patientPin, module, dayOfModule, averageLevelOfPersonalization);

				double l1_threshold = Double.parseDouble(_properties.getProperty(LEVEL_1_ADHERENCE_THRESHOLD));
				double l2_threshold = Double.parseDouble(_properties.getProperty(LEVEL_2_ADHERENCE_THRESHOLD));

				List<String> levelOneActivity = new ArrayList<>();
				List<String> levelTwoActivity = new ArrayList<>();

				for(String activityName : adherenceActivityMap.keySet()) {
					int dayBeforeLevelOfPersonalization = getDayBeforeLevelOfPersonalization(patientScheduleJSON,
							module, dayOfModule-1,activityName);
					if(dayBeforeLevelOfPersonalization == -1)
						continue;

					if(averageLevelOfPersonalization ==2 && adherenceActivityMap.get(activityName) <= l2_threshold) {
						if(dayBeforeLevelOfPersonalization == 0) 
							levelOneActivity.add(activityName);
						else
							levelTwoActivity.add(activityName);
					}else if(averageLevelOfPersonalization==1 && adherenceActivityMap.get(activityName) <= l1_threshold) {
						if(dayBeforeLevelOfPersonalization != 2)
							levelOneActivity.add(activityName);
					}
				}
				ArrayList<String> levelTwoNotifActivities = new ArrayList<>();
				ArrayList<String> levelOneNotifActivities = new ArrayList<>();

				for(ActivityScheduleJSON activity : activityList) {
					ArrayList<AvailableTime> time = activity.getAvailableTime();

					if(activity.getActualCount() >= activity.getMinimumCount())
						continue;
					// Check if cutrrent time is in available time
					for(AvailableTime t : time) {

						if(currHour >= t.getFrom() && currHour <= t.getTo()) {
							// Check if L1 or L2 notif needs to be sent
							if(activity.getLevelOfUIPersonalization() < 1 
									&& levelOneActivity.contains(activity.getActivity())) {
								levelOneNotifActivities.add(activity.getActivity());
							}else if(activity.getLevelOfUIPersonalization() < 2 
									&& levelTwoActivity.contains(activity.getActivity())) {
								levelTwoNotifActivities.add(activity.getActivity());

							}
						}
					}
				}

				if(!levelOneNotifActivities.isEmpty()) {
					sendLevelOneNotification(patientPin, module,
							dayOfModule,levelOneNotifActivities);
				}
				if(!levelTwoNotifActivities.isEmpty()) {
					sendLevelTwoNotification(patientPin, module, dayOfModule, levelTwoNotifActivities);
				}
			}
			rval=true;
		} catch (RuntimeException exception) {
			rval = false;
			ServerExceptionLogger exceptionLogger = new ServerExceptionLogger();
			int lineNumber = exception.getStackTrace()[0].getLineNumber(); 
			String classOfException = exception.toString();
			String nameOfClass = this.getClass().getSimpleName();
			String nameOfMethod = exception.getStackTrace()[0].getMethodName();
			exceptionLogger.logServerException(lineNumber, classOfException, nameOfClass, 
					nameOfMethod, patientPin,INTERNAL_ERROR_CODE);

			//runtimeException.printStackTrace();
		} catch (Exception exception) {
			rval=false;

			ServerExceptionLogger exceptionLogger = new ServerExceptionLogger();
			int lineNumber = exception.getStackTrace()[0].getLineNumber(); 
			String classOfException = exception.toString();
			String nameOfClass = this.getClass().getSimpleName();
			String nameOfMethod = exception.getStackTrace()[0].getMethodName();
			exceptionLogger.logServerException(lineNumber, classOfException, nameOfClass, 
					nameOfMethod, patientPin,INTERNAL_ERROR_CODE);

			//exception.printStackTrace();
		}
		return rval;
	}

	public boolean sendLevelOneNotification(int patientPin,int module, int dayOfModule,
			List<String> list) {
		boolean rval = false;
		try {
			boolean sudsConfig =false;
			if(isLastDayOfModule(patientPin))
				sudsConfig = true;
			String l1_class = _properties.getProperty("level_1.className");
			INotificationInterface notificationClass = null;

			Logger log;
			String trialTitle = TRIAL_NAME; // Refactor : needs to be done in a better way...
			SimpleDateFormat timeStampFormat = new SimpleDateFormat("MM.dd.YYYY HH:mm:ss", Locale.US);
			String date = timeStampFormat.format(new Date());
			Integer ppin = Integer.valueOf(patientPin);
			String metaData ="";
			String level="";
			String activityVal = "ACTIVITY_NAME";
			String levelOfUXP= "LEVEL_OF_PERSONALIZATION";
			String type ="PERSONALIZATION";
			String format = "JSON";
			String subType = "UX";
			String activityName = "notifOneLandingPage";

			DAO dao = DAOFactory.getTheDAO();

			// Level 1 notification
			if (l1_class != null) {
				Class<?> level_1 = Class.forName(l1_class);
				Constructor<?> constructor = level_1.getConstructor();
				notificationClass = (INotificationInterface) constructor.newInstance();
			}

			if(notificationClass != null) {

				if(notificationClass.sendNotification(activityName,module+1, 
						patientPin,0, 1, list,sudsConfig)) {

					rval = true;
					metaData = "{ \""+activityVal+ "\": \""+activityName+"\","
							+ "\""+levelOfUXP+"\" : \"1\" ,"
							+ "\"activityList\" : " + list.toString()
							+ "\"sudsConfig\" : " + sudsConfig
							+"\" } ";
					level="SENT";
					// Updating level of UI personalization in schedule
					if(dao.updateLvlOneTwoUIPersonalization(patientPin, module, dayOfModule,list,1))
						System.out.println("Update successful");
					else
						System.out.println("Update failed.");	//May need to do something here. Also, add to logs - Vishakha
				} else {
					metaData = "{ \"" + activityVal + "\": \"" + activityName + "\","
							+ "\"" + levelOfUXP + "\" : \"1\" ,"
							+ "\"activityList\" : " + list.toString()
							+ "\"sudsConfig\" : " + sudsConfig
							+ "\" } ";
					level = "NOT_SENT";

				}

			}
			else {
				metaData = "{ \"" + activityVal + "\": \"" + activityName + "\","
						+ "\"" + levelOfUXP + "\" : \"1\" ,"
						+ "\"activityList\" : " + list.toString()
						+ "\"sudsConfig\" : " + sudsConfig
						+ "\" } ";
				level = "NOT_SENT";

			}
			if(!metaData.equals("")) {
				log = new Logger(dao.getTrialIdByTitle(trialTitle),date,level,
						type, format, subType, ppin.toString(), metaData);
				ArrayList<Logger> al = new ArrayList<Logger>();
				al.add(log);
				Logger[] logs = new Logger[al.size()];

				logs = al.toArray(logs);
				dao.logPersonalizationMessage(logs);
			}
		}catch(Exception e) {

			ServerExceptionLogger exceptionLogger = new ServerExceptionLogger();
			int lineNumber = e.getStackTrace()[0].getLineNumber(); 
			String classOfException = e.toString();
			String nameOfClass = this.getClass().getSimpleName();
			String nameOfMethod = e.getStackTrace()[0].getMethodName();
			exceptionLogger.logServerException(lineNumber, classOfException, nameOfClass,
					nameOfMethod, patientPin,INTERNAL_ERROR_CODE);

		}
		return rval;
	}

	public boolean sendLevelTwoNotification(int patientPin,int module, int dayOfModule,
			List<String> list) {
		boolean rval = false;
		try {
			boolean sudsConfig =false;
			if(isLastDayOfModule(patientPin))
				sudsConfig = true;
			Logger log;
			String trialTitle = TRIAL_NAME; // Refactor : needs to be done in a better way...
			SimpleDateFormat timeStampFormat = new SimpleDateFormat("MM.dd.YYYY HH:mm:ss", Locale.US);
			String date = timeStampFormat.format(new Date());
			Integer ppin = Integer.valueOf(patientPin);
			String metaData ="";
			String level="";
			String activityVal = "ACTIVITY_NAME";
			String levelOfUXP= "LEVEL_OF_PERSONALIZATION";
			String type ="PERSONALIZATION";
			String format = "JSON";
			String subType = "UX";

			String l2_class = _properties.getProperty("level_2.className");
			INotificationInterface notificationClass = null;
			DAO dao = DAOFactory.getTheDAO();
			String activityName = "notifTwoLandingPage";

			// Level 2
			if (l2_class != null) {
				Class<?> level_2 = Class.forName(l2_class);
				Constructor<?> constructor = level_2.getConstructor();
				notificationClass = (INotificationInterface) constructor.newInstance();
				//				}
				if(notificationClass != null) {
					if(notificationClass.sendNotification(activityName, module+1,
							patientPin, 0, 2, list,sudsConfig)) {
						rval =true;
						metaData = "{ \""+activityVal+ "\": \""+activityName+"\","
								+ "\""+levelOfUXP+"\" : \"2\" ,"
								+ "\"activityList\" : " + list.toString()
								+ "\"sudsConfig\" : " + sudsConfig
								+"\" } ";
						level="SENT";
						// Updating level of UI personalization in schedule
						// For update we have to do iteratively.
						//	activity.setLevelOfUIPersonalization(2);

						if(dao.updateLvlOneTwoUIPersonalization(patientPin, module, dayOfModule,list,2))
							System.out.println("Update successful");
						else 
							System.out.println("Update failed.");
					}

				}
				else {
					metaData = "{ \""+activityVal+ "\": \""+activityName+"\","
							+ "\""+levelOfUXP+"\" : \"2\" ,"
							+ "\"activityList\" : " + list.toString()
							+ "\"sudsConfig\" : " + sudsConfig
							+"\" } ";
					level="NOT_SENT";
				}
			}
			else {
				// Do nothing
			}
			if(!metaData.equals("")) {
				log = new Logger(dao.getTrialIdByTitle(trialTitle),date,level,
						type, format, subType, ppin.toString(), metaData);
				ArrayList<Logger> al = new ArrayList<Logger>();
				al.add(log);
				Logger[] logs = new Logger[al.size()];

				logs = al.toArray(logs);
				dao.logPersonalizationMessage(logs);
			}
		}catch(Exception e) {

			ServerExceptionLogger exceptionLogger = new ServerExceptionLogger();
			int lineNumber = e.getStackTrace()[0].getLineNumber(); 
			String classOfException = e.toString();
			String nameOfClass = this.getClass().getSimpleName();
			String nameOfMethod = e.getStackTrace()[0].getMethodName();
			exceptionLogger.logServerException(lineNumber, classOfException, nameOfClass,
					nameOfMethod, patientPin,INTERNAL_ERROR_CODE);

		}
		return rval;
	}

	private double getAverageLevelOfAdherenceOfActivity(PatientScheduleJSON patientScheduleJSON,
			int module, int dayOfModule,String activityName) {
		int	avgSkillForDays= Integer.parseInt(_properties.getProperty(AVERAGE_UI_FOR_DAYS));
		int totalActualCount =0;
		int totalMinCount =0;
		int loopCount =-1;

		// Create a flag varibale. flag = true means we have value in properties file > 0 
		//or else it remains false (to calculate average so far)

		boolean flag =false;
		if(avgSkillForDays < 0) {
			loopCount = module;
		}
		else {
			loopCount = avgSkillForDays - 1; // going till 0
			flag=true;
		}
		while(loopCount >=0) {
			ActivityScheduleJSON result = getActivityInDayAndModule(patientScheduleJSON, module, dayOfModule, activityName);

			if(result != null) {
				totalActualCount += result.getActualCountLimitToMinCount();
				totalMinCount+= result.getMinimumCount();
			}else {
				break;
			}
			dayOfModule--;
			if(dayOfModule == -1) {
				if(!flag)
					loopCount--;
				module--;
				if(module < 0)
					break;
				ArrayList<ScheduleArrayJSON> schedule=patientScheduleJSON.getSchedule()
						.get(module).getSchedule();
				dayOfModule = schedule.size()-1;	
			}
			if(flag)
				loopCount--;
		}
		// If the the total minimum count is zero i.e. if it is for module 1 we return 100% adherence.
		if(totalMinCount ==0)
			return 100;
		else
			return ((totalActualCount/totalMinCount)*100);

	}

	private ActivityScheduleJSON getActivityInDayAndModule(PatientScheduleJSON patientScheduleJSON,
			int module, int dayOfModule,String activityName) {
		ArrayList<ScheduleArrayJSON> schedule = patientScheduleJSON.getSchedule()
				.get(module).getSchedule();
		// get schedule array for specific day
		ArrayList<ActivityScheduleJSON> activityList = schedule.get(dayOfModule).getActivitySchedule();

		// Getting object with activity name as activityName from above list - needs JRE 1.8 to execute
		// return null if no activity found
		return activityList.stream()            // Convert to steam
				.filter(x -> activityName.equals(x.getActivity()))   
				.findAny()                                     		 // If 'findAny' then return found
				.orElse(null); 

	}

	private int getDayBeforeLevelOfPersonalization(PatientScheduleJSON patientScheduleJSON,
			int module, int dayOfModule,String activityName) {
		if(dayOfModule == -1) {
			module--;
			if(module < 0)
				return -1;
			ArrayList<ScheduleArrayJSON> schedule=patientScheduleJSON.getSchedule()
					.get(module).getSchedule();
			dayOfModule = schedule.size()-1;	
		}
		ActivityScheduleJSON result = getActivityInDayAndModule(patientScheduleJSON, module, dayOfModule, activityName);
		if(result == null)
			return -1;
		else
			return result.getLevelOfUIPersonalization();

	}

	private int convertTotalAverageAdherenceToLevel(Double totalAverageAdherence){
		double l1_threshold = Double.parseDouble(_properties.getProperty(LEVEL_1_ADHERENCE_THRESHOLD));
		double l2_threshold = Double.parseDouble(_properties.getProperty(LEVEL_2_ADHERENCE_THRESHOLD));

		if(totalAverageAdherence <= l2_threshold)
			return 2;
		else if(totalAverageAdherence > l2_threshold && totalAverageAdherence <= l1_threshold)
			return 1;
		else
			return 0;
	}

	private int getTotalActualCountOfActivity(ArrayList<ScheduleArrayJSON> schedule, String activity, int dayOfModule) {

		int counter = dayOfModule;
		int totalActualCount =0;
		while(counter >= 0) {
			ScheduleArrayJSON obj = schedule.get(counter);
			ArrayList<ActivityScheduleJSON> act = obj.getActivitySchedule();
			for(ActivityScheduleJSON temp : act) {
				if(temp.getActivity().equals(activity)) {
					totalActualCount += temp.getActualCount();
				}
			}
			counter--;
		}
		return totalActualCount;

	}

	private int getNotDoneDays(ArrayList<ScheduleArrayJSON> schedule, String activity, int dayOfModule) {

		int counter = dayOfModule;
		int rval =0;
		while(counter >= 0) {
			ScheduleArrayJSON obj = schedule.get(counter);
			ArrayList<ActivityScheduleJSON> act = obj.getActivitySchedule();
			for(ActivityScheduleJSON temp : act) {
				if(temp.getActivity().equals(activity)) {
					if(temp.getActualCount() >= temp.getMinimumCount()) {
						return rval;
					}else if(temp.getActualCount() < temp.getMinimumCount()) {
						rval++;
					}
				}
			}
			counter--;
		}
		return rval;
	}

	private HashMap<String, Integer> getModuleAndDay(PatientScheduleJSON patientScheduleJSON, Date today) {

		if (patientScheduleJSON == null
				|| patientScheduleJSON.getSchedule() == null
				|| patientScheduleJSON.getSchedule().size() == 0) {
			return  new HashMap<>();
		}

		ArrayList<ModuleJSON> moduleJson = patientScheduleJSON.getSchedule();

		HashMap<String, Integer> rval = new HashMap<String, Integer>();
		try {
			for(int i =0; i<moduleJson.size(); i++) {

				Date startDate= getDateWithoutTime(moduleJson.get(i).getStartDate());// new SimpleDateFormat(ReachService.DATE_FORMAT).parse(.toString());
				Date endDate = getDateWithoutTime(moduleJson.get(i).getEndDate()); //new SimpleDateFormat(ReachService.DATE_FORMAT).parse(.toString());

				today = getDateWithoutTime(today);
				if(today.compareTo(startDate) >= 0 && today.compareTo(endDate) <=0) {
					rval.put(MODULE, Integer.valueOf(moduleJson.get(i).getModule())-1);
					long diffTime = today.getTime() - startDate.getTime();
					Long d = TimeUnit.DAYS.convert(diffTime, TimeUnit.MILLISECONDS);
					rval.put(DAY,d.intValue());
					Long moduleLen =TimeUnit.DAYS.convert(endDate.getTime() -
							startDate.getTime(),TimeUnit.MILLISECONDS)+1;
					rval.put(MODULE_LENGTH, moduleLen.intValue());
					break;
				}

			}

			return rval;
		}catch(Exception e) {

			ServerExceptionLogger exceptionLogger = new ServerExceptionLogger();
			int lineNumber = e.getStackTrace()[0].getLineNumber(); 
			String classOfException = e.toString();
			String nameOfClass = this.getClass().getSimpleName();
			String nameOfMethod = e.getStackTrace()[0].getMethodName();
			exceptionLogger.logServerException(lineNumber, classOfException, nameOfClass, 
					nameOfMethod, patientScheduleJSON.getPin(),INTERNAL_ERROR_CODE);

			return null;
		}
	}

	public Date getDateWithoutTime(Date date) throws ParseException {
		try {
			SimpleDateFormat formatter = new SimpleDateFormat(
					"dd/MM/yyyy");
			return formatter.parse(formatter.format(date));
		} catch (java.text.ParseException e) {

			ServerExceptionLogger exceptionLogger = new ServerExceptionLogger();
			int lineNumber = e.getStackTrace()[0].getLineNumber(); 
			String classOfException = e.toString();
			String nameOfClass = this.getClass().getSimpleName();
			String nameOfMethod = e.getStackTrace()[0].getMethodName();
			exceptionLogger.logServerException(lineNumber, classOfException, nameOfClass,
					nameOfMethod, -1,INTERNAL_ERROR_CODE);

		}
		return null;
	}

	public int getBlobTricks(int patientPin) {
		try {
			Date today = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("EEEE");
			DAO dao = DAOFactory.getTheDAO();
			int currVal = dao.getReleasedBlobTricksDAO(patientPin).getCount();
			return currVal;
		}catch(Exception e) {

			ServerExceptionLogger exceptionLogger = new ServerExceptionLogger();
			int lineNumber = e.getStackTrace()[0].getLineNumber(); 
			String classOfException = e.toString();
			String nameOfClass = this.getClass().getSimpleName();
			String nameOfMethod = e.getStackTrace()[0].getMethodName();
			exceptionLogger.logServerException(lineNumber, classOfException, nameOfClass,
					nameOfMethod, patientPin,INTERNAL_ERROR_CODE);

			return 0;
		}

	}

	public int updateBlobTricksCount(int patientPin) {
		try {
			DAO dao = DAOFactory.getTheDAO();
			int rval = dao.getReleasedBlobTricksDAO(patientPin).getCount();
			rval++;
			BlobTricks  obj = new BlobTricks(patientPin, rval);
			dao.updateBlobTrickCountDAO(obj);
			return rval;
		}catch(Exception e) {

			ServerExceptionLogger exceptionLogger = new ServerExceptionLogger();
			int lineNumber = e.getStackTrace()[0].getLineNumber(); 
			String classOfException = e.toString();
			String nameOfClass = this.getClass().getSimpleName();
			String nameOfMethod = e.getStackTrace()[0].getMethodName();
			exceptionLogger.logServerException(lineNumber, classOfException, nameOfClass,
					nameOfMethod, patientPin,INTERNAL_ERROR_CODE);

			return 0;
		}
	}

	@Override
	public boolean personalizeSkillSet(int patientPin){

		boolean rval = false;
		try {
			Logger log;
			String trialTitle = TRIAL_NAME; // Refactor : needs to be done in a better way...
			SimpleDateFormat timeStampFormat = new SimpleDateFormat("MM.dd.YYYY HH:mm:ss", Locale.US);
			String date = timeStampFormat.format(new Date());
			Integer ppin = Integer.valueOf(patientPin);
			String metaData ="";
			String level="";
			String activityVal = "ACTIVITY_NAME";
			String levelOfUXP= "LEVEL_OF_PERSONALIZATION";
			String type ="PERSONALIZATION";
			String format = "JSON";
			String subType = "SKILL";

			DAO dao = DAOFactory.getTheDAO();
			PatientScheduleJSON patientScheduleJSON = dao.getSchedule(patientPin);

			if(patientScheduleJSON == null || patientScheduleJSON.getSchedule() == null
					|| patientScheduleJSON.getSchedule().size() == 0) {
				return false;
			}

			Integer module = -1, resetModule=-1;
			Integer dayOfModule = -1, resetDay=-1;
			Integer moduleLen = 0;

			Date today = new Date();//new SimpleDateFormat(ReachService.DATE_FORMAT).parse(.toString());

			// create method  to get module and day of module - done
			HashMap<String, Integer> map = this.getModuleAndDay(patientScheduleJSON, today);

			if(map != null && map.size() > 0) {
				if (map.containsKey(MODULE) && map.get(MODULE) != null)
					module = map.get(MODULE);
				if (map.containsKey(DAY) && map.get(DAY) != null)
					dayOfModule = map.get(DAY);
				if (map.containsKey(MODULE_LENGTH) && map.get(MODULE_LENGTH) != null)
					moduleLen=map.get(MODULE_LENGTH);
			}
			if (module == -1) {
				// No module selected so no trials for this patient pin
				return false;
			} else {
				ArrayList<ModuleJSON> moduleJson = patientScheduleJSON.getSchedule();
				ArrayList<ScheduleArrayJSON> schedule = moduleJson.get(module).getSchedule();
				ArrayList<ActivityScheduleJSON> activityList = schedule.get(dayOfModule).getActivitySchedule();
				Date resetDate;
				int index =0;

				// check skill personalization for each activity
				for(ActivityScheduleJSON activity : activityList){

					String activityName = activity.getActivity();
					Integer l1_min = 0, l1_max = 0, l2_min = 0, l2_max = 0;
					if(activityName.equals("WorryHeads") || activityName.equals("StandUp") ||
							activityName.equals("MakeBelieve")){

						switch (activityName) {
						case "WorryHeads":
							resetDate= patientScheduleJSON.getWorryHeadsResetDate();
							l1_min = Integer.parseInt(_properties.getProperty(SKILL_WH_L1_MIN));
							l1_max = Integer.parseInt(_properties.getProperty(SKILL_WH_L1_MAX));
							l2_min = Integer.parseInt(_properties.getProperty(SKILL_WH_L2_MIN));
							l2_max = Integer.parseInt(_properties.getProperty(SKILL_WH_L2_MAX));
							break;
						case "StandUp":
							resetDate= patientScheduleJSON.getStandUpResetDate();
							l1_min = Integer.parseInt(_properties.getProperty(SKILL_SU_L1_MIN));
							l1_max = Integer.parseInt(_properties.getProperty(SKILL_SU_L1_MAX));
							l2_min = Integer.parseInt(_properties.getProperty(SKILL_SU_L2_MIN));
							l2_max = Integer.parseInt(_properties.getProperty(SKILL_SU_L2_MAX));
							break;
						case "MakeBelieve":
							resetDate= patientScheduleJSON.getMakeBelieveResetDate();
							l1_min = Integer.parseInt(_properties.getProperty(SKILL_MB_L1_MIN));
							l1_max = Integer.parseInt(_properties.getProperty(SKILL_MB_L1_MAX));
							l2_min = Integer.parseInt(_properties.getProperty(SKILL_MB_L2_MIN));
							l2_max = Integer.parseInt(_properties.getProperty(SKILL_MB_L2_MAX));
							break;
						default:
							continue;

						}


						float totalScore=0,totalActtualCount=0;
						int currModule = module;
						int prevDay = dayOfModule-1;
						HashMap<String, Integer> resetDateMap = this.getModuleAndDay(patientScheduleJSON, resetDate);

						if (resetDateMap != null && resetDateMap.size() > 0) {
							if (map.containsKey(MODULE) && map.get(MODULE) != null)
								resetModule = resetDateMap.get(MODULE);
							if (map.containsKey(DAY) && map.get(DAY) != null)
								resetDay = resetDateMap.get(DAY);
						}

						if (resetModule == -1) {
							// no reset date found in current module
							continue;
						}

						ArrayList<ScheduleArrayJSON> currentModuleSchedule = moduleJson.get(currModule).getSchedule();

						// calculate score till now for current activity
						while((currModule > resetModule)
								|| (currModule == resetModule && prevDay >= resetDay)) {

							if (prevDay < 0) {
								currModule--;
								if (module == -1)
									break;
								currentModuleSchedule = moduleJson.get(currModule).getSchedule();
								prevDay = currentModuleSchedule.size() - 1;
							}

							ArrayList<ActivityScheduleJSON> actList = currentModuleSchedule.get(prevDay)
									.getActivitySchedule();
							for (ActivityScheduleJSON a : actList) {

								if(a.getActivity().equals(activityName)) {
									if (a.getActualCount() > 0) {
										totalScore += a.getScore();
										totalActtualCount += a.getActualCount();
									}
									break;
								}
							}
							prevDay--;
						}

						Double result=0.0;

						// calculate level of personalization for activity
						if(totalActtualCount !=0) {
							result = Double.valueOf((totalScore / totalActtualCount) * 100);

							//set levelOfSkillPersonalization to 1
							if (result >= l1_min && result < l1_max && 
									shouldSetNewSkillSet(patientScheduleJSON, today, activityName)) {

								if (dao.updateLevelOfSkillPersonalization(patientPin, module,
										dayOfModule, index, 1)) {

									metaData = "{ \""+activityVal+ "\": \""+activity.getActivity()+"\","
											+ "\""+levelOfUXP+"\" : \"1\" } ";
									level="UPDATED_SKILL_LEVEL";
								} else {
									metaData = "{ \""+activityVal+ "\": \""+activity.getActivity()+"\","
											+ "\""+levelOfUXP+"\" : \"1\" } ";
									level="NOT_UPDATED_SKILL_LEVEL";
								}
								//set levelOfSkillPersonalization to 2
							} else if (result >= l2_min && result < l2_max && 
									shouldSetNewSkillSet(patientScheduleJSON, today, activityName)) {

								if (dao.updateLevelOfSkillPersonalization(patientPin, module,
										dayOfModule, index, 2)) {
									metaData = "{ \""+activityVal+ "\": \""+activity.getActivity()+"\","
											+ "\""+levelOfUXP+"\" : \"2\" } ";
									level="UPDATED_SKILL_LEVEL";
								} else {
									metaData = "{ \""+activityVal+ "\": \""+activity.getActivity()+"\","
											+ "\""+levelOfUXP+"\" : \"2\" } ";
									level="NOT_UPDATED_SKILL_LEVEL";
								}
							}else {
								System.out.println("Skill personalization has already applied for several days.");
							}
						}
					}
					index++;
				}
			}
			if(!metaData.equals("")) {
				log = new Logger(dao.getTrialIdByTitle(trialTitle),date,level,
						type, format, subType, ppin.toString(), metaData);
				ArrayList<Logger> al = new ArrayList<Logger>();
				al.add(log);
				Logger[] logs = new Logger[al.size()];

				logs = al.toArray(logs);
				dao.logPersonalizationMessage(logs);
			}
			rval = true;
		}catch (Exception e){
			rval = false;

			ServerExceptionLogger exceptionLogger = new ServerExceptionLogger();
			int lineNumber = e.getStackTrace()[0].getLineNumber(); 
			String classOfException = e.toString();
			String nameOfClass = this.getClass().getSimpleName();
			String nameOfMethod = e.getStackTrace()[0].getMethodName();
			exceptionLogger.logServerException(lineNumber, classOfException, nameOfClass,
					nameOfMethod, patientPin,INTERNAL_ERROR_CODE);

		}
		return rval;
	}

	@Override
	public SUDSActivitiesWrapper getActivitySchedule(int patientPin) {

		HashMap<String,Boolean> rval = new HashMap<String, Boolean>();

		SUDSActivitiesWrapper scheduledActivityList = new SUDSActivitiesWrapper();

		try {
			DAO dao = DAOFactory.getTheDAO();

			PatientScheduleJSON patientScheduleJSON = dao.getSchedule(patientPin);

			if (patientScheduleJSON == null
					|| patientScheduleJSON.getSchedule() == null
					|| patientScheduleJSON.getSchedule().size() == 0) {
				return new SUDSActivitiesWrapper();
			}

			ArrayList<ModuleJSON> moduleJson = patientScheduleJSON.getSchedule();
			Integer module =-1;
			Integer dayOfModule =-1;
			Integer moduleLen=0;

			Date today = new Date();//new SimpleDateFormat(ReachService.DATE_FORMAT).parse(.toString());

			// create method  to get module and day of module - done
			HashMap<String, Integer> map = this.getModuleAndDay(patientScheduleJSON,today);
			if(map != null && map.size() > 0) {
				if (map.containsKey(MODULE) && map.get(MODULE) != null)
					module = map.get(MODULE);
				if (map.containsKey(DAY) && map.get(DAY) != null)
					dayOfModule = map.get(DAY);
				if (map.containsKey(MODULE_LENGTH) && map.get(MODULE_LENGTH) != null)
					moduleLen=map.get(MODULE_LENGTH);
			} else {
				return new SUDSActivitiesWrapper() ;
			}
			ArrayList<ScheduleArrayJSON> schedule = moduleJson.get(module).getSchedule();
			ArrayList<ActivityScheduleJSON> activityList = schedule.get(dayOfModule).getActivitySchedule();
			for(ActivityScheduleJSON activity : activityList) {
				if(dayOfModule == moduleLen-1) {
					if(activity.getActualCount() < activity.getMinimumCount()) {
						rval.put(activity.getActivity(), true);
					}else {
						rval.put(activity.getActivity(),false);
					}
					scheduledActivityList.setSudsConfig(true);
				}else {
					if(activity.getActualCount() < activity.getMinimumCount()) {
						rval.put(activity.getActivity(), true);
					}else {
						rval.put(activity.getActivity(),false);
					}
				}
			}
			scheduledActivityList.setActvityMap(rval);

		}catch(Exception e) {

			ServerExceptionLogger exceptionLogger = new ServerExceptionLogger();
			int lineNumber = e.getStackTrace()[0].getLineNumber(); 
			String classOfException = e.toString();
			String nameOfClass = this.getClass().getSimpleName();
			String nameOfMethod = e.getStackTrace()[0].getMethodName();
			exceptionLogger.logServerException(lineNumber, classOfException, nameOfClass,
					nameOfMethod, patientPin,INTERNAL_ERROR_CODE);

		} finally {
			return scheduledActivityList;
		}
	}


	public boolean shouldSetNewSkillSet(PatientScheduleJSON patientScheduleJSON, Date today, String activityName) {
		try {
			DAO dao = DAOFactory.getTheDAO();
			Integer module=-1, dayOfModule=0, moduleLen=0;
			ArrayList<ModuleJSON> moduleJson = patientScheduleJSON.getSchedule();
			HashMap<String, Integer> map = this.getModuleAndDay(patientScheduleJSON,today);

			if(map != null && map.size() > 0) {
				if (map.containsKey(MODULE) && map.get(MODULE) != null)
					module = map.get(MODULE);
				if (map.containsKey(DAY) && map.get(DAY) != null)
					dayOfModule = map.get(DAY);
				if (map.containsKey(MODULE_LENGTH) && map.get(MODULE_LENGTH) != null)
					moduleLen=map.get(MODULE_LENGTH);
			}

			Integer resetModule=-1, resetDay=0;
			Date resetDate = new Date();

			int resetCount =-1;
			int count =0, levelOfSkill =2;
			int prevDay = dayOfModule-1;
			boolean rval = true;
			int flag=0;
			if(module ==-1) {

			}else {
				switch(activityName) {
				case "WorryHeads":
					resetDate = patientScheduleJSON.getWorryHeadsResetDate();
					resetCount = Integer.parseInt(_properties.getProperty(WH_RESET_COUNT));
					break;
				case "MakeBelieve":
					resetDate = patientScheduleJSON.getMakeBelieveResetDate();
					resetCount = Integer.parseInt(_properties.getProperty(MB_RESET_COUNT));
					break;
				case "SWAP":
					//		resetCount = Integer.parseInt(_properties.getProperty("SWAP_RESET_COUNT"));
					break;
				case "StandUp":
					resetDate = patientScheduleJSON.getStandUpResetDate();
					resetCount = Integer.parseInt(_properties.getProperty(SU_RESET_COUNT));
					break;
				}
				HashMap<String, Integer> resetDateMap = this.getModuleAndDay(patientScheduleJSON, resetDate);
				if (resetDateMap != null && resetDateMap.size() > 0) {
					if (map.containsKey(MODULE) && map.get(MODULE) != null)
						resetModule = resetDateMap.get(MODULE);
					if (map.containsKey(DAY) && map.get(DAY) != null)
						resetDay = resetDateMap.get(DAY);
				}

				if(resetCount == -1) {
					System.out.println("Reset count not set for the Activity : " + activityName);
				}else {
					ArrayList<ScheduleArrayJSON> schedule = moduleJson.get(module).getSchedule();;
					while(count < resetCount) {
						if(prevDay == -1) {
							module--;
							if (module == -1)
								break;
							schedule = moduleJson.get(module).getSchedule();
							prevDay = schedule.size() -1;
							if(resetModule > module
									|| (module == resetModule) && prevDay < resetDay) {
								break;
							}

						}
						ArrayList<ActivityScheduleJSON> activityList = schedule.get(prevDay).getActivitySchedule();

						for(ActivityScheduleJSON activity :activityList) {
							if(activity.getActivity().equals(activityName)) {
								if(activity.getLevelofSkillPersonalization() == levelOfSkill) {
									flag++;
								}
								break;
							}
						}
						prevDay--;
						count++;
					}
					if(flag == resetCount) {
						// update reset date
						if(dao.updateResetDate(patientScheduleJSON.getPin(), new Date(), activityName)) {
							System.out.println("Reset Date updated for activity : " + activityName);
						}else {
							System.out.println("Reset Date FAILED to update for activity : " + activityName);
						}
						rval = false;
					}
				}
			}
			return rval;
		}catch(Exception e) {

			ServerExceptionLogger exceptionLogger = new ServerExceptionLogger();
			int lineNumber = e.getStackTrace()[0].getLineNumber(); 
			String classOfException = e.toString();
			String nameOfClass = this.getClass().getSimpleName();
			String nameOfMethod = e.getStackTrace()[0].getMethodName();
			exceptionLogger.logServerException(lineNumber, classOfException, nameOfClass,
					nameOfMethod, patientScheduleJSON.getPin(),INTERNAL_ERROR_CODE);

			return false;
		}


	}

	public boolean isLastDayOfModule(int patientPin) {
		try {
			DAO dao = DAOFactory.getTheDAO();
			PatientScheduleJSON patientScheduleJSON = dao.getSchedule(patientPin);
			if(patientScheduleJSON == null)
				return false;
			Date today = new Date();
			HashMap<String,Integer> map = getModuleAndDay(patientScheduleJSON, today);
			Integer module =-1,dayOfModule=-1,moduleLen=0;
			if(map != null && map.size() > 0) {
				if (map.containsKey(MODULE) && map.get(MODULE) != null)
					module = map.get(MODULE);
				if (map.containsKey(DAY) && map.get(DAY) != null)
					dayOfModule = map.get(DAY);
				if (map.containsKey(MODULE_LENGTH) && map.get(MODULE_LENGTH) != null)
					moduleLen=map.get(MODULE_LENGTH);

			}
			else {
				return false;
			}
			if(dayOfModule == moduleLen-1) {
				return true;
			}

		}catch(Exception e) {

			ServerExceptionLogger exceptionLogger = new ServerExceptionLogger();
			int lineNumber = e.getStackTrace()[0].getLineNumber(); 
			String classOfException = e.toString();
			String nameOfClass = this.getClass().getSimpleName();
			String nameOfMethod = e.getStackTrace()[0].getMethodName();
			exceptionLogger.logServerException(lineNumber, classOfException, nameOfClass, 
					nameOfMethod, patientPin,INTERNAL_ERROR_CODE);

			return false;
		}
		return false;
	}

	public HashMap<String,Integer> getCurrentAndToBeDoneCount(int patientPin, String activityName) {
		HashMap<String,Integer> result = new HashMap<>();
		result.put(ActivityInstance.CURRENT_COUNT, -1);
		result.put(ActivityInstance.TO_BE_DONE_COUNT, -1);
		try {
			DAO dao = DAOFactory.getTheDAO();
			PatientScheduleJSON patientScheduleJSON = dao.getSchedule(patientPin);
			if(patientScheduleJSON == null || patientScheduleJSON.getSchedule() == null
					|| patientScheduleJSON.getSchedule().size() == 0) {
				return result;
			}
			Date today = new Date();
			HashMap<String,Integer> map = getModuleAndDay(patientScheduleJSON, today);
			Integer module =-1,dayOfModule=-1,moduleLen=0;
			if(map != null && map.size() > 0) {
				if (map.containsKey(MODULE) && map.get(MODULE) != null)
					module = map.get(MODULE);
				if (map.containsKey(DAY) && map.get(DAY) != null)
					dayOfModule = map.get(DAY);
				if (map.containsKey(MODULE_LENGTH) && map.get(MODULE_LENGTH) != null)
					moduleLen=map.get(MODULE_LENGTH);

			}
			else {
				return result;
			}

			List<ActivityScheduleJSON> activityList = patientScheduleJSON.getSchedule().get(module)
					.getSchedule().get(dayOfModule).getActivitySchedule();

			ActivityScheduleJSON activity = activityList.stream() 
					.filter(x -> activityName.equals(x.getActivity()))   
					.findAny()                                     		 
					.orElse(null);
			if(activity != null && (activity.getActualCount() < activity.getMinimumCount())) {
				result.put(ActivityInstance.CURRENT_COUNT, activity.getActualCount()+1);
				result.put(ActivityInstance.TO_BE_DONE_COUNT, activity.getMinimumCount());
			}
			return result;	
		}catch(Exception e) {

			ServerExceptionLogger exceptionLogger = new ServerExceptionLogger();
			int lineNumber = e.getStackTrace()[0].getLineNumber(); 
			String classOfException = e.toString();
			String nameOfClass = this.getClass().getSimpleName();
			String nameOfMethod = e.getStackTrace()[0].getMethodName();
			exceptionLogger.logServerException(lineNumber, classOfException, nameOfClass,
					nameOfMethod, patientPin,INTERNAL_ERROR_CODE);

			return result;
		}
	}

}

