
# This is the server logic for a Shiny web application.
# You can find out more about building applications with Shiny here:
# 
# http://www.rstudio.com/shiny/
#

library(shiny)
source("websocketserver.R")
source("eventhandler.R")

shinyServer(function(input, output, session) {
  #start websocket server to receive events in global variable "eventFrame"
  startWSServer()

  category = reactive({
    input$category
  })
  
  subcategory = reactive({
    input$subcategory
  })
  
  #event listener for map to update markers, input fields etc.
  mapevent = reactive({
    input$eventmap_bounds
    input$eventmap_click
    input$eventmap_zoom
  })
  
  observe({
    mapevent()
    updateCategories()
    updateMarkers()
  })
  
  #test button click event for further evaluation of event
  #id of the event is received
  checkevent <- eventReactive(input$button_click, {
    print("clicked")
    print(input$button_click)
  })
  output$test <- renderText({paste("test:", checkevent())})
  
  
  observe({
    tryCatch({
      subcategories = NULL
      if(category() == "All")
        subcategories = getEventSubcategoriesAll()
      else
        subcategories = getEventSubcategories(category())
      updateSelectInput(session, inputId = "subcategory", choices = c("All", subcategories))
    },
    error=function(cond){
      updateSelectInput(session, inputId = "subcategory", choices = "All")
    })
  })
  
  observe({
    category()
    subcategory()
    updateMarkers()
  })
  
  observeEvent(input$clearevents, {
    if(input$clearbox){
      clearEvents()
      output$eventlist <- renderDataTable(eventFrame)
      updateCheckboxInput(session, inputId = "clearbox", value=FALSE)
    }
  })

  #refresh event table on refresh button click
  observe({
    input$button_refresh
    output$eventlist <- renderDataTable(eventFrame)
  })
  
  #render map of Aarhus as starting point
  output$eventmap <- renderLeaflet({
    leaflet() %>% 
      addTiles(
        urlTemplate = "//{s}.tiles.mapbox.com/v3/jcheng.map-5ebohr46/{z}/{x}/{y}.png",
        attribution = 'Maps by <a href="http://www.mapbox.com/">Mapbox</a>'
      ) %>%
      setView(lng = 10.21, lat = 56.1575, zoom = 13)
  })
  
  #function to create markers for newest events for category and subcategory
  updateMarkers <- function(){
    # data = getNewestData(input$category, input$subcategory)
    data = getRunningEvents(input$category, input$subcategory)
    # data = getRunningEvents
    if(length(data) > 0){
      leafletProxy("eventmap", data=data) %>% clearMarkers() %>% 
        addMarkers(~lon, ~lat, popup=~content(id, type, eventclass, level, eSource, time), icon=~icon(eventclass, eSource))
    }
  }
  
  #creates html output for event popup including action button for further event evaluation
  content <- function(id, type, eventclass, level, eSource, time){
    popup = paste(sep = "",
      "EventID: ", id, "<br/>",
      "Type: ", type, "<br/>",
      "Class: ", eventclass, "<br/>",
      "Level: ", level, "<br/>",
      "Source: ", eSource, "<br/>",
      "Time: ", time, "<br/>")
    button = paste("<button onclick='Shiny.onInputChange(\"button_click\",  ",
      "\"", id, "\"",
      ")' id='checkevent' type='button' class='btn btn-default action-button'>Check Event</button>", sep="")
    return (paste(popup, button, sep="<br/>"))
  }
  
  icon <- function(eventclass, eSource){
    tmp = unlist(strsplit(eSource, "_"))
    tmp = tmp[seq(1, length(tmp), 2)]
    iconname = paste(eventclass, tmp, sep="_")
    iconname = unlist(lapply(iconname, iconcheck))
    return (icons[iconname])
  }
  
  iconcheck <-function(iname){
    if (! iname %in% names(icons)){
      return ("DEFAULT")
    }
    return (iname)
  }
  
  #create list of icons
  icons <- iconList(
    PublicParking_SENSOR = makeIcon("parking.png", "parking.png", 25, 25),
    TrafficJam_SENSOR = makeIcon("trafficjam.png", "trafficjam.png", 25, 25),
    PublicParking_USER = makeIcon("parking_user.png", "parking_user.png", 25, 25),
    TrafficJam_USER = makeIcon("trafficjam_user.png", "trafficjam_user.png", 25, 25),
    DEFAULT = makeIcon("default.png", "default.png", 25, 30)
  )
  
  updateCategories <- function(){
    categories = getEventCategories()
    selected = input$category
    updateSelectInput(session, inputId = "category", choices = c("All", categories), selected = selected)
  }
  
})
