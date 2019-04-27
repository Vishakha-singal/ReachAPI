package edu.asu.heal.reachv3.api.notification;

import java.util.HashSet;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.asu.heal.core.api.dao.DAO;
import edu.asu.heal.core.api.dao.DAOFactory;
import edu.asu.heal.core.api.models.NotificationData;
import edu.asu.heal.core.api.models.NotificationRequestModel;
import edu.asu.heal.core.api.models.Patient;

public class Notification {

	public Notification() {
		
	}
	/****************************************  Notification methods  *************************************************/
	// Reference 1: http://developine.com/how-to-send-firebase-push-notifications-from-app-server-tutorial/
	// Reference 2: https://firebase.google.com/docs/cloud-messaging/send-message
	public boolean sendNotification(NotificationData data, int patientPin, String serverKey) {

		// move server key to property and add para in method.
		
		try {
			DAO dao = DAOFactory.getTheDAO();
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpPost postRequest = new HttpPost(
					"https://fcm.googleapis.com/fcm/send");

			NotificationRequestModel notificationRequestModel = new NotificationRequestModel();

			Patient p = dao.getPatient(patientPin);
			HashSet<String> registrationToken = p.getRegistrationToken();

			for(String token : registrationToken ) {
				notificationRequestModel.setData(data);
				notificationRequestModel.setTo(token);
				//            notificationRequestModel.setTo("fxxJWeK-Fo8:APA91bG_-82urLmUgfZwGfY1QA4REuXZzzQojqu9Q4FzUVo3PScT-" +
				//                    "UFAEK9PSBeb2X6sRQvu6pZYfqRFeY5p3Zv2t0fqFOzFmcMBPx5nRq9GMRFzb-LuselMuS97bbmuVOzlk76_VJVu");


				ObjectMapper mapper = new ObjectMapper();
				String notificationJson = mapper.writeValueAsString(notificationRequestModel);

				StringEntity input = new StringEntity(notificationJson);
				input.setContentType("application/json");
				System.out.println(input);

				// You can get it from firebase console.
//				postRequest.addHeader("Authorization", "key=AAAAX5CbDOM:APA91bGd_AzSXfn64BsrxT1KEfCnh_yy99lXKPFo7l" +
//						"QUbGqM7tK0YU_YOUUO0X2lpJmMSmVkxZC6JPFkFeC6TimZFg0BsXsutnVhsGM-Ydp2ZFCVswMMnhHrzKbMZpTwKDyZU2XllSZn");
//				
				postRequest.addHeader("Authorization", serverKey);
				postRequest.setEntity(input);
				HttpResponse response = httpClient.execute(postRequest);
				if (response.getStatusLine().getStatusCode() != 200) {
					throw new RuntimeException("Failed : HTTP error code : "
							+ response.getStatusLine().getStatusCode());
				} else if (response.getStatusLine().getStatusCode() == 200) {
					System.out.println("response:" + EntityUtils.toString(response.getEntity()));
				}
			}
			return true;
		} catch (RuntimeException runtimeException) {
			runtimeException.printStackTrace();
			return false;
		} catch (Exception exception) {
			exception.printStackTrace();
			return false;
		}
		finally {
			
		}
	}

}
