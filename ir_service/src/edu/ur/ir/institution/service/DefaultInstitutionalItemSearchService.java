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


package edu.ur.ir.institution.service;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.MapFieldSelector;
import org.apache.lucene.document.NumberTools;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.misc.ChainedFilter;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryWrapperFilter;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.util.OpenBitSet;
import org.apache.lucene.util.OpenBitSetDISI;

import edu.ur.ir.FacetSearchHelper;
import edu.ur.ir.SearchHelper;
import edu.ur.ir.institution.InstitutionalCollection;
import edu.ur.ir.institution.InstitutionalItemSearchService;
import edu.ur.ir.search.FacetFilter;
import edu.ur.ir.search.FacetResult;
import edu.ur.ir.search.FacetResultHitComparator;

/**
 * Excecute the search and determine facets
 * 
 * @author Nathan Sarr
 *
 */
public class DefaultInstitutionalItemSearchService implements InstitutionalItemSearchService{

	/** Analyzer to use for parsing the queries */
	private Analyzer analyzer;
	
	/**  Logger for editing a file database. */
	private static final Logger log = Logger.getLogger(DefaultInstitutionalItemSearchService.class);
	
	/** max number of hits to retrieve */
	private int maxNumberOfMainQueryHits = 10000;
	
	
	/** fields to search in the index*/
	private static final String[] fields = 
	{DefaultInstitutionalItemIndexService.ABSTRACT, 
	 DefaultInstitutionalItemIndexService.NAME,
	 DefaultInstitutionalItemIndexService.DESCRIPTION,
	 DefaultInstitutionalItemIndexService.LINK_NAMES,
	 DefaultInstitutionalItemIndexService.FILE_TEXT,
	 DefaultInstitutionalItemIndexService.CONTRIBUTOR_NAMES,
	 DefaultInstitutionalItemIndexService.LANGUAGE,
	 DefaultInstitutionalItemIndexService.IDENTIFIERS,
	 DefaultInstitutionalItemIndexService.KEY_WORDS,
	 DefaultInstitutionalItemIndexService.SUB_TITLES,
	 DefaultInstitutionalItemIndexService.PUBLISHER,
	 DefaultInstitutionalItemIndexService.CITATION,
	 DefaultInstitutionalItemIndexService.CONTENT_TYPES,
	 DefaultInstitutionalItemIndexService.COLLECTION_LEFT_VALUE,
	 DefaultInstitutionalItemIndexService.COLLECTION_RIGHT_VALUE,
	 DefaultInstitutionalItemIndexService.COLLECTION_NAME};
	
	
	/**
	 * Get the facets and results
	 * @see edu.ur.ir.institution.InstitutionalItemSearchService#executeSearchWithFacets(java.lang.String, java.lang.String, int, int, int, int)
	 */
	public FacetSearchHelper executeSearchWithFacets(
			String mainQueryString,
			String indexFolder, 
			int numberOfHitsToProcessForFacets, 
			int numberOfResultsToCollectForFacets,
			int numberOfFactsToShow,
			int numberOfIdsToCollect,
			int idsToCollectStartPosition ) 
	        throws CorruptIndexException, IOException, ParseException 
	{
		if( searchDirectoryIsEmpty(indexFolder) || isInvalidQuery(mainQueryString))
		{
			return new FacetSearchHelper(new HashSet<Long>(), 0, new HashMap<String, Collection<FacetResult>>(), mainQueryString);
		}
		
		IndexSearcher searcher = new IndexSearcher(indexFolder);
		IndexReader reader = searcher.getIndexReader();
		QueryParser parser = new MultiFieldQueryParser(fields, analyzer);
		parser.setDefaultOperator(QueryParser.AND_OPERATOR);
		
		
		HashMap<String, Collection<FacetResult>> facetResults = new HashMap<String, Collection<FacetResult>>();
		
		// execute the main query - we will use this to extract data to determine the facet searches
		String executedQuery = SearchHelper.prepareMainSearchString(mainQueryString, true);
		Query mainQuery = parser.parse(executedQuery);
		if( log.isDebugEnabled() )
		{
			log.debug("main query = " + executedQuery );
		}
		
		
		TopDocs topDocs = searcher.search(mainQuery,  maxNumberOfMainQueryHits);
		// determine the set of data we should use to determine facets
		HashMap<String, HashMap<String, FacetResult>> possibleFacets = this.generateFacetSearches(
				topDocs, 
				numberOfHitsToProcessForFacets, 
				numberOfResultsToCollectForFacets,
				searcher);

		QueryWrapperFilter mainQueryWrapper = new QueryWrapperFilter(mainQuery);
		log.debug("executeSearchWithFacets 1 query = " + mainQuery);
		DocIdSet mainQueryBits = mainQueryWrapper.getDocIdSet(reader);
		OpenBitSetDISI mainQueryBitSet = new OpenBitSetDISI(mainQueryBits.iterator(), maxNumberOfMainQueryHits);
		// process the data and determine the facets
        FacetSearchHelper helper = processPossibleFacets(possibleFacets, 
        		reader, 
        		mainQueryBitSet, 
        		facetResults, 
        		topDocs, 
        		numberOfIdsToCollect, 
        		idsToCollectStartPosition,
        		numberOfFactsToShow,
        		mainQueryString,
        		searcher);
		helper.setExecutedQuery(executedQuery);
        searcher.close();
        return helper;
	}
	
