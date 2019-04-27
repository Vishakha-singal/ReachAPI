package edu.asu.heal.core.api.resources;

import edu.asu.heal.core.api.models.*;
import edu.asu.heal.core.api.responses.ActivityInstanceResponse;
import edu.asu.heal.core.api.responses.ActivityResponse;
import edu.asu.heal.core.api.responses.HEALResponse;
import edu.asu.heal.core.api.responses.HEALResponseBuilder;
import edu.asu.heal.core.api.service.HealService;
import edu.asu.heal.core.api.service.HealServiceFactory;
import edu.asu.heal.reachv3.api.models.SUDSActivitiesWrapper;
import edu.asu.heal.reachv3.api.service.ReachService;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.text.NumberFormat;
import java.util.List;

@Path("/activities")
@Produces(MediaType.APPLICATION_JSON)
public class ActivityResource {

	@Context
	private UriInfo _uri;

	private static HealService reachService =
			HealServiceFactory.getTheService();

	/** @apiDefine ActivityNotFoundError
	 * @apiError (Error 4xx) {404} NotFound Activity cannot be found
	 * */

	/**
	 * @apiDefine InternalServerError
	 * @apiError (Error 5xx) {500} InternalServerError Something went wrong at server, Please contact the administrator!
	 * */

	/**
	 * @api {get} /activities?domain=domainName Get list of Activities for a given domain
	 * @apiName GetActivities
	 * @apiGroup Activity
	 * @apiParam {String} domain Domain name for which activities are to be fetched. Use "_" in place of space
	 * character. Case sensitive.
	 * @apiUse ActivityNotFoundError
	 * @apiUse InternalServerError
	 */
	@GET
	@QueryParam("domain")
	public Response getActivities(@QueryParam("domain") String domain) {
		List<Activity> activities = reachService.getActivities(domain);

		HEALResponse response;
		HEALResponseBuilder builder;
		try{
			builder = new HEALResponseBuilder(ActivityResponse.class);
		}catch (InstantiationException | IllegalAccessException ie){
			ie.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}

		if (activities == null) {
			response = builder
					.setStatusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
					.setData("SOME SERVER ERROR. PLEASE CONTACT ADMINISTRATOR")
					.build();
		} else if (activities.isEmpty()) {
			response = builder
					.setStatusCode(Response.Status.OK.getStatusCode())
					.setData("THERE ARE NO ACTIVITIES FOR THIS DOMAIN")
					.build();
		} else if (activities.size() == 1) {
			if (activities.get(0).equals(NullObjects.getNullActivity())) {
				response = builder
						.setStatusCode(Response.Status.BAD_REQUEST.getStatusCode())
						.setData("THE DOMAIN YOU'VE PASSED IN IS INCORRECT OR DOES NOT EXIST")
						.build();
			} else {
				response = builder
						.setStatusCode(Response.Status.OK.getStatusCode())
						.setData(activities)
						.setServerURI(_uri.getBaseUri().toString())
						.build();
			}
		} else {
			response = builder
					.setStatusCode(Response.Status.OK.getStatusCode())
					.setData(activities)
					.setServerURI(_uri.getBaseUri().toString())
					.build();
		}
		return Response.status(response.getStatusCode()).entity(response.toEntity()).build();
	}

