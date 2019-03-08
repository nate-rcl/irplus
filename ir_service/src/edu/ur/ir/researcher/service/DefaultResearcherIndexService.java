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


package edu.ur.ir.researcher.service;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.NumberTools;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;

import edu.ur.ir.ErrorEmailService;
import edu.ur.ir.NoIndexFoundException;
import edu.ur.ir.researcher.Researcher;
import edu.ur.ir.researcher.ResearcherIndexService;
import edu.ur.ir.user.Department;

/**
 * This updates the specified index with the given researcher information
 * 
 * @author Sharmila Ranganathan
 *
 */
public class DefaultResearcherIndexService implements ResearcherIndexService{
	
	/** eclipse generated id */
	private static final long serialVersionUID = 3165292267647040418L;

	/** Id of researcher */
	public static final String ID = "id";
	
	/** First name of researcher */
	public static final String FIRST_NAME = "first_name";
	
	/** Last name of researcher */
	public static final String LAST_NAME = "last_name";
	
	/** Email of researcher */
	public static final String EMAIL = "email";
	
	/** Department of researcher */
	public static final String DEPARTMENT = "department";
	
	/** Title of researcher */
	public static final String TITLE = "title";
	
	/** Field of researcher */
	public static final String FIELD = "field";
	
	/** Teaching interest of researcher */
	public static final String TEACHING_INTEREST = "teaching_interest";
	
	/** Research interest of researcher */
	public static final String RESEARCH_INTEREST = "research_interest";
	
	/** Keywords for researcher */
	public static final String KEY_WORDS = "key_words";
	
	/** separator used for multi-set data */
	public static final String SEPERATOR = "|";
	
	/** This is used to remove all seperator characters for indexed values if needed */
	public static final String ESCAPED_SEPERATOR = "\\|";
	
	/** Analyzer for dealing with text indexing */
	private Analyzer analyzer;
	
	/** Service for sending email errors */
	private ErrorEmailService errorEmailService;


	/**  Get the logger for this class */
	private static final Logger log = LogManager.getLogger(DefaultResearcherIndexService.class);

	/**
	 * Analyzer used to analyze the file.
	 * 
	 * @return the analyzer used to analyze the information
	 */
	public Analyzer getAnalyzer() {
		return analyzer;
	}

	/**
	 * Set the analyzer researcher to index the information.
	 * 
	 * @param analyzer
	 */
	public void setAnalyzer(Analyzer analyzer) {
		this.analyzer = analyzer;
	}
	
	
	/**
	 * Add the researcher to the index.
	 * 
	 * @see edu.ur.ir.researcher.ResearcherIndexService#addToIndex(edu.ur.ir.researcher.Researcher, java.io.File)
	 */
	public void addToIndex(Researcher researcher, File researcherIndexFolder)
			throws NoIndexFoundException {
		
		if( log.isDebugEnabled() )
		{
			log.debug("adding researcher: " + researcher + " to index folder " + researcherIndexFolder.getAbsolutePath());
		}
		
		if( !researcherIndexFolder.exists())
		{
			throw new NoIndexFoundException("the folder " + researcherIndexFolder.getAbsolutePath() + " could not be found");
		}
	    writeDocument(researcherIndexFolder.getAbsolutePath(), getDocument(researcher));
	}

	/**
	 * Create a single string set of keyword values separated by  a |
	 *  
	 * @param keywordValues - keyword values in comma separated values
	 * @return the string with | separated values
	 */
	private String getKeywords(String keywordValues)
	{
		String keywords = "";
		if( keywordValues == null || keywordValues.trim().equals(""))
		{
			return keywords;
		}
		StringTokenizer tokenizer = new StringTokenizer(keywordValues, ",");
		boolean first = true;
		while(tokenizer.hasMoreElements())
		{
			String nextValue = tokenizer.nextToken().toLowerCase().trim();
			if( first )
			{
			    keywords = nextValue;
			    first = false;
			}
			else
			{
				keywords = keywords + " " + SEPERATOR + " " + nextValue;
			}
		}
		
		return keywords;
	}
	
