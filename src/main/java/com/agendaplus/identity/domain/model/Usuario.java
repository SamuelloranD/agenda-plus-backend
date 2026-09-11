package com.agendaplus.identity.domain.model;

import java.util.UUID;

public final class Usuario {
    private final UUID id;
    private final String nome;
    private final Email email;
    private final String senhaHash;
    private final Role role;

    public Usuario(UUID id, String nome, Email email, String senhaHash, Role role) {
        if (id == null || email == null || role == null) {
            throw new IllegalArgumentException("Id, email e role são obrigatórios.");
        }
        if (nome == null || nome.isBlank() || nome.strip().length() > 150) {
            throw new IllegalArgumentException("Nome deve conter entre 1 e 150 caracteres.");
        }
        if (senhaHash == null || senhaHash.isBlank()) {
            throw new IllegalArgumentException("Hash de senha obrigatório.");
        }
        this.id = id;
        this.nome = nome.strip();
        this.email = email;
        this.senhaHash = senhaHash;
        this.role = role;
    }

    public UUID getId() { return id; }
    public String getNome() { return nome; }
    public Email getEmail() { return email; }
    public String getSenhaHash() { return senhaHash; }
    public Role getRole() { return role; }

    public Usuario atualizarPerfil(String nome, Email email) {
        return new Usuario(id, nome, email, senhaHash, role);
    }
}
