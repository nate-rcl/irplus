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


public interface HtmlPasswordInput extends HtmlBaseInput{
	
	public String getDisabled();

	public void setDisabled(String disabled);

	public String getMaxlength();

	public void setMaxlength(String maxlength);

	public String getSize();

	public void setSize(String size);

	public String getType();

	public void setType(String type);

	public String getValue();

	public void setValue(String value);
	
	public StringBuffer getAttributes();

}
