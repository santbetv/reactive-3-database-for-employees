/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sbvdeveloper.employeereact.config.client;

import io.netty.channel.ChannelOption;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * @author Santiago Betancur Villegas
 */
@Component
public class ClientWebClient {

    private final WebClient.Builder webClientBuilder;
    //private final CustomerRepository customerRepository;

    @Value("${URL.PRODUCT}")
    private String urlProduct;

    @Value("${URL.TRANSACTION}")
    private String urlTransaction;

    @Autowired
    public ClientWebClient(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    //webClient requires HttpClient library to work propertly       
    HttpClient client = HttpClient.create()
            //Connection Timeout: is a period within which a connection between a client and a server must be established
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
            .option(ChannelOption.SO_KEEPALIVE, true)
            .option(EpollChannelOption.TCP_KEEPIDLE, 300)
            .option(EpollChannelOption.TCP_KEEPINTVL, 60)
            //Response Timeout: The maximun time we wait to receive a response after sending a request
            .responseTimeout(Duration.ofSeconds(1))
            // Read and Write Timeout: A read timeout occurs when no data was read within a certain 
            //period of time, while the write timeout when a write operation cannot finish at a specific time
            .doOnConnected(connection -> {
                connection.addHandlerLast(new ReadTimeoutHandler(5000, TimeUnit.MILLISECONDS));
                connection.addHandlerLast(new WriteTimeoutHandler(5000, TimeUnit.MILLISECONDS));
            });

//    public Customer get(String code) throws BussinesRuleException, UnknownHostException {
//        Optional<Customer> customer = customerRepository.findByCode(code);
//
//        if (customer.isPresent()) {
//            if (customer.get().getProducts() != null) {
//                List<CustomerProduct> products = customer.get().getProducts();
//                products.forEach(dto -> {
//                    try {
//                        String productName = getProductName(dto.getProductId(), urlProduct, "name");
//                        dto.setProductName(productName);
//                    } catch (UnknownHostException ex) {
//                        Logger.getLogger(ClientWebClient.class.getName()).log(Level.SEVERE, null, ex);
//                    }
//                });
//            }
//
//            customer.get().setTransactions(getTransacctions(customer.get().getIban(), urlTransaction, "ibanAccount"));
//            return customer.get();
//        } else {
//            BussinesRuleException exception = new BussinesRuleException("1025", "Error de validacion, producto no existe", HttpStatus.PRECONDITION_FAILED);
//            throw exception;
//        }
//    }

//    public String getProductName(long id, String URL, String searchedAttribute) throws UnknownHostException {
//        String name = "";
//
//        try {
//            WebClient build = webClientBuilder
//                    .clientConnector(new ReactorClientHttpConnector(client))
//                    .baseUrl(URL)
//                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
//                    .defaultUriVariables(Collections.singletonMap("url", URL))
//                    .build();
//
//            JsonNode block = build
//                    .method(HttpMethod.GET)
//                    .uri("/" + id)
//                    .retrieve()
//                    .bodyToMono(JsonNode.class)
//                    .block();
//
//            if (block != null) {
//                name = block.get(searchedAttribute).asText();
//            }
//        } catch (WebClientResponseException e) {
//
//            HttpStatus httpStatus = e.getStatusCode();
//            if (httpStatus == HttpStatus.NOT_FOUND) {
//                return "";
//            } else {
//                throw new UnknownHostException(e.getMessage());
//            }
//
//        }
//
//        return name;
//    }

    public <T> Flux<T> getExtraccionData(String searchedAttribute, String param, String URL, Class<T> responseType) {
        WebClient webClient = webClientBuilder
                .clientConnector(new ReactorClientHttpConnector(client))
                .baseUrl(URL)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        return webClient.method(HttpMethod.GET)
                .uri(uriBuilder -> uriBuilder
                        .path("/transactions")
                        .queryParam(searchedAttribute, param)
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse ->
                        clientResponse.createException().flatMap(Mono::error)
                )
                .bodyToFlux(responseType)
                .onErrorResume(error -> {
                    System.err.println("Error al consumir la API: " + error.getMessage());
                    return Flux.empty(); // Devuelve un flujo vacío en caso de error
                });
    }

    public <T> Flux<T> getExtraccionData(String URL, Class<T> responseType) {
        WebClient webClient = webClientBuilder
                .clientConnector(new ReactorClientHttpConnector(client))
                .baseUrl(URL)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        return webClient.method(HttpMethod.GET)
                .uri(uriBuilder -> uriBuilder.build()) // Sin parámetros adicionales
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse ->
                        clientResponse.createException().flatMap(Mono::error)
                )
                .bodyToFlux(responseType)
                .onErrorResume(error -> {
                    System.err.println("Error al consumir la API: " + error.getMessage());
                    return Flux.empty();
                });
    }


}
