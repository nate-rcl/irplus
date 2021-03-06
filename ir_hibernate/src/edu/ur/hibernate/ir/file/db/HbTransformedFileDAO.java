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

package edu.ur.hibernate.ir.file.db;

import java.util.List;

import org.hibernate.SessionFactory;

import edu.ur.file.db.FileInfo;
import edu.ur.hibernate.HbCrudDAO;
import edu.ur.hibernate.HbHelper;
import edu.ur.ir.file.TransformedFile;
import edu.ur.ir.file.TransformedFileDAO;


/**
 * Maintain persistence for a transformed file.
 * 
 * @author Nathan Sarr
 *
 */
public class HbTransformedFileDAO implements TransformedFileDAO{

	/** eclipse generated id */
	private static final long serialVersionUID = -3354162592690448160L;
	
	/** hibernate helper */
	private final HbCrudDAO<TransformedFile> hbCrudDAO;
	
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
	 * Default Constructor
	 */
	public HbTransformedFileDAO() {
		hbCrudDAO = new HbCrudDAO<TransformedFile>(TransformedFile.class);
	}
	
	
	/**
	 * Get all transformed files in the system.
	 * 
	 * @see edu.ur.dao.CrudDAO#getAll()
	 */
	@SuppressWarnings("unchecked")
	public List getAll() {
		return hbCrudDAO.getAll();
	}

	/**
	 * Get a transfomed file by id.
	 * 
	 * @see edu.ur.dao.CrudDAO#getById(java.lang.Long, boolean)
	 */
	public TransformedFile getById(Long id, boolean lock) {
		return hbCrudDAO.getById(id, lock);
	}

	/**
	 * Save a transformed file to database storage.
	 * 
	 * @see edu.ur.dao.CrudDAO#makePersistent(java.lang.Object)
	 */
	public void makePersistent(TransformedFile entity) {
		hbCrudDAO.makePersistent(entity);
	}

	/**
	 * Remove the transformed file information from database storage.
	 * 
	 * @see edu.ur.dao.CrudDAO#makeTransient(java.lang.Object)
	 */
	public void makeTransient(TransformedFile entity) {
		hbCrudDAO.makeTransient(entity);
	}

	
	public FileInfo getTransformedFile(Long irFileId,
			String transformedFileTypeSystemCode) {
		
		Object[] values = {irFileId, transformedFileTypeSystemCode};
		return (FileInfo)
		HbHelper.getUnique(hbCrudDAO.getHibernateTemplate().findByNamedQuery("getTransformByIrFileIdTransformTypeSystemCode", values));
	}

}
