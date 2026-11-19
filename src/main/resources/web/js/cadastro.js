import {cadastrarUsuario,redirecionarAposAcesso,sessaoAtual} from './auth.js';
import {$,mensagem} from './ui.js';

const form=$('#form-cadastro');
form.addEventListener('submit',async e=>{
  e.preventDefault();if(!form.reportValidity())return;
  const valores=Object.fromEntries(new FormData(form)),botao=$('#cadastrar');
  if(valores.senha!==valores.confirmacao){mensagem('As senhas não coincidem.',true);return;}
  botao.disabled=true;mensagem('Criando seu acesso…');
  try{
    const sessao=await cadastrarUsuario(valores.apelido,valores.senha);
    const confirmada=await sessaoAtual();
    if(!confirmada||confirmada.usuarioId!==sessao.usuarioId)throw new Error('O cadastro foi concluído, mas a sessão não pôde ser mantida. Tente entrar novamente.');
    redirecionarAposAcesso(sessao,'locais.html');
  }catch(erro){mensagem(erro.message||'Não foi possível criar o acesso.',true);}finally{botao.disabled=false;}
});
