package edu.ur.ir.web.action.institution;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.opensymphony.xwork2.ActionSupport;

import edu.ur.ir.institution.InstitutionalCollection;
import edu.ur.ir.institution.InstitutionalCollectionService;
import edu.ur.ir.institution.InstitutionalItemService;
import edu.ur.ir.item.ContentType;
import edu.ur.ir.item.ContentTypeCount;
import edu.ur.ir.item.ContentTypeService;
import edu.ur.ir.item.SponsorService;
import edu.ur.ir.statistics.DownloadStatisticsService;
import edu.ur.simple.type.AscendingNameComparator;

/**
 * Allows users to view the institutional collection statistics
 * 
 * @author Nathan Sarr
 *
 */
public class InstitutionalCollectionStatistics extends ActionSupport{

	/** eclipse generated is */
	private static final long serialVersionUID = 2636705546230962104L;

	/** Institutional Collection service */
	private InstitutionalCollectionService institutionalCollectionService;
	
	/** id of the institutional collection  */
	private Long collectionId;
	
	/** the institutional collection */
	private InstitutionalCollection institutionalCollection;
	
	/** Number of items within a collection and its sub-collection */
	private Long institutionalItemCount;

	/** Number of items within a collection */
	private Long institutionalItemsCountForACollection;

	/** Count of sub collections */
	private int subcollectionCount;
	
	/** Count of the subcollection and its children */
	private Long allSubcollectionCount;
	
	/** Download Statistics service */
	private DownloadStatisticsService downloadStatisticsService;
	
	/** File download count for this collection */
	private Long fileDownloadCountForCollection; 

	/** File download count for this collection */
	private Long fileDownloadCountForCollectionAndItsChildren; 
	
	/** get a count of content types */
	private List<ContentTypeCount> contentTypeCounts = new LinkedList<ContentTypeCount>();
	
	/** service to get content type information */
	private ContentTypeService contentTypeService;
	
	/** Service for sponsor information */
	private SponsorService sponsorService;

	/** count for the name of the sponsors */
	private Long sponsorCount;
	
	/** Service for dealing with institutional items */
	private InstitutionalItemService institutionalItemService;
	
	/** Used for sorting name based entities */
	private AscendingNameComparator nameComparator = new AscendingNameComparator();

	
	/**
     * Get the statistics information
     *
     * @return {@link #SUCCESS}
     */
    public String execute() throws Exception 
    {
    	if( collectionId != null )
		{
		    institutionalCollection = 
			    institutionalCollectionService.getCollection(collectionId, false);
		}
		
		if( institutionalCollection != null )
		{
		    institutionalItemCount = institutionalCollectionService.getInstitutionalItemCountForCollectionAndChildren(institutionalCollection);
		    institutionalItemsCountForACollection = institutionalCollectionService.getInstitutionalItemCountForCollection(institutionalCollection); 
		    subcollectionCount = institutionalCollection.getChildCount();
		    allSubcollectionCount =institutionalCollectionService.getTotalSubcollectionCount(institutionalCollection); 
		    fileDownloadCountForCollection = downloadStatisticsService.getNumberOfDownloadsForCollection(institutionalCollection);
		    fileDownloadCountForCollectionAndItsChildren = downloadStatisticsService.getNumberOfDownloadsForCollectionAndItsChildren(institutionalCollection);

		    contentTypeCounts = institutionalItemService.getCollectionContentTypeCount(institutionalCollection);
		   
		    Collections.sort(contentTypeCounts, nameComparator);
		    
		    sponsorCount = sponsorService.getCount(institutionalCollection);
        }
    	return SUCCESS;

    }
    
	public Long getCollectionId() {
		return collectionId;
	}


	public void setCollectionId(Long collectionId) {
		this.collectionId = collectionId;
	}


	public void setInstitutionalCollectionService(
			InstitutionalCollectionService institutionalCollectionService) {
		this.institutionalCollectionService = institutionalCollectionService;
	}

	public InstitutionalCollection getInstitutionalCollection() {
		return institutionalCollection;
	}


	public Long getInstitutionalItemCount() {
		return institutionalItemCount;
	}


	public Long getInstitutionalItemsCountForACollection() {
		return institutionalItemsCountForACollection;
	}


	public int getSubcollectionCount() {
		return subcollectionCount;
	}


	public Long getAllSubcollectionCount() {
		return allSubcollectionCount;
	}


	public Long getFileDownloadCountForCollection() {
		return fileDownloadCountForCollection;
	}


	public Long getFileDownloadCountForCollectionAndItsChildren() {
		return fileDownloadCountForCollectionAndItsChildren;
	}


	public void setDownloadStatisticsService(
			DownloadStatisticsService downloadStatisticsService) {
		this.downloadStatisticsService = downloadStatisticsService;
	}

	public List<ContentTypeCount> getContentTypeCounts() {
		return contentTypeCounts;
	}

	public Long getSponsorCount() {
		return sponsorCount;
	}

	public void setContentTypeService(ContentTypeService contentTypeService) {
		this.contentTypeService = contentTypeService;
	}

	public void setSponsorService(SponsorService sponsorService) {
		this.sponsorService = sponsorService;
	}

	public void setInstitutionalItemService(
			InstitutionalItemService institutionalItemService) {
		this.institutionalItemService = institutionalItemService;
	}
	
}
