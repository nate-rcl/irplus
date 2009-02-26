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

<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="ur" uri="ur-tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<!--  document type -->
<c:import url="/inc/doctype-frag.jsp"/>
          
<fmt:setBundle basename="messages"/>
<html>

<head>
    <title>News</title>
    
    <!-- Medatadata fragment for page cache -->
    <c:import url="/inc/meta-frag.jsp"/>

    <!-- Core + Skin CSS -->
    <ur:styleSheet href="page-resources/yui/reset-fonts-grids/reset-fonts-grids.css"/>
    <ur:styleSheet href="page-resources/yui/assets/skins/sam/skin.css"/>
    <ur:styleSheet href="page-resources/css/base-ur.css"/>

    <ur:styleSheet href="page-resources/css/main_menu.css"/>
    <ur:styleSheet href="page-resources/css/global.css"/>
    <ur:styleSheet href="page-resources/css/tables.css"/>
    
    <!--  Style for dialog boxes -->
    <ur:js src="page-resources/yui/utilities/utilities.js"/>
    <ur:js src="page-resources/yui/button/button-min.js"/>
    <ur:js src="page-resources/yui/container/container-min.js"/>
 	<ur:js src="page-resources/yui/menu/menu-min.js"/>
 	 	
 	<ur:js src="pages/js/base_path.js"/>
 	<ur:js src="page-resources/js/util/ur_util.js"/>
 	<ur:js src="page-resources/js/menu/main_menu.js"/>
    <ur:js src="pages/js/ur_table.js"/>
    <ur:js src="page-resources/js/admin/news.js"/>
    
     
</head>

<body class=" yui-skin-sam">

    <!--  yahoo doc 2 template creates a page 950 pixles wide -->
    <div id="doc2">
    
        <!--  this is the header of the page -->
        <c:import url="/inc/header.jsp"/>
      
        <h3>Edit News</h3>
  
        <div id="bd">
            
	        <table>
	            <tr>
	                <td>
		                <button id="showNewsItem" 
		                        class="ur_button" 
 		                        onmouseover="this.className='ur_buttonover';"
 		                        onmouseout="this.className='ur_button';">New News Item</button> 
	                </td>
	                <td>
	                    <button id="showDeleteNewsItem" 
	                            class="ur_button" 
 		                        onmouseover="this.className='ur_buttonover';"
 		                        onmouseout="this.className='ur_button';">Delete</button>
	               </td>
	            </tr>
	        </table>
	        <br/>
	      
	        <ur:div id="newNewsItems"></ur:div>
	      

	      
      </div>
      <!--  end body div -->

      <!--  this is the footer of the page -->
      <c:import url="/inc/footer.jsp"/>
  </div>
  <!--  End  doc div-->
     
	        <ur:div id="newNewsItemDialog" >
	            <ur:div cssClass="hd">News Information</ur:div>
		        <ur:div cssClass="bd">
		            <ur:basicForm id="addNewsItem" name="newNewsItemForm" 
		                 method="post" 
		                 action="user/addNewsItem.action">
		              
		                 <input type="hidden" id="newNewsItemForm_id"
		                        name="id" value=""/>
		               
		                 <input type="hidden" id="newNewsItemForm_new"
		                        name="newNewsItem" value="true"/>
		              
		                 <ur:div id="newsItemError" cssClass="errorMessage"></ur:div>
			          
			             <table class="formTable">    
		                     <tr>       
	                            <td align="left" class="label">Name:*</td>
	                            <td align="left" class="input"><input type="text"
	                                size="45" 
			                        id="newNewsItemForm_name" 
			                        name="newsItem.name" 
			                        value=""/>
			                    </td>
			                 </tr>
			             </table>
		             </ur:basicForm>
		         </ur:div>
	         </ur:div>
	        
	      
	         <ur:div id="deleteNewsItemDialog">
	             <ur:div cssClass="hd">Delete News Items</ur:div>
		         <ur:div cssClass="bd">
		             <ur:basicForm id="deleteNewsItem" 
		                  name="deleteNewsItem" method="POST" 
		                  action="user/deleteNewsItem.action">
		              
		              
		              <ur:div id="deleteNewsItemError" cssClass="errorMessage"></ur:div>
			          <p>Are you sure you wish to delete the selected news item?</p>
		          </ur:basicForm>
		      </ur:div>
	      </ur:div>

</body>
</html>