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

import edu.ur.tag.html.attribute.HtmlCoreAttributes;
import edu.ur.tag.html.attribute.HtmlLanguageAttributes;
import edu.ur.tag.html.event.HtmlKeyboardEvents;
import edu.ur.tag.html.event.HtmlMouseEvents;

public interface HtmlAnchor extends HtmlCoreAttributes, HtmlKeyboardEvents, HtmlMouseEvents, 
HtmlLanguageAttributes {
	
	public String getCharset();
	
	public void setCharset(String charset);
	
	public String getCoords();
	
	public void setCoords(String coords);
	
	public String getHref();
	
	public void setHref(String href);
	
	public String getHreflang();
	
	public void setHreflang(String hreflang);
	
	public String getName();
	
	public void setName(String name);
	
	public String getRel();
	
	public void setRel(String rel);
	
	public String getRev();
	
	public void setRev(String rev);
	
	public String getShape();
	
	public void setShape(String shape);
	
	public String getTarget();
	
	public void setTarget(String target);
	
	public String getType();
	
	public void setType(String type);
	
	public StringBuffer getAttributes();


}
