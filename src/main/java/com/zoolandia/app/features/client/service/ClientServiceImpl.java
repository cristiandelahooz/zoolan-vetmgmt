package com.zoolandia.app.features.client.service;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.BrowserCallable;
import com.vaadin.hilla.crud.CrudRepositoryService;
import com.vaadin.hilla.crud.FormService;
import com.vaadin.hilla.crud.ListRepositoryService;
import com.zoolandia.app.features.client.domain.Client;
import com.zoolandia.app.features.client.domain.ClientRating;
import com.zoolandia.app.features.client.repository.ClientRepository;
import com.zoolandia.app.features.client.service.dto.ClientCreateDTO;
import com.zoolandia.app.features.client.service.dto.ClientUpdateDTO;
import com.zoolandia.app.features.client.service.exception.ClientNotFoundException;
import com.zoolandia.app.features.client.service.exception.DuplicateIdentificationException;
import com.zoolandia.app.features.client.mapper.ClientMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Optional;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
@BrowserCallable
@AnonymousAllowed // TODO: Remove @AnonymousAllowed and restrict access before deploying to production. This is only for development/testing purposes.
public class ClientServiceImpl extends ListRepositoryService<Client, Long, ClientRepository>
        implements ClientService, FormService<ClientCreateDTO, Long> {

    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;

    @Override
    @Transactional
    //@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public Client createClient(@Valid ClientCreateDTO clientDTO) {
        log.debug("Request to create Client : {}", clientDTO);

        validateUniqueIdentification(clientDTO.getCedula(), clientDTO.getPassport(), clientDTO.getRnc());

        Client client = clientMapper.toEntity(clientDTO);
        client = clientRepository.save(client);

        log.info("Created Client with ID: {}", client.getId());
        return client;
    }

    @Override
    public @Nullable ClientCreateDTO save(ClientCreateDTO value) {
        try {
            Client client = createClient(value);
            ClientCreateDTO generated = clientMapper.toDTO(client);
            log.info("Client created successfully with ID: {}", generated.getUsername());
            return generated;
        } catch (Exception e) {
            log.error("Error creating Client: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public void delete(Long id) {
        deactivateClient(id);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public Client updateClient(Long id, @Valid ClientUpdateDTO clientDTO) {
        log.debug("Request to update Client : {}", clientDTO);

        Client existingClient = clientRepository.findById(id).orElseThrow(() -> new ClientNotFoundException(id));

        validateUniqueIdentificationForUpdate(id, clientDTO.getCedula(), clientDTO.getPassport(), clientDTO.getRnc());

        clientMapper.updateClientFromDTO(clientDTO, existingClient);

        return clientRepository.save(existingClient);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Client> getClientById(Long id) {
        log.debug("Request to get Client : {}", id);
        return clientRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Client> getClientByCedula(String cedula) {
        log.debug("Request to get Client by cedula : {}", cedula);
        return clientRepository.findByCedula(cedula);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Client> getClientByPassport(String passport) {
        log.debug("Request to get Client by passport : {}", passport);
        return clientRepository.findByPassport(passport);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Client> getClientByRnc(String rnc) {
        log.debug("Request to get Client by RNC : {}", rnc);
        return clientRepository.findByRnc(rnc);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Client> getAllClients(Pageable pageable) {
        log.debug("Request to get all Clients");
        return clientRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Client> searchClients(String searchTerm, Pageable pageable) {
        log.debug("Request to search Clients with term: {}", searchTerm);
        return clientRepository.findAll((root, query, cb) -> {
            String pattern = "%" + searchTerm.toLowerCase() + "%";
            return cb.or(cb.like(cb.lower(root.get("firstName")), pattern),
                    cb.like(cb.lower(root.get("lastName")), pattern), cb.like(root.get("cedula"), pattern),
                    cb.like(root.get("passport"), pattern), cb.like(root.get("rnc"), pattern));
        }, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Client> getClientsByRating(ClientRating rating, Pageable pageable) {
        log.debug("Request to get Clients by rating : {}", rating);
        return clientRepository.findByRating(rating, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Client> getClientsByProvince(String province, Pageable pageable) {
        log.debug("Request to get Clients by province : {}", province);
        return clientRepository.findByProvince(province, pageable);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIAN')")
    public void updateClientRating(Long id, ClientRating newRating) {
        log.debug("Request to update Client rating : {} to {}", id, newRating);

        Client client = clientRepository.findById(id).orElseThrow(() -> new ClientNotFoundException(id));

        client.setRating(newRating);
        clientRepository.save(client);

        log.info("Updated rating for Client ID: {} to {}", id, newRating);
    }

    @Override
    @Transactional
    //@PreAuthorize("hasRole('ADMIN')")
    public void deactivateClient(Long id) {
        log.debug("Request to deactivate Client : {}", id);

        Client client = clientRepository.findById(id).orElseThrow(() -> new ClientNotFoundException(id));

        client.setActive(false);
        clientRepository.save(client);

        log.info("Deactivated Client ID: {}", id);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void reactivateClient(Long id) {
        log.debug("Request to reactivate Client : {}", id);

        Client client = clientRepository.findById(id).orElseThrow(() -> new ClientNotFoundException(id));

        client.setActive(true);
        clientRepository.save(client);

        log.info("Reactivated Client ID: {}", id);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public boolean verifyClient(Long id) {
        log.debug("Request to verify Client : {}", id);

        Client client = clientRepository.findById(id).orElseThrow(() -> new ClientNotFoundException(id));

        client.setVerified(true);
        clientRepository.save(client);

        log.info("Verified Client ID: {}", id);
        return true;
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void updateCreditLimit(Long id, Double newLimit) {
        log.debug("Request to update Client credit limit : {} to {}", id, newLimit);

        Client client = clientRepository.findById(id).orElseThrow(() -> new ClientNotFoundException(id));

        client.setCreditLimit(newLimit);
        clientRepository.save(client);

        log.info("Updated credit limit for Client ID: {} to {}", id, newLimit);
    }

    @Override
    public boolean isValidIdentification(String cedula, String passport, String rnc) {
        return !cedula.trim().isEmpty() || !passport.trim().isEmpty() || !rnc.trim().isEmpty();
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteClient(Long id) {
        log.debug("Request to delete Client : {}", id);
        clientRepository.deleteById(id);
        log.info("Deleted Client ID: {}", id);
    }

    private void validateUniqueIdentification(String cedula, String passport, String rnc) {
        if (clientRepository.existsByCedula(cedula)) {
            throw new DuplicateIdentificationException("cedula", cedula);
        }
        if (clientRepository.existsByPassport(passport)) {
            throw new DuplicateIdentificationException("passport", passport);
        }
        if (clientRepository.existsByRnc(rnc)) {
            throw new DuplicateIdentificationException("rnc", rnc);
        }
    }

    private void validateUniqueIdentificationForUpdate(Long id, String cedula, String passport, String rnc) {
        clientRepository.findByCedula(cedula).ifPresent(client -> {
            if (!client.getId().equals(id)) {
                throw new DuplicateIdentificationException("cedula", cedula);
            }
        });
        clientRepository.findByPassport(passport).ifPresent(client -> {
            if (!client.getId().equals(id)) {
                throw new DuplicateIdentificationException("passport", passport);
            }
        });
        clientRepository.findByRnc(rnc).ifPresent(client -> {
            if (!client.getId().equals(id)) {
                throw new DuplicateIdentificationException("rnc", rnc);
            }
        });
    }
}