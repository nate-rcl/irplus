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

package edu.ur.ir.web.action.institution;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.opensymphony.xwork2.ActionSupport;


import edu.ur.exception.DuplicateNameException;
import edu.ur.ir.index.IndexProcessingType;
import edu.ur.ir.index.IndexProcessingTypeService;
import edu.ur.ir.institution.InstitutionalCollection;
import edu.ur.ir.institution.InstitutionalCollectionSecurityService;
import edu.ur.ir.institution.InstitutionalCollectionService;
import edu.ur.ir.institution.InstitutionalItemIndexProcessingRecordService;
import edu.ur.ir.repository.Repository;
import edu.ur.ir.repository.RepositoryService;
import edu.ur.ir.security.IrAcl;
import edu.ur.ir.security.IrClassTypePermission;
import edu.ur.ir.security.IrUserGroupAccessControlEntry;
import edu.ur.ir.user.IrRole;
import edu.ur.ir.user.IrUser;
import edu.ur.ir.user.IrUserGroup;
import edu.ur.ir.user.UserGroupService;
import edu.ur.ir.user.UserService;
import edu.ur.ir.web.action.UserIdAware;

/**
 * Manage an institutional collection.
 * 
 * @author Nathan Sarr
 *
 */
public class EditInstitutionalCollection extends ActionSupport implements UserIdAware {
	
	/** id of the user making changes */
	private Long userId;

	/**  Logger. */
	private static final Logger log = LogManager.getLogger(EditInstitutionalCollection.class);

	/**  Generated version id */
	private static final long serialVersionUID = 3604761597348556125L;

	/**  Parent Collection id */
	private Long parentCollectionId= 0l;
	
	/** Repository service for dealing with institutional repository information */
	private RepositoryService repositoryService;
	
	/** Repository service for dealing with institutional repository information */
	private InstitutionalCollectionSecurityService institutionalCollectionSecurityService;
	
	/** service for dealing with user information */
	private UserService userService;
	

	/** Name of the collection */
	private String collectionName;
	
	/** description of the collection */
	private String collectionDescription;
	
	/** Message to be sent when a collection is added */
	private String collectionMessage;
	
	/** tells if the collection has been added */
	boolean actionSuccess = false;
	
	/** Id of the collection */
	private InstitutionalCollection collection;
	
	/** Id of the collection */
	private Long collectionId;
	
    /** set of personal collections that are the path for the current personal collection */
    private Collection <InstitutionalCollection> collectionPath;
    
 	/** Institutional Collection service */
	private InstitutionalCollectionService institutionalCollectionService;
	
	/** Access control entries */
	private Set<IrUserGroupAccessControlEntry> entries = new HashSet<IrUserGroupAccessControlEntry>();
    
	/** User groups that can be added to a institutional collection */
	private List<IrUserGroup> userGroups = new LinkedList<IrUserGroup>();
	
	/** Service to get user groups */
	private UserGroupService userGroupService;
	
	/** permissions that can be given to a collection */
	private List<IrClassTypePermission> permissions = new LinkedList<IrClassTypePermission>();
	
	/** service for marking items that need to be indexed */
	private InstitutionalItemIndexProcessingRecordService institutionalItemIndexProcessingRecordService;



	/** index processing type service */
	private IndexProcessingTypeService indexProcessingTypeService;
	
