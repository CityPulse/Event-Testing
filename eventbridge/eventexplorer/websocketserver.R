library(httpuv)
library(jsonlite)


startWSServer <- function(){
  if(exists('wsserver')){
    stopDaemonizedServer(wsserver)
  }
  app <- list(
    onWSOpen = function(ws) {
      ws$onMessage(function(binary, message) {
        print(message)
        retValue = handleMessage(message)
        lastUpdate <<- Sys.time()
        ws$send(retValue)
      })
    }
  )
  wsserver <<- startDaemonizedServer(wshost, wsport, app)
}

stopWSServer <- function(){
  stopDaemonizedServer(wsserver)
  remove(wsserver)
}

