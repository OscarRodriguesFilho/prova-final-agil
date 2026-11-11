import { useCallback, useEffect, useMemo, useState } from 'react'
import { useAuth0 } from '@auth0/auth0-react'
import { cadastrar, excluir, listar, rolesDoToken } from './api.js'
import { ACOES, CAMPOS, RECURSO } from './campos.js'

const formatarData = (iso) => (iso ? String(iso).slice(0, 10).split('-').reverse().join('/') : '')
const maiuscula = (texto) => texto.charAt(0).toUpperCase() + texto.slice(1)

function formatarValor(campo, valor) {
  if (valor === null || valor === undefined || valor === '') return '–'
  if (campo.tipo === 'date') return formatarData(valor)
  if (campo.tipo === 'boolean') return valor === true || valor === 'true' ? 'Sim' : 'Não'
  if (campo.tipo === 'select') return campo.opcoes?.find((o) => String(o.valor) === String(valor))?.rotulo ?? String(valor)
  return String(valor)
}

export default function App() {
  const { isLoading, isAuthenticated, error, loginWithRedirect, logout, user, getAccessTokenSilently } = useAuth0()
  const [roles, setRoles] = useState([])
  const [registros, setRegistros] = useState([])
  const [carregando, setCarregando] = useState(false)
  const [erro, setErro] = useState(null)

  // renovarToken = true ignora o token em cache e pede um novo ao Auth0,
  // para que roles atribuídas depois do login apareçam sem precisar sair e entrar.
  const carregar = useCallback(async (renovarToken = false) => {
    setCarregando(true)
    setErro(null)
    try {
      const token = await getAccessTokenSilently(renovarToken ? { cacheMode: 'off' } : undefined)
      const rolesAtuais = rolesDoToken(token)
      setRoles(rolesAtuais)
      if (rolesAtuais.includes('ADMIN') || rolesAtuais.includes('USER')) {
        setRegistros(await listar(token))
      }
    } catch (e) {
      const sessaoExpirada = ['login_required', 'missing_refresh_token', 'invalid_grant'].includes(e.error)
      setErro(sessaoExpirada ? 'Sua sessão no Auth0 expirou. Clique em Sair e entre novamente.' : e.message)
    } finally {
      setCarregando(false)
    }
  }, [getAccessTokenSilently])

  useEffect(() => {
    if (isAuthenticated) carregar()
  }, [isAuthenticated, carregar])

  // Usado pelo botão "Excluir" da lista (só aparece se ACOES.excluir = true e o usuário for ADMIN)
  async function excluirRegistro(id) {
    if (!window.confirm(`Excluir este ${RECURSO.singular}?`)) return
    setErro(null)
    try {
      const token = await getAccessTokenSilently()
      await excluir(token, id)
      setRegistros((lista) => lista.filter((r) => r.id !== id))
    } catch (e) {
      setErro(e.message)
    }
  }

  if (isLoading) {
    return <main className="center"><p className="muted">Carregando…</p></main>
  }

  if (!isAuthenticated) {
    return (
      <main className="center">
        <div className="card narrow login">
          <h1>{RECURSO.titulo}</h1>
          <p className="muted">Entre com sua conta para ver e cadastrar {RECURSO.plural}.</p>
          {error && <p className="alert">{error.message}</p>}
          <button className="primary" onClick={() => loginWithRedirect()}>Entrar com Auth0</button>
        </div>
      </main>
    )
  }

  const isAdmin = roles.includes('ADMIN')
  const temAcesso = isAdmin || roles.includes('USER')

  return (
    <>
      <header className="topbar">
        <div className="brand">{RECURSO.titulo}</div>
        <div className="user">
          <div className="user-info">
            <span className="email">{user?.email ?? user?.name}</span>
            <span className="roles">
              {roles.length ? roles.map((r) => <span key={r} className={`chip ${r.toLowerCase()}`}>{r}</span>)
                : <span className="chip">sem role</span>}
            </span>
          </div>
          <button className="ghost" onClick={() => logout({ logoutParams: { returnTo: window.location.origin } })}>Sair</button>
        </div>
      </header>

      <main className="container">
        {erro && <p className="alert">{erro}</p>}

        {!carregando && !temAcesso && !erro && (
          <div className="card">
            <h2>Sem permissão</h2>
            <p className="muted">
              Seu usuário não tem as roles <b>ADMIN</b> ou <b>USER</b> no Auth0. Peça para um administrador atribuir uma
              role e depois clique no botão abaixo.
            </p>
            <button className="primary" onClick={() => carregar(true)}>Já recebi uma role: atualizar permissões</button>
          </div>
        )}

        {temAcesso && (
          <div className={isAdmin ? 'grid' : ''}>
            {isAdmin && (
              <Formulario
                emailUsuario={user?.email}
                onCadastrado={(novo) => setRegistros((lista) => [novo, ...lista])}
              />
            )}
            <Lista
              registros={registros}
              carregando={carregando}
              onAtualizar={() => carregar(true)}
              podeExcluir={isAdmin && ACOES.excluir}
              onExcluir={excluirRegistro}
            />
          </div>
        )}
      </main>
    </>
  )
}

