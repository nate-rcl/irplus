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

package edu.ur.ir.item;

import edu.ur.ir.person.Contributor;

/**
 * Exception to be thrown when a duplicate contributor is added to 
 * an item.
 * 
 * @author Nathan Sarr
 *
 */
public class DuplicateContributorException extends Exception {
	
	/** Eclipse generated id */
	private static final long serialVersionUID = 9182332479588403694L;
	
	/** Contributor that was duplicated */
	private Contributor contributor;
	
	/**
	 * Message and the name that was duplicated.
	 * 
	 * @param message
	 * @param name
	 */
	public DuplicateContributorException(String message, Contributor c)
	{
		super(message);
		this.contributor = c;
	}

	public Contributor getContributor() {
		return contributor;
	}

}
