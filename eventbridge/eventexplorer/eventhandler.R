

handleMessage <- function(message){
  job = fromJSON(message)
  if (job$command == "newEvent"){
    event = job$event
    eSource = as.character(event$source)
    lat = as.double(event$lat)
    lon = as.double(event$lon)
    level = as.integer(event$level)
    type = as.character(event$type)
    eventclass = as.character(event$eventclass)
    id = as.character(event$identifier)
    time = as.POSIXct(event$time, format="%Y-%m-%dT%H:%M:%S")
    newEvent = data.frame(eSource, lat, lon, level, type, eventclass, id, time, stringsAsFactors=FALSE)
    eventFrame <<- rbind(eventFrame, newEvent)
    return ("ok")
  }
}

clearEvents <-function(){
  eventFrame <<- eventFrame[0,]
}

getEventSource <-function(eventid){
  return (unlist(strsplit(eventFrame[eventFrame$id == eventid,]$eSource, "_"))[1])
}

getSensorEvents <-function(){
  return (eventFrame[startsWith(eventFrame$eSource, "SENSOR"),])
}

getUserEvents <-function(){
  return (eventFrame[startsWith(eventFrame$eSource, "USER"),])
}

getEventCategories <-function(){
  return (unique(eventFrame$type))
}

getEventSubcategories <-function(category){
  return (unique(eventFrame[eventFrame$type == category,]$eventclass))
}

getEventSubcategoriesAll <-function(){
  return (unique(eventFrame$eventclass))
}

getNewestEvents <-function(){
  maxTime = max(eventFrame$time)
  return (eventFrame[eventFrame$time==maxTime,])
}

getOldestEvents <-function(){
  minTime = min(eventFrame$time)
  return (eventFrame[eventFrame$time==minTime,])
}

getEventsSensor <- function(sensor){
  return (eventFrame[eventFrame$eSource==sensor,])
}

getEventsSensorType <- function(sensor, type){
  events = getEventsSensor(sensor)
  return (events[events$type==type,])
}

getEventsSensorTypeClass <- function(sensor, type, class){
  events = getEventsSensorType(sensor, type)
  return (events[events$eventclass==class,])
}

getNewestEventForSensor <- function(sensorId){
  sensData = eventFrame[eventFrame$eSource == sensorId,]
  maxTime = max(sensData$time)
  sensData = sensData[sensData$time == maxTime,]
  return (sensData)
}

getNewestDataForEachSensor <- function(){
  sensIds = unique(eventFrame$eSource)
  tmp = data.frame()
  for (id in sensIds){
    tmp = rbind(tmp, getNewestEventForSensor(id))
  }
  return (tmp)
}

getNewestDataForCategory <- function(category){
  sensIds = unique(eventFrame[eventFrame$type==category,]$eSource)
  tmp = data.frame()
  for (id in sensIds){
    tmp = rbind(tmp, getNewestEventForSensor(id))
  }
  return (tmp)
}

getNewestDataForCategorySubcategory <- function(category, subcategory){
  sensIds = unique(eventFrame[eventFrame$type==category & eventFrame$eventclass==subcategory,]$eSource)
  tmp = data.frame()
  for (id in sensIds){
    tmp = rbind(tmp, getNewestEventForSensor(id))
  }
  return (tmp)
}

getNewestDataForSubcategory <- function(subcategory){
  sensIds = unique(eventFrame[eventFrame$eventclass==subcategory,]$eSource)
  tmp = data.frame()
  for (id in sensIds){
    tmp = rbind(tmp, getNewestEventForSensor(id))
  }
  return (tmp)
}

getNewestData <- function(category, subcategory){
  if(category =="All" && subcategory=="All")
    return (getNewestDataForEachSensor())
  else if(subcategory=="All")
    return (getNewestDataForCategory(category))
  else if (category=="All")
    return (getNewestDataForSubcategory(subcategory))
  return (getNewestDataForCategorySubcategory(category, subcategory))
}

getRunningEvents <- function(category, subcategory){
  if(category =="All" && subcategory=="All")
    return (getAllRunningEvents())
  else if(subcategory=="All")
    return (getRunningEventsForCategory(category))
  else if (category=="All")
    return (getRunningEventsForSubcategory(subcategory))
  return (getRunningEventsForCategorySubcategory(category, subcategory))
}

getRunningEventsForCategorySubcategory <- function(category, subcategory){
  data = getAllRunningEvents()
  return (data[data$type==category & data$eventclass==subcategory,])
}

getRunningEventsForSubCategory <- function(subcategory){
  data = getAllRunningEvents()
  return (data[data$eventclass == subcategory,])
}

getRunningEventsForCategory <- function(category){
  data = getAllRunningEvents()
  return (data[data$type == category,])
}

getAllRunningEvents <- function(){
  eventids = getEventIDs()
  tmp = data.frame()
  for(id in eventids){
    if(isEventRunning(id))
      tmp = rbind(tmp, getNewestDataForEventId(id))
  }
  return (tmp)
}

getNewestDataForEventId <- function(eventid){
  event = eventFrame[eventFrame$id == eventid,]
  maxtime = max(event$time)
  lasteventdata = event[event$time == maxtime,]
  return (lasteventdata)
}

getEventDuration <- function(eventid){
  
}

getEventIDs <- function(){
  return (unique(eventFrame$id))
}

isEventRunning <- function(eventid){
  if(getNewestDataForEventId(eventid)$level != 0)
    return (TRUE)
  return (FALSE)
}
