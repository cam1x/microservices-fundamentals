package com.chachotkin.resource.service.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Slf4j
@Component
public class PublishEventCallback implements ListenableFutureCallback<SendResult<Long, String>> {

    @Override
    public void onSuccess(SendResult<Long, String> result) {
        log.info(
                "Upload event for resource id [{}] was published to topic [{}] with offset [{}], "
                        + "event message [{}].",
                result.getProducerRecord().key(),
                result.getRecordMetadata().topic(),
                result.getRecordMetadata().offset(),
                result.getProducerRecord().value()
        );
    }

    @Override
    public void onFailure(Throwable ex) {
        log.error("Failed to publish upload event: {}!", ex.getMessage(), ex);
    }
}
