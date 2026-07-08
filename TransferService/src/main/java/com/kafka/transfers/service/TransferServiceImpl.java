package com.kafka.transfers.service;

import com.kafka.transfers.error.TransferServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.kafka.transfers.model.TransferRestModel;
import com.kafka.payments.core.events.DepositRequestedEvent;
import com.kafka.payments.core.events.WithdrawalRequestedEvent;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransferServiceImpl implements TransferService {
	private final KafkaTemplate<String, Object> kafkaTemplate;
	private final RestTemplate restTemplate;

	@Value("${withdraw-money-topic}")
	private String withdrawTopicName;

	@Value("${deposit-money-topic}")
	private String depositTopicName;
	@Transactional
	@Override
	public boolean transfer(TransferRestModel transferRestModel) {
		WithdrawalRequestedEvent withdrawalEvent = new WithdrawalRequestedEvent(transferRestModel.getSenderId(), transferRestModel.getRecipientId(), transferRestModel.getAmount());
		DepositRequestedEvent depositEvent = new DepositRequestedEvent(transferRestModel.getSenderId(), transferRestModel.getRecipientId(), transferRestModel.getAmount());

		try {
			kafkaTemplate.send(withdrawTopicName, withdrawalEvent);
			log.info("Sent event to withdrawal topic.");
			// Business logic that causes and error
			callRemoteService();
			kafkaTemplate.send(depositTopicName, depositEvent);
			log.info("Sent event to deposit topic");

		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			throw new TransferServiceException(ex);
		}

		return true;
	}

	private void callRemoteService() throws Exception {
		String requestUrl = "http://localhost:8082/response/200";
		ResponseEntity<String> response = restTemplate.exchange(requestUrl, HttpMethod.GET, null, String.class);

		if (response.getStatusCode().value() == HttpStatus.SERVICE_UNAVAILABLE.value()) {
			throw new Exception("Destination Microservice not available");
		}

		if (response.getStatusCode().value() == HttpStatus.OK.value()) {
            log.info("Received response from mock service: {}", response.getBody());
		}
	}

}
