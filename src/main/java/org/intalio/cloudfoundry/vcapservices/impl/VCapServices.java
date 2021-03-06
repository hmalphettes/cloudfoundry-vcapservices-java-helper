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
package org.intalio.cloudfoundry.vcapservices.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.intalio.cloudfoundry.vcapservices.IVCapService;
import org.intalio.cloudfoundry.vcapservices.IVCapServiceCredentials;
import org.intalio.cloudfoundry.vcapservices.IVCapServices;
import org.intalio.cloudfoundry.vcapservices.NegatablePattern;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 
 * 
 * @author hmalphettes
 */
public class VCapServices implements IVCapServices {
	
	/**
	 * The services.
	 */
	private final LinkedHashMap<String, ArrayList<IVCapService>> _services = new LinkedHashMap<String, ArrayList<IVCapService>>();
	
	private final Map<String,IVCapService> _servicesIndexedByName = new HashMap<String, IVCapService>();
	
	public VCapServices() throws JSONException {
		String vcapServices = System.getenv("VCAP_SERVICES");
		if (vcapServices == null) {
			vcapServices = System.getProperty("VCAP_SERVICES");
		}
		if (vcapServices != null) {
			this.setServices(vcapServices, false);
		}
	}
	
	/**
	 * @param defaultIfEnvValueNotDefined default value if the environment constant 'VCAP_SERVICES' is not defined.
	 * @throws JSONException
	 */
	public VCapServices(String defaultIfEnvValueNotDefined) throws JSONException {
		this(defaultIfEnvValueNotDefined, true);
	}
		
	/**
	 * @param services
	 * @throws JSONException
	 */
	public VCapServices(String services, boolean lookAtEnvFirst) throws JSONException {
		if (lookAtEnvFirst) {
			String envServices = System.getenv("VCAP_SERVICES");
			if (envServices != null && envServices.trim().length() != 0) {
				this.setServices(envServices, false);
				return;
			}
		}
		this.setServices(services, true);
	}
	
	protected void setServices(String services, boolean resolveSysProperty) throws JSONException {
		if (services == null) {
			throw new IllegalArgumentException("The services string description" +
					" must not be null. No VCAP_SERVICES to parse.");
		}
		JSONObject obj = new JSONObject(services);
		Iterator<?> it = obj.keys();
		while (it.hasNext()) {
			String serviceType = (String)it.next();
			JSONArray arr = obj.getJSONArray(serviceType);
			ArrayList<IVCapService> servicesArr = new ArrayList<IVCapService>(arr.length());
			if (_services.put(serviceType, servicesArr) != null) {
				throw new IllegalArgumentException("Duplicate service type arrays '" + serviceType + "'.");
			}
			for (int i = 0; i < arr.length(); i++) {
				JSONObject servOb = arr.getJSONObject(i);
				VCapService serv = new VCapService(serviceType, servOb, resolveSysProperty);
				servicesArr.add(serv);
				String name = serv.getName();
				if (name != null) {
					if (_servicesIndexedByName.put(name, serv) != null) {
						throw new IllegalArgumentException("Duplicate service with the name '" + name + "'.");
					}
				}
			}
		}
	}
	

	
	/**
	 * This reflects directly the way the vcap services are described in the environment variable
	 * VCAP_SERVICES.
	 * VCAP_SERVICES are an ordered bag of vcap services where the index is the service type (for example mongodb-1.8).
	 * @return
	 */
	public LinkedHashMap<String,ArrayList<IVCapService>> getVCapServices() {
		return _services;
	}
	
	/**
	 * @param serviceType
	 * @return The services of a given type.
	 */
	public ArrayList<IVCapService> getVCapServicesByType(String serviceType) {
		return _services.get(serviceType);
	}
	
	/**
	 * Helper method: traverses the vcap services and
	 * returns the first one that name matches the argument.
	 * @param name
	 * @return The first service with this name or null.
	 */
	public IVCapService getVCapServiceByName(String name) {
		return _servicesIndexedByName.get(name);
	}
	
	/**
	 * @param serviceType
	 * @param index The 0-based index
	 * @return
	 */
	public IVCapService getVCapService(String serviceType, int index) {
		ArrayList<IVCapService> servs = getVCapServicesByType(serviceType);
		if (servs == null || servs.size() > index+1) {
			return null;
		}
		return servs.get(index);
	}
	
