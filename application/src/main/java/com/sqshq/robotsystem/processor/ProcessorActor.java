package com.sqshq.robotsystem.processor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.cluster.pubsub.DistributedPubSubMediator;
import com.sqshq.robotsystem.config.Actor;
import com.sqshq.robotsystem.processor.service.ProcessorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Random;

@Actor
public class ProcessorActor extends AbstractActor {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final Random random = new Random();

    @Autowired
    private ProcessorService processorService;

    @Autowired
    @Qualifier("pubSubMediator")
    private ActorRef mediator;

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Integer.class, this::process)
                .matchAny(this::unhandled)
                .build();
    }

    private void process(Integer sensorData) {
        log.info("Processor : {}", sensorData);

        int computedValue = processorService.compute(sensorData);
        int targetRobot = random.nextInt(100);

        log.info("Routing processed value to robot #{}", targetRobot);
        mediator.tell(new DistributedPubSubMediator.Publish(String.valueOf(targetRobot), computedValue), self());
        sender().tell(targetRobot, self());
    }
}
