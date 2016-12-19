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

package com.foxgang.teamcity.msTeams.test;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URISyntaxException;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import jetbrains.buildServer.controllers.BaseControllerTestCase;
import jetbrains.buildServer.controllers.MockRequest;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.web.openapi.WebControllerManager;

import org.apache.http.HttpStatus;
import org.apache.log4j.BasicConfigurator;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import com.foxgang.teamcity.msTeams.MSTeamsApiProcessor;
import com.foxgang.teamcity.msTeams.MSTeamsConfiguration;
import com.foxgang.teamcity.msTeams.MSTeamsConfigurationController;
import com.foxgang.teamcity.msTeams.MSTeamsEmoticonCache;
import com.foxgang.teamcity.msTeams.MSTeamsEventConfiguration;
import com.foxgang.teamcity.msTeams.MSTeamsNotificationMessageTemplates;
import com.foxgang.teamcity.msTeams.MSTeamsProjectConfiguration;
import com.foxgang.teamcity.msTeams.TeamCityEvent;

import org.testng.annotations.BeforeClass;
import org.springframework.web.servlet.ModelAndView;

import freemarker.template.Template;
import freemarker.template.TemplateException;

public class MSTeamsConfigurationControllerTest extends BaseControllerTestCase<MSTeamsConfigurationController> {

	private MSTeamsConfiguration configuration;
	private MSTeamsApiProcessor processor;
	private MSTeamsNotificationMessageTemplates templates;
	private MSTeamsEmoticonCache emoticonCache;

	@BeforeClass
	public static void ClassSetup() {
		// Set up a basic logger for debugging purposes
		BasicConfigurator.configure();
	}

	@Test
	public void testProjectConfiguration() throws URISyntaxException, IOException {
		// Test parameters
		String expectedProjectId1 = "project1";
		String expectedProjectId2 = "project2";
		String expectedRoomId1 = "room1";
		String expectedRoomId2 = "room2";
		boolean expectedNotify1 = true;
		boolean expectedNotify2 = false;
		String expectedConfigDir = ".";

		// Mocks
		ServerPaths serverPaths = org.mockito.Mockito.mock(ServerPaths.class);
		when(serverPaths.getConfigDir()).thenReturn(expectedConfigDir);
		SBuildServer server = org.mockito.Mockito.mock(SBuildServer.class);
		WebControllerManager manager = org.mockito.Mockito.mock(WebControllerManager.class);
		MSTeamsEmoticonCache emoticonCache = org.mockito.Mockito.mock(MSTeamsEmoticonCache.class);
		MSTeamsNotificationMessageTemplates templates = org.mockito.Mockito.mock(MSTeamsNotificationMessageTemplates.class);

		// Prepare
		MSTeamsConfiguration configuration = new MSTeamsConfiguration();
		configuration.setProjectConfiguration(new MSTeamsProjectConfiguration(expectedProjectId1, expectedRoomId1, expectedNotify1));
		configuration.setProjectConfiguration(new MSTeamsProjectConfiguration(expectedProjectId2, expectedRoomId2, expectedNotify2));
		MSTeamsApiProcessor processor = new MSTeamsApiProcessor(configuration);
		MSTeamsConfigurationController controller = new MSTeamsConfigurationController(server, serverPaths, manager, configuration, processor, templates,
				emoticonCache);
		controller.saveConfiguration();

		// Execute
		configuration = new MSTeamsConfiguration();
		AssertJUnit.assertNull(configuration.getProjectConfiguration(expectedProjectId1));
		AssertJUnit.assertNull(configuration.getProjectConfiguration(expectedProjectId2));
		controller = new MSTeamsConfigurationController(server, serverPaths, manager, configuration, processor, templates, emoticonCache);
		controller.loadConfiguration();

		// Test
		MSTeamsProjectConfiguration projectConfiguration1 = configuration.getProjectConfiguration(expectedProjectId1);
		AssertJUnit.assertEquals(expectedRoomId1, projectConfiguration1.getRoomId());
		AssertJUnit.assertEquals(expectedNotify1, projectConfiguration1.getNotifyStatus());
		MSTeamsProjectConfiguration projectConfiguration2 = configuration.getProjectConfiguration(expectedProjectId2);
		AssertJUnit.assertEquals(expectedRoomId2, projectConfiguration2.getRoomId());
		AssertJUnit.assertEquals(expectedNotify2, projectConfiguration2.getNotifyStatus());
	}

