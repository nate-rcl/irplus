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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.opensymphony.xwork2.ActionSupport;

import edu.ur.cgLib.CgLibHelper;
import edu.ur.ir.file.FileCollaborator;
import edu.ur.ir.file.VersionedFile;
import edu.ur.ir.repository.Repository;
import edu.ur.ir.repository.RepositoryService;
import edu.ur.ir.security.IrAcl;
import edu.ur.ir.security.IrClassTypePermission;
import edu.ur.ir.security.SecurityService;
import edu.ur.ir.user.FileSharingException;
import edu.ur.ir.user.InviteInfo;
import edu.ur.ir.user.InviteUserService;
import edu.ur.ir.user.IrRole;
import edu.ur.ir.user.IrUser;
import edu.ur.ir.user.PersonalFile;
import edu.ur.ir.user.RoleService;
import edu.ur.ir.user.SharedInboxFile;
import edu.ur.ir.user.UserFileSystemService;
import edu.ur.ir.user.UserService;
import edu.ur.ir.user.UserWorkspaceIndexService;
import edu.ur.ir.web.action.UserIdAware;
import edu.ur.util.TokenGenerator;

/**
 * Action to invite user
 * 
 * @author Sharmila Ranganathan
 *
 */
public class InviteUser extends ActionSupport implements UserIdAware {

	/**
	 * Eclipse generated id
	 */
	private static final long serialVersionUID = -30583149959913239L;

	/**  Logger for invite user action */
	private static final Logger log = Logger.getLogger(InviteUser.class);
	
	/** Invite user data access */
	private InviteUserService inviteUserService;
	
	/** User data access */
	private UserService userService;
	
	/** Role data access */
	private RoleService roleService;
	
	/**  Indicates the user has been invited */
	private boolean inviteSent = false;

	/**  Indicates the permissions has be added */
	private boolean added = false;

	/**  User object */
	private Long userId;
	
	/**  Email id to invite user */
	private String email;

	/**  Personal file id that is shared */
	private Long personalFileId;
	
	/** Message that is sent to the user in the email. */
	private String inviteMessage;
	
	/** Message that can be displayed to the user. */
	private String inviteErrorMessage;
	
	/** File collaborator id to remove the collaborator */
	private Long fileCollaboratorId;

	/** ACL service */
	private  SecurityService securityService;
	
	/** Permissions to be assigned while sharing the file */
	private List<Long> selectedPermissions = new ArrayList<Long>();	
	
	/** Permissions for the collaborator */
	private Set<IrClassTypePermission> collaboratorPermissions;	

	/** Permission types for the file */
	private List<IrClassTypePermission> classTypePermissions;
	
	/** User file system management services. */
	private UserFileSystemService userFileSystemService;
	
	/** Invite info Id to be deleted */
	private Long inviteInfoId;
	
	/** The personal file's parent folder id */
	private Long parentFolderId;

	/** list of folder ids to perform actions on*/
	private Long[] folderIds;
	
	/** list of file ids to perform actions on*/
	private Long[] fileIds;

	private List<PersonalFile> filesToShare; 
	
	/** list of file ids to perform actions on*/
	private String shareFileIds;
	
	/** List of file names that do not have share permission */
	private List<PersonalFile> filesWithNoSharePermission;
	
	/** Indicates whether all the files have share permission */
	private boolean hasSharePermission;
	
	/** Share permission */
	private static final String SHARE = "SHARE";
	
	/** User index service for indexing files */
	private UserWorkspaceIndexService userWorkspaceIndexService;
	
	/** Repository service for placing information in the repository */
	private RepositoryService repositoryService;


	/**
	 * Get collaborators for the personal file
	 */
	public String viewCollaborators()
	{
		log.debug("Get collaborators");

		filesToShare = new LinkedList<PersonalFile>();
		
		if( shareFileIds != null )
		{
			List<Long> parsedFileIds = new LinkedList<Long>();
		    StringTokenizer tokenizer = new StringTokenizer(shareFileIds, ",");

		    while(tokenizer.hasMoreElements())
		    {
		    	Long fileId = new Long(tokenizer.nextToken());
				parsedFileIds.add(fileId);
			}
			
		    // files to share are accessed using user id - so no unauthorized 
		    // access can occur
		    filesToShare = userFileSystemService.getFiles(userId, parsedFileIds);
		}
		return SUCCESS;

	}
	
