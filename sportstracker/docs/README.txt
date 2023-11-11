About SportsTracker
-------------------

SportsTracker is an application for people who want to track their sporting
activities. It is not bound to a specific kind of sport, the user can create
categories for all sport types such as cycling, running, swimming or tennis.

The main advantage is a good overview of your exercises, you can easily create
diagrams and statistics for specific date ranges and sport types. In the
calendar you can also track your body weight or create note entries, e.g.
the training plan or upcoming sport events.

All the application data is stored in a local SQLite database. So it is very
easy to access them with SQLite database tools or to write importers and
exporters for other applications. Users with SQL experience can also create
easily custom statistics by using an SQLite database browser application.
Prior to SportsTracker 8.0.0 the application data was stored in XML files.
The migration from XML to SQLite storage is done automatically on first
startup of SportsTracker >= 8.0.0. The XML files will not be deleted due to
backup reasons.

If you are using a sport watch or another tracking device with a computer
interface you can display the recorded exercise files and evaluate the diagrams
with the integrated ExerciseViewer application.
You can organize them by attaching the recorded files to the exercise entries.
When adding new exercises you can import the data from the recorded exercise
files.

ExerciseViewer supports Garmin, Polar, Timex, Suunto, CicloSport, Oregon, HOLUX
and Kalenji tracking devices. This is the current compatibility list (other
devices might work too, but I can't test them, user feedback is welcome):

  - Garmin Edge        (tested with Edge 500, 520, 530, 705, 820, FIT and TCX files)
  - Garmin Forerunner  (tested with Forerunner 35, 305, 910XT, 645, 955, FIT and TCX files)
  - Garmin Oregon      (tested with Oregon 450, GPX files)
  - Garmin Fenix       (tested with Fenix 2, 5x, 6, 6S Pro, FIT files)
  - Polar S610(i)      (tested)
  - Polar S710(i)      (tested)
  - Polar S720i        (tested)
  - Polar S725         (tested)
  - Polar S625x        (tested, HRM files only)
  - Polar S410         (tested, RAW files untested)
  - Polar S510         (tested)
  - Polar S520         (tested, RAW files untested)
  - Polar RS200SD      (tested)
  - Polar RS400        (initial support)
  - Polar RS800        (initial support)
  - Polar F6           (tested)
  - Polar F11          (tested)
  - Polar CS600        (tested, HRM files only)
  - Polar RCX3         (tested, HRM files only)
  - CicloSport HAC4    (tested)
  - CicloSport HAC4Pro (tested)
  - CicloSport HAC5    (tested)
  - Timex Ironmen Race Trainer (tested)
  - Timex Ironmen Run Trainer (tested)
  - Timex Ironman Global Trainer  (tested)
  - Suunto Ambit       (tested with Ambit2, downloaded GPX files)
  - Suunto Spartan     (tested with Spartan Sport Wrist HR Baro, FIT files)
  - Oregon Scientific SmartSync WM100 (tested)
  - HOLUX FunTrek      (tested with FunTrek 130, GPX files)
  - Some Sony Ericsson mobiles (tested with W580i)
  - W Kalenji 300      (tested, GPX imports)
  - CW Kalenji 700     (tested, GPX imports)
  - All devices recording GPX files (tested some models)  

It's also possible to view HRM exercise files (downloaded with the Polar
Software for Windows).

Users of tracking devices with an integrated GPS receiver (e.g. the Garmin Edge
series) can also view and replay the track of the recorded exercises in the
interactive map viewer component.

