package expondo.evolution.okr.mapper;

import expondo.evolution.okr.Cycle;
import expondo.evolution.okr.dto.CycleCreateDto;
import expondo.evolution.okr.dto.CycleDto;
import expondo.evolution.okr.dto.CycleUpdateDto;
import java.time.LocalDate;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-02-14T16:12:30+0100",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.2 (Oracle Corporation)"
)
@Component
public class CycleMapperImpl implements CycleMapper {

    @Override
    public CycleDto toDto(Cycle cycle) {
        if ( cycle == null ) {
            return null;
        }

        Long id = null;
        String name = null;
        LocalDate startDate = null;
        LocalDate endDate = null;
        boolean current = false;

        id = cycle.getId();
        name = cycle.getName();
        startDate = cycle.getStartDate();
        endDate = cycle.getEndDate();
        current = cycle.isCurrent();

        int objectiveCount = cycle.getCompanyObjectives().size();
        int timeboxCount = cycle.getTimeboxes().size();

        CycleDto cycleDto = new CycleDto( id, name, startDate, endDate, current, objectiveCount, timeboxCount );

        return cycleDto;
    }

    @Override
    public Cycle toEntity(CycleCreateDto dto) {
        if ( dto == null ) {
            return null;
        }

        Cycle cycle = new Cycle();

        cycle.setName( dto.name() );
        cycle.setStartDate( dto.startDate() );
        cycle.setEndDate( dto.endDate() );

        cycle.setCurrent( false );

        return cycle;
    }

    @Override
    public void updateEntity(CycleUpdateDto dto, Cycle cycle) {
        if ( dto == null ) {
            return;
        }

        cycle.setName( dto.name() );
        cycle.setStartDate( dto.startDate() );
        cycle.setEndDate( dto.endDate() );
    }
}
