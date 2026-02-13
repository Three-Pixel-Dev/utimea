package org.uit.utimea.features.dashboard.service.impl;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.uit.utimea.features.dashboard.dto.response.DashboardResponse;
import org.uit.utimea.features.dashboard.service.DashboardService;
import org.uit.utimea.shared.entity.Profile;
import org.uit.utimea.shared.repository.ProfileRepository;
import org.uit.utimea.shared.repository.RoomRepository;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final RoomRepository roomRepository;
    private final ProfileRepository profileRepository;

    @Override
    public DashboardResponse getCounts() {
        // Count rooms
        Long totalRooms = roomRepository.count();

        // Count teachers (must have degree or department)
        Specification<Profile> teacherSpec = (root, query, cb) -> {
            Predicate hasDegree = cb.isNotNull(root.get("degree"));
            Predicate hasDepartment = cb.isNotNull(root.get("department"));
            return cb.or(hasDegree, hasDepartment);
        };
        Long totalTeachers = profileRepository.count(teacherSpec);

        // Count students (must have batch or majorSection)
        Specification<Profile> studentSpec = (root, query, cb) -> {
            Predicate hasBatch = cb.isNotNull(root.get("batch"));
            Predicate hasMajorSection = cb.isNotNull(root.get("majorSection"));
            return cb.or(hasBatch, hasMajorSection);
        };
        Long totalStudents = profileRepository.count(studentSpec);

        return DashboardResponse.builder()
                .totalRooms(totalRooms)
                .totalTeachers(totalTeachers)
                .totalStudents(totalStudents)
                .build();
    }
}
