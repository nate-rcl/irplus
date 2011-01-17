/**  
   Copyright  - 2011 University of Rochester

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/  

package edu.ur.ir.web.action.user;

import org.apache.log4j.Logger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.TriggerUtils;

import com.opensymphony.xwork2.ActionSupport;

/**
 * Re - index all user groups 
 * 
 * @author Nathan Sarr
 *
 */
public class ReIndexUserGroups extends ActionSupport{
	
	/* eclipse generated id */
	private static final long serialVersionUID = 6796256135001803887L;

	/* Quartz scheduler instance to schedule jobs  */
	private transient Scheduler quartzScheduler;
	
	/**  Get the logger for this class */
	private static final Logger log = Logger.getLogger(ReIndexUserGroups.class);
	
	/* Default Batch Size */
	private int batchSize = 25;
	
	public String execute() throws Exception
	{
		log.debug("re index users called");
		//create the job detail
		JobDetail jobDetail = new JobDetail("reIndexUserGroupsJob", Scheduler.DEFAULT_GROUP, 
				edu.ur.ir.user.service.DefaultReIndexUserGroupsJob.class);
		
		jobDetail.getJobDataMap().put("batchSize", Integer.valueOf(batchSize));
		
		//create a trigger that fires once right away
		Trigger trigger = TriggerUtils.makeImmediateTrigger(0,0);
		trigger.setName("SingleReIndexUserGroupsJobFireNow");
		quartzScheduler.scheduleJob(jobDetail, trigger);
		
		return SUCCESS;
	}
	
	public void setQuartzScheduler(Scheduler quartzScheduler) {
		this.quartzScheduler = quartzScheduler;
	}

	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}


}
