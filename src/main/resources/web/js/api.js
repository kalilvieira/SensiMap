import {validar} from './regras.js';
async function request(path,method='GET',data) {
  let r;try {r=await fetch(`/api/${path}`,{method,headers:method==='GET'?{}:{'Content-Type':'application/x-www-form-urlencoded','X-SensiMap':'local'},body:data?new URLSearchParams(Object.entries(data).map(([k,v])=>[k,v??''])):undefined});}catch{throw new Error('Serviço indisponível. Tente novamente em instantes.');}
  let body;try{body=await r.json();}catch{throw new Error('O serviço não respondeu corretamente. Tente novamente em instantes.');}
  if(!r.ok)throw new Error(body.erro||'Não foi possível concluir a operação.');return body;
}
export const api={
  async iniciar(){const cfg=await request('config');if(cfg.modo!=='ORACLE')throw new Error('O servidor não está conectado ao Oracle.');return cfg.modo;},
  async listar(t){return request(t);},
  async buscar(t,n){return request(`${t}/${n}`);},
  async salvar(t,n,v){const clean=validar(t,v);return request(`${t}${n?'/'+n:''}`,n?'PUT':'POST',clean);},
  async indicadores(n,p=''){return request(`locais/${n}/indicadores?periodo=${encodeURIComponent(p)}`);},
  async estimulosLocal(n){return request(`locais/${n}/estimulos`);}
};
