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

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Element;

import edu.ur.ir.institution.InstitutionalItemVersion;
import edu.ur.ir.oai.exception.CannotDisseminateFormatException;
import edu.ur.ir.oai.metadata.provider.OaiMetadataProvider;
import edu.ur.ir.oai.metadata.provider.OaiMetadataServiceProvider;

/**
 * Default Implementation for an oai metadata service provider.
 * 
 * @author Nathan Sarr
 *
 */
public class DefaultOaiMetadataServiceProvider implements OaiMetadataServiceProvider{
    
	/** eclipse generated id */
	private static final long serialVersionUID = 3096747633058885629L;
	
	/** List of oai metadata providers */
	private List<OaiMetadataProvider> providers = new LinkedList<OaiMetadataProvider>();
	
	/** logger */
	private static Logger log = LogManager.getLogger(DefaultOaiMetadataServiceProvider.class);
	
	/**
	 * @see edu.ur.ir.oai.metadata.provider.OaiMetadataServiceProvider#getProvider(java.lang.String)
	 */
	public OaiMetadataProvider getProvider(String metadataPrefix) {
		for( OaiMetadataProvider provider : providers)
		{
			if( provider.supports(metadataPrefix))
			{
				return provider;
			}
		}
		return null;
	}

	/**
	 * Returns an unmodifiable list of providers.
	 * 
	 * @see edu.ur.ir.oai.metadata.provider.OaiMetadataServiceProvider#getProviders()
	 */
	public List<OaiMetadataProvider> getProviders() {
		return Collections.unmodifiableList(providers);
	}

	/**
	 * 
	 * @see edu.ur.ir.oai.metadata.provider.OaiMetadataServiceProvider#getXml(java.lang.String)
	 */
	public void addXml(String metadataPrefix, Element record, InstitutionalItemVersion institutionalItemVersion)
			throws CannotDisseminateFormatException {
		
		if( !supports(metadataPrefix))
		{
			throw new CannotDisseminateFormatException("format " + metadataPrefix + "not supported");
		}
		for( OaiMetadataProvider provider : providers)
		{
			if( provider.supports(metadataPrefix))
			{
				 provider.addXml(record, institutionalItemVersion);
			}
		}
		
	}

	/**
	 * Determine a metadata prefix is supported.
	 * 
	 * @see edu.ur.ir.oai.metadata.provider.OaiMetadataServiceProvider#supports(java.lang.String)
	 */
	public boolean supports(String metadataPrefix) {
		for( OaiMetadataProvider provider : providers)
		{
			log.debug("Checking if provider " + provider + " supports prefix " + metadataPrefix);
			if( provider.supports(metadataPrefix))
			{
				log.debug("Returning true");
				return true;
			}
		}
		return false;
	}

	/**
	 * 
	 * @see edu.ur.ir.oai.metadata.provider.OaiMetadataServiceProvider#addProvider(edu.ur.ir.oai.metadata.provider.OaiMetadataProvider)
	 */
	public void addProvider(OaiMetadataProvider provider) {
		providers.add(provider);
	}

	/**
	 * @see edu.ur.ir.oai.metadata.provider.OaiMetadataServiceProvider#removeProvider(edu.ur.ir.oai.metadata.provider.OaiMetadataProvider)
	 */
	public void removeProvider(OaiMetadataProvider provider) {
		providers.remove(provider);
	}
	
	/**
	 * Set the list of providers.
	 * 
	 * @param providers
	 */
	public void setProviders(List<OaiMetadataProvider> providers) {
		this.providers = providers;
	}

	/**
	 * Return the list of supported format prefixes.
	 * 
	 * @see edu.ur.ir.oai.metadata.provider.OaiMetadataServiceProvider#getSupportedFormats()
	 */
	public Set<String> getSupportedMetadataPrefixes() {
		HashSet<String> formats = new HashSet<String>();
		for(OaiMetadataProvider provider : providers )
		{
			formats.add(provider.getMetadataPrefix());
		}
		
		return formats;
	}
	
}
