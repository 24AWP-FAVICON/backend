package com.example.demo.converter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class DtoConverter {

    /**
     * 엔티티 리스트를 DTO 리스트로 변환하는 메서드
     *
     * @param entityList 변환할 엔티티 리스트
     * @param converter  각 엔티티를 DTO로 변환하는 함수
     * @param <T>        엔티티 타입
     * @param <R>        DTO 타입
     * @return 변환된 DTO 리스트
     */
    public static <T, R> List<R> convertEntityListToDtoList(List<T> entityList, Function<T, R> converter) {
        List<R> dtoList = new ArrayList<>();
        entityList.forEach(item -> dtoList.add(converter.apply(item)));
        return dtoList;
    }
}