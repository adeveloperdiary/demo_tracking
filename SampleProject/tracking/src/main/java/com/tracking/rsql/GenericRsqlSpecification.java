package com.tracking.rsql;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;

import cz.jirutka.rsql.parser.ast.ComparisonOperator;

/**
 * GenericRsqlSpecification class for creating the Specification of rsql parser.
 *
 * @version 1.0
 * @dete 05-18-2020
 **/

public class GenericRsqlSpecification<T> implements Specification<T> {

    private String property;
    private ComparisonOperator operator;
    private List<String> arguments;

    public GenericRsqlSpecification(final String property, final ComparisonOperator operator, final List<String> arguments) {
        super();
        this.property = property;
        this.operator = operator;
        this.arguments = arguments;
    }

    /**
     * This method needs to be updated from default implementation as new type checking were added for Date and other
     * other data types.
     *
     * @param root
     * @return List<Object>
     */
    @Override
    public Predicate toPredicate(final Root<T> root, final CriteriaQuery<?> query, final CriteriaBuilder builder) {
        final List<Object> args = castArguments(root);
        Object argument = args.get(0);

        switch (RsqlSearchOperation.getSimpleOperator(operator)) {

            case EQUAL: {
                if (argument instanceof String) {
                    return builder.like(root.get(property), argument.toString().replace('*', '%'));
                } else if (argument == null) {
                    return builder.isNull(root.get(property));
                } else {
                    return builder.equal(root.get(property), argument);
                }
            }
            case NOT_EQUAL: {
                if (argument instanceof String) {
                    return builder.notLike(root.<String>get(property), argument.toString().replace('*', '%'));
                } else if (argument == null) {
                    return builder.isNotNull(root.get(property));
                } else {
                    return builder.notEqual(root.get(property), argument);
                }
            }
            case GREATER_THAN: {
                /* Add additional condition for LocalDate and LocalTime */
                if (argument instanceof String) {
                    return builder.greaterThan(root.<String>get(property), argument.toString());
                } else if (argument instanceof LocalDate) {
                    return builder.greaterThan(root.<LocalDate>get(property), (LocalDate) argument);
                } else if (argument instanceof LocalTime) {
                    return builder.greaterThan(root.<LocalTime>get(property), (LocalTime) argument);
                } else {
                    return builder.greaterThan(root.<String>get(property), argument.toString());
                }
            }
            case GREATER_THAN_OR_EQUAL: {
                /* Add additional condition for LocalDate and LocalTime */
                if (argument instanceof String) {
                    return builder.greaterThanOrEqualTo(root.<String>get(property), argument.toString());
                } else if (argument instanceof LocalDate) {
                    return builder.greaterThanOrEqualTo(root.<LocalDate>get(property), (LocalDate) argument);
                } else if (argument instanceof LocalTime) {
                    return builder.greaterThanOrEqualTo(root.<LocalTime>get(property), (LocalTime) argument);
                } else {
                    return builder.greaterThanOrEqualTo(root.<String>get(property), argument.toString());
                }

            }
            case LESS_THAN: {
                /* Add additional condition for LocalDate and LocalTime */
                if (argument instanceof String) {
                    return builder.lessThan(root.<String>get(property), argument.toString());
                } else if (argument instanceof LocalDate) {
                    return builder.lessThan(root.<LocalDate>get(property), (LocalDate) argument);
                } else if (argument instanceof LocalTime) {
                    return builder.lessThan(root.<LocalTime>get(property), (LocalTime) argument);
                } else {
                    return builder.lessThan(root.<String>get(property), argument.toString());
                }

            }
            case LESS_THAN_OR_EQUAL: {
                /* Add additional condition for LocalDate and LocalTime */
                if (argument instanceof String) {
                    return builder.lessThanOrEqualTo(root.<String>get(property), argument.toString());
                } else if (argument instanceof LocalDate) {
                    return builder.lessThanOrEqualTo(root.<LocalDate>get(property), (LocalDate) argument);
                } else if (argument instanceof LocalTime) {
                    return builder.lessThanOrEqualTo(root.<LocalTime>get(property), (LocalTime) argument);
                } else {
                    return builder.lessThanOrEqualTo(root.<String>get(property), argument.toString());
                }
            }
            case IN:
                return root.get(property).in(args);
            case NOT_IN:
                return builder.not(root.get(property).in(args));
        }

        return null;
    }

    /**
     * This method needs to be updated from default implementation as new type checking were added for Date and other
     * other data types.
     *
     * @param root
     * @return List<Object>
     */
    private List<Object> castArguments(final Root<T> root) {

        final Class<? extends Object> type = root.get(property).getJavaType();

        /* Add the additional type checking for this implementation */
        final List<Object> args = arguments.stream().map(arg -> {
            if (type.equals(Integer.class)) {
                return Integer.parseInt(arg);
            } else if (type.equals(Long.class)) {
                return Long.parseLong(arg);
            }
            if (type.equals(LocalDate.class)) {
                return LocalDate.parse(arg);
            }
            if (type.equals(LocalTime.class)) {
                return LocalTime.parse(arg);
            }
            if (type.equals(Double.class)) {
                return Double.parseDouble(arg);
            } else {
                return arg;
            }
        }).collect(Collectors.toList());

        return args;
    }

}