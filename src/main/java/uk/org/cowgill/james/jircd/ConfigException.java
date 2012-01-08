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
package uk.org.cowgill.james.jircd;

import uk.org.cowgill.james.jircd.util.PushbackLineInputStream;

/**
 * Exception which is thrown when an error is encountered parsing a config file
 * 
 * @author James
 */
public class ConfigException extends Exception
{
	private static final long serialVersionUID = 1L;
		
	/**
	 * The line number the exception was generated at in the file
	 * 
	 * If lineNo <= 0, no line or character was specified
	 */
	public final int lineNo;
		
	/**
	 * The character number the exception was generated at in the file
	 * 
	 * If charNo <= 0, no character is unknown
	 */
	public final int charNo;

	public ConfigException()
	{
		this.lineNo = 0;
		this.charNo = 0;
	}
	
	public ConfigException(String str)
	{
		super(str);
		this.lineNo = 0;
		this.charNo = 0;
	}
	
	public ConfigException(String str, Throwable err)
	{
		super(str, err);
		this.lineNo = 0;
		this.charNo = 0;
	}
	
	public ConfigException(Throwable err)
	{
		super(err);
		this.lineNo = 0;
		this.charNo = 0;
	}
	
	public ConfigException(String str, int lineNo, int charNo)
	{
		super(str);
		this.lineNo = lineNo;
		this.charNo = charNo;
	}
	
	ConfigException(String str, PushbackLineInputStream stream)
	{
		super(str);
		this.lineNo = stream.getLineNo();
		this.charNo = stream.getCharNo();
	}
	
	@Override
	public String toString()
	{
		return super.toString() + ", at line " + lineNo + " col " + charNo;
	}
}
