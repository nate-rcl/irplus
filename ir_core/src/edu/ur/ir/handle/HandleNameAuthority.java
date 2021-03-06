package edu.ur.ir.handle;

import edu.ur.persistent.BasePersistent;

/**
 * This class represents the naming authority including the local name and
 * the base url of the naming authority.
 * 
 * @author Nathan Sarr
 *
 */
public class HandleNameAuthority extends BasePersistent{

	/** eclipse generated id  */
	private static final long serialVersionUID = 5099575658284711364L;
	
	/** Name representing the naming authority */
	private String namingAuthority;
	
	/** url for the authority  */
	private String authorityBaseUrl;
	
	/** Description of the handle name authority */
	private String description;


	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Package protected constructor
	 */
	HandleNameAuthority(){}
	
	/**
	 * Create a handle name authority
	 * 
	 * @param handleNamingAuthority
	 * @param handleLocalName
	 */
	public HandleNameAuthority(String namingAuthority)
	{
		this.setNamingAuthority(namingAuthority);
	}

	/**
	 *  Name representing the naming authority
	 *  
	 * @return
	 */
	public String getNamingAuthority() {
		return namingAuthority;
	}

	/**
	 *  Name representing the naming authority
	 *  
	 * @param handleNamingAuthority
	 */
	public void setNamingAuthority(String handleNamingAuthority) {
		this.namingAuthority = handleNamingAuthority;
	}
	

	public String getAuthorityBaseUrl() {
		return authorityBaseUrl;
	}

	public void setAuthorityBaseUrl(String authorityBaseUrl) {
		this.authorityBaseUrl = authorityBaseUrl;
	}
	

	
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (!(o instanceof HandleNameAuthority)) return false;

		final HandleNameAuthority other = (HandleNameAuthority) o;
		if( (namingAuthority != null && !namingAuthority.equals(other.getNamingAuthority()) ) ||
			(namingAuthority == null && other.getNamingAuthority() != null ) ) return false;
		
		return true;
	}
	
	public int hashCode()
	{
		int value = 0;
		value += namingAuthority == null ? 0 : namingAuthority.hashCode();
		return value;
	}

	public String toString()
	{
		StringBuffer sb = new StringBuffer("[ id = ");
		sb.append(id);
		sb.append(" namingAuthority = ");
		sb.append( namingAuthority);
		sb.append( " base url = ");
		sb.append(authorityBaseUrl);
		sb.append("]");
		return sb.toString();
	}
	
	

}
