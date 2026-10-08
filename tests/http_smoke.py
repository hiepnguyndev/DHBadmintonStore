import urllib.request, urllib.error, json, time, uuid, os
if os.environ.get('RUN_HTTP_SMOKE') != 'true':
    raise SystemExit('Set RUN_HTTP_SMOKE=true and API_BASE_URL for an isolated local test server first.')
base=os.environ.get('API_BASE_URL','http://localhost:8080/api').rstrip('/')
def call(path,method='GET',body=None,token=None,expected=200):
 headers={'Content-Type':'application/json'}
 if token:headers['Authorization']='Bearer '+token
 req=urllib.request.Request(base+path,data=None if body is None else json.dumps(body).encode(),method=method,headers=headers)
 try:
  with urllib.request.urlopen(req,timeout=10) as r:code=r.status;data=json.load(r)
 except urllib.error.HTTPError as e:code=e.code;data=json.load(e)
 assert code==expected,(path,code,data.get('message'))
 return data.get('data')
for i in range(90):
 try:call('/products');break
 except (OSError,AssertionError):time.sleep(1)
else:raise RuntimeError('Backend HTTP startup timeout')
email='http-'+uuid.uuid4().hex+'@example.com'
password='LocalTest123!'
call('/auth/register','POST',dict(fullName='HTTP Smoke',email=email,password=password,confirmPassword=password),expected=201)
token=call('/auth/login','POST',dict(email=email,password=password))['accessToken']
assert call('/users/me',token=token)['email']==email
profile=dict(skillLevel='BEGINNER',playStyle='BALANCED',playType='BOTH',wristStrength='MEDIUM',preferredWeight='4U',minBudget=0,maxBudget=2000000)
call('/player-profile','POST',profile,token,201)
assert call('/player-profile',token=token)['preferredWeight']=='4U'
assert isinstance(call('/recommendations',token=token),list)
address=dict(receiverName='HTTP User',receiverPhone='0901234567',province='Ha Noi',district='Ha Dong',ward='Mo Lao',detailAddress='123 Test',isDefault=True)
call('/addresses','POST',address,token,201)
assert len(call('/addresses',token=token))==1
assert call('/cart',token=token)['items']==[]
call('/admin/statistics/overview',token=token,expected=403)
call('/users/me',expected=401)
req=urllib.request.Request(base+'/cart',method='OPTIONS',headers={'Origin':'http://localhost:5173','Access-Control-Request-Method':'GET','Access-Control-Request-Headers':'authorization'})
with urllib.request.urlopen(req) as r:assert r.headers.get('Access-Control-Allow-Origin')=='http://localhost:5173'
print('PASS: real HTTP registration/login/JWT/profile/recommendation/address/cart/authorization/CORS')
