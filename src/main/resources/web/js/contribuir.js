import {api} from './api.js';
import {exigirAcesso,ativarNavegacao} from './auth.js';
import {hoje,id} from './regras.js';
import {$,el,iniciar,falha,mensagem,option,link} from './ui.js';
const sessao=await exigirAcesso('USUARIO');if(!sessao)await new Promise(()=>{});ativarNavegacao(sessao);
const form=$('#form-avaliacao'),button=$('#salvar-avaliacao'),formLocal=$('#form-local'),localButton=$('#salvar-local'),params=new URLSearchParams(location.search);let editar=null;
form.elements.data_visita.max=hoje();
formLocal.addEventListener('submit',async e=>{
  e.preventDefault();if(!formLocal.reportValidity())return;localButton.disabled=true;mensagem('Adicionando local…',false,'#local-status');
  try{const novo=await api.salvar('locais',null,Object.fromEntries(new FormData(formLocal)));option(form.elements.local_id,novo.id,novo.nome);form.elements.local_id.value=String(novo.id);formLocal.reset();mensagem('Local adicionado e selecionado para a sua percepção.',false,'#local-status');$('#local-status').focus();}
  catch(error){mensagem(error.message||'Não foi possível adicionar o local.',true,'#local-status');$('#local-status').focus();}finally{localButton.disabled=false;}
});
form.addEventListener('submit',async e=>{
  e.preventDefault();if(!form.reportValidity())return;button.disabled=true;mensagem('Salvando percepção…');
  try{const valores={...Object.fromEntries(new FormData(form)),usuario_id:sessao.usuarioId};const registro=await api.salvar('avaliacoes',editar,valores);mensagem('Registro concluído.');$('#pagina-status').append(el('br'),link('Consultar as percepções do local →',`locais.html?local=${registro.local_id}`));$('#pagina-status').focus();if(!editar){form.reset();form.elements.local_id.value=registro.local_id;form.elements.usuario_id.value=String(sessao.usuarioId);}}
  catch(error){falha(error);$('#pagina-status').focus();}finally{button.disabled=false;}
});
(async()=>{try{
  await iniciar();$('#destino-dados').textContent='Os registros são enviados ao banco de dados do SensiMap.';
  const locais=await api.listar('locais');for(const l of locais)option(form.elements.local_id,l.id,l.nome);form.elements.usuario_id.replaceChildren();option(form.elements.usuario_id,sessao.usuarioId,sessao.apelido);form.elements.usuario_id.setAttribute('aria-readonly','true');
  if(!locais.length)throw new Error('Nenhum local está disponível para avaliação.');
  if(params.has('editar')){editar=id(params.get('editar'));const a=await api.buscar('avaliacoes',editar);if(a.usuario_id!==sessao.usuarioId)throw new Error('Você só pode editar suas próprias percepções.');for(const[k,v]of Object.entries(a)){const input=form.elements.namedItem(k);if(input)input.value=v??'';}$('#titulo-form').textContent='Editar uma percepção.';button.textContent='Salvar alterações →';}
  else if(params.has('local'))form.elements.local_id.value=params.get('local');
  button.disabled=false;localButton.disabled=false;
}catch(e){falha(e);}})();
