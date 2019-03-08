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


package edu.ur.ir.web.action.researcher;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.StringTokenizer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.ParseException;

import edu.ur.ir.FacetSearchHelper;
import edu.ur.ir.repository.Repository;
import edu.ur.ir.repository.RepositoryService;
import edu.ur.ir.researcher.Researcher;
import edu.ur.ir.researcher.ResearcherSearchService;
import edu.ur.ir.researcher.ResearcherService;
import edu.ur.ir.search.FacetFilter;
import edu.ur.ir.web.table.Pager;

/**
 * Search class for researcher.
 * 
 * @author Sharmila Ranganathan
 *
 */
public class ResearcherSearch extends Pager {
	
	/** Generated serial id */
	private static final long serialVersionUID = 7696775155766510734L;

	/**  Logger */
	private static final Logger log = LogManager.getLogger(ResearcherSearch.class);
	
	/** helper that contains data for the searches  */
	private FacetSearchHelper searchDataHelper;
	
	/** Search service for researchers */
	private ResearcherSearchService researcherSearchService;
	
	/** Query executed by the user */
	private String query;
	
	/** comma separated set of facets passed in by user */
	private String facets;
	
	/** facet values */
	private String facetValues;
	
	/** Display names for the facets */
	private String facetDisplayNames;
	
	/** Facet index to remove if user wants to remove a facet */
	private int facetIndexToRemove = -1;
	
	/** most recent facet selected */
	private String mostRecentFacetName;
	
	/** most recent facet value  */
	private String mostRecentFacetValue;
	
	/** Display name for the facet */
	private String mostRecentFacetDisplayName;
	
	/** Service for dealing with repositories */
	private RepositoryService repositoryService;
	
	private int numberOfHitsToProcessForFacets = 100;
	
	private int numberOfResultsToCollectForFacets = 100;
	
	/** total number of facets to show */
	private int numberOfFacetsToShow = 10;
	
	/** Researchers retrieved from the list of ids */
	private HashSet<Researcher> searchResearchers = new HashSet<Researcher>();  
	
	/** indicates this the first time the user viewing the search */
	boolean searchInit = true;
	
	/** Service for dealing with Researchers. */
	private ResearcherService researcherService;
	
	/** Indicates this is a search view */
	private String viewType = "search";
	
	/** End position */
	private int rowEnd;
	


	/**
	 * Start searching for Researchers.
	 * 
	 * @return
	 */
	public String startSearch()
	{
		return SUCCESS;
	}
	
	public ResearcherSearch()
	{
		numberOfResultsToShow = 25;
		numberOfPagesToShow = 20;
	}
	
	/**
	 * Execute the search for the user this is for the initial search.
	 * 
	 * @return
	 * @throws CorruptIndexException
	 * @throws IOException
	 * @throws ParseException
	 */
	public String searchResearchers() throws CorruptIndexException, IOException, ParseException
	{
		Repository repository = repositoryService.getRepository(Repository.DEFAULT_REPOSITORY_ID, false);
		searchInit = false;
		rowEnd = rowStart + numberOfResultsToShow;
		
		
		log.debug("Executing query : " + query);

		searchDataHelper = researcherSearchService.executeSearchWithFacets(query,
				repository.getResearcherIndexFolder(), 
				numberOfHitsToProcessForFacets, 
				numberOfResultsToCollectForFacets, 
				numberOfFacetsToShow,
				numberOfResultsToShow,
			    rowStart);
		
		if(rowEnd > searchDataHelper.getHitSize())
		{
			rowEnd = searchDataHelper.getHitSize();
		}
		
		log.debug("searchDataHelper hit size = " + searchDataHelper.getHitSize());
		
		LinkedList<Long> ids = new LinkedList<Long>();
		ids.addAll(searchDataHelper.getHitIds());
		
		searchResearchers.addAll(researcherService.getResearchers(ids));
		
		return SUCCESS;
		
	}
	
