
# This is the user-interface definition of a Shiny web application.
# You can find out more about building applications with Shiny here:
# 
# http://www.rstudio.com/shiny/
#

library(shiny)
library(leaflet)
library(dplyr)
source("eventhandler.R")

shinyUI(navbarPage("Event Explorer", id="nav",
  tabPanel("Event Map",
    h3("Event Map"),

    div(class="outer",
      tags$head( # Include our custom CSS
        includeCSS("styles.css")#,
      ),
        
      leafletOutput("eventmap", width="100%", height="100%"),
        
      absolutePanel(id = "controls", class = "panel panel-default", fixed = TRUE,
                    draggable = TRUE, top = 60, left = "auto", right = 20, bottom = "auto",
                    width = "auto", height = "auto",

          selectInput("category", "Category", c("All", try(getEventCategories()))),
          selectInput("subcategory", "Subcategory", "All"),
          verbatimTextOutput("test")
      )
    )
  ),
  tabPanel("Event List",
    h3("List of events"),
    actionButton("button_refresh", "Refresh Events"),
    dataTableOutput("eventlist"),
    checkboxInput("clearbox", "Check to clear events", value=FALSE),
    actionButton("clearevents", "Clear Events")
  )
))
