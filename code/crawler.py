import urllib2
import json
import xml.etree.ElementTree as ET
import sys
import os
import re


def Crawler(xmlfilename):
	tree = ET.parse(xmlfilename)
	root = tree.getroot()
	queries = root.findall("./session/query")
	bing_tag = "https://www.bing.com/search?q=%s&go=Submit&first=%d"

	for q in queries:
		affix = q.find('text').text.strip().replace(' ','+')
		wikiaffix = "wikipedia+" + q.find('text').text.strip().replace(' ','+')
		html = ""
		wikihtml = ""
		for i in range(0, 1):
			print bing_tag % ( affix, i*10+1)
			print bing_tag % ( wikiaffix, i*10+1)
			htmlstr = urllib2.urlopen(bing_tag % ( affix , i*10+1)).read()
			wikistr = urllib2.urlopen(bing_tag % ( wikiaffix , i*10+1)).read()
			html = html + htmlstr
			wikihtml = wikihtml + wikistr
		fob1 = open('../resources/query-results-test/Bing/' + wikiaffix, 'w')
		fob2 = open('../resources/query-results-test/Bing/' + affix, 'w')
		fob1.write(html)
		fob2.write(wikihtml)
		fob1.close()
		fob2.close()

def boldRequest(folder):
	filelist = os.listdir(folder)
	csvfile = "../resources/query-results-test/search_bing.csv"
	fob = open( csvfile, 'ab')

	tree = ET.parse('../resources/query-data-test-set-unlabelled.xml')
	root = tree.getroot()
	queries = root.findall("./session/query")

	for q in queries:
		affix = q.find('text').text.strip().replace(' ','+')
		wikiaffix = "wikipedia+" + q.find('text').text.strip().replace(' ','+')

		frwiki = open('../resources/query-results-test/Bing/' + wikiaffix, 'r')
		fr = open('../resources/query-results-test/Bing/' + affix, 'r')
		fstr = fr.read()
		fstrwiki = frwiki.read()

		line = affix
		linewiki = wikiaffix
		line = line + '\t' + q.find('text').text.strip()
		linewiki = linewiki + '\t' + 'wikipedia '+ q.find('text').text.strip()

		if 'Including results for ' in fstr:
			match = re.search(r'Including results for <a href="http:\/\/www.bing.com\/search?q=(.*?)&', fstr)
			if match:
				line = line + '\t' + match.group().replace('+', ' ')
			else:
				line = line + '\t'
		else:
			line = line + '\t'

		if 'Including results for ' in fstrwiki:
			match = re.search(r'Including results for <a href="http:\/\/www.bing.com\/search?q=(.*?)&', fstrwiki)
			if match:
				linewiki = linewiki + '\t' + match.group().replace('+', ' ')
			else:
				linewiki = linewiki + '\t'
		else:
			linewiki = linewiki + '\t'

		bolds = re.findall(r'<strong>(.*?)<\/strong>', fstr)
		boldswiki = re.findall(r'<strong>(.*?)<\/strong>', fstrwiki)
		bolddict = {}
		bolddictwiki = {}
		for bold in bolds:
			if(bolddict.get(bold)):
				bolddict[bold] = bolddict.get(bold) + 1
			else:
				bolddict[bold] = 1

		for bold in boldswiki:
			if(bolddictwiki.get(bold)):
				bolddictwiki[bold] = bolddictwiki.get(bold) + 1
			else:
				bolddictwiki[bold] = 1

		for bold in bolddict.keys():
			line = line + '\t' + bold

		for bold in bolddictwiki.keys():
			linewiki = linewiki + '\t' + bold

		wikis = re.findall(r'href=\"http:\/\/en\.wikipedia\.org\/wiki\/(.*?)[#|\"]', fstr)
		wikiswiki = re.findall(r'href=\"http:\/\/en\.wikipedia\.org\/wiki\/(.*?)[#|\"]', fstrwiki)

		for wiki in wikis:
			pagename = 'Wikipedia-en:' + wiki
			print pagename
  			line = line + '\t' + pagename
  		
  		for wiki in wikiswiki:
			pagename = 'Wikipedia-en:' + wiki
			print pagename
  			linewiki = linewiki + '\t' + pagename
  		
  		fob.write(line + '\n')
  		fob.write(linewiki + '\n')

  	fob.close()
  	fr.close()
  	frwiki.close()


def main():
	# Crawler('../resources/query-data-test-set-unlabelled.xml')
	boldRequest('../resources/query-results-test/Bing/')

if __name__ == '__main__':
	main()

