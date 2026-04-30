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

    // 대회 생성
    Competition create(CreateCompetitionCommand command);

    // 대회 단건 조회
    Competition findById(UUID id);

    // 대회 목록 조회
    Page<Competition> findAll(Pageable pageable);

    // 대회 상태별 목록 조회
    Page<Competition> findAllByStatus(CompetitionStatus status, Pageable pageable);

    // 게시 (isReadable = true)
    Competition publish(UUID competitionId);

    // 대회 정보 수정
    Competition updateInfo(UpdateCompetitionCommand command);

    // 대회 취소
    Competition cancel(UUID competitionId);

    // 대회 삭제 (소프트 딜리트)
    void delete(UUID competitionId, String deletedBy);

    // 대회 정보 변경 공지 조회
    List<CompetitionChangeNotice> findChangeNotices(UUID competitionId);

    // 대회 시작
    Competition start(UUID competitionId);

    // 대회 종료
    Competition finish(UUID competitionId);
}
