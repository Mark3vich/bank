package com.example.bank.mapper;

import org.mapstruct.MappingTarget;

public interface Mappable <E, D> {
    E toEntity(D dto);
    D toDto(E entity);
    void updateEntityFromDto(D dto, @MappingTarget E entity);
}