SportsTracker itself is not able to download the exercise files from the
tracking device. Some monitors (e.g. Garmin) allow the direct download via
USB. For other monitors you might need one of these tools:

  - 's710' for many Polar models (S6XX, S7XX, ...), at least version 0.19
    URL: http://daveb.net/s710
  - 'rs200-decoder' for Polar RS200
    URL: http://sourceforge.net/projects/rs200-decoder
  - 'RS400 Tools' for the Polar RS400 (and probably RS800) monitor
    URL: http://users.tkk.fi/jjvayryn/polar_f55_hrm.html
  - SonicRead for Polar S410, S510 and S520
    URL: http://code.google.com/p/sonicread
  - 'F6 Split Tool' for Polar F6 and F11 (also needs 'rs200-decoder')
    URL: http://toazter.ch/sites/F6SplitTool
  - 'HAC4Linux' for CicloSport HAC4
    URL: http://sourceforge.net/projects/hac4linux
  - SonyEricsson Importer for some Sony Ericsson mobiles
    URL: http://luka.tnode.com/software/sonyericsson-importer-sportstracker
  - kalenji-gps-watch-reader for Kalenji GPS watches (imports GPX files)
    URL: http://code.google.com/p/kalenji-gps-watch-reader

Garmin users don't need special transfer software, the exercise files can
be downloaded by using the USB mass storage support.

Users of Polar S725 monitors needs to keep the default settings (80 - 160)
of the 'Exercise Heartrate Range Summary'. This is right now the only known
method to detect the S725 exercise file type, because it needs different
parsing compared to other S7XX models (this is not necessary for parsing
HRM files of S725).

Users of the website PolarPersonalTrainer.com can open their exported exercise
files (.ped extension) in ExerciseViewer.


Requirements
------------

SportsTracker is an application for the Java platform written in Java and
Kotlin. It was developed and tested with the OpenJDK JVM, other JVM
implementations will probably work too (e.g. Oracle JDK).

The SportsTracker installer packages contain an embedded Java SE Runtime
Environment (JRE), a Java installation is not needed. Users of binary packages
need to install a suited JDK before.
The OpenJDK can be downloaded e.g. from: https://adoptium.net/

If you want to download exercise files from your heartrate monitor you might
need one of the download tools listed above.

The application was tested on GNU/Linux (e.g. Ubuntu 22.04), Windows 10 and
macOS (12 - 14), although it should work on all systems with the required
Java Runtime Environment.


Installation and Start
----------------------

For Windows, macOS and some Linux systems there are native installer packages
available on the GitHub project release page. They include an embedded Java
Runtime Environment (JRE), so the user does not need to install Java before.
The installers will create a menu entry for easy SportsTracker startup.

If you are not using an installer, you need to download the ZIP file with the
application source code and build the application locally. The build process
and the application execution is documented in the chapter 'Developer
Requirements' below.

The default directory for the application data is '$HOME/.sportstracker',
e.g. '/home/foo/.sportstracker' for the Linux user foo. You can also specify
another directory with the '--datadir' command line parameter. 
Example: 'java -jar sportstracker-x.y.z.jar --datadir=.' stores the
application data in the SportsTracker installation directory. So you're able
to put SportsTracker and it's data on an USB stick and use it on any available
computer.

In case of problems there is often detailed information on the console output.
The displayed error dialog gives a hint for that. The console output is only
visible when SportsTracker has been started by using the command line.

Console start example for macOS: Start a Terminal and enter:
  'cd /Applications/SportsTracker.app/Contents/MacOS'
  './SportsTracker'


Installer security notes for macOS and Windows 10 users
-------------------------------------------------------

The SportsTracker installer packages doesn't contain trusted code signing
certificates. This can't be provided by an individual open source project, the
expense and the effort are just too high.

This causes problems on recent macOS versions (since macOS 10.15) when
executing the downloaded and installed SportsTracker application. By default
macOS marks all unsigned applications as quarantine, the user will get an error
when executing them.
This can be fixed easily be removing the quarantine flag on command line by
executing this command in the terminal:

  sudo xattr -cr /Applications/SportsTracker.app

On Windows 10 the Microsoft Defender SmartScreen might try to prevent the
SportsTracker installation, because it has no trusted code signing certificate.
This SmartScreen warning needs to be bypassed by clicking on 'More info' and
then 'Run anyway'.

If these workarounds can't be used for some reasons, then the application needs
to be build and packaged locally by the user. The locally created packages can
be installed without problems.


Usage
-----

This is a short introduction for the usage of the application:

