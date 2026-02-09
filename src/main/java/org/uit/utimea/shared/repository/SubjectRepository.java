package org.uit.utimea.shared.repository;

import org.springframework.stereotype.Repository;
import org.uit.utimea.shared.data.SubjectYear;
import org.uit.utimea.shared.entity.Subject;

import java.util.List;

@Repository
public interface SubjectRepository extends BaseRepository<Subject> {
    List<Subject> findByIsFirstSem(Boolean isFirstSem);
    List<Subject> findByIsFirstSemAndSubjectYear(Boolean isFirstSem, SubjectYear subjectYear);
}