	/**
	 * Determine the number of hits based on the overall number of results the main search returned.
	 * 
	 * @param baseBitSet
	 * @param filterBitSet
	 * @return
	 */
	private long getFacetHitCount(OpenBitSet baseBitSet, OpenBitSet filterBitSet)
	{
		filterBitSet.and(baseBitSet);
		return filterBitSet.cardinality();
	}
	
	/**
	 * This determines the possible facets for each of the categories.  For example - possible authors 
	 * for the display.  This does not care about counts later on counts will be important.
	 * 
	 * @param topDocs - top doucment hits found
	 * @param numberOfHitsToProcess
	 * @return
	 * @throws CorruptIndexException
	 * @throws IOException
	 */
	private HashMap<String, HashMap<String, FacetResult>> generateFacetSearches(TopDocs topDocs, 
			int numberOfHitsToProcess, 
			int numberOfResultsToCollect,
			IndexSearcher searcher) throws CorruptIndexException, IOException
	{
		String[] fieldsToLoad = {
				DefaultInstitutionalItemIndexService.CONTRIBUTOR_NAMES,
				DefaultInstitutionalItemIndexService.LANGUAGE,
				DefaultInstitutionalItemIndexService.KEY_WORDS,
				DefaultInstitutionalItemIndexService.CONTENT_TYPES,
				DefaultInstitutionalItemIndexService.COLLECTION_NAME,
				DefaultInstitutionalItemIndexService.CONTRIBUTOR_NAMES
		};
		
		MapFieldSelector fieldSelector= new MapFieldSelector(fieldsToLoad);
		HashMap<String, HashMap<String, FacetResult>> facets = new HashMap<String, HashMap<String, FacetResult>>();
		HashMap<String, FacetResult> authorsMap = new HashMap<String, FacetResult>();
		HashMap<String, FacetResult> languagesMap = new HashMap<String, FacetResult>();
		HashMap<String, FacetResult> subjectsMap = new HashMap<String, FacetResult>();
		HashMap<String, FacetResult> formatsMap = new HashMap<String, FacetResult>();
		HashMap<String, FacetResult> collectionMap = new HashMap<String, FacetResult>();
		
		facets.put(AUTHOR_MAP, authorsMap);
		facets.put(LANGUAGE_MAP, languagesMap);
		facets.put(SUBJECT_MAP, subjectsMap);
		facets.put(FORMAT_MAP, formatsMap);
		facets.put(COLLECTION_MAP,  collectionMap);
		
		int length = topDocs.totalHits;
		
		if (length <= numberOfHitsToProcess)
		{
			numberOfHitsToProcess = length;
		}
		
		for( int index = 0; index < numberOfHitsToProcess; index++)
		{
			
			Document doc = searcher.doc(topDocs.scoreDocs[index].doc, fieldSelector);
			String names = doc.get(DefaultInstitutionalItemIndexService.CONTRIBUTOR_NAMES);
			
			String language = doc.get(DefaultInstitutionalItemIndexService.LANGUAGE);
			
			if( language != null )
			{
				language = language.trim();
			}
			
			String subjects = doc.get(DefaultInstitutionalItemIndexService.KEY_WORDS);
			
			String formats = doc.get(DefaultInstitutionalItemIndexService.CONTENT_TYPES);
			
			String collection = doc.get(DefaultInstitutionalItemIndexService.COLLECTION_NAME);
			
			if( collection != null)
			{
				collection = collection.trim();
				FacetResult f = collectionMap.get(collection);
				if( f == null )
				{
					f = new FacetResult(1l, DefaultInstitutionalItemIndexService.COLLECTION_NAME, collection);
		    		collectionMap.put(collection, f);
				}
			}
			
	
			
			if( names != null && authorsMap.size() < numberOfResultsToCollect)
			{
			    StringTokenizer tokenizer = new StringTokenizer(names, DefaultInstitutionalItemIndexService.SEPERATOR);
			    while(tokenizer.hasMoreElements() && authorsMap.size() < numberOfResultsToCollect)
			    {
			    	String nextName = tokenizer.nextToken().trim();
			    	FacetResult f = authorsMap.get(nextName);
			    	if( f== null )
			    	{
			    		f = new FacetResult(1l, DefaultInstitutionalItemIndexService.CONTRIBUTOR_NAMES, nextName);
			    		authorsMap.put(nextName, f);
			    	}
			    }
			}
			
			if( language != null && languagesMap.size() < numberOfResultsToCollect )
			{
				FacetResult f = languagesMap.get(language);
				if( f == null )
				{
					f = new FacetResult(1l, DefaultInstitutionalItemIndexService.LANGUAGE, language);
		    		languagesMap.put(language, f);
				}
			}
			
			if( subjects != null && subjectsMap.size() < numberOfResultsToCollect)
			{
			    StringTokenizer tokenizer = new StringTokenizer(subjects, DefaultInstitutionalItemIndexService.SEPERATOR);
			    while(tokenizer.hasMoreElements() && subjectsMap.size() < numberOfResultsToCollect)
			    {
			    	String subject = tokenizer.nextToken().trim();
			    	FacetResult f = subjectsMap.get(subject);
			    	if( f == null )
			    	{
			    		f = new FacetResult(1l, DefaultInstitutionalItemIndexService.KEY_WORDS, subject);
			    		subjectsMap.put(subject, f);
			    	}
			    }
			}
			
			if( formats != null && formatsMap.size() < numberOfResultsToCollect)
			{
			    StringTokenizer tokenizer = new StringTokenizer(formats, DefaultInstitutionalItemIndexService.SEPERATOR);
			    while(tokenizer.hasMoreElements() && formatsMap.size() < numberOfResultsToCollect)
			    {
			    	String nextFormat = tokenizer.nextToken().trim();
			    	FacetResult f = formatsMap.get(nextFormat);
			    	if( f== null )
			    	{
			    		f = new FacetResult(1l, DefaultInstitutionalItemIndexService.CONTENT_TYPES, nextFormat);
			    		formatsMap.put(nextFormat, f);
			    	}
			    }
				
			}
			
			doc = null;
		}
		return facets;
	}
	
	
	/**
	 * Determines the number of hits for each facet across the main query.
	 * 
	 * @param facets
	 * @param reader
	 * @param mainQueryBits
	 * @throws ParseException
	 * @throws IOException
	 */
	private void processFacetCategory(Collection<FacetResult> facets, 
			IndexReader reader, 
			OpenBitSetDISI mainQueryBitSet,
			IndexSearcher searcher) 
	   throws ParseException, IOException
	{
		for(FacetResult f : facets )
		{
		    long count = 0;
		
			String searchString = SearchHelper.prepareFacetSearchString(f.getFacetName(), false);
			
			if( !searchString.trim().equals(""))
			{
				// add quotes around the search string
				searchString = "\"" + searchString + "\"";
				
			    QueryParser subQueryParser = new QueryParser(f.getField(), analyzer);
					    subQueryParser.setDefaultOperator(QueryParser.AND_OPERATOR);
			    Query subQuery = subQueryParser.parse(searchString);
			
			    QueryWrapperFilter subQueryWrapper = new QueryWrapperFilter(subQuery);
			    
			    log.debug("Fixed query in process facet catagory 2 = " + searchString);
			    
			    DocIdSet subQueryBits = subQueryWrapper.getDocIdSet(reader);
			
			    OpenBitSetDISI subQuerybitSet = new OpenBitSetDISI(subQueryBits.iterator(), maxNumberOfMainQueryHits);

			    count = getFacetHitCount(mainQueryBitSet, subQuerybitSet);
		    }
			else
			{
				log.error("bad search string " + searchString);
			}
			f.setHits(count);
		}
	}