Before you can add exercises you need to define a list of your sport types in
the Sport Type Editor dialog. An initial set will be added at application
start when there are no sport types defined yet. You should adjust this list
to meet your sporting requirements. Examples for sport types are "cycling",
"running" or "swimming".
For sport types which are not endurance related (e.g. "tennis") you need to
specify that distance will not be recorded for such exercises. The distance
record mode can only be changed for new sport types or when no exercises for
this sport type exist.
For every sport type you need to define whether speed data will be handled as
speed (e.g. in km/h) or pace values (e.g. in min/km).
By assigning a custom color for each sport type the list of exercises looks
much more clear.
For each sport type you need to create at least one subtype. Subtype examples
for cycling are "MTB tour", "MTB race", "Road tour" and so on. If subtypes
do not make sense for your sport type just create a subtype called "default".
You can also define a list of equipment for a sport type (optional), e.g.
specific bikes for cycling or shoes for running. Worn and outdated equipment
can be set to 'not in use' state, then it can't be selected anymore.

After that it's possible to add and edit exercises. In the exercise dialog
you need to specify the date, the sport type, the subtype and the intensity.
You also need to enter the distance, the average speed and the duration. Only
2 of these 3 values needs to be entered, the third one will be calculated
automatically. The default calculation is for duration, but you can select
another one of these values.
When you choose a sport type for which the distance will not be recorded
then you only need to enter the duration of the exercise.
All the other inputs are optional. You can select the equipment used for the
exercise when there is equipment defined for the selected sport type.
Data such as the route description can be added to the comment text.

Note entries can be added for special dates, so you can e.g. enter your
training plan or descriptions of sport events. You can also track your body
weight by adding weight entries for special dates. Note and weight entries
don't need the definition of sport types.

Entries of all types can be simply copied. This is very useful when you have
many similar exercises or weight entries. You just select the entry to copy 
and start the copy action from the context or main menu. It displays the Add
dialog for the copied entry with all data prefilled. You just need to enter the
new date.

The calendar view displays all exercise, note and weight entries of the 
selected month. The last column contains the distance and duration summary for
all exercises of the appropriate week. New exercise entries can be added by
double clicking the appropriate day cell. Note and weight entries can be added
by using the context menu inside a day cell. An existing entry can be edited
by double clicking it.

Additionally there are special list views for all exercise, note and weight
entries. These list views are very helpful for the analysis, especially when
using sorting and filters.
Filters can be used in all views, all entry types are filterable. The filter
can only be enabled for one specific entry type, the entries of all other types
will not be filtered meanwhile.
The filter can be used to search in the comments of the entries. In the normal
mode each of the entered filter words needs to be found in the entry comment
(AND logic, case-insensitive). There is also a regular expression mode which,
this filter is case-sensitive and supports only one valid regular expression.

Users of heartrate monitors (HRM) can assign the recorded file to the exercise.
Most of the exercise data can be imported from this file, so it does
not need to be entered manually. The exercise files can also be viewed with 
the integrated ExerciseViewer application, which displays all the recorded
data, diagrams and the track for it.

HRM exercise files can be easily imported by drag & drop. The user must drag
one single HRM file from the systems file manager and drop it to a day cell in
the calendar view. If there is an exercise entry under the mouse cursor then 
the HRM file will be assigned to this exercise. Otherwise, a new exercise will
be created and the data will be imported from the HRM file.
Drag & drop has been successfully tested on Linux (Gnome and KDE), Windows and
macOS.

When importing Garmin FIT exercise files the sport type and sport subtype can
be mapped automatically from the FIT exercise to the appropriate SportsTracker
types.
The user needs to configure the mapping between FIT and SportsTracker types
before, which can be done in the Sport Type Dialog. The Garmin FIT sport type
and subtype of an exercise can be found in the ExerciseViewer Main panel when
displaying FIT files.
When importing a FIT file SportsTracker searches for a matching mapping for
both the sport type and subtype and preselects them in the Exercise Dialog if
found.

For the creation of statistics the user needs to specify at least the 
calculation filter for the time range, e.g. the current month. It's also 
possible to set filters for the sport type, the subtype, the intensity, the
equipment and the comment, so only the specified exercises will be included in
the statistic calculation.

