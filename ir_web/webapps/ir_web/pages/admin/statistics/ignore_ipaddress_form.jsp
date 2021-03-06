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

<!--  form fragment for dealing with editing ipaddress
      this form will return with error messages in it if there
      is an issue.
 -->
<%@ taglib prefix="ir" uri="ir-tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

		<!--  represents a successful submission -->
		<input type="hidden" id="newIgnoreIpAddressForm_success" 
		       value="${added}"/>
		       
		<!--  if editing an id must be passed -->     
	    <input type="hidden" id="newIgnoreIpAddressForm_id"
		        name="id" value="${ignoreIpAddress.id}"/>
		               
	    <input type="hidden" id="newIgnoreIpAddressForm_new"
		        name="newIgnoreIpAddress" value="true"/>
		  
		<div id="ignoreIpAddressError">           
		    <!--  get the error messages from fieldErrors -->
			<p class="errorMessage"><ir:printError errors="${fieldErrors}" 
			key="ignoreIpAddress.name"/></p>
			<p class="errorMessage"><ir:printError errors="${fieldErrors}" 
			key="ignoreIpAddressAlreadyExists"/></p>    
		</div>

	    <table class="formTable">    
		    <tr>       
	            <td align="left" class="label">Name:*</td>
	            <td align="left" class="input"><input type="text" 
			    id="newIgnoreIpAddressForm_name" 
			    name="ignoreIpAddress.name" 
			    value="${ignoreIpAddress.name}" size="45"/> </td>
			</tr>
			<tr>       
	            <td align="left" class="label">Store counts:</td>
	            <td align="left" class="input">
	                <input type="checkbox"  id="newIgnoreIpAddressForm_storeCounts" name="storeCounts" value="true" 
	                <c:if test="${ignoreIpAddress.storeCounts == true}"> 
	              	    checked = "true"
	                </c:if> 
	                />
	            </td>
			</tr>
			<tr>
			    <td align="left" class="label">Description:</td>
			    <td align="left" colspan="2" class="input"> <textarea name="ignoreIpAddress.description" 
	                  id="newIgnoreIpAddressForm_description"  cols="42" rows="4">${ignoreIpAddress.description}</textarea>
	            </td>
			</tr>
			<tr>
			    <td align="left" class="label">Ignore Ip Address:</td>
			    <td align="left" colspan="2" class="input"> 
				    From
				    <input type="text" 
				    id="newIgnoreIpAddressForm_fromAddress1" 
				    name="ignoreIpAddress.fromAddress1" 
				    value="${ignoreIpAddress.fromAddress1}" size="4"/> 
				  
				    <input type="text" 
				    id="newIgnoreIpAddressForm_fromAddress2" 
				    name="ignoreIpAddress.fromAddress2" 
				    value="${ignoreIpAddress.fromAddress2}" size="4"/> 
				   
				    <input type="text" 
				    id="newIgnoreIpAddressForm_fromAddress3" 
				    name="ignoreIpAddress.fromAddress3" 
				    value="${ignoreIpAddress.fromAddress3}" size="4"/> 
				   
				    <input type="text" 
				    id="newIgnoreIpAddressForm_fromAddress4" 
				    name="ignoreIpAddress.fromAddress4" 
				    value="${ignoreIpAddress.fromAddress4}" size="4"/> 
				    
				    To 
				    <input type="text" 
				    id="newIgnoreIpAddressForm_toAddress4" 
				    name="ignoreIpAddress.toAddress4" 
				    value="${ignoreIpAddress.toAddress4}" size="4"/> 
			    </td>
			</tr>			
	    </table>

	   
