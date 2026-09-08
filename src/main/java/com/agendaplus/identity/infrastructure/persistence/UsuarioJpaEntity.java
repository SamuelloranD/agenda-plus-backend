package com.agendaplus.identity.infrastructure.persistence;

import com.agendaplus.identity.domain.model.Role;
import jakarta.persistence.*;
import java.util.UUID;

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

    protected UsuarioJpaEntity() {}

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
}
