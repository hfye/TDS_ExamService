package tds.exam.services;

import tds.config.ClientTestProperty;

import java.util.Optional;

/**
 * A service that handles configuration interaction.
 */
public interface ConfigService {

    Optional<ClientTestProperty> findClientTestPropertyByClientAndAssessment(final String clientName, final String assessmentId);
}
