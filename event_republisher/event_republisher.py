#!/bin/python

__author__ = "Marten Fischer <m.fischer@hs-osnabrueck.de>"

"""
This script receives events on a specific exchange on the MessageBus and republishes them on the 'standard' events exchange in a given interval.
The idea is that mobile devices may report an event only once. Components and other consumers on the MessageBus, which where not active at that
time, would miss it. This script avoids that. The event is as long republished until an other event with identical type and source but with level
0 is received.
This class needs some additional modules from resourcemanagement. If not started from within it please get files from CP_Resourcemanagement and 
reconfigure imports.
"""

import rdflib
import threading
import time
import datetime
import json
import os.path
import sys
sys.path.append(os.path.join(os.path.dirname(__file__), '..'))
from rabbitmq import RabbitMQ
from virtualisation.misc.threads import QueueThread

### CONFIG START

# specify the MessageBus Exchange where repeatable events shall be received
REPEATABLE_EVENTS_EXCHANGE = RabbitMQ.exchange_repeatable_events #RabbitMQ.exchange_events #

# specifiy the MessageBus Exchange where the events will be republished on
EVENTS_EXCHANGE = RabbitMQ.exchange_events

# number of seconds before received events will be republished in the message bus
REPUBLISH_INTERVAL = 60

### CONFIG END

time_format = "%Y-%m-%dT%H:%M:%S" # we don't care about milliseconds
print_time = lambda time_object: datetime.datetime.strftime(time_object, time_format) #+ ".000Z"

event_buffer = {}
stop_thread = False

class Config(object):
	"""mini config class"""
	def __init__(self):
		f = open(os.path.join(os.path.dirname(__file__), "..", "virtualisation", "config.json"), "rb")
		j = json.load(f)
		self.host = j["rabbitmq"]["host"]
		self.port = j["rabbitmq"]["port"]
		self.username = j["rabbitmq"]["username"] if "username" in j["rabbitmq"] else None
		self.password = j["rabbitmq"]["password"] if "password" in j["rabbitmq"] else None

	def __repr__(self):
		return ", ".join([self.host, str(self.port), str(self.username), str(self.password)])

def republish_thread(qThread):
	while not stop_thread:
		time.sleep(REPUBLISH_INTERVAL)
		if stop_thread:
			break
		for _id in event_buffer:
			evt, rk = event_buffer[_id]
			evt.update_time()
			print"republishing event", evt, "on routing key", rk
			# republish on the message bus
			#RabbitMQ.sendMessage(evt.n3(), EVENTS_EXCHANGE, rk)
			qThread.add((evt.n3(), EVENTS_EXCHANGE, rk))
			print evt.n3()
		print "event buffer length", len(event_buffer)

def sendMessage(tuple):
	msg = tuple[0]
	exchange = tuple[1]
	key = tuple[2]
	print "resending event", msg, "on routing key", key
	RabbitMQ.sendMessage(msg, exchange, key)

class RepeatableEvent(object):
	def __init__(self, graph_n3, event_id):
		self.g = rdflib.Graph()
		self.g.parse(data=graph_n3, format='n3')

		self.event_id, self.event_type = self.find_event_type()
		if "#" in self.event_id:
			self.event_id = self.event_id.split("#")[1]
		elif ":" in self.event_id:
			self.event_id = self.event_id.split(":")[1]

		self.lvl = self.inspect_event(self.g)

	def __repr__(self):
		return self.event_id + "@" + self.lvl

	def find_event_type(self):
		for stmt in self.g.subject_objects(rdflib.URIRef("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")):
			if stmt[0].startswith("http://"):
				return str(stmt[0]), str(stmt[1])

	def inspect_event(self, evt):
		# Get the level of the event.
		lvl = None
		q = "select ?s ?o WHERE {?s sao:hasLevel ?o}"
		qres = evt.query(q)
		for _qres in qres:
			_, lvl = _qres

		# Wuery for the event itself. Needed to update the time later
		q = "select ?s ?o WHERE {?s a <" + str(self.event_type) + "> }"
		self.myself = "-"
		qres = evt.query(q)
		for _qres in qres:
			self.myself, _ = _qres

		return str(lvl)

	def update_time(self):
		"""Updates the timestamp in a RDF event graph"""
		n = rdflib.Namespace("http://purl.org/NET/c4dm/timeline.owl#")
		nt = rdflib.Literal(print_time(datetime.datetime.now()), datatype=rdflib.XSD.dateTime)
		self.g.set( [self.myself, n.time, nt] )
		self.g.commit()

	def n3(self):
		return self.g.serialize(format='n3')

class MessageBusConsumer(object):
    """Listener for new events on the message bus."""

    def __init__(self, exchange, key, qThread):
    	c = Config()

        self.exchange = exchange
        self.routing_key = key
        self.qThread = qThread
        rmq_host = str(c.host)
        rmq_port = c.port
        rmq_username = c.username
        rmq_password = c.password
        print "connecting with username", rmq_username, "and password", rmq_password

        if rmq_username:
            if rmq_password:
                print ("Connection established" if RabbitMQ.establishConnection(rmq_host, rmq_port, rmq_username, rmq_password) else "Failed to connect")
            else:
                print ("Connection established" if RabbitMQ.establishConnection(rmq_host, rmq_port, rmq_username) else "Failed to connect")
        else:
            print ("Connection established" if RabbitMQ.establishConnection(rmq_host, rmq_port) else "Failed to connect")

        print "start listening on", rmq_host, "with port", rmq_port
        print "waiting for", key, "on exchange", exchange

    def start(self):
        if hasattr(RabbitMQ, 'channel'):
            queue = RabbitMQ.channel.queue_declare()
            queue_name = queue.method.queue
            RabbitMQ.channel.queue_bind(exchange=self.exchange, queue=queue_name, routing_key=self.routing_key)
            RabbitMQ.channel.basic_consume(self.onMessage, no_ack=True)
            # register Exchange and Queue for repeatable events
            RabbitMQ.registerExchanges(RabbitMQ.channel)
            print "start consuming ..."
            RabbitMQ.channel.start_consuming()
        else:
        	print "Can not start the MessageBusConsumer. Not connected."

    def stop(self):
        RabbitMQ.channel.stop_consuming()

    def onMessage(self, ch, method, properties, body):
        # print method.routing_key
        print "received message: " + body
        evt_received(body, method.routing_key)
        self.qThread.add((body, EVENTS_EXCHANGE, method.routing_key))

def main():
	global stop_thread
	# start sending thread with queue
	qThread = QueueThread(handler=sendMessage)
	qThread.start()

	# start thread to republish received events until one with level 0 was received
	threading.Thread(target=republish_thread, args=(qThread,)).start()

	# start listening on the message bus for new 'repeatable' events
	mbc = MessageBusConsumer(REPEATABLE_EVENTS_EXCHANGE, "#", qThread)
	threading.Thread(target=mbc.start).start()

	raw_input("Press ENTER to stop.\n")
	print "stoping threads...",
	stop_thread = True
	mbc.stop()
	print "done"


def evt_received(evt, routing_key):
	re = RepeatableEvent(evt, routing_key)
	if re.lvl == "0":
		if re.event_id in event_buffer:
			# remove the event from the buffer
			del event_buffer[re.event_id]
	else:
		# add the event to the buffer
		event_buffer[re.event_id] = (re, routing_key)

	# print re.n3()

if __name__ == '__main__':
	main()
