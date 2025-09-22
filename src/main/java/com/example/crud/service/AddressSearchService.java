package com.example.crud.service;
import com.example.crud.domain.product.Product;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class AddressSearchService {

    @Autowired
    private RestTemplate restTemplate;

    private static final String VIA_CEP_URL = "https://viacep.com.br/ws";

    public AddressResponse searchByCep(String cep) {
        // Constrói a URL: https://viacep.com.br/ws/08773380/json/
        String url = UriComponentsBuilder.fromHttpUrl(VIA_CEP_URL)
                .pathSegment(cep, "json")
                .toUriString();

        System.out.println("Buscando na URL: " + url);

        // Faz a requisição GET e mapeia a resposta JSON no record AddressResponse
        return restTemplate.getForObject(url, AddressResponse.class);
    }


    public AddressResponse[] searchByAddress(String state, String city, String street) {

        String url = UriComponentsBuilder.fromHttpUrl(VIA_CEP_URL)
                .pathSegment(state, city, street, "json")
                .build(false)
                .toUriString();

        System.out.println("Buscando na URL: " + url);


        return restTemplate.getForObject(url, AddressResponse[].class);
    }
    public Boolean checkDistributionCenterAvailability(String cep, Product product) {

        AddressResponse address = this.searchByCep(cep);

        if (address == null || address.localidade() == null || product == null || product.getDistributionCenter() == null) {
            return false;
        }

        String destinationCity = address.localidade();
        String distributionCenter = product.getDistributionCenter();

        return destinationCity.equalsIgnoreCase(distributionCenter);
    }

}