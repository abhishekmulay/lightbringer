from collections import defaultdict

import operator

import properties
from elasticsearch import Elasticsearch
import collections

from DocStatistics import DocStatistics
from qrel_reader import QrelReader

es = Elasticsearch([{'host': 'localhost', 'port': 9200}])


class ModelScoreCalculator(object):
    def __init__(self):
        self.index = properties.individual_index
        self.type = properties.individual_type

    def get_okapi_score_dict(self):
        filepath = properties.okapi_all_file
        return self.read_query_doc_score_dict(filepath)

    def get_tfidf_score_dict(self):
        filepath = properties.tfidf_all_file
        return self.read_query_doc_score_dict(filepath)

    def get_bm25_score_dict(self):
        filepath = properties.bm25_all_file
        return self.read_query_doc_score_dict(filepath)

    def get_laplace_score_dict(self):
        filepath = properties.laplace_all_file
        return self.read_query_doc_score_dict(filepath)

    def get_jelinek_score_dict(self):
        filepath = properties.jelinek_all_file
        return self.read_query_doc_score_dict(filepath)

    def read_query_doc_score_dict(self, filepath):
        qid_docid_score_dict = dict()
        with open(filepath) as the_file:
            for line in the_file:
                line = line.strip('\n')
                if line is None or len(line) < 10:  # invalid line
                    continue

                # 85 Q0 AP890220-0143 1 3.234694184248636 Exp
                records = line.split(' ')
                qid = int(records[0])
                docid = records[2]
                score = float(records[4])

                if qid not in qid_docid_score_dict:
                    qid_docid_score_dict[qid] = {docid: score}
                else:
                    prev_dict = qid_docid_score_dict[qid]
                    prev_dict[docid] = score
                    qid_docid_score_dict[qid] = prev_dict

        return qid_docid_score_dict

    # create final dict with all docs from qrel
    def get_final_qid_docid_score_dict_from_qrel(self):
        okapi_dict = self.get_okapi_score_dict()
        tfidf_dict = self.get_tfidf_score_dict()
        bm25_dict = self.get_bm25_score_dict()
        laplace_dict = self.get_laplace_score_dict()
        jelinek_dict = self.get_jelinek_score_dict()

        qrel_reader = QrelReader()
        qrel_dict = qrel_reader.get_qrel_dict()

        final_qid_docid_score_dict = dict()

        for qid, docid_relevance_dict in qrel_dict.iteritems():
            for docid, relevance in docid_relevance_dict.iteritems():
                okapi = okapi_dict[qid].get(docid, -1)
                tfidf = tfidf_dict[qid].get(docid, -1)
                bm25 = bm25_dict[qid].get(docid, -1)
                laplace = laplace_dict[qid].get(docid, float("-inf"))
                jelinek = jelinek_dict[qid].get(docid, float("-inf"))
                label = relevance

                if okapi > -1 and tfidf > -1 and bm25 > -1 and laplace > float("-inf") and jelinek > float("-inf"):
                    stats = DocStatistics(qid, docid, okapi, tfidf, bm25, laplace, jelinek, label)
                    if qid not in final_qid_docid_score_dict:
                        final_qid_docid_score_dict[qid] = {docid: stats}
                    else:
                        prev_dict = final_qid_docid_score_dict[qid]
                        prev_dict[docid] = stats
                        final_qid_docid_score_dict[qid] = prev_dict

        print 'Created final_qid_docid_score_dict of size = ', len(final_qid_docid_score_dict)
        # for qid, b in final_qid_docid_score_dict.iteritems():
        #     print qid, len(b)
        return final_qid_docid_score_dict

    # gets dict from get_final_qid_docid_score_dict_from_qrel and adds additional irrelevant docs so that each
    # query has 1000 total docs
    def get_final_qid_docid_score_dict_with_all_docs(self):
        okapi_dict = self.get_okapi_score_dict()
        tfidf_dict = self.get_tfidf_score_dict()
        bm25_dict = self.get_bm25_score_dict()
        laplace_dict = self.get_laplace_score_dict()
        jelinek_dict = self.get_jelinek_score_dict()

        qrel_reader = QrelReader()
        qrel_dict = qrel_reader.get_qrel_dict()
        label = 0
        threshod = 1000
        # it has now all docs from qrel
        final_qid_docid_score_dict = self.get_final_qid_docid_score_dict_from_qrel()

        for qid, docid_score_dict in laplace_dict.iteritems():
            qrel_docs_for_qid = qrel_dict[qid]
            doc_count = len(qrel_docs_for_qid)

            # print qid, 'current', doc_count, 'total docs =', len(docid_score_dict)
            for docid, score in docid_score_dict.iteritems():
                prev_dict = final_qid_docid_score_dict[qid]
                # need 1000 docs for each qid
                if len(prev_dict) >= threshod:
                    break
                # only take docs that are not in qrel
                if docid in qrel_docs_for_qid:
                    continue

                okapi = okapi_dict[qid].get(docid, 0)
                tfidf = tfidf_dict[qid].get(docid, 0)
                bm25 = bm25_dict[qid].get(docid, 0)
                laplace = laplace_dict[qid].get(docid, float("-inf"))
                jelinek = jelinek_dict[qid].get(docid, float("-inf"))

                # if okapi > -1 and tfidf > -1 and bm25 > -1 and laplace > float("-inf") and jelinek > float("-inf"):
                stats = DocStatistics(qid, docid, okapi, tfidf, bm25, laplace, jelinek, label)
                prev_dict[docid] = stats
                final_qid_docid_score_dict[qid] = prev_dict
                doc_count += 1

        print 'Created final_qid_docid_score_dict of size = ', len(final_qid_docid_score_dict)
        return final_qid_docid_score_dict

    def write_lib_linear_file(self):
        d = self.get_final_qid_docid_score_dict_with_all_docs()
        liblinear_test_path = properties.liblinear_output_test
        feature_matrix_test_path = properties.feature_matrix_test_path

        liblinear_train_path = properties.liblinear_output_train
        feature_matrix_train_path = properties.feature_matrix_train_path

        test_qids = [56, 57, 64, 71, 99]

        with open(liblinear_train_path, 'w') as f_liblinear_train:
            with open (liblinear_test_path, 'w') as f_liblinear_test:
                with open(feature_matrix_test_path, 'w') as f_feature_test:
                    with open(feature_matrix_train_path, 'w') as f_feature_train:
                        for qid, doc_score_dict in d.iteritems():
                            for docid, stats_obj in doc_score_dict.iteritems():
                                liblinear_line = str(stats_obj.label) + ' 1:' + str(stats_obj.okapi) + ' 2:' + str(stats_obj.tfidf) + ' 3:' + \
                                       str(stats_obj.bm25) + ' 4:' + str(stats_obj.laplace) + ' 5:' + str(stats_obj.jelinek) + '\n'

                                matrix_line = str(qid) + ' ' + str(docid) + ' ' + str(stats_obj.okapi) + ' ' + str(stats_obj.tfidf) + ' ' + \
                                       str(stats_obj.bm25) + ' ' + str(stats_obj.laplace) + ' ' + str(stats_obj.jelinek) + '\n'
                                if qid in test_qids:
                                    f_liblinear_test.write(liblinear_line)
                                    f_feature_test.write(matrix_line)
                                else:
                                    f_liblinear_train.write(liblinear_line)
                                    f_feature_train.write(matrix_line)

        print 'Created test and train LibLinear files'

    def create_trec_file(self, liblinear_output_file, feature_matrix_file, output_file_name):

        liblinear_lines, feature_lines = [], []
        with open(liblinear_output_file, 'r') as liblinear_file:
            liblinear_lines = liblinear_file.readlines()

        with open(feature_matrix_file, 'r') as feature_file:
            feature_lines = feature_file.readlines()

        qid_docid_score_dict = defaultdict(lambda : defaultdict(int))
        for lib_line, feature_line in zip(liblinear_lines, feature_lines):
            feature_data = feature_line.split()
            qid = int(feature_data[0])
            docid = str(feature_data[1])
            score = float(lib_line.split()[2])
            qid_docid_score_dict[qid][docid] = score

        with open(output_file_name, 'w') as output_file:
            # 85 Q0 AP890220-0143 1 3.234694184248636 Exp
            for qid, docid_score_dict in qid_docid_score_dict.iteritems():
                rank = 0

                for docid, score in sorted(docid_score_dict.items(), key=operator.itemgetter(1), reverse=True):
                    line = str(qid) + ' Q0 ' + docid + ' ' + str(rank) + ' ' + str(score) + ' Exp' + '\n'
                    rank += 1
                    output_file.write(line)

        print "Created file", output_file_name


if __name__ == '__main__':
    calculator = ModelScoreCalculator()
    # calculator.write_lib_linear_file()
    calculator.create_trec_file(properties.test_output, properties.feature_matrix_test_path, properties.trec_test_file)
    calculator.create_trec_file(properties.train_output, properties.feature_matrix_train_path, properties.trec_train_file)
