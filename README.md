# CRF Management Tool

A Java Swing desktop project for organizing tasks, meeting notes, reminders
and spreadsheet-based project tracking. It uses internal windows so several
views can be open together.

## What is in the source

- Dashboard and PSL tracker that read Excel workbooks through Apache POI
- Task lists, meeting notes, sticky-note editing and reminders
- Appearance and application settings with property-change notifications
- Internal-window cascade, tiling, reopening and pop-out controls
- A Windows-specific command that opens the installed Outlook calendar

The calendar action is a local launcher, not Outlook API synchronization.
The current source does not implement GitHub repository management.

## Requirements and setup

Use a full JDK 17 or newer, Maven, and a graphical desktop. The Maven
dependency is Apache POI 5.2.3 as declared in [pom.xml](pom.xml).
The Outlook launcher additionally requires Windows and an appropriate
registered application.

```sh
git clone https://github.com/Danmachi1/Management-Tool.git
cd Management-Tool
mvn -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package
```

Import the Maven project into an IDE and run
`com.crfmanagement.gui.MainApp`. Set the launch working directory to a
new, empty directory outside the checkout. This prevents loading the
repository's existing local-data files. Build from source rather than
reusing the committed `target/` output.

The build command is provided for a normal JDK/Maven setup. A complete
Maven build and graphical launch were not verified in the focused
settings regression pass described below.

## Local data and safe demonstrations

The application reads and writes `notes.dat`, `tasks.dat`,
`reminders.dat` and `settings.properties` relative to its working
directory. The `.dat` files use Java serialization: never load files from
an untrusted source. Use synthetic notes and workbooks for demonstrations.
Do not commit personal data, customer records or local file paths.

The Excel readers expect particular columns on the first worksheet; they
are not a generic spreadsheet importer. See [ExcelReader](src/main/java/com/crfmanagement/utils/ExcelReader.java)
and [DashboardPanel](src/main/java/com/crfmanagement/dashboard/DashboardPanel.java)
before preparing a fixture.

## Settings regression tests

The focused tests need Python 3 and JDK 17 or newer, but no Maven,
third-party test library, graphical session or network access:

```sh
python3 scripts/test_settings.py
```

On Windows, `python scripts/test_settings.py` is equivalent. Put the JDK
on PATH; alternatively set `JAVA` to the Java executable. The runner uses
`javac` when available, otherwise the JDK compiler module.

Each case runs in its own temporary working directory. Tests cover
defaults, hex/RGB colors, malformed files, invalid updates, null updates,
reload behavior, typed change events and ordinary setting persistence.

Color values accept `#RRGGBB` or three comma-separated RGB integers.
Malformed stored colors fall back to white without destroying unrelated
settings. Invalid updates are rejected before mutating or saving state.

Focused validation on OpenJDK 21: **13/13 scenarios passed**.
With the original SettingsManager source and the same tests, **8/13
scenarios failed**. This is scoped regression evidence, not proof of
complete application or dependency security.

## Project scope and licensing

This desktop project focuses on local task management and Excel-based
tracking. It does not include a hosted service, encryption or user access
controls.

The older README stated MIT licensing, but no root LICENSE file is
present. This documentation does not establish or change licensing terms.
