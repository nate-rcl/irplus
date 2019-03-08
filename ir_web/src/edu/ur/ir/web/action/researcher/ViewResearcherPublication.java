package edu.ur.ir.web.action.researcher;


import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.opensymphony.xwork2.ActionSupport;

import edu.ur.ir.item.GenericItem;
import edu.ur.ir.item.ItemObject;
import edu.ur.ir.researcher.Researcher;
import edu.ur.ir.researcher.ResearcherFileSystemService;
import edu.ur.ir.researcher.ResearcherPublication;

import edu.ur.ir.web.action.UserIdAware;
import edu.ur.order.AscendingOrderComparator;

/**
 * Action for viewing a researcher publication.
 * @author Nathan Sarr
 *
 */
public class ViewResearcherPublication extends ActionSupport implements UserIdAware {

	/**  Eclipse generated id */
	private static final long serialVersionUID = -4433567083486517704L;
	
	/** id of the researcher publication being accessed */
	private Long researcherPublicationId;

	/** Logger for add files to item action */
	private static final Logger log = LogManager.getLogger(ViewResearcherPublication.class);

	/** Service for dealing with researcher file system information */
	private ResearcherFileSystemService researcherFileSystemService;

	/** Id of the personal item to add the files */
	private Long researcherId;

	/** User logged in */
	private Researcher researcher;
	
	/** user trying to access the publication */
	private Long userId;

	/**  Generic Item being viewed */
	private GenericItem item;

	/** Item object sorted for display */
	private List<ItemObject> itemObjects;

	/**
	 * Execute method
	 * 
	 */
	public String execute() 
	{
        log.debug("execute called");
        
        if( researcherPublicationId == null || researcherPublicationId <= 0  )
        {
        	return "notFound";
        }
        
        ResearcherPublication publication = researcherFileSystemService.getResearcherPublication(researcherPublicationId, false);
        
        if( publication == null)
        {
        	return "notFound";
        }
        
        researcher = publication.getResearcher();
        
		if( researcher.isPublic() || researcher.getUser().getId().equals(userId))
		{
			item = publication.getPublication();
			itemObjects = item.getItemObjects();
			
			// Sort item objects by order
			Collections.sort(itemObjects,   new AscendingOrderComparator());
			return SUCCESS;
		}
		else
		{
			return "accessDenied";
		}
		
	}

	/**
	 * Get the generic item
	 * 
	 * @return
	 */
	public GenericItem getItem() {
		return item;
	}


	/**
	 * Set the generic item.
	 * 
	 * @param item
	 */
	public void setItem(GenericItem item) {
		this.item = item;
	}


	/**
	 * Get the item objects.
	 * 
	 * @return
	 */
	public List<ItemObject> getItemObjects() {
		return itemObjects;
	}


	/**
	 * Set the item objects.
	 * 
	 * @param itemObjects
	 */
	public void setItemObjects(List<ItemObject> itemObjects) {
		this.itemObjects = itemObjects;
	}
	
	/**
	 * Get the item id
	 * 
	 * @return item id
	 */
	public Long getItemId() {
		return researcherId;
	}

	/**
	 * Set the item id
	 * 
	 * @param researcherId
	 *            item id
	 */
	public void setItemId(Long researcherId) {
		this.researcherId = researcherId;
	}

	/**
	 * Get the researcher
	 * 
	 * @return researcher
	 */
	public Researcher getResearcher() {
		return researcher;
	}

	/**
	 * Set the researcher
	 * 
	 * @param researcher
	 *            researcher
	 */
	public void setResearcher(Researcher researcher) {
		this.researcher = researcher;
	}

	public Long getResearcherId() {
		return researcherId;
	}

	public void setResearcherId(Long researcherId) {
		this.researcherId = researcherId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}
	
	public void setResearcherPublicationId(Long researcherPublicationId) {
		this.researcherPublicationId = researcherPublicationId;
	}

	public ResearcherFileSystemService getResearcherFileSystemService() {
		return researcherFileSystemService;
	}

	public void setResearcherFileSystemService(
			ResearcherFileSystemService researcherFileSystemService) {
		this.researcherFileSystemService = researcherFileSystemService;
	}

	public Long getResearcherPublicationId() {
		return researcherPublicationId;
	}

}
