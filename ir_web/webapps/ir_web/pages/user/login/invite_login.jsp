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

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="ur" uri="ur-tags"%>
<%@ taglib prefix="ir" uri="ir-tags"%>

<!--  document type -->
<c:import url="/inc/doctype-frag.jsp"/>

<html>
    <head>
        <title>Invite User(s)</title>
        <c:import url="/inc/meta-frag.jsp"/>
        
        <!-- Core + Skin CSS -->
        <ur:styleSheet href="page-resources/yui/reset-fonts-grids/reset-fonts-grids.css"/>
        <ur:styleSheet href="page-resources/css/base-ur.css"/>
        <ur:styleSheet href="page-resources/yui/menu/assets/skins/sam/menu.css"/>

        <ur:styleSheet href="page-resources/css/global.css"/>
        <ur:styleSheet href="page-resources/css/home_page_content_area.css"/>
        <ur:styleSheet href="page-resources/css/main_menu.css"/>

        <!-- Dependencies --> 
        <ur:js src="page-resources/yui/yahoo-dom-event/yahoo-dom-event.js"/>
        <ur:js src="page-resources/yui/container/container_core-min.js"/>
        <ur:js src="page-resources/yui/menu/menu-min.js"/>

        <!-- Source File -->
        <ur:js src="page-resources/js/menu/main_menu.js"/>
    </head>
    
    <body class="yui-skin-sam">
        <!--  yahoo doc 2 template creates a page 950 pixles wide -->
        <div id="doc2">  

            <!--  this is the header of the page -->
            <c:import url="/inc/header.jsp"/>
            
            <!--  this is the body regin of the page -->
            <div id="bd">
                
                <!--  create the first column -->
                <div class="yui-g">
                <div class="yui-u first">
                    
                   <div class="contentContainer">
                       <div class="contentBoxTitle">
                           <p>Welcome! </p>
                       </div>
                   
                       <div class="contentBoxContent">
                       	  </br>
                       	  <p class="errorMessage"><ir:printError errors="${fieldErrors}" 
		                       key="tokenDoesnotExist"/></p>
                       	  
                       	  <p> Already Registered?
            	          <ur:basicForm id="login" name="alreadyUserLoginForm" method="POST" 
				              action="user/workspace.action?token=${token}">
	        					</br>
	        	               <input type="submit" value="Login"/> 
				          </ur:basicForm>
				          </p>
				          </br>
                          <p>		    New Users
						  <ur:basicForm name="newUserLoginForm" method="POST" 
					              action="viewAddUser.action?token=${token}">
					       		</br>
		                  		<input type="submit" value="Create account"/> 
				          </ur:basicForm>				                                      
				          </p>
                       </div>
                   </div>
                </div>
                <!--  end the first column -->
                 
                 <div class="yui-u">
                    
                    <div class="contentContainer">
                        <div class="contentBoxTitle">
                            <p>You have been invited to collaborate on the following file(s)</p>
                        </div>
                   
                        <div class="contentBoxContent">
                        	</br>
                        	  <c:forEach var="file" items="${inviteInfo.files}">
					     		 <p>  <ir:fileTypeImg cssClass="tableImg" versionedFile="${file}"/> ${file.name} </p> </br>
				     		  </c:forEach>
	                    </div>
	                </div>
                </div>
                <!--  end the second column -->
            
                </div>
                <!--  end the grid -->
                
            </div>
            <!--  end the body tag --> 

            <!--  this is the footer of the page -->
            <c:import url="/inc/footer.jsp"/>
        
        </div>
        <!-- end doc -->
    </body>
</html>

    
