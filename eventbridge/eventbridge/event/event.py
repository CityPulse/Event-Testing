from rdflib.graph import Graph
from rdflib.term import URIRef
from misc.jsonobject import JSONObject

__author__ = 'Thorben Iggena (t.iggena@hs-osnabrueck.de)'


class Event(object):
    def __init__(self, n3message):   
        graph = Graph()
        result = graph.parse(data=n3message, format="n3")
        self.identifier = str(self.parseRoot(graph)[0])
        if "#" in self.identifier:
            self.identifier = self.identifier.split("#")[1]
        self.eventclass = str(self.parseRoot(graph)[1])
        if "#" in self.eventclass:
            self.eventclass = self.eventclass.split("#")[1]
        self.source = self.parseAttribute(graph, "http://purl.oclc.org/NET/UNIS/sao/ec#hasSource")
        self.level = self.parseAttribute(graph, "http://purl.oclc.org/NET/UNIS/sao/sao#hasLevel")
        self.lat = self.parseAttribute(graph, "http://www.w3.org/2003/01/geo/wgs84_pos#lat")
        self.lon = self.parseAttribute(graph, "http://www.w3.org/2003/01/geo/wgs84_pos#lon")
        self.type = str(self.parseAttribute(graph, "http://purl.oclc.org/NET/UNIS/sao/sao#hasType"))
        if "#" in self.type:
            self.type = self.type.split("#")[1]
        self.time = self.parseAttribute(graph, "http://purl.org/NET/c4dm/timeline.owl#time")
        
    def toJson(self):
        rJOb = JSONObject()
        rJOb.identifier = self.identifier
        rJOb.eventclass = self.eventclass
        rJOb.source = self.source
        rJOb.level = self.level
        rJOb.lat = self.lat
        rJOb.lon = self.lon
        rJOb.type = self.type
        rJOb.time = self.time
        return rJOb
    
    def parseRoot(self, graph):
        for stmt in graph.subject_objects(URIRef("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")):
            if stmt[0].startswith("http://"):
#                 print stmt[1]
                return (stmt[0], stmt[1])
        
    def parseAttribute(self, graph, url):
        for stmt in graph.subject_objects(URIRef(url)):
            return stmt[1]
            
 
