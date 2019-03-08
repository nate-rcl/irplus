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

package edu.ur.ir.item;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import edu.ur.ir.file.IrFile;
import edu.ur.ir.item.ItemFile;
import edu.ur.ir.item.ItemLink;
import edu.ur.ir.item.ItemObject;
import edu.ur.ir.person.Contributor;
import edu.ur.ir.person.PersonName;
import edu.ur.ir.repository.License;
import edu.ur.ir.user.IrUser;
import edu.ur.order.AscendingOrderComparator;
import edu.ur.persistent.CommonPersistent;

/**
 * Base implementation of a generic item.  This contains all of the information
 * for an publication
 * 
 * @author Nathan Sarr
 *
 */
public class GenericItem extends CommonPersistent implements Cloneable {
	
	/** Separator that should be used for keywords */
	public static final String KEYWORD_SEPARATOR = ";";

    /** leading articles for the name */
    private String leadingNameArticles;
    
	/** first character of the name of this item's name  */
	private char nameFirstChar;
	
	/** lower case value of the name  this is for database performance */
	private String lowerCaseName;
    
	/**  Logger */
	private static final Logger log = LogManager.getLogger(GenericItem.class);
	
	/**  Files associated with this item. */
	private Set<ItemFile> itemFiles = new HashSet<ItemFile>();
	
	/**    Primary image file - this is the primary image file for the generic item*/
	private ItemFile primaryImageFile;
	
	/**  Set of links for information about this item. */
	private Set<ItemLink> links = new HashSet<ItemLink>();
	
	/** Generated id */
	private static final long serialVersionUID = -1438304568529011205L;
	
	/** The first time the publication was shown or presented to the public */
	private FirstAvailableDate firstAvailableDate;
	
	/** Date this publication can be made available to the public. This is to allow
	 * an item to be embargoed until sometime in the future  */
	private Date releaseDate;
	
	/** Original date this item was created */
	private OriginalItemCreationDate originalItemCreationDate;
	
	/**  People who have contributed to this item. */
	private List<ItemContributor> contributors = new LinkedList<ItemContributor>();
	
	/**  The content types for this item. For example Book, Music piece, etc. */
	private Set<ItemContentType> itemContentTypes = new HashSet<ItemContentType>();
	
	/**  Language used by this item. */
	private LanguageType languageType;
	
	/**  Set of licenses applicable to this item. */
	private Set<License> licenses = new HashSet<License>();
	
	/**  Set of identifiers for the item. */
	private Set<ItemIdentifier> itemIdentifiers = new HashSet<ItemIdentifier>();

	/**  Set of extent types for the item. */
	private Set<ItemExtent> itemExtents = new HashSet<ItemExtent>();

	/**  The abstract for the item */
	private String itemAbstract;
	
	/**  The subject keywords for the item - semicolon (;) separated */
	private String itemKeywords;
	
	/**  Indicates the item has been publicly published */
	private boolean publishedToSystem = false;
	
	/** Owner of the item */
	private IrUser owner;
	
	/**  Series and report numbers for this item. */
	private Set<ItemReport> itemReports = new HashSet<ItemReport>();
	
	/**  Information about the external publish. */
	private ExternalPublishedItem externalPublishedItem;
	
	/**  sponsor for the item. */
	private Set<ItemSponsor> itemSponsors = new HashSet<ItemSponsor>();
	
	/** Set of sub titles for item */
	private Set<ItemTitle> subTitles = new HashSet<ItemTitle>();
	
	/** indicates whether the item is locked for review */
	private boolean locked = false;
	
	/** Indicates that this item can be viewed by the public. */
	private boolean publiclyViewable = false;
	
	/** Copyright statement for the item */
	private CopyrightStatement copyrightStatement;
	
	/**
	 * Default constructor; 
	 */
	GenericItem(){}
	
	/**
	 * Create an item with the specified name.
	 * 
	 * @param name
	 */
	public GenericItem(String name)
	{
		setName(name);
	}
	
	/**
	 * Create an item with the specified name.
	 * 
	 * @param leading Name articles  - articles that should prefix the name - 
	 * A, an, d', de, the, ye
	 * 
	 * @param name - (Title) / name of the item
	 */
	public GenericItem(String leadingNameArticles, String name)
	{
		this(name);
		setLeadingNameArticles(leadingNameArticles);
	}
	
	/**
	 * Override the set name.
	 * 
	 * @see edu.ur.persistent.CommonPersistent#setName(java.lang.String)
	 */
	public void setName(String name)
	{
		this.name = name.trim();
		if(name.length() > 0)
		{
		    this.nameFirstChar = Character.toLowerCase(name.charAt(0));
		}
		lowerCaseName = name.toLowerCase();

	}
	
	/**
	 * Get a file info object based on it's id.
	 * 
	 * @param id
	 */
	public ItemFile getItemFile(Long id)
	{	
		if( log.isDebugEnabled() )
		{
			log.debug("Searching for file " + id);
		}
		
		ItemFile info = null;
		boolean found = false;
		Iterator<ItemFile> iter = itemFiles.iterator();
		
		while( iter.hasNext() && !found)
		{
			ItemFile f = iter.next();
			if( f.getId().equals(id))
			{
				info = f;
				found = true;
		    }
		}
		return info;
		
	}

	/**
	 * Get a primary file
	 * 
	 * @param id
	 */
	public ItemFile getPrimaryImageFile()
	{	
		return primaryImageFile;
	}
	
	/**
	 * Sets the primary image file.  
	 * 
	 * @param itemFile
	 * @return
	 */
	void setPrimaryImageFile(ItemFile itemFile)
	{
	    primaryImageFile = itemFile;
	}
	
