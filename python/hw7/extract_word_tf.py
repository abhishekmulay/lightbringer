from collections import defaultdict
import re
import properties
from elasticsearch import Elasticsearch
from elasticsearch.client import IndicesClient

index = properties.spam_index
type = properties.spam_index_type

es = Elasticsearch([{'host': 'localhost', 'port': 9200}])
indices_es = IndicesClient(es)


class WordTFExtractor(object):
    def get_word_tf(self, word, split):
        query = {
            "query": {
                "bool": {
                    "should": [
                        {"match": {"body": word}},
                        {"term": {"split": split}}
                    ]
                }
            },
            "size": 10000,
            "stored_fields": ["file_name"],
            "script_fields": {
                "tf": {
                    "script": {
                        "lang": "groovy",
                        "inline": "double tf = _index['body'][word].tf(); return tf;",
                        "params": {
                            "word": word
                        }
                    }
                }
            }
        }

        page = es.search(index=index, doc_type=type, body=query, scroll='2m', request_timeout=70)
        scroll_id = page['_scroll_id']
        has_more_hits = len(page['hits']['hits']) > 0
        all_hits = page['hits']['hits']
        while has_more_hits:
            next_page = es.scroll(scroll_id=scroll_id, scroll='2m', request_timeout=70)
            scroll_id = next_page['_scroll_id']
            has_more_hits = len(next_page['hits']['hits']) > 0
            if not has_more_hits:
                break
            hits = next_page['hits']['hits']
            all_hits += hits

        print word, len(all_hits)
        return all_hits

    def get_all_spam_words(self):
        words = []
        with open(properties.spam_words_file) as spam_file:
            for line in spam_file:
                word = line.rstrip('\n')
                words.append(word)
        return words

    def analyse(self, word):
        query = {
            "analyzer": "customAnalyzer",
            "text": str(word)
        }
        try:
            res = indices_es.analyze(index=index, body=query)
            return None if len(res['tokens']) < 1 else res['tokens'][0]['token']
        except Exception, e:
            print e

    def write_liblinear_feature_file(self, split):
        filename_word_tf_dict = defaultdict(lambda: defaultdict(int))

        spam_words = self.get_all_spam_words()
        for word in spam_words:
            analysed_word = self.analyse(word)
            if analysed_word is not None and len(analysed_word) > 1:
                result = self.get_word_tf(analysed_word, split)
                for d in result:
                    filename = d['fields']['file_name'][0]
                    tf = d['fields']['tf'][0]
                    filename_word_tf_dict[filename][word] = tf

        # make tf = 0 for all spam words that are not found in a file
        for filename, word_tf_dict in filename_word_tf_dict.iteritems():
            for w in spam_words:
                if w not in word_tf_dict:
                    word_tf_dict[w] = 0

        for i, w in enumerate(spam_words):
            print i, ' => ', w

        # write to file
        filename_label_dict = self.get_filename_label_dict()
        if split == 'test':
            output_file = properties.trec07_features_test_path
        else:
            output_file = properties.trec07_features_train_path

        with open(output_file, 'w') as features_file:
            for filename, word_tf_dict in filename_word_tf_dict.iteritems():
                label = filename_label_dict[filename]
                line = str(1 if label == 'spam' else 0)
                for i, w in enumerate(spam_words):
                    tf = int(word_tf_dict.get(w, 0))
                    if tf > 0:
                        line += ' ' + str(i + 1) + ':' + str(tf)
                line += '\n'
                features_file.write(line)
        print 'Created feature file', split

    def get_filename_label_dict(self):
        filename_label_dict = dict()
        with open(properties.trec07_index_file) as index_file:
            for line in index_file:
                data = line.split()
                label = data[0]
                filepath = data[1]
                filename = filepath[filepath.rfind('/') + 1:]
                filename_label_dict[filename] = label
        print 'Read index file, doc count =', len(filename_label_dict)
        return filename_label_dict


if __name__ == '__main__':
    extractor = WordTFExtractor()
    extractor.write_liblinear_feature_file(split='test')
    extractor.write_liblinear_feature_file(split='train')
