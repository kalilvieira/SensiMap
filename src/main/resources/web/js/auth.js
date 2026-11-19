async function requestAuth(rota,metodo='GET',dados){
  const r=await fetch(`/api/auth/${rota}`,{method:metodo,credentials:'same-origin',headers:metodo==='GET'?{}:{'Content-Type':'application/x-www-form-urlencoded','X-SensiMap':'local'},body:metodo==='GET'?undefined:new URLSearchParams(dados??{})});
  let body;try{body=await r.json();}catch{throw new Error('Resposta inesperada do servidor.');}
  if(!r.ok)throw new Error(body.erro||'Não foi possível concluir o acesso.');return body;
}
function destinoSeguro(valor){
  const permitido=['locais.html','contribuir.html'];
  const pagina=String(valor??'').split('?')[0].split('/').pop();return permitido.includes(pagina)?String(valor):permitido[0];
}
export async function sessaoAtual(){
  try{return await requestAuth('sessao');}catch{return null;}
}
export async function exigirAcesso(perfis){
  const aceitos=Array.isArray(perfis)?perfis:[perfis],s=await sessaoAtual();
  if(s&&aceitos.includes(s.perfil))return s;
  const destino=encodeURIComponent(location.pathname.split('/').pop()+location.search);location.replace(`login.html?destino=${destino}`);return null;
}
export function ativarNavegacao(s){
  for(const e of document.querySelectorAll('[data-session-name]'))e.textContent=s.apelido;
  for(const b of document.querySelectorAll('[data-logout]'))b.addEventListener('click',sair);
}
export async function cadastrarUsuario(apelido,senha){
  const nome=String(apelido??'').trim();
  if(nome.length<3||nome.length>60)throw new Error('O pseudônimo deve ter entre 3 e 60 caracteres.');
  if(String(senha??'').length<8)throw new Error('A senha deve ter pelo menos 8 caracteres.');
  return requestAuth('cadastro','POST',{apelido:nome,senha});
}
export async function entrarUsuario(apelido,senha){
  return requestAuth('login','POST',{apelido,senha});
}
export function redirecionarAposAcesso(s,destino){location.assign(destinoSeguro(destino));}
export async function sair(){
  try{await requestAuth('logout','POST');}finally{location.assign('index.html');}
}
