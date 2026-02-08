package org.uit.utimea.shared.repository;

import org.springframework.stereotype.Repository;
import org.uit.utimea.shared.entity.TimetableInfo;

import java.util.List;

@Repository
public interface TimetableInfoRepository extends BaseRepository<TimetableInfo> {
    List<TimetableInfo> findByMajorSection_Id(Long majorSectionId);
}
