import sys
from collections import defaultdict, OrderedDict
import matplotlib.pyplot as plotter
import operator

import math


class Evaluator(object):
    def __init__(self, qrel_file_path, trec_file_path, verbose_enabled):
        self.num_rel = defaultdict(lambda: 0)
        self.qrel_file_path = qrel_file_path
        self.trec_file_path = trec_file_path
        self.verbose_enabled = verbose_enabled
        self.qrel_dict = self.read_qrel_file(self.qrel_file_path)
        self.trec_dict = self.read_trec_file(self.trec_file_path)

        self.recalls = [0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0]
        self.cutoffs = [5, 10, 15, 20, 30, 100, 200, 500, 1000]

    def read_qrel_file(self, filepath):
        qrel_dict = dict()
        with open(filepath) as file:
            for line in file:
                records = line.split(' ')
                query_id = records[0]
                doc_id = records[2]
                relevance = float(records[3])

                relevance = 1 if relevance > 0 else 0
                self.num_rel[query_id] = relevance

                if query_id not in qrel_dict:
                    qrel_dict[query_id] = {doc_id: float(relevance)}
                else:
                    prev = qrel_dict[query_id]
                    prev[doc_id] = float(relevance)
                    qrel_dict[query_id] = prev

        print "Created qrel dict of size = ", len(qrel_dict)
        return OrderedDict(sorted(qrel_dict.items()))

    def read_trec_file(self, filepath):
        trec_dict = dict()
        with open(filepath) as file:
            for line in file:
                records = line.strip('\n').split(' ')
                if not records or len(records) != 6:
                    continue
                query_id = records[0]
                doc_id = records[2]
                rank = records[3]
                score = records[4]

                if query_id not in trec_dict:
                    trec_dict[query_id] = {doc_id: float(score)}
                else:
                    prev = trec_dict[query_id]
                    prev[doc_id] = float(score)
                    trec_dict[query_id] = prev

        print "Created target dict of size = ", len(trec_dict)
        return OrderedDict(sorted(trec_dict.items()))

    def draw_graph(self, precision_at_recalls, query_id):
        plotter.title("Precision Recall Graph")
        plotter.xlabel("Recall")
        plotter.ylabel("Precision")
        plotter.plot(self.recalls, precision_at_recalls)
        plotter.savefig("./graph_%s.png" % query_id)
        plotter.close()

    def calculate_DCG(self, list_rel, num_ret):
        val = float(list_rel[0])
        for index in range(1, num_ret):
            val += float(list_rel[index]) / math.log(1.0 + index)
        return val

    def evaluate(self):
        print "[evaluate] Evaluating. [qrel_dict] = " + str(len(self.qrel_dict)) + ', [self.trec_dict] = ' + str(
            len(self.trec_dict)) + ', [self.num_rel] = ' + str(len(self.num_rel)) + ', verbose = ['+ str(self.verbose_enabled) + ']'



if __name__ == '__main__':
    if len(sys.argv) == 3 or len(sys.argv) == 4:
        verbose_enabled = '-q' in sys.argv and sys.argv[1] == '-q'
        # if verbose_enabled:
        qrel_file_path = sys.argv[2] if verbose_enabled else sys.argv[1]
        trec_file_path = sys.argv[3] if verbose_enabled else sys.argv[2]
        evaluator = Evaluator(qrel_file_path, trec_file_path, verbose_enabled)
        evaluator.evaluate()
    else:
        print "Invalid parameters. Usage:\n$ python trec_eval.py <qrel_file_path> <trec_file_path>.\nUse [-q] for verbose output."



# d = {123: 'a', 1: 'b', 20: 'c', -1: 'd', 0: 'e'}
# from collections import OrderedDict
# OrderedDict(sorted(d.items()))
# OrderedDict([(-1, 'd'), (0, 'e'), (1, 'b'), (20, 'c'), (123, 'a')])
