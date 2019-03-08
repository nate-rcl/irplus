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


package edu.ur.ir.repository.service;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.ur.file.db.FileDatabase;
import edu.ur.file.db.FileInfo;
import edu.ur.file.db.FileServerService;
import edu.ur.file.db.FolderInfo;
import edu.ur.file.db.LocationAlreadyExistsException;
import edu.ur.file.db.UniqueNameGenerator;
import edu.ur.file.IllegalFileSystemNameException;
import edu.ur.ir.file.FileVersion;
import edu.ur.ir.file.FileVersionDAO;
import edu.ur.ir.file.IrFile;
import edu.ur.ir.file.IrFileDAO;
import edu.ur.ir.file.TransformedFile;
import edu.ur.ir.file.TransformedFileDAO;
import edu.ur.ir.file.TransformedFileType;
import edu.ur.ir.file.TransformedFileTypeDAO;
import edu.ur.ir.file.VersionedFile;
import edu.ur.ir.file.VersionedFileDAO;
import edu.ur.ir.item.GenericItem;
import edu.ur.ir.item.ItemService;
import edu.ur.ir.repository.LicenseVersion;
import edu.ur.ir.repository.Repository;
import edu.ur.ir.repository.RepositoryDAO;
import edu.ur.ir.repository.RepositoryService;
import edu.ur.ir.repository.VersionedFileLockStrategy;
import edu.ur.ir.repository.VersionedFileUnLockStrategy;
import edu.ur.ir.researcher.ResearcherFileSystemService;
import edu.ur.ir.user.IrUser;


/**
 * Service class for dealing with files saved within the 
 * repository and storing information within it.
 * 
 * @author Nathan Sarr
 *
 */
public class DefaultRepositoryService implements RepositoryService {
	
	/** eclipse generated id */
	private static final long serialVersionUID = 7265577479425797457L;

	/**  File service for dealing with underlying file storage. */
	private FileServerService fileServerService;
	
	/**  Data access for versioned files  */
	private VersionedFileDAO versionedFileDAO;
	
	/** relational data access for ir file information  */
	private IrFileDAO irFileDAO;
	
	/** relational data access for file version information  */
	private FileVersionDAO fileVersionDAO;	
	
	/** Transformed file information */
	private TransformedFileDAO transformedFileDAO;
	
	/** Transformed file type data access  */
	private TransformedFileTypeDAO transformedFileTypeDAO;
	
	/**  Item service  */
	private ItemService itemService;
	
	/** Services for dealing with researcher file system information */
	private ResearcherFileSystemService researcherFileSystemService;
	
	/**  Data access for a repository  */
	private RepositoryDAO repositoryDAO;
		
	/** generates unique names for the files and folders */
	private UniqueNameGenerator uniqueNameGenerator;
	
	/**  Get the logger for this class */
	private static final Logger log = LogManager.getLogger(DefaultRepositoryService.class);
	
	/** Strategy for locking files  */
	private VersionedFileLockStrategy versionedFileLockStrategy;
	
	/** Strategy for unlocking files */
	private VersionedFileUnLockStrategy versionedFileUnLockStrategy;
	
	/** determine if the external authentication is enabled */
	private boolean externalAuthenticationEnabled = false;
	
	
	
	/**
	 * Create a versioned file in the repository.
	 * 
	 * @param repositoryId - id of the repository to create the versioned file in.
	 * @param f - file to add
	 * @param fileName - name user wishes to give the file.
	 * @param descrption - description of the file.
	 * @param original file name - original file name used to upload this file
	 * and the user wishes to give the file a name that is different than the one on the file system
	 * 
	 * @return - the created versioned file.
	 * @throws edu.ur.file.IllegalFileSystemNameException 
	 */
	public VersionedFile createVersionedFile(
			IrUser user,
			Repository repository, 
			File f, 
			String fileName,
			String description) throws IllegalFileSystemNameException
	{

		FileInfo info = createFileInfo(repository, f, fileName, description);
		
		VersionedFile versionedFile = new VersionedFile(user, info, 
				FilenameUtils.removeExtension(fileName));
		versionedFile.setDescription(description);
		versionedFileDAO.makePersistent(versionedFile);
		
		return versionedFile;
	}
	
