import cherrypy

__author__ = 'Thorben Iggena (t.iggena@hs-osnabrueck.de)'

class Ui(object):

    def __init__(self, eventbridge):
        self.eventbridge = eventbridge

    @cherrypy.expose
    def index(self):
        return "No UI available yet!"
