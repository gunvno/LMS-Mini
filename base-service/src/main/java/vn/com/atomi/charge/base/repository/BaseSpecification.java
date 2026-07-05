package vn.com.atomi.charge.base.repository;

import jakarta.annotation.Nonnull;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import vn.com.atomi.charge.base.model.entity.BaseEntity;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BaseSpecification<E extends BaseEntity> implements Specification<E> {

    protected final Map<String, String> filters;

    public boolean isFilterDelete = false;

    public boolean isFilterCreatedBy = false;

    private List<String> userNames;

    public BaseSpecification(Map<String, String> filters) {
        this.filters = filters;
    }

    @Override
    public Predicate toPredicate(@Nonnull Root<E> root, @Nonnull CriteriaQuery<?> query, @Nonnull CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();
        addConditions(predicates, root, query, cb);
        return cb.and(predicates.toArray(new Predicate[0]));
    }

    public void addConditions(List<Predicate> predicates, Root<E> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
      for (Map.Entry<String, String> entry : filters.entrySet()) {
        String key = entry.getKey();
        String value = entry.getValue();

        if (!hasField(root, key)) continue;

        Path<?> path = root.get(key);
        Class<?> type = path.getJavaType();

        if (!StringUtils.hasText(value) && type != Instant.class) {
          continue;
        }

        try {
          if (type == String.class) {
            if (key.toLowerCase().contains("id")) {
              if (value.length() >= 32) {
                predicates.add(cb.equal(root.get(key), value));
              } else {
                predicates.add(cb.like(cb.lower(root.get(key)), "%" + value.toLowerCase() + "%"));
              }
            } else if (key.toLowerCase().contains("status")) {
              if (StringUtils.hasText(value)) {
                CriteriaBuilder.In<String> filterStatus = cb.in(root.get(key));
                String[] listStatus = value.split("\\|");
                for (String item : listStatus) {
                  filterStatus.value(item);
                }
                predicates.add(filterStatus);
              }
            } else if (key.toLowerCase().contains("name") || key.toLowerCase().contains("code")) {
              predicates.add(cb.like(cb.lower(root.get(key)), "%" + value.toLowerCase() + "%"));
            } else {
              if (value.contains("|")) {
                CriteriaBuilder.In<String> filterList = cb.in(root.get(key));
                String[] listValue = value.split("\\|");
                for (String item : listValue) {
                  filterList.value(item);
                }
                predicates.add(filterList);
              } else {
                predicates.add(cb.equal(root.get(key), value));
              }
            }
          } else if (type == Integer.class) {
            predicates.add(cb.equal(root.get(key), Integer.parseInt(value)));
          } else if (type == Long.class) {
            predicates.add(cb.equal(root.get(key), Long.parseLong(value)));
          } else if (type == Boolean.class) {
            predicates.add(cb.equal(root.get(key), Boolean.parseBoolean(value)));
          } else if (type == LocalDate.class) {
            predicates.add(cb.equal(root.get(key), LocalDate.parse(value)));
          } else if (type == Instant.class) {
            if (StringUtils.hasText(value)) {
              predicates.add(cb.greaterThanOrEqualTo(root.get(key), Instant.parse(value)));
            } else {
              predicates.add(cb.isNull(root.get(key)));
            }
          } else {
            // fallback to equals
            predicates.add(cb.equal(root.get(key), value));
          }
        } catch (Exception e) {
          // Log and skip invalid type
          System.out.printf("Invalid value for key [%s]: %s%n", key, e.getMessage());
        }
      }
      if (isFilterDelete) {
        predicates.add(cb.isNotNull(root.get("deletedAt")));
      } else {
        predicates.add(cb.isNull(root.get("deletedAt")));
      }
      if (isFilterCreatedBy) {
        if (!this.userNames.isEmpty()) {
          CriteriaBuilder.In<String> inClause = cb.in(root.get("createdBy"));
          for (String user : userNames) {
            inClause.value(user);
          }
          predicates.add(inClause);
        }
      }
    }

    private boolean hasField(Root<E> root, String fieldName) {
        try {
            root.get(fieldName);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public void setFilterCreatedBy(List<String> usernames) {
        this.userNames = usernames;
    }
}