	/**
	 * Find a transformed file type by its system code.
	 * 
	 * @see edu.ur.ir.repository.RepositoryService#getTransformedFileTypeBySystemCode(java.lang.String)
	 */
	public TransformedFileType getTransformedFileTypeBySystemCode(String systemCode) {
		return transformedFileTypeDAO.findBySystemCode(systemCode);
	}
	
	
	/**
	 * Create a versioned file in the repository which is a file that is emtpy.
	 * 
	 * @param repositoryId - id of the repository to create the versioned file in.
	 * @param fileName - name user wishes to give the file.
	 * @param descrption - description of the file.
	 * @param original file name - original file name used to upload this file
	 * and the user wishes to give the file a name that is different than the one on the file system
	 * 
	 * @return - the created versioned file.
	 * @throws edu.ur.file.IllegalFileSystemNameException 
	 */
	public VersionedFile createVersionedFile(
			IrUser user,
			Repository repository, 
			String fileName, 
			String description) throws IllegalFileSystemNameException
	{
		FileInfo info = createFileInfo(repository, fileName);
		
		VersionedFile versionedFile = new VersionedFile(user, info, 
				FilenameUtils.removeExtension(fileName));
		versionedFile.setDescription(description);
		versionedFileDAO.makePersistent(versionedFile);
		
		return versionedFile;
	}

	
	/**
	 * Delete the versioned file from the system.  This permanently 
	 * deletes the versioned file information.
	 * 
	 * 
	 * @param versioned file to delete
	 */
	public void deleteVersionedFile(VersionedFile versionedFile)
	{
		LinkedList<IrFile> files = new LinkedList<IrFile>();
		LinkedList<FileInfo> fileInfos = new LinkedList<FileInfo>();
		Set<FileVersion> versions = versionedFile.getVersions();
		
        //get the ids to remove from the file system.
		for( FileVersion fileVersion : versions)
		{
			IrFile irFile = fileVersion.getIrFile();
			 
			//Check if this IrFile is being used by any Item or researcher.
			//If yes, then do not add the IrFile and FileInfo to the list to be deleted.
			if ((itemService.getItemFileCount(irFile) == 0) && (researcherFileSystemService.getResearcherFileCount(irFile) == 0)) {
				log.debug("Adding Ir file " + irFile);
				files.add(irFile);
				fileInfos.add(irFile.getFileInfo());
				
				// make sure we also remove any transformed files.
				// from the file system and file database
				Set<TransformedFile> transfromedFiles = irFile.getTransformedFiles();
				for( TransformedFile tf : transfromedFiles)
				{
				    FileInfo transformedFileInfo = tf.getTransformedFile();	
				    log.debug("Add transformed file id " + transformedFileInfo);
				    fileInfos.add(transformedFileInfo);
				}
			} 
		}
	
		versionedFileDAO.makeTransient(versionedFile);
		
		// Delete Irfiles
		for(IrFile file : files)
		{
			irFileDAO.makeTransient(file);
		}
		
		// Delete FileInfos	
		for(FileInfo fileInfo : fileInfos)
		{
			fileServerService.deleteFile(fileInfo);
		}
		
		
	}
	
	/**
	 * Get the versioned file.
	 * 
	 * @param versionedFileId - unique id for the versioned file.
	 * @return the found verioned file or null if it is not found.
	 */
	public VersionedFile getVersionedFile(Long versionedFileId, boolean lock)
	{   
		return versionedFileDAO.getById(versionedFileId, lock);
	}

	/**
	 * Get the file server service used to store files.
	 * 
	 * @return
	 */
	public FileServerService getFileServerService() {
		return fileServerService;
	}

	/**
	 * Set the file server service used to store files.
	 * 
	 * @param fileServerService
	 */
	public void setFileServerService(FileServerService fileServerService) {
		this.fileServerService = fileServerService;
	}

	/**
	 * Get the unique name generator.  This is used for generating names
	 * of files and folders within the system.
	 * 
	 * @return unique name generator
	 */
	public UniqueNameGenerator getUniqueNameGenerator() {
		return uniqueNameGenerator;
	}

	/**
	 * Set the unique name generator.  This is used for generating names
	 * of files and folders within the system.
	 * 
	 * @param uniqueNameGenerator
	 */
	public void setUniqueNameGenerator(UniqueNameGenerator uniqueNameGenerator) {
		this.uniqueNameGenerator = uniqueNameGenerator;
	}


