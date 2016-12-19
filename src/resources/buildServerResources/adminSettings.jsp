<!--
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
-->

<%@ include file="/include.jsp"%>

<c:url value="/configureMSTeams.html" var="actionUrl" />

<bs:linkCSS dynamic="${true}">
  ${teamcityPluginResourcesPath}css/msTeamsAdmin.css
</bs:linkCSS>

<form action="${actionUrl}" id="msTeamsForm" method="POST"
	onsubmit="return msTeamsAdmin.save()">
	<div class="editNotificatorSettingsPage">
		<c:choose>
			<c:when test="${disabled}">
				<div class="pauseNote" style="margin-bottom: 1em;">
					The notifier is <strong>disabled</strong>. All msTeams
					notifications are suspended.&nbsp;&nbsp;<a class="btn btn_mini"
						href="#" id="enable-btn">Enable</a>
				</div>
			</c:when>
			<c:otherwise>
				<div style="margin-left: 0.6em;">
					The notifier is <strong>enabled</strong>.&nbsp;&nbsp;<a
						class="btn btn_mini" href="#" id="disable-btn">Disable</a>
				</div>
			</c:otherwise>
		</c:choose>
		<bs:messages key="configurationSaved" />
       	<br>		
		<table class="runnerFormTable">
			<tr class="groupingTitle">
          		<td colspan="2">General Configuration</td>
        	</tr>
			<tr>
				<th>
					<label for="apiUrl">API URL: <l:star /></label>
				</th>
				<td>
					<forms:textField name="apiUrl" value="${apiUrl}" style="width: 300px;" />
					<span class="smallNote">This must be the base URL to the <a href="https://www.msTeams.com/docs/apiv2" target="_blank">msTeams version 2 API</a>.</span>
					<forms:checkbox name="bypassSslCheck" id="bypassSslCheck" checked="${bypassSslCheck}" value="${bypassSslCheck}" 
					onclick="if (this.checked) { jQuery('#bypassSslCheckText').css('color', '#C00'); jQuery('#bypassSslCheckWarning').show(); } else { jQuery('#bypassSslCheckText').css('color', '#888'); jQuery('#bypassSslCheckWarning').hide(); } return true;"/>
					<span id="bypassSslCheckText" style="color: #888; font-size: 90%;">When checked, SSL certificate validation will be bypassed.</span>
					<span id="bypassSslCheckWarning" style="color: #C00; font-size: 90%; display: none;">This option is intended exclusively for testing against 
					a stand-alone msTeams server instance. By disabling certificate validation you are exposing yourself to man-in-the-middle attacks, among others.
					Please install a valid certificate, or if using a self-signed certificate, add it to the Java keystore.</span>
				</td>
			</tr>
			<tr>
				<th>
					<label for="apiToken">API token: <l:star /></label>
				</th>
				<td>
					<forms:textField name="apiToken" value="${apiToken}" style="width: 300px;" />
					<span class="smallNote">A user OAuth token for a dedicated build server user on msTeams.</span>
				</td>
			</tr>
			<tr>
				<th>
					<label for="notifyLabel">Trigger notifications: </label>
				</th>
				<td>
					<forms:checkbox name="notify" checked="${notify}" value="${notify}"/>
					<span style="color: #888; font-size: 90%;">When checked, a notification for all people in the room will be triggered, taking user preferences into account.</span>
				</td>
			</tr>
			<tr>
				<th>
					<label for="emoticonCache">Emoticon cache: </label>
				</th>
				<td>
					${emoticonCacheSize} items&nbsp;&mdash;&nbsp;<a href="#" onclick="return msTeamsAdmin.reloadEmoticons()">Reload</a>
				</td>
			</tr>
			<tr class="groupingTitle">
          		<td colspan="2">Build Events Configuration&nbsp;<a href="http://www.whatsthatlight.com/index.php/projects/teamcity-msTeams-plugin/" class="helpIcon" style="vertical-align: middle;" target="_blank"><bs:helpIcon/></a></td>
        	</tr>
			<tr>
				<!-- TODO: Automatically refresh this on URL or token change. -->
				<th><label for="defaultRoomId">Default room: </label></th>
				<td>
				  <forms:select name="defaultRoomId">
				  	<forms:option value="">(None)</forms:option>
                    <c:forEach var="roomIdEntry" items="${roomIdList}">
                      <forms:option value="${roomIdEntry.value}" selected="${roomIdEntry.value == defaultRoomId}">
                        <c:out value="${roomIdEntry.key}"/>
                      </forms:option>
                    </c:forEach>
                  </forms:select>
                  &nbsp;
                  <a href="#" onclick="return msTeamsAdmin.save()">Save to reload</a>
                </td>
			</tr>
			<tr>
				<th>
					<label for="branchFilterLabel">Branch filter: </label>
				</th>
				<td>
					<textarea id="branchFilterRegex" name="branchFilterRegex" style="width: 92%;" placeholder="A regular expression to trigger notifications...">${branchFilterRegex}</textarea>
					<div>
						<forms:checkbox name="branchFilter" checked="${branchFilter}" value="${branchFilter}" />
						<span style="color: #888; font-size: 90%;">When checked, branches (where supported by the VCS) will be filtered inclusively according to the regular expression.</span>			
					</div>
				</td>
			</tr>			
			<tr>
				<th>
					<label for="buildStartedLabel">Build started: </label>
				</th>
				<td>
					<textarea id="buildStartedTemplate" name="buildStartedTemplate" style="width: 92%;">${buildStartedTemplate}</textarea>
					<a style="vertical-align: top;" href="#" id="buildStartedTemplateDefaultLink">Default</a>
					<input type="hidden" id="buildStartedTemplateDefault" value="${buildStartedTemplateDefault}" /><br>
					<forms:checkbox name="buildStarted" checked="${buildStarted}" value="${buildStarted}"/>
					<span style="color: #888; font-size: 90%;">When checked, a message will be sent when the build starts.</span>			
				</td>
			</tr>
			<tr>
				<th><label for="buildSuccessfulLabel">Build successful: </label></th>
				<td>
					<textarea id="buildSuccessfulTemplate" name="buildSuccessfulTemplate" style="width: 92%;">${buildSuccessfulTemplate}</textarea>	
					<a style="vertical-align: top;" href="#" id="buildSuccessfulTemplateDefaultLink">Default</a>
					<input type="hidden" id="buildSuccessfulTemplateDefault" value="${buildSuccessfulTemplateDefault}" /><br>
					<forms:checkbox name="buildSuccessful" checked="${buildSuccessful}" value="${buildSuccessful}"/>
					<span style="color: #888; font-size: 90%;">When checked, a message will be sent when a finished build is successful.</span><br>
					<span style="padding-left: 1.5em;"><forms:checkbox name="onlyAfterFirstBuildSuccessful" checked="${onlyAfterFirstBuildSuccessful}" value="${onlyAfterFirstBuildSuccessful}" onclick="if (this.checked) { jQuery('#buildSuccessful').prop('checked', true); jQuery('#buildSuccessful').prop('disabled', true); } else { jQuery('#buildSuccessful').prop('disabled', false); } return true;"/></span>
					<span style="color: #888; font-size: 90%;">When checked, a message will be sent only when a the first build after a failed build is successful.</span>
				</td>
			</tr>
			<tr>
				<th><label for="buildFailedLabel">Build failed: </label></th>
				<td>
					<textarea id="buildFailedTemplate" name="buildFailedTemplate" style="width: 92%;">${buildFailedTemplate}</textarea>
					<a style="vertical-align: top;" href="#" id="buildFailedTemplateDefaultLink">Default</a>
					<input type="hidden" id="buildFailedTemplateDefault" value="${buildFailedTemplateDefault}" /><br>
					<forms:checkbox name="buildFailed" checked="${buildFailed}" value="${buildFailed}"/>
					<span style="color: #888; font-size: 90%;">When checked, a message will be sent when a finished build failed.</span><br>
					<span style="padding-left: 1.5em;"><forms:checkbox name="onlyAfterFirstBuildFailed" checked="${onlyAfterFirstBuildFailed}" value="${onlyAfterFirstBuildFailed}" onclick="if (this.checked) { jQuery('#buildFailed').prop('checked', true); jQuery('#buildFailed').prop('disabled', true); } else { jQuery('#buildFailed').prop('disabled', false); } return true;"/></span>
					<span style="color: #888; font-size: 90%;">When checked, a message will be sent only when a the first build after a successful build has failed.</span>
				</td>
			</tr>
			<tr>
				<th><label for="buildInterruptedLabel">Build interrupted: </label></th>
				<td>
					<textarea id="buildInterruptedTemplate" name="buildInterruptedTemplate" style="width: 92%;">${buildInterruptedTemplate}</textarea>
					<a style="vertical-align: top;" href="#" id="buildInterruptedTemplateDefaultLink">Default</a>
					<input type="hidden" id="buildInterruptedTemplateDefault" value="${buildInterruptedTemplateDefault}" /><br>
					<forms:checkbox name="buildInterrupted" checked="${buildInterrupted}" value="${buildInterrupted}"/>
					<span style="color: #888; font-size: 90%;">When checked, a message will be sent when the build gets interrupted (i.e. cancelled).</span>
				</td>
			</tr>
			<tr class="groupingTitle">
          		<td colspan="2">Server Events Configuration&nbsp;<a href="http://www.whatsthatlight.com/index.php/projects/teamcity-msTeams-plugin/" class="helpIcon" target="_blank"><bs:helpIcon/></a></td>
        	</tr>
			<tr>
				<!-- TODO: Automatically refresh this on URL or token change. -->
				<th><label for="serverEventRoomId">Room: </label></th>
				<td>
				  <forms:select name="serverEventRoomId">
				  	<forms:option value="">(None)</forms:option>
                    <c:forEach var="roomIdEntry" items="${roomIdList}">
                      <forms:option value="${roomIdEntry.value}" selected="${roomIdEntry.value == serverEventRoomId}">
                        <c:out value="${roomIdEntry.key}"/>
                      </forms:option>
                    </c:forEach>
                  </forms:select>
                  &nbsp;
                  <a href="#" onclick="return msTeamsAdmin.save()">Save to reload</a>
                </td>
			</tr>
			<tr>
				<th><label for="serverStartupLabel">Server startup: </label></th>
				<td>
					<textarea id="serverStartupTemplate" name="serverStartupTemplate" style="width: 92%;">${serverStartupTemplate}</textarea>
					<a style="vertical-align: top;" href="#" id="serverStartupTemplateDefaultLink">Default</a>
					<input type="hidden" id="serverStartupTemplateDefault" value="${serverStartupTemplateDefault}" /><br>
					<forms:checkbox name="serverStartup" checked="${serverStartup}" value="${serverStartup}"/>
					<span style="color: #888; font-size: 90%;">When checked, a message will be sent to the <b>default</b> room.</span>
				</td>
			</tr>
			<tr>
				<th><label for="serverShutdownLabel">Server shutdown: </label></th>
				<td>
					<textarea id="serverShutdownTemplate" name="serverShutdownTemplate" style="width: 92%;">${serverShutdownTemplate}</textarea>
					<a style="vertical-align: top;" href="#" id="serverShutdownTemplateDefaultLink">Default</a>
					<input type="hidden" id="serverShutdownTemplateDefault" value="${serverShutdownTemplateDefault}" /><br>
					<forms:checkbox name="serverShutdown" checked="${serverShutdown}" value="${serverShutdown}"/>
					<span style="color: #888; font-size: 90%;">When checked, a message will be sent to the <b>default</b> room.</span>
				</td>
			</tr>
		</table>
		<div class="saveButtonsBlock">
			<forms:submit label="Save" />
			<forms:submit id="testConnection" type="button" label="Test connection" onclick="return msTeamsAdmin.testConnection()"/>
			<forms:saving />
		</div>
	</div>
