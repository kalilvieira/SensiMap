import {api} from './api.js';
import {classes,periodos} from './regras.js';
export const $=s=>document.querySelector(s);
export function el(tag,cls='',text) {const e=document.createElement(tag);if(cls)e.className=cls;if(text!==undefined)e.textContent=text;return e;}
export function mensagem(text,error=false,target='#pagina-status'){const e=$(target);e.textContent=text;e.classList.toggle('error',error);e.setAttribute('role',error?'alert':'status');}
export async function iniciar(){const m=await api.iniciar(),indicador=$('#modo');if(indicador)indicador.textContent='Conectado ao banco';return m;}
export function falha(e){mensagem(e.message||'Não foi possível carregar a página.',true);}
export function periodo(p){return periodos[p]||'Todos os períodos';}
export function data(d){return d?d.split('-').reverse().join('/'):'Sem relatos';}
export function media(n){return n===null?'—':Number(n).toLocaleString('pt-BR',{minimumFractionDigits:2,maximumFractionDigits:2});}
export function badge(c){return el('span',`badge ${c==='DADOS_INSUFICIENTES'?'insuficientes':c.toLowerCase()}`,classes[c]);}
export function option(select,value,text){const o=el('option','',text);o.value=value;select.append(o);}
export function link(text,href,cls='text-link'){const a=el('a',cls,text);a.href=href;return a;}
export function botao(text,action,cls='text-link'){const b=el('button',cls,text);b.type='button';b.addEventListener('click',action);return b;}
export function vazio(title,text){const e=el('div','empty-state');e.append(el('h3','',title),el('p','',text));return e;}
