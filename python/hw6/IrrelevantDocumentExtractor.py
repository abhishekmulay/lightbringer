import properties
from elasticsearch import Elasticsearch
from stemming.porter2 import stem

from qrel_reader import QrelReader
from query_reader import QueryReader

es = Elasticsearch([{'host': 'localhost', 'port': 9200}])


class IrrelevantDocumentExtractor(object):
    def __init__(self):
        self.index = properties.individual_index
        self.type = properties.individual_type

    def extract(self, search_term, required_count=1000):
        stemmed_search_term = stem(search_term)
        body_query = {
            "query": {
                "match": {
                    "text": str(stemmed_search_term)
                }
            },
            "sort": [{"_score": {"order": "asc"}}],
            "_source": "docLength",
            "script_fields": {
                "okapi": {
                    "script": {
                        "lang": "groovy",
                        "inline": "double tf = _index['text'][word].tf(); int dl =doc['docLength'].value; double df = _index['text'][word].df();  double okapiScore = tf / (tf + 0.5 + (1.5 * (dl/avgDocLength))); return okapiScore;",
                        "params": {
                            "word": "corrupt",
                            "avgDocLength": 441
                        }
                    }
                },
                "tfidf": {
                    "script": {
                        "lang": "groovy",
                        "inline": "double tf = _index['text'][word].tf(); int dl =doc['docLength'].value; double df = _index['text'][word].df();  double okapiScore = tf / (tf + 0.5 + (1.5 * (dl/avgDocLength))); double tfidf= okapiScore * Math.log10(corpusSize/df); return tfidf;",
                        "params": {
                            "word": "corrupt",
                            "corpusSize": 84678,
                            "avgDocLength": 441
                        }
                    }
                },
                "bm25": {
                    "script": {
                        "lang": "groovy",
                        "inline": "double tf = _index['text'][word].tf(); int dl = doc['docLength'].value; double df = _index['text'][word].df(); double first = Math.log10((corpusSize + 0.5)/(tf + 0.5)); double second = ((tf+ k1 * tf) / (tf + k1 * ((1-b) + (b* (dl/avgDocLength))))); double last = (tfwq + k2 * tfwq)/ (tfwq+ k2);  double bm = first * second * last; return bm;",
                        "params": {
                            "word": "corrupt",
                            "corpusSize": 84678.0,
                            "avgDocLength": 441.0,
                            "k1": 1.2,
                            "k2": 10.0,
                            "b": 0.75,
                            "tfwq": 1.0
                        }
                    }
                }
            }
        }

        results = []
        total_retrieved = 0
        first_page = es.search(index=self.index, doc_type=self.type, body=body_query, scroll='2m', request_timeout=70)
        first_records = first_page['hits']['hits']
        scroll_id = first_page['_scroll_id']
        has_more_hits = len(first_page['hits']['hits']) > 0
        total_retrieved += len(first_records)
        for doc in first_records:
            results.append(doc)

        while has_more_hits:
            page = es.scroll(scroll_id=scroll_id, scroll='2m', request_timeout=70)
            if not has_more_hits or total_retrieved >= required_count:
                break
            records = page['hits']['hits']
            total_retrieved += len(records)
            for doc in records:
                results.append(doc)
        print results

    def get_docs(self, search_term, size=2000):
        print "[get_docs] Fetching " + str(size) + " docs for term: [" + search_term + "]"
        body_query = {
            # "query": {
            #     "match": {
            #         "text": str(search_term)
            #     }
            # },
            "size": 4000,
            "stored_fields": ["_id"],
            "sort": [{"_score": {"order": "asc"}}]
        }
        result = es.search(index=self.index, doc_type=self.type, body=body_query, scroll='2m', request_timeout=70)
        hits = result['hits']['hits']
        return hits


if __name__ == '__main__':
    extractor = IrrelevantDocumentExtractor()
    queryReader = QueryReader()
    qrelReader = QrelReader()
    query_id_text_dict = queryReader.get_all_queries()

    query_doc_relevance_dict = dict()
    qrel_dict = qrelReader.get_qrel_dict()

    # add all results from qrel into final dict
    for qid, doc_dict in qrel_dict.iteritems():
        query_doc_relevance_dict[qid] = doc_dict

    print 'qrel query ids ', query_id_text_dict.keys()
    print 'my queries ', query_id_text_dict.keys()

    for qid, qtext in query_id_text_dict.iteritems():
        results_dict = extractor.get_docs(qtext)
        previous_doc_relevance_dict = query_doc_relevance_dict.get(qid, {})

        irrelevant_doc_count = 0
        for doc in results_dict:
            doc_id = doc['_id']
            if irrelevant_doc_count >= 1000:
                break

            # only add docs that are not in qrel
            if doc_id not in previous_doc_relevance_dict:
                previous_doc_relevance_dict[doc_id] = 0
                irrelevant_doc_count += 1

        query_doc_relevance_dict[qid] = previous_doc_relevance_dict

    # # for all queries
    # for qid, qtext in query_id_text_dict.iteritems():
    #     previous_doc_relevance_dict = query_doc_relevance_dict[qid]
    #     docs_for_query = extractor.get_docs(qtext)
    #
    #     doc_count= 0
    #     for doc_dict in docs_for_query:
    #         doc_id = doc_dict['_id']
    #         relevance = 0
    #         if doc_count >= 1000:
    #             break
    #
    #          if doc_id not in previous_doc_relevance_dict:
    #             previous_doc_relevance_dict[doc_id] = relevance
    #             doc_count += 1
    #
    #     query_doc_relevance_dict[qid] = previous_doc_relevance_dict

    for a , b in query_doc_relevance_dict.iteritems():
        print 'qid', a ,'docs', len(b) , b