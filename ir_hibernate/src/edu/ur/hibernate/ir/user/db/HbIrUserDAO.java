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

package edu.ur.hibernate.ir.user.db;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import edu.ur.hibernate.HbCrudDAO;
import edu.ur.hibernate.HbHelper;
import edu.ur.ir.user.IrUser;
import edu.ur.ir.user.IrUserDAO;
import edu.ur.order.OrderType;

/**
 * Persistence for a user.
 * 
 * @author Nathan Sarr
 *
 */
public class HbIrUserDAO implements IrUserDAO {
	
	/** eclipse generated id */
	private static final long serialVersionUID = -3904788289887401360L;
	
	/** hibernate helper  */
	private final HbCrudDAO<IrUser> hbCrudDAO;
	
	/**
	 * Default Constructor
	 */
	public HbIrUserDAO() {
		hbCrudDAO = new HbCrudDAO<IrUser>(IrUser.class);
	}
	
	/**
	 * Set the session factory.  
	 * 
	 * @param sessionFactory
	 */
	public void setSessionFactory(SessionFactory sessionFactory)
    {
        hbCrudDAO.setSessionFactory(sessionFactory);
    }
	
	/**
	 * Get a count of the users in the system
	 * 
	 * @see edu.ur.CountableDAO#getCount()
	 */
	public Long getCount() {
		return (Long)HbHelper.getUnique(hbCrudDAO.getHibernateTemplate().findByNamedQuery("userCount"));
	}

	/**
	 * Get all users in user name order.
	 * 
	 * @see edu.ur.NameListDAO#getAllNameOrder()
	 */
	@SuppressWarnings("unchecked")
	public List<IrUser> getAllNameOrder() {
		DetachedCriteria dc = DetachedCriteria.forClass(IrUser.class);
    	dc.addOrder(Order.asc("name"));
    	return (List<IrUser>) hbCrudDAO.getHibernateTemplate().findByCriteria(dc);
	}

	/**
	 * Get all users in name order.
	 * 
	 * @see edu.ur.NameListDAO#getAllOrderByName(int, int)
	 */
	public List<IrUser> getAllOrderByName(int startRecord, int numRecords) {
		return hbCrudDAO.getByQuery("getAllUserNameAsc", startRecord, numRecords);
	}

	/**
	 * Find the user by unique name.
	 * 
	 * @see edu.ur.UniqueNameDAO#findByUniqueName(java.lang.String)
	 */
	public IrUser findByUniqueName(String username) {
		return (IrUser) 
	    HbHelper.getUnique(hbCrudDAO.getHibernateTemplate().findByNamedQuery("getUserByName", username.toLowerCase()));
	}
	
	/**
	 * Load the ir user found by the user name.  This is coupled to the Acegi security.
	 * 
	 * @see org.acegisecurity.userdetails.UserDetailsService#loadUserByUsername(java.lang.String)
	 */
	public IrUser loadUserByUsername(String username) throws UsernameNotFoundException
	{
		IrUser irUser =  findByUniqueName(username);
		if (irUser == null)
		{
			throw new UsernameNotFoundException("User with user name  " + username + " could not be found");
	    }
		return irUser;
	}
	
	@SuppressWarnings("unchecked")
	public List getAll() {
		return hbCrudDAO.getAll();
	}

	public IrUser getById(Long id, boolean lock) {
		return hbCrudDAO.getById(id, lock);
	}

	public void makePersistent(IrUser entity) {
		hbCrudDAO.makePersistent(entity);
	}

	public void makeTransient(IrUser entity) {		
		hbCrudDAO.makeTransient(entity);
	}

	
	/**
	 * Get the users.
	 * 
	 * @see edu.ur.ir.user.IrUserDAO#getUsers(java.util.List)
	 */
	@SuppressWarnings("unchecked")
	public List<IrUser> getUsers(final List<Long> userIds) {
		List<IrUser> foundUsers = new LinkedList<IrUser>();
		if( userIds.size() > 0 )
        {
			foundUsers = (List<IrUser>) hbCrudDAO.getHibernateTemplate().execute(new HibernateCallback() {
		    public Object doInHibernate(Session session)
                throws HibernateException, SQLException {
                    Criteria criteria = session.createCriteria(hbCrudDAO.getClazz());
                    criteria.add(Restrictions.in("id",userIds));
                return criteria.list();
                }
             });
        }
		return foundUsers;
	}


	/**
	 * Load the ir user found by the token.  
	 * 
	 * @see edu.ur.ir.user.IrUserDAO#getUserByToken(java.lang.String)
	 */
	public IrUser getUserByToken(String token)
	{
		return (IrUser) 
	    HbHelper.getUnique(hbCrudDAO.getHibernateTemplate().findByNamedQuery("getUserByToken", token));
		
	}
	
