package com.wornux.services.implementations;

import com.vaadin.hilla.crud.ListRepositoryService;
import com.wornux.constants.ValidationConstants;
import com.wornux.data.entity.Client;
import com.wornux.data.enums.ClientRating;
import com.wornux.data.repository.ClientRepository;
import com.wornux.dto.request.ClientCreateRequestDto;
import com.wornux.dto.request.ClientUpdateRequestDto;
import com.wornux.exception.ClientNotFoundException;
import com.wornux.exception.DuplicateIdentificationException;
import com.wornux.mapper.ClientMapper;
import com.wornux.services.interfaces.ClientService;
import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
@Transactional
public class ClientServiceImpl extends ListRepositoryService<Client, Long, ClientRepository>
    implements ClientService {

  private final ClientRepository clientRepository;
  private final ClientMapper clientMapper;

  @Override
  @Transactional
  public Client createClient(@Valid ClientCreateRequestDto clientRequest) {
    log.debug("Request to create Client : {}", clientRequest);

    validateUniqueIdentification(
        clientRequest.cedula(), clientRequest.passport(), clientRequest.rnc());
    try {
      Client client = clientMapper.toEntity(clientRequest);
      client = clientRepository.save(client);
      log.info("Created Client with ID: {}", client.getId());

      return client;
    } catch (DataIntegrityViolationException e) {
      handleConstraintViolation(e);
      throw e;
    }
  }

  @Override
  @Transactional
  public Client updateClient(Long id, @Valid ClientUpdateRequestDto clientRequest) {
    log.debug("Request to update Client : {}", clientRequest);

    Client existingClient =
        clientRepository.findById(id).orElseThrow(() -> new ClientNotFoundException(id));

    if (clientRequest.getEmail() != null
        && !clientRequest.getEmail().equals(existingClient.getEmail())) {
      clientRepository
          .findByEmail(clientRequest.getEmail())
          .ifPresent(
              client -> {
                throw new ValidationException("El correo electrónico ya existe");
              });
    }
    validateUniqueIdentificationForUpdate(
        id, clientRequest.getCedula(), clientRequest.getPassport(), clientRequest.getRnc());

    try {
      clientMapper.updateClientFromDTO(clientRequest, existingClient);
      return clientRepository.save(existingClient);
    } catch (DataIntegrityViolationException e) {
      handleConstraintViolation(e);
      throw e;
    }
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Client> getClientById(Long id) {
    log.debug("Request to get Client : {}", id);
    return clientRepository.findById(id);
  }

  @Override
  @Transactional
  public Client getClientByIdMandatory(Long id) {
    log.debug("Request to get Client mandatory: {}", id);
    return clientRepository.findById(id).orElseThrow(() -> new ClientNotFoundException(id));
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
    return clientRepository.findAll(
        (root, query, cb) -> {
          String pattern = "%" + searchTerm.toLowerCase() + "%";
          return cb.or(
              cb.like(cb.lower(root.get("firstName")), pattern),
              cb.like(cb.lower(root.get("lastName")), pattern),
              cb.like(root.get("cedula"), pattern),
              cb.like(root.get("passport"), pattern),
              cb.like(root.get("rnc"), pattern));
        },
        pageable);
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

    Client client =
        clientRepository.findById(id).orElseThrow(() -> new ClientNotFoundException(id));

    client.setRating(newRating);
    clientRepository.save(client);

    log.info("Updated rating for Client ID: {} to {}", id, newRating);
  }

  @Override
  @Transactional
  // @PreAuthorize("hasRole('ADMIN')")
  public void deactivateClient(Long id) {
    Client client =
        clientRepository.findById(id).orElseThrow(() -> new ClientNotFoundException(id));

    if (client.getRnc() != null) {
      client.setRnc(client.getRnc().trim());
    }

    client.setActive(false);
    clientRepository.save(client);
    log.info("Deactivated Client ID: {}", id);
  }

  @Override
  @Transactional
  public void reactivateClient(Long id) {
    log.debug("Request to reactivate Client : {}", id);

    Client client =
        clientRepository.findById(id).orElseThrow(() -> new ClientNotFoundException(id));

    client.setActive(true);
    clientRepository.save(client);

    log.info("Reactivated Client ID: {}", id);
  }

  @Override
  @Transactional
  public boolean verifyClient(Long id) {
    log.debug("Request to verify Client : {}", id);

    Client client =
        clientRepository.findById(id).orElseThrow(() -> new ClientNotFoundException(id));

    client.setVerified(true);
    clientRepository.save(client);

    log.info("Verified Client ID: {}", id);
    return true;
  }

  @Override
  @Transactional
  public void updateCreditLimit(Long id, Double newLimit) {
    log.debug("Request to update Client credit limit : {} to {}", id, newLimit);

    Client client =
        clientRepository.findById(id).orElseThrow(() -> new ClientNotFoundException(id));

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
  public void deleteClient(Long id) {
    log.debug("Request to hard delete Client : {}", id);
    clientRepository.deleteById(id);
    log.info("Hard deleted Client ID: {}", id);
  }

  private void validateUniqueIdentificationForUpdate(
      Long clientId, String cedula, String passport, String rnc) {
    // Check for null values before calling trim()
    if (cedula != null && !cedula.trim().isEmpty()) {
      Optional<Client> existingClientWithCedula =
          clientRepository.findByCedulaAndIdNot(cedula, clientId);
      if (existingClientWithCedula.isPresent()) {
        throw new IllegalArgumentException("Ya existe un cliente con esta cédula");
      }
    }

    if (passport != null && !passport.trim().isEmpty()) {
      Optional<Client> existingClientWithPassport =
          clientRepository.findByPassportAndIdNot(passport, clientId);
      if (existingClientWithPassport.isPresent()) {
        throw new IllegalArgumentException("Ya existe un cliente con este pasaporte");
      }
    }

    if (rnc != null && !rnc.trim().isEmpty()) {
      Optional<Client> existingClientWithRnc = clientRepository.findByRncAndIdNot(rnc, clientId);
      if (existingClientWithRnc.isPresent()) {
        throw new IllegalArgumentException("Ya existe un cliente con este RNC");
      }
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
      throw new IllegalArgumentException(
          "Máximo "
              + ValidationConstants.MAX_IDENTIFICATION_DOCUMENT_COUNT
              + " documento de identificación permitido");
    }
    return documentCount == ValidationConstants.MAX_IDENTIFICATION_DOCUMENT_COUNT;
  }

  private int countNonEmptyDocuments(String cedula, String passport, String rnc) {
    int count = 0;
    if (isNotEmpty(cedula)) count++;
    if (isNotEmpty(passport)) count++;
    if (isNotEmpty(rnc)) count++;
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

  @Transactional
  public List<Client> getAllActiveClients() {
    log.debug("Request to get all active Clients");
    return clientRepository.findAllByActiveTrue();
  }

  @Override
  @Transactional
  public void archive(Client client) {

    clientRepository.save(client);

    log.info("Archived Client ID: {}", client.getId());
  }

  @Override
  public ClientRepository getRepository() {
    return clientRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public List<Client> findAllActiveByIds(List<Long> ids) {
    return (ids == null || ids.isEmpty())
        ? List.of()
        : clientRepository.findByIdInAndActiveTrue(ids);
  }

  private void handleConstraintViolation(DataIntegrityViolationException e) {
    String message = e.getMessage().toLowerCase();

    if (message.contains("uc_users_email") || message.contains("email")) {
      throw new ValidationException("El correo electrónico ya existe");
    } else if (message.contains("cedula")) {
      throw new ValidationException("La cédula ya existe");
    } else if (message.contains("passport")) {
      throw new ValidationException("El pasaporte ya existe");
    } else if (message.contains("rnc")) {
      throw new ValidationException("El RNC ya existe");
    } else {
      throw new ValidationException("Violación de restricción de unicidad");
    }
  }
}
