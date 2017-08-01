import sys
from collections import defaultdict, OrderedDict
import matplotlib.pyplot as plotter
import operator

import math
import os


# num_topics = 0.0
# prec_list = [0.0] * 1001
# rec_list = [0.0] * 1001
# num_ret = 0.0
# num_rel_ret = 0.0
# sum_prec = 0.0
#
# recalls = [0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0]
# cutoffs = [5, 10, 15, 20, 30, 100, 200, 500, 1000]
# sum_prec_at_cutoffs = [0.0] * len(cutoffs)
# sum_prec_at_recalls = [0.0] * len(recalls)
# avg_prec_at_recalls = [0.0] * len(recalls)
# sum_f1 = [0.0] * len(cutoffs)
#
# sum_avg_prec, sum_r_prec, sum_rel_ret = 0.0, 0.0, 0.0
# tot_num_ret, tot_num_rel, tot_num_rel_ret = 0.0, 0.0, 0.0
# sum_nDCG = 0.0


def calculate_dcg(rel_list, num_ret):
    val = float(rel_list[0])
    for i in range(1, num_ret):
        val += float(rel_list[i] / math.log(1.0 + i))
    return val


class Evaluator(object):
    def __init__(self, qrel_file_path, trec_file_path, verbose_enabled):
        self.num_rel = defaultdict(lambda :0)
        self.qrel_file_path = qrel_file_path
        self.trec_file_path = trec_file_path
        self.verbose_enabled = verbose_enabled
        self.qrel_dict = self.read_qrel_file(self.qrel_file_path)
        self.trec_dict = self.read_trec_file(self.trec_file_path)

        self.recalls = [0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0]
        self.cutoffs = [5, 10, 15, 20, 30, 100, 200, 500, 1000]

    def read_qrel_file(self, filepath):
        qrel_dict = defaultdict(lambda: defaultdict(lambda: 0))
        with open(filepath) as file:
            for line in file:
                records = line.strip('\n').split(' ')
                topic = int(records[0])
                doc_id = records[2]
                relevance = int(records[3])

                # relevance = 1 if relevance > 0 else 0
                self.num_rel[topic] += 1 if relevance > 0 else 0
                qrel_dict[topic][doc_id] = relevance

        # print "Created qrel dict of size = ", len(qrel_dict)
        return OrderedDict(sorted(qrel_dict.items()))

    def read_trec_file(self, filepath):
        trec_dict = defaultdict(lambda :{})
        with open(filepath) as file:
            for line in file:
                records = line.strip('\n').split(' ')
                if not records or len(records) != 6:
                    continue
                topic = int(records[0])
                doc_id = records[2]
                rank = records[3]
                score = float(records[4])

                trec_dict[topic][doc_id] = score

        # print "Created target dict of size = ", len(trec_dict)
        return OrderedDict(sorted(trec_dict.items()))

    def draw_graph(self, precision_at_recalls, query_id):
        plotter.title("Precision Recall Graph")
        plotter.xlabel("Recall")
        plotter.ylabel("Precision")
        plotter.plot(self.recalls, precision_at_recalls)
        plotter.savefig("./diagrams/diagram_" + str(query_id) + ".png")
        plotter.close()

    def calculate_DCG(self, list_rel, num_ret):
        val = float(list_rel[0])
        for index in range(1, num_ret):
            val += float(list_rel[index]) / math.log(1.0 + index)
        return val

    def eval_print(self, topic, num_ret, tot_num_rel_docs, num_rel_ret, prec_at_recalls, avg_prec, prec_at_cutoffs,
                   r_prec, f1_at_cutoffs, ndcg):
        print "\n\nQueryid (Num):    %5d" % topic
        print "Total number of documents over all queries"
        print "    Retrieved:    %5d" % num_ret
        print "    Relevant:     %5d" % tot_num_rel_docs
        print "    Rel_ret:      %5d" % num_rel_ret
        print "Interpolated Recall - Precision Averages:"
        print "    at 0.00       %.4f" % prec_at_recalls[0]
        print "    at 0.10       %.4f" % prec_at_recalls[1]
        print "    at 0.20       %.4f" % prec_at_recalls[2]
        print "    at 0.30       %.4f" % prec_at_recalls[3]
        print "    at 0.40       %.4f" % prec_at_recalls[4]
        print "    at 0.50       %.4f" % prec_at_recalls[5]
        print "    at 0.60       %.4f" % prec_at_recalls[6]
        print "    at 0.70       %.4f" % prec_at_recalls[7]
        print "    at 0.80       %.4f" % prec_at_recalls[8]
        print "    at 0.90       %.4f" % prec_at_recalls[9]
        print "    at 1.00       %.4f" % prec_at_recalls[10]
        print "Average precision (non-interpolated) for all rel docs(averaged over queries)"
        print "                  %.4f" % avg_prec
        print "Precision:"
        print "  At    5 docs:   %.4f" % prec_at_cutoffs[0]
        print "  At   10 docs:   %.4f" % prec_at_cutoffs[1]
        print "  At   15 docs:   %.4f" % prec_at_cutoffs[2]
        print "  At   20 docs:   %.4f" % prec_at_cutoffs[3]
        print "  At   30 docs:   %.4f" % prec_at_cutoffs[4]
        print "  At  100 docs:   %.4f" % prec_at_cutoffs[5]
        print "  At  200 docs:   %.4f" % prec_at_cutoffs[6]
        print "  At  500 docs:   %.4f" % prec_at_cutoffs[7]
        print "  At 1000 docs:   %.4f" % prec_at_cutoffs[8]
        print "R-Precision (precision after R (= num_rel for a query) docs retrieved):"
        print "    Exact:        %.4f\n" % r_prec
        print "F-1 measure:"
        print "  At    5 docs:   %.4f" % f1_at_cutoffs[0]
        print "  At   10 docs:   %.4f" % f1_at_cutoffs[1]
        print "  At   15 docs:   %.4f" % f1_at_cutoffs[2]
        print "  At   20 docs:   %.4f" % f1_at_cutoffs[3]
        print "  At   30 docs:   %.4f" % f1_at_cutoffs[4]
        print "  At  100 docs:   %.4f" % f1_at_cutoffs[5]
        print "  At  200 docs:   %.4f" % f1_at_cutoffs[6]
        print "  At  500 docs:   %.4f" % f1_at_cutoffs[7]
        print "  At 1000 docs:   %.4f" % f1_at_cutoffs[8]
        print "nDCG:"
        print "    Exact:        %.4f\n\n" % ndcg

    def evaluate(self):
        num_tpoics = len(self.trec_dict)
        tot_num_ret = 0
        tot_num_rel = 0
        tot_num_rel_ret = 0
        sum_avg_prec = 0.0
        sum_r_prec = 0.0
        sum_nDCG = 0.0

        sum_prec_at_cutoff = [0.0] * len(self.cutoffs)
        sum_prec_at_recall = [0.0] * len(self.recalls)
        sum_f1_at_cutoffs = [0.0] * len(self.recalls)

        for topic, doc_dict in self.trec_dict.iteritems():
            if self.num_rel.get(topic, 0) == 0:
                continue

            num_ret = 0
            num_rel_ret = 0
            sum_prec = 0
            prec_list = [None] * 1001
            rec_list = [None] * 1001
            rel_list = [None] * 1000
            tot_no_rel_docs = float(self.num_rel.get(topic))

            sorted_doc = sorted(doc_dict.iteritems(), key=operator.itemgetter(1), reverse=True)
            for doc_id, score in sorted_doc:
                temp_rel = float(self.qrel_dict[topic][doc_id])
                rel_list[num_ret] = temp_rel
                num_ret += 1

                if temp_rel > 0.0:
                    sum_prec += float((1.0 + num_rel_ret)) / num_ret
                    num_rel_ret += 1

                prec_list[num_ret] = float(num_rel_ret) / num_ret
                rec_list[num_ret] = float(num_rel_ret) / tot_no_rel_docs

                if num_ret > 1000:
                    break

            avg_prec = sum_prec / tot_no_rel_docs
            final_recall = float(num_rel_ret) / tot_no_rel_docs

            for i in range(num_ret + 1, 1001):
                prec_list[i] = num_rel_ret / i
                rec_list[i] = final_recall

            prec_at_cutoffs = []
            f1_at_cutoffs = []

            for c in self.cutoffs:
                prec = prec_list[c]
                recall = rec_list[c]
                f1 = 0.0

                prec_at_cutoffs.append(prec)
                if prec > 0.0 and recall > 0.0:
                    f1 = (2.0 * prec * recall) / (prec + recall)
                f1_at_cutoffs.append(f1)

            r_prec = None
            if tot_no_rel_docs > num_ret:
                r_prec = float(num_rel_ret) / tot_no_rel_docs
            else:
                r_prec = prec_list[int(tot_no_rel_docs)]

            max_prec = 0.0

            for i in range(1000, 0, -1):
                if prec_list[i] > max_prec:
                    max_prec = prec_list[i]
                else:
                    prec_list[i] = max_prec

            prec_at_recalls = []
            i = 1

            for r in self.recalls:
                while i <= 1000 and rec_list[i] < r:
                    i = i + 1
                if i <= 1000:
                    prec_at_recalls.append(prec_list[i])
                else:
                    prec_at_recalls.append(0.0)

            # draw_graph()
            tot_num_ret += num_ret
            tot_num_rel += tot_no_rel_docs
            tot_num_rel_ret += num_rel_ret

            for i in range(0, len(self.cutoffs)):
                sum_prec_at_cutoff[i] += prec_at_cutoffs[i]
                sum_f1_at_cutoffs[i] += f1_at_cutoffs[i]

            for i in range(0, len(self.recalls)):
                sum_prec_at_recall[i] += prec_at_recalls[i]

            sum_avg_prec += avg_prec
            sum_r_prec += r_prec

            ndcg1 = calculate_dcg(rel_list, num_ret)
            ndcg2 = calculate_dcg(sorted(rel_list, reverse=True), num_ret)

            if not all(x == 0.0 for x in rel_list) and ndcg2 != 0.0:
                ndcg = float(ndcg1) / ndcg2
            else:
                ndcg = 0.0
            sum_nDCG += ndcg

            if self.verbose_enabled:
                self.eval_print(int(topic), num_ret, tot_no_rel_docs, num_rel_ret, prec_at_recalls, avg_prec,
                                prec_at_cutoffs, r_prec, f1_at_cutoffs, ndcg)
                self.draw_graph(prec_at_recalls, topic)

        avg_prec_at_cutoffs = []
        avg_f1_at_cutoffs = []

        for i in range(0, len(self.cutoffs)):
            avg_prec_at_cutoffs.append(sum_prec_at_cutoff[i] / num_tpoics)
            avg_f1_at_cutoffs.append(sum_f1_at_cutoffs[i] / num_tpoics)

        avg_prec_at_recalls = []
        for i in range(0, len(self.recalls)):
            avg_prec_at_recalls.append(sum_prec_at_recall[i] / num_tpoics)

        mean_avg_prec = float(sum_avg_prec) / num_tpoics
        avg_r_prec = float(sum_r_prec) / num_tpoics
        avg_ndcg = float(sum_nDCG) / num_tpoics

        self.draw_graph(avg_prec_at_recalls, 'final_combined_graph')
        self.eval_print(num_tpoics, tot_num_ret, tot_num_rel, tot_num_rel_ret, avg_prec_at_recalls, mean_avg_prec,
                        avg_prec_at_cutoffs, avg_r_prec, avg_f1_at_cutoffs, avg_ndcg)

    def call_trec_eval(self):
        if self.verbose_enabled:
            os.system('perl trec_eval -q ' + self.qrel_file_path + ' ' + self.trec_file_path)
        else:
            os.system('perl trec_eval ' + self.qrel_file_path + ' ' + self.trec_file_path)


if __name__ == '__main__':
    if len(sys.argv) == 3 or len(sys.argv) == 4:
        print sys.argv
        verbose_enabled = '-q' in sys.argv and sys.argv[1] == '-q'
        qrel_file_path = sys.argv[2] if verbose_enabled else sys.argv[1]
        trec_file_path = sys.argv[3] if verbose_enabled else sys.argv[2]
        evaluator = Evaluator(qrel_file_path, trec_file_path, verbose_enabled)
        evaluator.evaluate()
    else:
        print "Invalid parameters. Usage:\n$ python trec_eval.py <qrel_file_path> <trec_file_path>.\nUse [-q] for verbose output."