	/**
	 * Create a new institutional collection 
	 * 
	 * @return {@link #SUCCESS}
	 */
	public String create() {
		log.debug("create called");
		actionSuccess = false;
		
		// user making the changes
		IrUser user = userService.getUser(userId, false);
		
		// assume that if the current collection id is null or equal to 0
		// then we are adding a root collection to the user.
		if(parentCollectionId == null || parentCollectionId == 0)
		{
			 // only admins can add root collections
			 if(!user.hasRole(IrRole.ADMIN_ROLE))
			 {
				return "accessDenied";
			 }
			 Repository repository = 
				 repositoryService.getRepository(Repository.DEFAULT_REPOSITORY_ID, 
						 false);
			try {
				collection = repository.createInstitutionalCollection(collectionName.trim());
				repositoryService.saveRepository(repository);
				actionSuccess = true;
			   
			} catch (DuplicateNameException e) {
				log.error(e);
				collectionMessage = getText("institutionalCollectionAlreadyExists", 
						new String[]{collectionName});
			}
		}
		else
		{
			// creating sub collection
			InstitutionalCollection parent = 
				institutionalCollectionService.getCollection(parentCollectionId, false); 
			
			    
			try {
				if( user.hasRole(IrRole.ADMIN_ROLE) || institutionalCollectionSecurityService.hasPermission(parent, user, InstitutionalCollectionSecurityService.ADMINISTRATION_PERMISSION) > 0 )
				{
				    collection = parent.createChild(collectionName.trim());
				    institutionalCollectionService.saveCollection(parent);
				    institutionalCollectionSecurityService.giveAdminPermissionsToParentCollections(collection);
				    actionSuccess = true;
				}
			} catch (DuplicateNameException e) {
					log.error(e);
					collectionMessage = getText("institutionalCollectionAlreadyExists", 
							new String[]{collectionName});
			}
		}
		
		
        return "create";
	}
	
	/**
	 * Update an existing institutional collection 
	 * 
s	 */
	public String update() {
		log.debug("update called collection name = " + collectionName + " collection description = " + collectionDescription);
		collection = 
			institutionalCollectionService.getCollection(collectionId, false);
        
		actionSuccess = true;
		//name change
		if( !collection.getName().equals(collectionName.trim()))
        {
			try {
				collection.reName(collectionName.trim());
				log.debug("success saving insitutional collection " + collection);
	            collection.setDescription(collectionDescription);	
	            institutionalCollectionService.saveCollection(collection);
	            //re-index the contents of the institutional collection
				IndexProcessingType processingType = indexProcessingTypeService.get(IndexProcessingTypeService.UPDATE); 
				institutionalItemIndexProcessingRecordService.processItemsInCollection( collection, processingType);
			} catch (DuplicateNameException e) {
				actionSuccess = false;
			}
        }
		else
		{
			// description change
			collection.setDescription(collectionDescription);
			institutionalCollectionService.saveCollection(collection);
		}
        return "update";
	}
	
	
	/**
	 * Get the pictures for the collection.
	 * 
	 * @return
	 */
	public String getPictures()
	{
		log.debug("get pictures called");
		collection = institutionalCollectionService.getCollection(collectionId, false);
		return SUCCESS;
	}
	
	/**
	 * View the institutional collection.
	 * 
	 * @return
	 */
	public String view()
	{
		
		collection = 
			institutionalCollectionService.getCollection(collectionId, false);
		
		// don't hit the database unless we need to
		if(!collection.isRoot())
		{
		    collectionPath = 
		    	institutionalCollectionService.getPath(collection.getParent());
		}
		
		IrAcl acl = institutionalCollectionSecurityService.getAcl(collection);
		if( acl != null )
		{
		    entries = acl.getGroupEntries();
		}
		userGroups = userGroupService.getAllNameOrder();
		this.loadPermissions();
		return "view";
	}


	/**
	 * Load the permissions allowed for an institutional collection
	 */
	private void loadPermissions()
	{
		permissions = institutionalCollectionSecurityService.getCollectionPermissions();
	}
	
 	/**
	 * Parent collection to add the new collection to.
	 * 
	 * @return
	 */
	public Long getParentCollectionId() {
		return parentCollectionId;
	}

	/**
	 * Set the parent collection to add the new collection to.
	 * 
	 * @param parentCollectionId
	 */
	public void setParentCollectionId(Long parentCollectionId) {
		this.parentCollectionId = parentCollectionId;
	}

	/**
	 * Repository service to access repository and institutional information.
	 * 
	 * @return
	 */
	public RepositoryService getRepositoryService() {
		return repositoryService;
	}

	/**
	 * Set the repository service
	 * 
	 * @param repositoryService
	 */
	public void setRepositoryService(RepositoryService repositoryService) {
		this.repositoryService = repositoryService;
	}

	/**
	 * Get the collection name to use when creating the collection.
	 * 
	 * @return
	 */
	public String getCollectionName() {
		return collectionName;
	}

