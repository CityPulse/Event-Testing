import cherrypy

from misc.jsonobject import JSONObject

__author__ = 'Thorben Iggena (t.iggena@hs-osnabrueck.de)'


class Api(object):
    def __init__(self, eventbridge):
        self.eventbridge = eventbridge
        
    @cherrypy.expose
    def numberofevents(self):
        rJOb = JSONObject()
        rJOb.numberOfEvents = len(self.eventbridge.eventMap)
        return rJOb.dumps()


    @cherrypy.expose
    def getevents(self):
        rJOb = JSONObject()
        rJOb.events = []
        for key in self.eventbridge.eventMap:
            rJOb.events.append(self.eventbridge.eventMap[key].toJson());
        return rJOb.dumps()
    