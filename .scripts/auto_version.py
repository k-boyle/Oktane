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
latest_stable = first_artefact.find('latestRelease').text

stable_major, stable_minor, stable_patch = [int(x) for x in latest_stable.split('.')]
snapshot_major, snapshot_minor, snapshot_patch = [int(x) for x in latest_snapshot.split('.')]

snapshot_patch += 1

if stable_minor > snapshot_minor:
    snapshot_minor = stable_minor
    snapshot_patch = 1

if stable_major > snapshot_major:
    snapshot_major = stable_major


print(f'{snapshot_major}.{snapshot_minor}.{snapshot_patch + 1}-SNAPSHOT')
