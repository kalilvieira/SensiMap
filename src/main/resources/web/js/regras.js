export const dimensoes = [['ruido', 'Ruído percebido'], ['movimento', 'Concentração de pessoas'], ['imprevisibilidade', 'Imprevisibilidade']];
export const periodos = {MANHA:'Manhã', TARDE:'Tarde', NOITE:'Noite'};
export const classes = {BAIXA:'Baixa', MODERADA:'Moderada', ALTA:'Alta', DADOS_INSUFICIENTES:'Dados insuficientes'};
export function hoje() { const d=new Date(); return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`; }
export function classificar(media,n) { return n<3 ? 'DADOS_INSUFICIENTES' : media<=2 ? 'BAIXA' : media<=3.5 ? 'MODERADA' : 'ALTA'; }
function texto(v,n,max,required=false) { const t=String(v??'').trim(); if(required&&!t) throw new Error(`${n} é obrigatório.`); if([...t].length>max) throw new Error(`${n} deve ter até ${max} caracteres.`); return t||null; }
export function id(v) { const n=Number(v); if(!Number.isSafeInteger(n)||n<=0) throw new Error('Selecione um registro válido.'); return n; }
export function validar(tipo, v) {
  if(tipo==='usuarios') return {apelido:texto(v.apelido,'Pseudônimo',60,true)};
  if(tipo==='estimulos') return {nome:texto(v.nome,'Nome',80,true),descricao:texto(v.descricao,'Descrição',300)};
  if(tipo==='locais') {
    const coord = (v,n,limite) => { if(v===null||v===undefined||String(v).trim()==='') return null; const x=Number(v); if(!Number.isFinite(x)||Math.abs(x)>limite||Math.abs(x*1e6-Math.round(x*1e6))>0.00001) throw new Error(`${n} deve estar na faixa válida e ter até 6 casas decimais.`); return x; };
    const latitude=coord(v.latitude,'Latitude',90),longitude=coord(v.longitude,'Longitude',180);
    if((latitude===null)!==(longitude===null)) throw new Error('Informe latitude e longitude juntas.');
    return {nome:texto(v.nome,'Nome',120,true),bairro:texto(v.bairro,'Bairro',80,true),endereco:texto(v.endereco,'Endereço',180),descricao:texto(v.descricao,'Descrição',500),latitude,longitude,dados_demonstrativos:false};
  }
  if(tipo==='avaliacoes') {
    if(!/^\d{4}-\d{2}-\d{2}$/.test(v.data_visita??'')) throw new Error('Informe uma data válida.');
    const date=new Date(`${v.data_visita}T12:00:00Z`);
    if(Number.isNaN(date.getTime())||date.toISOString().slice(0,10)!==v.data_visita||v.data_visita>hoje()) throw new Error('A data deve ser válida e não pode ser futura.');
    if(!Object.hasOwn(periodos,v.periodo)) throw new Error('Selecione manhã, tarde ou noite.');
    const out={usuario_id:id(v.usuario_id),local_id:id(v.local_id),data_visita:v.data_visita,periodo:v.periodo,comentario:texto(v.comentario,'Comentário',500)};
    for(const [k,label] of dimensoes) { out[k]=Number(v[k]); if(!Number.isInteger(out[k])||out[k]<1||out[k]>5) throw new Error(`${label}: selecione uma nota de 1 a 5.`); }
    return out;
  }
  throw new Error('Tipo de registro inválido.');
}
export function agregar(avaliacoes,periodo='') {
  const lista=avaliacoes.filter(a=>!periodo||a.periodo===periodo), n=lista.length;
  const r={quantidade:n,periodo:periodo||null,ultima_visita:n?lista.map(a=>a.data_visita).sort().at(-1):null,classificacoes:{}};
  for(const [k] of dimensoes) { r[k]=n?lista.reduce((s,a)=>s+a[k],0)/n:null; r.classificacoes[k]=classificar(r[k],n); }
  return r;
}