	/**
	 * Delete the researcher from the specified index.
	 * 
	 * @see edu.ur.ir.researcher.ResearcherIndexService#deleteFromIndex(edu.ur.ir.researcher.Researcher, java.io.File)
	 */
	public void deleteFromIndex(Long researcherId, File researcherIndexFolder) {
		if( log.isDebugEnabled() )
		{
			log.debug("deleting researcher id : " + researcherId + " from index folder " + researcherIndexFolder.getAbsolutePath());
		}
		// if the researcher does not have an index folder
		// don't need to do anything.
		if( researcherIndexFolder == null || !researcherIndexFolder.exists() || researcherIndexFolder.list() == null ||
				researcherIndexFolder.list().length == 0)
		{
			return;
		}
		
		Directory directory = null;
		IndexWriter writer = null;
		try {
			directory = FSDirectory.getDirectory(researcherIndexFolder.getAbsolutePath());
			writer = getWriter(directory);
			Term term = new Term(ID, NumberTools.longToString(researcherId));
			writer.deleteDocuments(term);
			writer.commit();
			
		} catch (Exception e) {
			log.error(e);
			errorEmailService.sendError(e);
		}
		finally {
			if (writer != null) {
			    try {
				    writer.close();
			    } catch (Exception e) {
				    log.error(e);
			    }
		    }
		    writer = null;
		    try {
				IndexWriter.unlock(directory);
			} 
	    	catch (IOException e1)
	    	{
				log.error(e1);
			}
		    
		    if( directory != null )
		    {
		    	try
		    	{
		    		directory.close();
		    	}
		    	catch (Exception e) {
				    log.error(e);
			    }
		    }
		    directory = null;
	    }
		
	}

	
	/**
	 * Update the index with the specified existing researcher.
	 * 
	 * @see edu.ur.ir.researcher.ResearcherIndexService#updateIndex(edu.ur.ir.researcher.Researcher, java.io.File)
	 */
	public void updateIndex(Researcher researcher, File researcherIndexFolder)
			throws NoIndexFoundException {
		if( log.isDebugEnabled() )
		{
			log.debug("updating index for researcher: " + researcher + " in index folder " + researcherIndexFolder.getAbsolutePath());
		}
		deleteFromIndex(researcher.getId(), researcherIndexFolder);
		addToIndex(researcher, researcherIndexFolder);
	}
	
	/**
	 * Write the document to the index in the directory.
	 * 
	 * @param directoryPath - location where the directory exists.
	 * @param documents - documents to add to the directory.
	 */
	private void writeDocument(String directoryPath, Document document)
	{
		log.debug("write document to directory " + directoryPath );
		Directory directory = null;
		IndexWriter writer = null;
		try {
			directory = FSDirectory.getDirectory(directoryPath);
			writer = getWriter(directory);
			writer.addDocument(document);
			writer.commit();
		} catch (Exception e) {
			log.error(e);
			errorEmailService.sendError(e);
		}
		finally
		{
			if (writer != null) {
			    try {
				    writer.close();
			    } catch (Exception e) {
				    log.error(e);
			    }
		    }
		    writer = null;
		    try {
				IndexWriter.unlock(directory);
			} 
	    	catch (IOException e1)
	    	{
				log.error(e1);
			}
		    
		    if( directory != null )
		    {
		    	try
		    	{
		    		directory.close();
		    	}
		    	catch (Exception e) {
				    log.error(e);
			    }
		    }
		    directory = null;
		}
	}
	
	/**
	 * Add researchers to the index
	 * @see edu.ur.ir.researcher.ResearcherIndexService#addResearchers(java.util.List, java.io.File, boolean)
	 */
	public void addResearchers(List<Researcher> researchers, File researcherIndexFolder,
			boolean overwriteExistingIndex) {
			
	    LinkedList<Document> docs = new LinkedList<Document>();
			
		for(Researcher r : researchers)
		{
			log.debug("Adding researcher " + r);
			docs.add(getDocument(r));
		}
			
		IndexWriter writer = null;
		Directory directory = null;
		try {
			directory = FSDirectory.getDirectory(researcherIndexFolder.getAbsolutePath());
			
			if(overwriteExistingIndex)
			{
			    writer = getWriterOverwriteExisting(directory);
			}
			else
			{
				writer = getWriter(directory);
			}
			 
			for(Document d : docs)
			{
			    writer.addDocument(d);
			}
			writer.commit();
		}
		catch (Exception e) 
		{
			log.error(e);
			errorEmailService.sendError(e);
		}
		finally 
		{
			if (writer != null) {
			    try {
				    writer.close();
			    } catch (Exception e) {
				    log.error(e);
			    }
		    }
		    writer = null;
		    try {
				IndexWriter.unlock(directory);
			} 
	    	catch (IOException e1)
	    	{
				log.error(e1);
			}
		    
		    if( directory != null )
		    {
		    	try
		    	{
		    		directory.close();
		    	}
		    	catch (Exception e) {
				    log.error(e);
			    }
		    }
		    directory = null;
		    docs = null;
		}
	}
	
