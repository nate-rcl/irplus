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

package edu.ur.ir.user;

import java.util.List;

import edu.ur.dao.CountableDAO;
import edu.ur.dao.CrudDAO;


/**
 * Interface for creating and saving personal file information
 * to a relational database.
 * 
 * @author Nathan Sarr
 *
 */
@SuppressWarnings("unchecked")
public interface PersonalFileDAO extends CountableDAO, 
CrudDAO<PersonalFile>
{

	/**
	 * Get the files for user id and listed file ids.  If the list of fileIds 
	 * is null no files are returned.
	 * 
	 * @param userId
	 * @param fileIds
	 * 
	 * @return the found files
	 */
	public List<PersonalFile> getFiles(Long userId, List<Long> fileIds);

	/**
	 * Get the personal file for given user id and ir file id .
	 * 
	 * @param userId
	 * @param irFileId
	 * 
	 * @return the found files
	 */
	public PersonalFile getFileForUserWithSpecifiedIrFile(Long userId, Long irFileId);

	/**
	 * Get the personal file for given user id and versioned file id .
	 * 
	 * @param userId
	 * @param versionedFileId
	 * 
	 * @return the found files
	 */
	public PersonalFile getFileForUserWithSpecifiedVersionedFile(Long userId, Long versionedFileId);

	/**
	 * Get the files for user id and folder id .
	 * 
	 * @param userId
	 * @param folderId
	 * 
	 * @return the found files or empty list if no files are found
	 */
	public List<PersonalFile> getFilesInFolderForUser(Long userId, Long folderId);
	
	
	/**
	 * Get the root files 
	 * 
	 * @param userId
	 * 
	 * @return the found files
	 */
	public List<PersonalFile> getRootFiles(Long userId);
	
	/**
	 * Get the files with specified ir file id .
	 * 
	 * @param irFileId
	 * 
	 * @return the found files
	 */
	public Long getFileWithSpecifiedIrFile(Long irFileId);
}
