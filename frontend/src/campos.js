// ============================================================================
// CONFIGURAÇÃO DO FRONT: Base de Conhecimentos
// Os "nome" dos campos precisam ser IGUAIS aos nomes do ArtigoRequest/Artigo no back-end.
// ============================================================================

export const hoje = () => new Date().toLocaleDateString('sv-SE') // AAAA-MM-DD no fuso local

export const RECURSO = {
  titulo: 'Base de Conhecimentos', // nome do sistema (topo e tela de login)
  singular: 'artigo', // usado em "Novo artigo", "Cadastrar artigo"
  plural: 'artigos', // usado na lista
  // Campo do back-end com o email do usuário logado (o front mostra "criado por ...").
  campoEmailUsuario: 'emailCriador',
}

// Ações extras na lista (só aparecem para ADMIN):
// excluir: true mostra um botão "Excluir" em cada item, que chama DELETE /artigos/{id}.
export const ACOES = {
  excluir: true,
}

// Tipos aceitos: 'text' | 'email' | 'textarea' | 'number' | 'date' | 'time' | 'datetime-local' | 'select' | 'boolean'
// O PRIMEIRO campo vira o título de cada item da lista.
export const CAMPOS = [
  { nome: 'titulo', rotulo: 'Título', tipo: 'text', obrigatorio: true, maxLength: 200, placeholder: 'Ex.: Como configurar a VPN' },
  { nome: 'descricao', rotulo: 'Descrição', tipo: 'textarea', obrigatorio: true, maxLength: 2000, placeholder: 'Conteúdo do artigo…' },
  { nome: 'prioridade', rotulo: 'Prioridade', tipo: 'select', obrigatorio: true, opcoes: [
    { valor: 'BAIXA', rotulo: 'Baixa' },
    { valor: 'MEDIA', rotulo: 'Média' },
    { valor: 'ALTA', rotulo: 'Alta' },
  ] },
]
