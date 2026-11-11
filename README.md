# Prova Final – Gestão Ágil (projeto base genérico)

Projeto pronto para adaptar no dia da prova:
- API Spring Boot protegida pelo **Auth0**, com roles `ADMIN` e `USER`;
- front-end React;
- **deploy automático** do back-end na EC2 pelo GitHub Actions e do front na **Vercel**.

| | Endereço |
| --- | --- |
| Front-end | https://prova-final-agil.vercel.app |
| API | http://ec2-44-197-175-105.compute-1.amazonaws.com:8080/prova-final-agil |
| Repositório | https://github.com/OscarRodriguesFilho/prova-final-agil |
| Pasta local | `C:\Users\oscar\Documents\insper\Gestão Ágil\prova-final-agil` |
| Auth0 | tenant `dev-luk1185ok6ie6gvy.us.auth0.com`, API (audience) `https://prova-final-agil`, aplicação SPA `Prova Final Agil Front` (Client ID `qwuqZ3x491Q23LCkjrTK7ssbaqxYdpQD`) |

> **Como usar este README com uma IA (Gemini, ChatGPT…):** cole a "Mensagem pronta para a IA" abaixo, depois **este README inteiro** e por último o **enunciado da prova**. Este documento tem tudo de que a IA precisa: versões, código atual completo, receitas testadas e um exemplo resolvido.

## Mensagem pronta para a IA

```text
Vou fazer uma prova de programação e posso usar IA. Abaixo está o README do meu projeto base
(Spring Boot 4 + React + Auth0), que JÁ está publicado e com deploy automático. Leia primeiro a
seção "Instruções para a IA" e siga o "Roteiro da prova". O "Exemplo completo resolvido" mostra
exatamente o resultado esperado para um caso parecido. Depois do README vem o enunciado.

Responda assim:
1) um resumo do que vai mudar (entidade, campos, rotas, permissões, regras);
2) para CADA arquivo a criar, alterar ou apagar: o caminho completo e o conteúdo COMPLETO do
   arquivo, pronto para colar (não use "..." nem "resto igual");
3) os comandos do PowerShell para rodar os testes, publicar e conferir.
Não mude arquivos que o README diz para não mexer.
```

---

# Instruções para a IA (leia antes de responder)

1. **Versões exatas:**
   - back-end: Spring Boot **4.1.0**, Java **21**, Spring Security **7**, Hibernate **7**, Jackson **3**, PostgreSQL 16 (H2 nos testes);
   - front-end: React **19**, Vite **8**, `@auth0/auth0-react` **2**.

   Várias APIs mudaram desde o Spring Boot 2 e 3. **Use exatamente estas:**
   - `@AutoConfigureMockMvc` → `import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;` (**não** use `org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc`, que não compila no Boot 4).
   - `@MockBean` não existe mais. Se precisar, use `@MockitoBean` (`org.springframework.test.context.bean.override.mockito.MockitoBean`).
   - Jackson 3 usa o pacote `tools.jackson.databind` (não `com.fasterxml.jackson.databind`). Os testes deste projeto mandam JSON como texto e **não precisam** de `ObjectMapper`.
   - Use `jakarta.*` (`jakarta.persistence`, `jakarta.validation`), nunca `javax.*`.
   - Não adicione dependências ao `pom.xml`. Tudo o que é necessário já está lá (web, validation, JPA, security/OAuth2 resource server, PostgreSQL, H2, spring-security-test).
