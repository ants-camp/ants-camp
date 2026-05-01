package io.antcamp.competitionservice.application;

import common.exception.BusinessException;
import common.exception.ErrorCode;
import io.antcamp.competitionservice.application.dto.CreateCompetitionCommand;
import io.antcamp.competitionservice.application.dto.UpdateCompetitionCommand;
import io.antcamp.competitionservice.application.event.CompetitionEventProducer;
import io.antcamp.competitionservice.domain.event.CompetitionEndedEvent;
import io.antcamp.competitionservice.domain.model.Competition;
import io.antcamp.competitionservice.domain.model.CompetitionChangeNotice;
import io.antcamp.competitionservice.domain.model.CompetitionParticipant;
import io.antcamp.competitionservice.domain.model.CompetitionPeriod;
import io.antcamp.competitionservice.domain.model.CompetitionStatus;
import io.antcamp.competitionservice.domain.model.ParticipantCount;
import io.antcamp.competitionservice.domain.model.RegisterPeriod;
import io.antcamp.competitionservice.domain.repository.CompetitionChangeNoticeRepository;
import io.antcamp.competitionservice.domain.repository.CompetitionParticipantRepository;
import io.antcamp.competitionservice.domain.repository.CompetitionRepository;
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
    private final CompetitionParticipantRepository competitionParticipantRepository;
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

        // 도메인 상태 변경 (PREPARING -> ONGOING)
        // 계좌 생성은 대회 신청 시점에 이미 완료되므로 별도 이벤트 발행 없음
        competition.startCompetition();
        return competitionRepository.save(competition);
    }

    @Transactional
    @Override
    public Competition finish(UUID competitionId) {
        Competition competition = competitionRepository.findById(competitionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT));

        // 1. 도메인 상태 변경 (ONGOING -> FINISHED)
        competition.finishCompetition();
        Competition saved = competitionRepository.save(competition);

        // 2. 참가자 userId 목록 조회
        List<CompetitionParticipant> participants = competitionParticipantRepository.findAllByCompetitionId(competitionId);
        List<UUID> participantUserIds = participants.stream()
                .map(CompetitionParticipant::getUserId)
                .toList();

        // 3. 대회 종료 이벤트 발행 (자산 서비스가 컨슘 → 최종 총자산 계산 후 랭킹 서비스로 전달)
        CompetitionEndedEvent event = new CompetitionEndedEvent(
                saved.getCompetitionId(),
                participantUserIds,
                LocalDateTime.now()
        );
        competitionEventProducer.publishCompetitionEnded(event);

        return saved;
    }
}
