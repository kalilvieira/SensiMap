"""Smoke tests somente leitura para o servidor conectado ao Oracle."""
import json
import urllib.request
import urllib.error
from pathlib import Path

checks = 0

def request(path, expected=200, method='GET', headers=None):
    global checks
    req = urllib.request.Request('http://127.0.0.1:8080' + path, method=method, headers=headers or {})
    try:
        response = urllib.request.urlopen(req, timeout=10)
    except urllib.error.HTTPError as error:
        response = error
    with response:
        body = response.read()
        assert response.status == expected, (path, response.status, expected, body)
        assert response.headers.get('X-Content-Type-Options') == 'nosniff'
        checks += 1
        return body, response.headers

body, _ = request('/api/config')
assert json.loads(body)['modo'] == 'ORACLE', 'O servidor deve exigir o Oracle.'

class NoRedirect(urllib.request.HTTPRedirectHandler):
    def redirect_request(self, req, fp, code, msg, headers, newurl):
        return None

try:
    urllib.request.build_opener(NoRedirect).open('http://localhost:8080/login.html', timeout=10)
    raise AssertionError('localhost deveria ser redirecionado para o host canônico.')
except urllib.error.HTTPError as error:
    assert error.code == 307
    assert error.headers['Location'] == 'http://127.0.0.1:8080/login.html'
    checks += 1

root = Path(__file__).resolve().parent.parent / 'src/main/resources/web'
for file in sorted(root.rglob('*')):
    if file.is_file():
        body, headers = request('/' + file.relative_to(root).as_posix())
        assert body == file.read_bytes(), f'Recurso desatualizado: {file.name}'
        if file.suffix == '.js':
            assert 'javascript' in headers['Content-Type']
request('/')
request('/admin.html', 404)
request('/js/demo.js', 404)
request('/.env', 404)
request('/%2e%2e/README.md', 404)
# Rotas de dados abrem conexão antes de recusarem a ausência de sessão.
request('/api/health', 401)
request('/api/usuarios', 401)
# A consulta da sessão não depende de uma segunda conexão ao Oracle.
request('/api/auth/sessao', 401)
request('/api/config', 405, method='OPTIONS')
request('/api/config', 403, headers={'Origin':'https://example.invalid'})
request('/api/config', 403, headers={'Host':'example.invalid'})
request('/api/usuarios', 403, method='POST')
print(f'{checks} verificações HTTP aprovadas: páginas, assets, Oracle acessível e controles de acesso local.')
