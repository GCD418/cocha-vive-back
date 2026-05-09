package cocha.vive.backend.model.mapper;

import cocha.vive.backend.model.Ticket;
import cocha.vive.backend.model.dto.TicketResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TicketMapper {

    @Mapping(target = "totalPrice", expression = "java(ticket.totalPrice())")
    @Mapping(target = "eventId", source = "event.id")
    @Mapping(target = "buyerUserId", source = "buyerUserId.id")
    TicketResponseDTO toResponseDto(Ticket ticket);

    List<TicketResponseDTO> toResponseDtoList(List<Ticket> tickets);
}
