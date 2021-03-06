
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

package edu.ur.tag.html;


public interface HtmlButtonInput extends HtmlBaseInput{
	
	public String getDisabled();

	public void setDisabled(String disabled);

	public String getValue();

	public void setValue(String vlaue);

	public String getType();
	
	public StringBuffer getButtonAttributes();

	public void setType(String type);


}
