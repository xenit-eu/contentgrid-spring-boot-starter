package com.contentgrid.userapps.holmes.dcm.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;

import javax.persistence.EntityManagerFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.dsl.MessageHandlerSpec;
import org.springframework.integration.test.mock.MockIntegration;
import org.springframework.messaging.MessageHandler;

import com.contentgrid.spring.integration.events.ContentGridEventPublisher;
import com.contentgrid.spring.integration.events.ContentGridMessageHandler;
import com.contentgrid.spring.integration.events.ContentGridPublisherEventListener;
import com.contentgrid.userapps.holmes.dcm.model.Case;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest(properties = { "spring.content.storage.type.default=fs" })
class DcmApiRepositoryIntegrationEventsTests {

    @TestConfiguration
    public static class TestConfig {

        @Bean
        ContentGridPublisherEventListener contentGridPublisherEventListenerSpyMock(
                EntityManagerFactory entityManagerFactory, ContentGridEventPublisher publisher) {
            ContentGridPublisherEventListener spy2 = spy(
                    new ContentGridPublisherEventListener(publisher, entityManagerFactory));
            return spy2;
        }

        @Bean
        ContentGridMessageHandler messageHandler(ObjectMapper objectMapper) {

            MessageHandler messageHandler = MockIntegration.mockMessageHandler().handleNext(m -> {
                Object payload = m.getPayload();
                assertThat(payload).isInstanceOf(String.class);

                try {
                    HashMap<String, Object> readValue = objectMapper.readValue((String) payload,
                            new TypeReference<HashMap<String, Object>>() {
                            });

                    assertThat(readValue).containsKey("application");
                    assertThat(readValue).containsKey("type");
                    assertThat(readValue).containsKey("old");
                    assertThat(readValue).containsKey("new");
                    assertThat(readValue).containsKey("entity");
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            });

            MessageHandlerSpec mockMessageHandlerSpec = mock(MessageHandlerSpec.class);
            when(mockMessageHandlerSpec.get()).thenReturn(messageHandler);

            return () -> mockMessageHandlerSpec;
        }
    }

    @Autowired
    CaseRepository repository;

    @Autowired
    ContentGridPublisherEventListener listener;

    @Autowired
    ContentGridMessageHandler contentGridMessageHandler;

    @BeforeEach
    public void buildSpy() {
        // since we are using spring injection we have to reset our spy mock before each
        // test
        reset(listener, contentGridMessageHandler.get().get());
    }

    @Test
    void whenCaseIsSavedOnce_postInsertShouldBeCalledOnce_ok() {
        repository.save(new Case());
        verify(listener, times(1)).onPostInsert(any());
        verify(listener, times(0)).onPostUpdate(any());
        verify(listener, times(0)).onPostDelete(any());

        verify(contentGridMessageHandler.get().get(), times(1)).handleMessage(any());
    }

    @Test
    void whenCaseIsSavedTwice_postInsertShouldBeCalledTwice_ok() {
        repository.save(new Case());
        repository.save(new Case());
        verify(listener, times(2)).onPostInsert(any());
        verify(listener, times(0)).onPostUpdate(any());
        verify(listener, times(0)).onPostDelete(any());

        verify(contentGridMessageHandler.get().get(), times(2)).handleMessage(any());
    }

    @Test
    void whenCaseIsUpdatedOnce_postUpdateShouldBeCalledOnce_ok() {

        Case case1 = new Case();
        case1.setDescription("old description");

        Case saved = repository.save(case1);
        saved.setDescription("description for update");
        repository.save(saved);

        verify(listener, times(1)).onPostInsert(any());
        verify(listener, times(1)).onPostUpdate(any());
        verify(listener, times(0)).onPostDelete(any());

        verify(contentGridMessageHandler.get().get(), times(2)).handleMessage(any());
    }

    @Test
    void whenCaseIsUpdatedTwice_postUpdateShouldBeCalledTwice_ok() {
        Case saved = repository.save(new Case());
        saved.setDescription("description for update");
        repository.save(saved);

        saved.setDescription("description for second update");
        repository.save(saved);

        verify(listener, times(1)).onPostInsert(any());
        verify(listener, times(2)).onPostUpdate(any());
        verify(listener, times(0)).onPostDelete(any());

        verify(contentGridMessageHandler.get().get(), times(3)).handleMessage(any());
    }

    @Test
    void whenCaseIsDeleted_postUpdateShouldBeCalledOnce_ok() {
        Case saved = repository.save(new Case());

        saved.setDescription("description for update");
        repository.save(saved);

        repository.delete(saved);

        verify(listener, times(1)).onPostInsert(any());
        verify(listener, times(1)).onPostUpdate(any());
        verify(listener, times(1)).onPostDelete(any());

        verify(contentGridMessageHandler.get().get(), times(3)).handleMessage(any());
    }
}