function Formulario({ emailUsuario, onCadastrado }) {
  const { getAccessTokenSilently } = useAuth0()
  const vazio = () => Object.fromEntries(CAMPOS.map((c) => [c.nome, c.padrao ? c.padrao() : '']))
  const [form, setForm] = useState(vazio)
  const [enviando, setEnviando] = useState(false)
  const [erro, setErro] = useState(null)
  const [sucesso, setSucesso] = useState(false)

  async function enviar(e) {
    e.preventDefault()
    setErro(null)
    setSucesso(false)
    setEnviando(true)
    try {
      // Monta o JSON com os campos preenchidos; o back-end valida os obrigatórios.
      const corpo = { [RECURSO.campoEmailUsuario]: emailUsuario }
      for (const campo of CAMPOS) {
        const valor = form[campo.nome]
        if (valor === '' || valor === null || valor === undefined) continue
        if (campo.tipo === 'number') corpo[campo.nome] = Number(valor)
        else if (campo.tipo === 'boolean') corpo[campo.nome] = valor === 'true'
        else corpo[campo.nome] = valor
      }
      const token = await getAccessTokenSilently()
      const novo = await cadastrar(token, corpo)
      onCadastrado(novo)
      setForm(vazio())
      setSucesso(true)
    } catch (e2) {
      setErro({ mensagem: e2.message, campos: e2.campos ?? {} })
    } finally {
      setEnviando(false)
    }
  }

  return (
    <form className="card form" onSubmit={enviar} noValidate>
      <h2>Novo {RECURSO.singular}</h2>
      <p className="muted small">Criado por: <b>{emailUsuario}</b></p>

      {CAMPOS.map((campo) => (
        <Campo
          key={campo.nome}
          campo={campo}
          valor={form[campo.nome]}
          erro={erro?.campos?.[campo.nome]}
          onChange={(valor) => setForm((atual) => ({ ...atual, [campo.nome]: valor }))}
        />
      ))}

      {erro && <p className="alert">{erro.mensagem}</p>}
      {sucesso && <p className="success">{maiuscula(RECURSO.singular)} cadastrado.</p>}

      <button className="primary" type="submit" disabled={enviando}>
        {enviando ? 'Enviando…' : `Cadastrar ${RECURSO.singular}`}
      </button>
    </form>
  )
}

function Campo({ campo, valor, erro, onChange }) {
  const comum = {
    value: valor ?? '',
    onChange: (e) => onChange(e.target.value),
    required: campo.obrigatorio,
    placeholder: campo.placeholder,
  }

  let entrada
  if (campo.tipo === 'textarea') {
    entrada = <textarea rows={4} maxLength={campo.maxLength} {...comum} />
  } else if (campo.tipo === 'boolean') {
    entrada = (
      <select {...comum}>
        <option value="">Selecione…</option>
        <option value="true">Sim</option>
        <option value="false">Não</option>
      </select>
    )
  } else if (campo.tipo === 'select') {
    entrada = (
      <select {...comum}>
        <option value="">Selecione…</option>
        {campo.opcoes?.map((o) => <option key={o.valor} value={o.valor}>{o.rotulo}</option>)}
      </select>
    )
  } else {
    entrada = <input type={campo.tipo} min={campo.min} max={campo.max} step={campo.step} maxLength={campo.maxLength} {...comum} />
  }

  return (
    <label>
      <span>{campo.rotulo}{campo.obrigatorio && <span className="obrigatorio"> *</span>}</span>
      {entrada}
      {erro && <span className="field-error">{erro}</span>}
    </label>
  )
}

function Lista({ registros, carregando, onAtualizar, podeExcluir, onExcluir }) {
  const [busca, setBusca] = useState('')
  const [principal, ...detalhes] = CAMPOS

  const filtrados = useMemo(() => {
    const termo = busca.trim().toLowerCase()
    if (!termo) return registros
    return registros.filter((r) => Object.values(r).some((v) => String(v ?? '').toLowerCase().includes(termo)))
  }, [registros, busca])

  return (
    <section className="card">
      <div className="list-header">
        <div>
          <h2>{maiuscula(RECURSO.plural)}</h2>
          <p className="muted small">{filtrados.length} {filtrados.length === 1 ? RECURSO.singular : RECURSO.plural}</p>
        </div>
        <button className="ghost" onClick={onAtualizar} disabled={carregando}>
          {carregando ? 'Atualizando…' : 'Atualizar'}
        </button>
      </div>

      <input type="search" className="search" placeholder="Filtrar…" value={busca} onChange={(e) => setBusca(e.target.value)} />

      {carregando && registros.length === 0 ? (
        <p className="muted">Carregando…</p>
      ) : filtrados.length === 0 ? (
        <p className="muted empty">Nenhum {RECURSO.singular} encontrado.</p>
      ) : (
        <ul className="registros">
          {filtrados.map((r) => (
            <li key={r.id} className="registro">
              <div className="linha">
                <span className="registro-titulo">{formatarValor(principal, r[principal.nome])}</span>
                <span className="muted small">#{r.id}</span>
              </div>
              <dl className="detalhes">
                {detalhes.map((campo) => (
                  <div key={campo.nome}>
                    <dt>{campo.rotulo}</dt>
                    <dd>{formatarValor(campo, r[campo.nome])}</dd>
                  </div>
                ))}
              </dl>
              <div className="linha">
                {r[RECURSO.campoEmailUsuario]
                  ? <span className="muted small">criado por {r[RECURSO.campoEmailUsuario]}</span>
                  : <span />}
                {podeExcluir && (
                  <button className="ghost perigo" onClick={() => onExcluir(r.id)}>Excluir</button>
                )}
              </div>
            </li>
          ))}
        </ul>
      )}
    </section>
  )
}
