import properties

class QueryReader(object):
    def __init__(self):
        self.query_file_path =  properties.stemmed_query_file

    def get_all_queries(self):
        queries = dict()
        with open(self.query_file_path) as qfile:
            for line in qfile:
                line = line.strip('\n')
                first_index_of_dot = line.index('.')
                query_id = int(line[0:first_index_of_dot])
                query_text = line[first_index_of_dot + 4:]
                # print 'id', query_id, 'text', query_text
                queries[query_id] = query_text
        return queries

if __name__ == '__main__':
    reader = QueryReader()
    reader.get_all_queries()
