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

package edu.ur.ir;

import java.io.Serializable;

import edu.ur.persistent.LongPersistentId;
import edu.ur.persistent.PersistentVersioned;
import edu.ur.simple.type.DescriptionAware;
import edu.ur.simple.type.NameAware;

/**
 * Interface to allow display systems to show file and folders 
 * in a unified manner.
 * 
 * @author Nathan Sarr
 *
 */
public interface FileSystem extends LongPersistentId, 
PersistentVersioned, Serializable, DescriptionAware, NameAware{
	
	/**  End separator for paths */
	public static final String PATH_SEPERATOR = "/";
	
	/** Illegal characters in name */
	public static final char[] INVALID_CHARACTERS = {'\\','/', ':', '*', '?', '\"', '<', '>', '|'};
	
	/**
	 * Either a file or a folder.
	 * 
	 * @return
	 */
	public FileSystemType getFileSystemType();
	
	/**
	 * Get the path for the file.
	 * 
	 * @return
	 */
	public String getPath();
	

}