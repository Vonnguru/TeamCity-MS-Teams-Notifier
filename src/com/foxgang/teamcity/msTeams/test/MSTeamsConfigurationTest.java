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

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URISyntaxException;

import org.testng.annotations.Test;

import com.foxgang.teamcity.msTeams.MSTeamsConfiguration;
import com.foxgang.teamcity.msTeams.MSTeamsProjectConfiguration;

public class MSTeamsConfigurationTest {

	@Test
	public void testProjectConfigurationContainsNoDuplicateProjectIds() throws URISyntaxException, IOException {
		// Test parameters
		String expectedProjectId = "project1";
		String expectedRoomIdFormer = "room1";
		boolean expectedNotifyStatusFormer = true;
		String expectedRoomIdLatter = "room2";
		boolean expectedNotifyStatusLatter = false;
				
		// Prepare
		MSTeamsConfiguration configuration = new MSTeamsConfiguration();
		assertEquals(0, configuration.getProjectRoomMap().size());
		configuration.setProjectConfiguration(new MSTeamsProjectConfiguration(expectedProjectId, expectedRoomIdFormer, expectedNotifyStatusFormer));
		assertEquals(1, configuration.getProjectRoomMap().size());
		MSTeamsProjectConfiguration projectConfigurationFormer = configuration.getProjectConfiguration(expectedProjectId);
		assertEquals(expectedRoomIdFormer, projectConfigurationFormer.getRoomId());
		assertEquals(expectedNotifyStatusFormer, projectConfigurationFormer.getNotifyStatus());
		
		// Execute
		configuration.setProjectConfiguration(new MSTeamsProjectConfiguration(expectedProjectId, expectedRoomIdLatter, expectedNotifyStatusLatter));
		
		// Test
		assertEquals(1, configuration.getProjectRoomMap().size());
		MSTeamsProjectConfiguration projectConfigurationLatter = configuration.getProjectConfiguration(expectedProjectId);
		assertEquals(expectedRoomIdLatter, projectConfigurationLatter.getRoomId());
		assertEquals(expectedNotifyStatusLatter, projectConfigurationLatter.getNotifyStatus());
	}

}
