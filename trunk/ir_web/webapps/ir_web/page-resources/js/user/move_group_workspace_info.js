/**
 * This code is for dealing with moving files and folders 
 * in the workspace.
 */
YAHOO.namespace("ur.group_workspace.move");

YAHOO.ur.group_workspace.move = 
{
    getMoveFolder : function(destinationId)
    {
        var viewMoveFoldersAction = basePath + 'user/getMoveGroupWorkspaceFolderDestinations.action';
        document.getElementById("destinationId").value = destinationId;
        document.viewChildContentsForMove.action = viewMoveFoldersAction;

	    // handle a successful return
	    var handleSuccess = function(o) 
	    {
			// check for the timeout - forward user to login page if timeout
	        // occurred
	        if( !urUtil.checkTimeOut(o.responseText) )
	        {       	    
	            var response = o.responseText;
	            document.getElementById('move_folder_frag').innerHTML = response;
	        }
	    };
	
	    // handle form submission failure
	    var handleFailure = function(o) 
	    {
	        alert('Get destinations failure '  + o.status + ' status text ' + o.statusText);
	    };

	    YAHOO.util.Connect.setForm('viewChildContentsForMove');
    
        YAHOO.util.Connect.asyncRequest('POST', viewMoveFoldersAction,
          {success: handleSuccess, failure: handleFailure});
    },
    
    moveFolder : function()
    {
    	YAHOO.ur.util.wait.waitDialog.showDialog();
    	// handle a successful return
	    var handleSuccess = function(o) 
	    {
	    	YAHOO.ur.util.wait.waitDialog.hide();
			// check for the timeout - forward user to login page if timeout
	        // occurred
	        if( !urUtil.checkTimeOut(o.responseText) )
	        {       	    
	            var response = o.responseText;
	            document.getElementById('move_folder_frag').innerHTML = response;

	            var success = document.getElementById('action_success').value;
	  
	            if( success != 'true' )
	            {
	                var errorMessage = document.getElementById('move_error').innerHTML;
	                document.getElementById('default_error_dialog_content').innerHTML= errorMessage;
	                YAHOO.ur.folder.move.moveErrorDialog.center();
	                YAHOO.ur.folder.move.moveErrorDialog.show();
 	            }
	            else
	            {
	                var destinationId = document.getElementById('destinationId').value;
	                var groupWorkspaceId = document.getElementById('groupWorkspaceId').value;
	                var getFoldersAction = basePath + 'user/workspace.action'
	        
	                getFoldersAction = getFoldersAction +
	                     '?groupWorkspaceFolderId=' + destinationId + 
	                     '&tabName=GROUP_WORKSPACE' +
	                     '&groupWorkspaceId=' +  groupWorkspaceId +
	                     '&bustcache='+new Date().getTime();
	            
	                window.location = getFoldersAction;
	            }
	        }	
	    };
	
	    // handle form submission failure
	    var handleFailure = function(o) 
	    {
	    	YAHOO.ur.util.wait.waitDialog.hide();
	        alert('Move failure '  + o.status + ' status text ' + o.statusText);
	    };


	    // Wire up the success and failure handlers
	    var callback = { success: handleSuccess, failure: handleFailure };


	    YAHOO.util.Connect.setForm('viewChildContentsForMove');
        var action = basePath + "user/moveGroupWorkspaceFolderInformation.action";
        var cObj = YAHOO.util.Connect.asyncRequest('POST',
                action, callback);
    },
    
    createMoveErrorDialog : function()
    {
        // Define various event handlers for Dialog
	    var handleYes = function() {
		    var contentArea = document.getElementById('default_error_dialog_content');
	        contentArea.innerHTML = ""; 
	       this.hide();
	    };

	    // Instantiate the Dialog
	    YAHOO.ur.folder.move.moveErrorDialog = 
	    new YAHOO.widget.Dialog("error_dialog_box", 
									     { width: "600px",
										   visible: false,
										   modal: true,
										   close: false,										   
										   buttons: [ { text:"Ok", handler:handleYes, isDefault:true } ]
										} );
	
	    YAHOO.ur.folder.move.moveErrorDialog.setHeader("Error");
	
	    // Render the Dialog
	    YAHOO.ur.folder.move.moveErrorDialog.render();
    }, 
    
    init : function()
    {
        YAHOO.ur.folder.move.createMoveErrorDialog();
    }
}

//initialize the code once the dom is ready
YAHOO.util.Event.onDOMReady(YAHOO.ur.group_workspace.move.init);