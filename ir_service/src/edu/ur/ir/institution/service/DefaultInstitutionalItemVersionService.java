/**  
   Copyright 2008-2010 University of Rochester

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

package edu.ur.ir.institution.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.ur.ir.handle.HandleInfo;
import edu.ur.ir.handle.HandleInfoDAO;
import edu.ur.ir.index.IndexProcessingTypeService;
import edu.ur.ir.institution.InstitutionalCollection;
import edu.ur.ir.institution.InstitutionalItemIndexProcessingRecordService;
import edu.ur.ir.institution.InstitutionalItemVersion;
import edu.ur.ir.institution.InstitutionalItemVersionDAO;
import edu.ur.ir.institution.InstitutionalItemVersionDownloadCount;
import edu.ur.ir.institution.InstitutionalItemVersionService;
import edu.ur.ir.item.ContentType;
import edu.ur.ir.item.CopyrightStatement;
import edu.ur.ir.item.ExtentType;
import edu.ur.ir.item.IdentifierType;
import edu.ur.ir.item.LanguageType;
import edu.ur.ir.item.PlaceOfPublication;
import edu.ur.ir.item.Publisher;
import edu.ur.ir.item.Series;
import edu.ur.ir.item.Sponsor;
import edu.ur.ir.person.ContributorType;
import edu.ur.ir.person.PersonName;
import edu.ur.ir.user.IrUser;
import edu.ur.order.OrderType;

/**
 * Default Implementation of the institutional item version service.
 * 
 * @author Nathan Sarr
 *
 */
public class DefaultInstitutionalItemVersionService implements InstitutionalItemVersionService{
	
	/** eclipse generated id */
	private static final long serialVersionUID = -5485702177414582275L;

	/** Institutional item version Data access. */
	private InstitutionalItemVersionDAO institutionalItemVersionDAO;
	
	/** url generator for institutional items. */
	private InstitutionalItemVersionUrlGenerator institutionalItemVersionUrlGenerator;

	/** data access for handle information */
	private HandleInfoDAO handleInfoDAO;
	
	/**  Service for processing indexing institutional items */
	private InstitutionalItemIndexProcessingRecordService institutionalItemIndexProcessingRecordService;

	/** Service for dealing with processing types */
	private IndexProcessingTypeService indexProcessingTypeService;
	
	/**  Logger for managing content types*/
	private static final Logger log = LogManager.getLogger(DefaultInstitutionalItemVersionService.class);

	
	/**
	 * Get the institutional item version.
	 * 
	 */
	public InstitutionalItemVersion getInstitutionalItemVersion(Long id, boolean lock) {
		return institutionalItemVersionDAO.getById(id, lock);
	}
	

	/**
	 * Save Institutional Item Version
	 * 
	 * @param institutionalItemVersion
	 */
	public void saveInstitutionalItemVersion(InstitutionalItemVersion institutionalItemVersion) {
		institutionalItemVersionDAO.makePersistent(institutionalItemVersion);
	}

	
	/**
	 * Get the institutional item version by the handle id.
	 * 
	 * @see edu.ur.ir.institution.InstitutionalItemService#getInstitutionalItemByHandleId(java.lang.Long)
	 */
	public InstitutionalItemVersion getInstitutionalItemByHandleId(Long handleId) {
		return institutionalItemVersionDAO.getItemVersionByHandleId(handleId);
	}

	
	/**
	 * Get the items by sponsor count.
	 * 
	 * @see edu.ur.ir.institution.InstitutionalItemService#getItemsBySponsorCount(long)
	 */
	public Long getItemsBySponsorCount(long sponsorId) {
		return institutionalItemVersionDAO.getItemsBySponsorCount(sponsorId);
	}

	
	/**
	 * Get the item sponsored by the given sponsor ordered by item name.
	 * 
	 * @see edu.ur.ir.institution.InstitutionalItemService#getItemsBySponsorItemNameOrder(int, int, long, edu.ur.order.OrderType)
	 */
	public List<InstitutionalItemVersionDownloadCount> getItemsBySponsorItemNameOrder(
			int rowStart, int maxResults, long sponsorId, OrderType orderType) {
		return institutionalItemVersionDAO.getItemsBySponsorItemNameOrder(rowStart, maxResults, sponsorId, orderType);
	}

