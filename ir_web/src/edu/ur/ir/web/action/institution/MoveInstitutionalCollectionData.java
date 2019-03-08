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

import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.opensymphony.xwork2.ActionSupport;

import edu.ur.ir.FileSystem;
import edu.ur.ir.NoIndexFoundException;
import edu.ur.ir.index.IndexProcessingType;
import edu.ur.ir.index.IndexProcessingTypeService;
import edu.ur.ir.institution.CollectionDoesNotAcceptItemsException;
import edu.ur.ir.institution.InstitutionalCollection;
import edu.ur.ir.institution.InstitutionalCollectionService;
import edu.ur.ir.institution.InstitutionalItemIndexProcessingRecordService;
import edu.ur.ir.institution.InstitutionalItem;
import edu.ur.ir.institution.InstitutionalItemService;
import edu.ur.ir.repository.Repository;
import edu.ur.ir.repository.RepositoryService;

/**
 * Allows institutional collections and items to be moved
 * 
 * @author Nathan Sarr
 *
 */
public class MoveInstitutionalCollectionData extends ActionSupport{

	/**  Eclipse generated id */
	private static final long serialVersionUID = 4796524578110023158L;
	
	/** set of collection ids to move */
	private Long[] collectionIds = {};
	
	/** set of file ids to move */
	private Long[] itemIds = {};
	
	/** collections to move */
	private List<InstitutionalCollection> collectionsToMove = new LinkedList<InstitutionalCollection>();
	
	/** items to move */
	private List<InstitutionalItem> itemsToMove = new LinkedList<InstitutionalItem> ();
	
	/** location to move the collections and items  */
	private Long destinationId = InstitutionalCollectionService.ROOT_COLLECTION_ID;
	
	/** Institutional Collection service */
	private InstitutionalCollectionService institutionalCollectionService;
	
	/** service for marking items that need to be indexed */
	private InstitutionalItemIndexProcessingRecordService institutionalItemIndexProcessingRecordService;

	/** index processing type service */
	private IndexProcessingTypeService indexProcessingTypeService;
	
	/** Repository service for dealing with institutional repository information */
	private RepositoryService repositoryService;
	
	/**  institutional item information data access  */
	private InstitutionalItemService institutionalItemService;
	
	/** path to the destination */
	private List<InstitutionalCollection> destinationPath;
	
	/** Repository for the system */
	private Repository repository;
	
	/** current contents of the destination folder */
	private List<FileSystem> currentDestinationContents = new LinkedList<FileSystem>();
	
	/** boolean to indicate that the action was successful */
	private boolean actionSuccess;
	
	/** current destination */
	private InstitutionalCollection destination;
	
	/**  Logger */
	private static final Logger log = LogManager.getLogger(MoveInstitutionalCollectionData.class);

    /** current root location where all items are being moved from*/
    private Long parentCollectionId;
	
	/**
	 * Takes the user to view the locations that the collection can be moved to.
	 * 
	 * @return
	 */
	public String viewLocations()
	{
		log.debug("view move locations");
		repository = repositoryService.getRepository(Repository.DEFAULT_REPOSITORY_ID, false);

		List<Long> listCollectionIds = new LinkedList<Long>();
		for( Long id : collectionIds)
		{
		    listCollectionIds.add(id);
		}
		
		collectionsToMove = institutionalCollectionService.getCollections(listCollectionIds);
		List<Long> listItemIds = new LinkedList<Long>();
		for( Long id : itemIds)
		{
			    listItemIds.add(id);
		}
		itemsToMove = institutionalItemService.getInstitutionalItems(listItemIds);
		
		// not moving these items to the root
		if( !destinationId.equals(InstitutionalCollectionService.ROOT_COLLECTION_ID))
		{
		    destination = 
		    	institutionalCollectionService.getCollection(destinationId, false);
		    
		    // make sure the user has not navigated into a child or itself- this is illegal
		    for(InstitutionalCollection collection: collectionsToMove)
		    {
		    	if(destination.equals(collection))
		    	{
		    		throw new IllegalStateException("cannot move a collection into itself destination = " + destination
		    				+ " collection = " + collection);
		    	}
		    	else if( collection.getTreeRoot().equals(destination.getTreeRoot()) &&
		    			 destination.getLeftValue() > collection.getLeftValue() &&
		    			 destination.getRightValue() < collection.getRightValue() )
		    	{
		    		throw new IllegalStateException("cannot move a collection into a child destination = " + destination
		    				+ " collection = " + collection);
		    	}
		    }
		    
		    destinationPath = institutionalCollectionService.getPath(destination);
		    currentDestinationContents.addAll(destination.getChildren());
		    
		}
		else
		{
			//moving to root location
			currentDestinationContents.addAll(repository.getInstitutionalCollections());
		}
		
		return SUCCESS;
	}

