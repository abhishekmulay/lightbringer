import sys

from hw5.target_statistics import TargetStatistics


class Evaluator(object):
    def __init__(self, argv):
        print argv
        self.qrel_file_path = argv[1]
        self.target_file_path = argv[2]
        self.qrel_dict = self.read_qrel_file(self.qrel_file_path)
        self.target_data_dict = self.read_target_data_file(self.target_file_path)

    def read_qrel_file(self, filepath):
        qrel_dict = dict()
        with open(filepath) as file:
            for line in file:
                records = line.split(' ')
                query_id = records[0]
                doc_id = records[2]
                relevance = records[3]

                if query_id not in qrel_dict:
                    qrel_dict[query_id] = {doc_id: float(relevance)}
                else:
                    prev = qrel_dict[query_id]
                    prev[doc_id] = float(relevance)
                    qrel_dict[query_id] = prev

        print "Created qrel dict of size = ", len(qrel_dict)
        return qrel_dict

    def read_target_data_file(self, filepath):
        target_dict = dict()
        with open(filepath) as file:
            for line in file:
                records = line.strip('\n').split(' ')
                if not records or len(records) != 6:
                    continue
                query_id = records[0]
                doc_id = records[2]
                rank = records[3]
                score = records[4]

                if query_id not in target_dict:
                    target_dict[query_id] = {doc_id : float(score)}
                else:
                    prev = target_dict[query_id]
                    prev[doc_id] = float(score)
                    target_dict[query_id] = prev

        print "Created target dict of size = ", len(target_dict)
        return target_dict

    def __str__(self):
        return str(self.qrel_dict) + '\n' + str(self.target_data_dict)


if __name__ == '__main__':
    if len(sys.argv) == 2 or len(sys.argv) == 3:
        evaluator = Evaluator(sys.argv)
    else:
        print "Invalid parameters. Usage:\n$ python trec_eval.py <qrel_file_path> <target_file_path>"
