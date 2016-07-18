filename = 'final.xml'
f1 = open(filename,'r')
for line in f1:
	line = line.replace('&lt;','<').replace('&gt;','>')
	print line