	/**
	 * Get users whose affiliation approval is pending
	 * 
	 * @see edu.ur.ir.user.IrUserDAO#getUsersPendingAffiliationApproval()
	 */
	@SuppressWarnings("unchecked")
	public List<IrUser> getUsersPendingAffiliationApproval()
	{
		return (List<IrUser>) hbCrudDAO.getHibernateTemplate().findByNamedQuery("getUsersPendingAffiliationApproval");
		
	}
	
	
	/**
	 * @see edu.ur.ir.user.IrUserDAO#getUsersPendingAffiliationApprovalCount()
	 */
	public Long getUsersPendingAffiliationApprovalCount() {
		Long count = (Long) hbCrudDAO.getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session)
                    throws HibernateException, SQLException {
                
		        Query q = session.getNamedQuery("getPendingApprovalsCount");
			    
	            return q.uniqueResult();
            }
        });
    	
    	return count;
	}

	/**
	 * @see edu.ur.ir.user.IrUserDAO#getUsersPendingAffiliationApprovals(int, int, String)
	 */
	@SuppressWarnings("unchecked")
	public List<IrUser> getUsersPendingAffiliationApprovals(final int rowStart, 
    		final int numberOfResultsToShow, final String sortType) {
		List<IrUser> users = 
			(List<IrUser>) hbCrudDAO.getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session)
                    throws HibernateException, SQLException {
                
		        Query q = null;
		        if (sortType.equalsIgnoreCase("asc")) {
		        	q = session.getNamedQuery("getPendingApprovalsOrderByNameAsc");
		        } else {
		        	q = session.getNamedQuery("getPendingApprovalsOrderByNameDesc");
		        }
			    
			    q.setFirstResult(rowStart);
			    q.setMaxResults(numberOfResultsToShow);
			    q.setReadOnly(true);
			    q.setFetchSize(numberOfResultsToShow);
	            return q.list();
            }
        });

        return users;
	}

	/**
	 * Get the user having the specified role Id
	 * 
	 * @see edu.ur.ir.user.IrUserDAO#getUserByRole(Long)
	 */
	@SuppressWarnings("unchecked")
	public List<IrUser> getUserByRole(String roleName) {
		return hbCrudDAO.getHibernateTemplate().findByNamedQuery("getUserByRole", roleName);
	}
	
	/**
	 * Get user having the specified person name authority
	 * 
	 * @param personNameAuthorityId Id of person name authority
	 * @return User
	 */
	public IrUser getUserByPersonNameAuthority(Long personNameAuthorityId) {
		return (IrUser) 
	    HbHelper.getUnique(hbCrudDAO.getHibernateTemplate().findByNamedQuery("getUserByPersonNameAuthority", personNameAuthorityId));
	}	

	/**
	 * Get a list of users for a specified sort criteria.
	 * 
	 * @param rowStart - Start row to fetch the data from
	 * @param numberOfResultsToShow - maximum number of results to fetch
	 * @param sortElement - column to sort on 
	 * @param sortType - The order to sort by (ascending/descending)
	 * 
	 * @return List of users
	 */
	@SuppressWarnings("unchecked")
	public List<IrUser> getUsers(final int rowStart, 
    		final int numberOfResultsToShow, final String sortElement, final OrderType orderType) {
		
		List<IrUser> users = new LinkedList<IrUser>();
		
		users = (List<IrUser>) hbCrudDAO.getHibernateTemplate().execute(new HibernateCallback() 
		{
		    public Object doInHibernate(Session session) throws HibernateException, SQLException 
		    {
		        Query q = null;
			    if( sortElement.equalsIgnoreCase("lastName") && orderType.equals(OrderType.ASCENDING_ORDER))
			    {
			        q = session.getNamedQuery("getUsersByLastNameOrderAsc");
			    } else if ( sortElement.equalsIgnoreCase("lastName") && orderType.equals(OrderType.DESCENDING_ORDER)){
			        q = session.getNamedQuery("getUsersByLastNameOrderDesc");
			    } else if ( sortElement.equalsIgnoreCase("username") && orderType.equals(OrderType.ASCENDING_ORDER)){
			        q = session.getNamedQuery("getUsersByUserNameOrderAsc");
			    } else if ( sortElement.equalsIgnoreCase("username") && orderType.equals(OrderType.DESCENDING_ORDER)){
			        q = session.getNamedQuery("getUsersByUserNameOrderDesc");
			    } else if ( sortElement.equalsIgnoreCase("email") && orderType.equals(OrderType.ASCENDING_ORDER)){
			        q = session.getNamedQuery("getUsersByEmailOrderAsc");
			    } else if ( sortElement.equalsIgnoreCase("email") && orderType.equals(OrderType.DESCENDING_ORDER)){
			        q = session.getNamedQuery("getUsersByEmailOrderDesc");
			    }
			    
			    q.setFirstResult(rowStart);
			    q.setMaxResults(numberOfResultsToShow);
			    q.setFetchSize(numberOfResultsToShow);
	            return q.list();
		    }
	    });
		return users;	
		
	}
}
