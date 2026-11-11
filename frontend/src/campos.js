// ============================================================================
// CONFIGURAÇÃO GENÉRICA DO FRONT
// Na prova, ajuste só este arquivo para mudar textos, formulário e lista.
// Os "nome" dos campos precisam ser IGUAIS aos nomes dos campos do
// RegistroRequest/Registro no back-end (o JSON enviado/recebido usa esses nomes).
// ============================================================================

export const hoje = () => new Date().toLocaleDateString('sv-SE') // AAAA-MM-DD no fuso local

export const RECURSO = {
  titulo: 'Prova Final Ágil', // nome do sistema (topo e tela de login)
  singular: 'registro', // usado em "Novo registro", "Cadastrar registro"
  plural: 'registros', // usado na lista
  // Nome do campo (no back-end) que guarda o email do usuário logado. Ex.: 'emailResponsavel', 'emailAvaliador'.
  // O front envia esse campo no POST e mostra "criado por ..." na lista.
  campoEmailUsuario: 'emailCriador',
}

// Ações extras na lista (só aparecem para ADMIN):
// excluir: true mostra um botão "Excluir" em cada item, que chama DELETE ROTA/{id}.
//          O back-end precisa ter a rota DELETE (receita no README).
export const ACOES = {
  excluir: false,
}

// Tipos aceitos: 'text' | 'email' | 'textarea' | 'number' | 'date' | 'time' | 'datetime-local' | 'select' | 'boolean'
// ('boolean' vira um select Sim/Não e envia true/false no JSON)
// Opções: obrigatorio, placeholder, min, max, step, maxLength, padrao: () => valor inicial,
//         opcoes: [{ valor: 'A', rotulo: 'Opção A' }] (só para 'select')
// O PRIMEIRO campo vira o título de cada item da lista.
export const CAMPOS = [
  { nome: 'titulo', rotulo: 'Título', tipo: 'text', obrigatorio: true, maxLength: 200, placeholder: 'Ex.: Primeiro registro' },
  { nome: 'descricao', rotulo: 'Descrição', tipo: 'textarea', obrigatorio: true, maxLength: 2000, placeholder: 'Detalhes…' },
  { nome: 'valor', rotulo: 'Valor (0 a 10)', tipo: 'number', obrigatorio: true, min: 0, max: 10, step: 1 },
  { nome: 'data', rotulo: 'Data', tipo: 'date', padrao: hoje },
]
