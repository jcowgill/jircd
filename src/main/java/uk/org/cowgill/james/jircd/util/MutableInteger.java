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

/**
 * Provides a mutable Integer
 *
 * @author James
 */
public final class MutableInteger extends Number implements Comparable<MutableInteger>
{
	private static final long serialVersionUID = 1L;

	private int value;

	/**
	 * Creates a new MutableInteger with the specified starting value
	 * @param newValue value to start with
	 */
	public MutableInteger(int newValue)
	{
		value = newValue;
	}

	/**
	 * Creates a new MutableInteger with the same value as another MutableInteger
	 * @param newValue value to start with
	 */
	public MutableInteger(MutableInteger newValue)
	{
		value = newValue.value;
	}

	/**
	 * Creates a new MutableInteger with the same value as an Integer
	 * @param newValue value to start with
	 */
	public MutableInteger(Integer newValue)
	{
		value = newValue;
	}

	/**
	 * Sets the value of this MutableInteger
	 * @param value new value for this MutableInteger
	 */
	public void setValue(int value)
	{
		this.value = value;
	}

	/**
	 * Increments this MutableInteger
	 */
	public void increment()
	{
		value++;
	}

	/**
	 * Decrements this MutableInteger
	 */
	public void decrement()
	{
		value--;
	}

	/**
	 * Adds a value to this MutableInteger
	 * @param data value to add
	 */
	public void add(int data)
	{
		value += data;
	}

	/**
	 * Subtracts a value from this MutableInteger
	 * @param data value to subtract
	 */
	public void subtract(int data)
	{
		value -= data;
	}

	@Override
	public String toString()
	{
		return Integer.toString(value);
	}

	@Override
	public int hashCode()
	{
		return value;
	}

	@Override
	public boolean equals(Object obj)
	{
		//Check self
		if(this == obj)
		{
			return true;
		}

		//Check instance
		if(!(obj instanceof MutableInteger))
		{
			return false;
		}

		//Check value
		return value == ((MutableInteger) obj).value;
	}

	@Override
	public int compareTo(MutableInteger paramT)
	{
		if(value < paramT.value)
		{
			return -1;
		}
		else if(value > paramT.value)
		{
			return 1;
		}
		else
		{
			return 0;
		}
	}

	@Override
	public int intValue()
	{
		return value;
	}

	@Override
	public long longValue()
	{
		return value;
	}

	@Override
	public float floatValue()
	{
		return value;
	}

	@Override
	public double doubleValue()
	{
		return value;
	}
}
