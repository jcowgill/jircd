package uk.org.cowgill.james.jircd;

import java.util.Collection;
import java.util.Map;

/**
 * A map which can contain multiple values for each key
 * 
 * @author James
 */
public interface MultiMap<K, V> extends Map<K, Collection<V>>
{
	/**
	 * Adds a value to the multimap with the specified key
	 * 
	 * @param key The key the value is to be given
	 * @param value The value
	 * @return True if the map changed
	 */
	boolean putValue(K key, V value);
	
	/**
	 * Removes a sepific value from the multimap
	 * 
	 * @param key The key which contains the value
	 * @param value The value to remove
	 * @return True if a value was removed
	 */
	boolean removeValue(K key, V value);
	
	/**
	 * Returns true if the multimap contains the specified value or collection
	 * 
	 * @param value The value to check for
	 * @return True if the value exists
	 */
	@Override
	boolean containsValue(Object value);
	
	/**
	 * Puts all the values in the specified map into the multimap
	 * 
	 * @param map The map to copy values from
	 */
	void putAllValues(Map<? extends K, ? extends V> map);
	
	/**
	 * Puts all the values in the specified collection into the multimap
	 * 
	 * @param key The key all the values will have
	 * @param values The values to add
	 */
	void putAll(K key, Collection<? extends V> values);

	/**
	 * Returns the number of values in the entire multimap
	 * 
	 * @return The number of values
	 */
	int totalSize();
	
	/**
	 * Returns the number of values with the specified key
	 * 
	 * @param key The key to check for
	 * @return The number of values with the specified key
	 */
	int size(K key);
}
