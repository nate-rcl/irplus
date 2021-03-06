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

package edu.ur.ir.security;

/**
 * This is thrown to indicate that the user did not have the permissions to perform
 * a given action.
 * 
 * @author Nathan Sarr
 *
 */
public class PermissionNotGrantedException extends Exception{

	/** eclipse generated exception */
	private static final long serialVersionUID = 4046900011589257184L;
	
	/**
	 * Constructor 
	 * 
	 * @param permission - permission needed to perform the given action.
	 */
	public PermissionNotGrantedException(String permission)
	{
		super("Permission " + permission + " not granted");
	}

}
