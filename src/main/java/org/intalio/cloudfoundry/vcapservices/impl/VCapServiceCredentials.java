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
import java.util.regex.Pattern;

import org.intalio.cloudfoundry.vcapservices.IVCapService;
import org.intalio.cloudfoundry.vcapservices.IVCapServiceCredentials;
import org.intalio.cloudfoundry.vcapservices.IVCapServices;
import org.intalio.cloudfoundry.vcapservices.NegatablePattern;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * "credentials":{
        "name":"d50dc30be91474b80a766367dcfb0dc31",
        "hostname":"172.30.48.26",
        "port":3306,
        "user":"ueQzIwnjcMq4B",
        "password":"pxk542cfA2HNf"
      }
 * @author hmalphettes
 *
 */
public class VCapServiceCredentials implements IVCapServiceCredentials {
 
	private final JSONObject _json;
	private final boolean _resolveSysProperty;

	/**
	 * Extract a connection URI from either an environment variable or from 
	 * the VCAP_SERVICES json.
	 * 
	 * java -DDATABASE_URL=postgresql://postgres:postgres@localhost/postgres
	 * @param defaultURIOrSysPropertyForIt Value of the URI to use if
	 *    VCAP_SERVICES is not present. Or sys property for this default value.
	 * @param scheme The scheme of the URI. For example: 'mysql' or 'postgresql'
	 * @param serviceTypeRegexpOrString name of the service type. Or regexp to
	 * select it.
	 * @param nameOfServiceSelector Name of the service or regexp to select it.
	 * or null to get the first one.
	 * @throws URISyntaxException 
	 */
	protected static URI getConnectionAsURI(VCapServices services,
			String defaultURIOrSysPropertyForIt,
			String scheme, String serviceTypeRegexpOrString)
	throws URISyntaxException, JSONException {
		return getConnectionAsURI(services,
				    defaultURIOrSysPropertyForIt, scheme,
					serviceTypeRegexpOrString, null);
	}
	
	/**
	 * Extract a connection URI from either an environment variable or from 
	 * the VCAP_SERVICES json.
	 * 
	 * java -DDATABASE_URL=postgresql://postgres:postgres@localhost/postgres
	 * @param defaultURIOrSysPropertyForIt Value of the URI to use if
	 *    VCAP_SERVICES is not present. Or sys property for this default value.
	 * @param scheme The scheme of the URI. For example: 'mysql' or 'postgresql'
	 * @param serviceTypeRegexpOrString name of the service type. Or regexp to
	 * select it.
	 * @param nameOfServiceSelector Name of the service or regexp to select it.
	 * or null to get the first one.
	 * @throws URISyntaxException 
	 */
	protected static URI getConnectionAsURI(
			VCapServices services,
			String defaultURIOrSysPropertyForIt,
			String scheme, String serviceTypeRegexpOrString,
			String nameOfServiceSelector) 
	throws URISyntaxException, JSONException {
		String envServices = System.getenv("VCAP_SERVICES");
		if (envServices == null) {
			// in production it is always set in the environment
			// looking for it in the system properties makes it easier to test.
			envServices = System.getProperty("VCAP_SERVICES");
		}
        if (envServices == null) {
        	if (defaultURIOrSysPropertyForIt.indexOf("://") != -1) {
        		return new URI(defaultURIOrSysPropertyForIt);
        	}
        	String value = System.getenv(defaultURIOrSysPropertyForIt);
        	if (value == null) {
        		value = System.getProperty(defaultURIOrSysPropertyForIt);
        	}
        	if (value == null) {
        		throw new IllegalArgumentException("The default URI '" +
        				defaultURIOrSysPropertyForIt + " for the " +
        				"database connection parameter is not defined;" +
        				"Expecting either a URI, a system property or an " +
        				"environment variable.");
        	}
        	return new URI(value);
        }
        VCapServiceCredentials creds = null;
        if (nameOfServiceSelector == null) {
            creds = (VCapServiceCredentials)VCapServiceCredentials
            			.getCredentialsOfFirstService(
            		services, serviceTypeRegexpOrString);
        } else {
        	creds = (VCapServiceCredentials)VCapServiceCredentials
        				.getCredentialsOfService(
            		services, serviceTypeRegexpOrString, nameOfServiceSelector);
        }
        if (creds == null) {
        	String nameLabel = nameOfServiceSelector == null ? ""
        			: "; service-name=" + nameOfServiceSelector;
        	throw new IllegalArgumentException("Unable to find in VCAP_SERVICES" +
        			" a service for (service-type=" + serviceTypeRegexpOrString +
        			nameLabel + "). VCAP_SERVICES: \n" + services);
        }
        String db = creds.getDb();
        if (db == null) {
        	db = creds.getName();
        }
        String port = creds.getString("port");
        if (port != null) {
        	port = ":" + port;
        } else {
        	port = "";
        }
		return new URI(scheme + "://" + creds.getUser() + ":" + creds.getPassword() +
				"@" + creds.getHostname() + port + "/" + db);
	}
	

	
	/**
	 * Factory method for Ioc like spring.
	 * @param services
	 * @param serviceType
	 * @return
	 */
	public static IVCapServiceCredentials getCredentialsOfFirstService(
			IVCapServices services, String serviceType) {
		if (serviceType.startsWith("/") && serviceType.endsWith("/")) {
			serviceType = serviceType.substring(1, serviceType.length() -1);
			return getCredentialsOfFirstServiceRegex(services, serviceType);
		} else {
			return services.getVCapService(serviceType, 0).getCredentials();
		}
	}

	
	/**
	 * Factory method for Ioc like spring.
	 * @param services
	 * @param serviceTypeRegexp The regular experession to select the service type.
	 * @return
	 */
	public static IVCapServiceCredentials getCredentialsOfFirstServiceRegex(
			IVCapServices services, String serviceTypeRegexp) {
		Pattern serviceTypeReg = Pattern.compile(serviceTypeRegexp);
		IVCapService vservice = services.getVCapService(serviceTypeReg, 0);
		if (vservice == null) {
			return null;
		}
		return vservice.getCredentials();
	}
	
