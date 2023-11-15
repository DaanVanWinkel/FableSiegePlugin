# Siege Plugin
## Table of contents

- [Command completion](#command-completion)
- [Managing siege presets](#managing-siege-presets)
- [Managing teams](#managing-teams)
- [Starting and stopping siege presets](#starting-and-stopping)
- [Listing existing items](#listing-existing-items)

## Command completion

The main command is /fablesiege aliases are /fs and /siege.
Siege presets and team names get suggested but I can't show you all of the objectives per siege since it is all dynamic and I wouldn't be able to get this per siege preset. There is a [command](#listing-existing-items) to list anything that currently exist 

## Managing siege presets
### Creating presets
#### General flow

**/fs create siege <  siegeName > (amountOfRespawns)**
-   siegeName: the name you want to give the preset.
-   amountOfRespawns: is the amount of respawns the attacking team has, this defaults to 50

**/fs create objective <  siegeName > <  objectiveName > < captureTime > < objectiveNumber >**
-   siegeName: name of the preset where you want to make the objective
-   objectiveName: name of the objective 
-   captureTime: time for all points in the objective to be captured
-   objectiveNumber: order in which the objectives have to go (1 - whatever you want)

**/fs create point < siegeName > < objectiveName > < pointName > < radius >**
:   Points get created on your player location, doesn't save to world.
-   siegeName: name of the preset where you want to make the point
-   objectiveName: name of the objective where you want to make the point
-   radius: radius of the cirlce

**/fs create respawnpoint < siege > < attacking/defending >**
:   Respawnpoint gets created on your player location, doesn't save to world
-   siegeName: name of the preset where you want to make the respawnpoint
-   attacking/defending: choice of which side you want to create the respawnpoint of

### Removing presets
#### General flow

**/fs remove siege < siegeName >**
:   Removes entire siege with everything in it. Asks for confirmation.
-   siegeName: name of preset you want to delete

**/fs remove objective < siegeName > < objectiveName >**
:   Removes entire objectives including all capturepoints.
-   siegeName: name of preset where you want to delete the objective
-   objectiveName: name of objective you want to delete

**/fs remove point < siegeName > < objectiveName > < pointName >**
-   siegeName: name of preset where you want to delete the objective
-   objectiveName: name of objective where you want to delete the capturepoint
-   pointName: name of the point you want to delete

**/fs remove respawnpoint < siegeName > < attacking/defending >**
-   siegeName: name of preset where you want to delete the respawnpoint of selected team
-   attacking/defending: choice of which side you want to delete the respawnpoint of

## Managing teams
### Creating teams/adding players

**/fs team create < teamName >**
-   teamName: name for the team

**/fs team remove < teamName >**
-   teamName: name of the team

**/fs team addplayer < teamName > < playerName >**
-   teamName: name of the team
-   playerName: name of the player, checks if the player exists to catch typos also checks if player isn't in an other team already

**/fs team removeplayer < teamName > < playerName >**
-   teamName: name of the team
-   playerName: name of the player, checks if the player exists to catch typos

## Starting and stopping siege presets
### Starting a siege

**/fs load < siegeName > < attackingTeam(s) > < defendingTeam(s) >**
:   You are able to place more then one team inside either the attacking team or the defending team. By serperating them with a ',' like this: Blue,Green,Red. Important to have them against each other. ***NO SPACES***
-   siegeName: name of the siege you'd like to start 
-   attackingTeam(s): name(s) of the teams who should be doing the capturing of the points
-   defendingTeam(s): name(s) of the teams who should be doing the defending of the points

## Listing existing items

**/fs list sieges**
:   Lists all siege names that exist.

**/fs list objectives < siegeName >**
-   siegeName: name of the siege you want to see the objectives of

**/fs list points < siegeName > < objectiveName >**
-   siegeName: name of the siege
-   objectiveName: name of the objective you want to see the points of

**/fs list respawnpoints < siegeName >**
-   siegeName: name of the siege you want to see the respawnpoints of
