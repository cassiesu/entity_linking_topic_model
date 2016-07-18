import urllib2
import json
import xml.etree.ElementTree as ET
import sys
from httplib import HTTP
from urlparse import urlparse
import time

import sys
reload(sys)
sys.setdefaultencoding("utf-8")

def F1relax(gtdict, predict):
	TP = 0.0
	FP = 0.0
	FN = 0.0

	for prekey in predict.keys():
		prevalue = predict.get(prekey)
		if prevalue in gtdict.values():
			TP = TP + 1.0
		else:
			FP = FP + 1.0
	for gtkey in gtdict.keys():
		gtvalue = gtdict.get(gtkey)
		if gtvalue not in predict.values():
			FN = FN + 1.0

	if (TP > 0):
		Precision = TP / (TP + FP)
		Recall = TP / (TP + FN)
		F1 = 2 * Precision * Recall / (Precision + Recall)
	else:
		F1 = 0
	return F1

def F1strict(gtdict, predict):
	TP = 0.0
	FP = 0.0
	FN = 0.0

	for prekey in predict.keys():
		prevalue = predict.get(prekey)
		if prekey in gtdict.keys() and prevalue == gtdict.get(prekey):
			TP = TP + 1.0
		else:
			FP = FP + 1.0
	for gtkey in gtdict.keys():
		gtvalue = gtdict.get(gtkey)
		if gtkey not in predict.keys() or predict.get(gtkey) != gtvalue:
			FN = FN + 1.0
	if (TP > 0):
		Precision = TP / (TP + FP)
		Recall = TP / (TP + FN)
		F1 = 2 * Precision * Recall / (Precision + Recall)
	else:
		F1 = 0
	return F1

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
		entity = anno['title'].encode("utf-8")
		predict[entity] = spot
	return predict



def Tagging3(affix):
	api_tag = "http://tagme.di.unipi.it/tag?key=tagme-NLP-ETH-2015&lang=en&text="
	jsonstr = urllib2.urlopen(api_tag+affix).read()
	jsondata = json.loads(jsonstr)
	annotations = jsondata['annotations']

	predict = {}
	for anno in annotations:
		# spot = str(anno['spot'])		# unicode to string
		spot = anno['spot'].encode("utf-8")
		# entity = str(anno['title'])
		entity = anno['title'].encode("utf-8")
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
	# tablename2 = '../resources/query-results-more/search_bing.csv'
	tablename2 = '../resources/query-results-test/search_bing.csv'
	
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


def resultfromtopicmodel():
	filename = 'entity_relevence_test_20.txt'
	f1 = open(filename,'r')
	tpresult = dict()
	for line in f1:
		line = line.strip().replace('_',' ')
		allelements = line.split('\t')
		tpresult[allelements[0]] = allelements[1:]
	f1.close()	
	return tpresult	

def utf8(b):  
	try:
		s = b.decode('utf-8')
		return s
	except:
		return str(b)

