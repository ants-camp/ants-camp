package io.antcamp.competitionservice.application;

import common.exception.BusinessException;
import common.exception.ErrorCode;
import io.antcamp.competitionservice.application.dto.CreateCompetitionCommand;
import io.antcamp.competitionservice.application.dto.UpdateCompetitionCommand;
import io.antcamp.competitionservice.domain.event.CompetitionAbortedEvent;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompetitionServiceImpl implements CompetitionService {

    private final CompetitionRepository competitionRepository;
    private final CompetitionChangeNoticeRepository competitionChangeNoticeRepository;
    private final CompetitionParticipantRepository competitionParticipantRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    // ── Create ────────────────────────────────────────────────────────────────

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
        log.info(competition.toString());
        Competition saved = competitionRepository.save(competition);
        log.info("저장 완료");
        return saved;
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Competition findById(UUID id) {
        return competitionRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPETITION_NOT_FOUND));
    }

    // ── Update ────────────────────────────────────────────────────────────────

    // 사용자들이 대회를 조회 가능하도록 변경
    @Override
    @Transactional
    public Competition openCompetition(UUID competitionId) {
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

        // 공개된 대회만 변경 공지를 남긴다
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
    public Competition startCompetition(UUID competitionId) {
        Competition competition = competitionRepository.findById(competitionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPETITION_NOT_FOUND));

        // 계좌 생성은 대회 신청 시점에 이미 완료되므로 별도 이벤트 발행 없음
        competition.startCompetition();
        return competitionRepository.save(competition);
    }

    @Transactional
    @Override
    public Competition finishCompetition(UUID competitionId) {
        Competition competition = competitionRepository.findById(competitionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPETITION_NOT_FOUND));

        competition.finishCompetition();
        Competition saved = competitionRepository.save(competition);

        List<CompetitionParticipant> participants = competitionParticipantRepository.findAllByCompetitionId(
                competitionId);
        List<UUID> participantUserIds = participants.stream()
                .map(CompetitionParticipant::getUserId)
                .toList();

        // 자산 서비스가 컨슘 → 최종 총자산 계산 후 랭킹 서비스로 전달
        applicationEventPublisher.publishEvent(new CompetitionEndedEvent(
                saved.getCompetitionId(),
                participantUserIds,
                LocalDateTime.now()
        ));
        return saved;
    }

    @Override
    @Transactional
    public Competition cancelCompetition(UUID competitionId) {
        Competition competition = competitionRepository.findById(competitionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPETITION_NOT_FOUND));
        competition.cancelCompetition();
        Competition saved = competitionRepository.save(competition);

        List<UUID> participantUserIds = competitionParticipantRepository
                .findAllByCompetitionId(competitionId)
                .stream()
                .map(CompetitionParticipant::getUserId)
                .toList();

        // 참가자 계좌 정리를 위해 이벤트 발행
        CompetitionAbortedEvent event = new CompetitionAbortedEvent(saved.getCompetitionId(), participantUserIds);
        applicationEventPublisher.publishEvent(event);

        return saved;
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public Competition deleteCompetition(UUID competitionId, String deletedBy) {
        Competition competition = competitionRepository.findById(competitionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPETITION_NOT_FOUND));
        competitionRepository.delete(competition, deletedBy);
        return competition;
    }

    // ── Search ────────────────────────────────────────────────────────────────

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
    @Transactional(readOnly = true)
    public List<CompetitionChangeNotice> findChangeNotices(UUID competitionId) {
        competitionRepository.findById(competitionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPETITION_NOT_FOUND));
        return competitionChangeNoticeRepository.findAllByCompetitionId(competitionId);
    }
}
