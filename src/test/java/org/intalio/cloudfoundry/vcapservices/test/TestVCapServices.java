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
package org.intalio.cloudfoundry.vcapservices.test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;

import org.junit.*;
import org.intalio.cloudfoundry.vcapservices.IVCapServiceCredentials;
import org.intalio.cloudfoundry.vcapservices.impl.VCapServiceCredentials;
import org.intalio.cloudfoundry.vcapservices.impl.VCapServices;

/**
 * Very basic tests. For simplicity reason, we don't use junit so that we can
 * develop it in eclipse PDE in the same project without having to have the
 * bundle depend on junit.
 * 
 * @author hmalphettes
 */
public class TestVCapServices {

	@Test
	public void testOne() throws Exception {
		VCapServices vservices = new VCapServices(readAsString("example.json"),
				false);
		IVCapServiceCredentials cred = VCapServiceCredentials
				.getCredentialsOfFirstServiceRegex(vservices, "^postgres.*");
		if (cred == null) {
			throw new IllegalArgumentException();
		}
	}

	@Test
	public void testThree() throws Exception {
		VCapServices vservices = new VCapServices(
				readAsString("example3.json"), false);
		IVCapServiceCredentials cred = VCapServiceCredentials
				.getCredentialsOfFirstService(vservices, "/^postgres.*/");
		if (cred == null) {
			throw new IllegalArgumentException();
		}
		cred = VCapServiceCredentials.getCredentialsOfFirstService(vservices,
				"/^mongo.*/");
		if (cred == null) {
			throw new IllegalArgumentException();
		}
	}

	@Test
	public void testFour() throws Exception {
		VCapServices vservices = new VCapServices(
				readAsString("example4.json"), false);
		// Test the negated exact name
		IVCapServiceCredentials cred = VCapServiceCredentials
				.getCredentialsOfService(vservices, "/^postgres.*/",
						"!intalio-prodrdb2");
		if (cred == null) {
			throw new IllegalArgumentException();
		}
		if (!cred.getName().equals("intalio")) {
			throw new IllegalArgumentException(
					"Expecting the service name to be intalio instead got "
							+ cred.getName());
		}
		// Test the exact name
		cred = VCapServiceCredentials.getCredentialsOfService(vservices,
				"/^postgres.*/", "intalio-prodrdb2");
		if (cred == null) {
			throw new IllegalArgumentException();
		}
		if (!cred.getName().equals("intalio2")) {
			throw new IllegalArgumentException(
					"Expecting the service name to be intalio2 instead got "
							+ cred.getName());
		}
		// Test the name regexp encapsulated into a system property's default
		// value
		cred = VCapServiceCredentials.getCredentialsOfService(vservices,
				"/^postgres.*/", "/${SERVICE_NAME_REGEXP,.*prodrdb2$}/");
		if (cred == null) {
			throw new IllegalArgumentException();
		}
		if (!cred.getName().equals("intalio2")) {
			throw new IllegalArgumentException(
					"Expecting the service name to be intalio2 instead got "
							+ cred.getName());
		}
		// Test the negated name regexp encapsulated into a system property
		System.setProperty("SERVICE_NAME_REGEXP", "!/prodrdb/");
		cred = VCapServiceCredentials.getCredentialsOfService(vservices,
				"/^postgres.*/", "${SERVICE_NAME_REGEXP,/prodrdb/}");
		if (cred == null) {
			throw new IllegalArgumentException();
		}
		if (!cred.getName().equals("intalio")) {
			throw new IllegalArgumentException(
					"Expecting the service name to be intalio instead got "
							+ cred.getName());
		}
	}
	
	private static String EXAMPLE_POSTGRES_JDBC_URI_STR = 
			"jdbc:postgresql://postgres:postgres@localhost/postgres";
	private static URI EXAMPLE_POSTGRES_JDBC_URI = 
			URI.create(EXAMPLE_POSTGRES_JDBC_URI_STR);
	
	@Test
	public void testTwoAsURINoVCAP() throws Exception {
		System.setProperty("DATABASE_URL", EXAMPLE_POSTGRES_JDBC_URI_STR);
		URI conn = VCapServiceCredentials.getConnectionAsURI("DATABASE_URL",
				"jdbc:postgresql", "/^postgres.*/");
		Assert.assertNotNull(conn);
		Assert.assertEquals(EXAMPLE_POSTGRES_JDBC_URI, conn);
	}

	@Test
	public void testTwoAsURIWithVCAP() throws Exception {
		System.setProperty("DATABASE_URL", EXAMPLE_POSTGRES_JDBC_URI_STR);
		System.setProperty("VCAP_SERVICES", readAsString("example2.json"));
		URI conn = VCapServiceCredentials.getConnectionAsURI("DATABASE_URL",
				"jdbc:postgresql", "/^postgres.*/");
		Assert.assertNotNull(conn);
		URI expected = new URI("jdbc:postgresql://u90868e07a7344e069ac1c144622c4f3e" +
				":p3d886e97141c4c73adf14ec9b37d4c54" +
				"@172.30.48.121:5432" +
				"/d2df24941d49b4d7f99ac9739a9f5a4ca");
		Assert.assertEquals(expected, conn);
	}
	

	private static String readAsString(String filename)
			throws java.io.IOException {
		StringBuffer fileData = new StringBuffer(1000);
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				TestVCapServices.class.getResourceAsStream(filename)));
		char[] buf = new char[1024];
		int numRead = 0;
		while ((numRead = reader.read(buf)) != -1) {
			String readData = String.valueOf(buf, 0, numRead);
			fileData.append(readData);
			buf = new char[1024];
		}
		reader.close();
		return fileData.toString();
	}
}
