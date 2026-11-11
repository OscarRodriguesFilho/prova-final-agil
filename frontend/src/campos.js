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
}

// Tipos aceitos: 'text' | 'email' | 'textarea' | 'number' | 'date' | 'select'
// Opções: obrigatorio, placeholder, min, max, step, maxLength, padrao: () => valor inicial,
//         opcoes: [{ valor: 'A', rotulo: 'Opção A' }] (só para 'select')
// O PRIMEIRO campo vira o título de cada item da lista.
export const CAMPOS = [
  { nome: 'titulo', rotulo: 'Título', tipo: 'text', obrigatorio: true, maxLength: 200, placeholder: 'Ex.: Primeiro registro' },
  { nome: 'descricao', rotulo: 'Descrição', tipo: 'textarea', obrigatorio: true, maxLength: 2000, placeholder: 'Detalhes…' },
  { nome: 'valor', rotulo: 'Valor (0 a 10)', tipo: 'number', obrigatorio: true, min: 0, max: 10, step: 1 },
  { nome: 'data', rotulo: 'Data', tipo: 'date', padrao: hoje },
]