	/**
	 * Get the items sponsored by the given sponsor ordered by deposit date.
	 * 
	 * @see edu.ur.ir.institution.InstitutionalItemService#getItemsBySponsorItemDepositDateOrder(int, int, long, edu.ur.order.OrderType)
	 */
	public List<InstitutionalItemVersionDownloadCount> getItemsBySponsorItemDepositDateOrder(
			int rowStart, int maxResults, long sponsorId, OrderType orderType) {
		return institutionalItemVersionDAO.getItemsBySponsorItemDepositDateOrder(rowStart, maxResults, sponsorId, orderType);
	}
	
	/**
	 * 
	 * @see edu.ur.ir.institution.InstitutionalItemService#getItemsBySponsorItemDownloadOrder(int, int, long, edu.ur.order.OrderType)
	 */
	public List<InstitutionalItemVersionDownloadCount> getItemsBySponsorItemDownloadOrder(
			int rowStart, int maxResults, long sponsorId, OrderType orderType) {
		return institutionalItemVersionDAO.getItemsBySponsorItemDownloadOrder(rowStart, maxResults, sponsorId, orderType);
	}

	/**
	 * Get the list of publication versions for names ordered by download.
	 * 
	 * @see edu.ur.ir.institution.InstitutionalItemService#getPublicationVersionsForNamesByDownload(int, int, java.util.Set, edu.ur.order.OrderType)
	 */
	public List<InstitutionalItemVersionDownloadCount> getPublicationVersionsForNamesByDownload(
			int rowStart, int maxResults, Set<PersonName> personNames,
			OrderType orderType) {
		List<Long> ids = new ArrayList<Long>();
		for (PersonName p: personNames) {
			ids.add(p.getId());
		}
		return institutionalItemVersionDAO.getPublicationVersionsForNamesByDownload(rowStart, maxResults, ids, orderType);
	}
	
	/**
	 * Get publication verssions for a set of names ordered by title.
	 * 
	 * @see edu.ur.ir.institution.InstitutionalItemService#getPublicationVersionsForNamesByTitle(int, int, java.util.Set, edu.ur.order.OrderType)
	 */
	public List<InstitutionalItemVersionDownloadCount> getPublicationVersionsForNamesByTitle(
			int rowStart, int maxResults, Set<PersonName> personNames,
			OrderType orderType) {
		List<Long> ids = new ArrayList<Long>();
		for (PersonName p: personNames) {
			ids.add(p.getId());
		}
		return institutionalItemVersionDAO.getPublicationVersionsForNamesByTitle(rowStart, maxResults, ids, orderType);
	}
	
	/**
	 * Get the number of downloads for a given set of name ids.
	 * 
	 * @see edu.ur.ir.statistics.DownloadStatisticsService#getNumberOfDownlodsForPersonNames(java.util.List)
	 */
	public Long getNumberOfDownlodsForPersonNames(Set<PersonName> personNames) {
		List<Long> ids = new ArrayList<Long>();
		for (PersonName p: personNames) {
			ids.add(p.getId());
		}
		return institutionalItemVersionDAO.getDownloadCountByPersonName(ids);
	}
	
	/**
	 * Get the publications for a set of names ordered by submission date.
	 * 
	 * @see edu.ur.ir.institution.InstitutionalItemService#getPublicationVersionsForNamesBySubmissionDate(int, int, java.util.Set, edu.ur.order.OrderType)
	 */
	public List<InstitutionalItemVersionDownloadCount> getPublicationVersionsForNamesBySubmissionDate(
			int rowStart, int maxResults, Set<PersonName> personNames,
			OrderType orderType) {
		List<Long> ids = new ArrayList<Long>();
		for (PersonName p: personNames) {
			ids.add(p.getId());
		}
		return institutionalItemVersionDAO.getPublicationVersionsForNamesBySubmissionDate(rowStart, maxResults, ids, orderType);
	}
	