	/**
	 * Add a new file to a versioned file as the next file in the set of versions.
	 * @throws edu.ur.file.IllegalFileSystemNameException 
	 * 
	 * @see edu.ur.ir.repository.RepositoryService#addNewFileToVersionedFile(java.lang.Long, java.lang.Long, java.io.File, java.lang.String)
	 */
	public void addNewFileToVersionedFile(Repository repository, 
			VersionedFile versionedFile, 
			File f, 
			String fileName, 
			IrUser versionCreator) throws edu.ur.file.IllegalFileSystemNameException {
	    
		addNewFileToVersionedFile(repository, versionedFile, f, fileName, null, versionCreator);
		
	}
	
	/**
	 * Add a new file to a versioned file as the next file in the set of versions.
	 * @throws edu.ur.file.IllegalFileSystemNameException 
	 * 
	 * @see edu.ur.ir.repository.RepositoryService#addNewFileToVersionedFile(java.lang.Long, java.lang.Long, java.io.File, java.lang.String)
	 */
	public void addNewFileToVersionedFile(Repository repository, 
			VersionedFile versionedFile, 
			File f, 
			String fileName, 
			String description,
			IrUser versionCreator) throws edu.ur.file.IllegalFileSystemNameException {
	    
		
		// if they can lock it they can edit it
		if( versionedFile.getLockedBy().equals(versionCreator) )
		{
			FileInfo info = createFileInfo(repository, f, fileName, description);
			versionedFile.addNewVersion(info, versionCreator);
		    versionedFileDAO.makePersistent(versionedFile);
		}
		else
		{
			throw new RuntimeException("User does not hold a lock on the file " + versionedFile);
		}
	}

	
	/**
	 * Creates a repository with the specified repository name and specified file database.
	 * 
	 * @param repositoryName - name to give the repository
	 * @param fileDatabase - file database to use to store files for the repository.
	 * 
	 * @return - the created repository.
	 */
	public Repository createRepository(String repositoryName, FileDatabase fileDatabase) {
		Repository repository = new Repository(repositoryName, fileDatabase);
		repositoryDAO.makePersistent(repository);
		return repository;
	}

    /**
     * Get a repository by name.
     * 
     * @param name - name of the repository.
     * @return - the found repository or null if the repository is not found.
     */
	public Repository getRepository(String name) {
		return repositoryDAO.findByUniqueName(name);
	}

    /**
     * Get a repository by id
     * 
     * @param id - unique id of the repository.
     * @param lock - upgrade the lock on the data
     * @return - the found repository or null if the repository is not found.
     */
	public Repository getRepository(Long id, boolean lock) {
		return repositoryDAO.getById(id, lock);
	}

	/**
	 * Repository data access object.
	 * 
	 * @return
	 */
	public RepositoryDAO getRepositoryDAO() {
		return repositoryDAO;
	}

	/**
	 * Repository data access object.
	 * 
	 * @param repositoryDAO
	 */
	public void setRepositoryDAO(RepositoryDAO repositoryDAO) {
		this.repositoryDAO = repositoryDAO;
	}

	/**
	 * Delete the repository from relational database.
	 * 
	 * @see edu.ur.ir.repository.RepositoryService#deleteRepository(edu.ur.ir.repository.Repository)
	 */
	public void deleteRepository(Repository repository) {
		//delete the index folders if they exist
		if( repository.getInstitutionalItemIndexFolder() != null)
		{
			File f = new File(repository.getInstitutionalItemIndexFolder());
			FileUtils.deleteQuietly(f);
		}
		
		if( repository.getUserIndexFolder() != null)
		{
			File f = new File(repository.getUserIndexFolder());
			FileUtils.deleteQuietly(f);
		}
		
		if( repository.getNameIndexFolder() != null)
		{
			File f = new File(repository.getNameIndexFolder());
			FileUtils.deleteQuietly(f);
		}
		
		if( repository.getResearcherIndexFolder() != null)
		{
			File f = new File(repository.getResearcherIndexFolder());
			FileUtils.deleteQuietly(f);
		}
		
		if( repository.getUserWorkspaceIndexFolder() != null)
		{
			File f = new File(repository.getUserWorkspaceIndexFolder());
			FileUtils.deleteQuietly(f);
		}
		repositoryDAO.makeTransient(repository);
		
	}