	/**
	 * Factory method for Ioc like spring.
	 * 
	 * For example in spring, assuming the bean vcapservices contains the value of the environment variable VCAP_SERVICES
	 * You would select the postgres data service that name contains 'definition' with:
	 * <code>
     * <bean id="postgresCredentials" class="org.intalio.cloudfoundry.vcapservices.impl.VCapServiceCredentials"
     *         factory-method="getCredentialsOfService">
     *  <constructor-arg index="0" ref="vcapservices"/>
     *  <constructor-arg index="1" value="/^postgres.STAR/"/>
     *  <constructor-arg index="2" value="/.*${TOKEN_OF_DEF_PERSISTENCE_DATA_SERVICE,definition}.STAR/"/>
     * </bean>
     * </code>
     * NOTE: don't copy paste this without replacing the STARss by a '*' (javadoc formatting issues)
     * 
     * And you would select the data-service that does NOT contain 'definition' with a negated pattern:
     * <code>
     * <bean id="postgresCredentials" class="org.intalio.cloudfoundry.vcapservices.impl.VCapServiceCredentials"
     *         factory-method="getCredentialsOfService">
     *  <constructor-arg index="0" ref="vcapservices"/>
     *  <constructor-arg index="1" value="/^postgres.STAR/"/>
     *  <constructor-arg index="2" value="!/.*${TOKEN_OF_DEF_PERSISTENCE_DATA_SERVICE,definition}.STAR/"/>
     * </bean>
	 * </code>
     * NOTE: don't copy paste this without replacing the STARss by a '*' (javadoc formatting issues)
     * 
	 * @param serviceTypeRegexpOrString Negatable regexp or name-matcher applied to the service-type
	 * @param nameOfServiceSelector Negatable regexp or name-matcher applied to the service-type
	 * @return The first of the selected credentials.
	 * @throws IllegalArgumentException if there is no selected service
	 */
	public static IVCapServiceCredentials getCredentialsOfService(IVCapServices services,
			String serviceTypeRegexpOrString,
			String nameOfServiceSelector) {
		NegatablePattern serviceTypeFilter = new NegatablePattern(serviceTypeRegexpOrString);
		NegatablePattern serviceNameFilter = new NegatablePattern(nameOfServiceSelector);
		ArrayList<IVCapService> selectedServices = services.getVCapServices(serviceTypeFilter, serviceNameFilter);
		return selectedServices.get(0).getCredentials();
	}

		
	/**
	 * @param service The service object. It contains a 'credentials' object.
	 * @throws JSONException
	 */
	public VCapServiceCredentials(JSONObject service, boolean resolveSysProperty) throws JSONException {
		_json = service.getJSONObject("credentials");
		_resolveSysProperty = resolveSysProperty;
		if (_json == null) {
			throw new IllegalArgumentException("Unable to find the 'credentials' object.");
		}
	}

	/**
	 * @return The name of the app service.
	 * For example, the database name or the top-level collection.
	 */
	public String getName() {
		return getString("name");
	}
	/**
	 * @return The IP or hostname where the service runs.
	 */
	public String getHostname() {
		return getString("hostname");
	}
	/**
	 * @return The port where it is accessible.
	 */
	public int getPort() {
		try {
			return _json.getInt("port");
		} catch (JSONException e) {
			if (_resolveSysProperty) {
				String portStr = getString("port");
				if (portStr != null) {
					return Integer.parseInt(portStr);
				}
			}
			return -1;
		}
	}
	/**
	 * @return The name of the user.
	 */
	public String getUser() {
		String result = getString("user");
		return result != null ? result : getString("username");
	}
	/**
	 * @return The name of the user.
	 */
	public String getUsername() {
		String result = getString("username");
		return result != null ? result : getString("user");
	}
	/**
	 * @return The password.
	 */
	public String getPassword() {
		return getString("password");
	}
	
	/**
	 * @return The db value or null when not defined. Used in mongodb.
	 */
	public String getDb() {
		return getString("db");
	}

        /**
         * @return a URI such as postgres://user:password@hostname/dbname
         */
        public URI asURI() {
             return null;
        }
	
	/**
	 * Returns the json string value for the given key.
	 * Takes care of resolving system properties if necessary.
	 * @param key
	 * @return
	 */
	private String getString(String key) {
		try {
			String str = _json.getString(key);
			if (str != null && _resolveSysProperty) {
				str = VCapService.resolvePropertyValue(str);
			}
			return str;
		} catch (JSONException e) {
			return null;
		}
	}
}
