import properties


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

if __name__ == '__main__':
    correct_inlinks()