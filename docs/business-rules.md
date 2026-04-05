# Business Rules
This document defines the core domain rules that apply across the application, independent of specific user flows.

## Character
- **BR-1**: A character belongs to exactly one user (the creator)
- **BR-2**: Only the character owner can modify character details
- **BR-3**: A character cannot participate in multiple campaigns simultaneously
- **BR-4**: Only the character owner can delete a character
- **BR-5**: When a character leaves a campaign, the character remains owned by the player
- **BR-6**: When a user is deleted, all character sheets owned by that user are deleted

## Campaign
- **BR-7**: A campaign has exactly one Game Master
- **BR-8**: A campaign can have 1–8 players (configurable by the Game Master)
- **BR-9**: Only the Game Master can invite or remove players
- **BR-10**: Players may leave a campaign freely; the Game Master cannot leave their own campaign
- **BR-11**: Game Masters can delete campaigns they own, which removes all associated campaign data
- **BR-12**: Only the Game Master can modify campaign settings
- **BR-13**: Only characters owned by campaign participants can be assigned to a campaign
- **BR-14**: When a user who owns a campaign is deleted, that campaign and all its associated data are deleted

## Campaign Documentation
- **BR-15**: Campaign notes are private to the Game Master and are not visible to players
- **BR-16**: Session logs are created by the Game Master and are readable by all campaign participants

## Permissions
- **BR-17**: Game Masters can view full mechanical details of all characters in their campaigns
- **BR-18**: Players can view limited public information of characters in shared campaigns
- **BR-19**: Character notes are private and visible only to the character owner
- **BR-20**: Game Masters cannot view or modify player-created character notes
