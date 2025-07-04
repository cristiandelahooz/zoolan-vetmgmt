package com.wornux.features.client.service;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.BrowserCallable;
import com.vaadin.hilla.crud.FormService;
import com.vaadin.hilla.crud.ListRepositoryService;
import com.wornux.common.constants.ValidationConstants;
import com.wornux.features.client.domain.Client;
import com.wornux.features.client.domain.ClientRating;
import com.wornux.features.client.repository.ClientRepository;
import com.wornux.features.client.service.dto.ClientCreateDTO;
import com.wornux.features.client.service.dto.ClientUpdateDTO;
import com.wornux.features.client.service.exception.ClientNotFoundException;
import com.wornux.features.client.service.exception.DuplicateIdentificationException;
import com.wornux.features.client.mapper.ClientMapper;
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
@AnonymousAllowed
@Transactional
// TODO: Remove @AnonymousAllowed and restrict access before deploying to production. This is only for development/testing purposes.
public class ClientServiceImpl extends ListRepositoryService<Client, Long, ClientRepository>
        implements ClientService, FormService<ClientCreateDTO, Long> {

    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;

    @Override
    @Transactional
    //@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public Client createClient(@Valid ClientCreateDTO clientDTO) {
        log.debug("Request to create Client : {}", clientDTO);

        validateUniqueIdentification(clientDTO.cedula(), clientDTO.passport(), clientDTO.rnc());

        Client client = clientMapper.toEntity(clientDTO);
        client = clientRepository.save(client);

        log.info("Created Client with ID: {}", client.getId());
        return client;
    }

    /**
     * Implementation of FormService.save() method. This method is used by Vaadin Hilla for CRUD operations.
     */
    @Override
    @Transactional
    public @Nullable ClientCreateDTO save(ClientCreateDTO value) {
        try {
            log.debug("Request to save Client via FormService: {}", value);

            validateUniqueIdentification(value.cedula(), value.passport(), value.rnc());

            Client client = clientMapper.toEntity(value);
            client = clientRepository.save(client);

            ClientCreateDTO result = clientMapper.toDTO(client);
            log.info("Client saved successfully via FormService with ID: {}", client.getId());
            return result;
        } catch (Exception e) {
            log.error("Error saving Client via FormService: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Implementation of FormService.delete() method. This method is used by Vaadin Hilla for CRUD operations. Uses soft
     * delete by deactivating the client.
     */
    @Override
    @Transactional
    public void delete(Long id) {
        log.debug("Request to delete Client via FormService : {}", id);

        Client client = clientRepository.findById(id).orElseThrow(() -> new ClientNotFoundException(id));

        client.setActive(false);
        clientRepository.save(client);

        log.info("Client deactivated via FormService, ID: {}", id);
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
        log.debug("Request to hard delete Client : {}", id);
        clientRepository.deleteById(id);
        log.info("Hard deleted Client ID: {}", id);
    }

    @Transactional(readOnly = true)
    public void validateUniqueIdentificationForUpdate(Long id, String cedula, String passport, String rnc) {
        if (!cedula.trim().isEmpty()) {
            clientRepository.findByCedula(cedula).ifPresent(client -> {
                if (!client.getId().equals(id)) {
                    throw new DuplicateIdentificationException("cedula", cedula);
                }
            });
        }

        if (!passport.trim().isEmpty()) {
            clientRepository.findByPassport(passport).ifPresent(client -> {
                if (!client.getId().equals(id)) {
                    throw new DuplicateIdentificationException("passport", passport);
                }
            });
        }

        if (!rnc.trim().isEmpty()) {
            clientRepository.findByRnc(rnc).ifPresent(client -> {
                if (!client.getId().equals(id)) {
                    throw new DuplicateIdentificationException("rnc", rnc);
                }
            });
        }
    }

    private void validateUniqueIdentification(String cedula, String passport, String rnc) {
        if (hasSingleValidDocument(cedula, passport, rnc)) {
            validateSingleDocumentUniqueness(cedula, passport, rnc);
        }
    }

    private boolean hasSingleValidDocument(String cedula, String passport, String rnc) {
        int documentCount = countNonEmptyDocuments(cedula, passport, rnc);

        if (documentCount > ValidationConstants.MAX_IDENTIFICATION_DOCUMENT_COUNT) {
            throw new IllegalArgumentException("Máximo " + ValidationConstants.MAX_IDENTIFICATION_DOCUMENT_COUNT
                    + " documento de identificación permitido");
        }
        return documentCount == ValidationConstants.MAX_IDENTIFICATION_DOCUMENT_COUNT;
    }

    private int countNonEmptyDocuments(String cedula, String passport, String rnc) {
        int count = 0;
        if (isNotEmpty(cedula))
            count++;
        if (isNotEmpty(passport))
            count++;
        if (isNotEmpty(rnc))
            count++;
        return count;
    }

    private boolean isNotEmpty(String document) {
        return document != null && !document.trim().isEmpty();
    }

    private void validateSingleDocumentUniqueness(String cedula, String passport, String rnc) {
        if (isNotEmpty(cedula)) {
            validateCedulaUniqueness(cedula);
        } else if (isNotEmpty(passport)) {
            validatePassportUniqueness(passport);
        } else if (isNotEmpty(rnc)) {
            validateRncUniqueness(rnc);
        }
    }

    @Transactional(readOnly = true)
    public void validateCedulaUniqueness(String cedula) {
        if (clientRepository.existsByCedula(cedula)) {
            throw new DuplicateIdentificationException("cedula", cedula);
        }
    }

    @Transactional(readOnly = true)
    public void validatePassportUniqueness(String passport) {
        if (clientRepository.existsByPassport(passport)) {
            throw new DuplicateIdentificationException("passport", passport);
        }
    }

    @Transactional(readOnly = true)
    public void validateRncUniqueness(String rnc) {
        if (clientRepository.existsByRnc(rnc)) {
            throw new DuplicateIdentificationException("rnc", rnc);
        }
    }
}