	@Test
	public void testConfigurationFileGetsCreatedWhenNoneExists() throws IOException, JDOMException, ParserConfigurationException, TransformerException,
			URISyntaxException {
		// Test parameters
		String expectedFileName = "msTeams.xml";
		String expectedApiUrlKey = "apiUrl";
		String expectedApiUrlValue = "http://example.com/";
		String expectedApiUrlDefaultValue = "https://api.msTeams.com/v2/";
		String expectedApiTokenKey = "apiToken";
		String expectedApiTokenValue = "admin_token";
		String expectedRoomIdKey = "defaultRoomId";
		String expectedRoomIdValue = "room_id";
		String expectedNotifyStatusKey = "notify";
		boolean expectedNotifyStatusValue = true;
		String expectedDisabledStatusKey = "disabled";
		boolean expectedDisabledStatusValue = false;
		String expectedProjectRoomMapKey = "projectRoom";
		String expectedConfigDir = ".";

		// Mocks
		ServerPaths serverPaths = org.mockito.Mockito.mock(ServerPaths.class);
		when(serverPaths.getConfigDir()).thenReturn(expectedConfigDir);
		SBuildServer server = org.mockito.Mockito.mock(SBuildServer.class);
		WebControllerManager manager = org.mockito.Mockito.mock(WebControllerManager.class);
		MSTeamsEmoticonCache emoticonCache = org.mockito.Mockito.mock(MSTeamsEmoticonCache.class);
		MSTeamsNotificationMessageTemplates templates = org.mockito.Mockito.mock(MSTeamsNotificationMessageTemplates.class);

		// Pre-conditions
		File initialConfigFile = new File(expectedConfigDir, expectedFileName);
		initialConfigFile.delete();
		AssertJUnit.assertFalse(initialConfigFile.exists());
		MSTeamsConfiguration configuration = new MSTeamsConfiguration();
		AssertJUnit.assertEquals(expectedApiUrlDefaultValue, configuration.getApiUrl());
		AssertJUnit.assertNull(configuration.getApiToken());
		AssertJUnit.assertNull(configuration.getDefaultRoomId());
		AssertJUnit.assertFalse(configuration.getDefaultNotifyStatus());
		AssertJUnit.assertFalse(configuration.getDisabledStatus());
		AssertJUnit.assertEquals(0, configuration.getProjectRoomMap().size());

		// Execute
		// The config file must exist on disk after initialisation
		MSTeamsApiProcessor processor = new MSTeamsApiProcessor(configuration);
		MSTeamsConfigurationController controller = new MSTeamsConfigurationController(server, serverPaths, manager, configuration, processor, templates,
				emoticonCache);
		controller.initialise();
		File postRegistrationConfigFile = new File(expectedFileName);
		AssertJUnit.assertTrue(postRegistrationConfigFile.exists());

		// Check XML of the newly created config file
		SAXBuilder builder = new SAXBuilder();
		Document document = builder.build(postRegistrationConfigFile);
		Element rootElement = document.getRootElement();
		AssertJUnit.assertEquals(expectedApiUrlDefaultValue, rootElement.getChildText(expectedApiUrlKey));
		AssertJUnit.assertNull(rootElement.getChildText(expectedApiTokenKey));
		AssertJUnit.assertNull(rootElement.getChildText(expectedRoomIdKey));
		AssertJUnit.assertFalse(Boolean.parseBoolean(rootElement.getChildText(expectedNotifyStatusKey)));
		AssertJUnit.assertFalse(Boolean.parseBoolean(rootElement.getChildText(expectedDisabledStatusKey)));
		AssertJUnit.assertNull(rootElement.getChildText(expectedProjectRoomMapKey));

		// And the instance values must still be the defaults
		AssertJUnit.assertEquals(expectedApiUrlDefaultValue, configuration.getApiUrl());
		AssertJUnit.assertNull(configuration.getApiToken());
		AssertJUnit.assertNull(configuration.getDefaultRoomId());
		AssertJUnit.assertFalse(configuration.getDefaultNotifyStatus());
		AssertJUnit.assertFalse(configuration.getDisabledStatus());
		AssertJUnit.assertEquals(0, configuration.getProjectRoomMap().size());

		// Now change and save the configuration
		configuration.setApiUrl(expectedApiUrlValue);
		configuration.setApiToken(expectedApiTokenValue);
		configuration.setDefaultRoomId(expectedRoomIdValue);
		configuration.setNotifyStatus(expectedNotifyStatusValue);
		configuration.setDisabledStatus(expectedDisabledStatusValue);
		controller.saveConfiguration();

		// Check XML of the saved config file
		builder = new SAXBuilder();
		document = builder.build(postRegistrationConfigFile);
		rootElement = document.getRootElement();
		AssertJUnit.assertEquals(expectedApiUrlValue, rootElement.getChildText(expectedApiUrlKey));
		AssertJUnit.assertEquals(expectedApiTokenValue, rootElement.getChildText(expectedApiTokenKey));
		AssertJUnit.assertEquals(expectedRoomIdValue, rootElement.getChildText(expectedRoomIdKey));
		AssertJUnit.assertEquals(Boolean.valueOf(expectedNotifyStatusValue).toString(), rootElement.getChildText(expectedNotifyStatusKey));
		AssertJUnit.assertEquals(Boolean.valueOf(expectedDisabledStatusValue).toString(), rootElement.getChildText(expectedDisabledStatusKey));
		AssertJUnit.assertNull(rootElement.getChildText(expectedProjectRoomMapKey));

		// And also the values in memory
		AssertJUnit.assertEquals(expectedApiUrlValue, configuration.getApiUrl());
		AssertJUnit.assertEquals(expectedApiTokenValue, configuration.getApiToken());
		configuration.setDefaultRoomId(expectedRoomIdValue);
		configuration.setNotifyStatus(expectedNotifyStatusValue);
		AssertJUnit.assertEquals(expectedDisabledStatusValue, configuration.getDisabledStatus());
		AssertJUnit.assertEquals(0, configuration.getProjectRoomMap().size());
	}

	@Test
	public void testConfigurationGetsReadCorrectlyFromFileUponInitialisation() throws IOException, JDOMException, URISyntaxException {
		// Test parameters
		String expectedFileName = "msTeams.xml";
		String expectedApiUrlKey = "apiUrl";
		String expectedApiUrlValue = "http://example.com/";
		String expectedApiTokenKey = "apiToken";
		String expectedApiTokenValue = "admin_token";
		String expectedRoomIdKey = "defaultRoomId";
		String expectedRoomIdValue = "room_id";
		String expectedNotifyStatusKey = "notify";
		boolean expectedNotifyStatusValue = true;
		String expectedDisabledStatusKey = "disabled";
		boolean expectedDisabledStatusValue = false;
		String expectedProjectRoomMapKey = "projectRoom";
		String expectedbranchFilterKey = "branchFilter";
		boolean expectedBranchFilterValue = true;
		String expectedbranchFilterRegexKey = "branchFilterRegex";
		String expectedBranchFilterRegexValue = "foo|bar|bas";
		String expectedbypassSslCheckKey = "bypassSslCheck";
		boolean expectedBypassSslCheckValue = true;
		String expectedServerEventRoomIdKey = "serverEventRoomId";
		String expectedServerEventRoomIdValue = "serverRoomId";
		String expectedConfigDir = ".";

		// Mocks
		ServerPaths serverPaths = org.mockito.Mockito.mock(ServerPaths.class);
		when(serverPaths.getConfigDir()).thenReturn(expectedConfigDir);
		SBuildServer server = org.mockito.Mockito.mock(SBuildServer.class);
		WebControllerManager manager = org.mockito.Mockito.mock(WebControllerManager.class);
		MSTeamsEmoticonCache emoticonCache = org.mockito.Mockito.mock(MSTeamsEmoticonCache.class);
		MSTeamsNotificationMessageTemplates templates = org.mockito.Mockito.mock(MSTeamsNotificationMessageTemplates.class);
		// Pre-conditions
		// @formatter:off
		String configFileContent = 
				"<msTeams>" + 
				"<apiToken>" + expectedApiTokenValue + "</apiToken>" + 
				"<apiUrl>" + expectedApiUrlValue + "</apiUrl>" + 
				"<defaultRoomId>" + expectedRoomIdValue + "</defaultRoomId>" +
				"<notify>" + expectedNotifyStatusValue + "</notify>" +
				"<disabled>" + expectedDisabledStatusValue + "</disabled>" + 
				"<branchFilter>" + expectedBranchFilterValue + "</branchFilter>" + 
				"<branchFilterRegex>" + expectedBranchFilterRegexValue + "</branchFilterRegex>" + 
				"<bypassSslCheck>" + expectedBypassSslCheckValue + "</bypassSslCheck>" + 
				"<serverEventRoomId>" + expectedServerEventRoomIdValue + "</serverEventRoomId>" + 
				"</msTeams>";
		// @formatter:on
		File configFile = new File(expectedConfigDir, expectedFileName);
		configFile.delete();
		configFile.createNewFile();
		AssertJUnit.assertTrue(configFile.exists());
		FileWriter fileWriter = new FileWriter(configFile);
		fileWriter.write(configFileContent);
		fileWriter.flush();
		fileWriter.close();

		// Execute
		// The config file must must not have been overwritten on disk after
		// initialisation
		MSTeamsConfiguration configuration = new MSTeamsConfiguration();
		MSTeamsApiProcessor processor = new MSTeamsApiProcessor(configuration);
		MSTeamsConfigurationController controller = new MSTeamsConfigurationController(server, serverPaths, manager, configuration, processor, templates,
				emoticonCache);
		controller.initialise();
		File postInitConfigFile = new File(expectedConfigDir, expectedFileName);
		SAXBuilder builder = new SAXBuilder();
		Document document = builder.build(postInitConfigFile);
		Element rootElement = document.getRootElement();
		AssertJUnit.assertEquals(expectedApiUrlValue, rootElement.getChildText(expectedApiUrlKey));
		AssertJUnit.assertEquals(expectedApiTokenValue, rootElement.getChildText(expectedApiTokenKey));
		AssertJUnit.assertEquals(expectedRoomIdValue, rootElement.getChildText(expectedRoomIdKey));
		AssertJUnit.assertEquals(expectedNotifyStatusValue, Boolean.parseBoolean(rootElement.getChildText(expectedNotifyStatusKey)));
		AssertJUnit.assertEquals(expectedDisabledStatusValue, Boolean.parseBoolean(rootElement.getChildText(expectedDisabledStatusKey)));
		AssertJUnit.assertNull(rootElement.getChildText(expectedProjectRoomMapKey));
		AssertJUnit.assertEquals(expectedBranchFilterValue, Boolean.parseBoolean(rootElement.getChildText(expectedbranchFilterKey)));
		AssertJUnit.assertEquals(expectedBranchFilterRegexValue, rootElement.getChildText(expectedbranchFilterRegexKey));
		AssertJUnit.assertEquals(expectedBypassSslCheckValue, Boolean.parseBoolean(rootElement.getChildText(expectedbypassSslCheckKey)));
		AssertJUnit.assertEquals(expectedServerEventRoomIdValue, rootElement.getChildText(expectedServerEventRoomIdKey));
				
		// Now check the loaded configuration
		AssertJUnit.assertEquals(expectedApiUrlValue, configuration.getApiUrl());
		AssertJUnit.assertEquals(expectedApiTokenValue, configuration.getApiToken());
		AssertJUnit.assertEquals(expectedRoomIdValue, configuration.getDefaultRoomId());
		AssertJUnit.assertEquals(expectedNotifyStatusValue, configuration.getDefaultNotifyStatus());
		AssertJUnit.assertEquals(expectedDisabledStatusValue, configuration.getDisabledStatus());
		AssertJUnit.assertEquals(expectedBranchFilterValue, configuration.getBranchFilterEnabledStatus());
		AssertJUnit.assertEquals(expectedBranchFilterRegexValue, configuration.getBranchFilterRegex());
		AssertJUnit.assertEquals(expectedBypassSslCheckValue, configuration.getBypassSslCheck());
		AssertJUnit.assertEquals(expectedServerEventRoomIdValue, configuration.getServerEventRoomId());
		AssertJUnit.assertEquals(0, configuration.getProjectRoomMap().size());
	}

