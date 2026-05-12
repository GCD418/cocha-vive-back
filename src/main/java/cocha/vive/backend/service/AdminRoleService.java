package cocha.vive.backend.service;
import cocha.vive.backend.exception.InvalidRoleTransitionException;
import cocha.vive.backend.exception.ResourceNotFoundException;
import cocha.vive.backend.model.User;
import cocha.vive.backend.model.dto.RoleChangeResponseDTO;
import cocha.vive.backend.repository.UserRepository;
import cocha.vive.backend.model.dto.PublisherDemotionDTO;
import cocha.vive.backend.service.PublisherRequestUserNotificationFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminRoleService {

    private static final String ROLE_USER = "ROLE_USER";
    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String ROLE_PUBLISHER = "ROLE_PUBLISHER";

    private final UserRepository userRepository;
    private final AuditService auditService;
    private final PublisherRequestUserNotificationFacade publisherRequestUserNotificationFacade;

    @Transactional
    public RoleChangeResponseDTO promoteToAdmin(Long targetUserId) {
        Long actorId = auditService.getActualUserId();
        log.info("Actor id: {} attempting to promote user id: {} to ROLE_ADMIN", actorId, targetUserId);

        User target = findActiveUser(targetUserId);

        if (!ROLE_USER.equals(target.getRole())) {
            throw new InvalidRoleTransitionException(
                "User id: " + targetUserId + " cannot be promoted. Current role: " + target.getRole()
            );
        }

        target.setRole(ROLE_ADMIN);
        target.setModifiedByUserId(actorId);
        userRepository.save(target);

        log.info("User id: {} promoted to ROLE_ADMIN by actor id: {}", targetUserId, actorId);
        return toResponse(target);
    }

    @Transactional
    public RoleChangeResponseDTO demoteToUser(Long targetUserId) {
        Long actorId = auditService.getActualUserId();
        log.info("Actor id: {} attempting to demote user id: {} to ROLE_USER", actorId, targetUserId);

        if (actorId.equals(targetUserId)) {
            throw new InvalidRoleTransitionException("Self-demotion is not allowed");
        }

        User target = findActiveUser(targetUserId);

        if (!ROLE_ADMIN.equals(target.getRole())) {
            throw new InvalidRoleTransitionException(
                "User id: " + targetUserId + " cannot be demoted. Current role: " + target.getRole()
            );
        }

        target.setRole(ROLE_USER);
        target.setModifiedByUserId(actorId);
        userRepository.save(target);

        log.info("User id: {} demoted to ROLE_USER by actor id: {}", targetUserId, actorId);
        return toResponse(target);
    }

    @Transactional
    public RoleChangeResponseDTO demotePublisherToUser(Long targetUserId, PublisherDemotionDTO dto) {
        Long actorId = auditService.getActualUserId();
        log.info("Actor id: {} attempting to demote publisher id: {} to ROLE_USER", actorId, targetUserId);

        if (actorId.equals(targetUserId)) {
            throw new InvalidRoleTransitionException("Self-demotion is not allowed");
        }

        User target = findActiveUser(targetUserId);

        if (!ROLE_PUBLISHER.equals(target.getRole())) {
            throw new InvalidRoleTransitionException(
                "User id: " + targetUserId + " cannot be demoted. Current role: " + target.getRole()
            );
        }

        target.setRole(ROLE_USER);
        target.setModifiedByUserId(actorId);
        userRepository.save(target);

        log.info("User id: {} demoted from ROLE_PUBLISHER to ROLE_USER by actor id: {}", targetUserId, actorId);

        publisherRequestUserNotificationFacade.notifyDemotedFromPublisher(target, dto.demotionReason());

        return toResponse(target);
    }

    private User findActiveUser(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    private RoleChangeResponseDTO toResponse(User user) {
        return new RoleChangeResponseDTO(user.getId(), user.getEmail(), user.getRole());
    }
}
