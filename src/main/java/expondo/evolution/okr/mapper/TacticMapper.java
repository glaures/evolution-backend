package expondo.evolution.okr.mapper;

import expondo.evolution.jira.JiraProperties;
import expondo.evolution.okr.Tactic;
import expondo.evolution.okr.dto.TacticCreateDto;
import expondo.evolution.okr.dto.TacticDto;
import expondo.evolution.okr.dto.TacticUpdateDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class TacticMapper {

    /**
     * JIRA base URL is read from configuration to compose deep links to issues.
     * Field-injected because MapStruct generates a subclass that doesn't allow
     * constructor parameters easily.
     */
    @Autowired
    protected JiraProperties jiraProperties;

    /**
     * Maps a Tactic entity to a DTO.
     * activityStatus is set to null here since it requires cycle-level context.
     * Use toDtoWithActivity() for enriched mapping.
     */
    public TacticDto toDto(Tactic entity) {
        return toDtoWithActivity(entity, null);
    }

    /**
     * Maps a Tactic entity to a DTO with an explicit activity status.
     */
    public TacticDto toDtoWithActivity(Tactic entity, String activityStatus) {
        if (entity == null) return null;
        return new TacticDto(
                entity.getId(),
                entity.getCode(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getPriority(),
                entity.getScore(),
                entity.getCompanyObjective().getId(),
                entity.getResponsibleUnit() != null ? entity.getResponsibleUnit().getId() : null,
                entity.getResponsibleUnit() != null ? entity.getResponsibleUnit().getName() : null,
                activityStatus,
                entity.getJiraIssueKey(),
                entity.getJiraDepartments(),
                buildJiraUrl(entity.getJiraIssueKey())
        );
    }

    private String buildJiraUrl(String issueKey) {
        if (issueKey == null || issueKey.isBlank()) return null;
        if (jiraProperties == null || jiraProperties.baseUrl() == null
                || jiraProperties.baseUrl().isBlank()) return null;
        String base = jiraProperties.baseUrl();
        if (base.endsWith("/")) base = base.substring(0, base.length() - 1);
        return base + "/browse/" + issueKey;
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "companyObjective", ignore = true)
    @Mapping(target = "responsibleUnit", ignore = true)
    @Mapping(target = "jiraIssueKey", ignore = true)
    @Mapping(target = "jiraDepartments", ignore = true)
    @Mapping(target = "archived", ignore = true)
    @Mapping(target = "lastSyncedAt", ignore = true)
    @Mapping(target = "jiraPriorityPushed", ignore = true)
    @Mapping(target = "priorityChangedAt", ignore = true)
    public abstract Tactic toEntity(TacticCreateDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "companyObjective", ignore = true)
    @Mapping(target = "responsibleUnit", ignore = true)
    @Mapping(target = "jiraIssueKey", ignore = true)
    @Mapping(target = "jiraDepartments", ignore = true)
    @Mapping(target = "archived", ignore = true)
    @Mapping(target = "lastSyncedAt", ignore = true)
    @Mapping(target = "jiraPriorityPushed", ignore = true)
    @Mapping(target = "priorityChangedAt", ignore = true)
    public abstract void updateEntity(TacticUpdateDto dto, @MappingTarget Tactic entity);
}