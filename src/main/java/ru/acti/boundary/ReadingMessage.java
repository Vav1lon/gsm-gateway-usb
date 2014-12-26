package ru.acti.boundary;

import org.springframework.transaction.annotation.Transactional;
import ru.acti.entity.internal.IncomingSms;

import javax.jws.WebService;
import java.util.List;

@WebService
@Transactional
public interface ReadingMessage {

    IncomingSms readMessageById(Long id);

    List<IncomingSms> readNewMessage();

}
