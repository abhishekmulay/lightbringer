import properties
import os
from elasticsearch import Elasticsearch
from elasticsearch import helpers
from bs4 import BeautifulSoup

es = Elasticsearch([{'host': 'localhost', 'port': 9200}])


class HW7Model(object):
    def __init__(self, body, label, split, file_name):
        self.body = body
        self.label = label
        self.split = split
        self.file_name = file_name

    def __repr__(self):
        return '{ file_name: ' + self.file_name + ' label: ' + str(self.label) + ' split: ' + str(
            self.split) + ' body: ' + str(self.body) + ' }'


def read_data_files(filename_label_dict):
    files = []
    for (dirpath, dirnames, filenames) in os.walk(properties.trec07p_folder_path):
        for f in filenames:
            files.append(os.path.abspath(os.path.join(dirpath, f)))

    test_spam_docs, train_spam_docs = index_spam_documents(filename_label_dict)
    test_ham_docs, train_ham_docs = index_ham_documents(filename_label_dict)

    models, split, count = [], '', 0

    for f in files:
        with open(f) as data_file:
            content = data_file.read().replace('\n', '')
            soup = BeautifulSoup(content, 'lxml')
            body = soup.get_text()
            filename = f[f.rfind('/') + 1:]
            label = filename_label_dict.get(filename)

            if label == 'ham':
                split = 'test' if filename in test_ham_docs else 'train'
            else:
                split = 'test' if filename in test_spam_docs else 'train'

            models.append({
                "body": body,
                "label": label,
                "split": split,
                "file_name": filename
            })
            count += 1
            if count % 1000 == 0:
                print 'created', count, 'models'

    # all models list generated before this point
    print "Created models list of size = ", len(models)
    k = ({
        "_index": properties.spam_index,
        "_type": properties.spam_index_type,
        "_id" : model['file_name'],
        "_source": model
    } for model in models)
    res = helpers.bulk(es, k)
    print res

    print 'Created models list', models, len(models)


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
    filename_label_dict = get_filename_label_dict()
    read_data_files(filename_label_dict)
