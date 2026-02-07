# Business Rules
This document defines the core domain rules that apply across the application, independent of specific user flows.

## Character
- **BR-1**: A character belongs to exactly one user (the creator)
- **BR-2**: Only the character owner can modify character details
- **BR-3**: A character cannot participate in multiple campaigns simultaneously
- **BR-4**: Only the character owner can delete a character
- **BR-5**: When a character leaves a campaign, the character remains owned by the player

## Campaign
- **BR-6**: A campaign has exactly one Game Master
- **BR-7**: A campaign can have 1–8 players (configurable by the Game Master)
- **BR-8**: Only the Game Master can invite or remove players
- **BR-9**: Players may leave a campaign freely; the Game Master cannot leave their own campaign
- **BR-10**: Game Masters can delete campaigns they own, which removes all associated campaign data
- **BR-11**: Only the Game Master can modify campaign settings
- **BR-12**: Only characters owned by campaign participants can be assigned to a campaign

## Campaign Documentation
- **BR-13**: Campaign notes are private to the Game Master and are not visible to players
- **BR-14**: Session logs are created by the Game Master and are readable by all campaign participants

## Permissions
- **BR-15**: Game Masters can view full mechanical details of all characters in their campaigns
- **BR-16**: Players can view limited public information of characters in shared campaigns
- **BR-17**: Character notes are private and visible only to the character owner
- **BR-18**: Game Masters cannot view or modify player-created character notes