	/**
	 * Excecute the search for the user this is for the initial search.
	 * 
	 * @return
	 * @throws CorruptIndexException
	 * @throws IOException
	 * @throws ParseException
	 */
	public String filteredSearchResearchers() throws CorruptIndexException, IOException, 
	ParseException
	{
		rowEnd = rowStart + numberOfResultsToShow;
		Repository repository = repositoryService.getRepository(Repository.DEFAULT_REPOSITORY_ID, false);
		searchInit = false;
		
		log.debug("Executing query  with facets : " + query);
		log.debug("Facet Names =  " + facets);
		log.debug("Facet Values =  " + facetValues);

		log.debug("mostRecentFacetName =  " + mostRecentFacetName );
		log.debug("mostRecentFacetValue =  " + mostRecentFacetValue );
		
		// parse the facet internal names
		LinkedList<String> facetNames = new LinkedList<String>();
		if( facets != null )
		{
		    StringTokenizer tokenizer = new StringTokenizer(facets, "|");
		
		
		    while(tokenizer.hasMoreElements())
		    {
			    String nextFacet = tokenizer.nextToken();
			    facetNames.add(nextFacet.trim());
		    }
		}
		
		LinkedList<String> facetDisplayNames = new LinkedList<String>();
		
		if( this.facetDisplayNames != null )
		{
		    // parse the facet display names
			StringTokenizer tokenizer = new StringTokenizer(this.facetDisplayNames, "|");
		
		
		    while(tokenizer.hasMoreElements())
		    {
			    String nextFacetDisplayName = tokenizer.nextToken();
			    facetDisplayNames.add(nextFacetDisplayName.trim());
		    }
		}
		
		
		LinkedList<String> facetValues = new LinkedList<String>();
		
		if( this.facetValues != null )
		{
		    // parse the facet values
			StringTokenizer tokenizer = new StringTokenizer(this.facetValues, "|");
		   
		
		    while(tokenizer.hasMoreElements())
		    {
			    String nextFacet = tokenizer.nextToken();
			    facetValues.add(nextFacet.trim());
		    }
		}
		
		if( facetValues.size() != facetNames.size())
		{
			throw new IllegalStateException( "facet values and facet names must be the same size" +
					" facet values size = " + facetValues.size() + 
					" facet names size = " + facetNames.size() );
		}
		
		LinkedList<FacetFilter> filters = new LinkedList<FacetFilter>();
		
		log.debug("facetValues size = " + facetValues.size());
		log.debug("facet names size = " + facetNames.size());
		
		for( int index = 0 ; index < facetValues.size(); index++ )
		{
			// don't add the facet if the user has choosen to remove it
			if( this.facetIndexToRemove != index )
			{
		        filters.add(new FacetFilter(facetNames.get(index), facetValues.get(index), facetDisplayNames.get(index)));	
			}
		}
		
		if(facetIndexToRemove >= 0 )
		{
			facets = "";
			this.facetValues = "";
			this.facetDisplayNames = "";
		
		    // rebuild the facets if the user has selected to remove one
		    for(int index = 0; index < facetValues.size(); index ++)
		    {
			    if( index != facetIndexToRemove)
			    {
			    	if( facets != null && !facets.trim().equals(""))
				    {
			    	    facets = facets + "|" + facetNames.get(index);
			    	    this.facetValues = this.facetValues + "|" + facetValues.get(index);
			    	    this.facetDisplayNames = this.facetDisplayNames + "|" + facetDisplayNames.get(index);
				    }
			    	else
			    	{
			    		facets = facetNames.get(index);
				    	this.facetValues = facetValues.get(index);
				    	this.facetDisplayNames = facetDisplayNames.get(index);
			    	}
			    }
		    }
	    }
		
		
		if( mostRecentFacetName != null && !mostRecentFacetName.trim().equals(""))
		{
		    filters.add(new FacetFilter(mostRecentFacetName, mostRecentFacetValue, mostRecentFacetDisplayName));
		    if( facets != null && !facets.trim().equals(""))
		    {
		        facets = facets + "|" + mostRecentFacetName;
		        this.facetValues = this.facetValues + "|" + mostRecentFacetValue;
		        this.facetDisplayNames = this.facetDisplayNames + "|" + mostRecentFacetDisplayName;
		    }
		    else
		    {
		    	facets = mostRecentFacetName;
		    	this.facetValues = mostRecentFacetValue;
		    	this.facetDisplayNames = mostRecentFacetDisplayName;
		    }
		}
		
		log.debug("Filter Size = " + filters.size() );

		searchDataHelper = researcherSearchService.executeSearchWithFacets(query,
				filters,
				repository.getResearcherIndexFolder(), 
				numberOfHitsToProcessForFacets, 
				numberOfResultsToCollectForFacets, 
				numberOfFacetsToShow,
				numberOfResultsToShow,
			    rowStart);
		
		if(rowEnd > searchDataHelper.getHitSize())
		{
			rowEnd = searchDataHelper.getHitSize();
		}
		
		log.debug("searchDataHelper hit size = " + searchDataHelper.getHitSize());
		
		LinkedList<Long> ids = new LinkedList<Long>();
		ids.addAll(searchDataHelper.getHitIds());
		
		searchResearchers.addAll(researcherService.getResearchers(ids));
		
		
		return SUCCESS;
		
	}

	

	
	/**
	 * Returns the total number of hits 
	 * 
	 * @see edu.ur.ir.web.table.Pager#getTotalHits()
	 */
	public int getTotalHits() {
		return searchDataHelper.getHitSize();
	}