	/**
	 * If the item has a primary image this allows it to be set.  The file must be in the list of
	 * item files for it to be set.  Use removPrimaryImageFile to remove the primary file
	 * 
	 * @param itemFile
	 * @return true if the primary image is set
	 */
	public boolean addPrimaryImageFile(ItemFile itemFile)
	{
		boolean set = false;
		
		for( ItemFile file : itemFiles)
		{
			if(file.equals(itemFile))
			{
				primaryImageFile = itemFile;
				set = true;
			}
		}
		return set;
	}
	
	/**
	 * Remove the primary image file
	 */
	public void removePrimaryImageFile()
	{
		primaryImageFile = null;
	}
	
	/**
	 * Get a link info object based on it's id.
	 * 
	 * @param id
	 */
	public ItemLink getItemLink(Long id)
	{	
		if( log.isDebugEnabled() )
		{
			log.debug("Searching for link " + id);
		}
		
		ItemLink info = null;
		boolean found = false;
		Iterator<ItemLink> iter = links.iterator();
		
		while( iter.hasNext() && !found)
		{
			ItemLink l = iter.next();
			if( l.getId().equals(id))
			{
				info = l;
				found = true;
		    }
		}
		return info;
		
	}
	/**
	 * Get a item object based on the position.
	 * 
	 * @param id
	 */
	public ItemObject getItemObjectByPosition(int position)
	{	
		ItemObject itemObject = null;
		boolean found = false;
		Iterator<ItemObject> iter = getItemObjects().iterator();
		
		while( iter.hasNext() && !found)
		{
			ItemObject i = iter.next();
			if( i.getOrder() == position)
			{
				itemObject = i;
				found = true;
		    }
		}
		return itemObject;
		
	}
	
	/**
	 * Get a file info object based on it's name
	 * 
	 * @param name
	 */
	public ItemFile getItemFile(String name)
	{
		if( log.isDebugEnabled() )
		{
			log.debug("Searching for file " + name);
		}
		
		ItemFile info = null;
		boolean found = false;
		Iterator<ItemFile> iter = itemFiles.iterator();
		
		while( iter.hasNext() && !found)
		{
			ItemFile f = iter.next();
			if( name.equals(f.getIrFile().getName()))
			{
				info = f;
				found = true;
		    }
		}
		return info;
	}

	/**
	 * Get the file information for this item.
	 * 
	 * @return the files for this item.
	 */
	public Set<ItemFile> getItemFiles() {
		return Collections.unmodifiableSet(itemFiles);
	}

	/**
	 * Add a file to this item  
	 * 
	 * @param irFile
	 */
	public ItemFile addFile(IrFile irFile)
	{
		ItemFile itemFile = new ItemFile(this, irFile);
		if( !itemFiles.contains(itemFile))
		{
			// set the order of the file
		    List<ItemObject> allObjects = getItemObjects();
		    ItemObject max = null;
		    if( allObjects.size() > 0 )
		    {
		        max = Collections.max(allObjects, new AscendingOrderComparator());
		    }
		
		    int order = 0;
		    if( max != null )
		    {
			    order = max.getOrder() + 1;
		    }
		    itemFile.setOrder(order);
		   
		    String extension = irFile.getFileInfo().getExtension();
		    itemFile.setCanViewInPlayer(extension != null && PlayerUtil.canPlay(extension));

			itemFiles.add(itemFile);
		} else {
			return null;
		}
		return itemFile;
	}
	
	/**
	 * Remove the irFile from this item.
	 * Sets the irFile's item to null.
	 * 
	 * @param irFile
	 * @return true if the ir file is removed.
	 */
	public boolean removeItemFile(ItemFile itemFile)
	{
		List<ItemObject> allObjects = getItemObjects();
		
		//Re-order the item objects
		for (ItemObject i: allObjects) {
			
			if (i.getOrder() > itemFile.getOrder()) {
				i.setOrder(i.getOrder() - 1 );
			}
		}
		
		if( itemFile.equals(primaryImageFile))
		{
			primaryImageFile = null;
		}
		
		return itemFiles.remove(itemFile);
	}
	
	/**
	 * Add a report to this item  
	 * 
	 * @param series Series report belongs to
	 * @param reportNumber Report number
	 * 
	 * @return Item report
	 */
	public ItemReport addReport(Series series, String reportNumber)
	{
		ItemReport itemReport = new ItemReport(this, series, reportNumber);
		if( !itemReports.contains(itemReport))
		{
			itemReports.add(itemReport);
		}
		return itemReport;
	}
	
	/**
	 * Remove the report from this item.
	 *  
	 * @param itemReport report to be removed
	 * @return true if the report is removed.
	 */
	public boolean removeItemReport(ItemReport itemReport)
	{
		boolean removed = false;
			
		if (itemReports.contains(itemReport)) {
			removed = itemReports.remove(itemReport);
		}

		return removed;
	}

	/**
	 * Remove all the irFile from this item.  This also 
	 * sets the primary file to null
	 * 
	 */
	public void removeAllItemFiles()
	{
		primaryImageFile = null;
		itemFiles.clear();
		
	}
	
	/**
	 * Remove all the reports from this item.
	 * 
	 */
	public void removeAllItemReports()
	{
		itemReports.clear();
		
	}
	
	/**
	 * Remove all the sub titles from this item.
	 * 
	 */
	public void removeAllItemSubTitles()
	{
		subTitles.clear();
		
	}

	/**
	 * Set the files for this item.
	 * 
	 * @param files
	 */
	void setItemFiles(Set<ItemFile> itemFiles) {
		this.itemFiles = itemFiles;
	}
	
	/**
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode()
	{
		int value = 0;
		value += id == null ? 0 : id.hashCode();
		value += name == null ? 0 : name.hashCode();
		value += leadingNameArticles == null ? 0 : leadingNameArticles.hashCode();
		return value;
	}
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (!(o instanceof GenericItem)) return false;

		final GenericItem other = (GenericItem) o;

		/*
		 * Two items can have all same property values(eg. cloned items), but they are different item.
		 * So the equals method checks for Id equals
		 */ 
		if( ( id != null && !id.equals(other.getId()) ) ||
			( id == null && other.getId() != null ) ) return false;