	public void setAnalyzer(Analyzer analyzer) {
		this.analyzer = analyzer;
	}

	public String[] getFields() {
		return fields;
	}

	/* (non-Javadoc)
	 * @see edu.ur.ir.repository.InstitutionalItemSearchService#executeSearchWithFacets(java.lang.String, java.util.Set, java.lang.String, int, int, int)
	 */
	public FacetSearchHelper executeSearchWithFacets(
			String mainQueryString, 
			List<FacetFilter> filters, 
			String indexFolder,
			int numberOfHitsToProcessForFacets,
			int numberOfResultsToCollectForFacets, 
			int numberOfFactsToShow,
			int numberOfIdsToCollect,
			int idsToCollectStartPosition)
			throws CorruptIndexException, IOException, ParseException {

		
		if( searchDirectoryIsEmpty(indexFolder) || isInvalidQuery(mainQueryString))
		{
			return new FacetSearchHelper(new HashSet<Long>(), 0, new HashMap<String, Collection<FacetResult>>(), mainQueryString);
		}
		
		IndexSearcher searcher = new IndexSearcher(indexFolder);
		IndexReader reader = searcher.getIndexReader();
		QueryParser parser = new MultiFieldQueryParser(fields, analyzer);
		parser.setDefaultOperator(QueryParser.AND_OPERATOR);
		
		HashMap<String, Collection<FacetResult>> facetResults = new HashMap<String, Collection<FacetResult>>();
		
		// execute the main query - we will use this to extract data to determine the facet searches
		String executedQuery = SearchHelper.prepareMainSearchString(mainQueryString, true);
		
		if( log.isDebugEnabled() )
		{
			log.debug("parsed query = " +  executedQuery.trim());
		}
		Query mainQuery = parser.parse( executedQuery);

		
		//create a filter for the main query
		QueryWrapperFilter mainQueryWrapper = new QueryWrapperFilter(mainQuery);
		
	    // get the bitset for main query
		DocIdSet mainQueryBits = mainQueryWrapper.getDocIdSet(reader);
		OpenBitSetDISI mainQueryBitSet = new OpenBitSetDISI(mainQueryBits.iterator(), maxNumberOfMainQueryHits);
		TopDocs hits = null;
		
		if( filters.size() > 0 )
		{
		    // create a filter that will match the main query plus all other filters
			List<Filter> luceneFilters = getSubQueryFilters(filters, searcher);		
		    Filter filter = new ChainedFilter(luceneFilters.toArray(new Filter[luceneFilters.size()]), ChainedFilter.AND);
		    if(log.isDebugEnabled())
		    {
		    	log.debug("filter = " + filter);
		    }
		    
		    // apply the facets and include them in the main query bit set
		    DocIdSet filterQueryBits = filter.getDocIdSet(reader);
		    
		    
		    OpenBitSetDISI filterBitSet = new OpenBitSetDISI(filterQueryBits.iterator(), maxNumberOfMainQueryHits);
		    mainQueryBitSet.and(filterBitSet);
		    
		    hits = searcher.search(mainQuery, filter, maxNumberOfMainQueryHits);
		    log.debug(" executeSearchWithFacets 2 = mainQuery = " + executedQuery + " filter = " + filter);	    
		}
		else
		{
			hits = searcher.search(mainQuery, maxNumberOfMainQueryHits);
		    log.debug(" executeSearchWithFacets 3 = mainQuery = " + mainQuery);	    

		}
		
		// determine the set of data we should use to determine facets
		HashMap<String, HashMap<String, FacetResult>> possibleFacets = this.generateFacetSearches(hits, numberOfHitsToProcessForFacets, numberOfResultsToCollectForFacets, searcher);


        FacetSearchHelper helper = processPossibleFacets(possibleFacets, 
        		reader, 
        		mainQueryBitSet, 
        		facetResults, 
        		hits, 
        		numberOfIdsToCollect, 
        		idsToCollectStartPosition,
        		numberOfFactsToShow,
        		mainQueryString,
        		searcher);
		
        helper.setExecutedQuery(executedQuery);
        helper.setFacetTrail(filters);
        
        searcher.close();
        return helper;
	}
	
	
	/* (non-Javadoc)
	 * @see edu.ur.ir.institution.InstitutionalItemSearchService#executeSearchWithFacets(java.lang.String, java.util.List, java.lang.String, int, int, int, edu.ur.ir.institution.InstitutionalCollection)
	 */
	public FacetSearchHelper executeSearchWithFacets(
			String mainQueryString, 
			List<FacetFilter> filters, 
			String indexFolder,
			int numberOfHitsToProcessForFacets,
			int numberOfResultsToCollectForFacets, 
			int numberOfFactsToShow,
			int numberOfIdsToCollect,
			int idsToCollectStartPosition,
			InstitutionalCollection collection)
			throws CorruptIndexException, IOException, ParseException {

		if( searchDirectoryIsEmpty(indexFolder) || isInvalidQuery(mainQueryString))
		{
			return new FacetSearchHelper(new HashSet<Long>(), 0, new HashMap<String, Collection<FacetResult>>(), mainQueryString);
		}
		
		IndexSearcher searcher = new IndexSearcher(indexFolder);
		IndexReader reader = searcher.getIndexReader();
		QueryParser parser = new MultiFieldQueryParser(fields, analyzer);
		parser.setDefaultOperator(QueryParser.AND_OPERATOR);
		
		HashMap<String, Collection<FacetResult>> facetResults = new HashMap<String, Collection<FacetResult>>();
		
		// execute the main query - we will use this to extract data to determine the facet searches
		String executedQuery = SearchHelper.prepareMainSearchString(mainQueryString, true);
		Query mainQuery = parser.parse(executedQuery);

		if( log.isDebugEnabled() )
		{
			log.debug("parsed query = " + executedQuery);
		}
		//create a filter for the main query
		QueryWrapperFilter mainQueryWrapper = new QueryWrapperFilter(mainQuery);
		
	    // get the bitset for main query
		DocIdSet mainQueryBits = mainQueryWrapper.getDocIdSet(reader);
		TopDocs hits = null;
		
		List<Filter> luceneFilters = new LinkedList<Filter>();
	
		if( filters.size() > 0 )
		{
		    // create a filter that will match the main query plus all other filters
			luceneFilters.addAll(getSubQueryFilters(filters, searcher));		
		}
		// add filters for the collection first
		luceneFilters.addAll(0, getCollectionFilters(collection));
		
		Filter filter = new ChainedFilter(luceneFilters.toArray(new Filter[luceneFilters.size()]), ChainedFilter.AND);
		   
		if(log.isDebugEnabled())
		{
		    log.debug("filter = " + filter);
		}
		
		// get the filter query doc id set
		DocIdSet filterQueryBits = filter.getDocIdSet(reader);
		 
		// apply the facets and include them in the main query bit set
	    OpenBitSetDISI mainQueryBitSet = new OpenBitSetDISI(mainQueryBits.iterator(), maxNumberOfMainQueryHits);
	    OpenBitSetDISI filterBitSet = new OpenBitSetDISI(filterQueryBits.iterator(), maxNumberOfMainQueryHits);
	    mainQueryBitSet.and(filterBitSet);
	    
	    hits = searcher.search(mainQuery, filter, maxNumberOfMainQueryHits);
	    log.debug(" executeSearchWithFacets 4 = mainQuery = " + mainQuery + " filter = " + filter);	    

		
		// determine the set of data we should use to determine facets
		HashMap<String, HashMap<String, FacetResult>> possibleFacets = this.generateFacetSearches(hits, numberOfHitsToProcessForFacets, numberOfResultsToCollectForFacets, searcher);


        FacetSearchHelper helper = processPossibleFacets(possibleFacets, 
        		reader, 
        		mainQueryBitSet, 
        		facetResults, 
        		hits, 
        		numberOfIdsToCollect, 
        		idsToCollectStartPosition,
        		numberOfFactsToShow,
        		mainQueryString,
        		searcher);
		
        helper.setFacetTrail(filters);
        helper.setExecutedQuery(executedQuery);
        searcher.close();
        return helper;
	}
	
	
	/**
	 * Execute the sub query facets and return the search results
	 * @throws ParseException 
	 * @throws IOException 
	 */
	private List<Filter> getSubQueryFilters( List<FacetFilter> filters, 
			IndexSearcher searcher) throws ParseException, IOException
	{
		List<Filter> luceneFilters = new LinkedList<Filter>();
		
		for(FacetFilter filter : filters)
		{	
			
			if(log.isDebugEnabled())
			{
				log.debug("adding filter for field " + filter.getField() + " and query " + filter.getQuery());
			}
		    QueryParser subQueryParser = new QueryParser(filter.getField(), analyzer);
		    subQueryParser.setDefaultOperator(QueryParser.AND_OPERATOR);
		    String fixedQuery = SearchHelper.prepareFacetSearchString(filter.getQuery(), false);
		    fixedQuery = "\"" + fixedQuery +"\"";
		    Query subQuery = subQueryParser.parse(SearchHelper.prepareFacetSearchString(filter.getQuery(), false));
		    if(log.isDebugEnabled())
			{
				log.debug("fixed query in get sub query filters 1 is " + fixedQuery);
			}
		    luceneFilters.add(new QueryWrapperFilter(subQuery));
		}
		
		return luceneFilters;
	}
	
	
	/** 
	 * Process the possible facets and determine the number of hits for each facet accross the main query.
	 * 
	 * @param possibleFacets - possible facets to show to the user
	 * @param reader - lucene reader
	 * @param mainQueryBits - bitset from the main query
	 * @param facetResults - set of facet results
	 * @param hits - number of hits
	 * @param numberOfIdsToCollect - number of ids to collect and show to user
	 * @param mainQueryString - main query 
	 * 
	 * @return - search helper
	 * @throws ParseException
	 * @throws IOException
	 */
	private FacetSearchHelper processPossibleFacets(HashMap<String, HashMap<String, FacetResult>> possibleFacets, 
			IndexReader reader, 
			OpenBitSetDISI mainQueryBits, 
			HashMap<String, 
			Collection<FacetResult>> facetResults, 
			TopDocs hits,
			int numberOfIdsToCollect, 
			int idsToCollectStartPosition,
			int numberOfFacetsToShow,
			String mainQueryString,
			IndexSearcher searcher) throws ParseException, IOException
	{
		FacetResultHitComparator facetResultHitComparator = new FacetResultHitComparator();
		// get the authors and create a facet for each author
		// determine the number of hits the author has in the main query
		HashMap<String, FacetResult> authorFacetMap = possibleFacets.get(AUTHOR_MAP);
		LinkedList<FacetResult> authorFacets = new LinkedList<FacetResult>();
		authorFacets.addAll(authorFacetMap.values()); 
		processFacetCategory(authorFacets, reader, mainQueryBits, searcher);	
		Collections.sort(authorFacets, facetResultHitComparator );

		// final holder of facets
		LinkedList<FacetResult> finalAuthorFacets;
		
		if( authorFacets.size() < numberOfFacetsToShow )
		{
			finalAuthorFacets = authorFacets;
		}
		else
		{
			finalAuthorFacets = new LinkedList<FacetResult>();
			for( int index = 0; index < numberOfFacetsToShow; index++ )
			{
				finalAuthorFacets.add(authorFacets.get(index));
			}
		}
        
		facetResults.put(AUTHOR_MAP,finalAuthorFacets);
		
		// get the subjects and create a facet for each subject
		// determine the number of hits the subject has in the main query
		HashMap<String, FacetResult> subjectFacetMap = possibleFacets.get(SUBJECT_MAP);
		LinkedList<FacetResult> subjectFacets = new LinkedList<FacetResult>();
		subjectFacets.addAll(subjectFacetMap.values()); 
		processFacetCategory(subjectFacets, reader, mainQueryBits, searcher);		
		Collections.sort(subjectFacets, facetResultHitComparator);
		
		// final holder of facets
		LinkedList<FacetResult> finalSubjectFacets;
		
		if( subjectFacets.size() < numberOfFacetsToShow )
		{
			finalSubjectFacets = subjectFacets;
		}
		else
		{
			finalSubjectFacets = new LinkedList<FacetResult>();
			for( int index = 0; index < numberOfFacetsToShow; index++ )
			{
				finalSubjectFacets.add(subjectFacets.get(index));
			}
		}
		
		
		facetResults.put(SUBJECT_MAP, finalSubjectFacets);
		
		// get the language and create a facet for each language
		// determine the number of hits the language has in the main query
		HashMap<String, FacetResult> languageFacetMap = possibleFacets.get(LANGUAGE_MAP);
		LinkedList<FacetResult> languageFacets = new LinkedList<FacetResult>();
		languageFacets.addAll(languageFacetMap.values()); 
		processFacetCategory(languageFacets, reader, mainQueryBits, searcher);
		Collections.sort(languageFacets, facetResultHitComparator);
		
		// final holder of facets
		LinkedList<FacetResult> finalLanguageFacets;
		
		if( languageFacets.size() < numberOfFacetsToShow )
		{
			finalLanguageFacets = languageFacets;
		}
		else
		{
			finalLanguageFacets = new LinkedList<FacetResult>();
			for( int index = 0; index < numberOfFacetsToShow; index++ )
			{
				finalLanguageFacets.add(languageFacets.get(index));
			}
		}
		
		
		
		facetResults.put(LANGUAGE_MAP, finalLanguageFacets);
		
		// get the format and create a facet for each format
		// determine the number of hits the format has in the main query
		HashMap<String, FacetResult> formatFacetMap = possibleFacets.get(FORMAT_MAP);
		LinkedList<FacetResult> formatFacets = new LinkedList<FacetResult>();
		formatFacets.addAll(formatFacetMap.values()); 
		processFacetCategory(formatFacets, reader, mainQueryBits, searcher);
		Collections.sort(formatFacets, facetResultHitComparator);
		
		// final holder of facets
		LinkedList<FacetResult> finalFormatFacets;
		
		if( formatFacets.size() < numberOfFacetsToShow )
		{
			finalFormatFacets = formatFacets;
		}
		else
		{
			finalFormatFacets = new LinkedList<FacetResult>();
			for( int index = 0; index < numberOfFacetsToShow; index++ )
			{
				finalFormatFacets.add(formatFacets.get(index));
			}
		}

		facetResults.put(FORMAT_MAP, finalFormatFacets);
		
		// get the format and create a facet for each format
		// determine the number of hits the format has in the main query
		HashMap<String, FacetResult> collectionFacetMap = possibleFacets.get(COLLECTION_MAP);
		LinkedList<FacetResult> collectionFacets = new LinkedList<FacetResult>();
		collectionFacets.addAll(collectionFacetMap.values()); 
		processFacetCategory(collectionFacets, reader, mainQueryBits, searcher);
		Collections.sort(collectionFacets, facetResultHitComparator);
		
		
		// final holder of facets
		LinkedList<FacetResult> finalCollectionFacets;
		
		if( collectionFacets.size() < numberOfFacetsToShow )
		{
			finalCollectionFacets = collectionFacets;
		}
		else
		{
			finalCollectionFacets = new LinkedList<FacetResult>();
			for( int index = 0; index < numberOfFacetsToShow; index++ )
			{
				finalCollectionFacets.add(collectionFacets.get(index));
			}
		}
		facetResults.put(COLLECTION_MAP, finalCollectionFacets);
		
		
		HashSet<Long> ids = new HashSet<Long>();
		
		// end position of id's to collect will be start position plus the number to collect
		int endPosition = idsToCollectStartPosition + numberOfIdsToCollect;
		
		// make sure that the end position is set up correctly
	    if(hits.totalHits < endPosition )
	    {
	    	endPosition = hits.totalHits;
	    }
	    
	    
	    String[] fieldsToLoad = { DefaultInstitutionalItemIndexService.ID };
    	MapFieldSelector fieldSelector= new MapFieldSelector(fieldsToLoad);
	    for( int index = idsToCollectStartPosition; index < endPosition; index ++ )
	    {
	    	Document doc = searcher.doc(hits.scoreDocs[index].doc,fieldSelector);
	    	ids.add(NumberTools.stringToLong(doc.get(DefaultInstitutionalItemIndexService.ID)));
	    }
        FacetSearchHelper helper = new FacetSearchHelper(ids, hits.totalHits, facetResults, mainQueryString);
        return helper;
	}
	