	@Test
	public void testConfigurationWithProjectRoomMapGetsReadCorrectlyFromFileUponInitialisation() throws IOException, JDOMException, URISyntaxException {
		// Test parameters
		String expectedFileName = "msTeams.xml";
		String expectedApiUrlKey = "apiUrl";
		String expectedApiUrlValue = "http://example.com/";
		String expectedApiTokenKey = "apiToken";
		String expectedApiTokenValue = "admin_token";
		String expectedRoomIdKey = "defaultRoomId";
		String expectedRoomIdValue = "room_id";
		String expectedNotifyStatusKey = "notify";
		boolean expectedNotifyStatusValue = true;
		String expectedDisabledStatusKey = "disabled";
		boolean expectedDisabledStatusValue = false;
		String expectedProjectRoomMapKey = "projectRoom";
		String expectedProjectIdKey = "projectId";
		String expectedProjectIdValue = "project1";
		String expectedProjectRoomIdKey = "roomId";
		String expectedConfigDir = ".";

		// Mocks
		ServerPaths serverPaths = org.mockito.Mockito.mock(ServerPaths.class);
		when(serverPaths.getConfigDir()).thenReturn(expectedConfigDir);
		SBuildServer server = org.mockito.Mockito.mock(SBuildServer.class);
		WebControllerManager manager = org.mockito.Mockito.mock(WebControllerManager.class);
		MSTeamsEmoticonCache emoticonCache = org.mockito.Mockito.mock(MSTeamsEmoticonCache.class);
		MSTeamsNotificationMessageTemplates templates = org.mockito.Mockito.mock(MSTeamsNotificationMessageTemplates.class);

		// Pre-conditions
		// @formatter:off
		String configFileContent = 
				"<msTeams>" + 
				"<apiToken>" + expectedApiTokenValue + "</apiToken>" + 
				"<apiUrl>" + expectedApiUrlValue + "</apiUrl>" + 
				"<defaultRoomId>" + expectedRoomIdValue + "</defaultRoomId>" +
				"<notify>" + expectedNotifyStatusValue + "</notify>" +
				"<disabled>" + expectedDisabledStatusValue + "</disabled>" + 
				"<projectRoom>" +
				  "<projectId>" +  expectedProjectIdValue + "</projectId>" + 
				  "<roomId>" +  expectedRoomIdValue + "</roomId>" + 
				  "<notify>" +  expectedNotifyStatusValue + "</notify>" + 
				"</projectRoom>" +
				"</msTeams>";
		// @formatter:on
		File configFile = new File(expectedConfigDir, expectedFileName);
		AssertJUnit.assertTrue(configFile.delete());
		AssertJUnit.assertTrue(configFile.createNewFile());
		AssertJUnit.assertTrue(configFile.exists());
		FileWriter fileWriter = new FileWriter(configFile);
		fileWriter.write(configFileContent);
		fileWriter.flush();
		fileWriter.close();

		// Execute
		// The config file must must not have been overwritten on disk after
		// initialisation
		MSTeamsConfiguration configuration = new MSTeamsConfiguration();
		MSTeamsApiProcessor processor = new MSTeamsApiProcessor(configuration);
		MSTeamsConfigurationController controller = new MSTeamsConfigurationController(server, serverPaths, manager, configuration, processor, templates,
				emoticonCache);
		controller.initialise();
		File postInitConfigFile = new File(expectedConfigDir, expectedFileName);
		SAXBuilder builder = new SAXBuilder();
		Document document = builder.build(postInitConfigFile);
		Element rootElement = document.getRootElement();
		AssertJUnit.assertEquals(expectedApiUrlValue, rootElement.getChildText(expectedApiUrlKey));
		AssertJUnit.assertEquals(expectedApiTokenValue, rootElement.getChildText(expectedApiTokenKey));
		AssertJUnit.assertEquals(expectedRoomIdValue, rootElement.getChildText(expectedRoomIdKey));
		AssertJUnit.assertEquals(expectedNotifyStatusValue, Boolean.parseBoolean(rootElement.getChildText(expectedNotifyStatusKey)));
		AssertJUnit.assertEquals(expectedDisabledStatusValue, Boolean.parseBoolean(rootElement.getChildText(expectedDisabledStatusKey)));
		AssertJUnit.assertTrue(rootElement.getChildText(expectedProjectRoomMapKey) != null);
		Element projectRoomElement = rootElement.getChild(expectedProjectRoomMapKey);
		AssertJUnit.assertEquals(expectedProjectIdValue, projectRoomElement.getChildText(expectedProjectIdKey));
		AssertJUnit.assertEquals(expectedRoomIdValue, projectRoomElement.getChildText(expectedProjectRoomIdKey));
		AssertJUnit.assertEquals(expectedNotifyStatusValue, Boolean.parseBoolean(projectRoomElement.getChildText(expectedNotifyStatusKey)));

		// Now check the loaded configuration
		AssertJUnit.assertEquals(expectedApiUrlValue, configuration.getApiUrl());
		AssertJUnit.assertEquals(expectedApiTokenValue, configuration.getApiToken());
		AssertJUnit.assertEquals(expectedRoomIdValue, configuration.getDefaultRoomId());
		AssertJUnit.assertEquals(expectedNotifyStatusValue, configuration.getDefaultNotifyStatus());
		AssertJUnit.assertEquals(expectedDisabledStatusValue, configuration.getDisabledStatus());
		AssertJUnit.assertEquals(1, configuration.getProjectRoomMap().size());
		MSTeamsProjectConfiguration projectConfiguration = configuration.getProjectConfiguration(expectedProjectIdValue);
		AssertJUnit.assertEquals(expectedRoomIdValue, projectConfiguration.getRoomId());
		AssertJUnit.assertEquals(expectedNotifyStatusValue, projectConfiguration.getNotifyStatus());
	}

