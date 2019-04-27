package edu.asu.heal.core.api.resources;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

@WebListener
public class SchedulerMain implements ServletContextListener {

	private ScheduledExecutorService service;
	private final static int fONE_DAY = 1;
	private final static int fTWELVE_AM = 12;
	private final static int fTHIRTY_MINUTES =30;
	private final static int fSIX_AM = 12;
	private final static int fZERO_MINUTES =00;
	private final static long oneDayInMiliSec = (1000*60*60*24); 


	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent) {
		service = Executors.newSingleThreadScheduledExecutor();

		Calendar cal = Calendar.getInstance();
		int currHour = cal.get(Calendar.HOUR_OF_DAY);
		int min = cal.get(Calendar.MINUTE);
		// Currently runs from 6 am to 11 pm , hourly basis.
		//if(currHour >=6 && currHour <=23) 
		if(true){
			long delay =0;
//			if(min >1) {
//				delay= 60-min;
//			}
			service.scheduleAtFixedRate(new HourlyScheduledTask(), delay , 60 , TimeUnit.MINUTES);
		}
		service.scheduleAtFixedRate(new MidnightScheduledTask(), getTomorrowMorning1230am(),oneDayInMiliSec , TimeUnit.MILLISECONDS);
	}

	@Override
	public void contextDestroyed(ServletContextEvent servletContextEvent) {
		service.shutdownNow();
	}

	private static long getTomorrowMorning1230am() {

		Calendar tomorrow = new GregorianCalendar();
		tomorrow.add(Calendar.DATE, fONE_DAY);
		Calendar result = new GregorianCalendar(tomorrow.get(Calendar.YEAR),
				tomorrow.get(Calendar.MONTH), tomorrow.get(Calendar.DATE), fTWELVE_AM,
				fTHIRTY_MINUTES);
		Date today = new Date();
		long ms =TimeUnit.MILLISECONDS.convert(result.getTime().getTime()- today.getTime(),TimeUnit.MILLISECONDS);
		return ms;
	}

}
