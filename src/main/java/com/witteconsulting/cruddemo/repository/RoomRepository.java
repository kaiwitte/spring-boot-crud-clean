package com.witteconsulting.cruddemo.repository;

import com.witteconsulting.cruddemo.entity.RoomEntity;
import java.util.UUID;
import org.springframework.data.repository.ListCrudRepository;

public interface RoomRepository extends ListCrudRepository<RoomEntity, UUID> {}
