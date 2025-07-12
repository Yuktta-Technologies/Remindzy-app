# Changelog

All notable changes to this project will be documented in this file.

The format follows [Keep a Changelog](https://keepachangelog.com/en/1.0.0/)  
and uses [Semantic Versioning](https://semver.org/).

---

## [Unreleased]

### Added

- Placeholder for upcoming changes.

---

## [1.0.1] - 2025-07-12

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
