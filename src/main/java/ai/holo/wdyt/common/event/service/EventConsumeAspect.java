package ai.holo.wdyt.common.event.service;

import ai.holo.wdyt.common.event.Event;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class EventConsumeAspect {
    private final EventService eventService;

    public EventConsumeAspect(EventService eventService) {
        this.eventService = eventService;
    }

    @Around("execution(* (@EventConsumer *).*(..))")
    public Object eventConsume(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        Event event = (Event) proceedingJoinPoint.getArgs()[0];
        if(event.consistencyMechanismEnabled()) {
            eventService.deleteEventLog(event);
        }
        return proceedingJoinPoint.proceed();
    }
}
