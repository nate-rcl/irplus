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

import edu.ur.persistent.CommonPersistent;

/**
 * Class for creating extent types
 * 
 * @author Sharmila Ranganathan
 *
 */
public class ExtentType extends CommonPersistent {
	
	// Eclipse gernerated id
	private static final long serialVersionUID = -90727690993732794L;
	
	
	/**
	 * Default constructor
	 */
	public ExtentType(){}
	
	/**
	 * @param name - name of the extent type
	 */
	public ExtentType(String name)
	{
		setName(name);
	}
	
	/**
	 * Create an extent type
	 * @param name - name of the extent type
	 * @param description - description of the extent type
	 */
	public ExtentType(String name, String description)
	{
		setName(name);
		setDescription(description);
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode()
	{
		int value = 0;
		value += name == null ? 0 : name.hashCode();
		return value;
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		StringBuffer sb = new StringBuffer("[Extent type id = ");
		sb.append(id);
		sb.append(" name = ");
		sb.append(name);
		sb.append(" description = ");
		sb.append(description);
		sb.append("]");
		return sb.toString();
	}
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (!(o instanceof ExtentType)) return false;

		final ExtentType other = (ExtentType) o;

		if( ( name != null && !name.equals(other.getName()) ) ||
			( name == null && other.getName() != null ) ) return false;
		
		return true;
	}
	

}
