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


package edu.ur.ir.web.action.user;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.ServletResponseAware;

import com.opensymphony.xwork2.ActionSupport;

import edu.ur.file.db.FileInfo;
import edu.ur.ir.file.FileVersion;
import edu.ur.ir.user.PersonalFile;
import edu.ur.ir.user.IrUser;
import edu.ur.ir.user.UserFileSystemService;
import edu.ur.ir.user.UserService;
import edu.ur.ir.web.util.WebIoUtils;
import edu.ur.ir.web.action.UserIdAware;

/**
 * Allows a personal file to be downloaded.
 * 
 * @author Nathan Sarr
 *
 */
public class DownloadPersonalFile extends ActionSupport 
implements ServletResponseAware, ServletRequestAware, UserIdAware
{

	/** Eclipse generated id. */
	private static final long serialVersionUID = 5430030320610916010L;

	/**  Logger for file upload */
	private static final Logger log = LogManager.getLogger(DownloadPersonalFile.class);
	
	/**  File to download */
	private Long personalFileId;
	
	/** id of the user **/
	private Long userId;
	
	/**  Version of the file to download */
	private int versionNumber;
	
	/** Service for accessing user information */
	private UserService userService;
	
	/** file system managagment services */
	private UserFileSystemService userFileSystemService;
	
	/**  Servlet response to write to */
	private HttpServletResponse response;
	
	/**  Servlet request made */
	private HttpServletRequest request;
	
	/** Utility for streaming files */
	private WebIoUtils webIoUtils;

	/**
     * Allows a file to be downloaded
     *
     * @return {@link #SUCCESS}
     */
    public String execute() throws Exception {
	    log.debug("Trying to download the file to user");
	    
        if (personalFileId == null) {
        	log.debug("File  id is null");
            return "notFound";
        }
        
        PersonalFile personalFile = userFileSystemService.getPersonalFile(personalFileId, false);
        IrUser user = userService.getUser(userId, false);
        
        if( !personalFile.getOwner().equals(user))
        {
        	return "accessDenied";
        }
        
        FileVersion fileVersion = null;
        if( versionNumber != 0)
        {
            fileVersion = personalFile.getVersionedFile().getVersion(versionNumber);
        }
        else
        {
        	fileVersion = personalFile.getVersionedFile().getCurrentVersion();
        }

        FileInfo fileInfo = null;
        if( fileVersion != null)
        {
        	fileInfo = fileVersion.getIrFile().getFileInfo();
        }
        else
        {
        	throw new IllegalStateException( "File version for personal file id = " + personalFileId +
        			" and file version number " + versionNumber + " could not be found");
        }
      
        webIoUtils.StreamFileInfo(fileVersion.getVersionedFile().getName(), fileInfo, response, request, (1024*4), false, true);
        return SUCCESS;
    }
	

	/* (non-Javadoc)
	 * @see com.opensymphony.webwork.interceptor.ServletResponseAware#setServletResponse(javax.servlet.http.HttpServletResponse)
	 */
	public void setServletResponse(HttpServletResponse arg0) {
		this.response = arg0;
	}

	/**
	 * Get the personal file id
	 * 
	 * @return
	 */
	public Long getPersonalFileId() {
		return personalFileId;
	}


	/**
	 * Set the personal file id.
	 * 
	 * @param personalFileId
	 */
	public void setPersonalFileId(Long personalFileId) {
		this.personalFileId = personalFileId;
	}


	/**
	 * Get the version number for the file 
	 * 
	 * @return
	 */
	public int getVersionNumber() {
		return versionNumber;
	}


	/**
	 * Set the version number for the file
	 * 
	 * @param versionNumber
	 */
	public void setVersionNumber(int versionNumber) {
		this.versionNumber = versionNumber;
	}


	/**
	 * User service
	 * 
	 * @return
	 */
	public UserService getUserService() {
		return userService;
	}


	/**
	 * Set the user service
	 * 
	 * @param userService
	 */
	public void setUserService(UserService userService) {
		this.userService = userService;
	}


	/**
	 * Get the user file system service.
	 * 
	 * @return
	 */
	public UserFileSystemService getUserFileSystemService() {
		return userFileSystemService;
	}

	/** id of the user */
	public void setUserId(Long userId)
	{
		this.userId = userId;
	}

	/**
	 * Set the user file system service.
	 * 
	 * @param userFileSystemService
	 */
	public void setUserFileSystemService(UserFileSystemService userFileSystemService) {
		this.userFileSystemService = userFileSystemService;
	}

	public void setServletRequest(HttpServletRequest request) {
		this.request = request;
	}

	public WebIoUtils getWebIoUtils() {
		return webIoUtils;
	}


	public void setWebIoUtils(WebIoUtils webIoUtils) {
		this.webIoUtils = webIoUtils;
	}

}
