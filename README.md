LinCal
======
Copyright (C) 2016-2017 Felix Wiemuth

License
-------

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

This program includes third party software which is licensed under their own terms (see LICENSE-3RD-PARTY).


About
-----
"LinCal" is short for "Link Calendar" and is an Android app which allows to show links or small messages at predefined points in time, in the form of notifications. If the message is a link, it can be opened by clicking the notification. Calendars are created as a text file using a simple format and can then be loaded into the app.

Project status
--------------
A first release is available on [FDroid](https://f-droid.org/repository/browse/?fdid=felixwiemuth.lincal). Smaller updates and improvements are planned for the first quarter 2017.

Features
--------
- Create calendars easily in a text file
  - Schedule notifications for specific date and time
- Receive calendars from friends and add them to the LinCal app
  - Receive notifications when specified in the calendar
  - Click on notifications to open contained link
  - Open links with matching app if installed (e.g. Dropbox, Spotify, ...)
  - Set an earliest notification time
  - List all entries in the calendar (optionally hide future or all entries)
  - Calendar files can be updated while in use

Planned features
----------------
There are a lot of small extensions and more features planned, some of them listed below.

- Display non-link content when clicking notifications
- Possibility to show link before opening when clicking a notification
- Option to not display notifications before the screen is turned on - this avoids accoustic or visual signals of a notification
- Default settings for notifications in addition to the calendar-specific settings
- Open file manager from app to choose calendar to add
- Improved layout/design
- Richer content for notifications: custom included offline media, HTML pages
- Widget to show today's notifications on click
- Obfuscated calendar files: App makes file unreadable before sending it to someone, to not allow others to see entries in advance

Usage
-----
Using LinCal is divided into two parts: creating a calendar and using a calendar in the app.

### Creating a calendar
A calendar is described by a text file (which has to be in [UTF-8 encoding](https://www.w3.org/International/questions/qa-choosing-encodings)), the format of which is described in this section.
For an example with explanations, see [ExampleCalendar](ExampleCalendar).
The file consists of a "header section" followed by an "entry section".
In general, each line starts with a command, indicated by `@`, to specify certain parameters of an entry or the calendar.
The format is `@cmd arg`, where *cmd* is the name of the command and *arg* is the argument to be used with the command (consisting of the remaining part of the line).
Any lines starting with `#` are ignored (can be used for comments) and so are empty lines.
The header and entry section use the following commands:

**Header section** (all commands are required)

The following commands are required. The information is displayed when a user views the calendar. It has no relevance to the functions of the app.

- `@author`: Who created the calendar
- `@title`: A title for the calendar, shown as title of notifications and in the list of calendars if the user doesn't choose another name
- `@descr`: A more detailed description of the calendar
- `@date`: Creation date of the calendar (must have the format `dd/mm/yyyy`)
- `@version`: Can be used to distinguish different versions of a calendar

The following commands are optional and concern the visibility of dates and descriptions of entries to the user (see [Using the app](#using-the-app)). Possible values for `<mode>` are `hideAll`, `hideFuture`, `hideAll`. The default is `set` and `hideFuture`. The `set` option only apply when the user initially adds the calendar, the `force` options can be changed when updating the calendar.

- `@setDateDisplayMode <mode>`: Set an initial value for the *Show entry date* option 
- `@forceDateDisplayMode <mode>`: Set a value for the *Show entry date* option such that the user cannot change it
- `@setDescriptionDisplayMode <mode>`: Set an initial value for the *Show entry description* option 
- `@forceDescriptionDisplayMode <mode>`: Set a value for the *Show entry description* option such that the user cannot change it

 

**Entry section** (started with `@begin` on a single line after the header section)

An entry for the calendar is specified by first using some of the commands `@d` `@t` and `@descr` and then providing the entry's content (link or text) as a single line not starting with `@` or `#`.

- `@d dd/mm/yyyy`: Specify the date for the next entry (Note: the year must have 4 digits!)
- `@d dd/mm`: Specify the date for the next entry, only changing day and month from the last date specified
- `@d dd`: Specify the date for the next entry, only changing the day from the last date specified

Note that if no date is specified for an entry, the next calendar day after the date for the last entry is used.
Thus, to add an entry for each day, except for the starting day no further dates have to be specified.

- `@t hh:mm`: Specify the time the notification for the next entry should be shown
- `@st hh:mm`: Set the default notification time - works like `@t`, but is valid for all following entries where `@t` is not specified (before the first usage of `@st` the default notification time is 0:00)
- `@descr`: Add a description to the next entry which will be used as the notification text

**Media**

In the future, you will be able to add media like photos to a calendar. For now the best way to include media is to upload it to some cloud storage, create a shared link and use this link for a LinCal entry. If the user has the app of the cloud service installed, they might be able to open the link directly with that app.

**Sending and updating calendars**

In general, before sending a calendar to someone, make sure that the dates are correct and the links work by loading it into LinCal (see next section).

A calendar can be updated while in use (that is, the user simply replaces the calendar file on their device). However, at the time the new file is next loaded by the app (which usually is at the next due notification) no entries in the past must have been added or deleted. Thus, in general make sure that you only make changes to entries past the one the user will see next. For a better overview it is therefore advisable to specify entries in the order of occurrence, though this is not required.

### Using the app
**Adding a calendar**

*Warning:* Only use calendars from people you trust - opening links to malicious sites represents a security risk!

Click the "+" button and enter the full path to the calendar file (e.g. `/sdcard/Downloads/MyCalendar.txt`) or open a calendar file directly and choose "Add calendar".
Optionally enter a custom title for the calendar to be shown in the list of calendars and as the title of notifications. Checking "Don't show any entries now" makes sure you don't see any entries directly after adding the calendar (but this can be changed later).

**Viewing a calendar**

Select a calendar from the list to view the calendar's information, settings and a list of its entries.

- For each entry, the time as scheduled by the calendar is shown on the left. If the notification time differs, it is shown in brackets after the first time. Next to the time, the description of the entry is shown. Depending on the "Show entry" setting, the date or description might be hidden.

- Click on an entry to show its description and options to see and open the link (hidden entries cannot be clicked).

**Notifications**

A notification is shown exactly once for each entry in each calendar. Unless otherwise specified, it is shown exactly at the time as defined by the calendar. When adding a calendar or enabling notifications, also the notifications for past entries are shown.

**Calendar settings**

- *Enable notifications*: Only if this is checked notifications will be shown. When reenabling, notifications for past entries will be shown.
- *Earliest notification time*: If enabled, no notifications will be shown before the given time - if they are scheduled earlier by the calendar, they will be shown exactly at the time specified here. If the author of the calendar hasn't specified any times, then the entries are scheduled for 0:00 - thus choose this time to make sure you won't receive notifications when you don't want to.
- *Show entry date/description*: This can be used to hide the date/description of all or future entries, to not reveal a description text in advance. "Future" here refers to the time as scheduled by the calendar (as opposed to notification time). Only entries where the description is not hidden can be clicked. If the option is greyed out the author of the calendar has set the value so it cannot be changed.

**Problems**

- If notifications stop working, reenable notifications in the calendar's settings.
- If any text content of a calendar is displayed with cryptic or missing characters, this is probably because the calendar file does not use UTF-8 encoding as required (see above). Ask the author of the calendar to use the correct encoding.

Downgrading
-----------
It is possible to use an older version of the app after a newer has been used. If the format of saving status and configuration of calendars is not compatible, a message will be shown with an option to reset the configuration.

Bug reports and feature requests
--------------------------------
Please report bugs using [Github](https://github.com/felixwiemuth/LinCal/issues). Always include version number (see "About" dialog in app), device name and Android version. Feature requests may also be filed as an issue.