</form>

<bs:linkScript>
    ${teamcityPluginResourcesPath}js/msTeamsAdmin.js
</bs:linkScript>

<script type="text/javascript">
	jQuery(document).ready(function() {
		if (${bypassSslCheck}) {
			jQuery('#bypassSslCheckText').css('color', '#C00'); jQuery('#bypassSslCheckWarning').show();
		}		
	});
</script>

<script type="text/javascript">
	(function($) {
		var sendAction = function(enable) {
			$.post("${actionUrl}?action=" + (enable ? 'enable' : 'disable'),
					function() {
						BS.reload(true);
					});
			$('msTeamsComponent').refresh();
			return false;
		};

		$("#enable-btn").click(function() {
			return sendAction(true);
		});

		$("#disable-btn")
				.click(
						function() {
							if (!confirm("msTeams notifications will not be sent until enabled. Disable the notifier?"))
								return false;
							return sendAction(false);
						});
		
		$('#buildStartedTemplateDefaultLink').click(function() {
			$("#buildStartedTemplate").val($("#buildStartedTemplateDefault").val()); 
			return false;
		});

		$('#buildSuccessfulTemplateDefaultLink').click(function() {
			$("#buildSuccessfulTemplate").val($("#buildSuccessfulTemplateDefault").val()); 
			return false;
		});

		$('#buildFailedTemplateDefaultLink').click(function() {
			$("#buildFailedTemplate").val($("#buildFailedTemplateDefault").val()); 
			return false;
		});

		$('#buildInterruptedTemplateDefaultLink').click(function() {
			$("#buildInterruptedTemplate").val($("#buildInterruptedTemplateDefault").val()); 
			return false;
		});

		$('#serverStartupTemplateDefaultLink').click(function() {
			$("#serverStartupTemplate").val($("#serverStartupTemplateDefault").val()); 
			return false;
		});

		$('#serverShutdownTemplateDefaultLink').click(function() {
			$("#serverShutdownTemplate").val($("#serverShutdownTemplateDefault").val()); 
			return false;
		});

	})(jQuery);
</script>