	/**
	 * Save the repository information to relational database.
	 * 
	 * @see edu.ur.ir.repository.RepositoryService#saveRepository(edu.ur.ir.repository.Repository)
	 */
	public void saveRepository(Repository repository) {
		repositoryDAO.makePersistent(repository);
	}

	/**
	 * Add a transformed file to the given ir file
	 * @throws edu.ur.file.IllegalFileSystemNameException 
	 * 
	 * @see edu.ur.ir.repository.RepositoryService#addTransformedFile(java.lang.Long, java.io.File)
	 */
	public void addTransformedFile(Repository repository, 
			IrFile irFile, 
			File f, 
			String transformedFileName, 
			String transformedFileExtension, 
			TransformedFileType transformedFileType) throws edu.ur.file.IllegalFileSystemNameException {

        
	    FileInfo info = this.createFileInfo(repository, f, 
	    		transformedFileName + "." + transformedFileExtension, null);
		
		TransformedFile transformedFile = irFile.addTransformedFile(info, transformedFileType);
		transformedFileDAO.makePersistent(transformedFile);
	}


	/**
	 * This locks a versioned file for editing.
	 * 
	 * @param versionedFile - file to lock
	 * @param user - user locking the file
	 * 
	 */
	public boolean canLockVersionedFile(VersionedFile versionedFile, IrUser user) {
		return versionedFileLockStrategy.canLockFile(versionedFile, user);
	}

	/**
	 * Determine if the user can lock the file.  This does not pay attention to
	 * if the file is already locked.  It only indicates that the user would
	 * have permission to unlock the file if the file was unlocked.
	 * 
	 * @param versionedFile - versioned file that is to be checked
	 * @param user - user to check if the file can be unlocked.
	 * 
	 * @return - true if the file can be unlocked by the user.
	 */
	public boolean canUnlockFile(VersionedFile versionedFile, IrUser user) {
		return versionedFileUnLockStrategy.canUnLockFile(versionedFile, user);
	}

	/**
	 * Locks the file so long as the user has the ability to unlock the file.
	 * 
	 * @param versionedFile - file to be unlocked
	 * @param user - user who wants to unlock the file.
	 * 
	 */
	public synchronized boolean lockVersionedFile(VersionedFile versionedFile, IrUser user) {
		
		boolean locked = false;
		if( canLockVersionedFile(versionedFile, user))
		{
			if( !versionedFile.isLocked())
			{
				versionedFile.lock(user);
				versionedFileDAO.makePersistent(versionedFile);
				locked = true;
			}
			else if(versionedFile.getLockedBy().equals(user))
			{
				locked = true;
			}
		}
		
		return locked;
	}
	/**
	 * Returns true if the file can be unlocked by the specified user.
	 * 
	 * @param versionedFile - versioned file to be locked
	 * @param user - user to use to unlock the file
	 * 
	 * @return true if the file is unlocked
	 */
	public synchronized boolean unlockVersionedFile(VersionedFile versionedFile, IrUser user) {
		versionedFile.unLock();
		versionedFileDAO.makePersistent(versionedFile);
		return true;
	}

	/**
	 * Return the versioned file lock strategy.
	 * 
	 * @return
	 */
	public VersionedFileLockStrategy getVersionedFileLockStrategy() {
		return versionedFileLockStrategy;
	}

	/**
	 * Set the versioned file lock strategy.
	 * 
	 * @param versionedFileLockStrategy
	 */
	public void setVersionedFileLockStrategy(
			VersionedFileLockStrategy versionedFileLockStrategy) {
		this.versionedFileLockStrategy = versionedFileLockStrategy;
	}

	/**
	 * Get the versioned file unlocking strategy.
	 * 
	 * @return
	 */
	public VersionedFileUnLockStrategy getVersionedFileUnLockStrategy() {
		return versionedFileUnLockStrategy;
	}