	@Test
	public void testConfigurationWithEventsGetsReadCorrectlyFromFileUponInitialisation() throws IOException, JDOMException, URISyntaxException {
		// Test parameters
		String expectedFileName = "msTeams.xml";
		String expectedApiUrlKey = "apiUrl";
		String expectedApiUrlValue = "http://example.com/";
		String expectedApiTokenKey = "apiToken";
		String expectedApiTokenValue = "admin_token";
		String expectedRoomIdKey = "defaultRoomId";
		String expectedRoomIdValue = "room_id";
		String expectedNotifyStatusKey = "notify";
		boolean expectedNotifyStatusValue = true;
		String expectedDisabledStatusKey = "disabled";
		boolean expectedDisabledStatusValue = false;
		String expectedEventsKey = "events";
		String expectedbuildStartedKey = "buildStarted";
		String expectedbuildSuccessfulKey = "buildSuccessful";
		String expectedbuildFailedKey = "buildFailed";
		String expectedBuildInterruptedKey = "buildInterrupted";
		String expectedServerStartupKey = "serverStartup";
		String expectedServerShutdownKey = "serverShutdown";
		String expectedConfigDir = ".";
		MSTeamsConfiguration configuration = new MSTeamsConfiguration();
		boolean expectedBuildStartedValue = !configuration.getEvents().getBuildStartedStatus();
		boolean expectedBuildSuccessfulValue = !configuration.getEvents().getBuildSuccessfulStatus();
		boolean expectedBuildFailedValue = !configuration.getEvents().getBuildFailedStatus();
		boolean expectedBuildInterruptedValue = !configuration.getEvents().getBuildInterruptedStatus();
		boolean expectedServerStartupValue = !configuration.getEvents().getServerStartupStatus();
		boolean expectedServerShutdownValue = !configuration.getEvents().getServerShutdownStatus();

		// Mocks
		ServerPaths serverPaths = org.mockito.Mockito.mock(ServerPaths.class);
		when(serverPaths.getConfigDir()).thenReturn(expectedConfigDir);
		SBuildServer server = org.mockito.Mockito.mock(SBuildServer.class);
		WebControllerManager manager = org.mockito.Mockito.mock(WebControllerManager.class);
		MSTeamsEmoticonCache emoticonCache = org.mockito.Mockito.mock(MSTeamsEmoticonCache.class);
		MSTeamsNotificationMessageTemplates templates = org.mockito.Mockito.mock(MSTeamsNotificationMessageTemplates.class);

		// Pre-conditions
		// @formatter:off
		String configFileContent = 
				"<msTeams>" + 
				"<apiToken>" + expectedApiTokenValue + "</apiToken>" + 
				"<apiUrl>" + expectedApiUrlValue + "</apiUrl>" + 
				"<defaultRoomId>" + expectedRoomIdValue + "</defaultRoomId>" +
				"<notify>" + expectedNotifyStatusValue + "</notify>" +
				"<disabled>" + expectedDisabledStatusValue + "</disabled>" + 
				"<events>" +
				  "<buildStarted>" +  expectedBuildStartedValue + "</buildStarted>" + 
				  "<buildSuccessful>" +  expectedBuildSuccessfulValue + "</buildSuccessful>" + 
				  "<buildFailed>" +  expectedBuildFailedValue + "</buildFailed>" + 
				  "<buildInterrupted>" +  expectedBuildInterruptedValue + "</buildInterrupted>" + 
				  "<serverStartup>" +  expectedServerStartupValue + "</serverStartup>" + 
				  "<serverShutdown>" +  expectedServerShutdownValue + "</serverShutdown>" + 
				"</events>" +
				"</msTeams>";
		// @formatter:on
		File configFile = new File(expectedConfigDir, expectedFileName);
		configFile.delete();
		configFile.createNewFile();
		AssertJUnit.assertTrue(configFile.exists());
		FileWriter fileWriter = new FileWriter(configFile);
		fileWriter.write(configFileContent);
		fileWriter.flush();
		fileWriter.close();

		// Execute
		// The config file must must not have been overwritten on disk after
		// initialisation
		MSTeamsApiProcessor processor = new MSTeamsApiProcessor(configuration);
		MSTeamsConfigurationController controller = new MSTeamsConfigurationController(server, serverPaths, manager, configuration, processor, templates,
				emoticonCache);
		controller.initialise();
		File postInitConfigFile = new File(expectedConfigDir, expectedFileName);
		SAXBuilder builder = new SAXBuilder();
		Document document = builder.build(postInitConfigFile);
		Element rootElement = document.getRootElement();
		AssertJUnit.assertEquals(expectedApiUrlValue, rootElement.getChildText(expectedApiUrlKey));
		AssertJUnit.assertEquals(expectedApiTokenValue, rootElement.getChildText(expectedApiTokenKey));
		AssertJUnit.assertEquals(expectedRoomIdValue, rootElement.getChildText(expectedRoomIdKey));
		AssertJUnit.assertEquals(expectedNotifyStatusValue, Boolean.parseBoolean(rootElement.getChildText(expectedNotifyStatusKey)));
		AssertJUnit.assertEquals(expectedDisabledStatusValue, Boolean.parseBoolean(rootElement.getChildText(expectedDisabledStatusKey)));
		// Events
		Element eventsElement = rootElement.getChild(expectedEventsKey);
		AssertJUnit.assertEquals(expectedBuildStartedValue, Boolean.parseBoolean(eventsElement.getChildText(expectedbuildStartedKey)));
		AssertJUnit.assertEquals(expectedBuildSuccessfulValue, Boolean.parseBoolean(eventsElement.getChildText(expectedbuildSuccessfulKey)));
		AssertJUnit.assertEquals(expectedBuildFailedValue, Boolean.parseBoolean(eventsElement.getChildText(expectedbuildFailedKey)));
		AssertJUnit.assertEquals(expectedBuildInterruptedValue, Boolean.parseBoolean(eventsElement.getChildText(expectedBuildInterruptedKey)));
		AssertJUnit.assertEquals(expectedServerStartupValue, Boolean.parseBoolean(eventsElement.getChildText(expectedServerStartupKey)));
		AssertJUnit.assertEquals(expectedServerShutdownValue, Boolean.parseBoolean(eventsElement.getChildText(expectedServerShutdownKey)));

		// Now check the loaded configuration
		AssertJUnit.assertEquals(expectedApiUrlValue, configuration.getApiUrl());
		AssertJUnit.assertEquals(expectedApiTokenValue, configuration.getApiToken());
		AssertJUnit.assertEquals(expectedRoomIdValue, configuration.getDefaultRoomId());
		AssertJUnit.assertEquals(expectedNotifyStatusValue, configuration.getDefaultNotifyStatus());
		AssertJUnit.assertEquals(expectedDisabledStatusValue, configuration.getDisabledStatus());
		// Events
		AssertJUnit.assertEquals(expectedBuildStartedValue, configuration.getEvents().getBuildStartedStatus());
		AssertJUnit.assertEquals(expectedBuildSuccessfulValue, configuration.getEvents().getBuildSuccessfulStatus());
		AssertJUnit.assertEquals(expectedBuildFailedValue, configuration.getEvents().getBuildFailedStatus());
		AssertJUnit.assertEquals(expectedBuildInterruptedValue, configuration.getEvents().getBuildInterruptedStatus());
		AssertJUnit.assertEquals(expectedServerStartupValue, configuration.getEvents().getServerStartupStatus());
		AssertJUnit.assertEquals(expectedServerShutdownValue, configuration.getEvents().getServerShutdownStatus());
	}

