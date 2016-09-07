eventList = {}
lastUpdate = NULL

eventFrame <- data.frame(
                eSource=character(),
                lat=double(),
                lon=double(),
                level=integer(),
                type=character(),
                eventclass=character(),
                id=character(),
                time=as.POSIXct(character()),
                stringsAsFactors=FALSE)

#websocket connection
wshost = "0.0.0.0"
wsport = 9455
