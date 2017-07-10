import properties
import urllib
from elasticsearch import Elasticsearch

es = Elasticsearch([{'host': 'localhost', 'port': 9200}])
index = properties.team_index
type = properties.team_type


class ESDocumentExtractor(object):
    def __init__(self):
        pass

    def get_docs_from_es(self):
        link_count = 0
        filepath = properties.es_outlinks_file_path
        with open(filepath, 'w') as file:
            query = {
                'size': 100,
                "_source": "_id",
                "stored_fields": ["out_links"]
            }

            first_page = es.search(index=index, doc_type=type, body=query, scroll='2m', request_timeout=70)
            scroll_id = first_page['_scroll_id']
            has_more_hits = len(first_page['hits']['hits']) > 0
            first_records = first_page['hits']['hits']

            for doc in first_records:
                link_count += 1
                link = doc['_id']
                has_outlinks = doc.has_key('fields') and doc['fields'].has_key('out_links')
                if not has_outlinks:
                    continue
                out_links = doc['fields']['out_links']
                url_encoded_outlinks = [urllib.quote_plus(x.encode('utf-8', 'ignore')) for x in out_links]
                out_links_string = link.encode('utf-8', 'ignore') + ' ' + ' '.join(url_encoded_outlinks).encode('utf-8','ignore') + '\n'
                file.write(out_links_string)


            while has_more_hits:
                print "Done = [" + str(link_count) + "], scrolling..."
                page = es.scroll(scroll_id=scroll_id, scroll='2m', request_timeout=70)
                scroll_id = page['_scroll_id']
                has_more_hits = len(page['hits']['hits']) > 0
                if not has_more_hits:
                    break
                records = page['hits']['hits']
                for doc in records:
                    link_count += 1
                    link = doc['_id']
                    has_outlinks = doc.has_key('fields') and doc['fields'].has_key('out_links')
                    if not has_outlinks:
                        continue
                    out_links = doc['fields']['out_links']
                    url_encoded_outlinks = [urllib.quote_plus(x.encode('utf-8', 'ignore')) for x in out_links]
                    out_links_string = link.encode('utf-8', 'ignore') + ' ' + ' '.join(url_encoded_outlinks).encode('utf-8','ignore') + '\n'
                    file.write(out_links_string)


if __name__ == '__main__':
    extractor = ESDocumentExtractor()
    extractor.get_docs_from_es()