	@Test
	public void testConfigurationGetsUpgradedFromV0dot1toV0dot2() throws IOException, JDOMException, URISyntaxException {
		// Test parameters
		String expectedDefaultRoomIdKey = "defaultRoomId";
		String expectedDefaultRoomIdValue = "12345";
		String expectedConfigDir = ".";
		String expectedConfigFileName = "msTeams.xml";
		// @formatter:off
		// roomId is the legacy key
		String v0dot1ConfigurationText = "<msTeams>\n" + 
								   "  <apiToken>token</apiToken>\n" + 
								   "  <apiUrl>https://api.msTeams.com/v2/</apiUrl>\n" + 
								   "  <disabled>false</disabled>\n" + 
								   "  <notify>true</notify>\n" + 
								   "  <roomId>" + expectedDefaultRoomIdValue + "</roomId>\n" +
								   "</msTeams>";
		// @formatter:on

		// Prepare
		File file = new File(expectedConfigDir, expectedConfigFileName);
		if (file.exists()) {
			AssertJUnit.assertTrue(file.delete());
		}
		file.createNewFile();
		System.out.println(String.format("Canonical path to config file for test: %s", file.getCanonicalPath()));
		FileWriter fileWriter = new FileWriter(file.getAbsoluteFile());
		fileWriter.write(v0dot1ConfigurationText);
		fileWriter.flush();
		fileWriter.close();

		// Mocks
		ServerPaths serverPaths = org.mockito.Mockito.mock(ServerPaths.class);
		when(serverPaths.getConfigDir()).thenReturn(expectedConfigDir);
		SBuildServer server = org.mockito.Mockito.mock(SBuildServer.class);
		WebControllerManager manager = org.mockito.Mockito.mock(WebControllerManager.class);
		MSTeamsEmoticonCache emoticonCache = org.mockito.Mockito.mock(MSTeamsEmoticonCache.class);
		MSTeamsNotificationMessageTemplates templates = org.mockito.Mockito.mock(MSTeamsNotificationMessageTemplates.class);

		// After initialisation, the config must've been upgraded
		MSTeamsConfiguration configuration = new MSTeamsConfiguration();
		MSTeamsApiProcessor processor = new MSTeamsApiProcessor(configuration);
		MSTeamsConfigurationController controller = new MSTeamsConfigurationController(server, serverPaths, manager, configuration, processor, templates,
				emoticonCache);
		controller.initialise();

		// Test XML was upgraded
		File configFile = new File(expectedConfigDir, expectedConfigFileName);
		SAXBuilder builder = new SAXBuilder();
		Document document = builder.build(configFile);
		Element rootElement = document.getRootElement();
		AssertJUnit.assertEquals(expectedDefaultRoomIdValue, rootElement.getChildText(expectedDefaultRoomIdKey));

		// Test config object contains value for room ID
		AssertJUnit.assertEquals(expectedDefaultRoomIdValue, configuration.getDefaultRoomId());
		// Re-read the config from disk
		controller.initialise();
		AssertJUnit.assertEquals(expectedDefaultRoomIdValue, configuration.getDefaultRoomId());
	}

	@Test
	public void testNoParameterMatch() throws Exception {
		// Mocks
		MockRequest request = new MockRequest();
		request.addParameters("foo", "bar");
		this.myRequest = request;

		// Execute
		ModelAndView result = processRequest();

		// Test
		AssertJUnit.assertNull(result);
	}

	@Test
	public void testProjectConfigurationChange() throws Exception {
		// Test parameters
		String expectedRoomId = "room1";
		boolean expectedNotifyStatus = false;
		String expectedProjectId = "project1";

		// Mocks
		MockRequest request = new MockRequest();
		request.addParameters("project", "1");
		request.addParameters("roomId", expectedRoomId);
		request.addParameters("notify", Boolean.valueOf(expectedNotifyStatus).toString());
		request.addParameters("projectId", expectedProjectId);
		this.myRequest = request;

		// Execute
		ModelAndView result = processRequest();

		// Test
		AssertJUnit.assertNull(result);
		MSTeamsProjectConfiguration actualProjectConfiguration = this.configuration.getProjectConfiguration(expectedProjectId);
		AssertJUnit.assertEquals(expectedRoomId, actualProjectConfiguration.getRoomId());
		AssertJUnit.assertEquals(expectedNotifyStatus, actualProjectConfiguration.getNotifyStatus());
	}

	@Test
	public void testEnablePlugin() throws Exception {
		// Test parameters
		boolean expectedPluginDisabledStatus = false;

		// Mocks
		MockRequest request = new MockRequest();
		request.addParameters("action", "enable");
		this.myRequest = request;

		// Execute
		this.configuration.setDisabledStatus(!expectedPluginDisabledStatus);
		ModelAndView result = processRequest();

		// Test
		AssertJUnit.assertNull(result);
		AssertJUnit.assertEquals(expectedPluginDisabledStatus, this.configuration.getDisabledStatus());
	}

	@Test
	public void testDisablePlugin() throws Exception {
		// Test parameters
		boolean expectedPluginDisabledStatus = true;

		// Mocks
		MockRequest request = new MockRequest();
		request.addParameters("action", "disable");
		this.myRequest = request;

		// Execute
		this.configuration.setDisabledStatus(!expectedPluginDisabledStatus);
		ModelAndView result = processRequest();

		// Test
		AssertJUnit.assertNull(result);
		AssertJUnit.assertEquals(expectedPluginDisabledStatus, this.configuration.getDisabledStatus());
	}

	@Test
	public void testTestConnectionSuccessful() throws Exception {
		// Mocks
		MockRequest request = new MockRequest();
		request.addParameters("test", "1");
		request.addParameters("apiUrl", "http://example.com/");
		request.addParameters("apiToken", "1234567890");
		this.myRequest = request;

		// Execute
		when(this.processor.testAuthentication()).thenReturn(true);
		ModelAndView result = processRequest();

		// Test
		AssertJUnit.assertNull(result);
		AssertJUnit.assertEquals(HttpStatus.SC_OK, this.myResponse.getStatus());
	}

	@Test
	public void testTestConnectionFailure() throws Exception {
		// Mocks
		MockRequest request = new MockRequest();
		request.addParameters("test", "1");
		request.addParameters("apiUrl", "http://example.com/");
		request.addParameters("apiToken", "1234567890");
		this.myRequest = request;

		// Execute
		when(this.processor.testAuthentication()).thenReturn(false);
		ModelAndView result = processRequest();

		// Test
		AssertJUnit.assertNull(result);
		AssertJUnit.assertEquals(HttpStatus.SC_BAD_REQUEST, this.myResponse.getStatus());
	}

