# Prova Final – Gestão Ágil (projeto base genérico)

Projeto pronto para adaptar no dia da prova: API Spring Boot protegida pelo **Auth0** (roles `ADMIN` e `USER`), front-end React, **deploy automático** do back-end na EC2 pelo GitHub Actions e front na **Vercel**.

| | Endereço |
| --- | --- |
| Front-end | https://prova-final-agil.vercel.app |
| API | http://ec2-44-197-175-105.compute-1.amazonaws.com:8080/prova-final-agil |
| Repositório | https://github.com/OscarRodriguesFilho/prova-final-agil |
| Auth0 | tenant `dev-luk1185ok6ie6gvy.us.auth0.com`, API `https://prova-final-agil`, aplicação SPA `Prova Final Agil Front` |

```
Navegador ──login──▶ Auth0 (access token com roles e email)
    │
    │ HTTPS  /api/prova-final-agil  + Authorization: Bearer <token>
    ▼
Vercel (front) ──rewrite /api/* ──▶ EC2 :8080 (Spring Boot) ──▶ PostgreSQL (container)
```

O front chama `/api/...` no próprio domínio da Vercel e o `vercel.json` repassa para a EC2. Isso evita *mixed content* (página HTTPS chamando API HTTP) e CORS.

## O que já está pronto

- [x] API com `POST /prova-final-agil` (só ADMIN) e `GET /prova-final-agil` (ADMIN e USER)
- [x] Entidade genérica `Registro` com campos-modelo e validações (400 com a lista de campos inválidos)
- [x] Auth0: API, aplicação SPA, roles `ADMIN`/`USER` e Action que coloca roles e email no token
- [x] 9 testes automatizados (401, 403, 201, 200, 400) com tokens simulados
- [x] Deploy automático: push na `main` → testes → imagem Docker → EC2
- [x] Front com login, lista e formulário gerados a partir de um único arquivo (`frontend/src/campos.js`)
- [x] Secrets e variáveis do GitHub Actions configurados

## Rotas e regras atuais

| Método | Rota | Quem pode | Resposta |
| --- | --- | --- | --- |
| `POST` | `/prova-final-agil` | `ADMIN` | `201` com o registro criado |
| `GET` | `/prova-final-agil` | `ADMIN` e `USER` | `200` com a lista (mais recentes primeiro) |

Sem token: `401`. Token sem a role exigida: `403`. Corpo inválido: `400` com `{"error": ..., "campos": {...}}`.

### Entidade genérica `Registro`

| Campo | Tipo | Regra | Serve de modelo para |
| --- | --- | --- | --- |
| `id` | Long | gerado pelo banco | |
| `emailCriador` | String | vem do token do usuário logado | "email do avaliador", "email do responsável" |
| `titulo` | String | obrigatório, até 200 | nome, título, assunto |
| `descricao` | String | obrigatório, até 2000 | comentários, observações |
| `valor` | Integer | obrigatório, 0 a 10 | nota, prioridade, quantidade |
| `data` | LocalDate | opcional; padrão = hoje | data da avaliação, prazo |

---

# Guia para o dia da prova

## 1. Trocar o nome da rota

A rota fica em **uma constante no back-end** e **uma no front**:

| Onde | O que mudar |
| --- | --- |
| `backend/src/main/java/br/edu/insper/provafinalagil/RegistroController.java` | `public static final String ROTA = "/prova-final-agil";` |
| `frontend/src/api.js` | `export const ROTA = '/prova-final-agil'` |

O `SecurityConfig` e os testes usam a constante do controller, então se ajustam sozinhos.

## 2. Trocar a entidade (campos do enunciado)

Exemplo: o enunciado pede **Tarefa** com `titulo`, `responsavel` (email), `prioridade` (1 a 5) e `prazo`.

**Back-end** (`backend/src/main/java/br/edu/insper/provafinalagil/`):

1. **`Registro.java`**: renomeie a classe (Shift+F6 no IntelliJ renomeia em todos os arquivos) e troque os atributos, o construtor e os getters.
2. **`RegistroRequest.java`**: os mesmos campos, com as validações. As mais usadas:
   - `@NotBlank` para texto obrigatório e `@NotNull` para número ou data obrigatórios
   - `@Email` para email
   - `@Min(1) @Max(5)` para faixa de inteiro; `@DecimalMin("0.0")` para decimal
   - `@Size(max = 200)` para tamanho de texto
   - `@PastOrPresent` (data até hoje) e `@Future` (data futura)
   - `@Pattern(regexp = "...")` para formato
3. **`RegistroService.java`**: no método `cadastrar`, monte a entidade com os campos novos. Regras de negócio entram aqui, com `throw new RegraDeNegocioException("mensagem")`, que vira `400`.
4. **`RegistroRepository.java`**: se mudar o campo de data, ajuste o nome do método de ordenação (ex.: `findAllByOrderByPrazoDescIdDesc`).
5. **`RegistroControllerTest.java`**: troque o `CORPO_VALIDO` e os `jsonPath` pelos campos novos.

**Front-end:** edite só **`frontend/src/campos.js`**. O formulário e a lista são gerados a partir dele:

```js
export const RECURSO = { titulo: 'Gestão de Tarefas', singular: 'tarefa', plural: 'tarefas' }

export const CAMPOS = [
  { nome: 'titulo', rotulo: 'Título', tipo: 'text', obrigatorio: true },
  { nome: 'responsavel', rotulo: 'Email do responsável', tipo: 'email', obrigatorio: true },
  { nome: 'prioridade', rotulo: 'Prioridade (1 a 5)', tipo: 'number', obrigatorio: true, min: 1, max: 5 },
  { nome: 'prazo', rotulo: 'Prazo', tipo: 'date' },
  { nome: 'status', rotulo: 'Status', tipo: 'select', opcoes: [
    { valor: 'ABERTA', rotulo: 'Aberta' }, { valor: 'FECHADA', rotulo: 'Fechada' } ] },
]
```

O `nome` de cada campo precisa ser **igual** ao nome do campo no `Request` do back-end, porque é a chave do JSON.

> ⚠️ **Banco de dados:** o Hibernate está com `ddl-auto=update`. Ele **cria** tabelas e colunas novas, mas **não apaga** colunas antigas. Se você mantiver o nome `Registro` e trocar os campos, as colunas antigas `NOT NULL` continuam lá e o `POST` passa a dar erro 500. Duas saídas:
> - **renomear a entidade** (ex.: `Registro` → `Tarefa`), que gera uma tabela nova; é o mais simples;
> - ou **zerar o banco** na EC2 (apaga todos os dados):
>
>   ```powershell
>   ssh -i "C:\Users\oscar\Documents\insper\agil\.credencias\projsoft26b.pem" ubuntu@ec2-44-197-175-105.compute-1.amazonaws.com "cd /home/ubuntu/prova-final-agil && sudo docker compose down -v && sudo docker compose up -d --no-build"
>   ```

## 3. Criar uma rota nova

Dentro do controller (ex.: buscar por id e excluir):

```java
@GetMapping("/{id}")
public Registro buscar(@PathVariable Long id) {
    return repository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro não encontrado"));
}

@DeleteMapping("/{id}")
@ResponseStatus(HttpStatus.NO_CONTENT)
public void excluir(@PathVariable Long id) {
    if (!repository.existsById(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    repository.deleteById(id);
}
```

Para isso funcionar, injete o `RegistroRepository` no controller ou crie os métodos no `RegistroService`. Depois **libere a rota no `SecurityConfig.java`**, uma linha por método:

```java
.requestMatchers(HttpMethod.DELETE, ROTA + "/**").hasRole("ADMIN")
.requestMatchers(HttpMethod.PUT, ROTA + "/**").hasRole("ADMIN")
```

`GET ROTA/**` já está liberado para ADMIN e USER, então `GET /prova-final-agil/{id}` funciona sem mudar a segurança.

**Um recurso novo inteiro** (ex.: `/categorias`) segue o mesmo molde: copie `Registro`, `RegistroRequest`, `RegistroRepository`, `RegistroService` e `RegistroController` com o novo nome, defina `ROTA = "/categorias"` e adicione as regras dele no `SecurityConfig`. No front, para chamar a rota nova, crie uma função em `src/api.js` (ex.: `export const excluir = (token, id) => apiFetch(\`${ROTA}/${id}\`, token, { method: 'DELETE' })`).

**Mudar quem pode o quê:** só no `SecurityConfig.java`. Use `hasRole("ADMIN")`, `hasAnyRole("ADMIN", "USER")` ou `authenticated()`.

## 4. Testar localmente antes de subir

```powershell
cd "C:\Users\oscar\Documents\insper\Gestão Ágil\prova-final-agil\backend"
.\mvnw.cmd verify
```

O `mvnw.cmd` baixa o Maven sozinho. Os testes usam H2 em memória e tokens simulados, então não precisam de banco nem de Auth0.

Front local, opcional: copie `frontend/.env.example` para `frontend/.env.local`, preencha os dados do Auth0 e rode `npm install` e `npm run dev` dentro de `frontend`.

## 5. Redeploy: commit direto na main (o que colar no terminal)

```powershell
cd "C:\Users\oscar\Documents\insper\Gestão Ágil\prova-final-agil"
git add -A
git commit -m "feat: adapta o projeto para a prova"
git push origin main
```

- **Back-end:** todo push na `main` que mexa em `backend/` (ou no workflow) dispara **testes → build da imagem → deploy na EC2 → verificação**. Leva uns 2 a 3 minutos. Acompanhe com:

  ```powershell
  gh run watch -R OscarRodriguesFilho/prova-final-agil
  ```

  Para forçar um deploy sem mudar código: `gh workflow run backend.yml -R OscarRodriguesFilho/prova-final-agil`
- **Front-end:** se a Vercel estiver ligada ao GitHub, o push publica sozinho. Para publicar na hora, pela CLI, rode na **raiz** do projeto:

  ```powershell
  npx vercel --prod
  ```

## 6. Conferir se está no ar