	/**
	 * @api {get} /activities/:id Activity Detail
	 * @apiName ActivityDetail
	 * @apiGroup Activity
	 * @apiParam {String} id Activity's Unique Id
	 * @apiParamExample http://localhost:8080/ReachAPI/rest/activities/5abd71b82e027e29ca2353a0
	 * @apiUse ActivityInstanceNotFoundError
	 * @apiUse InternalServerError
	 * @apiUse NotImplementedError
	 */
	@GET
	@Path("/{activityId}")
	public Response getActivity(@PathParam("activityId") String activityId){
		HEALResponse response;
		HEALResponseBuilder builder;
		try{
			builder = new HEALResponseBuilder(ActivityResponse.class);
		}catch (InstantiationException | IllegalAccessException ie){
			ie.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
		Activity activity = reachService.getActivity(activityId);

		if (activity == null) {
			response = builder
					.setStatusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
					.setData("SOME SERVER ERROR. PLEASE CONTACT ADMINISTRATOR")
					.build();
		} else if (activity.equals(NullObjects.getNullActivity())) {
			response = builder
					.setStatusCode(Response.Status.NOT_FOUND.getStatusCode())
					.setData("THE ACTIVITY YOU'RE REQUESTING DOES NOT EXIST")
					.build();
		} else {
			response = builder
					.setStatusCode(Response.Status.OK.getStatusCode())
					.setData(activity)
					.setServerURI(_uri.getBaseUri().toString())
					.build();
		}
		return Response.status(response.getStatusCode()).entity(response.toEntity()).build();
	}

	/**
	 * @apiDefine BadRequestError
	 * @apiError (Error 4xx) {400} BadRequest Bad Request Encountered
	 * */

	/** @apiDefine ActivityInstanceNotFoundError
	 * @apiError (Error 4xx) {404} NotFound ActivityInstance(s) cannot be found
	 * */

	/**
	 * @apiDefine InternalServerError
	 * @apiError (Error 5xx) {500} InternalServerError Something went wrong at server, Please contact the administrator!
	 * */

	/**
	 * @apiDefine NotImplementedError
	 * @apiError (Error 5xx) {501} NotImplemented The resource has not been implemented. Please keep patience, our developers are working hard on it!!
	 * */

	/**
	 * @api {get} /getSuggestedActivities?patientPin={patientPin}&emotion={emotionName}&intensity={intensityValue} ActivityInstances
	 * @apiName GetActivityInstances
	 * @apiGroup ActivityInstance
	 * @apiParam {Number} patientPin Patient's Unique Id
	 * @apiParam {String} Emotion Name of emotion
	 * @apiParam {Number} Intensity of emotion
	 * @apiUse BadRequestError
	 * @apiUse ActivityInstanceNotFoundError
	 * @apiUse InternalServerError
	 * @apiUse NotImplementedError
	 */
	@GET
	@Path("/getsuggestedactivities")
	public Response getSuggestedActivities(@QueryParam("patientPin") int patientPin,
			@QueryParam("emotion") String emotion,
			@QueryParam("intensity") int intensity) {
		HEALResponse response = null;
		HEALResponseBuilder builder;
		try{
			builder = new HEALResponseBuilder(ActivityInstanceResponse.class);
		}catch (InstantiationException | IllegalAccessException ie){
			ie.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
		if (patientPin == 0 || patientPin < -1) {
			response = builder
					.setData("YOUR PATIENT PIN IS ABSENT FROM THE REQUEST")
					.setStatusCode(Response.Status.BAD_REQUEST.getStatusCode())
					.setServerURI(_uri.getBaseUri().toString())
					.build();
		} else {
			if(emotion != null){
				ReachService service = (ReachService)reachService;
				String emotionsActivityResponse = service.getEmotionsActivityInstance(patientPin, emotion, intensity);
				if(emotionsActivityResponse == null){
					response = builder
							.setStatusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
							.setData("SOME ERROR ON THE SERVER. CONTACT ADMINISTRATOR")
							.build();
				}else if(emotionsActivityResponse.length() == 0){
					response = builder
							.setStatusCode(Response.Status.BAD_REQUEST.getStatusCode())
							.setData("THE EMOTION YOU PASSED IN IS INCORRECT")
							.build();
				}else{
					response = builder
							.setStatusCode(Response.Status.OK.getStatusCode())
							.setData(emotionsActivityResponse)
							.build();
				}
			}else{
				// Task #386 : Has to be done more general to satisfy activities suggestion for any activity.
				response = builder
						.setStatusCode(Response.Status.BAD_REQUEST.getStatusCode())
						.setData("EMOTION PARAMETER IS MISSING")
						.build();
			}
		}

		return Response.status(response.getStatusCode()).entity(response.toEntity()).build();
	}


	/**
	 * @api {post} /activities Create Activity
	 * @apiName CreateActivity
	 * @apiGroup Activity
	 * @apiParam {String} Title Title of the Activity
	 * @apiParam {String} Description Description of the Activity
	 * @apiParam (Login) {String} pass Only logged in user can get this
	 * @apiSuccess {Object[]} data null
	 * @apiSuccess {String} message Response Message
	 * @apiSuccess {String} messageType Response Message Type
	 * @apiSuccess {Number} statusCode  Response Status Code
	 * @apiSuccessExample {json} Success-Response:
	 * HTTP/1.1 201 CREATED
	 * {
	 * "data": null,
	 * "message": "Created",
	 * "messageType": "success",
	 * "statusCode": 201
	 * }
	 * @apiUse InternalServerError
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createActivity(Activity activityJSON) {
		HEALResponse response;
		HEALResponseBuilder builder;
		try{
			builder = new HEALResponseBuilder(ActivityResponse.class);
		}catch (InstantiationException | IllegalAccessException ie){
			ie.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}

		if (activityJSON.getTitle() == null || activityJSON.getTitle().trim().length() == 0) {
			response = builder
					.setStatusCode(Response.Status.BAD_REQUEST.getStatusCode())
					.setData("TITLE MISSING FROM REQUEST")
					.build();
		} else {
			Activity activity = reachService.createActivity(activityJSON.getTitle(), activityJSON.getDescription());
			if (activity == null) {
				response = builder
						.setStatusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
						.setData("COULD NOT CREATE ACTIVITY. CONTACT ADMINISTRATOR")
						.build();
			} else {
				response = builder
						.setStatusCode(Response.Status.CREATED.getStatusCode())
						.setData(activity)
						.setServerURI(_uri.getBaseUri().toString())
						.build();
			}
		}
		return Response.status(response.getStatusCode()).entity(response).build();
	}

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateActivity(Activity activityJSON){
		HEALResponse response;
		HEALResponseBuilder builder;
		try{
			builder = new HEALResponseBuilder(ActivityResponse.class);
		}catch (InstantiationException | IllegalAccessException ie){
			ie.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}

		if (activityJSON.getActivityId() == null || activityJSON.getActivityId().length() == 0) {
			response = builder
					.setStatusCode(Response.Status.BAD_REQUEST.getStatusCode())
					.setData("ACTIVITYID MISSING FROM REQUEST")
					.build();
		} else {
			Activity updatedActivity = reachService.updateActivity(activityJSON);
			if (updatedActivity == null) {
				response = builder
						.setStatusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
						.setData("COULD NOT UPDATE ACTIVITY. CONTACT ADMINISTRATOR")
						.build();
			} else if(updatedActivity.equals(NullObjects.getNullActivity())){
				response = builder
						.setStatusCode(Response.Status.BAD_REQUEST.getStatusCode())
						.setData("ACTIVITY ID YOU PASSED IN IS INCORRECT OR DOES NOT EXIST")
						.build();
			} else {
				response = builder
						.setStatusCode(Response.Status.OK.getStatusCode())
						.setData(updatedActivity)
						.setServerURI(_uri.getBaseUri().toString())
						.build();
			}
		}
		return Response.status(response.getStatusCode()).entity(response).build();
	}

	@DELETE
	@Path("/{id}")
	public Response removeActivity(@PathParam("id") String activityId) {
		HEALResponse response;
		HEALResponseBuilder builder;
		try{
			builder = new HEALResponseBuilder(ActivityResponse.class);
		}catch (InstantiationException | IllegalAccessException ie){
			ie.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}

		Activity removedActivity = reachService.deleteActivity(activityId);

		if (removedActivity.equals(NullObjects.getNullActivity())) {
			response = builder
					.setStatusCode(Response.Status.NOT_FOUND.getStatusCode())
					.setData("ACTIVITY DOES NOT EXIST")
					.build();
		} else if (removedActivity == null) {
			response = builder
					.setStatusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
					.setData("SOME PROBLEM IN DELETING ACTIVITY. CONTACT ADMINISTRATOR")
					.build();
		} else {
			response = builder
					.setStatusCode(Response.Status.NO_CONTENT.getStatusCode())
					.setData(null)
					.build();
		}
		return Response.status(response.getStatusCode()).build();
	}


	/** @apiDefine ActivityNotFoundError
	 * @apiError (Error 4xx) {404} NotFound Activity cannot be found
	 * */

	/**
	 * @apiDefine InternalServerError
	 * @apiError (Error 5xx) {500} InternalServerError Something went wrong at server, Please contact the administrator!
	 * */

	/**
	 * @api {get} /activities?domain=domainName Get list of Activities for a given domain
	 * @apiName GetActivities
	 * @apiGroup Activity
	 * @apiParam {String} domain Domain name for which activities are to be fetched. Use "_" in place of space
	 * character. Case sensitive.
	 * @apiUse ActivityNotFoundError
	 * @apiUse InternalServerError
	 */
	@GET
	@Path("/blobTricks")
	public Response getReleasedBlobTricks(@QueryParam("patientPin") int patientPin) {
		int numberOfBlobTrick = ((ReachService) reachService).getBlobTricks(patientPin);

		HEALResponse response;
		HEALResponseBuilder builder;
		try{
			builder = new HEALResponseBuilder(ActivityResponse.class);
		}catch (InstantiationException | IllegalAccessException ie){
			ie.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
		response = builder
				.setStatusCode(Response.Status.OK.getStatusCode())
				.setData("{ \"count\" : \""+numberOfBlobTrick +"\"}")
				.build();
		return Response.status(response.getStatusCode()).entity(response.toEntity()).build();
	}

	/** @apiDefine ActivityNotFoundError
	 * @apiError (Error 4xx) {404} NotFound Activity cannot be found
	 * */

	/**
	 * @apiDefine InternalServerError
	 * @apiError (Error 5xx) {500} InternalServerError Something went wrong at server, Please contact the administrator!
	 * */

	/**
	 * @api {get} /activities?domain=domainName Get list of Activities for a given domain
	 * @apiName GetActivities
	 * @apiGroup Activity
	 * @apiParam {String} domain Domain name for which activities are to be fetched. Use "_" in place of space
	 * character. Case sensitive.
	 * @apiUse ActivityNotFoundError
	 * @apiUse InternalServerError
	 */
	@GET
	@Path("/activityschedule")
	public Response getActivitySchedule(@QueryParam("patientPin") int patientPin) {

		SUDSActivitiesWrapper map = reachService.getActivitySchedule(patientPin);
		ObjectMapper mapper = new ObjectMapper();
		HEALResponse response;
		HEALResponseBuilder builder;
		String rval = null;
		try {
			rval = mapper.writeValueAsString(map);
			builder = new HEALResponseBuilder(ActivityResponse.class);
		} catch (JsonProcessingException | InstantiationException | IllegalAccessException ie) {
			ie.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
		if (map == null) {
			response = builder
					.setStatusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
					.setData("SOME SERVER ERROR. PLEASE CONTACT ADMINISTRATOR")
					.build();
		} else {
			response = builder
					.setStatusCode(Response.Status.OK.getStatusCode())
					.setData(rval)
					.setServerURI(_uri.getBaseUri().toString())
					.build();
		}
		return Response.status(response.getStatusCode()).entity(response.toEntity()).build();

	}

	/** @apiDefine ActivityNotFoundError
	 * @apiError (Error 4xx) {404} NotFound Activity cannot be found
	 * */

	/**
	 * @apiDefine InternalServerError
	 * @apiError (Error 5xx) {500} InternalServerError Something went wrong at server, Please contact the administrator!
	 * */

	/**
	 * @api {get} /activities/ping Get list of Activities for a given domain
	 * @apiName GetActivities
	 * @apiGroup Activity
	 * @apiParam {String} domain Domain name for which activities are to be fetched. Use "_" in place of space
	 * character. Case sensitive.
	 * @apiUse ActivityNotFoundError
	 * @apiUse InternalServerError
	 */
	@GET
	@Path("/ping")
	public Response ping() {
		Runtime runtime = Runtime.getRuntime();

		NumberFormat format = NumberFormat.getInstance();

		StringBuilder sb = new StringBuilder();
		long maxMemory = runtime.maxMemory();
		long allocatedMemory = runtime.totalMemory();
		long freeMemory = runtime.freeMemory();
		
		int availProcessor = runtime.availableProcessors();

		sb.append("free memory: " + format.format(freeMemory / 1024) + "\n");
		sb.append("allocated memory: " + format.format(allocatedMemory / 1024) + "\n");
		sb.append("max memory: " + format.format(maxMemory / 1024) + "\n");
		sb.append("total free memory: " + format.format((freeMemory + (maxMemory - allocatedMemory)) / 1024) + "\n");
		sb.append("Available CPU: " + availProcessor + "\n");
		
		
		System.out.println("Memory Status Before finalize : .-----------------------------");
		System.out.println(sb.toString());
		System.out.println("---------------------------------------------------------------");
		
		runtime.runFinalization();
		
		StringBuilder sb1 = new StringBuilder();
		 maxMemory = runtime.maxMemory();
		 allocatedMemory = runtime.totalMemory();
		 freeMemory = runtime.freeMemory();
		
		 availProcessor = runtime.availableProcessors();

		sb1.append("free memory: " + format.format(freeMemory / 1024) + "\n");
		sb1.append("allocated memory: " + format.format(allocatedMemory / 1024) + "\n");
		sb1.append("max memory: " + format.format(maxMemory / 1024) + "\n");
		sb1.append("total free memory: " + format.format((freeMemory + (maxMemory - allocatedMemory)) / 1024) + "\n");
		sb1.append("Available CPU: " + availProcessor + "\n");
		
		
		System.out.println("Memory Status After finalize : .-----------------------------");
		System.out.println(sb1.toString());
		System.out.println("---------------------------------------------------------------");

		
		return Response.status(Response.Status.OK).build();

	}
}
