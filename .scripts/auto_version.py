import sys
from xml.etree import ElementTree

import requests

args = sys.argv[1:]
group = args[0]
artefact = args[1]

search_req = requests.get(f'https://oss.sonatype.org/service/local/lucene/search?g={group}&a={artefact}')
response = ElementTree.fromstring(search_req.content)
first_artefact = response.find('./data/artifact[1]')
latest_snapshot = first_artefact.find('latestSnapshot').text.removesuffix('-SNAPSHOT')

versions = [int(x) for x in latest_snapshot.split('.')]

print(f'{versions[0]}.{versions[1]}.{versions[2] + 1}-SNAPSHOT')
