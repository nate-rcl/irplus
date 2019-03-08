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

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import edu.ur.ir.institution.InstitutionalItem;
import edu.ur.ir.institution.InstitutionalItemService;
import edu.ur.ir.item.ContentType;
import edu.ur.ir.item.ContentTypeService;
import edu.ur.ir.repository.Repository;
import edu.ur.ir.repository.RepositoryService;

import edu.ur.ir.web.table.Pager;
import edu.ur.order.OrderType;

/**
 * Browse for institutional items.
 * 
 * @author Nathan Sarr
 *
 */
public class RepositoryInstitutionalItemBrowse extends Pager {

	/**  Get the logger for this class */
	private static final Logger log = LogManager.getLogger(RepositoryInstitutionalItemBrowse.class);
	
	/** List of characters/options that can be selected */
	private String[] alphaList = new String[]{
			"All", "0-9", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", 
			"K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"
	};
	
	
	/** Service for dealing with repositories */
	private RepositoryService repositoryService;
	
	/** currently selected option */
	private String selectedAlpha ="All";
	
	/** Eclipse generated id */
	private static final long serialVersionUID = -6584343467238699904L;
	
	/** Service for accessing institutional collections*/
	private InstitutionalItemService institutionalItemService;
	
	/** List of institutional items found from searching */
	private List<InstitutionalItem> institutionalItems;
	
	/** type of sort [ ascending | descending ] 
	 *  this is for incoming requests */
	private String sortType = "asc";
	
	/** name of the element to sort on 
	 *   this is for incoming requests */
	private String sortElement = "name";
	
	/** the content type id to sort on -1 indicates no content type */
	private long contentTypeId = -1l;

	/** Total number of institutional items*/
	private int totalHits;
	
	/** Row End */
	private int rowEnd;
		
	/** Indicates this is a browse */
	private String viewType = "browse";
	
	/** repository object */
	private Repository repository;
	
	/** service for dealing with content types */
	private ContentTypeService contentTypeService;
	
	

	/** Default constructor */
	public RepositoryInstitutionalItemBrowse()
	{
		numberOfResultsToShow = 25;
		numberOfPagesToShow = 20;
	}
	
	public String getViewType() {
		return viewType;
	}

	public void setViewType(String viewType) {
		this.viewType = viewType;
	}