2. **Um único pacote Java:** `br.edu.insper.provafinalagil`, na pasta `backend/src/main/java/br/edu/insper/provafinalagil/`, e os testes em `backend/src/test/java/br/edu/insper/provafinalagil/`. Não crie subpacotes.
3. **O código atual completo** de cada arquivo está na seção [Código atual completo](#código-atual-completo-antes-da-prova). Baseie as mudanças nele.
4. **Não altere** (a não ser que uma receita mande):
   - `Auth0RolesConverter.java`, `ProvaFinalAgilApplication.java`, `ApiExceptionHandler.java`;
   - as três exceções;
   - `application.properties`, `pom.xml`, `Dockerfile`, `docker-compose.yml`;
   - `.github/workflows/backend.yml`, `vercel.json`;
   - `frontend/src/main.jsx`, `frontend/src/config.js`, `frontend/src/styles.css`.

   No `SecurityConfig.java`, mude **só** a linha da constante `ROTA` e as linhas `requestMatchers`.
5. **Erros prontos**, que respondem `{"error": "mensagem"}`:
   - `throw new RegraDeNegocioException("...")` → **400**;
   - `throw new ConflitoException("...")` → **409**;
   - `throw new NaoEncontradoException("...")` → **404**;
   - a validação do `@Valid` (anotações do Request) → **400**, com `{"error": "Dados inválidos", "campos": {"campo": "mensagem"}}`.
6. **Email do usuário logado:** vem do token, pela claim configurada em `app.auth0.email-claim`. Use o mesmo padrão do `RegistroController`: `@Value("${app.auth0.email-claim}")` no construtor e `@AuthenticationPrincipal Jwt jwt` no método.
7. **Testes:**
   - O GitHub Actions roda os testes **antes** do deploy. Se não compilarem ou falharem, **nada é publicado**.
   - Sempre entregue o teste da entidade nova, compilando: apague `RegistroControllerTest.java` e crie `XxxControllerTest.java` no modelo do exemplo.
   - Mande o aluno rodar `.\mvnw.cmd verify` antes do push.
8. **Front:** para CRUD comum **não é preciso mexer no `App.jsx`**. Troque só:
   - `frontend/src/campos.js`: textos, campos, `campoEmailUsuario` e `ACOES`;
   - a constante `ROTA` em `frontend/src/api.js`.
9. **Nomes de campos:** camelCase, sem acentos (`dataEntrega`, `valorTotal`). O mesmo nome vale no Java (Entity e Request), no JSON e no `campos.js`.
10. **Ambiente do aluno:** Windows + PowerShell. Use `curl.exe` (não `curl`), `.\mvnw.cmd` e caminhos com `\`.
11. **Banco:** renomear a entidade (ex.: `Registro` → `Reserva`) faz o Hibernate criar uma **tabela nova**. Não é preciso mexer no banco. Manter o nome `Registro` e trocar os campos causa erro 500; veja "Problemas comuns".

---

# Roteiro da prova

## Passo 1 – Extrair do enunciado

Anote antes de mexer no código:

| Item | Exemplo (Reservas de Salas) |
| --- | --- |
| Nome da entidade (singular, PascalCase) | `Reserva` |
| Rota | `/reservas` |
| Campo com o email do usuário logado | `emailResponsavel` |
| Demais campos, com tipos (tabela abaixo) | `sala` (texto), `data` (data), `horaInicio` e `horaFim` (hora), `motivo` (texto longo) |
| Quem pode fazer o quê | POST: ADMIN · GET: ADMIN e USER · DELETE: ADMIN |
| Regras de negócio, com o código HTTP | fim depois do início (400); sem sobreposição na mesma sala e dia (409) |
| Rotas extras | `DELETE /reservas/{id}` |

### Tabela de tipos (enunciado → Java → validação → JSON → `campos.js`)

| No enunciado | Tipo Java (Entity e Request) | Validação no Request | JSON | `tipo` no `campos.js` |
| --- | --- | --- | --- | --- |
| texto curto | `String` | `@NotBlank @Size(max = 200)` | `"Sala 1"` | `'text'` |
| texto longo | `String` + na Entity `@Column(length = 2000)` | `@NotBlank @Size(max = 2000)` | `"..."` | `'textarea'` |
| email | `String` | `@NotBlank @Email` | `"a@b.com"` | `'email'` |
| inteiro (nota, quantidade) | `Integer` | `@NotNull @Min(0) @Max(10)` | `7` | `'number'` (+ `min`, `max`) |
| decimal / dinheiro | `BigDecimal` + na Entity `@Column(precision = 10, scale = 2)` | `@NotNull @DecimalMin("0.01") @Digits(integer = 8, fraction = 2)` | `19.90` | `'number'` (+ `step: 0.01`) |
| data | `LocalDate` | `@NotNull`, `@FutureOrPresent` ou `@PastOrPresent` | `"2026-11-20"` | `'date'` |
| hora | `LocalTime` | `@NotNull` | `"14:30"` (a API devolve `"14:30:00"`) | `'time'` |
| data e hora | `LocalDateTime` | `@NotNull @Future` | `"2026-11-20T14:30"` | `'datetime-local'` |
| sim/não | `Boolean` | `@NotNull` | `true` | `'boolean'` |
| lista fixa (status, categoria) | `enum` + na Entity `@Enumerated(EnumType.STRING)` | `@NotNull` | `"ABERTA"` | `'select'` + `opcoes` |
| email do usuário logado | `String`: na Entity é obrigatório; no Request é opcional com `@Email` | — | — | `RECURSO.campoEmailUsuario` |

Imports: `java.math.BigDecimal` e `java.time.LocalDate`/`LocalTime`/`LocalDateTime`. Validações em `jakarta.validation.constraints.*`, JPA em `jakarta.persistence.*`.

**Enum** (ex.: status), em um arquivo novo `Status.java` no mesmo pacote:

```java
package br.edu.insper.provafinalagil;

public enum Status { ABERTA, EM_ANDAMENTO, CONCLUIDA }
```

Na Entity: `@Enumerated(EnumType.STRING) @Column(nullable = false) private Status status;` (imports `jakarta.persistence.EnumType` e `jakarta.persistence.Enumerated`). No Request: `@NotNull(message = "é obrigatório") Status status`. Um valor fora da lista no JSON gera 400 ("Corpo da requisição inválido"). No `campos.js`:

```js
{ nome: 'status', rotulo: 'Status', tipo: 'select', obrigatorio: true, opcoes: [
  { valor: 'ABERTA', rotulo: 'Aberta' },
  { valor: 'EM_ANDAMENTO', rotulo: 'Em andamento' },
  { valor: 'CONCLUIDA', rotulo: 'Concluída' },
] },
```

## Passo 2 – Back-end

Siga o [Exemplo completo resolvido](#exemplo-completo-resolvido-reservas-de-salas) como modelo.

1. **Crie** os arquivos da entidade nova no pacote `br.edu.insper.provafinalagil` (troque `Xxx` pelo nome):
   - `Xxx.java`, a entidade;
   - `XxxRequest.java`, o corpo do POST, com as validações;
   - `XxxRepository.java`;
   - `XxxService.java`, com as regras de negócio;
   - `XxxController.java`, com `public static final String ROTA = "/rota-do-enunciado";`.
2. **Apague** os arquivos antigos: `Registro.java`, `RegistroRequest.java`, `RegistroRepository.java`, `RegistroService.java` e `RegistroController.java`.
3. **`SecurityConfig.java`:** troque `RegistroController.ROTA` por `XxxController.ROTA` e ajuste as linhas `requestMatchers` às permissões do enunciado.
4. **Testes:** apague `RegistroControllerTest.java` e crie `XxxControllerTest.java` no modelo do exemplo, com um teste para cada regra (401, 403, 201, 200, 400, 409, 404…).

## Passo 3 – Front-end

1. `frontend/src/api.js`: `export const ROTA = '/rota-do-enunciado'`.
2. `frontend/src/campos.js`:
   - `RECURSO`: título, singular, plural e `campoEmailUsuario`, o nome do campo com o email do logado;
   - `ACOES.excluir = true`, se existir `DELETE`;
   - `CAMPOS`: um item por campo do Request, **exceto** o email do logado.

## Passo 4 – Testar, publicar e conferir

Veja a seção [Testar, publicar e conferir](#testar-publicar-e-conferir). Em resumo:

```powershell
cd "C:\Users\oscar\Documents\insper\Gestão Ágil\prova-final-agil\backend"
.\mvnw.cmd verify
cd ..
git add -A
git commit -m "feat: adapta o projeto para a prova"
git push origin main
gh run watch -R OscarRodriguesFilho/prova-final-agil
```

---

# Receitas (todas testadas no exemplo de Reservas)

> **Validação deste README:** uma IA sem nenhum contexto recebeu só este documento e o enunciado de um "Sistema de Chamados (Help Desk)". O enunciado tinha enum, valor em reais, campo sim/não, USER que cadastra mas só vê os próprios chamados, PUT, DELETE e uma regra 409. O código que ela escreveu compilou e passou nos 14 testes na primeira tentativa, sem nenhuma correção.

Os trechos usam `Reserva` como exemplo. Troque pelo nome da sua entidade.

### A) `GET /rota/{id}` – buscar um item (404 se não existir)

`ReservaService.java`:

```java
public Reserva buscar(Long id) {
    return repository.findById(id).orElseThrow(() -> new NaoEncontradoException("Reserva não encontrada"));
}
```

`ReservaController.java` (import `org.springframework.web.bind.annotation.PathVariable`):

```java
@GetMapping("/{id}")
public Reserva buscar(@PathVariable Long id) {
    return service.buscar(id);
}
```

O `SecurityConfig` já libera `GET ROTA/**` para ADMIN e USER.

### B) `DELETE /rota/{id}` – excluir (204, ou 404 se não existir)

`ReservaService.java`:

```java
public void excluir(Long id) {
    if (!repository.existsById(id)) {
        throw new NaoEncontradoException("Reserva não encontrada"); // 404
    }
    repository.deleteById(id);
}
```

`ReservaController.java` (imports `DeleteMapping` e `PathVariable`):

```java
@DeleteMapping("/{id}")
@ResponseStatus(HttpStatus.NO_CONTENT)
public void excluir(@PathVariable Long id) {
    service.excluir(id);
}
```

`SecurityConfig.java`, junto das outras `requestMatchers`:

```java
.requestMatchers(HttpMethod.DELETE, ROTA + "/**").hasRole("ADMIN")
```

**Front:** em `campos.js`, `ACOES.excluir: true`. O botão "Excluir" aparece em cada item para ADMIN e já chama `DELETE ROTA/{id}`.

### C) `PUT /rota/{id}` – editar

Na **Entity**, um método que troca os dados editáveis:

```java
public void atualizar(String sala, LocalDate data, LocalTime horaInicio, LocalTime horaFim, String motivo) {
    this.sala = sala;
    this.data = data;
    this.horaInicio = horaInicio;
    this.horaFim = horaFim;
    this.motivo = motivo;
}
```

`ReservaService.java`, usando o `buscar` da receita A:

```java
public Reserva atualizar(Long id, ReservaRequest request) {
    Reserva reserva = buscar(id); // 404 se não existir
    if (!request.horaFim().isAfter(request.horaInicio())) {
        throw new RegraDeNegocioException("A hora de fim deve ser depois da hora de início");
    }
    reserva.atualizar(request.sala().trim(), request.data(), request.horaInicio(), request.horaFim(), request.motivo().trim());
    return repository.save(reserva);
}
```

`ReservaController.java` (import `org.springframework.web.bind.annotation.PutMapping`):

```java
@PutMapping("/{id}")
public Reserva atualizar(@PathVariable Long id, @Valid @RequestBody ReservaRequest request) {
    return service.atualizar(id, request);
}
```

`SecurityConfig.java`:

```java
.requestMatchers(HttpMethod.PUT, ROTA + "/**").hasRole("ADMIN")
```

O front já tem a função `atualizar(token, id, dados)` em `api.js`, mas **não** tem tela de edição. Se o enunciado exigir editar pelo front, peça à IA um botão "Editar" no componente `Lista` do `App.jsx`, que preenche o `Formulario` e chama `atualizar`. O código completo do `App.jsx` está no fim deste README. Para demonstrar só a API, use o `curl.exe` da seção de testes.

### D) Filtro por parâmetro – `GET /rota?sala=Sala 1`

`ReservaRepository.java` (o Spring Data gera a consulta a partir do nome do método):

```java
List<Reserva> findBySalaIgnoreCaseOrderByDataAscHoraInicioAsc(String sala);
```

`ReservaService.java`:

```java
public List<Reserva> listarFiltrando(String sala) {
    return (sala == null || sala.isBlank()) ? listar() : repository.findBySalaIgnoreCaseOrderByDataAscHoraInicioAsc(sala.trim());
}
```

`ReservaController.java` (import `org.springframework.web.bind.annotation.RequestParam`). Substitua o `listar()`:

```java
@GetMapping
public List<Reserva> listar(@RequestParam(required = false) String sala) {
    return service.listarFiltrando(sala);
}
```

Mais exemplos de nomes de método que o Spring Data entende:
- `findByStatus(Status status)`
- `findByDataBetween(LocalDate inicio, LocalDate fim)`
- `findByValorGreaterThanEqual(Integer v)`
- `existsByEmailIgnoreCase(String email)`
- `countBySala(String sala)`
- `findByTituloContainingIgnoreCase(String trecho)`

### E) USER vê só os próprios itens (ADMIN vê todos)

`ReservaRepository.java`:

```java
List<Reserva> findByEmailResponsavelOrderByDataAscHoraInicioAsc(String emailResponsavel);
```

`ReservaService.java` (import `java.util.Locale`):

```java
public List<Reserva> listarDoUsuario(String email) {
    if (email == null || email.isBlank()) {
        return List.of(); // token sem email: não mostra nada
    }
    return repository.findByEmailResponsavelOrderByDataAscHoraInicioAsc(email.trim().toLowerCase(Locale.ROOT));
}
```

`ReservaController.java` (import `org.springframework.security.core.Authentication`). Substitua o `listar()`:

```java
@GetMapping
public List<Reserva> listar(Authentication auth, @AuthenticationPrincipal Jwt jwt) {
    boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    if (isAdmin) {
        return service.listar();
    }
    return service.listarDoUsuario(jwt.getClaimAsString(emailClaim));
}
```

### F) Regra de negócio com 409 (duplicidade ou conflito)

No Service, antes de salvar:

```java
if (repository.existsByEmailIgnoreCase(request.email())) {
    throw new ConflitoException("Já existe um cadastro com esse email"); // 409
}
```

Precisa do método `boolean existsByEmailIgnoreCase(String email);` no Repository. A sobreposição de horários está no exemplo de Reservas.

### G) Mudar permissões

Tudo fica no `SecurityConfig.java`, uma linha por método:

| Enunciado | Linha |
| --- | --- |
| só ADMIN | `.requestMatchers(HttpMethod.POST, ROTA, ROTA + "/**").hasRole("ADMIN")` |
| ADMIN e USER | `.requestMatchers(HttpMethod.POST, ROTA, ROTA + "/**").hasAnyRole("ADMIN", "USER")` |
| qualquer usuário logado | `.requestMatchers(HttpMethod.GET, ROTA, ROTA + "/**").authenticated()` |

**Se o USER também puder cadastrar,** mostre o formulário para ele no front. Em `frontend/src/App.jsx`, troque estas **duas** linhas, logo antes de `<Formulario`:

```jsx
          <div className={isAdmin ? 'grid' : ''}>
            {isAdmin && (
```

por:

```jsx
          <div className="grid">
            {(isAdmin || roles.includes('USER')) && (
```

A primeira linha põe formulário e lista lado a lado; a segunda mostra o formulário também para USER. Essa mudança foi testada numa simulação com o enunciado "Help Desk".

### H) Role nova (ex.: `GERENTE`)

1. No Auth0, em *User Management → Roles → Create Role*, crie `GERENTE` e atribua aos usuários. A Action já coloca **todas** as roles no token, então não precisa mudar nada nela.
2. No `SecurityConfig`, use `.hasRole("GERENTE")` ou `.hasAnyRole("ADMIN", "GERENTE")`.
3. No `App.jsx`, para quem só tem `GERENTE` ver a lista, troque a linha `const temAcesso = isAdmin || roles.includes('USER')` por:

   ```jsx
     const temAcesso = isAdmin || roles.includes('USER') || roles.includes('GERENTE')
   ```

### I) Dois recursos (ex.: `/salas` e `/reservas`)

No back-end, repita o molde (Entity, Request, Repository, Service e Controller) para cada recurso, cada um com a sua `ROTA`. No `SecurityConfig`, crie as regras dos dois (ex.: `private static final String ROTA_SALAS = SalaController.ROTA;` e linhas `requestMatchers` para `ROTA_SALAS`).

O front genérico mostra **um** recurso. Aponte `ROTA` e `campos.js` para o principal e demonstre o outro via `curl.exe`. Se o enunciado exigir os dois no front, peça à IA para generalizar `App.jsx` com base no código completo no fim deste README.

---

# Exemplo completo resolvido: Reservas de Salas

**Enunciado de exemplo:**
- Rotas:
  - `POST /reservas`: cadastrar (ADMIN);
  - `GET /reservas`: listar (ADMIN e USER);
  - `DELETE /reservas/{id}`: cancelar (ADMIN).
- Entidade Reserva: id, email do responsável (usuário logado), sala, data, hora de início, hora de fim e motivo.
- Regras:
  - hora de fim depois da de início (400);
  - não pode haver duas reservas sobrepostas da mesma sala no mesmo dia (409).
- Front: lista, cadastra e exclui.

> Este código foi **compilado e testado** a partir deste projeto base. Os 7 testes passaram: 401, 403, 201, 200, 400, 409, 204 e 404.

**Arquivos apagados:** `Registro.java`, `RegistroRequest.java`, `RegistroRepository.java`, `RegistroService.java`, `RegistroController.java` e `RegistroControllerTest.java`.

### `backend/src/main/java/br/edu/insper/provafinalagil/Reserva.java` (novo)
```java
package br.edu.insper.provafinalagil;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
public class Reserva {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String emailResponsavel;

    @Column(nullable = false, length = 100)
    private String sala;

    @Column(nullable = false)
    private LocalDate data;

    @Column(nullable = false)
    private LocalTime horaInicio;

    @Column(nullable = false)
    private LocalTime horaFim;

    @Column(nullable = false, length = 500)
    private String motivo;

    protected Reserva() { }

    public Reserva(String emailResponsavel, String sala, LocalDate data, LocalTime horaInicio, LocalTime horaFim, String motivo) {
        this.emailResponsavel = emailResponsavel;
        this.sala = sala;
        this.data = data;
        this.horaInicio = horaInicio;
        this.horaFim = horaFim;
        this.motivo = motivo;
    }

    public Long getId() { return id; }
    public String getEmailResponsavel() { return emailResponsavel; }
    public String getSala() { return sala; }
    public LocalDate getData() { return data; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public LocalTime getHoraFim() { return horaFim; }
    public String getMotivo() { return motivo; }
}
```

### `ReservaRequest.java` (novo)
```java
package br.edu.insper.provafinalagil;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;

public record ReservaRequest(
        @Email(message = "deve ser um email válido") String emailResponsavel,
        @NotBlank(message = "é obrigatória") @Size(max = 100, message = "deve ter no máximo 100 caracteres") String sala,
        @NotNull(message = "é obrigatória") LocalDate data,
        @NotNull(message = "é obrigatória") LocalTime horaInicio,
        @NotNull(message = "é obrigatória") LocalTime horaFim,
        @NotBlank(message = "é obrigatório") @Size(max = 500, message = "deve ter no máximo 500 caracteres") String motivo) {
}
```

### `ReservaRepository.java` (novo)
```java
package br.edu.insper.provafinalagil;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    List<Reserva> findAllByOrderByDataAscHoraInicioAsc();

    // Sobreposição de intervalos: inicioExistente < fimNovo E fimExistente > inicioNovo
    boolean existsBySalaIgnoreCaseAndDataAndHoraInicioLessThanAndHoraFimGreaterThan(
            String sala, LocalDate data, LocalTime fimNovo, LocalTime inicioNovo);
}
```

### `ReservaService.java` (novo)
```java
package br.edu.insper.provafinalagil;

import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class ReservaService {
    private final ReservaRepository repository;

    public ReservaService(ReservaRepository repository) {
        this.repository = repository;
    }

    public Reserva cadastrar(ReservaRequest request, String emailDoToken) {
        String email = temTexto(emailDoToken) ? emailDoToken : request.emailResponsavel();
        if (!temTexto(email)) {
            throw new RegraDeNegocioException("Não foi possível identificar o email do usuário");
        }
        if (!request.horaFim().isAfter(request.horaInicio())) {
            throw new RegraDeNegocioException("A hora de fim deve ser depois da hora de início"); // 400
        }
        String sala = request.sala().trim();
        boolean sobrepoe = repository.existsBySalaIgnoreCaseAndDataAndHoraInicioLessThanAndHoraFimGreaterThan(
                sala, request.data(), request.horaFim(), request.horaInicio());
        if (sobrepoe) {
            throw new ConflitoException("Já existe uma reserva dessa sala nesse horário"); // 409
        }
        Reserva reserva = new Reserva(email.trim().toLowerCase(Locale.ROOT), sala, request.data(),
                request.horaInicio(), request.horaFim(), request.motivo().trim());
        return repository.save(reserva);
    }

    public List<Reserva> listar() {
        return repository.findAllByOrderByDataAscHoraInicioAsc();
    }

    public void excluir(Long id) {
        if (!repository.existsById(id)) {
            throw new NaoEncontradoException("Reserva não encontrada"); // 404
        }
        repository.deleteById(id);
    }

    private static boolean temTexto(String valor) {
        return valor != null && !valor.isBlank();
    }
}
```

### `ReservaController.java` (novo)
```java
package br.edu.insper.provafinalagil;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ReservaController.ROTA)
public class ReservaController {
    public static final String ROTA = "/reservas";

    private final ReservaService service;
    private final String emailClaim;

    public ReservaController(ReservaService service, @Value("${app.auth0.email-claim}") String emailClaim) {
        this.service = service;
        this.emailClaim = emailClaim;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Reserva cadastrar(@Valid @RequestBody ReservaRequest request, @AuthenticationPrincipal Jwt jwt) {
        String emailDoToken = jwt != null ? jwt.getClaimAsString(emailClaim) : null;
        return service.cadastrar(request, emailDoToken);
    }

    @GetMapping
    public List<Reserva> listar() {
        return service.listar();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }
}
```

### `SecurityConfig.java` (alterado: `ROTA` aponta para `ReservaController` e há uma linha nova de DELETE)
```java
package br.edu.insper.provafinalagil;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {
    private static final String ROTA = ReservaController.ROTA;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, @Value("${app.auth0.roles-claim}") String rolesClaim) throws Exception {
        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(new Auth0RolesConverter(rolesClaim));

        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Regras por rota e método. Para uma rota nova, copie uma linha e ajuste.
                        // Ex.: .requestMatchers(HttpMethod.DELETE, ROTA + "/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, ROTA, ROTA + "/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, ROTA, ROTA + "/**").hasAnyRole("ADMIN", "USER")
                        .requestMatchers(HttpMethod.DELETE, ROTA + "/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtConverter)));
        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(@Value("${app.cors.allowed-origins}") List<String> allowedOrigins) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
```

### `backend/src/test/java/br/edu/insper/provafinalagil/ReservaControllerTest.java` (novo)
```java
package br.edu.insper.provafinalagil;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ReservaControllerTest {
    private static final String ROTA = ReservaController.ROTA;
    private static final String NS = "https://prova-final-agil";
    private static final Auth0RolesConverter ROLES = new Auth0RolesConverter(NS + "/roles");

    @Autowired private MockMvc mockMvc;
    @Autowired private ReservaRepository repository;

    @BeforeEach
    void limparBanco() {
        repository.deleteAll();
    }

    private static JwtRequestPostProcessor token(String email, String... roles) {
        return jwt()
                .jwt(j -> j.subject("auth0|" + email).claim(NS + "/roles", List.of(roles)).claim(NS + "/email", email))
                .authorities(ROLES);
    }

    private static String corpo(String sala, String inicio, String fim) {
        return """
                {"sala": "%s", "data": "2030-05-10", "horaInicio": "%s", "horaFim": "%s", "motivo": "Reunião"}
                """.formatted(sala, inicio, fim);
    }

    private long cadastrar(String json) throws Exception {
        String resposta = mockMvc.perform(post(ROTA).with(token("admin@empresa.com", "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return Long.parseLong(resposta.replaceAll(".*\"id\":(\\d+).*", "$1"));
    }

    @Test
    void semTokenRetorna401() throws Exception {
        mockMvc.perform(get(ROTA)).andExpect(status().isUnauthorized());
    }

    @Test
    void userNaoCadastraNemExclui() throws Exception {
        long id = cadastrar(corpo("Sala 1", "09:00", "10:00"));
        mockMvc.perform(post(ROTA).with(token("user@empresa.com", "USER"))
                        .contentType(MediaType.APPLICATION_JSON).content(corpo("Sala 2", "09:00", "10:00")))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete(ROTA + "/" + id).with(token("user@empresa.com", "USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCadastraEUserLista() throws Exception {
        cadastrar(corpo("Sala 1", "09:00", "10:00"));
        mockMvc.perform(get(ROTA).with(token("user@empresa.com", "USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].emailResponsavel").value("admin@empresa.com"))
                .andExpect(jsonPath("$[0].sala").value("Sala 1"));
    }

    @Test
    void fimAntesDoInicioRetorna400() throws Exception {
        mockMvc.perform(post(ROTA).with(token("admin@empresa.com", "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON).content(corpo("Sala 1", "10:00", "09:00")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("A hora de fim deve ser depois da hora de início"));
    }

    @Test
    void camposObrigatoriosRetornam400() throws Exception {
        mockMvc.perform(post(ROTA).with(token("admin@empresa.com", "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.sala").exists())
                .andExpect(jsonPath("$.campos.horaInicio").exists());
    }

    @Test
    void sobreposicaoRetorna409EHorarioEncostadoPode() throws Exception {
        cadastrar(corpo("Sala 1", "09:00", "10:00"));
        mockMvc.perform(post(ROTA).with(token("admin@empresa.com", "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON).content(corpo("sala 1", "09:30", "10:30")))
                .andExpect(status().isConflict());
        cadastrar(corpo("Sala 1", "10:00", "11:00")); // encostada: permitido
        cadastrar(corpo("Sala 2", "09:00", "10:00")); // outra sala: permitido
    }

    @Test
    void adminExcluiEIdInexistenteRetorna404() throws Exception {
        long id = cadastrar(corpo("Sala 1", "09:00", "10:00"));
        mockMvc.perform(delete(ROTA + "/" + id).with(token("admin@empresa.com", "ADMIN")))
                .andExpect(status().isNoContent());
        mockMvc.perform(delete(ROTA + "/" + id).with(token("admin@empresa.com", "ADMIN")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Reserva não encontrada"));
    }
}
```

### `frontend/src/campos.js` (alterado)
```js
export const hoje = () => new Date().toLocaleDateString('sv-SE')

export const RECURSO = {
  titulo: 'Reservas de Salas',
  singular: 'reserva',
  plural: 'reservas',
  campoEmailUsuario: 'emailResponsavel',
}

export const ACOES = {
  excluir: true,
}

export const CAMPOS = [
  { nome: 'sala', rotulo: 'Sala', tipo: 'text', obrigatorio: true, maxLength: 100, placeholder: 'Ex.: Sala 1' },
  { nome: 'data', rotulo: 'Data', tipo: 'date', obrigatorio: true, padrao: hoje },
  { nome: 'horaInicio', rotulo: 'Hora de início', tipo: 'time', obrigatorio: true },
  { nome: 'horaFim', rotulo: 'Hora de fim', tipo: 'time', obrigatorio: true },
  { nome: 'motivo', rotulo: 'Motivo', tipo: 'textarea', obrigatorio: true, maxLength: 500 },
]
```

### `frontend/src/api.js` (alterado: só a linha da `ROTA`)

```js
export const ROTA = '/reservas'
```

---

# Testar, publicar e conferir

### Rodar os testes no computador (antes do push)

```powershell
cd "C:\Users\oscar\Documents\insper\Gestão Ágil\prova-final-agil\backend"
.\mvnw.cmd verify
```

Tem que terminar com `BUILD SUCCESS`. O `mvnw.cmd` baixa o Maven sozinho. Os testes usam H2 em memória e tokens simulados, então não precisam de banco nem de Auth0.

### Publicar (commit direto na main)

```powershell
cd "C:\Users\oscar\Documents\insper\Gestão Ágil\prova-final-agil"
git add -A
git commit -m "feat: adapta o projeto para a prova"
git push origin main
```

- **Back-end:** o push na `main` que mexe em `backend/` dispara **testes → imagem Docker → deploy na EC2 → verificação**, em 2 a 3 minutos. Acompanhe com `gh run watch -R OscarRodriguesFilho/prova-final-agil`.
- **Front-end:** a Vercel está ligada ao repositório e publica sozinha em cerca de 30s. Para publicar sem commit, rode `npx vercel --prod` na **raiz**.
- **Forçar** o deploy do back-end: `gh workflow run backend.yml -R OscarRodriguesFilho/prova-final-agil`.
- **Ver por que o workflow falhou:** `gh run view --log-failed -R OscarRodriguesFilho/prova-final-agil`.

### Conferir

```powershell
# sem token: 401 (troque a rota)
curl.exe -i http://ec2-44-197-175-105.compute-1.amazonaws.com:8080/prova-final-agil
curl.exe -i https://prova-final-agil.vercel.app/api/prova-final-agil
```

### Testar a API com o token de um usuário (curl)

1. Entre no front (https://prova-final-agil.vercel.app) com o usuário desejado.
2. Aperte **F12 → Network**, clique em **Atualizar** na tela e selecione a requisição para a sua rota.
3. Em *Request Headers*, copie o valor de `authorization` sem a palavra `Bearer `.

Não use o token da aba *Test* da API no Auth0: ele não tem roles nem email e dá 403.

```powershell
$TOKEN = "COLE_O_TOKEN_AQUI"
$API = "https://prova-final-agil.vercel.app/api/reservas"   # troque a rota
curl.exe -i -H "Authorization: Bearer $TOKEN" $API
# POST com o JSON num arquivo (evita problema de aspas no PowerShell)
Set-Content -Path corpo.json -Encoding ascii -Value '{"sala":"Sala 1","data":"2030-05-10","horaInicio":"09:00","horaFim":"10:00","motivo":"Daily"}'
curl.exe -i -X POST -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" --data-binary "@corpo.json" $API
curl.exe -i -X DELETE -H "Authorization: Bearer $TOKEN" "$API/1"
```

### Logs da API e banco na EC2

```powershell
# logs
ssh -i "C:\Users\oscar\Documents\insper\agil\.credencias\projsoft26b.pem" ubuntu@ec2-44-197-175-105.compute-1.amazonaws.com "sudo docker logs --tail 100 prova-final-agil-api"
# zerar o banco (apaga TODOS os dados)
ssh -i "C:\Users\oscar\Documents\insper\agil\.credencias\projsoft26b.pem" ubuntu@ec2-44-197-175-105.compute-1.amazonaws.com "cd /home/ubuntu/prova-final-agil && sudo docker compose down -v && sudo docker compose up -d --no-build"
```

---

# Problemas comuns

| Sintoma | Causa e solução |
| --- | --- |
| Workflow vermelho em **Testes** | O código não compila ou um teste falhou. Rode `.\mvnw.cmd verify` no computador e leia o primeiro `ERROR`. |
| `cannot find symbol: RegistroController` | O `SecurityConfig` ainda aponta para a classe apagada. Troque por `XxxController.ROTA`. |
| `package org.springframework.boot.test.autoconfigure.web.servlet does not exist` | Import do Boot 3. Use `org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc`. |
| `500` no POST depois de trocar campos | Manteve o nome `Registro` e o banco tem colunas antigas. Renomeie a entidade ou zere o banco. |
| Front mostra "Sem permissão" | Usuário sem role. Atribua no Auth0 e clique em **Já recebi uma role** / **Atualizar**. |
| `403` com ADMIN | Token antigo, sem a role. Clique em **Atualizar** no front. Se não resolver, confira a linha do `SecurityConfig`. |
| `401` mesmo logado | Token de outro audience. O front precisa de `VITE_AUTH0_AUDIENCE=https://prova-final-agil`. |
| Formulário dá "Dados inválidos" num campo que foi preenchido | O `nome` no `campos.js` está diferente do nome no Request. |
| Botão "Excluir" não aparece | Falta `ACOES.excluir: true` no `campos.js`, ou o usuário não é ADMIN. |
| Excluir dá 403/405 | Falta a linha DELETE no `SecurityConfig` ou o `@DeleteMapping` no controller. |
| Deploy do back-end não rodou | O push não mexeu em `backend/`. Rode `gh workflow run backend.yml -R OscarRodriguesFilho/prova-final-agil`. |
| Front com "Configuração do Auth0 ausente" | Faltam as variáveis `VITE_*` na Vercel. |

---

# Como tudo está configurado

**Auth0 (já feito):**
- API `https://prova-final-agil` (RS256, offline access).
- Aplicação SPA `Prova Final Agil Front`, com callback, logout e web origins `https://prova-final-agil.vercel.app` e `http://localhost:5173`, e com **API Access** concedido na API.
- Roles `ADMIN` e `USER`.
- Action **"Roles no token (prova-final-agil)"** no fluxo Post Login:

```js
exports.onExecutePostLogin = async (event, api) => {
  const namespace = 'https://prova-final-agil';
  const roles = (event.authorization && event.authorization.roles) || [];
  api.accessToken.setCustomClaim(namespace + '/roles', roles);
  api.idToken.setCustomClaim(namespace + '/roles', roles);
  if (event.user.email) {
    api.accessToken.setCustomClaim(namespace + '/email', event.user.email);
  }
};
```

**Dar role a alguém:** *User Management → Users →* usuário *→ Roles → Assign Roles*. A pessoa precisa ter feito login uma vez. Depois ela clica em **Atualizar** no front.

**GitHub Actions**, em *Settings → Secrets and variables → Actions*:

| Tipo | Nome | Valor |
| --- | --- | --- |
| Secret | `EC2_HOST` | `ec2-44-197-175-105.compute-1.amazonaws.com` |
| Secret | `EC2_SSH_KEY` | conteúdo do `.pem` |
| Secret | `DB_PASSWORD` | senha aleatória do PostgreSQL |
| Variable | `AUTH0_DOMAIN` | `dev-luk1185ok6ie6gvy.us.auth0.com` |
| Variable | `AUTH0_AUDIENCE` | `https://prova-final-agil` |
| Variable | `AUTH0_CLAIMS_NAMESPACE` | `https://prova-final-agil` |
| Variable | `CORS_ALLOWED_ORIGINS` | `https://prova-final-agil.vercel.app,http://localhost:5173` |

**Workflow `.github/workflows/backend.yml`:**
1. **Testes** (`mvn verify`).
2. **Build** da imagem, publicada em `ghcr.io/oscarrodriguesfilho/prova-final-agil/api`.
3. **Deploy:** copia o `docker-compose.yml` para `/home/ubuntu/prova-final-agil`, escreve o `.env`, para outros containers que usem a porta 8080 e faz `docker compose up -d`.
4. **Verificação:** espera `401` em `/`, o que funciona com qualquer nome de rota.

**EC2:** só a porta **8080** está aberta. O PostgreSQL usa um volume próprio, então os dados sobrevivem aos redeploys.

**Vercel:**
- Projeto `prova-final-agil`, ligado ao GitHub: push na `main` gera deploy de produção.
- O `vercel.json` da raiz builda `frontend/`, publica `frontend/dist` e faz o rewrite `/api/*` → EC2.
- Variáveis de produção: `VITE_AUTH0_DOMAIN`, `VITE_AUTH0_CLIENT_ID` e `VITE_AUTH0_AUDIENCE`. Elas são lidas no build: depois de mudar alguma, publique de novo.

**Fluxo:** navegador → login no Auth0 (token com roles e email) → front na Vercel → `/api/*` (rewrite) → API na EC2 :8080 → PostgreSQL.

---

# Código atual completo (antes da prova)

Este é o código do projeto base como ele está agora. As instruções e receitas acima partem dele.

### `backend/src/main/java/br/edu/insper/provafinalagil/Registro.java`
```java
package br.edu.insper.provafinalagil;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDate;

/**
 * Entidade GENÉRICA de exemplo. Na prova, renomeie a classe e troque os campos
 * pelos do enunciado. Cada campo abaixo é um modelo de um tipo comum:
 * - emailCriador: email do usuário logado (vem do token do Auth0)
 * - titulo: texto curto obrigatório
 * - descricao: texto longo obrigatório
 * - valor: número inteiro com faixa (0 a 10)
 * - data: data (se não vier no corpo, usa hoje)
 */
@Entity
public class Registro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String emailCriador;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false, length = 2000)
    private String descricao;

    @Column(nullable = false)
    private Integer valor;

    @Column(nullable = false)
    private LocalDate data;

    protected Registro() { }

    public Registro(String emailCriador, String titulo, String descricao, Integer valor, LocalDate data) {
        this.emailCriador = emailCriador;
        this.titulo = titulo;
        this.descricao = descricao;
        this.valor = valor;
        this.data = data;
    }

    public Long getId() { return id; }
    public String getEmailCriador() { return emailCriador; }
    public String getTitulo() { return titulo; }
    public String getDescricao() { return descricao; }
    public Integer getValor() { return valor; }
    public LocalDate getData() { return data; }
}
```

### `RegistroRequest.java`
```java
package br.edu.insper.provafinalagil;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * Corpo do POST. As anotações de validação geram 400 automaticamente (ver ApiExceptionHandler).
 * Outras úteis: @Positive, @PastOrPresent, @Future, @Pattern(regexp = "..."), @DecimalMin("0.0").
 *
 * emailCriador é opcional: quando o token traz o email do usuário logado, ele tem prioridade.
 * data é opcional: quando ausente, a API usa a data de hoje.
 */