	/**
	 * Set the file unlocking strategy.
	 * 
	 * @param versionedFileUnLockStrategy
	 */
	public void setVersionedFileUnLockStrategy(
			VersionedFileUnLockStrategy versionedFileUnLockStrategy) {
		this.versionedFileUnLockStrategy = versionedFileUnLockStrategy;
	}

	
	/**
	 * Add the picture to the repository.
	 * @throws edu.ur.file.IllegalFileSystemNameException 
	 * 
	 * @see edu.ur.ir.repository.RepositoryService#addRepositoryPicture(java.lang.Long, java.io.File, java.lang.String, java.lang.String)
	 */
	public IrFile addRepositoryPicture(Repository repository, File f, String name, 
			String description) throws edu.ur.file.IllegalFileSystemNameException {
		IrFile irFile = null;
		try {
			 irFile = createIrFile(repository, f, name, description);
		} catch (IllegalFileSystemNameException e) {
			// This Exception will not happen here since the name is extracted from file name 
			// and not entered by user. So just catching and logging and not throwing.
			log.error("The IrFile name contains illegal special characters - " + e.getName());
		}
 		repository.addPicture(irFile);
		repositoryDAO.makePersistent(repository);
		return irFile;
		
	}

	/**
	 * Delete the repository picture from the repository.
	 * 
	 * @see edu.ur.ir.repository.RepositoryService#deleteRepositoryPicture(edu.ur.ir.repository.Repository, edu.ur.ir.file.IrFile)
	 */
	public boolean deleteRepositoryPicture(Repository repository, IrFile irFile) {
		boolean removed = false;
		
		if( repository.removePicture(irFile))
		{
			irFileDAO.makeTransient(irFile);
		}
		repositoryDAO.makePersistent(repository);
		
		return removed;
	}


	/**
	 * Create the file info object in the specified repository.
	 * @throws edu.ur.file.IllegalFileSystemNameException 
	 * 
	 * @see edu.ur.ir.repository.RepositoryService#createFileInfo(edu.ur.ir.repository.Repository, java.io.File, java.lang.String)
	 */
	public FileInfo createFileInfo(Repository repository, File f,
			String fileName) throws edu.ur.file.IllegalFileSystemNameException {
		
		return createFileInfo(repository, f, fileName, null);
	}


	/**
	 * Delete the specified file information object.
	 * 
	 * @see edu.ur.ir.repository.RepositoryService#deleteFileInfo(edu.ur.file.db.FileInfo)
	 */
	public boolean deleteFileInfo(FileInfo fileInfo) {
		return fileServerService.deleteFile(fileInfo);
	}
	
	/**
	 * Add the file to the repository.
	 * @throws edu.ur.file.IllegalFileSystemNameException 
	 * 
	 * @see edu.ur.ir.repository.RepositoryService#addFileInfo(edu.ur.ir.repository.Repository, java.io.File, java.lang.String, java.lang.String, java.lang.String)
	 */
	public FileInfo createFileInfo(Repository repository, 
			File f, 
			String fileName, 
			String description) throws edu.ur.file.IllegalFileSystemNameException 
	{
	    log.debug("respository = " + repository);
	    log.debug("file = " + f);
	    log.debug("fileName = " + fileName);
	    log.debug("description = " + description);
		FileInfo info = fileServerService.addFile(repository.getFileDatabase(), f,
				uniqueNameGenerator.getNextName(), getExtension(fileName), 
				FilenameUtils.removeExtension(fileName));
		info.setDescription(description);
		return info;
	}
	
	/**
	 * Determine the file extension.
	 * 
	 * @param fileName - file name user wants attached
	 * @param originalFileName - original file name
	 * @return - the extension found without the "."
	 */
	private String getExtension( String originalFileName)
	{
		String extension = null;
		if( originalFileName != null)
		{
		    extension = FilenameUtils.getExtension(originalFileName);
		}		
	    return extension;
	}
	
	
	/**
	 * Create an institutional repository file.
	 * @throws edu.ur.file.IllegalFileSystemNameException 
	 * 
	 * @see edu.ur.ir.repository.RepositoryService#createIrFile(edu.ur.ir.repository.Repository, java.io.File, java.lang.String, java.lang.String, java.lang.String)
	 */
	public IrFile createIrFile(Repository repository, 
			File f, 
			String fileName, 
			String description) throws IllegalFileSystemNameException
	{
	    FileInfo info = createFileInfo(repository, f, fileName, description);
		IrFile irFile = new IrFile(info, info.getDisplayName());
		irFileDAO.makePersistent(irFile);
		return irFile;
	}
	
