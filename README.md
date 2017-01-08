LinCal
======
Copyright (C) 2016 Felix Wiemuth, François Vincent

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
A first public release on [FDroid](https://f-droid.org/) will be available in the coming days.

Features
--------
- Create calendars easily in a text file
  - Schedule notifications for specific date and time
- Receive calendars from friends and add them to the LinCal app
  - Receive notifications when specified in the calendar
  - Click on notifications to open contained link
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
- Force hide entries by calendar file
- Obfuscated calendar files: App makes file unreadable before sending it to someone, to not allow others to see entries in advance

Usage
-----
Using LinCal is divided into two parts: creating a calendar and using a calendar in the app.

### Creating a calendar
A calendar is described by a text file, the format of which is described in this section.
For an example with explanations, see [ExampleCalendar](ExampleCalendar).
The file consists of a "header section" followed by an "entry section".
In general, each line starts with a command, indicated by `@`, to specify certain parameters of an entry or the calendar.
The format is `@cmd arg`, where *cmd* is the name of the command and *arg* is the argument to be used with the command (consisting of the remaining part of the line).
Any lines starting with `#` are ignored (can be used for comments) and so are empty lines.
The header and entry section use the following commands:

**Header section** (all commands are required)

This information is displayed when a user views the calendar. It has no relevance to the functions of the app.

- `@author`: Who created the calendar
- `@title`: A title for the calendar, shown as title of notifications and in the list of calendars if the user doesn't choose another name
- `@descr`: A more detailed description of the calendar
- `@date`: Creation date of the calendar (must have the format `dd/mm/yyyy`)
- `@version`: Can be used to distinguish different versions of a calendar

**Entry section** (started with `@begin` on a single line after the header section)

An entry for the calendar is specified by first using some of the commands `@d` `@t` and `@descr` and then providing the entry's content (link or text) as a single line not starting with `@` or `#`.

- `@d dd/mm/yyyy`: Specify the date for the next entry (Note: the year must have 4 digits!)
- `@d dd/mm`: Specify the date for the next entry, only changing day and month from the last date specified
- `@d dd`: Specify the date for the next entry, only changing the day from the last date specified

Note that if no date is specified for an entry, the next calendar day after the date for the last entry is used.
Thus, to add an entry for each day, except for the starting day no further dates have to be specified.

- `@t hh:mm`: Specify the time the notification for the next entry should be shown
- `@st hh:mm`: Set the default notification time - works like `@t`, but is valid for all following entries where `@t` is not specified (before the first usage of `@st` the default notification time is 0:00).
- `@descr`: Add a description to the next entry which will be used as the notification text

In general, before sending a calendar to someone, make sure that the dates are correct and the links work by loading it into LinCal (see next section).

A calendar can be updated while in use (that is, the user simply replaces the calendar file on their device). However, at the time the new file is next loaded by the app (which usually is at the next due notification) no entries in the past must have been added or deleted. Thus, in general make sure that you only make changes to entries past the one the user will see next. For a better overview it is therefore advisable to specify entries in the order of occurrence, though this is not required.

### Using the app
**Adding a calendar**

*Warning:* Only use calendars from people you trust - opening links to malicious sites represents a security risk!

Click the "+" button and enter the full path to the calendar file (e.g. `/sdcard/Downloads/MyCalendar.txt`) or open a calendar file directly and choose "Add calendar".
Optionally enter a custom title for the calendar to be shown in the list of calendars and as the title of notifications.

**Viewing a calendar**

Select a calendar from the list to view the calendar's information, settings and a list of its entries.

- For each entry, the time as scheduled by the calendar is shown on the left. If the notification time differs, it is shown in brackets after the first time. Next to the time, the description of the entry is shown. Depending on the "Show entries" setting, the description might be hidden.

- Click on an entry to show its description and options to see and open the link.

**Notifications**

A notification is shown exactly once for each entry in each calendar. Unless otherwise specified, it is shown exactly at the time as defined by the calendar. When adding a calendar or enabling notifications, also the notifications for past entries are shown.

**Calendar settings**

- *Enable notifications*: Only if this is checked notifications will be shown. When reenabling, notifications for past entries will be shown.
- *Earliest notification time*: If enabled, no notifications will be shown before the given time - if they are scheduled earlier by the calendar, they will be shown exactly at the time specified here. If the author of the calendar hasn't specified any times, then the entries are scheduled for 0:00 - thus choose this time to make sure you won't receive notifications when you don't want to.
- *Show entries*: This can be used to hide all or future entries, to not reveal a description text in advance. Entries can still be viewed by clicking on them.

Bug reports and feature requests
--------------------------------
Please report bugs using [Github](https://github.com/felixwiemuth/LinCal/issues). Always include version number (see "About" dialog in app), device and Android version. Feature requests may also be filed as an issue.
