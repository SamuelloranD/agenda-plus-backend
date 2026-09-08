package com.agendaplus.identity.infrastructure.persistence;

import com.agendaplus.identity.domain.exception.EmailJaCadastradoException;
import com.agendaplus.identity.domain.model.Email;
import com.agendaplus.identity.domain.model.Usuario;
import com.agendaplus.identity.domain.repository.UsuarioRepository;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public class UsuarioRepositoryImpl implements UsuarioRepository {
    private final SpringDataUsuarioRepository repository;
    private final UsuarioMapper mapper;

    public UsuarioRepositoryImpl(SpringDataUsuarioRepository repository, UsuarioMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Optional<Usuario> buscarPorEmail(Email email) {
        return repository.findByEmail(email.valor()).map(mapper::toDomain);
    }

    @Override
    public Optional<Usuario> buscarPorId(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Usuario salvar(Usuario usuario) {
        try {
            return mapper.toDomain(repository.saveAndFlush(mapper.toEntity(usuario)));
        } catch (DataIntegrityViolationException exception) {
            // A constraint cobre cadastros concorrentes após a consulta do caso de uso.
            for (Throwable causa = exception; causa != null; causa = causa.getCause()) {
                if (causa instanceof ConstraintViolationException violacao
                        && "uk_usuarios_email".equals(violacao.getConstraintName())) {
                    throw new EmailJaCadastradoException();
                }
            }
            throw exception;
        }
    }
}
