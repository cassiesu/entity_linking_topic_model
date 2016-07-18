import urllib2
import json
import xml.etree.ElementTree as ET
import sys


# def Spotting(affix):
#   api_spot = "http://tagme.di.unipi.it/spot?key=tagme-NLP-ETH-2015&lang=en&text="
#   jsonstr = urllib2.urlopen(api_spot+affix).read()
#   jsondata = json.loads(jsonstr)
#   spots = jsondata['spots']

#   entitylist = []
#   for spot in spots:
#   entitylist.append(spot['spot'].encode("utf-8"))
#   return entitylist

def Relating(couples):
	api_tag = "http://tagme.di.unipi.it/rel?key=tagme-NLP-ETH-2015&lang=en&"
	for couple in couples:
		api_tag = api_tag + "tt=" + couple + "&"
	api_tag = api_tag[:-1]
	print api_tag
	jsonstr = urllib2.urlopen(api_tag.replace(' ', '%20')).read()
	jsondata = json.loads(jsonstr)
	results = jsondata['result']

	predict = {}
	for result in results:
		couple = result['couple'].encode("utf-8")
		rel = float(result['rel'])
		predict[couple] = rel
	return predict

def Tagging(affix):
	api_tag = "http://tagme.di.unipi.it/tag?key=tagme-NLP-ETH-2015&lang=en&text="
	jsonstr = urllib2.urlopen(api_tag+affix).read()
	jsondata = json.loads(jsonstr)
	annotations = jsondata['annotations']

	predict = {}
	for anno in annotations:
		spot = anno['spot'].encode("utf-8")
		entity = anno['title'].encode("utf-8")
		predict[spot] = entity
	return predict


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

	#print TP,FP,FN
	return TP,FP,FN



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
	#print TP,FP,FN
	return TP,FP,FN



def Parser(xmlfilename):
	tree = ET.parse(xmlfilename)
	root = tree.getroot()
	queries = root.findall("./session/query")

	TPStrict = 0.0
	FPStrict = 0.0
	FNStrict = 0.0

	TPRelax = 0.0
	FPRelax = 0.0
	FNRelax = 0.0

	for q in queries:
		#processing part starts

		affix = q.find('text').text.strip().replace(' ','+')
		print affix

		#Spotting method
		# prelist = Spotting(affix)
		# prelen = len(prelist)

		#Tagging method
		predict = Tagging(affix)
		prelen = len(predict)

		#print "predict:",predict

		gtdict = {}
		annotations = q.findall('./annotation/target/..')
		for annotation in annotations:
			gtdict[annotation.find('span').text] = annotation.find('target').text[29:].replace('_',' ')
	
		#print 'gtdict:',gtdict
		TP,FP,FN  = F1strict(gtdict, predict)
		TPStrict = TPStrict + TP
		FPStrict = FPStrict + FP
		FNStrict = FNStrict + FN
	
		TP,FP,FN  = F1relax(gtdict, predict)
		TPRelax = TPRelax + TP
		FPRelax = FPRelax + FP
		FNRelax = FNRelax + FN


	PrecisionStrict = TPStrict / (TPStrict + FPStrict)
	RecallStrict = TPStrict / (TPStrict + FNStrict)
	F1s = 2 * PrecisionStrict * RecallStrict / (PrecisionStrict + RecallStrict)
	
	PrecisionRelax = TPRelax / (TPRelax + FPRelax)
	RecallRelax = TPRelax / (TPRelax + FNRelax)
	F1r = 2 * PrecisionRelax * RecallRelax / (PrecisionRelax + RecallRelax)
	
	print 'f1strict:', F1s
	print 'f1relax:', F1r


def main():
	# Parser('../resources/query-data-dev-set.xml')
	str1 = 'Linked_data Semantic_Web'
	str2 = 'Academy_Award David_Cameron'
	print Relating([str1, str2])

if __name__ == '__main__':
	main()

