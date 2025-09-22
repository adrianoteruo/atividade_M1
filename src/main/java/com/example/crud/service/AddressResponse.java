package com.example.crud.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 Record para mapear a resposta da API ViaCEP.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AddressResponse(
        String cep,
        String logradouro,
        String complemento,
        String bairro,
        String localidade, // Cidade
        String uf          // Estado
) {}