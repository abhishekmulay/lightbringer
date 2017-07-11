import properties
import random
import math
from elasticsearch import Elasticsearch
import urllib

es = Elasticsearch([{'host': 'localhost', 'port': 9200}])
index = properties.team_index
type = properties.team_type

root_set = []  # list of dics
base_set = set()  # set of urls

d = 200
base_set_size = 10000


def create_root_set():
    global root_set
    global base_set
    size = 1000
    doc_count = 0
    print "Getting top [" + str(size) + "] docs from ES."

    query = {
        "query": {
            "match": {
                "text": "fukushima nuclear disaster"
            }
        },
        'size': 100,
        "_source": "_id",
        "stored_fields": ["out_links", "in_links"]
    }

    first_page = es.search(index=index, doc_type=type, body=query, scroll='2m', request_timeout=70)
    scroll_id = first_page['_scroll_id']
    has_more_hits = len(first_page['hits']['hits']) > 0
    first_records = first_page['hits']['hits']
    root_set += first_records
    doc_count += len(first_records)
    base_set.update([doc['_id'] for doc in first_records])

    while has_more_hits:
        print "[" + str(doc_count) + "], scrolling..."
        page = es.scroll(scroll_id=scroll_id, scroll='2m', request_timeout=70)
        scroll_id = page['_scroll_id']
        has_more_hits = len(page['hits']['hits']) > 0
        if not has_more_hits or doc_count >= size:
            break
        records = page['hits']['hits']
        root_set += records
        base_set.update([doc['_id'] for doc in records])
        doc_count += len(records)

    print "Retrieved top [" + str(len(base_set)) + "] from ES."


def expand_root_set():
    global root_set
    global base_set
    try:
        for doc in root_set:
            has_outlinks = doc is not None and 'fields' in doc and 'out_links' in doc['fields']
            has_inlinks = doc is not None and 'fields' in doc and 'in_links' in doc['fields']

            if len(base_set) >= base_set_size:
                break
            if not has_outlinks or not has_inlinks:
                continue

            outlinks = doc['fields']['out_links']
            url_encoded_outlinks = [urllib.quote_plus(x.encode('utf-8', 'ignore')) for x in outlinks]
            base_set.update(url_encoded_outlinks)

            inlinks = doc['fields']['in_links']
            url_encoded_inlinks = [urllib.quote_plus(x.encode('utf-8', 'ignore')) for x in inlinks]
            if len(url_encoded_inlinks) <= d:
                base_set.update(url_encoded_inlinks)
            else:
                random_url_select_count = int(math.floor(len(url_encoded_inlinks) // 10))
                base_set.update(random.sample(url_encoded_inlinks, random_url_select_count))

                # print '[' + str(len(doc['fields']['out_links'])) + '], url=' + doc['_id']
    except Exception, e:
        print "Error\t", e.message

    print "Total docs in base set " + str(len(base_set))


#  1 G := set of pages
#  2 for each page p in G do
#  3   p.auth = 1 // p.auth is the authority score of the page p
#  4   p.hub = 1 // p.hub is the hub score of the page p
#  5 function HubsAndAuthorities(G)
#  6   for step from 1 to k do // run the algorithm for k steps
#  7     norm = 0
#  8     for each page p in G do  // update all authority values first
#  9       p.auth = 0
# 10       for each page q in p.incomingNeighbors do // p.incomingNeighbors is the set of pages that link to p
# 11          p.auth += q.hub
# 12       norm += square(p.auth) // calculate the sum of the squared auth values to normalise
# 13     norm = sqrt(norm)
# 14     for each page p in G do  // update the auth scores
# 15       p.auth = p.auth / norm  // normalise the auth values
# 16     norm = 0
# 17     for each page p in G do  // then update all hub values
# 18       p.hub = 0
# 19       for each page r in p.outgoingNeighbors do // p.outgoingNeighbors is the set of pages that p links to
# 20         p.hub += r.auth
# 21       norm += square(p.hub) // calculate the sum of the squared hub values to normalise
# 22     norm = sqrt(norm)
# 23     for each page p in G do  // then update all hub values
# 24       p.hub = p.hub / norm   // normalise the hub values

if __name__ == '__main__':
    create_root_set()
    expand_root_set()
