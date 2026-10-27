#!/usr/bin/env bash
set -euo pipefail
# Run from the repository root. Add --unavailable to briefly stop book-service.
python3 -u - "$@" <<'PYTHON'
import json, os, sys, time, urllib.request, urllib.error, subprocess, uuid
reader=os.getenv('READER_URL','http://localhost:18086')
book=os.getenv('BOOK_URL','http://localhost:18087')
loan=os.getenv('LOAN_URL','http://localhost:18088')
def request(base,path,method='GET',body=None,expected=200):
    raw=None if body is None else json.dumps(body).encode()
    if body is not None: print('Request body: '+json.dumps(body,ensure_ascii=False))
    req=urllib.request.Request(base+path,data=raw,method=method,headers={'Content-Type':'application/json'})
    try:
        with urllib.request.urlopen(req,timeout=10) as response: status=response.status; text=response.read().decode()
    except urllib.error.HTTPError as ex: status=ex.code;text=ex.read().decode()
    print(f'{method} {base}{path} -> HTTP {status}')
    print(text)
    assert status==expected,(status,expected,text)
    return json.loads(text) if text else None
def count(): return len(request(loan,'/api/loans'))
for url in [reader,book,loan]:
    for attempt in range(90):
        try:
            result=request(url,'/actuator/health')
            assert result['status']=='UP';break
        except (OSError,AssertionError): time.sleep(2)
    else: raise RuntimeError('Service did not become healthy: '+url)
r=request(reader,'/api/readers','POST',{'name':'王超浩'},201)
b=request(book,'/api/books','POST',{'title':'微服务拆分实践','author':'王超浩','isbn':'W06-'+uuid.uuid4().hex[:12]},201)
c=next(c for c in request(book,'/api/copies') if c['bookId']==b['id'])
before=count()
l=request(loan,'/api/loans','POST',{'readerId':r['id'],'copyId':c['id']},201)
assert count()==before+1
assert request(book,f"/api/copies/{c['id']}")['borrowed'] is True
print('SUCCESS: loan rows +1; copy.borrowed=true')
request(loan,'/api/loans','POST',{'readerId':r['id'],'copyId':c['id']},409)
assert count()==before+1
request(loan,f"/api/loans/{l['id']}/return",'POST')
assert request(book,f"/api/copies/{c['id']}")['borrowed'] is False
assert request(loan,f"/api/loans/{l['id']}")['returnedAt'] is not None
print('RETURN: returnedAt set; copy.borrowed=false')
before=count()
request(loan,'/api/loans','POST',{'readerId':9223372036854775807,'copyId':c['id']},404)
assert count()==before
assert request(book,f"/api/copies/{c['id']}")['borrowed'] is False
print('MISSING READER: HTTP 404; loan rows unchanged; copy remains available')
request(loan,'/api/loans','POST',{'readerId':r['id'],'copyId':9223372036854775807},404)
assert count()==before
print('MISSING COPY: HTTP 404; loan rows unchanged')
if '--unavailable' in sys.argv:
    subprocess.run(['docker','compose','stop','book-service'],check=True)
    try:
        request(loan,'/api/loans','POST',{'readerId':r['id'],'copyId':c['id']},503)
        assert count()==before
        print('UNAVAILABLE: HTTP 503; loan rows unchanged')
    finally:
        subprocess.run(['docker','compose','start','book-service'],check=True)
    for attempt in range(90):
        try:
            assert request(book,'/actuator/health')['status']=='UP';break
        except (OSError,AssertionError): time.sleep(2)
    else: raise RuntimeError('book-service did not recover')
    assert request(book,f"/api/copies/{c['id']}")['borrowed'] is False
    print('RECOVERED: copy remains available')
print('ALL SMOKE CHECKS PASSED')
PYTHON
