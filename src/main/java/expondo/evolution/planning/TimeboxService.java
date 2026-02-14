package expondo.evolution.planning;

import expondo.evolution.okr.Cycle;
import expondo.evolution.okr.CycleRepository;
import expondo.evolution.planning.dto.TimeboxDto;
import expondo.evolution.planning.dto.TimeboxGenerateSeriesDto;
import expondo.evolution.planning.dto.TimeboxUpdateDto;
import expondo.evolution.planning.mapper.TimeboxMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TimeboxService {

    private final TimeboxRepository timeboxRepository;
    private final CycleRepository cycleRepository;
    private final TimeboxMapper timeboxMapper;

    public List<TimeboxDto> findByCycleId(Long cycleId) {
        return timeboxRepository.findByCycleIdOrderByNumberAsc(cycleId).stream()
                .map(timeboxMapper::toDto)
                .toList();
    }

    /**
     * Generate a series of timeboxes for a cycle.
     * Starts at the given startDate, each timebox lasts intervalDays.
     * End dates are adjusted to fall on a workday (Mon-Fri):
     *   - If the calculated end falls on Saturday, it moves to Friday.
     *   - If it falls on Sunday, it moves to Friday.
     * The next timebox then starts on the following Monday.
     * The last timebox may extend beyond the cycle end date.
     * Existing timeboxes for this cycle are deleted first.
     */
    @Transactional
    public List<TimeboxDto> generateSeries(Long cycleId, TimeboxGenerateSeriesDto dto) {
        Cycle cycle = cycleRepository.findById(cycleId)
                .orElseThrow(() -> new RuntimeException("Cycle not found: " + cycleId));

        timeboxRepository.deleteAllByCycleId(cycleId);

        List<Timebox> timeboxes = new ArrayList<>();
        LocalDate currentStart = dto.startDate();
        int number = 1;

        while (!currentStart.isAfter(cycle.getEndDate())) {
            LocalDate rawEnd = currentStart.plusDays(dto.intervalDays() - 1);
            LocalDate adjustedEnd = adjustToWorkday(rawEnd);
            LocalDate nextStart = nextWorkday(adjustedEnd.plusDays(1));

            Timebox timebox = new Timebox();
            timebox.setCycle(cycle);
            timebox.setNumber(number);
            timebox.setStartDate(currentStart);
            timebox.setEndDate(adjustedEnd);

            timeboxes.add(timebox);

            currentStart = nextStart;
            number++;
        }

        return timeboxRepository.saveAll(timeboxes).stream()
                .map(timeboxMapper::toDto)
                .toList();
    }

    @Transactional
    public TimeboxDto update(Long id, TimeboxUpdateDto dto) {
        Timebox timebox = timeboxRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Timebox not found: " + id));
        timeboxMapper.updateEntity(dto, timebox);
        return timeboxMapper.toDto(timeboxRepository.save(timebox));
    }

    @Transactional
    public void delete(Long id) {
        if (!timeboxRepository.existsById(id)) {
            throw new RuntimeException("Timebox not found: " + id);
        }
        timeboxRepository.deleteById(id);
    }

    /**
     * If the date falls on a weekend, move it back to the previous Friday.
     */
    private LocalDate adjustToWorkday(LocalDate date) {
        return switch (date.getDayOfWeek()) {
            case SATURDAY -> date.minusDays(1);
            case SUNDAY -> date.minusDays(2);
            default -> date;
        };
    }

    /**
     * If the date falls on a weekend, move it forward to the next Monday.
     */
    private LocalDate nextWorkday(LocalDate date) {
        return switch (date.getDayOfWeek()) {
            case SATURDAY -> date.plusDays(2);
            case SUNDAY -> date.plusDays(1);
            default -> date;
        };
    }
}