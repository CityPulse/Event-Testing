# EventTesting
The event testing contains several components for testing the event system of the CityPulse framework:
- eventbridge
  - This folder contains a R shiny application, which shows a map to inspect current available events created within the CityPulse framework. An additional component is responsible to translate between the message bus and the R application, which cannot directly access the bus.
- eventrepublisher
  - This component is a simple python based helper component to repeat events sent by users of the eventtest_application to avoid enduring event resends by the Android application.
- eventtest_application
  - An Android application for displaying current events. It has also the possibility to report events by a user.

Further descriptions of the componens are included in the subfolders


## Contributers
The EventTesting components were developed as part of the EU project CityPulse. The consortium member University of Applied Sciences provided the main contributions for this component.

