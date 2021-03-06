<jsp:directive.page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" />


<!--  
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
-->

<!--  form fragment for dealing with editing departments
      this form will return with error messages in it if there
      is an issue.
 -->
<%@ taglib prefix="ir" uri="ir-tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

		<!--  represents a successful submission -->
		<input type="hidden" id="newFileDatabaseFormSuccess"   value="${added}"/>
		       
		<!--  if editing an id must be passed -->     
	    <input type="hidden" id="newFileDatabaseFormId" name="fileDatabaseId" value="${fileDatabase.id}"/>
		
	    <input type="hidden" id="newFileDatabaseFormNew" name="newFileDatabaseVal" value="true"/>
		        
		<div id="error_div">            
	        <!--  get the error messages from fieldErrors -->
		   <p class="errorMessage"><ir:printError errors="${fieldErrors}" 
		    key="fileDatabase.name"/></p>
		    <p class="errorMessage"><ir:printError errors="${fieldErrors}" 
		    key="fileDatabaseAlreadyExists"/></p>
		    <p class="errorMessage"><ir:printError errors="${fieldErrors}" 
		    key="folderLocationAlreadyExists"/></p>  
		</div>
		
	    <table class="formTable">   
	        <tr>       
	            <td align="left" colspan="2" class="label">This will create a folder on the file system with the specified name</td>
			</tr> 
		    <tr>       
	            <td align="left" class="label">Name:*</td>
	            <td align="left" class="input"><input type="text" 
			    id="newFileDatabaseFormName" 
			    name="name" 
			    size="45"
			    value="<c:out value='${fileDatabase.name}'/>"/> </td>
			</tr>
			 <tr>       
	            <td align="left" colspan="2" class="label">Path where to create folder(starts with C:\ for windows and / for unix)</td>
			</tr> 
			<tr>       
	            <td align="left" class="label">Path:*</td>
	            <td align="left" class="input"><input type="text" 
			    id="newFileDatabaseFormPath" 
			    name="path" 
			    size="45"
			    value="<c:out value='${fileDatabase.path}'/>"/> </td>
			</tr>
			<tr>
			    <td align="left" class="label">Description:</td>
			    <td align="left" colspan="2" class="input"> <textarea name="description" 
	                id="newFileDatabaseFormDescription" cols="42" rows="4"><c:out value='${fileDatabase.description}'/></textarea>
	            </td>
			</tr>
	    </table>