The user can create overview diagrams for the last 12 months, for all months
or weeks of a selected year or for a selectable time range of 10 years. 
The diagram will display the summary distance, duration, ascent, descent and
average speed for the sum of all sport types or splitted for each sport type.
For sport subtype or equipment usage overview the diagram can also display the
distance per sport subtype or equipment for a selected sport type.
And finally it can also display the history of your body weight in the 
selected time range.

The Equipment Usage dialog can be used for showing usage statistics of each
equipment for a selected sport type. The usage contains the total distance,
duration and dates of first and last usage. So it's very easy to identify
worn and outdated equipment or just to get interesting usage details.

Whenever SportsTracker displays multiple exercises, which are using sport types
with different speed modes, then the preferred speed mode will be used. This
can be defined in the Preferences Dialog.

If the heartrate monitor has an integrated GPS receiver and stores the 
location data in the exercise file (e.g. in TCX files from the Garmin Edge 
series), then ExerciseViewer will show the exercise track inside a map viewer
component. This interactive map is zoomable and moveable, so it's easy to 
view all details of the track. The map viewer supports multiple layers, e.g.
OpenStreetMap, OpenCycleMap and Hike&Bike Map.
The green marker is the start, the red is the end position and the grey markers
are the lap split positions.
The exercise track can be replayed by using the track position slider. The
current position is marked in the map by the blue marker. A tooltip shows all
the details of the current position.
The map data will be downloaded on demand from the OpenStreetMap project
(http://www.openstreetmap.org).

ExerciseViewer can also display power data (for FIT files only), if it has been
recorded. It displays the average, maximum and normalized power for the
exercise and each lap. The Diagram panel can display the power graph for all
recorded samples.
Further info on normalized power is available here:
https://help.trainingpeaks.com/hc/en-us/articles/204071804-Normalized-Power

New users, which have many Polar HRM files recorded before switching to
SportsTracker, can import all HRM files at once by using the external tool
'sportstracker-importer', located in the 'misc' directory.


Notes for Garmin users
----------------------

The data stored in the recorded TCX files is sometimes wrong or imprecise.
ExerciseViewer can only display the stored data, it can't decide whether the
recorded data makes sense or not.

The speed calculated by the distance between two trackpoints is sometimes
impossible. Example in test file Edge705-Running-Heartrate-2Laps.tcx 
(from Edge 705, same problem in Forerunner 305 files):
- the distance between 07:47:43 and 07:47:47 is about 182.7 meters 
- the speed in these 4 seconds is 164.4 km/h => impossible for a runner 

The maximum speed stored for each lap is often wrong, example in test file
Edge705-Running-Heartrate-2Laps.tcx (from Edge 705):
- the calc. average speed is 11.76 km/h, the maximum speed is 10.085 km/h!
Workaround: the speed will be calculated for each trackpoint, so the 
maximum speed is taken from there.

The official "Garmin Training Center" software shows similar problems for
the included test exercise files. 


Notes for Timex users
---------------------

The ExerciseViewer is able to parse and display PWX exercise files. These
files are generated when the watch data is transferred from the watch to your
computer using the Timex Device Agent software (available only for Windows
and macOS systems.)
If you are connected to the Internet when transferring data from your watch,
the files are uploaded to the Training Peaks website and saved on your local
hard disk.  If you are not connected to to the Internet, the Timex Device
Agent software will save your PWX files on your hard disk until the next time
you are connected to the Internet and run Timex Device Agent.

Windows users can find the PWX files in the following directories:
- before transfer to trainingpeaks.com: %USERDIR%/Documents/TimexDA/Queued/
- after transfer to trainingpeaks.com: %USERDIR%/Documents/TimexDA/Sent/

The filenames created by the Timex Data Exchanger are automatically generated
but can be renamed without effecting usability. 
The standard format is:  TimexYYYYmmddHHMMSS_1.pwx
(YYYYmmdd and HHMMSS are the date and time when the exercise was started)

The initial version of the parser only works with data collected in CHRONO
mode. The watch does allow you to operate in both CHRONO and INTERVAL mode at
the same time so if you like to use INTERVAL mode on the watch, you can still
get your heart rate data by simultaneously operating in CHRONO mode.

If you wish to connect to the Internet but not send your data to the Training
Peaks website, you must block the Timex Device Agent with your Firewall. In
this case, the data files will be stored as though you were not connected to
the Internet.


SQLite export
-------------

Advances users with SQL skills can export all SportsTracker application data to
a SQLite v3 database. This provides much more capabilities for data analysis,
statistics or migration.
On each export a new database will be created in the users home directory, an
existing database will be overwritten. The database schema is defined in the
source file 'st-schema.sql'.

The SportsTracker application already contains the native SQLite libraries for
Windows, macOS and Linux (part of the sqlite-jdbc library). Users of other
systems must provide the native libraries manually.

There are many command line and graphical tools available for working with
SQLite. If you´re looking for a handy, cross platform, open source application
you should try 'DB Browser for SQLite' (http://sqlitebrowser.org/).

Note: SQLite does not provide a date and time data type, the export uses a
string in ISO 8601 format "yyyy-MM-dd HH:mm:ss" (see
https://www.sqlite.org/datatype3.html).
The queries can use the built-in functions for conversion and formatting (see
https://www.sqlite.org/lang_datefunc.html).

Example for a date query:
  select e.id, e.date_time, max(e.distance)
  from exercise e
  where date(e.date_time) between date('2015-06-29') and date('2015-12-31');


Developer Requirements
----------------------

For compilation of the SportsTracker sources you need:
  - Java SE Development Kit (JDK) 21 or greater
    (from http://jdk.java.net or https://adoptium.net)
  - Maven 3.8.0 or greater
    (from http://maven.apache.org)

Tested IDE's (should work an any IDE with Maven support)
  - IntelliJ IDEA Community Edition (http://www.jetbrains.com/idea/)
    => preferred IDE, tested with version 2023.2
  - NetBeans IDE (from http://www.netbeans.org), Kotlin support not tested
  - Eclipse (from http://eclipse.org), Kotlin support not tested

The Maven build configuration supports all typical goals (clean, compile,
test, package, ...). The project is split into following modules (Maven
multi project), so it's not possible to create circular module dependencies.

  - st-parent: 
    Maven parent project with shared configuration and dependencies 
  - sportstracker: 
    Main application component
  - st-exerciseviewer: 
    Component for parsing and displaying HRM exercise files, written mostly in
    Kotlin (was named PolarViewer before, but support now many other devices
    too)
  - st-util: 
    Component with common util classes for calculation, UI and more
  - st-packager:
    Module for creating native application packages for distribution
  - leafletmap:
    Module for the JavaFX wrapper component of the Leaflet map viewer

The configuration of your IDE (IDEA or Eclipse) is documented in the folder
'misc/ide-configuration'. It contains instructions how to import the project
into your IDE and how to setup the code formatter properly.
It's important to ensure a consistent code format and style all over the
project. That's why all developers need to use the same configuration for their
IDE.

SportsTracker can be started from the IDE by executing the class
"de.saring.sportstracker.STMain".
It can also be started from command line after execution of "mvn package"
with the command (inside the project root directory):
  java --module-path sportstracker/target/lib \
       --add-modules javafx.controls,javafx.fxml,javafx.web \
       -jar sportstracker/target/sportstracker-x.y.z.jar

All user interfaces are defined in FXML by using the JavaFX Scene Builder 9.x.

The SportsTracker project uses the following libraries:

  - OpenJFX 21.0.1 (https://openjfx.io/)
      License: GPL v2 + Classpath Exception
  - EasyDI 0.6.0 (https://github.com/lestard/EasyDI)
      Includes: jakarta.inject 2.0.1
      License: Apache License v2.0
  - Kotlin 1.9.20 (http://kotlinlang.org/)
      License: Apache License v2.0
  - JDOM 2.0.6.1 (http://www.jdom.org)
      License: Apache-style open source license
  - ControlsFX 11.1.2 (http://controlsfx.org/)
      License: BSD 3-Clause License
  - JFreeChart 1.5.3 and JFreeChart-FX 2.0.1 (http://www.jfree.org/)
      License: Lesser General Public License (LGPL)
  - LeafletMap 1.0.8 (https://github.com/ssaring/sportstracker)
      License: Apache License v2.0
  - leaflet-color-markers (https://github.com/pointhi/leaflet-color-markers)
    - Modified version, included in leafletmap
    - License: BSD 2-Clause License
  - commons-cli 1.5.0 (http://commons.apache.org/cli/)
      License: Apache License v2.0
  - sqlite-jdbc 3.43.2.1 (https://github.com/xerial/sqlite-jdbc)
      License: Apache License v2.0
  - JUnit 5.10.0 (http://www.junit.org)
      License: Eclipse Public License v2.0. and Apache License v2.0
  - Mockito 5.4.0 (http://code.google.com/p/mockito/)
      License: MIT License
  - Flexible & Interoperable Data Transfer (FIT) Protocol SDK 21.94.0
      URL: https://www.thisisant.com/developer/
      License: FIT Protocol License (open source by Dynastream / Garmin)
      License URL: https://developer.garmin.com/fit/download/

All dependencies will be downloaded automatically by Maven. The Garmin FIT
library is missing in the Maven central repository, so I've created my own
repository for it. It's available at: http://saring.de/st-maven-repo/

If you're wondering why the complete application has been ported from the .NET
platform (C# language, running on Mono) to the Java platform, here are the most
important reasons:

  - much better tooling support for development, e.g. IDE's, debuggers,
    profilers, refactoring tools and so on
  - much more useful open source libraries, e.g. for diagram creation
  - support for more operating systems (e.g. macOS, ...)
  - less installation problems (no .NET runtime required on Linux or macOS,
    no GTK+ libraries required on Windows and macOS)
  - I18N support on all operating systems
  - the Java platform is much more mature and reliable than Mono


Application Data Management
---------------------------

All application data is stored in a local SQLite database which is created on
initial application startup.
The data can be processed and evaluated easily by using 3rd party SQLite
database browsers (e.g. for custom statistics via SQL).

Development notes:
- SQLite storage implementation was done in plain JDBC to avoid further
  complexity, dependencies and performance impacts
- class STDocument contains all application data for read access, will be read
  on application start
  -> no further database queries for displaying, search, filter etc. needed
- all data modification is done via specific repositories, not in the lists
  handled by STDocument
  - STDocument:updateApplicationData() needs to be called manually after every
    application data change
  - STDocument will reload all application data for read access and notify all
    views for updates
- application data can be processed and evaluated easily by using 3rd party
  SQLite database browsers (e.g. for custom statistics via SQL)
- Documentation: https://www.sqlitetutorial.net/sqlite-java/

Comparison with prior XML file storage:
- SQLite storage provides faster loading times, almost no saving times
- database schema provides foreign keys between exercises, sport types etc. to
  ensure data consistency
- application memory consumption has not been increased compared to XML storage


Source Code Management
----------------------

The SportsTracker project uses Git for Source Code Management (SCM), the project
repository is hosted at GitHub. 
URL: https://github.com/ssaring/sportstracker

You can find further details for the SCM usage and the collaboration workflow
at the GitHub project site and in the Git / GitHub documentation.


Contact
-------

The website of the SportsTracker project can be found at:
https://www.saring.de/sportstracker

For enhancement requests or bug reports please use the issue tracker on the 
GitHub project page (https://github.com/ssaring/sportstracker/issues).

If you want to contribute improvements or translations, feel free to fork the
GitHub repository and submit Pull Requests. It would be great when bigger 
changes could be discussed before starting the implementation.

Before creating a translation for your language please take a look at the file
I18N.txt. Translations can also be send by mail, if you are not familiar with
GitHub usage.

For direct email contact you can use the address: projects@saring.de 


License
-------

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

The SportsTracker icon is based on icons from the open source projects GTK+
(http://www.gtk.org) and Tango (http://tango.freedesktop.org).
All the toolbar and menu icons are taken from the Free Version of the IcoMoon
project (https://icomoon.io, GPL licensed). Some icons are new creations
based on the IcoMoon icons.


Stefan Saring
2023/11/11
