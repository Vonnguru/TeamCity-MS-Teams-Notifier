TeamCity-HipChat-Notifier
=========================

A fun TeamCity HipChat Notifier for sending build server notifications to a HipChat room, using colours and emoticons.

![Screen shot of app](images/screen_shot.png "Screen shot of app")

# Installation

[Download](https://github.com/parautenbach/TeamCity-HipChat-Notifier/releases/latest) the ZIP file release, drop it in your TeamCity installation's `.BuildServer/plugins/` 
directory (as explained by [Jetbrains](http://www.jetbrains.com/teamcity/plugins/)) and restart the server. 

Note: I've tested the plugin with TeamCity 8. Support for older versions are uncertain, but I would gladly provide information and experiences by others here. 

# Configuration

On HipChat, create a user account to represent the build server and generate a token for that user. 
Note: There are two HipChat APIs, so ensure your token is for the v2 API and not the v1 API. 

On TeamCity, as an administrator, configure the generated token and other settings on the Administration panel.

# Developers

Clone the repository and set the `teamcity.home` property in the `build.xml` to your TeamCity server's home directory and you're good to go. 

For debugging, add the snippets in `teamcity-server-log4j.xml` in this project's root to `conf/teamcity-server-log4j.xml` and then monitor `logs/hipchat-notifier.log `.

# Future Improvements

* Add a link to the build in the notification.
* List contributors in build started notification message.
* Configurable notification message templates, with their colours and emoticon sets. 
* Implement more events. 
* Use @ mentions to send guaranteed messages to persons that have contributed to a failed build. 

# Changelog

## Version 0.3.0 

* Feature: Disable or enable build and server events. 

## Version 0.2.0 

* Feature: Allow setting different rooms for different projects, and allow to use the default configuration or none, or inherit from the parent project. As a consequence, server up and down events are sent to only the default room, if configured.
* Improvement: Instead of entering a room ID, it can now be selected from a dropdown list of available rooms.
* Improvement: Added a button on the configuration page to test the API credentials.
* Bug: Fixed UI bug where disabling the plugin after saving settings didn't respond.

## Version 0.1.0

* First release.
