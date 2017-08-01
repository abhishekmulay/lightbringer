from collections import defaultdict

import properties

class QrelReader(object):
    def __init__(self):
        self.qrel_file_path = properties.apdataset_qrel_file_path
        self.qrel_dict = defaultdict(lambda: defaultdict(int))

    def get_qrel_dict(self):
        print "Reading QREL file from: ", self.qrel_file_path
        with open(self.qrel_file_path) as qrel_file:
            for line in qrel_file:
                records = line.rstrip('\n').split(' ')
                # 51 0 AP890104-0259 0
                query_id, doc_id, relevance = int(records[0]), records[2], int(records[3])
                # print 'query_id', query_id, 'doc_id', doc_id, 'relevance', relevance
                if query_id not in self.qrel_dict:
                    self.qrel_dict[query_id] = {doc_id : relevance}
                else:
                    prev_dict = self.qrel_dict[query_id]
                    prev_dict[doc_id] = relevance
                    self.qrel_dict[query_id] = prev_dict

        # tot_docs = 0
        # for qid, doc_dict in self.qrel_dict.iteritems():
        #     print 'qid =', qid, 'docs =', len(doc_dict), 'one =', sum(doc_dict.values()), ' zero =', len(doc_dict.values()) - sum(doc_dict.values())

            # tot_docs += len(doc_dict)
        # print 'total = ', tot_docs
        return self.qrel_dict

if __name__ == '__main__':
    reader = QrelReader()
    reader.get_qrel_dict()




