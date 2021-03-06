/**  
   Copyright 2008-2011 University of Rochester

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


package edu.ur.hibernate.metadata.marc;

import org.springframework.context.ApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.testng.annotations.Test;

import edu.ur.hibernate.metadata.helper.test.ContextHolder;
import edu.ur.metadata.marc.MarcDataField;
import edu.ur.metadata.marc.MarcDataFieldDAO;

/**
 * Test Marc data field persistence
 * 
 * @author Nathan Sarr
 *
 */
public class MarcDataFieldDAOTest {
	
    /** spring application context manager  */
    ApplicationContext ctx = ContextHolder.getApplicationContext();
    
    /** dublin core element data access object */
    MarcDataFieldDAO marcDataFieldDAO = (MarcDataFieldDAO)ctx.getBean("marcDataFieldDAO");
    
    /** platform transaction manager  */
    PlatformTransactionManager tm = (PlatformTransactionManager)ctx.getBean("transactionManager");
    
    /** transaction definition */
    TransactionDefinition td = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRED);
    
	/**
	 * Test marc data field persistence
	 */
	@Test
	public void baseMarcDataFieldDAOTest() throws Exception{

		TransactionStatus ts = tm.getTransaction(td);
		MarcDataField element = new MarcDataField("field", true, "100");
 		element.setDescription("mdDescription");
 		marcDataFieldDAO.makePersistent(element);
 	    tm.commit(ts);
 	    
 	    ts = tm.getTransaction(td);
 		MarcDataField other = marcDataFieldDAO.getById(element.getId(), false);
        assert other.equals(element) : "Marc data fields should be equal mt = " + element + " other = " + other;
         
        MarcDataField dublinCoreElementByName =  marcDataFieldDAO.findByUniqueName(element.getCode());
        assert dublinCoreElementByName.equals(element) : "Marc data field should be found " + element; 
         
        marcDataFieldDAO.makeTransient(other);
        assert  marcDataFieldDAO.getById(other.getId(), false) == null : "Should no longer be able to find marc data field";
	    tm.commit(ts);
	}

}
