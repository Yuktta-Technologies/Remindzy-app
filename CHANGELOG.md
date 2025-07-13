# Changelog

All notable changes to this project will be documented in this file.

The format follows [Keep a Changelog](https://keepachangelog.com/en/1.0.0/)  
and uses [Semantic Versioning](https://semver.org/).

---

## [Unreleased]

### Added

- Placeholder for upcoming changes.

---

## [1.0.7] - 2025-07-13

- Worked on the ReminderListScreen.kt file

### [Added]

- Integrated `getCategoryColor()` and `getCategoryTextColor()` from `CategoryUtils.kt` into the reminder card display in `ReminderListScreen.kt` to dynamically style the category tag.

### [Changed]

- **Finance category colors updated** in `CategoryUtils.kt`:

  - Background: `#FFF9C4` → `#D7CCC8` (Soft Brown-Grey)
  - Text: `#F57F17` → `#4E342E` (Deep Brown)

- In `ReminderListScreen.kt`:

  - Replaced hardcoded color pairs with function-based values:

    ```kotlin
    val bgColor = getCategoryColor(reminder.category)
    val textColor = getCategoryTextColor(reminder.category)
    ```

  - This change ensures consistent and scalable category styling across the UI.

## [1.0.6] - 2025-07-13

- Worked on the AddReminderDialog.kt file

### [Added]

- Category selection feature in `AddReminderDialog.kt`.
- New `CategoryDropdownMenuBox` composable for selecting categories.
- Category enum class `ReminderCategory` with values like `Personal`, `Work`, and `Health`.

### [Changed]

- UI layout updated in `AddReminderDialog.kt` to place category dropdown next to repeat option.
- Extended the `onSave` callback to include a category parameter.
- Updated `Reminder.kt` data model to include a `category: ReminderCategory` field.
- Updated Room database schema to handle the new `category` column.
- Updated related DAO, repository, and database migration logic to support the new category field.

## [1.0.6] - 2025-07-13

- Worked on the AddReminderDialog.kt file

### Added

- Separate **Start Date** and **Start Time** buttons instead of a combined picker.
- Separate **End Date** and **End Time** buttons for better clarity.
- **Rounded corner customization** (`10dp`) for `OutlinedTextField`s and buttons.
- Custom **Switch color**:
  - Purple thumb when enabled
  - Light purple track
- Date and time formatting using:
  - `dd/MM/yyyy` for date
  - `h:mm a` for time

### Changed

- **Start Time button** now correctly shows time instead of date.
- Modified **end time updating logic**:
  - Reassigns `Calendar` instance directly
  - Ensures immediate UI update on time selection
- Updated **OutlinedTextField** styling:
  - Label colors for focused and unfocused states
  - Text color is now black in both states
  - Cursor color is black
- Customized **Save button**:
  - Purple background (`#6600EB`)
  - Capsule shape with `RoundedCornerShape(50)`
- Customized **Cancel button**:
  - Text color set to dark (`#1A1A1A`)
- Dialog **Title** styling:
  - Uses dark color and `titleLarge` typography

## [1.0.5] - 2025-07-12

- Redesigned ReminderItem card layout:
  - Added start and end **dates** next to respective **times** with calendar icons.
  - Ensured both date and time appear in the **same row** for better alignment.
  - Added **repeat mode badge** under the category tag with light blue background and blue text.
- Adjusted spacing and alignment to ensure proper layout across devices (emulator and real phones).
- Finalized UI based on Figma design for the reminder home screen.

## [1.0.4] - 2025-07-12

### Added

- Added a **repeat mode badge** (e.g., "Daily", "Weekly", "Monthly") below the category tag on each reminder card.
- Styled the repeat badge with:
  - Background color: `#DBEAFE`
  - Text color: `#5C7FCC`
  - Rounded corners and compact padding.

### Changed

- Aligned the repeat badge precisely under the category tag using `padding(top = 78.dp, end = 14.dp)`.

## [1.0.3] - 2025-07-12

### Added

- Displayed start and end times with a clock (`Schedule`) icon on each reminder card.
- Added start and end dates next to the corresponding times with a calendar icon.
- Ensured both time and date appear on the same row for a cleaner, compact layout.

### Changed

- Adjusted padding and spacing to align start/end time and date elements neatly.
- Fine-tuned icon sizes and text alignment for visual consistency.

## [1.0.2] - 2025-07-12

### Changed

- Adjusted clock icon and time layout for both Start and End times.
- Reduced spacing between clock icon and Starts: / Ends: text for better visual alignment.
- Increased space between End: label and time for improved readability.
- Changed the clock color and the text color to `#6C6C6C`
- Removed the start and end date visibility for now. Will implement next.

## [1.0.1] - 2025-07-12

### Added

- Implemented customized category tag design for "Personal":
  - Background color: `#FFEDD5`
  - Text color: `#CD7C5D`
- Aligned the category tag directly below the 3-dot overflow menu.

### Changed

- Ensured the tag stays in position without overlapping even when the title/description are long.
- Reverted from `libs.androidx.constraintlayout.compose` to `androidx.constraintlayout:constraintlayout-compose:1.1.1` for compatibility.

---

## [1.0.0] - 2025-07-09

### Added

- Created a new Reminder Card layout with rounded corners and white background (`#FFFFFF`).
- Introduced a vertical orange bar (`#FF7043`) on the left side of each card.
- Added a 3-dot overflow menu (`MoreVert`) to each reminder card.
- Moved `Edit` and `Delete` actions inside the overflow menu.
- Added category tag display inside the card with soft styling.
- Implemented category filter UI with a dropdown for `All`, `Personal`, `Work`, `Health`, and `Finance`.
- Configured `ReminderListScreen` to filter reminders by selected category.

### Changed

- Changed overall screen background color to `#F9FAFB` for a cleaner look.
- Made the title, description, and timestamps inside the cards darker for better readability.
- Styled the `TopAppBar` and `FloatingActionButton` with a purple theme (`#8E24AA`).
- Adjusted dropdown menu alignment and icon size for consistent UI.
- Minor padding and spacing tweaks to match the Figma design closely.

---

## [0.1.0] - 2025-06-30

### Added

- Initial setup for the Remindzy app with add, edit, and delete reminder functionality.
- Integrated Room DB with `ReminderViewModel`.
- Enabled scheduling of reminders with start time, end time, and repeat mode.