	/**
	 * Update the handle for a given item.
	 * 
	 * @see edu.ur.ir.institution.InstitutionalItemService#resetHandle(edu.ur.ir.institution.InstitutionalItem, edu.ur.ir.institution.InstitutionalItemVersion)
	 */
	public void resetHandle(InstitutionalItemVersion institutionalItemVersion)
	{
		HandleInfo info = institutionalItemVersion.getHandleInfo();
		if( info != null )
		{	
		    String url = institutionalItemVersionUrlGenerator.createUrl(institutionalItemVersion.getVersionedInstitutionalItem().getInstitutionalItem(), institutionalItemVersion.getVersionNumber());
		    info.setData(url);
		    handleInfoDAO.makePersistent(info);
		}
	}
	
	/**
	 * Get the institutional item versions by generic item id.
	 * 
	 * @see edu.ur.ir.institution.InstitutionalItemService#getInstitutionalItemVersionsByGenericItemId(java.lang.Long)
	 */
	
	public List<InstitutionalItemVersion> getInstitutionalItemVersionsByGenericItemId(
			Long genericItemId) {
		return institutionalItemVersionDAO.getInstitutionalItemVersionsByGenericItemId(genericItemId);
	}


	public InstitutionalItemVersionDAO getInstitutionalItemVersionDAO() {
		return institutionalItemVersionDAO;
	}

	public void setInstitutionalItemVersionDAO(
			InstitutionalItemVersionDAO institutionalItemVersionDAO) {
		this.institutionalItemVersionDAO = institutionalItemVersionDAO;
	}
	
	public InstitutionalItemVersionUrlGenerator getInstitutionalItemVersionUrlGenerator() {
		return institutionalItemVersionUrlGenerator;
	}


	public void setInstitutionalItemVersionUrlGenerator(
			InstitutionalItemVersionUrlGenerator institutionalItemVersionUrlGenerator) {
		this.institutionalItemVersionUrlGenerator = institutionalItemVersionUrlGenerator;
	}


	public HandleInfoDAO getHandleInfoDAO() {
		return handleInfoDAO;
	}


