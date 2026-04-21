package com.witteconsulting.cruddemo.repository;

import com.witteconsulting.cruddemo.entity.RoomEntity;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository extends CrudRepository<RoomEntity, UUID> {}
