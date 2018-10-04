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

import org.apache.log4j.Logger;

import edu.ur.ir.institution.InstitutionalCollection;
import edu.ur.ir.institution.InstitutionalCollectionService;
import edu.ur.ir.institution.InstitutionalItem;
import edu.ur.ir.institution.InstitutionalItemService;
import edu.ur.ir.item.ContentType;
import edu.ur.ir.item.ContentTypeService;
import edu.ur.ir.repository.Repository;
import edu.ur.ir.web.table.Pager;
import edu.ur.order.OrderType;

/**
 * Class for browsing items within an institutional colleciton.
 * 
 * @author Nathan Sarr
 *
 */
public class CollectionInstitutionalItemBrowse extends Pager {

	/** Eclipse generated id */
	private static final long serialVersionUID = -6924511274479254476L;

	/**  Get the logger for this class */
	private static final Logger log = Logger.getLogger(CollectionInstitutionalItemBrowse.class);
	
	/** List of characters/options that can be selected */
	private String[] alphaList = new String[]{
			"All", "0-9", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", 
			"K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"
	};
	
	/** currently selected option */
	private String selectedAlpha ="All";
	
	/** Service for accessing institutional collections*/
	private InstitutionalItemService institutionalItemService;
	
	/** Service for dealing with institutional collections */
	private InstitutionalCollectionService institutionalCollectionService;
	
	/** List of institutional items found in the browse */
	private List<InstitutionalItem> institutionalItems;
	
	/** type of sort [ ascending | descending ] 
	 *  this is for incoming requests */
	private String sortType = "asc";
	
	/** name of the element to sort on 
	 *   this is for incoming requests */
	private String sortElement = "name";

	/** Total number of institutional items*/
	private int totalHits;
	
	/** id of the collection */
	private long collectionId = 0l;
	
	/** collection being looked at  */
	private InstitutionalCollection institutionalCollection;
	
	/** Indicates this is a browse */
	private String viewType = "browse";
	
	private int rowEnd;
	
	/** Path for a the set of collections */
	private List<InstitutionalCollection> collectionPath;
	
	/** institutional repository  */
	private Repository repository;
	
	/** service for dealing with content types */
	private ContentTypeService contentTypeService;
	
	/** the content type id to sort on -1 indicates no content type */
	private long contentTypeId = -1l;