	public void setHandleInfoDAO(HandleInfoDAO handleInfoDAO) {
		this.handleInfoDAO = handleInfoDAO;
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


	/**
	 * Sets all vesions as updated for all institutional item versions pointing to the
	 * specified generic item 
	 * 
	 * @see edu.ur.ir.institution.InstitutionalItemVersionService#setAllVersionsAsUpdated(java.lang.Long, java.lang.String)
	 */
	public void setAllVersionsAsUpdated(IrUser user, Long genericItemId, String reason) {
		List<InstitutionalItemVersion> institutionalItemVersions = getInstitutionalItemVersionsByGenericItemId(genericItemId);
        for( InstitutionalItemVersion version : institutionalItemVersions)
        {
        	version.updateLastModified(user, reason);
        	saveInstitutionalItemVersion(version);
        }
		
	}


	/**
	 * Get the institutional item versions in id ascending order.
	 * 
	 * @see edu.ur.ir.institution.InstitutionalItemVersionService#getItemsIdOrder(long, int, edu.ur.order.OrderType)
	 */
	public List<InstitutionalItemVersion> getItemsIdOrder(
			long lastInstitutionalItemVersionId, int maxResults) {
		return institutionalItemVersionDAO.getItemsIdOrder(lastInstitutionalItemVersionId, 
				maxResults);
	}


	/**
	 * Get a list of institutional items ordered by institutional item version id order ascending.
	 * 
	 * @param lastInstitutionalItemVersionId - the last institutional item version id
	 * to be processed.  Use 0 if no items have yet been processed.  Will grab max results
	 * of where ids are greater than the given id.
	 * 
	 * @param institutional collection - the set to look within
	 * @param maxResults - maximum number of results
	 * 
	 * @return - items greater than the given id and belong to the specified set
	 */
	public List<InstitutionalItemVersion> getItemsIdOrder( long lastInstitutionalItemVersionId,
			InstitutionalCollection institutionalCollection, int maxResults)
	{
	    return 	institutionalItemVersionDAO.getItemsIdOrder(lastInstitutionalItemVersionId, 
	    		institutionalCollection, 
	    		maxResults);
	}


	/**
	 * Get a count
	 * @see edu.ur.ir.institution.InstitutionalItemVersionService#getCount()
	 */
	public Long getCount() {
		return institutionalItemVersionDAO.getCount();
	}


	/**
	 * Get a count of all institutional item versions within the collection.
	 * 
	 * @see edu.ur.ir.institution.InstitutionalItemVersionService#getCount(edu.ur.ir.institution.InstitutionalCollection)
	 */
	public Long getCount(InstitutionalCollection collection) {
		return institutionalItemVersionDAO.getCount(collection);
	}


	/**
	 * @see edu.ur.ir.institution.InstitutionalItemVersionService#getItemsBetweenModifiedDatesCount(java.util.Date, java.util.Date, edu.ur.ir.institution.InstitutionalCollection)
	 */
	public Long getItemsBetweenModifiedDatesCount(Date fromModifiedDate,
			Date untilModifiedDate,
			InstitutionalCollection institutionalCollection) {
		return institutionalItemVersionDAO.getItemsBetweenModifiedDatesCount(fromModifiedDate, untilModifiedDate, institutionalCollection);
	}


	/**
	 * 
	 * @see edu.ur.ir.institution.InstitutionalItemVersionService#getItemsBetweenModifiedDatesCount(java.util.Date, java.util.Date)
	 */
	public Long getItemsBetweenModifiedDatesCount(Date fromModifiedDate,
			Date untilModifiedDate) {
		return institutionalItemVersionDAO.getItemsBetweenModifiedDatesCount(fromModifiedDate, untilModifiedDate);
	}


	/**
	 * 
	 * @see edu.ur.ir.institution.InstitutionalItemVersionService#getItemsFromModifiedDateCount(java.util.Date, edu.ur.ir.institution.InstitutionalCollection)
	 */
	public Long getItemsFromModifiedDateCount(Date fromModifiedDate,
			InstitutionalCollection institutionalCollection) {
		return institutionalItemVersionDAO.getItemsFromModifiedDateCount(fromModifiedDate, institutionalCollection);
	}


	/**
	 * 
	 * @see edu.ur.ir.institution.InstitutionalItemVersionService#getItemsUntilModifiedDateCount(java.util.Date)
	 */
	public Long getItemsUntilModifiedDateCount(Date untilModifiedDate) {
		return institutionalItemVersionDAO.getItemsUntilModifiedDateCount(untilModifiedDate);
	}


	/**
	 * @see edu.ur.ir.institution.InstitutionalItemVersionService#getItemsUntilModifiedDateCount(java.util.Date, edu.ur.ir.institution.InstitutionalCollection)
	 */
	public Long getItemsUntilModifiedDateCount(Date untilModifiedDate,
			InstitutionalCollection institutionalCollection) {
		return institutionalItemVersionDAO.getItemsUntilModifiedDateCount(untilModifiedDate, institutionalCollection);
	}


	/**
	 * @see edu.ur.ir.institution.InstitutionalItemVersionService#getItemsIdOrderFromModifiedDate(long, java.util.Date, int)
	 */
	public List<InstitutionalItemVersion> getItemsIdOrderFromModifiedDate(
			long lastInstitutionalItemVersionId, Date fromModifiedDate,
			int maxResults) {
		return institutionalItemVersionDAO.getItemsIdOrderFromModifiedDate(lastInstitutionalItemVersionId, 
				fromModifiedDate, maxResults);
	}


	/**
	 * @see edu.ur.ir.institution.InstitutionalItemVersionService#getItemsFromModifiedDateCount(java.util.Date)
	 */
	public Long getItemsFromModifiedDateCount(Date fromModifiedDate) {
		return institutionalItemVersionDAO.getItemsFromModifiedDateCount(fromModifiedDate);
	}


	/**
	 * @see edu.ur.ir.institution.InstitutionalItemVersionService#getItemsIdOrderUntilModifiedDate(long, java.util.Date, int)
	 */
	public List<InstitutionalItemVersion> getItemsIdOrderUntilModifiedDate(
			long lastInstitutionalItemVersionId, Date untilModifiedDate,
			int maxResults) {
		return institutionalItemVersionDAO.getItemsIdOrderUntilModifiedDate(lastInstitutionalItemVersionId, 
				untilModifiedDate, maxResults);
	}


	/**
	 * @see edu.ur.ir.institution.InstitutionalItemVersionService#getItemsIdOrderBetweenModifiedDates(long, java.util.Date,
	 *  java.util.Date, int)
	 */
	public List<InstitutionalItemVersion> getItemsIdOrderBetweenModifiedDates(
			long lastInstitutionalItemVersionId, Date fromModifiedDate,
			Date untilModifiedDate, int maxResults) {
		return institutionalItemVersionDAO.getItemsIdOrderBetweenModifiedDates(lastInstitutionalItemVersionId,
				fromModifiedDate, untilModifiedDate, maxResults);
	}


	/**
	 * @see edu.ur.ir.institution.InstitutionalItemVersionService#getItemsIdOrderFromModifiedDate(long, java.util.Date, edu.ur.ir.institution.InstitutionalCollection, int)
	 */
	public List<InstitutionalItemVersion> getItemsIdOrderFromModifiedDate(
			long lastInstitutionalItemVersionId, Date fromModifiedDate,
			InstitutionalCollection institutionalCollection, int maxResults) {
		return institutionalItemVersionDAO.getItemsIdOrderFromModifiedDate(lastInstitutionalItemVersionId, fromModifiedDate, institutionalCollection, maxResults);
	}


	/**
	 * @see edu.ur.ir.institution.InstitutionalItemVersionService#getItemsIdOrderUntilModifiedDate(long, java.util.Date, edu.ur.ir.institution.InstitutionalCollection, int)
	 */
	public List<InstitutionalItemVersion> getItemsIdOrderUntilModifiedDate(
			long lastInstitutionalItemVersionId, Date untilModifiedDate,
			InstitutionalCollection institutionalCollection, int maxResults) {
		return institutionalItemVersionDAO.getItemsIdOrderUntilModifiedDate(lastInstitutionalItemVersionId, 
				untilModifiedDate, institutionalCollection, maxResults);
	}


	/**
	 * @see edu.ur.ir.institution.InstitutionalItemVersionService#getItemsIdOrderBetweenModifiedDates(long, java.util.Date, java.util.Date, edu.ur.ir.institution.InstitutionalCollection, int)
	 */
	public List<InstitutionalItemVersion> getItemsIdOrderBetweenModifiedDates(
			long lastInstitutionalItemVersionId, Date fromModifiedDate,
			Date untilModifiedDate,
			InstitutionalCollection institutionalCollection, int maxResults) {
		return institutionalItemVersionDAO.getItemsIdOrderBetweenModifiedDates(lastInstitutionalItemVersionId, fromModifiedDate, 
				untilModifiedDate, institutionalCollection, maxResults);
	}


	/**
	 * Set all institutional item versions as updated that have the specified content type.
	 * 
	 * @see edu.ur.ir.institution.InstitutionalItemVersionService#setAllVersionsAsUpdatedForContentType(java.lang.Long, java.lang.Long, java.lang.String)
	 */
	public Long setAllVersionsAsUpdatedForContentType(ContentType contentType,
			IrUser user, String message) {
	    Long indexCount = institutionalItemIndexProcessingRecordService.insertAllItemsForContentType(contentType, indexProcessingTypeService.get(IndexProcessingTypeService.UPDATE));
		Long updatedCount = institutionalItemVersionDAO.setAsModifiedByContentTypeChange(contentType, user, message);
		log.debug("Total number of content type records set as updated = " + updatedCount);
		log.debug("Total number of content type records set as index = " + indexCount);
		return updatedCount;
	}


	/**
	 * Set all institutional item versions as updated that have the specified contributor type. 
	 * 
	 * @see edu.ur.ir.institution.InstitutionalItemVersionService#setAllVersionsAsUpdatedForContributorType(java.lang.Long, edu.ur.ir.user.IrUser, java.lang.String)
	 */
	public Long setAllVersionsAsUpdatedForContributorType(
			ContributorType contributorType, IrUser user, String message) {
	    Long indexCount = institutionalItemIndexProcessingRecordService.insertAllItemsForContributorType(contributorType, indexProcessingTypeService.get(IndexProcessingTypeService.UPDATE));
	    Long updatedCount = institutionalItemVersionDAO.setAsModifiedByContributorTypeChange(contributorType, user, message);
		log.debug("Total number of contributor type records set as updated = " + updatedCount);
		log.debug("Total number of contributor type records set as index = " + indexCount);
		return updatedCount;
	}


	/**
	 * Set all institutional item versions as updated that have the specified copyright statement.
	 * 
	 * @see edu.ur.ir.institution.InstitutionalItemVersionService#setAllVersionsAsUpdatedForCopyrightStatement(edu.ur.ir.item.CopyrightStatement, edu.ur.ir.user.IrUser, java.lang.String)
	 */
	public Long setAllVersionsAsUpdatedForCopyrightStatement(
			CopyrightStatement copyrightStatement, IrUser user, String message) {
		
		Long updatedCount = institutionalItemVersionDAO.setAsModifiedByCopyrightStatementChange(copyrightStatement, user, message);
		log.debug("Total number of copyright statement records set as updated = " + updatedCount);
		return updatedCount;

	}


	/**
	 * Set all institutional item versions as updated that have the specified extent type
	 * 
	 * @see edu.ur.ir.institution.InstitutionalItemVersionService#setAllVersionsAsUpdatedForExtentType(edu.ur.ir.item.ExtentType, edu.ur.ir.user.IrUser, java.lang.String)
	 */
	public Long setAllVersionsAsUpdatedForExtentType(ExtentType extentType,
			IrUser user, String message) {
		Long updatedCount =  institutionalItemVersionDAO.setAsModifiedByExtentTypeChange(extentType, user, message);
		log.debug("Total number of extent type records set as updated = " + updatedCount);
		return updatedCount;
	}


	/**
	 * Set all versions as updated for the identifier type.
	 * 
	 * @see edu.ur.ir.institution.InstitutionalItemVersionService#setAllVersionsAsUpdatedForIdentifierType(edu.ur.ir.item.IdentifierType, edu.ur.ir.user.IrUser, java.lang.String)
	 */
	public Long setAllVersionsAsUpdatedForIdentifierType(
			IdentifierType identifierType, IrUser user, String message) {
	    Long indexCount = institutionalItemIndexProcessingRecordService.insertAllItemsForIdentifierType(identifierType, indexProcessingTypeService.get(IndexProcessingTypeService.UPDATE));
	    Long updatedCount = institutionalItemVersionDAO.setAsModifiedByIdentifierTypeChange(identifierType, user, message);
	    log.debug("Total number of identifier type records set as updated = " + updatedCount);
		log.debug("Total number of identifier type records set as index = " + indexCount);
		return updatedCount;
	}
	
	/**
	 * Set all versions as updated for the language type.
	 * 
	 * @see edu.ur.ir.institution.InstitutionalItemVersionService#setAllVersionsAsUpdatedForLanguageType(edu.ur.ir.item.LanguageType, edu.ur.ir.user.IrUser, java.lang.String)
	 */
	public Long setAllVersionsAsUpdatedForLanguageType(
			LanguageType languageType, IrUser user, String message) {
	    Long indexCount = institutionalItemIndexProcessingRecordService.insertAllItemsForLangaugeType(languageType, indexProcessingTypeService.get(IndexProcessingTypeService.UPDATE));
	    Long updatedCount =  institutionalItemVersionDAO.setAsModifiedByLanguageTypeChange(languageType, user, message);
	    log.debug("Total number of language type records set as updated = " + updatedCount);
		log.debug("Total number of language type records set as index = " + indexCount);
		return updatedCount;
	}
	
	
	/**
	 * Set all versions as updated for the place publication.
	 * 
	 * @see edu.ur.ir.institution.InstitutionalItemVersionService#setAllVersionsAsUpdatedForIdentifierType(edu.ur.ir.item.IdentifierType, edu.ur.ir.user.IrUser, java.lang.String)
	 */
	public Long setAllVersionsAsUpdatedForPlaceOfPublication(
			PlaceOfPublication placeOfPublication, IrUser user, String message) {
	    Long indexCount = institutionalItemIndexProcessingRecordService.insertAllItemsForPlaceOfPublication(placeOfPublication, indexProcessingTypeService.get(IndexProcessingTypeService.UPDATE));
	    Long updatedCount =  institutionalItemVersionDAO.setAsModifiedByPlaceOfPublicationChange(placeOfPublication, user, message);
	    log.debug("Total number of language type records set as updated = " + updatedCount);
		log.debug("Total number of language type records set as index = " + indexCount);
		return updatedCount;
	}


	/**
	 * Set all versions as updated for the person name
	 * 
	 * @see edu.ur.ir.institution.InstitutionalItemVersionService#setAllVersionsAsUpdatedForPersonName(edu.ur.ir.person.PersonName, edu.ur.ir.user.IrUser, java.lang.String)
	 */
	public Long setAllVersionsAsUpdatedForPersonName(PersonName personName,
			IrUser user, String message) {
	     Long indexCount = institutionalItemIndexProcessingRecordService.insertAllItemsForPersonName(personName, indexProcessingTypeService.get(IndexProcessingTypeService.UPDATE));
		 Long updatedCount =   institutionalItemVersionDAO.setAsModifiedByPersonNameChange(personName, user, message);
		 log.debug("Total number of person name records set as updated = " + updatedCount);
		 log.debug("Total number of person name records set as index = " + indexCount);
		 return updatedCount;
	}


	/**
	 * Set all versions as updated for the publisher.
	 * 
	 * @see edu.ur.ir.institution.InstitutionalItemVersionService#setAllVersionsAsUpdatedForPublisher(edu.ur.ir.item.Publisher, edu.ur.ir.user.IrUser, java.lang.String)
	 */
	public Long setAllVersionsAsUpdatedForPublisher(Publisher publisher,
			IrUser user, String message) {
		
	     Long indexCount = institutionalItemIndexProcessingRecordService.insertAllItemsForPublisher(publisher, indexProcessingTypeService.get(IndexProcessingTypeService.UPDATE));
		 Long updatedCount =   institutionalItemVersionDAO.setAsModifiedByPublisherChange(publisher, user, message);
		 log.debug("Total number of publisher records set as updated = " + updatedCount);
		 log.debug("Total number of publisher records set as index = " + indexCount);
		 return updatedCount;
	}


	/**
	 * Set all versions as updated for the publisher.
	 * 
	 * @see edu.ur.ir.institution.InstitutionalItemVersionService#setAllVersionsAsUpdatedForSeries(edu.ur.ir.item.Series, edu.ur.ir.user.IrUser, java.lang.String)
	 */
	public Long setAllVersionsAsUpdatedForSeries(Series series, IrUser user,
			String message) {
	     Long indexCount = institutionalItemIndexProcessingRecordService.insertAllItemsForSeries(series, indexProcessingTypeService.get(IndexProcessingTypeService.UPDATE));
		 Long updatedCount =   institutionalItemVersionDAO.setAsModifiedBySeriesChange(series, user, message);
		 log.debug("Total number of series records set as updated = " + updatedCount);
		 log.debug("Total number of series records set as index = " + indexCount);
		 return updatedCount;

	}


	/**
	 * Set all versions as updated for the sponsor.
	 * 
	 * @see edu.ur.ir.institution.InstitutionalItemVersionService#setAllVersionsAsUpdatedForSponsor(edu.ur.ir.item.Sponsor, edu.ur.ir.user.IrUser, java.lang.String)
	 */
	public Long setAllVersionsAsUpdatedForSponsor(Sponsor sponsor, IrUser user,
			String message) {
	     Long indexCount = institutionalItemIndexProcessingRecordService.insertAllItemsForSponsor(sponsor, indexProcessingTypeService.get(IndexProcessingTypeService.UPDATE));
		 Long updatedCount =   institutionalItemVersionDAO.setAsModifiedBySponsorChange(sponsor, user, message);
		 log.debug("Total number of sponsor records set as updated = " + updatedCount);
		 log.debug("Total number of sponsor records set as index = " + indexCount);
		 return updatedCount;

	}
	
	


}
