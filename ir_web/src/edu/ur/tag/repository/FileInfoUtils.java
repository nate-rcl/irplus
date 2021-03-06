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


package edu.ur.tag.repository;


import java.io.File;

import edu.ur.file.db.FileInfo;

/**
 * Utils for file info information
 * 
 * @author Nathan Sarr
 *
 */
public class FileInfoUtils {
	
	/**
	 * Return the size of the file on disk 
	 * 
	 * @param fileInfo
	 * @return size of file in bytes
	 */
	public static Long sizeOnDisk(FileInfo fileInfo)
	{
		Long size = 0l;
		File f = new File(fileInfo.getFullPath());
		if( f != null  && f.canRead())
		{
			size = f.length();
		}
	    return size;	
	}

}
