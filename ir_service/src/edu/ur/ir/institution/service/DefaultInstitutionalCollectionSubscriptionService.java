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

package edu.ur.ir.institution.service;

import java.util.Date;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;


import org.apache.log4j.Logger;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import edu.ur.ir.institution.InstitutionalCollection;
import edu.ur.ir.institution.InstitutionalCollectionSubscription;
import edu.ur.ir.institution.InstitutionalCollectionSubscriptionDAO;
import edu.ur.ir.institution.InstitutionalCollectionSubscriptionService;
import edu.ur.ir.institution.InstitutionalItem;
import edu.ur.ir.institution.InstitutionalItemHandleUrlGenerator;
import edu.ur.ir.institution.InstitutionalItemService;
import edu.ur.ir.user.IrUser;

/**
 * Institutional Collection subscription service implementation.
 * 
 * @author Nathan Sarr
 *
 */
public class DefaultInstitutionalCollectionSubscriptionService implements InstitutionalCollectionSubscriptionService{

    /** Java mail sender to send emails */
    private JavaMailSender mailSender;
    
    /** Service for dealing with institutional items  */
    private InstitutionalItemService institutionalItemService;
    
	/**  Get the logger for this class */
	private static final Logger log = Logger.getLogger(DefaultInstitutionalCollectionSubscriptionService.class);
    
	/**  Used to get the url for a given item */
	private InstitutionalItemHandleUrlGenerator institutionalItemHandleUrlGenerator;

	/**
	 * Institutional collection subscription data access object
	 */
	private InstitutionalCollectionSubscriptionDAO institutionalCollectionSubscriptionDAO;
	
	/**
	 * 
	 * @see edu.ur.ir.institution.InstitutionalCollectionSubscriptionService#delete(edu.ur.ir.institution.InstitutionalCollectionSubscription)
	 */
	public void delete(InstitutionalCollectionSubscription entity) {
		institutionalCollectionSubscriptionDAO.makeTransient(entity);
	}

	/**
	 * @see edu.ur.ir.institution.InstitutionalCollectionSubscriptionService#getAllSubscriptionsForUser(edu.ur.ir.user.IrUser)
	 */
	public List<InstitutionalCollectionSubscription> getAllSubscriptionsForUser(
			IrUser user) {
		return institutionalCollectionSubscriptionDAO.getAllSubscriptionsForUser(user);
	}

	/**
	 * @see edu.ur.ir.institution.InstitutionalCollectionSubscriptionService#getById(java.lang.Long, boolean)
	 */
	public InstitutionalCollectionSubscription getById(Long id, boolean lock) {
		return institutionalCollectionSubscriptionDAO.getById(id, lock);
	}

	/**
	 * @see edu.ur.ir.institution.InstitutionalCollectionSubscriptionService#getSubscriberCount(edu.ur.ir.institution.InstitutionalCollection)
	 */
	public Long getSubscriberCount(
			InstitutionalCollection institutionalCollection) {
		return institutionalCollectionSubscriptionDAO.getSubscriberCount(institutionalCollection);
	}

	/**
	 * @see edu.ur.ir.institution.InstitutionalCollectionSubscriptionService#save(edu.ur.ir.institution.InstitutionalCollectionSubscription)
	 */
	public void save(InstitutionalCollectionSubscription entity) {
		institutionalCollectionSubscriptionDAO.makePersistent(entity);
	}

	public InstitutionalCollectionSubscriptionDAO getInstitutionalCollectionSubscriptionDAO() {
		return institutionalCollectionSubscriptionDAO;
	}

	public void setInstitutionalCollectionSubscriptionDAO(
			InstitutionalCollectionSubscriptionDAO institutionalCollectionSubscriptionDAO) {
		this.institutionalCollectionSubscriptionDAO = institutionalCollectionSubscriptionDAO;
	}

	/**
	 * @see edu.ur.ir.institution.InstitutionalCollectionSubscriptionService#isSubscribed(edu.ur.ir.institution.InstitutionalCollection, edu.ur.ir.user.IrUser)
	 */
	public boolean isSubscribed(InstitutionalCollection collection, IrUser user) {
		return institutionalCollectionSubscriptionDAO.isSubscriberCount(collection, user) == 1l;
	}

	
	/**
	 * @see edu.ur.ir.institution.InstitutionalCollectionSubscriptionService#sendSubriberEmail(edu.ur.ir.user.IrUser)
	 */
	public void sendSubscriberEmail(IrUser user, Date startDate, Date endDate) throws MessagingException
	{	
		boolean sendEmail = true;
		StringBuffer emailText = new StringBuffer("test");
		
		List<InstitutionalCollectionSubscription> subscriptions = getAllSubscriptionsForUser(user);
		if( subscriptions.size() > 0  )
		{
			for( InstitutionalCollectionSubscription subscription : subscriptions)
			{
				InstitutionalCollection collection = subscription.getInstitutionalCollection();
			    List<InstitutionalItem> items = institutionalItemService.getItems(collection, startDate, endDate);
			    if( items.size() > 0 )
			    {
			    	sendEmail = true;
			    	emailText.append("New Publications in Collection: " + collection.getName() + "\n\n");
			    	for( InstitutionalItem item : items )
			    	{
			    		// get the url to the most recent item
			    		String url = institutionalItemHandleUrlGenerator.getUrl(item.getVersionedInstitutionalItem().getCurrentVersion());
			    		emailText.append(item.getName() + " - " + url + "\n");
			    	}
			    	emailText.append("\n\n");
			    }
			}
		}
		
		if( sendEmail )
		{
		    log.debug("send subscribers emails");
		    MimeMessage message = mailSender.createMimeMessage();
		
		    MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(message);
		    mimeMessageHelper.setTo(user.getDefaultEmail().getEmail());
		    mimeMessageHelper.setText(emailText.toString());
		    mailSender.send(message);
		}
		
		emailText = null;
	}
	
	/**
	 * Set the mail sender.
	 * 
	 * @param mailSender
	 */
	public void setMailSender(JavaMailSender javaMailSender) {
	    this.mailSender = javaMailSender;
	}

	
	/**
	 * @see edu.ur.ir.institution.InstitutionalCollectionSubscriptionService#getUniqueSubsciberUserIds()
	 */
	public List<Long> getUniqueSubsciberUserIds() {
		return institutionalCollectionSubscriptionDAO.getUniqueSubsciberUserIds();
	}

	public InstitutionalItemService getInstitutionalItemService() {
		return institutionalItemService;
	}

	public void setInstitutionalItemService(
			InstitutionalItemService institutionalItemService) {
		this.institutionalItemService = institutionalItemService;
	}

	public InstitutionalItemHandleUrlGenerator getInstitutionalItemHandleUrlGenerator() {
		return institutionalItemHandleUrlGenerator;
	}

	public void setInstitutionalItemHandleUrlGenerator(
			InstitutionalItemHandleUrlGenerator institutionalItemHandleUrlGenerator) {
		this.institutionalItemHandleUrlGenerator = institutionalItemHandleUrlGenerator;
	}


}
