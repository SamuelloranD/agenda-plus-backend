package com.agendaplus.services.domain.model;

import com.agendaplus.shared.domain.model.Dinheiro;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;

@Entity
@Table(name = "servicos")
public class Servico {
    @Id
    private UUID id;
    private String nome;
    @Column(name = "duracao_minutos")
    private int duracaoMinutos;
    @Column(name = "preco_valor", precision = 19, scale = 2)
    private BigDecimal precoValor;
    @Column(name = "preco_moeda", length = 3)
    private String precoMoeda;

    protected Servico() {}

    public Servico(UUID id, String nome, int duracaoMinutos, Dinheiro preco) {
        this.id = id;
        atualizar(nome, duracaoMinutos, preco);
    }

    public void atualizar(String nome, int duracaoMinutos, Dinheiro preco) {
        if (nome == null || nome.isBlank() || nome.strip().length() > 150) {
            throw new IllegalArgumentException("Nome deve conter entre 1 e 150 caracteres.");
        }
        if (duracaoMinutos <= 0) throw new IllegalArgumentException("A duração deve ser positiva.");
        if (preco == null) throw new IllegalArgumentException("Preço é obrigatório.");
        this.nome = nome.strip();
        this.duracaoMinutos = duracaoMinutos;
        this.precoValor = preco.getValor();
        this.precoMoeda = preco.getMoeda().getCurrencyCode();
    }

    public UUID getId() { return id; }
    public String getNome() { return nome; }
    public int getDuracaoMinutos() { return duracaoMinutos; }
    public Dinheiro getPreco() { return new Dinheiro(precoValor, Currency.getInstance(precoMoeda)); }
}
