import properties
from elasticsearch import Elasticsearch
es = Elasticsearch([{'host': 'localhost', 'port': 9200}])


class ElasticSearchServer(object):
    def __init__(self, index, doc_type):
        self.index = index
        self.type = doc_type
        print "Connected ElasticSearch index=[" + self.index +'], type=[' + self.type +']'

    def search(self, search_term, scroll_id):
        query = {
            "query": {
                "match": {
                    "text": search_term
                }
            },
            'size': 20
        }

        if not scroll_id:
            return es.search(index=self.index, doc_type=self.type, body=query, scroll='2m', request_timeout=70)
        else:
            return es.scroll(scroll_id=scroll_id, scroll='2m', request_timeout=70)

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

    def evaluate(self, id, score, evaluator):
        print 'evaluate id = [' + str(id) + '], score = [' + str(score) + '], evaluator = [' + str(evaluator) + ']'
        update_query = {
            "script": {
                "inline": "ctx._source.score = params.score; ctx._source.evaluator = params.evaluator; ctx._source.evaluation_done = params.evaluation_done;",
                "lang": "painless",
                "params": {
                    "score": score,
                    "evaluator" : evaluator,
                    "evaluation_done" : True
                }
            }
        }
        try:
            return es.update(index=self.index, doc_type=self.type, id=id, body=update_query)
        except Exception, e:
            print "Exception while evaluating: ", e