	/**
	 * Browse researcher
	 * 
	 * @return
	 */
	public String browseRepositoryItems() {
		repository = repositoryService.getRepository(Repository.DEFAULT_REPOSITORY_ID, false);
		log.debug("selected Alpha = " + selectedAlpha);
		rowEnd = rowStart + numberOfResultsToShow;
		
		if( contentTypeId == -1l )
		{
			log.debug("Viewing all items with no content types");
		    if( selectedAlpha == null || selectedAlpha.equals("All") || selectedAlpha.trim().equals(""))
		    {
		    	if( sortElement.equalsIgnoreCase("name"))
		    	{
		            institutionalItems = institutionalItemService.getRepositoryItemsOrderByName(rowStart, 
		    		    numberOfResultsToShow, Repository.DEFAULT_REPOSITORY_ID, OrderType.getOrderType(sortType));
		    	}
		    	else if( sortElement.equalsIgnoreCase("publicationDate"))
		    	{
		    		institutionalItems = institutionalItemService.getRepositoryItemsByPublicationDateOrder(rowStart, 
		    				numberOfResultsToShow, Repository.DEFAULT_REPOSITORY_ID, OrderType.getOrderType(sortType) );
		    	}
		    	else if( sortElement.equalsIgnoreCase("firstAvailableDate"))
		    	{
		    		institutionalItems = institutionalItemService.getRepositoryItemsByFirstAvailableOrder(rowStart, 
		    				numberOfResultsToShow, Repository.DEFAULT_REPOSITORY_ID, OrderType.getOrderType(sortType));
		    	}
		        totalHits = institutionalItemService.getCount(Repository.DEFAULT_REPOSITORY_ID).intValue();
		    }
		    else if (selectedAlpha.equals("0-9"))
		    {
		    	if( sortElement.equalsIgnoreCase("name"))
		    	{
			        institutionalItems = institutionalItemService.getRepositoryItemsBetweenChar(rowStart, numberOfResultsToShow, 
					    Repository.DEFAULT_REPOSITORY_ID, '0', '9', OrderType.getOrderType(sortType));
		    	}
		    	else if( sortElement.equalsIgnoreCase("publicationDate"))
		    	{
		    		institutionalItems = institutionalItemService.getRepositoryItemsBetweenCharByPublicationDateOrder(rowStart, numberOfResultsToShow, 
					    Repository.DEFAULT_REPOSITORY_ID, '0', '9', OrderType.getOrderType(sortType));
		    	}
		    	else if( sortElement.equalsIgnoreCase("firstAvailableDate"))
		    	{
		    		institutionalItems = institutionalItemService.getRepositoryItemsBetweenCharByFirstAvailableOrder(rowStart, numberOfResultsToShow, 
					    Repository.DEFAULT_REPOSITORY_ID, '0', '9', OrderType.getOrderType(sortType));
		    	}
			    totalHits = institutionalItemService.getCount(Repository.DEFAULT_REPOSITORY_ID, '0', '9').intValue();
		    }
		    else
		    {
		    	if( sortElement.equalsIgnoreCase("name"))
		    	{
			        institutionalItems = institutionalItemService.getRepositoryItemsByChar(rowStart, numberOfResultsToShow, 
			        		Repository.DEFAULT_REPOSITORY_ID, selectedAlpha.charAt(0), OrderType.getOrderType(sortType));
		    	}
		    	else if( sortElement.equalsIgnoreCase("publicationDate"))
		        {
		    		institutionalItems = institutionalItemService.getRepositoryItemsByCharPublicationDateOrder(rowStart, numberOfResultsToShow,
		    				Repository.DEFAULT_REPOSITORY_ID, selectedAlpha.charAt(0), OrderType.getOrderType(sortType));
		        }
		    	else if( sortElement.equalsIgnoreCase("firstAvailableDate"))
		    	{
		    		institutionalItems = institutionalItemService.getRepositoryItemsByCharFirstAvailableOrder(rowStart, numberOfResultsToShow,
		    				Repository.DEFAULT_REPOSITORY_ID, selectedAlpha.charAt(0), OrderType.getOrderType(sortType));
		    	}
			    totalHits = institutionalItemService.getCount(Repository.DEFAULT_REPOSITORY_ID, selectedAlpha.charAt(0)).intValue();
		    }
		}
		else
		{
			log.debug("Viewing all items with content type id " + contentTypeId);
			if( selectedAlpha == null || selectedAlpha.equals("All") || selectedAlpha.trim().equals(""))
		    {
				if( sortElement.equalsIgnoreCase("name"))
		    	{
		            institutionalItems = institutionalItemService.getRepositoryItemsOrderByName(rowStart, 
		    		    numberOfResultsToShow, Repository.DEFAULT_REPOSITORY_ID, contentTypeId, OrderType.getOrderType(sortType));
		    	}
		    	else if( sortElement.equalsIgnoreCase("publicationDate"))
		        {
		    		institutionalItems = institutionalItemService.getRepositoryItemsByPublicationDateOrder(rowStart, 
		    		    numberOfResultsToShow, Repository.DEFAULT_REPOSITORY_ID, contentTypeId, OrderType.getOrderType(sortType));
		        }
		    	else if( sortElement.equalsIgnoreCase("firstAvailableDate"))
		    	{
		    		institutionalItems = institutionalItemService.getRepositoryItemsByFirstAvailableOrder(rowStart, 
		    		    numberOfResultsToShow, Repository.DEFAULT_REPOSITORY_ID, contentTypeId, OrderType.getOrderType(sortType));
		    	}
		        totalHits = institutionalItemService.getCount(Repository.DEFAULT_REPOSITORY_ID, contentTypeId).intValue();
		    }
		    else if (selectedAlpha.equals("0-9"))
		    {
		    	if( sortElement.equalsIgnoreCase("name"))
		    	{
			        institutionalItems = institutionalItemService.getRepositoryItemsBetweenChar(rowStart, numberOfResultsToShow, 
					    Repository.DEFAULT_REPOSITORY_ID, '0', '9', contentTypeId, OrderType.getOrderType(sortType));
		    	}
		    	else if( sortElement.equalsIgnoreCase("publicationDate"))
		        {
		    		institutionalItems = institutionalItemService.getRepositoryItemsBetweenCharByPublicationDateOrder(rowStart, numberOfResultsToShow, 
					    Repository.DEFAULT_REPOSITORY_ID, '0', '9', contentTypeId, OrderType.getOrderType(sortType));
		        }
		    	else if( sortElement.equalsIgnoreCase("firstAvailableDate"))
		    	{
		    		institutionalItems = institutionalItemService.getRepositoryItemsBetweenCharByFirstAvailableOrder(rowStart, numberOfResultsToShow, 
						    Repository.DEFAULT_REPOSITORY_ID, '0', '9', contentTypeId, OrderType.getOrderType(sortType));
		    	}
			    totalHits = institutionalItemService.getCount(Repository.DEFAULT_REPOSITORY_ID, '0', '9', contentTypeId).intValue();
		    }
		    else
		    {
		    	if( sortElement.equalsIgnoreCase("name"))
		    	{
			        institutionalItems = institutionalItemService.getRepositoryItemsByChar(rowStart, numberOfResultsToShow, 
			        		Repository.DEFAULT_REPOSITORY_ID, contentTypeId, selectedAlpha.charAt(0), OrderType.getOrderType(sortType));
		    	}
		    	else if( sortElement.equalsIgnoreCase("publicationDate"))
		        {
		    		institutionalItems = institutionalItemService.getRepositoryItemsByCharPublicationDateOrder(rowStart, 
		    				numberOfResultsToShow, Repository.DEFAULT_REPOSITORY_ID, contentTypeId, selectedAlpha.charAt(0), 
		    				OrderType.getOrderType(sortType));
		        }
		    	else if( sortElement.equalsIgnoreCase("firstAvailableDate"))
		    	{
		    		institutionalItems = institutionalItemService.getRepositoryItemsByCharFirstAvailableOrder(rowStart, 
		    				numberOfResultsToShow, Repository.DEFAULT_REPOSITORY_ID, contentTypeId, selectedAlpha.charAt(0), 
		    				OrderType.getOrderType(sortType));
		    	}
			    totalHits = institutionalItemService.getCount(Repository.DEFAULT_REPOSITORY_ID, selectedAlpha.charAt(0), contentTypeId).intValue();
		    }
		}
		
		if(rowEnd > totalHits)
		{
			rowEnd = totalHits;
		}
		
		return SUCCESS;
	}
	
