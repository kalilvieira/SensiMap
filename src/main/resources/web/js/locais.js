import {api} from './api.js';
import {exigirAcesso,ativarNavegacao} from './auth.js';
import {dimensoes,classes} from './regras.js';
import {$,el,iniciar,falha,mensagem,periodo,data,media,badge,option,link,botao,vazio} from './ui.js';
const sessao=await exigirAcesso('USUARIO');if(!sessao)await new Promise(()=>{});ativarNavegacao(sessao);
const form=$('#filtros');let lista=[],sequencia=0,detalheSeq=0;
for(const [k,nome] of dimensoes){const field=el('div','field'),label=el('label','',nome),select=el('select');select.name=k;select.id=`filtro-${k}`;label.htmlFor=select.id;option(select,'','Todas as percepções');for(const [v,n]of Object.entries(classes))option(select,v,n);field.append(label,select);$('#filtros-sensoriais').append(field);}
async function render(){
  const seq=++sequencia, f=Object.fromEntries(new FormData(form));mensagem('');$('#contagem').textContent='Consultando percepções…';
  try{
    const encontrados=lista.filter(l=>(l.nome.toLocaleLowerCase('pt-BR').includes(f.nome.trim().toLocaleLowerCase('pt-BR')))&&(l.bairro.toLocaleLowerCase('pt-BR').includes(f.bairro.trim().toLocaleLowerCase('pt-BR'))));
    const resultados=await Promise.all(encontrados.map(async local=>({local,i:await api.indicadores(local.id,f.periodo)})));if(seq!==sequencia)return;
    const filtrados=resultados.filter(({i})=>dimensoes.every(([k])=>!f[k]||i.classificacoes[k]===f[k]));
    $('#lista-locais').replaceChildren();$('#contagem').textContent=`${filtrados.length} ${filtrados.length===1?'local encontrado':'locais encontrados'}`;$('#recorte').textContent=`${periodo(f.periodo)} · relatos subjetivos · amostra simulada`;
    for(const {local:l,i} of filtrados){const card=el('article','place-card'),top=el('div','place-top'),icon=el('span','place-icon','⌂');icon.setAttribute('aria-hidden','true');top.append(icon,el('h3','',l.nome),el('p','bairro',l.bairro));const body=el('div','place-body');for(const[k,n]of dimensoes){const row=el('div','dimension');row.append(el('span','dim-label',n),badge(i.classificacoes[k]));body.append(row);}body.append(el('p','sample-line',`${i.quantidade===0?'Ainda não avaliado':i.quantidade===1?'1 avaliação':i.quantidade+' avaliações'} · ${periodo(f.periodo)}\nÚltima visita: ${data(i.ultima_visita)}`));const bottom=el('div','place-bottom'),a=link('Ver percepções',`locais.html?local=${l.id}${f.periodo?'&periodo='+f.periodo:''}`,'');a.addEventListener('click',async ev=>{ev.preventDefault();history.replaceState(null,'',a.href);try{await detalhe(l.id,true);}catch(e){falha(e);}});bottom.append(a);card.append(top,body,bottom);$('#lista-locais').append(card);}
    if(!filtrados.length)$('#lista-locais').append(vazio('Nenhum local neste recorte','Tente outro nome, bairro ou percepção. Você também pode limpar os filtros.'));
    const selecionado=Number(new URLSearchParams(location.search).get('local'));if(selecionado)await detalhe(selecionado,false);
  }catch(e){if(seq===sequencia){$('#contagem').textContent='Consulta indisponível';falha(e);}}
}
async function detalhe(id,focus){
  const ds=++detalheSeq,p=form.elements.periodo.value;
  const [l,i,relatos,estimulos,usuarios]=await Promise.all([api.buscar('locais',id),api.indicadores(id,p),api.listar('avaliacoes'),api.estimulosLocal(id),api.listar('usuarios')]);
  if(ds!==detalheSeq||p!==form.elements.periodo.value)return;
  const area=$('#detalhe');area.replaceChildren();area.hidden=false;const head=el('div','detail-heading'),titles=el('div');titles.append(el('p','eyebrow','PERCEPÇÕES SOBRE O LOCAL'),el('h2','',l.nome),el('p','meta',l.bairro));head.append(titles,botao('Fechar detalhes',()=>{detalheSeq++;area.hidden=true;history.replaceState(null,'','locais.html');$('#nome').focus();}));area.append(head);if(l.descricao)area.append(el('p','meta',l.descricao));if(l.endereco)area.append(el('p','meta',l.endereco));area.append(el('p','meta',`${i.quantidade} ${i.quantidade===1?'avaliação':'avaliações'} · ${periodo(p)} · Última visita: ${data(i.ultima_visita)}`));
  if(!i.quantidade)area.append(el('p','notice','Ainda não avaliado. Falta de registros não significa ausência de estímulos.'));
  const metrics=el('div','three-grid');for(const[k,n]of dimensoes){const m=el('div','metric'),mean=el('strong','mean',media(i[k]));mean.append(el('small','',i[k]===null?'':' / 5 · média'));m.append(el('h3','',n),mean,badge(i.classificacoes[k]));metrics.append(m);}area.append(metrics,el('p','meta','Classificações independentes, calculadas com pelo menos três relatos. Não são previsões clínicas nem garantia de segurança.'));
  const tags=el('div','tags');for(const e of estimulos)tags.append(el('span','tag',e.nome));area.append(tags);const actions=el('div','actions');actions.append(link('Registrar uma percepção →',`contribuir.html?local=${id}`,'button primary'));if(l.latitude!==null&&l.longitude!==null){const map=link('Ver coordenadas no mapa externo ↗',`https://www.openstreetmap.org/?mlat=${encodeURIComponent(l.latitude)}&mlon=${encodeURIComponent(l.longitude)}#map=16/${encodeURIComponent(l.latitude)}/${encodeURIComponent(l.longitude)}`);map.target='_blank';map.rel='noopener noreferrer';actions.append(map);}area.append(actions,el('h3','sample-line','Relatos deste período'));
  for(const a of relatos.filter(a=>a.local_id===id&&(!p||a.periodo===p)).sort((a,b)=>b.data_visita.localeCompare(a.data_visita)||b.id-a.id)){const r=el('article','report');r.append(el('p','meta',`${usuarios.find(u=>u.id===a.usuario_id)?.apelido||'Pseudônimo'} · ${data(a.data_visita)} · ${periodo(a.periodo)}`),el('p','',a.comentario||'Sem comentário adicional.'),el('p','meta',`Ruído: ${a.ruido}/5 · Concentração de pessoas: ${a.movimento}/5 · Imprevisibilidade: ${a.imprevisibilidade}/5`));area.append(r);}
  if(focus){area.focus();area.scrollIntoView({block:'start'});}
}
form.addEventListener('submit',e=>{e.preventDefault();render();});form.addEventListener('reset',()=>{detalheSeq++;history.replaceState(null,'','locais.html');$('#detalhe').hidden=true;setTimeout(render,0);});
(async()=>{try{await iniciar();lista=await api.listar('locais');const p=new URLSearchParams(location.search).get('periodo');if(['MANHA','TARDE','NOITE'].includes(p))form.elements.periodo.value=p;await render();}catch(e){$('#contagem').textContent='Consulta indisponível';falha(e);}})();
