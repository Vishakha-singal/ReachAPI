package edu.asu.heal.reachv3.api.notification;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.json.JSONArray;
import org.json.JSONObject;

import edu.asu.heal.core.api.models.NotificationData;

public class LevelOneNotification implements INotificationInterface {

	private static JSONObject notificationData;
	private static Properties _properties;

	static {
		try {
			String data = readFile("notificationData.json");
			notificationData = new JSONObject(data);

			_properties = new Properties();
			InputStream propFile = LevelOneNotification.class.getResourceAsStream("urls.properties");
			_properties.load(propFile);
			propFile.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	public LevelOneNotification() {
	}

	@Override
	public boolean sendNotification(String activityName,int module, int patientPin, 
			Integer numberOfDaysNotDone, int levelOfNotification,List<String> list, boolean sudsConfig ) {

		String details = getNotifiactionDetails(String.valueOf(module),
				levelOfNotification,numberOfDaysNotDone.toString(), list);

		if(!details.equals("")) {
			String url = _properties.getProperty(activityName);
			String serverKey = _properties.getProperty("serverKey");

			List<ActivityList> l1List = new ArrayList<>();

			for(int i=0; i<list.size();i++) {
				String activityUrl = "";
				if (_properties.getProperty(list.get(i)) != null) {
					activityUrl = _properties.getProperty(list.get(i));
				}
				ActivityList obj = new ActivityList(list.get(i), activityUrl, false);
				l1List.add(obj);
			}

			NotificationData data = new NotificationData(details, null, url,
					levelOfNotification, l1List,sudsConfig);

			Notification obj = new Notification();
			if(obj.sendNotification(data, patientPin, serverKey)) {
				return true;
			}
		}
		return false;		
	}

	public String getNotifiactionDetails(String module, int levelOfNotification, String numberOfDaysNotDone,
										 List<String> activityList ) {

		if (notificationData.has("level_" + levelOfNotification)
				&& notificationData.getJSONArray("level_" + levelOfNotification) != null) {
			JSONArray arr = notificationData.getJSONArray("level_" + levelOfNotification);

			for (int index = 0; index < arr.length(); index++) {
				JSONObject obj = arr.getJSONObject(index);

				if (obj.getString("activity") != null) {
					if (activityList.contains(obj.getString("activity"))) {
						return obj.getString("message");

					} else if (obj.getString("activity").equalsIgnoreCase("Other")) {
						return obj.getString("message");
					}
				}
			}
		}
		return "";
	}

	public static String readFile(String filename) {
		String result = "";
		try {
			InputStream is = LevelTwoNotification.class.getResourceAsStream(filename);
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();
			while (line != null) {
				sb.append(line);
				line = br.readLine();
			}
			result = sb.toString();
			br.close();
			is.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
		return result;
	}

}
