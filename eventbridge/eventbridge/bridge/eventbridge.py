import cherrypy
import threading
from websocket import create_connection
import sys
import os.path

sys.path.append(os.path.join(os.path.dirname(__file__), '..'))

from api.api import Api
from api.ui import Ui
from event.event import Event
from messagebus.rabbitmq import RabbitMQ
from misc.jsonobject import JSONObject

__author__ = 'Thorben Iggena (t.iggena@hs-osnabrueck.de)'


class EventBridge(object):

    def __init__(self, exchange_key_list):
#         self.exchange = exchange
#         self.routing_key = key
        self.exchange_key_list = exchange_key_list
        
        #event variables
        self.eventMap = {}
        
        #cherrypy interface
        self.ui = Ui(self)
        self.ui.api = Api(self)
        
        #reading config
        self.config = JSONObject(file(os.path.join(os.path.dirname(__file__), "..", "", "config.json"), "rb"))
        self.host = str(self.config.rabbitmq.host)
        self.port = self.config.rabbitmq.port
        self.websocketport = str(self.config.websocket.port)
        self.websockethost = self.config.websocket.host

    def start(self):
        print "start listening on", self.host, "with port", self.port
#         print "waiting for", self.routing_key, "on exchange", self.exchange
        if RabbitMQ.establishConnection(self.host, self.port):
            print ("Connection established")
        else:
            print ("Failed to connect, exiting")
            return
        
        if hasattr(RabbitMQ, 'channel'):
            queue = RabbitMQ.channel.queue_declare()
            queue_name = queue.method.queue
            
            for exchange_key in self.exchange_key_list:
                RabbitMQ.channel.queue_bind(exchange=exchange_key[0], queue=queue_name, routing_key=exchange_key[1])
            
            RabbitMQ.channel.basic_consume(self.onMessage, no_ack=True)
            print "start conssuming ..."
            RabbitMQ.channel.start_consuming()

    def stop(self):
        RabbitMQ.channel.stop_consuming()

    def onMessage(self, ch, method, properties, body):
        print "Message for key " + method.routing_key + " received:"
        print body
        #deconstruct message...
        e = self.parseMessage(body)
        self.sendToWebSocket(e)
        
    def sendToWebSocket(self, event):
        try:
            ws = create_connection("ws://"+ self.websockethost + ":" + self.websocketport)
            job = JSONObject()
            job.command = "newEvent"
            job.event = event.toJson()
            ws.send(job.dumps())
            print ws.recv()
            ws.close()
            return
        except:
            print "Unable to connect to websocket server, event dropped"
        
        
    def parseMessage(self, message):
        e = Event(message)
        self.eventMap[(e.source, e.eventclass)] = e
        return e
        
    def startBridge(self):
        #start cherrypy
        #threading.Thread(target=cherrypy.quickstart, args=(self.ui, '/', self.config.interface.raw())).start()
        #start messagebus
        self.start()
        
    def stopBridge(self):
        cherrypy.engine.exit()
        


if __name__ == '__main__':
    exchange_key_list = []
    exchange_key_list.append(("events", "#"))
    
    for exchange_key in exchange_key_list:
        print exchange_key[0] + " " + exchange_key[1]
    
    consumer = EventBridge(exchange_key_list)
    consumer.startBridge()                             
#         


    
