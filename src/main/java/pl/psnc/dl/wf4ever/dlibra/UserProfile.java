/**
 * 
 */
package pl.psnc.dl.wf4ever.dlibra;

/**
 * @author piotrhol
 *
 */
public class UserProfile
{

	public enum Role {
		ADMIN, AUTHENTICATED, PUBLIC
	}

	private final String login;

	private final String password;

	private final String name;

	private final Role role;


	/**
	 * @param login
	 * @param name
	 */
	public UserProfile(String login, String password, String name, Role role)
	{
		super();
		this.login = login;
		this.password = password;
		this.name = name;
		this.role = role;
	}


	public String getLogin()
	{
		return login;
	}


	/**
	 * @return the password
	 */
	public String getPassword()
	{
		return password;
	}


	public String getName()
	{
		return name;
	}


	public Role getRole()
	{
		return role;
	}

}
