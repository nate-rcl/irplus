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


package edu.ur.ir.web.action.user;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.opensymphony.xwork2.ActionSupport;

import edu.ur.ir.security.SecurityService;
import edu.ur.ir.user.IrUser;
import edu.ur.ir.user.PersonalFile;
import edu.ur.ir.user.UserFileSystemService;
import edu.ur.ir.user.UserService;
import edu.ur.ir.web.action.UserIdAware;

/**
 * Action to change personal file owner
 * 
 * @author Sharmila Ranganathan
 *
 */
public class ChangeOwnerForPersonalFile extends ActionSupport implements UserIdAware{

	/** Eclipse generated Id	 */
	private static final long serialVersionUID = -7262077243101493697L;
	
	/** User file system service. */
	private UserFileSystemService userFileSystemService;
	
	/** User service. */
	private UserService userService;
	
	/**  Logger for action */
	private static final Logger log = LogManager.getLogger(ChangeOwnerForPersonalFile.class);
	
	/** Id of personal file */
	private Long personalFileId;
	
	/** Collaborator to be made as new owner */
	private Long newOwnerId;
	
	/** user making the change */
	private Long userId;
	
	/** reason for access denied if access is denied */
	private String reason;

	/** security service to deal with files */
	private SecurityService securityService;



	/**
	 * changes the file owner 
	 * 
	 * @return
	 */
	public String execute() {
		
		log.debug("Change owner Personal file::" + personalFileId);
		PersonalFile personalFile = userFileSystemService.getPersonalFile(personalFileId, false);
		
		// user must be an owner of the file
		if(!personalFile.getVersionedFile().getOwner().getId().equals(userId))
		{
			reason = "You are not the owner";
			return "accessDenied";
		}
		
		IrUser newOwner = userService.getUser(newOwnerId, false);
		personalFile.getVersionedFile().changeOwner(newOwner);
		userFileSystemService.makePersonalFilePersistent(personalFile);
		securityService.assignOwnerPermissions(personalFile.getVersionedFile(), newOwner);
		
		// set the permissions for this user as the owner.
		
		return SUCCESS;
	}
	
	/**
	 * reason for access denied.
	 * 
	 * @return
	 */
	public String getReason() {
		return reason;
	}


	public void setReason(String reason) {
		this.reason = reason;
	}


	/**
	 * Get User file system service
	 *  
	 * @return
	 */
	public UserFileSystemService getUserFileSystemService() {
		return userFileSystemService;
	}

	/**
	 * Set User file system service
	 * 
	 * @param userFileSystemService
	 */
	public void setUserFileSystemService(UserFileSystemService userFileSystemService) {
		this.userFileSystemService = userFileSystemService;
	}

	/**
	 * Get personal file id
	 * 
	 * @return
	 */
	public Long getPersonalFileId() {
		return personalFileId;
	}

	/**
	 * Set personal file id
	 * 
	 * @param personalFileId
	 */
	public void setPersonalFileId(Long personalFileId) {
		this.personalFileId = personalFileId;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}


	public Long getNewOwnerId() {
		return newOwnerId;
	}


	public void setNewOwnerId(Long newOwnerId) {
		this.newOwnerId = newOwnerId;
	}


	public void setUserId(Long userId) {
		this.userId = userId;
	}
	
	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}


}
