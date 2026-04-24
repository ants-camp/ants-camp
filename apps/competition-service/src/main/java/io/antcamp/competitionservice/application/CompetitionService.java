package io.antcamp.competitionservice.application;

import io.antcamp.competitionservice.application.dto.CreateCompetitionCommand;
import io.antcamp.competitionservice.application.dto.UpdateCompetitionCommand;
import io.antcamp.competitionservice.domain.model.Competition;
import io.antcamp.competitionservice.domain.model.CompetitionChangeNotice;
import io.antcamp.competitionservice.domain.model.CompetitionStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CompetitionService {

    Competition create(CreateCompetitionCommand command);

    Competition findById(UUID id);

    Page<Competition> findAll(Pageable pageable);

    Page<Competition> findAllByStatus(CompetitionStatus status, Pageable pageable);

    // 게시 (isReadable = true)
    Competition publish(UUID competitionId);

    // 대회 정보 수정
    Competition updateInfo(UpdateCompetitionCommand command);

    // 대회 취소
    Competition cancel(UUID competitionId);

    // 대회 삭제 (소프트 딜리트)
    void delete(UUID competitionId, String deletedBy);

    List<CompetitionChangeNotice> findChangeNotices(UUID competitionId);
}
