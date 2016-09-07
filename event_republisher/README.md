# Event Republisher
This component acts as a simple helper for the Event Report Application. To avoid a continous resending of messages from the mobile application to the message bus, this component takes over the job of resending event to the message bus in intervals of one minute. It is able to react for changed "levels" of received events and to delete events if a "level 0" is received.
blabla


## Requirements and Dependencies
- Python 2.7
- rdflib (there is a patch provided to avoid some problems with class "double" in rdflib, pach could be installed via provided script)
- several python classes from CityPulse Framework, see imports in event_republisher.py

## Installation
After fulfilling requirements just run the start script. 

## Contributers
The Event Republisher component was developed as part of the EU project CityPulse. The consortium member University of Applied Sciences provided the main contributions for this component.


