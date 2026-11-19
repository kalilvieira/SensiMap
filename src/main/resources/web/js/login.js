import {entrarUsuario,redirecionarAposAcesso,sessaoAtual} from './auth.js';
import {$,mensagem} from './ui.js';

const form=$('#form-login'),identificacao=$('#identificacao');
form.addEventListener('submit',async e=>{
  e.preventDefault();if(!form.reportValidity())return;
  const botao=$('#entrar');botao.disabled=true;mensagem('Verificando acesso…');
  try{
    const s=await entrarUsuario(identificacao.value,form.elements.senha.value);
    const confirmada=await sessaoAtual();
    if(!confirmada||confirmada.usuarioId!==s.usuarioId)throw new Error('O acesso foi validado, mas o navegador não manteve a sessão. Reabra o SensiMap em http://127.0.0.1:8080.');
    redirecionarAposAcesso(s,new URLSearchParams(location.search).get('destino'));
  }catch(erro){mensagem(erro.message,true);}finally{botao.disabled=false;}
});
const atual=await sessaoAtual();if(atual)mensagem(`Há uma sessão ativa como ${atual.apelido}. Você pode entrar com outro acesso.`);
