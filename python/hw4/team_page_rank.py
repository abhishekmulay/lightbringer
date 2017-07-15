import properties
import math
import operator

# Source= CS 6200:Information Retrieval, Spring 2017, Homework 2.
#
# // P is the set of all pages; |P| = N
# // S is the set of sink nodes, i.e., pages that have no out links
# // M(p) is the set of pages that link to page p
# // L(q) is the number of out-links from page q
# // d is the PageRank damping/teleportation factor; use d = 0.85 as is typical
#
# foreach page p in P
#   PR(p) = 1/N                          /* initial value */
#
# while PageRank has not converged do
#   sinkPR = 0
#   foreach page p in S                  /* calculate total sink PR */
#     sinkPR += PR(p)
#   foreach page p in P
#     newPR(p) = (1-d)/N                 /* teleportation */
#     newPR(p) += d*sinkPR/N             /* spread remaining sink PR evenly */
#     foreach page q in M(p)             /* pages pointing to p */
#       newPR(p) += d*PR(q)/L(q)         /* add share of PageRank from in-links */
#   foreach page p
#     PR(p) = newPR(p)
#
# return PR
############################################################################

PR = dict()
P = set() # 26,98,619
inlinks_dict = dict()
outlinks_dict = dict()
sink_nodes_set = set()
N = 2698619.0
d = 0.85

# iteration = [48], Perplexity = [0.6322930664755404, 0.5318524055182934, 0.44770473008975387, 0.37700916919857264]


previous_perplexity = 0
perplexity = []  # should hold 4 values.


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

def inlink_to_outlink(inlinks_map):
    for page, inlinks in inlinks_map.iteritems():
        for inlink in inlinks:
            if inlink in outlinks_dict:
                previous = outlinks_dict[inlink]
                previous.update([page])
                outlinks_dict[inlink] = previous
            else:
                outlinks_dict[inlink] = set([page])

    print "outlinks dict created. size = [" + str(len(outlinks_dict)) + "]"

def get_sink_nodes_set(P, outlinks_dict):
    sink_nodes_set = set()
    for key in P:
        if key not in outlinks_dict:
            sink_nodes_set.add(key)
    print "sink nodes set created. size = [" + str(len(sink_nodes_set)) + "]"
    return sink_nodes_set


############################################
#   Page rank algo
############################################
def setup():
    # 1) Create outlinks dict
    # 2) Create inlinks dict
    # 3) Create sink nodes set

    global inlinks_dict
    global outlinks_dict
    global sink_nodes_set
    global P
    outlinks_dict = create_links_map(properties.es_outlinks_file_path)
    inlinks_dict = outlink_to_inlink(outlinks_dict)

    P.update(inlinks_dict.keys())
    P.update(outlinks_dict.keys())

    print "Set of all pages P created. size = [" + str(len(P)) + "]"
    sink_nodes_set = get_sink_nodes_set(P, outlinks_dict)
    print "Running Page Rank on team index."

def page_rank():
    setup()
    newPR = dict()

    for x in P:
        PR[x] = 1.0 / N

    iteration = 0
    while not is_converged(PR, P):
        iteration += 1
        print "iteration = [" + str(iteration) + "], Perplexity = " + str(perplexity)
        sinkPR = 0
        for p in sink_nodes_set:
            sinkPR += PR[p]
        for p in P:
            newPR[p] = (1.0 - d) / N
            newPR[p] += (d * sinkPR) / N

            inlinks_of_p = inlinks_dict.get(p, [])
            for q in inlinks_of_p:
                num_outlinks_from_q = len(outlinks_dict.get(q, 1))
                newPR[p] += (d * PR[q]) / num_outlinks_from_q

        for p in P:
            PR[p] = newPR[p]
            print 'outlinks = [' + str(len(outlinks_dict.get(p, []))) + '], inlinks = [' + str(
                len(inlinks_dict.get(p, []))) + '], score = [' + str(PR.get(p, -1)) + '], doc = [' + p + ']'

    sort_and_write_dict(PR)

def is_converged(PR, P):
    global perplexity
    global previous_perplexity
    if len(perplexity) == 4:
        if all(i < 1 for i in perplexity):
            print "PageRank has converged."
            return True
        else:
            perplexity = []

    total = 0.0
    for p in P:
        total += PR[p] * math.log(PR[p], 2)
    h = -1 * total
    perp = math.pow(2, h)
    perplexity.append(previous_perplexity - perp)
    previous_perplexity = perp
    return False

def sort_and_write_dict(map):
    # ex. sorted_list = [('p3', 3212), ('p1', 123), ('p2', 111)]
    sorted_list = sorted(map.items(), key=operator.itemgetter(1), reverse=True)
    filepath = properties.team_page_rank_output_file_path
    rank = 0
    with open(filepath, 'w') as op_file:
        for key, val in sorted_list:
            rank += 1
            line = str(key) + ' ' + str(rank) + ' ' + str(val) + '\n'
            op_file.write(line.encode('utf-8', 'ignore'))
    print "Wrote sorted dict to file = [" + filepath + ']'


if __name__ == '__main__':
    page_rank()
