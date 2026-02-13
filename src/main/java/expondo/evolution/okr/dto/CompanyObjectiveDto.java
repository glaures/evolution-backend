package expondo.evolution.okr.dto;

import java.util.List;

public record CompanyObjectiveDto(
        Long id,
        String code,
        String name,
        String description,
        Long cycleId,
        List<KeyResultDto> keyResults,
        List<TacticDto> tactics
) {}