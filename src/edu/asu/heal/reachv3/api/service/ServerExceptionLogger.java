package edu.asu.heal.reachv3.api.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import edu.asu.heal.core.api.dao.DAO;
import edu.asu.heal.core.api.dao.DAOFactory;
import edu.asu.heal.core.api.models.Logger;

public class ServerExceptionLogger {
	private DAO __dao = null;
	private static String TRIAL_NAME="Compass";

	public ServerExceptionLogger() {
		try {
			__dao = DAOFactory.getTheDAO();
		} catch (Exception e) {
			e.printStackTrace();
		//	setServerExceptions(lineNumber, classOfException, nameOfClass, nameOfMethod, -1);		
		}
	}

	public void logServerException(int lineNumber, String classOfException, 
			String nameOfClass, String nameOfMethod, Integer patientPin, Integer statusCode) {
		System.out.println("In Log server exception....");
		SimpleDateFormat timeStampFormat = new SimpleDateFormat("MM.dd.YYYY HH:mm:ss", Locale.US);
		String date = timeStampFormat.format(new Date());
		
		String trialId = __dao.getTrialIdByTitle(TRIAL_NAME);
		
		String metaData = "{ \"NAME_OF_CLASS :\" \"" + nameOfClass + "\" ,"
				+ " \"NAME_OF_METHOD\" : \"" + nameOfMethod + "\" ,"
						+ " \"LINE_NUMBER\" : \"" + lineNumber + "\"} ";
		Logger __logger = new Logger(trialId, date, "EXCEPTION", classOfException, "JSON", String.valueOf(statusCode),
				patientPin.toString(), metaData);
		ArrayList<Logger> al = new ArrayList<Logger>();
		al.add(__logger);
		Logger[] logs = new Logger[al.size()];

		logs = al.toArray(logs);
		__dao.logExceptionMessage(logs);
	}
}
