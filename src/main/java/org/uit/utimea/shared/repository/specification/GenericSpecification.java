package org.uit.utimea.shared.repository.specification;

import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GenericSpecification<T> {
    
    public Specification<T> getSpecification(Map<String, Object> keywordMap, List<String> fields) {
        return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            if (keywordMap == null || keywordMap.isEmpty() || fields.isEmpty()) {
                return null;
            }

            List<Predicate> predicates = new ArrayList<>();
            Map<String, Join<?, ?>> joins = new HashMap<>();

            for (String field : fields) {
                Object value = keywordMap.get(field);
                if (value == null) {
                    continue;
                }

                String[] parts = field.split("\\.");
                Path<?> path;

                if (parts.length == 1) {
                    path = root.get(parts[0]);
                } else {
                    Join<?, ?> join = joins.computeIfAbsent(parts[0], j -> root.join(j, JoinType.LEFT));
                    path = join.get(parts[1]);
                }

                if (value instanceof String stringValue) {
                    if (stringValue.trim().isEmpty()) {
                        continue;
                    }
                    String likePattern = "%" + stringValue.toLowerCase() + "%";
                    predicates.add(cb.like(cb.lower(path.as(String.class)), likePattern));
                } else {
                    predicates.add(cb.equal(path, value));
                }
            }

            return predicates.isEmpty() ? null : cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
