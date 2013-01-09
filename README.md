eGORep
======

A minecraft plugin for eGO that adds reputation for players



Commands:
===

/rep up \<username\>: increase the reputation of username by 1

/rep down \<username\>: decrease the reputation of username by 1

/rep check [username]: check the reputation of yourself or username

/rep version: get the current version

/rep log [pageNum]: show a log of reputation actions (up, down, and set)

/rep reload: reloads the configuration file from disk

/rep help: show the help for the plugin

/rep set \<username\> \<value\>: sets the reputation of username to value. Only available to console


Permissions:
===

egorep.*<br/>
gives access to everything except dedicated supporter

egorep.rep<br/>
gives access to up, down and check

egorep.rep.up<br/>
egorep.rep.down<br/>
egorep.rep.check<br/>
give access to their respecive commands

egorep.rep.check<br/>
gives access to egorep.rep.check.self and egorep.rep.check.others

egorep.rep.check.self<br/>
allows the player to check their own reputation

egorep.rep.check.others<br/>
allows the player to check other players' reputations

egorep.show<br/>
allows the player to see when anyone gets a reputation increase or decrease
without this, a player won't even see when they get an increase or decrease

egorep.showset<br/>
allows the player to see when the console directly sets their reputation

egorep.reload<br/>
allows the player to reload the configuration file

egorep.ds<br/>
gives the player 2 extra reputation points per the time unit in the config

egorep.version<br/>
allows the player to use /rep version to see the current version of the plugin

egorep.getlog<br/>
allows the player to get the log of all reputation actions that have been issued


Changelog 1.1.8
===

Major update! Changes will need to be made to some server configurations!

Database field 'rep' needs to be changed to type DOUBLE
you must add the line
logTable: \<tableNameHere\>
to the config file in the mysql section


Changes:

ability to check reputation of offline players<br/>
ability to check and modify reputation based on display name (nickname)
logging system<br/>
new algorithm to limit people's ability to rep the same person all the time<br/>
more helpful messages if a connection to the Database server cannot be made<br/>
