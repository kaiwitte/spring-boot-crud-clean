package com.witteconsulting.cruddemo.repository;

import com.witteconsulting.cruddemo.entity.RoomEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RoomRepository extends CrudRepository<RoomEntity, UUID> {
}
