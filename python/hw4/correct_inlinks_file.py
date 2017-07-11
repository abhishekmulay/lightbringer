import properties
from elasticsearch import Elasticsearch

es = Elasticsearch([{'host': 'localhost', 'port': 9200}])
index = properties.team_index
type = properties.team_type


def get_my_inlinks_dict():
    my_inlinks_dict = dict()
    inlinks_filepath = properties.abhishek_inlinks_file
    with open(inlinks_filepath) as inlinks_file:
        for line in inlinks_file:
            records = line.strip(' \n').split(' ')
            link = records[0]
            my_inlinks_dict[link] = set(records[1:])

    print "created inlinks dict with size = [" + str(len(my_inlinks_dict)) + "]"
    return my_inlinks_dict


def get_main_inlinks_dict():
    inlinks_dict = dict()
    filepath = properties.es_outlinks_file_path
    line_count = 0
    with open(filepath) as inlinks_file:
        for line in inlinks_file:
            line_count += 1
            records = line.strip(' \n').split(" ")
            link = records[0]
            inlinks_dict[link] = set(records[1:])
            if line_count % 1000 == 0:
                print "Read [" + str(line_count) + "] lines..."
    print "Created main inlinks dict " + str(len(inlinks_dict))
    return inlinks_dict


def correct_inlinks():
    my_inlinks_dict = get_my_inlinks_dict()
    main_inlinks_dict = get_main_inlinks_dict()

    final_inlinks_file = properties.final_inlinks_file
    with open(final_inlinks_file, 'w') as final_file:
        for link, inlinks in main_inlinks_dict.iteritems():
            if link in my_inlinks_dict:
                inlinks = my_inlinks_dict.get(link, [])
            line = link.encode('utf-8', 'ignore') + ' ' + ' '.join(inlinks).encode('utf-8', 'ignore') + '\n'
            final_file.write(line)

    print "Created final inlinks file."


# Reads file of in-links or out-links and returns a dict
# File has this format:
# <link> <in-link1> <in-link2> <in-link3> <in-link4>\n
#  or
# <link> <out-link1> <out-link2> <out-link3> <out-link4>\n
#
# Returns: dict[link] = set([links])
#
def create_links_map(link_file_path):
    links_dict = dict()
    with open(link_file_path) as links_file:
        for line in links_file:
            records = line.strip(' \n').split(" ")
            page = records[0]
            if page in links_dict:
                previous = links_dict[page]
                previous.update(records[1:])
                links_dict[page] = previous
            else:
                links_dict[page] = set(records[1:])
    print "Dict created from = [" + link_file_path + "] size = [" + str(len(links_dict)) + "]"
    return links_dict

def outlink_to_inlink(outlinks_map):
    inlinks_dict = dict()
    for link, outlinks in outlinks_map.iteritems():
        for outlink in outlinks:
            if outlink in inlinks_dict:
                previous = inlinks_dict.get(outlink, {})
                previous.update([link])
                inlinks_dict[outlink] = previous
            else:
                inlinks_dict[outlink] = set([link])

    print "Created inlink dict of size = [" + str(len(inlinks_dict)) + "]"
    return inlinks_dict


def update_inlinks(correct_inlinks_dict):
    count = 0
    for link, inlinks in correct_inlinks_dict.iteritems():
        if (link is None) or (inlinks is None or len(inlinks) == 0):
            continue

        update_query = {
            "script": {
                "inline": "ctx._source.in_links = params.in_links",
                "lang": "painless",
                "params": {
                    "in_links": list(correct_inlinks_dict.get(link))
                }
            }
        }
        try:

            es.update(index=index, doc_type=type, id=link, body=update_query)
            count += 1
            print '[' + str(count) + ']\t' + link
        except Exception, e:
            pass


if __name__ == '__main__':
    outlinks_dict = create_links_map(properties.es_outlinks_file_path)
    correct_inlinks_dict = outlink_to_inlink(outlinks_dict)
    print "Updating elastic search for [" + str(len(correct_inlinks_dict)) + '] docs'
    # update_inlinks(correct_inlinks_dict)
    print "Done!!!"
