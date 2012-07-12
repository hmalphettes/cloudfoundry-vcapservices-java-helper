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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.regex.Pattern;

/**
 * Parses a JSON string into a collection of vcap services.
 * <p>
 * Database connection parameters and other application services 
 * are provided by cloudfoundry in the environment variable VCAP_SERVICES.
 * </p>
 * <p>
 * The value of this variable is a JSON string that contains the name of the wired
 * app services and the connection parameters for each one of them.
 * </p>
 * <p>
 * This helper class parses JSON and provides the value of those connection
 * parameters.
 * </p>
 * @author hmalphettes
 */
public interface IVCapServices {

	/**
	 * This reflects directly the way the vcap services are described in the environment variable
	 * VCAP_SERVICES.
	 * VCAP_SERVICES are an ordered bag of vcap services where the index is the service type
	 * (for example mongodb-1.8).
	 * @return
	 */
	public LinkedHashMap<String,ArrayList<IVCapService>> getVCapServices();
	
	/**
	 * @param serviceType
	 * @return The services of a given type.
	 */
	public ArrayList<IVCapService> getVCapServicesByType(String serviceType);
	
	/**
	 * Helper method: traverses the vcap services and
	 * returns the first one that name matches the argument.
	 * @param name
	 * @return The first service with this name or null.
	 */
	public IVCapService getVCapServiceByName(String name);
	
	/**
	 * @param serviceType
	 * @param index The 0-based index
	 * @return
	 */
	public IVCapService getVCapService(String serviceType, int index);
	/**
	 * @param serviceType regexp
	 * @param index The 0-based index
	 * @return
	 */
	public IVCapService getVCapService(Pattern serviceType, int index);
	
	/**
	 * @param serviceTypeFilter regexp filter
	 * @param serviceNameFilter regexp filter
	 * @return
	 */
	public ArrayList<IVCapService> getVCapServices(NegatablePattern serviceTypeFilter, NegatablePattern serviceNameFilter);
	
}
