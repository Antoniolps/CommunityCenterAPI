package br.com.antoniolps.CommunityCenterAPI.repository;

import br.com.antoniolps.CommunityCenterAPI.model.CommunityCenter;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommunityCenterRepository extends MongoRepository<CommunityCenter, UUID> {

    List<CommunityCenter> findByCurrentOccupancyGreaterThan(int occupancy);
}