	/**
	 * @param serviceType regexp
	 * @param index The 0-based index
	 * @return
	 */
	public IVCapService getVCapService(Pattern serviceType, int index) {
		for (Entry<String,ArrayList<IVCapService>> e : _services.entrySet()) {
			String servType = e.getKey();
			Matcher m = serviceType.matcher(servType);
			if (m.matches()) {
				if (e.getValue().size() > index+1) {
					continue;
				} else {
					return e.getValue().get(index);
				}
			}
		}
		return null;
	}
	

	/**
	 * Helper method: traverses the vcap services and
	 * returns the first one that name matches the argument.
	 * @param name
	 * @return The first service with this name or null.
	 */
	public IVCapServiceCredentials getVCapServiceCredentialsByName(String name) {
		IVCapService serv = getVCapServiceByName(name);
		if (serv != null) {
			return serv.getCredentials();
		}
		return null;

	}
	
	/**
	 * @param serviceType
	 * @param index The 0-based index
	 * @return
	 */
	public IVCapServiceCredentials getVCapServiceCredentials(String serviceType, int index) {
		IVCapService serv = getVCapService(serviceType, index);
		if (serv != null) {
			return serv.getCredentials();
		}
		return null;
	}
	
	/**
	 * @param serviceTypeFilter regexp filter
	 * @param serviceNameFilter regexp filter
	 * @return
	 */
	public ArrayList<IVCapService> getVCapServices(NegatablePattern serviceTypeFilter, NegatablePattern serviceNameFilter) {
		ArrayList<IVCapService> res = new ArrayList<IVCapService>();
		for (Entry<String,ArrayList<IVCapService>> e : _services.entrySet()) {
			String servType = e.getKey();
			if (serviceTypeFilter.matches(servType)) {
				for (IVCapService service : e.getValue()) {
					if (serviceNameFilter.matches(service.getName())) {
						res.add(service);
					}
				}
			}
		}
		return res;
	}
	
	/**
	 * Extract a connection URI from either an environment variable or from 
	 * the VCAP_SERVICES json.
	 * <p>
	 * java -DDATABASE_URL=postgresql://postgres:postgres@localhost/postgres
	 * </p>
	 * @param defaultURIOrSysPropertyForIt Value of the URI to use if
	 *    VCAP_SERVICES is not present. Or sys property for this default value.
	 * @param scheme The scheme of the URI. For example: 'mysql' or 'postgresql'
	 * @param serviceTypeRegexpOrString name of the service type. Or regexp to
	 * select it.
	 * @param nameOfServiceSelector Name of the service or regexp to select it.
	 * or null to get the first one.
	 * @throws URISyntaxException 
	 */
	public URI getConnectionAsURI(
			String defaultURIOrSysPropertyForIt,
			String scheme, String serviceTypeRegexpOrString)
	throws URISyntaxException, JSONException {
		return VCapServiceCredentials.getConnectionAsURI(this,
				defaultURIOrSysPropertyForIt, scheme, serviceTypeRegexpOrString);
	}
	
	/**
	 * Extract a connection URI from either an environment variable or from 
	 * the VCAP_SERVICES json.
	 * <p>
	 * java -DDATABASE_URL=postgresql://postgres:postgres@localhost/postgres
	 * </p>
	 * @param defaultURIOrSysPropertyForIt Value of the URI to use if
	 *    VCAP_SERVICES is not present. Or sys property for this default value.
	 * @param scheme The scheme of the URI. For example: 'mysql' or 'postgresql'
	 * @param serviceTypeRegexpOrString name of the service type. Or regexp to
	 * select it.
	 * @param nameOfServiceSelector Name of the service or regexp to select it.
	 * or null to get the first one.
	 * @throws URISyntaxException 
	 */
	public URI getConnectionAsURI(
			String defaultURIOrSysPropertyForIt,
			String scheme, String serviceTypeRegexpOrString,
			String nameOfServiceSelector)
    throws URISyntaxException, JSONException {
		return VCapServiceCredentials.getConnectionAsURI(this,
				defaultURIOrSysPropertyForIt, scheme, serviceTypeRegexpOrString,
				nameOfServiceSelector);
	}	
}