		if( ( name != null && !name.equals(other.getName()) ) ||
			( name == null && other.getName() != null ) ) return false;
		
		if( ( leadingNameArticles != null && !leadingNameArticles.equals(other.getLeadingNameArticles()) ) ||
			( leadingNameArticles == null && other.getLeadingNameArticles() != null ) ) return false;
		
		return true;
	}
	
	/** 
	 * Return a string representation of the string buffer.
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		StringBuffer sb = new StringBuffer("[Item id = ");
		sb.append(id);
		sb.append(" version = " );
		sb.append(version);
		sb.append(" name = " );
		sb.append(name);
		sb.append("]");
		
		return  sb.toString();
	}

	/**
	 * Returns the links for this item as an unmodifiable set.
	 * 
	 * @return
	 */
	public Set<ItemLink> getLinks() {
		return Collections.unmodifiableSet(links);
	}

	/**
	 * Set the Item links for this item.
	 * 
	 * @param itemLinks
	 */
	void setLinks(Set<ItemLink> links) {
		this.links = links;
	}

	/**
	 * Get the contributors for this item.
	 * 
	 * @return the contributors.
	 */
	public List<ItemContributor> getContributors() {
		return Collections.unmodifiableList(contributors);
	}

	/**
	 * Set the list of contributors.
	 * 
	 * @param contributors
	 */
	void setContributors(List<ItemContributor> contributors) {
		this.contributors = contributors;
	}
	
	/**
	 * Add a contributor to this item.
	 * 
	 * @param contributor
	 * @throws DuplicateContributorException - if the contributor already exists.
	 */
	public ItemContributor addContributor(Contributor c) throws DuplicateContributorException
	{
		if( this.getContributor(c) != null)
		{
			throw new DuplicateContributorException("The contributor already exists for this item " +  this, c );
		}
		ItemContributor max = null;
		if( contributors.size() > 0 )
		{
		    max = Collections.max(contributors, new AscendingOrderComparator());
		}
		
		int order = 0;
		if( max != null )
		{
			order = max.getOrder() + 1;
		}
		ItemContributor itemContributor = new ItemContributor(this, order, c);
		contributors.add(itemContributor);
		return itemContributor;
	}
	
	/**
	 * Package protected to add contributor
	 * @param c
	 */
	void addContributor(ItemContributor c)
	{
		c.setItem(this);
		contributors.add(c);
	}

	/**
	 * Find a contributor based on id
	 * 
	 * @param id
	 * @return
	 */
	public ItemContributor getContributor(Long id)
	{
	   ItemContributor contributor = null;
	   for( ItemContributor c : contributors)
	   {
		   if( c.getId().equals(id))
		   {
			   contributor = c;
			   break;
	       }
	   }
	   return contributor;
	}
	
	/**
	 * Find a contributor based on the position
	 * 
	 * @param atPosition
	 * @return
	 */
	public ItemContributor getContributor(int atPosition)
	{
	   ItemContributor contributor = null;
	   for( ItemContributor c : contributors)
	   {
		   if( c.getOrder() == atPosition)
		   {
			   contributor = c;
			   break;
	       }
	   }
	   return contributor;
	}
	
	/**
	 * Find a contributor based on person name
	 * 
	 * @param personName
	 * @return
	 */
	public ItemContributor getContributor(PersonName personName)
	{
	   ItemContributor contributor = null;
	   for( ItemContributor c : contributors)
	   {
		   if( c.getContributor().getPersonName().equals(personName))
		   {
			   contributor = c;
			   break;
	       }
	   }
	   return contributor;
	}
	
	/**
	 * Get an item contributor based on the contributor.
	 * 
	 * @param c - contributor who should be in the Item contributor
	 * @return the found contributor or null if the contributor is not found
	 */
	public ItemContributor getContributor(Contributor c)
	{
		for(ItemContributor contrib : contributors)
		{
			if(contrib.getContributor().equals(c) )
			{
				return contrib;
			}
		}
		return null;
	}

	/**
	 * Find a Link based on name
	 * 
	 * @param name Link name
	 * @return
	 */
	public ItemLink getItemLink(String name)
	{
	    for( ItemLink i : links)
	    {
		   if( i.getName().equals(name))
		   {
			   return i;
	       }
	    }
	    return null;
	}
	
	/**
	 * Remove a contributor from the list of contributors.
	 * 
	 * @param c
	 * @return
	 */
	public boolean removeContributor(ItemContributor contributor)
	{
        boolean removed = contributors.remove(contributor);
		
		if( removed )
		{
		    
		   Collections.sort(contributors, new AscendingOrderComparator());
		   int index = 0;
		   for(ItemContributor c : contributors)
		   {
			   c.setOrder(index);
			   index = index + 1;
		   }
		}
		
		return removed;

		
	}
	
    /**
     * Move the contributor to the specified order position.  If the order is less than 1
     * it is moved to the beginning of the list.  If the order is larger than the 
     * size of the list it is moved to the end.  The list is re-ordered
 
     * @param contributor - item object being moved
     * @param position - position to move th object to
     * @return - true if the link is moved
     * 
     */
    public boolean moveContributor(ItemContributor c, int position) 
    {
    	ItemContributor itemContributor = this.getContributor(c.getContributor());
    	if( itemContributor == null)
    	{
    		return false;
    	}
    	
    	int currentLinkPosition = itemContributor.getOrder();
    	
    	// if the position is greater than the size of the list put it at the end
		if(position > (contributors.size() - 1))
		{
			position = contributors.size() - 1;
		}
		
		if( position < 0 )
		{
			position = 0;
		}
		
		int change = 0;
		
		
		// moving up in list
		if( (currentLinkPosition - position) > 0 )
		{
			change = 1;
		}
		else // moving down in list
		{
			change = -1;
		}
		

		for(ItemContributor aContributor : contributors)
		{
		    if(!aContributor.equals(c))
			{
		    	// move up
		    	if( change == 1)
		    	{
		    		if( aContributor.getOrder() < currentLinkPosition && aContributor.getOrder() >= position)
		    		{
		    			aContributor.setOrder(aContributor.getOrder() + change);
		    		}
		    	}
		    	else if( change == -1)
		    	{
		    		if( aContributor.getOrder() <= position && aContributor.getOrder() > currentLinkPosition)
		    		{
		    			aContributor.setOrder(aContributor.getOrder() + change);
		    		}
		    	}
			}
		    else
		    {
		    	aContributor.setOrder(position);
		    }
		   
		}
		
		// order the links
    	Collections.sort(contributors , new AscendingOrderComparator());
    	return true;
    }
    
    /**
     * Move the item object to the specified order position.  If the order is less than 1
     * it is moved to the beginning of the list.  If the order is larger than the 
     * size of the list it is moved to the end.  The list is re-ordered
 
     * @param item object - object to move
     * @param position - position to move the object to.
     * @return - true if the link is moved
     * 
     */
    public boolean moveItemObject(ItemObject o, int position) 
    {
    	ItemObject theObject = null;
    	
    	if( o.getType().equals(ItemFile.TYPE))
    	{
    		theObject = this.getItemFile(((ItemFile)o).getIrFile().getName());
    	}
    	else if( o.getType().equals(ItemLink.TYPE))
    	{
    	    theObject = this.getItemLink(o.getName());
    	}
    	
    	if( theObject == null)
    	{
    		return false;
    	}
    	
    	List<ItemObject> allObjects = getItemObjects();
    	int currentItemObjectPosition = o.getOrder();
    	
    	// if the position is greater than the size of the list put it at the end
		if(position > (allObjects.size() - 1))
		{
			position = allObjects.size() - 1;
		}
		
		if( position < 0 )
		{
			position = 0;
		}
		
		int change = 0;
		
		
		// moving up in list
		if( (currentItemObjectPosition - position) > 0 )
		{
			change = 1;
		}
		else // moving down in list
		{
			change = -1;
		}
		

		for(ItemObject aObject : allObjects)
		{
		    if(!aObject.equals(o))
			{
		    	// move up
		    	if( change == 1)
		    	{
		    		if( aObject.getOrder() < currentItemObjectPosition && aObject.getOrder() >= position)
		    		{
		    			aObject.setOrder(aObject.getOrder() + change);
		    		}
		    	}
		    	else if( change == -1)
		    	{
		    		if( aObject.getOrder() <= position && aObject.getOrder() > currentItemObjectPosition)
		    		{
		    			aObject.setOrder(aObject.getOrder() + change);
		    		}
		    	}
			}
		    else
		    {
		    	aObject.setOrder(position);
		    }
		   
		}
		
    	return true;
    }
	
	/**
	 * Add a license to this item.
	 * 
	 * @param license
	 */
	public void addLicense(License l)
	{
		licenses.add(l);
	}
	
	/**
	 * Remove a license from the list of licenses.
	 * 
	 * @param l
	 * @return true ifthe license is removed
	 */
	public boolean removeLicense(License l)
	{
		return licenses.remove(l);
	}
	
	/**
	 * Find a license based on id
	 * 
	 * @param id
	 * @return the found license or null if not found
	 */
	public License getLicense(Long id)
	{
	   License license = null;
	   for( License l : licenses)
	   {
		   if( l.getId().equals(id))
		   {
			   license = l;
	       }
	   }
	   return license;
	}
	

	/**
	 * Language used by this item.
	 * 
	 * @return the language type used.
	 */
	public LanguageType getLanguageType() {
		return languageType;
	}

	/**
	 * Set the language used by this item.
	 * 
	 * @param languageType
	 */
	public void setLanguageType(LanguageType languageType) {
		this.languageType = languageType;
	}

	/**
	 * Set of licenses applicable to this item.
	 * 
	 * @return
	 */
	public Set<License> getLicenses() {
		return Collections.unmodifiableSet(licenses);
	}

	/**
	 * Set of licenses applicable to this item.
	 * 
	 * @param licenses
	 */
	void setLicenses(Set<License> licenses) {
		this.licenses = licenses;
	}

	/**
	 * Get the item identifiers
	 * 
	 * @return an unmodifiable set of this item identifiers.
	 */
	public Set<ItemIdentifier> getItemIdentifiers() {
		return Collections.unmodifiableSet(itemIdentifiers);
	}

	/**
	 * Set the item identifiers 
	 * 
	 * @param itemIdentifier
	 */
	public void setItemIdentifiers(Set<ItemIdentifier> itemIdentifier) {
		this.itemIdentifiers = itemIdentifier;
	}

	/**
	 * Get the item extents
	 * 
	 * @return an unmodifiable set of this item extents.
	 */
	public Set<ItemExtent> getItemExtents() {
		return Collections.unmodifiableSet(itemExtents);
	}

	/**
	 * Set the item extents 
	 * 
	 * @param itemExtents
	 */
	public void setItemExtents(Set<ItemExtent> itemExtents) {
		this.itemExtents = itemExtents;
	}

	/**
	 * Remove the item extent.
	 * 
	 * @param itemExtent
	 * @return
	 */
	public boolean removeItemExtent(ItemExtent itemExtent)
	{
		return itemExtents.remove(itemExtent);
	}
	
	/**
	 * Remove all item extent.
	 * 
	 */
	public void removeAllItemExtents()
	{
		itemExtents.clear();
		
	}

	/**
	 * Remove all item sponsor.
	 * 
	 */
	public void removeAllItemSponsors()
	{
		itemSponsors.clear();
		
	}
	
	/**
	 * Remove the item identifier.
	 * 
	 * @param itemIdentifier
	 * @return
	 */
	public boolean removeItemIdentifier(ItemIdentifier itemIdentifier)
	{
		return itemIdentifiers.remove(itemIdentifier);
	}
	
	/**
	 * Remove all item identifier.
	 * 
	 */
	public void removeAllItemIdentifiers()
	{
		itemIdentifiers.clear();
		
	}

	/**
	 * Create an item identifier with the specified value and type and add it to this item.
	 * 
	 * @param value - value of the identifier 
	 * @param identifierType - type of identifier
	 * @return - the created item identifier type.
	 */
	public ItemIdentifier addItemIdentifier(String value, IdentifierType identifierType)
	{
		ItemIdentifier itemIdentifier = new ItemIdentifier(identifierType, this);
		itemIdentifier.setValue(value);
		itemIdentifiers.add(itemIdentifier);
		return itemIdentifier;
		
	}

	/**
	 * Add an item extent to this item.  Removed the extent from
	 * any other item it is already added to the extent.
	 * 
	 * @param itemExtent
	 */
	public ItemExtent addItemExtent(ExtentType extentType, String value)
	{
		
		ItemExtent itemExtent = new ItemExtent(extentType, this, value);
		itemExtents.add(itemExtent);
		return itemExtent;
	}
	
	/**
	 * The abstract for the item
	 * 
	 * @return the item abstract.
	 */
	public String getItemAbstract() {
		return itemAbstract;
	}

	/**
	 * Set the item abstract.
	 * 
	 * @param itemAbstract
	 */
	public void setItemAbstract(String itemAbstract) {
		this.itemAbstract = itemAbstract;
	}
	
	/**
	 * Remove the item link.
	 * 
	 * @param itemLink
	 * @return true if the link was removed
	 */
	public boolean removeLink(ItemLink link)
	{
		
		List<ItemObject> allObjects = getItemObjects();
		
		//Re-order the item objects
		for (ItemObject i: allObjects) {
			
			if (i.getOrder() > link.getOrder()) {
				i.setOrder(i.getOrder() - 1 );
			}
		}		
		return links.remove(link);

	}
	
	/**
	 * Remove the sub title.
	 * 
	 * @param title
	 * @return true if the title was removed
	 */
	public boolean removeSubTitle(ItemTitle title)
	{
		return subTitles.remove(title);

	}
	
	/**
	 * Add an item link to this item.  Removed the link from
	 * any other item it has been added to.
	 * 
	 * @param link
	 */
	public void addLink(ItemLink link)
	{
		if( link.getItem() != null && !link.getItem().equals(this))
		{
		    if (!link.getItem().removeLink(link))
		    {
		    	throw new IllegalStateException("Item link could not be remvoed from other item");
		    }
		}
		// set the order of the file
	    List<ItemObject> allObjects = getItemObjects();
	    ItemObject max = null;
	    if( allObjects.size() > 0 )
	    {
	        max = Collections.max(allObjects, new AscendingOrderComparator());
	    }
	
	    int order = 0;
	    if( max != null )
	    {
		    order = max.getOrder() + 1;
	    }
	    link.setOrder(order);
		link.setItem(this);
		links.add(link);
	}
	
	/**
	 * Add an title to this item.  
	 * 
	 * @param title
	 */
	public void addSubTitle(String title, String leadingArticles)
	{
		ItemTitle subTitle = new ItemTitle(this , title, leadingArticles);
		subTitles.add(subTitle);
	}

	/**
	 * Create Item link and add to item
	 * 
	 * @param itemLink
	 */
	public ItemLink createLink(String name, String url)
	{
		ItemLink itemLink = new ItemLink(this, name, url);
		this.addLink(itemLink);
		return itemLink;
	}

	public boolean isPublishedToSystem() {
		return publishedToSystem;
	}

	public void setPublishedToSystem(boolean publishedToSystem) {
		this.publishedToSystem = publishedToSystem;
	}


	/**
	 * Owner of the item.
	 * 
	 * @return
	 */
	public IrUser getOwner() {
		return owner;
	}

	/**
	 * Owner of the item.
	 * 
	 * @param owner
	 */
	public void setOwner(IrUser owner) {
		this.owner = owner;
	}

	public Set<ItemReport> getItemReports() {
		return Collections.unmodifiableSet(itemReports);
	}

	public void setItemReports(Set<ItemReport> itemReports) {
		this.itemReports = itemReports;
	}

	public String getItemKeywords() {
		return itemKeywords;
	}

	public void setItemKeywords(String itemKeywords) {
		this.itemKeywords = itemKeywords;
	}

	/**
	 * Remove the sponsor from thisitem.
	 * 
	 * @param name - name of the role to remove
	 * @return true if the role is removed
	 */
	public boolean removeItemSponsor(ItemSponsor itemSponsor)
	{
		return itemSponsors.remove(itemSponsor);
	}
	
	/**
	 * Add a sponsor to this item.
	 * 
	 * @param sponsor
	 * @param description of the contribution made by the sponsor
	 */
	public ItemSponsor addItemSponsor(Sponsor sponsor, String description)
	{
		ItemSponsor itemSponsor = new ItemSponsor(sponsor, this);
		
		if( description != null && !description.equals(""))
		{
			itemSponsor.setDescription(description);
		}
		itemSponsor.setItem(this);
		itemSponsors.add(itemSponsor);
		return itemSponsor;
	}
	
	/**
	 * Remove all non primary content types from the list
	 */
	public void removeAllNonPrimaryContentTypes()
	{
		HashSet<ItemContentType> contentTypes = new HashSet<ItemContentType>();
		contentTypes.addAll(itemContentTypes);	
		
		for(ItemContentType ict : contentTypes)
		{
			if( !ict.getPrimary() )
			{
			    removeItemContentType(ict);	
			}
		}
	}
	
	
	/**
	 * Add the sponsor to the item and return the created item sponsor.
	 * 
	 * @param sponsor
	 * @return
	 */
	public ItemSponsor addItemSponsor(Sponsor sponsor)
	{
		return addItemSponsor(sponsor, null);
	}
	

	/**
	 * Get the external published item for this generic item.
	 * 
	 * @return the external published item. 
	 */
	public ExternalPublishedItem getExternalPublishedItem() {
		return externalPublishedItem;
	}

	/**
	 * Set the external published item.  Sets the generic item as the owner.
	 * 
	 * @param externalPublishedItem
	 */
	public void setExternalPublishedItem(ExternalPublishedItem externalPublishedItem) {
		this.externalPublishedItem = externalPublishedItem;
	}
	
	/**
	 * If this item does not have an external published item a new one is
	 * created otherwise the exiting one is returned.  
	 * 
	 * @return A new external published item or the one that already exists.
	 */
	public ExternalPublishedItem createExternalPublishedItem()
	{
		if( externalPublishedItem == null )
		{
		    externalPublishedItem = new ExternalPublishedItem();
		}
		return externalPublishedItem;
	}
	
	/**
	 * Sets the external published item to null.
	 */
	public void deleteExternalPublishedItem()
	{
		if( externalPublishedItem != null )
		{
			externalPublishedItem = null;
		}
		    
	}

	/**
	 * Clone for Item
	 * 
	 */
	public GenericItem clone() {

		GenericItem newItem = new GenericItem(this.getName());
		newItem.setLeadingNameArticles(this.getLeadingNameArticles());
		newItem.setDescription(this.getDescription());
		newItem.setItemAbstract(this.getItemAbstract());
		newItem.setItemKeywords(this.getItemKeywords());
		newItem.setLanguageType(this.getLanguageType());
		newItem.setOwner(this.getOwner());
		newItem.setPrimaryImageFile(this.getPrimaryImageFile());
		newItem.setCopyrightStatement(this.getCopyrightStatement());
		
        if( firstAvailableDate != null )
        {
		    FirstAvailableDate firstAvailDate = new FirstAvailableDate();
		    firstAvailDate.setDay(firstAvailableDate.getDay());
		    firstAvailDate.setFractionOfSecond(firstAvailableDate.getFractionOfSecond());
		    firstAvailDate.setHours(firstAvailableDate.getFractionOfSecond());
		    firstAvailDate.setItem(newItem);
		    firstAvailDate.setMonth(firstAvailableDate.getMonth());
		    firstAvailDate.setSeconds(firstAvailableDate.getSeconds());
		    firstAvailDate.setYear(firstAvailableDate.getYear());
		    newItem.setFirstAvailableDate(firstAvailDate);
        }

        if( originalItemCreationDate != null )
        {
		    OriginalItemCreationDate origCreationDate = new OriginalItemCreationDate();
		    origCreationDate.setDay(originalItemCreationDate.getDay());
		    origCreationDate.setFractionOfSecond(originalItemCreationDate.getFractionOfSecond());
		    origCreationDate.setHours(originalItemCreationDate.getFractionOfSecond());
		    origCreationDate.setItem(newItem);
		    origCreationDate.setMonth(originalItemCreationDate.getMonth());
		    origCreationDate.setSeconds(originalItemCreationDate.getSeconds());
		    origCreationDate.setYear(originalItemCreationDate.getYear());
		    newItem.setOriginalItemCreationDate(origCreationDate);
        }
        
		newItem.setReleaseDate(this.getReleaseDate());
		

		// Copy files
		for(ItemFile oldFile:this.getItemFiles()) {
			ItemFile itemFile = newItem.addFile(oldFile.getIrFile());
			itemFile.setDescription(oldFile.getDescription());
			itemFile.setOrder(oldFile.getOrder());
		}
		
		// Copy information
		for(ItemIdentifier oldIdentifier:this.getItemIdentifiers()) {
			newItem.addItemIdentifier(oldIdentifier.getValue(), oldIdentifier.getIdentifierType());
		}

		// Copy information
		for(ItemExtent oldExtent:this.getItemExtents()) {
			newItem.addItemExtent(oldExtent.getExtentType(), oldExtent.getValue());
		}

		// Copy information
		for(ItemSponsor oldSponsor:this.getItemSponsors()) {
			newItem.addItemSponsor(oldSponsor.getSponsor(), oldSponsor.getDescription());
		}

		// Copy information
		for(ItemContentType oldType:this.getItemContentTypes() ) {
			
			if( !oldType.getPrimary() )
			{
			    newItem.addContentType(oldType.getContentType());
			}
			else
			{
				newItem.setPrimaryContentType(oldType.getContentType());
			}
			
			
		}
		
		// Copy reports
		for(ItemReport oldReport:this.getItemReports()) {
			newItem.addReport(oldReport.getSeries(), oldReport.getReportNumber());
		}
		
		// Copy links
		for(ItemLink oldItemLink:this.getLinks()) {
			ItemLink newLink = new ItemLink(newItem, oldItemLink.getName(), oldItemLink.getUrlValue());
			newItem.addLink(newLink);
		}

		// Copy item's externally published data
		ExternalPublishedItem oldExternalPublishedItem = this.getExternalPublishedItem();
		if (oldExternalPublishedItem != null) {
			ExternalPublishedItem newExternalPublishedItem  = new ExternalPublishedItem ();
			newExternalPublishedItem.setCitation(oldExternalPublishedItem.getCitation());
			newExternalPublishedItem.setPublishedDate(oldExternalPublishedItem.getPublishedDate());
			newExternalPublishedItem.setPublisher(oldExternalPublishedItem.getPublisher());
			newItem.setExternalPublishedItem(newExternalPublishedItem);
		}
		
		// Copy Contributors
		for(ItemContributor oldContributor:this.getContributors()) {
			ItemContributor newContributor = new ItemContributor();
			newContributor.setContributor(oldContributor.getContributor());
			newContributor.setOrder(oldContributor.getOrder());
			newItem.addContributor(newContributor);
		}		

		// Copy sub titles
		for(ItemTitle oldTitle:this.getSubTitles()) {
			newItem.addSubTitle(oldTitle.getTitle(), oldTitle.getLeadingArticles());
		}		

		// Copy license
		for(License oldLicense:this.getLicenses()) {
			newItem.addLicense(oldLicense);
		}
		
		return newItem;

	}

	/**
	 * Get the date the item was first made available to public
	 * 
	 * @return
	 */
	public FirstAvailableDate getFirstAvailableDate() {
		return firstAvailableDate;
	}

	/**
	 * Set the date the item was first made available to public
	 * 
	 * @param dateFirstAvailable
	 */
	void setFirstAvailableDate(FirstAvailableDate firstAvailableDate) {
		this.firstAvailableDate = firstAvailableDate;
	}
	
	/**
	 * Update the published date with the given values.  If month, day and year are 
	 * all 0 published date is set to null.  If published date does not yet exist,
	 * a new one is created.
	 * 
	 * @param month
	 * @param day
	 * @param year
	 */
	public FirstAvailableDate updateFirstAvailableDate(int month, int day, int year)
	{
		if(month > 0 || day > 0 || year > 0)
		{
			if( firstAvailableDate != null )
			{
				firstAvailableDate.setMonth(month);
				firstAvailableDate.setYear(year);
				firstAvailableDate.setDay(day);
			}
			else
			{
				this.firstAvailableDate = new FirstAvailableDate(month, day, year);
				firstAvailableDate.setItem(this);
			}
		}
		else
		{
			if( firstAvailableDate != null )
			{
				firstAvailableDate = null;
			}
		}
		
		return firstAvailableDate;
	}
	
	/**
	 * Get the date the item can be made available to public
	 * 
	 * @return
	 */
	public OriginalItemCreationDate getOriginalItemCreationDate() {
		
		return originalItemCreationDate;
	}

	/**
	 * Set  the date the item can be made available to public
	 * 
	 * @param releaseDate
	 */
	void setOriginalItemCreationDate(OriginalItemCreationDate originalItemCreationDate) {
		this.originalItemCreationDate = originalItemCreationDate;
	}
	
	/**
	 * Update the original item creating date with the given values.  If month, day and year are 
	 * all 0 creation date is set to null.  If creation date does not yet exist,
	 * a new one is created.
	 * 
	 * @param month
	 * @param day
	 * @param year
	 */
	public OriginalItemCreationDate updateOriginalItemCreationDate(int month, int day, int year)
	{
		if(month > 0 || day > 0 || year > 0)
		{
			if( originalItemCreationDate != null )
			{
				originalItemCreationDate.setMonth(month);
				originalItemCreationDate.setYear(year);
				originalItemCreationDate.setDay(day);
			}
			else
			{
				this.originalItemCreationDate = new OriginalItemCreationDate(month, day, year);
				originalItemCreationDate.setItem(this);
			}
		}
		else
		{
			if( originalItemCreationDate != null )
			{
				originalItemCreationDate = null;
			}
		}
		
		return originalItemCreationDate;
	}
	
	/**
	 * Get item objects
	 * 
	 * @return
	 */
	public List<ItemObject> getItemObjects() {
		List<ItemObject> itemObjects = new LinkedList<ItemObject>();
		
		itemObjects.addAll(itemFiles);
		itemObjects.addAll(links);
		
		return itemObjects;
		
	}
	
	/**
	 * Get item object by id and type
	 * 
	 * @param id Id of item object
	 * @param type Type of item object
	 * 
	 * @return ItemObject found
	 */
	public ItemObject getItemObject(Long id, String type) {
		
		ItemObject itemObject = null;
		boolean found = false;
		
		if (type.equalsIgnoreCase(ItemFile.TYPE)) {
			
			Iterator<ItemFile> iter = itemFiles.iterator();
			
			while( iter.hasNext() && !found)
			{
				ItemFile f = iter.next();
				if( f.getId().equals(id))
				{
					itemObject = f;
					found = true;
			    }
			}
		} else {
			
			Iterator<ItemLink> iter = links.iterator();
			
			while( iter.hasNext() && !found)
			{
				ItemLink l = iter.next();
				if( l.getId().equals(id))
				{
					itemObject = l;
					found = true;
			    }
			}

		} 
		
		return itemObject;
		
	}

	public Set<ItemTitle> getSubTitles() {
		return Collections.unmodifiableSet(subTitles);
	}

	public void setSubTitles(Set<ItemTitle> subTitles) {
		this.subTitles = subTitles;
	}

	/**
	 * Get a item report for given report number
	 * 
	 * @param reportNumber
	 */
	public ItemReport getItemReportByReportNumber(String reportNumber) {
		ItemReport itemReport = null;
	    for( ItemReport i : itemReports)
	    {
		   if( i.getReportNumber().equals(reportNumber))
		   {
			   itemReport = i;
			   break;
	       }
	    }
	    return itemReport;		
	}

	public boolean isLocked() {
		return locked;
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	/**
	 * Indicates that this item is publicly viewable.
	 * 
	 * @return
	 */
	public boolean isPubliclyViewable() {
		return publiclyViewable;
	}
	
	/**
	 * Returns true if the generic item is publicly viewable.
	 * 
	 * @return
	 */
	public boolean getPubliclyViewable()
	{
		return isPubliclyViewable();
	}

	/**
	 * Set this item to publicly viewable.
	 * 
	 * @param publiclyViewable
	 */
	public void setPubliclyViewable(boolean publiclyViewable) {
		this.publiclyViewable = publiclyViewable;
	}

	/**
	 * The set of secondary content types - other ways to describe
	 * the material.
	 * 
	 * @return content types
	 */
	public Set<ItemContentType> getItemContentTypes() {
		return Collections.unmodifiableSet(itemContentTypes);
	}
	
	/**
	 * Return all non primary content types.
	 * 
	 * @return set of non primary content types
	 */
	public Set<ItemContentType> getAllNonPrimaryItemContentTypes()
	{
		Set<ItemContentType> contentTypes = new HashSet<ItemContentType>();
		contentTypes.addAll(itemContentTypes);
		contentTypes.remove(this.getPrimaryItemContentType());
		return Collections.unmodifiableSet(contentTypes);
	}

	/**
	 * Set the secondary content types for this item.
	 * 
	 * @param secondaryContentTypes
	 */
	void setItemContentTypes(Set<ItemContentType> itemContentTypes) {
		this.itemContentTypes = itemContentTypes;
	}

	/**
	 * Get the primary content type for this item.
	 * 
	 * @return primary content type.
	 */
	public ItemContentType getPrimaryItemContentType() {
		
		for(ItemContentType ict : itemContentTypes )
		{
			if( ict.getPrimary() )
			{
				return ict;
			}
		}
		return null;
	}


	/**
	 * Add a content type to this item.
	 * 
	 * @param c
	 * @param primary 
	 */
	public ItemContentType addContentType(ContentType c) {
		ItemContentType ict = new ItemContentType(this, c);
		if( !itemContentTypes.contains(ict) )
		{
			itemContentTypes.add(ict);
		}
		else
		{
			ict = this.getItemContentType(c);
		}
		
		return ict;
	}
	
	/**
	 * Remove the specified item content type.
	 * 
	 * @param itemContentType - content type to remove
	 * @return true if the item content type is removed.
	 */
	public boolean removeItemContentType(ItemContentType itemContentType)
	{
		return itemContentTypes.remove(itemContentType); 
	}
	
	/**
	 * Set the content type as primary.  If there is another
	 * primary content type, it is removed from the list.
	 * 
	 * @param content type
	 * @return the newley created item content type.  If content type is
	 * null the primary content type is removed if one exists and null
	 * is returned
	 */
	public ItemContentType setPrimaryContentType(ContentType contentType)
	{
		if( contentType == null )
		{
			removeItemContentType(getPrimaryItemContentType());
			return null;
		}		
		else
		{
		    ItemContentType ict = this.getItemContentType(contentType);

		    if(ict == null)
		    {
			    ict = new ItemContentType( this, contentType);
			    ict.setPrimary(true);
			    removeItemContentType(getPrimaryItemContentType());
		        itemContentTypes.add(ict);
		    }
		    else if(!ict.getPrimary())
		    {
			    removeItemContentType(getPrimaryItemContentType());
			    ict.setPrimary(true);
		    }
		    return ict;
		}
	}

	/**
	 * Get the item content type
	 * 
	 * @param contentType
	 * @return
	 */
	public ItemContentType getItemContentType(ContentType contentType)
	{
		for(ItemContentType ict : itemContentTypes)
		{
			if( ict.getContentType().equals(contentType))
			{
				return ict;
			}
		}
		return null;
	}
	/**
	 * Get the set of item sponsors.
	 * 
	 * @return
	 */
	public Set<ItemSponsor> getItemSponsors() {
		return itemSponsors;
	}

	/**
	 * Set the item sponsors.
	 * 
	 * @param itemSponsors
	 */
	public void setItemSponsors(Set<ItemSponsor> itemSponsors) {
		this.itemSponsors = itemSponsors;
	}

	/**
	 * Get the release date.
	 * 
	 * @return
	 */
	public Date getReleaseDate() {
		return releaseDate;
	}

	/**
	 * Set the release date.
	 * 
	 * @param releaseDate
	 */
	public void setReleaseDate(Date releaseDate) {
		this.releaseDate = releaseDate;
	}
	
	/**
	 * Determine if the current date is before the release date if it is returns true.
	 * 
	 * @return true if the item is embargoed.
	 */
	public boolean isEmbargoed()
	{
		boolean embargoed = false;
		if( releaseDate != null)
		{
		    Date d = new Date();
		    embargoed = d.before(releaseDate);
		}
		return embargoed;
	}
	
	/**
	 * Determine if the current date is before the release date if it is returns true.
	 * 
	 * @return true if the item is embargoed.
	 */
	public boolean getEmbargoed()
	{
		return isEmbargoed();
	}


	/**
	 * The leading name articles for example "A, an, the, ye"
	 * @return
	 */
	public String getLeadingNameArticles() {
		return leadingNameArticles;
	}

	/**
	 * The leading name articles 
	 * 
	 * @param leadingNameArticles
	 */
	public void setLeadingNameArticles(String leadingNameArticles) {
		if( leadingNameArticles != null )
		{
		    this.leadingNameArticles = leadingNameArticles.trim();
		}
		else
		{
			this.leadingNameArticles = null;
		}
	}

	public char getNameFirstChar() {
		return nameFirstChar;
	}

	public String getLowerCaseName() {
		return lowerCaseName;
	}
	
	/**
	 * Get the full name of the generic item
	 * @return
	 */
	public String getFullName()
	{
		if( leadingNameArticles != null && !leadingNameArticles.trim().equals(""))
		{
			return  leadingNameArticles + " " + name;
		}
		else
		{
			return name;
		}
		
	}

	public CopyrightStatement getCopyrightStatement() {
		return copyrightStatement;
	}

	public void setCopyrightStatement(CopyrightStatement copyrightStatement) {
		this.copyrightStatement = copyrightStatement;
	}

}