	@Test
	public void testConfigurationChange() throws Exception {
		// Test parameters
		String expectedApiUrl = "http://example.com/";
		String expectedApiToken = "1234567890";
		String expectedDefaultRoomId = "room1";
		String expectedServerEventRoomId = "room2";
		boolean expectedNotifyStatus = false;
		boolean expectedEventStatus = true;
		String expectedTemplate = "template";
		boolean expectedBranchFilter = true;
		String expectedBranchFilterRegex = "master";

		// Prepare
		MSTeamsEventConfiguration events = new MSTeamsEventConfiguration();
		events.setBuildStartedStatus(!expectedEventStatus);
		events.setBuildSuccessfulStatus(!expectedEventStatus);
		events.setBuildInterruptedStatus(!expectedEventStatus);
		events.setBuildFailedStatus(!expectedEventStatus);
		events.setServerStartupStatus(!expectedEventStatus);
		events.setServerShutdownStatus(!expectedEventStatus);
		// Global
		this.configuration.setEvents(events);
		this.configuration.setApiUrl("test");
		this.configuration.setApiToken("test");
		this.configuration.setDefaultRoomId("test");
		this.configuration.setNotifyStatus(!expectedNotifyStatus);
		this.configuration.setBranchFilterEnabledStatus(!expectedBranchFilter);
		this.configuration.setBranchFilterRegex("test");
		// Templates
		this.templates.writeTemplate(TeamCityEvent.BUILD_STARTED, "test");
		this.templates.writeTemplate(TeamCityEvent.BUILD_SUCCESSFUL, "test");
		this.templates.writeTemplate(TeamCityEvent.BUILD_INTERRUPTED, "test");
		this.templates.writeTemplate(TeamCityEvent.BUILD_FAILED, "test");
		this.templates.writeTemplate(TeamCityEvent.SERVER_STARTUP, "test");
		this.templates.writeTemplate(TeamCityEvent.SERVER_SHUTDOWN, "test");

		// Mocks
		MockRequest request = new MockRequest();
		request.addParameters("edit", "1");
		request.addParameters("apiUrl", expectedApiUrl);
		request.addParameters("apiToken", expectedApiToken);
		request.addParameters("defaultRoomId", expectedDefaultRoomId);
		request.addParameters("serverEventRoomId", expectedServerEventRoomId);
		request.addParameters("branchFilter", Boolean.valueOf(expectedBranchFilter));
		request.addParameters("branchFilterRegex", expectedBranchFilterRegex);
		// Events
		request.addParameters("buildStarted", Boolean.valueOf(expectedEventStatus));
		request.addParameters("buildSuccessful", Boolean.valueOf(expectedEventStatus));
		request.addParameters("buildFailed", Boolean.valueOf(expectedEventStatus));
		request.addParameters("buildInterrupted", Boolean.valueOf(expectedEventStatus));
		request.addParameters("serverStartup", Boolean.valueOf(expectedEventStatus));
		request.addParameters("serverShutdown", Boolean.valueOf(expectedEventStatus));
		// Templates
		request.addParameters("buildStartedTemplate", expectedTemplate);
		request.addParameters("buildSuccessfulTemplate", expectedTemplate);
		request.addParameters("buildFailedTemplate", expectedTemplate);
		request.addParameters("buildInterruptedTemplate", expectedTemplate);
		request.addParameters("serverStartupTemplate", expectedTemplate);
		request.addParameters("serverShutdownTemplate", expectedTemplate);
		this.myRequest = request;

		// Execute
		ModelAndView result = processRequest();

		// Test
		AssertJUnit.assertNull(result);
		AssertJUnit.assertEquals(expectedApiUrl, this.configuration.getApiUrl());
		AssertJUnit.assertEquals(expectedApiToken, this.configuration.getApiToken());
		AssertJUnit.assertEquals(expectedDefaultRoomId, this.configuration.getDefaultRoomId());
		AssertJUnit.assertEquals(expectedServerEventRoomId, this.configuration.getServerEventRoomId());
		AssertJUnit.assertEquals(expectedNotifyStatus, this.configuration.getDefaultNotifyStatus());
		AssertJUnit.assertEquals(expectedBranchFilter, this.configuration.getBranchFilterEnabledStatus());
		AssertJUnit.assertEquals(expectedBranchFilterRegex, this.configuration.getBranchFilterRegex());
		// Events
		MSTeamsEventConfiguration actualEvents = this.configuration.getEvents();
		AssertJUnit.assertEquals(expectedEventStatus, actualEvents.getBuildStartedStatus());
		AssertJUnit.assertEquals(expectedEventStatus, actualEvents.getBuildSuccessfulStatus());
		AssertJUnit.assertEquals(expectedEventStatus, actualEvents.getBuildInterruptedStatus());
		AssertJUnit.assertEquals(expectedEventStatus, actualEvents.getBuildFailedStatus());
		AssertJUnit.assertEquals(expectedEventStatus, actualEvents.getServerStartupStatus());
		AssertJUnit.assertEquals(expectedEventStatus, actualEvents.getServerShutdownStatus());
		// Templates
		System.out.println(expectedTemplate);
		System.out.println(this.templates.readTemplate(TeamCityEvent.BUILD_STARTED));
		AssertJUnit.assertTrue(expectedTemplate.equals(renderTemplate(this.templates.readTemplate(TeamCityEvent.BUILD_STARTED))));
		AssertJUnit.assertTrue(expectedTemplate.equals(renderTemplate(this.templates.readTemplate(TeamCityEvent.BUILD_SUCCESSFUL))));
		AssertJUnit.assertTrue(expectedTemplate.equals(renderTemplate(this.templates.readTemplate(TeamCityEvent.BUILD_INTERRUPTED))));
		AssertJUnit.assertTrue(expectedTemplate.equals(renderTemplate(this.templates.readTemplate(TeamCityEvent.BUILD_FAILED))));
		AssertJUnit.assertTrue(expectedTemplate.equals(renderTemplate(this.templates.readTemplate(TeamCityEvent.SERVER_STARTUP))));
		AssertJUnit.assertTrue(expectedTemplate.equals(renderTemplate(this.templates.readTemplate(TeamCityEvent.SERVER_SHUTDOWN))));
	}

	@Test
	public void testConfigurationTemplateValidationFails() throws Exception {
		// Issue #39
		// Test parameters
		String expectedApiUrl = "http://example.com/";
		String expectedApiToken = "1234567890";
		String expectedDefaultRoomId = "room1";
		String expectedServerEventRoomId = "room2";
		boolean expectedEventStatus = true;
		// The if's condition is faulty as the variable is wrapped in ${...}
		String testTemplate = "<#if ${.data_model[\"system.deployBuildNumber\"]}> ${.data_model[\"system.deployBuildNumber\"]} is being deployed</#if>";
		boolean expectedBranchFilter = true;
		String expectedBranchFilterRegex = "master";

		// Prepare
		MSTeamsEventConfiguration events = new MSTeamsEventConfiguration();
		// Global
		this.configuration.setEvents(events);
		this.configuration.setBranchFilterEnabledStatus(!expectedBranchFilter);
		this.configuration.setBranchFilterRegex("test");

		// Mocks
		MockRequest request = new MockRequest();
		request.addParameters("edit", "1");
		request.addParameters("apiUrl", expectedApiUrl);
		request.addParameters("apiToken", expectedApiToken);
		request.addParameters("defaultRoomId", expectedDefaultRoomId);
		request.addParameters("serverEventRoomId", expectedServerEventRoomId);
		request.addParameters("branchFilter", Boolean.valueOf(expectedBranchFilter));
		request.addParameters("branchFilterRegex", expectedBranchFilterRegex);
		// Events
		request.addParameters("buildStarted", Boolean.valueOf(expectedEventStatus));
		request.addParameters("buildSuccessful", Boolean.valueOf(expectedEventStatus));
		request.addParameters("buildFailed", Boolean.valueOf(expectedEventStatus));
		request.addParameters("buildInterrupted", Boolean.valueOf(expectedEventStatus));
		request.addParameters("serverStartup", Boolean.valueOf(expectedEventStatus));
		request.addParameters("serverShutdown", Boolean.valueOf(expectedEventStatus));
		// Templates
		request.addParameters("buildStartedTemplate", testTemplate);
		request.addParameters("buildSuccessfulTemplate", testTemplate);
		request.addParameters("buildFailedTemplate", testTemplate);
		request.addParameters("buildInterruptedTemplate", testTemplate);
		request.addParameters("serverStartupTemplate", testTemplate);
		request.addParameters("serverShutdownTemplate", testTemplate);
		this.myRequest = request;

		// Execute -- and don't blow up
		processRequest();
	}

