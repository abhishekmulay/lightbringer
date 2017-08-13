from collections import defaultdict

import operator

import properties
from extract_word_tf import WordTFExtractor

from elasticsearch import Elasticsearch

from elasticsearch.client import IndicesClient

index = properties.spam_index
type = properties.spam_index_type
es = Elasticsearch([{'host': 'localhost', 'port': 9200}])
indices_es = IndicesClient(es)


def get_all_unigrams():
    query = {
        "query": {
            "match_all": {}
        },
        "size": 1000,
        "script_fields": {
            "words": {
                "script": {
                    "inline": "doc['body'].values"
                }
            }
        }
    }
    page = es.search(index=index, doc_type=type, body=query, scroll='2m', request_timeout=70)
    scroll_id = page['_scroll_id']
    has_more_hits = len(page['hits']['hits']) > 0
    all_hits = page['hits']['hits']
    while has_more_hits:
        print 'scrolling...'
        next_page = es.scroll(scroll_id=scroll_id, scroll='2m', request_timeout=70)
        scroll_id = next_page['_scroll_id']
        has_more_hits = len(next_page['hits']['hits']) > 0
        if not has_more_hits:
            break
        hits = next_page['hits']['hits']
        all_hits += hits

    print 'Retrieved ', len(all_hits), 'hits'
    unigrams = [set(d.get('fields', {}).get('words', [])) for d in all_hits]
    all_unigrams = reduce(lambda x, y: add_set(x, y), unigrams)

    with open(properties.vocabulary_file_path, 'w') as vocab_file:
        for word in all_unigrams:
            if len(word) < 15:
                vocab_file.write(word.encode('utf-8', 'ignore'))
                vocab_file.write('\n')

    print 'Created vocabulary file at', properties.vocabulary_file_path


def get_term_vector(filename):
    res = es.termvectors(index=index, doc_type=type, id=filename)
    return res['term_vectors']


def create_docid_word_tf_dict(split):
    _, filename_index_dict = get_all_docids_and_index_dict()
    filename_word_tf_dict = defaultdict(lambda: defaultdict(int))

    filename_label_dict = get_filename_label_dict()
    line_no = 1

    term_index_dict, term_index = dict(), 1
    try:
        for filename, file_index in sorted(filename_index_dict.iteritems(), key=operator.itemgetter(1)):
            vectors = get_term_vector(filename)
            terms_dict = vectors.get('body', {}).get('terms', {})

            for term, d in terms_dict.iteritems():
                # filename_word_tf_dict => <file_name : <feature_index : tf>>
                tf = d['term_freq']
                # need term and index for writing features to file.
                if term not in term_index_dict:
                    term_index_dict[term] = term_index
                    term_index += 1

                if filename in filename_word_tf_dict:
                    prev_dict = filename_word_tf_dict.get(filename, {})
                    prev_dict[term] = tf
                    filename_word_tf_dict[filename] = prev_dict
                else:
                    filename_word_tf_dict[filename] = {term: tf}

            if line_no % 1000 == 0:
                print 'Done ', line_no
            line_no += 1
    except Exception, e:
        print e

    print "All unigrams size ", len(term_index_dict)
    print "filename_word_tf_dict ", len(filename_word_tf_dict)

    test_spam_docs, train_spam_docs = index_spam_documents(filename_label_dict)
    test_ham_docs, train_ham_docs = index_ham_documents(filename_label_dict)

    test_output_file = properties.trec07p_feature_all_unigram_test_file
    train_output_file = properties.trec07p_feature_all_unigram_train_file

    print 'Writing to file'
    try:
        with open(test_output_file, 'w') as test_output:
            with open(train_output_file, 'w') as train_output:
                for filename, word_tf_dict in filename_word_tf_dict.iteritems():
                    label = str(1 if filename_label_dict.get(filename) == 'spam' else 0)
                    line = label
                    feature_tf_list = []
                    # create list of (feature_index, tf) this will be unsorted
                    for word, tf in word_tf_dict.iteritems():
                        feature_index = term_index_dict.get(word, 1)
                        feature_tf_list.append((feature_index, tf))

                    # need features in sorted order
                    for feature_index, tf in sorted(feature_tf_list, key=operator.itemgetter(0)):
                        line += ' ' + str(feature_index) + ':' + str(tf)
                    line += '\n'

                    if filename in test_spam_docs or filename in test_ham_docs:
                        test_output.write(line)
                    else:
                        train_output.write(line)
            print 'Done writing: '
    except Exception, e:
        print e, e.message


def get_all_unigrams_from_file():
    unigrams = []
    with open(properties.vocabulary_file_path, 'r') as vocab_file:
        for w in vocab_file:
            unigrams.append(w.rstrip('\n'))
    return unigrams


def get_all_docids_and_index_dict():
    filenames = defaultdict(lambda: defaultdict(int))
    filename_index_dict = dict()
    line_no = 1
    with open(properties.trec07_index_file) as index_file:
        for line in index_file:
            data = line.split()
            # label = data[0]
            filepath = data[1]
            filename = filepath[filepath.rfind('/') + 1:]
            filenames[filename] = dict()
            filename_index_dict[filename] = line_no
            line_no += 1
    return (filenames, filename_index_dict)


def add_set(a, b):
    a = a if a is not None else set([])
    b = b if b is not None else set([])
    a.update(b)
    return a


def get_filename_label_dict():
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

def index_ham_documents(filename_label_dict):
    hams = [k for k, v in filename_label_dict.items() if v == 'ham']
    test_docs_count = int(len(hams) * 0.2)
    test_docs = hams[:test_docs_count]
    train_docs = hams[test_docs_count:]
    print 'ham: ', len(hams), '\ttest_docs:', len(test_docs), '\ttrain_docs:', len(train_docs)
    return (test_docs, train_docs)


def index_spam_documents(filename_label_dict):
    spams = [k for k, v in filename_label_dict.items() if v == 'spam']
    test_docs_count = int(len(spams) * 0.2)
    test_docs = spams[:test_docs_count]
    train_docs = spams[test_docs_count:]
    print 'spam:', len(spams), '\ttest_docs:', len(test_docs), '\ttrain_docs:', len(train_docs)
    return (test_docs, train_docs)


if __name__ == '__main__':
    create_docid_word_tf_dict('test')
    create_docid_word_tf_dict('train')
