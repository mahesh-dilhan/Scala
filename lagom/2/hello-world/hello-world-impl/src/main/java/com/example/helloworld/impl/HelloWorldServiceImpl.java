package com.example.helloworld.impl;

import akka.Done;
import akka.NotUsed;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.cluster.sharding.typed.javadsl.Entity;
import akka.cluster.sharding.typed.javadsl.EntityRef;
import akka.japi.Pair;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.broker.Topic;
import com.lightbend.lagom.javadsl.api.transport.BadRequest;
import com.lightbend.lagom.javadsl.broker.TopicProducer;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRegistry;
import com.example.helloworld.api.GreetingMessage;
import com.example.helloworld.api.HelloWorldService;
import com.example.helloworld.impl.HelloWorldCommand.*;

import javax.inject.Inject;
import java.time.Duration;

/**
 * Implementation of the HelloWorldService.
 */
public class HelloWorldServiceImpl implements HelloWorldService {
    private final PersistentEntityRegistry persistentEntityRegistry;
    private final Duration askTimeout = Duration.ofSeconds(5);
    private ClusterSharding clusterSharding;

    @Inject
     public HelloWorldServiceImpl(PersistentEntityRegistry persistentEntityRegistry, ClusterSharding clusterSharding) {
        this.clusterSharding = clusterSharding;
        // The persistent entity registry is only required to build an event stream for the TopicProducer
        this.persistentEntityRegistry = persistentEntityRegistry;

        // register the Aggregate as a sharded entity
        this.clusterSharding.init(
            Entity.of(
                HelloWorldAggregate.ENTITY_TYPE_KEY,
                HelloWorldAggregate::create
            )
        );
    }

    @Override
    public ServiceCall<NotUsed, String> hello(String id) {
        return request -> {
            // Look up the aggregete instance for the given ID.
            EntityRef<HelloWorldCommand> ref = clusterSharding.entityRefFor(HelloWorldAggregate.ENTITY_TYPE_KEY, id);
            // Ask the entity the Hello command.
            return ref.
                <HelloWorldCommand.Greeting>ask(replyTo -> new Hello(id, replyTo), askTimeout)
                .thenApply(greeting -> greeting.message);
        };
    }

    @Override
    public ServiceCall<GreetingMessage, Done> useGreeting(String id) {
        return request -> {
            // Look up the aggregete instance for the given ID.
            EntityRef<HelloWorldCommand> ref = clusterSharding.entityRefFor(HelloWorldAggregate.ENTITY_TYPE_KEY, id);
            // Tell the entity to use the greeting message specified.
            return ref.
                <HelloWorldCommand.Confirmation>ask(replyTo -> new UseGreetingMessage(request.message, replyTo), askTimeout)
                .thenApply(confirmation -> {
                        if (confirmation instanceof HelloWorldCommand.Accepted) {
                            return Done.getInstance();
                        } else {
                            throw new BadRequest(((HelloWorldCommand.Rejected) confirmation).reason);
                        }
                    }
                );
        };
    }

    @Override
    public Topic<com.example.helloworld.api.HelloWorldEvent> helloEvents() {
        // We want to publish all the shards of the hello event
        return TopicProducer.taggedStreamWithOffset(HelloWorldEvent.TAG.allTags(), (tag, offset) ->
            // Load the event stream for the passed in shard tag
            persistentEntityRegistry.eventStream(tag, offset).map(eventAndOffset -> {
                // Now we want to convert from the persisted event to the published event.
                // Although these two events are currently identical, in future they may
                // change and need to evolve separately, by separating them now we save
                // a lot of potential trouble in future.
                com.example.helloworld.api.HelloWorldEvent eventToPublish;

                if (eventAndOffset.first() instanceof HelloWorldEvent.GreetingMessageChanged) {
                    HelloWorldEvent.GreetingMessageChanged messageChanged = (HelloWorldEvent.GreetingMessageChanged) eventAndOffset.first();
                    eventToPublish = new com.example.helloworld.api.HelloWorldEvent.GreetingMessageChanged(
                        messageChanged.getName(), messageChanged.getMessage()
                    );
                } else {
                    throw new IllegalArgumentException("Unknown event: " + eventAndOffset.first());
                }

                // We return a pair of the translated event, and its offset, so that
                // Lagom can track which offsets have been published.
                return Pair.create(eventToPublish, eventAndOffset.second());
            })
        );
    }
}
