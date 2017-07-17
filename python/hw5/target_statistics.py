class TargetStatistics(object):
    def __init__(self, query_id, doc_id, rank, score):
        self.query_id = query_id
        self.doc_id = doc_id
        self.rank = rank
        self.score = score

    def __str__(self):
        return super(TargetStatistics, self).__str__()

