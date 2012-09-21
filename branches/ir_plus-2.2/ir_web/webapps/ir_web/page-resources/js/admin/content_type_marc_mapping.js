/*
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

YAHOO.namespace("ur.marc.contentType");

// global delete id
var deleteId;
var deleteAction = basePath + 'admin/deleteContentTypeMarcMapping.action';

/**
 * copyright statement name space
 */
YAHOO.ur.marc.contentTypeMapping = {
		
		/**
		 * Creates a mac content type
		 *
		 */
		createDeleteDialog : function()
		{
		    
			// redirect with delete
			var handleSubmit = function() {
			     window.location = deleteAction + '?id=' + deleteId;
			};
			
				
			// handle a cancel of deleting copyrigght statement dialog
			var handleCancel = function() {
				YAHOO.ur.marc.contentTypeMapping.deleteDialog.hide();
			};
			
			
			// Instantiate the Dialog
			// make it modal - 
			// it should not start out as visible - it should not be shown until 
			// new copyrigght statement button is clicked.
			YAHOO.ur.marc.contentTypeMapping.deleteDialog = new YAHOO.widget.Dialog('deleteDialog', 
		        { width : "500px",
				  visible : false, 
				  modal : true,
				  buttons : [ { text:'Yes', handler:handleSubmit, isDefault:true },
							  { text:'No', handler:handleCancel } ]
				} );
		     
		    //center and show the delete dialog
			YAHOO.ur.marc.contentTypeMapping.deleteDialog.showDialog = function()
		    {
				YAHOO.ur.marc.contentTypeMapping.deleteDialog.center();
				YAHOO.ur.marc.contentTypeMapping.deleteDialog.show();
		    }
		   
					
			// Render the Dialog
			YAHOO.ur.marc.contentTypeMapping.deleteDialog.render();
		
		},
		
		/**
		 * Sets up the form and the delete
		 */
		deleteMapping : function(id)
		{
		    deleteId = id;	
		    YAHOO.ur.marc.contentTypeMapping.deleteDialog.showDialog();
		},
		
		// initialize the page
		// this is called once the dom has
		// been created
		init : function() 
		{
	        YAHOO.ur.marc.contentTypeMapping.createDeleteDialog();
		}	
}
//initialize the code once the dom is ready
YAHOO.util.Event.onDOMReady(YAHOO.ur.marc.contentTypeMapping.init);