	/**
	 * Determine if the search directory is empty.
	 * 
	 * @param indexFolder
	 * @return
	 */
	private boolean searchDirectoryIsEmpty(String indexFolder )
	{
		
		File indexFolderLocation = new File(indexFolder);
		if( indexFolderLocation.isDirectory() && indexFolderLocation.exists())
		{
			// do not search if the folder is empty
			String[] files = indexFolderLocation.list();
			if( files == null || files.length <= 0 )
			{
				log.debug("no index files found");
				return true;
			}
			return false;
		}
		else
		{
			log.debug("search directory is not a directory or does not exist " + indexFolder);
	        return true;
		}
	}
	
	private boolean isInvalidQuery(String mainQuery)
	{
		log.debug("check to see if problem with main query " + mainQuery);
		return (mainQuery == null || mainQuery.trim().equals("") || mainQuery.trim().equals("*"));
		
	}

	
	/* (non-Javadoc)
	 * @see edu.ur.ir.institution.InstitutionalItemSearchService#executeSearchWithFacets(java.lang.String, java.lang.String, int, int, int, edu.ur.ir.institution.InstitutionalCollection)
	 */
	public FacetSearchHelper executeSearchWithFacets(
			String mainQueryString, 
			String indexFolder,
			int numberOfHitsToProcessForFacets,
			int numberOfResultsToCollectForFacets, 
			int numberOfFactsToShow,
			int numberOfIdsToCollect,
			int idsToCollectStartPosition,
			InstitutionalCollection collection) throws CorruptIndexException,
			IOException, ParseException 
	{
		log.debug("execute search with facets for a collection");
		if( searchDirectoryIsEmpty(indexFolder) || isInvalidQuery(mainQueryString))
		{
			log.debug("problem with search!");
			return new FacetSearchHelper(new HashSet<Long>(), 0, new HashMap<String, Collection<FacetResult>>(), mainQueryString);
		}
		
		IndexSearcher searcher = new IndexSearcher(indexFolder);
		IndexReader reader = searcher.getIndexReader();
		QueryParser parser = new MultiFieldQueryParser(fields, analyzer);
		parser.setDefaultOperator(QueryParser.AND_OPERATOR);
		
		
		HashMap<String, Collection<FacetResult>> facetResults = new HashMap<String, Collection<FacetResult>>();
		
		// execute the main query - we will use this to extract data to determine the facet searches
		String executedQuery  = SearchHelper.prepareMainSearchString(mainQueryString, true);
		Query mainQuery = parser.parse(executedQuery);
		
		if(log.isDebugEnabled())
		{
			log.debug("Executed query = " + executedQuery);
		}

        Filter[] aFilters = this.getCollectionFilters(collection).toArray(new Filter[2]);
 		
		Filter chainedFilter = new ChainedFilter(aFilters, ChainedFilter.AND);
		
		//create a filter for the main query
		QueryWrapperFilter mainQueryWrapper = new QueryWrapperFilter(mainQuery);
			
		// get the bitset for main query
		DocIdSet mainQueryBits =  mainQueryWrapper.getDocIdSet(reader);
		
		// get the filter query doc id set
		DocIdSet filterQueryBits = chainedFilter.getDocIdSet(reader);
		
		 // apply the filters for the collection root and range
	    OpenBitSetDISI mainQueryBitSet = new OpenBitSetDISI(mainQueryBits.iterator(), maxNumberOfMainQueryHits);
	    OpenBitSetDISI filterBitSet = new OpenBitSetDISI(filterQueryBits.iterator(), maxNumberOfMainQueryHits);
	    mainQueryBitSet.and(filterBitSet);
		 
		log.debug(" executeSearchWithFacets 5 = mainQuery = " + mainQuery + " filter = " + chainedFilter);	    
		 
		TopDocs hits = null;
		hits = searcher.search(mainQuery, chainedFilter, maxNumberOfMainQueryHits);
		
		// determine the set of data we should use to determine facets
		HashMap<String, HashMap<String, FacetResult>> possibleFacets = this.generateFacetSearches(hits, 
				numberOfHitsToProcessForFacets, 
				numberOfResultsToCollectForFacets,
				searcher);

		// process the data and determine the facets
        FacetSearchHelper helper = processPossibleFacets(possibleFacets, 
        		reader, 
        		mainQueryBitSet, 
        		facetResults, 
        		hits, 
        		numberOfIdsToCollect, 
        		idsToCollectStartPosition,
        		numberOfFactsToShow,
        		mainQueryString,
        		searcher);
        
		helper.setExecutedQuery(executedQuery);
        searcher.close();
        return helper;
	}
	
	
	/**
	 * Set up the filters for collections - this is for searching within collections.
	 * 
	 * @param collection - to search within
	 * @return - created filter
	 * @throws ParseException
	 */
	private List<Filter> getCollectionFilters(InstitutionalCollection collection) throws ParseException
	{
		List<Filter> filters = new LinkedList<Filter>();
		
        //isolate the collection root
   	    QueryParser subQueryParser = new QueryParser("collection_root_id", analyzer);
		subQueryParser.setDefaultOperator(QueryParser.AND_OPERATOR);
		Query subQuery = subQueryParser.parse(NumberTools.longToString(collection.getTreeRoot().getId()));
		filters.add(new QueryWrapperFilter(subQuery));
		
		
		//isolate the range of children
		subQueryParser = new QueryParser("collection_left_value", analyzer);
		subQueryParser.setDefaultOperator(QueryParser.AND_OPERATOR);
		subQuery = subQueryParser.parse("[" + NumberTools.longToString(collection.getLeftValue()) + " TO " + NumberTools.longToString(collection.getRightValue()) + "]" );
		filters.add(new QueryWrapperFilter(subQuery));
	    return filters;
	}

	/**
	 * Maxmimum number of main query hits to retrieve.
	 * 
	 * @return
	 */
	public int getMaxNumberOfMainQueryHits() {
		return maxNumberOfMainQueryHits;
	}

	/**
	 * Maximum number of main query hits to retrieve
	 * 
	 * @param maxNumberOfMainQueryHits
	 */
	public void setMaxNumberOfMainQueryHits(int maxNumberOfMainQueryHits) {
		this.maxNumberOfMainQueryHits = maxNumberOfMainQueryHits;
	}

	
	

}
