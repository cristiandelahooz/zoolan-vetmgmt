package com.wornux.services;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final EntityManager entityManager;

    @Transactional
    public List<?> getRowChanges(Class<?> entityClass, Long entityId) {
        AuditReader auditReader = AuditReaderFactory.get(entityManager);

        return auditReader.createQuery().forRevisionsOfEntity(entityClass, false, true)
                .add(AuditEntity.id().eq(entityId)).getResultList();
    }

    @Transactional
    public Map<String, Object[]> getChangedProperties(Class<?> entityClass, Long entityId, Number revisionNumber) {
        AuditReader auditReader = AuditReaderFactory.get(entityManager);

        Object currentRevision = auditReader.find(entityClass, entityId, revisionNumber);
        Object previousRevision = auditReader.find(entityClass, entityId, revisionNumber.intValue() - 1);

        Map<String, Object[]> changes = new HashMap<>();

        if (currentRevision != null && previousRevision != null) {
            var fields = entityClass.getDeclaredFields();
            for (var field : fields) {
                field.setAccessible(true);
                try {
                    Object currentValue = field.get(currentRevision);
                    Object previousValue = field.get(previousRevision);

                    if (currentValue != null && !currentValue.equals(previousValue)) {
                        changes.put(field.getName(), new Object[] { previousValue, currentValue });
                    }
                } catch (IllegalAccessException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
        return changes;
    }

}
