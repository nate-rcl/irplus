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

package edu.ur.hibernate.ir.item.metadata.marc.db;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.SessionFactory;

import edu.ur.hibernate.HbCrudDAO;
import edu.ur.ir.marc.IdentifierTypeSubFieldMapper;
import edu.ur.ir.marc.IdentifierTypeSubFieldMapperDAO;

/**
 * Data access for the identifer type sub field mapper.
 * 
 * @author Nathan Sarr
 *
 */
public class HbIdentifierTypeSubFieldMapperDAO implements IdentifierTypeSubFieldMapperDAO{

	// Eclipse generated id.
	private static final long serialVersionUID = -3009362822867315140L;
	
	/**  Helper for persisting information using hibernate.  */
	private final HbCrudDAO<IdentifierTypeSubFieldMapper> hbCrudDAO;
	
	/**
	 * Default Constructor
	 */
	public  HbIdentifierTypeSubFieldMapperDAO() {
		hbCrudDAO = new HbCrudDAO<IdentifierTypeSubFieldMapper>(IdentifierTypeSubFieldMapper.class);
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
	 * Get all identifier type sub field mappers.
	 * 
	 * @see edu.ur.dao.CrudDAO#getAll()
	 */
	public List<IdentifierTypeSubFieldMapper> getAll() {
		return hbCrudDAO.getAll();
	}

	/**
	 * Get identifier type sub field mapper by id.
	 * 
	 * @see edu.ur.dao.CrudDAO#getById(java.lang.Long, boolean)
	 */
	public IdentifierTypeSubFieldMapper getById(Long id, boolean lock) {
		return hbCrudDAO.getById(id, lock);
	}

	/**
	 * Save the identifier type sub field mapper.
	 * 
	 * @see edu.ur.dao.CrudDAO#makePersistent(java.lang.Object)
	 */
	public void makePersistent(IdentifierTypeSubFieldMapper entity) {
		hbCrudDAO.makePersistent(entity);
	}

	/**
	 * Remove the identifier type sub field mapper from persistence.
	 * 
	 * @see edu.ur.dao.CrudDAO#makeTransient(java.lang.Object)
	 */
	public void makeTransient(IdentifierTypeSubFieldMapper entity) {
		hbCrudDAO.makeTransient(entity);
	}
	

}