```powershell
# sem token deve dar 401
curl.exe -i http://ec2-44-197-175-105.compute-1.amazonaws.com:8080/prova-final-agil
# pela Vercel, também 401
curl.exe -i https://prova-final-agil.vercel.app/api/prova-final-agil
```

Logs da API na EC2:

```powershell
ssh -i "C:\Users\oscar\Documents\insper\agil\.credencias\projsoft26b.pem" ubuntu@ec2-44-197-175-105.compute-1.amazonaws.com "sudo docker logs --tail 100 prova-final-agil-api"
```

---

# Como tudo está configurado

## Autenticação (Auth0)

1. O front faz login pedindo o `audience` `https://prova-final-agil`, e o Auth0 devolve um **access token JWT** para a API.
2. A Action **"Roles no token (prova-final-agil)"** (Post Login) coloca as roles do usuário na claim `https://prova-final-agil/roles` e o email na claim `https://prova-final-agil/email`:

   ```js
   exports.onExecutePostLogin = async (event, api) => {
     const namespace = 'https://prova-final-agil';
     const roles = (event.authorization && event.authorization.roles) || [];
     api.accessToken.setCustomClaim(`${namespace}/roles`, roles);
     api.idToken.setCustomClaim(`${namespace}/roles`, roles);
     if (event.user.email) {
       api.accessToken.setCustomClaim(`${namespace}/email`, event.user.email);
     }
   };
   ```

3. A API é um **OAuth2 Resource Server** e valida assinatura, `issuer` e `audience`. O `Auth0RolesConverter` transforma `["ADMIN"]` em `ROLE_ADMIN`, e o `SecurityConfig` aplica as regras por rota.
4. O front lê a mesma claim para mostrar ou esconder o formulário. Quem garante a regra é a API.

**Dar uma role a alguém:** Auth0 → *User Management → Users* → usuário → aba *Roles* → *Assign Roles*. A pessoa precisa fazer login uma vez antes, para aparecer na lista. Depois, ela clica em **Atualizar** no front (ou em "Já recebi uma role"), que pede um token novo sem precisar sair e entrar.

**Detalhes que costumam travar:**
- A API usa *Per-app authorization*: a aplicação SPA precisa de **API Access → Prova Final Agil API → Grant Access** (já feito). Sem isso, não vem token.
- A URL do front precisa estar em *Allowed Callback URLs*, *Allowed Logout URLs* e *Allowed Web Origins* da aplicação (já estão: Vercel e `http://localhost:5173`).

## GitHub Actions (`.github/workflows/backend.yml`)

| Tipo | Nome | Valor |
| --- | --- | --- |
| Secret | `EC2_HOST` | `ec2-44-197-175-105.compute-1.amazonaws.com` |
| Secret | `EC2_SSH_KEY` | conteúdo do `.pem` |
| Secret | `DB_PASSWORD` | senha aleatória do PostgreSQL |
| Variable | `AUTH0_DOMAIN` | `dev-luk1185ok6ie6gvy.us.auth0.com` |
| Variable | `AUTH0_AUDIENCE` | `https://prova-final-agil` |
| Variable | `AUTH0_CLAIMS_NAMESPACE` | `https://prova-final-agil` |
| Variable | `CORS_ALLOWED_ORIGINS` | `https://prova-final-agil.vercel.app,http://localhost:5173` |

Etapas: **Testes** (`mvn verify`) → **build da imagem** no GitHub, publicada em `ghcr.io/oscarrodriguesfilho/prova-final-agil/api` → **deploy**: copia o `docker-compose.yml` para `/home/ubuntu/prova-final-agil`, escreve o `.env`, faz `docker compose pull` e `up -d` → **verificação**: espera `401` sem token.

A EC2 só tem a porta **8080** aberta. O deploy **para automaticamente containers de outros projetos** que estejam usando a 8080 (ex.: o simulado), sem apagar os dados deles. O PostgreSQL usa um volume próprio, então os dados sobrevivem aos redeploys.

## Vercel

- Projeto `prova-final-agil`, configurado pelo `vercel.json` da **raiz**: instala e builda `frontend/`, publica `frontend/dist` e faz o rewrite `/api/*` → EC2.
- Variáveis de ambiente (Production): `VITE_AUTH0_DOMAIN`, `VITE_AUTH0_CLIENT_ID` e `VITE_AUTH0_AUDIENCE`. Como são lidas no **build**, depois de mudar alguma é preciso publicar de novo.

## Problemas comuns

| Sintoma | Causa provável |
| --- | --- |
| Front mostra "Sem permissão" | Usuário sem role no Auth0 → atribua e clique em **Atualizar** |
| `401` mesmo logado | Token de outro `audience`, ou `AUTH0_DOMAIN` errado na API |
| `403` | Role sem permissão para o método (veja o `SecurityConfig`) |
| `500` no POST depois de trocar campos | Colunas antigas no banco (veja a seção 2) |
| Deploy não rodou | O push não mexeu em `backend/` → `gh workflow run backend.yml` |
| Front com "Configuração do Auth0 ausente" | Faltam as variáveis `VITE_*` na Vercel |
| Mudança no front não aparece | Publique de novo: `npx vercel --prod` na raiz |
