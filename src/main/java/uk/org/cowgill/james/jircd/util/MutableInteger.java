package uk.org.cowgill.james.jircd.util;

/**
 * Provides a mutable int wrapper
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
		value = (int) newValue;
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
	
	public String toString()
	{
		return Integer.toString(value);
	}
	
	public int hashCode()
	{
		return value;
	}
	
	public boolean equals(Object obj)
	{
		//Check self
		if(this == obj)
		{
			return true;
		}
		
		//Check instace
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