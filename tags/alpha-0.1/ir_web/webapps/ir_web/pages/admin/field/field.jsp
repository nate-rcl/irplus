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
<%@ taglib prefix="ir" uri="ir-tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<!--  document type -->
<c:import url="/inc/doctype-frag.jsp"/>
          
<fmt:setBundle basename="messages"/>
<html>

<head>
    <title>Fields</title>
    
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
    <ur:js src="page-resources/js/admin/field.js"/>
</head>

<body class="yui-skin-sam">

    <!--  yahoo doc 2 template creates a page 950 pixles wide -->
    <div id="doc2">
      
        <!--  this is the header of the page -->
        <c:import url="/inc/header.jsp"/>
      
        <h3>Fields</h3>
  
        <div id="bd">
      
 		<button id="showField" class="ur_button" 
 		        onmouseover="this.className='ur_buttonover';"
 		        onmouseout="this.className='ur_button';">New field</button> 

	    <button id="showDeleteField" class="ur_button" 
 		        onmouseover="this.className='ur_buttonover';"
 		        onmouseout="this.className='ur_button';">Delete</button>

        <br/>
        <br/>
	    <ur:div id="newFields"></ur:div>
	      

	        
      </div>
      <!--  end body div -->
      
      <!--  this is the footer of the page -->
      <c:import url="/inc/footer.jsp"/>
  
  </div>
  <!--  End doc div-->
  
  <ur:div id="newFieldDialog" cssClass="hidden">
      <ur:div cssClass="hd">Field Information</ur:div>
      <ur:div cssClass="bd">
          <ur:basicForm id="addField" name="newFieldForm" 
		                    method="post" 
		                    action="admin/createField.action">
	          <ur:div id="newFieldDialogFields">
	              <c:import url="field_form.jsp"/>
	          </ur:div>
	      </ur:basicForm>
      </ur:div>
  </ur:div>
	         
  <ur:div id="deleteFieldDialog" cssClass="hidden">
      <ur:div cssClass="hd">Delete fields</ur:div>
		<ur:div cssClass="bd">
		    <ur:basicForm id="deleteField" name="deleteField" method="post" 
		                action="admin/deleteField.action">
		       <ur:div id="deleteFieldError" cssClass="errorMessage"></ur:div>
			   <p>Are you sure you wish to delete the selected field(s)?</p>
		    </ur:basicForm>
		</ur:div>
  </ur:div>

</body>
</html>