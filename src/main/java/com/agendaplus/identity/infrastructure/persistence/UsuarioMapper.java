package com.agendaplus.identity.infrastructure.persistence;

import com.agendaplus.identity.domain.model.Email;
import com.agendaplus.identity.domain.model.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface UsuarioMapper {
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    UsuarioJpaEntity toEntity(Usuario usuario);
    Usuario toDomain(UsuarioJpaEntity entity);
    default String map(Email email) { return email.valor(); }
    default Email map(String email) { return new Email(email); }
}