public record RegistroRequest(
        @Email(message = "deve ser um email válido") String emailCriador,
        @NotBlank(message = "é obrigatório") @Size(max = 200, message = "deve ter no máximo 200 caracteres") String titulo,
        @NotBlank(message = "é obrigatória") @Size(max = 2000, message = "deve ter no máximo 2000 caracteres") String descricao,
        @NotNull(message = "é obrigatório") @Min(value = 0, message = "deve estar entre 0 e 10") @Max(value = 10, message = "deve estar entre 0 e 10") Integer valor,
        LocalDate data) {
}
```

### `RegistroRepository.java`
```java
package br.edu.insper.provafinalagil;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistroRepository extends JpaRepository<Registro, Long> {
    List<Registro> findAllByOrderByDataDescIdDesc();
}
```

### `RegistroService.java`
```java
package br.edu.insper.provafinalagil;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class RegistroService {
    private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");

    private final RegistroRepository repository;

    public RegistroService(RegistroRepository repository) {
        this.repository = repository;
    }

    public Registro cadastrar(RegistroRequest request, String emailDoToken) {
        String emailCriador = temTexto(emailDoToken) ? emailDoToken : request.emailCriador();
        if (!temTexto(emailCriador)) {
            throw new RegraDeNegocioException("Não foi possível identificar o email do usuário");
        }

        // Exemplo de regra de negócio (devolve 400 com a mensagem):
        // if (request.valor() < 5 && request.descricao().length() < 20) {
        //     throw new RegraDeNegocioException("Valores abaixo de 5 precisam de uma descrição detalhada");
        // }

        LocalDate data = request.data() != null ? request.data() : LocalDate.now(FUSO);
        Registro registro = new Registro(
                emailCriador.trim().toLowerCase(Locale.ROOT),
                request.titulo().trim(),
                request.descricao().trim(),
                request.valor(),
                data);
        return repository.save(registro);
    }

    public List<Registro> listar() {
        return repository.findAllByOrderByDataDescIdDesc();
    }

    private static boolean temTexto(String valor) {
        return valor != null && !valor.isBlank();
    }
}
```

### `RegistroController.java`
```java
package br.edu.insper.provafinalagil;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(RegistroController.ROTA)
public class RegistroController {
    /** Nome da rota. Trocar aqui muda o controller E as regras de segurança (SecurityConfig usa esta constante). */
    public static final String ROTA = "/prova-final-agil";

