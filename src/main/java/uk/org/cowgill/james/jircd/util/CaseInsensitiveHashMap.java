package uk.org.cowgill.james.jircd.util;

import java.util.HashMap;
import java.util.Map;

/**
 * A case insensitive hash map class
 * 
 * <p>Note that keyset and entryset return keys in lowercase
 * 
 * @author James
 *
 * @param <V> type of the value of the map
 */
public class CaseInsensitiveHashMap<V> extends HashMap<String, V>
{
	private static final long serialVersionUID = 1L;

	public boolean containsKey(Object key)
	{
		if(key == null)
		{
			return super.containsKey(null);
		}
		else
		{
			return super.containsKey(key.toString().toLowerCase());
		}
	}
	
	public V get(Object key)
	{
		if(key == null)
		{
			return super.get(null);
		}
		else
		{
			return super.get(key.toString().toLowerCase());
		}
	}
	
	public V put(String key, V value)
	{
		if(key == null)
		{
			return super.put(null, value);
		}
		else
		{
			return super.put(key.toLowerCase(), value);
		}
	}
	
	public void putAll(Map<? extends String, ? extends V> paramMap)
	{
		//Put each entry
		for(Map.Entry<? extends String, ? extends V> entry : paramMap.entrySet())
		{
			put(entry.getKey(), entry.getValue());
		}
	}
	
	public V remove(Object key)
	{
		if(key == null)
		{
			return super.remove(null);
		}
		else
		{
			return super.remove(key.toString().toLowerCase());
		}
	}
}
