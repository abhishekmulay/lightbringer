import properties
import math

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
inlinks_dict = dict()  # 183811
outlinks_dict = dict()  # 117636
sink_nodes_set = set()  # 66175
N = 183811.0
d = 0.85

previous_perplexity = 0
perplexity = []  # should hold 4 values.


def create_inlinks_map():
    filepath = properties.inlinks_file_path
    with open(filepath) as inlinks_file:
        for line in inlinks_file:
            records = line.strip(' \n').split(" ")
            page = records[0]
            if page in inlinks_dict:
                previous = inlinks_dict[page]
                previous.update(records[1:])
                inlinks_dict[page] = previous
            else:
                inlinks_dict[page] = set(records[1:])
    print "inlinks dict created. size = [" + str(len(inlinks_dict)) + "]"


def create_outlinks_map(inlinks_map):
    for page, inlinks in inlinks_map.iteritems():
        for inlink in inlinks:
            if inlink in outlinks_dict:
                previous = outlinks_dict[inlink]
                previous.update([page])
                outlinks_dict[inlink] = previous
            else:
                outlinks_dict[inlink] = set([page])

    print "outlinks dict created. size = [" + str(len(outlinks_dict)) + "]"

    for key in inlinks_dict.keys():
        if key not in outlinks_dict:
            sink_nodes_set.add(key)
    print "sink nodes set created. size = [" + str(len(sink_nodes_set)) + "]"


def page_rank():
    create_inlinks_map()
    create_outlinks_map(inlinks_dict)
    newPR = dict()

    P = inlinks_dict.keys()  # equal to N
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

            inlinks_of_p = inlinks_dict.get(p)
            for q in inlinks_of_p:
                num_outlinks_from_q = len(outlinks_dict.get(q, 1))
                newPR[p] += (d * PR[q]) / num_outlinks_from_q

        for p in P:
            PR[p] = newPR[p]


def is_converged(PR, P):
    global perplexity
    global previous_perplexity
    if len(perplexity) == 4:
        # all 4 values are True
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


if __name__ == '__main__':
    page_rank()
