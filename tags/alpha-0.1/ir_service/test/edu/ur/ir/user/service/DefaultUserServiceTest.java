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


package edu.ur.ir.user.service;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.springframework.context.ApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.testng.annotations.Test;

import edu.ur.ir.repository.RepositoryService;
import edu.ur.ir.repository.service.test.helper.ContextHolder;
import edu.ur.ir.repository.service.test.helper.PropertiesLoader;
import edu.ur.ir.user.IrUser;
import edu.ur.ir.user.UserDeletedPublicationException;
import edu.ur.ir.user.UserEmail;
import edu.ur.ir.user.UserHasPublishedDeleteException;
import edu.ur.ir.user.UserService;

/**
 * Test the service methods for the default user services.
 * 
 * @author Nathan Sarr
 *
 */
@Test(groups = { "baseTests" }, enabled = true)
public class DefaultUserServiceTest {

	/** Application context  for loading information*/
	ApplicationContext ctx = ContextHolder.getApplicationContext();

	/** User data access */
	UserService userService = (UserService) ctx.getBean("userService");
	
	
	/** Repository data access */
	RepositoryService repositoryService = (RepositoryService) ctx.getBean("repositoryService");

	/** Platform transaction manager  */
	PlatformTransactionManager tm = (PlatformTransactionManager)ctx.getBean("transactionManager");
	
	/** Transaction definition */
	TransactionDefinition td = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRED);
	
	
	/** Properties file with testing specific information. */
	PropertiesLoader propertiesLoader = new PropertiesLoader();
	
	/** Get the properties file  */
	Properties properties = propertiesLoader.getProperties();
	
	/**
	 * Test creating a user
	 * @throws UserHasPublishedDeleteException 
	 */
	public void createUserTest() throws UserHasPublishedDeleteException, UserDeletedPublicationException
	{
 	    // Start the transaction this is for lazy loading
		TransactionStatus ts = tm.getTransaction(td);
		UserEmail email = new UserEmail("email");

		UserEmail email2 = new UserEmail("email2");

		IrUser user = userService.createUser("password","username",email);
		IrUser user2 = userService.createUser("password2", "username2", email2);
		tm.commit(ts);
		
		// make sure the users exist
		ts = tm.getTransaction(td);
		IrUser otherUser = userService.getUser(user.getId(), false);
		assert otherUser.equals(user) : "Users " + otherUser + 
		" should be the same as " + user;
       
		IrUser otherUser2 = userService.getUser(user.getUsername());
		
		assert otherUser.equals(otherUser2):
			"other user " + otherUser + " should equal " + otherUser2;
		tm.commit(ts);

		
        // start a new transaction
		ts = tm.getTransaction(td);
		
		List<Long> userIds = new LinkedList<Long>();
		userIds.add(user.getId());
		userIds.add(user2.getId());
		
		List<IrUser> users = userService.getUsers(userIds);
		assert users.size() == 2 : "should find 2 users but found " + users.size();
		
		IrUser myUser1 = userService.getUser(user.getId(), false);
		IrUser myUser2 = userService.getUser(user2.getId(), false);
		
		userService.deleteUser(myUser1);
		userService.deleteUser(myUser2);
		
		tm.commit(ts);
		
		assert userService.getUser(myUser1.getId(), false) == null :
			"should not be able to find other user " + myUser1;
		
		assert userService.getUser(myUser2.getId(), false) == null :
			"should not be able to find other user " + myUser2;
		
	}
	
	public void testHandleForgotPassword() throws UserHasPublishedDeleteException, UserDeletedPublicationException { 
		// determine if we should be sending emails 
		boolean sendEmail = new Boolean(properties.getProperty("send_emails")).booleanValue();

		// Start the transaction 
		TransactionStatus ts = tm.getTransaction(td);
		UserEmail email = new UserEmail("email");
		IrUser user = userService.createUser("password", "username", email);
		tm.commit(ts);
		
		ts = tm.getTransaction(td);
		String token = userService.savePasswordToken(user);
		if (sendEmail) {
			userService.sendEmailForForgotPassword(token, properties.getProperty("user_2_email"));
		}
		
		IrUser otherUser = userService.getUser("username");
		
		assert otherUser.getPasswordToken() != null : "Password token should exist";
		tm.commit(ts);
		
		// Start new transaction
		ts = tm.getTransaction(td);
		userService.deleteUser(userService.getUser(user.getId(), false));
		tm.commit(ts);
		
		assert userService.getUser(user.getId(), false) == null : "User should be null";
	}
	



}