    private final RegistroService service;
    private final String emailClaim;

    public RegistroController(RegistroService service, @Value("${app.auth0.email-claim}") String emailClaim) {
        this.service = service;
        this.emailClaim = emailClaim;
    }

    /** POST /prova-final-agil: somente ADMIN (regra em SecurityConfig). */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Registro cadastrar(@Valid @RequestBody RegistroRequest request, @AuthenticationPrincipal Jwt jwt) {
        String emailDoToken = jwt != null ? jwt.getClaimAsString(emailClaim) : null;
        return service.cadastrar(request, emailDoToken);
    }

    /** GET /prova-final-agil: ADMIN e USER (regra em SecurityConfig). */
    @GetMapping
    public List<Registro> listar() {
        return service.listar();
    }
}
```

### `SecurityConfig.java`
```java
package br.edu.insper.provafinalagil;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {
    private static final String ROTA = RegistroController.ROTA;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, @Value("${app.auth0.roles-claim}") String rolesClaim) throws Exception {
        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(new Auth0RolesConverter(rolesClaim));

        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Regras por rota e método. Para uma rota nova, copie uma linha e ajuste.
                        // Ex.: .requestMatchers(HttpMethod.DELETE, ROTA + "/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, ROTA, ROTA + "/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, ROTA, ROTA + "/**").hasAnyRole("ADMIN", "USER")
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtConverter)));
        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(@Value("${app.cors.allowed-origins}") List<String> allowedOrigins) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
