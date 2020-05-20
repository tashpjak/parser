package RSP.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import RSP.dto.SortAttribute;
import RSP.dto.SortOrder;
import RSP.dto.TripsQueryCriteria;
import RSP.model.Trip;

class TripQueryBuilder {

    private final EntityManager entityManager;
    private final CriteriaBuilder criteriaBuilder;
    private final CriteriaQuery<Trip> criteriaQuery;
    private final Root<Trip> trips;

    private final List<Predicate> predicates = new ArrayList<>();
    private boolean needsDeduplication = false;

    TripQueryBuilder(EntityManager entityManager) {
        this.entityManager = entityManager;
        criteriaBuilder = entityManager.getCriteriaBuilder();
        criteriaQuery = criteriaBuilder.createQuery(Trip.class);
        trips = criteriaQuery.from(Trip.class);
    }

    TypedQuery<Trip> build(TripsQueryCriteria criteria) {
        filterByNumericInterval("price", criteria.getMinPrice(), criteria.getMaxPrice());
        filterByNumericInterval("length", criteria.getMinLength(), criteria.getMaxLength());
        filterByDateInterval("startDate", criteria.getStartAfter(), criteria.getStartBefore());
        filterByDateInterval("endDate", criteria.getEndAfter(), criteria.getEndBefore());
        filterBySubstring("name", criteria.getInName());
        filterByJoin("countries", criteria.getCountry(), criteria.getCountryAny());
        filterByJoin("tags", criteria.getTag(), criteria.getTagAny());

        List<Order> order = sort(criteria.getSortBy(), criteria.getOrder());
        return entityManager.createQuery(criteriaQuery
                .select(trips)
                .distinct(needsDeduplication)
                .where(predicates.toArray(new Predicate[predicates.size()]))
                .orderBy(order));
    }

    private void filterByNumericInterval(
            String columnName, Integer minValue, Integer maxValue) {

        if (minValue != null || maxValue != null) {
            Path<Integer> column = trips.get(columnName);
            if (minValue != null) {
                filter(criteriaBuilder.greaterThanOrEqualTo(column, minValue));
            }
            if (maxValue != null) {
                filter(criteriaBuilder.lessThanOrEqualTo(column, maxValue));
            }
        }
    }

    private void filterByDateInterval(String columnName, Date minValue, Date maxValue) {
        if (minValue != null || maxValue != null) {
            Path<Date> column = trips.get(columnName);
            if (minValue != null) {
                filter(criteriaBuilder.greaterThanOrEqualTo(column, minValue));
            }
            if (maxValue != null) {
                filter(criteriaBuilder.lessThanOrEqualTo(column, maxValue));
            }
        }
    }

    private void filterBySubstring(String columnName, String substring) {
        if (substring != null) {
            Path<String> column = trips.get(columnName);
            filter(criteriaBuilder.notEqual(
                criteriaBuilder.locate(criteriaBuilder.lower(column), substring), 0));
        }
    }

    private void filterByJoin(String columnName, List<String> names, boolean conjunction) {
        if (names != null) {
            if (!conjunction) {
                for (String name : names) {
                    filter(criteriaBuilder.equal(trips.join(columnName).get("name"), name));
                }
            } else {
                Path<String> column = trips.join(columnName).get("name");
                List<Predicate> choices = new ArrayList<>();

                for (String name : names) {
                    choices.add(criteriaBuilder.equal(column, name));
                }

                int count = choices.size();
                switch (count) {
                    case 0:
                        break;
                    case 1:
                        filter(choices.get(0));
                        break;
                    default:
                        filter(criteriaBuilder.or(choices.toArray(new Predicate[count])));
                        needsDeduplication = true;
                }
            }
        }
    }


    private void filter(Predicate predicate) {
        predicates.add(predicate);
    }

    private List<Order> sort(SortAttribute attribute, SortOrder order) {
        List<Order> orders = new ArrayList<>();
        orders.add(makeOrder(attribute, order));
        if (!attribute.isTotalOrdering()) {
            orders.add(makeOrder(SortAttribute.NAME, order));
        }
        return orders;
    }

    private Order makeOrder(SortAttribute attribute, SortOrder order) {
        Path<?> orderByColumn = trips.get(attribute.getColumnName());
        return (SortOrder.ASCENDING == order)
                ? criteriaBuilder.asc(orderByColumn) : criteriaBuilder.desc(orderByColumn);
    }
}