	/**
	 * Checks if the user has share permission on the selected files
	 */
	public String checkFileSharePermission()
	{
		hasSharePermission = false;
		
		StringBuffer buffer = new StringBuffer();
		filesWithNoSharePermission = new LinkedList<PersonalFile>();
		
		IrUser user = userService.getUser(userId, false);
		
		if( fileIds != null )
		{
			for( Long fileId : fileIds)
			{
				PersonalFile personalFile = userFileSystemService.getPersonalFile(fileId, false);
				IrAcl acl = securityService.getAcl(personalFile.getVersionedFile(), user);
				
				// Check if the user has SHARE permission for the file
				if (acl.isGranted(SHARE, user, false)) {
					buffer.append(fileId);
					buffer.append(",");
				} else {
					filesWithNoSharePermission.add(personalFile);
				}
			}
			
		}

		if( folderIds != null )
		{
			List<PersonalFile> files = new LinkedList<PersonalFile>();
			
			// Get all the files from the selected folders and its sub folders
			for( Long folderId : folderIds)
			{
				files.addAll(userFileSystemService.getAllFilesInFolderAndSubFolder(folderId, userId));
			}
			
			// Check if the user has SHARE permission for the file 
			for( PersonalFile file : files)
			{
				IrAcl acl = securityService.getAcl(file.getVersionedFile(), user);
				
				if (acl.isGranted(SHARE, user, false)) {
					buffer.append(file.getId());
					buffer.append(",");
				} else {
					filesWithNoSharePermission.add(file);
				}
			}

		}
 
		if (filesWithNoSharePermission.size() == 0) {
			hasSharePermission = true;
		}
		
		shareFileIds =  buffer.toString();
		
	    return SUCCESS;
	}
	
	/**
	 * Initialize the invite user page with permissions
	 */
	public String execute() {

		classTypePermissions = securityService.getClassTypePermissions(VersionedFile.class.getName());

	    return SUCCESS;
		
	}
	
	/**
	 * Get the user for this folder
	 * 
	 * @return
	 */
	public Long getUserId() {
		return userId;
	}
	
	/**
	 * Set the user for this user.
	 * 
	 * @see edu.ur.ir.web.action.UserAware#setOwner(edu.ur.ir.user.IrUser)
	 */
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	

	/**
	 * Send Invite for a user to collaborate on a document
	 */
	public String sendInvite() {
		inviteSent = false;
		
		// Create invite info object
		IrUser invitingUser = userService.getUser(userId, true);
		
		StringTokenizer tokenizer = new StringTokenizer(shareFileIds, ",");
		
		List<PersonalFile> personalFilesToShare = new LinkedList<PersonalFile>();

		// Make a list of files that has to be shared - verify permissions to share
	    while(tokenizer.hasMoreElements())
	    {
	    	Long fileId = new Long(tokenizer.nextToken());

			PersonalFile personalFile = userFileSystemService.getPersonalFile(fileId, false);
			IrAcl acl = securityService.getAcl(personalFile.getVersionedFile(), invitingUser);
			if( acl == null || !acl.isGranted(SHARE, invitingUser, false))
			{
				return("accessDenied");
			}
			
			log.debug("Inviting a user to collaborate on personal file ID = " + personalFile.toString() );
	
			personalFilesToShare.add(personalFile);

	    }
		
	
		
		if (selectedPermissions.size() == 0) {
			inviteErrorMessage = getText("emptyPermissions");
			return "added";
		}
		
		email = email.trim();
		
		// Check if user exist in the system
		IrUser invitedUser = userService.getUserForVerifiedEmail(email);


		
		//check if the user is sharing the file with themselves
		if (invitingUser.equals(invitedUser)) {
			inviteErrorMessage = getText("sharingWithYourself");
			return "added";
		}		
		
		Set<VersionedFile>  versionedFiles = new HashSet<VersionedFile>();
		for(PersonalFile pf : personalFilesToShare)
		{
			VersionedFile versionedFile = pf.getVersionedFile();
			
			//check if the file is already shared with this user 
			//AND 
			//check if email is already sent to this Id for sharing and the user has not created the account yet
			if ((versionedFile.getCollaborator(invitedUser) == null) && (!versionedFile.getInviteeEmails().contains(email))) {
				versionedFiles.add(versionedFile);
			}
		}

	
		InviteInfo inviteInfo
		= new InviteInfo(invitingUser, versionedFiles);
	
		inviteInfo.setEmail(email);
		inviteInfo.setInviteMessage(inviteMessage);
	    
		// Create the list of permissions
		Set<IrClassTypePermission> permissions = new HashSet<IrClassTypePermission>();
		
		for(Long id : selectedPermissions)
		{
			permissions.add(securityService.getIrClassTypePermissionById(id, false));
		}			

		
		/* If user exist in the system then share the file and send email*/
		if (invitedUser != null) {
			
			// If the shared user has no Author or collaborator or researcher or admin role, then assign collaborator role
			if (!invitedUser.hasRole(IrRole.AUTHOR_ROLE) && !invitedUser.hasRole(IrRole.COLLABORATOR_ROLE) 
					&& !invitedUser.hasRole(IrRole.RESEARCHER_ROLE) && !invitedUser.hasRole(IrRole.ADMIN_ROLE)) {
				
				invitedUser.addRole(roleService.getRole(IrRole.COLLABORATOR_ROLE));
				userService.makeUserPersistent(invitedUser);
			}

			for (VersionedFile file : versionedFiles) {
				try {
					SharedInboxFile sif = inviteUserService.shareFile(invitingUser, invitedUser, file);
					userWorkspaceIndexService.addToIndex(getRepository(), sif);
				} catch (FileSharingException e1) {
					throw new RuntimeException("This should never happen", e1);
				}
				
				// Create permissions for the file that is being shared
				securityService.createPermissions(file, invitedUser, permissions);
			}

			try {
				inviteUserService.sendEmailToExistingUser(inviteInfo);
				inviteSent = true;
			} catch(IllegalStateException e) {
				inviteErrorMessage = getText("emailNotSent", new String[]{email});
			}
				
		} else {
			
			/* If user does not exist in the system then get a token and send email with the token */
			inviteInfo.setToken(TokenGenerator.getToken());
			inviteInfo.setPermissions(permissions);

			try {
				inviteUserService.sendEmailToNotExistingUser(inviteInfo);
				inviteUserService.makeInviteInfoPersistent(inviteInfo);
				inviteSent = true;
			} catch(IllegalStateException e) {
				inviteErrorMessage = getText("emailIncorrect", new String[]{email});
			}
		}
		
        return "added";
	}

