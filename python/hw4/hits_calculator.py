import math
import operator
import random
import urllib

from elasticsearch import Elasticsearch

import properties
from hw4.LinksProvider import LinksProvider

es = Elasticsearch([{'host': 'localhost', 'port': 9200}])
index = properties.team_index
type = properties.team_type

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

#########################################
# Global variables

root_set = []  # list of dics
base_set = set()  # set of urls
auth_scores = dict()
hub_scores = dict()

d = 200
k = 200
base_set_size = 10000
########################################

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

def hits_algo(G):
    global auth_scores
    global hub_scores

    for p in G:
        auth_scores[p] = 1
        hub_scores[p] = 1

    print "Initialized auth_scores dict, size = [" + str(len(auth_scores)) + ']'
    print "Initialized hub_scores dict, size = [" + str(len(hub_scores)) + ']'

    print "Starting HITS. k = [" + str(k) +']'
    HubsAndAuthorities(G)


def HubsAndAuthorities(G):
    global k

    linkProvider = LinksProvider()
    outlinks_dict = linkProvider.get_outlinks_dict()
    inlinks_dict = linkProvider.get_inlinks_dict()

    for step in range(0, k):
        try:
            print "Step = [" + str(step) + ']'
            norm = 0
            for p in G:
                auth_scores[p] = 0
                for q in inlinks_dict.get(p, []):
                    auth_scores[p] += hub_scores.get(q, 0)
                norm += math.pow(auth_scores.get(p, 1), 2)
            norm = math.sqrt(norm)

            for p in G:
                auth_scores[p] = auth_scores.get(p, 1) / norm
            norm = 0

            for p in G:
                hub_scores[p] = 0
                for r in outlinks_dict.get(p, []):
                    hub_scores[p] += auth_scores.get(r, 0)
                norm += math.pow(hub_scores.get(p, 1), 2)
            norm = math.sqrt(norm)

            for p in G:
                hub_scores[p] = hub_scores.get(p, 1) / norm

            print 'outlinks = [' + str(len(outlinks_dict.get(p,[]))) + '], inlinks = [' + str(len(inlinks_dict.get(p,[]))) + ']\t' + \
                  ', hub score = [' + str(hub_scores.get(p, 0)) + '], auth score = [' + str(auth_scores.get(p, 0))  + ']' + p
        except Exception, e:
            print "Exception: ", e

    print "HITS finished.\nWriting hub_scores to [" + str( properties.hits_hub_output_file) + ']'
    sort_and_write_dict(hub_scores, properties.hits_hub_output_file, lines_to_write=500)
    print "Writing auth_scores to [" + str(properties.hits_auth_output_file) + ']'
    sort_and_write_dict(auth_scores, properties.hits_auth_output_file, lines_to_write=500)

def sort_and_write_dict(map, filepath, lines_to_write):
    # ex. sorted_list = [('p3', 3212), ('p1', 123), ('p2', 111)]
    sorted_list = sorted(map.items(), key=operator.itemgetter(1), reverse=True)
    rank = 0
    with open(filepath, 'w') as op_file:
        for key, val in sorted_list:
            if lines_to_write <= rank:
                break
            rank += 1
            line = str(key) + '\t' + str(val) + '\n'
            op_file.write(line.encode('utf-8', 'ignore'))
    print "Wrote sorted dict to file = [" + filepath + ']'

if __name__ == '__main__':
    create_root_set()
    expand_root_set()
    hits_algo(base_set)
