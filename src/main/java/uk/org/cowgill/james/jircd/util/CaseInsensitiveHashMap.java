/*
   Copyright 2011 James Cowgill

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

	@Override
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
	
	@Override
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
	
	@Override
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
	
	@Override
	public void putAll(Map<? extends String, ? extends V> paramMap)
	{
		//Put each entry
		for(Map.Entry<? extends String, ? extends V> entry : paramMap.entrySet())
		{
			put(entry.getKey(), entry.getValue());
		}
	}
	
	@Override
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