	/**
	 * UnShare a file - Remove collaborator 
	 */
	public String deleteCollaborator() {

		String returnResult = SUCCESS;
		FileCollaborator fileCollaborator = inviteUserService.findFileCollaborator(fileCollaboratorId, true);
		
		// make sure user doing the un-inviting is valid
		IrUser unInvitingUser = userService.getUser(userId, true);
		
		IrAcl acl = securityService.getAcl(fileCollaborator.getVersionedFile(), unInvitingUser);
		if( acl == null || !acl.isGranted(SHARE, unInvitingUser, false))
		{
		    return("accessDenied");
		}
		
		IrUser user = fileCollaborator.getCollaborator();

		// If user is unsharing themselves, then unshare the user and load the parent folder
		if (userId.equals(user.getId())) {
			returnResult =  "workspace";
		} 

		// un-index the file
		PersonalFile pf = userFileSystemService.getPersonalFile(user, fileCollaborator.getVersionedFile());

		// Check if personal file exist. Sometimes the file may be still in Shared file inbox.
		// In that case, there is no need to delete from index
		if (pf != null) {
			userWorkspaceIndexService.deleteFromIndex(pf);
		}
		else
		{
			SharedInboxFile sif = user.getSharedInboxFile(fileCollaborator.getVersionedFile());
			if( sif != null )
			{
				userWorkspaceIndexService.deleteFromIndex(sif);
			}
		}
		
		inviteUserService.unshareFile(fileCollaborator);
		
		return returnResult;
	}
		
	/**
	 * Remove pending invitee
	 */
	public String deletePendingInvitee() {
		log.debug("Removing Pending invit info Id= " + inviteInfoId);
		
		// verify access to unshare
		PersonalFile unsharingUserPersonalFile = userFileSystemService.getPersonalFile(personalFileId, false);
		
		if(unsharingUserPersonalFile != null)
		{
		    // make sure user doing the un-inviting is valid
		    IrUser unInvitingUser = userService.getUser(userId, true);
		
		    IrAcl acl = securityService.getAcl(unsharingUserPersonalFile.getVersionedFile(), unInvitingUser);
		    if( acl == null || !acl.isGranted(SHARE, unInvitingUser, false))
		    {
			    return("accessDenied");
		    }
		}
		else
		{
			return("accessDenied");
		}

		InviteInfo inviteInfo = inviteUserService.getInviteInfoById(inviteInfoId, false);
		
		PersonalFile personalFile = userFileSystemService.getPersonalFile(personalFileId, false);
	
		inviteInfo.removeFile(personalFile.getVersionedFile());
		
		inviteUserService.makeInviteInfoPersistent(inviteInfo);
		
		return SUCCESS;
	}
	
