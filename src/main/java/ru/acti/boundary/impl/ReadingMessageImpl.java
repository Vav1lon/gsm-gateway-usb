package ru.acti.boundary.impl;

import org.springframework.stereotype.Component;
import ru.acti.boundary.ReadingMessage;
import ru.acti.entity.internal.IncomingSms;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/read/")
@Component("readingMessage")
@Produces(MediaType.APPLICATION_JSON)
public class ReadingMessageImpl implements ReadingMessage {

    @GET
    @Path("/{id}")
    @Override
    public IncomingSms readMessageById(@PathParam("id") Long id) {
        return null;
    }

    @GET
    @Path("/")
    @Override
    public List<IncomingSms> readNewMessage() {
        return null;
    }
}