def main():
	xmlname = '../resources/query-data-test-set-unlabelled.xml'
	#querytable = resultfromsearch()
	topicmodelresult = resultfromtopicmodel()	


	tree = ET.parse(xmlname)
	root = tree.getroot()
	queries = tree.findall('./session/query')
	f1relaxsum = 0
	tagmerelaxsum = 0
	tagmestrictsum = 0
	f1strictsum = 0
	f1num = 0
	thresold = 0.1
	for query in queries:
		text = query.find('text').text.strip().replace('\\', '')
		affix = text.replace(' ','+')
		tagme = Tagging(affix)
		tagmetest = Tagging3(affix)
		print text
		words = text.split(' ')

		for key in tagmetest.keys():
			print 'tagme result: ' + key + ': ' +tagmetest[key]
		if (topicmodelresult.get(text)):
			tpresult = topicmodelresult[text]
			print tpresult
			length = len(tpresult)
			predict = {}
			used = []
			flag2 = 1
			for res in tpresult:
				curentity = res.split(':')[0]
				if (curentity.lower() == text.lower()):
					flag2 = 0
					predict[text] = curentity
			if (flag2 == 1):
				for res in tpresult:
					curentity = res.split(':')[0]
					curprob = res.split(':')[1]
					if (tagme.get(curentity.lower())):
						spot = tagme[curentity.lower()]			
						flag = 1					
						for w in spot.split(' '):
							if (w in used):
								flag = 0
						if (flag == 1):
							predict[spot] = curentity
							for w in spot.split(' '):
								used.append(w)
							print '1 ' + spot + ': ' + curentity
					else:
						spot = ''
						best = ''
						curlen = 0
						bestlen = 0
						for word in words:
							dmin = 1
							for  entityword in curentity.split(' ') :
				 				dist = levenshtein(word, entityword)/max(len(word), len(entityword))
				 				if(dist<dmin):
				 					dmin = dist
							if (dmin < thresold):
								if (spot == ''):
									spot = word
									curlen = 1
								else:
									spot = spot + ' ' + word
									curlen = curlen + 1
							else:
								if (curlen > bestlen):
									best = spot
									bestlen = curlen								
								spot = ''
								curlen = 0
						if (curlen > bestlen):
							best = spot
							bestlen = curlen								
						if (bestlen > 0):
							flag = 1					
							for w in best.split(' '):
								if (w in used):
									flag = 0
							if (flag == 1):
								predict[best] = curentity
								for w in best.split(' '):
									used.append(w)
								print '2 ' + best +': '+ curentity		
			f1num = f1num + 1
		else:
			print text+" not found in file"
			f1num = f1num+1	
	t = 0
	for query in queries:
		text = query.find('text').text.strip().replace('\\', '')
		affix = text.replace(' ','+')
		if (t % 3 == 0):
		
			tagme = Tagging(affix)
			tagmetest = Tagging3(affix)
			print text
			words = text.split(' ')

			for key in tagmetest.keys():
				print 'tagme result: ' + key + ': ' +tagmetest[key]
			if (topicmodelresult.get(text)):
				tpresult = topicmodelresult[text]
				print tpresult
				length = len(tpresult)
				predict = {}
				used = []
				flag2 = 1
				for res in tpresult:
					curentity = res.split(':')[0]
					if (curentity.lower() == text.lower()):
						flag2 = 0
						predict[text] = curentity
				if (flag2 == 1):
					for res in tpresult:
						curentity = res.split(':')[0]
						curprob = res.split(':')[1]
						if (tagme.get(curentity.lower())):
							spot = tagme[curentity.lower()]			
							flag = 1					
							for w in spot.split(' '):
								if (w in used):
									flag = 0
							if (flag == 1):
								predict[spot] = curentity
								for w in spot.split(' '):
									used.append(w)
								print '1 ' + spot + ': ' + curentity
						else:
							spot = ''
							best = ''
							curlen = 0
							bestlen = 0
							for word in words:
								dmin = 1
								for  entityword in curentity.split(' ') :
					 				dist = levenshtein(word, entityword)/max(len(word), len(entityword))
					 				if(dist<dmin):
					 					dmin = dist
								if (dmin < thresold):
									if (spot == ''):
										spot = word
										curlen = 1
									else:
										spot = spot + ' ' + word
										curlen = curlen + 1
								else:
									if (curlen > bestlen):
										best = spot
										bestlen = curlen								
									spot = ''
									curlen = 0
							if (curlen > bestlen):
								best = spot
								bestlen = curlen								
							if (bestlen > 0):
								flag = 1					
								for w in best.split(' '):
									if (w in used):
										flag = 0
								if (flag == 1):
									predict[best] = curentity
									for w in best.split(' '):
										used.append(w)
									print '2 ' + best +': '+ curentity		
				f1num = f1num + 1
			else:
				print text+" not found in file"
				f1num = f1num+1	
		else:
			predict = Tagging3(affix)
		t = t+1
		for key in predict.keys():
			anno = ET.SubElement(query,'annotation')
			sp = ET.SubElement(anno,'span')
			sp.text = '<![CDATA['+key+']]>'
			tar = ET.SubElement(anno,'target')
			tar.text = '<![CDATA['+'http://en.wikipedia.org/wiki/'+predict[key].replace(' ','_')+']]>'

	tree.write('result.xml')
if __name__ == '__main__':
	main()

