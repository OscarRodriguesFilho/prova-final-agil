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
