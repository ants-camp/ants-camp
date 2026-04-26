package io.antcamp.competitionservice.application;

import common.exception.BusinessException;
import common.exception.ErrorCode;
import io.antcamp.competitionservice.application.dto.CreateCompetitionCommand;
import io.antcamp.competitionservice.application.dto.UpdateCompetitionCommand;
import io.antcamp.competitionservice.application.event.CompetitionEventProducer;
import io.antcamp.competitionservice.domain.event.CompetitionStartedPayload;
import io.antcamp.competitionservice.domain.model.Competition;
import io.antcamp.competitionservice.domain.model.CompetitionChangeNotice;
import io.antcamp.competitionservice.domain.model.CompetitionPeriod;
import io.antcamp.competitionservice.domain.model.CompetitionStatus;
import io.antcamp.competitionservice.domain.model.JoinHistory;
import io.antcamp.competitionservice.domain.model.ParticipantCount;
import io.antcamp.competitionservice.domain.model.RegisterPeriod;
import io.antcamp.competitionservice.domain.repository.CompetitionChangeNoticeRepository;
import io.antcamp.competitionservice.domain.repository.CompetitionRepository;
import io.antcamp.competitionservice.domain.repository.JoinHistoryRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CompetitionServiceImpl implements CompetitionService {

    private final CompetitionRepository competitionRepository;
    private final CompetitionChangeNoticeRepository competitionChangeNoticeRepository;
    private final JoinHistoryRepository joinHistoryRepository;
    private final CompetitionEventProducer competitionEventProducer;


    @Override
    @Transactional
    public Competition create(CreateCompetitionCommand command) {
        Competition competition = Competition.createCompetition(
                command.name(),
                command.type(),
                command.description(),
                command.firstSeed(),
                RegisterPeriod.of(command.registerStartAt(), command.registerEndAt()),
                CompetitionPeriod.of(command.competitionStartAt(), command.competitionEndAt()),
                ParticipantCount.of(command.minParticipants(), command.maxParticipants())
        );
        return competitionRepository.save(competition);
    }

    @Override
    @Transactional(readOnly = true)
    public Competition findById(UUID id) {
        return competitionRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPETITION_NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Competition> findAll(Pageable pageable) {
        return competitionRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Competition> findAllByStatus(CompetitionStatus status, Pageable pageable) {
        return competitionRepository.findAllByCompetitionStatus(status, pageable);
    }

    @Override
    @Transactional
    public Competition publish(UUID competitionId) {
        Competition competition = competitionRepository.findById(competitionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPETITION_NOT_FOUND));
        competition.publish();
        return competitionRepository.save(competition);
    }

    @Override
    @Transactional
    public Competition updateInfo(UpdateCompetitionCommand command) {
        Competition competition = competitionRepository.findById(command.competitionId())
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPETITION_NOT_FOUND));

        competition.updateInfo(
                command.name(),
                command.description(),
                RegisterPeriod.of(command.registerStartAt(), command.registerEndAt()),
                CompetitionPeriod.of(command.competitionStartAt(), command.competitionEndAt()),
                ParticipantCount.of(command.minParticipants(), command.maxParticipants())
        );

        // isReadable = true인 경우 변경 공지 저장
        if (competition.isReadable()) {
            CompetitionChangeNotice notice = CompetitionChangeNotice.create(
                    competition.getCompetitionId(),
                    competition.getStatus(),
                    command.beforeContents(),
                    command.afterContents(),
                    command.reason()
            );
            competitionChangeNoticeRepository.save(notice);
        }

        return competitionRepository.save(competition);
    }

    @Override
    @Transactional
    public Competition cancel(UUID competitionId) {
        Competition competition = competitionRepository.findById(competitionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPETITION_NOT_FOUND));
        competition.cancelCompetition();
        return competitionRepository.save(competition);
    }

    @Override
    @Transactional
    public void delete(UUID competitionId, String deletedBy) {
        Competition competition = competitionRepository.findById(competitionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPETITION_NOT_FOUND));
        competitionRepository.delete(competition, deletedBy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompetitionChangeNotice> findChangeNotices(UUID competitionId) {
        competitionRepository.findById(competitionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPETITION_NOT_FOUND));
        return competitionChangeNoticeRepository.findAllByCompetitionId(competitionId);
    }

    @Override
    @Transactional
    public Competition start(UUID competitionId) {
        Competition competition = competitionRepository.findById(competitionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT));

        // 1. 도메인 상태 변경 (PREPARING -> ONGOING)
        competition.startCompetition();
        Competition saved = competitionRepository.save(competition);

        // 2. 참가자 목록 조회
        List<JoinHistory> joinHistories = joinHistoryRepository.findAllByCompetitionId(competitionId);
        List<CompetitionStartedPayload.Participant> participants = joinHistories.stream()
                .map(jh -> new CompetitionStartedPayload.Participant(jh.getUserId(), jh.getNickname()))
                .toList();

        // 3. 이벤트 발행 (계좌 서비스가 컨슘 -> 참가자별 대회 계좌 생성)
        CompetitionStartedPayload payload = new CompetitionStartedPayload(
                saved.getCompetitionId(),
                saved.getName(),
                saved.getFirstSeed(),
                participants,
                LocalDateTime.now()
        );
        competitionEventProducer.publishCompetitionStarted(payload);

        return saved;
    }

    @Transactional
    @Override
    public Competition finish(UUID competitionId) {
        Competition competition = competitionRepository.findById(competitionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT));
        competition.finishCompetition();
        return competitionRepository.save(competition);
    }
}
