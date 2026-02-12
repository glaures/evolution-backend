package expondo.evolution.config;

import expondo.evolution.config.dto.AuditEntryDto;
import expondo.evolution.config.dto.AuditRevisionDto;
import expondo.evolution.okr.*;
import expondo.evolution.planning.*;
import expondo.evolution.user.AppUser;
import expondo.evolution.user.Unit;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.CrossTypeRevisionChangesReader;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuditService {

    private final EntityManager entityManager;

    private static final Map<String, Class<?>> AUDITED_ENTITIES = Map.ofEntries(
            Map.entry("Cycle", Cycle.class),
            Map.entry("CompanyObjective", CompanyObjective.class),
            Map.entry("KeyResult", KeyResult.class),
            Map.entry("Team", Team.class),
            Map.entry("Unit", Unit.class),
            Map.entry("Commitment", Commitment.class),
            Map.entry("Deliverable", Deliverable.class),
            Map.entry("Timebox", Timebox.class),
            Map.entry("TimeboxReport", TimeboxReport.class),
            Map.entry("TimeboxEffort", TimeboxEffort.class),
            Map.entry("TimeboxDelivery", TimeboxDelivery.class),
            Map.entry("AppUser", AppUser.class)
    );

    public List<AuditRevisionDto> getRevisions(String entityType, String userEmail,
                                               Instant from, Instant to, int limit) {
        AuditReader auditReader = AuditReaderFactory.get(entityManager);

        // Get all revision numbers with filters
        List<Number> revisionNumbers = getFilteredRevisionNumbers(auditReader, entityType, userEmail, from, to, limit);

        List<AuditRevisionDto> result = new ArrayList<>();
        for (Number revNum : revisionNumbers) {
            AuditRevisionEntity revEntity = auditReader.findRevision(AuditRevisionEntity.class, revNum);

            List<AuditEntryDto> changes = getChangesForRevision(auditReader, revNum, entityType);

            if (!changes.isEmpty()) {
                result.add(new AuditRevisionDto(
                        revEntity.getId(),
                        Instant.ofEpochMilli(revEntity.getTimestamp()),
                        revEntity.getUserEmail(),
                        revEntity.getUserName(),
                        changes
                ));
            }
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    private List<Number> getFilteredRevisionNumbers(AuditReader auditReader, String entityType,
                                                    String userEmail, Instant from, Instant to, int limit) {
        // If filtering by entity type, query that entity's audit table
        if (entityType != null && AUDITED_ENTITIES.containsKey(entityType)) {
            Class<?> entityClass = AUDITED_ENTITIES.get(entityType);
            AuditQuery query = auditReader.createQuery()
                    .forRevisionsOfEntity(entityClass, false, true)
                    .addProjection(AuditEntity.revisionNumber())
                    .addOrder(AuditEntity.revisionNumber().desc());

            if (from != null) {
                query.add(AuditEntity.revisionProperty("timestamp").ge(from.toEpochMilli()));
            }
            if (to != null) {
                query.add(AuditEntity.revisionProperty("timestamp").le(to.toEpochMilli()));
            }
            if (userEmail != null) {
                query.add(AuditEntity.revisionProperty("userEmail").eq(userEmail));
            }

            query.setMaxResults(limit);
            List<Number> results = query.getResultList();
            return results.stream().distinct().collect(Collectors.toList());
        }

        // No entity filter: query revision table directly
        var criteriaBuilder = entityManager.getCriteriaBuilder();
        var criteriaQuery = criteriaBuilder.createQuery(AuditRevisionEntity.class);
        var root = criteriaQuery.from(AuditRevisionEntity.class);

        List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

        if (from != null) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("timestamp"), from.toEpochMilli()));
        }
        if (to != null) {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("timestamp"), to.toEpochMilli()));
        }
        if (userEmail != null) {
            predicates.add(criteriaBuilder.equal(root.get("userEmail"), userEmail));
        }

        criteriaQuery.where(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        criteriaQuery.orderBy(criteriaBuilder.desc(root.get("id")));

        return entityManager.createQuery(criteriaQuery)
                .setMaxResults(limit)
                .getResultList()
                .stream()
                .map(AuditRevisionEntity::getId)
                .map(id -> (Number) id)
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private List<AuditEntryDto> getChangesForRevision(AuditReader auditReader, Number revNum, String entityTypeFilter) {
        List<AuditEntryDto> entries = new ArrayList<>();

        Map<String, Class<?>> toScan = entityTypeFilter != null && AUDITED_ENTITIES.containsKey(entityTypeFilter)
                ? Map.of(entityTypeFilter, AUDITED_ENTITIES.get(entityTypeFilter))
                : AUDITED_ENTITIES;

        for (Map.Entry<String, Class<?>> entry : toScan.entrySet()) {
            String typeName = entry.getKey();
            Class<?> entityClass = entry.getValue();

            try {
                List<Object[]> results = auditReader.createQuery()
                        .forRevisionsOfEntity(entityClass, false, true)
                        .add(AuditEntity.revisionNumber().eq(revNum))
                        .getResultList();

                for (Object[] row : results) {
                    Object entity = row[0];
                    RevisionType revType = (RevisionType) row[2];

                    Map<String, Object> data = entityToMap(entity);

                    entries.add(new AuditEntryDto(
                            typeName,
                            extractId(entity),
                            revType.name(),
                            data
                    ));
                }
            } catch (Exception e) {
                // Entity might not have changes in this revision
            }
        }

        return entries;
    }

    private Map<String, Object> entityToMap(Object entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (var field : entity.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object value = field.get(entity);
                if (value == null || value instanceof String || value instanceof Number
                        || value instanceof Boolean || value instanceof Enum
                        || value instanceof java.time.LocalDate || value instanceof java.math.BigDecimal
                        || value instanceof java.time.Instant) {
                    map.put(field.getName(), value != null ? value.toString() : null);
                }
            } catch (IllegalAccessException e) {
                // skip
            }
        }
        return map;
    }

    private Long extractId(Object entity) {
        try {
            var idField = entity.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            return (Long) idField.get(entity);
        } catch (Exception e) {
            return null;
        }
    }

    public List<String> getEntityTypes() {
        return new ArrayList<>(AUDITED_ENTITIES.keySet()).stream().sorted().toList();
    }

    @SuppressWarnings("unchecked")
    public List<String> getAuditUsers() {
        return entityManager.createQuery(
                        "SELECT DISTINCT r.userEmail FROM AuditRevisionEntity r WHERE r.userEmail IS NOT NULL ORDER BY r.userEmail")
                .getResultList();
    }
}