	/**
	 * Get permissions for a collaborator on a particular file
	 * 
	 * @param fileCollaborator collaborator for a versioned file
	 * 
	 * @return permissions for a collaborator on a file 
	 */
	public String getPermissions() {

		
		FileCollaborator fileCollaborator 
		= inviteUserService.findFileCollaborator(fileCollaboratorId, true);

		  collaboratorPermissions = securityService.getPermissions(fileCollaborator.getVersionedFile(), fileCollaborator.getCollaborator());
		
		  classTypePermissions = securityService.getClassTypePermissions(
				  CgLibHelper.cleanClassName(fileCollaborator.getVersionedFile().getClass().getName()));
			
		  return SUCCESS;
		
	}
	
	/**
	 * Update the permissions for a collaborator
	 * 
	 * @return SUCCESS if update was sucess
	 */
	public String updatePermissions() {
		
		log.debug("Update Permissions for :" + fileCollaboratorId);
		FileCollaborator fileCollaborator  = inviteUserService.findFileCollaborator(fileCollaboratorId, true);
		
		// verify access to unshare
		VersionedFile versionedFile = fileCollaborator.getVersionedFile();

		// make sure user doing the un-inviting is valid
		IrUser changingPermissionsUser = userService.getUser(userId, true);
		
		IrAcl acl = securityService.getAcl(versionedFile, changingPermissionsUser);
		if( acl == null || !acl.isGranted(SHARE, changingPermissionsUser, false))
		{
		    return("accessDenied");
		}
		
		
		added = false;

		if (selectedPermissions.size() == 0) {
			inviteErrorMessage = getText("emptyPermissions");
			addFieldError("emptyPermissions", inviteErrorMessage);
			return "added";
		}
		

		
		securityService.deletePermissions(fileCollaborator.getVersionedFile().getId(),
				CgLibHelper.cleanClassName(fileCollaborator.getVersionedFile().getClass().getName()), fileCollaborator.getCollaborator());
		
		List<IrClassTypePermission> permissions = new ArrayList<IrClassTypePermission>();
		
		for(Long id : selectedPermissions)
		{
			permissions.add(securityService.getIrClassTypePermissionById(id, false));
		}
		// Create permissions for the file that is being shared
		securityService.createPermissions(fileCollaborator.getVersionedFile(), 
				fileCollaborator.getCollaborator(), permissions);
		
		added = true;
	
		return "added";
		
	}
	
	/**
	 * Remove a file  
	 */
	public String removeFile(){
		
		log.debug("Remove FileId = " + personalFileId);

		StringTokenizer tokenizer = new StringTokenizer(shareFileIds, ",");
	    StringBuffer buffer = new StringBuffer();

	    while(tokenizer.hasMoreElements())
	    {
	    	Long fileId = new Long(tokenizer.nextToken());
	    	if (!fileId.equals(personalFileId)) {
	    		buffer.append(fileId);
	    		buffer.append(",");
	    	}
		}
		
		shareFileIds = buffer.toString();
		
		return SUCCESS;
	}

	/**
	 * Get the Invite user service for dealing with action
	 * 
	 * @return invite user service 
	 */
	public InviteUserService getInviteUserService() {
		return inviteUserService;
	}

	/**
	 * Set Invite user service 
	 * 
	 * @param inviteUserService invite user service object
	 */
	public void setInviteUserService(InviteUserService inviteUserService) {
		this.inviteUserService = inviteUserService;
	}

	/**
	 * Get email id to send invitation
	 *  
	 * @return
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * Set email id to send invitation
	 * @param email
	 */
	public void setEmail(String email) {
		this.email = email;
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
	 * @param personalFileId
	 */
	public void setPersonalFileId(Long personalFileId) {
		this.personalFileId = personalFileId;
	}

	/**
	 * Indicates if the invite has been added 
	 * 
	 * @return
	 */
	public boolean isInviteSent() {
		return inviteSent;
	}

	/**
	 * Get the invite message
	 * 
	 * @return
	 */
	public String getInviteMessage() {
		return inviteMessage;
	}

	/**
	 * Get user data service
	 * 
	 * @return
	 */
	public UserService getUserService() {
		return userService;
	}

	/**
	 * Set the service class for user
	 * 
	 * @param userService
	 */
	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	/**
	 * Get file collaborator id
	 * 
	 * @return
	 */
	public Long getFileCollaboratorId() {
		return fileCollaboratorId;
	}

	/**
	 * Set file collaborator id
	 * 
	 * @param fileCollaboratorId
	 */
	public void setFileCollaboratorId(Long fileCollaboratorId) {
		this.fileCollaboratorId = fileCollaboratorId;
	}

	public SecurityService getSecurityService() {
		return securityService;
	}

	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}

