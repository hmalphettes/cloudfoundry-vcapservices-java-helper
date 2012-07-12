/*
 * Copyright (c) 2011 Intalio Inc
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * 
 */
package org.intalio.cloudfoundry.vcapservices;

/**
 * 
 * "mysql-5.1":[
    {
      "name":"mysql-1",
      "label":"mysql-5.1",
      "plan":"free",
      "tags":[
        "mysql",
        "mysql-5.1",
        "relational"
      ],
      "credentials":{
      ....
      
      }
    }
  ]
 * @author hmalphettes
 *
 */
public interface IVCapService {

	/**
	 * @return The type of the service. For example mysql-5.1 or mongodb-1.8
	 */
	public String getServiceType();
	
	/**
	 * @return The name of the service.
	 */
	public String getName();
	
	/**
	 * @return The name of the service.
	 */
	public String getLabel();
	/**
	 * @return The name of the plan.
	 */
	public String getPlan();
	
	/**
	 * @return The tags
	 */
	public String[] getTags();
	
	/**
	 * @return The connection parameters.
	 */
	public IVCapServiceCredentials getCredentials();
}
