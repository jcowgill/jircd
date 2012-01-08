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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MultiHashMap<K, V> implements MultiMap<K, V>
{
	private HashMap<K, Collection<V>> data = new HashMap<K, Collection<V>>();

	@Override
	public boolean putValue(K key, V value)
	{
		//Find key value
		Collection<V> keyData = data.get(key);
		
		if(keyData == null)
		{
			//Create new collection
			keyData = new ArrayList<V>();
			data.put(key, keyData);
		}
		
		//Add to collection
		return keyData.add(value);
	}

	@Override
	public boolean removeValue(K key, V value)
	{		
		//Find key value
		boolean removeStatus = false;
		Collection<V> keyData = data.get(key);
		
		if(keyData != null)
		{
			//Remove from collection
			removeStatus = keyData.remove(value);
			
			//If empty, remove key
			if(keyData.isEmpty())
			{
				data.remove(key);
			}
		}
		
		return removeStatus;
	}

	@Override
	public boolean containsValue(Object value)
	{
		//If value is a Collection, pass to data
		// otherwize check every collection
		if(value instanceof Collection<?>)
		{
			return data.containsValue(value);
		}
		else
		{
			//Process each key
			for(Map.Entry<K, Collection<V>> entry : data.entrySet())
			{
				if(entry.getValue().contains(value))
				{
					return true;
				}
			}
			
			//Not found
			return false;
		}
	}

	@Override
	public void putAllValues(Map<? extends K, ? extends V> map)
	{
		//Put each vaue separately
		for(Map.Entry<? extends K, ? extends V> entry : map.entrySet())
		{
			this.putValue(entry.getKey(), entry.getValue());
		}
	}

	@Override
	public void putAll(Map<? extends K, ? extends Collection<V>> map)
	{
		//Put each key separately
		for(Map.Entry<? extends K, ? extends Collection<V>> entry : map.entrySet())
		{
			this.putAll(entry.getKey(), entry.getValue());
		}
	}

	@Override
	public void putAll(K key, Collection<? extends V> values)
	{
		//Key exists?
		Collection<V> keyData = data.get(key);
		
		if(keyData == null)
		{
			data.put(key, new ArrayList<V>(values));
		}
		else
		{
			keyData.addAll(values);
		}
	}

	@Override
	public int totalSize()
	{
		int currSize = 0;
		
		//Add up every collection's size
		for(Map.Entry<K, Collection<V>> entry : data.entrySet())
		{
			currSize += entry.getValue().size();
		}
		
		return currSize;
	}

	@Override
	public int size(K key)
	{
		Collection<V> keyData = data.get(key);
		
		if(keyData == null)
		{
			return 0;
		}
		else
		{
			return keyData.size();
		}
	}
	
	//-------------------------------------------------------------------
	
	@Override
	public void clear()
	{
		data.clear();
	}

	@Override
	public boolean containsKey(Object key)
	{
		return data.containsKey(key);
	}

	@Override
	public Set<java.util.Map.Entry<K, Collection<V>>> entrySet()
	{
		return data.entrySet();
	}

	@Override
	public Collection<V> get(Object key)
	{
		return data.get(key);
	}

	@Override
	public boolean isEmpty()
	{
		return data.isEmpty();
	}

	@Override
	public Set<K> keySet()
	{
		return data.keySet();
	}

	@Override
	public Collection<V> put(K key, Collection<V> value)
	{
		return data.put(key, value);
	}

	@Override
	public Collection<V> remove(Object key)
	{
		return data.remove(key);
	}

	@Override
	public int size()
	{
		return data.size();
	}

	@Override
	public Collection<Collection<V>> values()
	{
		return data.values();
	}
}