	public ResearcherSearchService getResearcherSearchService() {
		return researcherSearchService;
	}


	public void setResearcherSearchService(
			ResearcherSearchService researcherSearchSevice) {
		this.researcherSearchService = researcherSearchSevice;
	}


	public RepositoryService getRepositoryService() {
		return repositoryService;
	}


	public void setRepositoryService(RepositoryService repositoryService) {
		this.repositoryService = repositoryService;
	}


	public String getQuery() {
		return query;
	}


	public void setQuery(String query) {
		this.query = query;
	}


	public FacetSearchHelper getSearchDataHelper() {
		return searchDataHelper;
	}


	public void setSearchDataHelper(FacetSearchHelper searchDataHelper) {
		this.searchDataHelper = searchDataHelper;
	}


	public HashSet<Researcher> getSearchResearchers() {
		return searchResearchers;
	}


	public void setSearchResearchers(HashSet<Researcher> searchResearchers) {
		this.searchResearchers = searchResearchers;
	}


	public ResearcherService getResearcherService() {
		return researcherService;
	}


	public void setResearcherService(
			ResearcherService researcherService) {
		this.researcherService = researcherService;
	}


	public String getViewType() {
		return viewType;
	}


	public void setViewType(String viewType) {
		this.viewType = viewType;
	}

	public boolean isSearchInit() {
		return searchInit;
	}

	public void setSearchInit(boolean searchInit) {
		this.searchInit = searchInit;
	}

	public String getFacets() {
		return facets;
	}

	public void setFacets(String facets) {
		this.facets = facets;
	}

	public String getCurrentFacet() {
		return mostRecentFacetName;
	}

	public void setCurrentFacet(String currentFacet) {
		this.mostRecentFacetName = currentFacet;
	}

	public String getFacetValues() {
		return facetValues;
	}

	public void setFacetValues(String facetValues) {
		this.facetValues = facetValues;
	}

	public String getMostRecentFacetValue() {
		return mostRecentFacetValue;
	}

	public void setMostRecentFacetValue(String mostRecentFacetValue) {
		this.mostRecentFacetValue = mostRecentFacetValue;
	}

	public String getMostRecentFacetName() {
		return mostRecentFacetName;
	}

	public void setMostRecentFacetName(String mostRecentFacetName) {
		this.mostRecentFacetName = mostRecentFacetName;
	}

	public String getMostRecentFacetDisplayName() {
		return mostRecentFacetDisplayName;
	}

	public void setMostRecentFacetDisplayName(String mostRecentFacetDisplayName) {
		this.mostRecentFacetDisplayName = mostRecentFacetDisplayName;
	}

	public String getFacetDisplayNames() {
		return facetDisplayNames;
	}

	public void setFacetDisplayNames(String facetDisplayNames) {
		this.facetDisplayNames = facetDisplayNames;
	}

	public int getFacetIndexToRemove() {
		return facetIndexToRemove;
	}

	public void setFacetIndexToRemove(int facetIndexToRemove) {
		this.facetIndexToRemove = facetIndexToRemove;
	}

	public int getRowEnd() {
		return rowEnd;
	}

	public void setRowEnd(int rowEnd) {
		this.rowEnd = rowEnd;
	}
	

}