	/**
	 * Get a list of content types.
	 * 
	 * @return list of content types
	 */
	public List<ContentType> getContentTypes()
	{
		return contentTypeService.getAllContentTypeByNameOrder();
	}
	
	/**
	 * Get total number of researchers
	 * 
	 * @see edu.ur.ir.web.table.Pager#getTotalHits()
	 */
	public int getTotalHits() {		
		return totalHits;
	}


	public String getSortElement() {
		return sortElement;
	}

	public void setSortElement(String sortElement) {
		this.sortElement = sortElement;
	}

	public String getSortType() {
		return sortType;
	}

	public void setSortType(String sortType) {
		this.sortType = sortType;
	}


	public List<InstitutionalItem> getInstitutionalItems() {
		return institutionalItems;
	}

	public void setInstitutionalItems(List<InstitutionalItem> institutionalItems) {
		this.institutionalItems = institutionalItems;
	}

	public InstitutionalItemService getInstitutionalItemService() {
		return institutionalItemService;
	}

	public void setInstitutionalItemService(
			InstitutionalItemService institutionalItemService) {
		this.institutionalItemService = institutionalItemService;
	}

	public String[] getAlphaList() {
		return alphaList;
	}

	public void setAlphaList(String[] alphaList) {
		this.alphaList = alphaList;
	}

	public String getSelectedAlpha() {
		return selectedAlpha;
	}

	public void setSelectedAlpha(String selectedAlpha) {
		this.selectedAlpha = selectedAlpha;
	}

	public int getRowEnd() {
		return rowEnd;
	}

	public void setRowEnd(int rowEnd) {
		this.rowEnd = rowEnd;
	}

	public Repository getRepository() {
		return repository;
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	public RepositoryService getRepositoryService() {
		return repositoryService;
	}

	public void setRepositoryService(RepositoryService repositoryService) {
		this.repositoryService = repositoryService;
	}

	public long getContentTypeId() {
		return contentTypeId;
	}

	public void setContentTypeId(long contentTypeId) {
		this.contentTypeId = contentTypeId;
	}

	public void setContentTypeService(ContentTypeService contentTypeService) {
		this.contentTypeService = contentTypeService;
	}

}
