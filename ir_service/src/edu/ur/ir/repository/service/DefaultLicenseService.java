package edu.ur.ir.repository.service;

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

import java.util.List;

import edu.ur.ir.repository.LicenseService;
import edu.ur.ir.repository.LicenseVersion;
import edu.ur.ir.repository.LicenseVersionDAO;
import edu.ur.ir.repository.VersionedLicense;
import edu.ur.ir.repository.VersionedLicenseDAO;
import edu.ur.ir.user.IrUser;

/**
 * Default service for dealing with repository licenses.
 * 
 * @author Nathan Sarr
 *
 */
public class DefaultLicenseService implements LicenseService{
	
	/** eclipse generated id */
	private static final long serialVersionUID = 5057651663296196438L;

	/**Class for versioned license persistence */
	private VersionedLicenseDAO versionedLicenseDAO;
	
	/** Class for license version data access  */
	private LicenseVersionDAO licenseVersionDAO;

	
	/**
	 * Create a versioned license.
	 * 
	 * @see edu.ur.ir.repository.LicenseService#createLicense(edu.ur.ir.user.IrUser, java.lang.String, java.lang.String)
	 */
	public VersionedLicense createLicense(IrUser creator, String licenseText,
			String name, String description) {
		
		VersionedLicense versionedLicense = new VersionedLicense(creator, licenseText, name);
		versionedLicense.getCurrentVersion().getLicense().setDescription(description);
		versionedLicenseDAO.makePersistent(versionedLicense);
		return versionedLicense;
	}

	
	/**
	 * Delete the versioned license.
	 * @see edu.ur.ir.repository.LicenseService#delete(edu.ur.ir.repository.VersionedLicense)
	 */
	public void delete(VersionedLicense versionedLicense) {
		versionedLicenseDAO.makeTransient(versionedLicense);
	}

	
	/**
	 * Get the versioned license by id.
	 * @see edu.ur.ir.repository.LicenseService#get(java.lang.Long, boolean)
	 */
	public VersionedLicense get(Long id, boolean lock) {
		return versionedLicenseDAO.getById(id, lock);
	}
	
	/**
	 * Get all license versions 
	 * 
	 * @return the list of all license versions across all versiond licenses
	 */
	@SuppressWarnings("unchecked")
	public List<LicenseVersion> getAllLicenseVersions()
	{
	    return licenseVersionDAO.getAll();	
	}
	
	/**
	 * Get the specified license version.
	 * 
	 * @param id - id of the license version
	 * @param lock - true to lock 
	 * 
	 * @return - the found license version
	 */
	public LicenseVersion getLicenseVersion(Long id, boolean lock)
	{
		return licenseVersionDAO.getById(id, lock);
	}

	
	/**
	 * Get all versioned licenses.
	 * @see edu.ur.ir.repository.LicenseService#getAll()
	 */
	@SuppressWarnings("unchecked")
	public List<VersionedLicense> getAll() {
		return versionedLicenseDAO.getAll();
	}

	
	/**
	 * Save the versioned license to persistent storage.
	 * @see edu.ur.ir.repository.LicenseService#save(edu.ur.ir.repository.VersionedLicense)
	 */
	public void save(VersionedLicense versionedLicense) {
		versionedLicenseDAO.makePersistent(versionedLicense);
	}


	public VersionedLicenseDAO getVersionedLicenseDAO() {
		return versionedLicenseDAO;
	}


	public void setVersionedLicenseDAO(VersionedLicenseDAO versionedLicenseDAO) {
		this.versionedLicenseDAO = versionedLicenseDAO;
	}


	public LicenseVersionDAO getLicenseVersionDAO() {
		return licenseVersionDAO;
	}


	public void setLicenseVersionDAO(LicenseVersionDAO licenseVersionDAO) {
		this.licenseVersionDAO = licenseVersionDAO;
	}

}
