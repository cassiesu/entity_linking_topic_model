import urllib2
import json
import xml.etree.ElementTree as ET
import sys
from httplib import HTTP
from urlparse import urlparse
import time

def repl_non_words(words):
	for word in words:
		word = word.lower()
		word.replace('[ .,;:?&*^%$#@!()\"\'\t\n\r\f]+', ' ')
	return words

def levenshtein(word1, word2):
    columns = len(word1) + 1
    rows = len(word2) + 1

    currentRow = [0]
    for column in xrange( 1, columns ):
        currentRow.append( currentRow[column - 1] + 1 )

    for row in xrange( 1, rows ):
        previousRow = currentRow
        currentRow = [ previousRow[0] + 1 ]

        for column in xrange( 1, columns ):

            insertCost = currentRow[column - 1] + 1
            deleteCost = previousRow[column] + 1

            if word1[column - 1] != word2[row - 1]:
                replaceCost = previousRow[ column - 1 ] + 1
            else:                
                replaceCost = previousRow[ column - 1 ]

            currentRow.append( min( insertCost, deleteCost, replaceCost ) )

    return currentRow[-1]

def Tagging(affix):
	api_tag = "http://tagme.di.unipi.it/tag?key=tagme-NLP-ETH-2015&lang=en&text="
	jsonstr = urllib2.urlopen(api_tag+affix).read()
	jsondata = json.loads(jsonstr)
	annotations = jsondata['annotations']

	predict = {}
	for anno in annotations:
		# spot = str(anno['spot'])		# unicode to string
		spot = anno['spot'].encode("utf-8")
		# entity = str(anno['title'])
		entity = anno['title'].encode("utf-8").lower()
		predict[spot] = entity
	return predict


def Tagging2(affix):
	api_tag = "http://tagme.di.unipi.it/tag?key=tagme-NLP-ETH-2015&lang=en&text="
	jsonstr = urllib2.urlopen(api_tag+affix).read()
	jsondata = json.loads(jsonstr)
	annotations = jsondata['annotations']

	predict = {}
	for anno in annotations:	
		# API returns unicode: deal with UnicodeEncodeError (nothing can be done with repr() str())
		entity = anno['title'].replace(' ', '_').encode("utf-8")
		if (predict.get(entity)):
			predict[entity] +=1
		else:
			predict[entity] = 1
	return predict

def resultfromsearch():
	# tablename1 = '../resources/query-results-more/search_google.csv'
	tablename2 = '../resources/query-results-more/search_bing.csv'
	# tablename2 = '../resources/query-results-test/search_bing.csv'
	
	querytable = dict()
	# f1 = open(tablename1, 'r')
	f2 = open(tablename2, 'r')
	# for line in f1:
	# 	line = line.strip()
	# 	allelements = line.split('\t')
	# 	querytext = allelements[1].strip()
	# 	querytable[querytext] = allelements[2:]
	for line in f2:
		line = line.strip()
		allelements = line.split('\t')
		querytext = allelements[1]
		if(querytable.get(querytext)):
			querytable[querytext] = querytable.get(querytext) + allelements[2:]
		else:
			querytable[querytext] = allelements[2:]

	f2.close()

	return querytable

def utf8(b):  
	try:
		s = b.decode('utf-8')
		return s
	except:
		return str(b)

def main():
	xmlname = '../resources/query-data-train-set.xml'
	querytable = resultfromsearch()

	# print levenshtein("mis", "mist");
	# print levenshtein("this", "that");
	# print levenshtein("apple", "pineapple");
	
	tree = ET.parse(xmlname)
	root = tree.getroot()
	queries = tree.findall('./session/query')

	fob = open('query_entity_train.txt', 'ab')

	for query in queries:
		text = query.find('text').text.strip().replace('\\', '')
		# print text
		qtokens = repl_non_words(text.split(' '))
		wikipages = []
		
		datafromse = querytable['wikipedia '+text]
		length = len(datafromse)
		for i in xrange(1,length):
			bold = datafromse[i]
			if(bold.startswith('Wikipedia-en:')):
				wikipages.append(bold[13:])
		# print wikipages

		boldtotal = ""
		
		datafromse = querytable[text]
		length = len(datafromse)
		# if(datafromse[0] != ""):
		# 	boldtotal = datafromse[0] + ' '
		# else:
		boldtotal = text + ' '

		threshold = 0.7
		for i in xrange(0,length):
			bold = datafromse[i]
			btokens = repl_non_words(bold.split(' '))
			totaldist = 0

			if(bold.startswith('Wikipedia-en:')):
				wikipages.append(bold[13:])
		
			else:
				for btoken in btokens:
					if len(btoken)==0:
						continue 
					dmin = 1
					for qtoken in qtokens:
						dist = levenshtein(btoken, qtoken)/max(len(btoken), len(qtoken))
						if(dist<dmin):
							dmin = dist
					totaldist = totaldist + dmin/len(btokens)
				if(totaldist < threshold):
					boldtotal += bold + ' '
		
		boldtotal = boldtotal.strip().replace(' ', '+')
		print boldtotal
		predict = Tagging2(boldtotal)
		# print len(predict)
		# print predict
		# print len(wikipages)
		# print wikipages

		joinset = set(predict.keys()).union(set(wikipages))
		line = text + '\t' + '\t'.join(joinset)
		print line

		# try:
		# 	line = line.decode('gbk')
		# except:
		# 	line = utf8(line)
		# finally:
		# 	line = str(line)

		fob.write(line + '\n')

	fob.close()


if __name__ == '__main__':
	main()

