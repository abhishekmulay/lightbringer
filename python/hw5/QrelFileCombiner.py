import os
from collections import defaultdict
from os import walk
import codecs

import math

import properties


class QrelFilesCombiner(object):
    def __init__(self):
        print "Combining QERL files in dir: ", properties.qrel_files_folder_path, '\n\n'
        self.qrel_folder_path = properties.qrel_files_folder_path

    def read_files(self):
        files = []
        # Map<query_id, Map<doc_id, score>>
        map_dict = defaultdict(lambda: defaultdict(lambda: 0))
        for (dirpath, dirnames, filenames) in walk(self.qrel_folder_path):
            files.extend(filenames)
            for f in files:
                path = self.qrel_folder_path + '/' + f
                with open(path, 'r') as qrel_file:
                    for line in qrel_file:
                        try:
                            record = line.strip('\n').split(' ')
                            query_id = int(record[0])
                            evaluator = record[1]
                            docno = record[2]
                            score = int(record[3])
                            if query_id in map_dict:
                                doc_score_map = map_dict[query_id]
                                if docno in doc_score_map:
                                    previous_score = doc_score_map.get(docno, 0)
                                    doc_score_map[docno] = previous_score + score
                                else:
                                    doc_score_map[docno] = score
                                map_dict[query_id] = doc_score_map
                            else:
                                map_dict[query_id] = {docno : score}

                        except Exception, e:

                            print "Error: ", e.message
        return map_dict

    def combine_files(self, map_dict):
        combined_file = self.qrel_folder_path + '/' + 'qrel_combined.txt'
        max_doc_count = 150
        with open(combined_file, 'w') as combined_qrel:
            for query_id, doc_score_dict in map_dict.iteritems():
                doc_count = 0
                for doc_id, score in doc_score_dict.iteritems():
                    if doc_count >= max_doc_count:
                        break
                    # 88 0 AP890419-0241 0
                    score = score if score < 3 else 2
                    line = str(query_id) + ' 0 ' + doc_id + ' ' + str(score) + '\n'
                    combined_qrel.write(line)
                    doc_count += 1
        print "Combined QREL file created at : ", combined_file



if __name__ == '__main__':
    combiner = QrelFilesCombiner()
    a = combiner.read_files()
    combiner.combine_files(a)
