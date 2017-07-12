import properties

class LinksProvider(object):

    def __init__(self):
        self.inlinks_dict = dict()
        self.outlinks_dict = dict()

    def get_inlinks_dict(self):
        if len(self.outlinks_dict) == 0:
            self.outlinks_dict = self.get_outlinks_dict()
        return self.outlinks_to_inlinks_dict(self.outlinks_dict)

    def get_outlinks_dict(self):
        if len(self.outlinks_dict) == 0:
            self._read_outlinks_file(properties.es_outlinks_file_path)
        return self.outlinks_dict

    def inlinks_to_outlinks_dict(self, inlinks_map):
        print "Not implemented yet."

    def outlinks_to_inlinks_dict(self, outlinks_map):
        inlinks_dict = dict()
        for link, outlinks in outlinks_map.iteritems():
            for outlink in outlinks:
                if outlink in inlinks_dict:
                    previous = inlinks_dict.get(outlink, {})
                    previous.update([link])
                    inlinks_dict[outlink] = previous
                else:
                    inlinks_dict[outlink] = set([link])

        print "Created inlink dict of size = [" + str(len(inlinks_dict)) + "]"
        return inlinks_dict

    def _read_outlinks_file(self, link_file_path):
        links_dict = dict()
        with open(link_file_path) as links_file:
            for line in links_file:
                records = line.strip(' \n').split(" ")
                page = records[0]
                if page in links_dict:
                    previous = links_dict[page]
                    previous.update(records[1:])
                    links_dict[page] = previous
                else:
                    links_dict[page] = set(records[1:])
        print "Dict created from = [" + link_file_path + "] size = [" + str(len(links_dict)) + "]"
        self.outlinks_dict = links_dict
