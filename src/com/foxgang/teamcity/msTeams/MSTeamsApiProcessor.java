/**
Copyright 2016 Tyler Evert

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

package com.foxgang.teamcity.msTeams;

import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Properties;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.MediaType;

public class MSTeamsApiProcessor {
	
	private MSTeamsConfiguration configuration;
	private Properties systemProperties;
	
	private static Logger logger = Logger.getLogger("com.foxgang.teamcity.msTeams");
	
	public MSTeamsApiProcessor(@NotNull MSTeamsConfiguration configuration) throws URISyntaxException {
		this(configuration, System.getProperties());
	}	
	
	public MSTeamsApiProcessor(@NotNull MSTeamsConfiguration configuration, Properties systemProperties) throws URISyntaxException {
		this.configuration = configuration;
		this.systemProperties = systemProperties;
	}
	
	public MSTeamsEmoticons getEmoticons(int startIndex) {
		try {
			URI uri = new URI(String.format("%s%s?start-index=%s", this.configuration.getApiUrl(), "emoticon", startIndex));
			String authorisationHeader = String.format("Bearer %s", this.configuration.getApiToken());

			// Make request
			HttpClient client = createClient();
			HttpGet getRequest = new HttpGet(uri.toString());
			getRequest.addHeader(HttpHeaders.AUTHORIZATION, authorisationHeader);
			getRequest.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString());
			HttpResponse getResponse = client.execute(getRequest);
			StatusLine status = getResponse.getStatusLine();
			if (status.getStatusCode() != HttpStatus.SC_OK) {
				logger.error(String.format("Could not retrieve emoticons: %s %s", status.getStatusCode(), status.getReasonPhrase()));
				return null;
			}
			
			Reader reader = new InputStreamReader(getResponse.getEntity().getContent());
			ObjectMapper mapper = new ObjectMapper();
			return mapper.readValue(reader, MSTeamsEmoticons.class);
		} catch (Exception e) {
			logger.error("Could not get emoticons", e);
		}
		
		return null;
	}
	
	public MSTeamsRooms getRooms(int startIndex) {
		try {
			URI uri = new URI(String.format("%s%s?start-index=%s", this.configuration.getApiUrl(), "room", startIndex));
			String authorisationHeader = String.format("Bearer %s", this.configuration.getApiToken());

			// Make request
			HttpClient client = createClient();
			HttpGet getRequest = new HttpGet(uri.toString());
			getRequest.addHeader(HttpHeaders.AUTHORIZATION, authorisationHeader);
			getRequest.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString());
			HttpResponse getResponse = client.execute(getRequest);
			StatusLine status = getResponse.getStatusLine();
			if (status.getStatusCode() != HttpStatus.SC_OK) {
				logger.error(String.format("Could not retrieve rooms: %s %s", status.getStatusCode(), status.getReasonPhrase()));
				return new MSTeamsRooms(new ArrayList<MSTeamsRoom>(), 0, 0, null);
			}
			
			Reader reader = new InputStreamReader(getResponse.getEntity().getContent());
			ObjectMapper mapper = new ObjectMapper();
			return mapper.readValue(reader, MSTeamsRooms.class);
		} catch (Exception e) {
			logger.error("Could not get rooms", e);
		}
		
		return new MSTeamsRooms(new ArrayList<MSTeamsRoom>(), 0, 0, null);
	}
	
	public void sendNotification(MSTeamsRoomNotification notification, String roomId) {
		try {
			String resource = String.format("room/%s/notification", roomId);
			URI uri = new URI(String.format("%s%s", this.configuration.getApiUrl(), resource));
			String authorisationHeader = String.format("Bearer %s", this.configuration.getApiToken());

			// Serialise the notification to JSON
			ObjectMapper mapper = new ObjectMapper();
			String json = mapper.writeValueAsString(notification);
			logger.debug(json);

			// Make request
			HttpClient client = createClient();
			HttpPost postRequest = new HttpPost(uri.toString());
			postRequest.addHeader(HttpHeaders.AUTHORIZATION, authorisationHeader);
			postRequest.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString());
			postRequest.setEntity(new StringEntity(json, Charset.forName("UTF-8")));
			HttpResponse postResponse = client.execute(postRequest);
			StatusLine status = postResponse.getStatusLine();
			if (status.getStatusCode() != HttpStatus.SC_NO_CONTENT) {
				logger.error(String.format("Message could not be delivered: %s %s", status.getStatusCode(), status.getReasonPhrase()));
			}
		} catch (Exception e) {
			logger.error("Could not post room notification", e);
		}
	}
	
	public boolean testAuthentication() {
		try {
			String resource = String.format("room?auth_token=%s&auth_test=true", this.configuration.getApiToken());
			URI uri = new URI(String.format("%s%s", this.configuration.getApiUrl(), resource));

			// Make request
			HttpClient client = createClient();
			HttpGet getRequest = new HttpGet(uri.toString());
			HttpResponse postResponse = client.execute(getRequest);
			StatusLine status = postResponse.getStatusLine();
			if (status.getStatusCode() == HttpStatus.SC_ACCEPTED) {
				return true;
			} else {
				logger.error(String.format("Authentication failed: %s %s", status.getStatusCode(), status.getReasonPhrase()));
			}
		} catch (Exception e) {
			logger.error("Request failed", e);
		}
		
		return false;		
	}

	private CloseableHttpClient createClient() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
		if (this.configuration.getBypassSslCheck()) {
			logger.warn("SSL check being bypassed");
			SSLContextBuilder sslContextBuilder = new SSLContextBuilder();
			sslContextBuilder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
			SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContextBuilder.build(), SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			HttpClientBuilder httpClientBuilder = HttpClients.custom().setSSLSocketFactory(socketFactory);
			CloseableHttpClient client = httpClientBuilder.build();
			return client;
		} else {
			HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
			String proxyHost = systemProperties.getProperty("http.proxyHost");
			if (proxyHost != null) {
				logger.info("Proxy configuration detected");
				logger.debug(String.format("Host: %s", proxyHost));
				int proxyPort = 80;
				String proxyPortString = systemProperties.getProperty("http.proxyPort");
				if (proxyPortString != null) {
					proxyPort = Integer.parseInt(proxyPortString);
				}
				HttpHost proxy = new HttpHost(proxyHost, proxyPort, "http");
				DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);
				httpClientBuilder.setRoutePlanner(routePlanner);
				logger.info(String.format("Proxy configured: %s:%s", proxyHost, proxyPort));
			}
			return httpClientBuilder.build();
		}
	}

}
