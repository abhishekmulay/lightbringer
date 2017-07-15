import properties
from elasticsearch import Elasticsearch
es = Elasticsearch([{'host': 'localhost', 'port': 9200}])


class ElasticSearchServer(object):
    def __init__(self, index, doc_type):
        self.index = index
        self.type = doc_type
        print "Connected ElasticSearch index=[" + self.index +'], type=[' + self.type +']'

    def search(self, search_term):
        query = {
            "query": {
                "match": {
                    "text": search_term
                }
            },
            'size': 20
        }
        return es.search(index=self.index, doc_type=self.type, body=query, scroll='2m', request_timeout=70)

    def scroll(self, scroll_id):
        all_hits = []
        while has_more_hits:
            page = es.scroll(scroll_id=scroll_id, scroll='2m', request_timeout=70)
            scroll_id = page['_scroll_id']
            has_more_hits = len(page['hits']['hits']) > 0
            if not has_more_hits:
                break
            hits = page['hits']['hits']
            all_hits += hits
        return all_hits