	/** Default constructor */
	public CollectionInstitutionalItemBrowse()
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
	public String browseCollectionItems() {
		
		institutionalCollection = institutionalCollectionService.getCollection(collectionId, false);
		if( institutionalCollection == null )
		{
			return "collectionNotFound";
		}
		repository = institutionalCollection.getRepository();		
		collectionPath = institutionalCollectionService.getPath(institutionalCollection);
		log.debug("selected Alpha = " + selectedAlpha);
		rowEnd = rowStart + numberOfResultsToShow;
		log.debug("looking at collection " + institutionalCollection);
		
		institutionalItems = new LinkedList<InstitutionalItem>();

		if( contentTypeId == -1l )
		{
			if( selectedAlpha == null || selectedAlpha.equals("All") || selectedAlpha.trim().equals(""))
		    {
				if( sortElement.equalsIgnoreCase("name"))
		    	{
		            institutionalItems = institutionalItemService.getCollectionItemsOrderByName(rowStart, 
		    		    numberOfResultsToShow, institutionalCollection, OrderType.getOrderType(sortType));
		    	}
		    	else if( sortElement.equalsIgnoreCase("publicationDate"))
		        {
		    		institutionalItems = institutionalItemService.getCollectionItemsPublicationDateOrder(rowStart, 
		    		    numberOfResultsToShow, institutionalCollection, OrderType.getOrderType(sortType));
		        }
		    	else if( sortElement.equalsIgnoreCase("firstAvailableDate"))
		        {
		    		institutionalItems = institutionalItemService.getCollectionItemsFirstAvailableOrder(rowStart, 
		    		    numberOfResultsToShow, institutionalCollection, OrderType.getOrderType(sortType));
		        }
		        totalHits = institutionalItemService.getCountForCollectionAndChildren(institutionalCollection).intValue();
		    }
		    else if (selectedAlpha.equals("0-9"))
		    {
		    	if( sortElement.equalsIgnoreCase("name"))
		    	{
			        institutionalItems = institutionalItemService.getCollectionItemsBetweenChar(rowStart, numberOfResultsToShow, 
					    institutionalCollection, '0', '9', OrderType.getOrderType(sortType));
		    	}
		    	else if( sortElement.equalsIgnoreCase("publicationDate"))
		        {
		    		institutionalItems = institutionalItemService.getCollectionItemsBetweenCharPublicationDateOrder(rowStart, numberOfResultsToShow, 
					    institutionalCollection, '0', '9', OrderType.getOrderType(sortType));
		        }
		    	else if( sortElement.equalsIgnoreCase("firstAvailableDate"))
		        {
		    		institutionalItems = institutionalItemService.getCollectionItemsBetweenCharFirstAvailableOrder(rowStart, numberOfResultsToShow, 
					    institutionalCollection, '0', '9', OrderType.getOrderType(sortType));
		        }
			    totalHits = institutionalItemService.getCount(institutionalCollection, '0', '9').intValue();
		    }
		    else
		    {
		    	if( sortElement.equalsIgnoreCase("name"))
		    	{
			        institutionalItems = institutionalItemService.getCollectionItemsByChar(rowStart, numberOfResultsToShow, 
			        		institutionalCollection, selectedAlpha.charAt(0), OrderType.getOrderType(sortType));
		    	}
		    	else if( sortElement.equalsIgnoreCase("publicationDate"))
		        {
		    		institutionalItems = institutionalItemService.getCollectionItemsByCharPublicationDateOrder(rowStart, numberOfResultsToShow, 
			        		institutionalCollection, selectedAlpha.charAt(0), OrderType.getOrderType(sortType));
		        }
		    	else if( sortElement.equalsIgnoreCase("firstAvailableDate"))
		        {
		    		institutionalItems = institutionalItemService.getCollectionItemsByCharFirstAvailableOrder(rowStart, numberOfResultsToShow, 
			        		institutionalCollection, selectedAlpha.charAt(0), OrderType.getOrderType(sortType));
		        }
			    
			    log.debug("test hits = " + institutionalItemService.getCount(institutionalCollection, selectedAlpha.charAt(0)).intValue());
			    totalHits = institutionalItemService.getCount(institutionalCollection, selectedAlpha.charAt(0)).intValue();
		    }
		}
		else
		{
		    if( selectedAlpha == null || selectedAlpha.equals("All") || selectedAlpha.trim().equals(""))
		    {
		    	if( sortElement.equalsIgnoreCase("name"))
		    	{
		            institutionalItems = institutionalItemService.getCollectionItemsOrderByName(rowStart, 
		    		numberOfResultsToShow, institutionalCollection, contentTypeId, OrderType.getOrderType(sortType));
		    	}
		    	else if( sortElement.equalsIgnoreCase("publicationDate"))
		        {
		    		institutionalItems = institutionalItemService.getCollectionItemsPublicationDateOrder(rowStart, 
		    		numberOfResultsToShow, institutionalCollection, contentTypeId, OrderType.getOrderType(sortType));
		        }
		    	else if( sortElement.equalsIgnoreCase("firstAvailableDate"))
		        {
		    		institutionalItems = institutionalItemService.getCollectionItemsFirstAvailableOrder(rowStart, 
		    		numberOfResultsToShow, institutionalCollection, contentTypeId, OrderType.getOrderType(sortType));
		        }
		        totalHits = institutionalItemService.getCount(institutionalCollection, contentTypeId).intValue();
		    }
		    else if (selectedAlpha.equals("0-9"))
		    {
		    	if( sortElement.equalsIgnoreCase("name"))
		    	{
			        institutionalItems = institutionalItemService.getCollectionItemsBetweenChar(rowStart, numberOfResultsToShow, 
					institutionalCollection, contentTypeId, '0', '9', OrderType.getOrderType(sortType));
		    	}
		    	else if( sortElement.equalsIgnoreCase("publicationDate"))
		        {
		    		institutionalItems = institutionalItemService.getCollectionItemsBetweenCharPublicationDateOrder(rowStart, numberOfResultsToShow, 
					institutionalCollection, contentTypeId, '0', '9', OrderType.getOrderType(sortType));
		        }
		    	else if( sortElement.equalsIgnoreCase("firstAvailableDate"))
		        {
		    		institutionalItems = institutionalItemService.getCollectionItemsBetweenCharFirstAvailableOrder(rowStart, numberOfResultsToShow, 
					institutionalCollection, contentTypeId, '0', '9', OrderType.getOrderType(sortType));
		        }
			    totalHits = institutionalItemService.getCount(institutionalCollection, '0', '9', contentTypeId).intValue();
		    }
		    else
		    {
		    	if( sortElement.equalsIgnoreCase("name"))
		    	{
			        institutionalItems = institutionalItemService.getCollectionItemsByChar(rowStart, numberOfResultsToShow, 
			    		institutionalCollection, contentTypeId, selectedAlpha.charAt(0), OrderType.getOrderType(sortType));
		    	}
		    	else if( sortElement.equalsIgnoreCase("publicationDate"))
		        {
		    		 institutionalItems = institutionalItemService.getCollectionItemsByCharPublicationDateOrder(rowStart, numberOfResultsToShow, 
			    		institutionalCollection, contentTypeId, selectedAlpha.charAt(0), OrderType.getOrderType(sortType));
		        }
		    	else if( sortElement.equalsIgnoreCase("firstAvailableDate"))
		        {
		    		institutionalItems = institutionalItemService.getCollectionItemsByCharFirstAvailableOrder(rowStart, numberOfResultsToShow, 
			    		institutionalCollection, contentTypeId, selectedAlpha.charAt(0), OrderType.getOrderType(sortType));
		        }
			    log.debug("test hits = " + institutionalItemService.getCount(institutionalCollection, selectedAlpha.charAt(0), contentTypeId).intValue());
			    totalHits = institutionalItemService.getCount(institutionalCollection, selectedAlpha.charAt(0), contentTypeId).intValue();
		    }
		}
		
		if(institutionalItems != null ) {
			log.debug("institutionalItems size = " + institutionalItems.size());			
		}
		log.debug("total hits = " + totalHits);

		if(rowEnd > totalHits)
		{
			rowEnd = totalHits;
		}
		return SUCCESS;
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

	public InstitutionalCollectionService getInstitutionalCollectionService() {
		return institutionalCollectionService;
	}

	public void setInstitutionalCollectionService(
			InstitutionalCollectionService institutionalCollectionService) {
		this.institutionalCollectionService = institutionalCollectionService;
	}

	public InstitutionalCollection getInstitutionalCollection() {
		return institutionalCollection;
	}

	public void setInstitutionalCollection(InstitutionalCollection collection) {
		this.institutionalCollection = collection;
	}

	public long getCollectionId() {
		return collectionId;
	}

	public void setCollectionId(long collectionId) {
		this.collectionId = collectionId;
	}

	public List<InstitutionalCollection> getCollectionPath() {
		return collectionPath;
	}

	public void setCollectionPath(List<InstitutionalCollection> collectionPath) {
		this.collectionPath = collectionPath;
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
	
	/**
	 * Get a list of content types.
	 * 
	 * @return list of content types
	 */
	public List<ContentType> getContentTypes()
	{
		return contentTypeService.getAllContentTypeByNameOrder();
	}
	
	public void setContentTypeService(ContentTypeService contentTypeService) {
		this.contentTypeService = contentTypeService;
	}

	public long getContentTypeId() {
		return contentTypeId;
	}

	public void setContentTypeId(long contentTypeId) {
		this.contentTypeId = contentTypeId;
	}

}