	public List<Long> getSelectedPermissions() {
		return selectedPermissions;
	}

	public void setSelectedPermissions(List<Long> selectedPermissions) {
		this.selectedPermissions = selectedPermissions;
	}

	public List<IrClassTypePermission> getClassTypePermissions() {
		return classTypePermissions;
	}

	public void setClassTypePermissions(
			List<IrClassTypePermission> classTypePermissions) {
		this.classTypePermissions = classTypePermissions;
	}

	public void setInviteMessage(String inviteMessage) {
		this.inviteMessage = inviteMessage;
	}

	public Set<IrClassTypePermission> getCollaboratorPermissions() {
		return collaboratorPermissions;
	}

	public void setCollaboratorPermissions(
			Set<IrClassTypePermission> collaboratorPermissions) {
		this.collaboratorPermissions = collaboratorPermissions;
	}

	public boolean isAdded() {
		return added;
	}

	public void setAdded(boolean added) {
		this.added = added;
	}

	public UserFileSystemService getUserFileSystemService() {
		return userFileSystemService;
	}

	public void setUserFileSystemService(UserFileSystemService userFileSystemService) {
		this.userFileSystemService = userFileSystemService;
	}

	public Long getInviteInfoId() {
		return inviteInfoId;
	}

	public void setInviteInfoId(Long inviteInfoId) {
		this.inviteInfoId = inviteInfoId;
	}

	public Long getParentFolderId() {
		return parentFolderId;
	}

	public void setParentFolderId(Long parentFolderId) {
		this.parentFolderId = parentFolderId;
	}

	public String getInviteErrorMessage() {
		return inviteErrorMessage;
	}

	public void setInviteErrorMessage(String inviteErrorMessage) {
		this.inviteErrorMessage = inviteErrorMessage;
	}

	public Long[] getFolderIds() {
		return folderIds;
	}

	public void setFolderIds(Long[] folderIds) {
		this.folderIds = folderIds;
	}

	public Long[] getFileIds() {
		return fileIds;
	}

	public void setFileIds(Long[] fileIds) {
		this.fileIds = fileIds;
	}

	public List<PersonalFile> getFilesToShare() {
		return filesToShare;
	}

	public void setFilesToShare(List<PersonalFile> filesToShare) {
		this.filesToShare = filesToShare;
	}


	public String getShareFileIds() {
		return shareFileIds;
	}

	public void setShareFileIds(String shareFileIds) {
		this.shareFileIds = shareFileIds;
	}

	public List<PersonalFile> getFilesWithNoSharePermission() {
		return filesWithNoSharePermission;
	}

	public void setFilesWithNoSharePermission(
			List<PersonalFile> filesWithNoSharePermission) {
		this.filesWithNoSharePermission = filesWithNoSharePermission;
	}

	public boolean isHasSharePermission() {
		return hasSharePermission;
	}

	public void setHasSharePermission(boolean hasSharePermission) {
		this.hasSharePermission = hasSharePermission;
	}

	public RepositoryService getRepositoryService() {
		return repositoryService;
	}

	public void setRepositoryService(RepositoryService repositoryService) {
		this.repositoryService = repositoryService;
	}

	private Repository getRepository()
	{
    		Repository repository = repositoryService.getRepository(Repository.DEFAULT_REPOSITORY_ID,
					false);
    		return repository;

	}
	
	public UserWorkspaceIndexService getUserWorkspaceIndexService() {
		return userWorkspaceIndexService;
	}

	public void setUserWorkspaceIndexService(
			UserWorkspaceIndexService userWorkspaceIndexService) {
		this.userWorkspaceIndexService = userWorkspaceIndexService;
	}

	public RoleService getRoleService() {
		return roleService;
	}

	public void setRoleService(RoleService roleService) {
		this.roleService = roleService;
	}


	

}
