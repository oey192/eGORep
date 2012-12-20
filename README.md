eGORep
======

A minecraft plugin for eGO that adds reputation for players



Commands:
===

/rep up <username>: increase the reputation of username by 1
/rep down <username>: decrease the reputation of username by 1
/rep check [username]: check the reputation of yourself or username


Permissions:
===

egorep.*
gives access to everything except dedicated supporter

egorep.rep.*
gives access to up, down and check

egorep.rep.up
egorep.rep.down
egorep.rep.check
give access to their respecive commands

egorep.rep.check.self
allows the player to check their own reputation

egorep.rep.check.others
allows the player to check other players' reputations

egorep.show
allows the player to see when anyone gets a reputation increase or decrease
without this, a player won't even see when they get an increase or decrease

egorep.ds
gives the player 2 extra reputation points per the time unit in the config