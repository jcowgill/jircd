package uk.org.cowgill.james.jircd.util;

import uk.org.cowgill.james.jircd.Client;

/**
 * Object splits up a list of names into multiple messages to send to clients
 *
 * <p>Used for things like NAMES, where multiple nicknames can be sent on one line
 */
public class NamesListBuilder
{
	private final Client client;
	private final StringBuilder builder;
	private final int prefixLength;

	/**
	 * Creates a new NamesListBuilder, sending messages to client and using the given prefix
	 *
	 * <p>Prefix should contain the ENTIRE prefix (including the final : character)
	 *
	 * @param client client to send messages to
	 * @param prefix prefix string
	 */
	public NamesListBuilder(Client client, String prefix)
	{
		this.client = client;
		this.builder = new StringBuilder(prefix);
		this.prefixLength = prefix.length();
	}

	/**
	 * Appends the given name to the list
	 *
	 * @param name name to append
	 */
	public void addName(String name)
	{
		// Flush if adding this would make the message too long
		//  509 = 512 (max msg len) - 2 (crlf) - 1 (space before previous name)
		if (builder.length() + name.length() >= 509)
			flush();

		// Add name to buffer
		if (builder.length() > prefixLength)
			builder.append(' ');

		builder.append(name);
	}

	/**
	 * Flushes any names in the builder but not sent yet
	 */
	public void flush()
	{
		if (builder.length() > prefixLength)
		{
			client.send(builder);
			builder.setLength(prefixLength);
		}
	}
}
