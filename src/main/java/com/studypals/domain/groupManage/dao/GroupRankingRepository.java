package com.studypals.domain.groupManage.dao;

import org.springframework.stereotype.Repository;

import com.studypals.domain.groupManage.entity.GroupRanking;
import com.studypals.global.redis.redisHashRepository.RedisHashRepository;

@Repository
public interface GroupRankingRepository extends RedisHashRepository<GroupRanking, String> {}