	/**
	 * Takes the user to view the locations that the collection can be moved to.
	 * 
	 * @return
	 * @throws NoIndexFoundException 
	 * @throws CollectionDoesNotAcceptItemsException 
	 */
	public String move() throws NoIndexFoundException, CollectionDoesNotAcceptItemsException
	{
		log.debug("move called");
		List<InstitutionalCollection> collectionsNotMoved = new LinkedList<InstitutionalCollection>();
		actionSuccess = true;
		repository = repositoryService.getRepository(Repository.DEFAULT_REPOSITORY_ID, false);

		List<Long> listCollectionIds = new LinkedList<Long>();
		for( Long id : collectionIds)
		{
		    listCollectionIds.add(id);
		}
		
		collectionsToMove = institutionalCollectionService.getCollections(listCollectionIds);
		
        // add the old tree roots they will need to be re-indexed
		// because their left and right values will be changed.
        LinkedList<InstitutionalCollection> oldRoots = new LinkedList<InstitutionalCollection>();
        for(InstitutionalCollection c : collectionsToMove)
        {
        	if(!oldRoots.contains(c.getTreeRoot()))
        	{
        		oldRoots.add(c.getTreeRoot());
        	}
        }
        
        
		List<Long> listItemIds = new LinkedList<Long>();
		for( Long id : itemIds)
		{
		    listItemIds.add(id);
		}
		
		itemsToMove = institutionalItemService.getInstitutionalItems(listItemIds);
		
		log.debug( "destination id = " + destinationId);
		
		// moving this collection and items into another collection
		if( !destinationId.equals(InstitutionalCollectionService.ROOT_COLLECTION_ID))
		{
		    destination = 
		    	institutionalCollectionService.getCollection(destinationId, false);
		    
			collectionsNotMoved = 
				institutionalCollectionService.moveCollectionInformation(destination, 
						collectionsToMove, itemsToMove);
			
			for( InstitutionalItem i : itemsToMove)
			{
				// set item to be updated in index
				IndexProcessingType processingType = indexProcessingTypeService.get(IndexProcessingTypeService.UPDATE); 
				institutionalItemIndexProcessingRecordService.save(i.getId(), processingType);
			}
		}
		// moving collections to root of repository - only collections can be at the
		// root of the repository - items are not allowed.
		else
		{
			if(itemsToMove.size() > 0 )
			{
				actionSuccess = false;
				String message = getText("itemsIntoRepositoryRoot");
				addFieldError("moveError", message);

			}
			else
			{
			    collectionsNotMoved = institutionalCollectionService.moveCollectionInformation(repository, 
						collectionsToMove);
			}
		}
		
	    
		
		if( collectionsNotMoved.size() > 0 )
		{
			String message = getText("collectionNamesAlreadyExist");
			actionSuccess = false;
			StringBuffer sb = new StringBuffer();
			sb.append(message);
			for(InstitutionalCollection collection : collectionsNotMoved)
			{
			    sb.append(" ");
			    sb.append(collection.getName());
			}
			addFieldError("moveError", sb.toString());
		}
		else
		{
			// re-index all items within collections that are moved
			for(InstitutionalCollection c : collectionsToMove)
			{
				IndexProcessingType processingType = indexProcessingTypeService.get(IndexProcessingTypeService.UPDATE); 
				institutionalItemIndexProcessingRecordService.processItemsInCollection( c, processingType);
			}
		}
		
		
		//load the data
        viewLocations();		
		
		return SUCCESS;
	}

	public void setCollectionIds(Long[] collectionIds) {
		this.collectionIds = collectionIds;
	}


	public void setItemIds(Long[] itemIds) {
		this.itemIds = itemIds;
	}


	public void setCollectionsToMove(List<InstitutionalCollection> collectionsToMove) {
		this.collectionsToMove = collectionsToMove;
	}


	public void setItemsToMove(List<InstitutionalItem> itemsToMove) {
		this.itemsToMove = itemsToMove;
	}


	public void setDestinationId(Long destinationId) {
		this.destinationId = destinationId;
	}


	public void setInstitutionalCollectionService(
			InstitutionalCollectionService institutionalCollectionService) {
		this.institutionalCollectionService = institutionalCollectionService;
	}


	public void setRepositoryService(RepositoryService repositoryService) {
		this.repositoryService = repositoryService;
	}


	public List<InstitutionalCollection> getCollectionsToMove() {
		return collectionsToMove;
	}


	public List<InstitutionalItem> getItemsToMove() {
		return itemsToMove;
	}


	public List<InstitutionalCollection> getDestinationPath() {
		return destinationPath;
	}


	public List<FileSystem> getCurrentDestinationContents() {
		return currentDestinationContents;
	}


	public Repository getRepository() {
		return repository;
	}

	public boolean getActionSuccess() {
		return actionSuccess;
	}

	public Long getDestinationId() {
		return destinationId;
	}

	public InstitutionalCollection getDestination() {
		return destination;
	}

	public void setDestination(InstitutionalCollection destination) {
		this.destination = destination;
	}

	public Long getParentCollectionId() {
		return parentCollectionId;
	}

	public void setParentCollectionId(Long parentCollectionId) {
		this.parentCollectionId = parentCollectionId;
	}

	public InstitutionalItemService getInstitutionalItemService() {
		return institutionalItemService;
	}

	public void setInstitutionalItemService(
			InstitutionalItemService institutionalItemService) {
		this.institutionalItemService = institutionalItemService;
	}

	public InstitutionalItemIndexProcessingRecordService getInstitutionalItemIndexProcessingRecordService() {
		return institutionalItemIndexProcessingRecordService;
	}

	public void setInstitutionalItemIndexProcessingRecordService(
			InstitutionalItemIndexProcessingRecordService institutionalItemIndexProcessingRecordService) {
		this.institutionalItemIndexProcessingRecordService = institutionalItemIndexProcessingRecordService;
	}

	public IndexProcessingTypeService getIndexProcessingTypeService() {
		return indexProcessingTypeService;
	}

	public void setIndexProcessingTypeService(
			IndexProcessingTypeService indexProcessingTypeService) {
		this.indexProcessingTypeService = indexProcessingTypeService;
	}


}
