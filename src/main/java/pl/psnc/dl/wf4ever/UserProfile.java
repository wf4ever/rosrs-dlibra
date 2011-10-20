/**
 * 
 */
package pl.psnc.dl.wf4ever;

/**
 * @author piotrhol
 *
 */
public class UserProfile
{

	private final String login;

	private final String name;

	private final boolean admin;


	/**
	 * @param login
	 * @param name
	 */
	public UserProfile(String login, String name, boolean admin)
	{
		super();
		this.login = login;
		this.name = name;
		this.admin = admin;
	}


	public String getLogin()
	{
		return login;
	}


	public String getName()
	{
		return name;
	}


	public boolean isAdmin()
	{
		return admin;
	}
}
