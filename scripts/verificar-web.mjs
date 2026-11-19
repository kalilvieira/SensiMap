// Verifica regras do frontend sem criar dados e sem usar armazenamento do navegador.
import assert from 'node:assert/strict';
import {readFile} from 'node:fs/promises';
import {validar,agregar,classificar} from '../src/main/resources/web/js/regras.js';
import {cadastrarUsuario,entrarUsuario,sessaoAtual} from '../src/main/resources/web/js/auth.js';

let checks=0;
function check(fn){fn();checks++;}

check(()=>assert.equal(classificar(2,3),'BAIXA'));
check(()=>assert.equal(classificar(2.00001,3),'MODERADA'));
check(()=>assert.equal(classificar(3.5,3),'MODERADA'));
check(()=>assert.equal(classificar(3.50001,3),'ALTA'));
check(()=>assert.equal(classificar(5,2),'DADOS_INSUFICIENTES'));
check(()=>assert.equal(agregar([]).ultima_visita,null));
check(()=>assert.equal(agregar([{data_visita:'2026-11-01',periodo:'MANHA',ruido:2,movimento:3,imprevisibilidade:4}]).quantidade,1));

const avaliacao={usuario_id:1,local_id:2,data_visita:'2026-11-01',periodo:'MANHA',ruido:1,movimento:2,imprevisibilidade:3,comentario:'Relato subjetivo.'};
check(()=>assert.deepEqual(validar('avaliacoes',avaliacao),avaliacao));
for(const campo of ['ruido','movimento','imprevisibilidade'])for(const valor of [0,6,1.5,''])
  check(()=>assert.throws(()=>validar('avaliacoes',{...avaliacao,[campo]:valor})));
for(const data of ['2026-02-30','2099-01-01','data inválida'])
  check(()=>assert.throws(()=>validar('avaliacoes',{...avaliacao,data_visita:data})));
check(()=>assert.throws(()=>validar('avaliacoes',{...avaliacao,periodo:'MADRUGADA'})));
check(()=>assert.throws(()=>validar('usuarios',{apelido:' '.repeat(10)})));
check(()=>assert.throws(()=>validar('locais',{nome:'Local',bairro:'Bairro',latitude:1,longitude:null})));

const chamadas=[];
globalThis.fetch=async (url,opcoes={})=>{
  chamadas.push({url:String(url),opcoes});
  const body=Object.fromEntries(opcoes.body??[]);
  if(String(url).endsWith('/cadastro'))return {ok:true,json:async()=>({perfil:'USUARIO',apelido:body.apelido,usuarioId:91})};
  if(String(url).endsWith('/login'))return body.senha==='senha-incorreta'
    ?{ok:false,json:async()=>({erro:'Pseudônimo ou senha incorretos.'})}
    :{ok:true,json:async()=>({perfil:'USUARIO',apelido:body.apelido,usuarioId:91})};
  if(String(url).endsWith('/sessao'))return {ok:true,json:async()=>({perfil:'USUARIO',apelido:'perfil_teste',usuarioId:91})};
  throw new Error('Rota inesperada no teste.');
};

const conta=await cadastrarUsuario('perfil_teste','senha-segura');
check(()=>assert.equal(conta.perfil,'USUARIO'));
check(()=>assert.equal(chamadas[0].url,'/api/auth/cadastro'));
check(()=>assert.equal(chamadas[0].opcoes.method,'POST'));
check(()=>assert.equal(chamadas[0].opcoes.credentials,'same-origin'));
check(()=>assert.equal(Object.fromEntries(chamadas[0].opcoes.body).senha,'senha-segura'));
const sessao=await sessaoAtual();
check(()=>assert.equal(sessao.usuarioId,91));
check(()=>assert.equal(chamadas[1].opcoes.body,undefined));
const login=await entrarUsuario('perfil_teste','senha-segura');
check(()=>assert.equal(login.apelido,'perfil_teste'));
await assert.rejects(()=>entrarUsuario('perfil_teste','senha-incorreta'),/incorretos/);checks++;
globalThis.fetch=async()=>{throw new Error('rede indisponível');};
await assert.rejects(()=>entrarUsuario('perfil_teste','senha-segura'),/Serviço indisponível/);checks++;

for(const arquivo of ['api.js','auth.js','cadastro.js','contribuir.js']){
  const fonte=await readFile(new URL(`../src/main/resources/web/js/${arquivo}`,import.meta.url),'utf8');
  check(()=>assert.doesNotMatch(fonte,/localStorage|sessionStorage/));
  check(()=>assert.doesNotMatch(fonte,/['\"]DEMO['\"]/));
  check(()=>assert.doesNotMatch(fonte,/127\.0\.0\.1|localhost/));
}

const paginaInicial=await readFile(new URL('../src/main/resources/web/index.html',import.meta.url),'utf8');
check(()=>assert.doesNotMatch(paginaInicial,/id="pagina-status"/));
check(()=>assert.doesNotMatch(paginaInicial,/js\/inicio\.js/));
const paginaCadastro=await readFile(new URL('../src/main/resources/web/cadastro.html',import.meta.url),'utf8');
check(()=>assert.doesNotMatch(paginaCadastro,/ambiente local|autenticação segura de produção/));

console.log(`${checks} verificações JavaScript aprovadas. Persistência local desativada; sem acesso ao Oracle.`);
