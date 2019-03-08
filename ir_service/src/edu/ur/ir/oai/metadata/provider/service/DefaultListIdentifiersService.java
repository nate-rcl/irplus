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

package edu.ur.ir.oai.metadata.provider.service;

import java.io.StringWriter;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

import edu.ur.ir.ErrorEmailService;
import edu.ur.ir.institution.DeletedInstitutionalItemVersion;
import edu.ur.ir.institution.DeletedInstitutionalItemVersionService;
import edu.ur.ir.institution.InstitutionalCollection;
import edu.ur.ir.institution.InstitutionalCollectionService;
import edu.ur.ir.institution.InstitutionalItemVersion;
import edu.ur.ir.institution.InstitutionalItemVersionService;
import edu.ur.ir.oai.OaiUtil;
import edu.ur.ir.oai.exception.BadArgumentException;
import edu.ur.ir.oai.exception.BadResumptionTokenException;
import edu.ur.ir.oai.exception.CannotDisseminateFormatException;
import edu.ur.ir.oai.exception.NoRecordsMatchException;
import edu.ur.ir.oai.exception.NoSetHierarchyException;
import edu.ur.ir.oai.metadata.provider.ListIdentifiersService;
import edu.ur.ir.oai.metadata.provider.ListRecordsService;
import edu.ur.ir.oai.metadata.provider.ListSetsService;
import edu.ur.ir.oai.metadata.provider.OaiMetadataProvider;
import edu.ur.ir.oai.metadata.provider.OaiMetadataServiceProvider;

/**
 * Default implementation of the list identifiers service.
 * 
 * @author Nathan Sarr
 *
 */
public class DefaultListIdentifiersService implements ListIdentifiersService, ListRecordsService{
	
	/** eclipse generated id */
	private static final long serialVersionUID = 9056980425349175595L;
	
	/** logger for this class */
	private static Logger log = LogManager.getLogger(DefaultListIdentifiersService.class);
	
	/**  Default batch size for harvesting */
	private int batchSize = 500;
	
	/**  List of oai metadata service providers */
	private OaiMetadataServiceProvider oaiMetadataServiceProvider;
	
	/** Service for dealing with institutional item information */
	private InstitutionalItemVersionService institutionalItemVersionService;
	
	/** Service for dealing with deleted information */
	private DeletedInstitutionalItemVersionService deletedInstitutionalItemVersionService;	

	/** namespace for the oai url */
	private String namespaceIdentifier;
	
	/** Service to deal with institutional collection information */
	private InstitutionalCollectionService institutionalCollectionService;
	
	/** service to deal with listing set information */
	private ListSetsService listSetsService;
	
	/** service to send emails on error */
	private ErrorEmailService errorEmailService;


	


	/**
	 * List the identifiers for the system.
	 * 
	 * @see edu.ur.ir.oai.metadata.provider.ListIdentifiersService#listIdentifiers(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	public String listIdentifiers(String metadataPrefix, String set,
			String from, String until, String strResumptionToken)
			throws BadResumptionTokenException,
			CannotDisseminateFormatException, NoRecordsMatchException, BadArgumentException{
		
		/** resumption token */
		DefaultResumptionToken resumptionToken = this.initilizeResumptionToken(metadataPrefix, set, from, until, 
        		strResumptionToken);
 		
        log.debug("resumption token = " + resumptionToken);
        if( !oaiMetadataServiceProvider.supports(resumptionToken.getMetadataPrefix()) )
		{
			throw new CannotDisseminateFormatException("Format: " + resumptionToken.getMetadataPrefix() + " is not supported");
		}

		int completeListSize = getCompleteListSize(resumptionToken);
		if( resumptionToken.isInitialRequest() && completeListSize <= 0)
		{
			throw new NoRecordsMatchException("No records found for request");
		}
		
		 DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		 DocumentBuilder builder;
		
		 
		 try {
			builder = factory.newDocumentBuilder();
		 } catch (ParserConfigurationException e) {
			throw new IllegalStateException(e);
		 }
		
		 DOMImplementation impl = builder.getDOMImplementation();
		 DOMImplementationLS domLs = (DOMImplementationLS)impl.getFeature("LS" , "3.0");
		 LSSerializer serializer = domLs.createLSSerializer();
		 LSOutput lsOut= domLs.createLSOutput();
		 StringWriter stringWriter = new StringWriter();
		 lsOut.setCharacterStream(stringWriter);
		 
