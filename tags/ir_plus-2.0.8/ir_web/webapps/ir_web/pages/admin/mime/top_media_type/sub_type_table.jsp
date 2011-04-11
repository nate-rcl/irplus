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

<%@ taglib prefix="ur" uri="ur-tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="urstb" uri="simple-ur-table-tags"%>

<fmt:setBundle basename="messages"/>

<c:if test="${totalHits > 0}">
	<h3>Viewing: ${rowStart + 1} - ${rowEnd} of ${totalHits}</h3>
	<c:import url="browse_all_sub_types_pager.jsp"/>
</c:if>
 


<div class="dataTable">
	<ur:basicForm method="post" id="subTypes" name="mySubTypes" >
	    <input type="hidden" name="id" value="${id}"/>         
	    <urstb:table width="100%">
	        <urstb:thead>
	            <urstb:tr>
					<urstb:td><ur:checkbox name="checkAllSetting" 
								value="off" onClick="YAHOO.ur.subType.setCheckboxes();"/></urstb:td>         
	                <urstb:td>Id</urstb:td>
             
	                <urstb:tdHeadSort  height="33"
	                    currentSortAction="${sortType}"
	                    ascendingSortAction="javascript:YAHOO.ur.subType.getSubTypes(${rowStart}, ${startPageNumber}, ${currentPageNumber}, 'asc');"
	                    descendingSortAction="javascript:YAHOO.ur.subType.getSubTypes(${rowStart}, ${startPageNumber}, ${currentPageNumber}, 'desc');">
	                    <u>Name</u>                                              
	                    <urstb:thImgSort
	                                 sortAscendingImage="page-resources/images/all-images/bullet_arrow_up.gif"
	                                 sortDescendingImage="page-resources/images/all-images/bullet_arrow_down.gif"/></urstb:tdHeadSort>
					<urstb:td>Description</urstb:td>
	                <urstb:td>Edit</urstb:td>

	            </urstb:tr>
	            </urstb:thead>
	            <urstb:tbody
	                var="subType" 
	                oddRowClass="odd"
	                evenRowClass="even"
	                currentRowClassVar="rowClass"
	                collection="${subTypes}">
	                    <urstb:tr 
	                        cssClass="${rowClass}"
	                        onMouseOver="this.className='highlight'"
	                        onMouseOut="this.className='${rowClass}'">
	                        <urstb:td>
		                        <ur:checkbox name="subTypeIds" value="${subType.id}"/>
	                        </urstb:td>
	                        <urstb:td>
		                        ${subType.id}
	                        </urstb:td>
	                        <urstb:td>
			                   <c:url var="viewSubTypeUrl" value="/admin/initSubTypeExtension.action">
		                           <c:param name="subTypeId" value="${subType.id}"/>
		                       </c:url>
			                   <a href="${viewSubTypeUrl}">${subType.name}</a>
	                        </urstb:td>
	                        <urstb:td>
	                             ${subType.description}
	                        </urstb:td>
	                        <urstb:td>
		                   		<a href="javascript:YAHOO.ur.subType.editSubType(${subType.id});">Edit</a>
	                        </urstb:td>

	                    </urstb:tr>
	            </urstb:tbody>
	        </urstb:table>
		</ur:basicForm>
</div>	

<c:if test="${totalHits > 0}">
	<c:import url="browse_all_sub_types_pager.jsp"/>
</c:if>

