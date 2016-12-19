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

import com.thoughtworks.xstream.annotations.XStreamAlias;

public class MSTeamsEventConfiguration {
	
	@XStreamAlias(MSTeamsConfiguration.BUILD_STARTED_KEY)
	private boolean buildStarted = true;

	@XStreamAlias(MSTeamsConfiguration.BUILD_SUCCESSFUL_KEY)
	private boolean buildSuccessful = true;

	@XStreamAlias(MSTeamsConfiguration.BUILD_FAILED_KEY)
	private boolean buildFailed = true;

	@XStreamAlias(MSTeamsConfiguration.BUILD_INTERRUPTED_KEY)
	private boolean buildInterrupted = true;

	@XStreamAlias(MSTeamsConfiguration.SERVER_STARTUP_KEY)
	private boolean serverStartup = true;

	@XStreamAlias(MSTeamsConfiguration.SERVER_SHUTDOWN_KEY)
	private boolean serverShutdown = true;

	@XStreamAlias(MSTeamsConfiguration.ONLY_AFTER_FIRST_BUILD_SUCCESSFUL_KEY)
	private boolean onlyAfterFirstBuildSuccessful = false;

	@XStreamAlias(MSTeamsConfiguration.ONLY_AFTER_FIRST_BUILD_FAILED_KEY)
	private boolean onlyAfterFirstBuildFailed = false;

	
	public MSTeamsEventConfiguration() {
	}

	public boolean getBuildStartedStatus() {
		return this.buildStarted;
	}

	public void setBuildStartedStatus(boolean status) {
		this.buildStarted = status;
	}
	
	public boolean getBuildSuccessfulStatus() {
		return this.buildSuccessful;
	}

	public void setBuildSuccessfulStatus(boolean status) {
		this.buildSuccessful = status;
	}
	
	public boolean getBuildFailedStatus() {
		return this.buildFailed;
	}

	public void setBuildFailedStatus(boolean status) {
		this.buildFailed = status;
	}
	
	public boolean getBuildInterruptedStatus() {
		return this.buildInterrupted;
	}

	public void setBuildInterruptedStatus(boolean status) {
		this.buildInterrupted = status;
	}
	
	public boolean getServerStartupStatus() {
		return this.serverStartup;
	}

	public void setServerStartupStatus(boolean status) {
		this.serverStartup = status;
	}
	
	public boolean getServerShutdownStatus() {
		return this.serverShutdown;
	}

	public void setServerShutdownStatus(boolean status) {
		this.serverShutdown = status;
	}

	public boolean getOnlyAfterFirstBuildSuccessfulStatus() {
		return this.onlyAfterFirstBuildSuccessful;
	}

	public void setOnlyAfterFirstBuildSuccessfulStatus(boolean status) {
		this.onlyAfterFirstBuildSuccessful = status;
	}

	public boolean getOnlyAfterFirstBuildFailedStatus() {
		return this.onlyAfterFirstBuildFailed;
	}

	public void setOnlyAfterFirstBuildFailedStatus(boolean status) {
		this.onlyAfterFirstBuildFailed = status;
	}

}
