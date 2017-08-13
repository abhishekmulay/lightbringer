import properties
from elasticsearch import Elasticsearch


es = Elasticsearch([{'host': 'localhost', 'port': 9200}])


class ElasticSearchServer(object):
    def __init__(self, index, doc_type):
        self.index = index
        self.type = doc_type
        print "Connected ElasticSearch index=[" + self.index + '], type=[' + self.type + ']'

    def search(self, search_term, from_size=0):
        query = {
            "query": {
                "multi_match": {
                    "query": search_term,
                    "fields": ["title", "text", "url"]
                }
            },
            "size": 20,
            "from": from_size
        }

        return es.search(index=self.index, doc_type=self.type, body=query, request_timeout=70)

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
                    "evaluator": evaluator,
                    "evaluation_done": True
                }
            }
        }
        try:
            return es.update(index=self.index, doc_type=self.type, id=id, body=update_query)
        except Exception, e:
            print "Exception while evaluating: ", e

    def get_evaluation_details(self, evaluator):
        print "Finding evaluated documents for: [" + str(evaluator) + ']'
        query = {
            "query": {
                "term": {
                    "evaluator": {
                        "value": evaluator.lower()
                    }
                }
            },
            'size': 100
        }
        page = es.search(index=self.index, doc_type=self.type, body=query, scroll='2m', request_timeout=70)
        scroll_id = page['_scroll_id']
        has_more_hits = len(page['hits']['hits']) > 0
        all_hits = page['hits']['hits']
        while has_more_hits:
            print "scrolling..."
            next_page = es.scroll(scroll_id=scroll_id, scroll='2m', request_timeout=70)
            scroll_id = next_page['_scroll_id']
            has_more_hits = len(next_page['hits']['hits']) > 0
            if not has_more_hits:
                break
            hits = next_page['hits']['hits']
            all_hits += hits
        return all_hits

    def dump_evaluated_files(self):
        query_id = 152703
        print "\n\n"
        query = {
            "query": {
                "term": {
                    "qid": {
                        "value": query_id
                    }
                }
            },
            "size": 150,
            "_source": "score",
            "stored_fields": ["url"]
        }
        results = es.search(index=self.index, doc_type=self.type, body=query, request_timeout=70)
        hits = results['hits']['hits']
        # query_id = 152701 # Fukushima nuclear accident
        # query_id = 152702 # Chernobyl accident
        # query_id = 152703 # Three Mile Island accident
        # query_id = 152704 # Kyshtym disaster
        accessor = 'Abhishek'

        # query_1_file_path = '152701_Fukushima_nuclear_accident.txt'
        # with open(query_1_file_path) as query_1_file:
        #     lines = [line.rstrip('\n') for line in query_1_file]

        for hit in hits:
            id = hit['_id']
            grade = hit['_source']['score']
            line = str(query_id) + ' ' + accessor + ' ' + id + ' ' + grade
            print line

    def get_top_docs(self, query_id, search_term, file_to_write, size=10):
        # 64 Q0 AP890901-0048 1 2.0160589373642317 Exp
        query = {
            "query": {
                "multi_match": {
                    "query": search_term,
                    "fields": ["title", "text", "url"]
                }
            },
            "_source": "_id",
            "size": size
        }

        results = es.search(index=self.index, doc_type=self.type, body=query, request_timeout=70)
        hits = results['hits']['hits']
        rank = 1
        for hit in hits:
            docno = hit['_id']
            score = hit['_score']
            line = str(query_id) + ' Q0 ' + str(docno) + ' ' + str(rank) + ' ' + str(score) + ' Exp\n'
            # print line
            rank += 1
            file_to_write.write(line)

if __name__ == '__main__':
    server = ElasticSearchServer(properties.team_index, properties.team_type)
    d = {152701 : 'Fukushima nuclear accident', 152702 : 'Chernobyl accident', 152703 : 'Three Mile Island accident', 152704 :'Kyshtym disaster'}
    # query_id = 152701 # Fukushima nuclear accident
    # query_id = 152702 # Chernobyl accident
    # query_id = 152703 # Three Mile Island accident
    # query_id = 152704 # Kyshtym disaster
    combined_trec_file = 'final_trec.txt'
    with open(combined_trec_file, 'a+') as trec_file:
        for query_id, search_term in d.iteritems():
            server.get_top_docs(query_id, search_term, trec_file, 1000)
