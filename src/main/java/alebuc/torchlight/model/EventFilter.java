package alebuc.torchlight.model;

import alebuc.torchlight.model.consumer.EndChoice;
import alebuc.torchlight.model.consumer.StartChoice;
import lombok.Builder;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public record EventFilter(
        StartChoice startChoice,
        LocalDateTime startLocalDateTime,
        EndChoice endChoice,
        LocalDateTime endLocalDateTime,
        ZoneId zoneId,
        Instant startInstant,
        Instant endInstant
) {

    /**
     * Constructs an EventFilter object that represents filters with specified start and end choices, date-time values,
     * time zone, and instants for filtering events.
     *
     * @param startChoice        the choice describing the start of the event filter duration; if null, defaults to StartChoice.DEFAULT
     * @param startLocalDateTime the specific local date-time used if the start choice is StartChoice.SPECIFIC_DATE; can be null if not relevant
     * @param endChoice          the choice describing the end of the event filter duration; if null, defaults to EndChoice.DEFAULT
     * @param endLocalDateTime   the specific local date-time used if the end choice is EndChoice.SPECIFIC_DATE; can be null if not relevant
     * @param zoneId             the time-zone ID used to localize date-time calculations; defaults to the system's default time zone if null
     * @param startInstant       the exact start instant of the event filter duration; if null, it is computed based on startChoice
     * @param endInstant         the exact end instant of the event filter duration; if null, it is computed based on endChoice
     */
    @Builder
    public EventFilter(
            StartChoice startChoice,
            LocalDateTime startLocalDateTime,
            EndChoice endChoice,
            LocalDateTime endLocalDateTime,
            ZoneId zoneId,
            Instant startInstant,
            Instant endInstant
    ) {
        StartChoice effectiveStartChoice = (startChoice != null) ? startChoice : StartChoice.DEFAULT;
        EndChoice effectiveEndChoice = (endChoice != null) ? endChoice : EndChoice.DEFAULT;
        ZoneId effectiveZoneId = (zoneId != null) ? zoneId : ZoneId.systemDefault();
        LocalDateTime effectiveNow = LocalDateTime.now(effectiveZoneId);

        Instant baseInstant = effectiveNow.atZone(effectiveZoneId).toInstant();
        Clock clock = Clock.fixed(baseInstant, effectiveZoneId);

        Instant computedStartInstant = (startInstant != null)
                ? startInstant
                : computeStartInstant(effectiveStartChoice, startLocalDateTime, clock, effectiveZoneId);

        Instant computedEndInstant = (endInstant != null)
                ? endInstant
                : computeEndInstant(effectiveEndChoice, endLocalDateTime, clock, effectiveZoneId);

        this.startChoice = effectiveStartChoice;
        this.startLocalDateTime = startLocalDateTime;
        this.endChoice = effectiveEndChoice;
        this.endLocalDateTime = endLocalDateTime;
        this.zoneId = effectiveZoneId;
        this.startInstant = computedStartInstant;
        this.endInstant = computedEndInstant;
    }

    /**
     * Computes the start {@link Instant} based on the provided parameters and {@link StartChoice}.
     *
     * @param choice           the {@link StartChoice} that determines the starting point for the {@link Instant}
     * @param specificDateTime a specific {@link LocalDateTime} used when {@link StartChoice#SPECIFIC_DATE} is selected
     * @param clock            the {@link Clock} used for time-based computations
     * @param zoneId           the {@link ZoneId} used to convert the {@link LocalDateTime} to an {@link Instant}
     * @return the calculated start {@link Instant} based on the provided inputs, or null if no start {@link Instant} is determined
     */
    private static Instant computeStartInstant(StartChoice choice,
                                               LocalDateTime specificDateTime,
                                               Clock clock,
                                               ZoneId zoneId) {
        if (choice == StartChoice.SPECIFIC_DATE) {
            if (specificDateTime == null) {
                return null;
            }
            return specificDateTime.atZone(zoneId).toInstant();
        }

        if (choice == StartChoice.EARLIEST) {
            return null;
        }

        return choice.getInstant().apply(clock);
    }

    /**
     * Computes the end instant based on the provided end choice, specific date-time, clock, and time zone.
     *
     * @param choice           The {@link EndChoice} that determines the basis for the end instant.
     *                         If it is NO_LIMIT, the method will return null.
     * @param specificDateTime The specific date-time to use when the choice is {@link EndChoice#SPECIFIC_DATE}.
     *                         If null and the choice is SPECIFIC_DATE, the method will return null.
     * @param clock            The {@link Clock} used to compute time-based values (e.g., NOW, TODAY, YESTERDAY).
     * @param zoneId           The {@link ZoneId} representing the time zone in which date-time conversions take place.
     * @return An {@link Instant} representing the computed end time, or null if no valid instant can be determined.
     */
    private static Instant computeEndInstant(EndChoice choice,
                                             LocalDateTime specificDateTime,
                                             Clock clock,
                                             ZoneId zoneId) {
        if (choice == EndChoice.NO_LIMIT) {
            return null;
        }

        if (choice == EndChoice.SPECIFIC_DATE) {
            if (specificDateTime == null) {
                return null;
            }
            return specificDateTime.atZone(zoneId).toInstant();
        }

        return choice.getInstant().apply(clock);
    }
}
