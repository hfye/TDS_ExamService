package tds.exam.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import tds.assessment.Assessment;
import tds.assessment.ItemConstraint;
import tds.assessment.ItemProperty;
import tds.exam.configuration.ExamServiceProperties;
import tds.exam.services.AssessmentService;

@Service
class AssessmentServiceImpl implements AssessmentService {
    private final RestTemplate restTemplate;
    private final ExamServiceProperties examServiceProperties;

    @Autowired
    public AssessmentServiceImpl(RestTemplate restTemplate, ExamServiceProperties examServiceProperties) {
        this.restTemplate = restTemplate;
        this.examServiceProperties = examServiceProperties;
    }

    @Override
    public Optional<Assessment> findAssessmentByKey(String key) {
        UriComponentsBuilder builder =
            UriComponentsBuilder
                .fromHttpUrl(String.format("%s/%s", examServiceProperties.getAssessmentUrl(), key));

        Optional<Assessment> maybeAssessment = Optional.empty();
        try {
            final Assessment assessment = restTemplate.getForObject(builder.toUriString(), Assessment.class);
            maybeAssessment = Optional.of(assessment);
        } catch (HttpClientErrorException hce) {
            if (hce.getStatusCode() != HttpStatus.NOT_FOUND) {
                throw hce;
            }
        }

        return maybeAssessment;
    }

    @Override
    public List<ItemConstraint> findItemConstraints(String clientName, String assessmentId) {
        UriComponentsBuilder builder =
                UriComponentsBuilder
                        .fromHttpUrl(String.format("%s/items/constraints/%s/%s",
                                examServiceProperties.getAssessmentUrl(), clientName, assessmentId));

        List<ItemConstraint> itemConstraints = new ArrayList<>();
        try {
            ResponseEntity<List<ItemConstraint>> responseEntity = restTemplate.exchange(builder.toUriString(),
                    HttpMethod.GET, null, new ParameterizedTypeReference<List<ItemConstraint>>() {
                    });
            //TODO: Will 404 throw exception? do I need to check headers and throw it manually?
            itemConstraints = responseEntity.getBody();

        } catch (HttpClientErrorException hce) {
            if (hce.getStatusCode() != HttpStatus.NOT_FOUND) {
                throw hce;
            }
        }

        return itemConstraints;
    }

    @Override
    public List<ItemProperty> findActiveItemProperties(String segmentKey) {
        UriComponentsBuilder builder =
                UriComponentsBuilder
                        .fromHttpUrl(String.format("%s/items/properties/%s",
                                examServiceProperties.getAssessmentUrl(), segmentKey));

        List<ItemProperty> itemProperties = new ArrayList<>();
        try {
            ResponseEntity<List<ItemProperty>> responseEntity = restTemplate.exchange(builder.toUriString(),
                    HttpMethod.GET, null, new ParameterizedTypeReference<List<ItemProperty>>() {
                    });
            //TODO: Will 404 throw exception? do I need to check headers and throw it manually?
            itemProperties = responseEntity.getBody();

        } catch (HttpClientErrorException hce) {
            if (hce.getStatusCode() != HttpStatus.NOT_FOUND) {
                throw hce;
            }
        }

        return itemProperties;
    }


}