	/**
	 * Get the document for the researcher 
	 * 
	 * @param researcher - researcher to create the document from
	 * @return - the created document
	 */
	private Document getDocument(Researcher researcher)
	{
		Document doc = new Document();
        
	    doc.add(new Field(ID, 
	    	NumberTools.longToString(researcher.getId()), 
			Field.Store.YES, 
			Field.Index.NOT_ANALYZED));
	    
	    String title = researcher.getTitle();
	    if( title != null && !title.trim().equals(""))
	    {
	    	title = title.replaceAll(ESCAPED_SEPERATOR, " ");
	    	doc.add(new Field(TITLE, 
				title, 
				Field.Store.YES, 
				Field.Index.ANALYZED));
	    }
	    
	    //keywords for the researcher
		String keywords = getKeywords(researcher.getKeywords());
	    if(keywords != null && !keywords.equals(""))
		{
	    	doc.add(new Field(KEY_WORDS, 
	    		keywords, 
				Field.Store.YES, 
				Field.Index.ANALYZED));
	    }
	    
	    if( researcher.getUser().getFirstName() != null )
	    {
	        doc.add(new Field(FIRST_NAME, 
				researcher.getUser().getFirstName(), 
				Field.Store.YES, 
				Field.Index.ANALYZED));
	    }
	    
	    if( researcher.getUser().getLastName() != null )
	    {
	        doc.add(new Field(LAST_NAME, 
				researcher.getUser().getLastName(), 
				Field.Store.YES, 
				Field.Index.ANALYZED));
	    }
	    
	    if( researcher.getEmail() != null )
	    {
	    	doc.add(new Field(EMAIL, 
				researcher.getEmail(), 
				Field.Store.YES, 
				Field.Index.ANALYZED));
	    }
	    
	    StringBuffer departmentNames = new StringBuffer();
    	for (Department d : researcher.getUser().getDepartments()) {
	    	departmentNames.append(d.getName());
	    	departmentNames.append(SEPERATOR);
    	}
	    
    	String names = departmentNames.toString();
	    if( names != null && !names.trim().equals(""))
	    {
	    	doc.add(new Field(DEPARTMENT, 
					names, 
					Field.Store.YES, 
					Field.Index.ANALYZED));
	    }

	    String fields = researcher.getAllFieldNames();
	    
	    if (fields != null && !fields.trim().equals("")) {
		    doc.add(new Field(FIELD, 
		    		fields, 
					Field.Store.YES, 
					Field.Index.ANALYZED));
	    }

		if(researcher.getResearchInterest() != null && !researcher.getResearchInterest().equals(""))
		{
			doc.add(new Field(RESEARCH_INTEREST, 
					researcher.getResearchInterest(), 
					Field.Store.YES, 
					Field.Index.ANALYZED));
		}

		if(researcher.getTeachingInterest() != null && !researcher.getTeachingInterest().equals(""))
		{
			doc.add(new Field(TEACHING_INTEREST, 
					researcher.getTeachingInterest(), 
					Field.Store.YES, 
					Field.Index.ANALYZED));
		}
		
		return doc;
	}
	
	/**
	 * All methods should use this to obtain a writer on the directory.  This will return 
	 * a null writer if the index is locked.  A while loop can be set up to determine if an index
	 * writer is available for the specified directory. This ensures that only one writer is writing to a 
	 * users index at once.
	 * 
	 * @param directory
	 * @return - writer that will not overwrite an existing directory if it exists.  This will create a directory if 
	 * one does not yet exist.
	 * 
	 * @throws CorruptIndexException
	 * @throws LockObtainFailedException
	 * @throws IOException
	 */
	private IndexWriter getWriter(Directory directory) throws CorruptIndexException, LockObtainFailedException, IOException
	{
		IndexWriter writer = null;
	    writer = new IndexWriter(directory, analyzer, IndexWriter.MaxFieldLength.LIMITED);
		return writer;
	}
	
	/**
	 * All methods should use this to obtain a writer on the directory.  This will return 
	 * a null writer if the index is locked.  A while loop can be set up to determine if an index
	 * writer is available for the specified directory. This ensures that only one writer is writing to a 
	 * users index at once.
	 * 
	 * @param directory
	 * @return - writer set to overwrite the existing index
	 * @throws CorruptIndexException
	 * @throws LockObtainFailedException
	 * @throws IOException
	 */
	private IndexWriter getWriterOverwriteExisting(Directory directory) throws CorruptIndexException, LockObtainFailedException, IOException
	{
		IndexWriter  writer = new IndexWriter(directory, analyzer, true, IndexWriter.MaxFieldLength.LIMITED);
		return writer;
	}
	
	/**
	 * Optimize the index.
	 * 
	 * @see edu.ur.ir.person.NameAuthorityIndexService#optimize(java.io.File)
	 */
	public void optimize(File researcherIndex) {
		IndexWriter writer = null;
		Directory directory = null;
		try 
		{
		    directory = FSDirectory.getDirectory(researcherIndex.getAbsolutePath());
			writer = getWriter(directory);
			writer.optimize();
		} 
		catch (Exception e) 
		{
			log.error(e);
			errorEmailService.sendError(e);
		}
		finally 
        {
		    if (writer != null) {
			    try {
				    writer.close();
			    } catch (Exception e) {
				    log.error(e);
			    }
		    }
		    writer = null;
		    try 
		    {
				IndexWriter.unlock(directory);
			} 
	    	catch (IOException e1)
	    	{
				log.error(e1);
			}
		    if( directory != null )
		    {
		    	try
		    	{
		    		directory.close();
		    	}
		    	catch (Exception e) {
				    log.error(e);
			    }
		    }
		    directory = null;
	    }
	}
	
	
	/**
	 * Service for dealing with emails.
	 * 
	 * @return
	 */
	public ErrorEmailService getErrorEmailService() {
		return errorEmailService;
	}

	/**
	 * Service for dealing with emails.
	 * 
	 * @param errorEmailService
	 */
	public void setErrorEmailService(ErrorEmailService errorEmailService) {
		this.errorEmailService = errorEmailService;
	}

}