	@Test
	public void testConfigurationEmptyBranchFilterRegexTranslatesToNull() throws Exception {
		// Test parameters
		String expectedTemplate = "template";
		boolean expectedBranchFilter = true;
		String expectedBranchFilterRegex = null;
		String inputBranchFilterRegex = "";
		String expectedApiUrl = "http://example.com/";
		String expectedApiToken = "1234567890";
		String expectedDefaultRoomId = "room1";
		String expectedServerEventRoomId = "room2";
		boolean expectedNotifyStatus = false;
		boolean expectedEventStatus = true;

		// Prepare
		MSTeamsEventConfiguration events = new MSTeamsEventConfiguration();
		events.setBuildStartedStatus(!expectedEventStatus);
		events.setBuildSuccessfulStatus(!expectedEventStatus);
		events.setBuildInterruptedStatus(!expectedEventStatus);
		events.setBuildFailedStatus(!expectedEventStatus);
		events.setServerStartupStatus(!expectedEventStatus);
		events.setServerShutdownStatus(!expectedEventStatus);
		// Global
		this.configuration.setEvents(events);
		this.configuration.setApiUrl("test");
		this.configuration.setApiToken("test");
		this.configuration.setDefaultRoomId("test");
		this.configuration.setNotifyStatus(!expectedNotifyStatus);
		this.configuration.setBranchFilterEnabledStatus(!expectedBranchFilter);
		this.configuration.setBranchFilterRegex("test");
		// Templates
		this.templates.writeTemplate(TeamCityEvent.BUILD_STARTED, "test");
		this.templates.writeTemplate(TeamCityEvent.BUILD_SUCCESSFUL, "test");
		this.templates.writeTemplate(TeamCityEvent.BUILD_INTERRUPTED, "test");
		this.templates.writeTemplate(TeamCityEvent.BUILD_FAILED, "test");
		this.templates.writeTemplate(TeamCityEvent.SERVER_STARTUP, "test");
		this.templates.writeTemplate(TeamCityEvent.SERVER_SHUTDOWN, "test");

		// Mocks
		MockRequest request = new MockRequest();
		request.addParameters("edit", "1");
		request.addParameters("apiUrl", expectedApiUrl);
		request.addParameters("apiToken", expectedApiToken);
		request.addParameters("defaultRoomId", expectedDefaultRoomId);
		request.addParameters("serverEventRoomId", expectedServerEventRoomId);
		request.addParameters("branchFilter", Boolean.valueOf(expectedBranchFilter));
		request.addParameters("branchFilterRegex", inputBranchFilterRegex);
		// Events
		request.addParameters("buildStarted", Boolean.valueOf(expectedEventStatus));
		request.addParameters("buildSuccessful", Boolean.valueOf(expectedEventStatus));
		request.addParameters("buildFailed", Boolean.valueOf(expectedEventStatus));
		request.addParameters("buildInterrupted", Boolean.valueOf(expectedEventStatus));
		request.addParameters("serverStartup", Boolean.valueOf(expectedEventStatus));
		request.addParameters("serverShutdown", Boolean.valueOf(expectedEventStatus));
		// Templates
		request.addParameters("buildStartedTemplate", expectedTemplate);
		request.addParameters("buildSuccessfulTemplate", expectedTemplate);
		request.addParameters("buildFailedTemplate", expectedTemplate);
		request.addParameters("buildInterruptedTemplate", expectedTemplate);
		request.addParameters("serverStartupTemplate", expectedTemplate);
		request.addParameters("serverShutdownTemplate", expectedTemplate);
		this.myRequest = request;

		// Execute
		ModelAndView result = processRequest();

		// Test
		AssertJUnit.assertNull(result);
		AssertJUnit.assertEquals(expectedApiUrl, this.configuration.getApiUrl());
		AssertJUnit.assertEquals(expectedApiToken, this.configuration.getApiToken());
		AssertJUnit.assertEquals(expectedDefaultRoomId, this.configuration.getDefaultRoomId());
		AssertJUnit.assertEquals(expectedServerEventRoomId, this.configuration.getServerEventRoomId());
		AssertJUnit.assertEquals(expectedNotifyStatus, this.configuration.getDefaultNotifyStatus());
		AssertJUnit.assertEquals(expectedBranchFilter, this.configuration.getBranchFilterEnabledStatus());
		AssertJUnit.assertEquals(expectedBranchFilterRegex, this.configuration.getBranchFilterRegex());
		// Events
		MSTeamsEventConfiguration actualEvents = this.configuration.getEvents();
		AssertJUnit.assertEquals(expectedEventStatus, actualEvents.getBuildStartedStatus());
		AssertJUnit.assertEquals(expectedEventStatus, actualEvents.getBuildSuccessfulStatus());
		AssertJUnit.assertEquals(expectedEventStatus, actualEvents.getBuildInterruptedStatus());
		AssertJUnit.assertEquals(expectedEventStatus, actualEvents.getBuildFailedStatus());
		AssertJUnit.assertEquals(expectedEventStatus, actualEvents.getServerStartupStatus());
		AssertJUnit.assertEquals(expectedEventStatus, actualEvents.getServerShutdownStatus());
		// Templates
		System.out.println(expectedTemplate);
		System.out.println(this.templates.readTemplate(TeamCityEvent.BUILD_STARTED));
		AssertJUnit.assertTrue(expectedTemplate.equals(renderTemplate(this.templates.readTemplate(TeamCityEvent.BUILD_STARTED))));
		AssertJUnit.assertTrue(expectedTemplate.equals(renderTemplate(this.templates.readTemplate(TeamCityEvent.BUILD_SUCCESSFUL))));
		AssertJUnit.assertTrue(expectedTemplate.equals(renderTemplate(this.templates.readTemplate(TeamCityEvent.BUILD_INTERRUPTED))));
		AssertJUnit.assertTrue(expectedTemplate.equals(renderTemplate(this.templates.readTemplate(TeamCityEvent.BUILD_FAILED))));
		AssertJUnit.assertTrue(expectedTemplate.equals(renderTemplate(this.templates.readTemplate(TeamCityEvent.SERVER_STARTUP))));
		AssertJUnit.assertTrue(expectedTemplate.equals(renderTemplate(this.templates.readTemplate(TeamCityEvent.SERVER_SHUTDOWN))));
	}

