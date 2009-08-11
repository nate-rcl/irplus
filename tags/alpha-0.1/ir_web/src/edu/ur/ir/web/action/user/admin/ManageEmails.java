/**  
   Copyright 2008 University of Rochester

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


package edu.ur.ir.web.action.user.admin;

import java.io.File;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.apache.struts2.interceptor.ServletRequestAware;

import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.Preparable;

import edu.ur.dao.CriteriaHelper;
import edu.ur.ir.NoIndexFoundException;
import edu.ur.ir.repository.Repository;
import edu.ur.ir.repository.RepositoryService;
import edu.ur.ir.user.IrRole;
import edu.ur.ir.user.IrUser;
import edu.ur.ir.user.UserEmail;
import edu.ur.ir.user.UserIndexService;
import edu.ur.ir.user.UserService;
import edu.ur.ir.web.action.UserIdAware;
import edu.ur.ir.web.table.PropertyConverter;
import edu.ur.ir.web.table.TableCollectionInfo;
import edu.ur.ir.web.table.TableRequestHelper;
import edu.ur.util.TokenGenerator;

/**
 * Action for managing the emails of the user
 * 
 * @author Sharmila Ranganathan
 *
 */
public class ManageEmails extends ActionSupport implements  
ServletRequestAware,  PropertyConverter, TableCollectionInfo, Preparable, UserIdAware{

	/** eclipse generated id */
	private static final long serialVersionUID = 4890200754192783056L;

	/**  Logger  */
	private static final Logger log = Logger.getLogger(ManageEmails.class);

	/** The request for the system. */
	private HttpServletRequest request;
	
	/** user service */
	private UserService userService;
	
	/** Set of emails  */
	private Collection<UserEmail> emails;
	
	/** Set of email ids */
	private List<Long> emailIds;
	
	/** total number of emails */
	private Integer totalNumberOfEmails;
	
	/**  Indicates the email has been added*/
	private boolean added = false;
	
	/** Indicates the emails have been deleted */
	private boolean deleted = false;
	
	/** id of the user  */
	private Long id;
	
	/** id of the user making the changes - could be admin - could be user */
	private Long userId;
	
	/** id of the email  */
	private Long emailId;
	
	/** Email of the user being created */
	private UserEmail email;
	
	/** User being edited */
	private IrUser irUser;
	
	/** Indicates whether the email is the default email or not  */
	private boolean defaultEmail;
	
	/** Service for indexing users */
	private UserIndexService userIndexService;
	
	/** Repository service for placing information in the repository */
	private RepositoryService repositoryService;
	
	/** Holds the old email before editing it */
	private String oldEmail;
	
	/** Message about email verification */
	private String emailVerificationMessage;
	


	/**
	 * Prepares for the action
	 * 
	 * @see com.opensymphony.xwork2.Preparable#prepare()
	 */
	public void prepare() throws Exception {
		log.debug( "Preparing id " + id);

		if( id != null)
		{
			irUser = userService.getUser(id, false);
		}
		
		if( emailId != null)
		{
			email = userService.getEmail(emailId, false);
		}
		
	}
	/**
	 * Create a new email.
	 * 
	 * @return
	 * @throws NoIndexFoundException 
	 */
	public String create() throws NoIndexFoundException
	{
		email.setEmail(email.getEmail().trim());
		IrUser otherUser = userService.getUserByEmail(email.getEmail());
		StringBuffer buffer = new StringBuffer();
		

		
		if (otherUser == null) {
			//user making the change
			IrUser changeMakingUser = userService.getUser(userId, false);
			
			// if they are not an administrator and trying to change
			// an account that does not belong to them then deny
			// access 
			if( !changeMakingUser.hasRole(IrRole.ADMIN_ROLE))
			{
				if( !changeMakingUser.equals(irUser))
				{
				    return "accessDenied";
				}
			}
			
			String emailToken = TokenGenerator.getToken();
			email.setToken(emailToken);
			irUser.addUserEmail(email, false);
			userService.makeUserPersistent(irUser);

			// send email With URL to verify email
			userService.sendEmailForEmailVerification(emailToken, email.getEmail(), irUser.getUsername());

			buffer.append("An email is sent to the address - " + email.getEmail() 
					+ ". Please follow the URL in the email to verify this email address.");
			
			if (defaultEmail) {
				buffer.append(
						"The email Id - " + email.getEmail() + " cannot be set as Default email until its verified.");				
			}
			
			Repository repository = repositoryService.getRepository(Repository.DEFAULT_REPOSITORY_ID,
						false);
			userIndexService.updateIndex(irUser, 
					new File( repository.getUserIndexFolder().getFullPath()) );
			emailVerificationMessage = buffer.toString();
			added = true;
		} else {
			addFieldError("emailExistError", 
					"This Email already exists in the system. Email: " + email.getEmail());
		}
		
		return "added";
	}
	
	/**
	 * Method to update an existing email.
	 * 
	 * @return
	 */
	public String update()
	{
		log.debug("updating email  = " + email);

		StringBuffer buffer = new StringBuffer();
		email.setEmail(email.getEmail().trim());
		
		UserEmail userEmail = userService.getUserEmailByEmail(email.getEmail());

		if ((userEmail == null) || (emailId.equals(userEmail.getId()))) {
			
			//user making the change
			IrUser changeMakingUser = userService.getUser(userId, false);
			
			// if they are not an administrator and trying to change
			// an account that does not belong to them then deny
			// access 
			if( !changeMakingUser.hasRole(IrRole.ADMIN_ROLE))
			{
				if( !changeMakingUser.equals(irUser))
				{
				    return "accessDenied";
				}
			}

			if (!email.getEmail().equals(oldEmail)) {
			    String emailToken = TokenGenerator.getToken();
				email.setToken(emailToken);
				email.setVerified(false);
				
				// send email With URL to verify email
				userService.sendEmailForEmailVerification(emailToken, email.getEmail(), irUser.getUsername());

				buffer.append("An email is sent to the address - " + email.getEmail() 
					+ ". Please follow the URL in the email to verify this email address.");
			} 
			
			if( defaultEmail)
		    {
				if (email.isVerified()) {
			    	if (irUser.changeDefaultEmail(email.getId()))
			    	{
			    		log.debug("Default email changed!");
			    	}
			    	else
			    	{
			    		log.debug("DIDN't change!");
			    	}
				} else {
					buffer.append(
							"The email Id - " + email.getEmail() + " cannot be set as Default email until its verified.");

				}
		    }
			
			emailVerificationMessage = buffer.toString();
			userService.makeUserPersistent(irUser);
			added = true;
		} else {
			addFieldError("emailExistError", 
					"This Email already exists in the system. Email: " + email.getEmail());
		}
	    return "added";
	}
	
	/**
	 * Removes the selected emails.
	 * 
	 * @return
	 */
	public String delete()
	{
		log.debug("Delete emails called" + emailIds);
		
		//user making the change
		IrUser changeMakingUser = userService.getUser(userId, false);
		
		if( emailIds != null )
		{
		    for(Long emailId :emailIds)
		    {
			    UserEmail removeEmail = userService.getEmail(emailId, false);
			    if( !removeEmail.getIrUser().equals(changeMakingUser) && !changeMakingUser.hasRole(IrRole.ADMIN_ROLE))
			    {
			    	 return "accessDenied";
			    }
			    userService.makeEmailTransient(removeEmail);
		    }
		}
		deleted = true;
		return "deleted";
	}
	
	/**
	 * Get the emails table data.
	 * 
	 * @return
	 */
	public String viewEmails()
	{
		//user making the change
		IrUser changeMakingUser = userService.getUser(userId, false);
		
		// if they are not an administrator and trying to view
		// an account that does not belong to them then deny
		// access 
		if( !changeMakingUser.hasRole(IrRole.ADMIN_ROLE))
		{
			if( !changeMakingUser.getId().equals(id))
			{
			    return "accessDenied";
			}
		}
		
		TableRequestHelper helper = new TableRequestHelper();
		helper.processTableData(request, this, this);
		List<CriteriaHelper> criteria = helper.getCriteriaHelpers();
		
		emails = userService.getEmails(id, criteria, 
				helper.getRowStart(), helper.getRowEnd());
		return SUCCESS;

	}
	
	/**
	 * Set the servlet request.
	 * 
	 * @see org.apache.struts2.interceptor.ServletRequestAware#setServletRequest(javax.servlet.http.HttpServletRequest)
	 */
	public void setServletRequest(HttpServletRequest request) {
		this.request = request;
	}

	/**
	 * Convert the string types to the nessesary types for searching.
	 * 
	 * @see edu.ur.ir.web.table.PropertyConverter#convertValue(java.lang.String, java.lang.String)
	 */
	public Object convertValue(String property, String value) {
		Object returnValue = value;
        if( property.equalsIgnoreCase("id") )
		{
			String tempValue = value.toString();
			returnValue = new Long(tempValue);
		}
        
        return returnValue;
	}

	/**
	 * Get the total number of results.
	 * 
	 * @see edu.ur.ir.web.table.TableCollectionInfo#getTotalNumberOfResults(java.util.List)
	 */
	public int getTotalNumberOfResults(List<CriteriaHelper> criteriaHelpers) {
		totalNumberOfEmails = userService.getEmailCount(id, criteriaHelpers);
		log.debug("Total number of results = " + totalNumberOfEmails);
		return totalNumberOfEmails;

	}
	
	/**
	 * Get the user service 
	 * 
	 * @return user service
	 * 
	 */
	public UserService getUserService() {
		return userService;
	}

	/**
	 * Set the user service
	 * 
	 * @param userService user service
	 */
	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	/**
	 * Get emails for this user
	 * 
	 * @return Collection of emails for the user
	 */
	public Collection<UserEmail> getEmails() {
		return emails;
	}

	/**
	 * Set emails for this user
	 * 
	 * @param emails Collection of user emails
	 */
	public void setEmails(Collection<UserEmail> emails) {
		this.emails = emails;
	}

	/**
	 * Get the number of emails for this user
	 * 
	 * @return number of emails
	 */
	public Integer getTotalNumberOfEmails() {
		return totalNumberOfEmails;
	}

	/**
	 * Set the number of emails for this user
	 * 
	 * @param totalNumberOfEmails number of emails for the user
	 */
	public void setTotalNumberOfEmails(Integer totalNumberOfEmails) {
		this.totalNumberOfEmails = totalNumberOfEmails;
	}

	/**
	 * Indicates whether the email is added
	 * 
	 * @return Returns true if email added else returns false
	 */
	public boolean isAdded() {
		return added;
	}

	/**
	 * Set true if email added else set false
	 * 
	 * @param added True if email added else false
	 */
	public void setAdded(boolean added) {
		this.added = added;
	}

	/**
	 * Indicates whether the email is deleted
	 * 
	 * @return Returns true if email deleted else returns false
	 */
	public boolean isDeleted() {
		return deleted;
	}

	/**
	 * Set true if email deleted else set false
	 * 
	 * @param deleted True if email deleted else false
	 */
	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	/**
	 * Get the user id
	 * 
	 * @return id of the user
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Set the id of the user 
	 * 
	 * @param id user id
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * Get email that is created or edited
	 * 
	 * @return email
	 */
	public UserEmail getEmail() {
		return email;
	}

	/**
	 * Set email that is created or edited
	 * 
	 * @param email email information
	 */
	public void setEmail(UserEmail email) {
		this.email = email;
	}

	/**
	 * Get user that is being edited
	 * 
	 * @return user information
	 */
	public IrUser getIrUser() {
		return irUser;
	}

	/**
	 * Set user that is being edited
	 * 
	 * @param irUser user information
	 */
	public void setIrUser(IrUser irUser) {
		this.irUser = irUser;
	}
	
	/**
	 * Get email ids that needs to be deleted
	 * 
	 * @return List of email ids
	 */
	public List<Long> getEmailIds() {
		return emailIds;
	}
	
	/**
	 * Set email ids to be deleted
	 * 
	 * @param emailIds List of email ids
	 */
	public void setEmailIds(List<Long> emailIds) {
		this.emailIds = emailIds;
	}
	
	/**
	 * Get email Id that is being edited
	 * 
	 * @return id of the email
	 */
	public Long getEmaild() {
		return emailId;
	}
	
	/**
	 * Set email id 
	 * 
	 * @param emailId id of the email
	 */
	public void setEmailId(Long emailId) {
		this.emailId = emailId;
	}
	
	/**
	 * Returns true if the email is default email else returns false
	 * 
	 * @return True if the email is default email else returns false
	 */
	public boolean isDefaultEmail() {
		return defaultEmail;
	}
	
	/**
	 * Set true if the email is default email else set false
	 * 
	 * @param defaultEmail True if the email is default email else false
	 */
	public void setDefaultEmail(boolean defaultEmail) {
		this.defaultEmail = defaultEmail;
	}
	public UserIndexService getUserIndexService() {
		return userIndexService;
	}
	public void setUserIndexService(UserIndexService userIndexService) {
		this.userIndexService = userIndexService;
	}
	public RepositoryService getRepositoryService() {
		return repositoryService;
	}
	public void setRepositoryService(RepositoryService repositoryService) {
		this.repositoryService = repositoryService;
	}
	
	public String getOldEmail() {
		return oldEmail;
	}
	
	public void setOldEmail(String oldEmail) {
		this.oldEmail = oldEmail;
	}

	public String getEmailVerificationMessage() {
		return emailVerificationMessage;
	}
	
	public void setUserId(Long userId) {
		this.userId = userId;
	}
}

