class DocStatistics(object):
    def __init__(self, qid, docid, okapi_score, tfidf_score, bm25_score, laplace_score, jelinek_score, label):
        self.query_id = qid
        self.docid = docid
        self.okapi = okapi_score
        self.tfidf = tfidf_score
        self.bm25 = bm25_score
        self.laplace = laplace_score
        self.jelinek = jelinek_score
        self.label = label

    def __repr__(self):
        return 'DocStatistics:{ qid = {0}, docid = {1}, okapi_score = {2}, tfidf_score = {3}, bm-25_score = {4}, laplace_score = {5}, jelinek_score = {6}'.format(self.query_id, self.docid, self.okapi, self.tfidf, self.bm25, self.laplace, self.jelinek)

    def __eq__(self, o):
        return self.query_id == o.query_id and self.docid == o.docid