```

### `ApiExceptionHandler.java` (não alterar)
```java
package br.edu.insper.provafinalagil;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    Map<String, Object> validacao(MethodArgumentNotValidException exception) {
        Map<String, String> campos = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors()
                .forEach(erro -> campos.putIfAbsent(erro.getField(), erro.getDefaultMessage()));
        return Map.of("error", "Dados inválidos", "campos", campos);
    }

    @ExceptionHandler(RegraDeNegocioException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    Map<String, String> regraDeNegocio(RegraDeNegocioException exception) {
        return Map.of("error", exception.getMessage());
    }

    @ExceptionHandler(ConflitoException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    Map<String, String> conflito(ConflitoException exception) {
        return Map.of("error", exception.getMessage());
    }

    @ExceptionHandler(NaoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    Map<String, String> naoEncontrado(NaoEncontradoException exception) {
        return Map.of("error", exception.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    Map<String, String> corpoInvalido(HttpMessageNotReadableException exception) {
        return Map.of("error", "Corpo da requisição inválido");
    }
}
```

### `RegraDeNegocioException.java`, `ConflitoException.java`, `NaoEncontradoException.java` (não alterar)
```java
package br.edu.insper.provafinalagil;

/** Lance esta exceção no Service para devolver 400 com uma mensagem própria. */
public class RegraDeNegocioException extends RuntimeException {
    public RegraDeNegocioException(String message) {
        super(message);
    }
}
```
```java
package br.edu.insper.provafinalagil;

/** Lance esta exceção no Service para devolver 409 (conflito, ex.: horário já ocupado, item duplicado). */
public class ConflitoException extends RuntimeException {
    public ConflitoException(String message) {
        super(message);
    }
}
```
```java
package br.edu.insper.provafinalagil;

/** Lance esta exceção no Service para devolver 404 (ex.: buscar/excluir um id que não existe). */
public class NaoEncontradoException extends RuntimeException {
    public NaoEncontradoException(String message) {
        super(message);
    }
}
```

### `Auth0RolesConverter.java` (não alterar)
```java
package br.edu.insper.provafinalagil;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Lê a claim de roles que a Action do Auth0 coloca no access token
 * (ex.: "https://prova-final-agil/roles": ["ADMIN"]) e transforma em ROLE_ADMIN / ROLE_USER.
 */
public class Auth0RolesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
    private final String rolesClaim;

    public Auth0RolesConverter(String rolesClaim) {
        this.rolesClaim = rolesClaim;
    }

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList(rolesClaim);
        if (roles == null) {
            return List.of();
        }
        return roles.stream()
                .map(role -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + role.trim().toUpperCase(Locale.ROOT)))
                .toList();
    }
}
```

### `backend/src/main/resources/application.properties` (não alterar)
```properties
spring.application.name=prova-final-agil-api
server.port=8080

# Banco de dados (PostgreSQL)
spring.datasource.url=jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:provafinalagil}
spring.datasource.username=${DB_USER:provafinalagil_user}
spring.datasource.password=${DB_PASSWORD:provafinalagil_password}
# update: cria/atualiza as tabelas a partir das entidades (novos campos viram colunas novas)
spring.jpa.hibernate.ddl-auto=update
spring.jpa.open-in-view=false

