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
 * Credentials to connect to a data service. As modeled in VCAP_SERVICES.
 * 
 *  "credentials":{
        "name":"d50dc30be91474b80a766367dcfb0dc31",
        "hostname":"172.30.48.26",
        "port":3306,
        "user":"ueQzIwnjcMq4B",
        "password":"pxk542cfA2HNf"
      }
 * @author hmalphettes
 *
 */
public interface IVCapServiceCredentials {

	/**
	 * @return The name of the app service.
	 * For example, the database name of a relational database.
	 */
	public String getName();
	/**
	 * @return The IP or hostname where the service runs.
	 */
	public String getHostname();
	/**
	 * @return The port where it is accessible.
	 */
	public int getPort();
	/**
	 * @return The name of the user.
	 */
	public String getUser();
	/**
	 * @return The password.
	 */
	public String getPassword();
	
	/**
	 * @return The db value or null when not defined. Used in mongodb.
	 * For a relational database, the name of the database is returned by IVCapCredentials#getName
	 */
	public String getDb();
	
}
