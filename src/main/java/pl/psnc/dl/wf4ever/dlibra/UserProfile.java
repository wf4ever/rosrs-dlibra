/**
 * 
 */
package pl.psnc.dl.wf4ever.dlibra;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

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

	private final URI uri;

	private URI homePage;


	/**
	 * @param login
	 * @param name
	 */
	public UserProfile(String login, String password, String name, Role role, URI uri)
	{
		super();
		this.login = login;
		this.password = password;
		this.name = name;
		this.role = role;
		if (uri == null) {
			uri = URI.create(login);
			if (uri == null) {
				try {
					uri = new URI(null, login, null);
				}
				catch (URISyntaxException e) {
					uri = URI.create(UUID.randomUUID().toString());
				}
			}
		}
		if (!uri.isAbsolute())
			uri = URI.create("http://sandbox.wf4ever.project.com/users/").resolve(uri);
		this.uri = uri;
	}


	/**
	 * @param login
	 * @param name
	 */
	public UserProfile(String login, String password, String name, Role role)
	{
		this(login, password, name, role, null);
	}


	/**
	 * @param uri
	 *            the uri to set
	 */
	public void setHomePage(URI uri)
	{
		this.homePage = uri;
	}


	/**
	 * @return the uri
	 */
	public URI getHomePage()
	{
		return homePage;
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


	/**
	 * @return the uri
	 */
	public URI getUri()
	{
		return uri;
	}

}