	/**
	 * Set the collection name.
	 * 
	 * @param collectionName
	 */
	public void setCollectionName(String collectionName) {
		this.collectionName = collectionName;
	}

	/**
	 * Get the collection message.  This is used to indicate success or failure.
	 * 
	 * @return
	 */
	public String getCollectionMessage() {
		return collectionMessage;
	}

	/**
	 * Get the collection message.  This is used to indicate success or failure.
	 * 
	 * @param collectionMessage
	 */
	public void setCollectionMessage(String collectionMessage) {
		this.collectionMessage = collectionMessage;
	}

	/**
	 * Get the institutional collection.
	 * 
	 * @return
	 */
	public InstitutionalCollection getCollection() {
		return collection;
	}

	/**
	 * Set the institutional collection 
	 * 
	 * @param collection
	 */
	public void setCollection(
			InstitutionalCollection institutionalCollection) {
		this.collection = institutionalCollection;
	}

	/**
	 * Collection id to load
	 * 
	 * @return
	 */
	public Long getCollectionId() {
		return collectionId;
	}

	/**
	 * Set the collection id.
	 * 
	 * @param collectionId
	 */
	public void setCollectionId(Long collectionId) {
		this.collectionId = collectionId;
	}
	
	/**
	 * The number of collection pictures.
	 * 
	 * @return
	 */
	public int getNumberOfCollectionPictures() {
		return collection.getPictures().size();
	}


	/**
	 * Path to this collection.
	 * 
	 * @return
	 */
	public Collection<InstitutionalCollection> getCollectionPath() {
		return collectionPath;
	}


	/**
	 * Path to this collection.
	 * 
	 * @param collectionPath
	 */
	public void setCollectionPath(Collection<InstitutionalCollection> collectionPath) {
		this.collectionPath = collectionPath;
	}


	/**
	 * Service for setting up security.
	 * 
	 * @return
	 */
	public InstitutionalCollectionSecurityService getInstitutionalCollectionSecurityService() {
		return institutionalCollectionSecurityService;
	}


	/**
	 * Service for setting up security.
	 * 
	 * @param institutionalCollectionSecurityService
	 */
	public void setInstitutionalCollectionSecurityService(
			InstitutionalCollectionSecurityService institutionalCollectionSecurityService) {
		this.institutionalCollectionSecurityService = institutionalCollectionSecurityService;
	}


	public InstitutionalCollectionService getInstitutionalCollectionService() {
		return institutionalCollectionService;
	}


	public void setInstitutionalCollectionService(
			InstitutionalCollectionService institutionalCollectionService) {
		this.institutionalCollectionService = institutionalCollectionService;
	}

	public String getCollectionDescription() {
		return collectionDescription;
	}

	public void setCollectionDescription(String collectionDescription) {
		this.collectionDescription = collectionDescription;
	}
	
	public boolean getActionSuccess()
	{
		return actionSuccess;
	}

	public Set<IrUserGroupAccessControlEntry> getEntries() {
		return entries;
	}

	public void setEntries(Set<IrUserGroupAccessControlEntry> entries) {
		this.entries = entries;
	}


	public UserGroupService getUserGroupService() {
		return userGroupService;
	}

	public void setUserGroupService(UserGroupService userGroupService) {
		this.userGroupService = userGroupService;
	}

	public List<IrUserGroup> getUserGroups() {
		return userGroups;
	}

	public void setUserGroups(List<IrUserGroup> userGroups) {
		this.userGroups = userGroups;
	}

	public List<IrClassTypePermission> getPermissions() {
		return permissions;
	}

	public void setPermissions(List<IrClassTypePermission> permissions) {
		this.permissions = permissions;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}
	
	public UserService getUserService() {
		return userService;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	public void setInstitutionalItemIndexProcessingRecordService(
			InstitutionalItemIndexProcessingRecordService institutionalItemIndexProcessingRecordService) {
		this.institutionalItemIndexProcessingRecordService = institutionalItemIndexProcessingRecordService;
	}

	public void setIndexProcessingTypeService(
			IndexProcessingTypeService indexProcessingTypeService) {
		this.indexProcessingTypeService = indexProcessingTypeService;
	}

}
