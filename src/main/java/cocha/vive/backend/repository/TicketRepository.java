package cocha.vive.backend.repository;

import cocha.vive.backend.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findAllByBuyerUserIdIdOrderByCreatedAtDesc(Long buyerUserId);
}
