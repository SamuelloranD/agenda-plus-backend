package com.agendaplus.identity.infrastructure.persistence;

import com.agendaplus.identity.domain.model.Role;
import jakarta.persistence.*;
import java.util.UUID;
import java.time.Instant;

@Entity
@Table(name = "usuarios")
public class UsuarioJpaEntity {
    @Id
    private UUID id;
    @Column(nullable = false, length = 150)
    private String nome;
    @Column(nullable = false, unique = true, length = 254)
    private String email;
    @Column(name = "senha_hash", nullable = false, length = 60)
    private String senhaHash;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected UsuarioJpaEntity() {}

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    void onUpdate() { updatedAt = Instant.now(); }

    public UsuarioJpaEntity(UUID id, String nome, String email, String senhaHash, Role role) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.senhaHash = senhaHash;
        this.role = role;
    }

    public UUID getId() { return id; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getSenhaHash() { return senhaHash; }
    public Role getRole() { return role; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void atualizar(String nome, String email, String senhaHash, Role role) {
        this.nome = nome; this.email = email; this.senhaHash = senhaHash; this.role = role;
    }
}
