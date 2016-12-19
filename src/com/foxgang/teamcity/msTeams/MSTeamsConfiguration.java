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

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.*;

@XStreamAlias("msTeams")
public class MSTeamsConfiguration {

	public static final String API_TOKEN_KEY = "apiToken";
	public static final String API_URL_KEY = "apiUrl";
	public static final String DISABLED_STATUS_KEY = "disabled";
	public static final String NOTIFY_STATUS_KEY = "notify";
	public static final String DEFAULT_ROOM_ID_KEY = "defaultRoomId";
	public static final String DEFAULT_ROOM_ID_KEY_V0DOT1 = "roomId";
	public static final String SERVER_EVENT_ROOM_ID_KEY = "serverEventRoomId";
	public static final String ROOM_ID_KEY = "roomId";
	public static final String PROJECT_ID_KEY = "projectId";
	public static final String PROJECT_ROOM_KEY = "projectRoom";
	public static final String ROOM_ID_NONE_VALUE = "none";
	public static final String ROOM_ID_DEFAULT_VALUE = "default";
	public static final String ROOM_ID_PARENT_VALUE = "parent";
	public static final String IS_ROOT_PROJECT_KEY = "isRootProject";
	public static final String ROOT_PROJECT_ID_VALUE = "_Root";
	public static final String EVENTS_KEY = "events";
	public static final String BUILD_STARTED_KEY = "buildStarted";
	public static final String BUILD_SUCCESSFUL_KEY = "buildSuccessful";
	public static final String BUILD_FAILED_KEY = "buildFailed";
	public static final String BUILD_INTERRUPTED_KEY = "buildInterrupted";
	public static final String SERVER_STARTUP_KEY = "serverStartup";
	public static final String SERVER_SHUTDOWN_KEY = "serverShutdown";
	public static final String EMOTICON_CACHE_SIZE_KEY = "emoticonCacheSize";
	public static final String ONLY_AFTER_FIRST_BUILD_SUCCESSFUL_KEY = "onlyAfterFirstBuildSuccessful";
	public static final String ONLY_AFTER_FIRST_BUILD_FAILED_KEY = "onlyAfterFirstBuildFailed";
	public static final String BRANCH_FILTER_KEY = "branchFilter";
	public static final String BRANCH_FILTER_REGEX_KEY = "branchFilterRegex";
	public static final String BYPASS_SSL_CHECK = "bypassSslCheck";
	  
	@XStreamAlias(API_TOKEN_KEY)
	private String apiToken = null;

	@XStreamAlias(API_URL_KEY)
	private String apiUrl = "https://api.msTeams.com/v2/";

	@XStreamAlias(DISABLED_STATUS_KEY)
	private boolean disabled = false;

	@XStreamAlias(NOTIFY_STATUS_KEY)
	private boolean notify = false;

	@XStreamAlias(DEFAULT_ROOM_ID_KEY)
	private String defaultRoomId;
	
	@XStreamAlias(SERVER_EVENT_ROOM_ID_KEY)
	private String serverEventRoomId;
	
	// We use a list for correct serialization. It causes us to perform a linear search when getting or setting, but that's ok. 
	@XStreamImplicit
	private List<MSTeamsProjectConfiguration> projectRoomMap = new ArrayList<MSTeamsProjectConfiguration>();
	
	@XStreamAlias(MSTeamsConfiguration.EVENTS_KEY)
	private MSTeamsEventConfiguration events = new MSTeamsEventConfiguration();
	
	@XStreamAlias(BRANCH_FILTER_KEY)
	private boolean branchFilterEnabled;

	@XStreamAlias(BRANCH_FILTER_REGEX_KEY)
	private String branchFilterRegex;

	@XStreamAlias(BYPASS_SSL_CHECK)
	private boolean bypassSslCheck;
	
	public MSTeamsConfiguration() {
		// Intentionally left empty
	}

	public MSTeamsEventConfiguration getEvents() {
		return this.events;
	}
	
	public void setEvents(MSTeamsEventConfiguration events) {
		this.events = events;
	}
	
	public List<MSTeamsProjectConfiguration> getProjectRoomMap() {
		return this.projectRoomMap;
	}
	
	public void setProjectConfiguration(MSTeamsProjectConfiguration newProjectConfiguration) {
		boolean found = false;
		for (MSTeamsProjectConfiguration projectConfiguration : this.projectRoomMap) {
			if (projectConfiguration.getProjectId().contentEquals(newProjectConfiguration.getProjectId())) {
				projectConfiguration.setRoomId(newProjectConfiguration.getRoomId());
				projectConfiguration.setNotifyStatus(newProjectConfiguration.getNotifyStatus());
				found = true;
			}
		}
		if (!found) {
			this.projectRoomMap.add(newProjectConfiguration);		
		}
	}
	
	public MSTeamsProjectConfiguration getProjectConfiguration(String projectId) {
		for (MSTeamsProjectConfiguration projectConfiguration : this.projectRoomMap) {
			if (projectConfiguration.getProjectId().contentEquals(projectId)) {
				return projectConfiguration;
			}
		}
		return null;
	}
	
	public String getApiToken() {
		return this.apiToken;
	}

	public String getApiUrl() {
		return this.apiUrl;
	}

	public boolean getDisabledStatus() {
		return this.disabled;
	}

	public boolean getDefaultNotifyStatus() {
		return this.notify;
	}

	public String getDefaultRoomId() {
		return this.defaultRoomId;
	}

	public String getServerEventRoomId() {
		return this.serverEventRoomId;
	}

	public boolean getBranchFilterEnabledStatus() {
		return this.branchFilterEnabled;
	}

	public String getBranchFilterRegex() {
		return this.branchFilterRegex;
	}
	
	public boolean getBypassSslCheck() {
		return this.bypassSslCheck;
	}
		  
	public void setApiToken(String token) {
		this.apiToken = token;
	}

	public void setApiUrl(String url) {
		// TODO: Validate URL
		this.apiUrl = url;
	}

	public void setDisabledStatus(boolean status) {
		this.disabled = status;
	}

	public void setNotifyStatus(boolean status) {
		this.notify = status;
	}

	public void setDefaultRoomId(String roomId) {
		this.defaultRoomId = roomId;
	}
	
	public void setServerEventRoomId(String roomId) {
		this.serverEventRoomId = roomId;
	}
	
	public void setBranchFilterEnabledStatus(boolean status) {
		this.branchFilterEnabled = status;
	}

	public void setBranchFilterRegex(String regex) {
		this.branchFilterRegex = regex;
	}

	public void setBypassSslCheck(boolean bypassSslCheck) {
		this.bypassSslCheck = bypassSslCheck;
	}
	
}
