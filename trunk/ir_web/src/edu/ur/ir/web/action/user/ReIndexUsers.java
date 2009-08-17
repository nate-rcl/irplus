package edu.ur.ir.web.action.user;

import org.apache.log4j.Logger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.TriggerUtils;

import com.opensymphony.xwork2.ActionSupport;

/**
 * This re-indexes all users in the system.
 * 
 * @author Nathan Sarr
 *
 */
public class ReIndexUsers extends ActionSupport{
	

	/** eclipse generated id */
	private static final long serialVersionUID = -3469784982224968731L;

	/** Quartz scheduler instance to schedule jobs  */
	private Scheduler quartzScheduler;
	
	/**  Get the logger for this class */
	private static final Logger log = Logger.getLogger(ReIndexUsers.class);
	
	public String execute() throws Exception
	{
		log.debug("re index users called");
		//create the job detail
		JobDetail jobDetail = new JobDetail("reIndexUsersJob", Scheduler.DEFAULT_GROUP, 
				edu.ur.ir.user.service.DefaultReIndexUsersJob.class);
		
		jobDetail.getJobDataMap().put("batchSize", new Integer(batchSize));
		
		//create a trigger that fires once right away
		Trigger trigger = TriggerUtils.makeImmediateTrigger(0,0);
		trigger.setName("SingleReIndexItemsJobFireNow");
		quartzScheduler.scheduleJob(jobDetail, trigger);
		
		return SUCCESS;
	}
	
	public Scheduler getQuartzScheduler() {
		return quartzScheduler;
	}

	public void setQuartzScheduler(Scheduler quartzScheduler) {
		this.quartzScheduler = quartzScheduler;
	}

	public int getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}

	/** Default Batch Size */
	private int batchSize = 1;

}