# Auth0: valida assinatura, emissor (issuer) e audience do access token
spring.security.oauth2.resourceserver.jwt.issuer-uri=https://${AUTH0_DOMAIN}/
spring.security.oauth2.resourceserver.jwt.audiences=${AUTH0_AUDIENCE:https://prova-final-agil}

# Claims customizadas colocadas pela Action do Auth0
app.auth0.claims-namespace=${AUTH0_CLAIMS_NAMESPACE:https://prova-final-agil}
app.auth0.roles-claim=${app.auth0.claims-namespace}/roles
app.auth0.email-claim=${app.auth0.claims-namespace}/email

# Origens liberadas no CORS (separadas por virgula)
app.cors.allowed-origins=${CORS_ALLOWED_ORIGINS:http://localhost:5173}
```

### `backend/src/test/java/br/edu/insper/provafinalagil/RegistroControllerTest.java`
```java
package br.edu.insper.provafinalagil;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Testes de integração das rotas com tokens simulados (não precisa do Auth0 de verdade).
 * Ao trocar a entidade na prova, ajuste o CORPO_VALIDO e os jsonPath abaixo.
 */
@SpringBootTest
@AutoConfigureMockMvc
class RegistroControllerTest {
    private static final String ROTA = RegistroController.ROTA;
    private static final String NS = "https://prova-final-agil";
    private static final Auth0RolesConverter ROLES = new Auth0RolesConverter(NS + "/roles");

    private static final String CORPO_VALIDO = """
            {
              "titulo": "Primeiro registro",
              "descricao": "Descrição de exemplo",
              "valor": 9,
              "data": "2026-11-10"
            }
            """;

    @Autowired private MockMvc mockMvc;
    @Autowired private RegistroRepository repository;

    @BeforeEach
    void limparBanco() {
        repository.deleteAll();
    }

    /** Simula um access token do Auth0 com as claims que a Action adiciona. */
    private static JwtRequestPostProcessor token(String email, String... roles) {
        return jwt()
                .jwt(j -> j.subject("auth0|" + email).claim(NS + "/roles", List.of(roles)).claim(NS + "/email", email))
                .authorities(ROLES);
    }

    @Test
    void semTokenRetorna401() throws Exception {
        mockMvc.perform(get(ROTA)).andExpect(status().isUnauthorized());
        mockMvc.perform(post(ROTA).contentType(MediaType.APPLICATION_JSON).content(CORPO_VALIDO))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void userNaoPodeCadastrar() throws Exception {
        mockMvc.perform(post(ROTA).with(token("user@empresa.com", "USER"))
                        .contentType(MediaType.APPLICATION_JSON).content(CORPO_VALIDO))
                .andExpect(status().isForbidden());
    }

    @Test
    void tokenSemRoleNaoPodeListar() throws Exception {
        mockMvc.perform(get(ROTA).with(token("visitante@empresa.com")))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCadastraEUserLista() throws Exception {
        mockMvc.perform(post(ROTA).with(token("admin@empresa.com", "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON).content(CORPO_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.emailCriador").value("admin@empresa.com"))
                .andExpect(jsonPath("$.titulo").value("Primeiro registro"))
                .andExpect(jsonPath("$.valor").value(9))
                .andExpect(jsonPath("$.data").value("2026-11-10"));

        mockMvc.perform(get(ROTA).with(token("user@empresa.com", "USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].descricao").value("Descrição de exemplo"));
    }

    @Test
    void emailDoTokenTemPrioridadeEDataPadraoEHoje() throws Exception {
        mockMvc.perform(post(ROTA).with(token("admin@empresa.com", "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"emailCriador": "outra@empresa.com", "titulo": "Sem data",
                                 "descricao": "Teste", "valor": 7}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.emailCriador").value("admin@empresa.com"))
                .andExpect(jsonPath("$.data").isNotEmpty());
    }

    @Test
    void valorForaDoIntervaloRetorna400() throws Exception {
        mockMvc.perform(post(ROTA).with(token("admin@empresa.com", "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo": "X", "descricao": "Teste", "valor": 11}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.valor").value("deve estar entre 0 e 10"));
    }

    @Test
    void camposObrigatoriosRetornam400() throws Exception {
        mockMvc.perform(post(ROTA).with(token("admin@empresa.com", "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.titulo").exists())
                .andExpect(jsonPath("$.campos.descricao").exists())
                .andExpect(jsonPath("$.campos.valor").exists());
    }
}
```

### `frontend/src/campos.js`
```js
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
```

### `frontend/src/api.js`
```js
import { config } from './config.js'

/** Nome da rota no back-end (igual a RegistroController.ROTA). */
export const ROTA = '/prova-final-agil'

async function apiFetch(path, token, options = {}) {
  const resposta = await fetch(`${config.apiUrl}${path}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`,
      ...options.headers,
    },
  })

  const texto = await resposta.text()
  let dados = null
  try {
    dados = texto ? JSON.parse(texto) : null
  } catch {
    dados = null
  }

  if (!resposta.ok) {
    const erro = new Error(mensagemDeErro(resposta.status, dados))
    erro.status = resposta.status
    erro.campos = dados?.campos ?? {}
    throw erro
  }
  return dados
}

function mensagemDeErro(status, dados) {
  if (status === 401) return 'Token inválido ou expirado. Saia e faça login novamente.'
  if (status === 403) return 'Seu usuário não tem permissão para esta ação.'
  if (dados?.error) return dados.error
  if (status >= 500) return 'A API está indisponível no momento.'
  return `Erro inesperado (HTTP ${status}).`
}

export function listar(token) {
  return apiFetch(ROTA, token)
}

export function cadastrar(token, dados) {
  return apiFetch(ROTA, token, { method: 'POST', body: JSON.stringify(dados) })
}

/** DELETE ROTA/{id} — usado pelo botão "Excluir" (ligue ACOES.excluir no campos.js). Resposta 204 vira null. */
export function excluir(token, id) {
  return apiFetch(`${ROTA}/${id}`, token, { method: 'DELETE' })
}

/** PUT ROTA/{id} — pronto para quando o back-end tiver a rota de edição. */
export function atualizar(token, id, dados) {
  return apiFetch(`${ROTA}/${id}`, token, { method: 'PUT', body: JSON.stringify(dados) })
}

/** Lê as roles da claim customizada do access token (a mesma que a API usa). */
export function rolesDoToken(token) {
  try {
    const base64 = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')
    const payload = JSON.parse(atob(base64))
    const roles = payload[`${config.claimsNamespace}/roles`]
    return Array.isArray(roles) ? roles.map((r) => String(r).toUpperCase()) : []
  } catch {
    return []
  }
}
```

### `frontend/src/App.jsx` (normalmente não precisa alterar)
```jsx
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
```

### `vercel.json` (raiz; não alterar)
```json
{
  "$schema": "https://openapi.vercel.sh/vercel.json",
  "framework": null,
  "installCommand": "cd frontend && npm ci",
  "buildCommand": "cd frontend && npm run build",
  "outputDirectory": "frontend/dist",
  "rewrites": [
    {
      "source": "/api/:path*",
      "destination": "http://ec2-44-197-175-105.compute-1.amazonaws.com:8080/:path*"
    }
  ]
}
```