	@Test
	public void testConfigurationChangeNullRoomId() throws Exception {
		// Test parameters
		String expectedApiUrl = "http://example.com/";
		String expectedApiToken = "1234567890";
		String expectedDefaultRoomId = null;
		boolean expectedNotifyStatus = false;
		boolean expectedEventStatus = true;
		String expectedTemplate = "template";
        boolean expectedBranchFilter = true;
        String expectedBranchFilterRegex = "master";

		// Prepare
		MSTeamsEventConfiguration events = new MSTeamsEventConfiguration();
		events.setBuildStartedStatus(!expectedEventStatus);
		events.setBuildSuccessfulStatus(!expectedEventStatus);
		events.setBuildInterruptedStatus(!expectedEventStatus);
		events.setBuildFailedStatus(!expectedEventStatus);
		events.setServerStartupStatus(!expectedEventStatus);
		events.setServerShutdownStatus(!expectedEventStatus);
		// Global
		this.configuration.setEvents(events);
		this.configuration.setApiUrl("test");
		this.configuration.setApiToken("test");
		this.configuration.setDefaultRoomId("test");
		this.configuration.setNotifyStatus(!expectedNotifyStatus);
        this.configuration.setBranchFilterEnabledStatus(!expectedBranchFilter);
        this.configuration.setBranchFilterRegex("test");
		// Templates
		this.templates.writeTemplate(TeamCityEvent.BUILD_STARTED, "test");
		this.templates.writeTemplate(TeamCityEvent.BUILD_SUCCESSFUL, "test");
		this.templates.writeTemplate(TeamCityEvent.BUILD_INTERRUPTED, "test");
		this.templates.writeTemplate(TeamCityEvent.BUILD_FAILED, "test");
		this.templates.writeTemplate(TeamCityEvent.SERVER_STARTUP, "test");
		this.templates.writeTemplate(TeamCityEvent.SERVER_SHUTDOWN, "test");

		// Mocks
		MockRequest request = new MockRequest();
		request.addParameters("edit", "1");
		request.addParameters("apiUrl", expectedApiUrl);
		request.addParameters("apiToken", expectedApiToken);
		request.addParameters("defaultRoomId", "");
		request.addParameters("serverEventRoomId", "");
		request.addParameters("notify", Boolean.valueOf(expectedNotifyStatus));
		request.addParameters("branchFilter", Boolean.valueOf(expectedBranchFilter));
		request.addParameters("branchFilterRegex", expectedBranchFilterRegex);
		// Events
		request.addParameters("buildStarted", Boolean.valueOf(expectedEventStatus));
		request.addParameters("buildSuccessful", Boolean.valueOf(expectedEventStatus));
		request.addParameters("buildFailed", Boolean.valueOf(expectedEventStatus));
		request.addParameters("buildInterrupted", Boolean.valueOf(expectedEventStatus));
		request.addParameters("serverStartup", Boolean.valueOf(expectedEventStatus));
		request.addParameters("serverShutdown", Boolean.valueOf(expectedEventStatus));
		// Templates
		request.addParameters("buildStartedTemplate", expectedTemplate);
		request.addParameters("buildSuccessfulTemplate", expectedTemplate);
		request.addParameters("buildFailedTemplate", expectedTemplate);
		request.addParameters("buildInterruptedTemplate", expectedTemplate);
		request.addParameters("serverStartupTemplate", expectedTemplate);
		request.addParameters("serverShutdownTemplate", expectedTemplate);
		this.myRequest = request;

		// Execute
		ModelAndView result = processRequest();

		// Test
		AssertJUnit.assertNull(result);
		AssertJUnit.assertEquals(expectedApiUrl, this.configuration.getApiUrl());
		AssertJUnit.assertEquals(expectedApiToken, this.configuration.getApiToken());
		AssertJUnit.assertEquals(expectedDefaultRoomId, this.configuration.getDefaultRoomId());
		AssertJUnit.assertEquals(expectedNotifyStatus, this.configuration.getDefaultNotifyStatus());
		// Events
		MSTeamsEventConfiguration actualEvents = this.configuration.getEvents();
		AssertJUnit.assertEquals(expectedEventStatus, actualEvents.getBuildStartedStatus());
		AssertJUnit.assertEquals(expectedEventStatus, actualEvents.getBuildSuccessfulStatus());
		AssertJUnit.assertEquals(expectedEventStatus, actualEvents.getBuildInterruptedStatus());
		AssertJUnit.assertEquals(expectedEventStatus, actualEvents.getBuildFailedStatus());
		AssertJUnit.assertEquals(expectedEventStatus, actualEvents.getServerStartupStatus());
		AssertJUnit.assertEquals(expectedEventStatus, actualEvents.getServerShutdownStatus());
		// Templates
		System.out.println(expectedTemplate);
		System.out.println(this.templates.readTemplate(TeamCityEvent.BUILD_STARTED));
		AssertJUnit.assertTrue(expectedTemplate.equals(renderTemplate(this.templates.readTemplate(TeamCityEvent.BUILD_STARTED))));
		AssertJUnit.assertTrue(expectedTemplate.equals(renderTemplate(this.templates.readTemplate(TeamCityEvent.BUILD_SUCCESSFUL))));
		AssertJUnit.assertTrue(expectedTemplate.equals(renderTemplate(this.templates.readTemplate(TeamCityEvent.BUILD_INTERRUPTED))));
		AssertJUnit.assertTrue(expectedTemplate.equals(renderTemplate(this.templates.readTemplate(TeamCityEvent.BUILD_FAILED))));
		AssertJUnit.assertTrue(expectedTemplate.equals(renderTemplate(this.templates.readTemplate(TeamCityEvent.SERVER_STARTUP))));
		AssertJUnit.assertTrue(expectedTemplate.equals(renderTemplate(this.templates.readTemplate(TeamCityEvent.SERVER_SHUTDOWN))));
	}

	@Test
	public void testReloadEmoticons() throws Exception {
		// Mocks
		MockRequest request = new MockRequest();
		request.addParameters("reloadEmoticons", "1");
		this.myRequest = request;

		// Execute
		ModelAndView result = processRequest();

		// Test
		AssertJUnit.assertNull(result);
		org.mockito.Mockito.verify(this.emoticonCache, times(1)).reload();
	}

	@Override
	protected MSTeamsConfigurationController createController() throws IOException {
		try {
			ServerPaths serverPaths = org.mockito.Mockito.mock(ServerPaths.class);
			when(serverPaths.getConfigDir()).thenReturn(".");
			this.emoticonCache = org.mockito.Mockito.mock(MSTeamsEmoticonCache.class);
			this.configuration = new MSTeamsConfiguration();
			this.templates = new MSTeamsNotificationMessageTemplates(serverPaths);
			this.processor = org.mockito.Mockito.mock(MSTeamsApiProcessor.class);
			return new MSTeamsConfigurationController(this.myServer, serverPaths, this.myWebManager, configuration, processor, templates, emoticonCache);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private static String renderTemplate(Template template) throws TemplateException, IOException {
		HashMap<String, Object> templateMap = new HashMap<String, Object>();
		Writer writer = new StringWriter();
		template.process(templateMap, writer);
		writer.flush();
		String renderedTemplate = writer.toString();
		writer.close();
		return renderedTemplate;
	}
}
