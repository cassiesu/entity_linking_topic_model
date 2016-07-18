#!/usr/bin/python
import sys
import xml.etree.ElementTree as ET


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

		# print TP,FP,FN
	return TP, FP, FN


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
		# print TP,FP,FN
	return TP, FP, FN


def search(start, end, depth):
	res = dict()
	if depth >=4:
		return res;
	length = end - start;
	if length <= 0:
		return res
	for k in range(length, 0, -1):
		maxprob = 1E-3
		maxstart = 0
		maxend = end
		for i in range(start, length - k + 1):
			totalword = words[i]
			for j in range(1,k):
				totalword = totalword + ' ' +words[i+j]
			if totalword in linktable.keys():
				curword = totalword
				maxstart = i
				maxend = i+k
				curentity = highestprob[totalword]
				maxprob = probtable[curword][curentity]
			else:
				curset = set()
				if words[i] in linktable.keys():
					curset = linktable[words[i]]
					flag = True
				else:
					continue
				for j in range(1, k):
					if words[i + j] in linktable.keys():
						curset = curset.intersection(linktable[words[i + j]])
					else:
						flag = False
						break
					if len(curset) == 0:
						flag = False
						break
				if flag:
					if k == 1:
						curentity = highestprob[words[i]]
						curword = words[i]
						maxprob = probtable[words[i]][curentity]
						maxstart = i
						maxend = i+1
					else:
						for x in curset:
							curprob = probtable[words[i]][x]
							for j in range(1, k):
								if x in probtable[words[i + j]].keys():
									curprob = curprob * probtable[words[i + j]][x]
							if curprob > maxprob:
								maxprob = curprob
								maxstart = i
								maxend = i + k
								curentity = x
								curword = words[i]
								for j in range(1, k):
									curword = curword + ' ' + words[i + j]
		if maxprob > 1E-3:
			res[curword] = curentity;
			res.update(search(start, maxstart,depth+1))
			res.update(search(maxend, end,depth+1))
			return res
	return res



def main(argv):
	# xmlname = 'test2.xml'
	xmlname = '../resources/query-data-train-set.xml'
	tablename = '../resources/crosswikis-dict-preprocessed'

	TPStrict = 0.0
	FPStrict = 0.0
	FNStrict = 0.0

	TPRelax = 0.0
	FPRelax = 0.0
	FNRelax = 0.0

	global linktable
	global probtable
	global words
	global highestprob

	linktable = dict()
	probtable = dict()
	words = list()
	highestprob = dict()
	f = open(tablename, 'r')
	for line in f:
		line = line.strip()
		tag = line.split('\t')[0]
		prob, entity = line.split('\t')[1].split(' ')
		if tag not in linktable:
			linktable[tag] = set()
		if tag not in probtable:
			probtable[tag] = dict()
		if tag not in highestprob:
			highestprob[tag] = entity
		if prob > 1E-3:
			linktable[tag].add(entity)
			probtable[tag][entity] = float(prob)
	tree = ET.parse(xmlname)
	root = tree.getroot()
	queries = tree.findall('./session/query')
	for query in queries:
		text = query.find('text').text
		words = text.split(' ')
		print text
		length = len(words)

		predict = search(0, length,1);

		print 'predict:', predict

		gtdict = {}
		annotations = query.findall('./annotation/target/..')
		for annotation in annotations:
			gtdict[annotation.find('span').text] = annotation.find('target').text[29:]

		print 'gtdict:', gtdict

		TP, FP, FN = F1strict(gtdict, predict)
		TPStrict = TPStrict + TP
		FPStrict = FPStrict + FP
		FNStrict = FNStrict + FN

		TP, FP, FN = F1relax(gtdict, predict)
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


if __name__ == "__main__":
	main(sys.argv)