		 Document doc = impl.createDocument(null, "ListIdentifiers", null);


		if(!resumptionToken.getDeleted())
		{
			List<InstitutionalItemVersion> versions = getVersions(resumptionToken);
		    addIdentifiers(versions, doc, resumptionToken);
		    resumptionToken.setCompleteListSize(completeListSize);
		}
		else if( resumptionToken.getDeleted() )
		{
			List<DeletedInstitutionalItemVersion> deletedVersions = getDeletedVersions(resumptionToken);
		    addDeletedIdentifiers(deletedVersions, doc, resumptionToken);
		    resumptionToken.setCompleteListSize(completeListSize);
		}
		 
		 addResumptionToken(doc, resumptionToken);
		 
		 // do not output the headers
		 Element root = doc.getDocumentElement();
		 serializer.getDomConfig().setParameter("xml-declaration", false);
		 serializer.write(root, lsOut);
		 return stringWriter.getBuffer().toString();
	}
	
	/**
	 * A request to list identifiers with only the metadata prefix.
	 * 
	 * @param metadataPrefix
	 * @return
	 */
	private void addIdentifiers(List<InstitutionalItemVersion> versions, Document doc, DefaultResumptionToken resumptionToken )
	{
		 Element root = doc.getDocumentElement();		 
		 for(InstitutionalItemVersion version : versions)
		 {
			 // create the header element of the record 
			 Element header = doc.createElement("header");
			 root.appendChild(header);
			 
			 // identifier element
			 Element identifier = doc.createElement("identifier");
			 Text data = doc.createTextNode("oai:" + namespaceIdentifier + ":" + version.getId().toString());
			 identifier.appendChild(data);
			 header.appendChild(identifier);
			 
			 // datestamp element
			 Element datestamp = doc.createElement("datestamp");
			 Date d = version.getDateLastModified();
			 if( d == null )
			 {
				 d = version.getDateOfDeposit();
			 }
			 String zuluDateTime = OaiUtil.getZuluTime(d);
			 
			 data = doc.createTextNode(zuluDateTime);
			 datestamp.appendChild(data);
			 header.appendChild(datestamp);
			 
			 InstitutionalCollection collection = version.getVersionedInstitutionalItem().getInstitutionalItem().getInstitutionalCollection();
			 Element setSpec = doc.createElement("setSpec");
			 data = doc.createTextNode(listSetsService.getSetSpec(collection));
			 setSpec.appendChild(data);
			 header.appendChild(setSpec);
		 }
	}
	
	/**
	 * A request to list identifiers with only the metadata prefix.
	 * 
	 * @param metadataPrefix
	 * @return
	 */
	private void addDeletedIdentifiers(List<DeletedInstitutionalItemVersion> deletedVersions, Document doc, DefaultResumptionToken resumptionToken )
	{
		 Element root = doc.getDocumentElement();		 
		 for(DeletedInstitutionalItemVersion version : deletedVersions)
		 {
			 // create the header element of the record 
			 Element header = doc.createElement("header");
			 header.setAttribute("status", "deleted");
			 root.appendChild(header);
			 
			 // identifier element
			 Element identifier = doc.createElement("identifier");
			 Text data = doc.createTextNode("oai:" + namespaceIdentifier + ":" + version.getInstitutionalItemVersionId().toString());
			 identifier.appendChild(data);
			 header.appendChild(identifier);
			 
			 // datestamp element
			 Element datestamp = doc.createElement("datestamp");
			 Date d = version.getDeletedInstitutionalItem().getDeletedDate();
			 String zuluDateTime = OaiUtil.getZuluTime(d);
			 
			 data = doc.createTextNode(zuluDateTime);
			 datestamp.appendChild(data);
			 header.appendChild(datestamp);
			 
			 Long collectionId = version.getDeletedInstitutionalItem().getInstitutionalCollectionId();
			 if( collectionId != null )
			 {
			     String setSpecStr = listSetsService.getSetSpec(collectionId);
			     if( setSpecStr != null )
			     {
			         Element setSpec = doc.createElement("setSpec");
			         data = doc.createTextNode(setSpecStr);
			         setSpec.appendChild(data);
			         header.appendChild(setSpec);
			     }
			 }
		 }
	}
	
	/**
	 * Add the resumption token as XML.
	 * 
	 * @param doc
	 */
	private void addResumptionToken(Document doc, DefaultResumptionToken resumptionToken )
	{
		 Element root = doc.getDocumentElement();	
		// create the header element of the record 
		 Element resumption = doc.createElement("resumptionToken");
		 if( resumptionToken.getCompleteListSize() != null )
		 {
		     resumption.setAttribute("completeListSize", resumptionToken.getCompleteListSize().toString());
		 }
		 
		 Text data;
		 if( resumptionToken.getInsertToken() )
		 {
		     data = doc.createTextNode(resumptionToken.getAsTokenString());
		 }
		 else
		 {
			 data = doc.createTextNode("");
		 }
			 
		 resumption.appendChild(data);
		 root.appendChild(resumption);
	}
	
	public int getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}
	
	public OaiMetadataServiceProvider getOaiMetadataServiceProvider() {
		return oaiMetadataServiceProvider;
	}

	public void setOaiMetadataServiceProvider(
			OaiMetadataServiceProvider oaiMetadataServiceProvider) {
		this.oaiMetadataServiceProvider = oaiMetadataServiceProvider;
	}

	public InstitutionalItemVersionService getInstitutionalItemVersionService() {
		return institutionalItemVersionService;
	}

	public void setInstitutionalItemVersionService(
			InstitutionalItemVersionService institutionalItemVersionService) {
		this.institutionalItemVersionService = institutionalItemVersionService;
	}
	
	public String getNamespaceIdentifier() {
		return namespaceIdentifier;
	}

	public void setNamespaceIdentifier(String namespaceIdentifier) {
		this.namespaceIdentifier = namespaceIdentifier;
	}

	public InstitutionalCollectionService getInstitutionalCollectionService() {
		return institutionalCollectionService;
	}

	public void setInstitutionalCollectionService(
			InstitutionalCollectionService institutionalCollectionService) {
		this.institutionalCollectionService = institutionalCollectionService;
	}
	

	
	public ListSetsService getListSetsService() {
		return listSetsService;
	}

	public void setListSetsService(ListSetsService listSetsService) {
		this.listSetsService = listSetsService;
	}
	
	/**
	 * Get a count of the number of items that will be retrieved this includes
	 * existing as well as deleted records.
	 * 
	 * @return
	 */
	private int getCompleteListSize(DefaultResumptionToken resumptionToken)
	{
		int completeListSize = 0;
		if( resumptionToken.getSet() == null)
		{
			if(resumptionToken.getFrom() == null && resumptionToken.getUntil() == null)
			{
			    // no set and no from until set
			    completeListSize += institutionalItemVersionService.getCount();
			    completeListSize += deletedInstitutionalItemVersionService.getCount();
		    }
			else if(resumptionToken.getFrom() != null && resumptionToken.getUntil() == null)
		    {
			    // no set but from date passed in
			    completeListSize += institutionalItemVersionService.getItemsFromModifiedDateCount(resumptionToken.getFrom());
			    completeListSize += deletedInstitutionalItemVersionService.getItemsFromDeletedDateCount(resumptionToken.getFrom());
		    }
		    else if( resumptionToken.getFrom() == null && resumptionToken.getUntil() != null )
		    {
			    // no set but until date passed in
			    completeListSize += institutionalItemVersionService.getItemsUntilModifiedDateCount(resumptionToken.getUntil());
			    completeListSize += deletedInstitutionalItemVersionService.getItemsUntilDeletedDateCount(resumptionToken.getUntil());
		    }
		    else if( resumptionToken.getFrom() != null && resumptionToken.getUntil() != null)
		    {
			    // no set but from and until passed in
			    completeListSize += institutionalItemVersionService.getItemsBetweenModifiedDatesCount(resumptionToken.getFrom(), 
					resumptionToken.getUntil());
			    completeListSize += deletedInstitutionalItemVersionService.getItemsBetweenDeletedDatesCount(resumptionToken.getFrom(), 
					resumptionToken.getUntil());
		    }
		}
		else if( resumptionToken.getSet() != null) 
		{
		    InstitutionalCollection collection = institutionalCollectionService.getCollection(resumptionToken.getLastSetId(), false);

		    if( collection != null )
		    {
			    if( resumptionToken.getFrom() == null && resumptionToken.getUntil() == null )
		        {
			        // set passed in but no from until passed in
			        completeListSize += institutionalItemVersionService.getCount(collection);
			        completeListSize += deletedInstitutionalItemVersionService.getCount(collection);
		        }
		        else if( resumptionToken.getFrom() != null && resumptionToken.getUntil() == null)
		        {
			        // set and from passed in but no until passed in
			        completeListSize += institutionalItemVersionService.getItemsFromModifiedDateCount(resumptionToken.getFrom(), collection);
			        completeListSize += deletedInstitutionalItemVersionService.getItemsFromDeletedDateCount(resumptionToken.getFrom(), collection);
		        }
		        else if(resumptionToken.getFrom() == null && resumptionToken.getUntil() != null)
		        {
			        // set and until passed in but no from 
			        completeListSize += institutionalItemVersionService.getItemsUntilModifiedDateCount(resumptionToken.getUntil(), collection);
			        completeListSize += deletedInstitutionalItemVersionService.getItemsUntilDeletedDateCount(resumptionToken.getUntil(), collection);
		        }
		        else if(resumptionToken.getSet() != null && resumptionToken.getFrom() != null && resumptionToken.getUntil() != null)
		        {
			        completeListSize += institutionalItemVersionService.getItemsBetweenModifiedDatesCount(resumptionToken.getFrom(), resumptionToken.getUntil(), collection);
			        completeListSize += deletedInstitutionalItemVersionService.getItemsBetweenDeletedDatesCount(resumptionToken.getFrom(), resumptionToken.getUntil(), collection);
		        }
		    }
		}
		
		return completeListSize;

	}
	
	/**
	 * Get the list of institutional item versions.
	 * 
	 * @return - list of institutional item versions
	 */
	private List<InstitutionalItemVersion> getVersions(DefaultResumptionToken resumptionToken)
	{
		List<InstitutionalItemVersion> versions = new LinkedList<InstitutionalItemVersion>();
	    // do batch size plus one - if we retrieve all records then another request must be issued
	    // with resumption token
		if( resumptionToken.getSet() == null)
		{
			if(resumptionToken.getFrom() == null && resumptionToken.getUntil() == null)
			{
	            versions = institutionalItemVersionService.getItemsIdOrder(resumptionToken.getLastId(), batchSize + 1);
		    }
			else if(resumptionToken.getFrom() != null && resumptionToken.getUntil() == null)
		    {
			    versions = institutionalItemVersionService.getItemsIdOrderFromModifiedDate(resumptionToken.getLastId(), resumptionToken.getFrom(), batchSize + 1 );
		    }
		    else if( resumptionToken.getFrom() == null && resumptionToken.getUntil() != null )
		    {
			    versions = institutionalItemVersionService.getItemsIdOrderUntilModifiedDate(resumptionToken.getLastId(), resumptionToken.getUntil(), batchSize + 1 );
		
		    }
		    else if( resumptionToken.getFrom() != null && resumptionToken.getUntil() != null)
		    {
			    versions = institutionalItemVersionService.getItemsIdOrderBetweenModifiedDates(resumptionToken.getLastId(), 
					resumptionToken.getFrom(), 
					resumptionToken.getUntil(), 
					batchSize + 1);
		    }
		}
		else if( resumptionToken.getSet() != null) 
		{
		    InstitutionalCollection collection = institutionalCollectionService.getCollection(resumptionToken.getLastSetId(), false);

		    if( collection != null )
		    {
			    if( resumptionToken.getFrom() == null && resumptionToken.getUntil() == null )
		        {
			        versions = institutionalItemVersionService.getItemsIdOrder(resumptionToken.getLastId(), collection,  batchSize + 1);
		        }
		        else if( resumptionToken.getFrom() != null && resumptionToken.getUntil() == null)
		        {
			        versions =  institutionalItemVersionService.getItemsIdOrderFromModifiedDate(resumptionToken.getLastId(), resumptionToken.getFrom(), collection, batchSize + 1);
		        }
		        else if(resumptionToken.getFrom() == null && resumptionToken.getUntil() != null)
		        {
			        versions =  institutionalItemVersionService.getItemsIdOrderUntilModifiedDate(resumptionToken.getLastId(), resumptionToken.getUntil(), collection, batchSize + 1);
		        }
		        else if(resumptionToken.getSet() != null && resumptionToken.getFrom() != null && resumptionToken.getUntil() != null)
		        {
			        versions =  institutionalItemVersionService.getItemsIdOrderBetweenModifiedDates(resumptionToken.getLastId(), resumptionToken.getFrom(), resumptionToken.getUntil(), collection, batchSize + 1);
		        }
		    }
		}
		
	    int size = versions.size();
		 
	    // we will need to send resumption token
	    if( size == (batchSize + 1))
	    {
		    // remove the last item as it should not be sent
		    // this only indicates that there is one more than the batch size
		    // allows
		    versions.remove(size - 1);
		    resumptionToken.setDeleted(Boolean.FALSE);
		    resumptionToken.setInsertToken(Boolean.TRUE);
		    resumptionToken.setLastId(versions.get(versions.size()-1).getId());
	    }
	    else 
	    {
	    	resumptionToken.setDeleted(Boolean.TRUE);
	    	resumptionToken.setInsertToken(Boolean.TRUE);
	    	resumptionToken.setLastId(0l);
	    }
	    return versions;
	}
	
	/**
	 * Get the list of deleted institutional item versions.
	 * 
	 * @return list of deleted versions
	 */
	private List<DeletedInstitutionalItemVersion> getDeletedVersions(DefaultResumptionToken resumptionToken)
	{
		List<DeletedInstitutionalItemVersion> deletedVersions = new LinkedList<DeletedInstitutionalItemVersion>();
	    // do batch size plus one - if we retrieve all records then another request must be issued
	    // with resumption token
		if( resumptionToken.getSet() == null)
		{
			if(resumptionToken.getFrom() == null && resumptionToken.getUntil() == null)
			{
	            deletedVersions = deletedInstitutionalItemVersionService.getItemsIdOrder(resumptionToken.getLastId(), batchSize + 1);
		    }
			else if(resumptionToken.getFrom() != null && resumptionToken.getUntil() == null)
		    {
				deletedVersions = deletedInstitutionalItemVersionService.getItemsIdOrderFromDeletedDate(resumptionToken.getLastId(), resumptionToken.getFrom(), batchSize + 1 );
		    }
		    else if( resumptionToken.getFrom() == null && resumptionToken.getUntil() != null )
		    {
		    	deletedVersions = deletedInstitutionalItemVersionService.getItemsIdOrderUntilDeletedDate(resumptionToken.getLastId(), resumptionToken.getUntil(), batchSize + 1 );
		
		    }
		    else if( resumptionToken.getFrom() != null && resumptionToken.getUntil() != null)
		    {
		    	deletedVersions = deletedInstitutionalItemVersionService.getItemsIdOrderBetweenDeletedDates(resumptionToken.getLastId(), 
					resumptionToken.getFrom(), 
					resumptionToken.getUntil(), 
					batchSize + 1);
		    }
		}
		else if( resumptionToken.getSet() != null) 
		{
		    InstitutionalCollection collection = institutionalCollectionService.getCollection(resumptionToken.getLastSetId(), false);

		    if( collection != null )
		    {
			    if( resumptionToken.getFrom() == null && resumptionToken.getUntil() == null )
		        {
				    deletedVersions = deletedInstitutionalItemVersionService.getItemsIdOrder(resumptionToken.getLastId(), collection,  batchSize + 1);
		        }
		        else if( resumptionToken.getFrom() != null && resumptionToken.getUntil() == null)
		        {
		    	    deletedVersions =  deletedInstitutionalItemVersionService.getItemsIdOrderFromDeletedDate(resumptionToken.getLastId(), resumptionToken.getFrom(), collection, batchSize + 1);
		        }
		        else if(resumptionToken.getFrom() == null && resumptionToken.getUntil() != null)
		        {
		    	    deletedVersions =  deletedInstitutionalItemVersionService.getItemsIdOrderUntilDeletedDate(resumptionToken.getLastId(), resumptionToken.getUntil(), collection, batchSize + 1);
		        }
		        else if(resumptionToken.getSet() != null && resumptionToken.getFrom() != null && resumptionToken.getUntil() != null)
		        {
		    	    deletedVersions =  deletedInstitutionalItemVersionService.getItemsIdOrderBetweenDeletedDates(resumptionToken.getLastId(), resumptionToken.getFrom(), resumptionToken.getUntil(), collection, batchSize + 1);
		        }
		    }
		}
		
	        
	    int size = deletedVersions.size();
	   
	    // we will need to send resumption token
	    if( size == (batchSize + 1))
	    {
		    // remove the last item as it should not be sent
		    // this only indicates that there is one more than the batch size
		    // allows
	    	deletedVersions.remove(size - 1);
		    resumptionToken.setDeleted(Boolean.TRUE);
		    resumptionToken.setInsertToken(Boolean.TRUE);
		    resumptionToken.setLastId(deletedVersions.get(deletedVersions.size()-1).getId());
	    }
	    else
	    {
	    	// all done processing
	    	resumptionToken.setInsertToken(Boolean.FALSE);
	    }
	    
	    return deletedVersions;
	}
	

	/**
	 * Get the list of records.
	 * 
	 * @see edu.ur.ir.oai.metadata.provider.ListRecordsService#listRecords(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	public String listRecords(String metadataPrefix, String set, String from,
			String until, String strResumptionToken) throws BadArgumentException,
			BadResumptionTokenException, CannotDisseminateFormatException,
			NoRecordsMatchException, NoSetHierarchyException {
		   DefaultResumptionToken resumptionToken  = this.initilizeResumptionToken(metadataPrefix, set, from, 
				   until, strResumptionToken);
	       log.debug("resumption token = " + resumptionToken);
	       if( !oaiMetadataServiceProvider.supports(resumptionToken.getMetadataPrefix()) )
			{
				throw new CannotDisseminateFormatException("Format: " + resumptionToken.getMetadataPrefix() + " is not supported");
			}

			int completeListSize = getCompleteListSize(resumptionToken);
			if( resumptionToken.isInitialRequest() && completeListSize <= 0)
			{
				throw new NoRecordsMatchException("No records found for request");
			}
		
		 DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		 DocumentBuilder builder;
		 
		 try {
			builder = factory.newDocumentBuilder();
		 } catch (ParserConfigurationException e) {
			throw new IllegalStateException(e);
		 }
		
		 DOMImplementation impl = builder.getDOMImplementation();
		 DOMImplementationLS domLs = (DOMImplementationLS)impl.getFeature("LS" , "3.0");
		 LSSerializer serializer = domLs.createLSSerializer();
		 LSOutput lsOut= domLs.createLSOutput();
		 StringWriter stringWriter = new StringWriter();
		 lsOut.setCharacterStream(stringWriter);
		 
		 Document doc = impl.createDocument(null, "ListRecords", null);

		List<InstitutionalItemVersion> versions = null;
		List<DeletedInstitutionalItemVersion> deletedVersions = null;
		
		if(!resumptionToken.getDeleted())
		{
			versions = getVersions(resumptionToken);
		    addRecords(versions, doc, oaiMetadataServiceProvider.getProvider(resumptionToken.getMetadataPrefix()), resumptionToken);
		    resumptionToken.setCompleteListSize(completeListSize);
		}
		else if( resumptionToken.getDeleted() )
		{
			deletedVersions = getDeletedVersions(resumptionToken);
		    addDeletedRecords(deletedVersions, doc, oaiMetadataServiceProvider.getProvider(resumptionToken.getMetadataPrefix()), resumptionToken);
		    resumptionToken.setCompleteListSize(completeListSize);
		}
		 
		 addResumptionToken(doc, resumptionToken);
		 
		 // do not output the headers
		 Element root = doc.getDocumentElement();
		 serializer.getDomConfig().setParameter("xml-declaration", false);
		 try{
			 serializer.write(root, lsOut);
		 } catch(Exception e){
			 StringBuffer buffer = new StringBuffer();
			 if(versions != null){
				 buffer.append("Regular - institutional item ids = [ ");
				 for(InstitutionalItemVersion v:versions){
					 buffer.append(v.getVersionedInstitutionalItem().getInstitutionalItem().getId() + ", ");
				 }
				 buffer.append("]");
			 } else if( deletedVersions != null ){
				 buffer.append("Deleted - institutional item ids = [ ");
				 for(DeletedInstitutionalItemVersion v:deletedVersions){
					 buffer.append(v.getDeletedInstitutionalItem().getInstitutionalItemId() + ", ");
				 }
				 buffer.append("]");
			 } else {
				 buffer.append("Not regular or deleted?");
			 }
			 errorEmailService.sendError(buffer.toString());
			 errorEmailService.sendError(e);
			 errorEmailService.sendError(lsOut.toString());
		 }
		 
		 return stringWriter.getBuffer().toString();
	}
	
	
	/**
	 * A request to list identifiers with only the metadata prefix.
	 * 
	 * @param metadataPrefix
	 * @return the last id processed
	 */
	private void addRecords(List<InstitutionalItemVersion> versions, Document doc, 
			OaiMetadataProvider metadataProvider, 
			DefaultResumptionToken resumptionToken )
	{
		 Element root = doc.getDocumentElement();	
		 for(InstitutionalItemVersion version : versions)
		 {
			 Element record = doc.createElement("record");
			 root.appendChild(record);
			 metadataProvider.addXml(record, version);
		 }
		 
	}
	
	/**
	 * A request to list deleted records.
	 * 
	 * @param versions - 
	 * @param doc
	 * @param metadataProvider
	 * @return
	 */
	private void addDeletedRecords(List<DeletedInstitutionalItemVersion> versions, Document doc, 
			OaiMetadataProvider metadataProvider, DefaultResumptionToken resumptionToken)
	{
		 Element root = doc.getDocumentElement();	
		 for(DeletedInstitutionalItemVersion version : versions)
		 {
			 Element record = doc.createElement("record");
			 root.appendChild(record);
			 metadataProvider.addXml(record, version);
		 }
	}

	public DeletedInstitutionalItemVersionService getDeletedInstitutionalItemVersionService() {
		return deletedInstitutionalItemVersionService;
	}

	public void setDeletedInstitutionalItemVersionService(
			DeletedInstitutionalItemVersionService deletedInstitutionalItemVersionService) {
		this.deletedInstitutionalItemVersionService = deletedInstitutionalItemVersionService;
	}
	
	/**
	 * Initialize the resumption token and determine if this is the initial request.
	 * 
	 * @param metadataPrefix
	 * @param set
	 * @param from
	 * @param until
	 * @param strResumptionToken
	 * 
	 * @return true if this is the initial request.
	 * 
	 * @throws BadArgumentException
	 * @throws BadResumptionTokenException
	 */
	private DefaultResumptionToken initilizeResumptionToken(String metadataPrefix, String set,
			String from, String until, String strResumptionToken) throws BadArgumentException, BadResumptionTokenException
	{
		boolean initialRequest = false;
		DefaultResumptionToken resumptionToken = null;
		// parse the token if it exists
		if(strResumptionToken != null && !strResumptionToken.equals(""))
		{
			resumptionToken = new DefaultResumptionToken(strResumptionToken);
			resumptionToken.setInitialRequest(initialRequest);
		}
		else
		{
			
			resumptionToken = new DefaultResumptionToken();
			
			resumptionToken.setBatchSize(batchSize);
			if( from != null && !from.equals(""))
			{
				resumptionToken.setFrom(from);
			}
			if( set != null && !set.equals(""))
			{
				resumptionToken.setSet(set);
			}
			if( until != null && !until.equals(""))
			{
				resumptionToken.setUntil(until);
			}
			resumptionToken.setMetadataPrefix(metadataPrefix);
			resumptionToken.setLastId(0l);
		}
		
		if( resumptionToken.getFrom() != null && resumptionToken.getUntil() != null )
		{
		    if( resumptionToken.getFrom().after(resumptionToken.getUntil()))
		    {
		        throw new BadArgumentException("from date greater than until date");
		    }
		    resumptionToken.checkFromUntilDates();
		}
		
		
		if( resumptionToken.getMetadataPrefix() == null || resumptionToken.getMetadataPrefix().equals(""))
		{
			throw new BadArgumentException("missing metadata prefix");
		}
		
		return resumptionToken;
	}
	
	public ErrorEmailService getErrorEmailService() {
		return errorEmailService;
	}

	public void setErrorEmailService(ErrorEmailService errorEmailService) {
		this.errorEmailService = errorEmailService;
	}

  
}