	/**
	 * Delete the specified ir file.  This also deletes the underlying file
	 * information.
	 * 
	 * @see edu.ur.ir.repository.RepositoryService#deleteIrFile(edu.ur.ir.file.IrFile)
	 */
	public boolean deleteIrFile(IrFile irFile) {
	
		LinkedList<FileInfo> filesToDelete = new LinkedList<FileInfo>();
		
		FileInfo fileInfo = irFile.getFileInfo();
	    filesToDelete.add(fileInfo);
		
		Set<TransformedFile> transforms = irFile.getTransformedFiles();
		for(TransformedFile tf : transforms)
		{
			FileInfo info = tf.getTransformedFile();
			filesToDelete.add(info);
		}
		
		irFileDAO.makeTransient(irFile);
		
		for(FileInfo info : filesToDelete)
		{
		    fileServerService.deleteFile(info);
		}
		return true;
	}
	
	/**
	 * Save the ir file.
	 * 
	 * @param irFile
	 */
	public void save(IrFile irFile)
	{
		irFileDAO.makePersistent(irFile);
	}

	
	/**
	 * Create a file information object with an empty file.
	 * @throws edu.ur.file.IllegalFileSystemNameException 
	 * 
	 * @see edu.ur.ir.repository.RepositoryService#createFileInfo(edu.ur.ir.repository.Repository, java.lang.String)
	 */
	public FileInfo createFileInfo(Repository repository, String fileName) throws edu.ur.file.IllegalFileSystemNameException {
		
		String extension = null;
		if( fileName != null )
		{
			extension = FilenameUtils.getExtension(fileName);
		}
		
		return fileServerService.createEmptyFile(repository.getFileDatabase(), 
				uniqueNameGenerator.getNextName(), extension, 
				FilenameUtils.removeExtension(fileName));
	}
	
	/**
	 * Delete the folder information object.
	 * 
	 * @see edu.ur.ir.repository.RepositoryService#deleteFolderInfo(edu.ur.file.db.FolderInfo)
	 */
	public void deleteFolderInfo(FolderInfo folder) {
		fileServerService.deleteFolder(folder);
	}

	/**
	 * Create the folder with the specified name.
	 * @throws LocationAlreadyExistsException - if the folder location already exists
	 * 
	 * @see edu.ur.ir.repository.RepositoryService#createFolderInfo(edu.ur.ir.repository.Repository, java.lang.String)
	 */
	public FolderInfo createFolderInfo(Repository repository, String folderName) throws LocationAlreadyExistsException {
		
		return fileServerService.createFolder(repository.getFileDatabase(), 
				uniqueNameGenerator.getNextName(), folderName);
	
	}

	public ItemService getItemService() {
		return itemService;
	}
	
	/**
	 * Get the ir file with the specified id.
	 * 
	 * @see edu.ur.ir.repository.RepositoryService#getIrFile(java.lang.Long, boolean)
	 */
	public IrFile getIrFile(Long id, boolean lock) {
		return irFileDAO.getById(id, lock);
	}

	/**
	 * Service for dealing with items.
	 * 
	 * @param itemService
	 */
	public void setItemService(ItemService itemService) {
		this.itemService = itemService;
	}

	/**
	 * Get folder by name
	 * 
	 * @param name name of the folder
	 * @param fileDatabaseId Id of  the file database
	 * 
	 * @return
	 */
	public FolderInfo getFolderInfo(String name, Long fileDatabaseId) {
		return fileServerService.getTreeFolderInfoByName(name, fileDatabaseId);
	}

	public VersionedFileDAO getVersionedFileDAO() {
		return versionedFileDAO;
	}

	public void setVersionedFileDAO(VersionedFileDAO versionedFileDAO) {
		this.versionedFileDAO = versionedFileDAO;
	}

	public IrFileDAO getIrFileDAO() {
		return irFileDAO;
	}

	public void setIrFileDAO(IrFileDAO irFileDAO) {
		this.irFileDAO = irFileDAO;
	}

	public TransformedFileDAO getTransformedFileDAO() {
		return transformedFileDAO;
	}

