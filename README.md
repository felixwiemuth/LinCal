LinCal
======
Copyright (C) 2016-2017 Felix Wiemuth and contributors

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
The current version is 1.3.1 (2018-01-02) and is available on [F-Droid](https://f-droid.org/repository/browse/?fdid=felixwiemuth.lincal) (2018-02-16). Further development is intended but currently not planned. However, translations will be incorporated and other contributions are also possible (see [Contributing](#contributing)).

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
  - Calendar files can be updated while in use and synced with the cloud (e.g. Google Drive or Nextcloud)

Planned features
----------------
There are a lot of small extensions and more features planned, some of them listed below.

- Display non-link content when clicking notifications
- Possibility to show link before opening when clicking a notification
- Option to not display notifications before the screen is turned on - this avoids acoustic or visual signals of a notification
- Default settings for notifications in addition to the calendar-specific settings
- Richer content for notifications: custom included offline media, HTML pages
- Widget to show today's notifications on click
- Obfuscated calendar files: app makes file unreadable before sending it to someone, to not allow others to see entries in advance

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

**Header section**

The following commands are required. The information is displayed when a user views the calendar. It has no relevance to the functions of the app.

- `@author`: Who created the calendar
- `@title`: A title for the calendar, shown as title of notifications and in the list of calendars if the user doesn't choose another name
- `@descr`: A more detailed description of the calendar
- `@date`: Creation date of the calendar (must have the format `dd/mm/yyyy`)
- `@version`: Can be used to distinguish different versions of a calendar

The following commands are optional and concern the visibility of dates and descriptions of entries to the user (see [Using the app](#using-the-app)). Possible values for `<mode>` are `hideAll`, `hideFuture`, `hideAll`. The default is `set` and `hideFuture`. The `set` options only apply when the user initially adds the calendar, the `force` options can be changed when updating the calendar.

- `@setDateDisplayMode <mode>`: Set an initial value for the *Show entry date* option 
- `@forceDateDisplayMode <mode>`: Set a value for the *Show entry date* option such that the user cannot change it
- `@setDescrDisplayMode <mode>`: Set an initial value for the *Show entry description* option 
- `@forceDescrDisplayMode <mode>`: Set a value for the *Show entry description* option such that the user cannot change it

 

**Entry section** (started with `@begin` on a single line after the header section)

An entry for the calendar is specified by first using some of the commands `@d` `@t` and `@descr` and then providing the entry's content (link or text) as a single line not starting with `@` or `#`.

- `@d dd/mm/yyyy`: Specify the date for the next entry (Note: the year must have 4 digits!)
- `@d dd/mm`: Specify the date for the next entry, only changing day and month from the last date specified
- `@d dd`: Specify the date for the next entry, only changing the day from the last date specified

Note that if no date is specified for an entry, the next calendar day after the date for the last entry is used.
Thus, to add an entry for each day, except for the starting day no further dates have to be specified.
To add two entries for the same day, `@d ...` has to be used to avoid the increment to the next day.
It is possible to have multiple entries at the same day and time (they will be displayed in the order given).

- `@t hh:mm`: Specify the time the notification for the next entry should be shown
- `@st hh:mm`: Set the default notification time - works like `@t`, but is valid for all following entries where `@t` is not specified (before the first usage of `@st` the default notification time is 0:00)
- `@descr`: Add a description to the next entry which will be used as the notification text

**Media**

In the future, you will be able to add media like photos to a calendar. For now the best way to include media is to upload it to some cloud storage, create a shared link and use this link for a LinCal entry. If the user has the app of the cloud service installed, they might be able to open the link directly with that app.

**Sending and updating calendars**

In general, before sending a calendar to someone, make sure that the dates are correct and the links work by loading it into LinCal (see "Adding a calendar").

A calendar can be updated while in use (the user simply replaces the calendar file on their device). However, at the time the new file is next loaded by the app (which usually is at the next due notification) no entries in the past must have been added or deleted. Thus, in general make sure that you only make changes to entries past the one the user will see next. For a better overview it is therefore advisable to specify entries in the order of occurrence, though this is not required.

An easy way to be able to update a calendar without the user having to manually replace it is to have it in cloud storage. The user either has to add the calendar by selecting it from cloud storage (only if it is listed when choosing a file with the app, that is, the cloud storage provider must support this, like Google Drive or Nextcloud) or by making sure the file on local storage is automatically synchronized with the cloud (see also "Adding a calendar").

### Using the app
**Adding a calendar**

*Warning:* Only use calendars from people you trust - opening links to malicious sites represents a security risk!

Click the "+" button and then "Choose..." to select a calendar file or enter the full path directly (e.g. `/sdcard/Downloads/MyCalendar.txt`). Alternatively, select the calendar file in a file manager and choose LinCal to open it.
Optionally enter a custom title for the calendar to be shown in the list of calendars and as the title of notifications. Checking "Don't show any entries now" makes sure you don't see any entries directly after adding the calendar (but this can be changed later). Finally click "Add calendar".

*Note:* LinCal does not create a copy when adding a calendar but always uses the file at the location chosen. This allows calendars to be updated by simply replacing the calendar file with a new version. By synchronizing it with the cloud, automatic updates can be achieved. LinCal will use the newer version as soon it has been restarted. If you want to move a calendar file, you have to remove the calendar from LinCal first and readd it after moving the file.

Calendars can be added from a cloud storage provider that supports the Android Storage Access Framework (SAF). When clicking "Choose...", you can for example add calendars from Google Drive or Nextcloud. This way the calendar will be automatically updated from the cloud as soon as the cloud service synchronizes (and LinCal restarts). Note, however, that if you are offline and the cloud storage cache is cleared, LinCal won't get access to the calendar file and show an error.

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

Translations
------------
You are welcome to add translations to the app, they will be incorporated after being checked to be appropriate and complete. You may file an [issue](https://github.com/felixwiemuth/LinCal/issues/new) with your plans if you want to make sure no one is already working on the translation. Note that translations might be temporarily disabled when they get outdated or incomplete with an update. They will be included again as soon as updated. 

### Available translations
The following table shows for which languages translations are available, also noting in which versions the translation is included, which parts of the app are translated and which translations need an update.

| Language | Translators | Included | Strings | Help | Change log | About |
| -------- | ----------- | -------- | :-----: | :--: | :--------: | :---: |
| Japanese | [naofum](https://github.com/naofum) | 1.1.0+ | o | - | - | - |
| German | [felixwiemuth](https://github.com/felixwiemuth/), [jaller94](https://github.com/jaller94) | 1.2.0+ | + | - | + | - |
| French | [Poussinou](https://github.com/poussinou) | | o | o | - | o |

Legend: (-) not translated (+) fully translated (o) partially translated / needs update

### Planned translations
Translations for the following languages are already planned:
- German
- Danish

### How to translate the app
- To translate, use a tool of your choice, for example [Android Studio](https://developer.android.com/studio/write/translations-editor.html) or the app [Stringlate](https://f-droid.org/app/io.github.lonamiwebs.stringlate).
- Basically, translating works as follows (some of the steps are done automatically by some tools):
  - [Fork](https://github.com/felixwiemuth/LinCal/fork) the project on Github and do all the following actions in your fork.
  - Choose a file from the list below you want to translate. This file is contained in a folder under the [res](app/src/main/res/) folder (e.g. `values`, `raw` or `xml`). Create (if it doesn't already exist) an empty copy of this folder with `-x` appended, where `x` is the code of the language/region you are translating to ([list of codes](https://github.com/championswimmer/android-locales/blob/master/README.md)). Into this folder, put a copy of the original file and translate it by changing it. If you created the translation with a tool, instead of copying the original file, put the file obtained from the tool into the folder.
  - In each file you change, change the first line of the copyright header to "Copyright (C) yyyy Felix Wiemuth and contributors (see CONTRIBUTORS.md)", do not change the year. If you create a new file, copy the license header from another file of that type and change the year to the current year.
  - Add the new language to the `settings_language_values` array in [strings.xml](app/src/main/res/values/strings.xml) (and translate it in the other `strings.xml` files) and add the language code you used to name the folder(s) to the `locales` array in [locales.xml](app/src/main/res/values/locales.xml).
    - Note that this is only for choosing the language manually in the app. It for now only works with language codes ("en") but not with country codes ("en_US"). Thus, only add the language code here and ommit the rest.
  - If possible, test your translations by running the app.
  - Add or update the corresponding entry in the following places, where an entry has the following format: `[<github-username>](https://github.com/github-username) (<full name>, <email>)` (name and email address are optional, links and email should be formatted according to the file's format):
    - section "Available translations" in this file (do not edit "Included" field)
    - section "Translations" in [About](app/src/main/res/raw/about.html)
    - section "Translations" in [CONTRIBUTORS.md](CONTRIBUTORS.md)
  - When you are done, commit your changes and create a [pull request](https://help.github.com/articles/creating-a-pull-request/).
    - Include `closes #<issue number>` in your commit messages if you resolve an issue.
    - Please rebase onto the master branch of the main repository and squash your commits into one or a few significant ones that are relevant for the project's history (if you don't do this, it may be done by the maintainers).
  - For more information on how translations work in general see the [Android Developer Guide](https://developer.android.com/guide/topics/resources/localization.html).
  - If you want to translate but need help, file an [issue](https://github.com/felixwiemuth/LinCal/issues/new).


### How to update translations
- Read "How to translate the app" and follow all relevant steps.
- If you want to update someone else's translation:
  - If you find typos or other obvious mistakes, simply correct them (and only add yourself to the lists of contributors if you find it appropriate).
  - If you want to continue a translation or make more significant changes, first assign an [issue](https://github.com/felixwiemuth/LinCal/issues/new) to the person(s) who made the translation describing your intents and agree with them on what to do.
- If you have started a translation, you will be notified by the maintainers when your translation has to be updated (you will be assigned an issue).
- To update translations, use a diff utility to see changes in the original files since you last translated. To do this on Github, simply visit `https://github.com/felixwiemuth/LinCal/compare/<commit-last-translated>...<commit-to-translate>` where you replace the `<commit>` parameters with the corresponding commits' SHA-1 hashes (such a link may be provided to you by the maintainers when being asked to update translations). To see what commits changed a file click "History" when viewing the file.

### Files that may be translated
Please only translate the following files and note the importance of translations for different files (translations will only be accepted if they include `strings.xml`):
- [strings.xml](app/src/main/res/values/strings.xml): contains the text used in the app's main user interface, should always be translated
  - Do not translate entries with `translatable="false"` (remove those entries)
  - Look for "TRANSLATION NOTE" comments 
  - Keep order and structure of the original file and do not delete comments
- [Help](app/src/main/res/raw/help.html): a help page in HTML (accessed in the app via the main menu), good if translated
- [Change log](app/src/main/res/xml/changelog_master.xml): lists changes introduced with new versions of the app, good if translated
  - Note: the translated file must be called `changelog.xml` instead of `changelog_master.xml`
- [About](app/src/main/res/raw/about.html): an HTML page with information about the app (accessed in the app via the main menu), can be translated but it's less important
  - Important: In the "License" section do not translate the first paragraph (the license text - an appropriate translation will be added by the maintainers if found) but do translate the heading "License" and everything after the first paragraph.

Contributing
------------
If you have ideas on how to improve LinCal, you are welcome to contribute. There are several ways you can contribute:
- Share your ideas in an issue
- [Translate](#translations) LinCal
- Implement features: please see [Contributing](CONTRIBUTING.md) for more information

Bug reports and feature requests
--------------------------------
Please report bugs on [Github](https://github.com/felixwiemuth/LinCal/issues). Always include version number (see "About" dialog in app), device name and Android version. Feature requests may also be filed as an issue.
