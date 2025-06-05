
package com.zoolandia.app.features.client.service;

import com.vaadin.hilla.BrowserCallable;
import com.zoolandia.app.features.client.domain.Client;
import com.zoolandia.app.features.client.domain.ClientRating;
import com.zoolandia.app.features.client.service.dto.ClientCreateDTO;
import com.zoolandia.app.features.client.service.dto.ClientUpdateDTO;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Optional;

/**
 * Service Interface for managing {@link Client} entities.
 */
@BrowserCallable
@Validated
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface ClientService {

	/**
	 * Creates a new Client.
	 *
	 * @param clientDTO the DTO containing the client data
	 * @return the created Client entity
	 */
	Client createClient(@Valid ClientCreateDTO clientDTO);

	/**
	 * Updates an existing Client.
	 *
	 * @param id the ID of the client to update
	 * @param clientDTO the DTO containing the updated client data
	 * @return the updated Client entity
	 */
	Client updateClient(Long id, @Valid ClientUpdateDTO clientDTO);

	/**
	 * Retrieves a Client by ID.
	 *
	 * @param id the ID of the client
	 * @return the Client entity if found
	 */
	Optional<Client> getClientById(Long id);

	/**
	 * Retrieves a Client by cédula.
	 *
	 * @param cedula the cédula number
	 * @return the Client entity if found
	 */
	Optional<Client> getClientByCedula(String cedula);

	/**
	 * Retrieves a Client by passport number.
	 *
	 * @param passport the passport number
	 * @return the Client entity if found
	 */
	Optional<Client> getClientByPassport(String passport);

	/**
	 * Retrieves a Client by RNC.
	 *
	 * @param rnc the RNC number
	 * @return the Client entity if found
	 */
	Optional<Client> getClientByRnc(String rnc);

	/**
	 * Retrieves all Clients with pagination.
	 *
	 * @param pageable pagination information
	 * @return paginated list of Clients
	 */
	Page<Client> getAllClients(Pageable pageable);

	/**
	 * Searches for Clients based on a search term.
	 *
	 * @param searchTerm the term to search for in client fields
	 * @param pageable pagination information
	 * @return paginated list of matching Clients
	 */
	Page<Client> searchClients(String searchTerm, Pageable pageable);

	/**
	 * Retrieves Clients by rating.
	 *
	 * @param rating the client rating to filter by
	 * @param pageable pagination information
	 * @return paginated list of Clients with specified rating
	 */
	Page<Client> getClientsByRating(ClientRating rating, Pageable pageable);

	/**
	 * Retrieves Clients by province.
	 *
	 * @param province the province to filter by
	 * @param pageable pagination information
	 * @return paginated list of Clients in the specified province
	 */
	Page<Client> getClientsByProvince(String province, Pageable pageable);

	/**
	 * Updates a Client's rating.
	 *
	 * @param id the ID of the client
	 * @param newRating the new rating to set
	 */
	void updateClientRating(Long id, ClientRating newRating);

	/**
	 * Deactivates a Client account.
	 *
	 * @param id the ID of the client to deactivate
	 */
	void deactivateClient(Long id);

	/**
	 * Reactivates a previously deactivated Client account.
	 *
	 * @param id the ID of the client to reactivate
	 */
	void reactivateClient(Long id);

	/**
	 * Verifies a Client's identity and documentation.
	 *
	 * @param id the ID of the client to verify
	 * @return true if verification is successful
	 */
	boolean verifyClient(Long id);

	/**
	 * Updates a Client's credit limit.
	 *
	 * @param id the ID of the client
	 * @param newLimit the new credit limit to set
	 */
	void updateCreditLimit(Long id, Double newLimit);

	/**
	 * Validates if at least one form of identification is provided.
	 *
	 * @param cedula the cédula number
	 * @param passport the passport number
	 * @param rnc the RNC number
	 * @return true if at least one valid identification is provided
	 */
	boolean isValidIdentification(String cedula, String passport, String rnc);

	/**
	 * Permanently deletes a Client.
	 *
	 * @param id the ID of the client to delete
	 */
	void deleteClient(Long id);
}