	public void setTransformedFileDAO(TransformedFileDAO transformedFileDAO) {
		this.transformedFileDAO = transformedFileDAO;
	}

	public TransformedFileTypeDAO getTransformedFileTypeDAO() {
		return transformedFileTypeDAO;
	}

	public void setTransformedFileTypeDAO(
			TransformedFileTypeDAO transformedFileTypeDAO) {
		this.transformedFileTypeDAO = transformedFileTypeDAO;
	}
	
	/**
	 * Get the versioned file containing the given IrFile id
	 * 
	 * @param irFile file to get the VersionedFile
	 * 
	 * @return VersionedFile containing the IrFile
	 */
	public VersionedFile getVersionedFileByIrFile(IrFile irFile) {
		return versionedFileDAO.getVersionedFileByIrFileId(irFile.getId());
		
	}
	
	/**
	 * Get the versioned files for given item id
	 * 
	 * @param item item to get the versioned files
	 * 
	 * @return VersionedFiles containing the IrFile
	 */
	public List<VersionedFile> getVersionedFilesForItem(GenericItem item) {
		return versionedFileDAO.getVersionedFilesForItem(item.getId());
		
	}

	/**
	 * Get the file version with the specified id.
	 * 
	 * @see edu.ur.ir.repository.RepositoryService#getFileVersion(java.lang.Long, boolean)
	 */
	public FileVersion getFileVersion(Long id, boolean lock) {
		return fileVersionDAO.getById(id, lock);
	}

	public FileVersionDAO getFileVersionDAO() {
		return fileVersionDAO;
	}

	public void setFileVersionDAO(FileVersionDAO fileVersionDAO) {
		this.fileVersionDAO = fileVersionDAO;
	}

	/**
	 * Get the sum of versioned file size for a user
	 * 
	 * @param user user the VersionedFile belongs to
	 * 
	 * @return sum of versioned files size
	 */
	public Long getFileSystemSizeForUser(IrUser user) {
		return versionedFileDAO.getSumOfVersionedFilesSizeForUser(user.getId());
	}

	
	/**
	 * Get the transfored file by system code
	 * 
	 * @see edu.ur.ir.repository.RepositoryService#getTransformByIrFileSystemCode(java.lang.Long, java.lang.String)
	 */
	public FileInfo getTransformByIrFileSystemCode(Long irFileId,
			String systemCode) {
		return transformedFileDAO.getTransformedFile(irFileId, systemCode);
	}

	
	/**
	 * Get the available list of licenses that can be attached.
	 * 
	 * @see edu.ur.ir.repository.RepositoryService#getAvailableRepositoryLicenses(java.lang.Long)
	 */
	public List<LicenseVersion> getAvailableRepositoryLicenses(Long repositoryId) {
		return repositoryDAO.getAvailableRepositoryLicenses(repositoryId);
	}

	public ResearcherFileSystemService getResearcherFileSystemService() {
		return researcherFileSystemService;
	}

	public void setResearcherFileSystemService(
			ResearcherFileSystemService researcherFileSystemService) {
		this.researcherFileSystemService = researcherFileSystemService;
	}

	/**
	 * Determine if the external authentication is enabled
	 * 
	 * @see edu.ur.ir.repository.RepositoryService#getExternalAuthenticationEnabled()
	 */
	public boolean getExternalAuthenticationEnabled() {
		return externalAuthenticationEnabled;
	}

	/**
	 * Determine if the external authentication is enabled.
	 * 
	 * @see edu.ur.ir.repository.RepositoryService#isExternalAuthenticationEnabled()
	 */
	public boolean isExternalAuthenticationEnabled() {
		return externalAuthenticationEnabled;
	}

	/**
	 * Set the external authentication flag.
	 * 
	 * @param externalAuthenticationEnabled
	 */
	public void setExternalAuthenticationEnabled(
			boolean externalAuthenticationEnabled) {
		this.externalAuthenticationEnabled = externalAuthenticationEnabled;
	}

	
	/**
	 * Get the IrFile by the file info id otherwise return null.
	 * 
	 * @see edu.ur.ir.repository.RepositoryService#getIrFileByFileInfoId(java.lang.Long)
	 */
	public IrFile getIrFileByFileInfoId(Long fileInfoId) {
		return irFileDAO.getByFileInfoId(fileInfoId);
	}
}
